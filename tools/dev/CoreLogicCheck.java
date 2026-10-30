package com.darwish.nppsim;

/**
 * 反应堆核心逻辑自检（不依赖全厂，直接驱动 C++ 物理引擎）。
 *
 * 检查项：
 *  ① 停堆初值：功率 0、棒全插、氙碘归零、周期 ∞、反应性深度负；
 *  ② 棒价值曲线：从全插逐步抽出，反应性单调上升，找到临界棒位；
 *  ③ 临界点附近加小阶跃：功率按周期增长（正反应性）／衰减（负反应性）；
 *  ④ 带功率运行后氙毒增长；
 *  ⑤ 数值健康（无 NaN、空泡份额在范围内、裂变功率+衰变热≈总功率）。
 *
 * 用法：java -cp "…jar-with-dependencies;target/verify" com.darwish.nppsim.CoreLogicCheck
 */
public class CoreLogicCheck {

    static int fails = 0;

    static void check(String what, boolean ok, String detail) {
        if (!ok) {
            fails++;
        }
        System.out.printf("  [%s] %-42s %s%n", ok ? "OK" : "FAIL", what, detail);
    }

    static boolean ok(double v) {
        return !Double.isNaN(v) && !Double.isInfinite(v);
    }

    static String fmt(double v) {
        if (Double.isNaN(v)) {
            return "NaN";
        }
        if (Double.isInfinite(v)) {
            return v > 0 ? "∞" : "-∞";
        }
        if (Math.abs(v) >= 1e4 || (Math.abs(v) < 1e-3 && v != 0)) {
            return String.format("%.3e", v);
        }
        return String.format("%.3f", v);
    }

    /** 停在某个棒位、跑若干 tick，让反馈跟上 */
    static void settle(ReactorKinetics k, double insertion, int ticks) {
        k.rodInsertion = insertion;
        for (int i = 0; i < ticks; i++) {
            k.update(0.05);
        }
    }

