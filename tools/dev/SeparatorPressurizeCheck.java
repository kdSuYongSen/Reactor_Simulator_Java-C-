package com.darwish.nppsim;

/**
 * 汽水分离器建压自检：冷态常压起步，喂蒸汽后应升到 4.0 MPa 设定值并稳住。
 *
 * <p>2026-10-27 把分离器初值从 4.0 MPa 改成 0.1014（对齐原版汽包冷态初值）后，
 * 用这个探针确认"产汽 → 建压 → 稳压"这条链还在。</p>
 *
 * 用法：java -cp "…jar-with-dependencies;target/verify" com.darwish.nppsim.SeparatorPressurizeCheck
 */
public class SeparatorPressurizeCheck {

    static int fails = 0;

    static void check(String what, boolean ok, String detail) {
        if (!ok) {
            fails++;
        }
        System.out.printf("  [%s] %-40s %s%n", ok ? "OK" : "FAIL", what, detail);
    }

    public static void main(String[] a) {
        Separator s = new Separator("A");
        System.out.printf("  初值：p=%.4f MPa  水位=%.1f%%%n", s.pressureMPa, s.waterLevel);
        check("冷态常压起步", s.pressureMPa < 0.3, fmt(s.pressureMPa) + " MPa");

        // 真实 RBMK-1500 的鼓形分离器是静止设备：不该有转速/电机/刹车这些概念
        String[] forbidden = {"rpm", "auxMotorOn", "absBrake",
                "alarmHighSpeed", "alarmAbsBrake", "alarmLowSpeed", "alarmReversePower"};
        StringBuilder bad = new StringBuilder();
        for (String f : forbidden) {
            try {
                Separator.class.getDeclaredField(f);
                bad.append(f).append(' ');
            } catch (NoSuchFieldException ignore) {
                // 正常：字段已删除
            }
        }
        check("无转速/辅助电机/ABS 刹车等转动部件字段", bad.length() == 0,
                bad.length() == 0 ? "已全部移除" : ("仍存在：" + bad));

        // 无进汽 60 s：压力应该基本不动（不会自己涨到设定值）
        for (int i = 0; i < 1200; i++) {
            s.update(0.05, 0.0, 250.0);
        }
        check("无进汽不自增压", s.pressureMPa < 0.3,
                fmt(s.pressureMPa) + " MPa（跑 60 s 后）");

        // 喂 600 kg/s、250 °C 蒸汽：压力应升到设定值附近
        double t = 0;
        for (int i = 0; i < 6000 && s.pressureMPa < 3.9; i++) {
            s.update(0.05, 600.0, 250.0);
            t += 0.05;
        }
        System.out.printf("  进汽 600 kg/s：%.1f s 后 p=%.3f MPa  出口汽=%.0f kg/s  携带水=%.1f kg/s%n",
                t, s.pressureMPa, s.outSteamKgs, s.outWaterKgs);
        check("产汽后建压到设定值附近（>3.0 MPa）", s.pressureMPa > 3.0, fmt(s.pressureMPa) + " MPa");
        check("建压后开始出汽", s.outSteamKgs > 1.0, fmt(s.outSteamKgs) + " kg/s");

        // 继续跑 120 s：应稳定在设定值附近（不无限上涨）
        for (int i = 0; i < 2400; i++) {
            s.update(0.05, 600.0, 250.0);
        }
        System.out.printf("  稳态：p=%.3f MPa  出口汽=%.0f kg/s  水位=%.0f%%%n",
                s.pressureMPa, s.outSteamKgs, s.waterLevel);
        check("压力稳定在 4.0 MPa 附近（3.0~5.0）",
                s.pressureMPa > 3.0 && s.pressureMPa < 5.0, fmt(s.pressureMPa) + " MPa");
        check("水位/流量有效",
                !Double.isNaN(s.waterLevel) && !Double.isNaN(s.outSteamKgs),
                "水位=" + String.format("%.0f", s.waterLevel) + "%  汽=" + fmt(s.outSteamKgs) + " kg/s");

        System.out.println(fails == 0 ? "SEPARATOR_OK" : "SEPARATOR_FAIL (" + fails + ")");
    }

    static String fmt(double v) {
        if (Double.isNaN(v)) {
            return "NaN";
        }
        return String.format("%.3f", v);
    }
}
