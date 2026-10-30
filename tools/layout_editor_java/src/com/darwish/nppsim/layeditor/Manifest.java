package com.darwish.nppsim.layeditor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 游戏导出的布局清单（ui_layout_pages.json）在编辑器里的内存模型。
 *
 * <p>清单由模拟器的 {@code PageLayout.dumpManifest()} 生成，每个窗口一页，
 * 每页带若干个"方块"（面板）以及**递归**的子组件树，所以编辑器能列到任意层级。</p>
 *
 * <p>纯数据，不依赖 Swing，方便用命令行探针直接测。</p>
 */
public final class Manifest {

    /** 一个组件节点（方块也算节点，path 为空串） */
    public static final class Node {
        public final String blockId;
        public final String path;          // "0/2" 相对所属方块；方块本身是 ""
        public final String cls;
        public final String label;
        public final List<Node> children = new ArrayList<>();
        /** 原始位置（清单里导出时的值，用于"恢复默认"） */
        public final int defX, defY, defW, defH;
        public final boolean defVisible;
        /** 编辑中的位置 */
        public int x, y, w, h;
        public boolean visible;
        /** 是否改过（决定写盘时是否落到 children / nodes） */
        public boolean dirty;

        Node(String blockId, String path, String cls, String label,
             int x, int y, int w, int h, boolean visible) {
            this.blockId = blockId;
            this.path = path;
            this.cls = simpleName(cls);
            this.label = cleanLabel(label, this.cls);
            this.defX = x;
            this.defY = y;
            this.defW = w;
            this.defH = h;
            this.defVisible = visible;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.visible = visible;
        }

        public void reset() {
            x = defX;
            y = defY;
            w = defW;
            h = defH;
            visible = defVisible;
            dirty = false;
            for (Node c : children) {
                c.reset();
            }
        }

        /** 深度优先找第一个满足条件的节点 */
        public Node find(String wantPath) {
            if (path.equals(wantPath)) {
                return this;
            }
            for (Node c : children) {
                Node r = c.find(wantPath);
                if (r != null) {
                    return r;
                }
            }
            return null;
        }

        public String display() {
            String name = label.isEmpty() ? cls : label + "  ·" + cls;
            return path.isEmpty() ? "■ " + name : name;
        }

        /**
         * 树里显示的名字。有文字就只显示文字（不拖类名，树列窄、否则会被截断）；
         * 没文字才退回 "#索引 类名"。
         */
        public String treeLabel() {
            String mark = path.isEmpty() ? "■ " : "";
            if (!label.isEmpty()) {
                return mark + label;
            }
            String idx = path.isEmpty() ? ("#" + blockId) : ("#" + path);
            return mark + idx + "  " + cls;
        }

        private static String simpleName(String cls) {
            if (cls == null || cls.isEmpty()) {
                return "Component";
            }
            int i = cls.lastIndexOf('.');
            String s2 = i >= 0 ? cls.substring(i + 1) : cls;
            int j = s2.lastIndexOf('$');           // 内部类 a$b → b
            return j >= 0 ? s2.substring(j + 1) : s2;
        }

        /** 清单里的 label 有时塞的是类名/包名，这种就别当文字显示 */
        private static String cleanLabel(String label, String cls) {
            if (label == null) {
                return "";
            }
            String t = label.trim().replace('\n', ' ').replace('\r', ' ');
            if (t.length() > 40) {
                t = t.substring(0, 40) + "…";
            }
            if (t.startsWith("com.") || t.startsWith("javax.") || t.startsWith("java.")
                    || t.equals(cls) || t.equalsIgnoreCase(cls)) {
                return "";
            }
            return t;
        }
    }

    /** 一个窗口（页） */
    public static final class Page {
        public final String id;
        public final String title;
        public final String file;
        public final int w, h;
        public final List<Node> blocks = new ArrayList<>();

        Page(String id, String title, String file, int w, int h) {
            this.id = id;
            this.title = title;
            this.file = file;
            this.w = Math.max(1, w);
            this.h = Math.max(1, h);
        }

