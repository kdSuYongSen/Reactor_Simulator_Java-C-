package com.darwish.nppsim;

/**
 * 泄压阀自检：高压力 / 高水位自动开阀、红色报警、水排向除盐水箱（物质不凭空消失）。
 */
public class ReliefCheck {

    static int fails = 0;

    public static void main(String[] a) throws Exception {
        Localization.init();
        UiStyle.applyLaf();
        Fonts.init();
        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();
        new NPPSim();
        Thread.sleep(600);

        Tank tank = NPPSim.pcs.demineralizedWaterTank;
        Separator s = new Separator("T");
        s.setReliefTarget(tank);

        check("初始泄压阀关闭", !s.reliefValveOpen && !s.alarmReliefOpen, "");

        // ---- 高水位 ----
        double level0 = s.waterLevel;
        double tank0 = tank.getWaterMass();
        s.waterLevel = 95.0;
        for (int i = 0; i < 20; i++) {
            s.update(0.05, 0, 20.0);
        }
        tank.update();                 // 结算进水（Tank 是"先累加、update 时混合"的模型）
        double tank1 = tank.getWaterMass();
        check("水位 95% → 泄压阀自动开启", s.reliefValveOpen, "水位 " + fmt(s.waterLevel) + "%");
        check("泄压阀开启 → 红色报警置位", s.alarmReliefOpen, "");
        check("排水进入除盐水箱", tank1 > tank0 + 1.0,
                String.format("%.0f → %.0f kg", tank0, tank1));
        check("排水后水位下降", s.waterLevel < 95.0, fmt(s.waterLevel) + "%");

        // ---- 回座 ----
        s.waterLevel = 30.0;
        for (int i = 0; i < 10; i++) {
            s.update(0.05, 0, 20.0);
        }
        check("水位回 30% → 泄压阀关闭", !s.reliefValveOpen, "");

        // ---- 高压力 ----
        s.pressureMPa = 8.0;
        s.update(0.05, 0, 20.0);
        check("压力 8.0 MPa → 泄压阀自动开启", s.reliefValveOpen, "");
        check("高压泄压有排放量", s.reliefDrainKgs > 0.0, fmt(s.reliefDrainKgs) + " kg/s");

        // ---- 正常工况不误动 ----
        s.pressureMPa = 4.0;
        s.waterLevel = 40.0;
        s.update(0.05, 0, 20.0);
        check("正常 4.0 MPa / 40% 不开启", !s.reliefValveOpen, "");

        System.out.println(fails == 0 ? "RELIEF_OK" : ("RELIEF_FAIL " + fails));
        System.exit(fails == 0 ? 0 : 1);
    }

    static String fmt(double v) {
        return String.format("%.1f", v);
    }

    static void check(String name, boolean ok, String detail) {
        if (!ok) {
            fails++;
        }
        System.out.printf("[%s] %s %s%n", ok ? "OK" : "FAIL", name, detail);
    }
}
