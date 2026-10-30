package com.darwish.nppsim;

import static com.darwish.nppsim.Loader.soundProvider;
import static com.darwish.nppsim.NPPSim.TG1InletValves;
import static com.darwish.nppsim.NPPSim.TG2InletValves;
import static com.darwish.nppsim.NPPSim.auxFeederValves;
import static com.darwish.nppsim.NPPSim.condensate1B;
import static com.darwish.nppsim.NPPSim.condensate2B;
import static com.darwish.nppsim.NPPSim.core;
import static com.darwish.nppsim.NPPSim.dearatorValves;
import static com.darwish.nppsim.NPPSim.mainFeederValves;
import static com.darwish.nppsim.NPPSim.msvLoop1;
import static com.darwish.nppsim.NPPSim.msvLoop2;
import static com.darwish.nppsim.NPPSim.pcs;
import static com.darwish.nppsim.NPPSim.sdv_a;
import static com.darwish.nppsim.NPPSim.sdv_c;
import java.io.Serializable;
import java.util.ArrayList;
import static com.darwish.nppsim.NPPSim.mcc;
import static com.darwish.nppsim.NPPSim.tg1;
import static com.darwish.nppsim.NPPSim.tg2;
import java.util.Arrays;

public class AutoControl extends Component {
    ArrayList<String> eventLog = new ArrayList<>();
    ArrayList<OutflowWaterLevelControl> condenserWaterLevelControl = new ArrayList<>();
    ArrayList<InflowWaterLevelControl> dearatorWaterControl = new ArrayList<>();
    ArrayList<InflowWaterLevelControl> dearatorMakeupControl = new ArrayList<>();
    ArrayList<InflowWaterLevelControl> dearatorWaterAndMakeupControl = new ArrayList<>();
    ArrayList<InletSteamPressureControl> dearatorPressureControl = new ArrayList<>();
    ArrayList<InflowWaterLevelControl> auxFeederControl = new ArrayList<>();
    ArrayList<InflowWaterLevelControl> mainFeederControl = new ArrayList<>();
    ArrayList<OutletSteamPressureControl> sdv_cControl = new ArrayList<>();
    ArrayList<OutletSteamPressureControl> sdv_aControl = new ArrayList<>();
    ArrayList<OutletSteamPressureControl> tgValveControl = new ArrayList<>();
    ArrayList<OutletSteamPressureControl> msv1Control = new ArrayList<>();
    ArrayList<OutletSteamPressureControl> msv2Control = new ArrayList<>();
    AZ1Control az1Control;
    /** 自动 AZ 总开关（反应堆窗口离线冷却面板下方的开关）：关闭后 AZ-1~AZ-5 不再自动跳闸 SCRAM。
     *  UI 在 EDT 上写、仿真线程在读，因此用 volatile 保证关闭动作立刻对仿真可见。 */
    public volatile boolean azAutoEnabled = true;

    /**
     * 功率保护阶梯（MW，额定热功率 4800 MW）。
     * 约束：AZ3 &lt; AZ4 &lt; AZ5 &lt; AZ1，低定值必须先动作，否则高定值永远不会被触发。
     *   AZ-3 5200 MW = 108.3%（备用警声 alarm3）
     *   AZ-4 5400 MW = 112.5%
     *   AZ-5 5600 MW = 116.7%
     *   AZ-1 5800 MW = 120.8%（独立的高功率跳闸）
     */
    static final double AZ3_POWER_LIMIT_MW = 5200.0;
    static final double AZ4_POWER_LIMIT_MW = 5400.0;
    /** AZ-6：功率 5600 MW（116.7%）。AZ-5 是手动停堆按钮，没有自动功率定值。 */
    static final double AZ6_POWER_LIMIT_MW = 5600.0;
    static final double AZ1_POWER_LIMIT_MW = 5800.0;
    AZPowerProtection az2Control, az3Control, az4Control, az5Control, az6Control;
    FASRControl fasrControl;
    FluidAutomaticRodController automaticRodController, lepController;
    //AutomaticRodController ar1Control, ar2Control, ar12Control, larControl;
    ArrayList<ControlRodChannel> ar1 = new ArrayList<>();
    ArrayList<ControlRodChannel> ar2 = new ArrayList<>();
    ArrayList<ControlRodChannel> lar = new ArrayList<>();
    private long simulationTime = 0; //simulation time in seconds
    private boolean timeUpdated = false;

