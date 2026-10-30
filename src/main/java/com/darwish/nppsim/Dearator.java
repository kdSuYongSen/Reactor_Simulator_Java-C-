package com.darwish.nppsim;

public class Dearator extends WaterSteamComponent implements Connectable, UIReadable, SteamSupplyLimit {
    final SteamValve steamInlet, steamOutlet;
    /**
     * 蒸汽入口阀 Cv。全厂蒸汽改走除氧器后每台要通 ~550 kg/s：
     * 入口压差 = 母管(0.70) − 除氧器(0.63) = 70 kPa（非击穿区），
     * flow ≈ 2.73·Cv·0.95·√(Δp/ρ) → Cv≈1.4e5 时约 550 kg/s。
     * 旧值 244（注释"48.61 for 10 kg/s"）只够 ~10 kg/s 的加热旁路。 */
    private static final double STEAM_INLET_CV = 1.3e4;
    /** 蒸汽出口阀 Cv：除氧器(0.63) → 联箱(0.61)，Δp=20 kPa，按 550 kg/s 定径 */
    private static final double STEAM_OUTLET_CV = 8.0e3;
    /** 除氧器自身压力设定值（MPa）：抽汽式除氧器由抽汽压力决定，不再跟随主蒸汽母管 */
    private static final double DEAERATOR_PRESSURE_SETPOINT = 0.53;
    /** 除氧器相对蒸汽母管的阻降（MPa，旧接线用，保留兼容） */
    private static final double DEAERATOR_PRESSURE_DROP_MPA = 0.07;
    /** 除氧器压力慢跟踪时间常数 s（越大越能拉开阀门驱动压差、实现自限流） */
    private static final double DEAERATOR_PRESSURE_LAG_S = 5.0;
    /** 汽空间缓冲上限（kg）：正常只存一个时间步的透传量，超过按安全阀排放处理 */
    private static final double STEAM_SPACE_MAX_KG = 200.0;
    private final double volume = 340.0;        /* m³（原 193，二回路全厂蒸汽改走除氧器后不够用） */
    private final double nominalWaterVolume;
    private double initialSteamMass; // this would be air in reality
    private double steamMass = 0.0; // kg
    private double waterMass = 320000.0; // 初始水量 kg（≈额定容积，340 m³ @ ~100C）
    private double thermalLoss = 0.0; // TODO conductive energy loss Mwt

    // calculated results from the update thread stored here for use in other
    // functions:
    private double steamDensity;
    private double specificHeatWater = NPPMath.calculateSpecificHeatWater(waterTemperature);
    private double specificVaporEnthalpy = NPPSim.tables.getSpecificVaporEnthalpyByTemperature(waterTemperature);// NPPSim.calculateSpecificVaporEnthalpy(waterTemperature);
    private double specificDensityWater = NPPSim.tables.getWaterDensityByTemp(waterTemperature);
    private double waterVolume;
    private double steamVolume;
    private double deltaEnergy = 0.0, deltaHeatingEnergy = 0.0; // surplus/deficit energy from last time ste
    private double waterInflowTemperature = 20.0; // c

    public double deltaSteamMass; // debug
    /** 本 tick 实际透传给汽轮机的蒸汽（kg/s，界面/验证用） */
    private double actualPassKgs = 0.0;

    /** 旧接线（主蒸汽全过除氧器）：仅给验证台/旧存档兼容用。 */
    public Dearator(int tgIndex) {
        this(tgIndex, NPPSim.steamPiping, NPPSim.deaeratorSteamHeader);
    }

    /**
     * 抽汽式除氧器（2026-10-19）：
     *   steamSource = 汽轮机中压缸抽汽口（0.60 MPa），不是主蒸汽母管；
     *   steamDrain  = 凝汽器（只在汽空间超过缓冲上限时排放余汽）。
     * 主蒸汽现在直供汽轮机，不再经过除氧器。
     */
    public Dearator(int tgIndex, Connectable steamSource, Connectable steamDrain) {
        steamInlet = new SteamValve(STEAM_INLET_CV, 24, steamSource, this);
        steamOutlet = new SteamValve(STEAM_OUTLET_CV, 48, this, steamDrain);
        waterVolume = waterMass * specificDensityWater;
        steamVolume = volume - waterVolume;
        nominalWaterVolume = waterVolume;
        initialSteamMass = steamVolume / 1.6718; // constant density for 20c
        steamDensity = steamVolume / (steamMass + initialSteamMass);
        specificDensityWater = NPPSim.tables.getWaterDensityByTemp(20);
    }

