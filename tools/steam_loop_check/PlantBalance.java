package com.darwish.nppsim;

import java.util.Locale;

/**
 * 全厂能量平衡验证台（无界面）。
 *
 * 用真实组件把一整条链路跑起来：
 *   汽包(定压) → 换热器壳侧(冷凝) → 凝结水回注
 *   给水 ← 除氧器 → 换热器管侧(蒸发) → 汽水分离器 → 蒸汽母管
 *        → 除氧器入口阀 → 除氧器 → 出口阀 → 除氧器蒸汽出口联箱
 *        → 主汽门 → （汽轮机排汽汇）
 *   汽轮机排汽 → 凝汽器（只做质量/温度口径）→ 除氧器水入口
 *
 * 目的：证明"4800 MWt → 多少 kg/s 蒸汽 → 多少 MWe"这条账能对上，
 * 并给出主汽门定径（Cv）的实测依据。
 *
 * 用法： java ... PlantBalance [-Dcv=25000]
 */
public class PlantBalance {

    /** 汽轮机排汽汇（兼凝汽器）：按凝汽器真空接收排汽，回水温度取饱和温度 */
    static class ExhaustSink implements Connectable, java.io.Serializable {
        private double pressure;
        private double inflow = 0.0;              // kg/s
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

        final double drumP = 6.9, drumT = 284.0;
        final double coreMW = 4800.0;

        double cv = Double.parseDouble(System.getProperty("cv", "25000"));

        NPPSim.deaeratorSteamHeader = new SteamCollectorHeader();
        Separator[] sep = {new Separator("A"), new Separator("B")};
        SeparatorSteamSource[] src = {new SeparatorSteamSource(sep[0]), new SeparatorSteamSource(sep[1])};
        OneWaySteamHeader steamPiping = new OneWaySteamHeader(new Connectable[]{src[0], src[1]});
        NPPSim.steamPiping = steamPiping;

        HeatExchanger[] hx = {new HeatExchanger("A"), new HeatExchanger("B")};
        Dearator[] dea = new Dearator[4];
        for (int i = 0; i < 4; i++) dea[i] = new Dearator(i);
        NPPSim.deaeratorSteamHeader.setSources(new Connectable[]{dea[0], dea[1], dea[2], dea[3]});

        ExhaustSink[] cond = {new ExhaustSink(0.0061), new ExhaustSink(0.0061)};
        // 每台汽轮机两只主汽门
        SteamValve[][] tgv = new SteamValve[2][2];
        for (int t = 0; t < 2; t++) {
            for (int v = 0; v < 2; v++) {
                tgv[t][v] = new SteamValve(cv, 20, NPPSim.deaeratorSteamHeader, cond[t]);
                tgv[t][v].setPosition(1.0f);
            }
        }
        for (Dearator d : dea) {
            d.steamInlet.setPosition(1.0f);
            d.steamOutlet.setPosition(1.0f);
        }

        // 给水/凝结水用直连代替泵与阀（只保留质量与能量路径）
        double[] deaWaterIn = new double[4];       // 上 tick 每台除氧器的进水量 kg/s（泵的前馈）
        double[] feedToHx = new double[4];         // 本 tick 每台除氧器送出的给水 kg

