import com.rbmk.real.RbmkCore;

/** 冷停堆 → 慢速提棒到 0.50，观察能否稳定在额定附近 */
public class Probe14 {
    public static void main(String[] a) {
        RbmkCore c = new RbmkCore();
        c.resetToShutdown();
        c.setRegulatorEnabled(false);
        c.setFlowFraction(1.0);
        c.setOfflineFlow(0.0);

        c.setRodPosition(0.0); c.refresh();
        System.out.printf("全插棒冷态 rho=%+.0f pcm%n", c.reactivityPcm());
        c.setRodPosition(1.0); c.refresh();
        System.out.printf("全提棒冷态 rho=%+.0f pcm%n", c.reactivityPcm());

        double p = 0.30;
        c.setRodPosition(p);
        for (int i = 0; i <= 36000; i++) {          // 30 min
            if (i % 1200 == 0) {
                c.refresh();
                StringBuilder sb=new StringBuilder(); for(int j=0;j<8;j++) sb.append(String.format(" %.2f", c.nodeN(j)));
                System.out.printf("t=%5.0fs p=%.3f rho=%+6.0f P=%8.1fMW n=[%s]%n",
                        i * 0.05, p, c.reactivityPcm(), c.powerMW(), sb);
            }
            c.step(0.05);
            // 0.002/s 慢提棒，最多提到 0.50
            if (i < 15000 && p < 0.50) { p = Math.min(0.50, p + 0.002 * 0.05); c.setRodPosition(p); }
        }
        c.close();
    }
}
