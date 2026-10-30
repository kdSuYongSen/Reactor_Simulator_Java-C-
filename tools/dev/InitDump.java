import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 子系统初值导出（原版 jar / 本工程 jar 都能跑，纯反射，无编译期依赖）。
 *
 * <pre>
 *   java -cp "OriginalJarFile\RBMK-1500-Simulator.jar;target\initdump"        InitDump 原版  > orig.txt
 *   java -cp "target\RBMK-1500-Simulator-1.00Alpha-jar-with-dependencies.jar;target\initdump" InitDump 本工程 > ours.txt
 * </pre>
 *
 * 每个读数先找 getXxx()/isXxx()，再找同名字段（两版类结构不完全一致，反射兜底）。
 */
public final class InitDump {

    static Class<?> NPPSIM;

    static final String[] CORE_PROPS = {"neutronCount", "reactivity", "period", "thermalPower"};
    static final String[] DRUM_PROPS = {"pressure", "waterTemperature", "steamTemperature",
            "waterMass", "steamMass", "waterLevel", "waterDensity"};
    static final String[] TANK_PROPS = {"pressure", "waterTemperature", "steamTemperature",
            "waterMass", "steamMass", "waterLevel"};
    static final String[] PUMP_PROPS = {"rpm", "setPoint", "flowRate", "flow", "head", "powerUsage"};
    static final String[] VALVE_PROPS = {"position", "flow"};
    static final String[] TG_PROPS = {"rpm", "generatorLoad", "steamInflow", "tripped", "steamTemperature"};

    public static void main(String[] args) {
        String label = args.length > 0 ? args[0] : "sim";
        try {
            run(label);
        } catch (Throwable t) {
            System.out.println("DUMP_ERROR: " + t);
            t.printStackTrace(System.out);
        } finally {
            System.exit(0);      // 两版都会起 UI/刷新线程，必须强制退出
        }
    }

    static void run(String label) throws Exception {
        Class<?> loader = Class.forName("com.darwish.nppsim.Loader");
        Class<?> tablesCls = Class.forName("com.darwish.nppsim.SteamTables");
        Constructor<?> tc = tablesCls.getDeclaredConstructor();
        tc.setAccessible(true);
        Object tables = tc.newInstance();
        Field tf = loader.getDeclaredField("tables");
        tf.setAccessible(true);
        tf.set(null, tables);

        try {   // 原版构造 UI 时会用 soundProvider，缺了会刷 NPE（不影响初值）
            Class<?> sp = Class.forName("com.darwish.nppsim.SoundProvider");
            Object provider = sp.getDeclaredConstructor().newInstance();
            Field sf = loader.getDeclaredField("soundProvider");
            sf.setAccessible(true);
            sf.set(null, provider);
        } catch (Throwable ignore) {
            // 忽略
        }

        NPPSIM = Class.forName("com.darwish.nppsim.NPPSim");
        Constructor<?> ctor = NPPSIM.getDeclaredConstructor();
        ctor.setAccessible(true);
        ctor.newInstance();

        System.out.println("### " + label);

        dumpObj("atmosphere", fld("atmosphere"), TANK_PROPS);

        Object mcc = fld("mcc");
        dumpObj("mcc.drum1", memberOrNull(mcc, "drum1"), DRUM_PROPS);
        dumpObj("mcc.drum2", memberOrNull(mcc, "drum2"), DRUM_PROPS);

        for (String tgName : new String[]{"tg1", "tg2"}) {
            Object tg = fld(tgName);
            dumpObj(tgName, tg, TG_PROPS);
            Object cond = memberOrNull(tg, "condenser");
            Object first = firstOf(cond);
            dumpObj(tgName + ".condenser", first, new String[]{"pressure", "waterTemperature",
                    "steamTemperature", "waterMass", "steamMass", "waterLevel", "condensationRate",
                    "feedwaterMass", "feedwaterLevel", "feedwaterTemperature"});
            if ("tg1".equals(tgName)) {
                Object vp = fldOrNull("vacuumPump1");
                if (vp != null) {
                    dumpObj("vacuumPump1", vp, new String[]{"rpm", "flowRate", "running"});
                }
            }
        }

        dumpList("dearators", fld("dearators"), TANK_PROPS);
        dumpList("condensate1A", fldOrNull("condensate1A"), PUMP_PROPS);
        dumpList("condensate1B", fldOrNull("condensate1B"), PUMP_PROPS);
        dumpList("mainFeedwaterPumps", fldOrNull("mainFeedwaterPumps"), PUMP_PROPS);
        dumpList("auxFeedwaterPumps", fldOrNull("auxFeedwaterPumps"), PUMP_PROPS);
        dumpList("sdv_c", fldOrNull("sdv_c"), VALVE_PROPS);
        dumpList("sdv_a", fldOrNull("sdv_a"), VALVE_PROPS);
        dumpList("msvLoop1", fldOrNull("msvLoop1"), VALVE_PROPS);
        dumpList("TG1InletValves", fldOrNull("TG1InletValves"), VALVE_PROPS);

        dumpObj("steamPiping", fldOrNull("steamPiping"), new String[]{"pressure"});
        dumpObj("condensateHeader1", fldOrNull("condensateHeader1"), new String[]{"pressure"});
        dumpObj("mainFWPressureHeader", fldOrNull("mainFWPressureHeader"), new String[]{"pressure"});
        dumpObj("auxiliaryFWPressureHeader", fldOrNull("auxiliaryFWPressureHeader"), new String[]{"pressure"});
        dumpObj("fwSuctionHeader", fldOrNull("fwSuctionHeader"), new String[]{"pressure"});
        dumpObj("condensateHeader", fldOrNull("condensateHeader"), new String[]{"pressure"});

        dumpList("separators", fldOrNull("separators"),
                new String[]{"pressureMPa", "outSteamKgs", "outWaterKgs", "waterLevel"});
        dumpList("heatExchangers", fldOrNull("heatExchangers"),
                new String[]{"secondaryPressureMPa", "primaryPressureMPa", "secondarySteamTC",
                        "primaryTempC", "secondaryWaterKg"});
        dumpObj("lpHeater", fldOrNull("lpHeater"), TANK_PROPS);
        dumpObj("pcsCondensateFilter", fldOrNull("pcsCondensateFilter"), TANK_PROPS);

        Object pcs = fldOrNull("pcs");
        if (pcs != null) {
            dumpObj("pcs.demineralizedWaterTank", memberOrNull(pcs, "demineralizedWaterTank"), TANK_PROPS);
        }

        dumpObj("core", fld("core"), CORE_PROPS);
    }

