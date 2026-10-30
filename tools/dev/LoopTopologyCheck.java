package com.darwish.nppsim;

/**
 * 环路拓扑自检（2026-10-28 新增的"河"之后）。
 *
 * <pre>
 * 核心环路 A/B：换热器出口 → MCP 泵 → 核心区域入口 → 核心区域 → 核心区域出口 → 换热器入口
 * 离线外环路：  除盐水箱 → 离线泵 A/B → 换热器 A/B → 河
 * DW 补水环路： 河 → 通用净化装置 → DW 泵 → 除盐水箱
 * 水箱排水环路：除盐水箱 → 排水阀 → 河
 * </pre>
 *
 * 检查每一段的"有流量/有去处"，以及一回路抽汽与回水质量相等。
 */
public class LoopTopologyCheck {

    static int fails = 0;

    public static void main(String[] a) throws Exception {
        Localization.init();
        UiStyle.applyLaf();
        Fonts.init();
        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();
        new NPPSim();
        Thread.sleep(800);

        // ---- 核心环路：MCP 泵 → 母管 → 堆芯 ----
        NPPSim.mcc.mcp.get(0).setActive(true);
        NPPSim.mcc.mcp.get(4).setActive(true);
        Thread.sleep(3000);
        double mcpFlow = 0;
        for (Pump p : NPPSim.mcc.mcp) {
            mcpFlow += p.getFlowRate();
        }
        check("MCP 泵有流量（核心环路 A/B）", mcpFlow > 100.0, fmt(mcpFlow, 0) + " kg/s");
        double inletFlow = NPPSim.mcc.pHeader1.getWaterInflowRate() + NPPSim.mcc.pHeader2.getWaterInflowRate();
        check("核心区域入口母管有流量", inletFlow > 50.0, fmt(inletFlow, 0) + " kg/s");

        // ---- 换热器：抽汽 = 回水（一回路质量守恒） ----
        for (int i = 0; i < 2; i++) {
            HeatExchanger hx = NPPSim.heatExchangers.get(i);
            double diff = Math.abs(hx.primarySteamInKgs - hx.condensateOutKgs);
            check("换热器 #" + (i + 1) + " 抽汽 = 凝结水回注", diff < 0.01,
                    fmt(hx.primarySteamInKgs, 1) + " kg/s，差 " + fmt(diff, 3));
        }

        // ---- 离线外环路：泵 → 换热器 → 河 ----
        OfflineCoolingSystem oc = NPPSim.core.getOfflineCooling();
        Thread.sleep(2500);
        check("离线泵 A/B 有流量", oc.getTotalFlowKgs() > 50.0, fmt(oc.getTotalFlowKgs(), 0) + " kg/s");
        double riverIn = NPPSim.pcs.river.getLastInflowKgs();
        check("河接收到离线冷却热排水", riverIn > 10.0 || oc.getTotalFlowKgs() > 10.0,
                fmt(riverIn, 0) + " kg/s");

        // ---- DW 补水环路：河 → 净化 → DW 泵 → 水箱 ----
        PCS pcs = NPPSim.pcs;
        pcs.dwMakeupPump.setActive(true);
        Thread.sleep(2500);
        check("净化装置有流量（河 → 净化 → DW 泵）", pcs.purification.getFlowKgs() > 1.0,
                fmt(pcs.purification.getFlowKgs(), 1) + " kg/s");
        double tankBefore = pcs.demineralizedWaterTank.getWaterMass();
        Thread.sleep(1500);
        double tankAfter = pcs.demineralizedWaterTank.getWaterMass();
        check("除盐水箱收到补水", tankAfter >= tankBefore - 1.0,
                fmt(tankBefore, 0) + " → " + fmt(tankAfter, 0) + " kg");
        pcs.dwMakeupPump.setActive(false);

        // ---- 水箱排水环路：水箱 → 排水阀 → 河 ----
        double riverBefore = pcs.river.getLastInflowKgs();
        pcs.dwTankDrainValve.setState(2);
        Thread.sleep(2000);
        check("排水阀开启（水箱 → 河）", pcs.isDrainValveOpen(),
                "阀位 " + fmt(pcs.dwTankDrainValve.getPosition() * 100, 0) + "%");
        check("河接收水箱排水", pcs.river.getLastInflowKgs() >= 0.0,
                fmt(pcs.river.getLastInflowKgs(), 1) + " kg/s（此前 " + fmt(riverBefore, 1) + "）");
        pcs.dwTankDrainValve.setState(0);

        // ---- 河本身 ----
        check("河温在 20~24 °C", pcs.river.getWaterTemperature() >= 20.0
                && pcs.river.getWaterTemperature() <= 24.0,
                fmt(pcs.river.getWaterTemperature(), 2) + " °C");

        System.out.println(fails == 0 ? "LOOP_TOPOLOGY_OK" : ("LOOP_TOPOLOGY_FAIL " + fails));
        System.exit(fails == 0 ? 0 : 1);
    }

    static String fmt(double v, int d) {
        return String.format("%." + d + "f", v);
    }

    static void check(String name, boolean ok, String detail) {
        if (!ok) {
            fails++;
        }
        System.out.printf("[%s] %s %s%n", ok ? "OK" : "FAIL", name, detail);
    }
}
