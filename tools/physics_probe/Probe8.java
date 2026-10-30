import com.rbmk.real.RbmkCore;

/** 冷停堆态下的棒位-反应性曲线（可用总反应性） */
public class Probe8 {
    public static void main(String[] a) {
        RbmkCore c = new RbmkCore();
        c.resetToShutdown();
        c.setFlowFraction(1.0);
        c.setOfflineFlow(0.0);
        double best = -1e9, bestPos = 0;
        for (double p = 0.0; p <= 1.0001; p += 0.02) {
            c.setRodPosition(Math.min(1.0, p));
            c.refresh();
            if (c.reactivityPcm() > best) { best = c.reactivityPcm(); bestPos = p; }
        }
        c.setRodPosition(bestPos); c.refresh();
        System.out.printf("冷态最大可用反应性 %+.0f pcm @ 棒位 %.2f (%.2f beta)%n",
                best, bestPos, best / 650.0);
        System.out.println("棒位  rho(pcm)");
        for (double p = 0.5; p <= 1.0001; p += 0.05) {
            c.setRodPosition(Math.min(1.0, p)); c.refresh();
            System.out.printf(" %.2f  %+8.0f%n", p, c.reactivityPcm());
        }
        c.close();
    }
}
