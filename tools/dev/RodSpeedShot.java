package com.darwish.nppsim;

import java.awt.Rectangle;
import java.awt.Robot;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/** 抓"控制棒按钮块"的局部截图（看金属键与标题）。 */
public class RodSpeedShot {
    public static void main(String[] a) throws Exception {
        Localization.init();
        UiStyle.applyLaf();
        Fonts.init();
        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();
        new NPPSim();
        Thread.sleep(1200);
        UI ui = NPPSim.ui;
        JPanel p5 = ui.getRodPanelForShot();
        int w = p5.getWidth();
        int h = p5.getHeight();
        java.awt.image.BufferedImage img =
                new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = img.createGraphics();
        SwingUtilities.invokeAndWait(() -> p5.paintAll(g));
        g.dispose();
        ImageIO.write(img, "png", new File("target/shots/rod_speed.png"));
        System.out.println("shot " + w + "x" + h);
        System.exit(0);
    }
}