    // ------------------------------------------------------------------
    static Object fld(String name) throws Exception {
        Field f = NPPSIM.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(null);
    }

    static Object fldOrNull(String name) {
        try {
            return fld(name);
        } catch (Throwable t) {
            return null;
        }
    }

    static Object memberOrNull(Object o, String name) {
        if (o == null) {
            return null;
        }
        Class<?> c = o.getClass();
        while (c != null) {
            try {
                Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                return f.get(o);
            } catch (Throwable t) {
                c = c.getSuperclass();
            }
        }
        return null;
    }

    static Object firstOf(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof List) {
            List<?> l = (List<?>) o;
            return l.isEmpty() ? null : l.get(0);
        }
        if (o.getClass().isArray()) {
            return Array.getLength(o) == 0 ? null : Array.get(o, 0);
        }
        return o;
    }

    /** 读一个属性：先 getter/isXxx，再同名字段 */
    static Object prop(Object o, String name) {
        if (o == null) {
            return null;
        }
        String cap = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        for (String m : new String[]{"get" + cap, "is" + cap, name}) {
            Class<?> c = o.getClass();
            while (c != null) {
                try {
                    Method mm = c.getDeclaredMethod(m);
                    mm.setAccessible(true);        // 包内可见的类/方法也能读
                    return mm.invoke(o);
                } catch (Throwable ignore) {
                    c = c.getSuperclass();
                }
            }
        }
        return memberOrNull(o, name);
    }

    static void dumpObj(String label, Object o, String[] props) {
        if (o == null) {
            System.out.printf("  %-28s = n/a%n", label);
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String p : props) {
            Object v = prop(o, p);
            if (v != null) {
                sb.append(p).append('=').append(fmt(v)).append(' ');
            }
        }
        System.out.printf("  %-28s %s%n", label, sb.toString().trim());
    }

    static void dumpList(String label, Object list, String[] props) {
        if (list == null) {
            System.out.printf("  %-28s = n/a%n", label);
            return;
        }
        int n = (list instanceof List) ? ((List<?>) list).size()
                : (list.getClass().isArray() ? Array.getLength(list) : 0);
        System.out.printf("  %-28s count=%d%n", label, n);
        int limit = Math.min(n, 6);          // 最多打印 6 个，避免刷屏
        for (int i = 0; i < limit; i++) {
            Object o = (list instanceof List) ? ((List<?>) list).get(i) : Array.get(list, i);
            dumpObj("  " + label + "[" + i + "]", o, props);
        }
    }

    static String fmt(Object v) {
        if (v == null) {
            return "n/a";
        }
        if (v instanceof Boolean) {
            return v.toString();
        }
        if (v instanceof Number) {
            double d = ((Number) v).doubleValue();
            if (Double.isNaN(d)) {
                return "NaN";
            }
            if (Math.abs(d) >= 1000) {
                return String.format("%.1f", d);
            }
            return String.format("%.4g", d);
        }
        return String.valueOf(v);
    }
}
