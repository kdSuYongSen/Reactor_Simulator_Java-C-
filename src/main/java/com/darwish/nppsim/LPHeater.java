package com.darwish.nppsim;

import java.io.Serializable;

/**
 * 低压加热器 LP-1（2026-10-19 新增）：一级抽汽回热。
 *
 * 位置：凝汽器 → 凝结水泵 → 凝结水联箱 → **本加热器** → PCS 过滤段 → 除氧器
 * 汽源：汽轮机低压抽汽口 0.15 MPa（Tsat 111 °C）
 *
 * 为什么要它：
 *   改造前全部给水加热都由除氧器一级完成（36.5 → 153 °C），需要 25% 的抽汽，
 *   而这 25% 的汽只膨胀到 0.60 MPa 就离开汽轮机。加一级低压加热器后，
 *   凝结水先被 0.15 MPa 抽汽加热到约 105 °C，除氧器只做 105 → 153 °C 这一段；
 *   抽汽总量基本不变，但每一 kg 抽汽都多膨胀了 0.60 → 0.15 MPa，
 *   按 4 MPa 主蒸汽算，每 kg 多出约 150~190 kJ/kg 的功（≈ +58 MW）。
 *
 * 建模口径：
 *   - 流过式加热器：只加热"本 tick 流过的水"，不按整箱存水算热量；
 *   - 抽汽量由温度控制给定（出口温度目标 Tsat − 上端差），不靠压差自由抽取，
 *     避免"抽多了把水加热到饱和再白白扔掉"；
 *   - 抽汽凝结水按质量分给各除氧器（级间疏水），质量守恒。
 */
class LPHeater implements Connectable, Serializable {
    private static final long serialVersionUID = 1L;

    /** 壳侧压力（MPa）—— 抽汽口 0.15 MPa 经阀门/管道压损后 */
    private static final double SHELL_PRESSURE_MPA = 0.13;
    /** 上端差 K：出口水温 = Tsat(壳侧) − 端差 */
    private static final double TERMINAL_DIFF_K = 3.0;

    private final Connectable[] steamSources;          // 各汽轮机的低压抽汽口
    private Connectable[] drains;                // 疏水去向（各除氧器）
    private Connectable upstream;                // 凝结水联箱

    private double demand = 0.0;                 // 下游登记的取水 kg/tick
    private double waterTemperature = 36.5;      // 出口水温（下游读这个）
    private double throughputKgs = 0.0;
    private double extractionKgs = 0.0;

    LPHeater(Connectable[] steamSources, Connectable[] drains, Connectable upstream) {
        this.steamSources = steamSources;
        this.drains = drains;
        this.upstream = upstream;
        if (upstream != null) {
            waterTemperature = upstream.getWaterTemperature();
        }
    }

    /** 疏水去向（除氧器建好之后补） */
    void setDrains(Connectable[] drains) {
        this.drains = drains;
    }

    Connectable getUpstream() {
        return upstream;
    }

    /** 读档后重新接线（上游精处理段/联箱是存档之外的对象） */
    void setUpstream(Connectable upstream) {
        this.upstream = upstream;
    }

    public void update() {
        double take = Math.max(0.0, demand);       // kg/tick
        demand = 0.0;

        double tIn = (upstream != null) ? upstream.getWaterTemperature() : waterTemperature;
        if (upstream != null && take > 0.0) {
            upstream.updateWaterOutflow(take, tIn);
        }

        double takeKgs = take * 20.0;
        double tSat = Loader.tables.getSteamTemperatureByPressure(SHELL_PRESSURE_MPA);
        double tMax = tSat - TERMINAL_DIFF_K;
        double cp = NPPMath.calculateSpecificHeatWater(Math.max(20.0, tIn));
        double hfg = Math.max(1.0, Loader.tables.getSpecificVaporEnthalpyByTemperature(tSat));

        double extKg = 0.0;                        // 本 tick 抽汽量 kg
        double tOut = tIn;
        if (take > 0.0 && tMax > tIn) {
            /* 把 take 的水从 tIn 加热到 tMax 需要的热量；抽汽本身也变成同温度的水，
             * 所以每 kg 抽汽净放 (hfg − cp·(tMax − tIn))。 */
            double needMJ = take * cp * (tMax - tIn);
            double perKg = hfg - cp * (tMax - tIn);
            double wantKg = perKg > 1.0 ? needMJ / perKg : 0.0;
            extKg = drawSteam(wantKg);
            if (extKg > 0.0) {
                double q = extKg * hfg;
                double mass = take + extKg;
                tOut = Math.min(tMax, tIn + q / (mass * cp));
            } else {
                tOut = tIn;
            }
        }

        if (extKg > 0.0 && drains != null) {
            double each = extKg / drains.length;
            for (Connectable d : drains) {
                d.updateWaterInflow(each, tOut);
            }
        }

        waterTemperature = tOut;
        throughputKgs = takeKgs;
        extractionKgs = extKg * 20.0;
    }

    /** 按需向各抽汽口取汽（kg），不超过各口的可供量 */
    private double drawSteam(double wantKg) {
        if (wantKg <= 0.0 || steamSources == null) return 0.0;
        double remain = wantKg;
        double got = 0.0;
        for (Connectable p : steamSources) {
            if (remain <= 1e-9) break;
            double cap = (p instanceof SteamSupplyLimit) ? ((SteamSupplyLimit) p).availableSteamKg() : remain;
            double avail = Math.min(remain, cap);
            if (avail > 0.0) {
                p.updateSteamOutflow(avail, p.getSteamTemperature());
                got += avail;
                remain -= avail;
            }
        }
        return got;
    }

    // ------------------------------------------------------------------
    // Connectable
    // ------------------------------------------------------------------
    @Override public void updateWaterOutflow(double flow, double tempC) {
        if (flow > 0.0 && !Double.isNaN(flow)) {
            demand += flow;
        }
    }

    @Override public void updateWaterInflow(double flow, double tempC) { }
    @Override public void updateSteamInflow(double flow, double tempC) { }
    @Override public void updateSteamOutflow(double flow, double tempC) { }

    @Override public double getPressure() { return SHELL_PRESSURE_MPA; }
    @Override public double getSteamDensity() {
        return Loader.tables.getSteamDensityByPressure(SHELL_PRESSURE_MPA);
    }
    @Override public double getWaterDensity() {
        return Loader.tables.getWaterDensityByTemp(Math.max(20.0, waterTemperature));
    }
    @Override public double getSteamMass() { return 0.0; }
    @Override public double getWaterMass() { return 1.0e6; }
    @Override public double getSteamVolume() { return 0.0; }
    @Override public double getWaterTemperature() { return waterTemperature; }
    @Override public double getSteamTemperature() {
        return Loader.tables.getSteamTemperatureByPressure(SHELL_PRESSURE_MPA);
    }
    @Override public double getWaterLevel() { return 0.0; }

    // ---- 监控访问器 ----
    double getThroughputKgs() { return throughputKgs; }
    double getExtractionKgs() { return extractionKgs; }
}
