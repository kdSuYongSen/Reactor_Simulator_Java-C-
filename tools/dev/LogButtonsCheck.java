package com.darwish.nppsim;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/**
 * 复核：设置 → 原版设置 里的"打开日志 / 清除日志"两个按钮：
 *  ① 不再重合（并排，各自有独立点击区域）；
 *  ② 清除日志点了能清空事件日志；
 *  ③ 打开日志点了能关闭设置框并弹出日志窗口（设置框是模态的，之前日志被它挡住显得"没反应"）。
 */
public class LogButtonsCheck {

    static int fails = 0;

    static void check(String what, boolean ok, String detail) {
        if (!ok) {
            fails++;
        }
        System.out.printf("  [%s] %-44s %s%n", ok ? "OK" : "FAIL", what, detail);
    }

    public static void main(String[] a) throws Exception {
        Localization.init();
        UiStyle.applyLaf();
        Fonts.init();
        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();
        new NPPSim();
        Thread.sleep(500);

        // 造一条事件，确保"清除日志"有东西可清
        NPPSim.autoControl.recordEvent("测试事件 1");
        int before = NPPSim.autoControl.eventLog.size();

        final SettingsDialog d = new SettingsDialog(NPPSim.ui);
        // 用 pack() 完成布局即可（不真的弹模态框，避免阻塞主线程）；
        // 按钮的点击回调不依赖"可见"，doClick 一样会触发。
        SwingUtilities.invokeAndWait(() -> {
            d.pack();
            d.validate();
        });
        Thread.sleep(200);

        JTabbedPane tabs = findTabs(d.getContentPane());
        Container classic = (Container) tabs.getComponentAt(6);   // 原版设置
        JButton open = null;
        JButton clear = null;
        for (Component c : collect(classic)) {
            if (c instanceof JButton) {
                String t = ((JButton) c).getText();
                if (t.contains(Localization.t("settings.classic.openLog"))) {
                    open = (JButton) c;
                } else if (t.contains(Localization.t("settings.classic.clearLog"))) {
                    clear = (JButton) c;
                }
            }
        }
        check("找到两个按钮", open != null && clear != null,
                "open=" + (open == null ? "null" : open.getText())
                        + "  clear=" + (clear == null ? "null" : clear.getText()));

        if (open != null && clear != null) {
            final JButton fOpen = open;
            final JButton fClear = clear;
            Rectangle ro = open.getBounds();
            Rectangle rc = clear.getBounds();
            boolean overlap = ro.intersects(rc);
            check("两个按钮不重合", !overlap,
                    "打开=" + ro + "  清除=" + rc);
            // 清除日志：先暂停仿真，避免后台线程在清除后又补事件
            NPPSim.setPaused(true);
            SwingUtilities.invokeAndWait(() -> fClear.doClick());
            Thread.sleep(200);
            int after = NPPSim.autoControl.eventLog.size();
            check("清除日志生效", after < before,
                    "清除前=" + before + " 清除后=" + after);

            // 打开日志：会 dispose 设置框 + 弹出日志窗口
            SwingUtilities.invokeAndWait(() -> fOpen.doClick());
            Thread.sleep(600);
            boolean settingsGone = !d.isVisible() && !d.isDisplayable();
            boolean logVisible = false;
            for (UIUpdateable u : UI.elementsToUpdate) {
                if (u instanceof LogWindow && ((LogWindow) u).isVisible()) {
                    logVisible = true;
                }
            }
            check("打开日志：设置框已关闭", settingsGone, "settingsVisible=" + d.isVisible());
            check("打开日志：日志窗口已弹出", logVisible, "logVisible=" + logVisible);
        }

        System.out.println(fails == 0 ? "LOG_BUTTONS_OK" : "LOG_BUTTONS_FAIL (" + fails + ")");
        System.exit(0);
    }

    static java.util.List<Component> collect(Container c) {
        java.util.List<Component> out = new java.util.ArrayList<>();
        collect(c, out);
        return out;
    }

    static void collect(Container c, java.util.List<Component> out) {
        for (Component x : c.getComponents()) {
            out.add(x);
            if (x instanceof Container) {
                collect((Container) x, out);
            }
        }
    }

    static JTabbedPane findTabs(Container c) {
        for (Component x : c.getComponents()) {
            if (x instanceof JTabbedPane) {
                return (JTabbedPane) x;
            }
            if (x instanceof Container) {
                JTabbedPane t = findTabs((Container) x);
                if (t != null) {
                    return t;
                }
            }
        }
        return null;
    }
}
