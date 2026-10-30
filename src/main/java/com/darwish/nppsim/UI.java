package com.darwish.nppsim;

import static com.darwish.nppsim.NPPSim.autoControl;
import static com.darwish.nppsim.NPPSim.core;
import static com.darwish.nppsim.NPPSim.msvLoop1;
import static com.darwish.nppsim.NPPSim.msvLoop2;
import static com.darwish.nppsim.NPPSim.sdv_a;
import static com.darwish.nppsim.NPPSim.tg1;
import static com.darwish.nppsim.NPPSim.tg2;
import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;
import static com.darwish.nppsim.NPPSim.mcc;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.plaf.metal.MetalToggleButtonUI;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class UI extends javax.swing.JFrame implements Serializable {
    /** 全厂统一背景色（唯一来源见 {@link UiTheme#BG}） */
    static Color BACKGROUND = UiTheme.BG;
    static ArrayList<ControlRodChannel> selectedControlRods;
    static ArrayList<UIUpdateable> elementsToUpdate; 
    static final ArrayList<Thread> uiThreads = new ArrayList<>();
    final Annunciator annunciator;
    private boolean debounce = false;
    private static long uiUpdateRate = 50;

    /** 设置页面刷新率（毫秒/帧）；下限 20ms 防止刷爆 */
    public static void setUiUpdateRate(long ms) {
        uiUpdateRate = Math.max(20, ms);
    }

    /** MTK 面板访问器（设置 → 原版设置 用；未开新游戏时为 null） */
    public MTK getMTK1() {
        return mTK1;
    }
    static boolean hush = true;
    // 性能优化：记录上次刷新标题的仿真秒数，只在秒数变化时更新窗口标题
    private long lastTitleSecond = Long.MIN_VALUE;

    /* ---- 外部布局编辑器（ui_layout.json）状态 ---- */
    /** jPanel3 的 GroupLayout 引用（切到绝对布局后仍保留，便于恢复） */
    private javax.swing.GroupLayout jPanel3GroupLayout;
    /** 面板注册表：key → 组件（key 与 Python 工具 BLOCKS 一一对应） */
    private java.util.Map<String, javax.swing.JComponent> layoutBlockMap;
    /** 进入绝对布局前记录的默认坐标（GroupLayout 计算值），用作未列出面板的兜底 */
    private java.util.Map<String, java.awt.Rectangle> layoutFallbackBounds;
    /** 当前是否处于绝对布局模式 */
    private boolean absoluteLayoutActive = false;
    /** 布局文件轮询（每秒检查一次变化） */
    private javax.swing.Timer layoutWatchTimer;
    private String layoutFilePath;
    private long layoutFileStamp = -1;
    /** 面板内容缩放器：方块放大/缩小时，内部按键、文字、仪表数值同步缩放 */
    private final PanelContentScaler contentScaler = new PanelContentScaler();
    /** AZ 保护盖（AZ-1K / BSM / AZ-5）：按"复位 AZ"时联动复位 */
    private ProtectedButton guardAZ1K, guardBSM, guardAZ5;
    /** 控制棒数量限制开关（ON=选中超过限值禁止整组拔出；OFF=不限制） */
    static boolean rodSelectionLimitEnabled = true;
    /** 子组件绝对布局：面板 key → 切换前的原始 LayoutManager（用于恢复） */
    private final java.util.Map<String, java.awt.LayoutManager> savedChildLayouts = new java.util.LinkedHashMap<>();
    /** 子组件绝对布局：面板 key → 各子组件切换前的位置（未列出的子组件保持原位） */
    private final java.util.Map<String, java.awt.Rectangle[]> childFallbackBounds = new java.util.LinkedHashMap<>();
    
    public UI() {
        hush = true;
        try {
            // 恢复原有的 NetBeans DarkMetal 金属风格外观（由反编译源码移植，见 library-java/org/netbeans/swing/laf/dark）
            UiStyle.applyLaf();
            UiTheme.install();          // 窗口背板统一成原版橄榄色（面板背板仍用 LAF 深色）
            uiUpdateRate = SettingsDialog.getUiUpdateRate();   // 上次选择的页面刷新率
            //UI.BACKGROUND = new Color(115, 53, 0); //TODO
        } catch (Exception e) {
            e.printStackTrace();
        }
        initComponents();
        // 主反应堆窗口：可自由缩放/最大化（只有「主菜单窗口」Loader 禁止最大化）
        java.awt.Dimension startSize = new java.awt.Dimension(1366, 768);
        setSize(startSize);
        setMinimumSize(new java.awt.Dimension(1024, 700));
        setResizable(true);
        setLocationRelativeTo(null);
        selectedControlRods = new ArrayList<>();
        elementsToUpdate = new ArrayList<>();
        annunciator = new Annunciator(annunciatorPanel);
        // AZ 保护盖：AZ-1K / BSM / AZ-5 第一次点击只开盖（单击无效，文字保留、亮黄闪烁），
        // 10 秒内再按一次才真正触发；10 秒无响应自动复位；按"复位 AZ"时联动复位
        guardAZ1K = new ProtectedButton(jButton3, () -> jButton3ActionPerformed(null));
        guardBSM = new ProtectedButton(jButton4, () -> jButton4ActionPerformed(null));
        guardAZ5 = new ProtectedButton(azButton5, () -> autoControl.az5Control.trip());
        /* 主窗口的 Window 菜单也交给统一清单重建（2026-10-25）：
         * 否则主窗口少了新增的 8 个页面，而子窗口有 —— 又是一个"选项不一致"。 */
        WindowMenu.install(this);
        initLayoutSync();   // 外部布局编辑器：恢复已保存布局 + 启动文件轮询
        this.setVisible(true);
        /* 棒速度块要钉在"插入"按键下面：方块运行时是绝对布局（PageLayout 把 layout 置 null），
         * 布局文件里也没有这个新控件，所以用一个 400 ms 的轻量定时器对齐一次（只在位置变了才 setBounds）。
         * 这样在布局编辑器里挪动"插入"后，棒速度块也会跟着走。 */
        javax.swing.Timer rodSpeedSync = new javax.swing.Timer(400, e -> syncRodSpeedPanelBounds());
        rodSpeedSync.setRepeats(true);
        rodSpeedSync.start();
        toggleAR1.setUI(new MetalToggleButtonUI() {
            @Override
            protected Color getSelectColor() {
                return Annunciator.BLUEON_COLOR;
            }
        });
        toggleAR2.setUI(new MetalToggleButtonUI() {
            @Override
            protected Color getSelectColor() {
                return Annunciator.BLUEON_COLOR;
            }
        });
        toggleLAC.setUI(new MetalToggleButtonUI() {
            @Override
            protected Color getSelectColor() {
                return Annunciator.BLUEON_COLOR;
            }
        });
        
        initializeDialUpdateThread();
        precisionController();
        
        //set values
        toggleAR1.getModel().setSelected(autoControl.automaticRodController.isEnabled()[1]);
        toggleAR2.getModel().setSelected(autoControl.automaticRodController.isEnabled()[2]);
        toggleLAC.getModel().setSelected(autoControl.automaticRodController.isEnabled()[0]);
        setpoint.setLcdValue(autoControl.automaticRodController.getSetpoint());
        createOrContinue(PCSUI.class, true, true);
        createOrContinue(TGUI.class, true, true);
        createOrContinue(CondensateUI.class, true, true);
        createOrContinue(FeedwaterUI.class, true, true);
        createOrContinue(DearatorUI.class, true, true);
        createOrContinue(MCPUI.class, true, true);
        createOrContinue(SteamLoopUI.class, false, true);   // 读数窗口按自身尺寸，不套 1366×768
    }

    /* ================================================================
     * 外部布局编辑器支持（Python 工具 → ui_layout.json → Java 实时应用）
     *  - 配套工具：SourceCode/tools/layout_editor/layout_editor.py
     *    （把主窗口每个面板简化为彩色方块，拖动改变位置后自动保存 JSON）
     *  - 本程序启动时读取该文件；运行中每 1 秒轮询文件变化，编辑后界面自动跟随；
     *  - 文件不存在、或被删除、或 mode != "absolute" 时使用默认 GroupLayout 布局。
     * ================================================================ */

    public void update() {
        // 性能优化：标题只在秒数变化时才更新，避免每帧都做字符串格式化并触发原生窗口标题重绘
        long simulationSeconds = (long) autoControl.getSimulationTime();
        if (simulationSeconds != lastTitleSecond) {
            lastTitleSecond = simulationSeconds;
            setTitle(NPPMath.formatSecondsToDaysAndTime(autoControl.getSimulationTime(), true));
        }
        for (UIUpdateable i: elementsToUpdate) {
            try {
                i.update();
            } catch (Exception e) {
                // 单个窗口刷新异常不得拖垮全局 UI 更新（否则所有 LCD 冻结在初始值）
                e.printStackTrace();
            }
        }
        mTK1.update();
        double currentNeutronFlux = core.getNeutronCount();
        //double kEffective = Math.pow(currentNeutronFlux / previousNeutronFlux, 1 / 10.0);
        double reactivity = core.getReactivity();//(kEffective - 1) / kEffective;
        double reactorPeriod = core.getPeriod();//0.1 / (kEffective - 1);
        double thermalPower = core.getThermalPower();

        java.awt.EventQueue.invokeLater(() -> {
            showPower(thermalPower1, thermalPower, true);
            neutronFlux.setLcdValue(currentNeutronFlux / 4.9808177502E13);
            keff.setLcdValue(reactivity);
            period.setLcdValue(reactorPeriod < -9999 || reactorPeriod > 9999 ? Double.POSITIVE_INFINITY : reactorPeriod);
            deltaKeff.setValue(reactivity * 100);
            showUnit(drumTemp, Units.Quantity.TEMPERATURE, mcc.drum1.getWaterTemperature());
            showUnit(drumTemp1, Units.Quantity.TEMPERATURE, mcc.drum2.getWaterTemperature());
            showUnit(headerTemp, Units.Quantity.TEMPERATURE, mcc.pHeader1.getWaterTemperature());
            showUnit(headerTemp1, Units.Quantity.TEMPERATURE, mcc.pHeader2.getWaterTemperature());
            showUnit(suctionTemp, Units.Quantity.TEMPERATURE, mcc.sHeader1.getWaterTemperature());
            showUnit(suctionTemp1, Units.Quantity.TEMPERATURE, mcc.sHeader2.getWaterTemperature());
            // 联箱流量叠加离线冷却分流量：泵 A 接环路 1、泵 B 接环路 2（只有核心侧有效）
            if (NPPSim.core != null && NPPSim.core.offlineCooling != null) {
                showUnit(headerFlow, Units.Quantity.FLOW, mcc.pHeader1.getWaterInflowRate() + NPPSim.core.offlineCooling.flowA);
                showUnit(headerFlow1, Units.Quantity.FLOW, mcc.pHeader2.getWaterInflowRate() + NPPSim.core.offlineCooling.flowB);
            } else {
                showUnit(headerFlow, Units.Quantity.FLOW, mcc.pHeader1.getWaterInflowRate());
                showUnit(headerFlow1, Units.Quantity.FLOW, mcc.pHeader2.getWaterInflowRate());
            }
            showPower(electric1, tg1.getGeneratorLoad(), false);
            showPower(electric2, tg2.getGeneratorLoad(), false);
            showUnit(press1, Units.Quantity.PRESSURE, mcc.drum1.getPressure());
            showUnit(press2, Units.Quantity.PRESSURE, mcc.drum2.getPressure());
            offlineCoolingPanel.refresh();
        });
        
        syncRodSpeedPanelBounds();
        checkAlarms();
        // 性能优化：改为索引 for 循环（避免嵌套 lambda 的闭包对象与迭代器分配）；
        // 并且对 Void/Reflector 等没有任何表格数据的通道直接跳过，省去每帧数千次无意义的空调用。
        ArrayList<ArrayList<Channel>> coreArray = core.coreArray;
        for (int row = 0, rowCount = coreArray.size(); row < rowCount; row++) {
            ArrayList<Channel> channelRow = coreArray.get(row);
            for (int col = 0, colCount = channelRow.size(); col < colCount; col++) {
                Channel channel = channelRow.get(col);
                if (channel instanceof VoidChannel || channel instanceof ReflectorChannel) {
                    continue;
                }
                channel.updateTableValues();
                channel.resetFlowRates();
            }
        }
        mcc.drum1.resetFlowRates();
        mcc.drum2.resetFlowRates();
        hush = false;
    }
    
    public void initializeDialUpdateThread() {
        UI.uiThreads.add(
            new Thread(() -> {
                try {
                    while (true) {
                        annunciator.update();
                        if (this.isFocused()) {
                            manualRodControl1.update();
                            java.awt.EventQueue.invokeLater(() -> {
                                // AZ 灯分级配色：AZ-1 红、AZ-2 黄、AZ-3/4 橙、AZ-6/BAZ 红
                                // （面板行序：rodLimit1=AZ-1、rodLimit2=AZ-2、rodLimit3=AZ-6、
                                //   rodLimit4=AZ-4、rodLimit5=AZ-3、rodLimit10=BAZ；
                                //   AZ-5 为手动停堆，只有按钮、没有自动指示灯）
                                if (autoAzOn.isSelected() != autoControl.azAutoEnabled) {
                                    if (autoControl.azAutoEnabled) {
                                        autoAzOn.setSelected(true);
                                    } else {
                                        autoAzOff.setSelected(true);
                                    }
                                }
                                rodLimit1.setBackground(autoControl.az1Control.isTripped() ? Annunciator.REDON_COLOR : Annunciator.REDOFF_COLOR);
                                rodLimit2.setBackground(autoControl.az2Control.isTripped() ? Annunciator.YELLOWON_COLOR : Annunciator.YELLOWOFF_COLOR);
                                rodLimit5.setBackground(autoControl.az3Control.isTripped() ? Annunciator.ORANGEON_COLOR : Annunciator.ORANGEOFF_COLOR);
                                rodLimit4.setBackground(autoControl.az4Control.isTripped() ? Annunciator.ORANGEON_COLOR : Annunciator.ORANGEOFF_COLOR);
                                rodLimit3.setBackground(autoControl.az6Control.isTripped() ? Annunciator.REDON_COLOR : Annunciator.REDOFF_COLOR);
                                rodLimit10.setBackground(autoControl.fasrControl.isTripped() ? Annunciator.REDON_COLOR : Annunciator.REDOFF_COLOR);
                                if (autoControl.automaticRodController.isEnabled()[0]) {
                                    if (autoControl.automaticRodController.onLimit()[0]) {
                                        limitLAC.setBackground(Annunciator.YELLOWON_COLOR);
                                    } else {
                                        limitLAC.setBackground(Annunciator.YELLOWOFF_COLOR);
                                    }
                                    if (autoControl.automaticRodController.hasError()[0]) {
                                        errorLAC.setBackground(Annunciator.YELLOWON_COLOR);
                                    } else {
                                        errorLAC.setBackground(Annunciator.YELLOWOFF_COLOR);
                                    }
                                    if (autoControl.automaticRodController.isBusy()[0]) {
                                        busyLAC.setBackground(Annunciator.GREENON_COLOR);
                                    } else {
                                        busyLAC.setBackground(Annunciator.GREENOFF_COLOR);
                                    }
                                }
                                if (autoControl.automaticRodController.isEnabled()[1]) {
                                    if (autoControl.automaticRodController.onLimit()[1]) {
                                        limit1AR.setBackground(Annunciator.YELLOWON_COLOR);
                                    } else {
                                        limit1AR.setBackground(Annunciator.YELLOWOFF_COLOR);
                                    }
                                    if (autoControl.automaticRodController.hasError()[1]) {
                                        error1AR.setBackground(Annunciator.YELLOWON_COLOR);
                                    } else {
                                        error1AR.setBackground(Annunciator.YELLOWOFF_COLOR);
                                    }
                                    if (autoControl.automaticRodController.isBusy()[1]) {
                                        busy1AR.setBackground(Annunciator.GREENON_COLOR);
                                    } else {
                                        busy1AR.setBackground(Annunciator.GREENOFF_COLOR);
                                    }
                                }
                                if (autoControl.automaticRodController.isEnabled()[2]) {
                                    if (autoControl.automaticRodController.onLimit()[2]) {
                                        limit2AR.setBackground(Annunciator.YELLOWON_COLOR);
                                    } else {
                                        limit2AR.setBackground(Annunciator.YELLOWOFF_COLOR);
                                    }
                                    if (autoControl.automaticRodController.hasError()[2]) {
                                        error2AR.setBackground(Annunciator.YELLOWON_COLOR);
                                    } else {
                                        error2AR.setBackground(Annunciator.YELLOWOFF_COLOR);
                                    }
                                    if (autoControl.automaticRodController.isBusy()[2]) {
                                        busy2AR.setBackground(Annunciator.GREENON_COLOR);
                                    } else {
                                        busy2AR.setBackground(Annunciator.GREENOFF_COLOR);
                                    }
                                }
                            });
                        }
                        Thread.sleep(50);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            })
        );
        UI.uiThreads.get(UI.uiThreads.size() - 1).start();
    }
    
    private void checkAlarms() { 
        boolean sdvA = false;
        boolean msvI = false;
        boolean msvII = false;
        boolean msvIII = false;
        for (int i = 0; i < sdv_a.size(); i++) {
            if (sdv_a.get(i).getPosition() != 0.0f) {
                sdvA= true;
                break;
            }
        }
        if (msvLoop1.get(0).getPosition() != 0.0f || msvLoop2.get(0).getPosition() != 0.0f) {
            msvI = true;
        }
        for (int i = 1; i < 3; i++) {
            if (msvLoop1.get(i).getPosition() != 0.0f || msvLoop2.get(i).getPosition() != 0.0f) {
                msvII= true;
                break;
            }
        }
        for (int i = 3; i < 6; i++) {
            if (msvLoop1.get(i).getPosition() != 0.0f || msvLoop2.get(i).getPosition() != 0.0f) {
                msvIII= true;
                break;
            }
        }
        boolean arInop = (autoControl.automaticRodController.isEnabled()[1] && autoControl.automaticRodController.hasError()[1] && autoControl.automaticRodController.onLimit()[1]) || (autoControl.automaticRodController.isEnabled()[2] && autoControl.automaticRodController.hasError()[2] && autoControl.automaticRodController.onLimit()[2]);
        boolean lacInop = autoControl.automaticRodController.isEnabled()[0] && autoControl.automaticRodController.hasError()[0] && autoControl.automaticRodController.onLimit()[0];
        double currentPeriod = core.getPeriod();
        boolean periodLess30 = currentPeriod < 30 && currentPeriod > 0;
        annunciator.setTrigger(mcc.drum1.getWaterLevel() < -25 || mcc.drum2.getWaterLevel() < -25, lowWaterLevel);
        annunciator.setTrigger(mcc.drum1.getWaterLevel() > 25 || mcc.drum2.getWaterLevel() > 25, highWaterLevel);
        annunciator.setTrigger(mcc.drum1.getPressure() > 7.02 || mcc.drum2.getPressure() > 7.02, highDrumPress);
        annunciator.setTrigger(mcc.drum1.getPressure() < 5 || mcc.drum2.getPressure() < 5, lowDrumPress);
        annunciator.setTrigger(mcc.drum1.getPressure() > 7.02 || mcc.drum2.getPressure() > 7.02, highDrumPress);
        annunciator.setTrigger(msvI, msv1);
        annunciator.setTrigger(msvII, msv2);
        annunciator.setTrigger(msvIII, msv3);
        annunciator.setTrigger(sdvA, sdv_aOpen);
        annunciator.setTrigger(arInop, arINOP);
        annunciator.setTrigger(lacInop, lacINOP);
        annunciator.setTrigger(periodLess30, t30);
        annunciator.setTrigger(core.getThermalPower() > 5000, power);
        annunciator.setTrigger(core.getReactivity() > 0.01, nRate);
    }
    
    private void precisionController() {
        UI.uiThreads.add(
            new Thread(() -> {
                while(true) {
                    final boolean[] travelling = {false};
                    final boolean[] sequenceBlock = {autoControl.fasrControl.getSequenceBlock()};
                    try {
                        selectedControlRods.forEach(channel -> {
                            if (rodsOut.getModel().isPressed()) {
                                // 数量限制开关：ON 时选中超过 4 根禁止整组拔出（亮"selection limit"灯）
                                if (rodSelectionLimitEnabled && selectedControlRods.size() > 4) {
                                    rodLimit.setBackground(Annunciator.YELLOWON_COLOR);
                                } else {
                                    if (sequenceBlock[0]) {
                                        if (channel instanceof FASRChannel) {
                                            if (channel.getPosition() > 0) {
                                            channel.setState(0);
                                            travelling[0] = true;
                                            }
                                        } else {
                                            rodSeq.setBackground(Annunciator.YELLOWON_COLOR);
                                            channel.setState(1);
                                        }
                                    } else if (channel.getPosition() > 0) {
                                        channel.setState(0);
                                        travelling[0] = true;
                                    }
                                }
                            } else if (rodsIn.getModel().isPressed()){
                                if (channel.getPosition() < 1) {
                                    channel.setState(2);
                                    travelling[0] = true;
                                }
                            } else {
                                channel.setState(1);
                                rodSeq.setBackground(Annunciator.YELLOWOFF_COLOR);
                                rodLimit.setBackground(Annunciator.YELLOWOFF_COLOR);
                            }
                        });
                        if (travelling[0]) {
                            rodTravel.setBackground(Annunciator.GREENON_COLOR);
                        } else {
                            rodTravel.setBackground(Annunciator.GREENOFF_COLOR);
                        }
                        if (!sequenceBlock[0]) {
                            rodSeq.setBackground(Annunciator.YELLOWOFF_COLOR);
                        }
                        
                        if (debounce) {
                            Thread.sleep(50);
                            debounce = false;
                        }
                        if (precisionIncrement2.getModel().isPressed()) {
                            precisionIncrement2ActionPerformed(null);
                        } else if (precisionDecrement2.getModel().isPressed()) {
                            precisionDecrement2ActionPerformed(null);
                        }
                        
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            })
        );
        uiThreads.get(uiThreads.size() - 1).start();
    }

    public static void createOrContinue(Class element, boolean fullscreen, boolean justInit) {
        for (UIUpdateable i: elementsToUpdate) {
            if (i.getClass() == element) {
                i.setVisibility(true);
                ((JFrame)i).toFront();
                ((JFrame)i).requestFocus();
                return;
            }
        }
        try {
            Object newElement = element.getDeclaredConstructor().newInstance();
            if (fullscreen) {
                // 仅“主菜单窗口”锁定大小；子窗口（PCS/TG/Condensate/Feedwater/Dearator/MCP）解锁，
                // 可自由缩放。仍给一个合理的最小尺寸，避免被拖到看不见内容。
                javax.swing.JFrame frame = (javax.swing.JFrame)newElement;
                java.awt.Dimension childSize = new java.awt.Dimension(1366, 768);
                // 子窗口构造时若在底部追加了面板（如排水阀），首选尺寸会高于 1366×768：
                // 此时尊重首选尺寸，否则追加的行会被这次 setSize 挤没
                java.awt.Dimension pref = frame.getPreferredSize();
                frame.setSize(Math.max(childSize.width, pref.width),
                              Math.max(childSize.height, pref.height));
                frame.setMinimumSize(new java.awt.Dimension(800, 600));
                frame.setResizable(true);
                frame.setLocationRelativeTo(null);
                // 尺寸可能因首选尺寸而大于屏幕：钳位并移回屏幕内，保证底部追加的行（如排水阀）可见
                WindowLayout.clampToScreen(frame);
                // 设置→视频 里选定的窗口模式（窗口化/最大化）对新开窗口自动套用
                SettingsDialog.applyWindowMode(frame);
            }
            if (!justInit) {
                ((UIUpdateable)newElement).setVisibility(true);
            }
            elementsToUpdate.add((UIUpdateable)newElement);
        } catch (Exception e) {
            e.printStackTrace();
            new ErrorWindow("Error loading UI element class" , ExceptionUtils.getStackTrace(e), false).setVisible(true);
        }
    }
    
    public static long getUpdateRate() {
        return uiUpdateRate;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        buttonGroup6 = new javax.swing.ButtonGroup();
        buttonGroup7 = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        annunciatorPanel = new javax.swing.JPanel();
        highWaterLevel = new javax.swing.JTextField();
        highDrumPress = new javax.swing.JTextField();
        msv1 = new javax.swing.JTextField();
        nRate = new javax.swing.JTextField();
        arINOP = new javax.swing.JTextField();
        lowWaterLevel = new javax.swing.JTextField();
        sdv_aOpen = new javax.swing.JTextField();
        msv2 = new javax.swing.JTextField();
        t30 = new javax.swing.JTextField();
        lacINOP = new javax.swing.JTextField();
        mcpCavit = new javax.swing.JTextField();
        lowDrumPress = new javax.swing.JTextField();
        msv3 = new javax.swing.JTextField();
        power = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        manualRodControl1 = new com.darwish.nppsim.ManualRodControl();
        jPanel1 = new javax.swing.JPanel();
        rodLimit = new javax.swing.JTextField();
        rodSeq = new javax.swing.JTextField();
        rodTravel = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        rodLimit1 = new javax.swing.JTextField();
        rodLimit2 = new javax.swing.JTextField();
        rodLimit3 = new javax.swing.JTextField();
        rodLimit10 = new javax.swing.JTextField();
        rodLimit4 = new javax.swing.JTextField();
        rodLimit5 = new javax.swing.JTextField();
        jButton4 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        rodsOut1 = new javax.swing.JButton();
        rodsOut = new javax.swing.JButton();
        rodsIn = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        keff = new eu.hansolo.steelseries.gauges.DisplaySingle();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        period = new eu.hansolo.steelseries.gauges.DisplaySingle();
        neutronFlux = new eu.hansolo.steelseries.gauges.DisplaySingle();
        jLabel7 = new javax.swing.JLabel();
        deltaKeff = new eu.hansolo.steelseries.gauges.Linear();
        jLabel3 = new javax.swing.JLabel();
        electric2 = new eu.hansolo.steelseries.gauges.DisplayRectangular();
        jPanel7 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        setpoint = new eu.hansolo.steelseries.gauges.DisplaySingle();
        jLabel4 = new javax.swing.JLabel();
        toggleLAC = new javax.swing.JToggleButton();
        jPanel8 = new javax.swing.JPanel();
        toggleAR2 = new javax.swing.JToggleButton();
        toggleAR1 = new javax.swing.JToggleButton();
        precisionIncrement2 = new javax.swing.JButton();
        precisionDecrement2 = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        busy1AR = new javax.swing.JTextField();
        busy2AR = new javax.swing.JTextField();
        busyLAC = new javax.swing.JTextField();
        limit1AR = new javax.swing.JTextField();
        limit2AR = new javax.swing.JTextField();
        limitLAC = new javax.swing.JTextField();
        error1AR = new javax.swing.JTextField();
        error2AR = new javax.swing.JTextField();
        errorLAC = new javax.swing.JTextField();
        thermalPower1 = new eu.hansolo.steelseries.gauges.DisplayRectangular();
        electric1 = new eu.hansolo.steelseries.gauges.DisplayRectangular();
        mTK1 = new com.darwish.nppsim.MTK();
        jPanel9 = new javax.swing.JPanel();
        autoAzPanel = new javax.swing.JPanel();
        autoAzOn = new javax.swing.JRadioButton();
        autoAzOff = new javax.swing.JRadioButton();
        rodLimitPanel = new javax.swing.JPanel();
        rodLimitOn = new javax.swing.JRadioButton();
        rodLimitOff = new javax.swing.JRadioButton();
        drumTemp = new eu.hansolo.steelseries.gauges.DisplaySingle();
        drumTemp1 = new eu.hansolo.steelseries.gauges.DisplaySingle();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel49 = new javax.swing.JLabel();
        jLabel50 = new javax.swing.JLabel();
        suctionTemp = new eu.hansolo.steelseries.gauges.DisplaySingle();
        jLabel51 = new javax.swing.JLabel();
        suctionTemp1 = new eu.hansolo.steelseries.gauges.DisplaySingle();
        jLabel52 = new javax.swing.JLabel();
        press1 = new eu.hansolo.steelseries.gauges.DisplaySingle();
        press2 = new eu.hansolo.steelseries.gauges.DisplaySingle();
        jLabel53 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        headerTemp1 = new eu.hansolo.steelseries.gauges.DisplaySingle();
        jLabel55 = new javax.swing.JLabel();
        headerTemp = new eu.hansolo.steelseries.gauges.DisplaySingle();
        jLabel56 = new javax.swing.JLabel();
        headerFlow1 = new eu.hansolo.steelseries.gauges.DisplaySingle();
        jLabel57 = new javax.swing.JLabel();
        headerFlow = new eu.hansolo.steelseries.gauges.DisplaySingle();
        jLabel58 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem11 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem10 = new javax.swing.JMenuItem();
        jMenuItem12 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenuItem13 = new javax.swing.JMenuItem();
        jMenuItem14 = new javax.swing.JMenuItem();
        jMenuItem9 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        pause = new javax.swing.JCheckBoxMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenu7 = new javax.swing.JMenu();
        jRadioButtonMenuItem1 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItem2 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItem3 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItem4 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItem5 = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItem6 = new javax.swing.JRadioButtonMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenu5 = new javax.swing.JMenu();
        mtkPlus = new javax.swing.JRadioButtonMenuItem();
        mtkMin = new javax.swing.JRadioButtonMenuItem();
        jMenu6 = new javax.swing.JMenu();
        zero5 = new javax.swing.JRadioButtonMenuItem();
        one = new javax.swing.JRadioButtonMenuItem();
        two5 = new javax.swing.JRadioButtonMenuItem();
        five = new javax.swing.JRadioButtonMenuItem();
        ten = new javax.swing.JRadioButtonMenuItem();
        fifteen = new javax.swing.JRadioButtonMenuItem();
        twenty = new javax.swing.JRadioButtonMenuItem();
        twentyfive = new javax.swing.JRadioButtonMenuItem();
        jMenu8 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(UI.BACKGROUND);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        jScrollPane1.setBackground(UI.BACKGROUND);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(1366, 768));

        jPanel3.setBackground(BACKGROUND);
        jPanel3.setMinimumSize(new java.awt.Dimension(0, 0));

        annunciatorPanel.setBackground(UI.BACKGROUND);
        annunciatorPanel.setLayout(new java.awt.GridLayout(3, 5, 2, 2));

        highWaterLevel.setEditable(false);
        highWaterLevel.setBackground(new java.awt.Color(142, 0, 0));
        highWaterLevel.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        highWaterLevel.setForeground(new java.awt.Color(0, 0, 0));
        highWaterLevel.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        highWaterLevel.setText(Localization.t("High water level"));
        highWaterLevel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        annunciatorPanel.add(highWaterLevel);

        highDrumPress.setEditable(false);
        highDrumPress.setBackground(new java.awt.Color(142, 0, 0));
        highDrumPress.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        highDrumPress.setForeground(new java.awt.Color(0, 0, 0));
        highDrumPress.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        highDrumPress.setText(Localization.t("High drum pressure"));
        highDrumPress.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        highDrumPress.setPreferredSize(new java.awt.Dimension(100, 30));
        highDrumPress.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                highDrumPressActionPerformed(evt);
            }
        });
        annunciatorPanel.add(highDrumPress);

        msv1.setEditable(false);
        msv1.setBackground(new java.awt.Color(142, 0, 0));
        msv1.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        msv1.setForeground(new java.awt.Color(0, 0, 0));
        msv1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        msv1.setText(Localization.t("MSV group I"));
        msv1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        annunciatorPanel.add(msv1);

        nRate.setEditable(false);
        nRate.setBackground(new java.awt.Color(142, 0, 0));
        nRate.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        nRate.setForeground(new java.awt.Color(0, 0, 0));
        nRate.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        nRate.setText(Localization.t("Neuton rate"));
        nRate.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        annunciatorPanel.add(nRate);

        arINOP.setEditable(false);
        arINOP.setBackground(new java.awt.Color(142, 0, 0));
        arINOP.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        arINOP.setForeground(new java.awt.Color(0, 0, 0));
        arINOP.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        arINOP.setText(Localization.t("AR Authority"));
        arINOP.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        annunciatorPanel.add(arINOP);

        lowWaterLevel.setEditable(false);
        lowWaterLevel.setBackground(new java.awt.Color(142, 0, 0));
        lowWaterLevel.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        lowWaterLevel.setForeground(new java.awt.Color(0, 0, 0));
        lowWaterLevel.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        lowWaterLevel.setText(Localization.t("Low water level"));
        lowWaterLevel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        annunciatorPanel.add(lowWaterLevel);

        sdv_aOpen.setEditable(false);
        sdv_aOpen.setBackground(new java.awt.Color(142, 0, 0));
        sdv_aOpen.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        sdv_aOpen.setForeground(new java.awt.Color(0, 0, 0));
        sdv_aOpen.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        sdv_aOpen.setText(Localization.t("SDV-A open"));
        sdv_aOpen.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        annunciatorPanel.add(sdv_aOpen);

        msv2.setEditable(false);
        msv2.setBackground(new java.awt.Color(142, 0, 0));
        msv2.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        msv2.setForeground(new java.awt.Color(0, 0, 0));
        msv2.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        msv2.setText(Localization.t("MSV group II"));
        msv2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        msv2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                msv2ActionPerformed(evt);
            }
        });
        annunciatorPanel.add(msv2);

        t30.setEditable(false);
        t30.setBackground(new java.awt.Color(107, 103, 0));
        t30.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        t30.setForeground(new java.awt.Color(0, 0, 0));
        t30.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        t30.setText(Localization.t("T < 30"));
        t30.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        annunciatorPanel.add(t30);

        lacINOP.setEditable(false);
        lacINOP.setBackground(new java.awt.Color(142, 0, 0));
        lacINOP.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        lacINOP.setForeground(new java.awt.Color(0, 0, 0));
        lacINOP.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        lacINOP.setText(Localization.t("LAC Authority"));
        lacINOP.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        annunciatorPanel.add(lacINOP);

        mcpCavit.setEditable(false);
        mcpCavit.setBackground(new java.awt.Color(142, 0, 0));
        mcpCavit.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        mcpCavit.setForeground(new java.awt.Color(0, 0, 0));
        mcpCavit.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        mcpCavit.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        mcpCavit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mcpCavitActionPerformed(evt);
            }
        });
        annunciatorPanel.add(mcpCavit);

        lowDrumPress.setEditable(false);
        lowDrumPress.setBackground(new java.awt.Color(107, 103, 0));
        lowDrumPress.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        lowDrumPress.setForeground(new java.awt.Color(0, 0, 0));
        lowDrumPress.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        lowDrumPress.setText(Localization.t("Low drum pressure"));
        lowDrumPress.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        annunciatorPanel.add(lowDrumPress);

        msv3.setEditable(false);
        msv3.setBackground(new java.awt.Color(142, 0, 0));
        msv3.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        msv3.setForeground(new java.awt.Color(0, 0, 0));
        msv3.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        msv3.setText(Localization.t("MSV group III"));
        msv3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        annunciatorPanel.add(msv3);

        power.setEditable(false);
        power.setBackground(new java.awt.Color(107, 103, 0));
        power.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        power.setForeground(new java.awt.Color(0, 0, 0));
        power.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        power.setText(Localization.t("High Power"));
        power.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        power.setPreferredSize(new java.awt.Dimension(100, 30));
        power.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                powerActionPerformed(evt);
            }
        });
        annunciatorPanel.add(power);

        jButton1.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        jButton1.setText(Localization.t("ACK"));
        jButton1.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton1.setMaximumSize(new java.awt.Dimension(50, 50));
        jButton1.setMinimumSize(new java.awt.Dimension(50, 50));
        jButton1.setPreferredSize(new java.awt.Dimension(50, 50));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        manualRodControl1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        manualRodControl1.setMinimumSize(new java.awt.Dimension(520, 315));
        manualRodControl1.setPreferredSize(new java.awt.Dimension(520, 315));

        jPanel1.setLayout(new java.awt.GridLayout(2, 3));
        jPanel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(222, 222, 222), 1, true));
        jPanel1.setMinimumSize(new java.awt.Dimension(200, 60));
        jPanel1.setPreferredSize(new java.awt.Dimension(200, 60));

        rodLimit.setEditable(false);
        rodLimit.setBackground(new java.awt.Color(107, 103, 0));
        rodLimit.setFont(new java.awt.Font("Dialog", 1, 8)); // NOI18N
        rodLimit.setForeground(new java.awt.Color(0, 0, 0));
        rodLimit.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        rodLimit.setText(Localization.t("selection limit"));
        rodLimit.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jPanel1.add(rodLimit);

        rodSeq.setEditable(false);
        rodSeq.setBackground(new java.awt.Color(107, 103, 0));
        rodSeq.setFont(new java.awt.Font("Dialog", 1, 8)); // NOI18N
        rodSeq.setForeground(new java.awt.Color(0, 0, 0));
        rodSeq.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        rodSeq.setText(Localization.t("rod sequence"));
        rodSeq.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jPanel1.add(rodSeq);

        rodTravel.setEditable(false);
        rodTravel.setBackground(new java.awt.Color(27, 113, 27));
        rodTravel.setFont(new java.awt.Font("Dialog", 1, 8)); // NOI18N
        rodTravel.setForeground(new java.awt.Color(0, 0, 0));
        rodTravel.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        rodTravel.setText(Localization.t("rod traversal"));
        rodTravel.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jPanel1.add(rodTravel);

        jPanel4.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(222, 222, 222), 1, true));

        jButton3.setBackground(new java.awt.Color(183, 0, 0));
        jButton3.setText(Localization.t("AZ-1K"));
        jButton3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        // 点击行为改由 ProtectedButton 保护盖接管（构造器里包装：需连按两次确认）

        jButton2.setBackground(java.awt.SystemColor.controlDkShadow);
        jButton2.setFont(new java.awt.Font("Dialog", 0, 8)); // NOI18N
        jButton2.setText(Localization.t("Reset AZ"));
        jButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jPanel2.setLayout(new java.awt.GridLayout(2, 3));

        rodLimit1.setEditable(false);
        rodLimit1.setBackground(new java.awt.Color(142, 0, 0));
        rodLimit1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        rodLimit1.setForeground(new java.awt.Color(0, 0, 0));
        rodLimit1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        rodLimit1.setText(Localization.t("AZ-1"));
        rodLimit1.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jPanel2.add(rodLimit1);

        rodLimit2.setEditable(false);
        rodLimit2.setBackground(Annunciator.YELLOWOFF_COLOR);
        rodLimit2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        rodLimit2.setForeground(new java.awt.Color(0, 0, 0));
        rodLimit2.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        rodLimit2.setText(Localization.t("AZ-2"));
        rodLimit2.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jPanel2.add(rodLimit2);

        rodLimit3.setEditable(false);
        rodLimit3.setBackground(new java.awt.Color(142, 0, 0));
        rodLimit3.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        rodLimit3.setForeground(new java.awt.Color(0, 0, 0));
        rodLimit3.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        rodLimit3.setText(Localization.t("AZ-6"));
        rodLimit3.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jPanel2.add(rodLimit3);

        rodLimit10.setEditable(false);
        rodLimit10.setBackground(new java.awt.Color(142, 0, 0));
        rodLimit10.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        rodLimit10.setForeground(new java.awt.Color(0, 0, 0));
        rodLimit10.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        rodLimit10.setText(Localization.t("BAZ"));
        rodLimit10.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jPanel2.add(rodLimit10);

        rodLimit4.setEditable(false);
        rodLimit4.setBackground(Annunciator.ORANGEOFF_COLOR);
        rodLimit4.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        rodLimit4.setForeground(new java.awt.Color(0, 0, 0));
        rodLimit4.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        rodLimit4.setText(Localization.t("AZ-4"));
        rodLimit4.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jPanel2.add(rodLimit4);

        rodLimit5.setEditable(false);
        rodLimit5.setBackground(Annunciator.ORANGEOFF_COLOR);
        rodLimit5.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        rodLimit5.setForeground(new java.awt.Color(0, 0, 0));
        rodLimit5.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        rodLimit5.setText(Localization.t("AZ-3"));
        rodLimit5.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jPanel2.add(rodLimit5);

        jButton4.setBackground(new java.awt.Color(183, 0, 0));
        jButton4.setText(Localization.t("BSM"));
        jButton4.setMargin(new java.awt.Insets(0, 0, 0, 0));
        // 点击行为改由 ProtectedButton 保护盖接管（构造器里包装：需连按两次确认）

        // ---- 手动 AZ 跳闸按键 AZ-2 ~ AZ-5（AZ-5 的保护盖在构造器里包装）----
        azButton2 = azTripButton("AZ-2");
        azButton2.addActionListener(ev -> autoControl.az2Control.trip());
        azButton3 = azTripButton("AZ-3");
        azButton3.addActionListener(ev -> autoControl.az3Control.trip());
        azButton4 = azTripButton("AZ-4");
        azButton4.addActionListener(ev -> autoControl.az4Control.trip());
        azButton5 = azTripButton("AZ-5");
        azButton5.setToolTipText(Localization.t("Manual scram"));
        azButton6 = azTripButton("AZ-6");
        azButton6.addActionListener(ev -> autoControl.az6Control.trip());
        // AZ-2 / AZ-3 合成一行，放在指示灯阵列正下方（利用面板内既有空间，不改变整体尺寸）
        azTripPanel = new javax.swing.JPanel();
        azTripPanel.setOpaque(false);
        azTripPanel.setLayout(new java.awt.GridLayout(1, 3, 2, 2));
        azTripPanel.add(azButton2);
        azTripPanel.add(azButton3);
        azTripPanel.add(azButton6);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap(2, 2)
                // 列1：AZ-1K（保护盖）/ 复位 AZ
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                // 列2：BSM（保护盖）/ AZ-4 / AZ-5（保护盖）—— 利用 BSM 下方原有空槽
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(azButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(azButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 4, 4)
                // 列3：AZ 指示灯阵列（AZ-1/2/5、BAZ、AZ-4/3 六灯）+ 其下 AZ-1~AZ-3 跳闸行
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(azTripPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(2, 2))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap(2, 2)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    // 左二列：行1 = AZ-1K | BSM；行2 = 复位 | （AZ-4 / AZ-5 竖排）
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(azButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 1, 1)
                                .addComponent(azButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    // 右列：指示灯阵列 + AZ-1 ~ AZ-3 跳闸行
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                        .addComponent(azTripPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(222, 222, 222), 1, true));

        rodsOut1.setText(Localization.t("RESET"));
        rodsOut1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rodsOut1ActionPerformed(evt);
            }
        });

        rodsOut.setText(Localization.t("OUT"));
        rodsOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rodsOutActionPerformed(evt);
            }
        });

        rodsIn.setText(Localization.t("IN"));
        rodsIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rodsInActionPerformed(evt);
            }
        });

        // ---- 棒速度：标题 + 两个金属 < > 键 ----
        rodSlower = new javax.swing.JButton("<");
        rodFaster = new javax.swing.JButton(">");
        SteelButtonUI.install(rodSlower, 30, 18);
        SteelButtonUI.install(rodFaster, 30, 18);
        rodSlower.addActionListener(e -> {
            RodSpeed.slower();
            refreshRodSpeedUi();
        });
        rodFaster.addActionListener(e -> {
            RodSpeed.faster();
            refreshRodSpeedUi();
        });

        javax.swing.JPanel rodSpeedArrows = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 2, 0));
        rodSpeedArrows.setOpaque(false);
        rodSpeedArrows.add(rodSlower);
        rodSpeedArrows.add(rodFaster);
        rodSpeedArrows.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        rodSpeedArrows.setPreferredSize(new java.awt.Dimension(70, 18));
        rodSpeedArrows.setMinimumSize(new java.awt.Dimension(70, 18));
        rodSpeedArrows.setMaximumSize(new java.awt.Dimension(70, 18));

        // 标题自绘（放 JLabel 里时中文字形总被裁掉上面一截：CJK 字形高于拉丁 ascent）
        final String speedTitle = Localization.t("Rod Speed");
        rodSpeedPanel = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(Fonts.text("Dialog", java.awt.Font.BOLD, 8));
                java.awt.FontMetrics fm = g2.getFontMetrics();
                g2.setColor(new java.awt.Color(0xD6, 0xD6, 0xD6));   // 深色面板上用浅字
                g2.drawString(speedTitle, (getWidth() - fm.stringWidth(speedTitle)) / 2,
                        3 + fm.getAscent());
                g2.dispose();
            }
        };
        rodSpeedPanel.setLayout(new javax.swing.BoxLayout(rodSpeedPanel, javax.swing.BoxLayout.Y_AXIS));
        rodSpeedPanel.setOpaque(false);
        rodSpeedPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(
                1, 0, 0, 0, new java.awt.Color(120, 120, 120)));
        rodSpeedPanel.add(javax.swing.Box.createVerticalStrut(13));   // 标题占位（自绘）
        rodSpeedPanel.add(rodSpeedArrows);
        rodSpeedPanel.setPreferredSize(new java.awt.Dimension(70, 32));
        rodSpeedPanel.setMinimumSize(new java.awt.Dimension(70, 32));
        rodSpeedPanel.setMaximumSize(new java.awt.Dimension(70, 32));
        refreshRodSpeedUi();

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap(2, 2)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rodsOut1, javax.swing.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE)
                    .addComponent(rodsOut, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rodsIn, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rodSpeedPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(2, 2))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap(2, 2)
                .addComponent(rodsOut1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, 34)   // 原 50：给棒速度块腾地方
                .addComponent(rodsOut, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rodsIn, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 6, 6)
                .addComponent(rodSpeedPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(222, 222, 222), 1, true));

        keff.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        keff.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        keff.setLcdDecimals(4);
        keff.setLcdUnitString(Localization.t(""));
        keff.setLcdUnitStringVisible(false);
        keff.setMinimumSize(new java.awt.Dimension(94, 23));
        keff.setSize(new java.awt.Dimension(94, 23));

        jLabel5.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        jLabel5.setText(Localization.t("ρ"));

        jLabel6.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        jLabel6.setText(Localization.t("T"));

        period.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        period.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        period.setLcdDecimals(0);
        period.setLcdMaxValue(9999.0);
        period.setLcdMinValue(-9999.0);
        period.setLcdUnitString(Localization.t(""));
        period.setLcdUnitStringVisible(false);
        period.setLcdValue(999.0);
        period.setMinimumSize(new java.awt.Dimension(94, 23));
        period.setSize(new java.awt.Dimension(94, 23));

        neutronFlux.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        neutronFlux.setCustomLcdUnitFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        neutronFlux.setFont(new java.awt.Font("Dialog", 0, 5)); // NOI18N
        neutronFlux.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        neutronFlux.setLcdDecimals(4);
        neutronFlux.setLcdScientificFormat(true);
        neutronFlux.setLcdUnitString(Localization.t(""));
        neutronFlux.setLcdUnitStringVisible(false);
        neutronFlux.setLcdValue(1.43E-14);
        neutronFlux.setMinimumSize(new java.awt.Dimension(94, 23));
        neutronFlux.setSize(new java.awt.Dimension(94, 23));

        jLabel7.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        jLabel7.setText(Localization.t("N"));

        deltaKeff.setBackgroundColor(eu.hansolo.steelseries.tools.BackgroundColor.SATIN_GRAY);
        deltaKeff.setFrameVisible(false);
        deltaKeff.setLcdValueFont(new java.awt.Font("Verdana", 0, 36)); // NOI18N
        deltaKeff.setLcdVisible(false);
        deltaKeff.setLedVisible(false);
        deltaKeff.setMaxNoOfMajorTicks(5);
        deltaKeff.setMaxNoOfMinorTicks(5);
        deltaKeff.setMaxValue(10.0);
        deltaKeff.setMinValue(-10.0);
        deltaKeff.setTitle(Localization.t("%ΔK/K"));
        deltaKeff.setTitleAndUnitFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        deltaKeff.setTrackSectionColor(new java.awt.Color(255, 0, 0));
        deltaKeff.setTrackStart(800.0);
        deltaKeff.setTrackStartColor(new java.awt.Color(255, 0, 0));
        deltaKeff.setUnitString("kg/s");
        deltaKeff.setUnitStringVisible(false);
        deltaKeff.setValueColor(eu.hansolo.steelseries.tools.ColorDef.YELLOW);

        jLabel3.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        jLabel3.setText(Localization.t("Reactivity"));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap(2, 2)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(neutronFlux, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel5))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(period, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(keff, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(deltaKeff, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 4, Short.MAX_VALUE)))
                .addContainerGap(2, 2))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(period, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel6)
                        .addComponent(jLabel3)))
                .addGap(11, 11, 11)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(keff, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(11, 11, 11)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(neutronFlux, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(deltaKeff, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(2, 2))
        );

        electric2.setBackgroundColor(eu.hansolo.steelseries.tools.BackgroundColor.BEIGE);
        electric2.setBackgroundVisible(false);
        electric2.setFrameDesign(eu.hansolo.steelseries.tools.FrameDesign.BLACK_METAL);
        electric2.setFrameVisible(false);
        electric2.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.STANDARD_LCD);
        electric2.setLcdUnitString(Localization.t("MWe"));
        electric2.setLcdUnitStringVisible(true);
        electric2.setMaxValue(1000.0);
        electric2.setSize(new java.awt.Dimension(170, 90));

        jPanel7.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(222, 222, 222), 1, true));

        jLabel9.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        jLabel9.setText(Localization.t("Setpoint"));

        setpoint.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        setpoint.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        setpoint.setLcdDecimals(0);
        setpoint.setLcdMaxValue(5200.0);
        setpoint.setLcdUnitString(Localization.t(""));
        setpoint.setLcdUnitStringVisible(false);
        setpoint.setMinimumSize(new java.awt.Dimension(94, 23));
        setpoint.setSize(new java.awt.Dimension(94, 23));

        jLabel4.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N
        jLabel4.setText(Localization.t("Auto-Control"));

        toggleLAC.setBackground(Annunciator.BLUEOFF_COLOR);
        toggleLAC.setText(Localization.t("LAC"));
        toggleLAC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleLACActionPerformed(evt);
            }
        });

        jPanel8.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(222, 222, 222), 1, true));

        toggleAR2.setBackground(Annunciator.BLUEOFF_COLOR);
        toggleAR2.setText(Localization.t("2AR"));
        toggleAR2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleAR2ActionPerformed(evt);
            }
        });

        toggleAR1.setBackground(Annunciator.BLUEOFF_COLOR);
        toggleAR1.setText(Localization.t("1AR"));
        toggleAR1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleAR1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(toggleAR2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE)
                    .addComponent(toggleAR1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(2, 2))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap(2, 2)
                .addComponent(toggleAR1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                .addComponent(toggleAR2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        precisionIncrement2.setText(Localization.t("TGUI.precisionIncrement2.text")); // NOI18N
        precisionIncrement2.setMargin(new java.awt.Insets(2, 2, 2, 2));
        precisionIncrement2.setMaximumSize(new java.awt.Dimension(30, 30));
        precisionIncrement2.setMinimumSize(new java.awt.Dimension(25, 25));
        precisionIncrement2.setPreferredSize(new java.awt.Dimension(30, 30));
        precisionIncrement2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                precisionIncrement2ActionPerformed(evt);
            }
        });

        precisionDecrement2.setText(Localization.t("TGUI.precisionDecrement2.text")); // NOI18N
        precisionDecrement2.setMargin(new java.awt.Insets(2, 2, 2, 2));
        precisionDecrement2.setMaximumSize(new java.awt.Dimension(30, 30));
        precisionDecrement2.setMinimumSize(new java.awt.Dimension(25, 25));
        precisionDecrement2.setPreferredSize(new java.awt.Dimension(30, 30));
        precisionDecrement2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                precisionDecrement2ActionPerformed(evt);
            }
        });

        jPanel10.setLayout(new java.awt.GridLayout(3, 3));

        busy1AR.setEditable(false);
        busy1AR.setBackground(new java.awt.Color(27, 113, 27));
        busy1AR.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        busy1AR.setForeground(new java.awt.Color(0, 0, 0));
        busy1AR.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        busy1AR.setText(Localization.t("1AR"));
        busy1AR.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jPanel10.add(busy1AR);

        busy2AR.setEditable(false);
        busy2AR.setBackground(new java.awt.Color(27, 113, 27));
        busy2AR.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        busy2AR.setForeground(new java.awt.Color(0, 0, 0));
        busy2AR.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        busy2AR.setText(Localization.t("2AR"));
        busy2AR.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jPanel10.add(busy2AR);

        busyLAC.setEditable(false);
        busyLAC.setBackground(new java.awt.Color(27, 113, 27));
        busyLAC.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        busyLAC.setForeground(new java.awt.Color(0, 0, 0));
        busyLAC.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        busyLAC.setText(Localization.t("LAC"));
        busyLAC.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jPanel10.add(busyLAC);

        limit1AR.setEditable(false);
        limit1AR.setBackground(new java.awt.Color(107, 103, 0));
        limit1AR.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        limit1AR.setForeground(new java.awt.Color(0, 0, 0));
        limit1AR.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        limit1AR.setText(Localization.t("limit"));
        limit1AR.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jPanel10.add(limit1AR);

        limit2AR.setEditable(false);
        limit2AR.setBackground(new java.awt.Color(107, 103, 0));
        limit2AR.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        limit2AR.setForeground(new java.awt.Color(0, 0, 0));
        limit2AR.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        limit2AR.setText(Localization.t("limit"));
        limit2AR.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jPanel10.add(limit2AR);

        limitLAC.setEditable(false);
        limitLAC.setBackground(new java.awt.Color(107, 103, 0));
        limitLAC.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        limitLAC.setForeground(new java.awt.Color(0, 0, 0));
        limitLAC.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        limitLAC.setText(Localization.t("limit"));
        limitLAC.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jPanel10.add(limitLAC);

        error1AR.setEditable(false);
        error1AR.setBackground(new java.awt.Color(107, 103, 0));
        error1AR.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        error1AR.setForeground(new java.awt.Color(0, 0, 0));
        error1AR.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        error1AR.setText(Localization.t("error"));
        error1AR.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jPanel10.add(error1AR);

        error2AR.setEditable(false);
        error2AR.setBackground(new java.awt.Color(107, 103, 0));
        error2AR.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        error2AR.setForeground(new java.awt.Color(0, 0, 0));
        error2AR.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        error2AR.setText(Localization.t("error"));
        error2AR.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jPanel10.add(error2AR);

        errorLAC.setEditable(false);
        errorLAC.setBackground(new java.awt.Color(107, 103, 0));
        errorLAC.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        errorLAC.setForeground(new java.awt.Color(0, 0, 0));
        errorLAC.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        errorLAC.setText(Localization.t("error"));
        errorLAC.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jPanel10.add(errorLAC);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap(2, 2)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel7Layout.createSequentialGroup()
                            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(toggleLAC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel9)
                                .addGroup(jPanel7Layout.createSequentialGroup()
                                    .addComponent(setpoint, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                                    .addComponent(precisionIncrement2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                                    .addComponent(precisionDecrement2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addComponent(jLabel4))
                    .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addContainerGap(2, 2)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel7Layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                                        .addComponent(jLabel9))
                                    .addGroup(jPanel7Layout.createSequentialGroup()
                                        .addGap(23, 23, 23)
                                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(precisionIncrement2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(precisionDecrement2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                            .addComponent(setpoint, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addComponent(jLabel4)
                        .addGap(10, 10, 10)
                        .addComponent(toggleLAC)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        thermalPower1.setFrameDesign(eu.hansolo.steelseries.tools.FrameDesign.BLACK_METAL);
        thermalPower1.setFrameEffect(eu.hansolo.steelseries.tools.FrameEffect.EFFECT_BULGE);
        thermalPower1.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.BLACKRED_LCD);
        thermalPower1.setLcdUnitString(Localization.t("MWt"));
        thermalPower1.setLcdUnitStringVisible(true);
        thermalPower1.setLcdValueFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 32));
        thermalPower1.setLcdUnitFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 14));
        thermalPower1.setPreferredSize(new java.awt.Dimension(260, 110));
        thermalPower1.setMinimumSize(new java.awt.Dimension(260, 110));
        thermalPower1.setMaximumSize(new java.awt.Dimension(260, 110));
        thermalPower1.setSize(new java.awt.Dimension(260, 110));

        electric1.setBackgroundColor(eu.hansolo.steelseries.tools.BackgroundColor.BEIGE);
        electric1.setBackgroundVisible(false);
        electric1.setFrameDesign(eu.hansolo.steelseries.tools.FrameDesign.BLACK_METAL);
        electric1.setFrameVisible(false);
        electric1.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.STANDARD_LCD);
        electric1.setLcdUnitString(Localization.t("MWe"));
        electric1.setLcdUnitStringVisible(true);
        electric1.setMaxValue(1000.0);
        electric1.setSize(new java.awt.Dimension(170, 90));

        mTK1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        // 2026-10-18：堆芯图放大（50x50 格子 -> 430/50 ≈ 8.6 px）。
        // 原上限 300x320 把 350 的布局尺寸钳成 300x320，格子只有 6 px。
        mTK1.setMaximumSize(new java.awt.Dimension(470, 520));
        mTK1.setMinimumSize(new java.awt.Dimension(300, 260));
        mTK1.setPreferredSize(new java.awt.Dimension(430, 430));

        jPanel9.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(222, 222, 222), 1, true));

        // 自动AZ 控制（深色面板 + ON/OFF 互斥开关，参照泵开关风格）
        autoAzPanel.setBackground(new java.awt.Color(52, 52, 52));
        autoAzPanel.setOpaque(true);
        autoAzPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 222, 222), 1, true),
                Localization.t("Auto AZ")));
        javax.swing.ButtonGroup autoAzGroup = new javax.swing.ButtonGroup();
        autoAzGroup.add(autoAzOn);
        autoAzGroup.add(autoAzOff);
        org.openide.awt.Mnemonics.setLocalizedText(autoAzOn, Localization.t("ON"));
        org.openide.awt.Mnemonics.setLocalizedText(autoAzOff, Localization.t("OFF"));
        // 按仿真对象的实际开关初始化（读档时 azAutoEnabled 可能为 false），
        // 否则界面会先显示 ON、再由下面的刷新线程纠正，出现一次闪烁。
        autoAzOn.setSelected(autoControl.azAutoEnabled);
        autoAzOff.setSelected(!autoControl.azAutoEnabled);
        for (javax.swing.JRadioButton b : new javax.swing.JRadioButton[]{autoAzOn, autoAzOff}) {
            b.setOpaque(false);
            b.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 11));
            b.setForeground(new java.awt.Color(235, 235, 235));
        }
        autoAzPanel.setLayout(new java.awt.GridLayout(1, 2));
        autoAzPanel.add(autoAzOn);
        autoAzPanel.add(autoAzOff);
        autoAzOn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoControl.azAutoEnabled = true;
                autoControl.recordEvent(Localization.t("Auto AZ On"));
            }
        });
        autoAzOff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoControl.azAutoEnabled = false;
                autoControl.recordEvent(Localization.t("Auto AZ Off"));
            }
        });

        // 控制棒数量限制（与自动AZ 同款 UI：深色面板 + ON/OFF 互斥开关）
        // ON = 选中棒数超过限值（>4 根）时禁止整组拔出；OFF = 不限制
        rodLimitPanel.setBackground(new java.awt.Color(52, 52, 52));
        rodLimitPanel.setOpaque(true);
        rodLimitPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 222, 222), 1, true),
                Localization.t("Rod Count Limit")));
        javax.swing.ButtonGroup rodLimitGroup = new javax.swing.ButtonGroup();
        rodLimitGroup.add(rodLimitOn);
        rodLimitGroup.add(rodLimitOff);
        org.openide.awt.Mnemonics.setLocalizedText(rodLimitOn, Localization.t("ON"));
        org.openide.awt.Mnemonics.setLocalizedText(rodLimitOff, Localization.t("OFF"));
        rodSelectionLimitEnabled = SettingsDialog.getRodSelectionLimitEnabled();
        rodLimitOn.setSelected(rodSelectionLimitEnabled);
        rodLimitOff.setSelected(!rodSelectionLimitEnabled);
        for (javax.swing.JRadioButton rb : new javax.swing.JRadioButton[]{rodLimitOn, rodLimitOff}) {
            rb.setOpaque(false);
            rb.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 11));
            rb.setForeground(new java.awt.Color(235, 235, 235));
        }
        rodLimitPanel.setLayout(new java.awt.GridLayout(1, 2));
        rodLimitPanel.add(rodLimitOn);
        rodLimitPanel.add(rodLimitOff);
        rodLimitOn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rodSelectionLimitEnabled = true;
                SettingsDialog.setRodSelectionLimitEnabled(true);
                autoControl.recordEvent(Localization.t("Rod Count Limit On"));
            }
        });
        rodLimitOff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rodSelectionLimitEnabled = false;
                SettingsDialog.setRodSelectionLimitEnabled(false);
                autoControl.recordEvent(Localization.t("Rod Count Limit Off"));
            }
        });
        jPanel9.setMaximumSize(new java.awt.Dimension(9999, 9999));
        jPanel9.setMinimumSize(new java.awt.Dimension(30, 30));
        jPanel9.setName(""); // NOI18N
        jPanel9.setPreferredSize(new java.awt.Dimension(330, 235));

        drumTemp.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        drumTemp.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        drumTemp.setLcdUnitString(Localization.t("°C"));
        drumTemp.setLcdValue(20.0);
        drumTemp.setMaximumSize(new java.awt.Dimension(94, 32));
        drumTemp.setPreferredSize(new java.awt.Dimension(94, 32));

        drumTemp1.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        drumTemp1.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        drumTemp1.setLcdUnitString(Localization.t("°C"));
        drumTemp1.setLcdValue(20.0);
        drumTemp1.setMaximumSize(new java.awt.Dimension(94, 32));
        drumTemp1.setPreferredSize(new java.awt.Dimension(94, 32));

        jLabel10.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        jLabel10.setText(Localization.t("Parameters Loop 1"));

        jLabel11.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        jLabel11.setText(Localization.t("Parameters Loop 2"));

        jLabel49.setText(Localization.t("Drum Press"));

        jLabel50.setText(Localization.t("Drum Temp"));

        suctionTemp.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        suctionTemp.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        suctionTemp.setLcdUnitString(Localization.t("°C"));
        suctionTemp.setLcdValue(20.0);
        suctionTemp.setMaximumSize(new java.awt.Dimension(94, 32));
        suctionTemp.setPreferredSize(new java.awt.Dimension(94, 32));

        jLabel51.setText(Localization.t("Suction Temp"));

        suctionTemp1.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        suctionTemp1.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        suctionTemp1.setLcdUnitString(Localization.t("°C"));
        suctionTemp1.setLcdValue(20.0);
        suctionTemp1.setMaximumSize(new java.awt.Dimension(94, 32));
        suctionTemp1.setPreferredSize(new java.awt.Dimension(94, 32));

        jLabel52.setText(Localization.t("Suction Temp"));

        press1.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        press1.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        press1.setLcdDecimals(2);
        press1.setLcdMaxValue(10.0);
        press1.setLcdUnitString(Localization.t("MPa"));
        press1.setLcdValue(0.10142);
        press1.setMaximumSize(new java.awt.Dimension(94, 32));
        press1.setPreferredSize(new java.awt.Dimension(94, 32));

        press2.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        press2.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        press2.setLcdDecimals(2);
        press2.setLcdMaxValue(10.0);
        press2.setLcdUnitString(Localization.t("MPa"));
        press2.setLcdValue(0.10142);
        press2.setMaximumSize(new java.awt.Dimension(94, 32));
        press2.setPreferredSize(new java.awt.Dimension(94, 32));

        jLabel53.setText(Localization.t("Drum Press"));

        jLabel54.setText(Localization.t("Drum Temp"));

        headerTemp1.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        headerTemp1.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        headerTemp1.setLcdUnitString(Localization.t("°C"));
        headerTemp1.setLcdValue(20.0);
        headerTemp1.setMaximumSize(new java.awt.Dimension(94, 32));
        headerTemp1.setPreferredSize(new java.awt.Dimension(94, 32));

        jLabel55.setText(Localization.t("Header Temp"));

        headerTemp.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        headerTemp.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        headerTemp.setLcdUnitString(Localization.t("°C"));
        headerTemp.setLcdValue(20.0);
        headerTemp.setMaximumSize(new java.awt.Dimension(94, 32));
        headerTemp.setPreferredSize(new java.awt.Dimension(94, 32));

        jLabel56.setText(Localization.t("Header Temp"));

        headerFlow1.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        headerFlow1.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        headerFlow1.setLcdUnitString(Localization.t("kg/s"));
        headerFlow1.setLcdValue(20.0);
        headerFlow1.setMaximumSize(new java.awt.Dimension(94, 32));
        headerFlow1.setPreferredSize(new java.awt.Dimension(94, 32));

        jLabel57.setText(Localization.t("Header Flow"));

        headerFlow.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        headerFlow.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        headerFlow.setLcdUnitString(Localization.t("kg/s"));
        headerFlow.setLcdValue(20.0);
        headerFlow.setMaximumSize(new java.awt.Dimension(94, 32));
        headerFlow.setPreferredSize(new java.awt.Dimension(94, 32));

        jLabel58.setText(Localization.t("Header Flow"));

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap(2, 2)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel51)
                            .addComponent(jLabel49)
                            .addComponent(jLabel50)
                            .addComponent(jLabel56)
                            .addComponent(jLabel58))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(suctionTemp, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(drumTemp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(press1, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(headerTemp, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(headerFlow, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(30, 30, 30)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel52)
                            .addComponent(jLabel53, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel54, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel55, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel57, javax.swing.GroupLayout.Alignment.LEADING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(drumTemp1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(suctionTemp1, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(press2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(headerTemp1, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(headerFlow1, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11))
                .addGap(18, 18, 18)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(press1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel49, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel50, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(drumTemp, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel54, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(suctionTemp, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel51, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(headerTemp, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel56, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel55, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(headerFlow, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel58, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(press2, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel53, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                        .addComponent(drumTemp1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(suctionTemp1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel52, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                        .addComponent(headerTemp1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(headerFlow1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel57, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        offlineCoolingPanel = new OfflineCoolingPanel();   // 核心离线冷却（自动控制区下方）

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3GroupLayout = jPanel3Layout;   // 保留引用：外部布局编辑器切换/恢复时使用
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(2, 2)
                // 左区（=列1+列2）按行组织：
                //   行1 报警阵列+ACK 通栏；行2 = AZ面板+棒限值 | 反应性；行3 = 按钮列 | 控制棒网格
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    // 行1：报警阵列+ACK（通栏横跨列1+列2）
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(annunciatorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 570, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                    // 行2：AZ 面板+棒限值块 | 反应性
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE))
                    // 行3：按钮列 | 控制棒网格（放大并左移：紧贴按钮列，整幅 520x315，按钮更好点击）
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                        .addComponent(manualRodControl1, javax.swing.GroupLayout.PREFERRED_SIZE, 520, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                // 列3（跨3行）：MWt+MWe / 自动控制 / 离线冷却 / 自动AZ 开关
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(thermalPower1, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(electric1, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                        .addComponent(electric2, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(offlineCoolingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(autoAzPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                        .addComponent(rodLimitPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                // 列4：堆芯图（行1-2）/ 回路参数（行3）
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(mTK1, javax.swing.GroupLayout.PREFERRED_SIZE, 430, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, 430, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(2, Short.MAX_VALUE))
        );;
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(1, 1)
                // 行1+行2 与堆芯图并排（堆芯图跨两行，参照目标截图右上角）
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mTK1, javax.swing.GroupLayout.PREFERRED_SIZE, 430, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        // 行1：报警阵列+ACK / MWt+MWe（MWt 在自动控制列上方）
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(annunciatorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(thermalPower1, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(electric1, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(electric2, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 1, 1)
                        // 行2：AZ 面板 / 棒限值块（在 AZ 下方，参照目标截图）/ 反应性 / 自动控制
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 1, 1)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 1, 1)
                // 行3：按钮列 / 控制棒网格（min/pref=300 保证不出滚动条；max=420 让多余空间全部灌入，
                //      面板自动顶满到窗口底部）
                //  ★ 此组必须 resizable=true：否则"不可缩放"会把组的 max 钳制为首选值，
                //        多余空间传不进来，控制棒面板就长不大
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, true)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(manualRodControl1, javax.swing.GroupLayout.PREFERRED_SIZE, 300, 420)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(offlineCoolingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 2, 2)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(autoAzPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(rodLimitPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(1, 1))
        );;

        jScrollPane1.setViewportView(jPanel3);

        jMenu1.setText(Localization.t("File"));

        jMenuItem2.setText(Localization.t("Save IC"));
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuItem11.setText(Localization.t("Exit to Menu"));
        jMenuItem11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem11ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem11);

        jMenuBar1.add(jMenu1);

        jMenu2.setText(Localization.t("Window"));

        jMenuItem3.setText(Localization.t("Core Map"));
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem3);

        jMenuItem10.setText(Localization.t("Selsyns"));
        jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem10ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem10);

        jMenuItem12.setText(Localization.t("MCP"));
        jMenuItem12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem12ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem12);

        jMenuItem5.setText(Localization.t("Turbine-Generators"));
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem5);

        jMenuItem6.setText(Localization.t("Condensate"));
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem6);

        jMenuItem7.setText(Localization.t("Dearators"));
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem7);

        jMenuItem8.setText(Localization.t("Feedwater"));
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem8);

        /* 2026-10-25：换热器 / 汽水分离器两个窗口已删除（按需求去掉这两个选项）。
         * 菜单项仍由 .form 生成，这里直接隐藏并在监听里留空，
         * 避免改 NetBeans 的 .form 造成生成代码错位。 */
        jMenuItem13.setVisible(false);
        jMenuItem14.setVisible(false);

        // 蒸汽环路 / 全厂能量平衡（换热器→分离器→除氧器→汽轮机→凝汽器→PCS 的读数汇总）
        javax.swing.JMenuItem steamLoopItem = new javax.swing.JMenuItem(Localization.t("Steam Loop"));
        steamLoopItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createOrContinue(SteamLoopUI.class, false, false);
            }
        });
        jMenu2.add(steamLoopItem);

        jMenuItem9.setText(Localization.t("TGUI.jMenuItem4.text")); // NOI18N
        jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem9ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem9);

        jMenuBar1.add(jMenu2);

        jMenu3.setText(Localization.t("Settings"));

        pause.setText(Localization.t("Paused"));
        pause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseActionPerformed(evt);
            }
        });
        jMenu3.add(pause);


        // 新增：打开设置对话框（与主菜单窗口的 Settings 按钮同一入口）
        javax.swing.JMenuItem alarmSoundItem = new javax.swing.JMenuItem(Localization.t("settings.title") + "...");
        alarmSoundItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                new SettingsDialog(UI.this).setVisible(true);
            }
        });
        jMenu3.add(alarmSoundItem);

        // ============ 工具（预留 6 个位置） ============
        // 1 = 主窗口布局编辑器（Python 脚本）；2 = 布局编辑器 jar 模组；3~6 = 预留
        javax.swing.JMenu toolsMenu = new javax.swing.JMenu(Localization.t("Tools"));
        {
            javax.swing.JMenuItem layoutEditorItem = new javax.swing.JMenuItem(Localization.t("Layout Editor"));
            layoutEditorItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    launchLayoutEditor();
                }
            });
            toolsMenu.add(layoutEditorItem);

            javax.swing.JMenuItem layoutEditorJarItem =
                    new javax.swing.JMenuItem(Localization.t("Layout Editor") + " (jar)");
            layoutEditorJarItem.setToolTipText("RBMK-LayoutEditor.jar — 任意窗口/任意层级的组件布局编辑器");
            layoutEditorJarItem.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    launchLayoutEditorJar();
                }
            });
            toolsMenu.add(layoutEditorJarItem);

            for (int i = 0; i < 4; i++) {   // 预留 4 个占位（共 6 个工具位）
                javax.swing.JMenuItem reserved =
                        new javax.swing.JMenuItem(Localization.t("Reserved") + " " + (i + 3));
                reserved.setEnabled(false);
                toolsMenu.add(reserved);
            }
        }
        jMenu3.add(toolsMenu);


        jMenuBar1.add(jMenu3);


        jMenu8.setText(Localization.t("Data"));

        jMenuItem1.setText(Localization.t("Log"));
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu8.add(jMenuItem1);



        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1409, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 797, Short.MAX_VALUE)
        );

        // ---- 紧凑化：去掉滚动区外框留白，各分块统一 1px 描边 ----
        // 间距本身在各处 GroupLayout 里用显式 2px 的 addPreferredGap/addContainerGap 表达
        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        jScrollPane1.setViewportBorder(null);
        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        for (javax.swing.JPanel p : new javax.swing.JPanel[]{jPanel4, jPanel5, jPanel6, jPanel7, jPanel9}) {
            p.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(222, 222, 222), 1));
        }
        annunciatorPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder());

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        createOrContinue(SaveDialog.class, false, false);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        createOrContinue(TGUI.class, true, false);
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        createOrContinue(CondensateUI.class, true, false);
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        createOrContinue(DearatorUI.class, true, false);
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        createOrContinue(FeedwaterUI.class, true, false);
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void jMenuItem13ActionPerformed(java.awt.event.ActionEvent evt) {
        // 换热器窗口已移除（2026-10-25）
    }

    /**
     * 按当前单位显示一个量：数值换算 + 单位字符串跟着变。
     * 设置里改了单位后，下一帧就按新单位显示。
     */
    private static void showUnit(eu.hansolo.steelseries.gauges.Lcd lcd, Units.Quantity q, double si) {
        lcd.setLcdValue(Units.convert(q, si));
        lcd.setLcdUnitString(Units.unitOf(q));
        lcd.setLcdUnitStringVisible(true);
    }

    /** 功率专用（MWt / MWe 等带后缀） */
    private static void showPower(eu.hansolo.steelseries.gauges.Lcd lcd, double si, boolean thermal) {
        lcd.setLcdValue(Units.convert(Units.Quantity.POWER, si));
        lcd.setLcdUnitString(Units.powerLabel(thermal));
        lcd.setLcdUnitStringVisible(true);
    }

    private void jMenuItem14ActionPerformed(java.awt.event.ActionEvent evt) {
        // 汽水分离器窗口已移除（2026-10-25）
    }

    private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem9ActionPerformed
        UI.createOrContinue(PCSUI.class, true, false);
    }//GEN-LAST:event_jMenuItem9ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        annunciator.acknowledge();
        for (UIUpdateable window: elementsToUpdate) {
            window.acknowledge();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void powerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_powerActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_powerActionPerformed

    private void mcpCavitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mcpCavitActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_mcpCavitActionPerformed

    private void msv2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_msv2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_msv2ActionPerformed

    private void highDrumPressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_highDrumPressActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_highDrumPressActionPerformed

    private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem10ActionPerformed
        createOrContinue(SelsynPanel.class, false, false);
    }//GEN-LAST:event_jMenuItem10ActionPerformed

    private void rodsOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rodsOutActionPerformed

    }//GEN-LAST:event_rodsOutActionPerformed

    private void rodsInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rodsInActionPerformed

    }//GEN-LAST:event_rodsInActionPerformed

    /**
     * 棒速度块的显示刷新：按当前档位更新提示语和两端禁用状态。
     * 档位 0 = 最慢（17.5 s 全行程，与原版一致），最高档 = 4.0 s（AZ 棒约 4 秒全拔出）。
     */
    /**
     * 把"棒速度"块钉在"插入"下面。
     *
     * <p>主窗口的方块运行时会被 {@link PageLayout} 换成绝对布局（layout=null），
     * 所以 GroupLayout 只提供"出厂默认值"，实际位置要靠这里每帧对齐 ——
     * 这样在布局编辑器里挪动"插入"时，棒速度块也会跟着走，并且永远留在方块内。</p>
     */
    void syncRodSpeedPanelBounds() {
        if (jPanel5 == null || rodsIn == null || rodSpeedPanel == null) {
            return;
        }
        java.awt.Rectangle r = rodsIn.getBounds();
        if (r.width <= 0) {
            return;
        }
        int gap = 1;
        int h = 32;
        int maxBottom = jPanel5.getHeight() - 2;
        if (r.y + r.height + gap + h > maxBottom) {
            h = Math.max(24, maxBottom - (r.y + r.height + gap));   // 空间不够就压扁，绝不越界
        }
        int y = r.y + r.height + gap;
        java.awt.Rectangle want = new java.awt.Rectangle(r.x, y, r.width, h);
        if (!want.equals(rodSpeedPanel.getBounds())) {
            rodSpeedPanel.setBounds(want);
        }
    }

    /** 仅供自检/截图用：拿到控制棒按钮块 */
    javax.swing.JPanel getRodPanelForShot() {
        return jPanel5;
    }

    private void refreshRodSpeedUi() {
        if (rodSlower == null || rodFaster == null || rodSpeedPanel == null) {
            return;
        }
        rodSlower.setEnabled(RodSpeed.level() > 0);
        rodFaster.setEnabled(RodSpeed.level() < RodSpeed.levels() - 1);
        String tip = Localization.t("Rod Speed") + " " + RodSpeed.label()
                + "  (" + (RodSpeed.level() + 1) + "/" + RodSpeed.levels() + ")";
        rodSpeedPanel.setToolTipText(tip);
        rodSlower.setToolTipText(Localization.t("Rod Speed") + " ↓ " + RodSpeed.label());
        rodFaster.setToolTipText(Localization.t("Rod Speed") + " ↑ " + RodSpeed.label());
        rodSpeedPanel.repaint();
    }

    private void rodsOut1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rodsOut1ActionPerformed
        manualRodControl1.resetSelection();
    }//GEN-LAST:event_rodsOut1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        autoControl.az1Control.trip("AZ-1K Pressed");
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // 复位全部 AZ 保护（AZ-1K / BAZ / AZ-2~AZ-5）：跳闸状态与警声一起立即清除
        autoControl.resetAllAz();
        // 联动复位所有保护盖（AZ-1K / BSM / AZ-5）
        if (guardAZ1K != null) {
            guardAZ1K.resetCover();
        }
        if (guardBSM != null) {
            guardBSM.resetCover();
        }
        if (guardAZ5 != null) {
            guardAZ5.resetCover();
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        if (Loader.loader != null) {
            Loader.loader.setLoading(false);
        }
        if (layoutWatchTimer != null) {
            layoutWatchTimer.stop();   // 关闭本窗口时停止布局文件轮询
        }
        NPPSim.endSimulation();
        elementsToUpdate.forEach(elem -> {
            elem.discard();
        });
        elementsToUpdate.clear();
        if (Loader.loader != null) {
            Loader.loader.setVisible(true);
        }
        // 回到主菜单期间，利用空闲的 EDT 为下一局补充堆芯图组件池（分批进行，不卡界面）
        MTK.requestPreload();
    }//GEN-LAST:event_formWindowClosed

    /** 创建 AZ 手动跳闸按键（统一样式：暗红底、白字、居中、无外边距） */
    private static javax.swing.JButton azTripButton(String labelKey) {
        javax.swing.JButton b = new javax.swing.JButton();
        b.setBackground(new java.awt.Color(183, 0, 0));
        b.setForeground(java.awt.Color.WHITE);
        b.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        b.setText(Localization.t(labelKey));
        b.setMargin(new java.awt.Insets(0, 0, 0, 0));
        b.setFocusable(false);
        return b;
    }

    /* ================= 外部布局编辑器（ui_layout.json）实现 ================= */

    /** 建立 key → 组件 注册表（key 与 Python 工具 layout_editor.py 的 BLOCKS 一一对应） */
    private void buildLayoutBlockMap() {
        layoutBlockMap = new java.util.LinkedHashMap<>();
        layoutBlockMap.put("annunciator", annunciatorPanel);
        layoutBlockMap.put("ack", jButton1);
        layoutBlockMap.put("mwt", thermalPower1);
        layoutBlockMap.put("mwe1", electric1);
        layoutBlockMap.put("mwe2", electric2);
        layoutBlockMap.put("coremap", mTK1);
        layoutBlockMap.put("az_panel", jPanel4);
        layoutBlockMap.put("rod_limit", jPanel1);
        layoutBlockMap.put("reactivity", jPanel6);
        layoutBlockMap.put("autocontrol", jPanel7);
        layoutBlockMap.put("rod_buttons", jPanel5);
        layoutBlockMap.put("rod_grid", manualRodControl1);
        layoutBlockMap.put("offline_cooling", offlineCoolingPanel);
        layoutBlockMap.put("auto_az", autoAzPanel);
        layoutBlockMap.put("rod_limit_panel", rodLimitPanel);
        layoutBlockMap.put("loop_params", jPanel9);
    }

    /** 启动时调用一次：记录默认坐标 → 应用已有布局文件 → 启动每秒轮询 */
    private void initLayoutSync() {
        buildLayoutBlockMap();
        // 先让 GroupLayout 完整跑一遍，记录各面板的默认坐标（同时作为绝对布局的兜底）
        jPanel3.setSize(jPanel3.getPreferredSize());
        jPanel3.doLayout();
        layoutFallbackBounds = new java.util.LinkedHashMap<>();
        for (java.util.Map.Entry<String, javax.swing.JComponent> e : layoutBlockMap.entrySet()) {
            java.awt.Component c = e.getValue();
            layoutFallbackBounds.put(e.getKey(),
                    new java.awt.Rectangle(c.getX(), c.getY(), c.getWidth(), c.getHeight()));
            System.out.println(String.format("[Layout] 默认坐标 %s=%d,%d,%d,%d",
                    e.getKey(), c.getX(), c.getY(), c.getWidth(), c.getHeight()));
        }
        syncLayoutFile();
        layoutWatchTimer = new javax.swing.Timer(1000, ev -> syncLayoutFile());
        layoutWatchTimer.start();
        // 启动约 2.5 秒后（首次布局完成）导出子组件清单，供布局编辑器展开"小方块"
        javax.swing.Timer childrenExportTimer = new javax.swing.Timer(2500, ev -> {
            exportChildrenFile();
            ((javax.swing.Timer) ev.getSource()).stop();
        });
        childrenExportTimer.setRepeats(false);
        childrenExportTimer.start();
        // 把主窗口的方块也登记进页面清单（布局仍由本类自己管，PageLayout 只负责导出清单）
        PageLayout.registerExternal(this, "main", Localization.t("Main Control Room"),
                layoutBlockMap, null);
    }

    /** 检查 ui_layout.json 是否有变化，有则实时应用（EDT 上调用） */
    private void syncLayoutFile() {
        java.io.File f = UiLayoutFile.locate();
        if (f == null) {
            if (absoluteLayoutActive) {
                restoreGroupLayout();
            }
            return;
        }
        long stamp = f.lastModified() * 31 + f.length();
        if (f.getAbsolutePath().equals(layoutFilePath) && stamp == layoutFileStamp) {
            return;   // 文件未变化
        }
        try {
            java.util.Map<String, UiLayoutFile.Entry> panels = UiLayoutFile.parse(f);
            boolean newPath = !f.getAbsolutePath().equals(layoutFilePath);
            layoutFilePath = f.getAbsolutePath();
            layoutFileStamp = stamp;
            if (panels == null) {           // mode != absolute → 恢复默认布局
                if (absoluteLayoutActive) {
                    restoreGroupLayout();
                }
                return;
            }
            applyAbsoluteLayout(panels);
            if (newPath) {
                System.out.println("[Layout] 已加载布局文件: " + layoutFilePath
                        + "（" + panels.size() + " 个面板）");
            }
        } catch (Exception ex) {
            System.out.println("[Layout] 读取布局文件失败（忽略本次）: " + ex);
            layoutFileStamp = stamp;        // 避免对同一个坏文件反复重试
        }
    }

    /** 应用绝对布局；文件中未列出的面板保持默认坐标（仅首次切换时归位） */
    private void applyAbsoluteLayout(java.util.Map<String, UiLayoutFile.Entry> bounds) {
        if (!absoluteLayoutActive) {
            jPanel3.setLayout(null);
            absoluteLayoutActive = true;
            for (java.util.Map.Entry<String, java.awt.Rectangle> e : layoutFallbackBounds.entrySet()) {
                if (!bounds.containsKey(e.getKey())) {
                    javax.swing.JComponent c = layoutBlockMap.get(e.getKey());
                    if (c != null) {
                        c.setBounds(e.getValue());
                    }
                }
            }
        }
        int maxX = 0;
        int maxY = 0;
        java.util.Set<String> childKeysThisRound = new java.util.HashSet<>();
        for (java.util.Map.Entry<String, UiLayoutFile.Entry> e : bounds.entrySet()) {
            javax.swing.JComponent c = layoutBlockMap.get(e.getKey());
            if (c == null) {
                continue;
            }
            UiLayoutFile.Entry b = e.getValue();
            c.setBounds(b.x, b.y, b.w, b.h);
            // 编辑器里勾掉"可见"：主窗口的方块也照做（外部工具对任何窗口的可见性都有效）
            if (c.isVisible() != b.visible) {
                c.setVisible(b.visible);
            }
            maxX = Math.max(maxX, b.x + b.w);
            maxY = Math.max(maxY, b.y + b.h);
            // 放大功能：内部按键/文字/仪表 = 尺寸比例自动缩放 × 编辑器"内容缩放"倍率
            java.awt.Rectangle design = layoutFallbackBounds.get(e.getKey());
            if (design != null && design.width > 0 && design.height > 0) {
                double s = PanelContentScaler.scaleFor(design.width, design.height, b.w, b.h);
                s = PanelContentScaler.combine(s, b.contentScale);
                contentScaler.apply(c, s);
            }
            // 子组件（小方块）：面板内部按键/数据显示的绝对位置
            if (b.children != null) {
                applyPanelChildren(e.getKey(), c, b.children);
                childKeysThisRound.add(e.getKey());
            }
            /* 更深的组件（"0/2" 这类索引路径）：外部布局编辑器写的就是这一段。
             * 走 PageLayout.placeNode，和子窗口用同一套"父链逐层接管"逻辑，避免第二份实现。 */
            if (b.nodes != null) {
                java.util.List<String> paths = new java.util.ArrayList<>(b.nodes.keySet());
                paths.sort((x, y) -> {
                    int dx = x.split("/").length, dy = y.split("/").length;
                    return dx != dy ? Integer.compare(dx, dy) : x.compareTo(y);
                });
                for (String path : paths) {
                    UiLayoutFile.Child ch = b.nodes.get(path);
                    java.awt.Component t = childByPath(c, path);
                    if (t != null) {
                        PageLayout.placeNode(t, c, new java.awt.Rectangle(
                                ch.x, ch.y,
                                ch.w > 0 ? ch.w : t.getWidth(),
                                ch.h > 0 ? ch.h : t.getHeight()), ch.visible);
                    }
                }
            }
        }
        // 本轮不再带 children 的面板：恢复其原有内部布局
        for (String key : new java.util.ArrayList<>(savedChildLayouts.keySet())) {
            if (!childKeysThisRound.contains(key)) {
                restorePanelChildren(key);
            }
        }
        // 画布（jPanel3）尺寸 = 包住所有面板 + 20px 边距；窗口本身大小不变，超出部分可滚动查看
        jPanel3.setPreferredSize(new java.awt.Dimension(Math.max(maxX + 20, 640),
                Math.max(maxY + 20, 480)));
        jPanel3.revalidate();
        jPanel3.repaint();
    }

    /** 按 "0/2" 这类索引路径取面板内的组件（与编辑器写盘路径一一对应） */
    private static java.awt.Component childByPath(java.awt.Component root, String path) {
        java.awt.Component cur = root;
        if (path == null || path.isEmpty()) {
            return cur;
        }
        for (String seg : path.split("/")) {
            if (!(cur instanceof java.awt.Container)) {
                return null;
            }
            int i;
            try {
                i = Integer.parseInt(seg.trim());
            } catch (NumberFormatException ex) {
                return null;
            }
            java.awt.Component[] kids = ((java.awt.Container) cur).getComponents();
            if (i < 0 || i >= kids.length) {
                return null;
            }
            cur = kids[i];
        }
        return cur;
    }

    /** 面板内子组件绝对布局：首次切换为该面板 null 布局并记录各子组件原位置，之后按 JSON 坐标摆放 */
    private void applyPanelChildren(String key, javax.swing.JComponent panel,
            java.util.Map<String, UiLayoutFile.Child> children) {
        if (!savedChildLayouts.containsKey(key)) {
            panel.doLayout();   // 先让内部布局按当前尺寸算好各子组件位置，再记录
            java.awt.Component[] comps = panel.getComponents();
            java.awt.Rectangle[] fallback = new java.awt.Rectangle[comps.length];
            for (int i = 0; i < comps.length; i++) {
                fallback[i] = comps[i].getBounds();
            }
            savedChildLayouts.put(key, panel.getLayout());
            childFallbackBounds.put(key, fallback);
            panel.setLayout(null);
        }
        java.awt.Component[] comps = panel.getComponents();
        java.awt.Rectangle[] fallback = childFallbackBounds.get(key);
        for (int i = 0; i < comps.length; i++) {
            java.awt.Component comp = comps[i];
            java.awt.Rectangle fb = (fallback != null && i < fallback.length && fallback[i] != null)
                    ? fallback[i] : comp.getBounds();
            UiLayoutFile.Child ch = children.get(String.valueOf(i));
            if (ch == null || !childClassMatches(comp, ch.cls)) {
                if (!comp.getBounds().equals(fb)) {
                    comp.setBounds(fb);      // 未列出的子组件（或索引错位）保持默认位置
                }
                continue;
            }
            if (comp.getX() != ch.x || comp.getY() != ch.y) {
                comp.setLocation(ch.x, ch.y);
            }
            if (comp.getWidth() != fb.width || comp.getHeight() != fb.height) {
                comp.setSize(fb.width, fb.height);   // 尺寸不变，只调位置
            }
        }
        panel.revalidate();
        panel.repaint();
    }

    /** 校验索引对应的组件类型是否与 JSON 中记录的一致（防代码变化后索引错位） */
    private static boolean childClassMatches(java.awt.Component c, String cls) {
        return cls == null || cls.isEmpty() || cls.equals(c.getClass().getSimpleName());
    }

    /** 恢复面板的原有布局（GroupLayout），撤销子组件绝对定位 */
    private void restorePanelChildren(String key) {
        javax.swing.JComponent panel = layoutBlockMap.get(key);
        java.awt.LayoutManager lm = savedChildLayouts.remove(key);
        childFallbackBounds.remove(key);
        if (panel != null && lm != null) {
            panel.setLayout(lm);
            panel.revalidate();
            panel.repaint();
        }
    }

    /** 导出各面板子组件清单（供布局编辑器展开"小方块"）；优先导出默认（GroupLayout）位置 */
    private void exportChildrenFile() {
        try {
            org.json.JSONObject root = new org.json.JSONObject();
            root.put("generated_at", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
            org.json.JSONObject panels = new org.json.JSONObject();
            for (java.util.Map.Entry<String, javax.swing.JComponent> e : layoutBlockMap.entrySet()) {
                javax.swing.JComponent panel = e.getValue();
                org.json.JSONArray kids = new org.json.JSONArray();
                java.awt.Component[] comps = panel.getComponents();
                java.awt.Rectangle[] fallback = childFallbackBounds.get(e.getKey());
                for (int i = 0; i < comps.length; i++) {
                    java.awt.Component c = comps[i];
                    java.awt.Rectangle r = (fallback != null && i < fallback.length && fallback[i] != null)
                            ? fallback[i] : c.getBounds();
                    org.json.JSONObject k = new org.json.JSONObject();
                    k.put("i", i);
                    k.put("cls", c.getClass().getSimpleName());
                    k.put("text", componentText(c));
                    k.put("x", r.x);
                    k.put("y", r.y);
                    k.put("w", r.width);
                    k.put("h", r.height);
                    kids.put(k);
                }
                panels.put(e.getKey(), new org.json.JSONObject().put("children", kids));
            }
            root.put("panels", panels);
            java.io.File out = UiLayoutFile.childrenPath();
            java.io.File tmp = new java.io.File(out.getAbsolutePath() + ".tmp");
            java.nio.file.Files.write(tmp.toPath(),
                    root.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            java.nio.file.Files.move(tmp.toPath(), out.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[Layout] 已导出子组件清单: " + out);
        } catch (Exception ex) {
            System.out.println("[Layout] 导出子组件清单失败: " + ex);
        }
    }

    /** 取组件上显示的文字（用于编辑器里给小方块加标签） */
    private static String componentText(java.awt.Component c) {
        try {
            if (c instanceof javax.swing.AbstractButton) {
                String t = ((javax.swing.AbstractButton) c).getText();
                return t == null ? "" : t;
            }
            if (c instanceof javax.swing.JLabel) {
                String t = ((javax.swing.JLabel) c).getText();
                return t == null ? "" : t;
            }
            if (c instanceof javax.swing.text.JTextComponent) {
                String t = ((javax.swing.text.JTextComponent) c).getText();
                return t == null ? "" : t;
            }
        } catch (Exception ignore) {
            // 某些组件取文本可能抛异常，忽略
        }
        return "";
    }

    /** 恢复默认 GroupLayout 布局（布局文件被删除或 mode != absolute 时） */
    private void restoreGroupLayout() {
        absoluteLayoutActive = false;
        contentScaler.resetAll();   // 撤销内容缩放，恢复原始字体/尺寸
        // 恢复所有被切换为子组件绝对布局的面板
        for (String key : new java.util.ArrayList<>(savedChildLayouts.keySet())) {
            restorePanelChildren(key);
        }
        // 恢复默认布局时，被"可见"勾掉的面板也要重新显示
        for (javax.swing.JComponent c : layoutBlockMap.values()) {
            if (c != null && !c.isVisible()) {
                c.setVisible(true);
            }
        }
        jPanel3.setLayout(jPanel3GroupLayout);
        jPanel3.setPreferredSize(null);
        jPanel3.revalidate();
        jPanel3.repaint();
        System.out.println("[Layout] 已恢复默认 GroupLayout 布局");
    }

    /* ================= 工具菜单：主窗口布局编辑器 ================= */

    /** 设置 → 工具 → 主窗口布局编辑器：启动配套 Python 工具（pythonw 无控制台窗口） */
    private void launchLayoutEditor() {
        try {
            java.io.File script = findLayoutEditorScript();
            if (script == null) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "未找到布局编辑器脚本\n（应为 tools/layout_editor/layout_editor.py，位于工程目录）",
                        "布局编辑器", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            String pyw = findPythonw();
            java.io.File out = UiLayoutFile.writePath();
            java.util.List<String> cmd = new java.util.ArrayList<>();
            cmd.add(pyw);
            cmd.add(script.getAbsolutePath());
            cmd.add("--out");
            cmd.add(out.getAbsolutePath());
            new ProcessBuilder(cmd)
                    .directory(script.getParentFile())
                    .start();
            System.out.println("[Layout] 已启动布局编辑器: " + pyw + " " + script + " --out " + out);
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "启动布局编辑器失败：\n" + ex, "布局编辑器",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /** 找布局编辑器脚本：jar 同级 tools → jar 上级（工程目录）tools → 工作目录 tools */
    private static java.io.File findLayoutEditorScript() {
        String rel = "tools" + java.io.File.separator + "layout_editor" + java.io.File.separator + "layout_editor.py";
        try {
            java.net.URI uri = UI.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            java.io.File base = new java.io.File(uri);
            java.io.File dir = base.isFile() ? base.getParentFile() : base;   // .../target
            if (dir != null) {
                java.io.File f = new java.io.File(dir, rel);
                if (f.isFile()) {
                    return f;
                }
                if (dir.getParentFile() != null) {                            // 工程目录
                    f = new java.io.File(dir.getParentFile(), rel);
                    if (f.isFile()) {
                        return f;
                    }
                }
            }
        } catch (Exception ignore) {
            // 忽略，继续用工作目录兜底
        }
        java.io.File f = new java.io.File(rel);
        return f.isFile() ? f : null;
    }

    /** 找 pythonw（无控制台）：venv 优先，PATH 兜底 */
    private static String findPythonw() {        java.io.File venv = new java.io.File(System.getProperty("user.home"),
                ".workbuddy" + java.io.File.separator + "binaries" + java.io.File.separator
                + "python" + java.io.File.separator + "envs" + java.io.File.separator
                + "default" + java.io.File.separator + "Scripts" + java.io.File.separator + "pythonw.exe");
        if (venv.isFile()) {
            return venv.getAbsolutePath();
        }
        return "pythonw";
    }

    /* ---------- 工具 2：布局编辑器 jar 模组（RBMK-LayoutEditor.jar） ---------- */

    /**
     * 启动打包进本程序的布局编辑器 jar（任意窗口、任意层级的组件/按键都能调）。
     * jar 随 fat jar 分发在 {@code META-INF/RBMK-1500/tools/RBMK-LayoutEditor.jar}，
     * 运行时解到布局文件同级目录的 {@code tools/} 下再 {@code java -jar} 启动，
     * 这样换了机器/换了解压目录都不用另外带文件。
     */
    private void launchLayoutEditorJar() {
        try {
            java.io.File jar = resolveLayoutEditorJar();
            if (jar == null || !jar.isFile()) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "未找到 RBMK-LayoutEditor.jar\n"
                                + "（应随程序打包在 META-INF/RBMK-1500/tools/ 下，"
                                + "或位于 tools/layout_editor_java/）",
                        Localization.t("Layout Editor"), javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            java.io.File dir = UiLayoutFile.writePath().getParentFile();
            if (dir == null) {
                dir = new java.io.File(".");
            }
            java.util.List<String> cmd = new java.util.ArrayList<>();
            cmd.add(findJavaw());
            cmd.add("-jar");
            cmd.add(jar.getAbsolutePath());
            cmd.add(dir.getAbsolutePath());
            new ProcessBuilder(cmd).directory(dir).start();
            System.out.println("[Layout] 已启动布局编辑器 jar: " + jar + " 目录=" + dir);
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "启动布局编辑器 jar 失败：\n" + ex, Localization.t("Layout Editor"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 找布局编辑器 jar：优先用随程序打包的那份（每次比对大小，升级后会自动重新解包，
     * 不会被旧的解包副本挡住）；这个构建里没带资源时才退回工程里的散装副本。
     * 返回 null 表示两边都找不到。
     */
    private static java.io.File resolveLayoutEditorJar() {
        final String name = "RBMK-LayoutEditor.jar";
        java.io.File layoutDir = UiLayoutFile.writePath().getParentFile();

        // ---- 1. 资源里带的那份：解到 <布局目录>/tools/，大小不一致就重新解 ----
        java.io.File target = (layoutDir != null)
                ? new java.io.File(new java.io.File(layoutDir, "tools"), name)
                : new java.io.File(name);
        java.net.URL res = UI.class.getResource("/META-INF/RBMK-1500/tools/" + name);
        if (res != null) {
            long resSize = -1;
            try {
                java.net.URLConnection conn = res.openConnection();
                if (conn instanceof java.net.JarURLConnection) {
                    resSize = ((java.net.JarURLConnection) conn).getJarEntry().getSize();
                } else {
                    resSize = conn.getContentLengthLong();
                }
            } catch (Exception ignore) {
                // 拿不到长度就按"需要重新解"处理
            }
            if (resSize > 0 && target.isFile() && target.length() == resSize) {
                return target;       // 已经解过且是同版本
            }
            try (java.io.InputStream in = res.openStream()) {
                java.io.File parent = target.getParentFile();
                if (parent != null && !parent.isDirectory()) {
                    parent.mkdirs();
                }
                java.nio.file.Files.copy(in, target.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[Layout] 已解出布局编辑器 jar: " + target + "（" + target.length() + " B）");
                return target;
            } catch (Exception e) {
                System.out.println("[Layout] 解出布局编辑器 jar 失败（改用工程里的副本）: " + e);
            }
        }

        // ---- 2. 兜底：工程/程序目录里的散装副本 ----
        java.util.List<java.io.File> candidates = new java.util.ArrayList<>();
        if (layoutDir != null) {
            candidates.add(new java.io.File(new java.io.File(layoutDir, "tools"), name));
        }
        try {
            java.net.URI uri = UI.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            java.io.File base = new java.io.File(uri);
            java.io.File dir = base.isFile() ? base.getParentFile() : base;
            if (dir != null) {
                candidates.add(new java.io.File(dir, name));
                candidates.add(new java.io.File(new java.io.File(dir, "tools/layout_editor_java"), name));
                if (dir.getParentFile() != null) {
                    candidates.add(new java.io.File(
                            new java.io.File(dir.getParentFile(), "tools/layout_editor_java"), name));
                }
            }
        } catch (Exception ignore) {
            // 拿不到程序路径时只看布局目录/工作目录
        }
        candidates.add(new java.io.File(name));
        for (java.io.File f : candidates) {
            if (f.isFile() && f.length() > 1024) {
                return f;
            }
        }
        return null;
    }

    /** 找 javaw（无控制台）：优先用当前 JVM 自带的，找不到退回 java */
    private static String findJavaw() {
        String home = System.getProperty("java.home");
        if (home != null) {
            java.io.File javaw = new java.io.File(new java.io.File(home, "bin"),
                    System.getProperty("os.name", "").toLowerCase().contains("win") ? "javaw.exe" : "javaw");
            if (javaw.isFile()) {
                return javaw.getAbsolutePath();
            }
        }
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("win") ? "javaw" : "java";
    }

    private void jMenuItem11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem11ActionPerformed
        this.dispose();
    }//GEN-LAST:event_jMenuItem11ActionPerformed

    private void pauseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseActionPerformed
        if (pause.isSelected()) {
            NPPSim.setPaused(true);
        } else {
            NPPSim.setPaused(false);
        }
    }//GEN-LAST:event_pauseActionPerformed

    private void precisionDecrement2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_precisionDecrement2ActionPerformed
        if (debounce) {
            return;
        }
        debounce = true;
        if (autoControl.automaticRodController.getSetpoint() == 0) {
            return;
        }
        setpoint.setLcdValue(setpoint.getLcdValue() - 5);
        autoControl.automaticRodController.setSetpoint(setpoint.getLcdValue());
    }//GEN-LAST:event_precisionDecrement2ActionPerformed

    private void precisionIncrement2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_precisionIncrement2ActionPerformed
        if (debounce) {
            return;
        }
        debounce = true;
        if (autoControl.automaticRodController.getSetpoint() == 5200) {
            return;
        }
        setpoint.setLcdValue(setpoint.getLcdValue() + 5);
        autoControl.automaticRodController.setSetpoint(setpoint.getLcdValue());
    }//GEN-LAST:event_precisionIncrement2ActionPerformed

    private void toggleAR1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleAR1ActionPerformed
        if (toggleAR1.getModel().isSelected()) {
            autoControl.automaticRodController.enable1AR();
        } else {
            autoControl.automaticRodController.disable1AR();
        }
        limit1AR.setBackground(Annunciator.YELLOWOFF_COLOR);
        error1AR.setBackground(Annunciator.YELLOWOFF_COLOR);
        busy1AR.setBackground(Annunciator.GREENOFF_COLOR);
        
    }//GEN-LAST:event_toggleAR1ActionPerformed

    private void toggleAR2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleAR2ActionPerformed
        if (toggleAR2.getModel().isSelected()) {
            autoControl.automaticRodController.enable2AR();
        } else {
            autoControl.automaticRodController.disable2AR();
        }
        limit2AR.setBackground(Annunciator.YELLOWOFF_COLOR);
        error2AR.setBackground(Annunciator.YELLOWOFF_COLOR);
        busy2AR.setBackground(Annunciator.GREENOFF_COLOR);
        
    }//GEN-LAST:event_toggleAR2ActionPerformed

    private void toggleLACActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleLACActionPerformed
        if (toggleLAC.getModel().isSelected()) {
            autoControl.automaticRodController.enableLAR();
        } else {
            autoControl.automaticRodController.disableLar();
        }
        limitLAC.setBackground(Annunciator.YELLOWOFF_COLOR);
        errorLAC.setBackground(Annunciator.YELLOWOFF_COLOR);
        busyLAC.setBackground(Annunciator.GREENOFF_COLOR);
    }//GEN-LAST:event_toggleLACActionPerformed

    private void mtkPlusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mtkPlusActionPerformed
        NPPSim.ui.mTK1.setSignPositive(true);
    }//GEN-LAST:event_mtkPlusActionPerformed

    private void mtkMinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mtkMinActionPerformed
        NPPSim.ui.mTK1.setSignPositive(false);
    }//GEN-LAST:event_mtkMinActionPerformed

    private void zero5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zero5ActionPerformed
        NPPSim.ui.mTK1.setThresHold(0.005f);
    }//GEN-LAST:event_zero5ActionPerformed

    private void oneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_oneActionPerformed
        NPPSim.ui.mTK1.setThresHold(0.01f);
    }//GEN-LAST:event_oneActionPerformed

    private void two5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_two5ActionPerformed
        NPPSim.ui.mTK1.setThresHold(0.025f);
    }//GEN-LAST:event_two5ActionPerformed

    private void fiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fiveActionPerformed
        NPPSim.ui.mTK1.setThresHold(0.05f);
    }//GEN-LAST:event_fiveActionPerformed

    private void tenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tenActionPerformed
        NPPSim.ui.mTK1.setThresHold(0.1f);
    }//GEN-LAST:event_tenActionPerformed

    private void fifteenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fifteenActionPerformed
        NPPSim.ui.mTK1.setThresHold(0.15f);
    }//GEN-LAST:event_fifteenActionPerformed

    private void twentyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_twentyActionPerformed
        NPPSim.ui.mTK1.setThresHold(0.2f);
    }//GEN-LAST:event_twentyActionPerformed

    private void twentyfiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_twentyfiveActionPerformed
        NPPSim.ui.mTK1.setThresHold(0.25f);
    }//GEN-LAST:event_twentyfiveActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        createOrContinue(LogWindow.class, false, false);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        autoControl.eventLog.clear();
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jRadioButtonMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItem2ActionPerformed
        uiUpdateRate = 100;
    }//GEN-LAST:event_jRadioButtonMenuItem2ActionPerformed

    private void jRadioButtonMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItem1ActionPerformed
        uiUpdateRate = 50;
    }//GEN-LAST:event_jRadioButtonMenuItem1ActionPerformed

    private void jRadioButtonMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItem3ActionPerformed
        uiUpdateRate = 150;
    }//GEN-LAST:event_jRadioButtonMenuItem3ActionPerformed

    private void jRadioButtonMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItem4ActionPerformed
        uiUpdateRate = 200;
    }//GEN-LAST:event_jRadioButtonMenuItem4ActionPerformed

    private void jRadioButtonMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItem5ActionPerformed
        uiUpdateRate = 500;
    }//GEN-LAST:event_jRadioButtonMenuItem5ActionPerformed

    private void jRadioButtonMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItem6ActionPerformed
        uiUpdateRate = 1000;
    }//GEN-LAST:event_jRadioButtonMenuItem6ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
         autoControl.fasrControl.trip("BSM Button Pressed");
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jMenuItem12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem12ActionPerformed
        createOrContinue(MCPUI.class, true, false);
    }//GEN-LAST:event_jMenuItem12ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {
        createOrContinue(CoreMap.class, false, false);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel annunciatorPanel;
    private javax.swing.JTextField arINOP;
    private javax.swing.JTextField busy1AR;
    private javax.swing.JTextField busy2AR;
    private javax.swing.JTextField busyLAC;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.ButtonGroup buttonGroup6;
    private javax.swing.ButtonGroup buttonGroup7;
    private eu.hansolo.steelseries.gauges.Linear deltaKeff;
    private eu.hansolo.steelseries.gauges.DisplaySingle drumTemp;
    private eu.hansolo.steelseries.gauges.DisplaySingle drumTemp1;
    private eu.hansolo.steelseries.gauges.DisplayRectangular electric1;
    private eu.hansolo.steelseries.gauges.DisplayRectangular electric2;
    private javax.swing.JTextField error1AR;
    private javax.swing.JTextField error2AR;
    private javax.swing.JTextField errorLAC;
    private javax.swing.JRadioButtonMenuItem fifteen;
    private javax.swing.JRadioButtonMenuItem five;
    private eu.hansolo.steelseries.gauges.DisplaySingle headerFlow;
    private eu.hansolo.steelseries.gauges.DisplaySingle headerFlow1;
    private eu.hansolo.steelseries.gauges.DisplaySingle headerTemp;
    private eu.hansolo.steelseries.gauges.DisplaySingle headerTemp1;
    private javax.swing.JTextField highDrumPress;
    private javax.swing.JTextField highWaterLevel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton azButton2;   // 手动 AZ-2 跳闸
    private javax.swing.JButton azButton3;   // 手动 AZ-3 跳闸
    private javax.swing.JButton azButton4;   // 手动 AZ-4 跳闸
    private javax.swing.JButton azButton5;   // 手动 AZ-5 停堆（带保护盖，无自动定值）
    private javax.swing.JButton azButton6;   // 手动 AZ-6 跳闸（5600 MW 档）
    private javax.swing.JPanel azTripPanel;  // AZ-2 / AZ-3 按键行（指示灯阵列下方）
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenu jMenu7;
    private javax.swing.JMenu jMenu8;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem12;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JPanel autoAzPanel;
    private javax.swing.JPanel rodLimitPanel;        // 控制棒数量限制面板（UI 同自动AZ）
    private javax.swing.JRadioButton rodLimitOn;     // 开启数量限制
    private javax.swing.JRadioButton rodLimitOff;    // 关闭数量限制
    private javax.swing.JRadioButton autoAzOn;
    private javax.swing.JRadioButton autoAzOff;
    private javax.swing.JMenuItem jMenuItem13;
    private javax.swing.JMenuItem jMenuItem14;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private com.darwish.nppsim.OfflineCoolingPanel offlineCoolingPanel;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem1;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem2;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem3;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem4;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem5;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem6;
    private javax.swing.JScrollPane jScrollPane1;
    private eu.hansolo.steelseries.gauges.DisplaySingle keff;
    private javax.swing.JTextField lacINOP;
    private javax.swing.JTextField limit1AR;
    private javax.swing.JTextField limit2AR;
    private javax.swing.JTextField limitLAC;
    private javax.swing.JTextField lowDrumPress;
    private javax.swing.JTextField lowWaterLevel;
    private com.darwish.nppsim.MTK mTK1;
    private com.darwish.nppsim.ManualRodControl manualRodControl1;
    private javax.swing.JTextField mcpCavit;
    private javax.swing.JTextField msv1;
    private javax.swing.JTextField msv2;
    private javax.swing.JTextField msv3;
    private javax.swing.JRadioButtonMenuItem mtkMin;
    private javax.swing.JRadioButtonMenuItem mtkPlus;
    private javax.swing.JTextField nRate;
    private eu.hansolo.steelseries.gauges.DisplaySingle neutronFlux;
    private javax.swing.JRadioButtonMenuItem one;
    private javax.swing.JCheckBoxMenuItem pause;
    private eu.hansolo.steelseries.gauges.DisplaySingle period;
    private javax.swing.JTextField power;
    private javax.swing.JButton precisionDecrement2;
    private javax.swing.JButton precisionIncrement2;
    private eu.hansolo.steelseries.gauges.DisplaySingle press1;
    private eu.hansolo.steelseries.gauges.DisplaySingle press2;
    private javax.swing.JTextField rodLimit;
    private javax.swing.JTextField rodLimit1;
    private javax.swing.JTextField rodLimit10;
    private javax.swing.JTextField rodLimit2;
    private javax.swing.JTextField rodLimit3;
    private javax.swing.JTextField rodLimit4;
    private javax.swing.JTextField rodLimit5;
    private javax.swing.JTextField rodSeq;
    private javax.swing.JTextField rodTravel;
    private javax.swing.JButton rodsIn;
    private javax.swing.JButton rodsOut;
    private javax.swing.JButton rodsOut1;
    /** 棒速度小块：标题 + 金属质感 < > 两个键（在"插入"下面） */
    private javax.swing.JPanel rodSpeedPanel;
    private javax.swing.JButton rodSlower;
    private javax.swing.JButton rodFaster;
    private javax.swing.JTextField sdv_aOpen;
    private eu.hansolo.steelseries.gauges.DisplaySingle setpoint;
    private eu.hansolo.steelseries.gauges.DisplaySingle suctionTemp;
    private eu.hansolo.steelseries.gauges.DisplaySingle suctionTemp1;
    private javax.swing.JTextField t30;
    private javax.swing.JRadioButtonMenuItem ten;
    private eu.hansolo.steelseries.gauges.DisplayRectangular thermalPower1;
    private javax.swing.JToggleButton toggleAR1;
    private javax.swing.JToggleButton toggleAR2;
    private javax.swing.JToggleButton toggleLAC;
    private javax.swing.JRadioButtonMenuItem twenty;
    private javax.swing.JRadioButtonMenuItem twentyfive;
    private javax.swing.JRadioButtonMenuItem two5;
    private javax.swing.JRadioButtonMenuItem zero5;
    // End of variables declaration//GEN-END:variables
}
