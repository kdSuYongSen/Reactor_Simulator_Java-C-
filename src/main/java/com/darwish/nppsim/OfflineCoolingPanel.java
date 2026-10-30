package com.darwish.nppsim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

/**
 * 核心离线冷却控制面板（反应堆主窗口，自动控制区下方）。
 * 样式参照凝结水页面的真空抽气泵面板：Start/Stop + LCD 读数 + 运行灯，并加入温度显示。
 * 控制：离线泵 A/B（Start/Stop）+ 环路阀（Open/Close）；
 * 读数：总流量（kg/s）、泵电流（A，两台合计）、堆芯排放水温（°C）。
 */
public class OfflineCoolingPanel extends javax.swing.JPanel {

    private final JRadioButton startA = new JRadioButton();
    private final JRadioButton stopA = new JRadioButton();
    private final JRadioButton startB = new JRadioButton();
    private final JRadioButton stopB = new JRadioButton();
    private final JRadioButton openV = new JRadioButton();
    private final JRadioButton closeV = new JRadioButton();
    private final eu.hansolo.steelseries.gauges.DisplaySingle flowLcd = new eu.hansolo.steelseries.gauges.DisplaySingle();
    /** 电流表（A，两台泵合计）——原来这里是"除盐水箱水位 (t)"，与离线环路无关 */
    private final eu.hansolo.steelseries.gauges.DisplaySingle ampsLcd = new eu.hansolo.steelseries.gauges.DisplaySingle();
    private final eu.hansolo.steelseries.gauges.DisplaySingle tempLcd = new eu.hansolo.steelseries.gauges.DisplaySingle();
    private final eu.hansolo.steelseries.extras.Led runningLed = new eu.hansolo.steelseries.extras.Led();

    public OfflineCoolingPanel() {
        setBorder(javax.swing.BorderFactory.createTitledBorder(Localization.t("Core Offline Cooling")));
        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 2, 0, 2);
        gc.anchor = GridBagConstraints.WEST;

        ButtonGroup groupA = new ButtonGroup();
        groupA.add(startA);
        groupA.add(stopA);
        stopA.setSelected(true);
        ButtonGroup groupB = new ButtonGroup();
        groupB.add(startB);
        groupB.add(stopB);
        stopB.setSelected(true);
        ButtonGroup groupV = new ButtonGroup();
        groupV.add(openV);
        groupV.add(closeV);
        closeV.setSelected(true);   // 系统初值：阀关、泵停（与 OfflineCoolingSystem 一致）

        OfflineCoolingSystem sys = sys();
        if (sys != null) {
            if (sys.isPumpARunning()) startA.setSelected(true);
            if (sys.isPumpBRunning()) startB.setSelected(true);
            if (sys.isValveOpen()) openV.setSelected(true);
        }
        startA.addActionListener((ActionEvent e) -> { OfflineCoolingSystem s = sys(); if (s != null) s.setPumpARunning(true); });
        stopA.addActionListener((ActionEvent e) -> { OfflineCoolingSystem s = sys(); if (s != null) s.setPumpARunning(false); });
        startB.addActionListener((ActionEvent e) -> { OfflineCoolingSystem s = sys(); if (s != null) s.setPumpBRunning(true); });
        stopB.addActionListener((ActionEvent e) -> { OfflineCoolingSystem s = sys(); if (s != null) s.setPumpBRunning(false); });
        openV.addActionListener((ActionEvent e) -> { OfflineCoolingSystem s = sys(); if (s != null) s.setValveOpen(true); });
        closeV.addActionListener((ActionEvent e) -> { OfflineCoolingSystem s = sys(); if (s != null) s.setValveOpen(false); });

        for (JRadioButton b : new JRadioButton[]{startA, stopA, startB, stopB, openV, closeV}) {
            b.setOpaque(false);
            b.setFont(Fonts.text("Dialog", Font.PLAIN, 11));
        }

        runningLed.setLedColor(eu.hansolo.steelseries.tools.LedColor.RED_LED);
        runningLed.setLedOn(false);
        runningLed.setOpaque(false);
        runningLed.setPreferredSize(new Dimension(24, 24));

        configLcd(flowLcd, Localization.t("kg/s"));
        configLcd(ampsLcd, "A");
        configLcd(tempLcd, "\u00b0C");

        // 三行控制（泵 A / 泵 B / 阀门）+ 对应 LCD，末行运行灯
        gc.gridx = 0; gc.gridy = 0; add(row(Localization.t("Pump A"), startA, stopA), gc);
        gc.gridx = 1; gc.gridy = 0; add(flowLcd, gc);
        gc.gridx = 0; gc.gridy = 1; add(row(Localization.t("Pump B"), startB, stopB), gc);
        gc.gridx = 1; gc.gridy = 1; add(ampsLcd, gc);
        gc.gridx = 0; gc.gridy = 2; add(row(Localization.t("Valve"), openV, closeV), gc);
        gc.gridx = 1; gc.gridy = 2; add(tempLcd, gc);
        gc.gridx = 0; gc.gridy = 3; add(runningLed, gc);
    }

    /** 横排小盒：标签 + 两个互斥按钮 */
    private javax.swing.JPanel row(String label, JRadioButton b1, JRadioButton b2) {
        javax.swing.JPanel p = new javax.swing.JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 0, 4);
        c.anchor = GridBagConstraints.WEST;
        javax.swing.JLabel l = new javax.swing.JLabel(label);
        l.setFont(Fonts.text("Dialog", Font.BOLD, 11));
        l.setOpaque(false);
        c.gridx = 0; p.add(l, c);
        c.gridx = 1; c.insets = new Insets(0, 0, 0, 0); p.add(b1, c);
        c.gridx = 2; p.add(b2, c);
        return p;
    }

    private static void configLcd(eu.hansolo.steelseries.gauges.DisplaySingle lcd, String unit) {
        lcd.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        lcd.setCustomLcdForeground(new Color(204, 204, 255));
        lcd.setLcdUnitString(unit);
        lcd.setLcdUnitStringVisible(true);
        lcd.setPreferredSize(new Dimension(130, 28));
    }

    /** 每帧取当前系统实例（旧档反序列化后由 Core.update() 补建，这里动态获取保证兼容） */
    private static OfflineCoolingSystem sys() {
        return NPPSim.core != null ? NPPSim.core.offlineCooling : null;
    }

    /** 拨号线程（20Hz）每帧刷新 */
    public void refresh() {
        OfflineCoolingSystem s = sys();
        if (s == null) {
            return;
        }
        flowLcd.setLcdValue(s.getTotalFlowKgs());
        ampsLcd.setLcdValue(s.getCurrentA());
        tempLcd.setLcdValue(s.getOutletTempC());
        runningLed.setLedOn(s.isCoolingActive());
    }
}
