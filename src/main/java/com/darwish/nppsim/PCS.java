package com.darwish.nppsim;

import java.util.ArrayList;

import static com.darwish.nppsim.NPPSim.atmosphere;
import static com.darwish.nppsim.NPPSim.feedwaterMixer1;
import static com.darwish.nppsim.NPPSim.feedwaterMixer2;
import static com.darwish.nppsim.NPPSim.dearators;
import static com.darwish.nppsim.NPPSim.mcc;

public class PCS extends Component {
    WaterWaterHeatExchanger regenerator1, regenerator2, pcsCooler1, pcsCooler2;
    /** 河（外部冷源/热沉，2026-10-28 新增）：离线冷却排水、DW 补水、水箱排水都到这里 */
    River river;
    /** 通用净化装置：河 → 净化 → DW 泵 → 除盐水箱（默认开启） */
    PurificationUnit purification;
    Tank demineralizedWaterTank;
    pcsMockupValve pcsValve;
    SimpleSuctionHeader pcsSuctionHeader;
    SimplePressureHeader pcsPressureHeader, pcsFilterPressureHeader, pcsReturnHeader, pcsCoolingHeader;
    SimplePump pcsPump1, pcsPump2, coolingPump, dwMakeupPump;
    Atmosphere intermediateLoop;
    ArrayList<Pump> admsPumps = new ArrayList<>();
    PressureHeader admsHeader;
    ArrayList<WaterValve> dearatorMakeupValves = new ArrayList<>();
    ArrayList<WaterValve> dearatorOverflowValves = new ArrayList<>();
    /** 除盐水箱排水阀：把水排出系统（排到大气），带自动控制（水位过高自动排水） */
    WaterValve dwTankDrainValve;
    /** 除盐水箱排水阀自动控制的高/低水位阈值（%） */
    static final double DW_DRAIN_HIGH = 210.0;
    static final double DW_DRAIN_LOW = 190.0;
    
    public PCS() {
        intermediateLoop = new Atmosphere(20);
        river = new River();
        purification = new PurificationUnit(river);
        demineralizedWaterTank = new Tank(2000);   // PCS 水箱初始水位 2000
        pcsSuctionHeader = new SimpleSuctionHeader(new Connectable[] {mcc.pHeader1, mcc.pHeader2}, 0.5);
        pcsSuctionHeader.isolationValveArray.forEach(valve -> {
            valve.setPosition(1.0f); //open header valves for both loops
        });
        pcsReturnHeader = new SimplePressureHeader(new Connectable[] {feedwaterMixer1, feedwaterMixer2}, 0.5);
        pcsReturnHeader.isolationValveArray.forEach(valve -> {
            valve.setPosition(1.0f); //open header valves for both coolers
        });
        pcsFilterPressureHeader = new SimplePressureHeader(new Connectable[] {regenerator1, regenerator2, demineralizedWaterTank, feedwaterMixer1, feedwaterMixer2}, 0.5);
        pcsFilterPressureHeader.isolationValveArray.get(0).setPosition(1.0f);
        pcsFilterPressureHeader.isolationValveArray.get(1).setPosition(1.0f); //open header valves for both regenerator return circuits
        pcsCooler1 = new WaterWaterHeatExchanger(41.0f, 55.5, 65, pcsFilterPressureHeader, atmosphere); //drain2 is atmo because intermediate loop is not implemented yet
        pcsCooler2 = new WaterWaterHeatExchanger(41.0f, 55.5, 65, pcsFilterPressureHeader, atmosphere);
        regenerator1 = new WaterWaterHeatExchanger(91.5f, 55.5, 55.5, pcsCooler1, pcsReturnHeader);
        regenerator2 = new WaterWaterHeatExchanger(91.5f, 55.5, 55.5, pcsCooler2, pcsReturnHeader);
        pcsFilterPressureHeader.drains[0] = regenerator1; //set drains to address of instance
        pcsFilterPressureHeader.drains[1] = regenerator2;
        pcsFilterPressureHeader.drains[2] = demineralizedWaterTank;
        pcsFilterPressureHeader.drains[3] = NPPSim.feedwaterMixer1;
        pcsFilterPressureHeader.drains[4] = NPPSim.feedwaterMixer2;
        pcsPressureHeader = new SimplePressureHeader(new Connectable[] {regenerator1, regenerator2}, 0.5);
        pcsPressureHeader.isolationValveArray.forEach(valve -> {
            valve.setPosition(1.0f); //open header valves for both regenerators
        });
        pcsValve = new pcsMockupValve(pcsSuctionHeader, pcsPressureHeader); 
        pcsPump1 = new SimplePump(2985, 0.1389f, 12.36, 0.16671305, 20, 20, 83, pcsSuctionHeader, pcsPressureHeader);
        pcsPump2 = new SimplePump(2985, 0.1389f, 12.36, 0.16671305, 20, 20, 83, pcsSuctionHeader, pcsPressureHeader);
        pcsCoolingHeader = new SimplePressureHeader(new Connectable[] {pcsCooler1, pcsCooler2}, 0.5);
        pcsCoolingHeader.isolationValveArray.forEach(valve -> {
            valve.setPosition(1.0f); //open header valves for both regenerators
        });
        coolingPump = new SimplePump(1000, 0.577f, 2.0, 0, 20, 25, 83, intermediateLoop, pcsCoolingHeader);
        admsHeader = new PressureHeader();
        for (int i = 0; i < 4; i++) {
            admsPumps.add(new Pump(1480, 0.1389f, 2.157, 0.024516625, 5, 35, 83, demineralizedWaterTank, admsHeader));
        }
        admsHeader.setSources(new Pump[] {admsPumps.get(0), admsPumps.get(1), admsPumps.get(2), admsPumps.get(3)});
        for (int i = 0; i < 4; i++) {
            dearatorMakeupValves.add(new WaterValve(209.77, 12, admsHeader, dearators.get(i)));
        }
        for (int i = 0; i < 4; i++) {
            dearatorOverflowValves.add(new WaterValve(41.4, 10, dearators.get(i), demineralizedWaterTank));
        }
        // DW 补水环路（2026-10-28）：河 → 通用净化装置 → DW 泵 → 除盐水箱
        dwMakeupPump = new SimplePump(700, 0.2f, 0.83, 0.024516625, 10, 10, 10, purification, demineralizedWaterTank);   // 最大 ≈200 kg/s
        // 除盐水箱排水阀：箱体 → 大气（排出系统外）。仿给水系统阀门，全行程 10s，自动控制见 update()
        // 流量按孔口出流由液位驱动（见 DwTankDrainValve），额定 100 kg/s
        // 水箱排水环路（2026-10-28）：除盐水箱 → 排水阀 → 河
        dwTankDrainValve = new DwTankDrainValve(demineralizedWaterTank, river, 300);
    }
    
