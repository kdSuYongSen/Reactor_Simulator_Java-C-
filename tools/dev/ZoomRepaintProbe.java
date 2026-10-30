package com.darwish.nppsim;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

/** 量一下：缩放后子组件重绘时，Swing 记的"脏区"到底是未缩放的还是缩放后的。 */
public class ZoomRepaintProbe {

    static final List<String> log = new ArrayList<>();

    public static void main(String[] a) throws Exception {
        UiStyle.applyLaf();

        JFrame f = new JFrame("probe");
        f.getContentPane().setBackground(new Color(170, 130, 40));
        f.getContentPane().setLayout(null);
        JLabel lamp = new JLabel("");
        lamp.setOpaque(true);
        lamp.setBackground(new Color(107, 103, 0));
        lamp.setBounds(100, 50, 40, 20);
        f.getContentPane().add(lamp);
        f.setSize(400, 240);
        f.setLocation(60, 60);
        f.setVisible(true);
        Thread.sleep(600);

        // 用产品自己的 RepaintManager（UiZoom.init 安装），只把调用记下来
        UiZoom.init();
        RepaintManager old = RepaintManager.currentManager(f);

        // 100%：灯变亮
        lamp.setBackground(new Color(255, 246, 35));
        lamp.repaint();
        Thread.sleep(300);
        System.out.println("--- 100% ---");
        for (String s : log) System.out.println("  " + s);
        log.clear();

        // 放大到 2.0，锚点 (0,0)：屏幕上的灯占 (200,100)-(280,140)
        UiZoom.setWindowScale(f, 2.0f, new Point(0, 0));
        Thread.sleep(500);
        System.out.println("灯内容坐标=" + lamp.getBounds()
                + " 放大 2× 后屏幕上占 (" + (lamp.getX() * 2) + "," + (lamp.getY() * 2) + ")-("
                + ((lamp.getX() + lamp.getWidth()) * 2) + "," + ((lamp.getY() + lamp.getHeight()) * 2) + ")");

        lamp.setBackground(new Color(107, 103, 0));
        lamp.repaint();
        Thread.sleep(300);
        System.out.println("--- 200% ---");
        for (String s : log) System.out.println("  " + s);
        log.clear();

        System.out.println("manager=" + old.getClass().getSimpleName());
        System.out.println("PROBE_DONE");
        System.exit(0);
    }
}
