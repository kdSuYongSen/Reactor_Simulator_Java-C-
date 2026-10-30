package com.darwish.nppsim;

/**
 * 全厂物质总量账本（水 / 蒸汽分开）。
 *
 * <p>2026-10-27 新增。目的有两个：</p>
 * <ol>
 *   <li><b>把水与蒸汽分开看</b>：界面上的"水位"是水侧液位，"蒸汽量"是汽侧存量/流量，
 *       两者不能混成一个数字；</li>
 *   <li><b>证明总量守恒</b>：每 tick 汇总各容器的水/汽质量，运行中的漂移应与
 *       给水/排污/泄压的净交换量对应，而不是凭空增减。</li>
 * </ol>
 *
 * <p>口径说明：只统计**闭式主系统**里的工质（汽包、换热器管侧、除氧器、凝汽器热井、
 * 除盐水箱）。冷却水（循环水/河水）是开式回路，单独计，不混进来。</p>
 */
final class MassLedger {

    private MassLedger() {
    }

    /** 主系统水质量 kg */
    static double waterKg() {
        double w = 0;
        if (NPPSim.mcc != null) {
            w += safe(NPPSim.mcc.drum1.getWaterMass());
            w += safe(NPPSim.mcc.drum2.getWaterMass());
        }
        /* 用索引遍历而不是 for-each：这些列表由仿真线程维护（读档时会 clear/重建），
         * for-each 会抛 ConcurrentModificationException（界面刷新线程曾因此报错）。 */
        java.util.List<HeatExchanger> hxs = NPPSim.heatExchangers;
        if (hxs != null) {
            for (int i = 0; i < hxs.size(); i++) {
                HeatExchanger hx = hxs.get(i);
                if (hx != null) {
                    w += safe(hx.getWaterMass());
                }
            }
        }
        java.util.List<Dearator> ds = NPPSim.dearators;
        if (ds != null) {
            for (int i = 0; i < ds.size(); i++) {
                Dearator d = ds.get(i);
                if (d != null) {
                    w += safe(d.getWaterMass());
                }
            }
        }
        w += hotwellKg();
        if (NPPSim.pcs != null && NPPSim.pcs.demineralizedWaterTank != null) {
            w += safe(NPPSim.pcs.demineralizedWaterTank.getWaterMass());
        }
        return w;
    }

    /** 主系统蒸汽质量 kg */
    static double steamKg() {
        double s = 0;
        if (NPPSim.mcc != null) {
            s += safe(NPPSim.mcc.drum1.getSteamMass());
            s += safe(NPPSim.mcc.drum2.getSteamMass());
        }
        if (NPPSim.tg1 != null) {
            s += safe(NPPSim.tg1.condenser.getSteamMass());
        }
        if (NPPSim.tg2 != null) {
            s += safe(NPPSim.tg2.condenser.getSteamMass());
        }
        return s;
    }

    /** 8 台凝汽器热井里的凝结水 kg */
    static double hotwellKg() {
        double w = 0;
        if (NPPSim.tg1 != null && NPPSim.tg1.condenser != null) {
            w += safe(NPPSim.tg1.condenser.getHotwellMassKg());
        }
        if (NPPSim.tg2 != null && NPPSim.tg2.condenser != null) {
            w += safe(NPPSim.tg2.condenser.getHotwellMassKg());
        }
        return w;
    }

    static double totalKg() {
        return waterKg() + steamKg();
    }

    private static double safe(double v) {
        return Double.isFinite(v) ? v : 0.0;
    }
}
