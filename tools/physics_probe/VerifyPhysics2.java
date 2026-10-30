import com.rbmk.real.RbmkCore;

/** 独立物理核验 v2：所有量都用状态扰动/逐步积分反算，不复用引擎自报探针。 */
public class VerifyPhysics2 {

    static final double CP = 5500.0, HSAT = 5500.0 * (284.0 - 270.0), HFG = 1.509e6, DTB = 40.0;
    static final double BETA = 0.00650;

    static double h(double T) {
        return T <= 284.0 ? CP * (T - 270.0) : HSAT + (T - 284.0) / DTB * HFG;
    }

    static RbmkCore fresh() {
        RbmkCore c = new RbmkCore();
        c.reset();
        c.setRegulatorEnabled(false);
        c.setFlowFraction(1.0);
        c.setOfflineFlow(0.0);
        c.refresh();
        return c;
    }

    static void head(String s) { System.out.println("\n==== " + s + " ===="); }

    static double meanVoid(RbmkCore c) {
        double s = 0;
        for (int j = 0; j < 8; j++) s += c.nodeVoid(j);
        return s / 8;
    }

    static double meanT(RbmkCore c, int which) {
        double s = 0;
        for (int j = 0; j < 8; j++) {
            switch (which) {
                case 0: s += c.nodeTf(j); break;
                case 1: s += c.nodeTc(j); break;
                case 2: s += c.nodeTcool(j); break;
                default: s += c.nodeTmod(j);
            }
        }
        return s / 8;
    }

    static double run(RbmkCore c, double seconds) {
        int n = (int) Math.round(seconds / 0.05);
        for (int i = 0; i < n; i++) c.step(0.05);
        c.refresh();
        return c.powerMW();
    }

