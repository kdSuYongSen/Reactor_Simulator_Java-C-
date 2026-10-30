package com.darwish.nppsim;

import java.io.Serializable;

/**
 * 除氧器蒸汽出口联箱 —— 主蒸汽母管的下游段。
 *
 * 环路（2026-10-17 改版）：
 *   给水 → 换热器 → 汽水分离器 → 蒸汽母管(steamPiping) → 除氧器蒸汽入口
 *        → 除氧器 → **本联箱** → 汽轮机主汽门 / 旁路膨胀阀 / 抽气器 → 凝汽器
 *        → 凝结水泵 → 凝结水联箱 → PCS 凝结水过滤 → 除氧器水入口 → 除氧器水出口 → 给水
 *
 * 与上游 steamPiping 的区别：
 *   - steamPiping 是"按压力比例向各源抽取"的需求侧母管（源是汽水分离器适配器）；
 *   - 本联箱是被动汇管：蒸汽由各除氧器的出口阀送进来（updateSteamInflow），
 *     汽轮机/旁路/抽气器向下游登记取用（updateSteamOutflow）。不做二次分配，
 *     避免和除氧器出口阀对除氧器重复计量蒸汽流出。
 *   - 压力取源压力减去管线压降（母管阻力），这样除氧器出口阀有驱动压差。
 *
 * 刻意不继承 Component（与 HeatExchanger / Separator 同理）：由 {@link PlantState} 直接持有，
 * 读档时从除氧器蒸汽出口阀的引用重接，不再依赖旧版的硬编码索引。
 */
class SteamCollectorHeader implements Connectable, SteamSupplyLimit, Serializable {
    private static final long serialVersionUID = 1L;

    /** 母管阻力压降（MPa）：除氧器 → 汽轮机主蒸汽管线的温压降 */
    private static final double LINE_LOSS_MPA = 0.020;

    Connectable[] sources = new Connectable[0];

    private double pressure = 0.70;
    private double steamTemperature = 165.0;
    private double steamDensity = 0.4;   // update() 里按压力刷新（构造期蒸汽表可能还没就绪）
    private double steamMass = 0.0;
    private double demand = 0.0;          // 下游登记的取用（kg/tick）
    private double drawnThisTick = 0.0;   // 已被下游阀门领走的量（多个阀共享同一批汽）
    private double inflowThisTick = 0.0;  // 本 tick 实际送入的汽（kg），用于流量显示与总量
    private double inflowRate = 0.0;      // 本 tick 进汽 kg/s（显示用）
    private double outflowRate = 0.0;     // 本 tick 出汽 kg/s（显示用）
    private double totalSteamKg = 0.0;    // 累计通过量 kg
    private double reliefKgs = 0.0;       // 安全阀排放 kg/s（下游关闭时的出路，诊断用）
    /** 汇管缓冲上限 kg：正常一 tick 的流量级别；超过它靠**压力升高**让上游阀自己减流，
     *  只有超过硬上限才走安全阀排放（旧实现把超出缓冲的汽直接丢掉 = 凭空消灭质量）。 */
    private static final double MAX_BUFFER_KG = 500.0;
    /** 安全阀硬上限 kg（约 1.5 s 的全厂蒸汽量） */
    private static final double HARD_MAX_KG = 3000.0;
    /** 母管压力的存量响应增益 MPa/kg：汽堆积 -> 压力升高 -> 除氧器出口阀压差减小 -> 自限流 */
    private static final double PRESSURE_GAIN_MPA_PER_KG = 0.0005;
    private static final double PRESSURE_RESPONSE_MAX_MPA = 0.04;

    void setSources(Connectable[] sources) {
        this.sources = sources;
    }

