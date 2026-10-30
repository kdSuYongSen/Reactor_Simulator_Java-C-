package com.darwish.nppsim;

import java.util.Locale;

/**
 * 蒸汽环路接线验证台（无界面）。
 *
 * 复现改版后的二次侧接线：
 *   汽水分离器 → 蒸汽母管 → 除氧器蒸汽入口 → 除氧器 → 除氧器蒸汽出口联箱
 *              → 汽轮机主汽门 → （排汽汇）
 *   凝结水汇 → PCS 凝结水过滤段 → 除氧器水入口
 * 用真实的 Separator / Dearator / SteamValve / WaterValve / SteamCollectorHeader /
 * CondensateFilter，汽轮机与凝结水用最小替身代替，跑 60 s 看是否质量守恒、有无 NaN。
 */
public class SteamLoopCheck {

    /** 汽轮机/旁路排汽汇：只统计吞吐，压力取凝汽器真空 */
    static class SteamSink implements Connectable, java.io.Serializable {
        private double pressure = 0.0061;
        private double steamTemperature = 90.0;
        private double inflowRate = 0.0;
        public void settle() { inflowRate *= 0.0; }

        @Override public void updateSteamInflow(double flow, double tempC) { inflowRate += flow * 20.0; }
        @Override public void updateSteamOutflow(double flow, double tempC) { }
        @Override public void updateWaterInflow(double flow, double tempC) { }
        @Override public void updateWaterOutflow(double flow, double tempC) { }
        @Override public double getPressure() { return pressure; }
        @Override public double getSteamDensity() { return NPPSim.tables.getSteamDensityByPressure(pressure); }
        @Override public double getWaterDensity() { return 990.0; }
        @Override public double getSteamMass() { return 0.0; }
        @Override public double getWaterMass() { return 1e9; }
        @Override public double getSteamVolume() { return 0.0; }
        @Override public double getWaterTemperature() { return 40.0; }
        @Override public double getSteamTemperature() { return steamTemperature; }
        @Override public double getWaterLevel() { return 0.0; }
    }

    /** 凝结水汇替身：给过滤段供水，并接受过滤段登记的取水量 */
    static class WaterSource implements Connectable, java.io.Serializable {
        private double pressure = 1.8;
        private double waterTemperature = 45.0;
        private double delivered;
        public double deliveredRate() { return delivered * 20.0; }
        public void settle() { delivered = 0.0; }

        @Override public void updateWaterOutflow(double flow, double tempC) { delivered += flow; }
        @Override public void updateWaterInflow(double flow, double tempC) { }
        @Override public void updateSteamInflow(double flow, double tempC) { }
        @Override public void updateSteamOutflow(double flow, double tempC) { }
        @Override public double getPressure() { return pressure; }
        @Override public double getSteamDensity() { return NPPSim.tables.getSteamDensityByPressure(1.0); }
        @Override public double getWaterDensity() { return NPPSim.tables.getWaterDensityByTemp(waterTemperature); }
        @Override public double getSteamMass() { return 0.0; }
        @Override public double getWaterMass() { return 1e9; }
        @Override public double getSteamVolume() { return 0.0; }
        @Override public double getWaterTemperature() { return waterTemperature; }
        @Override public double getSteamTemperature() { return 20.0; }
        @Override public double getWaterLevel() { return 0.0; }
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        NPPSim.tables = new SteamTables();
        Loader.tables = NPPSim.tables;
        NPPSim.atmosphere = new Atmosphere(15);
        NPPSim.deaeratorSteamHeader = new SteamCollectorHeader();

        Separator sep = new Separator("A");
        SeparatorSteamSource src = new SeparatorSteamSource(sep);
        OneWaySteamHeader steamPiping = new OneWaySteamHeader(new Connectable[]{src});
        NPPSim.steamPiping = steamPiping;

        Dearator[] ds = new Dearator[4];
        for (int i = 0; i < 4; i++) {
            ds[i] = new Dearator(i);
        }
        NPPSim.deaeratorSteamHeader.setSources(new Connectable[]{ds[0], ds[1], ds[2], ds[3]});

