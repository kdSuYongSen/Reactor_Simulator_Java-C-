package com.darwish.nppsim;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;

/**
 * 面板内容缩放器 —— 配合外部布局编辑器（ui_layout.json）的"放大"功能。
 *
 * 当布局编辑器把某个面板方块放大/缩小时，不仅面板本身尺寸变化，其内部：
 *   · 所有显式设置的字体（按钮文字、标签、表格文字）按同一系数缩放；
 *   · 所有显式设置的最小/首选/最大尺寸按同一系数缩放（GridLayout 等自动拉伸的布局不受影响，
 *     但 BoxLayout / GroupLayout-用-DEFAULT_SIZE / 绝对定位的子组件会随之变大）；
 *   · SteelSeries 仪表（MWt/MWe 等）的 LCD 数值字体与单位字体通过其自身 API 缩放
 *     （这类仪表不读取组件字体，必须走专用接口）；当方块本身就是仪表或按键
 *     （MWt/MWe/ACK 等，没有子组件）时，字体直接作用在方块自身上。
 *
 * 用法：首次 apply 时对面板全部后代组件做原始状态快照；之后每次 apply 都从快照重新计算，
 * 保证多次调整不会反复叠加；reset 恢复全部原始值（切回 GroupLayout 时调用）。
 *
 * 缩放系数 s = min(新宽/设计宽, 新高/设计高)，钳制在 [0.75, 2.5]（防止字体过小/过大）；
 * 若布局编辑器设置了"内容缩放"倍率（content_scale），则在尺寸系数基础上再乘该倍率，
 * 合成结果钳制在 [0.4, 3.0] —— 这样不改变方块大小时也能单独放大/缩小面板内的按键和文字。
 * 全部操作在 EDT 上进行（由 UI 的布局同步逻辑调用）。
 */
final class PanelContentScaler {

    private static final double MIN_SCALE = 0.75;
    private static final double MAX_SCALE = 2.5;
    /** 合成（尺寸×内容滑块）后的最终钳制范围 */
    private static final double MIN_COMBINED = 0.4;
    private static final double MAX_COMBINED = 3.0;

    /** 单个组件的原始状态快照 */
    private static final class Orig {
        boolean fontSet;
        Font font;
        Dimension min;
        Dimension pref;
        Dimension max;
        Font lcdValueFont;   // SteelSeries 仪表专用（其余组件为 null）
        Font lcdUnitFont;
    }

    private final Map<JComponent, Orig> originals = new HashMap<>();
    /** 每个面板最近一次应用的缩放系数（相同则跳过，避免无谓重排） */
    private final Map<JComponent, Double> applied = new HashMap<>();

    /** 按目标尺寸与设计尺寸计算缩放系数（含钳制） */
    static double scaleFor(double designW, double designH, double nowW, double nowH) {
        if (designW <= 0 || designH <= 0) {
            return 1.0;
        }
        double s = Math.min(nowW / designW, nowH / designH);
        return Math.max(MIN_SCALE, Math.min(MAX_SCALE, s));
    }

    /** 将"尺寸自动缩放"与编辑器"内容缩放"倍率（content_scale）相乘合成（含整体钳制） */
    static double combine(double sizeScale, double contentScale) {
        double u = (contentScale > 0 && !Double.isNaN(contentScale)) ? contentScale : 1.0;
        double s = sizeScale * u;
        return Math.max(MIN_COMBINED, Math.min(MAX_COMBINED, s));
    }

    /** 对面板的内容应用缩放因子 s（只作用于面板的后代，面板自身尺寸由布局文件决定） */
    void apply(JComponent panel, double s) {
        Double prev = applied.get(panel);
        if (prev != null && Math.abs(prev - s) < 1e-4) {
            return;
        }
        applied.put(panel, s);
        // 方块组件本身就是"内容"的情况（MWt/MWe 方块=仪表、ACK 方块=按键）：
        // 它们没有需要缩放的子组件，字体必须作用在方块自身
        Orig self = originals.computeIfAbsent(panel, k -> snapshot(k));
        if (self.lcdValueFont != null) {
            applyGaugeFonts(panel, self, s);         // 仪表方块：LCD 数值/单位字体
        } else if (self.fontSet && panel.getComponentCount() == 0) {
            Font f = self.font.deriveFont((float) (self.font.getSize2D() * s));
            if (!f.equals(panel.getFont())) {
                panel.setFont(f);                    // 叶子方块：自身字体（如 ACK 按键文字）
            }
        }
        applyTo(panel, s);
    }