        public Node block(String blockId) {
            for (Node b : blocks) {
                if (b.blockId.equals(blockId)) {
                    return b;
                }
            }
            return null;
        }

        @Override public String toString() {
            return title == null || title.isEmpty() ? id : title;
        }
    }

    public final File dir;
    public final File manifestFile;
    public final List<Page> pages = new ArrayList<>();
    public String generatedAt = "";

    private Manifest(File dir) {
        this.dir = dir;
        this.manifestFile = new File(dir, "ui_layout_pages.json");
    }

    /** 读取清单；文件不存在返回 null */
    public static Manifest load(File dir) throws Exception {
        Manifest m = new Manifest(dir);
        File f = m.manifestFile;
        if (!f.isFile()) {
            return null;
        }
        String text = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
        JSONObject root = new JSONObject(text);
        m.generatedAt = root.optString("generated_at", "");
        JSONArray arr = root.optJSONArray("pages");
        if (arr == null) {
            return m;
        }
        for (int i = 0; i < arr.length(); i++) {
            JSONObject p = arr.optJSONObject(i);
            if (p == null) {
                continue;
            }
            Page page = new Page(p.optString("id", "page" + i), p.optString("title", ""),
                    p.optString("file", p.optString("id", "page") + ".layout.json"),
                    p.optInt("w", 1366), p.optInt("h", 768));
            JSONArray blocks = p.optJSONArray("blocks");
            if (blocks != null) {
                for (int j = 0; j < blocks.length(); j++) {
                    JSONObject b = blocks.optJSONObject(j);
                    if (b == null) {
                        continue;
                    }
                    String id = b.optString("id", String.valueOf(j));
                    Node n = new Node(id, "", b.optString("cls", ""), b.optString("label", ""),
                            b.optInt("x", 0), b.optInt("y", 0),
                            b.optInt("w", 1), b.optInt("h", 1), b.optBoolean("visible", true));
                    readChildren(n, b.optJSONArray("children"), "");
                    page.blocks.add(n);
                }
            }
            m.pages.add(page);
        }
        return m;
    }

    private static void readChildren(Node parent, JSONArray arr, String prefix) {
        if (arr == null) {
            return;
        }
        for (int i = 0; i < arr.length(); i++) {
            JSONObject c = arr.optJSONObject(i);
            if (c == null) {
                continue;
            }
            int idx = c.optInt("i", i);
            String path = prefix.isEmpty() ? String.valueOf(idx) : prefix + "/" + idx;
            Node n = new Node(parent.blockId, path, c.optString("cls", ""), c.optString("label", ""),
                    c.optInt("x", 0), c.optInt("y", 0),
                    c.optInt("w", 1), c.optInt("h", 1), c.optBoolean("visible", true));
            readChildren(n, c.optJSONArray("children"), path);
            parent.children.add(n);
        }
    }