    public void update() {
        river.update();
        // 净化装置流量跟随下游 DW 泵（串联）；泵停时流量为 0、电流为 0
        purification.update(dwMakeupPump != null ? dwMakeupPump.getFlowRate() : 0.0);
        dwMakeupPump.update();
        pcsValve.update();
        pcsPump1.update();
        pcsPump2.update();
        coolingPump.update();
        pcsSuctionHeader.update();
        pcsPressureHeader.update();
        regenerator1.update();
        regenerator2.update();
        pcsCoolingHeader.update();
        pcsFilterPressureHeader.update();
        pcsCooler1.update();
        pcsCooler2.update();
        pcsReturnHeader.update();
        demineralizedWaterTank.update();
        dearatorMakeupValves.forEach(valve -> {
            valve.update();
        });
        admsHeader.update();
        admsPumps.forEach(pump -> {
            pump.update();
        });
        
        dearatorOverflowValves.forEach(valve -> {
            if (valve.source.getWaterLevel() > 15) {
                valve.setState(2);
            } else if (valve.source.getWaterLevel() < 10) {
                valve.setState(0);
            }
            valve.update();
        });

        // 除盐水箱排水阀：水位过高自动开启排水（离开阈值自动关闭）。
        // 用 autoState 而不是 state，这样操作员在面板上手动"开/关"时优先执行手动指令。
        double dwLevel = demineralizedWaterTank.getWaterLevel();
        if (dwTankDrainValve.isAutoEnabled()) {
            if (dwLevel > DW_DRAIN_HIGH) {
                dwTankDrainValve.setAutoState(2);
            } else if (dwLevel < DW_DRAIN_LOW) {
                dwTankDrainValve.setAutoState(0);
            }
        } else {
            dwTankDrainValve.setAutoState(1);   // 自动退出且手动在"停止"位：保持当前阀位
        }
        dwTankDrainValve.update();
    }

    /** 水箱排水阀是否已开启（>1% 开度即视为"排水阀开启"，界面亮红色报警） */
    boolean isDrainValveOpen() {
        return dwTankDrainValve != null && dwTankDrainValve.getPosition() > 0.01f;
    }
}