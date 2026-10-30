package com.darwish.nppsim;

import java.io.Serializable;

/**
 * 汽水分离器（A/B 两套，对应两个环路）。
 *
 * <p><b>真实 RBMK-1500</b>：分离器是**卧式鼓形分离器（drum separator）**——一根直径约 2.6 m、
 * 长二十多米的卧式圆筒，汽水混合物从下部进入，内部靠**旋风分离器 + 顶部百叶/丝网干燥器**
 * 做汽水分离，水落在下半部水容积里，经降水管回堆芯。整套设备 <b>没有任何转动部件</b>，
 * 没有电机、没有刹车、也没有"转速"这一说；出口蒸汽湿度约 0.1% 量级。</p>
 *
 * <p>所以本类只保留静止设备该有的量：压力、汽/水流量、水位、温度，以及自动压力控制
 * （入口/出口阀把压力维持在设定值）。早期版本里的"辅助分离电机 / ABS 刹车 / 转速 /
 * 电机逆功率 / 高·低转速"是照搬了别的机型的概念，2026-10-27 按要求删除。</p>
 *
 * <p>警报集（annunciator 风格，红/橙/黄三档，只保留真实鼓形分离器会有的）：<br>
 * 红：高压力、高温度、水位高（疏水跟不上／接近满水）<br>
 * 橙：蒸汽产量低<br>
 * 黄：低压力、低温度</p>
 */
class Separator implements Serializable {
    private static final long serialVersionUID = 2L;

    final String id;                             // "A" / "B"

    // ---- 读数 ----
    double inSteamKgs = 0.0;                     // 输入蒸汽量 kg/s
    double outSteamKgs = 0.0;                    // 输出蒸汽量 kg/s（→主蒸汽母管）
    double outWaterKgs = 0.0;                    // 输出水量 kg/s（→给水回收）
    double inTempC = 20.0;                       // 输入温度（换热器蒸汽）
    double outTempC = 20.0;                      // 输出温度（去除氧器，略微损失）
    /**
     * 分离器压力 MPa。
     *
     * <p>2026-10-27：初始值从"设定值 4.0 MPa"改成常压 0.1014 ——
     * 原版 jar 里汽包（老拓扑的蒸汽源）初值就是 0.1014 MPa，冷态开机时
     * 主蒸汽母管也是常压；改成设定值会让冷态界面显示 4 MPa 的"假压力"。
     * 现在的压力模型是按净进汽量积分的（{@code p += (in-out)·dt·k}），
     * 所以冷态就是常压，产汽后才逐步升到 {@link #PRESSURE_SETPOINT}。</p>
     */
    double pressureMPa = 0.1014;                 // 分离器压力（冷态常压起步）
    double waterLevel = 30.0;                    // 水位 %（水容积装满=100）

    // ---- 控制 ----
    double inValve = 1.0, outValve = 1.0;        // 蒸汽入口/出口阀（自动压力控制）

    // ---- 泄压/排水阀（2026-10-27 新增）----
    /** 泄压阀是否开启（高压力或高水位自动投入；红色提示） */
    boolean reliefValveOpen = false;
    /** 本 tick 经泄压阀排向除盐水箱的流量 kg/s */
    double reliefDrainKgs = 0.0;
    /** 泄压阀排水目标（除盐水箱；PCS 建好后由 NPPSim 注入） */
    private transient Connectable reliefTarget;

    // ---- 警报判定结果（红/橙/黄三档）----
    boolean alarmHighPressure = false;           // 红：高压力
    boolean alarmHighTemp = false;               // 红：高温度
    boolean alarmWaterLevelHigh = false;         // 红：水位高（疏水跟不上/接近满水）
    boolean alarmLowSteamOutput = false;         // 橙：蒸汽产量低
    boolean alarmLowPressure = false;            // 黄：低压力
    boolean alarmLowTemp = false;                // 黄：低温度
    boolean alarmReliefOpen = false;             // 红：泄压阀开启

