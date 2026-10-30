package com.darwish.nppsim;

import java.lang.reflect.Field;
import java.util.Locale;
import sun.misc.Unsafe;

/**
 * 汽轮机"自转"复现与回归测试（无界面）。
 *
 * 场景：主汽门关闭 / 完全没有蒸汽时，转速必须保持 0。
 * 历史 bug：未并网转速按 `3000*(1+0.15*(loadRatio-1))` 计算，
 * loadRatio 为负时该式仍给出 ~2540 rpm 的正目标 → 汽轮机无汽自转。
 */
public class TgSpinTest {

    static TG tg;
    static Unsafe unsafe;

    static Unsafe unsafe() throws Exception {
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (Unsafe) f.get(null);
    }

    /** 给一个没有构造器的 MCC 实例灌上高压汽包，供 update() 的保护逻辑读取 */
    static void fakeMcc() throws Exception {
        if (unsafe == null) {
            unsafe = unsafe();
        }
        MCC mcc = (MCC) unsafe.allocateInstance(MCC.class);
        Class<?> drumClass = null;
        for (Class<?> c : MCC.class.getDeclaredClasses()) {
            if (c.getSimpleName().equals("SeparatorDrum")) {
                drumClass = c;
            }
        }
        Object d1 = unsafe.allocateInstance(drumClass);
        Object d2 = unsafe.allocateInstance(drumClass);
        Field pressure = drumClass.getSuperclass().getDeclaredField("pressure");
        pressure.setAccessible(true);
        pressure.setDouble(d1, 6.9);
        pressure.setDouble(d2, 6.9);
        Field f1 = MCC.class.getDeclaredField("drum1");
        Field f2 = MCC.class.getDeclaredField("drum2");
        f1.setAccessible(true);
        f2.setAccessible(true);
        unsafe.putObject(mcc, unsafe.objectFieldOffset(f1), d1);
        unsafe.putObject(mcc, unsafe.objectFieldOffset(f2), d2);
        NPPSim.mcc = mcc;
    }

    static void setField(Object o, String name, double v) throws Exception {
        Class<?> c = o.getClass();
        while (c != null) {
            try {
                Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                f.setDouble(o, v);
                return;
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    static void step(double steamKgs) throws Exception {
        setField(tg, "steamInflow", steamKgs * 0.05);        // kg/tick
        tg.update();
    }

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);
        Loader.tables = new SteamTables();
        NPPSim.tables = Loader.tables;
        NPPSim.atmosphere = new Atmosphere(15);
        fakeMcc();

        boolean ok = true;

        // ---- 1. 目标转速表 ----
        System.out.println("-- 目标转速（speedBenchmark）--");
        double[] flows = {0, 5, 20, 60, 200, 328, 900, 1200, 1500};
        for (double f : flows) {
            System.out.printf("  %6.0f kg/s → %7.1f rpm%n", f, TG.speedBenchmark(f));
        }
        ok &= TG.speedBenchmark(0.0) == 0.0;
        ok &= TG.speedBenchmark(5.0) == 0.0;
        ok &= TG.speedBenchmark(20.0) == 0.0;                 // 恰好等于空载汽耗
        ok &= TG.speedBenchmark(60.0) > 0.0 && TG.speedBenchmark(60.0) < 1000.0;
        ok &= Math.abs(TG.speedBenchmark(328.0) - 3000.0) < 1.0;   // 35% 汽量到额定
        ok &= Math.abs(TG.speedBenchmark(900.0) - 3000.0) < 1.0;   // 额定汽量
        ok &= TG.speedBenchmark(1200.0) < 3250.0;                  // 1.33 倍额定仍在速跳闸阈值内
        ok &= TG.speedBenchmark(1500.0) > 3250.0;                   // 1.67 倍额定应触发超速跳闸

        // ---- 2. 零蒸汽 3 s：转速必须保持 0 ----
        tg = new TG(new Condenser());
        for (int i = 0; i < 60; i++) {
            step(0.0);
        }
        System.out.printf("零蒸汽 3s：rpm = %.1f%n", tg.getRpm());
        ok &= tg.getRpm() < 0.5;

        // ---- 3. 极低蒸汽（5 kg/s，低于空载汽耗 20 kg/s）：同样不该转 ----
        tg = new TG(new Condenser());
        for (int i = 0; i < 60; i++) {
            step(5.0);
        }
        System.out.printf("5 kg/s 蒸汽 3s：rpm = %.1f%n", tg.getRpm());
        ok &= tg.getRpm() < 0.5;

        // ---- 4. 越过空载汽耗（60 kg/s）应当能转起来，但转速仍然很低 ----
        tg = new TG(new Condenser());
        for (int i = 0; i < 400; i++) {
            step(60.0);
        }
        System.out.printf("60 kg/s 蒸汽 20s：rpm = %.1f（应 > 0 且 < 1000）%n", tg.getRpm());
        ok &= tg.getRpm() > 10.0 && tg.getRpm() < 1000.0;

        // ---- 5. 额定汽量应当稳定在 3000 ----
        tg = new TG(new Condenser());
        for (int i = 0; i < 2000; i++) {
            step(900.0);
        }
        System.out.printf("900 kg/s 蒸汽 100s：rpm = %.1f（应进入同期窗口 2990~3010）%n", tg.getRpm());
        ok &= tg.getRpm() >= 2990.0 && tg.getRpm() <= 3010.0;

        // ---- 6. 惰走：从转动状态断汽，转速应衰减 ----
        tg = new TG(new Condenser());
        for (int i = 0; i < 400; i++) {
            step(900.0);
        }
        double spin = tg.getRpm();
        for (int i = 0; i < 120; i++) {
            step(0.0);
        }
        System.out.printf("断汽惰走 6s：%.1f → %.1f rpm（应下降）%n", spin, tg.getRpm());
        ok &= tg.getRpm() < spin;

        System.out.println(ok ? "\nPASS" : "\nFAIL");
        if (!ok) {
            System.exit(1);
        }
    }
}
