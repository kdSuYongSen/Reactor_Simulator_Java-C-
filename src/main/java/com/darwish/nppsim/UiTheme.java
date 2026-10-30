package com.darwish.nppsim;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.UIManager;

/**
 * 界面主题（2026-10-26 修订：按原版 jar 恢复"窗口背板"）。
 *
 * 原版（{@code OriginalJarFile/RBMK-1500-Simulator.jar}）的配色是两层：
 *
 * <pre>
 *   窗口背板 contentPane = rgb(170,130,40)   ← UI.BACKGROUND（反编译 static{} 里 new Color(170,130,40)）
 *   分组面板 背板        = LAF 默认 rgb(51,51,55)（DarkMetal）+ 文字 rgb(222,222,222)
 *   输入框/仪表          = LAF 默认 rgb(18,30,49)
 *   分组边框             = rgb(222,222,222) 1px LineBorder
 * </pre>
 *
 * 上一版为了让"所有界面同一色调"，把 {@code Panel.background} 等 LAF 默认值整体改成了
 * 背板橄榄色 —— 窗口背板是保住了，但**分组面板的深色背板被吞掉**：原本"橄榄底 + 深灰面板"
 * 的层次变成一整片橄榄，和原版不是同一个样子。这里按原版恢复：
 *
 *   1) 只把**窗口/内容面板**刷成 {@link #BG}（原版 UI 就是这么做的：
 *      {@code setBackground(BACKGROUND)} + {@code jScrollPane1.setBackground(BACKGROUND)}）；
 *   2) **不再覆盖** LAF 的面板/视口默认色，深色背板由 LAF 自己给；
 *   3) 新写的窗口（蒸汽环路、占位页…）用 {@link #plate()} / {@link #plateText()} /
 *      {@link #LINE} 取色，和原版窗口一模一样，切换 LAF（设置→视频）也跟着走。
 */
final class UiTheme {

    /** 窗口背板：与原版 {@code UI.BACKGROUND} 完全一致 */
    static final Color BG = new Color(170, 130, 40);
    /** 原版分组边框色（LineBorder） */
    static final Color LINE = new Color(222, 222, 222);
    /** 原版告警/数值红 */
    static final Color RED = new Color(142, 0, 0);

    /** DarkMetal 的原版面板底色，LAF 取不到时用它兜底 */
    private static final Color PLATE_FALLBACK = new Color(51, 51, 55);
    private static final Color TEXT_FALLBACK = new Color(222, 222, 222);

    private static boolean installed = false;

    private UiTheme() {
    }

    static void install() {
        if (installed) {
            return;
        }
        installed = true;

        // 任何窗口一打开就把"窗口背板"设成原版橄榄色。只改内容面板/滚动框，
        // 不碰里面面板的底色 —— 那是原版的深色背板，动了就走样。
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent event) {
                if (event instanceof WindowEvent
                        && event.getID() == WindowEvent.WINDOW_OPENED) {
                    apply(((WindowEvent) event).getWindow());
                }
            }
        }, AWTEvent.WINDOW_EVENT_MASK);

        for (Window w : Window.getWindows()) {
            apply(w);
        }
    }

    /** 把窗口背板刷成原版橄榄色（只动内容面板 + 滚动框外框） */
    static void apply(Window w) {
        if (w == null) {
            return;
        }
        Container cp = null;
        if (w instanceof javax.swing.JFrame) {
            cp = ((javax.swing.JFrame) w).getContentPane();
        } else if (w instanceof javax.swing.JDialog) {
            cp = ((javax.swing.JDialog) w).getContentPane();
        }
        if (cp == null) {
            return;
        }
        /* 只补"没人设过"的窗口：原版各窗口自己都设过底色（主菜单 157,109,34、
         * 主控制室 / 凝结水 / 汽机… 都是 UI.BACKGROUND=170,130,40）。
         * 这里如果无脑覆盖，主菜单那层更深的橄榄就被刷掉了 —— 那同样是"背板走样"。 */
        if (!isLafDefault(cp.getBackground())) {
            return;
        }
        cp.setBackground(BG);
        // 原版也把最外层 JScrollPane 的底色设成 BACKGROUND（视口留给 LAF）
        if (cp instanceof JScrollPane) {
            cp.setBackground(BG);
            Component view = ((JScrollPane) cp).getViewport().getView();
            if (view instanceof JViewport) {
                view.setBackground(BG);
            }
        }
    }

    /** 是不是"没设过色"的 LAF 默认底（各 LAF 的 Panel.background / 常见灰） */
    private static boolean isLafDefault(Color c) {
        if (c == null) {
            return true;
        }
        Color laf = UIManager.getColor("Panel.background");
        if (laf != null && c.getRGB() == laf.getRGB()) {
            return true;
        }
        int rgb = c.getRGB() & 0xFFFFFF;
        return rgb == 0xEEEEEE || rgb == 0xF0F0F0 || rgb == 0xF2F2F2 || rgb == 0xFFFFFF
                || rgb == 0xE8E8E8 || rgb == 0xD9D9D9 || rgb == 0xF7F7F7 || rgb == 0xCCCCCC;
    }

    // ------------------------------------------------------------------
    // 给"新窗口"用的原版色板（运行期从当前 LAF 取，切换风格也一致）
    // ------------------------------------------------------------------

    /** 分组面板背板（原版 DarkMetal = rgb(51,51,55)） */
    static Color plate() {
        Color c = UIManager.getColor("Panel.background");
        return c != null ? c : PLATE_FALLBACK;
    }

    /** 面板上的文字色（原版 = rgb(222,222,222)） */
    static Color plateText() {
        Color c = UIManager.getColor("Label.foreground");
        return c != null ? c : TEXT_FALLBACK;
    }

    /** 输入框/LCD 底（原版 = rgb(18,30,49)） */
    static Color field() {
        Color c = UIManager.getColor("TextField.background");
        return c != null ? c : new Color(18, 30, 49);
    }

    /** 面板底色是不是深色 —— 决定读数用什么颜色才看得清 */
    static boolean isDark() {
        Color c = plate();
        int lum = (299 * c.getRed() + 587 * c.getGreen() + 114 * c.getBlue()) / 1000;
        return lum < 128;
    }

    /** 读数强调色（深底用青，浅底用深蓝） */
    static Color accent() {
        return isDark() ? new Color(120, 220, 255) : new Color(16, 60, 140);
    }

    /** 次要文字（编号、单位） */
    static Color dimText() {
        Color c = plateText();
        return new Color(
                Math.max(0, c.getRed() - 55),
                Math.max(0, c.getGreen() - 55),
                Math.max(0, c.getBlue() - 55));
    }
}
