package com.darwish.nppsim;

import java.util.prefs.Preferences;

/**
 * 单位换算（2026-10-18 新增，2026-10-25 改为**按量独立**）。
 *
 * 模拟器内部一律用 SI（MPa / °C / kg/s / MW），只在**显示层**换算。
 *
 * 2026-10-25 改动：原来只有一个"公制/英制"总开关，一切捆绑切换 ——
 * 想只把压力换成 psi 而温度仍用 °C 做不到。现在每个物理量各自一套单位、
 * 各自记在首选项里，互不影响：
 *
 *   压力  MPa / bar / psi
 *   温度  °C / °F / K
 *   流量  kg/s / t/h / lb/h
 *   功率  MW / GW / hp
 *   长度  m / ft
 *
 * 老配置里只有一个 {@code ui.units}（SI/US），首次启动时按它给各量设默认值。
 */
public final class Units {

    /** 可独立切换的物理量 */
    public enum Quantity {
        PRESSURE("ui.unit.pressure"),
        TEMPERATURE("ui.unit.temperature"),
        FLOW("ui.unit.flow"),
        POWER("ui.unit.power"),
        LENGTH("ui.unit.length");

        final String prefKey;

        Quantity(String prefKey) {
            this.prefKey = prefKey;
        }
    }

    /** 各量可选的单位代号（顺序即下拉框顺序，第 0 项是公制默认） */
    public static final String[] PRESSURE_UNITS = {"MPa", "bar", "psi"};
    public static final String[] TEMPERATURE_UNITS = {"°C", "°F", "K"};
    public static final String[] FLOW_UNITS = {"kg/s", "t/h", "lb/h"};
    public static final String[] POWER_UNITS = {"MW", "GW", "hp"};
    public static final String[] LENGTH_UNITS = {"m", "ft"};

    private static final Preferences PREFS = Preferences.userRoot().node("com.darwish/nppsim");
    private static final String KEY_LEGACY = "ui.units";

    private Units() {
    }

    public static String[] unitsOf(Quantity q) {
        switch (q) {
            case PRESSURE:    return PRESSURE_UNITS;
            case TEMPERATURE: return TEMPERATURE_UNITS;
            case FLOW:        return FLOW_UNITS;
            case POWER:       return POWER_UNITS;
            case LENGTH:      return LENGTH_UNITS;
            default:          return PRESSURE_UNITS;
        }
    }

    /** 当前选中的单位下标（越界自动回到 0） */
    public static int indexOf(Quantity q) {
        String[] all = unitsOf(q);
        String saved = PREFS.get(q.prefKey, null);
        if (saved != null) {
            for (int i = 0; i < all.length; i++) {
                if (all[i].equals(saved)) return i;
            }
        }
        return legacyDefault(q);
    }

    /** 当前单位代号 */
    public static String unitOf(Quantity q) {
        return unitsOf(q)[indexOf(q)];
    }

    /**
     * SI 值 → 当前单位的显示值（显示层统一走这里）。
     *
     * <p>2026-10-27：以前只有蒸汽环路页用了单位换算，其它窗口一律显示 SI，
     * 于是"设置里切换单位"看起来毫无反应。现在各窗口的数值显示都走这个方法。</p>
     */
    public static double convert(Quantity q, double si) {
        switch (q) {
            case PRESSURE:    return pressure(si);
            case TEMPERATURE: return temperature(si);
            case FLOW:        return flow(si);
            case POWER:       return power(si);
            case LENGTH:      return length(si);
            default:          return si;
        }
    }

    /** 功率显示单位：MWt / MWe、GWt / GWe、hp（热功率/电功率区分后缀） */
    public static String powerLabel(boolean thermal) {
        String u = unitOf(Quantity.POWER);
        if ("MW".equals(u)) {
            return thermal ? "MWt" : "MWe";
        }
        if ("GW".equals(u)) {
            return thermal ? "GWt" : "GWe";
        }
        return u;                       // hp 不区分 t/e
    }

    /** 全部恢复公制默认（设置 → 单位页的"恢复公制"按钮） */
    public static void resetToMetric() {
        for (Quantity q : Quantity.values()) {
            setUnit(q, 0);
        }
    }

    /** 单独设置某一量的单位（不动其它量），并持久化 */
    public static void setUnit(Quantity q, int index) {
        String[] all = unitsOf(q);
        int i = Math.max(0, Math.min(all.length - 1, index));
        PREFS.put(q.prefKey, all[i]);
        try {
            PREFS.flush();
        } catch (Exception ignored) {
        }
    }

