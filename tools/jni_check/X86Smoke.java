import com.rbmk.real.RbmkCore;

/** 32 位 JVM 冒烟：验证 JNI 调用约定（stdcall）修好后不再崩栈。 */
public class X86Smoke {
    public static void main(String[] a) {
        System.out.println("arch = " + System.getProperty("os.arch")
                + "  java = " + System.getProperty("java.version"));
        RbmkCore c = new RbmkCore();
        c.reset();
        c.setFlowFraction(1.0);
        c.refresh();
        System.out.printf("power = %.1f MW  rho = %+.1f pcm  kEff = %.6f%n",
                c.powerMW(), c.reactivityPcm(), c.kEff());
        for (int i = 0; i < 2000; i++) {          // 100 s
            c.step(0.05);
            if (i % 400 == 0) c.refresh();
        }
        c.refresh();
        System.out.printf("after 100 s: P = %.1f MW  Tf = %.0f C  void = %.3f%n",
                c.powerMW(), c.nodeTf(4), c.nodeVoid(4));
        double[] st;
        st = c.saveState();
        System.out.printf("state[0..2] = %.3f %.3f %.3f  （非零=状态回读正常）%n",
                st[0], st[1], st[2]);
        c.close();
        System.out.println("X86_SMOKE_OK");
    }
}
