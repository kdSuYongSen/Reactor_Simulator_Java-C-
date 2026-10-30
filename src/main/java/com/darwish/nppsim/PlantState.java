package com.darwish.nppsim;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 整座电厂的设备状态快照（v2 存档的核心）。
 *
 * <p>与旧版 {@code ArrayList<Serializable> stateArray} 的“构造顺序 / 位置索引”不同，
 * 这里直接保存 {@link NPPSim} 的静态根字段与列表。Java 序列化会保留对象之间的引用
 * 关系（阀门 ↔ 母管、泵 ↔ 泵、堆芯 ↔ 通道等），读档时按字段恢复即可，
 * 不再依赖 {@code state.get(205)} 这类硬编码索引——只要设备对象的类型不变，
 * 增删无关组件不会让整份存档错位。
 *
 * <p>不在这里保存：
 * <ul>
 *   <li>{@code Loader.tables}（蒸汽物性表，运行环境资源）；</li>
 *   <li>{@code UI} 窗口/线程（读档后新建）；</li>
 *   <li>{@code Core.kinetics}（native 句柄，单独存 {@link SaveGame#nativeCoreState}）；</li>
 *   <li>{@code Separator.steamSource}（transient，读档后重连）。</li>
 * </ul>
 */
final class PlantState implements Serializable {

    private static final long serialVersionUID = 1L;

    Atmosphere atmosphere;
    MCC mcc;
    Core core;

    ArrayList<SteamValve> sdv_c;
    ArrayList<SteamValve> sdv_a;
    ArrayList<SteamValve> msvLoop1;
    ArrayList<SteamValve> msvLoop2;
    ArrayList<SteamValve> TG1InletValves;
    ArrayList<SteamValve> TG2InletValves;

    ArrayList<Pump> condensate1A;
    ArrayList<Pump> condensate2A;
    ArrayList<Pump> condensate1B;
    ArrayList<Pump> condensate2B;
    ArrayList<Pump> mainFeedwaterPumps;
    ArrayList<Pump> auxFeedwaterPumps;

    ArrayList<WaterValve> dearatorValves;
    ArrayList<WaterValve> mainFeederValves;
    ArrayList<WaterValve> auxFeederValves;

    ArrayList<Dearator> dearators;
    ArrayList<Ejector> ejectors;
    VacuumPump vacuumPump1;
    VacuumPump vacuumPump2;

    TG tg1;
    TG tg2;

    OneWaySteamHeader steamPiping;
    PressureHeader auxiliaryFWPressureHeader;
    PressureHeader mainFWPressureHeader;
    PressureHeader condensateHeader1;
    PressureHeader condensateHeader2;
    SimplifiedCondensateHeader condensateHeader;
    SimpleSuctionHeader fwSuctionHeader;
    WaterMixer feedwaterMixer1;
    WaterMixer feedwaterMixer2;

    AutoControl autoControl;
    PCS pcs;

    ArrayList<HeatExchanger> heatExchangers;
    ArrayList<Separator> separators;
    SeparatorSteamSource[] separatorSources;
    SteamCollectorHeader deaeratorSteamHeader;
    CondensateFilter pcsCondensateFilter;
    LPHeater lpHeater;

    /** 从当前 {@link NPPSim} 的静态字段抓取快照（调用方负责先暂停仿真）。 */
    static PlantState capture() {
        PlantState s = new PlantState();
        s.atmosphere = NPPSim.atmosphere;
        s.mcc = NPPSim.mcc;
        s.core = NPPSim.core;

        s.sdv_c = copy(NPPSim.sdv_c);
        s.sdv_a = copy(NPPSim.sdv_a);
        s.msvLoop1 = copy(NPPSim.msvLoop1);
        s.msvLoop2 = copy(NPPSim.msvLoop2);
        s.TG1InletValves = copy(NPPSim.TG1InletValves);
        s.TG2InletValves = copy(NPPSim.TG2InletValves);

        s.condensate1A = copy(NPPSim.condensate1A);
        s.condensate2A = copy(NPPSim.condensate2A);
        s.condensate1B = copy(NPPSim.condensate1B);
        s.condensate2B = copy(NPPSim.condensate2B);
        s.mainFeedwaterPumps = copy(NPPSim.mainFeedwaterPumps);
        s.auxFeedwaterPumps = copy(NPPSim.auxFeedwaterPumps);

        s.dearatorValves = copy(NPPSim.dearatorValves);
        s.mainFeederValves = copy(NPPSim.mainFeederValves);
        s.auxFeederValves = copy(NPPSim.auxFeederValves);

        s.dearators = copy(NPPSim.dearators);
        s.ejectors = copy(NPPSim.ejectors);
        s.vacuumPump1 = NPPSim.vacuumPump1;
        s.vacuumPump2 = NPPSim.vacuumPump2;

        s.tg1 = NPPSim.tg1;
        s.tg2 = NPPSim.tg2;

        s.steamPiping = NPPSim.steamPiping;
        s.auxiliaryFWPressureHeader = NPPSim.auxiliaryFWPressureHeader;
        s.mainFWPressureHeader = NPPSim.mainFWPressureHeader;
        s.condensateHeader1 = NPPSim.condensateHeader1;
        s.condensateHeader2 = NPPSim.condensateHeader2;
        s.condensateHeader = NPPSim.condensateHeader;
        s.fwSuctionHeader = NPPSim.fwSuctionHeader;
        s.feedwaterMixer1 = NPPSim.feedwaterMixer1;
        s.feedwaterMixer2 = NPPSim.feedwaterMixer2;

        s.autoControl = NPPSim.autoControl;
        s.pcs = NPPSim.pcs;

        s.heatExchangers = copy(NPPSim.heatExchangers);
        s.separators = copy(NPPSim.separators);
        s.separatorSources = NPPSim.separatorSources == null
                ? null : NPPSim.separatorSources.clone();
        s.deaeratorSteamHeader = NPPSim.deaeratorSteamHeader;
        s.pcsCondensateFilter = NPPSim.pcsCondensateFilter;
        s.lpHeater = NPPSim.lpHeater;

        return s;
    }

    private static <T> ArrayList<T> copy(ArrayList<T> src) {
        return src == null ? null : new ArrayList<>(src);
    }
}
