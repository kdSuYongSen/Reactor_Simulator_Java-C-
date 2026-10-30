package com.darwish.nppsim;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * SteamLoopUI 的离屏验证：用一组典型工况数值填充静态引用，弹出窗口供截图检查排版。
 * 只用于开发验证，不参与游戏逻辑。
 */
public class SteamLoopShot {

    static void set(Object o, String name, Object v) throws Exception {
        Class<?> c = o.getClass();
        while (c != null) {
            try {
                Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                f.set(o, v);
                return;
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    public static void main(String[] args) throws Exception {
        java.util.Locale.setDefault(java.util.Locale.SIMPLIFIED_CHINESE);
        Localization.init();
        Loader.tables = new SteamTables();
        NPPSim.tables = Loader.tables;
        NPPSim.atmosphere = new Atmosphere(15);

        Separator sepA = new Separator("A"), sepB = new Separator("B");
        NPPSim.separators = new ArrayList<>();
        NPPSim.separators.add(sepA);
        NPPSim.separators.add(sepB);
        set(sepA, "pressureMPa", 0.70);
        set(sepA, "outSteamKgs", 1130.0);
        set(sepA, "outWaterKgs", 62.0);
        set(sepA, "waterLevel", 14.0);
        set(sepB, "pressureMPa", 0.70);
        set(sepB, "outSteamKgs", 1128.0);
        set(sepB, "outWaterKgs", 58.0);
        set(sepB, "waterLevel", 12.0);

        HeatExchanger hxA = new HeatExchanger("A"), hxB = new HeatExchanger("B");
        NPPSim.heatExchangers = new ArrayList<>();
        NPPSim.heatExchangers.add(hxA);
        NPPSim.heatExchangers.add(hxB);
        hxA.primarySteamInKgs = 1548.0;
        hxA.primaryHeatMW = 2408.0;
        hxA.steamOutKgs = 1130.0;
        hxB.primarySteamInKgs = 1546.0;
        hxB.primaryHeatMW = 2395.0;
        hxB.steamOutKgs = 1128.0;

        NPPSim.steamPiping = new OneWaySteamHeader(new Connectable[]{});
        set(NPPSim.steamPiping, "pressure", 0.70);

        NPPSim.deaeratorSteamHeader = new SteamCollectorHeader();
        NPPSim.dearators = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            NPPSim.dearators.add(new Dearator(i));
        }
        NPPSim.deaeratorSteamHeader.setSources(new Connectable[]{
            NPPSim.dearators.get(0), NPPSim.dearators.get(1),
            NPPSim.dearators.get(2), NPPSim.dearators.get(3)});
        for (Dearator d : NPPSim.dearators) {
            d.steamInlet.setPosition(1.0f);
            d.steamOutlet.setPosition(1.0f);
            set(d, "pressure", 0.63);
            set(d, "waterTemperature", 159.0);
            set(d, "waterLevel", 10.0);
            set(d, "actualPassKgs", 451.0);
            set(d, "steamInflow", 28.2);
        }
        set(NPPSim.deaeratorSteamHeader, "pressure", 0.61);
        set(NPPSim.deaeratorSteamHeader, "inflowRate", 2262.0);
        set(NPPSim.deaeratorSteamHeader, "outflowRate", 1804.0);

        NPPSim.pcsCondensateFilter = new CondensateFilter(null, null);
        set(NPPSim.pcsCondensateFilter, "throughputRate", 1800.0);
        set(NPPSim.pcsCondensateFilter, "blowdownRate", 3.4);

        NPPSim.tg1 = new TG(new Condenser());
        NPPSim.tg2 = new TG(new Condenser());
        NPPSim.TG1InletValves = new ArrayList<>();
        NPPSim.TG2InletValves = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            NPPSim.TG1InletValves.add(new SteamValve(NPPSim.MAIN_STEAM_CV, 20,
                    NPPSim.deaeratorSteamHeader, NPPSim.tg1));
            NPPSim.TG2InletValves.add(new SteamValve(NPPSim.MAIN_STEAM_CV, 20,
                    NPPSim.deaeratorSteamHeader, NPPSim.tg2));
        }
        for (SteamValve v : NPPSim.TG1InletValves) {
            v.setPosition(1.0f);
            set(v, "flowRate", 22.65);
        }
        for (SteamValve v : NPPSim.TG2InletValves) {
            v.setPosition(0.86f);
            set(v, "flowRate", 19.4);
        }
        for (Dearator d : NPPSim.dearators) {
            set(d.steamInlet, "flowRate", 28.2);
            set(d.steamOutlet, "flowRate", 22.55);
        }
        for (TG tg : new TG[]{NPPSim.tg1, NPPSim.tg2}) {
            set(tg, "load", 516.3);
            set(tg, "rpm", 3000.0f);
            set(tg, "steamInflowTemperature", 159.0);
            set(tg.condenser, "pressure", 0.0061);
        }
        set(NPPSim.tg2, "load", 448.7);

        SteamLoopUI ui = new SteamLoopUI();
        System.out.println("PREF=" + ui.getPreferredSize() + " SIZE=" + ui.getSize()
                + " SCREEN=" + java.awt.Toolkit.getDefaultToolkit().getScreenSize());
        for (java.awt.Component c : ((java.awt.Container) ui.getContentPane()).getComponents()) {
            System.out.println("  child " + c.getClass().getSimpleName() + " pref=" + c.getPreferredSize());
            if (c instanceof java.awt.Container) {
                for (java.awt.Component cc : ((java.awt.Container) c).getComponents()) {
                    System.out.println("    panel pref=" + cc.getPreferredSize());
                }
            }
        }
        ui.setVisible(true);
        // 手动刷新几帧（正常由 UI.update 驱动）
        for (int i = 0; i < 6; i++) {
            ui.update();
            Thread.sleep(150);
        }
        Thread.sleep(60000);   // 留时间给截图脚本
    }
}
