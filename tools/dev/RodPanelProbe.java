package com.darwish.nppsim;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Field;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * 量一下主窗口"控制棒按钮块"（jPanel5：RESET / 拔出 / 插入）的实际尺寸，
 * 为新增"棒速度"小块预留空间。
 */
public class RodPanelProbe {

    public static void main(String[] a) throws Exception {
        Localization.init();
        UiStyle.applyLaf();
        Fonts.init();
        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();
        new NPPSim();
        Thread.sleep(900);

        UI ui = NPPSim.ui;
        JPanel p5 = (JPanel) get(ui, "jPanel5");
        System.out.println("jPanel5 bounds=" + p5.getBounds() + " pref=" + p5.getPreferredSize()
                + " min=" + p5.getMinimumSize());
        System.out.println("  父容器 bounds=" + p5.getParent().getBounds()
                + " 类型=" + p5.getParent().getClass().getSimpleName());
        for (Component c : p5.getComponents()) {
            String t = "";
            if (c instanceof javax.swing.AbstractButton) {
                t = ((javax.swing.AbstractButton) c).getText();
            }
            System.out.println("  - " + c.getClass().getSimpleName() + " '" + t + "' bounds=" + c.getBounds()
                    + " pref=" + c.getPreferredSize());
        }
        System.out.println("  剩余下方空间 = " + (p5.getHeight() - lastBottom(p5)) + " px");
        System.exit(0);
    }

    static int lastBottom(Container c) {
        int b = 0;
        for (Component x : c.getComponents()) {
            b = Math.max(b, x.getBounds().y + x.getBounds().height);
        }
        return b;
    }

    static Object get(Object o, String name) throws Exception {
        Field f = o.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(o);
    }
}
