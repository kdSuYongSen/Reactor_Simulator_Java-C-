package com.darwish.nppsim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * 排水阀控制面板（仿给水系统的阀门面板）。
 * 供「除盐水箱排水阀」与「凝汽器排水阀」复用：
 *   - 开 / 停止 / 关 三个旋钮（对应 WaterValve.state：2 / 1 / 0；停在"停止"位时由自动控制接管）
 *   - 阀位表（0~100%）
 *   - 排水流量显示（kg/s）
 *   - 可选的高水位报警灯（与报警器联动，会闪烁并发声）
 */
public class DrainValvePanel extends JPanel {

    private final WaterValve valve;
    private final JRadioButton openButton = new JRadioButton();
    private final JRadioButton stopButton = new JRadioButton();
    private final JRadioButton closeButton = new JRadioButton();
    private final eu.hansolo.steelseries.gauges.Radial positionGauge =
            new eu.hansolo.steelseries.gauges.Radial();
    private final eu.hansolo.steelseries.gauges.DisplaySingle flowDisplay =
            new eu.hansolo.steelseries.gauges.DisplaySingle();
    private JTextField alarmField;
    private final JLabel stateLabel = new JLabel();
    /** 阀位表缓动显示值 */
    private float dispPosition;
    private final javax.swing.JCheckBox autoBox = new javax.swing.JCheckBox();

    /**
     * @param title     面板标题（**已本地化**的文本，由调用方用 Localization.t(...) 取得）
     * @param valve     被控阀门
     * @param alarmText 报警灯文字（已本地化；null = 不要报警灯）
     */
    public DrainValvePanel(String title, WaterValve valve, String alarmText) {
        this.valve = valve;
        setBackground(UI.BACKGROUND);
        setBorder(BorderFactory.createTitledBorder(title));

        // ---- 位置表 ----
        positionGauge.setBackgroundColor(eu.hansolo.steelseries.tools.BackgroundColor.LIGHT_GRAY);
        positionGauge.setFrameDesign(eu.hansolo.steelseries.tools.FrameDesign.TILTED_BLACK);
        positionGauge.setFrameVisible(false);
        positionGauge.setLcdVisible(false);
        positionGauge.setLedVisible(false);
        positionGauge.setPointerColor(eu.hansolo.steelseries.tools.ColorDef.BLACK);
        positionGauge.setPointerShadowVisible(false);
        positionGauge.setTitle(Localization.t("Position"));
        positionGauge.setTitleAndUnitFont(Fonts.text("Verdana", Font.PLAIN, 8));
        positionGauge.setTitleAndUnitFontEnabled(true);
        positionGauge.setUnitString("%");
        positionGauge.setMaxValue(100);
        positionGauge.setMinValue(0);
        positionGauge.setPreferredSize(new Dimension(96, 96));

        // ---- 流量显示 ----
        flowDisplay.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        flowDisplay.setCustomLcdForeground(new Color(204, 204, 255));
        flowDisplay.setLcdDecimals(2);
        flowDisplay.setLcdUnitString(Localization.t("kg/s"));
        flowDisplay.setPreferredSize(new Dimension(130, 55));

        // ---- 旋钮 ----
        openButton.setText(Localization.t("Open"));
        stopButton.setText(Localization.t("Stop"));
        closeButton.setText(Localization.t("Close"));
        for (JRadioButton b : new JRadioButton[] {openButton, stopButton, closeButton}) {
            b.setBackground(UI.BACKGROUND);
            b.setFont(Fonts.text("Dialog", Font.PLAIN, 11));
        }
        ButtonGroup group = new ButtonGroup();
        group.add(openButton);
        group.add(stopButton);
        group.add(closeButton);
        stopButton.setSelected(true);           // 默认"停止"= 交给自动控制

        openButton.addActionListener((ActionEvent e) -> valve.setState(2));
        stopButton.addActionListener((ActionEvent e) -> valve.setState(1));
        closeButton.addActionListener((ActionEvent e) -> valve.setState(0));

        // ---- 布局 ----
        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(2, 4, 2, 4);
        gc.anchor = GridBagConstraints.WEST;

        // 报警灯放第一行：面板被窗口/滚动区裁切时也能先看到"排水阀开启"
        if (alarmText != null) {
            alarmField = new JTextField(alarmText);
            alarmField.setEditable(false);
            alarmField.setHorizontalAlignment(SwingConstants.CENTER);
            alarmField.setBackground(Annunciator.REDOFF_COLOR);
            alarmField.setForeground(Color.BLACK);
            alarmField.setFont(Fonts.text("Dialog", Font.BOLD, 10));
            alarmField.setPreferredSize(new Dimension(150, 22));
            gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
            add(alarmField, gc);
            gc.gridwidth = 1;
        }

        gc.gridx = 0; gc.gridy = 1; gc.gridheight = 2;
        add(positionGauge, gc);

        gc.gridheight = 1;
        gc.gridx = 1; gc.gridy = 1;
        add(flowDisplay, gc);

        gc.gridx = 1; gc.gridy = 2;
        JPanel radios = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));
        radios.setOpaque(false);
        radios.add(openButton);
        radios.add(stopButton);
        radios.add(closeButton);
        add(radios, gc);

        // ---- 自动控制开关（投入后由水位自动开/关；退出后只认手动指令）----
        autoBox.setText(Localization.t("Auto-Control"));
        autoBox.setSelected(valve != null && valve.isAutoEnabled());
        autoBox.setBackground(UI.BACKGROUND);
        autoBox.setFont(Fonts.text("Dialog", Font.PLAIN, 11));
        autoBox.addActionListener((ActionEvent e) -> {
            if (valve != null) {
                valve.setAutoEnabled(autoBox.isSelected());
            }
        });
        gc.gridx = 1; gc.gridy = 2;
        add(autoBox, gc);

        stateLabel.setBackground(UI.BACKGROUND);
        gc.gridx = 0; gc.gridy = 4; gc.gridwidth = 2;
        add(stateLabel, gc);
    }

    /** 报警灯（未启用报警时为 null） */
    public JTextField getAlarmField() {
        return alarmField;
    }

    /** 每帧刷新：阀位、流量、状态文字 */
    public void refresh() {
        if (valve == null) {
            return;
        }
        dispPosition += (float) ((valve.getPosition() * 100 - dispPosition) * 0.35f);
        positionGauge.setValue(dispPosition);
        flowDisplay.setLcdValue(valve.timestepFlow * 20);   // kg/s
        String state;
        switch (valve.getState()) {
            case 2: state = Localization.t("Open"); break;
            case 0: state = Localization.t("Close"); break;
            default: state = Localization.t("Stop"); break;
        }
        stateLabel.setText(state + "  ·  " + Localization.t("Auto-Control") + ": "
                + (valve.isAutoEnabled() ? Localization.t("On") : Localization.t("Off")));
        // 报警灯：阀门离开关闭位就亮红（"排水阀开启"），回到关闭位恢复暗红
        if (alarmField != null) {
            boolean open = valve.getPosition() > 0.01f;
            java.awt.Color want = open ? Annunciator.REDON_COLOR : Annunciator.REDOFF_COLOR;
            if (!want.equals(alarmField.getBackground())) {
                alarmField.setBackground(want);
            }
        }
    }
}
