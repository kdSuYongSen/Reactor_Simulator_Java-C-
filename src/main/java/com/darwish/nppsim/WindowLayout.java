package com.darwish.nppsim;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * 在不改动 NetBeans 生成的 GroupLayout 的前提下，往窗口底部追加一个面板：
 * 用 BorderLayout 把**原内容面板**包进 CENTER，新面板放到 SOUTH，再 pack 并限制在屏幕内。
 * 这样新功能与老界面互不干扰（老界面照旧，滚动区自动变矮）。
 */
public final class WindowLayout {

    private WindowLayout() {
    }

    public static void appendSouth(JFrame frame, JComponent addition) {
        if (frame == null || addition == null) {
            return;
        }
        Container oldRoot = frame.getContentPane();
        JPanel newRoot = new JPanel(new BorderLayout());
        newRoot.setBackground(UI.BACKGROUND);
        newRoot.add((JComponent) oldRoot, BorderLayout.CENTER);
        newRoot.add(addition, BorderLayout.SOUTH);
        frame.setContentPane(newRoot);
        frame.pack();
        clampToScreen(frame);
    }

    /** pack 后可能超出屏幕：限制到可用屏幕范围，并把窗口整体移回屏幕内（保证底部追加行可见） */
    public static void clampToScreen(JFrame frame) {
        try {
            Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            Dimension size = frame.getSize();
            int w = Math.min(size.width, screen.width);
            int h = Math.min(size.height, screen.height);
            if (w != size.width || h != size.height) {
                frame.setSize(w, h);
            }
            java.awt.Point loc = frame.getLocation();
            int x = loc.x;
            int y = loc.y;
            if (x + w > screen.x + screen.width) {
                x = Math.max(screen.x, screen.x + screen.width - w);
            }
            if (y + h > screen.y + screen.height) {
                y = Math.max(screen.y, screen.y + screen.height - h);
            }
            if (x != loc.x || y != loc.y) {
                frame.setLocation(x, y);
            }
        } catch (Throwable ignored) {
        }
    }
}
