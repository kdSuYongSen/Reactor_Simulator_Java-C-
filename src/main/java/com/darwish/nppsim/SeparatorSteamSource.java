package com.darwish.nppsim;

import java.io.Serializable;

/**
 * 把汽水分离器的出口蒸汽包装成 Connectable，作为二次侧蒸汽母管（steamPiping）的源。
 *
 * 二次侧：给水 → 换热器 → 汽水分离器 → **本适配器** → 除氧器蒸汽入口 / 汽轮机 / 旁路。
 * 母管每帧按压力比例向各源抽取，本适配器只允许抽走分离器实际产出的蒸汽量，
 * 避免“汽轮机拿到的汽比分离器产出的还多”。
 */
class SeparatorSteamSource implements Connectable, SteamSupplyLimit, Serializable {
    private static final long serialVersionUID = 1L;

    final Separator separator;
    private double availableKg = 0.0;   // 本 tick 可供 kg
    private double drawnKg = 0.0;       // 本 tick 已抽走 kg
    /** 分离器汽空间缓存 kg：产汽与抽汽不在同一瞬间完成，缓存吸收一个 tick 内的波动。
     *  没有它时，4 只除氧器入口阀在同一 tick 里抢"本 tick 产汽量"，先开的阀抽光、
     *  后面的阀一只汽都拿不到（实测 4 台除氧器只有 2 台进汽）。 */
    private double bufferKg = 800.0;
    private static final double BUFFER_MAX = 2000.0;
    /** 上一 tick 抽汽速率 kg/s（供分离器压力响应） */
    private double lastDrawKgs = 0.0;

    SeparatorSteamSource(Separator separator) {
        this.separator = separator;
    }

    /** 每帧刷新可供量（在分离器 update 之后调用） */
    void refresh() {
        double production = Math.max(0.0, separator.outSteamKgs) * 0.05;   // 本 tick 产汽 kg
        bufferKg = Math.max(0.0, Math.min(BUFFER_MAX, bufferKg + production - drawnKg));
        availableKg = production + bufferKg;
        lastDrawKgs = drawnKg * 20.0;
        drawnKg = 0.0;
    }

    /** 汽空间缓存比例 0..1（分离器据此调压力：泄空 → 压力跌 → 阀门自限流） */
    double bufferFraction() {
        return bufferKg / 800.0;
    }

    /** 上一 tick 的抽汽速率 kg/s */
    double lastDrawRate() {
        return lastDrawKgs;
    }

    @Override public void updateSteamOutflow(double flow, double tempC) {
        double room = Math.max(0.0, availableKg - drawnKg);
        drawnKg += Math.min(Math.max(0.0, flow), room);
    }

    @Override public double getPressure() {
        return Math.max(0.05, separator.pressureMPa);
    }

    @Override public double getSteamDensity() {
        return Loader.tables.getSteamDensityByPressure(Math.max(0.05, separator.pressureMPa));
    }

    @Override public double getWaterDensity() {
        return Loader.tables.getWaterDensityByTemp(Math.max(20.0, separator.outTempC));
    }

    @Override public double getSteamMass() { return 0.0; }
    @Override public double getWaterMass() { return 0.0; }
    @Override public double getSteamVolume() { return 0.0; }
    @Override public double getWaterTemperature() { return separator.outTempC; }
    @Override public double getSteamTemperature() { return separator.outTempC; }
    @Override public double getWaterLevel() { return 0.0; }
    /** 分离器本帧还没被抽走的产汽量 */
    @Override public double availableSteamKg() {
        return Math.max(0.0, availableKg - drawnKg);
    }

    @Override public void updateSteamInflow(double flow, double tempC) { }
    @Override public void updateWaterOutflow(double flow, double tempC) { }
    @Override public void updateWaterInflow(double flow, double tempC) { }
}
