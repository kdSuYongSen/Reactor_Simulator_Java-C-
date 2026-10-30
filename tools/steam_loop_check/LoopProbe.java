package com.darwish.nppsim;

import java.lang.reflect.Field;
import java.util.Locale;
import sun.misc.Unsafe;

/**
 * 主回路真实性探针（无界面）。
 *
 * 用真实的 Core + MCC（1661 根燃料通道、8 台 MCP、两个汽包）跑一回路：
 *   汽包 → MCP → 压力母管 → 燃料通道 → 汽包 → 换热器壳侧 → 凝结水回注汽包
 *
 * 每 tick 直接把 4800 MW 按径向形状灌进通道（绕开 C++ 引擎，只考察 Java 侧环路），
 * 输出 MCP 总流量、通道入口温度、汽包压力/水位、换热器抽汽量与传热。
 *
 * 目的：核对"4800 MW 需要多少主循环流量"、"核心入口是否真的过冷"、
 * "换热器抽汽导纳与堆芯产汽是否匹配"。
 */
public class LoopProbe {

    static Unsafe unsafe() throws Exception {
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (Unsafe) f.get(null);
    }

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);
        Loader.tables = new SteamTables();
        NPPSim.tables = Loader.tables;
        NPPSim.atmosphere = new Atmosphere(15);
        // MCC.update() 会调用 autoControl.resetTimeUpdatedFlag()；通道功率 <5 MW 不会触发 AZ-1
        NPPSim.autoControl = (AutoControl) unsafe().allocateInstance(AutoControl.class);

        NPPSim.core = new Core();
        MCC mcc = new MCC();
        NPPSim.mcc = mcc;

        boolean rated = !Boolean.getBoolean("defaultpumps");
        for (Pump p : mcc.mcp) {
            p.setActive(rated);
            p.rpmSetting = rated ? 1.0f : 0.0f;
            p.dischargeValve.setPosition(rated ? 1.0f : 0.7f);
            p.dischargeValve.setAutoState(1);
        }
        if (!rated) {   // 复现游戏默认：每环路 1 台、40% 转速、70% 阀位
            mcc.mcp.get(0).setActive(true);
            mcc.mcp.get(4).setActive(true);
            mcc.mcp.get(0).rpmSetting = 0.4f;
            mcc.mcp.get(4).rpmSetting = 0.4f;
        }

        mcc.setWaterTemp(284.0);        // 汽包 = 6.9 MPa 饱和
        HeatExchanger[] hx = {new HeatExchanger("A"), new HeatExchanger("B")};
        MCC.SeparatorDrum[] drums = {mcc.drum1, mcc.drum2};

        final double totalMW = 4800.0;
        int fuelCount = 0;
        for (java.util.ArrayList<Channel> row : NPPSim.core.coreArray) {
            for (Channel c : row) if (c instanceof FuelChannel) fuelCount++;
        }
        System.out.printf("燃料通道 = %d，总额定功率 = %.0f MW%n", fuelCount, totalMW);

        int steps = Integer.parseInt(System.getProperty("steps", "4000"));   // 200 s
        for (int step = 0; step < steps; step++) {
            for (java.util.ArrayList<Channel> row : NPPSim.core.coreArray) {
                for (Channel c : row) {
                    if (c instanceof FuelChannel) {
                        FuelChannel fc = (FuelChannel) c;
                        fc.setKineticsPower(totalMW / fuelCount * fc.getRadialShape(), 0, 0);
                    }
                }
            }
            mcc.update();

            double mcpFlow = 0;
            for (Pump p : mcc.mcp) mcpFlow += p.getFlowRate();

            for (int i = 0; i < 2; i++) {
                HeatExchanger h = hx[i];
                double kg = h.updatePrimary(drums[i].getPressure(), drums[i].getSteamTemperature(), mcpFlow, 0.05);
                double avail = drums[i].availableSteamKg();
                if (kg > avail) {
                    kg = Math.max(0.0, avail);
                    double kgs = kg / 0.05;
                    h.primarySteamInKgs = kgs;
                    h.condensateOutKgs = kgs;
                    h.primaryHeatMW = kgs * Loader.tables
                            .getSpecificVaporEnthalpyByTemperature(drums[i].getSteamTemperature()) / 1000.0;
                }
                drums[i].updateSteamOutflow(kg, drums[i].getSteamTemperature());
                drums[i].updateWaterInflow(h.condensateOutKgs * 0.05, drums[i].getSteamTemperature());
                h.updateSecondary(0.05, 0.70);
            }

            if (step % 400 == 0 || step == steps - 1) {
                FuelChannel fc = null;
                for (java.util.ArrayList<Channel> row : NPPSim.core.coreArray) {
                    for (Channel c : row) if (c instanceof FuelChannel) { fc = (FuelChannel) c; break; }
                    if (fc != null) break;
                }
                double draw = hx[0].primarySteamInKgs + hx[1].primarySteamInKgs;
                double heat = hx[0].primaryHeatMW + hx[1].primaryHeatMW;
                System.out.printf(
                        "t=%5.1fs MCP=%7.0f kg/s | 汽包 %.2f MPa 水位 %+5.1f%% 汽温 %.0fC | 通道入口 %.1fC | "
                        + "抽汽 %6.0f kg/s 传热 %6.0f MW | 二回路产汽 %6.0f kg/s%n",
                        step * 0.05, mcpFlow,
                        drums[0].getPressure(), drums[0].getWaterLevel(), drums[0].getSteamTemperature(),
                        fc != null ? fc.getWaterInflowTemperature() : -1,
                        draw, heat, hx[0].steamOutKgs + hx[1].steamOutKgs);
            }
        }
        System.out.printf("C++ 引擎额定流量基准 = 12000 kg/s，当前流量分数 = %.3f%n",
                (mcc.mcp.stream().mapToDouble(Pump::getFlowRate).sum()) / 12000.0);
    }
}
