package com.darwish.nppsim;

import java.util.Locale;

/**
 * 二回路质量账本探针（无界面）。
 *
 * 目的：逐 tick 核对"给水/蒸汽"这条闭环的质量守恒，找出水到底漏在哪里。
 * 跟踪 4 台除氧器 + 2 台换热器管侧 + 除氧器出口联箱的存量，以及所有进出口流量。
 */
public class MassLedger {

    static class ExhaustSink implements Connectable, java.io.Serializable {
        private double pressure;
        private double inflow = 0.0;
        ExhaustSink(double p) { pressure = p; }
        double tempC() { return Loader.tables.getSteamTemperatureByPressure(pressure); }
        @Override public void updateSteamInflow(double flow, double tempC) { inflow += flow * 20.0; }
        @Override public void updateSteamOutflow(double flow, double tempC) { }
        @Override public void updateWaterInflow(double flow, double tempC) { }
        @Override public void updateWaterOutflow(double flow, double tempC) { }
        @Override public double getPressure() { return pressure; }
        @Override public double getSteamDensity() { return Loader.tables.getSteamDensityByPressure(pressure); }
        @Override public double getWaterDensity() { return Loader.tables.getWaterDensityByTemp(tempC()); }
        @Override public double getSteamMass() { return 0.0; }
        @Override public double getWaterMass() { return 1e9; }
        @Override public double getSteamVolume() { return 0.0; }
        @Override public double getWaterTemperature() { return tempC(); }
        @Override public double getSteamTemperature() { return tempC(); }
        @Override public double getWaterLevel() { return 0.0; }
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        NPPSim.tables = new SteamTables();
        Loader.tables = NPPSim.tables;

        NPPSim.deaeratorSteamHeader = new SteamCollectorHeader();
        Separator[] sep = {new Separator("A"), new Separator("B")};
        SeparatorSteamSource[] src = {new SeparatorSteamSource(sep[0]), new SeparatorSteamSource(sep[1])};
        sep[0].setSteamSource(src[0]);
        sep[1].setSteamSource(src[1]);
        OneWaySteamHeader steamPiping = new OneWaySteamHeader(new Connectable[]{src[0], src[1]});
        NPPSim.steamPiping = steamPiping;

        HeatExchanger[] hx = {new HeatExchanger("A"), new HeatExchanger("B")};
        Dearator[] dea = new Dearator[4];
        for (int i = 0; i < 4; i++) dea[i] = new Dearator(i);
        NPPSim.deaeratorSteamHeader.setSources(new Connectable[]{dea[0], dea[1], dea[2], dea[3]});

        ExhaustSink[] cond = {new ExhaustSink(0.0061), new ExhaustSink(0.0061)};
        SteamValve[][] tgv = new SteamValve[2][2];
        for (int t = 0; t < 2; t++) {
            for (int v = 0; v < 2; v++) {
                tgv[t][v] = new SteamValve(25000, 20, NPPSim.deaeratorSteamHeader, cond[t]);
                tgv[t][v].setPosition(1.0f);
            }
        }
        for (Dearator d : dea) {
            d.steamInlet.setPosition(1.0f);
            d.steamOutlet.setPosition(1.0f);
        }