    public AutoControl() {
        condenserWaterLevelControl.add(new OutflowWaterLevelControl(tg1.condenser, new WaterValve[] {condensate1B.get(0).dischargeValve, condensate1B.get(1).dischargeValve, condensate1B.get(2).dischargeValve}));
        condenserWaterLevelControl.add(new OutflowWaterLevelControl(tg2.condenser, new WaterValve[] {condensate2B.get(0).dischargeValve, condensate2B.get(1).dischargeValve, condensate2B.get(2).dischargeValve}));
        dearatorValves.forEach(valve -> {
            dearatorWaterControl.add(new InflowWaterLevelControl(valve.drain, new WaterValve[] {valve}));
            dearatorPressureControl.add(new InletSteamPressureControl(valve.drain, new SteamValve[] {((Dearator)valve.drain).steamInlet}));
        });
        short valveIterator[] = {0};
        pcs.dearatorMakeupValves.forEach(valve -> {
            dearatorMakeupControl.add(new InflowWaterLevelControl(valve.drain, new WaterValve[] {valve}));
            dearatorWaterAndMakeupControl.add(new InflowWaterLevelControl(valve.drain, new WaterValve[] {dearatorWaterControl.get(valveIterator[0]).valveArray[0], valve}));
            valveIterator[0]++;
        });
        auxFeederControl.add(new InflowWaterLevelControl(mcc.drum1, new WaterValve[] {auxFeederValves.get(0)}));
        auxFeederControl.add(new InflowWaterLevelControl(mcc.drum2, new WaterValve[] {auxFeederValves.get(1)}));
        mainFeederControl.add(new InflowWaterLevelControl(
            mcc.drum1,
            new WaterValve[] {mainFeederValves.get(0), mainFeederValves.get(1), mainFeederValves.get(2)})
        );
        mainFeederControl.add(new InflowWaterLevelControl(
            mcc.drum2,
            new WaterValve[] {mainFeederValves.get(3), mainFeederValves.get(4), mainFeederValves.get(5)})
        );
        sdv_cControl.add(new OutletSteamPressureControl(mcc.drum1, new SteamValve[] {sdv_c.get(0), sdv_c.get(1), sdv_c.get(2), sdv_c.get(3), sdv_c.get(4), sdv_c.get(5), sdv_c.get(6), sdv_c.get(7)}));
        sdv_cControl.add(new OutletSteamPressureControl(mcc.drum2, new SteamValve[] {sdv_c.get(0), sdv_c.get(1), sdv_c.get(2),sdv_c.get(3), sdv_c.get(4), sdv_c.get(5), sdv_c.get(6), sdv_c.get(7)}));
        sdv_aControl.add(new OutletSteamPressureControl(mcc.drum1, new SteamValve[] {sdv_a.get(0)}));
        sdv_aControl.add(new OutletSteamPressureControl(mcc.drum2, new SteamValve[] {sdv_a.get(1)}));
        msv1Control.add(new OutletSteamPressureControl(mcc.drum1, new SteamValve[] {msvLoop1.get(0)}));
        msv1Control.add(new OutletSteamPressureControl(mcc.drum1, new SteamValve[] {msvLoop1.get(1), msvLoop1.get(2)}));
        msv1Control.add(new OutletSteamPressureControl(mcc.drum1, new SteamValve[] {msvLoop1.get(3), msvLoop1.get(4), msvLoop1.get(5)}));
        msv2Control.add(new OutletSteamPressureControl(mcc.drum2, new SteamValve[] {msvLoop2.get(0)}));
        msv2Control.add(new OutletSteamPressureControl(mcc.drum2, new SteamValve[] {msvLoop2.get(1), msvLoop2.get(2)}));
        msv2Control.add(new OutletSteamPressureControl(mcc.drum2, new SteamValve[] {msvLoop2.get(3), msvLoop2.get(4), msvLoop2.get(5)}));
        tgValveControl.add(new OutletSteamPressureControl(mcc.drum1, new SteamValve[] {TG1InletValves.get(0), TG1InletValves.get(1)}));
        tgValveControl.add(new OutletSteamPressureControl(mcc.drum2, new SteamValve[] {TG1InletValves.get(0), TG1InletValves.get(1)}));
        tgValveControl.add(new OutletSteamPressureControl(mcc.drum1, new SteamValve[] {TG2InletValves.get(0), TG2InletValves.get(1)}));
        tgValveControl.add(new OutletSteamPressureControl(mcc.drum2, new SteamValve[] {TG2InletValves.get(0), TG2InletValves.get(1)}));
        tgValveControl.forEach(controller -> {
            controller.setpoint = 6.90;
        });
        dearatorPressureControl.forEach(controller -> {
            controller.setpoint = 1.0;
        });
        sdv_cControl.forEach(controller -> {
            controller.setEnabled(true);
            controller.setpoint = 6.98; 
            controller.activationTreshold = 6.98;
        });
        sdv_aControl.forEach(controller -> {
        controller.setEnabled(true);
        controller.setpoint = 7.06; 
        controller.activationTreshold = 7.06;
        });
        //activate msv controllers and set setpoints for group 1(0), 2(1-2), 3(3-5)
        msv1Control.forEach(controller -> {
        controller.setEnabled(true);
        });
        msv1Control.get(0).setpoint = 7.36; 
        msv1Control.get(0).activationTreshold = 7.36; 
        msv1Control.get(1).setpoint = 7.45; 
        msv1Control.get(1).activationTreshold = 7.45; 
        msv1Control.get(2).setpoint = 7.55; 
        msv1Control.get(2).activationTreshold = 7.55; 
        
        msv2Control.forEach(controller -> {
        controller.setEnabled(true);
        });
        msv2Control.get(0).setpoint = 7.36;
        msv2Control.get(0).activationTreshold = 7.36; 
        msv2Control.get(1).setpoint = 7.45;
        msv2Control.get(1).activationTreshold = 7.45; 
        msv2Control.get(2).setpoint = 7.55;
        msv2Control.get(2).activationTreshold = 7.55; 
        
        az1Control = new AZ1Control();
        // AZ-2~AZ-5 功率/周期分层保护：越限依次点亮对应指示灯并触发 SCRAM。
        // 功率阶梯见上方 AZ3/AZ4/AZ5/AZ1_POWER_LIMIT_MW 常量（5200 < 5400 < 5600 < 5800）。
        // 关闭「自动 AZ」后，这些自动保护全部不动作；面板上的手动 AZ 按钮仍然有效。
        az2Control = new AZPowerProtection("AZ-2", 20.0, 0.0, true, false);     // 中子周期 < 20 s（上升过快）→ 备用警声 alarm3
        az3Control = new AZPowerProtection("AZ-3", 0.0, AZ3_POWER_LIMIT_MW, true, false);   // 热功率 > 5200 MW（108.3%）
        az4Control = new AZPowerProtection("AZ-4", 0.0, AZ4_POWER_LIMIT_MW, false, false);  // 热功率 > 5400 MW（112.5%）
        az5Control = new AZPowerProtection("AZ-5", 0.0, Double.MAX_VALUE, false, true);     // 手动停堆（紧急停堆，全插）
        az6Control = new AZPowerProtection("AZ-6", 0.0, AZ6_POWER_LIMIT_MW, false, false);  // 热功率 > 5600 MW（116.7%），面板灯红色组
        fasrControl = new FASRControl();

        core.coreArray.forEach(row -> {
            row.forEach(channel -> {
                if (channel instanceof ACRChannel) {
                    ar1.add((ControlRodChannel)channel);
                } else if (channel instanceof SACRChannel) {
                    ar2.add((ControlRodChannel)channel);
                } else if (channel instanceof LACChannel) {
                    lar.add((ControlRodChannel)channel);
                }
            });
        });
        automaticRodController = new FluidAutomaticRodController();
    }

