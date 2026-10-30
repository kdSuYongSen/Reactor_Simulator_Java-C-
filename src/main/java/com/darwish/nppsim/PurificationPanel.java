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
 * 通用净化装置面板（PCS 窗口）。
 *
 * <p>2026-10-28 新增。深色背板（与其它仪表板一致）+ 运行指示灯 + 启停切换开关
 * + 流量 / 电流 / 温度三个读数。装在 <b>河 → 通用净化装置 → DW 泵 → 除盐水箱</b>
 * 这条补水链路上，默认开启。</p>
 */
public class PurificationPanel extends javax.swing.JPanel {

    private final JRadioButton start = new JRadioButton();
    private final JRadioButton stop = new JRadioButton();
    private final eu.hansolo.steelseries.extras.Led runLed = new eu.hansolo.steelseries.extras.Led();
    private final eu.hansolo.steelseries.gauges.DisplaySingle flowLcd = new eu.hansolo.steelseries.gauges.DisplaySingle();
    private final eu.hansolo.steelseries.gauges.DisplaySingle ampsLcd = new eu.hansolo.steelseries.gauges.DisplaySingle();
    private final eu.hansolo.steelseries.gauges.DisplaySingle tempLcd = new eu.hansolo.steelseries.gauges.DisplaySingle();
    /** 排水阀开启提示（红底白字），只在除盐水箱排水阀开时显示 */
    private final javax.swing.JLabel drainAlarm = new javax.swing.JLabel(Localization.t("Drain Valve Open"));

    public PurificationPanel() {
        setOpaque(true);
        setBackground(UiTheme.plate());
        javax.swing.border.TitledBorder tb = javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(120, 120, 120), 1, true),
                Localization.t("Purification Unit"),
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                Fonts.text("Dialog", Font.BOLD, 11),
                UiTheme.plateText());
        setBorder(tb);
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(380, 104));
        setMinimumSize(new Dimension(380, 104));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(1, 3, 1, 3);
        gc.anchor = GridBagConstraints.WEST;

        ButtonGroup group = new ButtonGroup();
        group.add(start);
        group.add(stop);
        start.setText(Localization.t("Start"));
        stop.setText(Localization.t("Stop"));
        for (JRadioButton b : new JRadioButton[]{start, stop}) {
            b.setOpaque(false);
            b.setForeground(UiTheme.plateText());
            b.setFont(Fonts.text("Dialog", Font.PLAIN, 10));
            b.setFocusable(false);
        }
        PurificationUnit unit = unit();
        boolean on = unit == null || unit.isRunning();
        start.setSelected(on);
        stop.setSelected(!on);
        start.addActionListener((ActionEvent e) -> {
            PurificationUnit u = unit();
            if (u != null) {
                u.setRunning(true);
            }
        });
        stop.addActionListener((ActionEvent e) -> {
            PurificationUnit u = unit();
            if (u != null) {
                u.setRunning(false);
            }
        });

        runLed.setLedColor(eu.hansolo.steelseries.tools.LedColor.GREEN_LED);
        runLed.setLedOn(on);
        runLed.setOpaque(false);
        runLed.setPreferredSize(new Dimension(18, 18));

        configLcd(flowLcd, Localization.t("kg/s"));
        configLcd(ampsLcd, "A");
        configLcd(tempLcd, "\u00b0C");

        javax.swing.JPanel switchRow = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 2, 0));
        switchRow.setOpaque(false);
        switchRow.add(start);
        switchRow.add(stop);

        javax.swing.JPanel ledRow = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 2, 0));
        ledRow.setOpaque(false);
        ledRow.add(runLed);
        javax.swing.JLabel cap = new javax.swing.JLabel(Localization.t("Running"));
        cap.setForeground(UiTheme.dimText());
        cap.setFont(Fonts.text("Dialog", Font.PLAIN, 10));
        ledRow.add(cap);

        drainAlarm.setOpaque(true);
        drainAlarm.setBackground(new Color(200, 20, 20));
        drainAlarm.setForeground(Color.WHITE);
        drainAlarm.setFont(Fonts.text("Dialog", Font.BOLD, 10));
        drainAlarm.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        drainAlarm.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(255, 120, 120)));
        drainAlarm.setPreferredSize(new Dimension(112, 20));
        drainAlarm.setVisible(false);

        gc.gridx = 0; gc.gridy = 0; add(ledRow, gc);
        gc.gridx = 1; gc.gridy = 0; add(switchRow, gc);
        gc.gridx = 0; gc.gridy = 1; add(flowLcd, gc);
        gc.gridx = 1; gc.gridy = 1; add(ampsLcd, gc);
        gc.gridx = 0; gc.gridy = 2; add(tempLcd, gc);
        gc.gridx = 1; gc.gridy = 2; add(drainAlarm, gc);
    }

    private static void configLcd(eu.hansolo.steelseries.gauges.DisplaySingle lcd, String unit) {
        lcd.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        lcd.setCustomLcdForeground(new Color(204, 204, 255));
        lcd.setLcdUnitString(unit);
        lcd.setLcdUnitStringVisible(true);
        lcd.setPreferredSize(new Dimension(120, 28));
    }

    private static PurificationUnit unit() {
        return NPPSim.pcs != null ? NPPSim.pcs.purification : null;
    }

    /** 每帧刷新（由 PCS 窗口拨号线程调用） */
    public void refresh() {
        PurificationUnit u = unit();
        if (u == null) {
            return;
        }
        flowLcd.setLcdValue(u.getFlowKgs());
        ampsLcd.setLcdValue(u.getCurrentA());
        tempLcd.setLcdValue(u.getOutletC());
        runLed.setLedOn(u.isRunning());
        boolean drainOpen = NPPSim.pcs != null && NPPSim.pcs.isDrainValveOpen();
        if (drainAlarm.isVisible() != drainOpen) {
            drainAlarm.setVisible(drainOpen);
        }
        if (u.isRunning() && !start.isSelected()) {
            start.setSelected(true);
        } else if (!u.isRunning() && !stop.isSelected()) {
            stop.setSelected(true);
        }
    }
}
