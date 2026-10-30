package com.darwish.nppsim;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * 监控窗口顶部选项栏：「Window」导航菜单，可在任意监控窗口之间跳转。
 *
 * 2026-10-25：**合并**成一个菜单。
 *  - NetBeans 生成的窗口自带一个名为 "Window" 的导航菜单，早期实现又在末尾追加了一个
 *    "Windows"，于是每个窗口顶部都有两个几乎一样的窗口菜单；
 *  - 现在把已存在的那个（不管叫 Window 还是 Windows）就地接管：先清空它的条目，
 *    再按同一份清单重建；多出来的同名菜单直接删掉。
 *
 * 这样每个窗口都只有**一个** Window 菜单，且条目集合完全一致（不再出现
 * "这个窗口有除氧器、那个窗口没有"）。
 */
final class WindowMenu {

    private static final String MENU_NAME = "__window_nav__";
    /** 旧的 NetBeans 菜单标题（单数/复数/中文都认） */
    private static final String[] LEGACY_TITLES = {"Window", "Windows", "窗口"};

    /** 页面清单：i18n key + 窗口类（顺序即菜单顺序） */
    private static final Object[][] ITEMS = {
        {"Core Map", CoreMap.class},
        {"Selsyn Panel", SelsynPanel.class},
        {"Turbine-Generators", TGUI.class},
        {"MCP", MCPUI.class},
        {"Condensate", CondensateUI.class},
        {"Dearator", DearatorUI.class},
        {"Feedwater", FeedwaterUI.class},
        {"PCS", PCSUI.class},
        {"Steam Loop", SteamLoopUI.class},
        // —— 2026-10-25 新增（暂为占位页：黄底 + 未完待续）——
        {"Emergency Diesel Generators", EmergencyDieselUI.class},
        {"Grid System", GridSystemUI.class},
        {"ECCS", ECCSUI.class},
        {"Offline Cooling", OfflineCoolingUI.class},
        {"Ventilation System", VentilationUI.class},
        {"Lighting System", LightingUI.class},
        {"Heat Sink", HeatSinkUI.class},
        {"Radiation Monitoring", RadiationMonitoringUI.class},
    };

    static void install(JFrame f) {
        JMenuBar mb = f.getJMenuBar();
        if (mb == null) {
            mb = new JMenuBar();
            f.setJMenuBar(mb);
        }

        JMenu target = null;
        List<JMenu> duplicates = new ArrayList<>();
        for (int i = 0; i < mb.getMenuCount(); i++) {
            JMenu m = mb.getMenu(i);
            if (m == null) continue;
            boolean isNav = MENU_NAME.equals(m.getName()) || isLegacyTitle(m.getText());
            if (!isNav) continue;
            if (target == null) {
                target = m;
            } else {
                duplicates.add(m);          // 第二个及以后的同名菜单，稍后删掉
            }
        }
        for (JMenu dup : duplicates) {
            mb.remove(dup);
        }
        if (target == null) {
            target = new JMenu(Localization.t("Windows"));
            mb.add(target);
        }
        target.setName(MENU_NAME);
        target.setText(Localization.t("Windows"));
        target.setFont(new Font("Dialog", Font.BOLD, 12));

        // 就地重建：保证所有窗口的条目完全一致
        target.removeAll();

        JMenuItem main = new JMenuItem(Localization.t("Main Control Room"));
        main.addActionListener(e -> {
            if (NPPSim.ui != null) {
                NPPSim.ui.setVisible(true);
                NPPSim.ui.setState(JFrame.NORMAL);
                NPPSim.ui.toFront();
                NPPSim.ui.requestFocus();
            }
        });
        target.add(main);
        target.addSeparator();

        for (Object[] item : ITEMS) {
            add(target, (String) item[0], (Class<?>) item[1]);
        }
        mb.revalidate();
        mb.repaint();
    }

    private static boolean isLegacyTitle(String text) {
        if (text == null) return false;
        for (String t : LEGACY_TITLES) {
            if (t.equalsIgnoreCase(text.trim())) return true;
        }
        return false;
    }

    private static void add(JMenu menu, String key, Class<?> windowClass) {
        JMenuItem it = new JMenuItem(Localization.t(key));
        it.addActionListener(e -> UI.createOrContinue(windowClass, true, false));
        menu.add(it);
    }

    private WindowMenu() {
    }
}