        double[] deaWaterIn = new double[4];
        int steps = Integer.parseInt(System.getProperty("steps", "24000"));
        double prevWater = 0;
        for (int step = 0; step < steps; step++) {
            for (int i = 0; i < 2; i++) {
                hx[i].updatePrimary(6.9, 284.0, 8000, 0.05);
                hx[i].updateSecondary(0.05, sep[i].pressureMPa);
                sep[i].update(0.05, hx[i].steamOutKgs, hx[i].secondarySteamTC);
                sep[i].pressureMPa = Separator.PRESSURE_SETPOINT;
                src[i].refresh();
            }
            for (Dearator d : dea) { d.steamInlet.update(); d.steamOutlet.update(); }
            for (int t = 0; t < 2; t++) for (int v = 0; v < 2; v++) tgv[t][v].update();
            for (Dearator d : dea) d.update();
            NPPSim.deaeratorSteamHeader.update();
            steamPiping.update();

            // 与真实电厂一致：凝结水经 4 只除氧器水入口阀均分给 4 台除氧器
            // （旧写法把每台汽机的排汽只分给 2 台除氧器，另外 2 台永远不进凝结水，
            //  验证台自己的控制模型又把它们抽到最小流量，看起来就像"水漏了"）
            double totalExhaust = cond[0].inflow + cond[1].inflow;
            double exhaustTick = totalExhaust * 0.05;
            for (int i = 0; i < 4; i++) {
                dea[i].updateWaterInflow(exhaustTick / 4, cond[i / 2].tempC());
                deaWaterIn[i] = totalExhaust / 4
                        + (dea[i].steamInlet.getFlowRate() - dea[i].getPassKgs());
            }
            for (int i = 0; i < 4; i++) {
                // 给水泵按除氧器水位调节（真实电厂就是除氧器水位控制器）：
                // 水位高于设定值就减泵，低于就加泵。旧写法用"上 tick 进水量"当前馈，
                // 对不进汽的除氧器会一直按最小流量抽，水位缓慢漏光。
                double lvlErr = dea[i].getWaterLevel() - 5.0;
                double pump = Math.max(0.0, Math.min(1500.0, 700.0 - lvlErr * 200.0));
                double fwTick = Math.min(pump * 0.05, dea[i].getWaterMass() * 0.02);
                dea[i].updateWaterOutflow(fwTick, dea[i].getWaterTemperature());
                hx[i / 2].updateWaterInflow(fwTick, dea[i].getWaterTemperature());
                // 注意：每台换热器带 2 台除氧器，退水量要**对半分**，
                // 否则同一股水被计两次（验证台自己会"造水"）。
                if (hx[i / 2].secondaryDrainKgs > 0.0) {
                    dea[i].updateWaterInflow(hx[i / 2].secondaryDrainKgs * 0.05 / 2, hx[i / 2].getWaterTemperature());
                }
            }
            // 分离器甩出的水：4 台均分
            double sepWater = (sep[0].outWaterKgs + sep[1].outWaterKgs) * 0.05;
            for (int i = 0; i < 4; i++) {
                dea[i].updateWaterInflow(sepWater / 4, sep[0].outTempC);
            }
            for (ExhaustSink c : cond) c.inflow = 0.0;

            if (step % 600 == 0) {
                double water = 0, hxInv = 0;
                for (Dearator d : dea) water += d.getWaterMass();
                for (HeatExchanger h : hx) hxInv += h.getWaterMass();
                double feed = 0, steam = 0, pass = 0, inlet = 0;
                for (int i = 0; i < 4; i++) {
                    feed += deaWaterIn[i];
                    pass += dea[i].getPassKgs();
                    inlet += dea[i].steamInlet.getFlowRate();
                }
                for (int i = 0; i < 2; i++) steam += hx[i].steamOutKgs;
                double drift = (step == 0) ? 0 : (water - prevWater) / (30.0);
                prevWater = water;
                double hxFeed = 0, hxSteam = 0, hxBuf = 0;
                for (int i = 0; i < 2; i++) {
                    hxFeed += hx[i].secondaryFeedKgs;
                    hxSteam += hx[i].secondarySteamKgs;
                    hxBuf += Math.min(2.0, hx[i].getWaterMass() * 0.02);
                }
                double tgFlow = 0, wIn = 0, wOut = 0;
                for (int t = 0; t < 2; t++) for (int v = 0; v < 2; v++) tgFlow += tgv[t][v].getFlowRate();
                for (int i = 0; i < 4; i++) { wIn += dea[i].getWaterInflowRate(); wOut += dea[i].getWaterOutflowRate(); }
                System.out.printf("t=%6.0fs 除氧器水=%8.0fkg 换热器存=%7.0fkg | 给水=%6.0f 产汽=%6.0f 汽机=%6.0f | "
                                + "联箱进=%6.0f 出=%6.0f 安全阀=%6.1f | 除氧器进水=%6.0f 出水=%6.0f | 漂移=%+7.1f kg/s%n",
                        step * 0.05, water, hxInv, hxFeed, hxSteam, tgFlow,
                        NPPSim.deaeratorSteamHeader.getInflowKgs(), NPPSim.deaeratorSteamHeader.getOutflowKgs(),
                        NPPSim.deaeratorSteamHeader.getReliefKgs(), wIn, wOut, drift);
            }
        }
    }
}
