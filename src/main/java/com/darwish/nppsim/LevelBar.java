package com.darwish.nppsim;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;

/**
 * 除盐水箱液位柱（自绘，按用户合体图的外观）：
 * 深灰金属柱体 + 左侧白色刻度（0..4000）+ 红色低水位带（0..1000）+ 中央青色 LED 段液位柱。
 * 量程 0..4500：读数 = 容积百分数 × 10（额定容积 = 1000）；百分比读数以 3000 = 100%。
 * 亮区顶边与刻度共用同一坐标映射，指示线与方块无取整误差。
 */
public class LevelBar extends JComponent {

    private static final double MIN = 0.0, MAX = 4500.0;
    private static final double TRACK_STOP = 1000.0;   // 红色低水位带上限（低于额定容积）
    private static final Color CYAN_FILL = new Color(0x00, 0xB0, 0xE8);
    private static final Color CYAN_EDGE = new Color(0x14, 0x5C, 0x74);
    private static final Color EMPTY_SEG = new Color(0x2C, 0x2C, 0x2C);
    private static final Color EMPTY_EDGE = new Color(0x3A, 0x3A, 0x3A);
    private static final Color RED_ZONE = new Color(0xD4, 0x14, 0x14);
    private static final Color BODY_TOP = new Color(0x48, 0x4A, 0x4C);
    private static final Color BODY_BOTTOM = new Color(0x2E, 0x30, 0x32);

    private double value = 1000.0;

    public double getValue() {
        return value;
    }

    public void setValue(double v) {
        double clamped = Math.max(MIN, Math.min(MAX, v));
        if (clamped != value) {
            value = clamped;
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();

        // 柱体（上亮下暗的金属渐变）
        g.setPaint(new java.awt.GradientPaint(0, 0, BODY_TOP, 0, h, BODY_BOTTOM));
        g.fillRoundRect(0, 0, w, h, 10, 10);
        g.setColor(new Color(0x1C, 0x1C, 0x1C));
        g.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);

        int padT = 14, padB = 14;
        int scaleH = h - padT - padB;
        int scaleTop = padT, scaleBottom = h - padB;
        int tickX = 42;                       // 刻度起点（预留 4 位数字标签的宽度，避免被裁切）
        int stripX = tickX + 10;              // 红色低水位带
        int barX = stripX + 12;               // 液位柱
        int barW = w - barX - 10;

        // 刻度与标尺（白/灰）
        g.setFont(new Font("Dialog", Font.PLAIN, 11));
        FontMetrics fm = g.getFontMetrics();
        double[] ticks = {0, 500, 1000, 1500, 2000, 2500, 3000, 3500, 4000};
        for (double t : ticks) {
            int y = yFor(t, scaleTop, scaleH);
            boolean major = (Math.round(t) % 500 == 0);
            g.setColor(major ? new Color(0xE8, 0xE8, 0xE8) : new Color(0x8A, 0x8A, 0x8A));
            g.drawLine(tickX, y, tickX + (major ? 8 : 4), y);
            if (major) {
                String lbl = String.valueOf((int) t);
                g.setColor(new Color(0xE8, 0xE8, 0xE8));
                g.drawString(lbl, tickX - 4 - fm.stringWidth(lbl), y + fm.getAscent() / 2 - 2);
            }
        }

        // 红色低水位带（0 .. TRACK_STOP）
        int yRed = yFor(TRACK_STOP, scaleTop, scaleH);
        g.setColor(RED_ZONE);
        g.fillRect(stripX, yRed, 7, scaleBottom - yRed);

        // 液位柱：先画空柱底，再按精确刻度坐标连续填充亮段
        // （亮区顶边 = yFor(value)，与刻度线/红色低水位带使用同一映射 → 指示线与方块零误差）
        g.setColor(EMPTY_SEG);
        g.fillRect(barX, scaleTop, barW, scaleBottom - scaleTop);
        g.setColor(EMPTY_EDGE);
        g.drawRect(barX, scaleTop, barW, scaleBottom - scaleTop);

        int fillTop = Math.max(scaleTop, Math.min(scaleBottom, yFor(value, scaleTop, scaleH)));
        if (value > MIN) {
            g.setColor(CYAN_FILL);
            g.fillRect(barX, fillTop, barW, scaleBottom - fillTop);
            g.setColor(CYAN_EDGE);
            g.drawRect(barX, fillTop, barW, scaleBottom - fillTop);
            // LED 段观感：亮区内按固定节距画横向暗隔条（顶段保持完整，不产生计数取整误差）
            int step = 8;   // 亮段 5px + 隔条 3px
            g.setColor(EMPTY_SEG);
            for (int y = scaleBottom - step; y > fillTop + 2; y -= step) {
                g.fillRect(barX, y, barW, 3);
            }
        }
    }

    /** 刻度值 → 纵坐标（0 在底部） */
    private int yFor(double v, int scaleTop, int scaleH) {
        double frac = (v - MIN) / (MAX - MIN);
        return scaleTop + (int) Math.round(scaleH * (1.0 - frac));
    }
}
