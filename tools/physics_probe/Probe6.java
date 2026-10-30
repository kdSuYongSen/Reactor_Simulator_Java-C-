import com.rbmk.real.RbmkCore;

/** 冷停堆态的逐节点反应性分布 */
public class Probe6 {
    public static void main(String[] a) {
        RbmkCore c = new RbmkCore();
        c.resetToShutdown();
        c.setFlowFraction(1.0);
        c.setOfflineFlow(0.0);
        c.setRodPosition(0.75);
        c.refresh();
        System.out.printf("冷停堆 + 棒0.75: P=%.1fMW rho=%+.0f trim=%+.0f%n", c.powerMW(), c.reactivityPcm(), c.trimPcm());
        System.out.printf("%2s %9s %8s %8s %8s %8s %8s %8s%n", "j", "rho(pcm)", "dop", "void", "mod", "xe", "rod", "trim");
        for (int j = 0; j < 8; j++)
            System.out.printf("%2d %9.0f %8.0f %8.0f %8.0f %8.0f %8.0f %8.0f%n", j,
                    c.nodeRhoPcm(j), c.rbDoppler(j), c.rbVoid(j), c.rbMod(j), c.rbXenon(j), c.rbRod(j), c.trimPcm());
        c.close();
    }
}
