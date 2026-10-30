package com.darwish.nppsim;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/** 真机验证：缩放后闪烁的灯是否整块变色（没有残影）。 */
public class ZoomBlinkShot {
    public static void main(String[] a) throws Exception {
        UiStyle.applyLaf();
        JFrame f = new JFrame("blink");
        f.getContentPane().setBackground(new Color(170, 130, 40));
        f.getContentPane().setLayout(null);
        JLabel lamp = new JLabel("");
        lamp.setOpaque(true);
        lamp.setBackground(new Color(107, 103, 0));      // YELLOWOFF
        lamp.setBounds(100, 50, 40, 20);
        f.getContentPane().add(lamp);
        f.setSize(400, 240);
        f.setLocation(120, 120);
        f.setAlwaysOnTop(true);
        f.setVisible(true);
        UiZoom.init();
        Thread.sleep(700);

        UiZoom.setWindowScale(f, 2.0f, new Point(0, 0));
        Thread.sleep(400);

        Robot r = new Robot();
        Point o = f.getContentPane().getLocationOnScreen();
        // 屏幕上灯占 (200,100)-(280,140)：取样四角 + 中心
        int[][] pts = {{205,105},{275,105},{205,135},{275,135},{240,120}};

        lamp.setBackground(new Color(255, 246, 35));     // YELLOWON
        Thread.sleep(300);
        BufferedImage on = r.createScreenCapture(new Rectangle(o.x + 195, o.y + 95, 90, 50));
        System.out.print("亮: ");
        for (int[] p : pts) System.out.print(rgb(on, p[0] - 195, p[1] - 95) + " ");
        System.out.println();

        lamp.setBackground(new Color(107, 103, 0));      // YELLOWOFF
        Thread.sleep(300);
        BufferedImage off = r.createScreenCapture(new Rectangle(o.x + 195, o.y + 95, 90, 50));
        System.out.print("灭: ");
        for (int[] p : pts) System.out.print(rgb(off, p[0] - 195, p[1] - 95) + " ");
        System.out.println();

        ImageIO.write(on, "png", new File("target/verify/blink_on.png"));
        ImageIO.write(off, "png", new File("target/verify/blink_off.png"));
        System.out.println("BLINK_SHOT_DONE");
        System.exit(0);
    }

    static String rgb(BufferedImage img, int x, int y) {
        if (x < 0 || y < 0 || x >= img.getWidth() || y >= img.getHeight()) return "(oob)";
        int v = img.getRGB(x, y);
        return "(" + ((v >> 16) & 0xFF) + "," + ((v >> 8) & 0xFF) + "," + (v & 0xFF) + ")";
    }
}
