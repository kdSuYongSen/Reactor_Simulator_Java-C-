import com.rbmk.real.RbmkCore;

/** 静态棒价值曲线：每个点都先 reset() 回到额定参考态，避免"扫点之间功率已跑飞"。 */
public class RodScan {
    public static void main(String[] a) {
        System.out.println(" pos   Δrho(pcm)   rod(pcm)   n0棒    n7棒   注");
        double prev = Double.NaN;
        for (double pos = 1.0; pos >= -0.0001; pos -= 0.025) {
            RbmkCore c = new RbmkCore();
            c.setRegulatorEnabled(false);
            c.reset();
            c.setFlowFraction(1.0);
            c.setRodPosition(Math.max(0.0, pos));
            c.refresh();
            double rho = c.reactivityPcm();
            String note = "";
            if (!Double.isNaN(prev) && rho > prev + 0.5) note = "<- 下插反而正";
            System.out.printf(" %.3f  %+8.1f  %+8.0f  %+7.0f %+7.0f  %s%n",
                    Math.max(0, pos), rho, c.avgRod(),
                    c.probeRodWorthPcm(Math.max(0, pos), 0), c.probeRodWorthPcm(Math.max(0, pos), 7), note);
            prev = rho;
            c.close();
        }
    }
}
