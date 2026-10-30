package com.darwish.nppsim;

import java.awt.Component;

/** 核对：外部编辑器写的 "<page>.layout.json" 里的深层节点，模拟器是否真的照做。 */
public class DeepApplyCheck {
    public static void main(String[] a) throws Exception {
        Localization.init();
        UiStyle.applyLaf();
        Fonts.init();
        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();
        new NPPSim();
        CondensateUI w = new CondensateUI();
        w.setSize(980, 560);
        w.setVisible(true);
        w.validate();
        Thread.sleep(1800);            // 等页面初始化 + 读布局文件

        PageLayout.Page page = null;
        for (PageLayout.Page p : PageLayout.pages()) {
            if ("CondensateUI".equals(p.id)) page = p;
        }
        if (page == null) {
            System.out.println("没有 CondensateUI 页");
            System.exit(1);
        }
        javax.swing.JComponent block = page.blocks.get("5");
        if (block == null) {
            System.out.println("方块 5 不存在（清单与运行时不一致）");
            System.exit(1);
        }
        Component t = PageLayout.resolvePath(block, "0/2/2");
        if (t == null) {
            System.out.println("路径 0/2/2 解析失败");
            System.exit(1);
        }
        System.out.printf("运行时：方块 5 / 0/2/2 = %s  bounds=%s%n",
                t.getClass().getSimpleName(), t.getBounds());
        System.out.println("期望：java.awt.Rectangle[x=32,y=10,width=21,height=22]");
        System.out.println(t.getX() == 32 && t.getY() == 10 && t.getWidth() == 21 && t.getHeight() == 22
                ? "DEEP_APPLY_OK" : "DEEP_APPLY_MISMATCH");
        System.exit(0);
    }
}