    /** 老配置兼容：旧版只有一个 SI/US 总开关 */
    private static int legacyDefault(Quantity q) {
        String legacy = PREFS.get(KEY_LEGACY, null);
        if (legacy == null) return 0;
        boolean us = "US".equalsIgnoreCase(legacy);
        if (!us) return 0;
        switch (q) {
            case PRESSURE:    return 2;   // psi
            case TEMPERATURE: return 1;   // °F
            case FLOW:        return 2;   // lb/h
            case LENGTH:      return 1;   // ft
            default:          return 0;   // 功率仍用 MW
        }
    }

    /**
     * 是否"英制习惯"（压力用 psi）。
     * 保留给旧代码：现在只反映压力这一项，不再代表整体单位制。
     */
    public static boolean isUS() {
        return indexOf(Quantity.PRESSURE) == 2;
    }

    // ------------------------------------------------------------------ 压力
    /** MPa → 当前压力单位 */
    public static double pressure(double mpa) {
        switch (indexOf(Quantity.PRESSURE)) {
            case 1:  return mpa * 10.0;          // 1 MPa = 10 bar
            case 2:  return mpa * 145.0377;      // psi
            default: return mpa;
        }
    }

    public static String pressureUnit() {
        return unitOf(Quantity.PRESSURE);
    }

    /** 压力读数（digits < 0 时按单位自动选小数位） */
    public static String pressure(double mpa, int digits) {
        int i = indexOf(Quantity.PRESSURE);
        int d = (digits >= 0) ? digits : (i == 2 ? 0 : (i == 1 ? 2 : 3));
        return String.format("%." + d + "f %s", pressure(mpa), pressureUnit());
    }

    // ---------------------------------------------------------------- 温度
    /** °C → 当前温度单位 */
    public static double temperature(double celsius) {
        switch (indexOf(Quantity.TEMPERATURE)) {
            case 1:  return celsius * 9.0 / 5.0 + 32.0;   // °F
            case 2:  return celsius + 273.15;             // K
            default: return celsius;
        }
    }

    public static String temperatureUnit() {
        return unitOf(Quantity.TEMPERATURE);
    }

    public static String temperature(double celsius, int digits) {
        return String.format("%." + digits + "f %s", temperature(celsius), temperatureUnit());
    }

    // ---------------------------------------------------------------- 流量
    /** kg/s → 当前流量单位 */
    public static double flow(double kgPerSecond) {
        switch (indexOf(Quantity.FLOW)) {
            case 1:  return kgPerSecond * 3.6;        // t/h
            case 2:  return kgPerSecond * 7936.64;    // lb/h
            default: return kgPerSecond;
        }
    }

    public static String flowUnit() {
        return unitOf(Quantity.FLOW);
    }

    public static String flow(double kgPerSecond, int digits) {
        return String.format("%." + digits + "f %s", flow(kgPerSecond), flowUnit());
    }

    /** 质量流量按 t/h 显示（公制习惯，二回路蒸汽量级）；流量选 lb/h 时跟着变 */
    public static String flowTonsPerHour(double kgPerSecond, int digits) {
        if (indexOf(Quantity.FLOW) == 2) {
            return String.format("%." + digits + "f lb/h", kgPerSecond * 7936.64);
        }
        return String.format("%." + digits + "f t/h", kgPerSecond * 3.6);
    }

    // ---------------------------------------------------------------- 功率
    /** MW → 当前功率单位 */
    public static double power(double mw) {
        switch (indexOf(Quantity.POWER)) {
            case 1:  return mw / 1000.0;      // GW
            case 2:  return mw * 1341.02;     // hp（机械马力）
            default: return mw;
        }
    }

    public static String powerUnit() {
        return unitOf(Quantity.POWER);
    }

    public static String power(double mw, int digits) {
        return String.format("%." + digits + "f %s", power(mw), powerUnit());
    }

    // ---------------------------------------------------------------- 长度
    public static double length(double meters) {
        return indexOf(Quantity.LENGTH) == 1 ? meters * 3.28084 : meters;
    }

    public static String lengthUnit() {
        return unitOf(Quantity.LENGTH);
    }

    public static String length(double meters, int digits) {
        return String.format("%." + digits + "f %s", length(meters), lengthUnit());
    }
}