    public static void main(String[] a) throws Exception {
        Loader.tables = new SteamTables();
        ReactorKinetics k = new ReactorKinetics();
        k.mdotTotal = 12000.0;
        k.fuelChannelCount = 1661;

        System.out.println("=========== ① 停堆初值 ===========");
        settle(k, 1.0, 200);
        check("功率 ≈ 0 MW", k.totalPowerMW < 1.0, fmt(k.totalPowerMW) + " MW");
        check("反应性深度负", k.reactivityAbs < -0.005, fmt(k.reactivityAbs * 1e5) + " pcm");
        check("周期 ∞（不增长）", k.periodSeconds > 1e5 || k.periodSeconds < 0,
                fmt(k.periodSeconds) + " s");
        check("中子数为源量程", k.neutronTotal < 1e6, fmt(k.neutronTotal));
        check("氙价值 ≈ 0", Math.abs(k.xenonWorthPcm) < 50, fmt(k.xenonWorthPcm) + " pcm");

        System.out.println("=========== ② 棒价值曲线（全插 → 全抽） ===========");
        double rhoFull = k.reactivityAbs;
        double prevRho = rhoFull;
        boolean mono = true;
        double critInsertion = Double.NaN;
        double bestDiff = 1e9;
        for (double ins = 0.98; ins >= 0.20; ins -= 0.04) {
            settle(k, ins, 60);                    // 3 s
            double rho = k.reactivityAbs;
            System.out.printf("    棒位%.2f（插入）  ρ=%8.1f pcm  P=%9.3f MW%n",
                    ins, rho * 1e5, k.totalPowerMW);
            if (rho < prevRho - 1e-4) {
                mono = false;
            }
            prevRho = rho;
            if (Math.abs(rho) < bestDiff) {
                bestDiff = Math.abs(rho);
                critInsertion = ins;
            }
        }
        check("抽出时反应性单调上升", mono, "全插 " + fmt(rhoFull * 1e5) + " pcm → 20% 棒位 "
                + fmt(k.reactivityAbs * 1e5) + " pcm");
        check("存在临界棒位（|ρ| < 500 pcm）", bestDiff < 0.005,
                "临界棒位≈" + String.format("%.2f", critInsertion) + "（ρ=" + fmt(bestDiff * 1e5) + " pcm）");

        System.out.println("=========== ③ 临界附近小阶跃 ===========");
        if (!Double.isNaN(critInsertion)) {
            // 回到临界，再抽出 2% 棒位（正反应性），看功率增长
            settle(k, critInsertion, 600);
            double p0 = Math.max(1e-6, k.totalPowerMW);
            System.out.printf("    临界棒位稳态：P=%.4f MW  ρ=%.1f pcm%n", p0, k.reactivityAbs * 1e5);
            settle(k, Math.max(0.0, critInsertion - 0.02), 1200);   // 60 s
            double p1 = k.totalPowerMW;
            check("抽出 2% 棒位后功率上升", p1 > p0 * 1.5,
                    fmt(p0) + " → " + fmt(p1) + " MW（ρ=" + fmt(k.reactivityAbs * 1e5) + " pcm，周期 "
                            + fmt(k.periodSeconds) + " s）");
            // 再插入 4%（负反应性），看功率回落
            double before = k.totalPowerMW;
            settle(k, Math.min(1.0, critInsertion + 0.04), 1200);
            check("插回棒后功率回落", k.totalPowerMW < before,
                    fmt(before) + " → " + fmt(k.totalPowerMW) + " MW（ρ=" + fmt(k.reactivityAbs * 1e5) + " pcm）");
        }

        System.out.println("=========== ④ 带功率运行 → 氙毒增长 ===========");
        if (!Double.isNaN(critInsertion)) {
            settle(k, Math.max(0.0, critInsertion - 0.01), 600);
            double xe0 = k.xenonWorthPcm;
            double pStart = k.totalPowerMW;
            double maxP = pStart;
            for (int i = 0; i < 36000; i++) {      // 1800 s（半小时）
                k.update(0.05);
                if (k.totalPowerMW > maxP) {
                    maxP = k.totalPowerMW;
                }
            }
            // 氙是小时量级：半小时内价值只会长到几 pcm，判据看"单调变大 + 符号为负"
            check("氙价值增大（更负）", k.xenonWorthPcm < xe0 && Math.abs(k.xenonWorthPcm) > Math.abs(xe0) * 2,
                    fmt(xe0) + " → " + fmt(k.xenonWorthPcm) + " pcm（功率 " + fmt(pStart) + " → 峰值 "
                            + fmt(maxP) + " → " + fmt(k.totalPowerMW) + " MW）");
            check("碘/氙读数健康", ok(k.i135AvgForUI) && ok(k.xe135AvgForUI),
                    "I=" + fmt(k.i135AvgForUI) + "  Xe=" + fmt(k.xe135AvgForUI));
        }

        System.out.println("=========== ⑤ 数值健康 ===========");
        boolean voidOk = true;
        for (double v : k.nodeVoid) {
            if (!ok(v) || v < -1e-6 || v > 1.5) {
                voidOk = false;
            }
        }
        check("功率/反应性/周期无 NaN",
                ok(k.totalPowerMW) && ok(k.reactivityAbs) && ok(k.periodSeconds),
                fmt(k.totalPowerMW) + " MW / " + fmt(k.reactivityAbs) + " / " + fmt(k.periodSeconds) + " s");
        check("空泡份额在 0~1.5", voidOk, "节点数=" + k.nodeVoid.length);
        check("裂变功率 + 衰变热 ≈ 总功率",
                Math.abs(k.fissionPowerMW + k.decayHeatMW - k.totalPowerMW)
                        < Math.max(1.0, k.totalPowerMW * 0.02),
                fmt(k.fissionPowerMW) + " + " + fmt(k.decayHeatMW) + " = "
                        + fmt(k.fissionPowerMW + k.decayHeatMW) + " vs " + fmt(k.totalPowerMW));

        System.out.println(fails == 0 ? "CORE_LOGIC_OK" : "CORE_LOGIC_FAIL (" + fails + ")");
        System.exit(0);
    }
}
