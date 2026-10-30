package com.darwish.nppsim;

import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

/**
 * 会换算脏区的 {@link RepaintManager}（2026-10-26）。
 *
 * <p>问题：缩放是把内容整体放大 s 倍**画**出去的，但 Swing 的重绘是"组件坐标"驱动的。
 * 一个 40×20 的小灯在 200% 下屏幕上占 80×40，而它 repaint() 时 RepaintManager 只记
 * {@code (0,0,40,20)} —— 实际只重画了左上角四分之一，其余保持旧颜色。
 * 表现就是**报警灯/AZ 棒指示灯闪烁时出现残影、半亮**。</p>
 *
 * <p>这里把落在"已缩放视图"里的脏区换算成层坐标（{@code screen = content·s + t}）后
 * 记到 {@code host} 上，由 host 负责重画那一块（含背景与挡板）。100% 或不在缩放层里的
 * 组件原样透传，行为与默认 RepaintManager 完全一致。</p>
 */
final class ZoomRepaintManager extends RepaintManager {

    @Override
    public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
        if (w > 0 && h > 0 && c != null) {
            ZoomLayer z = ZoomLayer.ownerOf(c);
            if (z != null && z.isScaled()) {
                JComponent view = z.view();
                if (view != null) {
                    Rectangle r;
                    try {
                        r = SwingUtilities.convertRectangle(c, new Rectangle(x, y, w, h), view);
                    } catch (RuntimeException ex) {
                        r = null;
                    }
                    if (r != null) {
                        // 内容坐标 → 层坐标；外扩 1px 吃掉取整误差，避免留细缝
                        int rx = (int) Math.floor(r.x * z.scale + z.tx) - 1;
                        int ry = (int) Math.floor(r.y * z.scale + z.ty) - 1;
                        int rw = (int) Math.ceil(r.width * z.scale) + 2;
                        int rh = (int) Math.ceil(r.height * z.scale) + 2;
                        if (Boolean.getBoolean("zoom.debug")) {
                            System.out.printf("[repaint] %s (%d,%d,%d,%d) → host (%d,%d,%d,%d) s=%.2f%n",
                                    c.getClass().getSimpleName(), x, y, w, h, rx, ry, rw, rh, z.scale);
                        }
                        super.addDirtyRegion(z.host, rx, ry, rw, rh);
                        return;
                    }
                }
            }
        }
        super.addDirtyRegion(c, x, y, w, h);
    }
}