    /**
     * 退化读法：没有 {@code ui_layout_pages.json} 时（例如对着**原版**模拟器用，
     * 它不导出清单），直接从 {@code ui_layout.json} + {@code ui_layout_children.json} 拼一页。
     *
     * 这样即使只有旧模拟器生成的两个文件，也能拖主窗口的面板和它的第一层子组件；
     * 想要任意层级就把模拟器换成会导出清单的版本，或在游戏里开一次窗口让清单刷新。
     */
    public static Manifest fromLayoutFiles(File dir) throws Exception {
        File layout = new File(dir, "ui_layout.json");
        File kidsFile = new File(dir, "ui_layout_children.json");
        if (!layout.isFile() && !kidsFile.isFile()) {
            return null;
        }
        Manifest m = new Manifest(dir);

        int cw = 1366, chh = 768;
        java.util.List<Node> blocks = new java.util.ArrayList<>();
        if (layout.isFile()) {
            JSONObject root = new JSONObject(new String(
                    Files.readAllBytes(layout.toPath()), StandardCharsets.UTF_8));
            JSONObject canvas = root.optJSONObject("canvas");
            if (canvas != null) {
                cw = Math.max(200, canvas.optInt("w", cw));
                chh = Math.max(200, canvas.optInt("h", chh));
            }
            JSONObject panels = root.optJSONObject("panels");
            if (panels != null) {
                for (java.util.Iterator<String> it = panels.keys(); it.hasNext();) {
                    String id = it.next();
                    JSONObject b = panels.optJSONObject(id);
                    if (b == null) {
                        continue;
                    }
                    Node n = new Node(id, "", "", L.t("mf.panel") + id,
                            b.optInt("x", 0), b.optInt("y", 0),
                            Math.max(1, b.optInt("w", 10)), Math.max(1, b.optInt("h", 10)),
                            b.optBoolean("visible", true));
                    readLegacyChildren(n, b.optJSONObject("children"));
                    blocks.add(n);
                }
            }
        }
        if (blocks.isEmpty() && kidsFile.isFile()) {
            JSONObject root = new JSONObject(new String(
                    Files.readAllBytes(kidsFile.toPath()), StandardCharsets.UTF_8));
            JSONObject panels = root.optJSONObject("panels");
            if (panels != null) {
                for (java.util.Iterator<String> it = panels.keys(); it.hasNext();) {
                    String id = it.next();
                    Node n = new Node(id, "", "", L.t("mf.panel") + id, 0, 0, 10, 10, true);
                    JSONObject holder = panels.optJSONObject(id);
                    if (holder != null) {
                        readLegacyChildren(n, holder.optJSONObject("children"));
                    }
                    blocks.add(n);
                }
            }
        }
        if (blocks.isEmpty()) {
            return null;
        }
        Page page = new Page("main", L.t("mf.mainPage"), "ui_layout.json", cw, chh);
        page.blocks.addAll(blocks);
        m.pages.add(page);
        return m;
    }

    /** 旧格式 children：{"0": {"cls":..,"x":..,"y":..,"w":..,"h":..}} 或 {"children":[...]} */
    private static void readLegacyChildren(Node parent, JSONObject kids) {
        if (kids == null) {
            return;
        }
        JSONArray arr = kids.optJSONArray("children");
        if (arr != null) {
            readChildren(parent, arr, "");
            return;
        }
        for (java.util.Iterator<String> it = kids.keys(); it.hasNext();) {
            String key = it.next();
            JSONObject c = kids.optJSONObject(key);
            if (c == null) {
                continue;
            }
            Node n = new Node(parent.blockId, key, c.optString("cls", ""), c.optString("text", ""),
                    c.optInt("x", 0), c.optInt("y", 0),
                    Math.max(1, c.optInt("w", 10)), Math.max(1, c.optInt("h", 10)), true);
            parent.children.add(n);
        }
    }

    /** 找清单目录：CLI 参数 / -Ddir / 当前目录 / jar 所在目录及其上级 */
    public static File[] searchDirs(String[] args, File jarDir) {
        List<File> out = new ArrayList<>();
        String prop = System.getProperty("dir");
        if (prop != null && !prop.isEmpty()) {
            out.add(new File(prop));
        }
        for (String a : args) {
            if (!a.startsWith("-")) {
                out.add(new File(a));
            }
        }
        out.add(new File(System.getProperty("user.dir", ".")));
        if (jarDir != null) {
            out.add(jarDir);
            File parent = jarDir.getParentFile();
            if (parent != null) {
                out.add(parent);
                File grand = parent.getParentFile();
                if (grand != null) {
                    out.add(grand);
                }
            }
        }
        return out.toArray(new File[0]);
    }

    public static Manifest find(String[] args) throws Exception {
        File self = selfDir();
        for (File d : searchDirs(args, self)) {
            if (d == null) {
                continue;
            }
            if (new File(d, "ui_layout_pages.json").isFile()) {
                Manifest m = load(d);
                if (m != null) {
                    return m;
                }
            }
        }
        return null;
    }

    static File selfDir() {
        try {
            File f = new File(Manifest.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            return f.isFile() ? f.getParentFile() : f;
        } catch (Exception e) {
            return null;
        }
    }
}
