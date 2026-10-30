import com.rbmk.real.RbmkCore;

/** 从冷停堆按 0.75 棒位启动，观察能否稳定在额定功率 */
public class Probe5 {
    public static void main(String[] a) {
        RbmkCore c = new RbmkCore();
        c.resetToShutdown();
        c.setRegulatorEnabled(false);
        c.setFlowFraction(1.0);
        c.setOfflineFlow(0.0);

        // 30 s 内把棒从 0 提到 0.75（比真实提棒快得多，只为看平衡点）
        double pos = 0.0;
        for (int i = 0; i < 600; i++) { pos = Math.min(0.75, pos + 0.75 / 600); c.setRodPosition(pos); c.step(0.05); }
        System.out.printf("t=30s  P=%.1f MW rho=%+.0f rod=%.3f%n", c.powerMW(), c.reactivityPcm(), pos);
        for (int i = 0; i < 12000; i++) {
            if (i % 1200 == 0) {
                c.refresh();
                System.out.printf("hold t=%3.0fs P=%7.1f MW rho=%+6.0f Tf7=%.0f void7=%.2f n7=%.3f n0=%.3f%n",
                        i * 0.05, c.powerMW(), c.reactivityPcm(), c.nodeTf(7), c.nodeVoid(7), c.nodeN(7), c.nodeN(0));
            }
            c.step(0.05);
        }
        c.close();
    }
}
