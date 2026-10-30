import com.rbmk.real.RbmkCore;

/** 小阶跃稳定性：判断额定点附近的功率系数符号。 */
public class Stability {
    public static void main(String[] a) {
        for (double d : new double[]{+0.010, +0.005, -0.005}) {
            RbmkCore c = new RbmkCore();
            c.setRegulatorEnabled(false);
            c.reset();
            c.setFlowFraction(1.0);
            c.setRodPosition(0.75 + d);
            c.refresh();
            System.out.printf("%n棒位 0.750 -> %.3f  (Δrho=%+.0f pcm)%n", 0.75 + d, c.reactivityPcm());
            for (int k = 1; k <= 10; k++) {
                for (int i = 0; i < 1200; i++) c.step(0.05);   // 60 s
                c.refresh();
                System.out.printf("  t=%4ds  P=%8.1f MW (%5.1f%%)  rho=%+7.1f  Tf=%6.1f  void=%.3f%n",
                        k * 60, c.powerMW(), c.powerMW() / 48.0, c.reactivityPcm(),
                        c.nodeTf(7), c.nodeVoid(7));
            }
            c.close();
        }
    }
}
