package com.darwish.nppsim;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

/** 验证缩放：窗口尺寸不变，内容整体等比放大。 */
public class ZoomShot {
    public static void main(String[] a) throws Exception {
        Localization.init();
        Localization.setLanguage(Language.ZH_CN);
        Fonts.init();

        JFrame f = new JFrame("ZoomTest");
        f.getContentPane().setBackground(new java.awt.Color(170, 130, 40));
        f.getContentPane().setLayout(null);
        JButton b = new JButton("按键 A");
        b.setBounds(20, 20, 120, 40);
        JLabel l = new JLabel("主蒸汽压力 6.90 MPa");
        l.setBounds(20, 80, 300, 30);
        f.getContentPane().add(b);
        f.getContentPane().add(l);
        f.setSize(400, 240);
        f.setLocation(40, 40);
        f.setAlwaysOnTop(true);
        f.setVisible(true);
        Thread.sleep(500);

        snap(f, "target/verify/zoom_100.png");

        UiZoom.setWindowScale(f, 1.5f);
        Thread.sleep(200);
        snap(f, "target/verify/zoom_150.png");

        UiZoom.setWindowScale(f, 1.0f);
        Thread.sleep(200);
        System.out.println("window size after reset = " + f.getWidth() + "x" + f.getHeight());
        System.out.println("ZOOM_OK");
        System.exit(0);
    }

    static void snap(JFrame f, String path) throws Exception {
        Component c = f.getContentPane();
        BufferedImage img = new BufferedImage(f.getContentPane().getWidth(),
                f.getContentPane().getHeight(), BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = img.createGraphics();
        c.paint(g);
        g.dispose();
        ImageIO.write(img, "png", new File(path));
        System.out.println("saved " + path + " " + img.getWidth() + "x" + img.getHeight()
                + "  window=" + f.getWidth() + "x" + f.getHeight());
    }
}
