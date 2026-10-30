package com.darwish.nppsim;

import java.awt.Component;
import java.awt.Container;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/** 把"设置 → 工具"页渲染成 PNG，肉眼看布局是否合理。 */
public class ToolsTabShot {
    public static void main(String[] a) throws Exception {
        Localization.init();
        Localization.setLanguage(Language.valueOf(System.getProperty("lang", "ZH_CN")));
        UiStyle.applyLaf();
        Fonts.init();
        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();
        NPPSim sim = new NPPSim();
        CondensateUI cond = new CondensateUI();
        cond.setSize(980, 560);
        cond.setVisible(true);
        cond.validate();
        Thread.sleep(1500);                 // 等页面注册（350 ms）

        // SettingsDialog 是模态的：setVisible(true) 会阻塞，这里不显示，直接摆好再离屏绘制
        SettingsDialog d = new SettingsDialog(null);
        JTabbedPane tabs = find(d.getContentPane());
        int idx = Integer.getInteger("tab", 7);
        if (tabs != null) {
            tabs.setSelectedIndex(idx);
        }
        d.setSize(1000, 660);
        d.validate();
        Thread.sleep(1500);                 // 等工具页 600 ms 后枚举窗口

        Component page = tabs.getComponentAt(idx);
        page.setSize(980, 600);
        if (page instanceof Container) {
            ((Container) page).doLayout();
        }
        page.validate();
        Thread.sleep(200);
        BufferedImage img = new BufferedImage(Math.max(1, page.getWidth()),
                Math.max(1, page.getHeight()), BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = img.createGraphics();
        page.paint(g);
        g.dispose();
        ImageIO.write(img, "png", new File("target/verify/tools_tab.png"));
        System.out.println("saved tools_tab.png " + img.getWidth() + "x" + img.getHeight());

        System.out.println("SHOT_OK");
        System.exit(0);
    }

    static JTabbedPane find(Container c) {
        for (Component x : c.getComponents()) {
            if (x instanceof JTabbedPane) return (JTabbedPane) x;
            if (x instanceof Container) {
                JTabbedPane t = find((Container) x);
                if (t != null) return t;
            }
        }
        return null;
    }
}
