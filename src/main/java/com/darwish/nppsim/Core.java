package com.darwish.nppsim;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 反应堆堆芯。
 *
 * 物理由 {@link ReactorKinetics}（8 轴向节点点堆动力学，6 组缓发中子 + 氙毒 +
 * 多普勒/慢化剂/空泡反馈 + 自适应求解器）统一计算；本类负责：
 *  1) 构建 55×56 堆芯通道阵列（coreMap.csv，1661 根燃料通道 + 控制棒/探测器）；
 *  2) 每 tick 采集电厂状态（棒位、压力、流量、入口焓）喂给引擎；
 *  3) 把引擎结果（总功率/反应性/中子数/周期，及每通道功率与碘氙）分配出去——
 *     燃料通道继续按原有方式沸腾产汽送汽鼓，UI（堆芯图/MTK）数据接口不变。
 */
public class Core extends Component {
        final ArrayList<ArrayList<Channel>> coreArray = new ArrayList<>();
        private double neutronCount, previousNeutronCount, reactivity, period, thermalPower;
        private double fcNeutronCount;
        /** C++ 计算核心委托层（native 句柄不可序列化 → transient；读档后 update() 重建） */
        private transient ReactorKinetics kinetics = new ReactorKinetics();
        /** 核心离线冷却系统（PCS 水箱→离线泵 A/B→堆芯→排放），随存档序列化；
         *  旧档反序列化后为 null，update() 里自动补建。 */
        OfflineCoolingSystem offlineCooling = new OfflineCoolingSystem();
        private int fuelChannelCount = 0;

        public Core() {
            ArrayList<List<String>> coreData = getCoreData();
            for (int y = 0; y < 56; y++) {
                coreArray.add(new ArrayList<>());
                for (int x = 0; x < 56; x++) {
                    Channel channel = initializeChannelByIdentifier(coreData.get(y).get(x));
                    channel.setPositionString(NPPMath.formatChannelNumber(x, y));
                    coreArray.get(y).add(channel);
                }
            }
            computeRadialShape();
        }

        /** 径向功率形状：中心高、边缘低的 cos² 形，归一到均值 1（保留堆芯图的空间差异） */
        private void computeRadialShape() {
            final double cx = 27.5, cy = 27.5, rMax = 28.0;
            double sum = 0;
            int count = 0;
            for (int y = 0; y < coreArray.size(); y++) {
                ArrayList<Channel> row = coreArray.get(y);
                for (int x = 0; x < row.size(); x++) {
                    Channel channel = row.get(x);
                    if (channel instanceof FuelChannel) {
                        fuelChannelCount++;
                        FuelChannel fc = (FuelChannel) channel;
                        // 堆芯图行号从上往下（y=0 是顶部），取到笛卡尔距离即可（对称）
                        double dx = y - cy;
                        double dy = x - cy;
                        double rNorm = Math.min(1.0, Math.sqrt(dx * dx + dy * dy) / rMax);
                        double shape = 1.45 - 0.9 * rNorm * rNorm;
                        fc.setRadialShape(shape);
                        sum += shape;
                        count++;
                    }
                }
            }
            double mean = count > 0 ? sum / count : 1.0;
            for (ArrayList<Channel> row : coreArray) {
                for (Channel channel : row) {
                    if (channel instanceof FuelChannel) {
                        FuelChannel fc = (FuelChannel) channel;
                        fc.setRadialShape(fc.getRadialShape() / mean);
                    }
                }
            }
            kinetics.fuelChannelCount = Math.max(1, fuelChannelCount);
        }

