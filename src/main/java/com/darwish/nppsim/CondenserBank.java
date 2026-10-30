package com.darwish.nppsim;

import java.io.Serializable;

/**
 * 凝汽器组（2026-10-25）：每台汽轮机 4 台凝汽器并联。
 *
 * 为什么要分台而不是一台集总：
 *   RBMK-1500 每台 750 MW 汽轮机的排汽分成 4 个凝汽器壳体，每个壳体有自己的
 *   循环水泵、热井、真空系统。分台以后，单台停循环泵/单台热井抽空只影响 1/4 的
 *   排汽面积，真空下降是渐进的，还能看出"哪一台坏了"。
 *
 * 分工：
 *   - 排汽（汽轮机乏汽、SDV-C 旁路、除氧器余汽）按 1/4 平均分给 4 台；
 *   - 凝结水由 4 个热井共同供给，下游（凝结水泵）提出的水量按 1/4 均摊；
 *   - 对外读数取 4 台平均（压力/温度/水位），总量取 4 台之和（凝结水量/水质量）。
 *
 * 单台的模型仍然是 {@link Condenser}：集总热平衡 + 由冷却水量和热负荷反算真空。
 */
class CondenserBank implements Connectable, SteamSupplyLimit, Serializable {
    private static final long serialVersionUID = 1L;

    /** 每个汽轮机的凝汽器台数 */
    static final int UNITS_PER_TG = 4;

    final Condenser[] units;

    CondenserBank(Condenser[] units) {
        this.units = units;
    }

    int size() {
        return units.length;
    }

    /** 第 i 台（0 基，越界自动钳到合法范围） */
    Condenser unit(int i) {
        if (units.length == 0) return null;
        int k = Math.max(0, Math.min(units.length - 1, i));
        return units[k];
    }

    public void update() {
        for (Condenser c : units) {
            c.update();
        }
    }

    /** 把热井排水阀接到同一个去向（4 台各一只） */
    /** 应急排水目标（4 台都要设） */
    public void setReliefTarget(Connectable target) {
        for (Condenser c : units) {
            c.setReliefTarget(target);
        }
    }

    public void attachDrain(Connectable target) {
        for (Condenser c : units) {
            c.attachDrain(target);
        }
    }

    WaterValve getDrainValve() {
        return units.length > 0 ? units[0].getDrainValve() : null;
    }

    // ------------------------------------------------------------------
    // 汇总读数
    // ------------------------------------------------------------------
    private double meanPressure() {
        if (units.length == 0) return 0.10142;
        double s = 0;
        for (Condenser c : units) s += c.getPressure();
        return s / units.length;
    }

    /** 4 台里最差的那台的真空（界面报警用：任一台掉真空都该看见） */
    double maxPressure() {
        double m = 0;
        for (Condenser c : units) m = Math.max(m, c.getPressure());
        return m;
    }

    double getCondensationRate() {
        double s = 0;
        for (Condenser c : units) s += c.getCondensationRate();
        return s;
    }

    double getCondenserWaterTemperature() {
        double s = 0;
        for (Condenser c : units) s += c.getCondenserWaterTemperature();
        return units.length == 0 ? 0 : s / units.length;
    }

    boolean isEmpty() {
        for (Condenser c : units) {
            if (!c.isEmpty()) return false;
        }
        return true;
    }

    /** 4 台中最低的热井（任一台抽空都值得报警） */
    double minWaterLevel() {
        double m = Double.MAX_VALUE;
        for (Condenser c : units) m = Math.min(m, c.getWaterLevel());
        return units.length == 0 ? 0 : m;
    }

    /** 汽空间里的空气总量 kg（4 台之和）—— 真空系统的被抽对象 */
    double getGasMassKg() {
        double s = 0;
        for (Condenser c : units) s += c.getGasMassKg();
        return s;
    }

    /** 抽走空气：4 台平均分摊，返回实际抽走总量 kg */
    double removeGas(double kg) {
        if (units.length == 0) {
            return 0;
        }
        double per = Math.max(0.0, kg) / units.length;
        double total = 0;
        for (Condenser c : units) total += c.removeGas(per);
        return total;
    }

    // ------------------------------------------------------------------
    // Connectable：对外表现为"一个凝汽器"
    // ------------------------------------------------------------------
    @Override public double getPressure() { return meanPressure(); }
    @Override public double getSteamDensity() {
        return Loader.tables.getSteamDensityByPressure(Math.max(0.0042, meanPressure()));
    }
    @Override public double getWaterDensity() {
        double s = 0;
        for (Condenser c : units) s += c.getWaterDensity();
        return units.length == 0 ? 1 : s / units.length;
    }
    @Override public double getSteamMass() {
        double s = 0;
        for (Condenser c : units) s += c.getSteamMass();
        return s;
    }
    @Override public double getWaterMass() {
        double s = 0;
        for (Condenser c : units) s += c.getWaterMass();
        return s;
    }
    @Override public double getSteamVolume() {
        double s = 0;
        for (Condenser c : units) s += c.getSteamVolume();
        return s;
    }
    @Override public double getWaterTemperature() {
        double s = 0;
        for (Condenser c : units) s += c.getWaterTemperature();
        return units.length == 0 ? 0 : s / units.length;
    }
    @Override public double getSteamTemperature() {
        double s = 0;
        for (Condenser c : units) s += c.getSteamTemperature();
        return units.length == 0 ? 0 : s / units.length;
    }
    @Override public double getWaterLevel() {
        double s = 0;
        for (Condenser c : units) s += c.getWaterLevel();
        return units.length == 0 ? 0 : s / units.length;
    }
    /** 4 台凝汽器热井里的凝结水总量 kg（主系统质量账用） */
    public double getHotwellMassKg() {
        double s = 0;
        for (Condenser c : units) {
            s += c.getHotwellMassKg();
        }
        return s;
    }

    /** 4 台凝汽器热井水位最大值 %（报警用） */
    public double getMaxHotwellLevel() {
        double m = -1e9;
        for (Condenser c : units) {
            m = Math.max(m, c.getWaterLevel());
        }
        return units.length == 0 ? 0 : m;
    }

    public double getWaterOutflowRate() {
        double s = 0;
        for (Condenser c : units) s += c.getWaterOutflowRate();
        return s;
    }

    /** 进汽按 1/4 均分（各台壳体并联，蒸汽在母管里均压） */
    @Override public void updateSteamInflow(double flow, double tempC) {
        if (flow <= 0 || units.length == 0) return;
        double each = flow / units.length;
        for (Condenser c : units) {
            c.updateSteamInflow(each, tempC);
        }
    }

    /** 抽气泵抽走的不凝气也按 1/4 均摊 */
    @Override public void updateSteamOutflow(double flow, double tempC) {
        if (flow <= 0 || units.length == 0) return;
        double each = flow / units.length;
        for (Condenser c : units) {
            c.updateSteamOutflow(each, tempC);
        }
    }

    /** 凝结水泵从 4 个热井平均取水 */
    @Override public void updateWaterOutflow(double flow, double tempC) {
        if (flow <= 0 || units.length == 0) return;
        double each = flow / units.length;
        for (Condenser c : units) {
            c.updateWaterOutflow(each, tempC);
        }
    }

    @Override public void updateWaterInflow(double flow, double tempC) { }

    /** 可供排汽量 = 4 台汽空间之和 */
    @Override public double availableSteamKg() {
        double s = 0;
        for (Condenser c : units) s += c.availableSteamKg();
        return s;
    }
}
