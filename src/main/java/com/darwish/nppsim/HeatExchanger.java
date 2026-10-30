package com.darwish.nppsim;

import java.io.Serializable;

/**
 * 换热器 A/B —— 一回路（沸腾回路）与二回路（蒸汽回路）的热耦合界面。
 *
 * 环路（2026-10-17 改版后实际参与求解，不再只是监控模型）：
 *   一回路：堆芯 → 汽包（drum 汽水分离）→ **换热器壳侧（冷凝）** → 汽包/MCP → 堆芯
 *   二回路：给水 → **换热器管侧（蒸发）** → 汽水分离器 → 除氧器蒸汽入口 → 除氧器蒸汽出口
 *           → 汽轮机/旁路 → 凝汽器 → 热井 → 除氧器水入口 → 除氧器水出口 → 给水
 *   离线冷却：河水 → 离线泵 → 换热器（开式，20°C 冷源）——与主回路独立
 *
 * 模型：
 *   壳侧按“压差比例抽取汽包蒸汽”工作：m_p = ISOIN · K · max(0, P_drum − P_sec)。
 *   汽包压力下降会自动减小抽汽量（负反馈，稳态自持），冷凝水按同质量回注汽包，
 *   一回路水量守恒。冷凝放热 Q = m_p · (h_g − h_f)@汽包饱和温度。
 *   管侧用这份热量蒸发给水：m_s = Q / (h_g − h_f)@二回路饱和温度，
 *   受给水流量与管侧存量限制；产汽送汽水分离器。
 */
class HeatExchanger implements Connectable, Serializable {
    private static final long serialVersionUID = 3L;

    final String id;                            // "A" / "B"

    // ---- 一回路（壳侧：汽包蒸汽冷凝）----
    double primarySteamInKgs = 0.0;             // 进汽量 kg/s
    double primaryFlowKgs = 0.0;                // 显示：一回路循环流量（MCP 分摊）
    double primaryTempC = 280.0;                // 汽包饱和温度
    double primaryPressureMPa = 6.9;            // 汽包压力
    double condensateOutKgs = 0.0;              // 回注汽包的凝结水 kg/s
    double primaryHeatMW = 0.0;                 // 冷凝放热 MW

    // ---- 二回路（管侧：给水蒸发）----
    double secondaryFeedKgs = 0.0;              // 给水 kg/s
    double secondarySteamKgs = 0.0;             // 产汽 kg/s
    double secondaryFlowKgs = 0.0;              // 显示：= 给水
    double secondaryPressureMPa = 0.70;         // 二回路压力（跟随汽水分离器）
    double secondarySteamTC = 165.0;            // 二回路饱和温度
    double steamOutKgs = 0.0;                   // 送汽水分离器的蒸汽 kg/s
    double secondaryWaterKg = 20000.0;          // 管侧存量（原 6 t，二回路改造后加大）
    double secondaryDrainKgs = 0.0;             // 本 tick 未蒸发、退回除氧器的给水 kg/s
    double secondaryHeatMW = 0.0;               // 二回路实际吸收的热量 MW（应 ≈ primaryHeatMW）

    // ---- 离线河水侧 ----
    double riverFlowKgs = 0.0;
    double riverOutTC = 20.0;

    // ---- 控制 ----
    double isoIn = 1.0, isoOut = 1.0;           // 进汽/出汽阀（0..1）

    private double waterTemperature = 160.0;
    private double pressure = 0.70;             // Connectable 口径（= 二回路压力）
    private double feedKgAccum = 0.0;           // 本 tick 累计给水 kg
    private double feedTempAccum = 160.0;

    /** 抽汽导纳：满压差 6.2 MPa 时约 1550 kg/s/台（两台合计 3100 kg/s ≈ 4800 MW 的产汽量）。
     *  原值 500 会让两台合计抽 6200 kg/s（额定产汽量的两倍），汽包压力必然被抽塌。 */
    private static final double DRAW_KGS_PER_MPA = 548.0;   // 6.9−4.0=2.9 MPa 下两台合计 3180 kg/s

    HeatExchanger(String id) {
        this.id = id;
    }

