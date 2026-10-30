package com.darwish.nppsim;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseWheelEvent;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * Ctrl + 鼠标滚轮 缩放界面。开关在「设置 → 游戏」。
 *
 * 设计要点：
 *  1) **只缩放鼠标所在的那一个窗口**：每个窗口各记一份缩放系数（WeakHashMap），互不影响。
 *  2) **窗口尺寸不变**：内容按窗口尺寸布局，绘制时整体平移+等比放大（见 {@link ZoomLayer}），
 *     不 pack、不移动窗口、不重排；
 *  3) 每个组件的基础字号/首选尺寸存在 client property 里 → 反复缩放不累积误差。
 *  4) **以鼠标指向的位置为中心**：Ctrl+滚轮 时取鼠标在内容里的坐标作为锚点，
 *     放大/缩小后光标下那个点不动（像地图/相机变焦）；
 *  5) **缩放不持久化**：每次重启自动回到 100%；会话内新开的窗口沿用最近一次使用的缩放。
 */
public final class UiZoom {

    public static final float MIN = 0.6f;
    public static final float MAX = 2.0f;
    public static final float STEP = 0.1f;

    private static final String BASE_KEY = "uiZoom.baseFontSize";
    private static final String PREF_SIZE_KEY = "uiZoom.basePrefSize";
    private static final Preferences PREFS = Preferences.userRoot().node("com/darwish/nppsim");
    private static final String PREF_ENABLED = "uiZoomEnabled";

    /** 各窗口自己的缩放系数（窗口销毁后自动回收） */
    private static final Map<Window, Float> windowScales = new WeakHashMap<>();
    /** 已包装成缩放层的窗口（仅缩放比 ≠ 100% 时才存在） */
    private static final Map<Window, ZoomLayer> zoomLayers = new WeakHashMap<>();
    /** 会话内新开窗口使用的默认缩放（最近一次缩放值；重启后为 100%） */
    private static float defaultScale = 1.0f;
    private static boolean enabled = true;
    private static boolean installed = false;

    private UiZoom() {
    }

