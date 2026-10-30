package com.darwish.nppsim;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JLayer;
import javax.swing.SwingUtilities;
import javax.swing.plaf.LayerUI;

/**
 * 界面缩放层（2026-10-26 最终版：**以鼠标箭头指向的位置为中心**缩放，且放大后还能点得准）。
 *
 * 变换只有一条：
 *
 * <pre>
 *   screen = content × scale + (tx, ty)
 * </pre>
 *
 * 定点缩放：让光标下的那个"内容点"缩放前后落在同一个屏幕位置。
 * 设光标在层内的位置 p，缩放前 (s₀, t₀) → 内容点 c = (p − t₀)/s₀，
 * 缩放到 s₁ 后要求 c·s₁ + t₁ = p，于是
 *
 * <pre>
 *   t₁ = p − (p − t₀) · s₁ / s₀
 * </pre>
 *
 * 平移量夹在"内容刚好盖满窗口"的范围（放大时不许露空边，缩小时居中）。
 *
 * <b>为什么还要一层挡板</b>：{@code JLayer} 只对"派发给它自己"的事件回调
 * {@link LayerUI#eventDispatched}，子组件（按键、滑阀）会**直接**收到原始坐标的鼠标事件，
 * 放大后光标下的组件和实际点到的组件就对不上了（实测：200% 时点按键新位置完全没有反应）。
 * 所以在内容之上再叠一层透明 {@link ZoomGlass} 专门收鼠标事件，按 (p − t)/s 换算后
 * 转发给真正的目标组件。100% 时不叠任何东西，正常游玩零开销。
 *
 * 注意：{@code javax.swing.JLayer} 是 final，不能继承，所以这里用组合。
 */
final class ZoomLayer {

    /**
     * 内容视图 → 缩放层。
     *
     * 给 {@link ZoomRepaintManager} 用：子组件 repaint 时 Swing 记的脏区是**未缩放**的
     * 组件坐标，而屏幕上是按 s 倍画的，脏区必须换算成层坐标，否则只重画左上角一小块
     * （实测 2× 时 40×20 的灯只重画了 40×20 屏幕像素，实际占 80×40 → 闪烁时留残影）。
     * 用 IdentityHashMap（JComponent 没有重写 equals，本来就是身份比较）+ 拆层时注销。
     */
    private static final java.util.Map<JComponent, ZoomLayer> BY_VIEW =
            java.util.Collections.synchronizedMap(new java.util.IdentityHashMap<>());

    /** c 是否在某个"已缩放"的内容视图里；是则返回那个层 */
    static ZoomLayer ownerOf(java.awt.Component c) {
        java.awt.Component x = c;
        while (x != null) {
            ZoomLayer z = BY_VIEW.get(x);
            if (z != null) {
                return z;
            }
            x = x.getParent();
        }
        return null;
    }

    /** 装"内容层 + 挡板"的容器，替换掉的只是窗口内容面板，不动菜单栏 */
    final JLayeredPane host;
    final JLayer<JComponent> layer;
    private final ZoomUI ui;
    private final ZoomGlass glass;

    float scale = 1f;
    /** 平移量（层坐标像素）：screen = content·scale + t */
    float tx = 0f;
    float ty = 0f;

