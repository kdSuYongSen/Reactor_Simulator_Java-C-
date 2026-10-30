package com.darwish.nppsim;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JComponent;

/** 布局工具验收：能否改到第 3 层按键、兄弟是否不动、保存/恢复是否闭环。 */
public class ToolsTabCheck {

    static JComponent deepBlock;
    static Component deepTarget;
    static int deepDepth;

    public static void main(String[] a) throws Exception {
        Localization.init();
        Localization.setLanguage(Language.ZH_CN);
        UiStyle.applyLaf();
        Fonts.init();

        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();
        new NPPSim();
        CondensateUI w = new CondensateUI();
        w.setSize(980, 560);
        w.setVisible(true);
        w.validate();
        Thread.sleep(1400);                      // 等页面注册 + 枚举方块（350 ms）

        PageLayout.Page page = null;
        for (PageLayout.Page p : PageLayout.pages()) {
            if ("CondensateUI".equals(p.id)) {
                page = p;
            }
        }
        if (page == null) {
            System.out.println("找不到 CondensateUI 页");
            System.exit(1);
        }
        System.out.println("页=" + PageLayout.titleOf(page) + "  方块数=" + page.blocks.size());
        for (PageLayout.Page p : PageLayout.pages()) {
            System.out.printf("  page %-18s blocks=%3d host=%s managed=%s%n",
                    p.id, p.blocks.size(),
                    p.host == null ? "null" : p.host.getClass().getSimpleName(), p.managed);
        }

        // 找最深的一个按键
        for (java.util.Map.Entry<String, JComponent> e : page.blocks.entrySet()) {
            findDeep(e.getValue(), e.getValue(), 0, 0, e.getKey());
        }
        if (deepTarget == null) {
            System.out.println("没找到深层按键");
            System.exit(1);
        }
        String path = PageLayout.pathOf(deepBlock, deepTarget);
        System.out.printf("最深按键: %s  深度=%d  路径=%s  bounds=%s%n",
                label(deepTarget), deepDepth, path, deepTarget.getBounds());

        // 兄弟（同一父容器里另一个子组件）的原始位置
        Container parent = deepTarget.getParent();
        Component sibling = null;
        for (Component c : parent.getComponents()) {
            if (c != deepTarget) {
                sibling = c;
                break;
            }
        }
        Rectangle sibBefore = sibling == null ? null : sibling.getBounds();

        // 改：右移 40、下移 12、加大 18×8
        Rectangle r = deepTarget.getBounds();
        PageLayout.applyNode(page, deepTarget, new Rectangle(
                r.x + 40, r.y + 12, r.width + 18, r.height + 8), true);
        Thread.sleep(300);

        System.out.println("改后 bounds=" + deepTarget.getBounds());
        if (sibling != null) {
            System.out.println("兄弟 bounds 改前=" + sibBefore + " 改后=" + sibling.getBounds()
                    + (sibBefore.equals(sibling.getBounds()) ? "  （未动 ✔）" : "  （被带动 ✘）"));
        }

        // 保存 → 看文件里有没有深层路径
        PageLayout.savePage(page);
        Thread.sleep(200);
        String json = new String(java.nio.file.Files.readAllBytes(page.file.toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        boolean hasNode = json.contains("\"" + path + "\"");
        System.out.println("保存文件=" + page.file.getName()
                + "  含 paths 节点 \"" + path + "\" ? " + (hasNode ? "是 ✔" : "否 ✘")
                + "  文件行数=" + json.split("\n").length);

        // 落盘后再走一遍"读文件回放"：先把按键挪到别处，然后逼轮询重读文件
        Rectangle saved = deepTarget.getBounds();
        PageLayout.applyNode(page, deepTarget, new Rectangle(5, 5, 30, 15), true);
        Thread.sleep(200);
        System.out.println("挪走后 bounds=" + deepTarget.getBounds());
        // stamp 置 -1 让 1 s 轮询认为文件"变了"
        java.lang.reflect.Field f = PageLayout.Page.class.getDeclaredField("stamp");
        f.setAccessible(true);
        f.setLong(page, -1L);
        Thread.sleep(1800);
        System.out.println("读文件回放后 bounds=" + deepTarget.getBounds()
                + "  期望=" + saved
                + (saved.equals(deepTarget.getBounds()) ? "  （一致 ✔）" : "  （不一致 ✘）"));

        // 恢复默认
        PageLayout.resetPage(page);
        Thread.sleep(400);
        System.out.println("恢复默认后 bounds=" + deepTarget.getBounds()
                + "  布局文件还在? " + page.file.exists());
        System.out.println("TOOLS_CHECK_OK");
        System.exit(0);
    }

    static void findDeep(Component c, JComponent block, int depth, int guard, String blockId) {
        if (guard > 5000 || depth > 7) {
            return;
        }
        if (depth > deepDepth && c instanceof AbstractButton) {
            String t = ((AbstractButton) c).getText();
            if (t != null && !t.trim().isEmpty()) {
                deepDepth = depth;
                deepTarget = c;
                deepBlock = block;
            }
        }
        if (c instanceof Container) {
            for (Component k : ((Container) c).getComponents()) {
                findDeep(k, block, depth + 1, guard + 1, blockId);
            }
        }
    }

    static String label(Component c) {
        if (c instanceof AbstractButton) {
            return "[" + ((AbstractButton) c).getText() + "] " + c.getClass().getSimpleName();
        }
        return c.getClass().getSimpleName();
    }
}