    public void update() {
        sdv_c.forEach(valve -> {
            valve.setLocked(valve.drain.getPressure() > 0.023 || valve.drain.getSteamTemperature() > 100.0);
        });
        if(sdv_cControl.get(0).isEnabled()) {
            Runnable controller = mcc.drum1.getPressure() > mcc.drum2.getPressure() ? sdv_cControl.get(0)::update: sdv_cControl.get(1)::update;
            controller.run();
        }
        if(tgValveControl.get(0).isEnabled()) {
            Runnable controller = mcc.drum1.getPressure() > mcc.drum2.getPressure() ? tgValveControl.get(0)::update: tgValveControl.get(1)::update;
            controller.run();
        }
        if(tgValveControl.get(2).isEnabled()) {
            Runnable controller = mcc.drum1.getPressure() > mcc.drum2.getPressure() ? tgValveControl.get(2)::update: tgValveControl.get(3)::update;
            controller.run();
        }
        condenserWaterLevelControl.forEach(controller -> {
            if(controller.isEnabled()) {
                controller.update();
            }
        });
        for (short i = 0; i < 4; i++) {
            if (dearatorWaterAndMakeupControl.get(i).isEnabled()) {
                dearatorWaterAndMakeupControl.get(i).update();
            } else if (dearatorWaterControl.get(i).isEnabled()) {
                dearatorWaterControl.get(i).update();
            } else if (dearatorMakeupControl.get(i).isEnabled()) {
                dearatorMakeupControl.get(i).update();
            }
        }
        dearatorPressureControl.forEach(controller -> {
            if (controller.isEnabled()) {
                controller.update();
            }
        });
        auxFeederControl.forEach(controller -> {
            if (controller.isEnabled()) {
                controller.update();
            }
        });
        mainFeederControl.forEach(controller -> {
            if(controller.isEnabled()) {
                controller.update();
            }
        });
        sdv_aControl.forEach(controller -> {
            if(controller.isEnabled()) {
                controller.update();
            }
        });
        msv1Control.forEach(controller -> {
            if(controller.isEnabled()) {
                controller.update();
            }
        });
        msv2Control.forEach(controller -> {
            if(controller.isEnabled()) {
                controller.update();
            }
        });
        if (azAutoEnabled) {
            if (az1Control.isEnabled()) {
                az1Control.update();
            }
            az2Control.update();
            az3Control.update();
            az4Control.update();
            az6Control.update();
            // AZ-5 为手动停堆，无自动定值：不在这里判断
        }
        fasrControl.update();
        automaticRodController.update();
    }

