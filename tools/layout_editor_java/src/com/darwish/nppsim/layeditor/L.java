package com.darwish.nppsim.layeditor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 编辑器界面语言（只有中文 / English）。
 *
 * <p>模拟器导出的清单里，窗口名和组件名是模拟器当前语言的文字，这里不做翻译；
 * 本类只管编辑器自己的界面文字（按钮、标签、状态、说明页）。</p>
 */
public final class L {

    /** 支持的语言 */
    public enum Lang {
        ZH("中文"), EN("English");

        final String nativeName;

        Lang(String nativeName) {
            this.nativeName = nativeName;
        }

        @Override public String toString() {
            return nativeName;
        }
    }

    /** 启动时可用 -Dlayouteditor.lang=en|zh 或 --lang=en 指定 */
    private static Lang lang = detectStartupLang();
    private static final Map<String, String[]> TABLE = new LinkedHashMap<>();

    private L() {
    }

    private static Lang detectStartupLang() {
        String v = System.getProperty("layouteditor.lang");
        if (v != null && v.trim().toLowerCase().startsWith("en")) {
            return Lang.EN;
        }
        return Lang.ZH;
    }

    public static Lang lang() {
        return lang;
    }

    public static void setLang(Lang l) {
        if (l != null) {
            lang = l;
        }
    }

    /** 取当前语言的文字；没有这个键就原样返回键名 */
    public static String t(String key) {
        String[] v = TABLE.get(key);
        if (v == null) {
            return key;
        }
        return v[lang == Lang.ZH ? 0 : 1];
    }

    private static void put(String key, String zh, String en) {
        TABLE.put(key, new String[]{zh, en});
    }