        public void update() {
            // ---- 0) 离线冷却：泵/阀推进并写入引擎（旧档兼容：字段缺失时补建）----
            if (offlineCooling == null) {
                offlineCooling = new OfflineCoolingSystem();
            }
            if (kinetics == null) {
                kinetics = new ReactorKinetics();   // 读档：native 句柄不序列化，重建
            }
            offlineCooling.update(0.05);
            // 离线冷却外环路：除盐水箱 → 离线泵 A/B → 换热器 A/B → 河。河水记录带走的热量。
            if (NPPSim.pcs != null && NPPSim.pcs.river != null && offlineCooling.getTotalFlowKgs() > 0.1) {
                NPPSim.pcs.river.updateWaterInflow(offlineCooling.getTotalFlowKgs() * 0.05,
                        offlineCooling.getOutletTempC());
            }
            kinetics.mdotOffline = offlineCooling.getTotalFlowKgs();
            kinetics.hOfflineIn = Loader.tables.getWaterEnthalpyByTemperature(OfflineCoolingSystem.WATER_TEMP_C);

            // ---- 1) 采集电厂状态（上一 tick 的棒位/压力/流量/入口温度）----
            double rodSum = 0;
            int rodCount = 0;
            double flowSum = 0, inflowTempFlowSum = 0;
            int inflowCount = 0;
            double pressSum = 0;
            int pressCount = 0;
            for (ArrayList<Channel> row : coreArray) {
                for (Channel channel : row) {
                    if (channel instanceof ControlRodChannel) {
                        rodSum += ((ControlRodChannel) channel).getPosition();
                        rodCount++;
                    } else if (channel instanceof FuelChannel) {
                        FuelChannel fc = (FuelChannel) channel;
                        pressSum += fc.getPressure();
                        pressCount++;
                        double r = fc.getWaterInflowRate();
                        if (r > 0) {
                            flowSum += r;
                            inflowTempFlowSum += r * fc.getWaterInflowTemperature();   // 入口温度按通道流量加权
                            inflowCount++;
                        }
                    }
                }
            }
            kinetics.rodInsertion = rodCount > 0 ? rodSum / rodCount : kinetics.rodInsertion;
            kinetics.pressureMPa = pressCount > 0 ? pressSum / pressCount : kinetics.pressureMPa;
            // 仅主回路流量（MCP）；离线冷却经换热器/河水单独通道进引擎，不并入（P0：避免重复计入）。
            // 读档后的第一个 tick 通道流量可能尚未重新建立（为 0），此时保留存档恢复的输入，
            // 不能把刚恢复的堆芯按 0 流量再推一步。
            if (flowSum > 1.0) {
                kinetics.mdotTotal = flowSum;
                if (inflowCount > 0) {
                    double tIn = inflowTempFlowSum / flowSum;
                    // C++ 的焓参考点是 h(270°C)=0；蒸汽表给的是绝对焓（kJ/kg），
                    // 因此这里减去 270°C 的基准，避免把 ~1187 kJ/kg 的绝对值直接注入。
                    double hAbs = Loader.tables.getWaterEnthalpyByTemperature(tIn);
                    double hRef = Loader.tables.getWaterEnthalpyByTemperature(270.0);
                    kinetics.hInHeader = hAbs - hRef;
                }
            }

            // ---- 2) 物理引擎推进（内部自适应子步）----
            kinetics.update(0.05);   // 仿真帧长 50 ms

            // ---- 3) 结果分配：功率/碘/氙 → 每根燃料通道（按径向形状）----
            double perChannelBase = kinetics.totalPowerMW / Math.max(1, fuelChannelCount);
            double chNominalMW = 4800.0 / Math.max(1, fuelChannelCount);   // 额定通道功率（相对通量归一基准）
            double tempSum = 0;
            int tempCount = 0;
            for (ArrayList<Channel> row : coreArray) {
                for (Channel channel : row) {
                    if (channel instanceof FuelChannel) {
                        FuelChannel fc = (FuelChannel) channel;
                        double shape = fc.getRadialShape();
                        double p = perChannelBase * shape;
                        fc.setKineticsPower(p,
                                kinetics.i135AvgForUI * shape,
                                kinetics.xe135AvgForUI * shape);
                        // 通道相对通量（额定通道=1.0）：供 AR 棒控按局部功率挑选通道 + 表格 Count 行
                        double flux = p / chNominalMW;
                        fc.setNeutronPopulation(flux, flux);
                        tempSum += fc.getWaterTemperature() * flux;
                        tempCount += flux;   // 相对通量加权（等效流量加权，热通道不被稀释）
                    }
                }
            }
            thermalPower = kinetics.totalPowerMW;
            reactivity = kinetics.reactivityAbs;
            previousNeutronCount = neutronCount;   // 上一 tick 值（旧实现同 tick 双赋，差分恒 0）
            neutronCount = kinetics.neutronTotal;
            period = kinetics.periodSeconds;
            fcNeutronCount = kinetics.neutronTotal;
            // 离线冷却面板温度 = 电厂侧真实通道水温（堆芯出口）。
            // 不用 C++ 引擎内部 Tcool——其停堆初值保留满功率热态剖面（280-300°C），
            // 与电厂侧 20°C 冷水矛盾（曾显示 300.1°C）。
            offlineCooling.outletTempC = tempCount > 0 ? tempSum / tempCount : 20.0;
        }

