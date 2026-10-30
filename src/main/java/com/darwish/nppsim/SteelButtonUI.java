package com.darwish.nppsim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JButton;

/**
 * 金属质感按钮：竖向拉丝渐变 + 高光边 + 内凹阴影，按下时渐变反转、整体下沉 1 px。
 *
 * <p>给"棒速度"的两个 &lt; &gt; 键用；不依赖外观主题，任何 LAF 下都是同一副样子。</p>
 */
final class SteelButtonUI extends javax.swing.plaf.basic.BasicButtonUI {

    private static final Color TOP = new Color(0xF2, 0xF2, 0xF2);
    private static final Color BOTTOM = new Color(0xA9, 0xA9, 0xA9);
    private static final Color EDGE_DARK = new Color(0x6C, 0x6C, 0x6C);
    private static final Color EDGE_LIGHT = new Color(0xFF, 0xFF, 0xFF);

    /** 装到一个按钮上（顺便设好字体、尺寸、聚焦框） */
    static void install(JButton b, int w, int h) {
        b.setUI(new SteelButtonUI());
        b.setFont(Fonts.text("Dialog", Font.BOLD, 12));
        b.setPreferredSize(new Dimension(w, h));
        b.setMinimumSize(new Dimension(w, h));
        b.setMaximumSize(new Dimension(w, h));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setOpaque(false);
        b.setMargin(new java.awt.Insets(0, 0, 0, 0));
        b.setRolloverEnabled(true);
    }

    @Override
    public void paint(Graphics g, javax.swing.JComponent c) {
        AbstractButton b = (AbstractButton) c;
        ButtonModel m = b.getModel();
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = c.getWidth();
            int h = c.getHeight();
            boolean pressed = m.isArmed() && m.isPressed();
            boolean enabled = c.isEnabled();
            boolean hover = m.isRollover() && enabled;

            int y = pressed ? 1 : 0;                 // 按下整体下沉 1 px
            Color top = enabled ? TOP : new Color(0xE8, 0xE8, 0xE8);
            Color bot = enabled ? BOTTOM : new Color(0xC8, 0xC8, 0xC8);
            if (hover && !pressed) {
                top = new Color(0xFC, 0xFC, 0xFC);
                bot = new Color(0xBE, 0xBE, 0xBE);
            }
            // 主体：竖向渐变（按下反向 = 凹进去）
            g2.setPaint(pressed
                    ? new GradientPaint(0, y, bot, 0, y + h - 1, top)
                    : new GradientPaint(0, y, top, 0, y + h - 1, bot));
            g2.fillRoundRect(0, y, w - 1, h - 1, 5, 5);

            // 边：上/左高光，下/右暗边
            g2.setColor(pressed ? EDGE_DARK : EDGE_LIGHT);
            g2.drawLine(2, y + 1, w - 3, y + 1);
            g2.setColor(EDGE_DARK);
            g2.drawRoundRect(0, y, w - 1, h - 1, 5, 5);
            if (!pressed) {
                g2.setColor(new Color(0x8E, 0x8E, 0x8E));
                g2.drawLine(1, y + h - 2, w - 2, y + h - 2);
            }

            // 文字
            String text = b.getText();
            if (text != null && !text.isEmpty()) {
                g2.setFont(c.getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth(text)) / 2;
                int ty = y + (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.setColor(enabled ? new Color(0x20, 0x20, 0x20) : new Color(0x8A, 0x8A, 0x8A));
                g2.drawString(text, tx, ty);
            }
        } finally {
            g2.dispose();
        }
    }
}