    public static void main(String[] args) {
        String only = args.length > 0 ? args[0] : "all";

        if (only.equals("all") || only.equals("C")) {
            head("C. 能量守恒（焓口径，不用 cp×ΔT）");
            RbmkCore c = fresh();
            double q = 12000.0 * (h(c.outletTemp()) - 0.0);
            System.out.printf(" 入口焓=0(270C)  出口焓=%.0f kJ/kg -> 带走 %.0f MW ; 堆芯 %.0f MW ; 偏差 %+.2f%%%n",
                    h(c.outletTemp()) / 1000, q / 1e6, c.powerMW(), (q / 1e6 - c.powerMW()) / c.powerMW() * 100);
            System.out.printf(" 出口干度 x=%.3f（真实 RBMK-1500 约 0.14~0.20）%n",
                    (h(c.outletTemp()) - HSAT) / HFG);
            // 瞬态：功率阶跃后，检查 Σ节点功率 与 焓升 是否仍然闭合
            c.setRodPosition(c.rodPosition() + 0.02);
            run(c, 5.0);
            double q2 = 12000.0 * (h(c.outletTemp()) - 0.0);
            System.out.printf(" 提棒 5 s 后: P=%.0f MW, 流量焓升=%.0f MW（差 %+.1f%%，含堆芯储热）%n",
                    c.powerMW(), q2 / 1e6, (q2 / 1e6 - c.powerMW()) / c.powerMW() * 100);
            c.close();
        }

        if (only.equals("all") || only.equals("F")) {
            head("F. 周期表 vs 实测 e 折时间（先提到临界，再 +30 pcm）");
            RbmkCore c = fresh();
            // 找到临界棒位附近的 rho 曲线
            double base = 0.75;
            c.setRodPosition(base);
            for (int i = 0; i < 20; i++) c.step(0.05);
            c.refresh();
            double nStart = c.avgNodeN(), rhoStart = c.reactivityPcm();
            double n1 = c.avgNodeN();
            // 小阶跃 +25 pcm（棒位微调）
            double rhoBefore = c.reactivityPcm();
            double dpos = 0.0010;
            c.setRodPosition(base + dpos);
            c.refresh();
            double rhoAfter = c.reactivityPcm();
            double perStep = c.period();
            System.out.printf(" 棒位 %.4f -> %.4f : rho %+.1f -> %+.1f pcm (Δ%+.1f), 引擎 period=%.2f s%n",
                    base, base + dpos, rhoBefore, rhoAfter, rhoAfter - rhoBefore, perStep);
            double[] times = {2, 5, 10};
            double prevT = 0;
            for (double t : times) {
                run(c, t - prevT);
                prevT = t;
                double n = c.avgNodeN();
                System.out.printf("  t=%4.0fs  n=%.6f  ln(n/n0)/t = %.4f /s -> T_eff=%.1f s   (引擎 period=%.1f s, rho=%+.0f)%n",
                        t, n, Math.log(n / nStart) / t, t / Math.log(n / nStart), c.period(), c.reactivityPcm());
            }
            c.close();
        }

        if (only.equals("all") || only.equals("I")) {
            head("I. 棒价值曲线（静态，每点先跑到准稳态）");
            RbmkCore c = fresh();
            System.out.println(" pos     rho(pcm)    rod[pcm]  node0棒  node7棒");
            for (double pos = 1.0; pos >= -0.001; pos -= 0.05) {
                double pp = Math.max(0, Math.min(1, pos));
                c.setRodPosition(pp);
                for (int i = 0; i < 400; i++) c.step(0.05);   // 20 s 让温度跟上
                c.refresh();
                System.out.printf(" %.2f  %+9.0f  %+9.0f  %+8.0f  %+8.0f%n",
                        pp, c.reactivityPcm(), c.avgRod(),
                        c.probeRodWorthPcm(pp, 0), c.probeRodWorthPcm(pp, 7));
            }
            c.close();
        }

        if (only.equals("all") || only.equals("J")) {
            head("J. 压力 / 入口焓 是否真的进了引擎（P1-2 修复验证）");
            RbmkCore c = fresh();
            double rho0 = c.reactivityPcm();
            double v0 = meanVoid(c);
            c.setSaturationTemp(270.0);      // 相当于压力升高（6.9 -> ~5.5 MPa）
            for (int i = 0; i < 400; i++) c.step(0.05);
            c.refresh();
            System.out.printf(" Tsat 284->270C: rho %+.0f -> %+.0f pcm, void %.4f -> %.4f, P=%.0f MW%n",
                    rho0, c.reactivityPcm(), v0, meanVoid(c), c.powerMW());
            RbmkCore d = fresh();
            d.setInletEnthalpy(-55.0e3);    // 入口 260C（给水温度下降 10 K）
            for (int i = 0; i < 400; i++) d.step(0.05);
            d.refresh();
            System.out.printf(" 入口焓 -55 kJ/kg(260C): rho=%+.0f pcm void=%.4f P=%.0f MW (Tin=%.1f)%n",
                    d.reactivityPcm(), meanVoid(d), d.powerMW(), d.inletTemp());
            c.close();
            d.close();
        }

        if (only.equals("all") || only.equals("K")) {
            head("K. 功率系数检验：不同功率水平下的总反应性（应随功率上升而下降）");
            RbmkCore c = fresh();
            for (double p : new double[]{0.5, 1.0, 1.2, 1.5}) {
                RbmkCore d = fresh();
                // 用 trim 是固定的，改功率只能靠棒位：这里直接改 n 状态做静态检查
                double[] s = d.saveState();
                double[] q = s.clone();
                for (int j = 0; j < 8; j++) q[j] *= p;   // 只改中子密度
                d.restoreState(q);
                d.refresh();
                System.out.printf(" n x%.1f : rho=%+.0f pcm  (截距不变，仅看 n 对 rho 的影响)%n",
                        p, d.reactivityPcm());
                d.close();
            }
            c.close();
        }

        if (only.equals("all") || only.equals("H")) {
            head("H. 氙瞬态（额定 -> SCRAM，dt=1 s，12 h）");
            RbmkCore x = fresh();
            double[] tpk = new double[1];
            System.out.printf(" 引擎闭式解: 峰值 %.0f pcm @ %.2f h%n", x.probeXenonPeakPcm(tpk), tpk[0] / 3600);
            x.scram();
            double minXe = 0, tMin = 0;
            for (int i = 0; i < 12 * 3600; i++) {
                x.step(1.0);
                if (i % 60 == 0) {
                    x.refresh();
                    double w = x.avgXenon();
                    if (w < minXe) { minXe = w; tMin = x.time(); }
                }
            }
            x.refresh();
            System.out.printf(" 12 h 实测最负 %.0f pcm @ %.2f h（相对停堆时刻）%n", minXe, tMin / 3600);
            x.close();
        }

        if (only.equals("all") || only.equals("L")) {
            head("L. 慢速降流 100% -> 85%（真实机组可承受量级）");
            RbmkCore c = fresh();
            for (int k = 1; k <= 6; k++) {
                c.setFlowFraction(1.0 - 0.025 * k);
                run(c, 10.0);
                System.out.printf(" 流量 %.0f%% : P=%7.0f MW  rho=%+6.0f  void=%.3f  Tf=%.0f  Tout=%.1f%n",
                        (1.0 - 0.025 * k) * 100, c.powerMW(), c.reactivityPcm(), meanVoid(c),
                        meanT(c, 0), c.outletTemp());
            }
            c.close();
        }
    }
}
