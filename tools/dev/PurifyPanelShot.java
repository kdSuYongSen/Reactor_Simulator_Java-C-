package com.darwish.nppsim;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/** 通用净化装置面板截图（深色背板 + 运行灯 + 启停 + 流量/电流/温度）。 */
public class PurifyPanelShot {
    public static void main(String[] a) throws Exception {
        Localization.init();
        Localization.setLanguage(Language.ZH_CN);
        UiStyle.applyLaf();
        Fonts.init();
        UiTheme.install();
        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();
        new NPPSim();
        Thread.sleep(900);

        final PurificationPanel p = new PurificationPanel();
        final javax.swing.JFrame f = new javax.swing.JFrame("purify");
        SwingUtilities.invokeAndWait(() -> {
            f.setUndecorated(true);
            f.getContentPane().add(p);
            f.setSize(400, 140);
            f.setLocation(40, 40);
            f.setVisible(true);
        });
        Thread.sleep(1200);
        SwingUtilities.invokeAndWait(() -> {
            f.validate();
            p.refresh();
        });
        Thread.sleep(600);
        BufferedImage img = new BufferedImage(400, 140, BufferedImage.TYPE_INT_RGB);
        SwingUtilities.invokeAndWait(() -> {
            Graphics2D g = img.createGraphics();
            f.getContentPane().paintAll(g);
            g.dispose();
        });
        File out = new File(a.length > 0 ? a[0] : "target/shots/purification.png");
        out.getParentFile().mkdirs();
        ImageIO.write(img, "png", out);
        System.out.println("saved " + out.getAbsolutePath() + " 400x140");
        System.exit(0);
    }
}
