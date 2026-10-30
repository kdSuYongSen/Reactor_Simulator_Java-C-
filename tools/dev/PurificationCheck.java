package com.darwish.nppsim;

/**
 * 河 / 通用净化装置 / 排水阀报警 / 离线泵电流 自检。
 *
 * <p>验证四条：</p>
 * <ol>
 *   <li>河是无限冷源，水温 20 °C；</li>
 *   <li>净化装置默认开启，DW 泵启动后有流量/电流；停运后断水；</li>
 *   <li>水箱排水阀一开，{@code isDrainValveOpen()} 为真（界面亮红灯）；</li>
 *   <li>离线泵电流随转速变化（原来面板显示的是 1996.9 t 水箱水位）。</li>
 * </ol>
 */
public class PurificationCheck {

    static int fails = 0;

    public static void main(String[] a) throws Exception {
        Localization.init();
        UiStyle.applyLaf();
        Fonts.init();
        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();
        new NPPSim();
        Thread.sleep(800);

        PCS pcs = NPPSim.pcs;

        // ---- 河 ----
        check("河存在且为 20 °C 冷源", pcs.river != null && Math.abs(pcs.river.getWaterTemperature() - 20.0) < 0.6,
                pcs.river == null ? "null" : String.format("%.2f °C", pcs.river.getWaterTemperature()));

        // ---- 净化装置默认开启 ----
        check("净化装置默认开启", pcs.purification != null && pcs.purification.isRunning(), "");

        // ---- DW 泵启动 → 净化流量/电流 ----
        pcs.dwMakeupPump.setActive(true);
        Thread.sleep(2500);
        double flow = pcs.purification.getFlowKgs();
        double amps = pcs.purification.getCurrentA();
        check("DW 泵运行 → 净化流量 > 0", flow > 1.0, String.format("%.1f kg/s", flow));
        check("净化装置电流 > 0", amps > 1.0, String.format("%.1f A", amps));

        // ---- 停运净化装置 → 断水 ----
        pcs.purification.setRunning(false);
        Thread.sleep(1200);
        check("净化装置停运 → 流量 0", pcs.purification.getFlowKgs() < 0.01, "");
        check("净化装置停运 → 电流 0", pcs.purification.getCurrentA() < 0.01, "");
        pcs.purification.setRunning(true);
        pcs.dwMakeupPump.setActive(false);

        // ---- 排水阀报警 ----
        check("排水阀关闭时不报警", !pcs.isDrainValveOpen(), "");
        pcs.dwTankDrainValve.setState(2);
        Thread.sleep(1500);
        check("排水阀开启 → 红色报警条件成立", pcs.isDrainValveOpen(),
                String.format("阀位 %.0f%%", pcs.dwTankDrainValve.getPosition() * 100));
        pcs.dwTankDrainValve.setState(0);
        Thread.sleep(1500);

        // ---- 离线泵电流 ----
        OfflineCoolingSystem oc = NPPSim.core.getOfflineCooling();
        Thread.sleep(1500);
        double on = oc.getCurrentA();
        check("离线泵运行电流 ≈ 260 A（两台）", on > 150.0 && on < 300.0, String.format("%.0f A", on));
        oc.setPumpARunning(false);
        oc.setPumpBRunning(false);
        Thread.sleep(2500);
        double off = oc.getCurrentA();
        check("离线泵停运电流 → 0", off < 5.0, String.format("%.0f A", off));
        oc.setPumpARunning(true);
        oc.setPumpBRunning(true);

        System.out.println(fails == 0 ? "PURIFICATION_OK" : ("PURIFICATION_FAIL " + fails));
        System.exit(fails == 0 ? 0 : 1);
    }

    static void check(String name, boolean ok, String detail) {
        if (!ok) {
            fails++;
        }
        System.out.printf("[%s] %s %s%n", ok ? "OK" : "FAIL", name, detail);
    }
}