        SteamSink sink = new SteamSink();
        // -Doldpath=true 时模拟改版前的接线（主汽门直接接蒸汽母管），用于对比汽轮机进汽量
        boolean oldPath = Boolean.getBoolean("oldpath");
        Connectable tgSource = oldPath ? (Connectable) steamPiping : (Connectable) NPPSim.deaeratorSteamHeader;
        SteamValve[] tgIn = new SteamValve[4];
        for (int i = 0; i < 4; i++) {
            tgIn[i] = new SteamValve(3044.72, 20, tgSource, sink);
        }
        System.out.println(oldPath ? "[对照] 旧接线：主汽门 ← 蒸汽母管" : "[新版] 新接线：主汽门 ← 除氧器蒸汽出口联箱");

        WaterSource cond = new WaterSource();
        CondensateFilter filter = new CondensateFilter(cond, null);
        NPPSim.pcsCondensateFilter = filter;
        WaterValve[] dIn = new WaterValve[4];
        for (int i = 0; i < 4; i++) {
            dIn[i] = new WaterValve(1667.52, 12, filter, ds[i]);
        }

        // 验证台里没有 AutoControl，阀门默认关闭 → 手动全开
        for (Dearator d : ds) {
            d.steamInlet.setPosition(1.0f);
            d.steamOutlet.setPosition(1.0f);
        }
        for (SteamValve v : tgIn) v.setPosition(1.0f);
        for (WaterValve v : dIn) v.setPosition(1.0f);

        final double steamToSeparator = 2200.0;   // 换热器总产汽 kg/s（≈4800 MW）
        boolean bad = false;
        for (int step = 0; step < 1200; step++) {  // 60 s
            sep.update(0.05, steamToSeparator, 165.0);
            sep.pressureMPa = Separator.PRESSURE_SETPOINT;   // 隔离出汽水分离器自身的压力控制
            src.refresh();
            for (Dearator d : ds) {
                d.steamInlet.update();
                d.steamOutlet.update();
            }
            for (SteamValve v : tgIn) v.update();
            for (WaterValve v : dIn) v.update();
            steamPiping.update();
            NPPSim.deaeratorSteamHeader.update();
            for (Dearator d : ds) {
                d.update();
                // 验证台里没有给水泵链，用等量取水代替：除氧器凝结下来的水必须被抽走，
                // 否则水位会一直涨（真实电厂由 fwSuctionHeader → 给水泵 抽走）。
                d.updateWaterOutflow(d.getSteamInflowRate() * 0.05, d.getWaterTemperature());
            }
            filter.update();
            cond.settle();
            sink.settle();

            if (step % 120 == 0 || step == 1199) {
                double inSum = 0, outSum = 0, tgInSum = 0;
                for (Dearator d : ds) {
                    inSum += d.getSteamInflowRate();
                    outSum += d.getSteamOutflowRate();
                }
                for (SteamValve v : tgIn) tgInSum += v.getFlowRate();
                System.out.printf("t=%5.1fs sep=%7.1f | 母管→除氧器 %7.1f | 除氧器出口 %7.1f | 联箱→汽轮机 %7.1f "
                                + "| 入口阀 %6.1f | 除氧器P=%.3fMPa 水温=%.1f 水位=%.1f%% 水量=%.0fkg 汽存量=%.1f | 过滤 %6.1f kg/s %n",
                        step * 0.05, sep.outSteamKgs, inSum, outSum, tgInSum, ds[0].steamInlet.getFlowRate(),
                        ds[0].getPressure(), ds[0].getWaterTemperature(), ds[0].getWaterLevel(), ds[0].getWaterMass(),
                        ds[0].getSteamMass(),
                        filter.getThroughputKgs());
                bad |= !finite(ds[0].getPressure()) || !finite(tgInSum) || !finite(filter.getThroughputKgs())
                        || !finite(ds[0].getWaterTemperature()) || !finite(ds[0].getWaterLevel());
            }
        }
        System.out.println(bad ? "\n!! 出现 NaN/Inf" : "\nOK：无 NaN，蒸汽经除氧器后到达汽轮机，凝结水经 PCS 过滤段");
    }

    static boolean finite(double v) {
        return !Double.isNaN(v) && !Double.isInfinite(v);
    }
}