    /**
     * 除氧器更新（2026-10-17 改写汽侧）。
     *
     * 改版后**全厂蒸汽都要经过除氧器**（汽水分离器 → 母管 → 除氧器 → 除氧器蒸汽出口联箱
     * → 汽轮机/旁路），每台要通 ~550 kg/s。原汽侧模型是按“少量蒸汽加热给水”标定的
     * （入口阀 48.61 Cv ≈ 10 kg/s，见旧注释），其"汽空间存量 + 密度反查压力 + 空气置换"
     * 那套逻辑在大流量下会自激：存量涨到查表范围外 → 压力 NaN → 沿环路扩散，实测
     * 4 台除氧器在 30 s 内全部 NaN，蒸汽环路直接停摆。
     *
     * 现在改成直通式（物理上更贴近除氧器的真实角色）：
     *   1. 进汽先凝结，把汽化潜热+显热交给给水（除氧器的本职，给水升温/除氧）；
     *   2. 汽空间只保留很小的缓冲存量，余汽由出口阀透传到汽轮机；
     *   3. 压力跟随蒸汽母管（除氧器本体就是个汽压容器，其压力就是主蒸汽压力减阻力），
     *      不再用密反查，避免大流量下的数值自激。
     * 水侧（凝结水进、给水出、水位）维持原有质量平衡。
     */
    public void update() {
        sanitize();

        // ---- 压力：除氧器是带压力控制的低压汽压容器，设定值 = 母管压力 - 阻降（0.07 MPa）。
        // 关键（2026-10-18）：必须**慢跟踪**，不能瞬时等于母管压降。
        // 瞬时跟踪时，蒸汽母管（分离器）压力怎么跌，除氧器压力就跟着跌，入口阀压差恒为
        // 0.07 MPa → 4 台除氧器入口阀永远按定值全力抢汽（实测 4 台里只有 2~3 台进汽，
        // 且分离器汽空间泄空也无法让阀门减流）。慢跟踪后，母管侧的压力变化才能转成
        // 阀门的真实驱动压差，实现自限流。
        double linePressure = 0.70;
        if (NPPSim.steamPiping != null) {
            linePressure = NPPSim.steamPiping.getPressure();
        }
        /* 抽汽式：压力由抽汽压力与自身调压决定，取定值（0.53 MPa ≈ 154 °C）。
         * 旧接线是"母管压力 − 0.07"，母管一提压除氧器就跟着涨，给水温度失控。 */
        double pressureTarget = Math.min(DEAERATOR_PRESSURE_SETPOINT,
                Math.max(0.10, linePressure - DEAERATOR_PRESSURE_DROP_MPA));
        pressure += (pressureTarget - pressure) * Math.min(1.0, 0.05 / DEAERATOR_PRESSURE_LAG_S);
        double tsat = NPPSim.tables.getSteamTemperatureByPressure(pressure);

        specificHeatWater = NPPMath.calculateSpecificHeatWater(waterTemperature);

        // ---- 汽侧：进汽凝结、把**本 tick 流过的给水**加热到饱和温度，余汽透传给汽轮机 ----
        // 本 tick 进汽已由入口阀在 updateSteamInflow 里并入 steamMass，这里只做分配。
        //
        // 关键口径（2026-10-17 修正）：加热对象是**给水流量**，不是除氧器的整箱存水。
        // 除氧器是个"流过式"加热器：存水只是缓冲，真正被加热的是每秒流过的那几百 kg。
        // 原先按整箱 124 t 算需热量，一 tick 就要凝结 6.7e6 kg —— 结果所有进汽都被吞进
        // 水里，透传给汽轮机的汽变成 0，汽轮机直接停摆。
        // 真实除氧器的抽汽量也就是给水流量的 5~8%，和这个口径一致。
        double hfg = Math.max(1.0, NPPSim.tables.getSpecificVaporEnthalpyByTemperature(tsat)); // h_fg@Tsat
        double inflowKgs = Math.max(0.0, waterInflow);
        double heatNeeded = inflowKgs * specificHeatWater * Math.max(0.0, tsat - waterInflowTemperature);
        double wantCondense = heatNeeded / hfg;
        double condense = Math.min(Math.max(0.0, steamMass), Math.max(0.0, wantCondense));

        // 凝结放热 = 潜热 + 饱和水与进水的显热差；把本 tick 的进水加热（最多到饱和）后再入箱
        double inflowTemp = waterInflowTemperature;
        if (condense > 0.0 && inflowKgs > 1e-9) {
            /* 进汽是 tsat 下的**饱和汽**，凝结成 tsat 下的饱和水，只放汽化潜热 hfg；
             * 旧式再加一项 cp·(tsat−Tin) 是把进水的显热重复算了一次 —— 每 kg 进汽
             * 多给约 400 kJ，给水会被"免费"加热（实测能把 ΔT 拉高 ~30%）。 */
            double releasePerKg = hfg;
            double heatedTemp = waterInflowTemperature
                    + releasePerKg * condense / (specificHeatWater * inflowKgs);
            inflowTemp = Math.min(tsat, heatedTemp);
            steamMass -= condense;
            deltaEnergy += releasePerKg * condense;
        }

        // ---- 水侧：加热后的凝结水进来 / 给水泵抽走 ----
        double[] waterInflowData = NPPMath.mixWater(waterMass, waterTemperature, inflowKgs, inflowTemp);
        waterTemperature = waterInflowData[1];
        waterMass = waterInflowData[0];
        /* 给水泵只能抽走实际存在的水（2026-10-18 守恒修正）。
         * 旧实现直接 waterMass -= waterOutflow，抽超了会变成负质量，
         * 下一步 sanitize() 又把负值钳回 100 kg —— 质量凭空消失，
         * 表现为"给水莫名损失水"（除氧器 1 号被抽干、2~4 号又排溢出）。 */
        double draw = Math.min(Math.max(0.0, waterOutflow), Math.max(0.0, waterMass));
        waterMass -= draw;
        waterMass += condense;   // 凝结下来的汽变成水（质量守恒）

        /* 无水 → 明确空态（2026-10-18）。
         * 旧行为：质量被钳到 100 kg 后仍沿用上一次的水温/压力，
         * 于是"空除氧器"继续以 0.63 MPa/150C 向下游供水，界面读数也不合理。 */
        if (waterMass <= 1.0) {
            waterMass = 0.0;
            waterLevel = 0.0;
            waterTemperature = 20.0;
            pressure = 0.10142;
            steamMass = 0.0;
            steamTemperature = 20.0;
            resetFlows();
            return;
        }
        // 给水不可能超过饱和温度（超过就闪蒸）
        if (waterTemperature > tsat) {
            waterTemperature = tsat;
        }

        // ---- 余汽：不再向汽轮机透传 ----
        // 主蒸汽已经是"直供汽轮机 + 抽汽口加热"的两路结构，除氧器只承担抽汽的凝结。
        // 抽汽量大于凝结需求时（低负荷、给水已经饱和），余汽经出口阀排往凝汽器，
        // 质量仍然守恒（凝结水回到热井），不会凭空消失。
        double over = Math.max(0.0, steamMass - STEAM_SPACE_MAX_KG);
        if (over > 0.0) {
            steamMass -= over;
            steamOutlet.drain.updateSteamInflow(over, tsat);
        }
        actualPassKgs = over * 20.0;   // 界面口径：本 tick 排往凝汽器的余汽

        /* 进汽阀限位（2026-10-19）：除氧器应按需进汽。
         * 入口阀是按压差开的，全开时会抽走远超加热需要的汽（实测 553 kg/s，
         * 而 36.5→153 °C 只需要 ~230 kg/s），多出来的汽无处可去，只能从出口
         * 溢到凝汽器（实测 71 kg/s）—— 既白抽了汽，又把抽汽段的功丢掉了。
         * 这里只在"已经溢汽"或"明显欠热"两个极端上拨动阀位（±1%/tick），
         * 中间区间不动，手动操作照旧有效。 */
        double pos = steamInlet.getPosition();
        if (over > 0.05) {                      // kg/tick（1 kg/s），溢汽了 → 关小
            steamInlet.setPosition((float) Math.max(0.0, pos - 0.010));
        } else if (steamMass < STEAM_SPACE_MAX_KG * 0.5 && waterTemperature < tsat - 2.0) {
            steamInlet.setPosition((float) Math.min(1.0, pos + 0.008));   // 欠热 → 开大
        }

        steamTemperature = tsat;

        // ---- 水位 / 汽空间 ----
        specificDensityWater = NPPSim.tables.getWaterDensityByTemp(waterTemperature);
        waterVolume = waterMass * specificDensityWater;
        steamVolume = Math.max(0.0, volume - waterVolume);
        waterLevel = (waterVolume / nominalWaterVolume - 1) * 100;
        steamDensity = steamVolume / Math.max(1.0, steamMass);
        resetFlows();
    }


