package com.darwish.nppsim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * 占位窗口。
 *
 * 这八个系统还没做逻辑，但菜单里要能看到、能开窗，所以统一用一页
 * "未完待续" 的占位窗口，等做到哪个再替换成真正的面板。
 *
 * 2026-10-26 修订（"参考原 jar 恢复窗口背板，新窗口配色要跟原版一致"）：
 *   1) 底色不再用亮黄，改成**原版两层配色**：
 *        窗口背板 = {@link UiTheme#BG}（橄榄 rgb(170,130,40)，= 原版 UI.BACKGROUND）
 *        中间面板 = {@link UiTheme#plate()}（LAF 深色 rgb(51,51,55)）+ 1px {@link UiTheme#LINE} 边框
 *        文字     = {@link UiTheme#plateText()}（rgb(222,222,222)）
 *      和原版窗口里"橄榄底 + 深灰分组框"的层次完全一样；
 *   2) 初始化按其它窗口（原版窗口）的流程走：
 *      标题 → 背板 → 面板 → WindowMenu → PageLayout → 窗口模式 → 尺寸/居中钳位；
 *   3) 文字用 {@link Fonts#text} 取字族，并做一次"能否显示"兜底，避免个别语言出现方框。
 */
class PlaceholderUI extends JFrame implements UIUpdateable {

    /** 中间面板的固定尺寸（和原版分组框一样大小，不随窗口拉伸） */
    private static final Dimension PLATE_SIZE = new Dimension(560, 240);

    private final String pageId;

    PlaceholderUI(String pageId, String titleKey) {
        this.pageId = pageId;

        setTitle(Localization.t(titleKey));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ---- 窗口背板：与原版一致（橄榄色）----
        getContentPane().setBackground(UiTheme.BG);
        setBackground(UiTheme.BG);
        getContentPane().setLayout(new GridBagLayout());   // 居中放一块面板

        // ---- 面板：原版深色背板 + 浅色分组边框 ----
        JPanel plate = new JPanel(new GridBagLayout());
        plate.setBackground(UiTheme.plate());
        plate.setOpaque(true);
        plate.setBorder(BorderFactory.createLineBorder(UiTheme.LINE, 1, true));
        plate.setPreferredSize(PLATE_SIZE);
        plate.setMinimumSize(PLATE_SIZE);

        String text = Localization.t("To be continued");
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(UiTheme.plateText());
        label.setFont(textFont(text));
        label.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 16, 8, 16);
        plate.add(label, gc);

        GridBagConstraints outer = new GridBagConstraints();
        outer.anchor = GridBagConstraints.CENTER;
        getContentPane().add(plate, outer);

        // ---- 与其它窗口一致的初始化顺序 ----
        // 每步都单独兜底：任何一步失败都不能让窗口开不出来
        try {
            WindowMenu.install(this);               // 顶部 Window 菜单（统一清单）
        } catch (Throwable t) {
            t.printStackTrace();
        }
        try {
            PageLayout.installAuto(this, pageId);   // 允许布局编辑器摆放本页
        } catch (Throwable t) {
            t.printStackTrace();
        }
        setSize(1366, 768);
        setMinimumSize(new java.awt.Dimension(800, 600));
        setResizable(true);
        try {
            SettingsDialog.applyWindowMode(this);   // 跟随"窗口化/最大化"设置
            WindowLayout.clampToScreen(this);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        setLocationRelativeTo(null);
    }

    /** 取一个真的能显示出这句话的字体（防方框） */
    private static Font textFont(String text) {
        Font f = Fonts.text("Dialog", Font.BOLD, 36);
        if (f == null) {
            f = new Font(Font.DIALOG, Font.BOLD, 36);
        }
        if (!text.isEmpty() && f.canDisplayUpTo(text) != -1) {
            // 该字族缺字：退回逻辑字体（Java 会自动挑能显示的字形）
            f = new Font(Font.DIALOG, Font.BOLD, 36);
        }
        return f;
    }

    /** 页面 id（布局编辑器用的名字） */
    String getPageId() {
        return pageId;
    }

    @Override public void update() { }
    @Override public void initializeDialUpdateThread() { }
    @Override public void setVisibility(boolean visible) { setVisible(visible); }
    @Override public void discard() { dispose(); }
    @Override public void acknowledge() { }
}

/** 应急柴油发电机 */
class EmergencyDieselUI extends PlaceholderUI {
    EmergencyDieselUI() { super("EmergencyDieselUI", "Emergency Diesel Generators"); }
}

/** 电网系统 */
class GridSystemUI extends PlaceholderUI {
    GridSystemUI() { super("GridSystemUI", "Grid System"); }
}

/** ECCS 应急堆芯冷却 */
class ECCSUI extends PlaceholderUI {
    ECCSUI() { super("ECCSUI", "ECCS"); }
}

/** 离线冷却 */
class OfflineCoolingUI extends PlaceholderUI {
    OfflineCoolingUI() { super("OfflineCoolingUI", "Offline Cooling"); }
}

/** 通风系统 */
class VentilationUI extends PlaceholderUI {
    VentilationUI() { super("VentilationUI", "Ventilation System"); }
}

/** 照明系统 */
class LightingUI extends PlaceholderUI {
    LightingUI() { super("LightingUI", "Lighting System"); }
}

/** 热阱 */
class HeatSinkUI extends PlaceholderUI {
    HeatSinkUI() { super("HeatSinkUI", "Heat Sink"); }
}

/** 辐射监测 */
class RadiationMonitoringUI extends PlaceholderUI {
    RadiationMonitoringUI() { super("RadiationMonitoringUI", "Radiation Monitoring"); }
}
