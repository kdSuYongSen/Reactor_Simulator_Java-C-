package com.darwish.nppsim;

import java.awt.Window;
import java.util.prefs.Preferences;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;
import org.netbeans.swing.laf.dark.DarkMetalLookAndFeel;

/**
 * UI 风格（设置 → 视频）：
 *  0 = 经典（NetBeans DarkMetal 金属深色，原版默认）
 *  1 = FlatLaf 深色（Darcula）
 *  2 = FlatLaf 浅色（Light）
 * 选择持久化；applyLaf() 在启动与每次创建游戏界面时调用（UI 构造器原本就会重设 LAF）。
 */
public final class UiStyle {

    public static final int CLASSIC = 0;
    public static final int FLAT_DARK = 1;
    public static final int FLAT_LIGHT = 2;

    private static final Preferences PREFS = Preferences.userRoot().node("com/darwish/nppsim");
    private static final String KEY = "video.uiStyle";

    private UiStyle() {
    }

    public static int get() {
        return normalize(PREFS.getInt(KEY, CLASSIC));
    }

    /** 切换风格：持久化 + 重新设置 LAF + 刷新所有已打开窗口 */
    public static void set(int style) {
        PREFS.putInt(KEY, normalize(style));
        try {
            PREFS.flush();
        } catch (Exception ignored) {
        }
        applyLaf();
        refreshOpenWindows();
    }

    /** 设置 LookAndFeel（启动 / UI 构造前调用；失败回退经典深色） */
    public static void applyLaf() {
        try {
            switch (get()) {
                case FLAT_DARK:
                    UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarculaLaf());
                    break;
                case FLAT_LIGHT:
                    UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
                    break;
                default:
                    UIManager.setLookAndFeel(new DarkMetalLookAndFeel());
                    break;
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(new DarkMetalLookAndFeel());
            } catch (Exception ignored) {
            }
        }
    }

    /** 所有已打开窗口换肤 + 重新按语言选字体 */
    public static void refreshOpenWindows() {
        for (Window w : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(w);
        }
        Fonts.init();
    }

    private static int normalize(int s) {
        return (s == FLAT_DARK || s == FLAT_LIGHT) ? s : CLASSIC;
    }
}
