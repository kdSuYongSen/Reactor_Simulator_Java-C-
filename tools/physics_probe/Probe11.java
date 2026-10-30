import com.rbmk.real.RbmkCore;

/** 逐节点冷却剂温度/空泡，看首步到底哪来的 +40 pcm */
public class Probe11 {
    public static void main(String[] a) {
        RbmkCore c = new RbmkCore();
        c.reset();
        c.setRegulatorEnabled(false);
        c.setRodPosition(0.75);
        c.setFlowFraction(1.0);
        c.setOfflineFlow(0.0);
        for (int i = 0; i <= 4; i++) {
            c.refresh();
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 8; j++) sb.append(String.format(" %.2f/%.3f", c.nodeTcool(j), c.nodeVoid(j)));
            System.out.printf("step%d rho=%+.1f voidTerm=%+.1f dop=%+.1f n7=%.5f |%s%n",
                    i, c.reactivityPcm(), c.rbVoid(7), c.rbDoppler(7), c.nodeN(7), sb);
            c.step(0.05);
        }
        c.close();
    }
}
