import com.rbmk.real.RbmkCore;

/** 静态功率系数：按 +1% 功率对应的温度/空泡变化同时扰动状态，测 rho 变化。 */
public class PowerCoeff {
    static final double CP = 5500, HFG = 1.509e6, DTB = 40, TSAT = 284, HSAT = 5500 * 14;

    static double h(double T) { return T <= TSAT ? CP * (T - 270) : HSAT + (T - TSAT) / DTB * HFG; }
    static double Tinv(double hv) { return hv <= HSAT ? 270 + hv / CP : TSAT + (hv - HSAT) * DTB / HFG; }

    public static void main(String[] a) {
        RbmkCore c = new RbmkCore();
        c.setRegulatorEnabled(false);
        c.reset();
        c.setFlowFraction(1.0);
        c.refresh();
        double[] s0 = c.saveState();
        double rho0 = c.reactivityPcm();

        System.out.println(" ΔP%     Δrho(pcm)   d rho/dP (pcm/%)");
        double prevP = 0, prevRho = 0;
        for (double p = 0.90; p <= 1.1001; p += 0.01) {
            double[] s = s0.clone();
            double dp = p - 1.0;
            for (int j = 0; j < 8; j++) {
                // 中子密度
                s[j] *= p;
                // 燃料温度：Tf = Tcool + P*(1/Hcm + 1/Hfc)
                s[8 + j] += dp * 600e6 * (1 / 5.22e6 + 1 / 2.40e6);
                // 冷却剂：节点平均焓 h_j=(j+0.5)*P/flow
                double dh = (j + 0.5) * 50e3 * dp;
                s[24 + j] = Tinv(h(s[24 + j]) + dh);
                // 包壳
                s[16 + j] += dp * 600e6 / 5.22e6;
            }
            c.restoreState(s);
            c.refresh();
            double rho = c.reactivityPcm();
            if (p > 0.9) {
                double slope = (rho - prevRho) / (p - prevP) / 100.0;   // 每 1%
                System.out.printf(" %+5.1f%%   %+8.1f    %+7.2f%n", (p - 1) * 100, rho, slope);
            }
            prevP = p; prevRho = rho;
        }
        c.restoreState(s0);
        c.close();

        System.out.println("\n小阶跃 +5 pcm（rod 0.7500 -> 0.7505），看是否收敛：");
        RbmkCore d = new RbmkCore();
        d.setRegulatorEnabled(false);
        d.reset();
        d.setFlowFraction(1.0);
        d.setRodPosition(0.7505);
        d.refresh();
        System.out.printf(" 初始 rho=%+.1f pcm%n", d.reactivityPcm());
        for (int k = 1; k <= 10; k++) {
            for (int i = 0; i < 2000; i++) d.step(0.05);
            d.refresh();
            System.out.printf("  t=%3ds  P=%7.1f MW  rho=%+7.1f%n", k * 100, d.powerMW(), d.reactivityPcm());
        }
        d.close();
    }
}
