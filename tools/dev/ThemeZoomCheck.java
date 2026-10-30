package com.darwish.nppsim;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

/** 三件事的验收：原版背板 / 新窗口配色 / 鼠标定点缩放。 */
public class ThemeZoomCheck {
    public static void main(String[] a) throws Exception {
        Localization.init();
        Localization.setLanguage(Language.ZH_CN);
        UiStyle.applyLaf();          // 真实 LAF：DarkMetal（原版默认）
        Fonts.init();
        UiTheme.install();

        // ---- 1) 背板：LAF 默认面板色应是原版深色，不是橄榄 ----
        Color panelDefault = UIManager.getColor("Panel.background");
        Color labelFg = UIManager.getColor("Label.foreground");
        System.out.println("LAF Panel.background = " + rgb(panelDefault) + "   (原版 DarkMetal 应为 51,51,55)");
        System.out.println("LAF Label.foreground = " + rgb(labelFg) + "   (原版 222,222,222)");
        System.out.println("窗口背板 UiTheme.BG  = " + rgb(UiTheme.BG) + "   (原版 170,130,40)");

        JFrame probe = new JFrame("probe");
        JPanel plain = new JPanel();                       // 不设背景的普通面板
        probe.getContentPane().add(plain);
        UiTheme.apply(probe);
        System.out.println("窗口内容面板        = " + rgb(probe.getContentPane().getBackground()));
        System.out.println("未设色的普通面板    = " + rgb(plain.getBackground()));

        // ---- 2) 占位窗口配色 ----
        EmergencyDieselUI w = new EmergencyDieselUI();
        SwingUtilities.invokeAndWait(() -> { w.setSize(900, 500); w.setVisible(true); w.validate(); });
        Thread.sleep(400);
        BufferedImage img = shot(w.getContentPane());
        ImageIO.write(img, "png", new File("target/verify/theme_placeholder.png"));
        System.out.println("占位页 角落=" + px(img, 4, 4) + " 中部=" + px(img, img.getWidth() / 2, img.getHeight() / 2));

        // ---- 3) 鼠标定点缩放 ----
        JFrame zf = new JFrame("zoom");
        zf.getContentPane().setBackground(UiTheme.BG);
        zf.getContentPane().setLayout(null);
        JLabel mark = new JLabel("MARK");
        mark.setBounds(90, 40, 60, 24);
        zf.getContentPane().add(mark);
        zf.setSize(400, 240);
        zf.setVisible(true);
        Thread.sleep(400);

        Point anchor = new Point(100, 50);
        System.out.println("缩放前 锚点内容坐标 = " + anchor
                + " → 屏幕 " + SwingUtilities.convertPoint(zf.getContentPane(), anchor, zf));

        UiZoom.setWindowScale(zf, 1.5f, anchor);
        Thread.sleep(300);
        BufferedImage z1 = shot(zf.getContentPane());
        ImageIO.write(z1, "png", new File("target/verify/zoom_anchor150.png"));

        // 用 layer 的换算反求：内容点 (100,50) 现在落在哪个层坐标
        ZoomLayer zl = layerOf(zf);
        if (zl != null) {
            int sx = Math.round(anchor.x * zl.scale + zl.tx);
            int sy = Math.round(anchor.y * zl.scale + zl.ty);
            System.out.printf("缩放 1.5 后 同一内容点落在层坐标 = (%d,%d)  [应为 (100,50)]  scale=%.2f t=(%.1f,%.1f)%n",
                    sx, sy, zl.scale, zl.tx, zl.ty);
        } else {
            System.out.println("没有找到缩放层");
        }

        UiZoom.setWindowScale(zf, 2.0f, anchor);
        Thread.sleep(200);
        if (zl != null) {
            int sx = Math.round(anchor.x * zl.scale + zl.tx);
            int sy = Math.round(anchor.y * zl.scale + zl.ty);
            System.out.printf("缩放 2.0 后 = (%d,%d)  scale=%.2f t=(%.1f,%.1f)%n", sx, sy, zl.scale, zl.tx, zl.ty);
        }

        UiZoom.setWindowScale(zf, 1.0f);
        Thread.sleep(200);
        System.out.println("回到 100%: 窗口 " + zf.getWidth() + "x" + zf.getHeight()
                + "  layer=" + layerOf(zf));
        System.out.println("THEME_ZOOM_OK");
        System.exit(0);
    }

    static ZoomLayer layerOf(JFrame f) {
        try {
            java.lang.reflect.Field m = UiZoom.class.getDeclaredField("zoomLayers");
            m.setAccessible(true);
            Object v = ((java.util.Map<?, ?>) m.get(null)).get(f);
            return (ZoomLayer) v;
        } catch (Exception e) {
            return null;
        }
    }

    static String rgb(Color c) {
        return c == null ? "null" : "(" + c.getRed() + "," + c.getGreen() + "," + c.getBlue() + ")";
    }

    static BufferedImage shot(Component c) {
        BufferedImage img = new BufferedImage(Math.max(1, c.getWidth()), Math.max(1, c.getHeight()),
                BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = img.createGraphics();
        c.paint(g);
        g.dispose();
        return img;
    }

    static String px(BufferedImage img, int x, int y) {
        int v = img.getRGB(x, y);
        return "(" + ((v >> 16) & 0xFF) + "," + ((v >> 8) & 0xFF) + "," + (v & 0xFF) + ")";
    }
}
