package com.darwish.nppsim;

import java.awt.Component;
import java.awt.Container;
import javax.swing.*;

/** 验证：占位窗口内容 / 设置页签标题与内容是否对应。 */
public class UiInitCheck {
    public static void main(String[] a) throws Exception {
        Localization.init();
        SwingUtilities.invokeAndWait(() -> {
            EmergencyDieselUI w = new EmergencyDieselUI();
            System.out.println("占位窗口标题 = " + w.getTitle());
            System.out.println("背景色 = " + w.getContentPane().getBackground());
            System.out.println("文字 = " + findLabel(w.getContentPane()));

            SettingsDialog d = new SettingsDialog(null);
            JTabbedPane tabs = findTabs(d.getContentPane());
            if (tabs != null) {
                for (int i = 0; i < tabs.getTabCount(); i++) {
                    System.out.printf("  页签 %d: [%s]%n", i, tabs.getTitleAt(i));
                }
                StringBuilder sb = new StringBuilder();
                listCombos((Container) tabs.getComponentAt(3), sb);
                System.out.println("  单位页下拉框：" + sb);
            }
            d.dispose();
            w.dispose();
            System.out.println("UI_INIT_OK");
        });
        System.exit(0);
    }

    static void listCombos(Container c, StringBuilder sb) {
        for (Component x : c.getComponents()) {
            if (x instanceof JComboBox) {
                JComboBox<?> cb = (JComboBox<?>) x;
                sb.append("[");
                for (int i = 0; i < cb.getItemCount(); i++) {
                    sb.append(cb.getItemAt(i)).append(i == cb.getItemCount() - 1 ? "" : "/");
                }
                sb.append("]=").append(cb.getSelectedItem()).append(" ");
            }
            if (x instanceof Container) listCombos((Container) x, sb);
        }
    }

    static String findLabel(Container c) {
        for (Component x : c.getComponents()) {
            if (x instanceof JLabel) return ((JLabel) x).getText();
            if (x instanceof Container) {
                String s = findLabel((Container) x);
                if (s != null) return s;
            }
        }
        return null;
    }

    static JTabbedPane findTabs(Container c) {
        for (Component x : c.getComponents()) {
            if (x instanceof JTabbedPane) return (JTabbedPane) x;
            if (x instanceof Container) {
                JTabbedPane t = findTabs((Container) x);
                if (t != null) return t;
            }
        }
        return null;
    }
}
