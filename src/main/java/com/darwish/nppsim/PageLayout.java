package com.darwish.nppsim;

import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 页面级布局（2026-10-18）——把"外部布局编辑器"从只支持主窗口扩展到**每个窗口**。
 *
 * 每个窗口在构造时调用 {@link #installAuto(JFrame, String)}：
 *   1. 自动把内容面板的直接子组件当成"方块"（NetBeans 窗体正好是一堆面板铺在 contentPane 上）；
 *   2. 首次布局完成后记录默认坐标（作为没被编辑过的方块的兜底位置）；
 *   3. 若同级目录存在 {@code <页面id>.layout.json}（格式与 ui_layout.json 完全相同），
 *      就把内容面板切成绝对布局并按文件摆放，同时用 PanelContentScaler 缩放内部按键/文字；
 *   4. 每秒轮询该文件，编辑后 1 秒内热更新；文件被删除或 mode != absolute → 恢复原 GroupLayout；
 *   5. 把所有页面的方块清单导出到 {@code ui_layout_pages.json}，供布局编辑器列出"页面"。
 *
 * 主窗口（UI）自己的布局由 UI#initLayoutSync 管理（它还支持"展开内部小方块"），
 * 这里只以 {@link #registerExternal} 登记清单，不接管它的布局。
 */
final class PageLayout {

    /** 布局文件所在目录：与 ui_layout.json 同级 */
    private static final List<Page> PAGES = new ArrayList<>();
    /** 供各页面共用的内容缩放器（内部按组件缓存原始字号） */
    private static final PanelContentScaler SCALER = new PanelContentScaler();
    /** 方块内部被切成绝对布局后，原来的 LayoutManager 与各子组件默认 bounds */
    /** 被"布局工具"接管过布局的容器 → 原 LayoutManager（身份比较，任意深度） */
    private static final Map<Container, LayoutManager> takenLayout =
            new java.util.IdentityHashMap<>();
    /** 同上：接管前各子组件的默认位置（恢复默认时用） */
    private static final Map<Container, java.awt.Rectangle[]> takenFallback =
            new java.util.IdentityHashMap<>();

    private static javax.swing.Timer pollTimer;
    private static javax.swing.Timer manifestTimer;
    private static boolean manifestDumped = false;

    private PageLayout() {
    }

    /** 一个可编辑的页面 */
    static final class Page {
        final String id;
        final String title;
        final JFrame frame;
        final Map<String, JComponent> blocks = new LinkedHashMap<>();
        final Map<String, String> labels = new LinkedHashMap<>();
        final File file;
        /** false = 只导出清单，布局由窗口自己管（主窗口） */
        final boolean managed;
        /** 方块所在的容器（通常不是 contentPane：NetBeans 窗体的面板挂在中间层容器上） */
        Container host;
        LayoutManager originalLayout;
        Map<String, java.awt.Rectangle> fallback = new LinkedHashMap<>();
        long stamp = -1;
        boolean active;

        Page(String id, String title, JFrame frame, File file, boolean managed) {
            this.id = id;
            this.title = title;
            this.frame = frame;
            this.file = file;
            this.managed = managed;
        }
    }

    static File dir() {
        File layout = UiLayoutFile.writePath();
        File parent = layout.getParentFile();
        return parent != null ? parent : new File(".");
    }

    // ------------------------------------------------------------------
    // 安装
    // ------------------------------------------------------------------

    /** 自动发现方块并接管该页布局 */
    static void installAuto(JFrame frame, String pageId) {
        // 标题用窗口**自己的标题**（原版名称），不要拿 pageId 当显示名
        String title = frame.getTitle();
        if (title == null || title.trim().isEmpty()) {
            title = pageId;
        }
        installAuto(frame, pageId, title, null);
    }

    static void installAuto(JFrame frame, String pageId, String title, Map<String, String> labels) {
        // 注意：本方法通常在窗口构造的中途调用（内容面板还没装满），
        // 所以方块枚举推迟到 350 ms 后的初始化阶段再做，见 register()。
        Page page = new Page(pageId, title, frame,
                new File(dir(), pageId + ".layout.json"), true);
        if (labels != null) {
            page.labels.putAll(labels);
        }
        register(page);
    }

    /**
     * 登记一个"自己管布局"的页面（主窗口）：只用于导出清单，方便编辑器列出它的方块。
     * 传进来的 blocks 必须与窗口实际摆放的组件一致。
     */
    static void registerExternal(JFrame frame, String pageId, String title,
                                 Map<String, JComponent> blocks, Map<String, String> labels) {
        Page page = new Page(pageId, title, frame,
                new File(dir(), pageId + ".layout.json"), false);
        page.blocks.putAll(blocks);
        if (labels != null) {
            page.labels.putAll(labels);
        }
        register(page);
    }

    private static void register(Page page) {
        boolean replaced = false;
        for (int i = 0; i < PAGES.size(); i++) {
            if (PAGES.get(i).id.equals(page.id)) {
                PAGES.set(i, page);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            PAGES.add(page);
        }
        /* 2026-10-26：**替换**同一 pageId 时也必须重新初始化。
         * 旧实现 replace 后直接 return，新 Page 对象永远等不到那个 350 ms 定时器 ——
         * 结果是 blocks=0、host=null，工具页里该窗口"什么都选不到"。
         * （实测：NPPSim 构造时会先建一个同 id 窗口，用户再开一个就把登记项换成空页面。） */
        javax.swing.SwingUtilities.invokeLater(() -> {
            javax.swing.Timer once = new javax.swing.Timer(350, ev -> {
                if (page.blocks.isEmpty() && page.managed) {
                    page.blocks.putAll(discover(page.frame));
                }
                if (Boolean.getBoolean("layout.debug")) {
                    System.out.println("[layout] init " + page.id + "@"
                            + Integer.toHexString(System.identityHashCode(page))
                            + " blocks=" + page.blocks.size()
                            + " frame=" + Integer.toHexString(System.identityHashCode(page.frame))
                            + " displayable=" + page.frame.isDisplayable());
                }
                initialize(page);
                ((javax.swing.Timer) ev.getSource()).stop();
            });
            once.setRepeats(false);
            once.start();
        });
        startPolling();
        scheduleManifest();
    }

    private static void initialize(Page page) {
        // 方块挂在哪个容器上，就把那个容器切成绝对布局（不能直接改 contentPane：
        // NetBeans 窗体与手写窗口之间常常隔着一层 JPanel，改外层不起作用）
        page.host = null;
        for (JComponent c : page.blocks.values()) {
            if (c != null && c.getParent() != null) {
                page.host = c.getParent();
                break;
            }
        }
        if (page.host == null) {
            page.host = page.frame.getContentPane();
        }
        page.originalLayout = page.host.getLayout();
        page.fallback.clear();
        for (Map.Entry<String, JComponent> e : page.blocks.entrySet()) {
            Component c = e.getValue();
            if (c != null && c.getWidth() > 0 && c.getHeight() > 0) {
                page.fallback.put(e.getKey(), new java.awt.Rectangle(
                        c.getX(), c.getY(), c.getWidth(), c.getHeight()));
            }
        }
        if (page.managed) {
            page.stamp = -1;          // 强制重读一次（别被前面的轮询抢先记过 stamp）
            refresh(page);
        }
        scheduleManifest();
    }

    /**
     * 自动发现方块：内容面板的直接子组件。
     * 若内容面板只有一个子容器（有些窗体先包一层 JPanel），继续往下钻一层。
     */
    private static Map<String, JComponent> discover(JFrame frame) {
        Container c = frame.getContentPane();
        /* 2026-10-26：旧实现只看"直接子组件"，遇到 JScrollPane 就把
         * viewport / 垂直滚动条 / 水平滚动条当成三个"方块"列出来（实测各窗口都只有 3 项，
         * 真正的面板一个都进不去，所以"调不到内部按键"）。
         * 现在逐层剥：JScrollPane → 视口 → 视图；单一子容器也继续下钻。 */
        for (int guard = 0; guard < 6; guard++) {
            if (c instanceof javax.swing.JScrollPane) {
                javax.swing.JViewport vp = ((javax.swing.JScrollPane) c).getViewport();
                Component view = vp == null ? null : vp.getView();
                if (!(view instanceof Container) || ((Container) view).getComponentCount() == 0) {
                    break;
                }
                c = (Container) view;
                continue;
            }
            if (c.getComponentCount() == 1 && c.getComponent(0) instanceof Container
                    && ((Container) c.getComponent(0)).getComponentCount() > 0) {
                c = (Container) c.getComponent(0);
                continue;
            }
            break;
        }
        Map<String, JComponent> out = new LinkedHashMap<>();
        Component[] children = c.getComponents();
        for (int i = 0; i < children.length; i++) {
            if (!(children[i] instanceof JComponent)) {
                continue;
            }
            Component comp = children[i];
            if (comp instanceof javax.swing.JScrollBar || comp instanceof javax.swing.JViewport) {
                continue;                       // 结构件不算面板
            }
            out.put(String.valueOf(i), (JComponent) comp);
        }
        return out;
    }

    // ------------------------------------------------------------------
    // 应用 / 轮询
    // ------------------------------------------------------------------

    private static void startPolling() {
        if (pollTimer != null) {
            return;
        }
        pollTimer = new javax.swing.Timer(1000, ev -> {
            for (Page p : PAGES) {
                if (p.managed) {
                    refresh(p);
                }
            }
        });
        pollTimer.start();
    }

    private static void refresh(Page page) {
        /* 方块还没枚举完（窗口构造中途 / 350 ms 定时器未到）就先别读文件：
         * 否则会把文件"提前消费"掉——stamp 记上了，等真正初始化时反而因为
         * stamp 相同而跳过 apply，结果整个布局一个都没应用（实测过）。 */
        if (page.blocks.isEmpty()) {
            return;
        }
        File f = page.file;
        long stamp = f.isFile() ? (f.lastModified() * 31 + f.length()) : -1;
        if (stamp == page.stamp) {
            return;
        }
        page.stamp = stamp;
        try {
            Map<String, UiLayoutFile.Entry> entries = null;
            if (f.isFile()) {
                if (UiLayoutFile.hasVersion(f)) {
                    entries = UiLayoutFile.parse(f);
                } else {
                    System.out.println("[Layout] " + f.getName()
                            + " 是旧版（方块 id 含义已变），忽略并恢复默认布局");
                }
            }
            apply(page, entries);
        } catch (Exception ex) {
            System.out.println("[Layout] " + page.id + " 布局文件读取失败（忽略本次）: " + ex);
        }
    }

    private static void apply(Page page, Map<String, UiLayoutFile.Entry> entries) {
        Container root = page.host != null ? page.host : page.frame.getContentPane();
        if (entries == null) {                 // 没有文件 / mode != absolute → 恢复默认
            if (page.active) {
                // 先复位被缩放过的字号/min-pref-max，否则原来的 GroupLayout 会按缩小后的
                // 首选尺寸重新摆放，看起来"恢复不了"（实测 212×265 会停在 162×205）
                SCALER.resetAll();
                root.setLayout(page.originalLayout);
                root.revalidate();
                root.repaint();
                page.active = false;
            }
            return;
        }
        if (!page.active) {
            root.setLayout(null);
            page.active = true;
            // 文件里没列出的方块：回到默认坐标
            for (Map.Entry<String, java.awt.Rectangle> e : page.fallback.entrySet()) {
                if (!entries.containsKey(e.getKey())) {
                    JComponent c = page.blocks.get(e.getKey());
                    if (c != null) {
                        c.setBounds(e.getValue());
                    }
                }
            }
        }
        if (Boolean.getBoolean("layout.debug")) {
            System.out.println("[layout] apply " + page.id + " entries=" + entries.size()
                    + " blocks=" + page.blocks.keySet());
        }
        for (Map.Entry<String, UiLayoutFile.Entry> e : entries.entrySet()) {
            JComponent c = page.blocks.get(e.getKey());
            if (c == null) {
                if (Boolean.getBoolean("layout.debug")) {
                    System.out.println("[layout]   方块 " + e.getKey() + " 在运行时不存在，跳过");
                }
                continue;
            }
            UiLayoutFile.Entry b = e.getValue();
            c.setBounds(b.x, b.y, b.w, b.h);
            c.setVisible(b.visible);
            java.awt.Rectangle design = page.fallback.get(e.getKey());
            if (design != null && design.width > 0 && design.height > 0) {
                double s = PanelContentScaler.scaleFor(design.width, design.height, b.w, b.h);
                s = PanelContentScaler.combine(s, b.contentScale);
                SCALER.apply(c, s);
            }
            /* 方块内部：先 children（第一层，旧格式），再 nodes（任意深度，按路径深度排序
             * 保证父容器先接管布局、再摆它里面的孩子） */
            if (b.children != null) {
                for (Map.Entry<String, UiLayoutFile.Child> k : b.children.entrySet()) {
                    int idx;
                    try {
                        idx = Integer.parseInt(k.getKey());
                    } catch (NumberFormatException ex) {
                        continue;
                    }
                    java.awt.Component child = childAt(c, idx);
                    if (child != null) {
                        // 旧工具写的 children 只有坐标没有尺寸（w/h=0）→ 保持原尺寸，只挪位置
                        applyNode(page, child, new java.awt.Rectangle(
                                k.getValue().x, k.getValue().y,
                                k.getValue().w > 0 ? k.getValue().w : child.getWidth(),
                                k.getValue().h > 0 ? k.getValue().h : child.getHeight()),
                                k.getValue().visible);
                    }
                }
            }
            if (Boolean.getBoolean("layout.debug")) {
                System.out.println("[layout] " + page.id + " 方块 " + e.getKey()
                        + " children=" + (b.children == null ? 0 : b.children.size())
                        + " nodes=" + (b.nodes == null ? 0 : b.nodes.size()));
            }
            if (b.nodes != null) {
                java.util.List<String> paths = new java.util.ArrayList<>(b.nodes.keySet());
                paths.sort((x, y) -> {
                    int dx = x.split("/").length, dy = y.split("/").length;
                    return dx != dy ? Integer.compare(dx, dy) : x.compareTo(y);
                });
                for (String path : paths) {
                    UiLayoutFile.Child k = b.nodes.get(path);
                    java.awt.Component t = resolvePath(c, path);
                    if (t != null) {
                        applyNode(page, t, new java.awt.Rectangle(
                                k.x, k.y,
                                k.w > 0 ? k.w : t.getWidth(),
                                k.h > 0 ? k.h : t.getHeight()), k.visible);
                        if (Boolean.getBoolean("layout.debug")) {
                            System.out.println("[layout]   节点 " + path + " → "
                                    + t.getClass().getSimpleName() + " " + t.getBounds());
                        }
                    } else if (Boolean.getBoolean("layout.debug")) {
                        System.out.println("[layout]   节点 " + path + " 解析失败");
                    }
                }
            }
        }
        root.revalidate();
        root.repaint();
    }

    // ------------------------------------------------------------------
    // 设置 → 工具：逐窗口、逐组件的编辑接口
    // ------------------------------------------------------------------

    /** 已注册的页面（供设置里的工具页列出） */
    static List<Page> pages() {
        return new ArrayList<>(PAGES);
    }

    /** 页面显示名 = 窗口自己的标题（没有标题时退回 id） */
    static String titleOf(Page page) {
        String t = page.frame != null ? page.frame.getTitle() : null;
        if (t == null || t.trim().isEmpty()) {
            t = page.title;
        }
        return t == null ? page.id : t;
    }

    static Container hostOf(Page page) {
        return page.host != null ? page.host : page.frame.getContentPane();
    }

    /** 把页面切成绝对布局（幂等）；返回 true 表示这次真的切了 */
    static boolean ensureAbsolute(Page page) {
        if (page.active) {
            return false;
        }
        Container root = hostOf(page);
        page.originalLayout = root.getLayout();
        recordFallback(page);
        root.setLayout(null);
        page.active = true;
        for (Map.Entry<String, JComponent> e : page.blocks.entrySet()) {
            JComponent c = e.getValue();
            if (c == null) {
                continue;
            }
            java.awt.Rectangle fb = page.fallback.get(e.getKey());
            if (fb != null) {
                c.setBounds(fb);
            }
        }
        root.revalidate();
        return true;
    }

    private static void recordFallback(Page page) {
        page.fallback.clear();
        for (Map.Entry<String, JComponent> e : page.blocks.entrySet()) {
            JComponent c = e.getValue();
            if (c != null && c.getWidth() > 0 && c.getHeight() > 0) {
                page.fallback.put(e.getKey(), new java.awt.Rectangle(
                        c.getX(), c.getY(), c.getWidth(), c.getHeight()));
            }
        }
    }

    /** 设置某个方块的位置/尺寸/可见性（立即生效，不写盘） */
    static void setBlock(Page page, String id, java.awt.Rectangle r, boolean visible) {
        JComponent c = page.blocks.get(id);
        if (c == null) {
            return;
        }
        ensureAbsolute(page);
        c.setBounds(r);
        c.setVisible(visible);
        hostOf(page).revalidate();
        hostOf(page).repaint();
    }

    /** 方块内的直接子组件（供树形选择器枚举；不改布局） */
    static java.util.Map<Integer, JComponent> childrenOf(Page page, String id) {
        java.util.Map<Integer, JComponent> out = new java.util.LinkedHashMap<>();
        JComponent panel = page.blocks.get(id);
        if (panel == null) {
            return out;
        }
        java.awt.Component[] comps = panel.getComponents();
        for (int i = 0; i < comps.length; i++) {
            if (comps[i] instanceof JComponent) {
                out.put(i, (JComponent) comps[i]);
            }
        }
        return out;
    }

    /** 方块内第 index 个子组件（任意类型，按键/标签都算） */
    static java.awt.Component childAt(JComponent block, int index) {
        if (block == null) {
            return null;
        }
        java.awt.Component[] comps = block.getComponents();
        return (index < 0 || index >= comps.length) ? null : comps[index];
    }

    /**
     * 把某个容器切成绝对布局（幂等）：先按它原来的布局算好各子组件位置再接管，
     * 这样"只动选中的那一个，兄弟组件保持原位"。
     */
    private static void makeAbsoluteContainer(Container c) {
        if (c == null || takenLayout.containsKey(c)) {
            return;
        }
        c.doLayout();
        java.awt.Component[] comps = c.getComponents();
        java.awt.Rectangle[] fb = new java.awt.Rectangle[comps.length];
        for (int i = 0; i < comps.length; i++) {
            fb[i] = comps[i].getBounds();
        }
        takenLayout.put(c, c.getLayout());
        takenFallback.put(c, fb);
        c.setLayout(null);
    }

    /**
     * 任意层级组件：设位置 / 尺寸 / 可见性。
     *
     * 父链上凡是还没被接管的容器逐个接管（从里到外），因此既能动顶层面板，
     * 也能动"面板 → 子面板 → 按键"这种三层嵌套里的按键（旧接口只能动第一层）。
     */
    static void applyNode(Page page, java.awt.Component target, java.awt.Rectangle r,
                          boolean visible) {
        if (page == null || target == null) {
            return;
        }
        ensureAbsolute(page);
        placeNode(target, hostOf(page), r, visible);
        Container host = hostOf(page);
        host.revalidate();
        host.repaint();
    }

    /**
     * 不依赖 Page 的"按坐标摆放任意组件"：父链上未接管的容器逐个接管（幂等）。
     *
     * 主窗口（{@link UI}）自己管布局、不走 Page，但同样需要按 {@code nodes} 摆深层组件，
     * 所以把这段逻辑单独暴露出来给它复用，避免出现第二份实现。
     */
    static void placeNode(java.awt.Component target, Container stopAt,
                          java.awt.Rectangle r, boolean visible) {
        if (target == null) {
            return;
        }
        java.awt.Component node = target;
        while (node != null) {
            Container parent = node.getParent();
            if (parent == null || parent == stopAt) {
                break;                       // stopAt 及其以上由窗口自己管，不接管
            }
            makeAbsoluteContainer(parent);
            node = parent;
        }
        target.setBounds(r);
        target.setVisible(visible);
        Container p = target.getParent();
        if (p != null) {
            p.revalidate();
            p.repaint();
        }
    }

    /** 兼容旧接口：按索引设置方块的直接子组件 */
    static void setChild(Page page, String id, int index, java.awt.Rectangle r, boolean visible) {
        JComponent block = page.blocks.get(id);
        java.awt.Component c = childAt(block, index);
        if (c != null) {
            applyNode(page, c, r, visible);
        }
    }

    /** 子索引路径：block 自己 → ""；block 内第 2 个容器的第 1 个孩子 → "2/1" */
    static String pathOf(JComponent block, java.awt.Component target) {
        if (block == null || target == null) {
            return null;
        }
        if (target == block) {
            return "";
        }
        java.util.List<Integer> idx = new java.util.ArrayList<>();
        java.awt.Component x = target;
        while (x != null && x != block) {
            Container p = x.getParent();
            if (p == null) {
                return null;
            }
            int i = indexIn(p, x);
            if (i < 0) {
                return null;
            }
            idx.add(0, i);
            x = p;
        }
        if (x != block) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < idx.size(); i++) {
            if (i > 0) {
                sb.append('/');
            }
            sb.append(idx.get(i));
        }
        return sb.toString();
    }

    /** 按路径取组件（与 {@link #pathOf} 互逆）；路径无效返回 null */
    static java.awt.Component resolvePath(JComponent block, String path) {
        if (block == null || path == null) {
            return null;
        }
        java.awt.Component cur = block;
        if (path.isEmpty()) {
            return cur;
        }
        for (String seg : path.split("/")) {
            if (!(cur instanceof Container)) {
                return null;
            }
            int i;
            try {
                i = Integer.parseInt(seg.trim());
            } catch (NumberFormatException ex) {
                return null;
            }
            cur = childAt((JComponent) (cur instanceof JComponent ? cur : block), i);
            if (cur == null) {
                return null;
            }
        }
        return cur;
    }

    private static int indexIn(Container p, java.awt.Component c) {
        java.awt.Component[] comps = p.getComponents();
        for (int i = 0; i < comps.length; i++) {
            if (comps[i] == c) {
                return i;
            }
        }
        return -1;
    }

    /** 递归收集被接管的容器里的子组件（写盘用）：depth1 走 children，更深的走 nodes */
    private static void collectDeep(JComponent block, String prefix, Container c,
                                    org.json.JSONObject kids, org.json.JSONObject nodes) {
        if (c == null || !takenLayout.containsKey(c)) {
            return;
        }
        java.awt.Component[] comps = c.getComponents();
        for (int i = 0; i < comps.length; i++) {
            org.json.JSONObject k = new org.json.JSONObject();
            k.put("cls", comps[i].getClass().getSimpleName());
            k.put("x", comps[i].getX());
            k.put("y", comps[i].getY());
            k.put("w", Math.max(1, comps[i].getWidth()));
            k.put("h", Math.max(1, comps[i].getHeight()));
            if (!comps[i].isVisible()) {
                k.put("visible", false);
            }
            String path = prefix.isEmpty() ? String.valueOf(i) : prefix + "/" + i;
            if (prefix.isEmpty()) {
                kids.put(String.valueOf(i), k);
            } else {
                nodes.put(path, k);
            }
            if (comps[i] instanceof Container) {
                collectDeep(block, path, (Container) comps[i], kids, nodes);
            }
        }
    }

    /** 把整页布局写进 &lt;pageId&gt;.layout.json（含任意深度的组件位置） */
    static void savePage(Page page) {
        try {
            ensureAbsolute(page);
            org.json.JSONObject root = new org.json.JSONObject();
            root.put("mode", "absolute");
            /* v2：方块 id 的含义变了（v1 会把 JScrollPane 的视口/滚动条当成方块，
             * 现在剥到真正的面板层）。旧文件套到新 id 上会摆错组件，所以打版本号，
             * 读取时无版本号的一律忽略并恢复默认。 */
            root.put("layoutVersion", 2);
            org.json.JSONObject panels = new org.json.JSONObject();
            for (Map.Entry<String, JComponent> e : page.blocks.entrySet()) {
                JComponent c = e.getValue();
                if (c == null) {
                    continue;
                }
                org.json.JSONObject b = new org.json.JSONObject();
                b.put("x", c.getX());
                b.put("y", c.getY());
                b.put("w", Math.max(1, c.getWidth()));
                b.put("h", Math.max(1, c.getHeight()));
                if (!c.isVisible()) {
                    b.put("visible", false);
                }
                org.json.JSONObject kids = new org.json.JSONObject();
                org.json.JSONObject nodes = new org.json.JSONObject();
                collectDeep(c, "", c, kids, nodes);
                if (kids.length() > 0) {
                    b.put("children", kids);
                }
                if (nodes.length() > 0) {
                    b.put("nodes", nodes);      // "0/2" 这类路径：任意深度的组件
                }
                panels.put(e.getKey(), b);
            }
            root.put("panels", panels);
            Files.write(page.file.toPath(),
                    root.toString(2).getBytes(StandardCharsets.UTF_8));
            // 记下时间戳，避免轮询马上又按旧内容重放一次
            page.stamp = page.file.lastModified() * 31 + page.file.length();
            System.out.println("[Layout] 已保存 " + page.file.getName());
        } catch (Exception ex) {
            System.out.println("[Layout] 保存失败 " + page.id + ": " + ex);
        }
    }

    /** 把嵌套容器恢复成原来的 LayoutManager（reset 用） */
    private static void releaseContainers() {
        for (Map.Entry<Container, LayoutManager> e : takenLayout.entrySet()) {
            try {
                e.getKey().setLayout(e.getValue());
            } catch (RuntimeException ignored) {
            }
        }
        takenLayout.clear();
        takenFallback.clear();
    }

    /** 恢复该页默认布局（删掉布局文件 + 还原 GroupLayout） */
    static void resetPage(Page page) {
        try {
            if (page.file.isFile()) {
                page.file.delete();
            }
        } catch (Exception ignored) {
        }
        releaseContainers();
        page.stamp = -1;
        apply(page, null);
    }

    // ------------------------------------------------------------------
    // 清单（给布局编辑器）
    // ------------------------------------------------------------------

    /** 延迟导出清单：页面陆续注册时合并成一次写盘 */
    private static void scheduleManifest() {
        if (manifestTimer != null) {
            manifestTimer.stop();
        }
        manifestTimer = new javax.swing.Timer(1200, ev -> {
            ((javax.swing.Timer) ev.getSource()).stop();
            dumpManifest();
        });
        manifestTimer.setRepeats(false);
        manifestTimer.start();
    }

    /** 导出 ui_layout_pages.json：页面 → 方块（id/名称/当前坐标） */
    /** 递归导出子组件（限深度/数量，避免巨型窗体把清单撑爆） */
    private static org.json.JSONArray manifestChildren(java.awt.Component c, int depth, int[] count) {
        org.json.JSONArray arr = new org.json.JSONArray();
        if (depth > 6 || !(c instanceof Container) || count[0] > 400) {
            return arr;
        }
        java.awt.Component[] comps = ((Container) c).getComponents();
        for (int i = 0; i < comps.length && count[0] <= 400; i++) {
            count[0]++;
            org.json.JSONObject k = new org.json.JSONObject();
            k.put("i", i);
            k.put("cls", comps[i].getClass().getSimpleName());
            k.put("label", labelOfComponent(comps[i]));
            k.put("x", comps[i].getX());
            k.put("y", comps[i].getY());
            k.put("w", comps[i].getWidth());
            k.put("h", comps[i].getHeight());
            k.put("visible", comps[i].isVisible());
            org.json.JSONArray grand = manifestChildren(comps[i], depth + 1, count);
            if (grand.length() > 0) {
                k.put("children", grand);
            }
            arr.put(k);
        }
        return arr;
    }

    /** 组件上能显示的文字（按钮文字 / 标签文字 / 名字），给外部编辑器当节点名 */
    static String labelOfComponent(java.awt.Component c) {
        String text = null;
        if (c instanceof javax.swing.AbstractButton) {
            text = ((javax.swing.AbstractButton) c).getText();
        } else if (c instanceof javax.swing.JLabel) {
            text = ((javax.swing.JLabel) c).getText();
        } else if (c instanceof javax.swing.text.JTextComponent) {
            text = ((javax.swing.text.JTextComponent) c).getText();
        }
        if (text == null) {
            text = c.getName();
        }
        if (text == null) {
            return "";
        }
        text = text.trim().replace('\n', ' ').replace('\r', ' ');
        return text.length() > 40 ? text.substring(0, 40) + "…" : text;
    }

    static void dumpManifest() {
        try {
            JSONObject rootObj = new JSONObject();
            JSONArray pagesArr = new JSONArray();
            // 快照：导出期间可能有新窗口注册（并发修改会抛 ConcurrentModificationException）
            List<Page> snapshot = new ArrayList<>(PAGES);
            for (Page p : snapshot) {
                JSONObject pg = new JSONObject();
                pg.put("id", p.id);
                pg.put("title", p.title);
                pg.put("file", p.file.getName());
                Container host = p.host != null ? p.host : p.frame.getContentPane();
                int w = host.getWidth();
                int h = host.getHeight();
                pg.put("w", w > 0 ? w : p.frame.getWidth());
                pg.put("h", h > 0 ? h : p.frame.getHeight());
                pg.put("managed", p.managed);
                JSONArray blocksArr = new JSONArray();
                int idx = 0;
                for (Map.Entry<String, JComponent> e : p.blocks.entrySet()) {
                    JComponent c = e.getValue();
                    if (c == null) {
                        continue;
                    }
                    JSONObject b = new JSONObject();
                    b.put("id", e.getKey());
                    b.put("label", labelOf(p, e.getKey(), c, idx));
                    b.put("x", c.getX());
                    b.put("y", c.getY());
                    b.put("w", c.getWidth() > 0 ? c.getWidth() : c.getPreferredSize().width);
                    b.put("h", c.getHeight() > 0 ? c.getHeight() : c.getPreferredSize().height);
                    b.put("cls", c.getClass().getSimpleName());
                    b.put("visible", c.isVisible());
                    // 递归子组件：外部布局编辑器（RBMK-LayoutEditor.jar / python 工具）靠它列到任意层级
                    org.json.JSONArray kids = manifestChildren(c, 1, new int[]{0});
                    if (kids.length() > 0) {
                        b.put("children", kids);
                    }
                    blocksArr.put(b);
                    idx++;
                }
                pg.put("blocks", blocksArr);
                pagesArr.put(pg);
            }
            rootObj.put("pages", pagesArr);
            File out = new File(dir(), "ui_layout_pages.json");
            Files.write(out.toPath(), rootObj.toString(2).getBytes(StandardCharsets.UTF_8));
            manifestDumped = true;
        } catch (Exception e) {
            System.out.println("[Layout] 导出页面清单失败: " + e);
        }
    }

    static boolean isManifestDumped() {
        return manifestDumped;
    }

    /** 方块显示名：优先取方块内第一个有文字的标签/按钮 */
    private static String labelOf(Page page, String id, JComponent c, int index) {
        String custom = page.labels.get(id);
        if (custom != null && !custom.isEmpty()) {
            return custom;
        }
        String text = firstText(c, 3);
        if (text != null && !text.trim().isEmpty()) {
            text = text.trim().replace('\n', ' ');
            return text.length() > 18 ? text.substring(0, 18) + "…" : text;
        }
        String name = c.getName();
        if (name != null && !name.isEmpty()) {
            return name;
        }
        return c.getClass().getSimpleName() + " #" + index;
    }

    private static String firstText(Component c, int depth) {
        if (depth < 0) {
            return null;
        }
        if (c instanceof JLabel) {
            String s = ((JLabel) c).getText();
            if (s != null && !s.trim().isEmpty()) {
                return s;
            }
        }
        if (c instanceof AbstractButton) {
            String s = ((AbstractButton) c).getText();
            if (s != null && !s.trim().isEmpty()) {
                return s;
            }
        }
        if (c instanceof Container) {
            for (Component k : ((Container) c).getComponents()) {
                String s = firstText(k, depth - 1);
                if (s != null && !s.trim().isEmpty()) {
                    return s;
                }
            }
        }
        return null;
    }
}
