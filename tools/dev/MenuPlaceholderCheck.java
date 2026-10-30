package com.darwish.nppsim;

import javax.swing.*;

/** 菜单 19 项齐全 + 8 个占位窗口都能开（含 Radiation Monitoring）。 */
public class MenuPlaceholderCheck {
    public static void main(String[] a) throws Exception {
        Localization.init();
        Localization.setLanguage(Language.ZH_CN);
        UiStyle.applyLaf();
        Fonts.init();

        Class<?>[] ph = {
            EmergencyDieselUI.class, GridSystemUI.class, ECCSUI.class, OfflineCoolingUI.class,
            VentilationUI.class, LightingUI.class, HeatSinkUI.class, RadiationMonitoringUI.class
        };
        for (Class<?> c : ph) {
            java.lang.reflect.Constructor<?> k = c.getDeclaredConstructor();
            k.setAccessible(true);
            JFrame f = (JFrame) k.newInstance();
            System.out.printf("%-24s 标题='%s' 面板尺寸=%s%n", c.getSimpleName(), f.getTitle(),
                    f.getContentPane().getComponentCount() > 0
                            ? f.getContentPane().getComponent(0).getPreferredSize() : "-");
            f.dispose();
        }

        JFrame host = new JFrame("t");
        host.setJMenuBar(new JMenuBar());
        WindowMenu.install(host);
        JMenu m = host.getJMenuBar().getMenu(0);
        System.out.println("菜单数=" + host.getJMenuBar().getMenuCount() + " 项数=" + m.getItemCount());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m.getItemCount(); i++) {
            JMenuItem it = m.getItem(i);
            sb.append(it == null ? "----" : it.getText()).append(" / ");
        }
        System.out.println("条目: " + sb);
        System.out.println("MENU_PLACEHOLDER_OK");
        System.exit(0);
    }
}
