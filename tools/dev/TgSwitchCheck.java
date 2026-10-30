package com.darwish.nppsim;

import java.lang.reflect.Field;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;

/** 凝结水窗口 TG 切换自检：切到另一台凝汽器后，启停按钮/运行灯要跟着变。 */
public class TgSwitchCheck {

    static int fails = 0;

    public static void main(String[] a) throws Exception {
        Localization.init();
        UiStyle.applyLaf();
        Fonts.init();
        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();
        new NPPSim();
        Thread.sleep(500);

        CondensateUI ui = new CondensateUI();
        SwingUtilities.invokeAndWait(() -> {
            ui.pack();
            ui.setVisible(true);      // update() 只在可见时刷新（与运行时一致）
            ui.validate();
        });
        Thread.sleep(400);
        JSpinner sel = (JSpinner) get(ui, "condSelA");
        JRadioButton start = (JRadioButton) get(ui, "con1Start");
        JRadioButton stop = (JRadioButton) get(ui, "con1Stop");
        eu.hansolo.steelseries.extras.Led led = (eu.hansolo.steelseries.extras.Led) get(ui, "con1Power");

        // 1 号泵运行、2 号停运
        NPPSim.tg1.condenser.unit(0).condenserPump.setActive(true);
        NPPSim.tg1.condenser.unit(1).condenserPump.setActive(false);
        SwingUtilities.invokeAndWait(() -> sel.setValue(2));   // 先切走，确保下一次 setValue(1) 真的触发
        Thread.sleep(400);
        SwingUtilities.invokeAndWait(() -> sel.setValue(1));
        Thread.sleep(700);
        check("选中 1 号：启动钮选中、灯亮", start.isSelected() && led.isLedOn(),
                "start=" + start.isSelected() + " led=" + led.isLedOn());

        // 切到 2 号（泵停运）
        SwingUtilities.invokeAndWait(() -> sel.setValue(2));
        Thread.sleep(700);
        check("切到 2 号：停止钮选中、灯灭", stop.isSelected() && !led.isLedOn(),
                "stop=" + stop.isSelected() + " led=" + led.isLedOn());

        // 切回 1 号（泵运行）
        SwingUtilities.invokeAndWait(() -> sel.setValue(1));
        Thread.sleep(700);
        check("切回 1 号：启动钮选中、灯亮", start.isSelected() && led.isLedOn(),
                "start=" + start.isSelected() + " led=" + led.isLedOn());

        NPPSim.tg1.condenser.unit(0).condenserPump.setActive(false);
        SwingUtilities.invokeAndWait(ui::dispose);
        System.out.println(fails == 0 ? "TG_SWITCH_OK" : ("TG_SWITCH_FAIL " + fails));
        System.exit(fails == 0 ? 0 : 1);
    }

    static void check(String name, boolean ok, String detail) {
        if (!ok) {
            fails++;
        }
        System.out.printf("[%s] %s %s%n", ok ? "OK" : "FAIL", name, detail);
    }

    static Object get(Object o, String n) throws Exception {
        Class<?> c = o.getClass();
        while (c != null) {
            try {
                Field f = c.getDeclaredField(n);
                f.setAccessible(true);
                return f.get(o);
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            }
        }
        throw new NoSuchFieldException(n);
    }
}
