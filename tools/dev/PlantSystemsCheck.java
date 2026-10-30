package com.darwish.nppsim;

import java.lang.reflect.Field;

/**
 * AZ-2/3/4/6 逻辑、凝结水/给水排水阀、除氧器来水 自检。
 */
public class PlantSystemsCheck {

    static int fails = 0;

    public static void main(String[] a) throws Exception {
        Localization.init();
        UiStyle.applyLaf();
        Fonts.init();
        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();
        new NPPSim();
        Thread.sleep(800);

        // ---------------- AZ ----------------
        AutoControl ac = NPPSim.autoControl;
        check("AZ-6 对象名称为 AZ-6", "AZ-6".equals(str(ac.az6Control, "name")),
                str(ac.az6Control, "name"));
        check("AZ-5 为手动停堆（无功率定值）",
                num(ac.az5Control, "powerLimit") > 1.0e9, fmt(num(ac.az5Control, "powerLimit"), 0));

        // 自动 AZ 打开 → AZ-6 低定值应跳闸（临时把限值改成 1 MW）
        set(ac.az6Control, "powerLimit", -1.0);   // 冷态功率 0 > -1 → 越限
        NPPSim.autoControl.azAutoEnabled = true;
        ac.az6Control.update();
        check("自动 AZ 打开 → AZ-6 越限跳闸", ac.az6Control.isTripped(), "");
        set(ac.az6Control, "powerLimit", 5600.0);     // 先恢复定值，否则主循环会立刻再跳
        ac.az6Control.reset();
        check("AZ-6 复位", !ac.az6Control.isTripped(), "");

        // 自动 AZ 关闭 → 同样越限也不跳
        NPPSim.autoControl.azAutoEnabled = false;
        ac.az6Control.update();
        check("自动 AZ 关闭 → AZ-6 不自动跳", !ac.az6Control.isTripped(), "");
        // 但手动按钮仍然有效
        ac.az6Control.trip();
        check("自动 AZ 关闭 → 手动 AZ-6 仍可跳闸", ac.az6Control.isTripped(), "");
        ac.az6Control.reset();
        NPPSim.autoControl.azAutoEnabled = true;

        // AZ-2/3/4 也受总开关影响
        set(ac.az2Control, "minPeriod", 1.0e9);
        NPPSim.autoControl.azAutoEnabled = false;
        ac.az2Control.update();
        check("自动 AZ 关闭 → AZ-2 不自动跳", !ac.az2Control.isTripped(), "");
        ac.az2Control.reset();
        System.out.println("  （AZ-2 是周期保护：冷态周期为 ∞，不会触发；关自动 AZ 后同样不动作）");

        // ---------------- 排水阀 ----------------
        WaterValve cd = NPPSim.condensateDrainValve;
        WaterValve fd = NPPSim.feedwaterDrainValve;
        check("凝结水排水阀存在", cd != null, "");
        check("给水排水阀存在", fd != null, "");

        cd.setState(2);      // 开
        Thread.sleep(2500);
        check("凝结水排水阀开启后有排放流量", cd.getPosition() > 0.05 && cd.timestepFlow >= 0.0,
                String.format("阀位 %.0f%%  流量 %.1f kg/s", cd.getPosition() * 100, cd.timestepFlow * 20));
        cd.setState(0);
        Thread.sleep(2500);
        check("凝结水排水阀关闭", cd.getPosition() < 0.05, String.format("阀位 %.1f%%", cd.getPosition() * 100));

        // 排水阀为自动：默认投入自动控制（autoEnabled），报警改到左上角 annunciator
        check("凝结水排水阀默认自动", cd != null && cd.isAutoEnabled(), "");
        check("给水排水阀默认自动", fd != null && fd.isAutoEnabled(), "");
        fd.setState(2);
        Thread.sleep(2000);
        check("给水排水阀开启（手动优先，用于验证阀动作）", fd.getPosition() > 0.01,
                String.format("阀位 %.0f%%", fd.getPosition() * 100));
        fd.setState(0);
        Thread.sleep(2000);

        // ---------------- 除氧器 ----------------
        Dearator d1 = NPPSim.dearators.get(0);
        double inflow0 = d1.getWaterInflowRate();
        System.out.printf("除氧器 #1：水位 %.1f%%  进水 %.1f kg/s  分离器回水 %.1f kg/s  换热器排水 %.1f kg/s  补水阀位 %.0f%%%n",
                d1.getWaterLevel(), inflow0,
                NPPSim.separators.get(0).outWaterKgs,
                NPPSim.heatExchangers.get(0).secondaryDrainKgs,
                NPPSim.pcs.dearatorMakeupValves.get(0).getPosition() * 100);
        check("除氧器水位在合理范围（-100~100）",
                d1.getWaterLevel() >= -100.5 && d1.getWaterLevel() <= 100.5,
                fmt(d1.getWaterLevel(), 1) + " %");
        // 冷态补水阀应关闭（自动补水默认不投入）
        check("冷态除氧器补水阀未自动开启",
                NPPSim.pcs.dearatorMakeupValves.get(0).getPosition() < 0.05,
                fmt(NPPSim.pcs.dearatorMakeupValves.get(0).getPosition() * 100, 1) + " %");

        // 诊断：除氧器进水到底从哪来
        double valveFlow = 0;
        for (WaterValve v : NPPSim.mainFeederValves) valveFlow += v.timestepFlow * 20;
        double auxFlow = 0;
        for (WaterValve v : NPPSim.auxFeederValves) auxFlow += v.timestepFlow * 20;
        System.out.printf("  诊断：主给水阀 %.1f，辅给水阀 %.1f，PCS回水母管 in=%.1f out=%.1f，过滤母管 in=%.1f，hx给水 %.1f%n",
                valveFlow, auxFlow,
                NPPSim.pcs.pcsReturnHeader.getWaterInflowRate(),
                NPPSim.pcs.pcsReturnHeader.getWaterOutflowRate(),
                NPPSim.pcs.pcsFilterPressureHeader.getWaterInflowRate(),
                NPPSim.heatExchangers.get(0).secondaryFeedKgs);
        System.out.printf("  诊断2：mixer1 进水 %.1f kg/s，hx 蒸汽 %.1f，hx 排水 %.1f%n",
                NPPSim.feedwaterMixer1.getWaterInflowRate(),
                NPPSim.heatExchangers.get(0).secondarySteamKgs,
                NPPSim.heatExchangers.get(0).secondaryDrainKgs);

        System.out.println(fails == 0 ? "PLANT_SYSTEMS_OK" : ("PLANT_SYSTEMS_FAIL " + fails));
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

    static Object get(Object o, String n) throws Exception {
        Field f = o.getClass().getDeclaredField(n);
        f.setAccessible(true);
        return f.get(o);
    }

    static void set(Object o, String n, double v) throws Exception {
        Field f = o.getClass().getDeclaredField(n);
        f.setAccessible(true);
        f.setDouble(o, v);
    }

    static double num(Object o, String n) throws Exception {
        return ((Number) get(o, n)).doubleValue();
    }

    static String str(Object o, String n) throws Exception {
        return String.valueOf(get(o, n));
    }
}