    /**
     * 面板"复位 AZ"：复位**全部**保护（AZ-1K / BAZ / AZ-2 / AZ-3 / AZ-4 / AZ-5）。
     * 之前这里只复位了 AZ-1K 与 BAZ，AZ-2~AZ-5 的 tripped 永远不清 → 它们的警声也永远不会停，
     * 表现为"AZ 声音关断有延迟/关不掉"。另外顺手把残留的音频请求清干净，确保按下即静音。
     */
    public void resetAllAz() {
        az1Control.reset();
        fasrControl.reset();
        az2Control.reset();
        az3Control.reset();
        az4Control.reset();
        az5Control.reset();
        az6Control.reset();
        // 兜底：任何一方因异常残留的跳闸声请求都释放掉（正常路径下 reset() 已逐个释放）
        soundProvider.releaseAll(az1Control);
        soundProvider.releaseAll(fasrControl);
        soundProvider.releaseAll(az2Control);
        soundProvider.releaseAll(az3Control);
        soundProvider.releaseAll(az4Control);
        soundProvider.releaseAll(az6Control);
    }

    class InflowWaterLevelControl implements Serializable {
        WaterValve[] valveArray;
        Connectable target;
        private boolean enabled = false;
        protected double previousLevel, currentLevel, deltaLevel, deltaLevelSetpoint, setpoint = 0.0;
    
        public InflowWaterLevelControl(Connectable target, WaterValve[] valveArray) {
            this.valveArray = valveArray;
            this.target = target;
        }
        
        public void setWaterValveStateByLevel(double level, double setPoint) {
            
        }
        
        public void setWaterValveStateByLevel(double level) {
            setWaterValveStateByLevel(level, 0.0);
        }
    
        public void update() {
            currentLevel = target.getWaterLevel();
            deltaLevel = currentLevel - previousLevel;
            deltaLevelSetpoint = (setpoint - currentLevel) / 200;
            if (deltaLevel > deltaLevelSetpoint + 0.001) {
                for (WaterValve valve: valveArray) {
                    valve.setAutoState(0);
                }
            } else if (deltaLevel < deltaLevelSetpoint - 0.001) {
                for (WaterValve valve: valveArray) {
                    valve.setAutoState(2);
                }
            } else {
                for (WaterValve valve: valveArray) {
                    valve.setAutoState(1);
                }
            }
            previousLevel = currentLevel;
        }
    
        public void setSetpoint(double setpoint) {
            this.setpoint = setpoint;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
            if (!enabled) {
                for (WaterValve valve: valveArray) {
                    valve.setAutoState(1);
                }
            }
        }
    }
    
    class OutflowWaterLevelControl extends InflowWaterLevelControl implements Serializable {
        
        public OutflowWaterLevelControl(Connectable target, WaterValve[] valveArray) {
            super(target, valveArray);
        }
        
        @Override
        public void update() {
            currentLevel = target.getWaterLevel();
            deltaLevel = currentLevel - previousLevel;
            deltaLevelSetpoint = (setpoint - currentLevel) / 200;
            if (deltaLevel > deltaLevelSetpoint + 0.001) {
                for (WaterValve valve: valveArray) {
                    valve.setAutoState(2);
                }
            } else if (deltaLevel < deltaLevelSetpoint - 0.001) {
                for (WaterValve valve: valveArray) {
                    valve.setAutoState(0);
                }
            } else {
                for (WaterValve valve: valveArray) {
                    valve.setAutoState(1);
                }
            }
            previousLevel = currentLevel;
        }
        
    }
     
    class InletSteamPressureControl implements Serializable {
        SteamValve[] valveArray;
        Connectable target;
        private boolean enabled = false;
        protected double previousPressure, currentPressure, deltaPressure, deltaPressureSetpoint, setpoint = 0.0;

        public InletSteamPressureControl(Connectable target, SteamValve[] valveArray) {
            this.valveArray = valveArray;
            this.target = target;
        }

        public void update() {
            currentPressure = target.getPressure();
            deltaPressure = currentPressure - previousPressure;
            deltaPressureSetpoint = (setpoint - currentPressure) / 200;
            if (deltaPressure > deltaPressureSetpoint + 0.0001) {
                for (SteamValve valve: valveArray) {
                    valve.setAutoState(0);
                }
            } else if (deltaPressure < deltaPressureSetpoint - 0.0001) {
                for (SteamValve valve: valveArray) {
                    valve.setAutoState(2);
                }
            } else {
                for (SteamValve valve: valveArray) {
                    valve.setAutoState(1);
                }
            }
            previousPressure = currentPressure;
        }

        public void setSetpoint(double setpoint) {
            this.setpoint = setpoint;
        }
        
        public double getSetpoint() {
            return setpoint;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
            if (!enabled) {
                for (SteamValve valve: valveArray) {
                    valve.setAutoState(1);
                }
            }
        }
    }

    class OutletSteamPressureControl extends InletSteamPressureControl {
        private double activationTreshold = 0.0;

        public OutletSteamPressureControl(Connectable target, SteamValve[] valveArray) {
            super(target, valveArray);
        }

