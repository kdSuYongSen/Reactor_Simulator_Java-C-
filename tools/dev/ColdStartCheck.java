package com.darwish.nppsim;

import java.util.ArrayList;

/**
 * 冷态启动联调：反应堆提棒 → 一回路升温 → 二回路产汽 → 分离器/母管建压。
 *
 * <p>验证 2026-10-27 把分离器初值从 4.0 MPa 改成常压后，整条启动链还能建立起来
 * （冷态图标的是常压，不是"假 4 MPa"）。</p>
 *
 * 用法：java -cp "…jar-with-dependencies;target/verify" com.darwish.nppsim.ColdStartCheck
 */
public class ColdStartCheck {

    static int fails = 0;

    static void check(String what, boolean ok, String detail) {
        if (!ok) {
            fails++;
        }
        System.out.printf("  [%s] %-40s %s%n", ok ? "OK" : "FAIL", what, detail);
    }

    static boolean ok(double v) {
        return !Double.isNaN(v) && !Double.isInfinite(v);
    }

    public static void main(String[] a) throws Exception {
        Localization.init();
        UiStyle.applyLaf();
        Fonts.init();
        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();
        new NPPSim();
        Thread.sleep(800);

        // 全插 → 常压
        double p0 = NPPSim.separators.get(0).pressureMPa;
        double h0 = NPPSim.steamPiping.getPressure();
        check("冷态：分离器常压", p0 < 0.3, fmt(p0) + " MPa");
        check("冷态：主蒸汽母管常压", h0 < 0.3, fmt(h0) + " MPa");

        // 提棒到略超临界（实测临界约 0.34 插入）
        int rods = 0;
        for (ArrayList<Channel> row : NPPSim.core.coreArray) {
            for (Channel c : row) {
                if (c instanceof ControlRodChannel) {
                    ((ControlRodChannel) c).setPosition(0.32f);
                    rods++;
                }
            }
        }
        System.out.println("  棒数 " + rods + " → 棒位 0.32（略超临界）");

        System.out.println("  时间  堆功率MW  ρ(pcm)   汽包MPa  二回路MPa  母管MPa  除氧器MPa");
        for (int i = 1; i <= 12; i++) {          // 6 分钟
            Thread.sleep(30000);
            Core core = NPPSim.core;
            double drum = NPPSim.mcc.drum1.getPressure();
            double sec = NPPSim.separators.get(0).pressureMPa;
            double hdr = NPPSim.steamPiping.getPressure();
            double dea = NPPSim.dearators.get(0).getPressure();
            System.out.printf("  %3d0s %9.2f %8.1f %9.3f %10.3f %8.3f %10.3f%n",
                    i, core.getThermalPower(), core.getReactivity() * 1e5, drum, sec, hdr, dea);
            if (i == 12) {
                check("堆功率建立（>1 MW）", core.getThermalPower() > 1.0,
                        fmt(core.getThermalPower()) + " MW");
                check("汽包压力上升（>0.15 MPa）", drum > 0.15, fmt(drum) + " MPa");
                check("二回路建压（>0.3 MPa）", sec > 0.3, fmt(sec) + " MPa");
                check("主蒸汽母管跟随（>0.3 MPa）", hdr > 0.3, fmt(hdr) + " MPa");
                check("数值健康（无 NaN）", ok(core.getThermalPower()) && ok(sec) && ok(hdr)
                                && ok(NPPSim.dearators.get(0).getPressure()),
                        "P=" + fmt(core.getThermalPower()) + " 母管=" + fmt(hdr));
            }
        }

        System.out.println(fails == 0 ? "COLD_START_OK" : "COLD_START_FAIL (" + fails + ")");
        System.exit(0);
    }

    static String fmt(double v) {
        if (Double.isNaN(v)) {
            return "NaN";
        }
        return String.format("%.3f", v);
    }
}
