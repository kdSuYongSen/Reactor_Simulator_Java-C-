import com.rbmk.real.RbmkCore;

/** 指纹探针：用同一组操作打出一串数字，用于对比"出厂 DLL"与"源码重编译 DLL"。 */
public class Fingerprint {
    public static void main(String[] a) {
        RbmkCore c = new RbmkCore();
        c.setRegulatorEnabled(false);
        c.reset();
        c.setFlowFraction(1.0);
        c.setOfflineFlow(0.0);
        c.refresh();
        p(c, "reset(额定)");
        System.out.printf("trim=%+.1f  probeVoid=%.3f  fullVoid=%.1f  doppler=%.4f  mod=%.2f%n",
                c.trimPcm(), c.probeVoidCoeffPcm(), c.probeFullVoidPcm(),
                c.probeDopplerCoeff(), c.probeModeratorCoeff());
        for (int j = 0; j < 8; j++)
            System.out.printf(" n%d Tcool=%.4f Tf=%.2f Tmod=%.2f void=%.5f Tin=%.4f%n",
                    j, c.nodeTcool(j), c.nodeTf(j), c.nodeTmod(j), c.nodeVoid(j), c.nodeTin(j));

        // 提棒到 0.47 的启动曲线（冷停堆）
        c.resetToShutdown();
        c.setFlowFraction(1.0);
        c.refresh();
        System.out.printf("shutdown: P=%.3f rho=%+.0f rod=%.3f%n", c.powerMW(), c.reactivityPcm(), c.rodPosition());
        double pos = 0.30;
        c.setRodPosition(pos);
        for (int i = 0; i < 3600; i++) {   // 3 min
            if (i % 100 == 0 && pos < 0.47) { pos = Math.min(0.47, pos + 0.002 * 5.0); c.setRodPosition(pos); }
            c.step(0.05);
        }
        c.refresh();
        p(c, "启动 3min(rod=0.47)");

        // 降流
        c.setFlowFraction(0.5);
        for (int i = 0; i < 600; i++) c.step(0.05);
        c.refresh();
        p(c, "流量50%% 30s");

        // SCRAM
        c.reset();
        c.setRegulatorEnabled(false);
        c.setFlowFraction(1.0);
        c.scram();
        for (int i = 0; i < 400; i++) c.step(0.05);
        c.refresh();
        p(c, "SCRAM 20s");
        c.close();
    }

    static void p(RbmkCore c, String tag) {
        System.out.printf("%-22s P=%9.3f MW rho=%+8.2f period=%.4g Tout=%.4f Tf0=%.3f Tmod0=%.3f%n",
                tag, c.powerMW(), c.reactivityPcm(), c.period(), c.outletTemp(), c.nodeTf(0), c.nodeTmod(0));
    }
}
