package com.darwish.nppsim;

import java.io.Serializable;

/**
 * 河（外部冷源 / 热沉）。
 *
 * <p>2026-10-28 新增。厂内三条水路的最终去向都在这里：</p>
 * <ul>
 *   <li>离线冷却外环路：除盐水箱 → 离线泵 A/B → 换热器 A/B → <b>河</b>（带走一回路余热）；</li>
 *   <li>DW 泵进水：<b>河</b> → 通用净化装置 → DW 泵 → 除盐水箱；</li>
 *   <li>除盐水箱排水：除盐水箱 → 排水阀 → <b>河</b>。</li>
 * </ul>
 *
 * <p>河是**无限水量**的边界条件：进水不会被"抽干"，只记录带走的热量
 * （用于界面显示与后续做河水温升模型）。水温默认 20 °C，大量热排水时最多升到
 * {@link #MAX_TEMP_C}，再靠流动缓慢回落到环境温度。</p>
 */
public class River extends Atmosphere implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 河水环境温度 °C */
    static final double AMBIENT_C = 20.0;
    /** 大量热排水时河水的允许温升 °C（集总近似，不是真实河道的热容量） */
    static final double MAX_TEMP_C = 24.0;

    /** 最近一次接收的热排水流量 kg/s */
    private double lastInflowKgs = 0.0;
    /** 最近一次接收的热排水温度 °C */
    private double lastInflowC = AMBIENT_C;
    /** 累计带走的热功率 MW（显示口径） */
    private double heatMw = 0.0;
    /** 河水当前温度 °C */
    private double tempC = AMBIENT_C;

    public River() {
        super(AMBIENT_C);
    }

    /** 每 tick 推进：按上一 tick 的热排水量算温升，再向环境温度回落。 */
    void update() {
        double rise = Math.max(0.0, lastInflowC - AMBIENT_C) * (lastInflowKgs / 20000.0);
        tempC = Math.min(MAX_TEMP_C, AMBIENT_C + rise);
        heatMw = lastInflowKgs * 4.186 * Math.max(0.0, lastInflowC - AMBIENT_C) / 1000.0;
        lastInflowKgs *= 0.9;          // 一 tick 的排水很快被河水带走
    }

    @Override
    public void updateWaterInflow(double flowKg, double tempC) {
        // flowKg 是"每 tick 的 kg"（全厂惯例），换算成 kg/s 记录
        lastInflowKgs = Math.max(0.0, flowKg * 20.0);
        lastInflowC = tempC;
    }

    @Override
    public double getWaterTemperature() {
        return tempC;
    }

    double getHeatMw() {
        return heatMw;
    }

    double getLastInflowKgs() {
        return lastInflowKgs;
    }
}
