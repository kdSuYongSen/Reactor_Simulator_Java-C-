package com.darwish.nppsim;

import java.util.Locale;

/**
 * 主汽门定径台：给定除氧器出口联箱压力，扫描 Cv → 单阀流量 → 发电机功率。
 *
 * 目的：把"汽轮机到底能过多少汽、能发多少电"变成实测数字，而不是拍脑袋。
 * 只用真实的 SteamValve / SteamCollectorHeader / 蒸汽表，汽机侧用固定背压的汇。
 */
public class TurbineSizing {

    static class Fixed implements Connectable, java.io.Serializable {
        final double pressure, temp;
        double inflow;
        Fixed(double p, double t) { pressure = p; temp = t; }
        @Override public void updateSteamInflow(double flow, double tempC) { inflow += flow; }
        @Override public void updateSteamOutflow(double flow, double tempC) { }
        @Override public void updateWaterInflow(double flow, double tempC) { }
        @Override public void updateWaterOutflow(double flow, double tempC) { }
        @Override public double getPressure() { return pressure; }
        @Override public double getSteamDensity() { return Loader.tables.getSteamDensityByPressure(pressure); }
        @Override public double getWaterDensity() { return Loader.tables.getWaterDensityByTemp(temp); }
        @Override public double getSteamMass() { return 0.0; }
        @Override public double getWaterMass() { return 0.0; }
        @Override public double getSteamVolume() { return 0.0; }
        @Override public double getWaterTemperature() { return temp; }
        @Override public double getSteamTemperature() { return temp; }
        @Override public double getWaterLevel() { return 0.0; }
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        NPPSim.tables = new SteamTables();
        Loader.tables = NPPSim.tables;

        // 除氧器出口联箱 ≈ 0.61 MPa（母管 0.70 − 除氧器 0.07 − 管线 0.02）
        Fixed header = new Fixed(0.61, 160.0);
        SteamCollectorHeader hdr = new SteamCollectorHeader();
        hdr.setSources(new Connectable[]{header});
        hdr.update();

        Fixed condenser = new Fixed(0.0061, 36.0);

        System.out.printf("联箱 %.3f MPa / %.1f C   凝汽器 %.4f MPa%n",
                hdr.getPressure(), hdr.getSteamTemperature(), condenser.getPressure());

        // 焓降：主蒸汽饱和汽 → 凝汽器压力下的湿蒸汽（等熵干度取 0.80）
        double hIn = NPPSim.tables.getSpecificVaporEnthalpyByTemperature(hdr.getSteamTemperature());
        double tCond = NPPSim.tables.getSteamTemperatureByPressure(condenser.getPressure());
        double hf = NPPSim.tables.getWaterEnthalpyByTemperature(tCond);
        double hg = NPPSim.tables.getSpecificVaporEnthalpyByTemperature(tCond);
        double h2s = hf + 0.80 * (hg - hf);
        double dhS = hIn - h2s;
        System.out.printf("主蒸汽 h=%.0f  凝汽器 hf=%.0f hg=%.0f  h2s=%.0f  等熵焓降=%.0f kJ/kg%n",
                hIn, hf, hg, h2s, dhS);
        System.out.printf("%-10s %-12s %-12s %-12s %-12s%n", "Cv", "单阀kg/s", "2阀kg/s", "轴功率MW", "电功率MW");

        double[] cvs = {3044.72, 1.0e4, 1.4e4, 1.7e4, 2.0e4, 2.4e4, 2.8e4, 3.2e4};
        for (double cv : cvs) {
            SteamValve v = new SteamValve(cv, 20, hdr, condenser);
            v.setPosition(1.0f);
            for (int i = 0; i < 20; i++) v.update();
            double perValve = v.getFlowRate();
            double perTurbine = perValve * 2.0;
            double shaft = perTurbine * dhS * 0.88 / 1000.0;     // 88% 等熵效率
            double elec = shaft * 0.98;
            System.out.printf("%-10.0f %-12.1f %-12.1f %-12.1f %-12.1f%n",
                    cv, perValve, perTurbine, shaft, elec);
        }
    }
}
