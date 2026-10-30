package com.darwish.nppsim;

import java.util.Locale;

/**
 * 新二回路（抽汽式）稳态验证台。
 *
 * 拓扑（与 NPPSim 2026-10-19 改版一致）：
 *   一次侧汽包 6.9 MPa/284 °C → 换热器壳侧（凝结）→ 回注汽包
 *   二回路 4.0 MPa：给水 → 换热器管侧 → 汽水分离器 → 主蒸汽母管
 *        → 主汽门（直供）→ 汽轮机 → 凝汽器
 *        → 抽汽口 0.60 MPa → 除氧器（0.53 MPa）→ 给水
 *
 * 汽轮机流量按 产汽 − 抽汽 结算；功率用 TG 的熵法公式，抽汽段单独计入。
 */
public class SecondaryV2Check {

    /** 每台给水泵流量 kg/s（-Dfw 覆盖）；稳态时总量应等于产汽量 */
    static final double FW_KGS = Double.parseDouble(System.getProperty("fw", "560"));

    /** 抽汽口替身：0.60 MPa，可供量按汽包产汽给足 */
    static class ExtractionSource implements Connectable, SteamSupplyLimit {
        double pressure = 0.60;
        double drawnKgs = 0.0;
        double drawnKgTick = 0.0;
        double limitKgTick = Double.MAX_VALUE;
        @Override public double getPressure() { return pressure; }
        @Override public double getSteamDensity() { return Loader.tables.getSteamDensityByPressure(pressure); }
        @Override public double getWaterDensity() { return getSteamDensity(); }
        @Override public double getSteamMass() { return 1e9; }
        @Override public double getWaterMass() { return 0.0; }
        @Override public double getSteamVolume() { return 0.0; }
        @Override public double getWaterTemperature() { return getSteamTemperature(); }
        @Override public double getSteamTemperature() { return Loader.tables.getSteamTemperatureByPressure(pressure); }
        @Override public double getWaterLevel() { return 0.0; }
        @Override public void updateSteamOutflow(double flow, double tempC) { drawnKgTick += flow; drawnKgs += flow * 20.0; }
        @Override public void updateSteamInflow(double flow, double tempC) { }
        @Override public void updateWaterOutflow(double flow, double tempC) { }
        @Override public void updateWaterInflow(double flow, double tempC) { }
        /** 与 TG 抽汽口同口径：最多产汽的 35% */
        @Override public double availableSteamKg() { return Math.max(0.0, limitKgTick - drawnKgTick); }
    }

    /** 凝汽器替身：只收质量、定压 */
    static class Sink implements Connectable {
        double pressure;
        double inflowKgs = 0.0;
        Sink(double p) { pressure = p; }
        double tCond() { return Loader.tables.getSteamTemperatureByPressure(pressure); }
        @Override public double getPressure() { return pressure; }
        @Override public double getSteamDensity() { return Loader.tables.getSteamDensityByPressure(pressure); }
        @Override public double getWaterDensity() { return Loader.tables.getWaterDensityByTemp(tCond()); }
        @Override public double getSteamMass() { return 1e9; }
        @Override public double getWaterMass() { return 1e9; }
        @Override public double getSteamVolume() { return 0.0; }
        @Override public double getWaterTemperature() { return tCond(); }
        @Override public double getSteamTemperature() { return tCond(); }
        @Override public double getWaterLevel() { return 0.0; }
        @Override public void updateSteamInflow(double flow, double tempC) { inflowKgs += flow * 20.0; }
        @Override public void updateSteamOutflow(double flow, double tempC) { }
        @Override public void updateWaterInflow(double flow, double tempC) { }
        @Override public void updateWaterOutflow(double flow, double tempC) { }
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        NPPSim.tables = new SteamTables();
        Loader.tables = NPPSim.tables;

        final double drumP = 6.9, drumT = 284.0;
        final double coreMW = 4800.0;

        NPPSim.deaeratorSteamHeader = new SteamCollectorHeader();
        Separator[] sep = {new Separator("A"), new Separator("B")};
        SeparatorSteamSource[] src = {new SeparatorSteamSource(sep[0]), new SeparatorSteamSource(sep[1])};
        OneWaySteamHeader steamPiping = new OneWaySteamHeader(new Connectable[]{src[0], src[1]});
        NPPSim.steamPiping = steamPiping;

        HeatExchanger[] hx = {new HeatExchanger("A"), new HeatExchanger("B")};
        ExtractionSource ext = new ExtractionSource();
        ExtractionSource lpExt = new ExtractionSource();
        lpExt.pressure = 0.15;                      // 低压加热器抽汽口
        Sink cond = new Sink(0.0061);

        Dearator[] dea = new Dearator[4];
        for (int i = 0; i < 4; i++) {
            dea[i] = new Dearator(i, ext, cond);
            dea[i].steamInlet.setPosition(1.0f);
        }

        /* 凝结水链（与 NPPSim 一致）：
         *   凝汽器 → 精处理(CCPU) → 低压加热器 LP-1 → 除氧器 */
        CondensateFilter ccpu = new CondensateFilter(cond, null);
        LPHeater lp = new LPHeater(new Connectable[]{lpExt}, dea, ccpu);

        // 主汽门：直供（4 只，按 4 MPa 定径）
        SteamValve[] tgv = new SteamValve[4];
        for (int i = 0; i < 4; i++) {
            tgv[i] = new SteamValve(4.9e3, 20, steamPiping, cond);
            tgv[i].setPosition(1.0f);
        }

