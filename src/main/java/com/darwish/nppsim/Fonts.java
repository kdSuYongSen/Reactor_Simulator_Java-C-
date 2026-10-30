package com.darwish.nppsim;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.swing.UIManager;

/**
 * 字体与「语言预加载」：保证切换语言后**不出现方框（tofu）**。
 *
 * 三个层次一起做，缺一不可：
 *  1) **选择**：每种语言挑一个能显示该语言文字的字族（韩→Malgun Gothic、简中→Microsoft YaHei、
 *     繁中→Microsoft JhengHei、日→Yu Gothic UI；拉丁/西里尔语言保持系统默认）。
 *  2) **预加载（启动时一次性准备好，而不是等画出方框再补救）**：
 *     - 把该字族交给库内绘制用的 {@code eu.hansolo.steelseries.tools.TextFonts}
 *       （库里原先写死 Verdana/Arial/Serif，这些字体没有中日韩字形 → 仪表文字会变方框）；
 *     - 预先把 UIManager 里所有 "*.font" 默认值换成能显示该语言文字的字族（之后新建的
 *       组件即使自己不设字体也不会方框）；
 *     - 主动把样本字符**光栅化一次**，逼系统把字形加载进缓存（避免首帧才加载导致的闪烁/方框）。
 *  3) **兜底修复**：挂全局 AWT 监听（窗口打开/激活）+ 容器监听（组件后加入），
 *     把仍显示不了该语言的字体就地替换（保留字号与粗细）。
 */
public final class Fonts {

    /** UIManager 中与字体相关的键（预加载时逐个检查并替换） */
    private static final String[] UI_FONT_KEYS = {
        "Label.font", "Button.font", "CheckBox.font", "RadioButton.font", "ToggleButton.font",
        "Menu.font", "MenuItem.font", "CheckBoxMenuItem.font", "RadioButtonMenuItem.font", "MenuBar.font",
        "PopupMenu.font", "TabbedPane.font", "TextField.font", "TextArea.font", "TextPane.font",
        "EditorPane.font", "FormattedTextField.font", "PasswordField.font", "ComboBox.font",
        "List.font", "Table.font", "TableHeader.font", "Tree.font", "TitledBorder.font",
        "OptionPane.font", "OptionPane.messageFont", "OptionPane.buttonFont", "ToolTip.font",
        "Spinner.font", "ProgressBar.font", "Panel.font", "RootPane.font", "InternalFrame.font",
        "InternalFrame.titleFont", "DesktopIcon.font", "ColorChooser.font", "FileChooser.font"
    };

    private static String family;   // null = 无需替换（拉丁/西里尔语言）
    private static String sample = "";
    private static Font uiFont;     // 代表字体（用于预加载光栅化）
    private static boolean installed = false;

    private Fonts() {
    }

