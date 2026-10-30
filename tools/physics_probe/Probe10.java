import com.rbmk.real.RbmkCore;

/** 额定态起步前 10 步：确认是否数值瞬变 */
public class Probe10 {
    public static void main(String[] a) {
        RbmkCore c = new RbmkCore();
        c.reset();
        c.setRegulatorEnabled(false);
        c.setRodPosition(0.75);
        c.setFlowFraction(1.0);
        c.setOfflineFlow(0.0);
        for (int i = 0; i <= 10; i++) {
            c.refresh();
            System.out.printf("step %2d t=%.2f P=%.1f rho=%+.2f void=%+.1f dop=%+.1f Tf0=%.2f Tcool7=%.2f n7=%.5f%n",
                    i, c.time(), c.powerMW(), c.reactivityPcm(), c.avgVoid() * 1e5, c.avgDoppler(), c.nodeTf(0), c.nodeTcool(7), c.nodeN(7));
            c.step(0.05);
        }
        c.close();
    }
}
