package com.darwish.nppsim;

/** AZ 棒逻辑自检：自动 AZ 不动玩家手动棒，紧急停堆才全插。 */
public class AzRodLogicCheck {

    static int fails = 0;

    public static void main(String[] a) throws Exception {
        Localization.init();
        UiStyle.applyLaf();
        Fonts.init();
        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();
        new NPPSim();
        Thread.sleep(800);

        ControlRodChannel manual = null;
        ControlRodChannel fasr = null;
        ControlRodChannel acr = null;
        outer:
        for (java.util.ArrayList<Channel> row : NPPSim.core.coreArray) {
            for (Channel c : row) {
                if (c instanceof ControlRodChannel) {
                    if (manual == null && c.getClass() == MCRChannel.class) {
                        manual = (ControlRodChannel) c;
                    } else if (fasr == null && c instanceof FASRChannel) {
                        fasr = (ControlRodChannel) c;
                    } else if (acr == null && c instanceof ACRChannel) {
                        acr = (ControlRodChannel) c;
                    }
                }
                if (manual != null && fasr != null && acr != null) {
                    break outer;
                }
            }
        }
        check("找到手动棒 MCR", manual != null, "");
        check("找到快速停堆棒 FASR", fasr != null, "");
        check("找到自动棒 ACR", acr != null, "");

        AutoControl ac = NPPSim.autoControl;

        // ---- 自动 AZ-6：只插非手动棒 ----
        manual.setPosition(0.0f);   // 抽出（玩家操控状态）
        fasr.setPosition(0.0f);
        ac.az6Control.trip();
        Thread.sleep(400);
        check("AZ-6 触发后 FASR 被插入（scram）", fasr.getPosition() > 0.0f || isScram(fasr),
                "pos=" + fasr.getPosition());
        check("AZ-6 触发后手动棒不被插入", manual.getPosition() < 0.01f,
                "pos=" + manual.getPosition());
        ac.az6Control.reset();
        Thread.sleep(300);

        // ---- 紧急停堆 AZ-5：全插 ----
        manual.setPosition(0.0f);
        fasr.setPosition(0.0f);
        ac.az5Control.trip();
        Thread.sleep(400);
        check("AZ-5 触发后 FASR 被插入", fasr.getPosition() > 0.0f || isScram(fasr),
                "pos=" + fasr.getPosition());
        check("AZ-5 触发后手动棒也被插入", manual.getPosition() > 0.0f || isScram(manual),
                "pos=" + manual.getPosition());
        ac.az5Control.reset();
        Thread.sleep(300);

        System.out.println(fails == 0 ? "AZ_ROD_LOGIC_OK" : ("AZ_ROD_LOGIC_FAIL " + fails));
        System.exit(fails == 0 ? 0 : 1);
    }

    static boolean isScram(ControlRodChannel rod) throws Exception {
        java.lang.reflect.Field f = ControlRodChannel.class.getDeclaredField("scram");
        f.setAccessible(true);
        return f.getBoolean(rod);
    }

    static void check(String name, boolean ok, String detail) {
        if (!ok) {
            fails++;
        }
        System.out.printf("[%s] %s %s%n", ok ? "OK" : "FAIL", name, detail);
    }
}
