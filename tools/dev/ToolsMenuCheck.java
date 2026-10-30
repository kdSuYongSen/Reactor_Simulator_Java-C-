package com.darwish.nppsim;

import java.lang.reflect.Method;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * 验证：工具菜单的第 2 项是布局编辑器 jar 模组，且 jar 能从程序资源里解出来。
 *
 * 用法：
 *   javac -encoding UTF-8 -cp "target\RBMK-1500-Simulator-1.00Alpha-jar-with-dependencies.jar" ^
 *         -d target\verify tools\dev\ToolsMenuCheck.java
 *   java -cp "target\RBMK-1500-Simulator-1.00Alpha-jar-with-dependencies.jar;target\verify" ^
 *        com.darwish.nppsim.ToolsMenuCheck
 */
public class ToolsMenuCheck {
    public static void main(String[] a) throws Exception {
        Localization.init();
        UiStyle.applyLaf();
        Fonts.init();
        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();
        NPPSim sim = new NPPSim();
        Thread.sleep(800);

        JFrame frame = NPPSim.ui;
        JMenuBar mb = frame.getJMenuBar();
        JMenu tools = findMenu(mb, Localization.t("Tools"));
        if (tools == null) {
            System.out.println("TOOLS_MENU_MISSING");
            System.exit(1);
        }
        System.out.println("工具菜单项数 = " + tools.getItemCount());
        for (int i = 0; i < tools.getItemCount(); i++) {
            JMenuItem it = tools.getItem(i);
            System.out.printf("  %d. %-34s enabled=%s%n", i + 1,
                    it == null ? "(null)" : it.getText(), it != null && it.isEnabled());
        }
        boolean ok = tools.getItemCount() == 6
                && tools.getItem(0) != null && tools.getItem(0).isEnabled()
                && tools.getItem(1) != null && tools.getItem(1).isEnabled()
                && tools.getItem(1).getText().contains("jar");
        for (int i = 2; i < 6; i++) {
            ok &= tools.getItem(i) != null && !tools.getItem(i).isEnabled();
        }

        // jar 解析（资源解包）
        Method m = UI.class.getDeclaredMethod("resolveLayoutEditorJar");
        m.setAccessible(true);
        java.io.File jar = (java.io.File) m.invoke(null);
        System.out.println("解析到的 jar = " + jar);
        if (jar != null) {
            System.out.println("  exists=" + jar.isFile() + "  size=" + jar.length());
        }
        ok &= jar != null && jar.isFile() && jar.length() > 10 * 1024;

        System.out.println(ok ? "TOOLS_MENU_OK" : "TOOLS_MENU_FAIL");
        System.exit(ok ? 0 : 1);
    }

    /** 递归找菜单（"工具" 是 设置 下面的子菜单） */
    static JMenu findMenu(JMenuBar mb, String text) {
        for (int i = 0; i < mb.getMenuCount(); i++) {
            JMenu m = mb.getMenu(i);
            JMenu hit = findMenu(m, text);
            if (hit != null) {
                return hit;
            }
        }
        return null;
    }

    static JMenu findMenu(JMenu parent, String text) {
        if (parent == null) {
            return null;
        }
        if (text.equals(parent.getText())) {
            return parent;
        }
        for (int i = 0; i < parent.getItemCount(); i++) {
            JMenuItem it = parent.getItem(i);
            if (it instanceof JMenu) {
                JMenu hit = findMenu((JMenu) it, text);
                if (hit != null) {
                    return hit;
                }
            }
        }
        return null;
    }
}
