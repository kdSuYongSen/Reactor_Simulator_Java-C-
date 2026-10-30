package com.darwish.nppsim;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONObject;

/**
 * 外部布局文件（ui_layout.json）支持 —— 由配套 Python 布局编辑器生成。
 *
 * 配套工具：tools/layout_editor/layout_editor.py
 *   （把主窗口每个面板简化为彩色方块，拖动调整位置/大小后自动保存本文件，
 *      Java 侧每秒轮询，编辑后约 1 秒内界面自动跟随更新）
 *
 * 搜索顺序（取第一个存在的文件）：
 *   1. 当前工作目录 / ui_layout.json
 *   2. jar 所在目录 / ui_layout.json
 *   3. jar 所在目录的上级（jar 在 target/ 时即源码工程目录）/ ui_layout.json
 *
 * 文件格式（UTF-8）：
 * <pre>
 * {
 *   "mode": "absolute",
 *   "panels": {
 *     "rod_grid": {"x": 117, "y": 383, "w": 519, "h": 315, "content_scale": 1.2},
 *     "az_panel": {"x": 13, "y": 191, "w": 258, "h": 132,
 *                  "children": {"0": {"cls": "JButton", "x": 12, "y": 4}, ...}},
 *     ...
 *   }
 * }
 * </pre>
 * content_scale 为可选的"内容缩放"倍率（编辑器滑块，1.0 = 仅按方块尺寸自动缩放）：
 * 它在"尺寸自动缩放"的基础上再乘一个系数，用于单独放大/缩小面板内的按键和文字。
 * children 为可选的"面板内子组件（小方块）"绝对位置（面板坐标），键为子组件索引：
 * 存在时该面板内部切换为绝对布局、按坐标摆放列出的子组件；删除后恢复 GroupLayout。
 * mode 缺省或不为 "absolute" 时视为"恢复默认 GroupLayout 布局"。
 */
final class UiLayoutFile {

    private UiLayoutFile() {
    }

    /** 单个面板的布局项 */
    static final class Entry {
        final int x, y, w, h;
        /** 内容缩放倍率（缺失时为 1.0） */
        final double contentScale;
        /** 该方块是否可见（缺失时为 true）。设置→工具 里可以单独隐藏某个方块 */
        final boolean visible;
        /** 面板内子组件（"小方块"）的绝对位置：索引字符串 → Child；无则为 null */
        final Map<String, Child> children;
        /** 更深层组件：索引路径（"0/2"）→ Child；无则为 null */
        final Map<String, Child> nodes;

