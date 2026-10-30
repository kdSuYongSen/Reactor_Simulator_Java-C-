package com.darwish.nppsim;

/**
 * 堆芯热量 → 一回路汽包 → 换热器 → 汽水分离器 的耦合自检。
 *
 * <p>验证三点：① 注入热量后汽包会升温/建压/产汽；② 二回路产汽量随堆芯功率变化
 * （不再"凭空产汽"）；③ 汽包内水+汽的质量守恒（加热本身不增不减物质）。</p>
 */
public class CoreHeatCouplingCheck {

    static int fails = 0;

    public static void main(String[] a) throws Exception {
        Localization.init();
        UiStyle.applyLaf();
        Fonts.init();
        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();
        new NPPSim();
        Thread.sleep(800);

        MCC.SeparatorDrum d1 = NPPSim.mcc.drum1;
        MCC.SeparatorDrum d2 = NPPSim.mcc.drum2;

        double m0 = mass(d1) + mass(d2);
        System.out.printf("初始：汽包 %.4f MPa / %.1f°C / 水 %.0f t / 汽 %.0f kg%n",
                d1.getPressure(), d1.getWaterTemperature(), d1.getWaterMass() / 1000.0, d1.getSteamMass());

        // ---- 0 MW：先空跑 ----
        NPPSim.coreHeatOverrideMW = 0;
        NPPSim.feedwaterOverrideKgs = 3000;   // 模拟给水系统正常投运（两台合计 3000 kg/s）
        Thread.sleep(5000);
        double steam0 = steamOut();
        System.out.printf("0 MW 稳定 3 s：汽包 %.4f MPa / %.1f°C  二回路出口汽 %.1f kg/s%n",
                d1.getPressure(), d1.getWaterTemperature(), steam0);

        // ---- 4800 MW：满功率注入 ----
        NPPSim.coreHeatOverrideMW = 4800;
        double t0 = System.currentTimeMillis();
        for (int i = 0; i < 6; i++) {
            Thread.sleep(15000);          // 每 15 s 打一行，共 90 s
            System.out.printf("  t=%3.0fs 4800MW：汽包 %.3f MPa / %.1f°C / 水 %.0f t / 汽 %.0f kg | 蒸汽产量 %.0f kg/s | 抽汽 %.0f kg/s | 分离器出口 %.0f kg/s | 热功率 %.0f MW%n",
                    (System.currentTimeMillis() - t0) / 1000.0,
                    d1.getPressure(), d1.getWaterTemperature(), d1.getWaterMass() / 1000.0,
                    d1.getSteamMass(), d1.getSteamProduction(),
                    NPPSim.heatExchangers.get(0).primarySteamInKgs,
                    NPPSim.separators.get(0).outSteamKgs,
                    NPPSim.heatExchangers.get(0).primaryHeatMW);
        }
        double steamHot = steamOut();
        double pHot = d1.getPressure();
        double tHot = d1.getWaterTemperature();

        // ---- 回 0 MW：应降温/降产汽 ----
        NPPSim.coreHeatOverrideMW = 0;
        Thread.sleep(20000);
        double steamCold = steamOut();
        System.out.printf("回 0 MW 20 s：汽包 %.3f MPa / %.1f°C  二回路出口汽 %.1f kg/s%n",
                d1.getPressure(), d1.getWaterTemperature(), steamCold);

        double m1 = mass(d1) + mass(d2);
        double drift = Math.abs(m1 - m0) / Math.max(1.0, m0) * 100.0;

        check("4800 MW 时汽包升压", pHot > 1.0, String.format("%.3f MPa", pHot));
        check("4800 MW 时汽包升温", tHot > 100.0, String.format("%.1f °C", tHot));
        check("4800 MW 时二回路产汽", steamHot > 100.0, String.format("%.0f kg/s", steamHot));
        check("回 0 MW 后产汽明显下降", steamCold < steamHot * 0.6,
                String.format("%.0f → %.0f kg/s", steamHot, steamCold));
        check("汽包内水+汽质量守恒（漂移 < 2%）", drift < 2.0,
                String.format("%.0f → %.0f kg（%.3f%%）", m0, m1, drift));

        NPPSim.coreHeatOverrideMW = -1;
        NPPSim.feedwaterOverrideKgs = 0;
        System.out.println(fails == 0 ? "CORE_HEAT_OK" : ("CORE_HEAT_FAIL " + fails));
        System.exit(fails == 0 ? 0 : 1);
    }

    static double mass(MCC.SeparatorDrum d) {
        return d.getWaterMass() + d.getSteamMass();
    }

    /** 二回路产汽量（换热器管侧实际蒸发量，不受分离器出口阀位影响） */
    static double steamOut() {
        double s = 0;
        for (HeatExchanger hx : NPPSim.heatExchangers) {
            s += hx.secondarySteamKgs;
        }
        return s;
    }

    static void check(String name, boolean ok, String detail) {
        if (!ok) {
            fails++;
        }
        System.out.printf("[%s] %s：%s%n", ok ? "OK" : "FAIL", name, detail);
    }
}