    static {
        // ---- 标题 / 按钮 ----
        put("app.title", "RBMK-1500 布局编辑器", "RBMK-1500 Layout Editor");
        put("btn.reload", "重新读取清单", "Reload manifest");
        put("btn.help", "提示", "Help");
        put("btn.help.tip", "详细设置与操作说明（F1）", "Full setup & operation guide (F1)");
        put("btn.save", "保存", "Save");
        put("btn.resetNode", "恢复此节点", "Reset node");
        put("btn.resetPage", "恢复整页", "Reset page");
        put("btn.delete", "删除文件", "Delete file");
        put("btn.ok", "知道了", "Got it");
        put("lbl.window", "窗口", "Window");
        put("lbl.language", "语言", "Language");
        put("lbl.x", "X", "X");
        put("lbl.y", "Y", "Y");
        put("lbl.w", "W", "W");
        put("lbl.h", "H", "H");
        put("lbl.visible", "可见", "Visible");
        put("tree.title", "组件（任意层级）", "Components (any depth)");
        put("canvas.title", "画布", "Canvas");
        put("no.manifest", "（未找到清单）", "(no manifest)");
        put("not.selected", "（未选中组件）", "(no component selected)");

        // ---- 状态栏 / 对话框 ----
        put("status.noManifest",
                "没找到 ui_layout_pages.json —— 先启动一次模拟器，它会把清单导出到布局文件同目录。",
                "ui_layout_pages.json not found - start the simulator once; it exports the manifest "
                        + "next to the layout file.");
        put("status.saved", "已保存 → %s（%s），模拟器约 1 秒内跟随更新",
                "Saved -> %s (%s); the simulator picks it up within ~1 second");
        put("status.savedFile", "已保存 → ", "Saved -> ");
        put("status.savedTail", "），模拟器约 1 秒内跟随更新", "), the simulator picks it up within ~1 second");
        put("status.deleted", "已删除布局文件，模拟器将恢复默认布局",
                "Layout file deleted - the simulator falls back to the default layout");
        put("status.deleteFail", "删除失败（文件被占用？）", "Delete failed (file in use?)");
        put("status.summary", "%s：%d 个方块，%d 处改动　%s",
                "%s: %d blocks, %d change(s)  %s");
        put("status.noFile", "（尚无布局文件）", "(no layout file yet)");
        put("status.hasFile", "（已有 %s）", "(%s exists)");
        put("dlg.noManifest", "读不到清单", "Manifest unavailable");
        put("dlg.readFail", "读取失败：", "Read failed: ");
        put("dlg.error", "错误", "Error");
        put("dlg.saveFail", "保存失败：", "Save failed: ");
        put("dlg.deleteTitle", "删除布局文件", "Delete layout file");
        put("dlg.deleteAsk", "删除 %s 后，模拟器会把该窗口恢复成默认布局。继续？",
                "After deleting %s the simulator restores the default layout of this window. Continue?");
        put("dlg.manifestFail", "清单不存在或为空：\n%s\n\n先启动模拟器（它会每个窗口打开时刷新清单），再点这里。",
                "Manifest missing or empty:\n%s\n\nStart the simulator (it refreshes the manifest "
                        + "whenever a window is opened) and try again.");
        put("canvas.noChild", "这一层没有子组件", "This level has no child components");
        put("path.block", "方块 ", "block ");
        put("path.dirty", "[已改]", "[changed]");
        put("path.dirty2", "   [已改]", "   [changed]");

        // ---- 画布提示 ----
        put("hint.canvas",
                "<html>拖动方块＝移动，拖右下角＝改尺寸；方向键 ±1（Shift ±10）。"
                        + "画布显示的是“选中节点的父容器”，兄弟不会跟着动。"
                        + "　不清楚就点右上角“提示”（F1）。</html>",
                "<html>Drag a box to move it, drag its bottom-right corner to resize; arrow keys nudge "
                        + "by 1 px (Shift = 10). The canvas shows the parent container of the selected "
                        + "node, so siblings never move along. Stuck? Press the top-right Help button (F1)."
                        + "</html>");

        // ---- 说明对话框 ----
        put("help.title", "操作说明 —— RBMK-1500 布局编辑器", "Help - RBMK-1500 Layout Editor");
        put("help.tab1", "① 快速上手", "1) Quick start");
        put("help.tab2", "② 选组件 / 改位置", "2) Picking & moving");
        put("help.tab3", "③ 属性与存盘", "3) Properties & files");
        put("help.tab4", "④ 快捷键", "4) Shortcuts");
        put("help.tab5", "⑤ 常见问题", "5) FAQ");

        // ---- 说明正文（中文） ----
        put("help.b1",
                "【这个工具是干什么的】\n"
                        + "把模拟器里任意窗口的面板、子面板、按键和数值显示，摆到你想放的位置、改成你想要的大小、\n"
                        + "或者干脆隐藏起来。改完写进布局文件，模拟器每秒轮询该文件，约 1 秒内界面就跟着变，\n"
                        + "不用重启模拟器。\n\n"
                        + "【第一次使用：5 步】\n"
                        + "1) 启动模拟器，并把要调整的窗口逐个打开一次。\n"
                        + "   模拟器会把每个窗口的完整组件树导出成 ui_layout_pages.json（和布局文件放同一目录）：\n"
                        + "      %s\n"
                        + "2) 运行本工具（模拟器可以开着，两者互不干扰）。\n"
                        + "3) 顶部“窗口”下拉框选中要调整的窗口。\n"
                        + "   8 个窗口：主控制室、凝结水、除氧器、主循环泵、给水、净化与冷却、TG、蒸汽环路。\n"
                        + "4) 左侧“组件（任意层级）”树里点中要动的组件（面板 → 子面板 → 按键，能一直往下点）。\n"
                        + "5) 拖动 / 改数值 → 点“保存”（或 Ctrl+S）→ 回模拟器看效果。\n\n"
                        + "【改坏了怎么办】\n"
                        + "· 选中组件 → “恢复此节点”：把它的位置/尺寸/可见性恢复成出厂值（改在内存里，还要再按“保存”）。\n"
                        + "· “恢复整页”：整页所有方块与子组件恢复出厂值（同样要再按“保存”）。\n"
                        + "· “删除文件”：删掉该窗口的布局文件，模拟器立刻回到默认布局。\n\n"
                        + "【注意】\n"
                        + "本工具不会自动保存。所有改动都先在内存里，必须点“保存”（或 Ctrl+S）才会写盘、\n"
                        + "模拟器才会看到；底部状态栏会显示“已保存 → 文件名 + 时间”。\n",
                "【What this tool does】\n"
                        + "Move / resize / hide any panel, sub-panel, button or readout of any simulator window.\n"
                        + "The result is written to the layout file; the simulator polls it every second and\n"
                        + "repaints within about one second - no restart needed.\n\n"
                        + "【First time: 5 steps】\n"
                        + "1) Start the simulator and open every window you want to adjust once.\n"
                        + "   The simulator exports the full component tree as ui_layout_pages.json next to\n"
                        + "   the layout files:\n      %s\n"
                        + "2) Run this tool (the simulator may stay open; they do not interfere).\n"
                        + "3) Pick the window from the Window combo box at the top.\n"
                        + "   8 windows: Main control room, Condensate, Dearator, MCP, Feedwater, PCS, TG, Steam loop.\n"
                        + "4) Click the component in the tree on the left (panel -> sub-panel -> button, any depth).\n"
                        + "5) Drag it or type new values -> press Save (or Ctrl+S) -> look at the simulator.\n\n"
                        + "【If something goes wrong】\n"
                        + "· Select a node -> Reset node: back to its factory position/size/visibility (in memory;\n"
                        + "  press Save afterwards).\n"
                        + "· Reset page: resets every block and child of the current page (Save afterwards).\n"
                        + "· Delete file: removes the layout file - the simulator falls back to the default layout.\n\n"
                        + "【Note】\n"
                        + "This tool does NOT autosave. Changes stay in memory until you press Save (or Ctrl+S);\n"
                        + "the status bar then shows 'Saved -> <file> (time)'.\n");
        put("help.b2",
                "【左侧：组件树】\n"
                        + "· 树的层次就是界面里的真实嵌套：方块（面板）→ 子面板 → 按键/标签/数值显示。\n"
                        + "· 节点文字后面方括号里是组件类型，末尾是索引路径\n"
                        + "  （例如 0/2/2 表示：方块里的第 0 个子容器 → 它的第 2 个子容器 → 再里面第 2 个组件）。\n"
                        + "· 点树上的任意节点，右侧画布立刻切到“它所在的容器”。\n\n"
                        + "【右侧：画布】\n"
                        + "· 画布显示的是“选中节点所在的那个容器”以及它里面的所有直接子组件（兄弟）。\n"
                        + "  所以拖谁只影响谁，兄弟不会跟着动；外层容器看到的是方块，往里点就看到内部按键。\n"
                        + "· 画布会自动缩放到能放下当前容器（容器越大，方块显示越小），坐标始终按真实像素读写。\n"
                        + "· 矩形左上角的文字是组件名，第二行是 位置 x,y 和尺寸 W×H。\n"
                        + "· 红色的名字表示该组件被设为“不可见”。\n\n"
                        + "【三种改法】\n"
                        + "1) 拖动方块本体：改位置（x, y）。\n"
                        + "2) 拖方块右下角的小把手（约 8 像素见方）：改尺寸（W, H）。\n"
                        + "3) 下方属性栏直接输入 X / Y / W / H，或用方向键（见“快捷键”页）。\n"
                        + "   X、Y、W、H 都是“相对父容器左上角”的像素值；方块（最外层）就是相对窗口内容区。\n\n"
                        + "【进入下一层】\n"
                        + "想调某个面板内部的按键：在树上点开那个面板的子节点，或在画布上点中该面板对应的方块后，\n"
                        + "到树上继续往下点；画布就会显示面板内部。\n",
                "【Left: the component tree】\n"
                        + "· The tree mirrors the real nesting: block (panel) -> sub-panel -> button/label/readout.\n"
                        + "· After the name you see the component type in brackets and the index path\n"
                        + "  (e.g. 0/2/2 = child #0 of the block -> its child #2 -> its child #2).\n"
                        + "· Clicking any node switches the canvas to the container that node lives in.\n\n"
                        + "【Right: the canvas】\n"
                        + "· The canvas draws the container of the selected node plus all of its direct children\n"
                        + "  (its siblings). Dragging one never moves the others; the outermost level shows the\n"
                        + "  blocks, drilling in reveals the buttons inside a panel.\n"
                        + "· The canvas auto-scales to fit the current container; coordinates are always real pixels.\n"
                        + "· Each box shows the component name on top and 'x,y  WxH' underneath.\n"
                        + "· Red names mean the component is set to invisible.\n\n"
                        + "【Three ways to change something】\n"
                        + "1) Drag the box: change position (x, y).\n"
                        + "2) Drag the small handle at the bottom-right corner (~8 px): change size (W, H).\n"
                        + "3) Type X / Y / W / H in the property bar, or nudge with the arrow keys\n"
                        + "   (see the Shortcuts tab). All values are relative to the parent's top-left corner;\n"
                        + "   for a block that is the window content area.\n\n"
                        + "【Going one level deeper】\n"
                        + "To reach a button inside a panel, expand that panel in the tree (or click it on the\n"
                        + "canvas) and keep picking deeper nodes - the canvas then shows the panel interior.\n");
        put("help.b3",
                "【属性栏】\n"
                        + "· X / Y：相对父容器的左上角坐标（像素）。\n"
                        + "· W / H：宽高（像素）；最小 1，改成 0 会被自动改成 1。\n"
                        + "· 可见：勾掉 = 隐藏该组件（模拟器里 setVisible(false)）；\n"
                        + "  隐藏后面板照样占位置，只是看不见——想挪开请改坐标。\n"
                        + "· 属性栏上方那行显示：组件名、它属于哪个方块、索引路径，改动后会出现 [已改]。\n\n"
                        + "【保存到哪个文件】\n"
                        + "· 主控制室（id = main）      → ui_layout.json\n"
                        + "· 其它窗口（例如凝结水）      → <窗口ID>.layout.json，如 CondensateUI.layout.json\n"
                        + "· 目录：%s\n\n"
                        + "【文件里写了什么】\n"
                        + "只写“改过”的组件，没动过的保持模拟器默认布局：\n"
                        + "· panels.<方块id>：方块自身的位置、尺寸、内容缩放；\n"
                        + "· children：方块的第一层子组件（模拟器靠它把面板内部切成绝对布局）；\n"
                        + "· nodes：更深层的组件，键是索引路径（如 0/2/2），带 w/h/visible。\n"
                        + "· layoutVersion: 2 —— 版本号，别手工删；没有它的旧文件模拟器会忽略。\n\n"
                        + "【生效方式】\n"
                        + "模拟器每秒检查一次布局文件的时间戳和大小，变了就重新应用；\n"
                        + "修改后 1 秒左右界面就变，不需要重启，也不需要重新打开窗口。\n\n"
                        + "【恢复】\n"
                        + "· 恢复此节点 / 恢复整页：只改内存，按“保存”后才生效。\n"
                        + "· 删除文件：模拟器下一轮轮询发现文件没了，立刻回到默认 GroupLayout 布局。\n",
                "【Property bar】\n"
                        + "· X / Y: position relative to the top-left corner of the parent (pixels).\n"
                        + "· W / H: size in pixels (minimum 1; 0 is clamped to 1).\n"
                        + "· Visible: uncheck to hide the component (setVisible(false) in the simulator).\n"
                        + "  A hidden panel still occupies its place - move it by changing X / Y.\n"
                        + "· The line above shows the name, the owning block and the index path, plus [changed]\n"
                        + "  once you change something.\n\n"
                        + "【Which file is written】\n"
                        + "· Main control room (id = main)  -> ui_layout.json\n"
                        + "· Other windows (e.g. condensate)  -> <windowId>.layout.json, e.g. CondensateUI.layout.json\n"
                        + "· Folder: %s\n\n"
                        + "【What is inside the file】\n"
                        + "Only components you actually changed are written; everything else keeps the simulator\n"
                        + "default layout:\n"
                        + "· panels.<blockId>: position, size and content scale of the block itself;\n"
                        + "· children: the first level of children (the simulator switches the panel to an\n"
                        + "  absolute layout because of it);\n"
                        + "· nodes: deeper components, keyed by index path (e.g. 0/2/2) with w/h/visible.\n"
                        + "· layoutVersion: 2 - do not delete it; the simulator ignores files without it.\n\n"
                        + "【How it is applied】\n"
                        + "The simulator checks the timestamp and size of the layout file every second and\n"
                        + "re-applies it when it changed - roughly one second after you press Save.\n\n"
                        + "【Undo / reset】\n"
                        + "· Reset node / Reset page only change memory - press Save to make them effective.\n"
                        + "· Delete file: the next poll finds the file gone and restores the default layout.\n");
        put("help.b4",
                "【快捷键】\n"
                        + "  Ctrl + S            保存当前窗口的布局文件\n"
                        + "  Ctrl + R            重新读取模拟器导出的组件清单\n"
                        + "  F1                  打开本说明\n"
                        + "  Esc                 关闭本说明对话框\n"
                        + "  Tab / Shift+Tab     在工具栏控件之间切换焦点\n\n"
                        + "【画布上的键盘微调】\n"
                        + "  ↑ ↓ ← →            选中组件移动 1 像素\n"
                        + "  Shift + 方向键      选中组件移动 10 像素\n"
                        + "  （先单击画布里的方块让它获得焦点，键盘才生效）\n\n"
                        + "【鼠标】\n"
                        + "  单击方块            选中（同时在左侧树里定位）\n"
                        + "  拖动方块            移动\n"
                        + "  拖动右下角把手      改尺寸\n"
                        + "  鼠标悬停            光标变成十字箭头＝可移动，斜箭头＝可改尺寸\n\n"
                        + "【窗口】\n"
                        + "  顶部“窗口”下拉框    切换要编辑的界面\n"
                        + "  顶部“语言”下拉框    切换中文 / English\n"
                        + "  “重新读取清单”      模拟器新开过窗口后点它，重新读组件树（会重开本窗口）\n",
                "【Shortcuts】\n"
                        + "  Ctrl + S            save the layout file of the current window\n"
                        + "  Ctrl + R            reload the component tree exported by the simulator\n"
                        + "  F1                  open this help\n"
                        + "  Esc                 close this help\n"
                        + "  Tab / Shift+Tab     move focus through the toolbar\n\n"
                        + "【Keyboard nudging on the canvas】\n"
                        + "  Arrow keys          move the selected component by 1 px\n"
                        + "  Shift + arrow keys  move it by 10 px\n"
                        + "  (click a box on the canvas first so it has the keyboard focus)\n\n"
                        + "【Mouse】\n"
                        + "  click a box         select it (the tree follows)\n"
                        + "  drag a box          move it\n"
                        + "  drag the corner     resize it\n"
                        + "  hover               cursor turns into a cross (move) or a diagonal arrow (resize)\n\n"
                        + "【Window】\n"
                        + "  Window combo        switch the window you are editing\n"
                        + "  Language combo      switch between Chinese and English\n"
                        + "  Reload manifest     after opening new windows in the simulator (reopens this window)\n");
        put("help.b5",
                "Q1：打开工具提示“没找到 ui_layout_pages.json / 清单为空”？\n"
                        + "   说明模拟器还没导出组件树。先启动模拟器，把要调的窗口逐个打开一次；\n"
                        + "   导出的是 %s。\n"
                        + "   也可以在启动时把布局目录作为参数传进来：java -jar RBMK-LayoutEditor.jar <目录>\n\n"
                        + "Q2：我改了、也保存了，模拟器却没反应？\n"
                        + "   ① 看底部状态栏：是不是显示“已保存 → 文件名 + 时间”，没保存就不会生效；\n"
                        + "   ② 确认编辑的是同一个窗口（每个窗口一个文件，见“属性与存盘”页）；\n"
                        + "   ③ 确认模拟器读的是同一个目录（模拟器找 ui_layout.json 的顺序：\n"
                        + "      当前目录 → jar 所在目录 → jar 上级目录）；\n"
                        + "   ④ 文件若是旧版本（没有 layoutVersion）会被忽略，请用本工具保存一次覆盖它。\n\n"
                        + "Q3：主控制室改了没效果？\n"
                        + "   主窗口只认 ui_layout.json（不是 main.layout.json）。本工具已经自动按这个规则写，\n"
                        + "   只要确认文件落在模拟器能找到的目录即可。\n\n"
                        + "Q4：树里的组件和界面看到的对不上？\n"
                        + "   模拟器更新过界面结构。点“重新读取清单”重新导出；仍然不对就先重启模拟器。\n\n"
                        + "Q5：为什么有的组件调了尺寸没变化？\n"
                        + "   有些容器（滚动面板、带自定义布局的面板）会在重绘时把子组件位置算回去。\n"
                        + "   这类组件建议只改它自己的位置/尺寸，或改它上一层的面板。\n\n"
                        + "Q6：能在 32 位 JRE 8 上跑吗？\n"
                        + "   能。本 jar 用 --release 8 编译，也随模拟器（32 位 JRE 8）一起分发。\n\n"
                        + "Q7：怎么全部恢复出厂？\n"
                        + "   把该窗口对应的 .layout.json（主窗口是 ui_layout.json）删掉即可，或点“删除文件”。\n",
                "Q1: The tool says 'ui_layout_pages.json not found' / 'manifest empty'?\n"
                        + "   The simulator has not exported the component tree yet. Start it and open every\n"
                        + "   window you care about once; it exports to %s\n"
                        + "   You can also pass the folder explicitly: java -jar RBMK-LayoutEditor.jar <folder>\n\n"
                        + "Q2: I changed and saved something, but the simulator ignores it?\n"
                        + "   1) Check the status bar - it must show 'Saved -> <file> (time)';\n"
                        + "   2) Make sure you edited the same window (one file per window, see tab 3);\n"
                        + "   3) Make sure the simulator reads the same folder (it looks for ui_layout.json in\n"
                        + "      the working directory, then next to the jar, then one level above);\n"
                        + "   4) Old files without layoutVersion are ignored - save once with this tool.\n\n"
                        + "Q3: My changes to the main control room have no effect?\n"
                        + "   The main window only reads ui_layout.json (not main.layout.json). This tool\n"
                        + "   already follows that rule - just make sure the file lands where the simulator looks.\n\n"
                        + "Q4: The tree does not match what I see in the simulator?\n"
                        + "   The simulator UI changed. Press 'Reload manifest'; if it still differs, restart it.\n\n"
                        + "Q5: Some components ignore my size change?\n"
                        + "   Containers such as scroll panes or panels with custom layout managers recompute\n"
                        + "   their children when they repaint. For those, change only their own position or\n"
                        + "   size, or edit their parent instead.\n\n"
                        + "Q6: Does it run on a 32-bit JRE 8?\n"
                        + "   Yes - the jar is compiled with --release 8 and ships with the simulator.\n\n"
                        + "Q7: How do I reset everything to factory defaults?\n"
                        + "   Delete the matching .layout.json (ui_layout.json for the main window) or press\n"
                        + "   'Delete file'.\n");

        // ---- Main / Manifest ----
        put("dlg.noManifestLong",
                "没找到 ui_layout_pages.json。\n\n"
                        + "这个东西是模拟器运行时导出的（和 ui_layout.json 放在一起）。\n"
                        + "先启动一次模拟器、把要调的窗口各打开一次，再运行本工具；\n"
                        + "或者在这里先把目录指过去。",
                "ui_layout_pages.json not found.\n\n"
                        + "The simulator exports it at runtime (next to ui_layout.json).\n"
                        + "Start the simulator, open the windows you want to edit, then run this tool;\n"
                        + "or point the folder here.");
        put("dlg.noManifestList", "读不到布局清单", "Layout manifest unavailable");
        put("dlg.chooseDir", "选择布局文件所在目录（含 ui_layout.json）",
                "Choose the folder that holds ui_layout.json");
        put("dlg.noManifestInDir", "该目录里没有 ui_layout_pages.json。",
                "This folder does not contain ui_layout_pages.json.");
        put("dlg.notFound", "找不到清单", "Manifest not found");
        put("mf.panel", "面板 ", "panel ");
        put("mf.mainPage", "主控制室（ui_layout.json）", "Main control room (ui_layout.json)");
    }
}
