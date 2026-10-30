package com.darwish.nppsim;

/**
 * 反馈灵敏度自检：固定棒位，改流量 / 入口焓，看反应性是否跟着变
 * （验证空泡反馈与多普勒/温度反馈确实起作用，不是"死"的）。
 *
 * 用法：java -cp "…jar-with-dependencies;target/verify" com.darwish.nppsim.CoreFeedbackCheck
 */
public class CoreFeedbackCheck {
    public static void main(String[] a) throws Exception {
        Loader.tables = new SteamTables();
        ReactorKinetics k = new ReactorKinetics();
        k.fuelChannelCount = 1661;
        k.mdotTotal = 12000;
        k.rodInsertion = 0.32;
        for (int i = 0; i < 400; i++) {
            k.update(0.05);
        }
        double rhoBase = k.reactivityAbs;
        System.out.printf("基准：流量 12000 kg/s  ρ=%.1f pcm  P=%.4f MW%n", rhoBase * 1e5, k.totalPowerMW);

        boolean flowSensitive = false;
        for (double f : new double[]{9000, 6000, 3000, 1200}) {
            k.mdotTotal = f;
            for (int i = 0; i < 400; i++) {
                k.update(0.05);
            }
            double d = (k.reactivityAbs - rhoBase) * 1e5;
            if (Math.abs(d) > 1.0) {
                flowSensitive = true;
            }
            System.out.printf("  流量 %5.0f kg/s → ρ=%.1f pcm（Δ=%+.1f）  空泡节点0=%.3f%n",
                    f, k.reactivityAbs * 1e5, d, k.nodeVoid[0]);
        }

        k.mdotTotal = 12000;
        for (int i = 0; i < 400; i++) {
            k.update(0.05);
        }
        double rhoFlowBack = k.reactivityAbs;
        boolean enthalpySensitive = false;
        for (double h : new double[]{50, 120, 200}) {     // kJ/kg，相对 270 °C 的焓偏置
            k.hInHeader = h;
            for (int i = 0; i < 400; i++) {
                k.update(0.05);
            }
            double d = (k.reactivityAbs - rhoFlowBack) * 1e5;
            if (Math.abs(d) > 1.0) {
                enthalpySensitive = true;
            }
            System.out.printf("  入口焓偏置 %5.0f kJ/kg → ρ=%.1f pcm（Δ=%+.1f）%n",
                    h, k.reactivityAbs * 1e5, d);
        }

        System.out.println(flowSensitive ? "  [OK] 流量→反应性 有反馈"
                : "  [FAIL] 流量对反应性无影响（空泡反馈可能没接上）");
        System.out.println(enthalpySensitive ? "  [OK] 入口焓→反应性 有反馈"
                : "  [FAIL] 入口焓对反应性无影响（温度反馈可能没接上）");
        System.out.println(flowSensitive && enthalpySensitive ? "CORE_FEEDBACK_OK" : "CORE_FEEDBACK_FAIL");
        System.exit(0);
    }
}
