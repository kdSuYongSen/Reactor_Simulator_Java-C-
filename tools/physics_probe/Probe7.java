import com.rbmk.real.RbmkCore;

/** 冷停堆 → 全提棒，看堆能到多少功率（轴向形状是否正常） */
public class Probe7 {
    public static void main(String[] a) {
        RbmkCore c = new RbmkCore();
        c.resetToShutdown();
        c.setRegulatorEnabled(false);
        c.setFlowFraction(1.0);
        c.setOfflineFlow(0.0);
        c.setRodPosition(1.0);
        for (int i = 0; i <= 36000; i++) {   // 30 min
            if (i % 6000 == 0) {
                c.refresh();
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < 8; j++) sb.append(String.format(" %.2f", c.nodeN(j)));
                System.out.printf("t=%5.0fs P=%7.1fMW rho=%+6.0f void7=%.2f n=[%s]%n",
                        i * 0.05, c.powerMW(), c.reactivityPcm(), c.nodeVoid(7), sb);
            }
            c.step(0.05);
        }
        c.close();
    }
}