        double qPrimary = 0, qSecondary = 0, qTurbine = 0;
        int steps = Integer.parseInt(System.getProperty("steps", "2400"));   // 120 s
        for (int step = 0; step < steps; step++) {
            // ---- 一回路 → 换热器 ----
            qPrimary = 0;
            qSecondary = 0;
            for (int i = 0; i < 2; i++) {
                hx[i].updatePrimary(drumP, drumT, 8000, 0.05);
                // 与 NPPSim 同口径：二回路吸收不了的热量不能从一次侧抽走
                double kg = hx[i].primarySteamInKgs * 0.05;
                double cap = hx[i].maxCondensableKg(0.05);
                if (kg > cap) {
                    hx[i].applyActualDraw(cap, 0.05);
                }
                hx[i].updateSecondary(0.05, sep[i].pressureMPa);
                qPrimary += hx[i].primaryHeatMW;
                qSecondary += hx[i].secondaryHeatMW;
                sep[i].update(0.05, hx[i].steamOutKgs, hx[i].secondarySteamTC);
                sep[i].pressureMPa = Separator.PRESSURE_SETPOINT;   // 隔离分离器自身压力控制
                src[i].refresh();
            }

            // ---- 除氧器 + 主汽门 ----
            for (Dearator d : dea) { d.steamInlet.update(); d.steamOutlet.update(); }
            for (int t = 0; t < 2; t++) for (int v = 0; v < 2; v++) tgv[t][v].update();
            for (Dearator d : dea) d.update();
            NPPSim.deaeratorSteamHeader.update();
            steamPiping.update();

            // ---- 排汽 → 凝汽器 → 除氧器水入口；除氧器水出口 → 换热器给水 ----
            qTurbine = 0;
            for (int t = 0; t < 2; t++) {
                double inKgs = cond[t].inflow;
                qTurbine += inKgs * (TG.specificWorkKjPerKg(sep[0].outTempC, cond[t].pressure) / 0.88) / 1000.0;
                double inTick = inKgs * 0.05;
                // 每台汽轮机带 2 台除氧器
                dea[2 * t].updateWaterInflow(inTick / 2, cond[t].tempC());
                dea[2 * t + 1].updateWaterInflow(inTick / 2, cond[t].tempC());
                deaWaterIn[2 * t] = inKgs / 2 + (dea[2 * t].steamInlet.getFlowRate() - dea[2 * t].getPassKgs());
                deaWaterIn[2 * t + 1] = inKgs / 2 + (dea[2 * t + 1].steamInlet.getFlowRate() - dea[2 * t + 1].getPassKgs());
            }
            for (int i = 0; i < 4; i++) {
                // 给水泵：前馈（= 上 tick 实际进水）+ 水位修正（真实除氧器水位控制器）
                double lvlErr = dea[i].getWaterLevel() - 10.0;
                // 理想水位控制：给水 = 上 tick 实际回流（汽轮机排汽 + 除氧器凝结），
                // 这样二回路水量严格闭合，剩下的唯一约束就是换热器的传热能力。
                double pump = (step < 200) ? 560.0 : Math.max(50.0, deaWaterIn[i]);
                if (Boolean.getBoolean("rated")) pump = 700.0;   // 大给水：看主汽门成为瓶颈的额定工况
                double fwTick = Math.min(pump * 0.05, dea[i].getWaterMass() * 0.02);
                dea[i].updateWaterOutflow(fwTick, dea[i].getWaterTemperature());
                hx[i / 2].updateWaterInflow(fwTick, dea[i].getWaterTemperature());
                // 与 NPPSim 一致：换热器没蒸发的给水退回除氧器（否则验证台自己漏质量）
                if (hx[i / 2].secondaryDrainKgs > 0.0) {
                    dea[i].updateWaterInflow(hx[i / 2].secondaryDrainKgs * 0.05, hx[i / 2].getWaterTemperature());
                }
            }
            for (ExhaustSink c : cond) c.inflow = 0.0;

            if (step % 240 == 0 || step == steps - 1) {
                double sepSteam = sep[0].outSteamKgs + sep[1].outSteamKgs;
                double deaIn = 0, deaPass = 0, waterT = 0, level = 0;
                for (Dearator d : dea) {
                    deaIn += d.steamInlet.getFlowRate();
                    deaPass += d.steamOutlet.getFlowRate();
                    waterT += d.getWaterTemperature() / 4;
                    level += d.getWaterLevel() / 4;
                }
                double tgFlow = 0;
                for (int t = 0; t < 2; t++) for (int v = 0; v < 2; v++) tgFlow += tgv[t][v].getFlowRate();
                double pe = 0;
                for (int t = 0; t < 2; t++) {
                    double perTurbine = tgv[t][0].getFlowRate() + tgv[t][1].getFlowRate();
                    pe += TG.generatorLoadMW(perTurbine, sep[0].outTempC, cond[t].pressure);
                }
                double hxSteam = 0, sepOut = 0, condense = 0;
                for (int i = 0; i < 2; i++) { hxSteam += hx[i].steamOutKgs; sepOut += sep[i].outSteamKgs; }
                for (Dearator d : dea) condense += d.steamInlet.getFlowRate() - d.getPassKgs();
                System.out.printf(
                        "t=%5.1fs 一次侧 %.0f/%.0fMW | 产汽 %6.0f | 母管可供 %6.0f | 除氧器进汽 %6.0f 透传 %6.0f | 主汽门 %6.0f | "
                        + "除氧器 %.0fC 水位 %.0f%% 汽空间%.0fkg 阀请求%.0f 实际%.0f | 汽机 %6.1fMW×2 = %7.1fMW | 净效率 %.1f%%%n",
                        step * 0.05, qPrimary, qSecondary, sepSteam, steamPiping.availableSteamKg() * 20.0, deaIn, deaPass, tgFlow,
                        waterT, level, dea[0].getSteamMass(), dea[0].steamOutlet.getFlowRate(), dea[0].getPassKgs(),
                        pe / 2, pe, pe / coreMW * 100);
            }
        }

        {
            Dearator d = dea[0];
            double tsat = Loader.tables.getSteamTemperatureByPressure(d.getPressure());
            double hfg = Loader.tables.getSpecificVaporEnthalpyByTemperature(tsat);
            double want = d.steamInlet.getFlowRate() * 0.05 * 4200 * (tsat - 36.5) / hfg;
            System.out.printf("除氧器#1: P=%.3fMPa Tsat=%.1fC 水温=%.1fC 水质量=%.0fkg "
                            + "进水=%.0fkg/s 需凝结=%.0fkg 汽空间=%.0fkg 透传=%.0fkg/s%n",
                    d.getPressure(), tsat, d.getWaterTemperature(), d.getWaterMass(),
                    d.steamInlet.getFlowRate(), want, d.getSteamMass(), d.getPassKgs());
        }
        double dh = TG.specificWorkKjPerKg(sep[0].outTempC, 0.0061);
        System.out.printf("凝结水回到除氧器的温度 = %.1f C（凝汽器真空 %.4f MPa）%n",
                cond[0].tempC(), cond[0].pressure);
        System.out.printf("%n每 kg 主蒸汽可用焓降 = %.0f kJ/kg（等熵 88%%、排汽干度 0.80）%n", dh);
        System.out.printf("Cv = %.0f（4 只主汽门）%n", cv);
    }
}
