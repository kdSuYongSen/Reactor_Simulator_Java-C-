import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 * RBMK-1500 Simulator - self-check / smoke test (headless-capable, GUI tolerant).
 *
 * Pure reflection: compiles against plain JDK, so the SAME class can be run
 * against the original jar and against the rebuilt fat jar for comparison.
 *
 * Usage:
 *   java -cp "<this dir>;<jar>" SelfCheck [steps] [mode]
 *     steps : Core.update() iterations for the numeric check (default 100)
 *     mode  : "core" = numeric check only; "full" (default) = + NPPSim
 *             construction (the "click Start" path) + 5 s of the 20 Hz loop.
 */
public class SelfCheck {

    private static final List<String> uncaught = Collections.synchronizedList(new ArrayList<String>());

    public static void main(String[] args) throws Exception {
        final int steps = args.length > 0 ? Integer.parseInt(args[0]) : 100;
        final String mode = args.length > 1 ? args[1] : "full";

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            String msg = "UNCAUGHT in thread '" + t.getName() + "': " + e;
            uncaught.add(msg);
            System.out.println("*** " + msg);
            e.printStackTrace(System.out);
        });

        // ---- 0) Look and feel (non-fatal) ----
        try {
            Class<?> lafCls = Class.forName("org.netbeans.swing.laf.dark.DarkMetalLookAndFeel");
            Object laf = lafCls.getDeclaredConstructor().newInstance();
            javax.swing.UIManager.setLookAndFeel((javax.swing.LookAndFeel) laf);
            javax.swing.LookAndFeel cur = javax.swing.UIManager.getLookAndFeel();
            boolean isMetal = cur instanceof javax.swing.plaf.metal.MetalLookAndFeel;
            String themeName = (cur instanceof javax.swing.plaf.metal.MetalLookAndFeel)
                    ? ((javax.swing.plaf.metal.MetalLookAndFeel) cur).getCurrentTheme().getClass().getName() : "n/a";
            System.out.println("LAF OK: " + cur.getClass().getName()
                    + "  isMetal=" + isMetal + "  theme=" + themeName);
        } catch (Throwable t) {
            System.out.println("LAF SKIP (non-fatal): " + t);
        }

        // ---- 1) Bundle ----
        ResourceBundle bundle = ResourceBundle.getBundle("com.darwish.nppsim.Bundle");
        int keys = bundle.keySet().size();
        System.out.println("Bundle OK keys=" + keys);
        if (keys < 300) {
            throw new IllegalStateException("Bundle suspiciously small: " + keys);
        }

        // ---- 2) SteamTables + SoundProvider injection ----
        Class<?> loaderCls = Class.forName("com.darwish.nppsim.Loader");
        Object tables = newInstance("com.darwish.nppsim.SteamTables");
        setStatic(loaderCls, "tables", tables);
        System.out.println("SteamTables OK");
        Object sound = newInstance("com.darwish.nppsim.SoundProvider");
        setStatic(loaderCls, "soundProvider", sound);
        System.out.println("SoundProvider OK");

        // ---- 3) Core numeric check ----
        Class<?> coreCls = Class.forName("com.darwish.nppsim.Core");
        Object core = newInstance("com.darwish.nppsim.Core");
        Method update = coreCls.getDeclaredMethod("update");
        update.setAccessible(true);
        long t0 = System.nanoTime();
        for (int i = 0; i < steps; i++) {
            update.invoke(core);
        }
        long dtMs = (System.nanoTime() - t0) / 1_000_000L;
        System.out.println("Core.update x" + steps + " OK (" + dtMs + " ms)");
        System.out.println("METRIC neutronCount = " + fmt(getDouble(coreCls, core, "neutronCount")));
        System.out.println("METRIC kEff         = " + fmt(getDouble(coreCls, core, "kEff")));
        System.out.println("METRIC reactivity   = " + fmt(getDouble(coreCls, core, "reactivity")));
        System.out.println("METRIC period       = " + fmt(getDouble(coreCls, core, "period")));
        System.out.println("METRIC thermalPower = " + fmt(getDouble(coreCls, core, "thermalPower")));

        // ---- 4) NPPSim construction ("click Start" path) ----
        if ("full".equals(mode)) {
            Object sim = newInstance("com.darwish.nppsim.NPPSim");
            System.out.println("NPPSim constructed OK (UI created)");
            Thread.sleep(5000L); // let the 20 Hz simulation loop run a while
            System.out.println("20Hz loop ran ~5 s, uncaught so far=" + uncaught.size());
        }

        // ---- result ----
        Thread.sleep(300L);
        if (!uncaught.isEmpty()) {
            for (String m : new ArrayList<String>(uncaught)) {
                System.out.println("  * " + m);
            }
            System.out.println("UNCAUGHT_EXCEPTIONS=" + uncaught.size());
            System.out.println("SMOKE_FAIL");
            System.exit(1);
        }
        System.out.println("SMOKE_OK");
        System.exit(0);
    }

    private static String fmt(double v) {
        return String.format("%.10e", v);
    }

    private static Object newInstance(String cls) throws Exception {
        Constructor<?> c = Class.forName(cls).getDeclaredConstructor();
        c.setAccessible(true);
        return c.newInstance();
    }

    private static void setStatic(Class<?> cls, String field, Object val) throws Exception {
        Field f = cls.getDeclaredField(field);
        f.setAccessible(true);
        f.set(null, val);
    }

    private static double getDouble(Class<?> cls, Object o, String field) throws Exception {
        /* 字段可能改名/不存在（例如 Core 里没有 kEff）——探针本身不应该因此中断 */
        for (Class<?> c = cls; c != null; c = c.getSuperclass()) {
            try {
                Field f = c.getDeclaredField(field);
                f.setAccessible(true);
                return f.getDouble(o);
            } catch (NoSuchFieldException ignored) {
                // 继续找父类
            }
        }
        return Double.NaN;
    }
}