        /**
         *
         * @param x
         * @param y
         * @param c relative position
         * @param out 复用的输出数组，写入 {x, y}，避免每次调用分配数组
         */
        private Channel initializeChannelByIdentifier(String channelIdentifier) {
            switch (channelIdentifier) {
                case "\\": //PDMS-A Channel
                    return new PDMSChannel();
                case "^": //fission chamber
                    return new FissionChamber();
                case "_": //MCR
                    return new MCRChannel();
                case "E": //LEP
                    return new LEPChannel();
                case "o": //MCRM
                    return new MCRMChannel();
                case "L": //LAC
                    return new LACChannel();
                case "A": //AR
                    return new ACRChannel();
                case "x": //SAR
                    return new SARChannel();
                case "R": //axial control sar
                    return new SACRChannel();
                case "%": //FASR
                    return new FASRChannel();
                case "#": //fuel channel
                    return new FuelChannel();
                case "*": //reflector cooling channel
                    return new ReflectorCoolingChannel();
                case ".": //reflector
                    return new ReflectorChannel();
                case "v": //void
                    return new VoidChannel();
                default:
                    return null;
            }

        }

        private ArrayList<List<String>> getCoreData() {
            ArrayList<List<String>> result = new ArrayList<>();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader((getClass().getResourceAsStream("/res/coreMap.csv"))));
                ArrayList<String> dataStringArray = new ArrayList<>();
                String line;
                while ((line=reader.readLine())!=null) {
                    dataStringArray.add(line);
                }
                for (int i = 1; i < dataStringArray.size(); i++) {
                    String[] dataArray = dataStringArray.get(i).split(",");
                    ArrayList<String> data = new ArrayList<>(Arrays.asList(dataArray));
                    data.remove(data.size() - 1);
                    result.add(data);
                }
                if (result.size() != 56) {
                    throw new Exception("CorruptedCoreDataException");
                }
                for (List<String> i: result) {
                    if (i.size() != 56) {
                        throw new Exception("CorruptedCoreDataException");
                    }
                }
            } catch (Exception e) {
                // 堆芯图数据损坏时显式失败，避免构造出残缺 coreArray 后到处 NPE
                throw new RuntimeException("coreMap.csv 加载失败", e);
            }
            return result;
        }

        // ------------------------------------------------------------------
        // 存档：C++ 内核完整状态（显示字段本身已随 Core 一起序列化）
        // ------------------------------------------------------------------
        /** 导出 C++ 内核完整状态；安全模式/旧 DLL 返回 null。 */
        double[] captureNativeState() {
            return kinetics == null ? null : kinetics.captureNativeState();
        }

        /** 用存档状态重建/覆盖内核；成功返回 true。 */
        boolean restoreNativeState(double[] state) {
            if (state == null) {
                return false;
            }
            if (kinetics == null) {
                kinetics = new ReactorKinetics();   // 读档：transient 句柄重建
            }
            return kinetics.restoreNativeState(state);
        }

        /** 保存内核的 Java 侧输入（棒位/流量/压力/入口焓/离线流量）。 */
        SaveGame.CoreInputs captureInputs() {
            if (kinetics == null) {
                return null;
            }
            SaveGame.CoreInputs s = new SaveGame.CoreInputs();
            // 优先用 native 内核里实际生效的值：Java 侧 mdotTotal 可能恰好是
            // UI resetFlowRates() 后的 0，直接存档会把恢复后的堆芯按 0 流量再推。
            s.rodInsertion = kinetics.actualRodInsertion();
            s.mdotTotal = kinetics.actualFlowKgS();
            s.pressureMPa = kinetics.pressureMPa;
            s.hInHeader = kinetics.hInHeader;
            s.mdotOffline = kinetics.mdotOffline;
            s.hOfflineIn = kinetics.hOfflineIn;
            s.fuelChannelCount = kinetics.fuelChannelCount;
            return s;
        }

        /** 恢复内核的 Java 侧输入；必须在 restoreNativeState 之后调用。 */
        void restoreInputs(SaveGame.CoreInputs s) {
            if (s == null || kinetics == null) {
                return;
            }
            kinetics.rodInsertion = s.rodInsertion;
            kinetics.pressureMPa = s.pressureMPa;
            kinetics.mdotTotal = s.mdotTotal;
            kinetics.hInHeader = s.hInHeader;
            kinetics.mdotOffline = s.mdotOffline;
            kinetics.hOfflineIn = s.hOfflineIn;
            if (s.fuelChannelCount > 0) {
                kinetics.fuelChannelCount = s.fuelChannelCount;
            }
        }

        public double getThermalPower() {
            return thermalPower;
        }

        public double getReactivity() {
            return reactivity;
        }

        public double getNeutronCount() {
            return previousNeutronCount;
        }

        public double getPeriod() {
            return period;
        }

        /** 诊断：当前棒平均插入深度（0=全抽出，1=全插入） */
        public double getRodInsertion() {
            return kinetics.rodInsertion;
        }

        /** 诊断：氙毒（pcm，负值） */
        public double getXenonWorthPcm() {
            return kinetics.xenonWorthPcm;
        }

        /** 核心离线冷却系统（面板控制/读数） */
        public OfflineCoolingSystem getOfflineCooling() {
            return offlineCooling;
        }
    }