    /* 二回路压力设定值（2026-10-19：0.70 → 4.00 MPa）。
     * 4.0 MPa 对应饱和温度 250.4 °C，与 6.9 MPa/284 °C 的一回路留 33.6 K 端差，
     * 是间接循环下能兼顾"换热面积合理"和"汽轮机焓降"的折中值。
     * 0.7 MPa 时可用焓降只有 ~600 kJ/kg，出力被死死压在 900 MWe。 */
    static final double PRESSURE_SETPOINT = 4.00;   // 压力设定值 MPa
    private static final double LATENT_HEAT = 2.26e6; // 汽化潜热 J/kg（显示口径）

    // 警报阈值
    private static final double P_HIGH = 8.0, P_LOW = 0.30;
    /** 泄压阀定值：压力 7.5 MPa 或水位 90% 自动开启（低于 P_HIGH，先泄压再报警） */
    static final double RELIEF_P_OPEN = 7.5;
    static final double RELIEF_LEVEL_OPEN = 90.0;
    /** 泄压阀回座：压力 < 6.8 MPa 且水位 < 85% */
    static final double RELIEF_P_CLOSE = 6.8;
    static final double RELIEF_LEVEL_CLOSE = 85.0;
    /** 单台最大排放量 kg/s */
    static final double RELIEF_MAX_KGS = 400.0;
    private static final double T_HIGH = 320.0, T_LOW = 100.0;
    private static final double STEAM_OUT_LOW = 2.0;
    /** 机械携带水率基数（干饱和汽经旋风+干燥器后约 0.1~2%，取 2% 兼顾质量平衡） */
    private static final double CARRYOVER_BASE = 0.02;
    /** 水位超过这个值后，携带水率随水位线性变差 */
    private static final double CARRYOVER_LEVEL_KNEE = 60.0;

    /** 本分离器的蒸汽源适配器（NPPSim 建好后回填，用于汽空间压力响应） */
    private transient SeparatorSteamSource steamSource;

    Separator(String id) {
        this.id = id;
    }

    void setSteamSource(SeparatorSteamSource src) {
        this.steamSource = src;
    }

    /** 泄压阀排水目标（除盐水箱）。不设则只做限压、不外排。 */
    void setReliefTarget(Connectable target) {
        this.reliefTarget = target;
    }

