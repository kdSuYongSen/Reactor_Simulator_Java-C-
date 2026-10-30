package com.darwish.nppsim;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/** 离屏渲染指定窗口（不依赖屏幕，避免被别的窗口挡住）。 */
public class ShotCheck {
    public static void main(String[] a) throws Exception {
        Localization.init();
        Localization.setLanguage(Language.valueOf(System.getProperty("lang", "ZH_CN")));
        Fonts.init();

        EmergencyDieselUI w = new EmergencyDieselUI();
        SwingUtilities.invokeAndWait(() -> {
            w.pack();
            w.setSize(1024, 600);
            w.validate();
        });
        Thread.sleep(300);
        snap(w, "target/verify/shot_ph_" + System.getProperty("lang","ZH_CN") + ".png");

        SettingsDialog d = new SettingsDialog(null);
        final javax.swing.JTabbedPane[] tabs = new javax.swing.JTabbedPane[1];
        SwingUtilities.invokeAndWait(() -> {
            tabs[0] = findTabs(d.getContentPane());
            if (tabs[0] != null) tabs[0].setSelectedIndex(4);
            d.pack();
            d.validate();
        });
        Thread.sleep(300);
        snap(d, "target/verify/shot_lang_" + System.getProperty("lang","ZH_CN") + ".png");

        SwingUtilities.invokeAndWait(() -> { d.dispose(); w.dispose(); });
        System.out.println("SHOT_OK");
        System.exit(0);
    }

    static void snap(Component c, String path) throws Exception {
        if (c instanceof javax.swing.JFrame) {
            c = ((javax.swing.JFrame) c).getContentPane();   // JFrame 本身离屏画不出子组件
        }
        final Component cc = c;
        BufferedImage img = new BufferedImage(Math.max(1, c.getWidth()), Math.max(1, c.getHeight()),
                BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = img.createGraphics();
        SwingUtilities.invokeAndWait(() -> cc.paint(g));
        g.dispose();
        ImageIO.write(img, "png", new File(path));
        System.out.println("saved " + path + " " + img.getWidth() + "x" + img.getHeight());
    }

    static javax.swing.JTabbedPane findTabs(java.awt.Container c) {
        for (java.awt.Component x : c.getComponents()) {
            if (x instanceof javax.swing.JTabbedPane) return (javax.swing.JTabbedPane) x;
            if (x instanceof java.awt.Container) {
                javax.swing.JTabbedPane t = findTabs((java.awt.Container) x);
                if (t != null) return t;
            }
        }
        return null;
    }
}