        int steps = Integer.parseInt(System.getProperty("steps", "24000"));
        double[] deaIn = new double[4];
        double[] fwFilt = {560, 560, 560, 560};
        double qSec = 0, qPri = 0;
        for (int step = 0; step < steps; step++) {
            // ---- 一次侧 → 换热器（每台 6000 kg/s，两台 12000）----
            qPri = 0;
            qSec = 0;
            for (int i = 0; i < 2; i++) {
                hx[i].updatePrimary(drumP, drumT, 6000, 0.05);
                double kg = hx[i].primarySteamInKgs * 0.05;
                double cap = hx[i].maxCondensableKg(0.05);
                if (kg > cap) hx[i].applyActualDraw(cap, 0.05);
                hx[i].updateSecondary(0.05, sep[i].pressureMPa);
                qPri += hx[i].primaryHeatMW;
                qSec += hx[i].secondaryHeatMW;
                sep[i].update(0.05, hx[i].steamOutKgs, hx[i].secondarySteamTC);
                sep[i].pressureMPa = Separator.PRESSURE_SETPOINT;
                src[i].refresh();
                // 分离器甩出的水回收到除氧器（与 NPPSim 一致，否则 12% 质量会"消失"）
                dea[i].updateWaterInflow(sep[i].outWaterKgs * 0.05, sep[i].outTempC);
            }
            steamPiping.update();

            // ---- 除氧器抽汽 + 主汽门 ----
            ext.drawnKgs = 0.0;
            ext.drawnKgTick = 0.0;
            double steamNow = sep[0].outSteamKgs + sep[1].outSteamKgs;
            ext.limitKgTick = steamNow * 0.35 * 0.05;
            for (Dearator d : dea) { d.steamInlet.update(); d.steamOutlet.update(); }
            for (int i = 0; i < 4; i++) tgv[i].update();
            for (Dearator d : dea) d.update();

            // ---- 给水：除氧器 → 换热器（按上 tick 的进水 + 水位修正）----
            for (int i = 0; i < 4; i++) {
                // 稳态核对：固定给水（-Dfw 指定每台 kg/s，默认 560 = 四台 2240）
                double pump = FW_KGS;
                double fwTick = Math.min(pump * 0.05, dea[i].getWaterMass() * 0.02);
                dea[i].updateWaterOutflow(fwTick, dea[i].getWaterTemperature());
                hx[i / 2].updateWaterInflow(fwTick, dea[i].getWaterTemperature());
                if (hx[i / 2].secondaryDrainKgs > 0.0) {
                    dea[i].updateWaterInflow(hx[i / 2].secondaryDrainKgs * 0.05, hx[i / 2].getWaterTemperature());
                }
            }
            // ---- 凝结水：凝汽器热井 → 低压加热器 → 除氧器 ----
            double condReturn = 0;
            for (SteamValve v : tgv) condReturn += v.getFlowRate();
            for (Dearator d : dea) condReturn += d.getPassKgs();
            lpExt.drawnKgs = 0.0;
            lpExt.drawnKgTick = 0.0;
            lpExt.limitKgTick = Double.MAX_VALUE;
            lp.updateWaterOutflow(condReturn * 0.05, cond.tCond());
            lp.update();          // 加热（顺带把需求登记给上游精处理段）
            ccpu.update();        // 精处理段向凝汽器热井要水
            double lpOutT = lp.getWaterTemperature();
            for (int k = 0; k < 4; k++) {
                dea[k].updateWaterInflow(condReturn / 4 * 0.05, lpOutT);
                deaIn[k] = condReturn / 4;
            }

            if (step % 2400 == 0 || step == steps - 1) {
                double steam = sep[0].outSteamKgs + sep[1].outSteamKgs;
                double extKgs = ext.drawnKgs;
                double tgFlow = 0;
                for (SteamValve v : tgv) tgFlow += v.getFlowRate();
                double pIn = steamPiping.getPressure();
                double tIn = sep[0].outTempC;
                double wFull = TG.specificWorkKjPerKg(tIn, cond.pressure);
                double wExt = TG.specificWorkKjPerKg(tIn, ext.pressure);
                double wLp = TG.specificWorkKjPerKg(tIn, lpExt.pressure);
                double lpKgs = lp.getExtractionKgs();
                /* 质量守恒口径：产汽 = 汽机进汽 + 两级抽汽。抽汽口在汽轮机内部，
                 * 所以真正流过汽轮机的是 (产汽 − 抽汽)，各抽汽段只做到该口压力的功。 */
                double turbReal = Math.max(0.0, steam - extKgs - lpKgs);
                double pe = (turbReal - 40) * wFull * 0.98 / 1000.0
                        + extKgs * wExt * 0.98 / 1000.0
                        + lpKgs * wLp * 0.98 / 1000.0;
                System.out.printf(
                        "t=%5.0fs 一次侧 %.0fMW | 产汽 %6.0f | 抽汽 %5.0f 汽机 %6.0f | 母管 %.2f MPa %.0fC | "
                        + "w=%.0f/%.0f/%.0f | 汽机实流 %6.0f | 电 %6.1f MW | 净效率 %4.1f%% "
                        + "| 精处理 %.0fC | LP %.0fC 抽 %3.0f | 除氧器 %.0fC | 余汽 %.0f%n",
                        step * 0.05, qPri, steam, extKgs, tgFlow, pIn, tIn,
                        wFull, wExt, wLp, turbReal, pe, pe / coreMW * 100,
                        ccpu.getWaterTemperature(), lpOutT, lpKgs, dea[0].getWaterTemperature(), dea[0].getPassKgs());
            }
        }
        System.out.printf("%n稳态：一次侧 %.0f MW，二次侧 %.0f MW（差 %+.1f%%）%n",
                qPri, qSec, (qSec - qPri) / qPri * 100);
    }
}
