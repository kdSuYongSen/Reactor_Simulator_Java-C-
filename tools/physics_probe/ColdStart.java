import com.rbmk.real.RbmkCore;

/** 冷启动：停堆初值 -> 提棒，看能否自然升到额定。 */
public class ColdStart {
    public static void main(String[] a) {
        RbmkCore c = new RbmkCore();
        c.setRegulatorEnabled(false);
        c.resetToShutdown();
        c.setFlowFraction(1.0);
        c.setOfflineFlow(0.0);
        c.refresh();
        System.out.printf("初始: P=%.4f MW rho=%+.0f pcm rod=%.3f%n", c.powerMW(), c.reactivityPcm(), c.rodPosition());

        double pos = 0.0;
        c.setRodPosition(0.0);
        // 以 0.002/s 连续提棒到 0.46，共 230 s
        for (int i = 0; i < 4600; i++) {
            pos = Math.min(0.46, pos + 0.002 * 0.05);
            c.setRodPosition(pos);
            c.step(0.05);
        }
        c.refresh();
        System.out.printf("提棒到 %.3f: P=%.2f MW rho=%+.0f Tf=%.0f%n", pos, c.powerMW(), c.reactivityPcm(), c.nodeTf(7));

        for (int k = 1; k <= 20; k++) {
            for (int i = 0; i < 1200; i++) c.step(0.05);   // 60 s
            c.refresh();
            if (k % 2 == 0)
                System.out.printf(" t=%4ds P=%8.1f MW rho=%+7.1f Tf=%6.0f void_top=%.3f%n",
                        k * 60, c.powerMW(), c.reactivityPcm(), c.nodeTf(7), c.nodeVoid(7));
        }
        c.close();
    }
}