        @Override
        public void update() {
            currentPressure = target.getPressure() - 0.02;
            deltaPressure = currentPressure - previousPressure;
            deltaPressureSetpoint = (setpoint - currentPressure) / 200;
            if (deltaPressure < deltaPressureSetpoint - 0.0001) {
                for (SteamValve valve: valveArray) {
                    valve.setAutoState(0);
                }
            } else if (deltaPressure > deltaPressureSetpoint + 0.0001 && (target.getPressure() > activationTreshold || valveArray[0].getPosition() != 0.0)) {
                for (SteamValve valve: valveArray) {
                    valve.setAutoState(2);
                }
            } else {
                for (SteamValve valve: valveArray) {
                    valve.setAutoState(1);
                }
            }
            previousPressure = currentPressure;
        } 

        public void setActivationTreshold(double treshold) {
            this.activationTreshold = treshold;
        }
    }
    
    /**
     * 是否为"玩家手动操控的棒"（Manual Control Rod / Manual Control Rod Modernized）。
     *
     * <p>用**精确类匹配**：ACR/LAC/LEP 分别 extends MCR/MCRM，但它们是自动棒，
     * 不该被当成玩家棒；只有 getClass() 正好是 MCRChannel / MCRMChannel 才是玩家手动棒。</p>
     */
    static boolean isManualRod(ControlRodChannel channel) {
        return channel.getClass() == MCRChannel.class || channel.getClass() == MCRMChannel.class;
    }

    /**
     * 全堆 SCRAM（插入控制棒）。
     *
     * @param scram      true = 插入，false = 复位/抽出
     * @param fullScram  true = 紧急停堆，插**全部**棒（含玩家手动棒）；
     *                    false = 自动 AZ 保护，只插自动棒/FASR，跳过玩家手动棒
     *                    （否则玩家选中的手动棒被锁死，无法再操控/干预）。
     */
    void scramAllRods(boolean scram, boolean fullScram) {
        core.coreArray.forEach(row -> {
            row.forEach(channel -> {
                if (channel instanceof ControlRodChannel) {
                    ControlRodChannel rod = (ControlRodChannel) channel;
                    if (!fullScram && scram && isManualRod(rod)) {
                        return;
                    }
                    rod.setAutoState(scram ? 2 : 1);
                    rod.setState(scram ? 2 : 1);
                    rod.setScram(scram);
                }
            });
        });
    }

    class AZ1Control implements Serializable {
        private boolean tripped = false;
        boolean persistentReactivity = false;
        private boolean enabled = true;
        
