package com.darwish.nppsim.layeditor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

/**
 * 把编辑器里的改动写成模拟器能读的布局文件。
 *
 * <p>和模拟器 {@code PageLayout.savePage()} 完全同一套格式（这样两边可以互相读）：</p>
 *
 * <pre>
 * {
 *   "mode": "absolute",
 *   "layoutVersion": 2,
 *   "panels": {
 *     "1": {"x":..,"y":..,"w":..,"h":..,"visible":true,
 *           "children": {"0": {"cls":"JPanel","x":..,"y":..,"w":..,"h":..,"visible":true}},
 *           "nodes":    {"0/2": {"cls":"JButton", ...}}}          // 任意深度
 *   }
 * }
 * </pre>
 *
 * <p>只写"被改过"的组件（{@link Manifest.Node#dirty}），没动过的保持模拟器的默认布局——
 * 这样文件小、也不会把你没碰过的面板钉死。</p>
 */
public final class LayoutWriter {

    public static final String GENERATOR = "RBMK-1500 LayoutEditor (java)";

    private LayoutWriter() {
    }

    /** 该页对应哪个文件：主窗口是 ui_layout.json，其余是 &lt;pageId&gt;.layout.json */
    public static File targetFile(Manifest.Page page, File dir) {
        if ("main".equals(page.id)) {
            return new File(dir, "ui_layout.json");
        }
        String name = page.file == null || page.file.isEmpty()
                ? page.id + ".layout.json" : page.file;
        return new File(dir, name);
    }

    /** 生成 JSON 文本（不写盘，便于自检/测试） */
    public static String buildJson(Manifest.Page page) {
        JSONObject root = new JSONObject();
        root.put("mode", "absolute");
        // v2：方块 id 语义与旧版不同（旧版把滚动面板的视口/滚动条当方块），模拟器按这个版本号取舍
        root.put("layoutVersion", 2);
        root.put("generator", GENERATOR);
        root.put("page", page.id);
        root.put("saved_at", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new java.util.Date()));
        root.put("canvas", new JSONObject().put("w", page.w).put("h", page.h));

        JSONObject panels = new JSONObject();
        for (Manifest.Node block : page.blocks) {
            JSONObject b = new JSONObject();
            b.put("x", block.x);
            b.put("y", block.y);
            b.put("w", Math.max(1, block.w));
            b.put("h", Math.max(1, block.h));
            b.put("visible", block.visible);

            JSONObject kids = new JSONObject();
            JSONObject nodes = new JSONObject();
            collect(block, kids, nodes);
            if (kids.length() > 0) {
                b.put("children", kids);
            }
            if (nodes.length() > 0) {
                b.put("nodes", nodes);
            }
            panels.put(block.blockId, b);
        }
        root.put("panels", panels);
        return root.toString(2) + "\n";
    }

    /** 收集 dirty 的节点：一层写 children，更深写 nodes（路径 "0/2"） */
    private static void collect(Manifest.Node node, JSONObject kids, JSONObject nodes) {
        for (Manifest.Node c : node.children) {
            if (c.dirty) {
                JSONObject o = new JSONObject();
                o.put("cls", c.cls);
                o.put("x", c.x);
                o.put("y", c.y);
                o.put("w", Math.max(1, c.w));
                o.put("h", Math.max(1, c.h));
                o.put("visible", c.visible);
                if (c.path.indexOf('/') < 0) {
                    kids.put(c.path, o);
                } else {
                    nodes.put(c.path, o);
                }
            }
            collect(c, kids, nodes);
        }
    }

    /** 有没有任何改动 */
    public static boolean hasChanges(Manifest.Page page) {
        for (Manifest.Node b : page.blocks) {
            if (anyDirty(b)) {
                return true;
            }
        }
        return false;
    }

    private static boolean anyDirty(Manifest.Node n) {
        if (n.dirty) {
            return true;
        }
        for (Manifest.Node c : n.children) {
            if (anyDirty(c)) {
                return true;
            }
        }
        return false;
    }

    /** 原子写盘：先写 .tmp 再替换，模拟器 1 s 轮询不会读到半截文件 */
    public static File save(Manifest.Page page, File dir) throws Exception {
        File out = targetFile(page, dir);
        File tmp = new File(out.getAbsolutePath() + ".tmp");
        Files.write(tmp.toPath(), buildJson(page).getBytes(StandardCharsets.UTF_8));
        Files.move(tmp.toPath(), out.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return out;
    }

    /** 删掉该页的布局文件（模拟器随即恢复默认布局） */
    public static boolean remove(Manifest.Page page, File dir) {
        File out = targetFile(page, dir);
        return !out.isFile() || out.delete();
    }

    /** 列出该页当前已存在的布局文件（存在说明有自定义布局） */
    public static File existing(Manifest.Page page, File dir) {
        File f = targetFile(page, dir);
        return f.isFile() ? f : null;
    }

    /** 已 dirty 的节点（调试/状态栏用） */
    public static List<String> dirtyPaths(Manifest.Page page) {
        List<String> out = new ArrayList<>();
        for (Manifest.Node b : page.blocks) {
            gather(b, out);
        }
        return out;
    }

    private static void gather(Manifest.Node n, List<String> out) {
        if (n.dirty) {
            out.add(n.blockId + (n.path.isEmpty() ? "" : " / " + n.path));
        }
        for (Manifest.Node c : n.children) {
            gather(c, out);
        }
    }
}
