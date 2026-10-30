package com.darwish.nppsim;

import static com.darwish.nppsim.NPPSim.autoControl;
import static com.darwish.nppsim.NPPSim.condensate1A;
import static com.darwish.nppsim.NPPSim.condensate2A;
import static com.darwish.nppsim.NPPSim.condensate1B;
import static com.darwish.nppsim.NPPSim.condensate2B;
import static com.darwish.nppsim.NPPSim.ejectors;
import static com.darwish.nppsim.NPPSim.tg1;
import static com.darwish.nppsim.NPPSim.tg2;
import java.io.Serializable;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;

/**
 *
 * @author ali
 */
public class CondensateUI extends javax.swing.JFrame implements UIUpdateable, Serializable {
    private final Annunciator annunciator;

    /**
     * Creates new form CondensateUI
     */
    public CondensateUI() {
        initComponents();
        this.setTitle(Localization.t("Condensate Control"));
        WindowMenu.install(this);        // 统一窗口导航（含新增页面）
        PageLayout.installAuto(this, "CondensateUI");   // 本页布局可由布局编辑器调整
        annunciator = new Annunciator(annunciatorPanel);
        installVacuumEjectorPanels();
        
        ((JSpinner.DefaultEditor)spinner1A.getEditor()).getTextField().setEditable(false);
        ((JSpinner.DefaultEditor)spinner2A.getEditor()).getTextField().setEditable(false);
        ((JSpinner.DefaultEditor)spinner1B.getEditor()).getTextField().setEditable(false);
        ((JSpinner.DefaultEditor)spinner2B.getEditor()).getTextField().setEditable(false);
        ((JSpinner.DefaultEditor)ejector1Spinner.getEditor()).getTextField().setEditable(false);
        ((JSpinner.DefaultEditor)ejector2Spinner.getEditor()).getTextField().setEditable(false);
        // 每台汽轮机 4 台凝汽器：副标题右侧的 1–4 选择器
        installCondenserSelectors();
        spinner1A.addChangeListener((ChangeEvent e) -> {
            Pump currentSelection = condensate1A.get((int)spinner1A.getValue() - 1);
            rpm1A.setLcdValue(currentSelection.getRPM());
            amps1A.setLcdValue(currentSelection.getPowerUsage());
            if (currentSelection.isActive()) {
                start1A.setSelected(true);
            } else {
                stop1A.setSelected(true);
            }
        });
        spinner2A.addChangeListener((ChangeEvent e) -> {
            Pump currentSelection = condensate2A.get((int)spinner2A.getValue() - 1);
            rpm2A.setLcdValue(currentSelection.getRPM());
            amps2A.setLcdValue(currentSelection.getPowerUsage());
            if (currentSelection.isActive()) {
                start2A.setSelected(true);
            } else {
                stop2A.setSelected(true);
            }
        });
        spinner1B.addChangeListener((ChangeEvent e) -> {
            Pump currentSelection = condensate1B.get((int)spinner1B.getValue() - 1);
            rpm1B.setLcdValue(currentSelection.getRPM());
            amps1B.setLcdValue(currentSelection.getPowerUsage());
            if (currentSelection.isActive()) {
                start1B.setSelected(true);
            } else {
                stop1B.setSelected(true);
            }
        });
        spinner2B.addChangeListener((ChangeEvent e) -> {
            Pump currentSelection = condensate2B.get((int)spinner2B.getValue() - 1);
            rpm2B.setLcdValue(currentSelection.getRPM());
            amps2B.setLcdValue(currentSelection.getPowerUsage());
            if (currentSelection.isActive()) {
                start2B.setSelected(true);
            } else {
                stop2B.setSelected(true);
            }
        });
        ejector1Spinner.addChangeListener((ChangeEvent e) -> {
            Ejector currentSelection = ejectors.get((int)ejector1Spinner.getValue() - 1);
            if (currentSelection.getState() == 2) {
                ejector1Start.setSelected(true);
            } else {
                ejector1Stop.setSelected(true);
            }
        });
        ejector2Spinner.addChangeListener((ChangeEvent e) -> {
            Ejector currentSelection = ejectors.get((int)ejector2Spinner.getValue() - 1);
            if (currentSelection.getState() == 2) {
                ejector2Start.setSelected(true);
            } else {
                ejector2Stop.setSelected(true);
            }
        });
        
        totalFlow1.setBackgroundColor(eu.hansolo.steelseries.tools.BackgroundColor.SATIN_GRAY);
        totalFlow1.setFrameVisible(false);
        totalFlow1.setLcdVisible(false);
        totalFlow1.setLedVisible(false);
        totalFlow1.setMaxValue(4000.0);
        totalFlow1.setOrientation(eu.hansolo.steelseries.tools.Orientation.HORIZONTAL);
        totalFlow1.setUnitString("kg/s");
        totalFlow1.setValueColor(eu.hansolo.steelseries.tools.ColorDef.YELLOW);
        
        totalFlow2.setBackgroundColor(eu.hansolo.steelseries.tools.BackgroundColor.SATIN_GRAY);
        totalFlow2.setFrameVisible(false);
        totalFlow2.setLcdVisible(false);
        totalFlow2.setLedVisible(false);
        totalFlow2.setMaxValue(4000.0);
        totalFlow2.setOrientation(eu.hansolo.steelseries.tools.Orientation.HORIZONTAL);
        totalFlow2.setUnitString("kg/s");
        totalFlow2.setValueColor(eu.hansolo.steelseries.tools.ColorDef.YELLOW);
        
        //condTemp1.setTrackStart(100.0);
        //condTemp2.setTrackStart(100.0);
        initializeDialUpdateThread();
        
        //set variables to current state 
        Pump currentSelection = condensate1A.get((int)spinner1A.getValue() - 1);
        if (currentSelection.isActive()) {
            start1A.setSelected(true);
        } else {
            stop1A.setSelected(true);
        }
        currentSelection = condensate2A.get((int)spinner2A.getValue() - 1);
        if (currentSelection.isActive()) {
            start2A.setSelected(true);
        } else {
            stop2A.setSelected(true);
        }
        currentSelection = condensate1B.get((int)spinner1B.getValue() - 1);
        if (currentSelection.isActive()) {
            start1B.setSelected(true);
        } else {
            stop1B.setSelected(true);
        }
        currentSelection = condensate2B.get((int)spinner2B.getValue() - 1);
        if (currentSelection.isActive()) {
            start2B.setSelected(true);
        } else {
            stop2B.setSelected(true);
        }
        power1A1.setLedOn(condensate1A.get(0).isActive());
        power1A2.setLedOn(condensate1A.get(1).isActive());
        power1A3.setLedOn(condensate1A.get(2).isActive());
        power2A1.setLedOn(condensate2A.get(0).isActive());
        power2A2.setLedOn(condensate2A.get(1).isActive());
        power2A3.setLedOn(condensate2A.get(2).isActive());
        power1B1.setLedOn(condensate1B.get(0).isActive());
        power1B2.setLedOn(condensate1B.get(1).isActive());
        power1B3.setLedOn(condensate1B.get(2).isActive());
        power2B1.setLedOn(condensate2B.get(0).isActive());
        power2B2.setLedOn(condensate2B.get(1).isActive());
        power2B3.setLedOn(condensate2B.get(2).isActive());
        Ejector currentSelection1 = ejectors.get((int)ejector1Spinner.getValue() - 1);
        if (currentSelection1.getState() == 2) {
            ejector1Start.setSelected(true);
        } else {
            ejector1Stop.setSelected(true);
        }
        currentSelection1 = ejectors.get((int)ejector2Spinner.getValue() - 1);
        if (currentSelection1.getState() == 2) {
            ejector2Start.setSelected(true);
        } else {
            ejector2Stop.setSelected(true);
        }
        con1Start.setSelected(condA().condenserPump.isActive());
        con2Start.setSelected(condB().condenserPump.isActive());
        con1Power.setLedOn(condA().condenserPump.isActive());
        con2Power.setLedOn(condB().condenserPump.isActive());
        flowControl.setSelected(autoControl.condenserWaterLevelControl.get(0).isEnabled());
    }
    
    
    /** 凝汽器选择器：每台汽轮机 4 台，1–4 切换 */
    private javax.swing.JSpinner condSelA, condSelB;

    /* 8 台凝汽器统一编号：TG-A = 1~4，TG-B = 5~8。
     * 两个选择器各自显示自己范围里的号（A 显示 1-4，B 显示 5-8），
     * 这样全厂 8 台泵不会出现两个"3 号"。 */

    /** 当前选中的凝汽器（TG-A 区块：1~4）。选择器未装好时退回第 1 台，避免刷新线程 NPE。 */
    private Condenser condA() {
        return tg1.condenser.unit(selectorIndex(condSelA, 1));
    }

    /** 当前选中的凝汽器（TG-B 区块：5~8） */
    private Condenser condB() {
        return tg2.condenser.unit(selectorIndex(condSelB, 5));
    }

    /** 编号 → 本组内的 0 基索引 */
    private static int selectorIndex(javax.swing.JSpinner sp, int firstNumber) {
        if (sp == null) return 0;
        int n = ((Number) sp.getValue()).intValue() - firstNumber;
        return Math.max(0, Math.min(CondenserBank.UNITS_PER_TG - 1, n));
    }