    // ------------------------------------------------------------------
    // 一回路
    // ------------------------------------------------------------------
    /** 按汽包压力抽取蒸汽（比例式，压降自稳）；返回本 tick 抽汽质量 kg */
    double updatePrimary(double drumPressureMPa, double drumSteamTC, double mcpFlowKgs, double dt) {
        primaryPressureMPa = drumPressureMPa;
        primaryTempC = drumSteamTC;
        primaryFlowKgs = mcpFlowKgs * 0.5;

        double draw = isoIn * DRAW_KGS_PER_MPA * Math.max(0.0, drumPressureMPa - secondaryPressureMPa);
        draw = Math.max(0.0, Math.min(6000.0, draw));
        primarySteamInKgs = draw;
        condensateOutKgs = draw;                 // 质量守恒：抽多少汽，回多少水

        // 注意蒸汽表口径（2026-10-17 修正）：
        //   getSteamEnthalpyByTemperature(T)         = 饱和蒸汽 h_g
        //   getSpecificVaporEnthalpyByTemperature(T) = 汽化潜热 h_fg
        //   getWaterEnthalpyByTemperature(T)         = 饱和水   h_f
        // 壳侧蒸汽冷凝成同温度的饱和水，放出的正是 h_fg（原先 h_fg 与 h_g 用反，
        // 相减为负 → primaryHeatMW 恒为 0，二次侧一台汽都产不出来）。
        double hfg = Loader.tables.getSpecificVaporEnthalpyByTemperature(drumSteamTC);  // kJ/kg
        primaryHeatMW = draw * Math.max(0.0, hfg) / 1000.0;                            // kg/s·kJ/kg = kW → MW
        return draw * dt;
    }

    // ------------------------------------------------------------------
    // 二回路
    // ------------------------------------------------------------------
    /** 本 tick 二回路**最多能吸收**的一次侧凝结量（kg，按一次侧质量计）。
     *
     * 物理含义：管侧只能把 feedKgs + 小缓冲 的水蒸发掉，吸收的热量
     * = 这些水在二回路侧的焓升；一次侧每凝结 1 kg 放 h_fg(汽包)，所以
     *   可凝结的一次侧质量 = Q_sec_max / h_fg_pri。
     *
     * 旧实现没有这道约束：一次侧按压差想抽多少就抽多少，二次侧却只按给水量
     * 蒸发 —— 差额热量凭空消失。实测额定工况一次侧 4703 MW、二次侧只吸收
     * 约 3820 MW，**每小时丢掉约 880 MW（19%）**。
     */
    double maxCondensableKg(double dt) {
        double feedKgs = feedKgAccum / Math.max(dt, 1e-4);
        double tsat = Loader.tables.getSteamTemperatureByPressure(Math.max(0.05, secondaryPressureMPa));
        double hg = Loader.tables.getSteamEnthalpyByTemperature(tsat);
        double hf = Loader.tables.getWaterEnthalpyByTemperature(Math.min(waterTemperature, tsat - 1.0));
        double hfgSec = Math.max(1.0, hg - hf);
        double bufferKgs = Math.min(2.0, secondaryWaterKg * 0.02);
        double maxSteamKgs = Math.max(0.0, feedKgs + bufferKgs);
        double hfgPri = Math.max(1.0, Loader.tables.getSpecificVaporEnthalpyByTemperature(primaryTempC));
        return maxSteamKgs * hfgSec / hfgPri * dt;
    }

    /** 源端限流后，按**实际**抽汽质量 kg 回写流量与传热量（保持质量/能量一致）。 */
    void applyActualDraw(double kg, double dt) {
        double kgs = kg / Math.max(dt, 1e-4);
        primarySteamInKgs = kgs;
        condensateOutKgs = kgs;                  // 抽多少汽，回多少水
        double hfg = Loader.tables.getSpecificVaporEnthalpyByTemperature(primaryTempC);
        primaryHeatMW = kgs * Math.max(0.0, hfg) / 1000.0;
    }