        Entry(int x, int y, int w, int h, double contentScale, boolean visible,
              Map<String, Child> children, Map<String, Child> nodes) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.contentScale = contentScale;
            this.visible = visible;
            this.children = children;
            this.nodes = nodes;
        }
    }

    /** 面板内子组件（按键/数据显示）的绝对位置（面板坐标） */
    static final class Child {
        /** 组件类型（用于校验索引未因代码变化而错位；为空则不校验） */
        final String cls;
        final int x, y;
        /** 宽高（0 = 沿用组件当前尺寸） */
        final int w, h;
        /** 是否可见（缺失时为 true） */
        final boolean visible;

        Child(String cls, int x, int y) {
            this(cls, x, y, 0, 0, true);
        }

        Child(String cls, int x, int y, int w, int h, boolean visible) {
            this.cls = cls;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.visible = visible;
        }
    }

    /** 子组件清单文件（由游戏运行时导出、供布局编辑器读取），与布局文件同目录 */
    static File childrenPath() {
        File layout = writePath();
        File dir = layout.getParentFile();
        return new File(dir != null ? dir : new File("."), "ui_layout_children.json");
    }

    /** 当前生效的布局文件；三处都不存在时返回 null（表示使用默认 GroupLayout） */
    static File locate() {
        File cwd = new File("ui_layout.json");
        if (cwd.isFile()) {
            return cwd;
        }
        try {
            java.net.URI uri = UiLayoutFile.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            File base = new File(uri);
            File dir = base.isFile() ? base.getParentFile() : base;
            if (dir != null) {
                File f = new File(dir, "ui_layout.json");
                if (f.isFile()) {
                    return f;
                }
                File parent = dir.getParentFile();
                if (parent != null) {
                    f = new File(parent, "ui_layout.json");
                    if (f.isFile()) {
                        return f;
                    }
                }
            }
        } catch (Exception ignore) {
            // 拿不到 jar 路径时仅依赖工作目录
        }
        return null;
    }

    /**
     * 布局编辑器的推荐写入路径：
     *   · 若已有生效文件 → 沿用该文件（保证读写同一个）；
     *   · 否则 → jar 目录的上级（即源码工程目录）/ui_layout.json；
     *   · 兜底 → 当前工作目录/ui_layout.json。
     */
    static File writePath() {
        File current = locate();
        if (current != null) {
            return current;
        }
        try {
            java.net.URI uri = UiLayoutFile.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            File base = new File(uri);
            File dir = base.isFile() ? base.getParentFile() : base;
            if (dir != null && dir.getParentFile() != null) {
                return new File(dir.getParentFile(), "ui_layout.json");
            }
            if (dir != null) {
                return new File(dir, "ui_layout.json");
            }
        } catch (Exception ignore) {
            // 拿不到 jar 路径时用工作目录
        }
        return new File("ui_layout.json");
    }

    /**
     * 解析布局文件。
     *
     * @return null 表示需要恢复 GroupLayout（mode != "absolute"）；
     *         否则返回 key → Entry（坐标/尺寸 + 内容缩放倍率）
     * @throws IOException 文件读取失败或 JSON 非法
     */
    /** 该布局文件是否带当前版本号（每页布局 v2 起才有；没有的是旧"滚动条当方块"时代的） */
    static boolean hasVersion(File f) {
        try {
            String text = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            return text.contains("\"layoutVersion\"");
        } catch (Exception e) {
            return false;
        }
    }

    static Map<String, Entry> parse(File f) throws IOException {
        String text = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
        JSONObject root = new JSONObject(text);
        String mode = root.optString("mode", "group");
        if (!"absolute".equalsIgnoreCase(mode)) {
            return null;
        }
        JSONObject panels = root.optJSONObject("panels");
        Map<String, Entry> out = new LinkedHashMap<>();
        if (panels != null) {
            for (java.util.Iterator<String> it = panels.keys(); it.hasNext();) {
                String key = it.next();
                JSONObject b = panels.optJSONObject(key);
                if (b == null) {
                    continue;
                }
                int x = b.optInt("x", 0);
                int y = b.optInt("y", 0);
                int w = b.optInt("w", 0);
                int h = b.optInt("h", 0);
                if (w <= 0 || h <= 0) {
                    continue;      // 没有尺寸的项视为无效（由编辑器写入的一定带 w/h）
                }
                boolean visible = b.optBoolean("visible", true);
                double scale = b.optDouble("content_scale", 1.0);
                if (!(scale > 0) || Double.isNaN(scale)) {
                    scale = 1.0;
                }
                Map<String, Child> children = null;
                JSONObject kids = b.optJSONObject("children");
                if (kids != null && kids.length() > 0) {
                    children = new LinkedHashMap<>();
                    for (java.util.Iterator<String> kit = kids.keys(); kit.hasNext();) {
                        String idx = kit.next();
                        JSONObject c = kids.optJSONObject(idx);
                        if (c == null) {
                            continue;
                        }
                        children.put(idx, new Child(c.optString("cls", ""),
                                c.optInt("x", 0), c.optInt("y", 0),
                                c.optInt("w", 0), c.optInt("h", 0),
                                c.optBoolean("visible", true)));
                    }
                    if (children.isEmpty()) {
                        children = null;
                    }
                }
                Map<String, Child> nodes = null;
                JSONObject deep = b.optJSONObject("nodes");
                if (deep != null && deep.length() > 0) {
                    nodes = new LinkedHashMap<>();
                    for (java.util.Iterator<String> nit = deep.keys(); nit.hasNext();) {
                        String path = nit.next();
                        JSONObject c = deep.optJSONObject(path);
                        if (c == null) {
                            continue;
                        }
                        nodes.put(path, new Child(c.optString("cls", ""),
                                c.optInt("x", 0), c.optInt("y", 0),
                                c.optInt("w", 0), c.optInt("h", 0),
                                c.optBoolean("visible", true)));
                    }
                    if (nodes.isEmpty()) {
                        nodes = null;
                    }
                }
                out.put(key, new Entry(x, y, w, h, scale, visible, children, nodes));
            }
        }
        return out;
    }
}
