package com.darwish.nppsim;

import java.io.Serializable;

/**
 * 通用净化装置（PCS 窗口）。
 *
 * <p>2026-10-28 新增。装在"河 → DW 泵"之间，把河水净化后送进除盐水箱：</p>
 * <pre>河 → 通用净化装置 → DW 泵 → 除盐水箱</pre>
 *
 * <p>模型口径：</p>
 * <ul>
 *   <li>默认**开启**（除盐水箱要保持补水能力）；停运时断水，DW 泵会抽空、汽蚀；</li>
 *   <li>流量跟随下游 DW 泵的抽水量（串联，不可压缩）；</li>
 *   <li>电流按流量比例给：空载 15%，满负荷 100%（额定 {@link #RATED_AMPS} A）；</li>
 *   <li>出水温度 = 河水温度 + 0.3 K（净化过程的泵功/温升）。</li>
 * </ul>
 */
class PurificationUnit extends Atmosphere implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 额定处理流量 kg/s（略高于 DW 泵额定 200 kg/s） */
    static final double RATED_FLOW_KGS = 210.0;
    /** 额定电流 A */
    static final double RATED_AMPS = 32.0;

    private final River river;

    /** 运行状态（默认开启） */
    boolean running = true;
    private double flowKgs = 0.0;
    private double currentA = 0.0;
    private double outletC = River.AMBIENT_C;

    PurificationUnit(River river) {
        super(River.AMBIENT_C);
        this.river = river;
    }

    /** @param demandKgs 下游 DW 泵本 tick 的抽水量 kg/s */
    void update(double demandKgs) {
        double srcT = river != null ? river.getWaterTemperature() : River.AMBIENT_C;
        if (!running) {
            flowKgs = 0.0;
            currentA = 0.0;
            outletC = srcT;
        } else {
            flowKgs = Math.max(0.0, Math.min(RATED_FLOW_KGS, demandKgs));
            currentA = flowKgs <= 0.01 ? 0.0 : RATED_AMPS * (0.15 + 0.85 * flowKgs / RATED_FLOW_KGS);
            outletC = srcT + 0.3;
        }
        waterTemperature = outletC;
    }

    // ---- 监控读数 ----
    double getFlowKgs() {
        return flowKgs;
    }

    double getCurrentA() {
        return currentA;
    }

    double getOutletC() {
        return outletC;
    }

    boolean isRunning() {
        return running;
    }

    void setRunning(boolean r) {
        running = r;
    }

    // ---- Connectable 口径 ----
    @Override
    public double getWaterTemperature() {
        return outletC;
    }

    @Override
    public double getWaterMass() {
        // 停运时断水：下游泵抽不到水（联锁），运行时可视为无限供水
        return running ? 1.0e11 : 0.0;
    }
}
