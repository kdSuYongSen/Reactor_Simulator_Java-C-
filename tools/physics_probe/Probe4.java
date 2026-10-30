import com.rbmk.real.RbmkCore;

/** 时间步敏感性：同一初始态用不同 dt 推进 1 s */
public class Probe4 {
    static void run(double dt) {
        RbmkCore c = new RbmkCore();
        c.reset();
        c.setRegulatorEnabled(false);
        c.setRodPosition(0.75);
        c.setFlowFraction(1.0);
        c.setOfflineFlow(0.0);
        int n = (int) Math.round(1.0 / dt);
        for (int i = 0; i < n; i++) c.step(dt);
        c.refresh();
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < 8; j++) sb.append(String.format(" %.3f", c.nodeN(j)));
        System.out.printf("dt=%.3f  1s: P=%.0f rho=%+.0f  n=[%s ]%n", dt, c.powerMW(), c.reactivityPcm(), sb);
        c.close();
    }

    public static void main(String[] a) {
        run(0.005);
        run(0.01);
        run(0.05);
        run(0.2);
        run(0.5);
    }
}
