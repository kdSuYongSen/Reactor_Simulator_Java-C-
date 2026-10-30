package com.rbmk.real;

/**
 * C++ 引擎边界条件探针。
 *
 * 1) 在额定流量下把棒位提到 0.50，跑到 4800 MW 附近，读引擎自己认为的
 *    入口温度 / 出口温度 / 流量 / 各节点冷却剂温度；
 * 2) 把流量降到 15%（主泵跳闸量级），看功率与空泡反馈是否合理；
 * 3) 证明引擎没有"压力/入口温度"输入口：同样的棒位和流量，Java 侧汽包压力
 *    从 6.9 降到 5.0 MPa，引擎输出不会变（这是本次审查的重点）。
 */
public class EngineProbe {

    static void dump(RbmkCore c, String tag) {
        double maxTf = 0, maxTc = 0;
        for (int j = 0; j < RbmkCore.NODES; j++) {
            maxTf = Math.max(maxTf, c.nodeTf(j));
            maxTc = Math.max(maxTc, c.nodeTcool(j));
        }
        System.out.printf("%-22s P=%7.1f MW rho=%+6.0f pcm 入口=%.1fC 出口=%.1fC 流量=%.0fkg/s "
                        + "Tf_max=%.0fC Tcool_max=%.0fC 周期=%.3g s%n",
                tag, c.powerMW(), c.reactivityPcm(), c.inletTemp(), c.outletTemp(),
                c.flowKgS(), maxTf, maxTc, c.period());
    }

    public static void main(String[] args) {
        RbmkCore c = new RbmkCore();
        c.resetToShutdown();
        c.setRegulatorEnabled(false);
        c.setFlowFraction(1.0);
        c.setOfflineFlow(0.0);

        // 冷停堆 → 0.30 起提，0.002/s 提到 0.47，再稳 25 min（与 Probe13 同一启动曲线）
        double p = 0.30;
        c.setRodPosition(p);
        for (int i = 0; i <= 48000; i++) {
            c.step(0.05);
            if (i < 8500 && p < 0.47) {
                p = Math.min(0.47, p + 0.002 * 0.05);
                c.setRodPosition(p);
            }
        }
        c.refresh();
        dump(c, "额定流量 1.0");

        // 流量降到 15%（≈2 台泵 40% 的默认线）
        c.setFlowFraction(0.15);
        for (int i = 0; i < 400; i++) {
            c.step(0.05);
        }
        c.refresh();
        dump(c, "流量 0.15（20s）");

        // 恢复
        c.setFlowFraction(1.0);
        for (int i = 0; i < 800; i++) {
            c.step(0.05);
        }
        c.refresh();
        dump(c, "恢复 1.0（40s）");

        System.out.println();
        System.out.println("引擎边界（代码事实）:");
        System.out.println("  setRodPosition / setRodTarget / setRodSpeed");
        System.out.println("  setFlowFraction / setOfflineFlow");
        System.out.println("  setPowerSetpoint / setRegulatorEnabled / scram");
        System.out.println("  → 没有 setPressure / setInletTemperature；压力与入口温度是 C++ 内定值");
        System.out.println("  （rbmk_params.h: T_INLET_NOM=270C, T_SAT=284C@6.9MPa, MDOT_NOM=12000kg/s）");
        c.close();
    }
}
