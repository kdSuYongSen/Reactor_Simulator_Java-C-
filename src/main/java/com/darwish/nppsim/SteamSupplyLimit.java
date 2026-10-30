package com.darwish.nppsim;

/**
 * 蒸汽源可供量（kg / 本 tick，1 tick = 50 ms）。
 *
 * 由来：蒸汽阀门原先只按上下游压差算流量，源端只能在事后记账。于是
 * "阀门开多大就能流多少"，多出来的汽只能靠源端凭空变出来 —— 实测
 * 4 台除氧器合计凭空透传 3570 kg/s，母管也按需求而不是按供给分配。
 *
 * 实现本接口后，SteamValve 在发出流量前先问源端"你供得起吗"，
 * 供不起就按可供量限流。各源自行决定可供量：
 *   - Dearator / SteamCollectorHeader / Condenser / SeparatorDrum：自身蒸汽存量
 *   - SeparatorSteamSource：分离器本帧实际产汽
 *   - OneWaySteamHeader：各源可供量之和
 *
 * 刻意不继承 Connectable：改接口会牵动全部组件，而这个约束只对阀门有意义。
 */
interface SteamSupplyLimit {
    /** 本 tick 最多能送出的蒸汽质量（kg）。返回 Double.POSITIVE_INFINITY 表示不限（如排大气）。 */
    double availableSteamKg();
}
