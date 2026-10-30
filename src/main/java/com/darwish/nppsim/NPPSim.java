package com.darwish.nppsim;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class NPPSim {
    static UI ui;
    //list of all npp components
    static Atmosphere atmosphere;
    static MCC mcc;
    static Core core;
    static ArrayList<SteamValve> sdv_c;
    static ArrayList<SteamValve> sdv_a;
    static ArrayList<SteamValve> msvLoop1;
    static ArrayList<SteamValve> msvLoop2;
    static ArrayList<SteamValve> TG1InletValves;
    static ArrayList<SteamValve> TG2InletValves;
    static ArrayList<Pump> condensate1A; 
    static ArrayList<Pump> condensate2A;
    static ArrayList<Pump> condensate1B;
    static ArrayList<Pump> condensate2B;
    static ArrayList<Pump> mainFeedwaterPumps;
    static ArrayList<Pump> auxFeedwaterPumps;
    static ArrayList<WaterValve> dearatorValves;
    static ArrayList<WaterValve> mainFeederValves;
    static ArrayList<WaterValve> auxFeederValves;
    static ArrayList<Dearator> dearators;
    static ArrayList<Ejector> ejectors;
    /** 真空抽气泵（凝结水页面控制，各作用于一台凝汽器） */
    static VacuumPump vacuumPump1, vacuumPump2;
    static TG tg1, tg2;
    static SteamTables tables;
    static OneWaySteamHeader steamPiping;
    static PressureHeader auxiliaryFWPressureHeader, mainFWPressureHeader, condensateHeader1, condensateHeader2;
    static SimplifiedCondensateHeader condensateHeader;
    static SimpleSuctionHeader fwSuctionHeader;
    static WaterMixer feedwaterMixer1, feedwaterMixer2;
    static AutoControl autoControl;
    static PCS pcs;
    /** 凝结水系统排水阀（凝结水联箱 → 除盐水箱）：手动放水，开阀时界面亮红色"排水阀开启" */
    static WaterValve condensateDrainValve;
    /** 给水系统排水阀（主给水母管 → 除盐水箱）：手动放水，开阀时界面亮红色"排水阀开启" */
    static WaterValve feedwaterDrainValve;
    /** 换热器 A/B（一回路↔二回路热耦合 + 离线冷却河水侧） */
    static ArrayList<HeatExchanger> heatExchangers = new ArrayList<>();

    /**
     * 仅用于自检/调试：≥0 时替代 {@link Core#getThermalPower()} 作为堆芯热功率（MW）。
     * 正常运行为 -1（跟随动力学）。用来在不开棒的情况下验证"堆芯热量 → 汽包 → 二回路"链路。
     */
    static double coreHeatOverrideMW = -1;

    /**
     * 仅用于自检/调试：>0 时按该流量（kg/s，两台合计）给换热器管侧喂给水，
     * 用来在不启动整套给水系统的情况下验证"堆芯功率 → 产汽量"的关系。正常为 0。
     */
    static double feedwaterOverrideKgs = 0;
    static ArrayList<Separator> separators = new ArrayList<>();
    /** 除氧器蒸汽出口联箱：除氧器 → 汽轮机主汽门 / 旁路膨胀阀 / 抽气器 */
    static SteamCollectorHeader deaeratorSteamHeader;
    /** PCS 凝结水过滤段（热阱）：凝结水联箱 → 除氧器水入口 */
    static CondensateFilter pcsCondensateFilter;
    /** 低压加热器 LP-1：凝结水联箱 → 0.15 MPa 抽汽加热 → 过滤段（2026-10-19 新增） */
    static LPHeater lpHeater;
    /** 汽水分离器出口 → 二次侧蒸汽母管的适配器 */
    static SeparatorSteamSource[] separatorSources = new SeparatorSteamSource[2];
    
    /**
     * 汽轮机主汽门 Cv（每台机 2 只，共 4 只）。
     *
     * 定径依据（tools/steam_loop_check/PlantBalance 实测）：
     *   除氧器蒸汽出口联箱 0.61 MPa / 凝汽器 0.0061 MPa，单只阀门
     *   453 kg/s @ Cv=25000；额定工况单只需通过 ~452 kg/s（二回路产汽
     *   ~2255 kg/s，四只均分，已扣除氧器抽汽），故取 2.47e4。
     * 旧值 3044.72（≈53 kg/s/只）是全厂蒸汽直连汽轮机时代的旁路口径，
     * 全厂蒸汽改走除氧器后只够 250 kg/s，汽轮机永远带不上负荷。
     */
    /** 主汽门 Cv：主蒸汽 4.0 MPa、单机 900 kg/s 量级。
     *  旧值 2.47e4 是按 0.7 MPa 二回路定径的，压力提高到 4 MPa 后必须重定径。 */
    /** 每台汽轮机循环水总量（m³/s）；分给 4 台凝汽器 */
    static final double CONDENSER_CW_M3S = 45.0;

    static final double MAIN_STEAM_CV = 4.9e3;
    
    private static Thread updateThread, printThread;
    private static boolean simPaused = false, updating = false, run = true;
    public static boolean core1=false;

    //debug parts here

    //variables for update thread
    private static int updateCount = 0;
    /** UI 刷新节拍（仿真每 4 tick ≈ 200ms 刷一次界面） */
    private int uiTickCounter = 0;
    //private static boolean updated = false;
    private static int deltaTimeStepLength = 0;
    private static long timeStepLengthCumulative; //IT IS USED DONT BELIEVE THE IDE

    
    /**
     * new simulation instance with default values
     */
    public NPPSim() {
        sdv_c = new ArrayList<>(); 
        sdv_a = new ArrayList<>();
        msvLoop1 = new ArrayList<>(); 
        msvLoop2 = new ArrayList<>();
        TG1InletValves = new ArrayList<>(); 
        TG2InletValves = new ArrayList<>(); 
        condensate1A = new ArrayList<>(); 
        condensate2A = new ArrayList<>();
        condensate1B = new ArrayList<>();
        condensate2B = new ArrayList<>();
        mainFeedwaterPumps = new ArrayList<>();
        auxFeedwaterPumps = new ArrayList<>();
        dearatorValves = new ArrayList<>();
        mainFeederValves = new ArrayList<>();
        auxFeederValves = new ArrayList<>();
        dearators = new ArrayList<>();
        ejectors = new ArrayList<>();

        //parse the steam tables
        tables = Loader.tables;
        
        //initialize all components
        atmosphere = new Atmosphere(15);
        core = new Core();
        mcc = new MCC();

        // 换热器 A/B（一回路→二回路）与汽水分离器 A/B（二次侧）
        // 注意：必须在给水混合器之前创建（混合器直接送水到换热器管侧）
        heatExchangers.clear();
        separators.clear();
        heatExchangers.add(new HeatExchanger("A"));
        heatExchangers.add(new HeatExchanger("B"));
        separators.add(new Separator("A"));
        separators.add(new Separator("B"));
        separatorSources[0] = new SeparatorSteamSource(separators.get(0));
        separatorSources[1] = new SeparatorSteamSource(separators.get(1));
        separators.get(0).setSteamSource(separatorSources[0]);
        separators.get(1).setSteamSource(separatorSources[1]);

        // 二次侧蒸汽母管：由汽水分离器供汽（不再直接接汽包）
        steamPiping = new OneWaySteamHeader(separatorSources);
        // 除氧器蒸汽出口联箱：全厂蒸汽先经过除氧器，再由这里供汽轮机 / 旁路 / 抽气器
        deaeratorSteamHeader = new SteamCollectorHeader();

        //TG and Condenser Components
        /* 每台汽轮机 4 台凝汽器（共 8 台）。循环水总量不变，4 台各分 1/4：
         * 单台停循环泵只掉 1/4 的排汽面积，真空是渐进的，页面上也能看出是哪一台。 */
        tg1 = new TG(new Condenser[]{
                new Condenser(CONDENSER_CW_M3S / 4), new Condenser(CONDENSER_CW_M3S / 4),
                new Condenser(CONDENSER_CW_M3S / 4), new Condenser(CONDENSER_CW_M3S / 4)});
        tg2 = new TG(new Condenser[]{
                new Condenser(CONDENSER_CW_M3S / 4), new Condenser(CONDENSER_CW_M3S / 4),
                new Condenser(CONDENSER_CW_M3S / 4), new Condenser(CONDENSER_CW_M3S / 4)});
        tg1.setTG1();

        for (int i = 0; i < 4; i++) {
            ejectors.add(new Ejector(5.14, 10, steamPiping, atmosphere, tg1.condenser, atmosphere));
        }
        for (int i = 0; i < 4; i++) {
            ejectors.add(new Ejector(5.14, 10, steamPiping, atmosphere, tg2.condenser, atmosphere));
        }
        vacuumPump1 = new VacuumPump(tg1.condenser);
        vacuumPump2 = new VacuumPump(tg2.condenser);

        /* 主汽门：汽源 = 主蒸汽母管（2026-10-19 改为直供）。
         * 旧接线是"分离器 → 除氧器 → 汽轮机"，全部蒸汽都要过除氧器，
         * 既要 30% 的假抽汽，又把主蒸汽压力降到 0.6 MPa（压力等级白提）。 */
        TG1InletValves.add(new SteamValve(MAIN_STEAM_CV, 20, steamPiping, tg1));
        TG1InletValves.add(new SteamValve(MAIN_STEAM_CV, 20, steamPiping, tg1));
        TG2InletValves.add(new SteamValve(MAIN_STEAM_CV, 20, steamPiping, tg2));
        TG2InletValves.add(new SteamValve(MAIN_STEAM_CV, 20, steamPiping, tg2));

        //add the eight SDV-C valves, 4 per loop（旁路膨胀阀：除氧器出口 → 凝汽器）
        for (int i = 0; i < 4; i++) {
            sdv_c.add(new SteamValve(745.76, 8, steamPiping, tg1.condenser));
        }
        for (int i = 0; i < 4; i++) {
            sdv_c.add(new SteamValve(745.76, 8, steamPiping, tg2.condenser));
        }
        
        //SDV-A
        sdv_a.add(new SteamValve(471.03, 8, mcc.drum1, atmosphere));
        sdv_a.add(new SteamValve(471.03, 8, mcc.drum2, atmosphere));

        //MSV
        msvLoop1.add(new SteamValve(461.33, 8, mcc.drum1, atmosphere)); //group 1
        msvLoop2.add(new SteamValve(461.33, 8, mcc.drum2, atmosphere));
        for (int i = 0; i < 2; i++) {
            msvLoop1.add(new SteamValve(458.53, 8, mcc.drum1, atmosphere)); //group 2
            msvLoop2.add(new SteamValve(458.53, 8, mcc.drum2, atmosphere));
        }
        for (int i = 0; i < 3; i++) {
            msvLoop1.add(new SteamValve(455.48, 8, mcc.drum1, atmosphere)); //group 3
            msvLoop2.add(new SteamValve(455.48, 8, mcc.drum2, atmosphere));
        }
  
        //initialize condensate pumps and dearators  TODO this is greatly simplified needs reworking
        condensateHeader = new SimplifiedCondensateHeader();
        condensateHeader1 = new PressureHeader();
        condensateHeader2 = new PressureHeader();
        // PCS 凝结水过滤段（热阱）：凝结水联箱 → 除氧器水入口；排污送除盐水箱（PCS 建好后接上）
        /* 二回路凝结水流程（与设计拓扑一致）：
         *   凝汽器 → 凝结水泵 → 凝结水联箱 → **精处理(CCPU)** → **低压加热器 LP-1** → 除氧器
         * 精处理必须放在水温最低处：混合床树脂长期工作温度上限约 60 °C，
         * 放到加热器后面（104 °C）会把树脂烫坏。 */
        pcsCondensateFilter = new CondensateFilter(condensateHeader, null);
        lpHeater = new LPHeater(
                new Connectable[]{tg1.lpExtractionPort, tg2.lpExtractionPort},
                null,                // 疏水去向在除氧器建好之后补（见下方 setDrains）
                pcsCondensateFilter);
        for (int i = 0; i < 3; i++) {
            condensate1A.add(new Pump(740, 0.95f, 1.117, 0.022555295, 30, 40, 167, tg1.condenser, condensateHeader1));
        }
        for (int i = 0; i < 3; i++) {
            condensate2A.add(new Pump(740, 0.95f, 1.117, 0.022555295, 30, 40, 167, tg2.condenser, condensateHeader2));
        }
        for (int i = 0; i < 3; i++) {
            condensate1B.add(new Pump(2975, 0.95f, 1.766, 0.2157463, 30, 40, 267, condensateHeader1, condensateHeader));
        }
        for (int i = 0; i < 3; i++) {
            condensate2B.add(new Pump(2975, 0.95f, 1.766, 0.2157463, 30, 40, 267, condensateHeader2, condensateHeader));
        }
        condensateHeader1.setSources(new Pump[] {condensate1A.get(0), condensate1A.get(1), condensate1A.get(2)});
        condensateHeader2.setSources(new Pump[] {condensate2A.get(0), condensate2A.get(1), condensate2A.get(2)});
        condensateHeader.setSources(new Pump[] {condensate1B.get(0), condensate1B.get(1), condensate1B.get(2), condensate2B.get(0), condensate2B.get(1), condensate2B.get(2)});
        for (int i = 0; i < 4; i++) {
            /* 抽汽式：0/1 号接 TG-1 的抽汽口，2/3 号接 TG-2；余汽排各自的凝汽器。 */
            dearators.add(new Dearator(i,
                    i < 2 ? tg1.extractionPort : tg2.extractionPort,
                    i < 2 ? tg1.condenser : tg2.condenser));
            // 除氧器水入口：水来自 PCS 凝结水过滤段（热阱），不再直接取自凝结水联箱
            dearatorValves.add(new WaterValve(1667.52, 12, lpHeater, dearators.get(i))); //1206.3
        }
        // 低压加热器疏水 → 4 台除氧器（此时除氧器才建好）
        if (lpHeater != null) {
            lpHeater.setDrains(dearators.toArray(new Connectable[0]));
        }
        // 除氧器蒸汽出口联箱的源 = 4 台除氧器（除氧器必须在上面建完之后才能接）
        deaeratorSteamHeader.setSources(new Connectable[] {
            dearators.get(0), dearators.get(1), dearators.get(2), dearators.get(3)
        });
        
        //initialize feedwater pumps and piping/valves
        // 给水 → 换热器管侧（不再是直接进汽包；汽包水位由换热器凝结水回注维持）
        feedwaterMixer1 = new WaterMixer(heatExchangers.get(0), 2);
        feedwaterMixer2 = new WaterMixer(heatExchangers.get(1), 2);
        
        //auxiliaryFWSuctionHeader = new SimpleSuctionHeader(new Connectable[] {dearators.get(0), dearators.get(1), dearators.get(2), dearators.get(3)});
        fwSuctionHeader = new SimpleSuctionHeader(new Connectable[] {dearators.get(0), dearators.get(1), dearators.get(2), dearators.get(3)}, 1);
        // 4 台除氧器并联供水：隔离阀全部打开，按阀位均分给水需求。
        // 旧实现只打开第 1 台，全部给水都从 1 号除氧器抽取 —— 1 号被抽成负质量
        // （随后被 sanitize 钳掉，质量凭空消失），2~4 号却因只进不出而水位上涨、
        // 经除氧器溢流阀排到除盐水箱，整条给水回路的水就这么"漏"光了。
        fwSuctionHeader.isolationValveArray.forEach(valve -> valve.setPosition(1.0f));
        auxiliaryFWPressureHeader = new PressureHeader();
        mainFWPressureHeader = new PressureHeader();
        for (int i = 0; i < 7; i++) {
            mainFeedwaterPumps.add(new Pump(2982, 0.95f, 8.9, 0.14709975, 30, 40, 833, fwSuctionHeader, mainFWPressureHeader)); //9.5
        }
        for (int i = 0; i < 6; i++) {
            auxFeedwaterPumps.add(new Pump(2970, 0.15f, 8.6, 0.08825985, 30, 40, 133, fwSuctionHeader, auxiliaryFWPressureHeader)); //9.1
        } 
        mainFWPressureHeader.setSources(new Pump[] {mainFeedwaterPumps.get(0), mainFeedwaterPumps.get(1), mainFeedwaterPumps.get(2), mainFeedwaterPumps.get(3), mainFeedwaterPumps.get(4), mainFeedwaterPumps.get(5), mainFeedwaterPumps.get(6)});
        auxiliaryFWPressureHeader.setSources(new Pump[] {auxFeedwaterPumps.get(0), auxFeedwaterPumps.get(1), auxFeedwaterPumps.get(2), auxFeedwaterPumps.get(3), auxFeedwaterPumps.get(4), auxFeedwaterPumps.get(5)});
        for (int i = 0; i < 3; i++) {
            mainFeederValves.add(new WaterValve(498.01, 40, mainFWPressureHeader, feedwaterMixer1));
        }
        for (int i = 0; i < 3; i++) {
            mainFeederValves.add(new WaterValve(498.01, 40, mainFWPressureHeader, feedwaterMixer2));
        }
        auxFeederValves.add(new WaterValve(197.44, 40, auxiliaryFWPressureHeader, feedwaterMixer1));
        auxFeederValves.add(new WaterValve(197.44, 40, auxiliaryFWPressureHeader, feedwaterMixer2));
        
        //PCS
        pcs = new PCS();
        // PCS 凝结水过滤段的排污送除盐水箱
        pcsCondensateFilter.setBlowdownSink(pcs.demineralizedWaterTank);

        /* 凝结水 / 给水系统排水阀（2026-10-28 新增）。
         * 这两个是**手动放水阀**（检修/水质不合格时排放），默认关闭：
         *   · 凝结水联箱 → 除盐水箱
         *   · 主给水母管 → 除盐水箱
         * 开阀时对应窗口亮红色"排水阀开启"报警，提醒有人在放水。 */
        condensateDrainValve = new WaterValve(200.0, 30, condensateHeader, pcs.demineralizedWaterTank);
        feedwaterDrainValve = new WaterValve(120.0, 40, mainFWPressureHeader, pcs.demineralizedWaterTank);

        // MCP A/B 默认开启（环路 1/2 各一台）：400 RPM（额定 1000 的 40%）、节流阀 70%
        mcc.mcp.get(0).setActive(true);
        mcc.mcp.get(4).setActive(true);
        mcc.mcp.get(0).rpmSetting = 0.4f;
        mcc.mcp.get(4).rpmSetting = 0.4f;
        /* 8 台主循环泵的出口阀统一 70% 开度。
         * 原来只给 0/4 号（每环路第一台）设了 0.7，其余 6 台是 0%：
         * 操作员启动第 2/3/4 台泵时"泵转了但没流量"，看起来像阀门初始化坏了。
         * 主循环泵没有单独的流量控制回路，出口阀就是节流件，统一开度才一致。 */
        for (Pump pump : mcc.mcp) {
            if (pump != null && pump.dischargeValve != null) {
                pump.dischargeValve.setPosition(0.7f);
            }
        }
        mcc.mcp.get(0).dischargeValve.setAutoState(1);   // 阀位保持 70%（不被自动开阀推到全开）
        mcc.mcp.get(4).dischargeValve.setAutoState(1);

        // 换热器/汽水分离器已在上面创建（二次侧回路）

        //凝汽器排水阀：把热井多余的水排到除盐水箱（水位过高时自动开启，见 Condenser.update）
        tg1.condenser.attachDrain(pcs.demineralizedWaterTank);
        tg2.condenser.attachDrain(pcs.demineralizedWaterTank);
        // 应急排水目标：热井水位 >150% 时把多余水量转入除盐水箱（防止数值溢出）
        tg1.condenser.setReliefTarget(pcs.demineralizedWaterTank);
        tg2.condenser.setReliefTarget(pcs.demineralizedWaterTank);

        // 汽水分离器泄压阀：高压力/高水位自动开阀，排向除盐水箱（后续改抑压水池）
        if (separators != null) {
            for (Separator sep : separators) {
                if (sep != null) {
                    sep.setReliefTarget(pcs.demineralizedWaterTank);
                }
            }
        }
        
        //initialize the automatic control system
        autoControl = new AutoControl();

        //set variables if needed
//        core.coreArray.forEach(row -> {
//            row.forEach(channel -> {
//                channel.setNeutronCount(new Double[] {1e9, 0.0});
//                if(channel instanceof ControlRodChannel) {
//                    if (channel instanceof FASRChannel) {
//                        ((ControlRodChannel) channel).setPosition(0.0f);
//                    } else {
//                        ((ControlRodChannel) channel).setPosition(0.65f);
//                    }
//                }
//            });
//        });
//        autoControl.az1Control.setEnabled(false);
//        mcc.setWaterTemp(280);
//        mcc.mcp.get(0).setActive(true);
//        mcc.mcp.get(1).setActive(true);
//        mcc.mcp.get(4).setActive(true);
//        mcc.mcp.get(5).setActive(true);
//        mcc.mcp.get(0).setRPM(1000);
//        mcc.mcp.get(1).setRPM(1000);
//        mcc.mcp.get(4).setRPM(1000);
//        mcc.mcp.get(5).setRPM(1000);
//        autoControl.automaticRodController.setSetpoint(500);
//        dearators.forEach(dearator -> {
//            dearator.setWaterTemp(190);
//        });
        

        //initialize UI
        simPaused = false;
        ui = new UI();

        //start simulation
        run = true;
        updateTimer();
        printTimer();
    }
    
    /**
     * 从 v2 存档的 {@link PlantState} 构造仿真。
     *
     * 新的存档路径：直接按字段恢复设备对象图，不再依赖 stateArray 的位置索引。
     */
    public NPPSim(PlantState plant, SaveGame game) {
        if (plant == null) {
            throw new IllegalArgumentException("PlantState is null");
        }
        tables = Loader.tables;

        atmosphere = plant.atmosphere;
        core = plant.core;
        mcc = plant.mcc;

        sdv_c = orEmpty(plant.sdv_c);
        sdv_a = orEmpty(plant.sdv_a);
        msvLoop1 = orEmpty(plant.msvLoop1);
        msvLoop2 = orEmpty(plant.msvLoop2);
        TG1InletValves = orEmpty(plant.TG1InletValves);
        TG2InletValves = orEmpty(plant.TG2InletValves);

        condensate1A = orEmpty(plant.condensate1A);
        condensate2A = orEmpty(plant.condensate2A);
        condensate1B = orEmpty(plant.condensate1B);
        condensate2B = orEmpty(plant.condensate2B);
        mainFeedwaterPumps = orEmpty(plant.mainFeedwaterPumps);
        auxFeedwaterPumps = orEmpty(plant.auxFeedwaterPumps);

        dearatorValves = orEmpty(plant.dearatorValves);
        mainFeederValves = orEmpty(plant.mainFeederValves);
        auxFeederValves = orEmpty(plant.auxFeederValves);

        dearators = orEmpty(plant.dearators);
        ejectors = orEmpty(plant.ejectors);
        vacuumPump1 = plant.vacuumPump1;
        vacuumPump2 = plant.vacuumPump2;

        tg1 = plant.tg1;
        tg2 = plant.tg2;

        steamPiping = plant.steamPiping;
        auxiliaryFWPressureHeader = plant.auxiliaryFWPressureHeader;
        mainFWPressureHeader = plant.mainFWPressureHeader;
        condensateHeader1 = plant.condensateHeader1;
        condensateHeader2 = plant.condensateHeader2;
        condensateHeader = plant.condensateHeader;
        fwSuctionHeader = plant.fwSuctionHeader;
        feedwaterMixer1 = plant.feedwaterMixer1;
        feedwaterMixer2 = plant.feedwaterMixer2;

        autoControl = plant.autoControl;
        pcs = plant.pcs;

        heatExchangers = orEmpty(plant.heatExchangers);
        separators = orEmpty(plant.separators);
        separatorSources = plant.separatorSources;
        deaeratorSteamHeader = plant.deaeratorSteamHeader;
        pcsCondensateFilter = plant.pcsCondensateFilter;
        lpHeater = plant.lpHeater;

        if (separatorSources == null) {
            separatorSources = new SeparatorSteamSource[separators.size()];
            for (int i = 0; i < separators.size(); i++) {
                separatorSources[i] = new SeparatorSteamSource(separators.get(i));
            }
        }
        if (deaeratorSteamHeader == null && !dearators.isEmpty()
                && dearators.get(0).steamOutlet.drain instanceof SteamCollectorHeader) {
            deaeratorSteamHeader = (SteamCollectorHeader) dearators.get(0).steamOutlet.drain;
        }
        if (pcsCondensateFilter == null && !dearatorValves.isEmpty()
                && dearatorValves.get(0).source instanceof CondensateFilter) {
            pcsCondensateFilter = (CondensateFilter) dearatorValves.get(0).source;
        }
        if (pcsCondensateFilter == null && !dearatorValves.isEmpty()
                && dearatorValves.get(0).source instanceof LPHeater) {
            // 新接线：阀门源是低压加热器，精处理段在它的上游
            lpHeater = (LPHeater) dearatorValves.get(0).source;
            if (lpHeater.getUpstream() instanceof CondensateFilter) {
                pcsCondensateFilter = (CondensateFilter) lpHeater.getUpstream();
            }
        }

        relinkPlant();

        if (core != null && game != null && game.nativeCoreState != null) {
            if (core.restoreNativeState(game.nativeCoreState)) {
                core.restoreInputs(game.coreInputs);
                System.out.println("[Save] 堆芯状态恢复: " + core.getThermalPower()
                        + " MW, 棒位=" + core.getRodInsertion()
                        + ", 氙=" + core.getXenonWorthPcm() + " pcm");
            } else {
                System.err.println("[Save] 物理内核不支持状态恢复，堆芯按停堆态载入");
            }
        } else {
            System.err.println("[Save] 存档不含 native 堆芯状态，堆芯按停堆态载入");
        }

        simPaused = false;
        ui = new UI();
        run = true;
        updateTimer();
        printTimer();
    }

    private static <T> ArrayList<T> orEmpty(ArrayList<T> list) {
        return list != null ? list : new ArrayList<>();
    }

    /** 重新连接 transient 字段（Separator.steamSource）以及读档后可能失效的静态别名。 */
    private void relinkPlant() {
        if (steamPiping != null) {
            steamPiping.sources = separatorSources;
        }
        for (int i = 0; i < separators.size() && i < separatorSources.length; i++) {
            separators.get(i).setSteamSource(separatorSources[i]);
        }
        if (feedwaterMixer1 != null && heatExchangers.size() > 0) {
            feedwaterMixer1.drain = heatExchangers.get(0);
        }
        if (feedwaterMixer2 != null && heatExchangers.size() > 1) {
            feedwaterMixer2.drain = heatExchangers.get(1);
        }
        if (deaeratorSteamHeader != null) {
            deaeratorSteamHeader.setSources(dearators.toArray(new Connectable[0]));
        }
        if (pcsCondensateFilter != null) {
            if (condensateHeader != null) {
                pcsCondensateFilter.setUpstream(condensateHeader);
            }
            if (pcs != null && pcs.demineralizedWaterTank != null) {
                pcsCondensateFilter.setBlowdownSink(pcs.demineralizedWaterTank);
            }
        }
        /* 低压加热器是流过式的（没有自己的水库存），读档时重接上游即可：
         * 它位于精处理之后、除氧器之前。 */
        if (lpHeater != null) {
            lpHeater.setUpstream(pcsCondensateFilter != null ? pcsCondensateFilter : condensateHeader);
            if (dearators != null) {
                lpHeater.setDrains(dearators.toArray(new Connectable[0]));
            }
        }
    }
    private void updateTimer() {
        updateThread = new Thread(() -> {
            try {
                while(run) {
                    while (simPaused) {
                        updating = false;
                        Thread.sleep(100);
                    }
                    updating = true;
                    long start = System.nanoTime();
                    //all simulation components's update methods go here. 
                    //RULE OF THUMB: objects from "Components" file should be updated before their sources/drains if possible
                    sdv_c.forEach(valve -> {
                        valve.update();
                    });
                    sdv_a.forEach(valve -> {
                        valve.update();
                    });
                    msvLoop1.forEach(valve -> {
                        valve.update();
                    });
                    msvLoop2.forEach(valve -> {
                        valve.update();
                    });
                    
                    TG1InletValves.forEach(valve -> {
                        valve.update();
                    });
                    TG2InletValves.forEach(valve -> {
                        valve.update();
                    });
                    NPPSim.dearators.forEach(dearator -> {
                        dearator.steamInlet.update();
                        dearator.steamOutlet.update();
                    });
                    steamPiping.update();
                    TG.updateGridPhase(); //static variable of class TG updated
                    tg1.update();
                    tg2.update();
                    ejectors.forEach(ejector -> {
                        ejector.update();
                    });
                    vacuumPump1.update();
                    vacuumPump2.update();
                    tg1.condenser.update();
                    tg2.condenser.update();
                    
                    dearatorValves.forEach(valve -> {
                        valve.update();
                    });
                    // PCS 凝结水过滤段（精处理）：按上游登记的取水量向凝结水联箱要水
                    // （必须排在 condensateHeader.update() 之前，需求才会被分摊给凝结水泵）
                    pcsCondensateFilter.update();
                    // 低压加热器：把精处理后的凝结水加热到 ~104 °C 再送除氧器
                    if (lpHeater != null) {
                        lpHeater.update();
                    }

                    condensateHeader.update();
                    
                    condensate1B.forEach(pump -> {
                        pump.update();
                    });
                    condensate2B.forEach(pump -> {
                        pump.update();
                    });
                    
                    condensateHeader1.update();
                    // 凝结水 / 给水系统排水阀：**自动**排水（超压自动泄放），
                    // 手动指令优先（面板"开/关"优先于 autoState）。
                    if (condensateDrainValve != null) {
                        if (condensateDrainValve.isAutoEnabled()) {
                            double p = condensateHeader.getPressure();
                            if (p > 1.20) {
                                condensateDrainValve.setAutoState(2);
                            } else if (p < 1.00) {
                                condensateDrainValve.setAutoState(0);
                            } else {
                                condensateDrainValve.setAutoState(1);
                            }
                        }
                        condensateDrainValve.update();
                    }
                    if (feedwaterDrainValve != null) {
                        if (feedwaterDrainValve.isAutoEnabled()) {
                            double p = mainFWPressureHeader.getPressure();
                            if (p > 7.50) {
                                feedwaterDrainValve.setAutoState(2);
                            } else if (p < 7.20) {
                                feedwaterDrainValve.setAutoState(0);
                            } else {
                                feedwaterDrainValve.setAutoState(1);
                            }
                        }
                        feedwaterDrainValve.update();
                    }
                    condensateHeader2.update();
                    //System.out.println(condensateHeader.pressure + " " + condensateHeader1.pressure);
                    
                    condensate1A.forEach(pump -> {
                        pump.update();
                    });
                    condensate2A.forEach(pump -> {
                        pump.update();
                    });
                    
                    feedwaterMixer1.update();
                    feedwaterMixer2.update();
                    mainFeederValves.forEach(valve -> {
                        valve.update();
                    });
                    auxFeederValves.forEach(valve -> {
                        valve.update();
                    });
                    mainFWPressureHeader.update();
                    auxiliaryFWPressureHeader.update();
                    mainFeedwaterPumps.forEach(pump -> {
                        pump.update();
                    });
                    auxFeedwaterPumps.forEach(pump -> {
                        pump.update();
                    });
                    
                    fwSuctionHeader.update();
                    
                    dearators.forEach(dearator -> {
                        dearator.update();
                    });
                    // 除氧器 → 汽轮机/旁路/抽气器 的汇管。
                    // 必须排在除氧器 update 之后：除氧器在 update 里按实际可供量回调
                    // adjustInflow()，汇管随后结算才能拿到修正后的进出汽量。
                    // 压力由各除氧器 getPressure() 给出，供下一 tick 的主汽门使用。
                    deaeratorSteamHeader.update();
                    
                    pcs.update();

                    NPPSim.core1 = true; //this value is read by the MTK so it won't update while the core is updating
                    core.update();
                    NPPSim.core1 = false;

                    /* ---- 堆芯 → 一回路热耦合（2026-10-27 补）----
                     * 之前堆芯功率只喂给动力学与界面，汽包没有任何热源（FuelChannel.update() 是空实现），
                     * 一回路只能靠"降压闪蒸"出汽 —— 二回路产汽量与反应堆功率脱钩，
                     * 高功率时也不会多产汽、低功率也不会少产汽，看起来就是"凭空产生蒸汽"。
                     * 现在把 Core.thermalPower 两台汽包均分注入，产汽量由能量平衡决定：
                     *   水升温 → 到饱和 → 蒸发 → 汽包压力升高 → 换热器抽汽增加 → 二回路产汽。 */
                    double coreMW = coreHeatOverrideMW >= 0.0 ? coreHeatOverrideMW : core.getThermalPower();
                    if (!Double.isFinite(coreMW)) {
                        coreMW = 0.0;
                    }
                    coreMW = Math.max(0.0, coreMW);
                    mcc.drum1.addCoreHeatMW(coreMW * 0.5);
                    mcc.drum2.addCoreHeatMW(coreMW * 0.5);
                    mcc.update();

                    // ---- 一回路/二回路耦合 ----
                    // 一回路：汽包 → 换热器壳侧（冷凝）→ 凝结水按同质量回注汽包（水量守恒）
                    // 二回路：给水 → 换热器管侧（蒸发）→ 汽水分离器 → 二次侧蒸汽母管
                    double mcpFlowKgs = 0;
                    for (Pump p : mcc.mcp) {
                        mcpFlowKgs += p.getFlowRate();
                    }
                    double riverKgs = core.getOfflineCooling() != null
                            ? core.getOfflineCooling().getTotalFlowKgs() : 0.0;
                    MCC.SeparatorDrum[] drums = {mcc.drum1, mcc.drum2};
                    for (int i = 0; i < 2 && i < heatExchangers.size(); i++) {
                        HeatExchanger hx = heatExchangers.get(i);
                        MCC.SeparatorDrum drum = drums[i];
                        if (feedwaterOverrideKgs > 0.0) {
                            hx.updateWaterInflow(feedwaterOverrideKgs * 0.5 * 0.05, 160.0);
                        }
                        hx.riverFlowKgs = riverKgs * 0.5;
                        hx.riverOutTC = drum.getSteamTemperature();
                        double kg = hx.updatePrimary(drum.getPressure(), drum.getSteamTemperature(),
                                mcpFlowKgs, 0.05);
                        /* 抽汽不能超过汽包汽空间的实际存量（2026-10-18 守恒修正）。
                         * 换热器原先按压差抽汽、并按"抽多少就回多少凝结水"回注，
                         * 但汽包可能根本没有这么多汽（主泵流量低/降功率时），
                         * 多出来的凝结水就是凭空造水 —— 实测默认 2 台泵 40% 时
                         * 汽包水位 300 s 从 0 涨到 +51%，同时换热器还在传 4.6 GW。
                         * 这里按汽包可供量限流，并把回注量/传热量一起改成实际值。 */
                        double availKg = drum.availableSteamKg();
                        if (kg > availKg) {
                            kg = Math.max(0.0, availKg);
                        }
                        /* 二回路吸收能力限流（2026-10-19 能量守恒修正）。
                         * 一次侧按压差抽汽时，二次侧可能只蒸发了其中一部分（给水不够），
                         * 差额热量原来直接消失：实测一次侧 4703 MW、二次侧约 3820 MW，
                         * 差 880 MW。现在先问二回路"这 tick 最多能吸收多少"，按较小值抽汽，
                         * 多出来的蒸汽留在汽包里 → 汽包压力上升 → 下一 tick 抽汽自然减小。 */
                        double capKg = hx.maxCondensableKg(0.05);
                        if (kg > capKg) {
                            kg = Math.max(0.0, capKg);
                        }
                        hx.applyActualDraw(kg, 0.05);
                        drum.updateSteamOutflow(kg, drum.getSteamTemperature());
                        drum.updateWaterInflow(hx.condensateOutKgs * 0.05, drum.getSteamTemperature());
                        hx.updateSecondary(0.05, separators.get(i).pressureMPa);
                        separators.get(i).update(0.05, hx.steamOutKgs, hx.secondarySteamTC);
                        separatorSources[i].refresh();
                        // 二次侧水量守恒：分离器甩出的水 + 换热器里没蒸发的给水
                        // 都回收到除氧器。旧实现只回收了分离器的水，而换热器管侧
                        // 在"传热跟不上给水"时会把多余的水存在管侧（上限 80 t），
                        // 于是给水进得去、出不来 —— 表现为"水并没有进入气分"。
                        if (i < dearators.size()) {
                            dearators.get(i).updateWaterInflow(
                                    separators.get(i).outWaterKgs * 0.05, separators.get(i).outTempC);
                            if (hx.secondaryDrainKgs > 0.0) {
                                dearators.get(i).updateWaterInflow(
                                        hx.secondaryDrainKgs * 0.05, hx.getWaterTemperature());
                            }
                        }
                    }
                    
                    autoControl.update();
                    //end of components to update
                    updateCount++;
                    long end = System.nanoTime();
                    int timestep = (int)(end - start);
                    deltaTimeStepLength = (50000000 - timestep);
                    int deltaMillis = deltaTimeStepLength / 1000000;
                    int deltaNanos = deltaTimeStepLength - deltaMillis * 1000000;
                    if (deltaTimeStepLength > 0) {
                        updating = false;
                        Thread.sleep(deltaMillis, deltaNanos);
                        updating = true;
                        timeStepLengthCumulative += deltaTimeStepLength;
                    }
                    timeStepLengthCumulative += timestep;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                new ErrorWindow("A fatal error occured", ExceptionUtils.getStackTrace(e), false).setVisible(true);
            }
        });
        updateThread.start();
    }

    private void printTimer() {
        final long timeToSleep[] = {900};
        printThread = new Thread(() -> {
            try {
                while(run) {
                    Thread.sleep(timeToSleep[0]);
                    while (updateCount < 20 ) {
                        Thread.sleep(10);
                    }
                    if (timeStepLengthCumulative / updateCount > 50000000) {
                        System.out.println("WARNING: Average timeStep = " + timeStepLengthCumulative / updateCount);
                    }
                    //System.out.println("Average time step: " + timeStepLengthCumulative / updateCount + " " + updateCount); //used sometimes for debugging
                    if (updateCount > 20) {
                        if (timeToSleep[0] > 500) {
                            timeToSleep[0] -= 50;
                        }
                        System.out.println("WARNING: Timesteps = " + updateCount + ". UI Wait time will be " + timeToSleep[0]);
                    }
                    updateCount = 0;
                    timeStepLengthCumulative = 0;
                    autoControl.updateSimulationTime();
                    ui.update();
                    while (simPaused) {
                        Thread.sleep(100);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        printThread.start();
    }
    
    public static void endSimulation() {
        try {
            for (int i = 0; i < UI.uiThreads.size(); i++) {
                UI.uiThreads.get(i).interrupt();
            }
            UI.uiThreads.clear();
            ui.setVisible(false);
            updateThread.interrupt();
            printThread.interrupt();
            run = false;
            ui.dispose();
            Loader.soundProvider.stopAll();
        } catch (Exception e) {
            //void
        }
    }
    
    public static void setPaused(boolean paused) {
        simPaused = paused;
    }
    
    public static boolean isPaused() {
        return simPaused;
    }
    
    public static boolean isRunning() {
        return run;
    }
    
    public static void save(File file) {
        boolean wasPaused = isPaused();
        try {
            FileOutputStream fout = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            setPaused(true);
            while(updating) {
                Thread.sleep(5);
            }
            SaveGame game = new SaveGame();
            game.plant = PlantState.capture();
            game.nativeCoreState = core != null ? core.captureNativeState() : null;
            game.coreInputs = core != null ? core.captureInputs() : null;
            if (core != null) {
                System.out.println("[Save] 保存堆芯: " + core.getThermalPower()
                        + " MW, 棒位=" + core.getRodInsertion()
                        + ", 氙=" + core.getXenonWorthPcm() + " pcm");
            }
            if (game.nativeCoreState == null) {
                System.out.println("[Save] 警告：当前物理内核不支持状态导出，堆芯将无法恢复");
            }
            oos.writeObject(game);
            setPaused(wasPaused);
            oos.flush();
            fout.flush();
            oos.close();
            fout.close();
        } catch (Exception e) {
            new ErrorWindow("Error Saving IC", ExceptionUtils.getStackTrace(e), true).setVisible(true);
            setPaused(wasPaused);
        }
    }
    
    public static void load(File file) {
        if (Loader.loader != null) {
            Loader.loader.setLoading(false);
        }
        try (
         FileInputStream fin = new FileInputStream(file);
         ObjectInputStream ois = new ObjectInputStream(fin);          
        ) {
            Object loaded = ois.readObject();
            if (!(loaded instanceof SaveGame)) {
                if (loaded instanceof List) {
                    throw new java.io.IOException(
                            "旧版存档（无版本号，stateArray 位置索引）无法可靠恢复；请用当前版本新开一局并重新保存");
                }
                throw new java.io.IOException("无法识别的存档格式："
                        + (loaded == null ? "null" : loaded.getClass().getName()));
            }
            SaveGame game = (SaveGame) loaded;
            if (game.version > SaveGame.CURRENT_VERSION) {
                throw new java.io.IOException("存档版本 " + game.version
                        + " 高于当前程序支持的 " + SaveGame.CURRENT_VERSION
                        + "，请升级模拟器后再读取");
            }
            if (game.version != SaveGame.CURRENT_VERSION || game.plant == null) {
                throw new java.io.IOException("旧版存档（v" + game.version
                        + "）使用已废弃的位置索引格式，无法可靠恢复；请用当前版本新开一局并重新保存");
            }
            ois.close();
            fin.close();
            endSimulation();
            Loader.simulation = new NPPSim(game.plant, game);
        } catch (Exception e) {
            e.printStackTrace();
            new ErrorWindow("Error Loading IC", ExceptionUtils.getStackTrace(e), true).setVisible(true);
        }
    }
}
