import com.rbmk.real.RbmkCore;

/** 模拟操作员缓慢提棒启动：0.20 -> 0.36 (冷态临界附近)，再看能否稳定 */
public class Probe12 {
    public static void main(String[] a) {
        RbmkCore c = new RbmkCore();
        c.resetToShutdown();
        c.setRegulatorEnabled(false);
        c.setFlowFraction(1.0);
        c.setOfflineFlow(0.0);
        double p = 0.20;
        c.setRodPosition(p);
        // 缓慢提棒：0.01 每秒（比游戏内 0.057/s 更慢，先看物理是否正常）
        for (int i = 0; i <= 1600; i++) {
            if (i % 200 == 0 || (i % 40 == 0 && i > 0)) {
                c.refresh();
                System.out.printf("t=%5.1fs p=%.3f rho=%+6.0f P=%8.2fMW period=%.3g%n",
                        i * 0.05, p, c.reactivityPcm(), c.powerMW(), c.period());
            }
            c.step(0.05);
            if (i < 1200) { p = Math.min(0.36, p + 0.01 * 0.05); c.setRodPosition(p); }
        }
        c.close();
    }
}
