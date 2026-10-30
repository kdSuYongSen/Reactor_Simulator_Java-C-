package com.darwish.nppsim;

import java.awt.Color;
import javax.swing.JButton;
import javax.swing.Timer;

/**
 * 带"保护盖"的按钮 —— 用于 AZ-1K / BSM / AZ-5 等重要跳闸键，防止误触。
 *
 * 交互流程：
 *   第一次点击 → 只"打开保护盖"：按钮背景变亮黄并闪烁（**文字保持原样**），
 *                开始 10 秒倒计时等待确认；
 *   10 秒内的第二次点击 → "按下按键"：关盖复位并真正执行动作（跳闸）；
 *   10 秒内没有第二次点击 → 自动"关盖"复位（恢复原颜色）。
 *   外部调用 resetCover() 可立即关盖（如按下"复位 AZ"时联动复位所有保护盖）。
 *
 * 注：只需在构造时包装一次；被包装按钮不应再挂其它 ActionListener。
 * 所有状态变更都发生在 EDT（按钮点击回调和 Swing Timer 都在 EDT 上）。
 */
final class ProtectedButton {

    /** 保护盖打开后的提示色（亮黄） */
    private static final Color COVER_COLOR = new Color(255, 208, 51);
    /** 开盖后的确认时限（毫秒）——超时自动关盖 */
    private static final int CONFIRM_TIMEOUT_MS = 10000;
    /** 闪烁周期（毫秒） */
    private static final int BLINK_MS = 450;

    private final JButton button;
    private final Runnable action;
    private final String label;
    private final Color normalBackground;
    private final Color normalForeground;
    private final Timer timeout;   // 10 秒未确认 → 自动复位
    private final Timer blink;     // 开盖期间黄/原色交替闪烁
    private boolean armed = false;
    private boolean blinkOn = false;

    /** @param button 被保护的按钮（其文本/背景色在构造时快照，用于复位） */
    protected ProtectedButton(JButton button, Runnable action) {
        this.button = button;
        this.action = action;
        this.label = button.getText();
        this.normalBackground = button.getBackground();
        this.normalForeground = button.getForeground();
        button.setToolTipText(Localization.t("Press again to confirm"));
        button.addActionListener(ev -> onClick());
        timeout = new Timer(CONFIRM_TIMEOUT_MS, ev -> disarm());
        timeout.setRepeats(false);
        blink = new Timer(BLINK_MS, ev -> {
            if (!armed) {
                return;
            }
            blinkOn = !blinkOn;
            button.setBackground(blinkOn ? COVER_COLOR : normalBackground);
        });
    }

    /** 保护盖当前是否打开（等待第二次确认） */
    boolean isArmed() {
        return armed;
    }

    /** 外部强制关盖复位（如"复位 AZ"时联动复位所有保护盖）；未开盖时无操作 */
    void resetCover() {
        disarm();
    }

    private void onClick() {
        if (armed) {
            disarm();
            action.run();   // 第二次点击：真正触发
        } else {
            arm();
        }
    }

    /** 打开保护盖：变黄闪烁（文字保持原样），等待第二次点击（10 秒超时） */
    private void arm() {
        armed = true;
        blinkOn = true;
        button.setBackground(COVER_COLOR);
        button.setForeground(Color.BLACK);
        blink.start();
        timeout.restart();
    }

    /** 关盖复位：恢复原颜色（超时、确认或外部复位后调用） */
    private void disarm() {
        if (!armed) {
            return;
        }
        armed = false;
        blink.stop();
        timeout.stop();
        button.setText(label);
        button.setForeground(normalForeground);
        button.setBackground(normalBackground);
    }
}