    /** 启动时调用一次（语言切换后再次调用即可重新预加载） */
    public static void init() {
        Language lang = Localization.getLanguage();
        sample = sampleFor(lang);
        family = pickFamily(lang, sample);

        // ---- 2) 预加载 ----
        // 2a) 让库内绘制（仪表标题/刻度/LCD 文本）也使用能显示本语言文字的字族
        eu.hansolo.steelseries.tools.TextFonts.configure(family, sample);
        // 2b) UIManager 默认字体：替换掉显示不了本语言的项（新建组件即刻正确）
        if (family != null) {
            for (String key : UI_FONT_KEYS) {
                Font cur = UIManager.getFont(key);
                if (cur != null && cur.canDisplayUpTo(sample) != -1) {
                    UIManager.put(key, new Font(family, cur.getStyle(), cur.getSize()));
                }
            }
            // 2c) 预栅格化：逼系统把字形载入缓存
            uiFont = new Font(family, Font.PLAIN, 14);
            preloadGlyphs(uiFont, sample);
        } else {
            uiFont = null;
        }

        // ---- 3) 兜底修复 ----
        if (!installed) {
            installed = true;
            Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
                @Override
                public void eventDispatched(AWTEvent event) {
                    if (event instanceof WindowEvent) {
                        int id = event.getID();
                        Window w = ((WindowEvent) event).getWindow();
                        if (id == WindowEvent.WINDOW_OPENED) {
                            apply(w);
                            fitAll(w);          // 窗口尺寸已知 → 把放不下的文字缩到放得下
                            UiZoom.applyDefault(w);   // 套用默认缩放（若有）
                        } else if (id == WindowEvent.WINDOW_ACTIVATED) {
                            apply(w);           // 只做字体族修正，不做昂贵的逐组件 fit 扫描
                        }
                    }
                }
            }, AWTEvent.WINDOW_EVENT_MASK);
        }
        for (Window w : Window.getWindows()) {
            apply(w);
        }
    }

    /** 应用侧创建界面文本字体（英文界面下与原字族一致，中文/韩文等自动换成本语言字族） */
    public static Font text(String requestedFamily, int style, int size) {
        ensureInit();
        return eu.hansolo.steelseries.tools.TextFonts.get(requestedFamily, style, size);
    }

    /** 同上，不指定原字族 */
    public static Font text(int style, int size) {
        return text(null, style, size);
    }

    private static void ensureInit() {
        if (family == null && sample.isEmpty() && !installed) {
            init();
        }
    }

    /** 把样本文字画到 1×1 离屏图上，促使字形加载（预加载的关键一步） */
    private static void preloadGlyphs(Font font, String text) {
        try {
            BufferedImage scratch = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = scratch.createGraphics();
            g.setFont(font);
            g.drawString(text, 0, 0);
            g.dispose();
        } catch (Throwable ignored) {
            // 预加载失败不影响功能（后续兜底修复仍会生效）
        }
    }

    private static String sampleFor(Language lang) {
        switch (lang) {
            case KO: return "한국어 설정 경보 출력";
            case ZH_CN: return "简体中文设置报警功率";
            case ZH_TW: return "繁體中文設定警報功率";
            case JA: return "日本語設定警報出力";
            default: return "";
        }
    }

    private static String pickFamily(Language lang, String probe) {
        if (probe.isEmpty()) {
            return null;                 // 拉丁/西里尔：系统默认字体足够
        }
        String[] candidates;
        switch (lang) {
            case KO: candidates = new String[]{"Malgun Gothic", "Gulim", "Batang", "Dotum", "Arial Unicode MS"}; break;
            case ZH_CN: candidates = new String[]{"Microsoft YaHei", "SimSun", "SimHei", "Arial Unicode MS"}; break;
            case ZH_TW: candidates = new String[]{"Microsoft JhengHei", "MingLiU", "PMingLiU", "Arial Unicode MS"}; break;
            case JA: candidates = new String[]{"Yu Gothic UI", "Meiryo", "MS Gothic", "MS PGothic", "Arial Unicode MS"}; break;
            default: return null;
        }
        for (String c : candidates) {
            Font f = new Font(c, Font.PLAIN, 12);
            if (f.canDisplayUpTo(probe) == -1) {
                return c;
            }
        }
        return null;
    }

    /** 递归替换：仅当组件字体显示不了当前语言字符时才换；并给每个容器挂"新增组件"监听 */
    public static void apply(Component c) {
        if (c == null || family == null) {
            return;
        }
        Font f = c.getFont();
        if (f != null && !sample.isEmpty() && f.canDisplayUpTo(sample) != -1
                && !family.equals(f.getFamily())) {
            c.setFont(new Font(family, f.getStyle(), f.getSize()));
        }
        if (c instanceof Container) {
            Container cont = (Container) c;
            if (!hasListener(cont)) {
                cont.addContainerListener(ADD_LISTENER);
            }
            for (Component child : cont.getComponents()) {
                apply(child);
            }
        }
    }

    /**
     * 防遮挡：对文本组件，若当前字号下文字宽度超出组件宽度，则逐步缩小字号直到放得下（下限 9）。
     * 换语言后译文往往比英文长短不一，这一步保证标签/按钮/输入框的文字不会被截断。
     * 窗口尺寸已知时（WINDOW_ACTIVATED 之后）调用才有效。
     */
    public static void fitAll(java.awt.Window w) {
        if (w == null) {
            return;
        }
        for (Component c : w.getComponents()) {
            fit(c);
        }
    }

    private static void fit(Component c) {
        if (c instanceof javax.swing.JLabel || c instanceof javax.swing.AbstractButton
                || c instanceof javax.swing.JTextField) {
            String text = textOf(c);
            int width = c.getWidth();
            Font f = c.getFont();
            if (text != null && !text.isEmpty() && width > 0 && f != null) {
                java.awt.FontMetrics fm = c.getFontMetrics(f);
                int avail = width - 4;
                int size = f.getSize();
                while (size > 9 && fm.stringWidth(text) > avail) {
                    size--;
                    f = new Font(f.getFamily(), f.getStyle(), size);
                    fm = c.getFontMetrics(f);
                }
                if (size != c.getFont().getSize()) {
                    c.setFont(f);
                }
            }
        }
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                fit(child);
            }
        }
    }

    private static String textOf(Component c) {
        try {
            if (c instanceof javax.swing.JLabel) {
                return ((javax.swing.JLabel) c).getText();
            }
            if (c instanceof javax.swing.AbstractButton) {
                return ((javax.swing.AbstractButton) c).getText();
            }
            if (c instanceof javax.swing.JTextField) {
                return ((javax.swing.JTextField) c).getText();
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static final ContainerListener ADD_LISTENER = new ContainerListener() {
        @Override
        public void componentAdded(ContainerEvent e) {
            apply(e.getChild());          // 后加入的组件立即修正
        }

        @Override
        public void componentRemoved(ContainerEvent e) {
        }
    };

    private static boolean hasListener(Container c) {
        for (ContainerListener l : c.getContainerListeners()) {
            if (l == ADD_LISTENER) {
                return true;
            }
        }
        return false;
    }
}
