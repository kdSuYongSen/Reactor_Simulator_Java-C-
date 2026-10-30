package com.darwish.nppsim;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * 页面布局探针（无界面验证）：
 *   1. 构造一个窗口 → 检查自动发现的方块数量、清单文件是否导出；
 *   2. 写一份该页的布局文件 → 重新构造 → 检查方块坐标是否被应用；
 *   3. 删掉布局文件 → 检查是否恢复原 GroupLayout。
 *
 * 用法：在临时目录里运行（该目录需先放一个空的 ui_layout.json，
 * 让 UiLayoutFile.locate() 命中它，从而把布局目录指到这里）。
 */
public class PageLayoutProbe {

    static String read(File f) throws Exception {
        return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws Exception {
        java.util.Locale.setDefault(java.util.Locale.SIMPLIFIED_CHINESE);
        Localization.init();
        Loader.tables = new SteamTables();
        NPPSim.tables = Loader.tables;
        NPPSim.atmosphere = new Atmosphere(15);
        NPPSim.separators = new ArrayList<>();
        NPPSim.heatExchangers = new ArrayList<>();
        NPPSim.dearators = new ArrayList<>();

        boolean ok = true;

        // ---- 1. 构造窗口，等首次布局 ----
        SteamLoopUI ui = new SteamLoopUI();
        ui.setVisible(true);
        Thread.sleep(1600);
        PageLayout.dumpManifest();

        File manifest = new File(PageLayout.dir(), "ui_layout_pages.json");
        System.out.println("布局目录 = " + PageLayout.dir());
        System.out.println("清单存在 = " + manifest.isFile());
        ok &= manifest.isFile();
        String text = manifest.isFile() ? read(manifest) : "";
        System.out.println(text.length() > 800 ? text.substring(0, 800) + "..." : text);
        ok &= text.contains("\"SteamLoopUI\"");

        // ---- 2. 方块自动发现（与 PageLayout.discover 同一路径：单子节点链钻到底） ----
        java.awt.Container root = ui.getContentPane();
        int guard = 0;
        while (root.getComponentCount() == 1 && root.getComponent(0) instanceof java.awt.Container
                && guard++ < 4) {
            root = (java.awt.Container) root.getComponent(0);
        }
        java.awt.Component first = null;
        int blockCount = 0;
        for (java.awt.Component c : root.getComponents()) {
            if (c instanceof javax.swing.JComponent) {
                if (first == null) {
                    first = c;
                }
                blockCount++;
            }
        }
        System.out.println("方块数 = " + blockCount);
        ok &= blockCount >= 3;
        java.awt.Rectangle before = first.getBounds();

        // ---- 3. 写布局文件：把第一个方块挪到 (0,0) 并改尺寸 ----
        String json = "{\n  \"mode\": \"absolute\",\n  \"page\": \"SteamLoopUI\",\n  \"panels\": {\n"
                + "    \"0\": {\"x\": 0, \"y\": 0, \"w\": 321, \"h\": 123, \"content_scale\": 1.0}\n  }\n}\n";
        File pageFile = new File(PageLayout.dir(), "SteamLoopUI.layout.json");
        Files.write(pageFile.toPath(), json.getBytes(StandardCharsets.UTF_8));
        Thread.sleep(1800);   // 轮询周期 1 s

        java.awt.Rectangle after = first.getBounds();
        System.out.printf("应用前 %s → 应用后 %s%n", before, after);
        ok &= Math.abs(after.x) < 1 && Math.abs(after.y) < 1
                && after.width == 321 && after.height == 123;

        // ---- 4. 删除布局文件 → 恢复原布局 ----
        pageFile.delete();
        Thread.sleep(1800);
        java.awt.Rectangle restored = first.getBounds();
        System.out.printf("删除后 %s（应回到默认）%n", restored);
        ok &= restored.equals(before);

        // ---- 5. 窗口导航菜单：合并到已有菜单栏、重复安装不重复 ----
        javax.swing.JFrame probe = new javax.swing.JFrame();
        javax.swing.JMenuBar bar = new javax.swing.JMenuBar();
        bar.add(new javax.swing.JMenu("File"));
        probe.setJMenuBar(bar);
        WindowMenu.install(probe);
        int n1 = bar.getMenuCount();
        WindowMenu.install(probe);
        int n2 = bar.getMenuCount();
        System.out.println("菜单栏项目数：装一次=" + n1 + " 装两次=" + n2 + "（应为 2 和 2）");
        ok &= (n1 == 2) && (n2 == 2) && "File".equals(bar.getMenu(0).getText());
        javax.swing.JMenu nav = bar.getMenu(1);
        System.out.println("导航菜单项数 = " + nav.getItemCount() + "（主控制室 + 分隔符 + 页面）");
        ok &= nav.getItemCount() >= 12;

        System.out.println(ok ? "\nPASS" : "\nFAIL");
        System.exit(ok ? 0 : 1);
    }
}