    public static void init() {
        defaultScale = 1.0f;            // 缩放不持久化：每次重启自动重置
        enabled = PREFS.getBoolean(PREF_ENABLED, true);
        if (!installed) {
            installed = true;
            /* 缩放后子组件的脏区是未缩放的组件坐标，必须换算成层坐标 —— 否则报警灯
             * /AZ 棒指示灯闪烁时只重画左上角一小块，屏幕上是残影 */
            if (!(javax.swing.RepaintManager.currentManager(null) instanceof ZoomRepaintManager)) {
                javax.swing.RepaintManager.setCurrentManager(new ZoomRepaintManager());
            }
            Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
                @Override
                public void eventDispatched(AWTEvent event) {
                    if (!(event instanceof MouseWheelEvent)) {
                        return;
                    }
                    MouseWheelEvent e = (MouseWheelEvent) event;
                    if (!e.isControlDown() || !enabled) {
                        return;
                    }
                    Window target = windowOf(e.getComponent());
                    if (target == null) {
                        return;
                    }
                    // 锚点 = 鼠标在"内容面板"里的坐标（滚轮在哪个子组件上都换算过来）
                    Point anchor = anchorOf(target, e.getComponent(), e.getPoint());
                    zoomWindow(target, e.getWheelRotation() < 0 ? STEP : -STEP, anchor);
                    e.consume();
                }
            }, AWTEvent.MOUSE_WHEEL_EVENT_MASK);
        }
    }

    /** 鼠标位置换算到窗口内容面板坐标（缩放层的锚点） */
    private static Point anchorOf(Window w, Component src, Point p) {
        if (!(w instanceof javax.swing.JFrame) || src == null) {
            return null;
        }
        java.awt.Container cp = ((javax.swing.JFrame) w).getContentPane();
        if (cp == null) {
            return null;
        }
        try {
            return SwingUtilities.convertPoint(src, p, cp);
        } catch (Exception ex) {
            return null;
        }
    }

    private static Window windowOf(Component c) {
        if (c == null) {
            return null;
        }
        if (c instanceof Window) {
            return (Window) c;
        }
        return SwingUtilities.getWindowAncestor(c);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean on) {
        enabled = on;
        PREFS.putBoolean(PREF_ENABLED, on);
        flush();
    }

    /** 某窗口当前的缩放系数 */
    public static float getScale(Window w) {
        if (w == null) {
            return defaultScale;
        }
        Float s = windowScales.get(w);
        return (s == null) ? defaultScale : s;
    }

    /** 默认缩放（设置页显示与重置用） */
    public static float getScale() {
        return defaultScale;
    }

    /** 只调整传入的这一个窗口（以窗口中心为锚点） */
    public static void zoomWindow(Window w, float delta) {
        zoomWindow(w, delta, null);
    }

    /**
     * 以 {@code anchor}（内容面板坐标）为中心缩放该窗口。
     * anchor 为 null 时退回窗口中心。
     */
    public static void zoomWindow(Window w, float delta, Point anchor) {
        if (w == null) {
            return;
        }
        setWindowScale(w, getScale(w) + delta, anchor);
    }

    public static void setWindowScale(Window w, float s) {
        setWindowScale(w, s, null);
    }

    public static void setWindowScale(Window w, float s, Point anchor) {
        float v = clamp(s);
        if (w == null) {
            return;
        }
        windowScales.put(w, v);
        defaultScale = v;               // 会话内新开的窗口沿用最近一次缩放（重启后回到 100%）
        applyWindow(w, v, anchor);
    }

    /** 新窗口首次显示时套用默认缩放（默认 100% → 空操作） */
    public static void applyDefault(Window w) {
        if (w == null || defaultScale == 1.0f) {
            return;
        }
        windowScales.put(w, defaultScale);
        applyWindow(w, defaultScale);
    }

    /** 设置页的"重置缩放"：所有窗口回到 100%（不持久化） */
    public static void reset() {
        defaultScale = 1.0f;
        for (Window w : Window.getWindows()) {
            windowScales.put(w, 1.0f);
            applyWindow(w, 1.0f);
        }
    }

    // ------------------------------------------------------------------
    /**
     * 应用缩放（2026-10-26 重做）。
     *
     * 不再改字号/首选尺寸（那是"重排"，会把按键挤变形、把窗口撑大），
     * 改成把内容包进 {@link ZoomLayer}：内容按 1/s 布局、绘制时整体放大。
     * 窗口尺寸保持不变；100% 时不包装，正常游玩零开销。
     */
    private static void applyWindow(Window w, float scale) {
        applyWindow(w, scale, null);
    }

    private static void applyWindow(Window w, float scale, Point anchor) {
        if (w == null) {
            return;
        }
        if (!(w instanceof javax.swing.JFrame)) {
            // 对话框没有可替换的内容面板层级，退回字号缩放（影响面小）
            applyFonts(w, scale);
            w.invalidate();
            w.validate();
            w.repaint();
            return;
        }
        javax.swing.JFrame f = (javax.swing.JFrame) w;
        java.awt.Container content = f.getContentPane();
        if (!(content instanceof javax.swing.JComponent)) {
            return;
        }
        ZoomLayer layer = zoomLayers.get(f);
        boolean unity = Math.abs(scale - 1f) < 0.001f;

        if (layer == null) {
            if (unity) {
                return;                       // 100%：不包装
            }
            layer = new ZoomLayer((javax.swing.JComponent) content);
            zoomLayers.put(f, layer);
            f.setContentPane(layer.host);
            f.validate();
            layer.layoutHost();          // 内容层 + 挡板都铺满
            // 首次包装：内容面板换成同尺寸的容器，锚点坐标仍然有效
            layer.zoomAt(anchor, scale);
        } else if (unity) {
            // 回到 100%：拆掉包装，恢复原始内容面板
            javax.swing.JComponent view = layer.view();
            f.setContentPane(view);
            layer.dispose();              // 注销"视图 → 层"映射
            zoomLayers.remove(f);
            layer = null;                 // 已拆层，不要再调 zoomAt
        } else {
            layer.zoomAt(anchor, scale);
        }
        f.invalidate();
        f.validate();
        f.repaint();
    }

    /** 递归按 scale 调整字号（保留基础字号，反复缩放不累积误差） */
    private static void applyFonts(Component c, float scale) {
        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            // ⚠ 必须在改字号/改尺寸**之前**记录基准值，否则基准会取到"已放大过"的值
            if (jc.getClientProperty(BASE_KEY) == null && c.getFont() != null) {
                jc.putClientProperty(BASE_KEY, (float) c.getFont().getSize());
            }
            if (jc.getClientProperty(PREF_SIZE_KEY) == null && jc.getPreferredSize() != null) {
                java.awt.Dimension p = jc.getPreferredSize();
                jc.putClientProperty(PREF_SIZE_KEY, new java.awt.Dimension(p.width, p.height));
            }

            Font f = c.getFont();
            if (f != null) {
                Float base = (Float) jc.getClientProperty(BASE_KEY);
                int newSize = Math.max(8, Math.round(base * scale));
                if (newSize != f.getSize()) {
                    c.setFont(new Font(f.getFamily(), f.getStyle(), newSize));
                }
            }
            // 组件首选尺寸也等比缩放：仪表/按钮等固定尺寸控件才会随缩放变大
            // （缩回 100% 时同样按基准尺寸还原，否则尺寸只增不减）
            java.awt.Dimension pref = jc.getPreferredSize();
            java.awt.Dimension baseSize = (java.awt.Dimension) jc.getClientProperty(PREF_SIZE_KEY);
            if (pref != null && baseSize != null) {
                int nw = Math.max(1, Math.round(baseSize.width * scale));
                int nh = Math.max(1, Math.round(baseSize.height * scale));
                if (nw != pref.width || nh != pref.height) {
                    jc.setPreferredSize(new java.awt.Dimension(nw, nh));
                }
            }
        }
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                applyFonts(child, scale);
            }
        }
    }

    public static float clamp(float s) {
        return Math.max(MIN, Math.min(MAX, Math.round(s * 10f) / 10f));
    }

    private static void flush() {
        try {
            PREFS.flush();
        } catch (Exception ignored) {
        }
    }
}
