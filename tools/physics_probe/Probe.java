import com.rbmk.real.RbmkCore;

/** 直接对随包发布的 rbmk_core_x64.dll 做物理行为探针（不改动任何游戏文件） */
public class Probe {

    static RbmkCore c = new RbmkCore();

    static void p(String fmt, Object... a) { System.out.printf(fmt + "%n", a); }

    public static void main(String[] args) {
        c.reset();   // 额定稳态
        c.refresh();
        p("== 额定态 ==");
        p("P = %.1f MW   rho = %.1f pcm   k = %.5f   period = %.3g s", c.powerMW(), c.reactivityPcm(), c.kEff(), c.period());
        for (int j = 0; j < 8; j++)
            p(" node%d n=%.4f void=%.3f Tf=%.1f Tc=%.1f Tcool=%.1f Tmod=%.1f P=%.1fMW rho=%+.0f",
                    j, c.nodeN(j), c.nodeVoid(j), c.nodeTf(j), c.nodeTc(j), c.nodeTcool(j), c.nodeTmod(j),
                    c.nodePowerMW(j), c.nodeRhoPcm(j));
        p("Tin=%.1f Tout=%.1f decay=%.4f flow=%.0f trim=%+.1f pcm",
                c.inletTemp(), c.outletTemp(), c.decayFraction(), c.flowKgS(), c.trimPcm());

        p("%n== 探针 ==");
        p("空泡系数 %.2f pcm/%%void ; 满空泡插入 %.0f pcm (%.2f beta)",
                c.probeVoidCoeffPcm(), c.probeFullVoidPcm(), c.probeFullVoidPcm() / 650.0);
        double[] tpk = new double[1];
        p("氙峰值 %.0f pcm @ %.0f s (%.1f h)", c.probeXenonPeakPcm(tpk), tpk[0], tpk[0] / 3600.0);

        p("%n== 棒位扫描（静态反应性） ==");
        p(" pos    rho(pcm)   period(s)   node0 rod   node7 rod");
        for (double pos = 1.0001; pos >= -0.001; pos -= 0.05) {
            double pp = Math.max(0.0, Math.min(1.0, pos));
            c.setRodPosition(pp);
            c.refresh();
            p(" %.2f  %+9.0f   %10.3g   %+9.0f   %+9.0f",
                    pp, c.reactivityPcm(), c.period(), c.rbRod(0) * 0 + c.probeRodWorthPcm(pp, 0), c.probeRodWorthPcm(pp, 7));
        }

        p("%n== 周期显示检查（提棒到临界附近） ==");
        for (double pos = 0.90; pos <= 1.0001; pos += 0.02) {
            c.setRodPosition(Math.min(1.0, pos));
            c.refresh();
            p(" pos=%.2f  rho=%+.0f pcm  period=%.4g s", pos, c.reactivityPcm(), c.period());
        }

        p("%n== 额定态 600 s 漂移（无操作） ==");
        c.reset(); c.setRegulatorEnabled(false);
        c.setRodPosition(0.75); c.setFlowFraction(1.0); c.setOfflineFlow(0.0);
        for (int i = 0; i < 12000; i++) c.step(0.05);
        c.refresh();
        p(" P=%.1f MW rho=%.1f pcm Tout=%.1f Tf0=%.1f", c.powerMW(), c.reactivityPcm(), c.outletTemp(), c.nodeTf(0));

        p("%n== 离线冷却：400 kg/s（总）对额定态的冷却效果 ==");
        c.reset(); c.setRegulatorEnabled(false); c.setRodPosition(0.75); c.setFlowFraction(1.0);
        c.setOfflineFlow(400.0);
        double[] ts = {10, 60, 300};
        double tPrev = 0;
        for (double t : ts) {
            for (int i = 0; i < (int) ((t - tPrev) / 0.05); i++) c.step(0.05);
            c.refresh();
            p(" t=%.0fs  P=%.1f MW  Tcool0=%.1f  Tout=%.1f  void_top=%.3f  rho=%+.0f",
                    t, c.powerMW(), c.nodeTcool(0), c.outletTemp(), c.nodeVoid(7), c.reactivityPcm());
            tPrev = t;
        }

        p("%n== SCRAM 20 s（自额定 4800 MW） ==");
        c.reset(); c.setRegulatorEnabled(false); c.setRodPosition(0.75); c.setFlowFraction(1.0); c.setOfflineFlow(0.0);
        c.scram();
        for (int i = 0; i < 400; i++) c.step(0.05);
        c.refresh();
        p(" P=%.1f MW  rho=%.0f pcm  rod=%.3f  decay=%.4f  Tf0=%.0f", c.powerMW(), c.reactivityPcm(), c.rodPosition(), c.decayFraction(), c.nodeTf(0));

        p("%n== 瞬发临界附近：rho 与功率行为 ==");
        c.reset(); c.setRegulatorEnabled(false); c.setFlowFraction(1.0); c.setOfflineFlow(0.0);
        for (double pos = 0.99; pos <= 1.0001; pos += 0.002) {
            c.setRodPosition(pos); c.refresh();
            if (c.reactivityPcm() > 300) { p(" pos=%.3f rho=%+.0f pcm period=%.3f s", pos, c.reactivityPcm(), c.period()); }
        }
        c.close();
    }
}
