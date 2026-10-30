package com.darwish.nppsim;

/**
 * 离线冷却环路（开式）：外部河水 → 阀门 → 离线泵 A/B → 换热器 → 河水。
 *
 * 河水为无限冷源（20°C）：离线泵流量经 {@link HeatExchanger} 的离线侧带走一回路热量，
 * 热排水直接回到河水（不回收、不消耗 PCS 水箱）。
 * 对堆芯的物理作用：流量经 C++ 核心 setOfflineFlow 以 20°C 冷源置换热沉生效。
 *
 * 控制极简：两台泵（开/关）+ 一只环路阀（开/关）。默认全部开启
 * （停堆冷却必须可用）。
 */
class OfflineCoolingSystem implements java.io.Serializable {
    private static final long serialVersionUID = 3L;

    static final double RATED_RPM = 2985.0;
    static final double RATED_FLOW_KGS = 200.0;    // 每台泵额定流量 kg/s（2×200）
    static final double VALVE_TRAVEL_S = 3.0;      // 环路阀全行程时间
    static final double WATER_TEMP_C = 20.0;       // 河水温度
    /** 单台离线泵电机额定电流 A（380 V / 约 75 kW 的典型值） */
    static final double RATED_AMPS = 130.0;

    boolean pumpARunning = true, pumpBRunning = true;   // 默认启动
    boolean valveOpen = true;                           // 河水阀门默认开
    double pumpARpm = 0, pumpBRpm = 0, valvePos = 0;    // 阀位 0..1
    double flowA = 0, flowB = 0;                        // kg/s（当前实际流量）
    double outletTempC = WATER_TEMP_C;                  // 堆芯排放水温（Core 每 tick 回写，电厂侧）

    /** 每 tick 推进（dt = 0.05 s）：泵转速/阀位行程 + 可用流量（河水无限，不断流） */
    void update(double dt) {
        pumpARpm = ramp(pumpARpm, pumpARunning ? RATED_RPM : 0, RATED_RPM * dt);
        pumpBRpm = ramp(pumpBRpm, pumpBRunning ? RATED_RPM : 0, RATED_RPM * dt);
        double vTarget = valveOpen ? 1.0 : 0.0;
        double vStep = dt / VALVE_TRAVEL_S;
        valvePos = valvePos < vTarget ? Math.min(vTarget, valvePos + vStep)
                 : Math.max(vTarget, valvePos - vStep);
        flowA = RATED_FLOW_KGS * (pumpARpm / RATED_RPM) * valvePos;
        flowB = RATED_FLOW_KGS * (pumpBRpm / RATED_RPM) * valvePos;
    }

    private static double ramp(double cur, double target, double step) {
        if (cur < target) return Math.min(target, cur + step);
        if (cur > target) return Math.max(target, cur - step);
        return cur;
    }

    double getTotalFlowKgs()  { return flowA + flowB; }
    double getOutletTempC()   { return outletTempC; }
    boolean isCoolingActive() { return getTotalFlowKgs() > 0.1; }

    /**
     * 离线泵电流 A（A+B 两台合计）。
     *
     * <p>2026-10-28：面板第二个读数原来显示的是"除盐水箱水位 1996.9 t" ——
     * 那是 PCS 水箱的参数，离线冷却用的是河水、根本不消耗它，放在这里既误导又是常量。
     * 改成真正属于这个环路的量：两台离线泵的电机电流。</p>
     */
    double getCurrentA() {
        return RATED_AMPS * (pumpARpm / RATED_RPM + pumpBRpm / RATED_RPM);
    }

    // ---- 面板控制 ----
    void setPumpARunning(boolean r) { pumpARunning = r; }
    void setPumpBRunning(boolean r) { pumpBRunning = r; }
    void setValveOpen(boolean o)    { valveOpen = o; }
    boolean isPumpARunning() { return pumpARunning; }
    boolean isPumpBRunning() { return pumpBRunning; }
    boolean isValveOpen()    { return valveOpen; }
}
