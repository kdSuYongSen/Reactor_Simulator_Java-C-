package com.darwish.nppsim;

/**
 * 探针：列出当前构建里每个页面注册了哪些方块（诊断外部编辑器 id 是否对得上）。
 * 用法：
 *   javac -encoding UTF-8 -cp "target\RBMK-1500-Simulator-1.00Alpha-jar-with-dependencies.jar" ^
 *         -d target\verify tools\dev\PageBlocksProbe.java
 *   java -cp "target\RBMK-1500-Simulator-1.00Alpha-jar-with-dependencies.jar;target\verify" ^
 *        com.darwish.nppsim.PageBlocksProbe
 */
public class PageBlocksProbe {
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
        Thread.sleep(1800);
        for (PageLayout.Page p : PageLayout.pages()) {
            StringBuilder sb = new StringBuilder();
            for (String k : p.blocks.keySet()) {
                sb.append(k).append(' ');
            }
            System.out.println("PAGE " + p.id + " (" + p.title + ") blocks="
                    + p.blocks.size() + " [" + sb.toString().trim() + "]");
        }
        PageLayout.Page target = null;
        for (PageLayout.Page p : PageLayout.pages()) {
            if ("CondensateUI".equals(p.id)) {
                target = p;
            }
        }
        if (target != null) {
            javax.swing.JComponent b5 = target.blocks.get("5");
            System.out.println("CondensateUI block5=" + b5);
            if (b5 != null) {
                java.awt.Component t = PageLayout.resolvePath(b5, "0/2/2");
                System.out.println("0/2/2=" + (t == null ? "null"
                        : t.getClass().getSimpleName() + " " + t.getBounds()));
            }
        }
        // 主控制室：外部编辑器写的 ui_layout.json 是否生效（方块 + 深层节点）
        for (PageLayout.Page p : PageLayout.pages()) {
            if (!"main".equals(p.id)) {
                continue;
            }
            javax.swing.JComponent cm = p.blocks.get("coremap");
            System.out.println("MAIN coremap=" + (cm == null ? "null" : cm.getBounds()));
            if (cm != null) {
                java.awt.Component t = PageLayout.resolvePath(cm, "0/0");
                System.out.println("MAIN coremap/0/0=" + (t == null ? "null" : t.getBounds()));
            }
            javax.swing.JComponent mwe2 = p.blocks.get("mwe2");
            System.out.println("MAIN mwe2 visible=" + (mwe2 != null && mwe2.isVisible()));
        }
        System.exit(0);
    }
}
