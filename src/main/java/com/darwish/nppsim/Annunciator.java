package com.darwish.nppsim;

import static com.darwish.nppsim.Loader.soundProvider;
import static com.darwish.nppsim.NPPSim.autoControl;
import java.awt.Color;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Annunciator {
    private static boolean isInitialized = false;
    protected static boolean annunciatorBlinker = false;
    protected final HashMap<javax.swing.JTextField, Integer> annunciatorArray = new HashMap<>();
    public static final Color REDOFF_COLOR = new Color(142, 0, 0);
    public static final Color REDON_COLOR = new Color(255, 22, 25);
    public static final Color YELLOWOFF_COLOR = new Color(107, 103, 0);
    public static final Color YELLOWON_COLOR = new Color(255, 246, 35);
    public static final Color ORANGEOFF_COLOR = new Color(140, 78, 0);
    public static final Color ORANGEON_COLOR = new Color(255, 146, 20);
    public static final Color GREENON_COLOR = new Color(143, 228, 143);
    public static final Color GREENOFF_COLOR = new Color(27, 113, 27);
    public static final Color BLUEOFF_COLOR = new Color(8,106,107);
    public static final Color BLUEON_COLOR = new Color(15,206,208);
    public static Thread annunciatorThread;
    /** 所有已创建的报警器实例（警笛决策是全局的：任一窗口有未确认报警就响） */
    private static final java.util.ArrayList<Annunciator> INSTANCES = new java.util.ArrayList<>();
    /** 警笛的专用 owner：与"试听/其他兼容调用"的 GLOBAL_OWNER 分开记账，互不误停 */
    private static final Object HORN_OWNER = new Object();
    /** 本实例的灯板（判断所属窗口是否还活着） */
    private final javax.swing.JPanel panel;

    /**
     * @param annunciatorPanel the panel with JTextFields which act as annunciator lights
     */
    public Annunciator(javax.swing.JPanel annunciatorPanel) {
        this.panel = annunciatorPanel;
        for(short i = 0; i < annunciatorPanel.getComponentCount(); i++) {
            annunciatorArray.put((javax.swing.JTextField)annunciatorPanel.getComponent(i), 0);
        }
        if(!isInitialized) {
            initializeAnnunciatorBlinker();
            isInitialized = true;
        }
        INSTANCES.add(this);
    }
    
    private static void initializeAnnunciatorBlinker() {
        annunciatorThread = new Thread(() -> {
            while (true) {
                try {
                    while(NPPSim.isPaused()) {
                        Thread.sleep(500);
                    }
                    annunciatorBlinker = true;
                    Thread.sleep(500);
                    annunciatorBlinker = false;
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        annunciatorThread.start();
    }
    
    public void update() {
        // 性能优化：
        // 1) 原实现把 soundProvider 的播放/停止放在每个字段的 forEach 内部，一帧内重复调用最多十余次，
        //    这里移到循环外，每帧只处理一次，显著降低音频线程开销。
        // 2) 仅在颜色真正变化时才调用 setBackground，避免每帧对所有指示灯触发无谓重绘。
        for (java.util.Map.Entry<javax.swing.JTextField, Integer> entry : annunciatorArray.entrySet()) {
            javax.swing.JTextField field = entry.getKey();
            int state = entry.getValue();
            Color fieldColor = field.getBackground();
            Color colorOn, colorOff;
            if (fieldColor.equals(REDOFF_COLOR) || fieldColor.equals(REDON_COLOR)) {
                colorOn = REDON_COLOR;
                colorOff = REDOFF_COLOR;
            } else if (fieldColor.equals(ORANGEOFF_COLOR) || fieldColor.equals(ORANGEON_COLOR)) {
                colorOn = ORANGEON_COLOR;
                colorOff = ORANGEOFF_COLOR;
            } else {
                colorOn = YELLOWON_COLOR;
                colorOff = YELLOWOFF_COLOR;
            }
            Color desired;
            switch (state) {
                case 0:
                    desired = colorOff;
                    break;
                case 1:
                    desired = colorOn;
                    break;
                case 2:
                    desired = annunciatorBlinker ? colorOff : colorOn;
                    break;
                default:
                    desired = colorOff;
                    break;
            }
            if (!desired.equals(fieldColor)) {
                field.setBackground(desired);
            }
        }
        updateHorn();
    }

    /**
     * 全局警笛决策：任一窗口存在未确认（闪烁中）的报警 → 响；全部确认/消除 → 停。
     * 决策集中在这一处，避免各窗口线程互相 play/stop 打架（旧版 ACK 后警笛不停即源于此）。
     */
    private static void updateHorn() {
        if (soundProvider == null) {
            return;
        }
        /* 2026-10-18 重写：不再用 hornOn 做边沿检测。
         * 原实现把"是否正在响"缓存在静态布尔里，一旦外部清空过请求（停堆/换档/试听释放），
         * hornOn 仍是 true 而实际没有请求 → 警笛哑掉且再也不重启；反过来窗口被关掉、
         * 灯板停止更新时，未确认报警又会让警笛一直响到关游戏。
         * 现在直接按"全局是否还有未确认报警"幂等地 request/release，SoundProvider 自己按
         * 引用计数决定响不响，状态不可能失配。 */
        if (anyUnacknowledged()) {
            soundProvider.request(soundProvider.ALARM_1, HORN_OWNER);
        } else {
            soundProvider.release(soundProvider.ALARM_1, HORN_OWNER);
        }
        soundProvider.tick();   // 空操作保护：本该在响却停了的 Clip 会被看门狗重启
    }

    private static boolean anyUnacknowledged() {
        for (Annunciator a : INSTANCES) {
            // 窗口已被关闭（dispose）时它的灯板不会再刷新，不能拿它的旧状态当"未确认"，
            // 否则警笛会一直响、而 ACK 按钮在那个已经关掉的窗口里，按不到。
            if (a.panel == null || !a.panel.isDisplayable()) {
                continue;
            }
            for (int state : a.annunciatorArray.values()) {
                if (state == 2) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void trigger(javax.swing.JTextField element) {
        if (annunciatorArray.get(element) == 0) {
            autoControl.recordEvent(Localization.t("Warning: ") + element.getText());
        } else {
            return;
        }
        annunciatorArray.replace(element, 0, 2);
        if (UI.hush) { 
            annunciatorArray.replace(element, 2, 1);
        } else {
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(element);
            topFrame.setState(JFrame.NORMAL);
            topFrame.setVisible(true);
            topFrame.toFront();
        }
    }
    
    public void acknowledge() {
        annunciatorArray.forEach((element, state) -> {
            annunciatorArray.replace(element, 2, 1);
        });
        // 统一交给 updateHorn 重新评估：全部确认则释放警笛，其他窗口还有未确认就继续响。
        // 跳闸声（alarm2/alarm3）由各 AZ 保护的 tripped 状态独占，ACK 不会顺带掐掉它。
        updateHorn();
    }
    
    public void reset(javax.swing.JTextField element) {
        if (annunciatorArray.get(element) != 0) {
            autoControl.recordEvent(Localization.t("Cleared: ") + element.getText());
        } else {
            return;
        }
        annunciatorArray.replace(element, 0);
        updateHorn();
    }
    
    /**
     * Triggers or resets the element based on the condition given
     * @param condition the condition to trigger the element
     * @param element the element to trigger if condition is met
     */
    public void setTrigger(boolean condition, javax.swing.JTextField element) {
        if (condition) {
            trigger(element);
        } else {
            reset(element);
        }
    }
}
