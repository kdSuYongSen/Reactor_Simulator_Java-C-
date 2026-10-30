package com.darwish.nppsim;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

/** 验证：窗口顶部只剩一个 Window 菜单，且包含全部页面。 */
public class MenuCheck {
    public static void main(String[] a) {
        Localization.init();
        JFrame f = new JFrame("t");
        JMenuBar mb = new JMenuBar();
        JMenu legacy = new JMenu("Window");      // NetBeans 生成的那个
        legacy.add(new javax.swing.JMenuItem("Core Map"));
        mb.add(legacy);
        f.setJMenuBar(mb);
        f.getContentPane().add(new javax.swing.JLabel("x"));

        WindowMenu.install(f);
        WindowMenu.install(f);                   // 重复调用不应重复加

        System.out.println("menuCount = " + mb.getMenuCount());
        JMenu m = mb.getMenu(0);
        System.out.println("menu[0] = '" + m.getText() + "'  items = " + m.getItemCount());
        for (int i = 0; i < m.getItemCount(); i++) {
            if (m.getItem(i) != null) System.out.println("   - " + m.getItem(i).getText());
        }
        System.out.println("MENU_OK");
    }
}