    public void update() {
        double highest = 0.0;
        for (Connectable s : sources) {
            if (s != null) {
                highest = Math.max(highest, s.getPressure());
            }
        }
        /* 母管压力 = 源压力 - 管线压降 + 存量响应（2026-10-18）。
         * 只取"源压力 - 压降"时，只要除氧器出口阀还开着，它就按 0.02 MPa 压差全力送汽
         * （4 台合计 ~1800 kg/s），而汽轮机只能拿走 ~900 kg/s，多出来的汽没有出路 ——
         * 旧实现按"超出缓冲即排放"处理，等于每 tick 凭空消灭约 900 kg 蒸汽，
         * 表现为"给水/蒸汽莫名损失"。现在让堆积的汽抬高母管压力，
         * 上游除氧器出口阀的压差随之减小、流量自限制，只有真的超过硬上限才安全阀排放。 */
        double base = (highest > 0.0 ? highest : 0.70) - LINE_LOSS_MPA;
        double response = Math.min(PRESSURE_RESPONSE_MAX_MPA, steamMass * PRESSURE_GAIN_MPA_PER_KG);
        pressure = Math.max(0.05, Math.min(1.20, base + response));
        steamDensity = Loader.tables.getSteamDensityByPressure(pressure);

        inflowRate = inflowThisTick * 20.0;
        outflowRate = demand * 20.0;
        totalSteamKg += inflowThisTick;

        /* 质量守恒结算（2026-10-18 修正）：
         * 阀门的 update 顺序是"下游主汽门先取、除氧器出口阀后送"，本方法又在
         * 所有阀门之后结算。旧实现直接把 steamMass 置 0，等于把这一 tick 送进来、
         * 但还没被下游取走的汽全部丢掉 —— 主汽门在下个 tick 看到的可供量永远是 0，
         * 汽轮机实际拿不到任何蒸汽（实测 6 个 tick 取汽 0.00 kg）。
         * 现在保留"送进来 - 已取走"的余量，跨 tick 供下游取用；余量过大按安全阀排放。 */
        steamMass = Math.max(0.0, steamMass - demand);
        if (steamMass > HARD_MAX_KG) {
            reliefKgs = (steamMass - HARD_MAX_KG) * 20.0;
            steamMass = HARD_MAX_KG;
        } else {
            reliefKgs = 0.0;
        }
        inflowThisTick = 0.0;
        demand = 0.0;
        drawnThisTick = 0.0;
    }

    // ------------------------------------------------------------------
    // Connectable
    // ------------------------------------------------------------------
    @Override public void updateSteamInflow(double flow, double tempC) {
        if (flow < 0 || Double.isNaN(flow)) {
            return;
        }
        Double[] data = NPPMath.mixSteam(steamMass, steamTemperature, flow, tempC);
        steamMass = data[0];
        steamTemperature = data[1];
        inflowThisTick += flow;
    }

    @Override public void updateSteamOutflow(double flow, double tempC) {
        if (flow > 0 && !Double.isNaN(flow)) {
            demand += flow;
            drawnThisTick += flow;
        }
    }

    /**
     * 进出汽量修正（kg，本 tick）。
     *
     * 除氧器出口阀按压差算流量，可能比除氧器实际能供的多。若上游供不上，
     * 除氧器会把缺口回调到这里，把本 tick 已经登记进来的量扣掉，
     * 避免"阀门开大就能凭空造汽"（原先 4 台除氧器合计能造出 468 kg/s）。
     */
    void adjustInflow(double deltaKg) {
        if (Double.isNaN(deltaKg)) {
            return;
        }
        steamMass = Math.max(0.0, steamMass + deltaKg);
        // 缺口也要从本 tick 的进汽计数里扣掉，否则流量显示会把"阀门请求量"当成实际进汽
        inflowThisTick = Math.max(0.0, inflowThisTick + deltaKg);
    }

    @Override public void updateWaterInflow(double flow, double tempC) { }
    @Override public void updateWaterOutflow(double flow, double tempC) { }

    @Override public double getPressure() { return pressure; }

    @Override public double getSteamDensity() {
        return Loader.tables.getSteamDensityByPressure(Math.max(0.05, pressure));
    }

    @Override public double getWaterDensity() {
        return Loader.tables.getWaterDensityByTemp(Math.max(20.0, steamTemperature));
    }

    @Override public double getSteamMass() { return steamMass; }
    @Override public double getWaterMass() { return 0.0; }
    @Override public double getSteamVolume() { return 0.0; }
    @Override public double getWaterTemperature() { return steamTemperature; }
    @Override public double getSteamTemperature() { return steamTemperature; }
    @Override public double getWaterLevel() { return 0.0; }

    /** 本 tick 联箱剩余的汽：主汽门/SDV-C/抽气器共 20 只阀共享，按领取顺序递减 */
    @Override public double availableSteamKg() {
        double rest = steamMass - drawnThisTick;
        return rest > 0.0 ? rest : 0.0;
    }

    // ---- 监控访问器（界面用）----
    double getInflowKgs() { return inflowRate; }
    double getOutflowKgs() { return outflowRate; }
    double getTotalSteamKg() { return totalSteamKg; }
    double getReliefKgs() { return reliefKgs; }
}