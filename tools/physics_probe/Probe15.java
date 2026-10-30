import com.rbmk.real.RbmkCore;
public class Probe15 {
    public static void main(String[] a) {
        RbmkCore c = new RbmkCore();
        c.reset(); c.setRegulatorEnabled(false); c.setRodPosition(0.75); c.setFlowFraction(1.0); c.setOfflineFlow(0.0);
        c.scram();
        for (int i = 0; i < 400; i++) c.step(0.05);
        c.refresh();
        System.out.printf("SCRAM 20s: P=%.1f MW rho=%.0f pcm rod=%.3f decay=%.4f Tf0=%.0f%n",
                c.powerMW(), c.reactivityPcm(), c.rodPosition(), c.decayFraction(), c.nodeTf(0));
        c.close();
    }
}
