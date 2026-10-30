import com.rbmk.real.RbmkCore;

/** 额定态 600 s 漂移的成分追踪 */
public class Probe2 {
    public static void main(String[] a) {
        RbmkCore c = new RbmkCore();
        c.reset();
        c.setRegulatorEnabled(false);
        c.setRodPosition(0.75);
        c.setFlowFraction(1.0);
        c.setOfflineFlow(0.0);
        c.refresh();
        System.out.printf("%6s %8s %8s %8s %8s %8s %8s %8s %8s%n",
                "t", "P(MW)", "rho", "dop", "void", "mod", "xe", "rod", "Tf0");
        for (int i = 0; i <= 12000; i++) {
            if (i % 1200 == 0) {
                c.refresh();
                double dop = 0, vo = 0, mo = 0, xe = 0, rod = 0;
                for (int j = 0; j < 8; j++) {
                    dop += c.rbDoppler(j); vo += c.rbVoid(j); mo += c.rbMod(j);
                    xe += c.rbXenon(j); rod += c.rbRod(j);
                }
                System.out.printf("%6.0f %8.1f %8.1f %8.1f %8.1f %8.1f %8.1f %8.1f %8.1f%n",
                        i * 0.05, c.powerMW(), c.reactivityPcm(), dop / 8, vo / 8, mo / 8, xe / 8, rod / 8, c.nodeTf(0));
            }
            c.step(0.05);
        }
        c.close();
    }
}
