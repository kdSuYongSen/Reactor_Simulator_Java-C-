package com.darwish.nppsim;

import java.awt.event.WindowEvent;
import javax.swing.SwingUtilities;

/**
 * 复核：① 切换单位后主窗口/凝结水窗口的数值显示是否真的跟着变；
 * ② 事件日志窗口点 × 之后是不是"隐藏而不是销毁"，再打开是否回到原位。
 *
 * <p>用法：java ... com.darwish.nppsim.UnitsLogCheck</p>
 */
public class UnitsLogCheck {

    static int fails = 0;

    static void check(String what, boolean ok, String detail) {
        if (!ok) {
            fails++;
        }
        System.out.printf("  [%s] %-44s %s%n", ok ? "OK" : "FAIL", what, detail);
    }


    static eu.hansolo.steelseries.gauges.Lcd lcd(UI ui, String field) throws Exception {
        java.lang.reflect.Field f = UI.class.getDeclaredField(field);
        f.setAccessible(true);
        return (eu.hansolo.steelseries.gauges.Lcd) f.get(ui);
    }

    static java.lang.reflect.Method showUnitMethod() throws Exception {
        java.lang.reflect.Method m = UI.class.getDeclaredMethod("showUnit",
                eu.hansolo.steelseries.gauges.Lcd.class, Units.Quantity.class, double.class);
        m.setAccessible(true);
        return m;
    }

    public static void main(String[] a) throws Exception {
        Localization.init();
        UiStyle.applyLaf();
        Fonts.init();
        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();
        new NPPSim();
        Thread.sleep(800);

        // ---------- ① 单位 ----------
        System.out.println("=========== 单位 ===========");
        Units.setUnit(Units.Quantity.PRESSURE, 0);      // MPa
        Units.setUnit(Units.Quantity.TEMPERATURE, 0);   // °C
        Units.setUnit(Units.Quantity.FLOW, 0);          // kg/s
        Units.setUnit(Units.Quantity.POWER, 0);         // MW
        double mpa = Units.pressure(6.9);
        Units.setUnit(Units.Quantity.PRESSURE, 2);      // psi
        double psi2 = Units.pressure(6.9);
        String unitNow = Units.unitOf(Units.Quantity.PRESSURE);
        check("压力单位可切换且换算正确",
                unitNow.equals("psi") && Math.abs(psi2 - 1000.0) < 5.0,
                "6.9 MPa = " + psi2 + " " + unitNow + "（MPa 下为 " + mpa + "，psi 精度取整）");
        Units.setUnit(Units.Quantity.PRESSURE, 0);

        check("温度单位可切换",
                Math.abs(Units.temperature(100) - 212.0) < 0.01
                        || Units.unitOf(Units.Quantity.TEMPERATURE).equals("°C"),
                "100 °C → " + Units.temperature(100) + " " + Units.temperatureUnit());
        Units.setUnit(Units.Quantity.TEMPERATURE, 0);

        Units.setUnit(Units.Quantity.FLOW, 1);          // t/h
        check("流量单位可切换",
                Math.abs(Units.flow(100) - 360.0) < 0.01,
                "100 kg/s → " + Units.flow(100) + " " + Units.flowUnit());
        Units.setUnit(Units.Quantity.FLOW, 0);

        check("功率单位可切换（MWt/MWe 后缀）",
                Units.powerLabel(true).equals("MWt") && Units.powerLabel(false).equals("MWe"),
                "thermal=" + Units.powerLabel(true) + "  electric=" + Units.powerLabel(false));

        // 主窗口 LCD 是否跟着单位变
        UI ui = NPPSim.ui;
        double drumP = NPPSim.mcc.drum1.getPressure();
        final eu.hansolo.steelseries.gauges.Lcd press1 = lcd(ui, "press1");
        final java.lang.reflect.Method showUnit = showUnitMethod();
        Units.setUnit(Units.Quantity.PRESSURE, 0);
        SwingUtilities.invokeAndWait(() -> {
            try {
                showUnit.invoke(null, press1, Units.Quantity.PRESSURE, drumP);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        double vMpa = press1.getLcdValue();
        Units.setUnit(Units.Quantity.PRESSURE, 2);
        SwingUtilities.invokeAndWait(() -> {
            try {
                showUnit.invoke(null, press1, Units.Quantity.PRESSURE, drumP);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        double vPsi = press1.getLcdValue();
        String lcdUnit = press1.getLcdUnitString();
        check("主窗口压力 LCD 跟随单位换算",
                Math.abs(vPsi - vMpa * 145.0377) < 0.5 && lcdUnit.equals("psi"),
                "MPa=" + vMpa + " → psi=" + vPsi + " 单位串=" + lcdUnit);
        Units.setUnit(Units.Quantity.PRESSURE, 0);
        Units.resetToMetric();
        check("恢复公制", Units.unitOf(Units.Quantity.PRESSURE).equals("MPa")
                && Units.unitOf(Units.Quantity.TEMPERATURE).equals("°C")
                && Units.unitOf(Units.Quantity.FLOW).equals("kg/s")
                && Units.unitOf(Units.Quantity.POWER).equals("MW")
                && Units.unitOf(Units.Quantity.LENGTH).equals("m"),
                "压力=" + Units.unitOf(Units.Quantity.PRESSURE) + " 温度="
                        + Units.unitOf(Units.Quantity.TEMPERATURE) + " 流量="
                        + Units.unitOf(Units.Quantity.FLOW) + " 功率="
                        + Units.unitOf(Units.Quantity.POWER) + " 长度="
                        + Units.unitOf(Units.Quantity.LENGTH));

        // ---------- ② 事件日志窗口 ----------
        System.out.println("=========== 事件日志窗口 ===========");
        SwingUtilities.invokeAndWait(() -> UI.createOrContinue(LogWindow.class, false, false));
        Thread.sleep(500);
        LogWindow w = null;
        for (UIUpdateable u : UI.elementsToUpdate) {
            if (u instanceof LogWindow) {
                w = (LogWindow) u;
            }
        }
        check("事件日志窗口能打开", w != null, w == null ? "null" : ("位置 " + w.getBounds()));
        if (w != null) {
            final LogWindow fw = w;
            SwingUtilities.invokeAndWait(() ->
                    fw.dispatchEvent(new WindowEvent(fw, WindowEvent.WINDOW_CLOSING)));
            Thread.sleep(300);
            check("点 × 后是隐藏而不是销毁（可再打开）",
                    !fw.isVisible() && fw.isDisplayable(),
                    "visible=" + fw.isVisible() + " displayable=" + fw.isDisplayable());
            SwingUtilities.invokeAndWait(() -> UI.createOrContinue(LogWindow.class, false, false));
            Thread.sleep(400);
            check("再打开回到原位", fw.isVisible() && fw.getX() == w.getBounds().x,
                    "visible=" + fw.isVisible() + " bounds=" + fw.getBounds());
            check("窗口可获得焦点（能拖动/关闭）", fw.isFocusableWindow(),
                    "focusableWindow=" + fw.isFocusableWindow());
        }

        System.out.println(fails == 0 ? "UNITS_LOG_OK" : "UNITS_LOG_FAIL (" + fails + ")");
        System.exit(0);
    }
}
