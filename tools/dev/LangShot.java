package com.darwish.nppsim;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JTabbedPane;

/** 只渲染"语言"页，检查各语言自称是否方框。 */
public class LangShot {
    public static void main(String[] a) throws Exception {
        Localization.init();
        Localization.setLanguage(Language.valueOf(System.getProperty("lang", "EN")));
        Fonts.init();
        SettingsDialog d = new SettingsDialog(null);
        JTabbedPane tabs = find(d.getContentPane());
        Component page = tabs.getComponentAt(Integer.getInteger("tab", 4));
        page.setSize(500, 380);
        ((java.awt.Container) page).doLayout();
        BufferedImage img = new BufferedImage(500, 380, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = img.createGraphics();
        page.paint(g);
        g.dispose();
        ImageIO.write(img, "png", new File("target/verify/tab" + Integer.getInteger("tab", 4) + "_" + System.getProperty("lang","EN") + ".png"));
        System.out.println("saved");
        System.exit(0);
    }

    static JTabbedPane find(java.awt.Container c) {
        for (Component x : c.getComponents()) {
            if (x instanceof JTabbedPane) return (JTabbedPane) x;
            if (x instanceof java.awt.Container) {
                JTabbedPane t = find((java.awt.Container) x);
                if (t != null) return t;
            }
        }
        return null;
    }
}