        public void update() {
            // 「自动 AZ」关闭时不做任何自动跳闸判断；手动 trip() 不受影响。
            if (tripped || !azAutoEnabled) {
                return;
            }
            if (mcc.drum1.getPressure() > 7.26 || mcc.drum2.getPressure() > 7.26) {
                trip("High Drum Pressure");
            }
            if (mcc.drum1.getWaterLevel() > 30 || mcc.drum2.getWaterLevel() > 30 ) {
                trip("High Water Level");
            }
            if (mcc.drum1.getWaterLevel() < -40 || mcc.drum2.getWaterLevel() < -40) {
                trip("Low Water Level");
            }
            if (core.getReactivity() > 0.02) {
                if (persistentReactivity) {
                    trip("High Neutron Rate");
                    persistentReactivity = false;
                }
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                        persistentReactivity = true;
                    } catch (InterruptedException e) {
                        persistentReactivity = true;
                    }
                }).start();
            }
            if (core.getThermalPower() > AZ1_POWER_LIMIT_MW) {
                trip("High Thermal Power");
            }
        }
        
        public void trip(String reason) {
            if (tripped) {
                return;
            }
            soundProvider.request(soundProvider.ALARM_2, this);   // 以本保护为 owner：复位即时、不误停他人
            recordEvent(Localization.t("AZ-1 Trip Signal: ") + reason);
            tripped = true;
            // 紧急停堆（AZ-1）：插**全部**棒，含玩家手动棒
            scramAllRods(true, true);
        }   
        
        public void reset() {
            recordEvent(Localization.t("AZ-1 Reset"));
            tripped = false;
            soundProvider.release(soundProvider.ALARM_2, this);   // 立即静音（旧版有 500ms 防抖 → 听感延迟）
            scramAllRods(false, true);
        }
        
        public boolean isTripped() {
            return tripped;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
    }
    
     /**
     * AZ-2~AZ-6 功率/周期分层保护：越限跳闸（全棒 SCRAM）+ 面板指示灯，独立锁存/复位。
     * 其中 AZ-5 为**手动停堆**（无自动定值），AZ-6 为 5600 MW 自动档。
     * minPeriod > 0 时做周期保护（0 < T < minPeriod 触发）；否则做功率限值保护。
     * altAlarm = true 时跳闸/复位使用备用警声（ALARM_3），用于区分 AZ-2/AZ-3。
     */
    class AZPowerProtection implements Serializable {
        private boolean tripped = false;
        private final String name;
        /** 定值可在运行中调整（自检/调试用；正常不会变） */
        private double minPeriod, powerLimit;
        /** 是否使用备用警声（alarm3）——AZ-2/AZ-3 为 true */
        private final boolean altAlarm;
        /**
         * 是否为"紧急停堆"（插全部棒，含玩家手动棒）。
         * AZ-5（手动停堆）为 true；AZ-2/3/4/6 自动保护为 false ——
         * 自动保护只插自动棒/FASR，不锁死玩家的手动棒，玩家仍可干预。
         */
        private final boolean fullScram;

        AZPowerProtection(String name, double minPeriod, double powerLimit, boolean altAlarm, boolean fullScram) {
            this.name = name;
            this.minPeriod = minPeriod;
            this.powerLimit = powerLimit;
            this.altAlarm = altAlarm;
            this.fullScram = fullScram;
        }

        /** 本保护对应的警声 clip（跳闸/复位共用，保证停的是同一条） */
        private javax.sound.sampled.Clip alarmClip() {
            return altAlarm ? soundProvider.ALARM_3 : soundProvider.ALARM_2;
        }

        public void update() {
            // 自动保护受总开关控制：关闭「自动 AZ」后即使越限也不跳闸，
            // 仍可通过面板上的手动 AZ 按钮触发。
            if (tripped || !azAutoEnabled) {
                return;
            }
            boolean hit;
            if (minPeriod > 0) {
                double t = core.getPeriod();
                hit = t > 0 && t < minPeriod;
            } else {
                hit = core.getThermalPower() > powerLimit;
            }
            if (hit) {
                trip();
            }
        }

        public void trip() {
            if (tripped) {
                return;
            }
            tripped = true;
            soundProvider.request(alarmClip(), this);
            recordEvent(Localization.t(name + " Trip Signal"));
            scramAllRods(true, fullScram);
        }

        public void reset() {
            if (!tripped) {
                return;
            }
            recordEvent(Localization.t(name + " Reset"));
            tripped = false;
            soundProvider.release(alarmClip(), this);   // 立即静音；若 AZ-2/AZ-3 共用同一条警声，另一台仍跳闸时继续响
            scramAllRods(false, fullScram);
        }



        public boolean isTripped() {
            return tripped;
        }
    }

    class FASRControl implements Serializable {
        private boolean tripped = false, sequenceLock = false;
        private ArrayList<FASRChannel> channels = new ArrayList<>();
        
        public FASRControl() {
            core.coreArray.forEach(row -> {
                row.forEach(channel -> {
                    if (channel instanceof FASRChannel) {
                        channels.add((FASRChannel)channel);
                    }
                });
            });
        }
        
        public void update() {
            for (FASRChannel channel: channels) {
                if (channel.getPosition() > 0) {
                    sequenceLock = true;
                    return;
                }
            }
            sequenceLock = false;
        }
        
        public void trip(String reason) {
            if (tripped) {
                return;
            }
            soundProvider.request(soundProvider.ALARM_2, this);
            recordEvent(Localization.t("BAZ Trip Signal: ") + reason);
            tripped = true;
            core.coreArray.forEach(row -> {
                row.forEach(channel -> {
                    if (channel instanceof ControlRodChannel) {
                        ((ControlRodChannel) channel).setAutoState(2);
                        ((ControlRodChannel) channel).setState(2);
                        ((ControlRodChannel) channel).setScram(tripped);
                    }
                });
            });
            channels.forEach(channel -> {
                channel.setFastScram(true);
            });
        }   
        
        public void reset() {
            recordEvent(Localization.t("BAZ Reset"));
            soundProvider.release(soundProvider.ALARM_2, this);
            tripped = false;
            core.coreArray.forEach(row -> {
                row.forEach(channel -> {
                    if (channel instanceof ControlRodChannel) {
                        ((ControlRodChannel) channel).setAutoState(1);
                        ((ControlRodChannel) channel).setState(1);
                        ((ControlRodChannel) channel).setScram(tripped);
                    }
                });
            });
            channels.forEach(channel -> {
                channel.setFastScram(false);
            });
        }
        
        public boolean isTripped() {
            return tripped;
        }
        
        public boolean getSequenceBlock() {
            return sequenceLock;
        }
    }
    
    class AutomaticRodController implements Serializable {
        private double setpoint = 0.0;
        double ro, roSetpoint, thermalPower, averagePower;
        final ControlRodChannel[] linkedChannels;
        final ArrayList<ControlRodChannel> toControl = new ArrayList<>();
        private boolean enabled = false, error = false, limit = false, busy = false;
        public AutomaticRodController(ControlRodChannel[] linkedChannels) {
            this.linkedChannels = linkedChannels;
        }
        
        public void update() {
            thermalPower = core.getThermalPower();
            toControl.clear();
            toControl.addAll(Arrays.asList(linkedChannels));
            averagePower = 0;
            boolean upperLimit = false;
            boolean lowerLimit = false;
            boolean checkError = false;
            boolean checkBusy = false;
            for (ControlRodChannel channel: linkedChannels) {
                averagePower += channel.getNeutronPopulation()[0];
                if (channel.getPosition() == 0) {
                    upperLimit = true;
                } else if (channel.getPosition() == 1) {
                    lowerLimit = true;
                }
            }
            averagePower /= linkedChannels.length;
            ro = core.getReactivity();
            roSetpoint = 0 + ((setpoint - thermalPower) / 20000); 
            if (ro  > roSetpoint + 0.00005) {
                limit = lowerLimit;
                if (limit) {
                    checkError = true;
                }
                for (ControlRodChannel channel: linkedChannels) {
                    if (channel.getNeutronPopulation()[0] < averagePower * 0.95) {
                        toControl.remove(channel);
                    }
                }
                if (toControl.isEmpty()) {
                    toControl.addAll(Arrays.asList(linkedChannels));
                }
                for (ControlRodChannel rod: toControl) {
                    rod.setAutoState(2);
                    if (rod.getPosition() < 1) {
                        checkBusy = true;
                    }
                }
            } else if (ro < roSetpoint - 0.00005) {
                limit = upperLimit;
                if (fasrControl.getSequenceBlock()) {
                    error = true;
                    busy = false;
                    for (ControlRodChannel rod: linkedChannels) {
                        rod.setAutoState(1);
                    }
                    return;
                }
                for (ControlRodChannel channel: linkedChannels) {
                    if (channel.getNeutronPopulation()[0] > averagePower * 1.05) {
                        toControl.remove(channel);
                    }
                }
                if (toControl.isEmpty()) {
                    toControl.addAll(Arrays.asList(linkedChannels));
                }
                for (ControlRodChannel rod: toControl) {
                    rod.setAutoState(0);
                    if (rod.getPosition() > 0) {
                        checkBusy = true;
                    }
                }
            } else {
                limit = false;
                busy = false;
                for (ControlRodChannel rod: linkedChannels) {
                    rod.setAutoState(1);
                }
            }
            error = checkError;
            busy = checkBusy;
        }
        
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
            if (!enabled) {
                for (ControlRodChannel channel: linkedChannels) {
                    channel.setAutoState(1);
                }
            }
        }
        
        public void setSetpoint(double setpoint) {
            this.setpoint = setpoint;
        }
        
        public double getSetpoint() {
            return setpoint;
        }
        
        public boolean hasError() {
            return error;
        }
        
        public boolean onLimit() {
            return limit;
        }
        
        public boolean isBusy() {
            return busy;
        }
    }
    
    public long getSimulationTime() {
        return simulationTime;
    }

    /** 读档：恢复仿真时钟（事件日志/标题栏从正确的时刻继续）。 */
    public void setSimulationTime(long seconds) {
        simulationTime = Math.max(0L, seconds);
        timeUpdated = false;
    }
    
    public void updateSimulationTime() {
        simulationTime++;
        timeUpdated = true;
    }
    
    public void resetTimeUpdatedFlag() {
        timeUpdated = false;
    }
    
    public boolean getTimeUpdatedFlag() {
        return timeUpdated;
    }
    
    public void recordEvent(String event) {
        eventLog.add(NPPMath.formatSecondsToDaysAndTime(simulationTime, false) + "  " + event);
    }
    
    class FluidAutomaticRodController implements Serializable {
    private double setpoint = 0.0;
    double ro, roSetpoint, thermalPower, averagePower;
    final ArrayList<ControlRodChannel> linkedChannels = new ArrayList<>();
    final ArrayList<ControlRodChannel> toControl = new ArrayList<>();
    private final boolean[] enabled = {false, false, false}; //{lar, 1ar, 2ar}
    private final boolean[] error = {false, false, false};
    private final boolean[] limit = {false, false, false};
    private final boolean[] busy = {false, false, false};

        public void update() {
            thermalPower = core.getThermalPower();
            toControl.clear();
            toControl.addAll(linkedChannels);
            averagePower = 0;
            boolean upperLimit[] = {false, false, false};
            boolean lowerLimit[] = {false, false, false};
            boolean checkError[] = {false, false, false};
            boolean checkBusy[] = {false, false, false};
            boolean fineControl = true;
            for (ControlRodChannel channel: linkedChannels) {
                averagePower += channel.getNeutronPopulation()[0];
                if (channel.getPosition() == 0) {
                    if (ar1.contains(channel)) {
                        upperLimit[1] = true;
                    } else if (ar2.contains(channel)) {
                        upperLimit[2] = true;
                    } else {
                        upperLimit[0] = true;
                    }
                } else if (channel.getPosition() == 1) {
                    if (ar1.contains(channel)) {
                        lowerLimit[1] = true;
                    } else if (ar2.contains(channel)) {
                        lowerLimit[2] = true;
                    } else {
                        lowerLimit[0] = true;
                    }
                }
            }
            averagePower /= linkedChannels.size();
            ro = core.getReactivity();
            roSetpoint = 0 + ((setpoint - thermalPower) / 50000); 
            if (ro  > roSetpoint + 0.00001) {
                if (ro  > roSetpoint + 0.0001) {
                    fineControl = false;
                }
                for (int i = 0; i < 3; i++) {
                    limit[i] = lowerLimit[i];
                    if (limit[i]) {
                        checkError[i] = true;
                    }
                }
                if (!(limit[0] || limit[1] || limit[2])) {
                    for (ControlRodChannel channel: linkedChannels) {
                        if (channel.getNeutronPopulation()[0] < averagePower * 0.99) {
                            toControl.remove(channel);
                            channel.setAutoState(0);
                        }
                    }
                }
                if (toControl.isEmpty()) {
                    toControl.addAll(linkedChannels);
                }
                for (ControlRodChannel rod: toControl) {
                    if (rod.getPosition() < 1) {
                        if (fineControl) {
                            rod.setAutoState(1);
                            rod.setPosition(rod.getPosition() + 0.00025f);
                            if (rod.getPosition() > 1) {
                                rod.setPosition(1);
                            }
                        } else {
                            rod.setAutoState(2);
                        }
                        if (ar1.contains(rod)) {
                            checkBusy[1] = true;
                        } else if (ar2.contains(rod)) {
                            checkBusy[2] = true;
                        } else {
                            checkBusy[0] = true;
                        }
                    }
                }
            } else if (ro < roSetpoint - 0.00001) { 
                if (ro  < roSetpoint - 0.0001) {
                    fineControl = false;
                }
                for (int i = 0; i < 3; i++) {
                    limit[i] = upperLimit[i];
                }
                if (fasrControl.getSequenceBlock()) {
                    for (int i = 0; i < 3; i++) {
                        error[i] = true;
                        busy[i] = false;
                    }
                    for (ControlRodChannel rod: linkedChannels) {
                        rod.setAutoState(1);
                    }
                    return;
                }
                if (!(limit[0] || limit[1] || limit[2])) {
                    for (ControlRodChannel channel: linkedChannels) {
                        if (channel.getNeutronPopulation()[0] > averagePower * 1.01) {
                            toControl.remove(channel);
                            channel.setAutoState(2);
                        }
                    }
                }
                if (toControl.isEmpty()) {
                    toControl.addAll(linkedChannels);
                }
                for (ControlRodChannel rod: toControl) {
                    if (rod.getPosition() > 0) {
                        if (fineControl) {
                            rod.setAutoState(1);
                            rod.setPosition(rod.getPosition() - 0.00025f);
                            if (rod.getPosition() < 0) {
                                rod.setPosition(0);
                            }
                        } else {
                            rod.setAutoState(0);
                        }
                        if (ar1.contains(rod)) {
                            checkBusy[1] = true;
                        } else if (ar2.contains(rod)) {
                            checkBusy[2] = true;
                        } else {
                            checkBusy[0] = true;
                        }
                    }
                }
            } else {
                for (int i = 0; i < 3; i++) {
                    busy[i] = false;
                    limit[i] = false;
                }
                for (ControlRodChannel rod: linkedChannels) {
                    rod.setAutoState(1);
                }
            }
            for (int i = 0; i < 3; i++) {
                    error[i] = checkError[i];
                    busy[i] = checkBusy[i];
                }
        }
        
        public void setSetpoint(double setpoint) {
            this.setpoint = setpoint;
        }
        
        public double getSetpoint() {
            return setpoint;
        }
        
        public boolean[] isEnabled() {
            return enabled;
        }
        
        public boolean[] hasError() {
            return error;
        }
        
        public boolean[] onLimit() {
            return limit;
        }
        
        public boolean[] isBusy() {
            return busy;
        }
        
        public void enableLAR() {
            if (enabled[0]) {
                return;
            }
            linkedChannels.addAll(lar);
            enabled[0] = true;
        }
        
        public void disableLar() {
            if(!enabled[0]) {
                return;
            }
            lar.forEach(channel -> {
                channel.setAutoState(1);
            });
            linkedChannels.removeAll(lar);
            enabled[0] = false;
        }
        
        public void enable1AR() {
            if (enabled[1]) {
                return;
            }
            linkedChannels.addAll(ar1);
            enabled[1] = true;
        }
        
        public void disable1AR() {
            if (!enabled[1]) {
                return;
            }
            ar1.forEach(channel -> {
                channel.setAutoState(1);
            });
            linkedChannels.removeAll(ar1);
            enabled[1] = false;
        }
        
        public void enable2AR() {
            if (enabled[2]) {
                return;
            }
            linkedChannels.addAll(ar2);
            enabled[2] = true;
        }
        
        public void disable2AR() {
            if (!enabled[2]) {
                return;
            }
            ar2.forEach(channel -> {
                channel.setAutoState(1);
            });
            linkedChannels.removeAll(ar2);
            enabled[2] = false;
        }
    }
}
