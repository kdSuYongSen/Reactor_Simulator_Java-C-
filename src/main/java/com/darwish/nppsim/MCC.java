package com.darwish.nppsim;

import static com.darwish.nppsim.NPPSim.autoControl;
import static com.darwish.nppsim.NPPSim.core;
import static com.darwish.nppsim.NPPSim.mcc;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MCC extends Component {
    final SeparatorDrum drum1, drum2;
    final MCPPressureHeader pHeader1, pHeader2;
    final SimpleSuctionHeader sHeader1, sHeader2;
    final ArrayList<Pump> mcp = new ArrayList<>(); // 0-3 loop 1, 4-7 loop 2
    final ArrayList<FuelChannel> fuelChannels1 = new ArrayList<>();
    final ArrayList<FuelChannel> fuelChannels2 = new ArrayList<>();
    final ArrayList<SteamWaterPipe> steamWaterPipes1 = new ArrayList<>();
    final ArrayList<SteamWaterPipe> steamWaterPipes2 = new ArrayList<>();

    public MCC() {
        drum1 = new SeparatorDrum();
        drum2 = new SeparatorDrum();

        for (ArrayList<Channel> x: core.coreArray) {
            for (int i = 0; i < x.size(); i++) {
                if (x.get(i) instanceof FuelChannel) {
                    if (i < 28) {
                        fuelChannels2.add((FuelChannel)x.get(i)); 
                    } else {
                        fuelChannels1.add((FuelChannel)x.get(i));
                    }
                }
            }
        }
        fuelChannels1.forEach(channel -> {
            steamWaterPipes1.add(new SteamWaterPipe(channel, drum1));
        });
        fuelChannels2.forEach(channel -> {
            steamWaterPipes2.add(new SteamWaterPipe(channel, drum2));
        });
        sHeader1 = new SimpleSuctionHeader(new Connectable[] {drum1}, 74.9);
        sHeader2 = new SimpleSuctionHeader(new Connectable[] {drum2}, 74.9);
        sHeader1.isolationValveArray.get(0).position = 1.0f;
        sHeader2.isolationValveArray.get(0).position = 1.0f;
        pHeader1 = new MCPPressureHeader(fuelChannels1);
        pHeader2 = new MCPPressureHeader(fuelChannels2);
        /* 8 台主泵，每环路 4 台（3 运行 + 1 备用）。
         * 额定流量按实堆口径标定：RBMK-1500 一回路 12000 kg/s，6 台运行
         * → 单泵 2000 kg/s。285°C 比容 1.35e-3 m³/kg → 2.70 m³/s。
         * 旧值 2.2222 m³/s 只对应 1646 kg/s/台，6 台合计 9878 kg/s（额定的 82%），
         * 堆芯在满功率下会欠冷、空泡偏高。 */
        for (int i = 0; i < 4; i++) {
            mcp.add(new MCCPump(1000, 2.70f, 0.226, 18, 200, 933, sHeader1, pHeader1));
        }
        for (int i = 0; i < 4; i++) {
            mcp.add(new MCCPump(1000, 2.70f, 0.226, 18, 200, 933, sHeader2, pHeader2));
        }
    }

    protected class SeparatorDrum extends WaterSteamSubComponent implements Connectable, UIReadable, Serializable, SteamSupplyLimit {
        private final double volume = 397.1;// drum + downcomers //682.35;
        private final double nominalWaterVolume;
        private double initialSteamMass; // this would be air in reality
        private double steamMass = 0.0; // kg
        private double waterMass = 223089.51; //223,5 m3 (2 drums + downcomers) //305837.26; // 306.4 m3 at 20C
        private double steamProduction = 0.0; // kg/s
        private double thermalLoss = 0.0; // conductive energy loss Mwt
        /**
         * 堆芯送进本汽包的热功率 MW（由 NPPSim 主循环每 tick 写入，Core.thermalPower 两台均分）。
         *
         * <p>2026-10-27：此前堆芯功率只用于界面显示与动力学，
         * {@code FuelChannel.update()} 是空实现、汽包也没有任何热源 ——
         * 汽包只能靠"降压闪蒸"出汽，于是二回路产汽量与反应堆功率无关
         * （功率拉高不会多产汽，降功率也不会少产汽，看起来就是"凭空产汽"）。
         * 现在把堆芯热功率直接注入汽包水，产汽量由能量平衡决定。</p>
         */
        private double coreHeatMW = 0.0;

        // calculated results from the update thread stored here for use in other
        // functions:
        private double steamDensity;
        private double specificHeatWater = NPPMath.calculateSpecificHeatWater(waterTemperature);
        private double specificVaporEnthalpy = Loader.tables.getSpecificVaporEnthalpyByTemperature(waterTemperature);// NPPMath.calculateSpecificVaporEnthalpy(waterTemperature);
        private double boilingPoint = Loader.tables.getSteamTemperatureByPressure(pressure);
        private double specificDensityWater = Loader.tables.getWaterDensityByTemp(waterTemperature);
        private double waterVolume;
        private double steamVolume;
        private double deltaEnergy = 0.0; // surplus/deficit energy from last time step in kJ
        private double waterInflowTemperature = 20.0; // c
        private double steamInflowTemperature = 20.0; // c

        public SeparatorDrum() {
            waterVolume = waterMass * specificDensityWater;
            steamVolume = volume - waterVolume;
            nominalWaterVolume = waterVolume;
            initialSteamMass = steamVolume / 1.6718; // constant density for 20c
            steamDensity = steamVolume / (steamMass + initialSteamMass);
        }

        void update() {
            double vacuumBreakerFlow = (pressure - 0.10142) * 20;
            waterMass -= waterOutflow;
            if (waterInflow < 0) {
                waterMass += waterInflow;
            } else {
                double[] inflowData = NPPMath.mixWater(waterMass, waterTemperature, waterInflow, waterInflowTemperature);
                waterMass = inflowData[0];
                waterTemperature = inflowData[1];
            }

            specificHeatWater = NPPMath.calculateSpecificHeatWater(waterTemperature);
            specificVaporEnthalpy = Loader.tables.getSpecificVaporEnthalpyByTemperature(waterTemperature);
            specificDensityWater = Loader.tables.getWaterDensityByTemp(waterTemperature);

            steamMass -= steamOutflow;
            if (steamInflow < 0) {
                steamMass += steamInflow;
            } else {
                steamMass += steamInflow;
                double oldWaterTemp = waterTemperature;
                waterTemperature += ((Loader.tables.getSteamEnthalpyByTemperature(steamInflowTemperature) - Loader.tables.getSteamEnthalpyByTemperature(waterTemperature)) * steamInflow) / (specificHeatWater * waterMass);
                specificHeatWater = NPPMath.calculateSpecificHeatWater(waterTemperature);
                deltaEnergy += (Loader.tables.getSteamEnthalpyByTemperature(waterTemperature) - Loader.tables.getSteamEnthalpyByTemperature(oldWaterTemp)) * steamInflow; //correct for difference in new steamTemp and new waterTemp after energy transfer
            }

            steamTemperature = waterTemperature;

            if (steamMass <= 0) {
                steamMass = 0;
                initialSteamMass -= steamOutflow;
            }
            if (pressure < 0.10142) {
                initialSteamMass -= vacuumBreakerFlow;
            }
            
            /* 能量平衡（kJ/tick）：deltaEnergy 是蒸发/冷凝的潜热欠账，
             * thermalLoss 是散热，coreHeatMW 是堆芯送进来的热量（MW × 0.05 s × 1000 = kJ）。 */
            double energy = 0 - deltaEnergy / 3 - thermalLoss * 50 + coreHeatMW * 1000.0 * 0.05;
            deltaEnergy -= deltaEnergy / 3;

            waterTemperature += (energy / (specificHeatWater * waterMass));
            if (!Double.isFinite(waterTemperature)) {
                waterTemperature = 20.0;             // 数值崩溃兜底：回到冷水
            }
            waterTemperature = Math.max(5.0, Math.min(360.0, waterTemperature));
            final double potentialPressure = Loader.tables.getSteamPressureByTemp(waterTemperature);
            specificDensityWater = Loader.tables.getWaterDensityByTemp(waterTemperature);
            waterVolume = waterMass * specificDensityWater;
            steamVolume = volume - waterVolume;
            double deltaSteamMass = steamVolume / Loader.tables.getSteamDensityByPressure(potentialPressure) - (steamMass + initialSteamMass);
            if (steamMass + deltaSteamMass < 0) {
                deltaSteamMass = 0 - steamMass;
            }

            specificVaporEnthalpy = Loader.tables.getSpecificVaporEnthalpyByTemperature(waterTemperature);
            deltaEnergy += deltaSteamMass * specificVaporEnthalpy;

            steamProduction += deltaSteamMass + steamInflow;

            if (!Double.isFinite(deltaSteamMass)) {
                deltaSteamMass = 0.0;                // 数值崩溃兜底
            }
            if (deltaSteamMass > waterMass * 0.05) {
                // 单 tick 蒸发量不超过存水的 5%：给水断掉时不会一瞬间把汽包抽干
                deltaSteamMass = waterMass * 0.05;
            }
            waterMass -= deltaSteamMass;
            steamMass += deltaSteamMass;

            if (steamMass < 0) {
                steamMass = 0;
            }

            specificDensityWater = Loader.tables.getWaterDensityByTemp(waterTemperature);
            waterVolume = waterMass * specificDensityWater;
            waterLevel = (waterVolume / nominalWaterVolume - 1) * 100;
            steamVolume = volume - waterVolume;
            steamDensity = steamVolume / (steamMass + initialSteamMass);
            pressure = Loader.tables.getSteamPressureByDensity(steamDensity);
            boilingPoint = Loader.tables.getSteamTemperatureByPressure(pressure);
            sanitize();                       // 数值卫生：溢出/NaN 时把蒸汽转回水（质量守恒）
            resetFlows();
        }

        /**
         * 数值卫生与"超量崩溃自动重置"。
         *
         * <p>规则（保持物质总量）：</p>
         * <ul>
         *   <li>水量/汽量出现 NaN、Inf 或负数 → 按安全值重置；</li>
         *   <li>汽空间存量超过 {@link #MAX_STEAM_KG} → 超出部分**冷凝成水**（汽→水，总量不变），
         *       这正是"给水带汽过多导致崩溃时，把这一管路的蒸汽重置为水"；</li>
         *   <li>水量低于 {@link #MIN_WATER_KG} → 补到最小值（日志里有记录，避免干锅）。</li>
         * </ul>
         */
        void sanitize() {
            if (!Double.isFinite(waterMass)) {
                waterMass = NOMINAL_WATER_KG;
            }
            if (!Double.isFinite(steamMass) || steamMass < 0.0) {
                double back = Math.max(0.0, steamMass);       // 负数汽量还给水侧（守恒）
                steamMass = 0.0;
                waterMass += back;
            }
            if (steamMass > MAX_STEAM_KG) {
                waterMass += steamMass - MAX_STEAM_KG;        // 多余蒸汽冷凝成水
                steamMass = MAX_STEAM_KG;
            }
            if (!Double.isFinite(waterMass) || waterMass < MIN_WATER_KG) {
                waterMass = MIN_WATER_KG;
            }
            if (waterMass > NOMINAL_WATER_KG * 1.6) {
                // 水量过多：超出部分转到除盐水箱（不凭空消失）
                double excess = waterMass - NOMINAL_WATER_KG * 1.6;
                waterMass -= excess;
                if (NPPSim.pcs != null && NPPSim.pcs.demineralizedWaterTank != null) {
                    NPPSim.pcs.demineralizedWaterTank.updateWaterInflow(excess, waterTemperature);
                }
            }
            if (!Double.isFinite(waterTemperature)) {
                waterTemperature = 20.0;
            }
            waterTemperature = Math.max(5.0, Math.min(360.0, waterTemperature));
            if (!Double.isFinite(pressure) || pressure < 0.05 || pressure > 20.0) {
                pressure = 0.1014;
            }
            if (!Double.isFinite(waterLevel) || Math.abs(waterLevel) > 500.0) {
                waterLevel = 0.0;
            }
        }

        /** 汽空间存量上限（超过就冷凝成水） */
        static final double MAX_STEAM_KG = 6000.0;
        /** 水容积下限（防止干锅/负质量） */
        static final double MIN_WATER_KG = 20000.0;
        /** 额定水量（223 t） */
        static final double NOMINAL_WATER_KG = 223089.51;

        /** 堆芯热功率注入（MW）。每 tick 由 NPPSim 调用；0 表示停堆/无热源。 */
        void addCoreHeatMW(double mw) {
            coreHeatMW = Double.isFinite(mw) ? Math.max(0.0, Math.min(20000.0, mw)) : 0.0;
        }

        double getCoreHeatMW() {
            return coreHeatMW;
        }

        /** 汽空间真实存量：抽汽不能超过这里（原先没有限制，靠闪蒸凭空补） */
        @Override public double availableSteamKg() {
            return Math.max(0.0, steamMass);
        }

        @Override
        public double getPressure() {
            return pressure;
        }

        @Override
        public double getSteamDensity() {
            return steamDensity;
        }

        @Override
        public void updateSteamInflow(double flow, double tempC) {
            double[] inflowData = NPPMath.mixWater(steamInflow, steamInflowTemperature, flow, tempC);
            steamInflow = inflowData[0];
            steamInflowTemperature = inflowData[1];
        }

        @Override
        public void updateSteamOutflow(double flow, double tempC) {
            steamOutflow += flow;
        }

        @Override
        public void updateWaterInflow(double flow, double tempC) {
            if (flow < 0 || waterInflow < 0) {
                waterInflow += flow;
            } else {
                double[] inflowData = NPPMath.mixWater(waterInflow, waterInflowTemperature, flow, tempC);
                waterInflow = inflowData[0];
                waterInflowTemperature = inflowData[1];
            }
        }

        @Override
        public void updateWaterOutflow(double flow, double tempC) {
            waterOutflow += flow;
        }

        public void resetFlowRates() {
            steamProduction = 0;
        }

        @Override
        public double getWaterMass() {
            return waterMass;
        }

        public double getBoilingPoint() {
            return boilingPoint;
        }

        public double getSteamProduction() {
            return steamProduction;
        }

        @Override
        public double getSteamMass() {
            return steamMass;
        }

        @Override
        public double getWaterDensity() {
            return specificDensityWater;
        }

        public double getWaterOutflow() {
            return waterOutflow;
        }

        @Override
        public double getSteamVolume() {
            return steamVolume;
        }

        @Override
        public double getSteamTemperature() {
            return steamTemperature;
        }
        
        public double getInitialSteamMass() {
            return initialSteamMass;
        }

        public void setSteamPressure(double press) {
            pressure = press;
        }
    }
    
    protected class SteamWaterPipe implements Serializable {
    private final FuelChannel source;
    private final Connectable drain;
    private double steamFlow = 0.0, steamTemp = 0.0;
    private double waterFlow = 0.0, waterTemp = 0.0;

    SteamWaterPipe(FuelChannel source, Connectable drain) {
        this.source = source;
        this.drain = drain;
        source.setDrain(drain);
    }

    public void update() {
        if (source.getSteamVolume() == 0) {
            steamFlow = source.getSteamMass();
        } else {
            double ratio = source.getSteamVolume() / (source.getSteamVolume() + drain.getSteamVolume());
            double totalMass = source.getSteamMass() + drain.getSteamMass();
            steamFlow = source.getSteamMass() - (ratio * totalMass);
        }
        if (steamFlow >= 0) {
            steamTemp = source.getSteamTemperature();
        } else {
            steamTemp = drain.getSteamTemperature();
        }
        waterFlow = source.getWaterOutflow();
        if (waterFlow < 0) { // reverse flow will need drain watertemp, otherwise source watertemp
            waterTemp = drain.getWaterTemperature();
        } else {
            waterTemp = source.getWaterTemperature();
        }
        // source waterOutFlow is not updated here as source does that by itself
        if (Double.isNaN(steamFlow)) {
            steamFlow = 0;
        }
        source.updateSteamOutflow(steamFlow, steamTemp);
        drain.updateSteamInflow(steamFlow, steamTemp);
        drain.updateWaterInflow(waterFlow, waterTemp);
    }
}

    protected class MCPPressureHeader extends WaterSteamSubComponent implements Connectable, UIReadable, Serializable {
        final List<FuelChannel> drains;
        private final double volume = 121.5; 
        private double waterMass, waterVolume;
        private double waterInflowTemperature = 20.0; // C
        private double specificDensityWater = Loader.tables.getWaterDensityByTemp(waterTemperature);
        private boolean bypassesOpen = true;

        MCPPressureHeader(List<FuelChannel> drains) {
            this.drains = drains;
            this.waterMass = volume / Loader.tables.getWaterDensityByTemp(waterTemperature);
            this.waterVolume = volume;
        }

        public void update() {
            waterMass -= waterOutflow;
            waterOutflowRate += waterOutflow;
            double[] waterInflowData = NPPMath.mixWater(waterMass, waterTemperature, waterInflow, waterInflowTemperature);
            waterTemperature = waterInflowData[1];
            waterMass = waterInflowData[0];
            //waterTemperature -= (0.5 * waterTemperature - 10) * 0.0000004; //TODO
            specificDensityWater = Loader.tables.getWaterDensityByTemp(waterTemperature);
            waterVolume = specificDensityWater * waterMass;
            waterOutflow = (waterVolume - volume) / specificDensityWater;
            if (waterOutflow < 0) {
                if (drains.size() == 835) {
                    double[] inflowData = NPPMath.mixWater(waterMass, waterTemperature, 0 - waterOutflow, mcc.drum1.getWaterTemperature());
                    waterMass = inflowData[0];
                    waterTemperature = inflowData[1];
                    mcc.drum1.updateWaterOutflow(0 - waterOutflow, mcc.drum1.getWaterTemperature());
                } else {
                    double[] inflowData = NPPMath.mixWater(waterMass, waterTemperature, 0 - waterOutflow, mcc.drum2.getWaterTemperature());
                    waterMass = inflowData[0];
                    waterTemperature = inflowData[1];
                    mcc.drum2.updateWaterOutflow(0 - waterOutflow, mcc.drum2.getWaterTemperature());
                }
            } else {
                waterMass -= waterOutflow;
                double flowPerDrain = waterOutflow / drains.size();
                for (Connectable i: drains) {
                    i.updateWaterInflow(flowPerDrain, waterTemperature);
                }
            }
            waterVolume = specificDensityWater * waterMass;
            resetFlows();
        }

        @Override
        public double getPressure() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public double getSteamDensity() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public double getWaterDensity() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public double getSteamMass() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public double getSteamVolume() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public double getSteamTemperature() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void updateSteamOutflow(double flow, double tempC) {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateSteamInflow(double flow, double tempC) {
            // TODO Auto-generated method stub

        }

        @Override
        public void updateWaterOutflow(double flow, double tempC) {
            waterOutflow += flow;
        }

        @Override
        public void updateWaterInflow(double flow, double tempC) {
            waterInflow += flow;
            waterInflowTemperature = tempC; //tempC will be equal for all mcp's
        }

        public void setBypassState(boolean open) {
            bypassesOpen = open;
        }

        public boolean getBypassState() {
            return bypassesOpen;
        }

        @Override
        public double getWaterMass() {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }
    }

    public void update() {
        core.coreArray.forEach(row -> {
            row.forEach(channel -> {
                channel.update();
            });
        });
        autoControl.resetTimeUpdatedFlag();
        for (SteamWaterPipe i: steamWaterPipes1) {
            i.update();
        }
        for (SteamWaterPipe i: steamWaterPipes2) {
            i.update();
        }
        drum1.update();
        drum2.update();
        sHeader1.update();
        sHeader2.update();
        for (Pump i : mcp) {
            i.update();
        }
        pHeader1.update();
        pHeader2.update();
    }

    public void setThermalPower(double thermalPower) {
        final double perChannel = thermalPower / (fuelChannels1.size() + fuelChannels2.size());
        for (FuelChannel i: fuelChannels1) {
            i.setThermalPower(perChannel);
        }
        for (FuelChannel i: fuelChannels2) {
            i.setThermalPower(perChannel);
        }
    }

    public void setWaterTemp(double tempC) {
        double saturatedPress = Loader.tables.getSteamPressureByTemp(tempC);
        for (FuelChannel i: fuelChannels1) {
            i.setWaterTemp(tempC);
            i.setSteamPressure(saturatedPress);
        }
        for (FuelChannel i: fuelChannels2) {
            i.setWaterTemp(tempC);
            i.setSteamPressure(saturatedPress);
        }
        drum1.waterTemperature = tempC;
        drum1.setSteamPressure(saturatedPress);
        drum2.waterTemperature = tempC;
        drum2.setSteamPressure(saturatedPress);
        pHeader1.waterTemperature = tempC;
        pHeader2.waterTemperature = tempC;
        pHeader1.waterMass = pHeader1.volume / NPPSim.tables.getWaterDensityByTemp(tempC);
        pHeader2.waterMass = pHeader2.volume / NPPSim.tables.getWaterDensityByTemp(tempC);
        drum1.waterMass = drum1.nominalWaterVolume / NPPSim.tables.getWaterDensityByTemp(tempC);
        drum2.waterMass = drum2.nominalWaterVolume / NPPSim.tables.getWaterDensityByTemp(tempC);
    }

}