    void updateSecondary(double dt, double separatorPressureMPa) {
        if (separatorPressureMPa > 0.05) {
            secondaryPressureMPa = separatorPressureMPa;
        }
        pressure = secondaryPressureMPa;

        double tsat = Loader.tables.getSteamTemperatureByPressure(secondaryPressureMPa);
        secondarySteamTC = tsat;
        double hg = Loader.tables.getSteamEnthalpyByTemperature(tsat);                    // h_g
        double hf = Loader.tables.getWaterEnthalpyByTemperature(Math.min(waterTemperature, tsat - 1.0)); // h_f
        double hfg = Math.max(1.0, hg - hf);

        double maxSteam = primaryHeatMW * 1000.0 / hfg;                 // 热量允许的蒸发量 kg/s
        double feedKgs = feedKgAccum / Math.max(dt, 1e-4);
        secondaryFeedKgs = feedKgs;

        // 管侧存量只提供很小的缓冲（避免"无给水也能一直产汽"）。
        // 原式 min(80, 存量/dt*0.02) 在 6000 kg 存量下恒等于 80 kg/tick = 1600 kg/s，
        // 比给水量还大，等于可以靠烧库存无限产汽。
        double bufferKgs = Math.min(2.0, secondaryWaterKg * 0.02);
        secondarySteamKgs = Math.max(0.0, Math.min(maxSteam, feedKgs + bufferKgs));
        secondarySteamKgs = Math.min(secondarySteamKgs, 6000.0);

        /* 给水 > 可蒸发量时，多余的水不能只"存"在管侧（旧实现把超出 80 t 的部分
         * 无声吞掉，给水回路的水位就一直掉，而且这些水永远不会变成蒸汽进汽水分离器）。
         * 现在按 tick 退回给水系统（NPPSim 回收到除氧器），管侧存量保持恒定。 */
        double excess = Math.max(0.0, feedKgs - secondarySteamKgs);
        secondaryDrainKgs = excess;
        secondaryWaterKg += (feedKgs - secondarySteamKgs - excess) * dt;
        secondaryWaterKg = Math.max(2000.0, Math.min(120000.0, secondaryWaterKg));

        steamOutKgs = isoOut * secondarySteamKgs;
        secondaryFlowKgs = feedKgs;
        /* 二回路实际吸收的热量：应严格等于 primaryHeatMW（由 maxCondensableKg 限流保证）。 */
        secondaryHeatMW = secondarySteamKgs * hfg / 1000.0;

        waterTemperature = Math.min(tsat, Math.max(20.0, feedTempAccum));
        feedKgAccum = 0.0;
    }

    // ------------------------------------------------------------------
    // Connectable（给水阀/混合器把水送进来）
    // ------------------------------------------------------------------
    @Override public void updateWaterInflow(double flowKg, double tempC) {
        feedKgAccum += flowKg;
        feedTempAccum = tempC;
    }

    @Override public double getPressure() { return pressure; }

    @Override public double getSteamDensity() {
        return Loader.tables.getSteamDensityByPressure(Math.max(0.05, secondaryPressureMPa));
    }

    @Override public double getWaterDensity() {
        return Loader.tables.getWaterDensityByTemp(Math.max(20.0, waterTemperature));
    }

    @Override public double getSteamMass() { return 0.0; }
    @Override public double getWaterMass() { return secondaryWaterKg; }
    @Override public double getSteamVolume() { return 0.0; }
    @Override public double getWaterTemperature() { return waterTemperature; }
    @Override public double getSteamTemperature() { return secondarySteamTC; }
    @Override public double getWaterLevel() { return 0.0; }
    @Override public void updateSteamOutflow(double flow, double tempC) { }
    @Override public void updateSteamInflow(double flow, double tempC) { }
    @Override public void updateWaterOutflow(double flow, double tempC) { }

    // ---- 监控/控制访问器 ----
    void setIsoIn(double v)  { isoIn = Math.max(0.0, Math.min(1.0, v)); }
    void setIsoOut(double v) { isoOut = Math.max(0.0, Math.min(1.0, v)); }
    double getIsoIn()        { return isoIn; }
    double getIsoOut()       { return isoOut; }

    /** 二回路给水量清零（每帧由 NPPSim 调用前重置累计） */
    void resetFeedAccumulator() { feedKgAccum = 0.0; }
}