    /** 恢复所有已记录组件的原始字体/尺寸 */
    void resetAll() {
        for (Map.Entry<JComponent, Orig> e : originals.entrySet()) {
            JComponent c = e.getKey();
            Orig o = e.getValue();
            if (o.fontSet) {
                c.setFont(o.font);
            }
            if (o.min != null) {
                c.setMinimumSize(o.min);
            }
            if (o.pref != null) {
                c.setPreferredSize(o.pref);
            }
            if (o.max != null) {
                c.setMaximumSize(o.max);
            }
            applyGaugeFonts(c, o, 1.0);
        }
        applied.clear();
    }

    private void applyTo(JComponent parent, double s) {
        for (Component child : parent.getComponents()) {
            if (!(child instanceof JComponent)) {
                continue;
            }
            JComponent c = (JComponent) child;
            Orig o = originals.computeIfAbsent(c, k -> snapshot(k));
            if (o.fontSet) {
                Font f = o.font.deriveFont((float) (o.font.getSize2D() * s));
                if (!f.equals(c.getFont())) {
                    c.setFont(f);
                }
            }
            if (o.min != null) {
                setIfDifferent(c, "min", o.min, s);
            }
            if (o.pref != null) {
                setIfDifferent(c, "pref", o.pref, s);
            }
            if (o.max != null) {
                setIfDifferent(c, "max", o.max, s);
            }
            if (o.lcdValueFont != null) {
                applyGaugeFonts(c, o, s);
            }
            applyTo(c, s);
        }
    }

    private static void setIfDifferent(JComponent c, String kind, Dimension orig, double s) {
        Dimension target = new Dimension(
                Math.max(1, (int) Math.round(orig.width * s)),
                Math.max(1, (int) Math.round(orig.height * s)));
        switch (kind) {
            case "min":
                if (!target.equals(c.getMinimumSize())) {
                    c.setMinimumSize(target);
                }
                break;
            case "pref":
                if (!target.equals(c.getPreferredSize())) {
                    c.setPreferredSize(target);
                }
                break;
            default:
                if (!target.equals(c.getMaximumSize())) {
                    c.setMaximumSize(target);
                }
                break;
        }
    }

    private static void applyGaugeFonts(JComponent c, Orig o, double s) {
        if (!(c instanceof eu.hansolo.steelseries.gauges.AbstractLinear)) {
            return;
        }
        eu.hansolo.steelseries.gauges.AbstractLinear g = (eu.hansolo.steelseries.gauges.AbstractLinear) c;
        if (o.lcdValueFont != null) {
            Font f = o.lcdValueFont.deriveFont((float) (o.lcdValueFont.getSize2D() * s));
            if (!f.equals(g.getLcdValueFont())) {
                g.setLcdValueFont(f);
            }
        }
        if (o.lcdUnitFont != null) {
            Font f = o.lcdUnitFont.deriveFont((float) (o.lcdUnitFont.getSize2D() * s));
            if (!f.equals(g.getLcdUnitFont())) {
                g.setLcdUnitFont(f);
            }
        }
    }

    private static Orig snapshot(JComponent c) {
        Orig o = new Orig();
        o.fontSet = c.isFontSet();
        o.font = c.getFont();
        if (c.isMinimumSizeSet()) {
            o.min = c.getMinimumSize();
        }
        if (c.isPreferredSizeSet()) {
            o.pref = c.getPreferredSize();
        }
        if (c.isMaximumSizeSet()) {
            o.max = c.getMaximumSize();
        }
        if (c instanceof eu.hansolo.steelseries.gauges.AbstractLinear) {
            eu.hansolo.steelseries.gauges.AbstractLinear g = (eu.hansolo.steelseries.gauges.AbstractLinear) c;
            o.lcdValueFont = g.getLcdValueFont();
            o.lcdUnitFont = g.getLcdUnitFont();
        }
        return o;
    }
}
