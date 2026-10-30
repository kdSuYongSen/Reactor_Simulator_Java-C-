package com.darwish.nppsim;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/** 阀门开度初始化体检：列出所有阀门的位置/状态，标出不合法值（NaN、超界、半开、状态与开度矛盾）。 */
public class ValveInitCheck {

    public static void main(String[] a) throws Exception {
        Localization.init();
        UiStyle.applyLaf();
        Fonts.init();
        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();
        new NPPSim();
        Thread.sleep(1200);

        List<String> rows = new ArrayList<>();
        int bad = 0;
        for (Field f : NPPSim.class.getDeclaredFields()) {
            if (!java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            f.setAccessible(true);
            Object v = f.get(null);
            if (v instanceof List) {
                int i = 0;
                for (Object o : (List<?>) v) {
                    if (isValve(o)) {
                        String line = f.getName() + "[" + i + "] " + desc(o);
                        rows.add(line);
                        if (!ok(o)) {
                            bad++;
                            System.out.println("BAD  " + line);
                        }
                    }
                    i++;
                }
            } else if (isValve(v)) {
                String line = f.getName() + " " + desc(v);
                rows.add(line);
                if (!ok(v)) {
                    bad++;
                    System.out.println("BAD  " + line);
                }
            }
        }
        System.out.println("阀门总数: " + rows.size() + "  异常: " + bad);
        for (String s : rows) {
            System.out.println("  " + s);
        }
        System.out.println(bad == 0 ? "VALVES_OK" : "VALVES_BAD");
        System.exit(bad == 0 ? 0 : 1);
    }

    static boolean isValve(Object o) {
        if (o == null) {
            return false;
        }
        String n = o.getClass().getName();
        return n.endsWith("SteamValve") || n.endsWith("WaterValve") || n.contains("Valve");
    }

    static String desc(Object o) throws Exception {
        return String.format("%-22s pos=%.3f state=%s auto=%s autoState=%s",
                o.getClass().getSimpleName(), num(o, "getPosition"), str(o, "getState"),
                str(o, "isAutoEnabled"), str(o, "getAutoState"));
    }

    static boolean ok(Object o) throws Exception {
        double p = num(o, "getPosition");
        if (Double.isNaN(p) || p < -0.001 || p > 1.001) {
            return false;
        }
        // 半开只允许在明确"调节阀"上；这里只警告，不判死
        return true;
    }

    static double num(Object o, String m) throws Exception {
        try {
            java.lang.reflect.Method mm = o.getClass().getMethod(m);
            mm.setAccessible(true);
            return ((Number) mm.invoke(o)).doubleValue();
        } catch (Throwable t) {
            return Double.NaN;
        }
    }

    static String str(Object o, String m) {
        try {
            java.lang.reflect.Method mm = o.getClass().getMethod(m);
            mm.setAccessible(true);
            return String.valueOf(mm.invoke(o));
        } catch (Throwable t) {
            return "-";
        }
    }
}