    /**
     * @param dt            帧长 0.05 s
     * @param hxSteamKgs    换热器蒸汽流出量（ISOOUT 调制后）
     * @param hxSteamTC     换热器蒸汽温度
     */
    void update(double dt, double hxSteamKgs, double hxSteamTC) {
        inTempC = hxSteamTC;

        // ---- 自动压力控制：压力偏高→关小入口/开大出口；偏低→反之（比例控制）----
        double err = pressureMPa - PRESSURE_SETPOINT;
        inValve = clamp(1.0 - err * 2.0);
        outValve = clamp(1.0 + err * 2.0);
        /* 说明（2026-10-18）：原先在这里加过"缓存泄空 → 压力跌"的负反馈，
         * 但它和自动压力控制（低压时关小出口阀）互相抵消，形成"越跌越不出汽"的死循环
         * （实测流量从 1500 kg/s 掉到 200 kg/s）。汽空间缓存本身已经能吸收 tick 内的
         * 分配波动，持续超载由除氧器入口阀的驱动压差（母管压力）去限，这里不再加压。 */

        // ---- 流量链 ----
        inSteamKgs = Math.max(0.0, hxSteamKgs) * inValve;
        /* 汽水分离（静止设备）：机械携带水率随水位升高而变差——水位越接近顶部干燥器，
         * 携带的水滴越多。60% 以下按基数 2%，以上每 1% 加 0.1%，封顶 8%。 */
        double carryover = CARRYOVER_BASE
                + Math.max(0.0, waterLevel - CARRYOVER_LEVEL_KNEE) * 0.001;
        outWaterKgs = inSteamKgs * Math.min(0.08, carryover);
        outSteamKgs = Math.max(0.0, inSteamKgs - outWaterKgs) * outValve;
        // 压力模型：流入>流出 → 升压（集总，kg/s 差 × 系数）
        pressureMPa += (inSteamKgs - outSteamKgs) * dt * 2.5e-4;
        pressureMPa = Math.max(0.10, Math.min(9.0, pressureMPa));

        /* ---- 泄压阀（自动）----
         * 高压力或高水位时自动开启，把多余的汽/水排向除盐水箱（后续再改抑压水池）。
         * 回座用略低的定值，避免在阈值上反复开关（抖动）。 */
        boolean wantOpen = reliefValveOpen
                ? (pressureMPa > RELIEF_P_CLOSE || waterLevel > RELIEF_LEVEL_CLOSE)
                : (pressureMPa > RELIEF_P_OPEN || waterLevel > RELIEF_LEVEL_OPEN);
        reliefValveOpen = wantOpen;
        reliefDrainKgs = 0.0;
        if (reliefValveOpen) {
            double byP = Math.max(0.0, pressureMPa - RELIEF_P_CLOSE) * 160.0;
            double byL = Math.max(0.0, waterLevel - RELIEF_LEVEL_CLOSE) * 8.0;
            reliefDrainKgs = Math.min(RELIEF_MAX_KGS, Math.max(byP, byL));
            if (reliefDrainKgs > 0.0) {
                // 只从水侧排走（高压时也以"排放饱和水"处理，避免蒸汽质量凭空消失）
                waterLevel = Math.max(0.0, waterLevel - reliefDrainKgs * dt * 0.8);
                pressureMPa = Math.max(0.10, pressureMPa - reliefDrainKgs * dt * 2.0e-4);
                Connectable target = reliefTarget;
                if (target == null && NPPSim.pcs != null) {
                    target = NPPSim.pcs.demineralizedWaterTank;   // 读旧档后自动接回水箱
                }
                if (target != null) {
                    target.updateWaterInflow(reliefDrainKgs * dt, outTempC);
                }
            }
        }

        // ---- 水位：分离甩出的水由疏水泵连续排走（2026-10-18 加大疏水能力）。
        // 旧值 3/12 kg/s 相对 ~200 kg/s 的甩水量差 20 倍，水位必然一直涨到 130% 封顶。
        // 疏水量按甩水量取；疏水能力有上限，跟不上时水位上涨（触发"水位高"）。----
        double drainKgs = Math.min(outWaterKgs, waterLevel > 40.0 ? 400.0 : 200.0);
        boolean drainDeficit = outWaterKgs > 0.1 && drainKgs < outWaterKgs - 0.05;
        waterLevel += (outWaterKgs - drainKgs) * dt * 0.8;
        waterLevel = Math.max(0.0, Math.min(130.0, waterLevel));

        // ---- 输出温度：去除氧器方向略微损失温度 ----
        outTempC = Math.max(20.0, inTempC - 3.0);

        // ---- 警报判定 ----
        boolean running = inSteamKgs > 1.0;      // 有蒸汽进入才算运行（停机时低压/低温/汽量低不报警）
        alarmHighPressure = pressureMPa > P_HIGH;
        alarmHighTemp = inTempC > T_HIGH;
        alarmWaterLevelHigh = drainDeficit || waterLevel > 90.0;
        alarmLowSteamOutput = running && outSteamKgs < STEAM_OUT_LOW;
        alarmLowPressure = running && pressureMPa < P_LOW;
        alarmLowTemp = running && inTempC < T_LOW;
        alarmReliefOpen = reliefValveOpen;
    }

    private static double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    /** 输入蒸汽携带的热功率（MW，监控口径） */
    double inHeatMW() { return inSteamKgs * LATENT_HEAT / 1e6; }
}