    @Override
    public double getSteamDensity() {
        // 给阀门流量公式用：必须是"比容 m³/kg"（SteamValve 内部取 1/getSteamDensity 当作密度）。
        // 直接按压力查蒸汽表，而不是用汽空间存量换算——除氧器汽空间是流经式的，
        // 存量很小，按存量算会让出口阀几乎没有通流能力。
        return NPPSim.tables.getSteamDensityByPressure(Math.max(0.05, pressure));
    }

    /** 汽空间存量：本 tick 最多能透传出去的蒸汽 */
    @Override public double availableSteamKg() {
        return Math.max(0.0, steamMass);
    }

    /** 本 tick 实际透传给汽轮机的蒸汽（kg/s） */
    double getPassKgs() {
        return actualPassKgs;
    }

    /** 是否已抽空（无水量）——界面据此显示"空态"而不是陈旧读数 */
    public boolean isEmpty() {
        return waterMass <= 1.0;
    }

    /**
     * 数值越界保护（2026-10-17 新增）。
     * 全厂蒸汽改走除氧器后，除氧器蒸汽空间承受全部流量；一旦某个 tick 的存量/温度
     * 冲出蒸汽表范围，压力查表会返回 NaN 并沿环路扩散（曾经一次 19900 kg/s 的
     * 阀门误定径就把 4 台除氧器全部打成 NaN，整条蒸汽环路停摆）。这里在每步开头
     * 把状态夹回物理范围，越界时按"除氧器饱和压力 + 水温 150 °C"复位。
     */
    private void sanitize() {
        double steamVol = Math.max(1.0, volume - waterMass * NPPSim.tables.getWaterDensityByTemp(Math.max(1.0, waterTemperature)));
        double maxSteam = steamVol * 20.0;      // 20 kg/m³：远超任何工作压力，留足瞬态余量
        if (Double.isNaN(steamMass) || steamMass < 0.0 || steamMass > maxSteam) {
            steamMass = Math.max(0.0, steamVol * 2.35);
        }
        if (Double.isNaN(initialSteamMass)) {
            initialSteamMass = 0.0;
        }
        if (Double.isNaN(steamTemperature) || steamTemperature < 20.0 || steamTemperature > 400.0) {
            steamTemperature = Math.max(20.0, Math.min(400.0, waterTemperature));
        }
        if (Double.isNaN(waterTemperature) || waterTemperature < 1.0 || waterTemperature > 350.0) {
            waterTemperature = 150.0;
        }
        // 水位不能把汽空间挤没：waterVolume ≥ volume 会让 steamVolume 变负 → 密度查表 NaN。
        // 注意 getWaterDensityByTemp 返回的是**比容 m³/kg**（与构造函数 waterVolume =
        // waterMass × specificDensityWater 的口径一致），所以这里要除而不是乘。
        double rho = NPPSim.tables.getWaterDensityByTemp(Math.max(1.0, waterTemperature));
        double maxWater = 0.95 * volume / Math.max(1e-6, rho);
        if (Double.isNaN(waterMass) || waterMass < 0.0 || waterMass > maxWater) {
            waterMass = Math.min(maxWater, 320000.0);
        }
        if (Double.isNaN(pressure) || pressure < 0.0 || pressure > 9.0) {
            pressure = 0.70;
        }
        if (Double.isNaN(waterInflow)) waterInflow = 0.0;
        if (Double.isNaN(steamInflow)) steamInflow = 0.0;
        if (Double.isNaN(waterOutflow)) waterOutflow = 0.0;
        if (Double.isNaN(steamOutflow)) steamOutflow = 0.0;
        if (Double.isNaN(deltaEnergy)) deltaEnergy = 0.0;
        if (Double.isNaN(deltaHeatingEnergy)) deltaHeatingEnergy = 0.0;
        if (Double.isNaN(specificHeatWater) || specificHeatWater <= 0.0) specificHeatWater = 4200.0;
    }

    @Override
    public double getWaterDensity() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getSteamMass() {
        return steamMass;
    }

    @Override
    public double getSteamVolume() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getWaterMass() {
        return waterMass;
    }

    public void setWaterTemp(double tempC) {
        waterTemperature = tempC;
        waterMass = nominalWaterVolume / NPPSim.tables.getWaterDensityByTemp(tempC);
    }
    
    @Override
    public void updateSteamOutflow(double flow, double tempC) {
        steamOutflow = flow;
    }

    @Override
    public void updateSteamInflow(double flow, double tempC) {
        Double[] inflowData = NPPMath.mixSteam(steamMass, steamTemperature, flow, tempC);
        steamMass = inflowData[0];
        steamTemperature = inflowData[1];
        steamInflow = flow;
        
    }

    @Override
    public void updateWaterOutflow(double flow, double tempC) {
        waterOutflow = flow;
    }

    @Override
    public void updateWaterInflow(double flow, double tempC) {
        double[] waterInflowData = NPPMath.mixWater(waterInflow, waterInflowTemperature, flow, tempC);
        waterInflow = waterInflowData[0];
        waterInflowTemperature = waterInflowData[1];
    }
}
