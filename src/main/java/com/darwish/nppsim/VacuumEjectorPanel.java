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
 * 真空抽气泵控制面板（按用户合体图）：Start/Stop + 转速 + Power(A) + 运行灯 + 抽气流量。
 * 面板背景跟随所在页面（深色），文字/旋钮透明融入。
 */
public class VacuumEjectorPanel extends javax.swing.JPanel {

    private final VacuumPump pump;
    private final JRadioButton startButton = new JRadioButton();
    private final JRadioButton stopButton = new JRadioButton();
    private final eu.hansolo.steelseries.gauges.DisplaySingle rpmLcd = new eu.hansolo.steelseries.gauges.DisplaySingle();
    private final eu.hansolo.steelseries.gauges.DisplaySingle ampsLcd = new eu.hansolo.steelseries.gauges.DisplaySingle();
    private final eu.hansolo.steelseries.gauges.DisplaySingle flowLcd = new eu.hansolo.steelseries.gauges.DisplaySingle();
    private final eu.hansolo.steelseries.extras.Led runningLed = new eu.hansolo.steelseries.extras.Led();
    private final javax.swing.JLabel powerLabel = new javax.swing.JLabel();

    public VacuumEjectorPanel(String title, VacuumPump pump) {
        this.pump = pump;
        setBorder(javax.swing.BorderFactory.createTitledBorder(title));
        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(2, 6, 2, 6);
        gc.anchor = GridBagConstraints.WEST;

        ButtonGroup group = new ButtonGroup();
        group.add(startButton);
        group.add(stopButton);
        org.openide.awt.Mnemonics.setLocalizedText(startButton, Localization.t("Start"));
        org.openide.awt.Mnemonics.setLocalizedText(stopButton, Localization.t("Stop"));
        // 初始按钮状态跟着泵的实际状态走（标准 IC 下真空系统已投运 → 显示"开始"）
        (pump.isRunning() ? startButton : stopButton).setSelected(true);
        for (JRadioButton b : new JRadioButton[]{startButton, stopButton}) {
            b.setOpaque(false);
            b.setFont(Fonts.text("Dialog", Font.PLAIN, 11));
        }
        startButton.addActionListener((ActionEvent e) -> pump.setRunning(true));
        stopButton.addActionListener((ActionEvent e) -> pump.setRunning(false));

        runningLed.setLedColor(eu.hansolo.steelseries.tools.LedColor.RED_LED);
        runningLed.setLedOn(false);
        runningLed.setOpaque(false);
        runningLed.setPreferredSize(new Dimension(24, 24));

        configLcd(rpmLcd, "rpm");
        configLcd(ampsLcd, "A");
        configLcd(flowLcd, Localization.t("kg/s"));

        org.openide.awt.Mnemonics.setLocalizedText(powerLabel, Localization.t("Power"));
        powerLabel.setFont(Fonts.text("Dialog", Font.BOLD, 11));
        powerLabel.setOpaque(false);

        gc.gridx = 0; gc.gridy = 0; add(startButton, gc);
        gc.gridx = 1; gc.gridy = 0; add(rpmLcd, gc);
        gc.gridx = 0; gc.gridy = 1; add(stopButton, gc);
        gc.gridx = 1; gc.gridy = 1; add(ampsLcd, gc);
        gc.gridx = 0; gc.gridy = 2; add(powerLabel, gc);
        gc.gridx = 1; gc.gridy = 2; add(flowLcd, gc);
        gc.gridx = 0; gc.gridy = 3; add(runningLed, gc);
    }

    private static void configLcd(eu.hansolo.steelseries.gauges.DisplaySingle lcd, String unit) {
        lcd.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        lcd.setCustomLcdForeground(new Color(204, 204, 255));
        lcd.setLcdUnitString(unit);
        lcd.setLcdUnitStringVisible(true);
        lcd.setPreferredSize(new Dimension(130, 30));
    }

    /** 拨号线程（20Hz）每帧刷新 */
    public void refresh() {
        rpmLcd.setLcdValue(pump.getRpm());
        ampsLcd.setLcdValue(pump.getPowerUsage());
        flowLcd.setLcdValue(pump.getFlowRate());
        runningLed.setLedOn(pump.isRunning() && pump.getRpm() > VacuumPump.RATED_RPM * 0.05f);
        // 外部改了 running（读档、自动控制）时按钮跟着变
        javax.swing.JRadioButton want = pump.isRunning() ? startButton : stopButton;
        if (!want.isSelected()) {
            want.setSelected(true);
        }
    }
}