    /**
     * 在 "TG-A / TG-B Condensers" 副标题右侧各放一个 1–4 的选择器。
     * 控件挂在 contentPane 上（不塞进 NetBeans 的 GroupLayout 里），
     * 这样 PageLayout 的布局编辑器能像其它方块一样给它记坐标、拖动。
     */
    private void installCondenserSelectors() {
        try {
            condSelA = newCondenserSelector(1);   // TG-A：1~4
            condSelB = newCondenserSelector(5);   // TG-B：5~8
            condSelA.addChangeListener(e -> {
                placeCondenserSelectors();
                update();
            });
            condSelB.addChangeListener(e -> {
                placeCondenserSelectors();
                update();
            });
            attachSelector(condSelA, jLabel45);
            attachSelector(condSelB, jLabel48);
            placeCondenserSelectors();
            addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentShown(java.awt.event.ComponentEvent e) {
                    placeCondenserSelectors();
                }
            });
        } catch (Throwable t) {
            // 选择器装不上也不能把整个凝结水页面带崩（否则所有读数都停更）
            t.printStackTrace();
        }
    }

    /** 把选择器挂到副标题所在的区块面板里（跟着区块一起滚动/缩放） */
    private void attachSelector(javax.swing.JSpinner sp, javax.swing.JLabel anchor) {
        if (sp == null || anchor == null) return;
        java.awt.Container host = anchor.getParent();
        if (host == null) {
            getContentPane().add(sp);
            return;
        }
        host.add(sp);                       // 追加在最后 → 画在最上层
        sp.setVisible(true);
        host.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                placeOne(sp, anchor);
            }
        });
    }

    /** @param firstNumber 本组的起始编号（TG-A 从 1 开始，TG-B 从 5 开始） */
    private javax.swing.JSpinner newCondenserSelector(int firstNumber) {
        int last = firstNumber + CondenserBank.UNITS_PER_TG - 1;
        javax.swing.JSpinner sp = new javax.swing.JSpinner(
                new javax.swing.SpinnerNumberModel(firstNumber, firstNumber, last, 1));
        sp.setFont(Fonts.text("Dialog", java.awt.Font.BOLD, 12));
        ((javax.swing.JSpinner.DefaultEditor) sp.getEditor()).getTextField().setEditable(false);
        sp.setToolTipText(Localization.t("Condenser Unit"));
        return sp;
    }

    /** 把选择器摆到两个副标题的右边（跟随布局变化重新计算） */
    private void placeCondenserSelectors() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            placeOne(condSelA, jLabel45);
            placeOne(condSelB, jLabel48);
        });
    }

    private void placeOne(javax.swing.JSpinner sp, javax.swing.JLabel anchor) {
        if (sp == null || anchor == null) return;
        java.awt.Container host = anchor.getParent();
        if (host == null || sp.getParent() != host) return;
        int hostW = host.getWidth();
        int x = anchor.getX() + anchor.getWidth() + 8;
        if (hostW > 0 && x + 56 > hostW) {
            x = Math.max(4, hostW - 64);     // 贴右边缘，保证在面板内
        }
        int y = Math.max(2, anchor.getY() - 1);
        sp.setBounds(x, y, 56, 24);
        sp.setVisible(true);
        if (Boolean.getBoolean("cond.debug")) {
            System.out.printf("[cond] selector %s → parent=%s bounds=%s hostSize=%dx%d%n",
                    sp.getValue(), host.getClass().getSimpleName(), sp.getBounds(),
                    host.getWidth(), host.getHeight());
        }
    }

    private VacuumEjectorPanel vacuumPanel1, vacuumPanel2;
    /** 真空抽气控制区（流量控制面板正下方）；initComponents 先挂空面板，构造时再填充内容 */
    private final javax.swing.JPanel vacuumRow = new javax.swing.JPanel(new java.awt.BorderLayout());
    /** 凝结水系统排水阀面板（联箱 → 除盐水箱，开阀亮红色"排水阀开启"） */
    private DrainValvePanel condensateDrainPanel;

    /** 真空抽气泵控制区（合体图样式）：标题 + 两台泵并排，追加在页面底部 */
    private void installVacuumEjectorPanels() {
        if (NPPSim.vacuumPump1 == null) {
            return;
        }
        vacuumPanel1 = new VacuumEjectorPanel(Localization.t("Vacuum Ejector"), NPPSim.vacuumPump1);
        vacuumPanel2 = new VacuumEjectorPanel(Localization.t("Vacuum Ejector"), NPPSim.vacuumPump2);

        // 独立面板，置于「流量控制」(jPanel1) 正下方（由 jPanel3Layout 布局）
        vacuumRow.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(222, 222, 222), 1, true));
        javax.swing.JLabel header = new javax.swing.JLabel(Localization.t("Vacuum Ejector Control"));
        header.setFont(Fonts.text("Dialog", java.awt.Font.BOLD, 14));
        header.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 8, 2, 8));
        vacuumRow.add(header, java.awt.BorderLayout.NORTH);
        javax.swing.JPanel pair = new javax.swing.JPanel(new java.awt.GridLayout(1, 2, 8, 0));
        pair.add(vacuumPanel1);
        pair.add(vacuumPanel2);
        vacuumRow.add(pair, java.awt.BorderLayout.CENTER);

        // 凝结水系统排水阀（自动排水 → 除盐水箱；开阀报警在左上角 annunciator 的 drainOpen 灯）
        if (NPPSim.condensateDrainValve != null) {
            condensateDrainPanel = new DrainValvePanel(
                    Localization.t("Condensate System Drain"),
                    NPPSim.condensateDrainValve,
                    null);
            vacuumRow.add(condensateDrainPanel, java.awt.BorderLayout.SOUTH);
        }
    }

    @Override
    public void update() {
        checkAlarms();
        if (this.isVisible()) {
            java.awt.EventQueue.invokeLater(() -> {
                Pump selected1A = condensate1A.get((int)spinner1A.getValue() - 1);
                Pump selected2A = condensate2A.get((int)spinner2A.getValue() - 1);
                Pump selected1B = condensate1B.get((int)spinner1B.getValue() - 1);
                Pump selected2B = condensate2B.get((int)spinner2B.getValue() - 1);
                Ejector selected1Ejector = ejectors.get((int)ejector1Spinner.getValue() - 1);
                Ejector selected2Ejector = ejectors.get((int)ejector2Spinner.getValue() - 1);
                Pump con1 = condA().condenserPump;
                Pump con2 = condB().condenserPump;

                rpm1A.setLcdValue(selected1A.getRPM());
                showUnit(flow1A, Units.Quantity.FLOW, selected1A.getFlowRate());
                amps1A.setLcdValue(selected1A.getPowerUsage());
                rpm2A.setLcdValue(selected2A.getRPM());
                showUnit(flow2A, Units.Quantity.FLOW, selected2A.getFlowRate());
                amps2A.setLcdValue(selected2A.getPowerUsage());
                rpm1B.setLcdValue(selected1B.getRPM());
                showUnit(flow1B, Units.Quantity.FLOW, selected1B.getFlowRate());
                amps1B.setLcdValue(selected1B.getPowerUsage());
                rpm2B.setLcdValue(selected2B.getRPM());
                showUnit(flow2B, Units.Quantity.FLOW, selected2B.getFlowRate());
                amps2B.setLcdValue(selected2B.getPowerUsage());

                con1RPM.setLcdValue(con1.getRPM());
                showUnit(con1Flow, Units.Quantity.FLOW, con1.getFlow() * 20);
                con1A.setLcdValue(con1.getPowerUsage());
                /* TG 切换后按钮/指示灯也要跟着变（原来只刷 LCD，切换凝汽器后
                 * 启停按钮和运行灯仍停在上一个凝汽器的状态）。 */
                syncCirculationPumpUi(con1, con1Start, con1Stop, con1Power);
                showUnit(con1Inlet, Units.Quantity.TEMPERATURE, con1.source.getWaterTemperature());
                showUnit(con1Outlet, Units.Quantity.TEMPERATURE, condA().getCondenserWaterTemperature());
                con2RPM.setLcdValue(con2.getRPM());
                showUnit(con2Flow, Units.Quantity.FLOW, con2.getFlow() * 20);
                con2A.setLcdValue(con2.getPowerUsage());
                syncCirculationPumpUi(con2, con2Start, con2Stop, con2Power);
                showUnit(con2Inlet, Units.Quantity.TEMPERATURE, con2.source.getWaterTemperature());
                showUnit(con2Outlet, Units.Quantity.TEMPERATURE, condB().getCondenserWaterTemperature());

                showUnit(ejectorFlow1, Units.Quantity.FLOW, selected1Ejector.getFlowRate());
                showUnit(SGAMFlow1, Units.Quantity.FLOW, selected1Ejector.getEjectorFlowRate());
                if (condensateDrainPanel != null) {
                    condensateDrainPanel.refresh();
                }
                showUnit(ejectorFlow2, Units.Quantity.FLOW, selected2Ejector.getFlowRate());
                showUnit(SGAMFlow2, Units.Quantity.FLOW, selected2Ejector.getEjectorFlowRate());
            });
        }
    }

    @Override
    public void initializeDialUpdateThread() {
        UI.uiThreads.add(
            new Thread(() -> {
                try {
                    while (true) {
                        annunciator.update();
                        if (this.isVisible()) {
                            java.awt.EventQueue.invokeLater(() -> {
                                if (vacuumPanel1 != null) {
                                    vacuumPanel1.refresh();
                                }
                                if (vacuumPanel2 != null) {
                                    vacuumPanel2.refresh();
                                }
                                final double[] hotwellFlows = {0.0, 0.0};
                                final float[] totalValvePosition = {0.0f, 0.0f}; 
                                condensate1A.forEach(pump -> {
                                    hotwellFlows[0] += pump.getFlowRate();
                                });
                                condensate2A.forEach(pump -> {
                                    hotwellFlows[1] += pump.getFlowRate();
                                });
                                condensate1B.forEach(pump -> {
                                    totalValvePosition[0] += pump.dischargeValve.getPosition();
                                });
                                condensate2B.forEach(pump -> {
                                    totalValvePosition[1] += pump.dischargeValve.getPosition();
                                });
                                totalValvePosition[0] /= 3.0f;
                                totalValvePosition[1] /= 3.0f;
                                totalFlow1.setValue(hotwellFlows[0]);
                                totalFlow2.setValue(hotwellFlows[1]);
                                totalInflow1.setValue(condA().getCondensationRate());
                                totalInflow2.setValue(condB().getCondensationRate());
                                hotwellLevel1.setValue(condA().getWaterLevel());
                                hotwellLevel2.setValue(condB().getWaterLevel());
                                vacuum1.setValue(condA().getPressure() * 1000);
                                vacuum2.setValue(condB().getPressure() * 1000);
                                condTemp1.setValue(condA().getWaterTemperature());
                                condTemp2.setValue(condB().getWaterTemperature());
                                out1.setValue(totalValvePosition[0] * 100);
                                out2.setValue(totalValvePosition[1] * 100);
                            });
                        }
                        if (this.isFocused()) {
                            Thread.sleep(UI.getUpdateRate());
                        } else {
                            Thread.sleep(200);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            })
        );
        UI.uiThreads.get(UI.uiThreads.size() - 1).start();
    }

    /**
     * 让"循环水泵"的启停单选钮与运行灯跟随**当前选中的凝汽器**。
     *
     * <p>凝结水页的 TG-A / TG-B 各有一个 1–4 的凝汽器选择器。原来切换只刷新 LCD，
     * 单选钮和运行灯仍显示上一个凝汽器的状态（看起来像"切换没生效"）。</p>
     */
    private static void syncCirculationPumpUi(Pump pump, javax.swing.JRadioButton start,
            javax.swing.JRadioButton stop, eu.hansolo.steelseries.extras.Led led) {
        if (pump == null) {
            return;
        }
        boolean on = pump.isActive();
        if (start != null && start.isSelected() != on) {
            if (on) {
                start.setSelected(true);
            } else if (stop != null) {
                stop.setSelected(true);
            }
        }
        if (led != null) {
            led.setLedOn(on);
        }
    }

    /** 按当前单位显示一个量（数值换算 + 单位字符串） */
    private static void showUnit(eu.hansolo.steelseries.gauges.Lcd lcd, Units.Quantity q, double si) {
        lcd.setLcdValue(Units.convert(q, si));
        lcd.setLcdUnitString(Units.unitOf(q));
        lcd.setLcdUnitStringVisible(true);
    }

    @Override
    public void setVisibility(boolean visible) {
        this.setVisible(visible);
    }
    
    @Override
    public void discard() {
        this.setVisible(false);
    }
    
    @Override
    public void acknowledge() {
        annunciator.acknowledge();
    }
    
    private void checkAlarms() {
        if (tg1.condenser.getWaterLevel() < -25) {
            annunciator.trigger(hw1Low);
        } else {
            annunciator.reset(hw1Low);
        }
        if (tg2.condenser.getWaterLevel() < -25) {
            annunciator.trigger(hw2Low);
        } else {
            annunciator.reset(hw2Low);
        }
        if (tg1.condenser.getWaterLevel() > 25) {
            annunciator.trigger(hw1High);
        } else {
            annunciator.reset(hw1High);
        }
        if (tg2.condenser.getWaterLevel() > 25) {
            annunciator.trigger(hw2High);
        } else {
            annunciator.reset(hw2High);
        }
        if (tg1.condenser.maxPressure() > 0.023) {
            annunciator.trigger(lowVacuum1);
        } else {
            annunciator.reset(lowVacuum1);
        }
        if (tg2.condenser.maxPressure() > 0.023) {
            annunciator.trigger(lowVacuum2);
        } else {
            annunciator.reset(lowVacuum2);
        }
        if (tg1.condenser.maxPressure() > 0.10142) {
            annunciator.trigger(relief1);
        } else {
            annunciator.reset(relief1);
        }
        if (tg2.condenser.maxPressure() > 0.10142) {
            annunciator.trigger(relief2);
        } else {
            annunciator.reset(relief2);
        }
        // 凝结水系统排水阀开启 → 红色报警（自动排水时提醒）
        annunciator.setTrigger(NPPSim.condensateDrainValve != null
                && NPPSim.condensateDrainValve.getPosition() > 0.01f, drainOpen);
        boolean cavitation1A = false;
        for (Pump pump: condensate1A) {
            if (pump.isCavitating) {
                cavitation1A = true;
                break;
            }
        }
        annunciator.setTrigger(cavitation1A, cavit1A);
        boolean cavitation2A = false;
        for (Pump pump: condensate2A) {
            if (pump.isCavitating) {
                cavitation2A = true;
                break;
            }
        }
        annunciator.setTrigger(cavitation2A, cavit2A);
        boolean cavitation1B = false;
        for (Pump pump: condensate1B) {
            if (pump.isCavitating) {
                cavitation1B = true;
                break;
            }
        }
        annunciator.setTrigger(cavitation1B, cavit1B);
        boolean cavitation2B = false;
        for (Pump pump: condensate2B) {
            if (pump.isCavitating) {
                cavitation2B = true;
                break;
            }
        }
        annunciator.setTrigger(cavitation2B, cavit2B);
    }
    
     /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        buttonGroup6 = new javax.swing.ButtonGroup();
        buttonGroup7 = new javax.swing.ButtonGroup();
        buttonGroup8 = new javax.swing.ButtonGroup();
        buttonGroup9 = new javax.swing.ButtonGroup();
        buttonGroup10 = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        totalFlow1 = new eu.hansolo.steelseries.gauges.Linear();
        jPanel8 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        start1A = new javax.swing.JRadioButton();
        stop1A = new javax.swing.JRadioButton();
        jLabel12 = new javax.swing.JLabel();
        spinner1A = new javax.swing.JSpinner();
        jLabel13 = new javax.swing.JLabel();
        rpm1A = new eu.hansolo.steelseries.gauges.DisplaySingle();
        amps1A = new eu.hansolo.steelseries.gauges.DisplaySingle();
        flow1A = new eu.hansolo.steelseries.gauges.DisplaySingle();
        power1A1 = new eu.hansolo.steelseries.extras.Led();
        power1A2 = new eu.hansolo.steelseries.extras.Led();
        power1A3 = new eu.hansolo.steelseries.extras.Led();
        jLabel17 = new javax.swing.JLabel();
        jPanel15 = new javax.swing.JPanel();
        start1B = new javax.swing.JRadioButton();
        stop1B = new javax.swing.JRadioButton();
        jLabel25 = new javax.swing.JLabel();
        spinner1B = new javax.swing.JSpinner();
        jLabel26 = new javax.swing.JLabel();
        rpm1B = new eu.hansolo.steelseries.gauges.DisplaySingle();
        amps1B = new eu.hansolo.steelseries.gauges.DisplaySingle();
        flow1B = new eu.hansolo.steelseries.gauges.DisplaySingle();
        jLabel27 = new javax.swing.JLabel();
        power1B3 = new eu.hansolo.steelseries.extras.Led();
        power1B2 = new eu.hansolo.steelseries.extras.Led();
        power1B1 = new eu.hansolo.steelseries.extras.Led();
        annunciatorPanel = new javax.swing.JPanel();
        hw1Low = new javax.swing.JTextField();
        hw1High = new javax.swing.JTextField();
        cavit1A = new javax.swing.JTextField();
        drainOpen = new javax.swing.JTextField();
        cavit2A = new javax.swing.JTextField();
        hw2Low = new javax.swing.JTextField();
        hw2High = new javax.swing.JTextField();
        lowVacuum1 = new javax.swing.JTextField();
        trip1A = new javax.swing.JTextField();
        cavit1B = new javax.swing.JTextField();
        cavit2B = new javax.swing.JTextField();
        trip2A = new javax.swing.JTextField();
        lowVacuum2 = new javax.swing.JTextField();
        relief1 = new javax.swing.JTextField();
        trip1B = new javax.swing.JTextField();
        jTextField11 = new javax.swing.JTextField();
        jTextField12 = new javax.swing.JTextField();
        trip2B = new javax.swing.JTextField();
        relief2 = new javax.swing.JTextField();
        totalFlow2 = new eu.hansolo.steelseries.gauges.Linear();
        jPanel16 = new javax.swing.JPanel();
        jLabel28 = new javax.swing.JLabel();
        jPanel17 = new javax.swing.JPanel();
        start2A = new javax.swing.JRadioButton();
        stop2A = new javax.swing.JRadioButton();
        jLabel29 = new javax.swing.JLabel();
        spinner2A = new javax.swing.JSpinner();
        jLabel30 = new javax.swing.JLabel();
        rpm2A = new eu.hansolo.steelseries.gauges.DisplaySingle();
        amps2A = new eu.hansolo.steelseries.gauges.DisplaySingle();
        flow2A = new eu.hansolo.steelseries.gauges.DisplaySingle();
        power2A1 = new eu.hansolo.steelseries.extras.Led();
        power2A2 = new eu.hansolo.steelseries.extras.Led();
        power2A3 = new eu.hansolo.steelseries.extras.Led();
        jLabel31 = new javax.swing.JLabel();
        jPanel18 = new javax.swing.JPanel();
        start2B = new javax.swing.JRadioButton();
        stop2B = new javax.swing.JRadioButton();
        jLabel32 = new javax.swing.JLabel();
        spinner2B = new javax.swing.JSpinner();
        jLabel33 = new javax.swing.JLabel();
        rpm2B = new eu.hansolo.steelseries.gauges.DisplaySingle();
        amps2B = new eu.hansolo.steelseries.gauges.DisplaySingle();
        flow2B = new eu.hansolo.steelseries.gauges.DisplaySingle();
        jLabel34 = new javax.swing.JLabel();
        power2B3 = new eu.hansolo.steelseries.extras.Led();
        power2B2 = new eu.hansolo.steelseries.extras.Led();
        power2B1 = new eu.hansolo.steelseries.extras.Led();
        hotwellLevel1 = new eu.hansolo.steelseries.gauges.Linear();
        hotwellLevel2 = new eu.hansolo.steelseries.gauges.Linear();
        totalInflow1 = new eu.hansolo.steelseries.gauges.Linear();
        totalInflow2 = new eu.hansolo.steelseries.gauges.Linear();
        jPanel19 = new javax.swing.JPanel();
        ejector1Start = new javax.swing.JRadioButton();
        ejector1Stop = new javax.swing.JRadioButton();
        jLabel35 = new javax.swing.JLabel();
        ejector1Spinner = new javax.swing.JSpinner();
        jLabel36 = new javax.swing.JLabel();
        SGAMFlow1 = new eu.hansolo.steelseries.gauges.DisplaySingle();
        ejectorFlow1 = new eu.hansolo.steelseries.gauges.DisplaySingle();
        jLabel37 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        vacuum1 = new eu.hansolo.steelseries.gauges.Linear();
        vacuum2 = new eu.hansolo.steelseries.gauges.Linear();
        jPanel20 = new javax.swing.JPanel();
        ejector2Start = new javax.swing.JRadioButton();
        ejector2Stop = new javax.swing.JRadioButton();
        jLabel38 = new javax.swing.JLabel();
        ejector2Spinner = new javax.swing.JSpinner();
        jLabel40 = new javax.swing.JLabel();
        SGAMFlow2 = new eu.hansolo.steelseries.gauges.DisplaySingle();
        ejectorFlow2 = new eu.hansolo.steelseries.gauges.DisplaySingle();
        jLabel41 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        jPanel21 = new javax.swing.JPanel();
        jLabel43 = new javax.swing.JLabel();
        jPanel22 = new javax.swing.JPanel();
        con1Start = new javax.swing.JRadioButton();
        con1Stop = new javax.swing.JRadioButton();
        jLabel45 = new javax.swing.JLabel();
        con1RPM = new eu.hansolo.steelseries.gauges.DisplaySingle();
        con1A = new eu.hansolo.steelseries.gauges.DisplaySingle();
        con1Flow = new eu.hansolo.steelseries.gauges.DisplaySingle();
        con1Outlet = new eu.hansolo.steelseries.gauges.DisplaySingle();
        con1Inlet = new eu.hansolo.steelseries.gauges.DisplaySingle();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel46 = new javax.swing.JLabel();
        con1Power = new eu.hansolo.steelseries.extras.Led();
        jPanel23 = new javax.swing.JPanel();
        con2Start = new javax.swing.JRadioButton();
        con2Stop = new javax.swing.JRadioButton();
        jLabel48 = new javax.swing.JLabel();
        con2RPM = new eu.hansolo.steelseries.gauges.DisplaySingle();
        con2A = new eu.hansolo.steelseries.gauges.DisplaySingle();
        con2Flow = new eu.hansolo.steelseries.gauges.DisplaySingle();
        jLabel49 = new javax.swing.JLabel();
        con2Power = new eu.hansolo.steelseries.extras.Led();
        con2Inlet = new eu.hansolo.steelseries.gauges.DisplaySingle();
        con2Outlet = new eu.hansolo.steelseries.gauges.DisplaySingle();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        condTemp1 = new eu.hansolo.steelseries.gauges.Linear();
        condTemp2 = new eu.hansolo.steelseries.gauges.Linear();
        jButton1 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel44 = new javax.swing.JLabel();
        flowControl = new javax.swing.JCheckBox();
        jPanel24 = new javax.swing.JPanel();
        out1 = new eu.hansolo.steelseries.gauges.Radial2Top();
        steamOutVOpen4 = new javax.swing.JRadioButton();
        steamOutVStop4 = new javax.swing.JRadioButton();
        steamOutVClose4 = new javax.swing.JRadioButton();
        jLabel18 = new javax.swing.JLabel();
        jPanel25 = new javax.swing.JPanel();
        out2 = new eu.hansolo.steelseries.gauges.Radial2Top();
        steamOutVOpen5 = new javax.swing.JRadioButton();
        steamOutVStop5 = new javax.swing.JRadioButton();
        steamOutVClose5 = new javax.swing.JRadioButton();
        jLabel19 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem10 = new javax.swing.JMenuItem();
        jMenuItem12 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();

        jScrollPane1.setPreferredSize(new java.awt.Dimension(1366, 768));

        jPanel3.setBackground(UI.BACKGROUND);
        jPanel3.setPreferredSize(new java.awt.Dimension(1366, 768));

        totalFlow1.setBackgroundColor(eu.hansolo.steelseries.tools.BackgroundColor.SATIN_GRAY);
        totalFlow1.setFrameVisible(false);
        totalFlow1.setLcdVisible(false);
        totalFlow1.setLedVisible(false);
        totalFlow1.setMaxValue(4000.0);
        totalFlow1.setThreshold(800.0);
        totalFlow1.setTitle(Localization.t("Total Condensate Outlow 1"));
        totalFlow1.setTrackSectionColor(new java.awt.Color(255, 0, 0));
        totalFlow1.setTrackStart(800.0);
        totalFlow1.setTrackStartColor(new java.awt.Color(255, 0, 0));
        totalFlow1.setTrackStop(1000.0);
        totalFlow1.setUnitString(Localization.t("CondensateUI.totalFlow1.unitString")); // NOI18N
        totalFlow1.setValueColor(eu.hansolo.steelseries.tools.ColorDef.YELLOW);

        jPanel8.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(222, 222, 222), 1, true));

        jLabel11.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, Localization.t("Condensate Pumps 1"));

        jPanel9.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(222, 222, 222), 1, true));

        buttonGroup1.add(start1A);
        org.openide.awt.Mnemonics.setLocalizedText(start1A, Localization.t("Start"));
        start1A.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                start1AItemStateChanged(evt);
            }
        });
        start1A.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                start1AActionPerformed(evt);
            }
        });

        buttonGroup1.add(stop1A);
        stop1A.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(stop1A, Localization.t("Stop"));
        stop1A.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                stop1AItemStateChanged(evt);
            }
        });
        stop1A.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stop1AActionPerformed(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel12, Localization.t("CondensateUI.jLabel12.text")); // NOI18N

        spinner1A.setModel(new javax.swing.SpinnerNumberModel(1, 1, 3, 1));
        spinner1A.setDoubleBuffered(true);

        jLabel13.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel13, Localization.t("CondensateUI.jLabel13.text")); // NOI18N

        rpm1A.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        rpm1A.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        rpm1A.setLcdUnitString(Localization.t("CondensateUI.rpm1A.lcdUnitString")); // NOI18N

        amps1A.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        amps1A.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        amps1A.setLcdUnitString(Localization.t("CondensateUI.amps1A.lcdUnitString")); // NOI18N

        flow1A.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        flow1A.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        flow1A.setLcdUnitString(Localization.t("CondensateUI.flow1A.lcdUnitString")); // NOI18N

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinner1A, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(start1A)
                            .addComponent(stop1A))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(amps1A, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(rpm1A, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(flow1A, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 12, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(spinner1A, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(start1A)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stop1A))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(rpm1A, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(amps1A, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(flow1A, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        power1A1.setPreferredSize(new java.awt.Dimension(20, 20));

        power1A2.setPreferredSize(new java.awt.Dimension(20, 20));

        power1A3.setPreferredSize(new java.awt.Dimension(20, 20));

        jLabel17.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel17, Localization.t("CondensateUI.jLabel17.text")); // NOI18N

        jPanel15.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(222, 222, 222), 1, true));

        buttonGroup2.add(start1B);
        org.openide.awt.Mnemonics.setLocalizedText(start1B, Localization.t("Start"));
        start1B.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                start1BItemStateChanged(evt);
            }
        });
        start1B.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                start1BActionPerformed(evt);
            }
        });

        buttonGroup2.add(stop1B);
        stop1B.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(stop1B, Localization.t("Stop"));
        stop1B.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                stop1BItemStateChanged(evt);
            }
        });
        stop1B.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stop1BActionPerformed(evt);
            }
        });

        jLabel25.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel25, Localization.t("CondensateUI.jLabel25.text")); // NOI18N

        spinner1B.setModel(new javax.swing.SpinnerNumberModel(1, 1, 4, 1));
        spinner1B.setDoubleBuffered(true);

        jLabel26.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel26, Localization.t("CondensateUI.jLabel26.text")); // NOI18N

        rpm1B.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        rpm1B.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        rpm1B.setLcdUnitString(Localization.t("CondensateUI.rpm1B.lcdUnitString")); // NOI18N

        amps1B.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        amps1B.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        amps1B.setLcdUnitString(Localization.t("CondensateUI.amps1B.lcdUnitString")); // NOI18N

        flow1B.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        flow1B.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        flow1B.setLcdUnitString(Localization.t("CondensateUI.flow1B.lcdUnitString")); // NOI18N

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel26)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addComponent(jLabel25)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinner1B, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(start1B)
                            .addComponent(stop1B))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(amps1B, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(rpm1B, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(flow1B, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 12, Short.MAX_VALUE))
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel26)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(spinner1B, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addComponent(start1B)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stop1B))
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addComponent(rpm1B, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(amps1B, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(flow1B, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel27.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel27, Localization.t("CondensateUI.jLabel27.text")); // NOI18N

        power1B3.setPreferredSize(new java.awt.Dimension(20, 20));

        power1B2.setPreferredSize(new java.awt.Dimension(20, 20));

        power1B1.setPreferredSize(new java.awt.Dimension(20, 20));

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel17)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(power1A1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(power1A2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(power1A3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel27)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(power1B1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(power1B2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(power1B3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 12, Short.MAX_VALUE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(power1A1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(power1A2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(power1A3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17)
                    .addComponent(power1B1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(power1B2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(power1B3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel27))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        annunciatorPanel.setLayout(new java.awt.GridLayout(3, 6));

        hw1Low.setEditable(false);
        hw1Low.setBackground(new java.awt.Color(142, 0, 0));
        hw1Low.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        hw1Low.setForeground(new java.awt.Color(0, 0, 0));
        hw1Low.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        hw1Low.setText(Localization.t("Hotwell 1 low"));
        hw1Low.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        hw1Low.setFocusable(false);
        annunciatorPanel.add(hw1Low);

        hw1High.setEditable(false);
        hw1High.setBackground(new java.awt.Color(142, 0, 0));
        hw1High.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        hw1High.setForeground(new java.awt.Color(0, 0, 0));
        hw1High.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        hw1High.setText(Localization.t("Hotwell 1 high"));
        hw1High.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        hw1High.setFocusable(false);
        annunciatorPanel.add(hw1High);

        cavit1A.setEditable(false);
        cavit1A.setBackground(new java.awt.Color(142, 0, 0));
        cavit1A.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        cavit1A.setForeground(new java.awt.Color(0, 0, 0));
        cavit1A.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        cavit1A.setText(Localization.t("1A Cavitation"));
        cavit1A.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        cavit1A.setFocusable(false);
        cavit1A.setPreferredSize(new java.awt.Dimension(100, 30));
        cavit1A.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cavit1AActionPerformed(evt);
            }
        });
        annunciatorPanel.add(cavit1A);

        cavit2A.setEditable(false);
        cavit2A.setBackground(new java.awt.Color(142, 0, 0));
        cavit2A.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        cavit2A.setForeground(new java.awt.Color(0, 0, 0));
        cavit2A.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        cavit2A.setText(Localization.t("CondensateUI.cavit2A.text")); // NOI18N
        cavit2A.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        cavit2A.setFocusable(false);
        annunciatorPanel.add(cavit2A);

        hw2Low.setEditable(false);
        hw2Low.setBackground(new java.awt.Color(142, 0, 0));
        hw2Low.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        hw2Low.setForeground(new java.awt.Color(0, 0, 0));
        hw2Low.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        hw2Low.setText(Localization.t("Hotwell 2 low"));
        hw2Low.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        hw2Low.setFocusable(false);
        hw2Low.setPreferredSize(new java.awt.Dimension(100, 30));
        hw2Low.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hw2LowActionPerformed(evt);
            }
        });
        annunciatorPanel.add(hw2Low);

        hw2High.setEditable(false);
        hw2High.setBackground(new java.awt.Color(142, 0, 0));
        hw2High.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        hw2High.setForeground(new java.awt.Color(0, 0, 0));
        hw2High.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        hw2High.setText(Localization.t("CondensateUI.hw2High.text")); // NOI18N
        hw2High.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        hw2High.setFocusable(false);
        annunciatorPanel.add(hw2High);

        lowVacuum1.setEditable(false);
        lowVacuum1.setBackground(new java.awt.Color(142, 0, 0));
        lowVacuum1.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        lowVacuum1.setForeground(new java.awt.Color(0, 0, 0));
        lowVacuum1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        lowVacuum1.setText(Localization.t("CondensateUI.lowVacuum1.text")); // NOI18N
        lowVacuum1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lowVacuum1.setFocusable(false);
        annunciatorPanel.add(lowVacuum1);

        trip1A.setEditable(false);
        trip1A.setBackground(new java.awt.Color(142, 0, 0));
        trip1A.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        trip1A.setForeground(new java.awt.Color(0, 0, 0));
        trip1A.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        trip1A.setText(Localization.t("1A trip"));
        trip1A.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        trip1A.setFocusable(false);
        annunciatorPanel.add(trip1A);

        cavit1B.setEditable(false);
        cavit1B.setBackground(new java.awt.Color(142, 0, 0));
        cavit1B.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        cavit1B.setForeground(new java.awt.Color(0, 0, 0));
        cavit1B.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        cavit1B.setText(Localization.t("CondensateUI.cavit1B.text")); // NOI18N
        cavit1B.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        cavit1B.setFocusable(false);
        cavit1B.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cavit1BActionPerformed(evt);
            }
        });
        annunciatorPanel.add(cavit1B);

        cavit2B.setEditable(false);
        cavit2B.setBackground(new java.awt.Color(142, 0, 0));
        cavit2B.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        cavit2B.setForeground(new java.awt.Color(0, 0, 0));
        cavit2B.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        cavit2B.setText(Localization.t("CondensateUI.cavit2B.text")); // NOI18N
        cavit2B.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        cavit2B.setFocusable(false);
        annunciatorPanel.add(cavit2B);

        // 凝结水系统排水阀开启（红）：自动排水时亮，提醒操作员
        drainOpen.setEditable(false);
        drainOpen.setBackground(new java.awt.Color(142, 0, 0));
        drainOpen.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        drainOpen.setForeground(new java.awt.Color(0, 0, 0));
        drainOpen.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        drainOpen.setText(Localization.t("Drain Valve Open"));
        drainOpen.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        drainOpen.setFocusable(false);
        annunciatorPanel.add(drainOpen);

        trip2A.setEditable(false);
        trip2A.setBackground(new java.awt.Color(142, 0, 0));
        trip2A.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        trip2A.setForeground(new java.awt.Color(0, 0, 0));
        trip2A.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        trip2A.setText(Localization.t("2A trip"));
        trip2A.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        trip2A.setFocusable(false);
        annunciatorPanel.add(trip2A);

        lowVacuum2.setEditable(false);
        lowVacuum2.setBackground(new java.awt.Color(142, 0, 0));
        lowVacuum2.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        lowVacuum2.setForeground(new java.awt.Color(0, 0, 0));
        lowVacuum2.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        lowVacuum2.setText(Localization.t("CondensateUI.lowVacuum2.text")); // NOI18N
        lowVacuum2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lowVacuum2.setFocusable(false);
        annunciatorPanel.add(lowVacuum2);

        relief1.setEditable(false);
        relief1.setBackground(new java.awt.Color(142, 0, 0));
        relief1.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        relief1.setForeground(new java.awt.Color(0, 0, 0));
        relief1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        relief1.setText(Localization.t("CondensateUI.relief1.text")); // NOI18N
        relief1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        relief1.setFocusable(false);
        relief1.setPreferredSize(new java.awt.Dimension(100, 30));
        relief1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                relief1ActionPerformed(evt);
            }
        });
        annunciatorPanel.add(relief1);

        trip1B.setEditable(false);
        trip1B.setBackground(new java.awt.Color(142, 0, 0));
        trip1B.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        trip1B.setForeground(new java.awt.Color(0, 0, 0));
        trip1B.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        trip1B.setText(Localization.t("1B trip"));
        trip1B.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        trip1B.setFocusable(false);
        annunciatorPanel.add(trip1B);

        jTextField11.setEditable(false);
        jTextField11.setBackground(new java.awt.Color(142, 0, 0));
        jTextField11.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jTextField11.setForeground(new java.awt.Color(0, 0, 0));
        jTextField11.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField11.setText(Localization.t("CondensateUI.jTextField11.text")); // NOI18N
        jTextField11.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jTextField11.setFocusable(false);
        jTextField11.setPreferredSize(new java.awt.Dimension(100, 30));
        jTextField11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField11ActionPerformed(evt);
            }
        });
        annunciatorPanel.add(jTextField11);

        jTextField12.setEditable(false);
        jTextField12.setBackground(new java.awt.Color(142, 0, 0));
        jTextField12.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jTextField12.setForeground(new java.awt.Color(0, 0, 0));
        jTextField12.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField12.setText(Localization.t("CondensateUI.jTextField12.text")); // NOI18N
        jTextField12.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jTextField12.setFocusable(false);
        jTextField12.setPreferredSize(new java.awt.Dimension(100, 30));
        jTextField12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField12ActionPerformed(evt);
            }
        });
        annunciatorPanel.add(jTextField12);

        trip2B.setEditable(false);
        trip2B.setBackground(new java.awt.Color(142, 0, 0));
        trip2B.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        trip2B.setForeground(new java.awt.Color(0, 0, 0));
        trip2B.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        trip2B.setText(Localization.t("2B trip"));
        trip2B.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        trip2B.setFocusable(false);
        trip2B.setPreferredSize(new java.awt.Dimension(100, 30));
        trip2B.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trip2BActionPerformed(evt);
            }
        });
        annunciatorPanel.add(trip2B);

        relief2.setEditable(false);
        relief2.setBackground(new java.awt.Color(142, 0, 0));
        relief2.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        relief2.setForeground(new java.awt.Color(0, 0, 0));
        relief2.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        relief2.setText(Localization.t("CondensateUI.relief2.text")); // NOI18N
        relief2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        relief2.setFocusable(false);
        relief2.setPreferredSize(new java.awt.Dimension(100, 30));
        relief2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                relief2ActionPerformed(evt);
            }
        });
        annunciatorPanel.add(relief2);

        totalFlow2.setBackgroundColor(eu.hansolo.steelseries.tools.BackgroundColor.SATIN_GRAY);
        totalFlow2.setFrameVisible(false);
        totalFlow2.setLcdVisible(false);
        totalFlow2.setLedVisible(false);
        totalFlow2.setMaxValue(4000.0);
        totalFlow2.setThreshold(800.0);
        totalFlow2.setTitle(Localization.t("Total Condensate Outflow 2"));
        totalFlow2.setTrackSectionColor(new java.awt.Color(255, 0, 0));
        totalFlow2.setTrackStart(800.0);
        totalFlow2.setTrackStartColor(new java.awt.Color(255, 0, 0));
        totalFlow2.setTrackStop(1000.0);
        totalFlow2.setUnitString(Localization.t("CondensateUI.totalFlow2.unitString")); // NOI18N
        totalFlow2.setValueColor(eu.hansolo.steelseries.tools.ColorDef.YELLOW);

        jPanel16.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(222, 222, 222), 1, true));

        jLabel28.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel28, Localization.t("Condensate Pumps 2"));

        jPanel17.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(222, 222, 222), 1, true));

        buttonGroup3.add(start2A);
        org.openide.awt.Mnemonics.setLocalizedText(start2A, Localization.t("Start"));
        start2A.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                start2AItemStateChanged(evt);
            }
        });
        start2A.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                start2AActionPerformed(evt);
            }
        });

        buttonGroup3.add(stop2A);
        stop2A.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(stop2A, Localization.t("Stop"));
        stop2A.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                stop2AItemStateChanged(evt);
            }
        });
        stop2A.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stop2AActionPerformed(evt);
            }
        });

        jLabel29.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel29, Localization.t("CondensateUI.jLabel29.text")); // NOI18N

        spinner2A.setModel(new javax.swing.SpinnerNumberModel(1, 1, 3, 1));
        spinner2A.setDoubleBuffered(true);

        jLabel30.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel30, Localization.t("CondensateUI.jLabel30.text")); // NOI18N

        rpm2A.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        rpm2A.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        rpm2A.setLcdUnitString(Localization.t("CondensateUI.rpm2A.lcdUnitString")); // NOI18N

        amps2A.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        amps2A.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        amps2A.setLcdUnitString(Localization.t("CondensateUI.amps2A.lcdUnitString")); // NOI18N

        flow2A.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        flow2A.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        flow2A.setLcdUnitString(Localization.t("CondensateUI.flow2A.lcdUnitString")); // NOI18N

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel30)
                    .addGroup(jPanel17Layout.createSequentialGroup()
                        .addComponent(jLabel29)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinner2A, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel17Layout.createSequentialGroup()
                        .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(start2A)
                            .addComponent(stop2A))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(amps2A, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(rpm2A, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(flow2A, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 12, Short.MAX_VALUE))
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel30)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel29)
                    .addComponent(spinner2A, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel17Layout.createSequentialGroup()
                        .addComponent(start2A)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stop2A))
                    .addGroup(jPanel17Layout.createSequentialGroup()
                        .addComponent(rpm2A, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(amps2A, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(flow2A, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        power2A1.setPreferredSize(new java.awt.Dimension(20, 20));

        power2A2.setPreferredSize(new java.awt.Dimension(20, 20));

        power2A3.setPreferredSize(new java.awt.Dimension(20, 20));

        jLabel31.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel31, Localization.t("CondensateUI.jLabel31.text")); // NOI18N

        jPanel18.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(222, 222, 222), 1, true));

        buttonGroup4.add(start2B);
        org.openide.awt.Mnemonics.setLocalizedText(start2B, Localization.t("Start"));
        start2B.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                start2BItemStateChanged(evt);
            }
        });
        start2B.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                start2BActionPerformed(evt);
            }
        });

        buttonGroup4.add(stop2B);
        stop2B.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(stop2B, Localization.t("Stop"));
        stop2B.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                stop2BItemStateChanged(evt);
            }
        });
        stop2B.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stop2BActionPerformed(evt);
            }
        });

        jLabel32.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel32, Localization.t("CondensateUI.jLabel32.text")); // NOI18N

        spinner2B.setModel(new javax.swing.SpinnerNumberModel(1, 1, 3, 1));
        spinner2B.setDoubleBuffered(true);

        jLabel33.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel33, Localization.t("CondensateUI.jLabel33.text")); // NOI18N

        rpm2B.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        rpm2B.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        rpm2B.setLcdUnitString(Localization.t("CondensateUI.rpm2B.lcdUnitString")); // NOI18N

        amps2B.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        amps2B.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        amps2B.setLcdUnitString(Localization.t("CondensateUI.amps2B.lcdUnitString")); // NOI18N

        flow2B.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        flow2B.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        flow2B.setLcdUnitString(Localization.t("CondensateUI.flow2B.lcdUnitString")); // NOI18N

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel33)
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addComponent(jLabel32)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinner2B, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(start2B)
                            .addComponent(stop2B))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(amps2B, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(rpm2B, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(flow2B, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 12, Short.MAX_VALUE))
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel33)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel32)
                    .addComponent(spinner2B, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addComponent(start2B)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stop2B))
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addComponent(rpm2B, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(amps2B, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(flow2B, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel34.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel34, Localization.t("CondensateUI.jLabel34.text")); // NOI18N

        power2B3.setPreferredSize(new java.awt.Dimension(20, 20));

        power2B2.setPreferredSize(new java.awt.Dimension(20, 20));

        power2B1.setPreferredSize(new java.awt.Dimension(20, 20));

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel16Layout.createSequentialGroup()
                                .addComponent(jLabel31)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(power2A1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(power2A2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(power2A3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel16Layout.createSequentialGroup()
                                .addComponent(jLabel34)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(power2B1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(power2B2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(power2B3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 12, Short.MAX_VALUE))
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addComponent(jLabel28)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(power2A1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(power2A2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(power2A3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel31)
                    .addComponent(power2B1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(power2B2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(power2B3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel34))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        hotwellLevel1.setBackgroundColor(eu.hansolo.steelseries.tools.BackgroundColor.SATIN_GRAY);
        hotwellLevel1.setFrameVisible(false);
        hotwellLevel1.setLcdVisible(false);
        hotwellLevel1.setLedVisible(false);
        hotwellLevel1.setMinValue(-150.0);
        hotwellLevel1.setTitle(Localization.t("Hotwell Level 1"));
        hotwellLevel1.setTrackSectionColor(new java.awt.Color(255, 0, 0));
        hotwellLevel1.setTrackStart(800.0);
        hotwellLevel1.setTrackStartColor(new java.awt.Color(255, 0, 0));
        hotwellLevel1.setUnitString("%");
        hotwellLevel1.setValueColor(eu.hansolo.steelseries.tools.ColorDef.YELLOW);

        hotwellLevel2.setBackgroundColor(eu.hansolo.steelseries.tools.BackgroundColor.SATIN_GRAY);
        hotwellLevel2.setFrameVisible(false);
        hotwellLevel2.setLcdVisible(false);
        hotwellLevel2.setLedVisible(false);
        hotwellLevel2.setMinValue(-150.0);
        hotwellLevel2.setTitle(Localization.t("Hotwell Level 2"));
        hotwellLevel2.setTrackSectionColor(new java.awt.Color(255, 0, 0));
        hotwellLevel2.setTrackStart(800.0);
        hotwellLevel2.setTrackStartColor(new java.awt.Color(255, 0, 0));
        hotwellLevel2.setUnitString("%");
        hotwellLevel2.setValueColor(eu.hansolo.steelseries.tools.ColorDef.YELLOW);

        totalInflow1.setBackgroundColor(eu.hansolo.steelseries.tools.BackgroundColor.SATIN_GRAY);
        totalInflow1.setFrameVisible(false);
        totalInflow1.setLcdVisible(false);
        totalInflow1.setLedVisible(false);
        totalInflow1.setMaxValue(4000.0);
        totalInflow1.setThreshold(800.0);
        totalInflow1.setTitle(Localization.t("Total Condensate Inlow 1"));
        totalInflow1.setTrackSectionColor(new java.awt.Color(255, 0, 0));
        totalInflow1.setTrackStart(800.0);
        totalInflow1.setTrackStartColor(new java.awt.Color(255, 0, 0));
        totalInflow1.setTrackStop(1000.0);
        totalInflow1.setUnitString("kg/s");
        totalInflow1.setValueColor(eu.hansolo.steelseries.tools.ColorDef.YELLOW);

        totalInflow2.setBackgroundColor(eu.hansolo.steelseries.tools.BackgroundColor.SATIN_GRAY);
        totalInflow2.setFrameVisible(false);
        totalInflow2.setLcdVisible(false);
        totalInflow2.setLedVisible(false);
        totalInflow2.setMaxValue(4000.0);
        totalInflow2.setThreshold(800.0);
        totalInflow2.setTitle(Localization.t("Total Condensate Inflow 2"));
        totalInflow2.setTrackSectionColor(new java.awt.Color(255, 0, 0));
        totalInflow2.setTrackStart(800.0);
        totalInflow2.setTrackStartColor(new java.awt.Color(255, 0, 0));
        totalInflow2.setTrackStop(1000.0);
        totalInflow2.setUnitString(Localization.t("CondensateUI.totalInflow2.unitString")); // NOI18N
        totalInflow2.setValueColor(eu.hansolo.steelseries.tools.ColorDef.YELLOW);

        jPanel19.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(222, 222, 222), 1, true));

        buttonGroup5.add(ejector1Start);
        org.openide.awt.Mnemonics.setLocalizedText(ejector1Start, Localization.t("CondensateUI.ejector1Start.text")); // NOI18N
        ejector1Start.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                ejector1StartItemStateChanged(evt);
            }
        });
        ejector1Start.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ejector1StartActionPerformed(evt);
            }
        });

        buttonGroup5.add(ejector1Stop);
        ejector1Stop.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(ejector1Stop, Localization.t("CondensateUI.ejector1Stop.text")); // NOI18N
        ejector1Stop.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                ejector1StopItemStateChanged(evt);
            }
        });
        ejector1Stop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ejector1StopActionPerformed(evt);
            }
        });

        jLabel35.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel35, Localization.t("Ejector Number"));

        ejector1Spinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, 4, 1));

        jLabel36.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel36, Localization.t("Steam Ejectors 1"));

        SGAMFlow1.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        SGAMFlow1.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        SGAMFlow1.setLcdDecimals(2);
        SGAMFlow1.setLcdUnitString(Localization.t("kg/s"));

        ejectorFlow1.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        ejectorFlow1.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        ejectorFlow1.setLcdDecimals(2);
        ejectorFlow1.setLcdUnitString(Localization.t("kg/s"));

        jLabel37.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel37, Localization.t("Steam Flow"));

        jLabel39.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel39, Localization.t("SGAM Flow"));

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel36)
                    .addGroup(jPanel19Layout.createSequentialGroup()
                        .addComponent(jLabel35)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ejector1Spinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel19Layout.createSequentialGroup()
                        .addComponent(ejector1Start)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(ejector1Stop))
                    .addGroup(jPanel19Layout.createSequentialGroup()
                        .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ejectorFlow1, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel37))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel39)
                            .addComponent(SGAMFlow1, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jLabel36)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ejector1Spinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel35))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ejector1Start)
                    .addComponent(ejector1Stop))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel19Layout.createSequentialGroup()
                        .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel37)
                            .addComponent(jLabel39))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ejectorFlow1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(SGAMFlow1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        vacuum1.setBackgroundColor(eu.hansolo.steelseries.tools.BackgroundColor.SATIN_GRAY);
        vacuum1.setFrameVisible(false);
        vacuum1.setLcdVisible(false);
        vacuum1.setLedVisible(false);
        vacuum1.setMaxNoOfMajorTicks(15);
        vacuum1.setMaxNoOfMinorTicks(5);
        vacuum1.setMaxValue(150.0);
        vacuum1.setTitle(Localization.t("Condenser vacuum 1"));
        vacuum1.setTrackSectionColor(java.awt.Color.red);
        vacuum1.setTrackStart(23.0);
        vacuum1.setTrackStartColor(java.awt.Color.red);
        vacuum1.setTrackStop(150.0);
        vacuum1.setTrackStopColor(java.awt.Color.red);
        vacuum1.setTrackVisible(true);
        vacuum1.setUnitString("kPa");
        vacuum1.setValueColor(eu.hansolo.steelseries.tools.ColorDef.YELLOW);

        vacuum2.setBackgroundColor(eu.hansolo.steelseries.tools.BackgroundColor.SATIN_GRAY);
        vacuum2.setFrameVisible(false);
        vacuum2.setLcdVisible(false);
        vacuum2.setLedVisible(false);
        vacuum2.setMaxNoOfMajorTicks(15);
        vacuum2.setMaxNoOfMinorTicks(5);
        vacuum2.setMaxValue(150.0);
        vacuum2.setTitle(Localization.t("Condenser Vacuum 2"));
        vacuum2.setTrackSectionColor(java.awt.Color.red);
        vacuum2.setTrackStart(23.0);
        vacuum2.setTrackStartColor(java.awt.Color.red);
        vacuum2.setTrackStop(150.0);
        vacuum2.setTrackStopColor(java.awt.Color.red);
        vacuum2.setTrackVisible(true);
        vacuum2.setUnitString("kPa");
        vacuum2.setValueColor(eu.hansolo.steelseries.tools.ColorDef.YELLOW);

        jPanel20.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(222, 222, 222), 1, true));

        buttonGroup6.add(ejector2Start);
        org.openide.awt.Mnemonics.setLocalizedText(ejector2Start, Localization.t("CondensateUI.ejector2Start.text")); // NOI18N
        ejector2Start.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                ejector2StartItemStateChanged(evt);
            }
        });
        ejector2Start.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ejector2StartActionPerformed(evt);
            }
        });

        buttonGroup6.add(ejector2Stop);
        ejector2Stop.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(ejector2Stop, Localization.t("CondensateUI.ejector2Stop.text")); // NOI18N
        ejector2Stop.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                ejector2StopItemStateChanged(evt);
            }
        });
        ejector2Stop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ejector2StopActionPerformed(evt);
            }
        });

        jLabel38.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel38, Localization.t("Ejector Number"));

        ejector2Spinner.setModel(new javax.swing.SpinnerNumberModel(5, 5, 8, 1));

        jLabel40.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel40, Localization.t("Steam Ejectors 2"));

        SGAMFlow2.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        SGAMFlow2.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        SGAMFlow2.setLcdDecimals(2);
        SGAMFlow2.setLcdUnitString(Localization.t("kg/s"));

        ejectorFlow2.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        ejectorFlow2.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        ejectorFlow2.setLcdDecimals(2);
        ejectorFlow2.setLcdUnitString(Localization.t("kg/s"));

        jLabel41.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel41, Localization.t("CondensateUI.jLabel41.text")); // NOI18N

        jLabel42.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel42, Localization.t("CondensateUI.jLabel42.text")); // NOI18N

        javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
        jPanel20.setLayout(jPanel20Layout);
        jPanel20Layout.setHorizontalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel40)
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addComponent(jLabel38)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ejector2Spinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addComponent(ejector2Start)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(ejector2Stop))
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ejectorFlow2, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel41))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel42)
                            .addComponent(SGAMFlow2, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel20Layout.setVerticalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jLabel40)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ejector2Spinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel38))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ejector2Start)
                    .addComponent(ejector2Stop))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel20Layout.createSequentialGroup()
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel41)
                            .addComponent(jLabel42))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ejectorFlow2, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(SGAMFlow2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel21.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(222, 222, 222), 1, true));

        jLabel43.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel43, Localization.t("Condenser Circulating Water Pumps"));

        jPanel22.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(222, 222, 222), 1, true));

        buttonGroup7.add(con1Start);
        org.openide.awt.Mnemonics.setLocalizedText(con1Start, Localization.t("CondensateUI.con1Start.text")); // NOI18N
        con1Start.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                con1StartItemStateChanged(evt);
            }
        });
        con1Start.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                con1StartActionPerformed(evt);
            }
        });

        buttonGroup7.add(con1Stop);
        con1Stop.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(con1Stop, Localization.t("CondensateUI.con1Stop.text")); // NOI18N
        con1Stop.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                con1StopItemStateChanged(evt);
            }
        });
        con1Stop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                con1StopActionPerformed(evt);
            }
        });

        jLabel45.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel45, Localization.t("TG-A Condensers"));

        con1RPM.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        con1RPM.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        con1RPM.setLcdUnitString(Localization.t("CondensateUI.con1RPM.lcdUnitString")); // NOI18N

        con1A.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        con1A.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        con1A.setLcdUnitString(Localization.t("CondensateUI.con1A.lcdUnitString")); // NOI18N

        con1Flow.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        con1Flow.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        con1Flow.setLcdUnitString(Localization.t("CondensateUI.con1Flow.lcdUnitString")); // NOI18N

        con1Outlet.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        con1Outlet.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        con1Outlet.setLcdUnitString(Localization.t("°C"));
        con1Outlet.setLcdValue(15.0);

        con1Inlet.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        con1Inlet.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        con1Inlet.setLcdUnitString(Localization.t("°C"));
        con1Inlet.setLcdValue(15.0);

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, Localization.t("Inlet"));

        jLabel2.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, Localization.t("Outlet") + "\n");

        jLabel46.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel46, Localization.t("CondensateUI.jLabel46.text")); // NOI18N

        con1Power.setPreferredSize(new java.awt.Dimension(20, 20));

        javax.swing.GroupLayout jPanel22Layout = new javax.swing.GroupLayout(jPanel22);
        jPanel22.setLayout(jPanel22Layout);
        jPanel22Layout.setHorizontalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel22Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel22Layout.createSequentialGroup()
                        .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel22Layout.createSequentialGroup()
                                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(con1Inlet, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(con1Outlet, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel22Layout.createSequentialGroup()
                                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(con1Start)
                                        .addComponent(con1Stop))
                                    .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel22Layout.createSequentialGroup()
                                            .addGap(12, 12, 12)
                                            .addComponent(con1Power, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jLabel46)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(con1A, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(con1RPM, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(con1Flow, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap())
                    .addGroup(jPanel22Layout.createSequentialGroup()
                        .addComponent(jLabel45)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel22Layout.setVerticalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel22Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel45)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel22Layout.createSequentialGroup()
                        .addComponent(con1Start)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(con1Stop)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel46))
                    .addGroup(jPanel22Layout.createSequentialGroup()
                        .addComponent(con1RPM, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(con1A, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(con1Flow, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(con1Power, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel22Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(con1Inlet, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel22Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(con1Outlet, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jPanel23.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(222, 222, 222), 1, true));

        buttonGroup8.add(con2Start);
        org.openide.awt.Mnemonics.setLocalizedText(con2Start, Localization.t("CondensateUI.con2Start.text")); // NOI18N
        con2Start.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                con2StartItemStateChanged(evt);
            }
        });
        con2Start.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                con2StartActionPerformed(evt);
            }
        });

        buttonGroup8.add(con2Stop);
        con2Stop.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(con2Stop, Localization.t("CondensateUI.con2Stop.text")); // NOI18N
        con2Stop.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                con2StopItemStateChanged(evt);
            }
        });
        con2Stop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                con2StopActionPerformed(evt);
            }
        });

        jLabel48.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel48, Localization.t("TG-B Condensers"));

        con2RPM.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        con2RPM.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        con2RPM.setLcdUnitString(Localization.t("CondensateUI.con2RPM.lcdUnitString")); // NOI18N

        con2A.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        con2A.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        con2A.setLcdUnitString(Localization.t("CondensateUI.con2A.lcdUnitString")); // NOI18N

        con2Flow.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        con2Flow.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        con2Flow.setLcdUnitString(Localization.t("CondensateUI.con2Flow.lcdUnitString")); // NOI18N

        jLabel49.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel49, Localization.t("CondensateUI.jLabel49.text")); // NOI18N

        con2Power.setPreferredSize(new java.awt.Dimension(20, 20));

        con2Inlet.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        con2Inlet.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        con2Inlet.setLcdUnitString(Localization.t("CondensateUI.con2Inlet.lcdUnitString")); // NOI18N
        con2Inlet.setLcdValue(15.0);

        con2Outlet.setCustomLcdForeground(new java.awt.Color(204, 204, 255));
        con2Outlet.setLcdColor(eu.hansolo.steelseries.tools.LcdColor.LIGHTBLUE_LCD);
        con2Outlet.setLcdUnitString(Localization.t("CondensateUI.con2Outlet.lcdUnitString")); // NOI18N
        con2Outlet.setLcdValue(15.0);

        jLabel3.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, Localization.t("Outlet") + "\n");

        jLabel4.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, Localization.t("Inlet"));

        javax.swing.GroupLayout jPanel23Layout = new javax.swing.GroupLayout(jPanel23);
        jPanel23.setLayout(jPanel23Layout);
        jPanel23Layout.setHorizontalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel23Layout.createSequentialGroup()
                        .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel48)
                            .addGroup(jPanel23Layout.createSequentialGroup()
                                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(con2Start)
                                    .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(con2Stop)
                                        .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel23Layout.createSequentialGroup()
                                                .addGap(12, 12, 12)
                                                .addComponent(con2Power, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(jLabel49))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(con2A, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(con2RPM, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(con2Flow, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 24, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel23Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(con2Inlet, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(con2Outlet, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())))
        );
        jPanel23Layout.setVerticalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel48)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel23Layout.createSequentialGroup()
                        .addComponent(con2Start)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(con2Stop)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel49))
                    .addGroup(jPanel23Layout.createSequentialGroup()
                        .addComponent(con2RPM, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(con2A, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(con2Flow, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(con2Power, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel23Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(con2Inlet, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel23Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(con2Outlet, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel21Layout = new javax.swing.GroupLayout(jPanel21);
        jPanel21.setLayout(jPanel21Layout);
        jPanel21Layout.setHorizontalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel21Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel21Layout.createSequentialGroup()
                        .addComponent(jPanel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel43))
                .addGap(0, 12, Short.MAX_VALUE))
        );
        jPanel21Layout.setVerticalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel21Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel43, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel22, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        condTemp1.setBackgroundColor(eu.hansolo.steelseries.tools.BackgroundColor.SATIN_GRAY);
        condTemp1.setCustomThresholdColor(null);
        condTemp1.setFrameVisible(false);
        condTemp1.setLcdVisible(false);
        condTemp1.setLedVisible(false);
        condTemp1.setMaxNoOfMajorTicks(15);
        condTemp1.setMaxNoOfMinorTicks(5);
        condTemp1.setMaximumSize(new java.awt.Dimension(315, 65));
        condTemp1.setMinimumSize(new java.awt.Dimension(315, 65));
        condTemp1.setTitle(Localization.t("Condensate Temperature 1"));
        condTemp1.setTrackSectionColor(java.awt.Color.red);
        condTemp1.setTrackStart(80.0);
        condTemp1.setTrackStartColor(java.awt.Color.red);
        condTemp1.setTrackStopColor(java.awt.Color.red);
        condTemp1.setTrackVisible(true);
        condTemp1.setUnitString("°C");
        condTemp1.setValueColor(eu.hansolo.steelseries.tools.ColorDef.YELLOW);

        condTemp2.setBackgroundColor(eu.hansolo.steelseries.tools.BackgroundColor.SATIN_GRAY);
        condTemp2.setCustomThresholdColor(null);
        condTemp2.setFrameVisible(false);
        condTemp2.setLcdVisible(false);
        condTemp2.setLedVisible(false);
        condTemp2.setMaxNoOfMajorTicks(15);
        condTemp2.setMaxNoOfMinorTicks(5);
        condTemp2.setMaximumSize(new java.awt.Dimension(315, 65));
        condTemp2.setMinimumSize(new java.awt.Dimension(315, 65));
        condTemp2.setTitle(Localization.t("Condensate Temperature 2"));
        condTemp2.setTrackSectionColor(java.awt.Color.red);
        condTemp2.setTrackStart(80.0);
        condTemp2.setTrackStartColor(java.awt.Color.red);
        condTemp2.setTrackStopColor(java.awt.Color.red);
        condTemp2.setTrackVisible(true);
        condTemp2.setUnitString("°C");
        condTemp2.setValueColor(eu.hansolo.steelseries.tools.ColorDef.YELLOW);

        jButton1.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton1, Localization.t("CondensateUI.jButton1.text")); // NOI18N
        jButton1.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButton1.setMaximumSize(new java.awt.Dimension(50, 50));
        jButton1.setMinimumSize(new java.awt.Dimension(50, 50));
        jButton1.setPreferredSize(new java.awt.Dimension(50, 50));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jPanel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(222, 222, 222), 1, true));

        jLabel44.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel44, Localization.t("CondensateUI.jLabel44.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(flowControl, Localization.t("CondensateUI.flowControl.text")); // NOI18N
        flowControl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flowControlActionPerformed(evt);
            }
        });

        jPanel24.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(222, 222, 222), 1, true));

        out1.setBackgroundColor(eu.hansolo.steelseries.tools.BackgroundColor.LIGHT_GRAY);
        out1.setFrameDesign(eu.hansolo.steelseries.tools.FrameDesign.TILTED_BLACK);
        out1.setFrameVisible(false);
        out1.setLcdBackgroundVisible(false);
        out1.setLcdVisible(false);
        out1.setLedVisible(false);
        out1.setPointerColor(eu.hansolo.steelseries.tools.ColorDef.BLACK);
        out1.setPointerShadowVisible(false);
        out1.setPreferredSize(new java.awt.Dimension(100, 100));
        out1.setTitle(Localization.t("CondensateUI.out1.title")); // NOI18N
        out1.setTitleAndUnitFont(new java.awt.Font("Verdana", 0, 8)); // NOI18N
        out1.setTitleAndUnitFontEnabled(true);
        out1.setUnitString(Localization.t("CondensateUI.out1.unitString")); // NOI18N

        buttonGroup9.add(steamOutVOpen4);
        org.openide.awt.Mnemonics.setLocalizedText(steamOutVOpen4, Localization.t("CondensateUI.steamOutVOpen4.text")); // NOI18N
        steamOutVOpen4.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                steamOutVOpen4ItemStateChanged(evt);
            }
        });
        steamOutVOpen4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                steamOutVOpen4ActionPerformed(evt);
            }
        });

        buttonGroup9.add(steamOutVStop4);
        steamOutVStop4.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(steamOutVStop4, Localization.t("CondensateUI.steamOutVStop4.text")); // NOI18N
        steamOutVStop4.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                steamOutVStop4ItemStateChanged(evt);
            }
        });
        steamOutVStop4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                steamOutVStop4ActionPerformed(evt);
            }
        });

        buttonGroup9.add(steamOutVClose4);
        org.openide.awt.Mnemonics.setLocalizedText(steamOutVClose4, Localization.t("CondensateUI.steamOutVClose4.text")); // NOI18N
        steamOutVClose4.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                steamOutVClose4ItemStateChanged(evt);
            }
        });
        steamOutVClose4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                steamOutVClose4ActionPerformed(evt);
            }
        });

        jLabel18.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel18, Localization.t("CondensateUI.jLabel18.text")); // NOI18N

        javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
        jPanel24.setLayout(jPanel24Layout);
        jPanel24Layout.setHorizontalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel24Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel18)
                    .addGroup(jPanel24Layout.createSequentialGroup()
                        .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(steamOutVOpen4)
                            .addComponent(steamOutVStop4)
                            .addComponent(steamOutVClose4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(out1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel24Layout.setVerticalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel24Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel18)
                .addGap(7, 7, 7)
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel24Layout.createSequentialGroup()
                        .addComponent(steamOutVOpen4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(steamOutVStop4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(steamOutVClose4))
                    .addComponent(out1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel25.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(222, 222, 222), 1, true));

        out2.setBackgroundColor(eu.hansolo.steelseries.tools.BackgroundColor.LIGHT_GRAY);
        out2.setFrameDesign(eu.hansolo.steelseries.tools.FrameDesign.TILTED_BLACK);
        out2.setFrameVisible(false);
        out2.setLcdBackgroundVisible(false);
        out2.setLcdVisible(false);
        out2.setLedVisible(false);
        out2.setPointerColor(eu.hansolo.steelseries.tools.ColorDef.BLACK);
        out2.setPointerShadowVisible(false);
        out2.setPreferredSize(new java.awt.Dimension(100, 100));
        out2.setTitle(Localization.t("CondensateUI.out2.title")); // NOI18N
        out2.setTitleAndUnitFont(new java.awt.Font("Verdana", 0, 8)); // NOI18N
        out2.setTitleAndUnitFontEnabled(true);
        out2.setUnitString(Localization.t("CondensateUI.out2.unitString")); // NOI18N

        buttonGroup10.add(steamOutVOpen5);
        org.openide.awt.Mnemonics.setLocalizedText(steamOutVOpen5, Localization.t("CondensateUI.steamOutVOpen5.text")); // NOI18N
        steamOutVOpen5.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                steamOutVOpen5ItemStateChanged(evt);
            }
        });
        steamOutVOpen5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                steamOutVOpen5ActionPerformed(evt);
            }
        });

        buttonGroup10.add(steamOutVStop5);
        steamOutVStop5.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(steamOutVStop5, Localization.t("CondensateUI.steamOutVStop5.text")); // NOI18N
        steamOutVStop5.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                steamOutVStop5ItemStateChanged(evt);
            }
        });
        steamOutVStop5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                steamOutVStop5ActionPerformed(evt);
            }
        });

        buttonGroup10.add(steamOutVClose5);
        org.openide.awt.Mnemonics.setLocalizedText(steamOutVClose5, Localization.t("CondensateUI.steamOutVClose5.text")); // NOI18N
        steamOutVClose5.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                steamOutVClose5ItemStateChanged(evt);
            }
        });
        steamOutVClose5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                steamOutVClose5ActionPerformed(evt);
            }
        });

        jLabel19.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel19, Localization.t("CondensateUI.jLabel19.text")); // NOI18N

        javax.swing.GroupLayout jPanel25Layout = new javax.swing.GroupLayout(jPanel25);
        jPanel25.setLayout(jPanel25Layout);
        jPanel25Layout.setHorizontalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel25Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel19)
                    .addGroup(jPanel25Layout.createSequentialGroup()
                        .addGroup(jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(steamOutVOpen5)
                            .addComponent(steamOutVStop5)
                            .addComponent(steamOutVClose5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(out2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel25Layout.setVerticalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel25Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel19)
                .addGap(7, 7, 7)
                .addGroup(jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel25Layout.createSequentialGroup()
                        .addComponent(steamOutVOpen5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(steamOutVStop5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(steamOutVClose5))
                    .addComponent(out2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addComponent(jPanel24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel44)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(38, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(flowControl)
                        .addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(12, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel25, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel44)
                            .addComponent(flowControl))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel24, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(annunciatorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(vacuumRow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(hotwellLevel1, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(totalInflow1, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(totalFlow1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(totalInflow2, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(totalFlow2, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                            .addComponent(hotwellLevel2, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(254, 254, 254)
                                            .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(vacuum1, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(condTemp1, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(vacuum2, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(condTemp2, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGap(612, 612, 612))))))
                .addContainerGap(97, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(annunciatorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(hotwellLevel1, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(hotwellLevel2, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(totalInflow1, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(totalInflow2, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(totalFlow1, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(totalFlow2, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(condTemp2, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(vacuum2, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(condTemp1, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(vacuum1, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(vacuumRow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel21, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel16, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel3);

        org.openide.awt.Mnemonics.setLocalizedText(jMenu1, Localization.t("CondensateUI.jMenu1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jMenuItem6, Localization.t("CondensateUI.jMenuItem6.text")); // NOI18N
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem6);

        org.openide.awt.Mnemonics.setLocalizedText(jMenuItem3, Localization.t("Core Map"));
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        org.openide.awt.Mnemonics.setLocalizedText(jMenuItem10, Localization.t("CondensateUI.jMenuItem10.text")); // NOI18N
        jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem10ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem10);

        org.openide.awt.Mnemonics.setLocalizedText(jMenuItem12, Localization.t("CondensateUI.jMenuItem12.text")); // NOI18N
        jMenuItem12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem12ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem12);

        org.openide.awt.Mnemonics.setLocalizedText(jMenuItem1, Localization.t("Turbine-Generators"));
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        org.openide.awt.Mnemonics.setLocalizedText(jMenuItem4, Localization.t("CondensateUI.jMenuItem4.text")); // NOI18N
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem4);

        org.openide.awt.Mnemonics.setLocalizedText(jMenuItem8, Localization.t("CondensateUI.jMenuItem8.text")); // NOI18N
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem8);

        org.openide.awt.Mnemonics.setLocalizedText(jMenuItem5, Localization.t("CondensateUI.jMenuItem5.text")); // NOI18N
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem5);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 747, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        UI.createOrContinue(CoreMap.class, false, false);
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        UI.createOrContinue(TGUI.class, true, false);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        UI.createOrContinue(DearatorUI.class, true, false);
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void ejector1StopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ejector1StopActionPerformed
        ejectors.get((int)ejector1Spinner.getValue() - 1).setState(0);
    }//GEN-LAST:event_ejector1StopActionPerformed

    private void ejector1StopItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_ejector1StopItemStateChanged
        //
    }//GEN-LAST:event_ejector1StopItemStateChanged

    private void ejector1StartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ejector1StartActionPerformed
        ejectors.get((int)ejector1Spinner.getValue() - 1).setState(2);
    }//GEN-LAST:event_ejector1StartActionPerformed

    private void ejector1StartItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_ejector1StartItemStateChanged
        //
    }//GEN-LAST:event_ejector1StartItemStateChanged

    private void stop2BActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stop2BActionPerformed
        condensate2B.get((int)spinner2B.getValue() - 1).setActive(false);
        switch((int)spinner2B.getValue()) {
            case 1:
            power2B1.setLedOn(false);
            break;
            case 2:
            power2B2.setLedOn(false);
            break;
            case 3:
            power2B3.setLedOn(false);
            break;
        }
    }//GEN-LAST:event_stop2BActionPerformed

    private void stop2BItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_stop2BItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_stop2BItemStateChanged

    private void start2BActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_start2BActionPerformed
        condensate2B.get((int)spinner2B.getValue() - 1).setActive(true);
        switch((int)spinner2B.getValue()) {
            case 1:
            power2B1.setLedOn(true);
            break;
            case 2:
            power2B2.setLedOn(true);
            break;
            case 3:
            power2B3.setLedOn(true);
            break;
        }
    }//GEN-LAST:event_start2BActionPerformed

    private void start2BItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_start2BItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_start2BItemStateChanged

    private void stop2AActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stop2AActionPerformed
        condensate2A.get((int)spinner2A.getValue() - 1).setActive(false);
        switch((int)spinner2A.getValue()) {
            case 1:
            power2A1.setLedOn(false);
            break;
            case 2:
            power2A2.setLedOn(false);
            break;
            case 3:
            power2A3.setLedOn(false);
            break;
        }
    }//GEN-LAST:event_stop2AActionPerformed

    private void stop2AItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_stop2AItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_stop2AItemStateChanged

    private void start2AActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_start2AActionPerformed
        condensate2A.get((int)spinner2A.getValue() - 1).setActive(true);
        switch((int)spinner2A.getValue()) {
            case 1:
            power2A1.setLedOn(true);
            break;
            case 2:
            power2A2.setLedOn(true);
            break;
            case 3:
            power2A3.setLedOn(true);
            break;
        }
    }//GEN-LAST:event_start2AActionPerformed

    private void start2AItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_start2AItemStateChanged
        //TODO
    }//GEN-LAST:event_start2AItemStateChanged

    private void relief2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_relief2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_relief2ActionPerformed

    private void relief1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_relief1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_relief1ActionPerformed

    private void jTextField11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField11ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField11ActionPerformed

    private void trip2BActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trip2BActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_trip2BActionPerformed

    private void cavit1BActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cavit1BActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cavit1BActionPerformed

    private void cavit1AActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cavit1AActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cavit1AActionPerformed

    private void hw2LowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hw2LowActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_hw2LowActionPerformed

    private void stop1BActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stop1BActionPerformed
        condensate1B.get((int)spinner1B.getValue() - 1).setActive(false);
        switch((int)spinner1B.getValue()) {
            case 1:
            power1B1.setLedOn(false);
            break;
            case 2:
            power1B2.setLedOn(false);
            break;
            case 3:
            power1B3.setLedOn(false);
            break;
        }
    }//GEN-LAST:event_stop1BActionPerformed

    private void stop1BItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_stop1BItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_stop1BItemStateChanged

    private void start1BActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_start1BActionPerformed
        condensate1B.get((int)spinner1B.getValue() - 1).setActive(true);
        switch((int)spinner1B.getValue()) {
            case 1:
            power1B1.setLedOn(true);
            break;
            case 2:
            power1B2.setLedOn(true);
            break;
            case 3:
            power1B3.setLedOn(true);
            break;
        }
    }//GEN-LAST:event_start1BActionPerformed

    private void start1BItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_start1BItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_start1BItemStateChanged

    private void stop1AActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stop1AActionPerformed
        condensate1A.get((int)spinner1A.getValue() - 1).setActive(false);
        switch((int)spinner1A.getValue()) {
            case 1:
            power1A1.setLedOn(false);
            break;
            case 2:
            power1A2.setLedOn(false);
            break;
            case 3:
            power1A3.setLedOn(false);
            break;
        }
    }//GEN-LAST:event_stop1AActionPerformed

    private void stop1AItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_stop1AItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_stop1AItemStateChanged

    private void start1AActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_start1AActionPerformed
        condensate1A.get((int)spinner1A.getValue() - 1).setActive(true);
        switch((int)spinner1A.getValue()) {
            case 1:
            power1A1.setLedOn(true);
            break;
            case 2:
            power1A2.setLedOn(true);
            break;
            case 3:
            power1A3.setLedOn(true);
            break;
        }
    }//GEN-LAST:event_start1AActionPerformed

    private void start1AItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_start1AItemStateChanged
        //TODO
    }//GEN-LAST:event_start1AItemStateChanged

    private void ejector2StartItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_ejector2StartItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_ejector2StartItemStateChanged

    private void ejector2StartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ejector2StartActionPerformed
        ejectors.get((int)ejector2Spinner.getValue() - 1).setState(2);
    }//GEN-LAST:event_ejector2StartActionPerformed

    private void ejector2StopItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_ejector2StopItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_ejector2StopItemStateChanged

    private void ejector2StopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ejector2StopActionPerformed
        ejectors.get((int)ejector2Spinner.getValue() - 1).setState(0);
    }//GEN-LAST:event_ejector2StopActionPerformed

    private void con1StartItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_con1StartItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_con1StartItemStateChanged

    private void con1StartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_con1StartActionPerformed
        condA().condenserPump.setActive(true);
        con1Power.setLedOn(true);
    }//GEN-LAST:event_con1StartActionPerformed

    private void con1StopItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_con1StopItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_con1StopItemStateChanged

    private void con1StopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_con1StopActionPerformed
        condA().condenserPump.setActive(false);
        con1Power.setLedOn(false);
    }//GEN-LAST:event_con1StopActionPerformed

    private void con2StartItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_con2StartItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_con2StartItemStateChanged

    private void con2StartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_con2StartActionPerformed
        condB().condenserPump.setActive(true);
        con2Power.setLedOn(true);
    }//GEN-LAST:event_con2StartActionPerformed

    private void con2StopItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_con2StopItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_con2StopItemStateChanged

    private void con2StopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_con2StopActionPerformed
        condB().condenserPump.setActive(false);
        con2Power.setLedOn(false);
    }//GEN-LAST:event_con2StopActionPerformed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        UI.createOrContinue(FeedwaterUI.class, true, false);
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        annunciator.acknowledge();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        UI.createOrContinue(PCSUI.class, true, false);
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jTextField12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField12ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField12ActionPerformed

    private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem10ActionPerformed
        UI.createOrContinue(SelsynPanel.class, false, false);
    }//GEN-LAST:event_jMenuItem10ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        NPPSim.ui.toFront();
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void steamOutVOpen4ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_steamOutVOpen4ItemStateChanged
        
    }//GEN-LAST:event_steamOutVOpen4ItemStateChanged

    private void steamOutVOpen4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_steamOutVOpen4ActionPerformed
        condensate1B.forEach(pump -> {
            pump.dischargeValve.setAutoState(1);
            pump.dischargeValve.setState(2);
        });
    }//GEN-LAST:event_steamOutVOpen4ActionPerformed

    private void steamOutVStop4ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_steamOutVStop4ItemStateChanged
        
    }//GEN-LAST:event_steamOutVStop4ItemStateChanged

    private void steamOutVStop4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_steamOutVStop4ActionPerformed
        condensate1B.forEach(pump -> {
            pump.dischargeValve.setAutoState(1);
            pump.dischargeValve.setState(1);
        });
    }//GEN-LAST:event_steamOutVStop4ActionPerformed

    private void steamOutVClose4ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_steamOutVClose4ItemStateChanged
        
    }//GEN-LAST:event_steamOutVClose4ItemStateChanged

    private void steamOutVOpen5ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_steamOutVOpen5ItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_steamOutVOpen5ItemStateChanged

    private void steamOutVOpen5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_steamOutVOpen5ActionPerformed
        condensate2B.forEach(pump -> {
            pump.dischargeValve.setAutoState(1);
            pump.dischargeValve.setState(2);
        });
    }//GEN-LAST:event_steamOutVOpen5ActionPerformed

    private void steamOutVStop5ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_steamOutVStop5ItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_steamOutVStop5ItemStateChanged

    private void steamOutVStop5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_steamOutVStop5ActionPerformed
        condensate2B.forEach(pump -> {
            pump.dischargeValve.setAutoState(1);
            pump.dischargeValve.setState(1);
        });
    }//GEN-LAST:event_steamOutVStop5ActionPerformed

    private void steamOutVClose5ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_steamOutVClose5ItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_steamOutVClose5ItemStateChanged

    private void steamOutVClose4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_steamOutVClose4ActionPerformed
        condensate1B.forEach(pump -> {
            pump.dischargeValve.setAutoState(1);
            pump.dischargeValve.setState(0);
        });
    }//GEN-LAST:event_steamOutVClose4ActionPerformed

    private void steamOutVClose5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_steamOutVClose5ActionPerformed
        condensate2B.forEach(pump -> {
            pump.dischargeValve.setAutoState(1);
            pump.dischargeValve.setState(0);
        });
    }//GEN-LAST:event_steamOutVClose5ActionPerformed

    private void flowControlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_flowControlActionPerformed
        if (flowControl.isSelected()) {
            autoControl.condenserWaterLevelControl.get(0).setEnabled(true);
            autoControl.condenserWaterLevelControl.get(1).setEnabled(true);
        } else {
            autoControl.condenserWaterLevelControl.get(0).setEnabled(false);
            autoControl.condenserWaterLevelControl.get(1).setEnabled(false);
        }
    }//GEN-LAST:event_flowControlActionPerformed

    private void jMenuItem12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem12ActionPerformed
        UI.createOrContinue(MCPUI.class, true, false);
    }//GEN-LAST:event_jMenuItem12ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private eu.hansolo.steelseries.gauges.DisplaySingle SGAMFlow1;
    private eu.hansolo.steelseries.gauges.DisplaySingle SGAMFlow2;
    private eu.hansolo.steelseries.gauges.DisplaySingle amps1A;
    private eu.hansolo.steelseries.gauges.DisplaySingle amps1B;
    private eu.hansolo.steelseries.gauges.DisplaySingle amps2A;
    private eu.hansolo.steelseries.gauges.DisplaySingle amps2B;
    private javax.swing.JPanel annunciatorPanel;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup10;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.ButtonGroup buttonGroup6;
    private javax.swing.ButtonGroup buttonGroup7;
    private javax.swing.ButtonGroup buttonGroup8;
    private javax.swing.ButtonGroup buttonGroup9;
    private javax.swing.JTextField cavit1A;
    /** 凝结水系统排水阀开启报警灯（左上角 annunciator） */
    private javax.swing.JTextField drainOpen;
    private javax.swing.JTextField cavit1B;
    private javax.swing.JTextField cavit2A;
    private javax.swing.JTextField cavit2B;
    private eu.hansolo.steelseries.gauges.DisplaySingle con1A;
    private eu.hansolo.steelseries.gauges.DisplaySingle con1Flow;
    private eu.hansolo.steelseries.gauges.DisplaySingle con1Inlet;
    private eu.hansolo.steelseries.gauges.DisplaySingle con1Outlet;
    private eu.hansolo.steelseries.extras.Led con1Power;
    private eu.hansolo.steelseries.gauges.DisplaySingle con1RPM;
    private javax.swing.JRadioButton con1Start;
    private javax.swing.JRadioButton con1Stop;
    private eu.hansolo.steelseries.gauges.DisplaySingle con2A;
    private eu.hansolo.steelseries.gauges.DisplaySingle con2Flow;
    private eu.hansolo.steelseries.gauges.DisplaySingle con2Inlet;
    private eu.hansolo.steelseries.gauges.DisplaySingle con2Outlet;
    private eu.hansolo.steelseries.extras.Led con2Power;
    private eu.hansolo.steelseries.gauges.DisplaySingle con2RPM;
    private javax.swing.JRadioButton con2Start;
    private javax.swing.JRadioButton con2Stop;
    private eu.hansolo.steelseries.gauges.Linear condTemp1;
    private eu.hansolo.steelseries.gauges.Linear condTemp2;
    private javax.swing.JSpinner ejector1Spinner;
    private javax.swing.JRadioButton ejector1Start;
    private javax.swing.JRadioButton ejector1Stop;
    private javax.swing.JSpinner ejector2Spinner;
    private javax.swing.JRadioButton ejector2Start;
    private javax.swing.JRadioButton ejector2Stop;
    private eu.hansolo.steelseries.gauges.DisplaySingle ejectorFlow1;
    private eu.hansolo.steelseries.gauges.DisplaySingle ejectorFlow2;
    private eu.hansolo.steelseries.gauges.DisplaySingle flow1A;
    private eu.hansolo.steelseries.gauges.DisplaySingle flow1B;
    private eu.hansolo.steelseries.gauges.DisplaySingle flow2A;
    private eu.hansolo.steelseries.gauges.DisplaySingle flow2B;
    private javax.swing.JCheckBox flowControl;
    private eu.hansolo.steelseries.gauges.Linear hotwellLevel1;
    private eu.hansolo.steelseries.gauges.Linear hotwellLevel2;
    private javax.swing.JTextField hw1High;
    private javax.swing.JTextField hw1Low;
    private javax.swing.JTextField hw2High;
    private javax.swing.JTextField hw2Low;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem12;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField11;
    private javax.swing.JTextField jTextField12;
    private javax.swing.JTextField lowVacuum1;
    private javax.swing.JTextField lowVacuum2;
    private eu.hansolo.steelseries.gauges.Radial2Top out1;
    private eu.hansolo.steelseries.gauges.Radial2Top out2;
    private eu.hansolo.steelseries.extras.Led power1A1;
    private eu.hansolo.steelseries.extras.Led power1A2;
    private eu.hansolo.steelseries.extras.Led power1A3;
    private eu.hansolo.steelseries.extras.Led power1B1;
    private eu.hansolo.steelseries.extras.Led power1B2;
    private eu.hansolo.steelseries.extras.Led power1B3;
    private eu.hansolo.steelseries.extras.Led power2A1;
    private eu.hansolo.steelseries.extras.Led power2A2;
    private eu.hansolo.steelseries.extras.Led power2A3;
    private eu.hansolo.steelseries.extras.Led power2B1;
    private eu.hansolo.steelseries.extras.Led power2B2;
    private eu.hansolo.steelseries.extras.Led power2B3;
    private javax.swing.JTextField relief1;
    private javax.swing.JTextField relief2;
    private eu.hansolo.steelseries.gauges.DisplaySingle rpm1A;
    private eu.hansolo.steelseries.gauges.DisplaySingle rpm1B;
    private eu.hansolo.steelseries.gauges.DisplaySingle rpm2A;
    private eu.hansolo.steelseries.gauges.DisplaySingle rpm2B;
    private javax.swing.JSpinner spinner1A;
    private javax.swing.JSpinner spinner1B;
    private javax.swing.JSpinner spinner2A;
    private javax.swing.JSpinner spinner2B;
    private javax.swing.JRadioButton start1A;
    private javax.swing.JRadioButton start1B;
    private javax.swing.JRadioButton start2A;
    private javax.swing.JRadioButton start2B;
    private javax.swing.JRadioButton steamOutVClose4;
    private javax.swing.JRadioButton steamOutVClose5;
    private javax.swing.JRadioButton steamOutVOpen4;
    private javax.swing.JRadioButton steamOutVOpen5;
    private javax.swing.JRadioButton steamOutVStop4;
    private javax.swing.JRadioButton steamOutVStop5;
    private javax.swing.JRadioButton stop1A;
    private javax.swing.JRadioButton stop1B;
    private javax.swing.JRadioButton stop2A;
    private javax.swing.JRadioButton stop2B;
    private eu.hansolo.steelseries.gauges.Linear totalFlow1;
    private eu.hansolo.steelseries.gauges.Linear totalFlow2;
    private eu.hansolo.steelseries.gauges.Linear totalInflow1;
    private eu.hansolo.steelseries.gauges.Linear totalInflow2;
    private javax.swing.JTextField trip1A;
    private javax.swing.JTextField trip1B;
    private javax.swing.JTextField trip2A;
    private javax.swing.JTextField trip2B;
    private eu.hansolo.steelseries.gauges.Linear vacuum1;
    private eu.hansolo.steelseries.gauges.Linear vacuum2;
    // End of variables declaration//GEN-END:variables
}
