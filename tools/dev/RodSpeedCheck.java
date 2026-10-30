package com.darwish.nppsim;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.lang.reflect.Field;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * "棒速度"小块自检：
 *  ① 控件存在、位置在"插入"下面、不超出原窗口方块（jPanel5）范围；
 *  ② 两个箭头按钮左右对称；
 *  ③ 档位真的改变棒速——最高档 AZ 棒约 4 s 全拔出，最低档 ≈17.5 s；
 *  ④ 停堆（scram）速度不受档位影响。
 */
public class RodSpeedCheck {

    static int fails = 0;

    static void check(String what, boolean ok, String detail) {
        if (!ok) {
            fails++;
        }
        System.out.printf("  [%s] %-42s %s%n", ok ? "OK" : "FAIL", what, detail);
    }

    public static void main(String[] a) throws Exception {
        Localization.init();
        UiStyle.applyLaf();
        Fonts.init();
        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();
        new NPPSim();
        Thread.sleep(900);

        UI ui = NPPSim.ui;
        JPanel p5 = (JPanel) get(ui, "jPanel5");
        JPanel panel = (JPanel) get(ui, "rodSpeedPanel");
        JButton slower = (JButton) get(ui, "rodSlower");
        JButton faster = (JButton) get(ui, "rodFaster");
        JButton in = (JButton) get(ui, "rodsIn");

        check("棒速度块存在", panel != null && slower != null && faster != null,
                panel == null ? "null" : ("bounds=" + panel.getBounds()));

        Rectangle pr = panel.getBounds();
        Rectangle ir = in.getBounds();
        check("在“插入”按键下方", pr.y > ir.y + ir.height,
                "插入 y=" + ir.y + ".." + (ir.y + ir.height) + "  棒速度 y=" + pr.y);
        check("不超出原窗口方块（jPanel5 高 " + p5.getHeight() + "）",
                pr.y + pr.height <= p5.getHeight(),
                "棒速度底边 " + (pr.y + pr.height) + " ≤ " + p5.getHeight());
        check("宽度与上方按钮一致", pr.width == ir.width, pr.width + " vs " + ir.width);

        Rectangle sr = slower.getBounds();
        Rectangle fr = faster.getBounds();
        int leftGap = sr.x;
        int rightGap = pr.width - (fr.x + fr.width);
        check("两个箭头对称", Math.abs(leftGap - rightGap) <= 2 && sr.width == fr.width,
                "左空 " + leftGap + " / 右空 " + rightGap + "，宽 " + sr.width + "/" + fr.width);

        // ---- 档位 → 棒速 ----
        System.out.println("  —— 档位与全行程时间 ——");
        ControlRodChannel rod = null;
        for (java.util.ArrayList<Channel> row : NPPSim.core.coreArray) {
            for (Channel c : row) {
                if (c instanceof ControlRodChannel) {
                    rod = (ControlRodChannel) c;
                    break;
                }
            }
            if (rod != null) {
                break;
            }
        }
        check("找到一根控制棒（速度逻辑与棒型无关）", rod != null,
                rod == null ? "null" : rod.uiData.name + " / " + rod.getClass().getSimpleName());

        if (rod != null) {
            double minT = measure(rod, 0);          // 默认最低档
            double maxT = 0;
            while (RodSpeed.faster()) {
                // 升到最高档
            }
            maxT = measure(rod, 0);
            System.out.printf("    最低档全行程 %.1f s（档位 1/%d）；最高档全行程 %.1f s（档位 %d/%d）%n",
                    minT, RodSpeed.levels(), maxT, RodSpeed.level() + 1, RodSpeed.levels());
            check("最低档 ≈ 17.5 s", Math.abs(minT - 17.5) < 1.0, String.format("%.1f s", minT));
            check("最高档 ≈ 4.0 s（AZ 棒 4 秒全拔出）", Math.abs(maxT - 4.0) < 0.6, String.format("%.1f s", maxT));

            // 停堆速度不受档位影响（scramSpeed 固定，方向是插入：从全抽出开始）
            rod.setPosition(0.0f);
            rod.setScram(true);
            rod.update();
            double step = rod.getPosition();
            check("停堆速度不受棒速档位影响", Math.abs(step - 0.0038461538) < 1e-6,
                    String.format("scram 单步 %.6f", step));
            rod.setScram(false);
        }

        System.out.println(fails == 0 ? "ROD_SPEED_OK" : "ROD_SPEED_FAIL (" + fails + ")");
        System.exit(0);
    }

    /** 把棒放满插入，然后按 state=0（抽出）一步步跑到 0，返回耗时（秒） */
    static double measure(ControlRodChannel rod, int state) {
        rod.setPosition(1.0f);
        rod.setScram(false);
        rod.setState(0);
        rod.setAutoState(0);
        int ticks = 0;
        while (rod.getPosition() > 0.0f && ticks < 20000) {
            rod.update();
            ticks++;
        }
        rod.setState(1);          // 停手
        rod.setAutoState(1);
        return ticks * 0.05;
    }

    static Object get(Object o, String name) throws Exception {
        Field f = o.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(o);
    }

    static void list(Container c, java.util.List<Component> out) {
        for (Component x : c.getComponents()) {
            out.add(x);
            if (x instanceof Container) {
                list((Container) x, out);
            }
        }
    }
}