    ZoomLayer(JComponent view) {
        this.ui = new ZoomUI(this);
        this.layer = new JLayer<>(view, ui);
        this.glass = new ZoomGlass(this);

        host = new JLayeredPane();
        host.setLayout(null);
        host.add(layer, JLayeredPane.DEFAULT_LAYER);
        host.add(glass, JLayeredPane.DRAG_LAYER);
        BY_VIEW.put(layer.getView(), this);
        host.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                layoutHost();
                clampTranslate();
                updateViewBounds();
            }
        });
    }

    JComponent view() {
        return layer.getView();
    }

    /** 是否处于缩放状态（≠100% 才需要换算脏区） */
    boolean isScaled() {
        return Math.abs(scale - 1f) > 0.001f;
    }

    /** 拆掉这一层时注销注册表，别把窗口组件留在静态 map 里 */
    void dispose() {
        BY_VIEW.remove(layer.getView());
    }

    /** 内容层与挡板都铺满窗口内容区 */
    void layoutHost() {
        int w = Math.max(1, host.getWidth());
        int h = Math.max(1, host.getHeight());
        layer.setBounds(0, 0, w, h);
        glass.setBounds(0, 0, w, h);
    }

    /** 无锚点缩放（设置里调、新窗口套默认值）：以窗口中心为准 */
    void setScale(float s) {
        zoomAt(new Point(host.getWidth() / 2, host.getHeight() / 2), s);
    }

    /**
     * 以层内坐标 {@code anchor} 为中心缩放到 {@code newScale}。
     * Ctrl+滚轮 都走这里：光标下的那个像素缩放前后不动。
     */
    void zoomAt(Point anchor, float newScale) {
        float s1 = (newScale <= 0f) ? 1f : newScale;
        float s0 = this.scale;

        if (host.getWidth() <= 0 || host.getHeight() <= 0) {
            this.scale = s1;              // 还没布局：先记比例，等 doLayout 再算平移
            return;
        }
        if (Math.abs(s1 - s0) < 1e-4f) {
            return;
        }
        double ax = (anchor == null) ? host.getWidth() / 2.0 : anchor.x;
        double ay = (anchor == null) ? host.getHeight() / 2.0 : anchor.y;

        // 光标下的内容点（缩放前）
        double cx = (ax - tx) / s0;
        double cy = (ay - ty) / s0;

        this.scale = s1;
        // 让它缩放后还落在光标下面
        this.tx = (float) (ax - cx * s1);
        this.ty = (float) (ay - cy * s1);

        clampTranslate();
        updateViewBounds();
        host.revalidate();
        host.repaint();
    }

    /** 平移夹紧：放大时不许露空边；缩小时居中 */
    void clampTranslate() {
        int w = host.getWidth();
        int h = host.getHeight();
        if (w <= 0 || h <= 0) {
            tx = 0f;
            ty = 0f;
            return;
        }
        float sw = w * scale;
        float sh = h * scale;
        if (scale >= 1f) {
            tx = Math.min(0f, Math.max(w - sw, tx));
            ty = Math.min(0f, Math.max(h - sh, ty));
        } else {
            tx = (w - sw) / 2f;
            ty = (h - sh) / 2f;
        }
    }

    /**
     * 内容按**窗口尺寸**布局（不再按 1/s 缩小）。
     * 放大时内容只是画得更大、被窗口裁掉一部分 —— 这才是"变焦"，不是"重排"。
     */
    void updateViewBounds() {
        JComponent v = layer.getView();
        if (v == null) {
            return;
        }
        int w = Math.max(1, layer.getWidth());
        int h = Math.max(1, layer.getHeight());
        if (v.getWidth() != w || v.getHeight() != h) {
            v.setBounds(0, 0, w, h);
        }
        v.doLayout();
    }

    /** 层内坐标 → 内容坐标 */
    Point toView(Point inLayer) {
        if (Math.abs(scale - 1f) < 1e-4f && tx == 0f && ty == 0f) {
            return inLayer;
        }
        return new Point(
                Math.round((inLayer.x - tx) / scale),
                Math.round((inLayer.y - ty) / scale));
    }

    /** 绘制（先平移再缩放，等价于 screen = content·s + t） */
    static final class ZoomUI extends LayerUI<JComponent> {

        private final ZoomLayer owner;

        ZoomUI(ZoomLayer owner) {
            this.owner = owner;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            float s = owner.scale;
            if (Math.abs(s - 1f) < 0.001f && owner.tx == 0f && owner.ty == 0f) {
                super.paint(g, c);
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.translate(owner.tx, owner.ty);
            g2.scale(s, s);
            super.paint(g2, c);
            g2.dispose();
        }

        @Override
        public void doLayout(JLayer<? extends JComponent> l) {
            owner.updateViewBounds();
        }

        @Override
        public void eventDispatched(AWTEvent e, JLayer<? extends JComponent> l) {
            // 正常路径交给 ZoomGlass；这里只兜底"直接派发给层"的滚轮
            if (!(e instanceof MouseWheelEvent)) {
                return;
            }
            MouseWheelEvent we = (MouseWheelEvent) e;
            if (we.getSource() != l) {
                return;
            }
            owner.glass.forward(we);
        }
    }

    /**
     * 挡板：铺满内容区、完全透明，专收鼠标事件。
     *
     * 收到事件后按 {@code (p − t) / s} 换算成内容坐标，找到真正的目标组件，
     * 用同一套 id/修饰键合成一个"本地坐标正确"的事件转发过去 ——
     * 这样放大后按键仍然点得准，也不用改动任何业务组件。
     */
    static final class ZoomGlass extends JComponent {

        private final ZoomLayer owner;

        ZoomGlass(ZoomLayer owner) {
            this.owner = owner;
            setOpaque(false);
            setFocusable(false);
            MouseAdapter h = new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { forward(e); }
                @Override public void mouseReleased(MouseEvent e) { forward(e); }
                @Override public void mouseClicked(MouseEvent e) { forward(e); }
                @Override public void mouseMoved(MouseEvent e) { forward(e); }
                @Override public void mouseDragged(MouseEvent e) { forward(e); }
                @Override public void mouseWheelMoved(MouseWheelEvent e) { forward(e); }
            };
            addMouseListener(h);
            addMouseMotionListener(h);
            addMouseWheelListener(h);
        }

        /** 把挡板上的事件换算后转发给内容里的目标组件 */
        void forward(MouseEvent e) {
            if (Math.abs(owner.scale - 1f) < 0.001f) {
                return;
            }
            Component view = owner.layer.getView();
            if (view == null) {
                return;
            }
            Point inLayer = SwingUtilities.convertPoint(this, e.getPoint(), owner.host);
            Point pv = owner.toView(inLayer);
            if (pv.x < 0 || pv.y < 0 || pv.x >= view.getWidth() || pv.y >= view.getHeight()) {
                return;                                  // 落在内容之外（缩小态的留边）
            }
            Component target = SwingUtilities.getDeepestComponentAt(view, pv.x, pv.y);
            if (target == null) {
                target = view;
            }
            Point local = SwingUtilities.convertPoint(view, pv, target);
            MouseEvent copy;
            if (e instanceof MouseWheelEvent) {
                MouseWheelEvent we = (MouseWheelEvent) e;
                copy = new MouseWheelEvent(target, we.getID(), we.getWhen(), we.getModifiersEx(),
                        local.x, local.y, we.getClickCount(), we.isPopupTrigger(),
                        we.getScrollType(), we.getScrollAmount(), we.getWheelRotation());
            } else {
                copy = new MouseEvent(target, e.getID(), e.getWhen(), e.getModifiersEx(),
                        local.x, local.y, e.getClickCount(), e.isPopupTrigger(), e.getButton());
            }
            target.dispatchEvent(copy);
        }
    }
}
