package com.darwish.nppsim;

import java.io.Serializable;

/**
 * 带版本号的存档容器（v2）。
 *
 * <p>v2 不再保存 {@code ArrayList<Serializable> stateArray}，而是保存
 * {@link PlantState}（按字段/列表组织的完整设备对象图）。旧版的
 * {@code state.get(0..205)} 位置索引会随组件增删而错位（实测读档时
 * {@code state.get(205)} 已经是 DwTankDrainValve，导致 ClassCastException），
 * 因此 v2 直接按根字段恢复，设备之间的引用由 Java 序列化保留。
 *
 * <ul>
 *   <li>{@link #plant}：汽包/堆芯/泵/阀/母管/汽轮机/除氧器/PCS/二回路等；</li>
 *   <li>{@link #nativeCoreState}：C++ 内核 125 维完整状态（功率/氙/温度/棒位/历史）。</li>
 * </ul>
 *
 * <p>版本策略：v0 = 裸 List；v1 = 旧的 stateArray + 快照；v2 = PlantState。
 * 读到 v0/v1 会明确拒绝并提示“旧格式无法可靠恢复”，不再尝试按位置索引硬读。
 */
public final class SaveGame implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int CURRENT_VERSION = 2;

    /** 存档格式版本；旧版裸 List 视为 0，旧 SaveGame 为 1。 */
    int version = CURRENT_VERSION;

    /** v2：按字段组织的完整设备状态。 */
    PlantState plant;

    /** C++ 内核完整状态；null = 旧档/安全模式（无原生核心）。 */
    double[] nativeCoreState;

    /** 内核输入（棒位/流量/压力/入口焓/离线流量）。
     *  读档后第一个 tick 的通道流量可能还是 0，必须用这份输入而不是默认值，
     *  否则会把刚恢复的堆芯用 0 流量/默认棒位再推一次。 */
    CoreInputs coreInputs;

    /** 保存时间戳（诊断用，不参与恢复逻辑）。 */
    long savedAt = System.currentTimeMillis();

    static final class CoreInputs implements Serializable {
        private static final long serialVersionUID = 1L;

        double rodInsertion;
        double pressureMPa;
        double mdotTotal;
        double hInHeader;
        double mdotOffline;
        double hOfflineIn;
        int fuelChannelCount;
    }
}
