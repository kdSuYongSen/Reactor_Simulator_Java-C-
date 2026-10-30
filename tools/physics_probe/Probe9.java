import com.rbmk.real.RbmkCore;

/** 冷停堆 → 最佳棒位 0.58（最大可用反应性），看稳态功率 */
public class Probe9 {
    public static void main(String[] a) {
        RbmkCore c = new RbmkCore();
        c.resetToShutdown();
        c.setRegulatorEnabled(false);
        c.setFlowFraction(1.0);
        c.setOfflineFlow(0.0);
        c.setRodPosition(0.58);
        for (int i = 0; i <= 48000; i++) {   // 40 min
            if (i % 6000 == 0) {
                c.refresh();
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < 8; j++) sb.append(String.format(" %.2f", c.nodeN(j)));
                System.out.printf("t=%5.0fs P=%7.1fMW rho=%+6.0f Tf7=%.0f n=[%s]%n",
                        i * 0.05, c.powerMW(), c.reactivityPcm(), c.nodeTf(7), sb);
            }
            c.step(0.05);
        }
        c.close();
    }
}
