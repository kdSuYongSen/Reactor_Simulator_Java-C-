import com.rbmk.real.RbmkCore;

/** 轴向功率形状随时间演化 */
public class Probe3 {
    public static void main(String[] a) {
        RbmkCore c = new RbmkCore();
        c.reset();
        c.setRegulatorEnabled(false);
        c.setRodPosition(0.75);
        c.setFlowFraction(1.0);
        c.setOfflineFlow(0.0);
        int[] marks = {0, 20, 200, 1200};
        int prev = 0;
        for (int m : marks) {
            for (int i = 0; i < m - prev; i++) c.step(0.05);
            prev = m;
            c.refresh();
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 8; j++)
                sb.append(String.format(" n%d=%.3f(%.0fMW,%.0fC)", j, c.nodeN(j), c.nodePowerMW(j), c.nodeTf(j)));
            System.out.printf("t=%5.1fs P=%.0fMW rho=%+.0f%s%n", m * 0.05, c.powerMW(), c.reactivityPcm(), sb);
        }
        c.close();
    }
}
