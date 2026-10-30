package com.darwish.nppsim;

import java.io.Serializable;

/**
 * PCS 凝结水过滤段（热阱）。
 *
 * 环路（2026-10-17 改版）：
 *   凝汽器 → 凝结水泵 → 凝结水联箱 → **本过滤段** → 除氧器水入口
 *          → 除氧器水出口 → 给水泵 → 给水混合器 → 换热器管侧
 *
 * 原版把凝结水净化并入凝结水系统；这里显式建成一个环节：凝结水在进入给水除氧器
 * 之前先经 PCS 过滤（去盐/去放射性核素），排污送除盐水箱，带少量管线压降。
 *
 * 建模口径：
 *   - 不产生压头。取水量由下游（除氧器水入口阀）登记，本段按登记量向上游联箱要水，
 *     上游再按需求分摊给凝结水泵（与泵/母管同一套"下游要、上游供"的约定一致）。
 *   - 排污默认关闭（blowdownRate = 0），打开后按流量比例抽出送除盐水箱。
 *   - 库存只用于给下游阀提供 getWaterMass() 口径，避免按库存限流。
 *
 * 同样不继承 Component：由 {@link PlantState} 直接持有；读档时从除氧器水入口阀的引用重接。
 */
class CondensateFilter implements Connectable, Serializable {
    private static final long serialVersionUID = 1L;

    /** 过滤器水容积（kg），只用于下游阀的库存口径 */
    private static final double NOMINAL_MASS = 4.0e4;
    /** 管线压降（MPa） */
    private static final double PRESSURE_DROP_MPA = 0.05;

    private Connectable upstream;              // 凝结水联箱（读档后重接）
    private double waterMass = NOMINAL_MASS;
    private double waterTemperature = 20.0, waterInflowTemperature = 20.0;
    private double pressure = 1.5;

    private double demand = 0.0;               // 下游登记的取水（kg/tick）
    private double throughputRate = 0.0;       // 通过量 kg/s
    private double blowdownRate = 0.0;         // 排污量 kg/s
    private double blowdownFraction = 0.0;     // 排污比例（0~1，相对通过量）
    private Connectable blowdownSink;          // 排污去处（除盐水箱）
    private boolean running = false;

    CondensateFilter(Connectable upstream, Connectable blowdownSink) {
        this.upstream = upstream;
        this.blowdownSink = blowdownSink;
        if (upstream != null) {
            waterTemperature = upstream.getWaterTemperature();
            pressure = upstream.getPressure();
        }
    }

    /** 读档后重新接线（上游联箱 / 排污去向都是存档之外的对象） */
    void setUpstream(Connectable upstream) {
        this.upstream = upstream;
    }

    void setBlowdownSink(Connectable sink) {
        this.blowdownSink = sink;
    }

    void setBlowdownFraction(double fraction) {
        this.blowdownFraction = Math.max(0.0, Math.min(0.5, fraction));
    }

    double getBlowdownFraction() {
        return blowdownFraction;
    }

    public void update() {
        double take = Math.max(0.0, demand);
        double blow = take * blowdownFraction;
        demand = 0.0;

        if (upstream != null) {
            // 过滤段没有压头：要多少就向上游要多少（kg/tick）
            upstream.updateWaterOutflow(take + blow, waterTemperature);
            pressure = Math.max(0.05, upstream.getPressure() - PRESSURE_DROP_MPA);
            waterTemperature = upstream.getWaterTemperature();
        }
        if (blow > 0.0 && blowdownSink != null) {
            blowdownSink.updateWaterInflow(blow, waterTemperature);
        }

        throughputRate = take * 20.0;
        blowdownRate = blow * 20.0;
        waterMass = NOMINAL_MASS;
        running = throughputRate > 0.1;
    }

    // ------------------------------------------------------------------
    // Connectable
    // ------------------------------------------------------------------
    @Override public void updateWaterOutflow(double flow, double tempC) {
        if (flow > 0 && !Double.isNaN(flow)) {
            demand += flow;
        }
    }

    @Override public void updateWaterInflow(double flow, double tempC) {
        double[] data = NPPMath.mixWater(waterMass, waterTemperature, flow, tempC);
        waterMass = data[0];
        waterTemperature = data[1];
        waterInflowTemperature = tempC;
    }

    @Override public void updateSteamInflow(double flow, double tempC) { }
    @Override public void updateSteamOutflow(double flow, double tempC) { }

    @Override public double getPressure() { return pressure; }
    @Override public double getSteamDensity() {
        return Loader.tables.getSteamDensityByPressure(Math.max(0.05, pressure));
    }
    @Override public double getWaterDensity() {
        return Loader.tables.getWaterDensityByTemp(Math.max(20.0, waterTemperature));
    }
    @Override public double getSteamMass() { return 0.0; }
    @Override public double getWaterMass() { return waterMass; }
    @Override public double getSteamVolume() { return 0.0; }
    @Override public double getWaterTemperature() { return waterTemperature; }
    @Override public double getSteamTemperature() { return 20.0; }
    @Override public double getWaterLevel() { return 0.0; }

    // ---- 监控访问器（界面用）----
    double getThroughputKgs() { return throughputRate; }
    double getBlowdownKgs() { return blowdownRate; }
    boolean isRunning() { return running; }
}