# -*- coding: utf-8 -*-
"""
RBMK-1500 布局编辑器（Python / Tkinter）
========================================
在原来「拖动彩色方块调面板」的基础上，扩展为**任意窗口、任意层级**都能调：

    · 页面下拉框      —— 每个窗口一页（主控制室、凝结水、除氧器、主循环泵……全部可选）
    · 左侧组件树      —— 页面 → 方块（面板）→ 子面板 → 按键/数值显示……任意深度
    · 画布            —— 显示「当前容器」及其所有子组件，拖动即改坐标，右下角拖动改尺寸
    · 双击方块/树节点 —— 进入该组件的内部（继续往下钻），「返回上级」退一层
    · 属性栏          —— 直接输入 X / Y / W / H，或勾选「可见」

写盘格式与模拟器 `UiLayoutFile` 完全一致（同时也是 RBMK-LayoutEditor.jar 的格式）：

    {
      "mode": "absolute",
      "layoutVersion": 2,
      "panels": {
        "5": { "x": 55, "y": 543, "w": 388, "h": 269, "content_scale": 1.0,
               "children": { "0": {"cls":"JPanel","x":2,"y":2} },      ← 第一层（相对面板）
               "nodes":    { "0/2/2": {"cls":"NumberEditor","x":32,"y":10,"w":21,"h":22,"visible":true} } }
      }
    }

    children 只放第一层（模拟器靠它把面板内部切成绝对布局），
    nodes 放被改过的任意深度组件（键是索引路径，如 "0/2/2"），
    坐标一律**相对父组件**，只写改过的项，其余由模拟器保持默认。

组件清单来自模拟器运行时导出的 `ui_layout_pages.json`（和布局文件同目录）：
每个窗口、每个方块、每个子组件（递归，最多 6 层 / 每块 400 个）。没有清单时退回
`ui_layout_children.json`（第一层）或内置主窗口定义，保证老版本也能用。

用法：
    python layout_editor.py [--out 布局文件路径] [--page 页面id] [--drill 方块/路径]

    --page   启动即切到某页（id 或标题，如 CondensateUI）
    --drill  启动即钻进某个组件（如 --drill 5 或 --drill 5/0/2）
    --selftest   不开窗口自检（打印页数/方块数/节点数），供自动化检查用

操作：
    · 左键拖动方块            —— 移动位置（相对父组件）
    · 拖动方块右下角          —— 改变尺寸
    · 双击方块 / 树节点       —— 进入内部（看它的子组件）
    · 「返回上级」            —— 回到父容器
    · 单击选中 + 方向键       —— 1px 微调（Shift = 10px）
    · 内容缩放滑块 / [ ]      —— 单独放大缩小**选中方块内部**的按键与文字（×0.5~×2.5）
    · Ctrl+滚轮 / +-          —— 缩放画布；「适应窗口」自动缩放到当前容器
    · 自动保存                —— 每次改动直接写盘，模拟器每秒轮询，约 1 秒内界面跟着变
"""
import json
import os
import sys
import time
import tkinter as tk
from tkinter import messagebox, ttk

# ---------------------------------------------------------------------------
# 兜底面板定义：没有 ui_layout_pages.json 时用（主控制室）
# key（与 Java 端 layoutBlockMap 对应）, 中文名, x, y, 宽, 高, 颜色, 内容缩放
# ---------------------------------------------------------------------------
BLOCKS = [
    ("annunciator",     "报警阵列",      13,  13, 570,  94, "#c0504d", 1.0),
    ("ack",             "ACK",           589, 13,  45,  51, "#b06a6a", 1.0),
    ("mwt",             "MWt 功率",      643, 13, 260, 125, "#e8a33d", 1.0),
    ("mwe1",            "MWe-1",         643, 144, 110,  41, "#e6c860", 1.0),
    ("mwe2",            "MWe-2",         759, 144, 110,  41, "#e6c860", 1.0),
    ("coremap",         "堆芯图",        954, 13, 350, 350, "#20b2a6", 1.0),
    ("az_panel",        "AZ 面板",       13, 191, 258, 132, "#9b59b6", 1.0),
    ("rod_limit",       "棒限值/棒序",   13, 329, 200,  60, "#8e9aaf", 1.0),
    ("reactivity",      "反应性",        277, 191, 355, 183, "#4a90d9", 1.0),
    ("autocontrol",     "自动控制",      643, 191, 305, 163, "#6fbf73", 1.0),
    ("rod_buttons",     "复位/拔出/插入", 13, 395,  98, 145, "#cd7f32", 1.0),
    ("rod_grid",        "控制棒网格",    117, 395, 520, 315, "#2fa8a0", 1.0),
    ("offline_cooling", "离线冷却",      643, 395, 229, 130, "#e8836b", 1.0),
    ("auto_az",         "自动AZ",        643, 531, 140, 140, "#c2a83e", 1.0),
    ("rod_limit_panel", "控制棒数量限制", 789, 531, 140, 140, "#4e8f8f", 1.0),
    ("loop_params",     "回路参数",      954, 395, 350, 235, "#5c6bc0", 1.0),
]

PALETTE = ["#c0504d", "#e8a33d", "#20b2a6", "#9b59b6", "#4a90d9", "#6fbf73",
           "#cd7f32", "#2fa8a0", "#e8836b", "#c2a83e", "#4e8f8f", "#5c6bc0",
           "#8e9aaf", "#b06a6a", "#e6c860", "#7d6b91", "#3f8f5f", "#a45c5c",
           "#5f9ea0", "#b8860b", "#8fbc8f", "#cd5c5c", "#4682b4", "#dda0dd"]

DEFAULT_CANVAS = (1366, 768)      # 无清单时的参考画布尺寸
MARGIN = 40                       # 画布四周留白（模型坐标外扩）
SCALE_MIN, SCALE_MAX = 0.5, 2.5   # 内容缩放范围
ZOOM_MIN, ZOOM_MAX = 0.15, 3.0    # 画布缩放范围

FONT = ("Microsoft YaHei UI", 9)
FONT_SMALL = ("Microsoft YaHei UI", 8)
FONT_BOLD = ("Microsoft YaHei UI", 10, "bold")
FONT_TINY = ("Microsoft YaHei UI", 7)

# 脚本位置 → 默认输出 = 工程目录（SourceCode/）下的 ui_layout.json
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
DEFAULT_OUT = os.path.normpath(os.path.join(SCRIPT_DIR, "..", "..", "ui_layout.json"))


# ---------------------------------------------------------------- 小工具
def _clamp_scale(v):
    """内容缩放值钳制/容错：非法或缺失 → 1.0"""
    try:
        s = float(v)
    except (TypeError, ValueError):
        return 1.0
    return max(SCALE_MIN, min(SCALE_MAX, round(s, 2)))


def _ival(v, d=0):
    """容错取整"""
    try:
        return int(round(float(v)))
    except (TypeError, ValueError):
        return d


def _norm_node(raw):
    """把清单里的一个子组件规整成内部节点（递归）。

    内部节点：{"i","cls","label","x","y","w","h","visible","children":[...]}
    坐标是**相对父组件**的。
    """
    node = {
        "i": _ival(raw.get("i"), 0),
        "cls": str(raw.get("cls") or ""),
        "label": str(raw.get("label") or raw.get("text") or "")[:24],
        "x": _ival(raw.get("x"), 0),
        "y": _ival(raw.get("y"), 0),
        "w": max(1, _ival(raw.get("w"), 1)),
        "h": max(1, _ival(raw.get("h"), 1)),
        "visible": bool(raw.get("visible", True)),
        "children": [],
    }
    for c in raw.get("children") or []:
        if isinstance(c, dict):
            node["children"].append(_norm_node(c))
    return node


def _builtin_main_blocks():
    """没有页面清单时的兜底：内置主窗口定义"""
    out = []
    for b in BLOCKS:
        out.append({"key": b[0], "label": b[1], "cls": "",
                    "x": b[2], "y": b[3], "w": b[4], "h": b[5],
                    "color": b[6], "cs": b[7], "visible": True, "kids": [],
                    "d": {"x": b[2], "y": b[3], "w": b[4], "h": b[5], "cs": b[7]}})
    return out


def _load_level1_children(out_path):
    """老版本模拟器只导出第一层（ui_layout_children.json）：面板 key -> 子组件列表"""
    folder = os.path.dirname(os.path.abspath(out_path))
    path = os.path.join(folder, "ui_layout_children.json")
    result = {}
    if not os.path.isfile(path):
        return result
    try:
        with open(path, "r", encoding="utf-8") as f:
            data = json.load(f)
        for k, v in (data.get("panels") or {}).items():
            kids = v.get("children") if isinstance(v, dict) else None
            if isinstance(kids, list):
                result[str(k)] = [_norm_node(c) for c in kids if isinstance(c, dict)]
    except Exception as e:
        print("读取子组件清单失败:", e)
    return result


def _load_pages(out_path):
    """读取模拟器导出的 ui_layout_pages.json：每个窗口一页 + 方块 + 递归子组件。

    没有清单时依次退回：ui_layout_children.json（第一层）→ 内置主窗口定义。
    """
    folder = os.path.dirname(os.path.abspath(out_path))
    manifest = os.path.join(folder, "ui_layout_pages.json")
    pages = []
    if os.path.isfile(manifest):
        try:
            with open(manifest, "r", encoding="utf-8") as f:
                data = json.load(f)
            for p in data.get("pages") or []:
                blocks = []
                for i, b in enumerate(p.get("blocks") or []):
                    key = str(b.get("id"))
                    blocks.append({
                        "key": key,
                        "label": str(b.get("label") or key),
                        "cls": str(b.get("cls") or ""),
                        "x": _ival(b.get("x"), 0),
                        "y": _ival(b.get("y"), 0),
                        "w": max(12, _ival(b.get("w"), 80)),
                        "h": max(12, _ival(b.get("h"), 40)),
                        "color": PALETTE[i % len(PALETTE)],
                        "cs": 1.0,
                        "visible": bool(b.get("visible", True)),
                        "kids": [_norm_node(c) for c in (b.get("children") or [])
                                 if isinstance(c, dict)],
                        "d": {"x": _ival(b.get("x"), 0), "y": _ival(b.get("y"), 0),
                              "w": max(12, _ival(b.get("w"), 80)),
                              "h": max(12, _ival(b.get("h"), 40)), "cs": 1.0},
                    })
                if blocks:
                    pid = p.get("id") or ("page%d" % len(pages))
                    # 主控制室的布局文件是 ui_layout.json（模拟器 UI 只认这个），
                    # 其他窗口是 <pageId>.layout.json —— 与 RBMK-LayoutEditor.jar 一致。
                    fname = "ui_layout.json" if pid == "main" else (
                        p.get("file") or (pid + ".layout.json"))
                    pages.append({"id": pid,
                                  "title": p.get("title") or pid,
                                  "file": fname,
                                  "w": _ival(p.get("w"), DEFAULT_CANVAS[0]) or DEFAULT_CANVAS[0],
                                  "h": _ival(p.get("h"), DEFAULT_CANVAS[1]) or DEFAULT_CANVAS[1],
                                  "managed": bool(p.get("managed", True)),
                                  "blocks": blocks})
        except Exception as e:
            print("读取页面清单失败（退回内置主窗口）:", e)

    if not pages:      # 没有清单：内置主窗口 + 可能的 ui_layout_children.json
        blocks = _builtin_main_blocks()
        l1 = _load_level1_children(out_path)
        for b in blocks:
            if l1.get(b["key"]):
                b["kids"] = l1[b["key"]]
                b["cls"] = b["kids"][0].get("cls", "") if b["kids"] else ""
        pages.append({"id": "main", "title": "主控制室",
                      "file": os.path.basename(out_path),
                      "w": DEFAULT_CANVAS[0], "h": DEFAULT_CANVAS[1], "managed": False,
                      "blocks": blocks})
    return pages


def _parent_path(path):
    """"0/2/2" → "0/2"；"0" → ""；"" → None（已经是方块本身）"""
    if path == "":
        return None
    return path.rsplit("/", 1)[0] if "/" in path else ""


class LayoutEditor:

    def __init__(self, root, out_path):
        self.root = root
        self.base_dir = os.path.dirname(os.path.abspath(out_path))
        self.out_path = out_path
        self.scale = 1.0
        self.pages = _load_pages(out_path)
        self.page_idx = 0
        self.page_state = {}          # pageId -> {"order","blocks","ovr"}
        self.blocks = {}              # key -> 方块 dict（含当前 x/y/w/h/cs/visible/kids/d）
        self.order = []
        self.ovr = {}                 # key -> {path: {"x","y","w","h","visible"}}（只存改过的）
        self.canvas_size = DEFAULT_CANVAS
        self.sel = None               # (blockKey, path)；path=='' 表示方块本身
        self.view_key = None          # None = 页面根（画所有方块）
        self.view_path = None
        self.drag = None
        self.autosave = tk.BooleanVar(value=True)
        self._updating_scale = False
        self._tree_map = {}           # iid -> (key, path)
        self._tree_rev = {}           # (key, path) -> iid
        self._loaded = set()          # 已经读过盘的页面 id
        self._syncing_tree = False    # 程序里改树选中时抑制回调

        self._apply_page(0, initial=True)
        self._build_ui()
        self.build_tree()
        self.fit_view()
        self.redraw()
        self.update_status()

    # ------------------------------------------------------------ 页面切换
    def _sync_title(self):
        """窗口标题带上当前页的输出文件（切页后跟着变）"""
        try:
            self.root.title("RBMK-1500 布局编辑器 — " + self.out_path)
        except Exception:
            pass

    def _stash_page(self):
        if not self.pages:
            return
        pid = self.pages[self.page_idx]["id"]
        self.page_state[pid] = {"blocks": self.blocks, "order": self.order, "ovr": self.ovr}

    def _apply_page(self, idx, initial=False):
        """切换到第 idx 页：换出方块表、输出文件、画布尺寸，并载入该页已有布局"""
        if not initial:
            self._stash_page()
        self.page_idx = max(0, min(len(self.pages) - 1, idx))
        page = self.pages[self.page_idx]
        pid = page["id"]
        if pid not in self.page_state:
            # 深拷贝一份可编辑副本（kids 只读，共享无妨；"d" 保存出厂默认值）
            self.page_state[pid] = {
                "order": [b["key"] for b in page["blocks"]],
                "blocks": {b["key"]: dict(b) for b in page["blocks"]},
                "ovr": {},
            }
        st = self.page_state[pid]
        self.order = st["order"]
        self.blocks = st["blocks"]
        self.ovr = st["ovr"]
        self.out_path = os.path.join(self.base_dir, page["file"])
        self.canvas_size = (max(320, page["w"]), max(240, page["h"]))
        self._sync_title()
        self.sel = None
        self.view_key = None
        self.view_path = None
        self.drag = None
        # 每页只在第一次进入时读盘（切页不会冲掉未保存的编辑）
        if pid not in self._loaded:
            self._load_from_disk()
            self._loaded.add(pid)

    def _load_from_disk(self):
        """把当前页布局文件读进内存：方块坐标/内容缩放 + 各层组件覆盖值"""
        if not os.path.isfile(self.out_path):
            return
        try:
            with open(self.out_path, "r", encoding="utf-8") as f:
                data = json.load(f)
        except Exception as e:
            print("载入布局文件失败（使用默认值）:", e)
            return
        self.ovr.clear()
        for k, v in (data.get("panels") or {}).items():
            k = str(k)
            if k not in self.blocks or not isinstance(v, dict):
                continue
            b = self.blocks[k]
            if _ival(v.get("w"), 0) > 0 and _ival(v.get("h"), 0) > 0:
                b["x"], b["y"] = _ival(v.get("x"), 0), _ival(v.get("y"), 0)
                b["w"], b["h"] = _ival(v.get("w"), 0), _ival(v.get("h"), 0)
                b["cs"] = _clamp_scale(v.get("content_scale"))
                b["visible"] = bool(v.get("visible", True))
            # 第一层（旧格式也可能只有这一段）
            for idx, c in (v.get("children") or {}).items():
                if not isinstance(c, dict):
                    continue
                self.ovr.setdefault(k, {}).setdefault(str(idx), {}).update(
                    {"x": _ival(c.get("x"), 0), "y": _ival(c.get("y"), 0)})
            # 任意深度
            for path, c in (v.get("nodes") or {}).items():
                if not isinstance(c, dict):
                    continue
                o = self.ovr.setdefault(k, {}).setdefault(str(path), {})
                o["x"], o["y"] = _ival(c.get("x"), 0), _ival(c.get("y"), 0)
                if _ival(c.get("w"), 0) > 0:
                    o["w"] = _ival(c.get("w"), 0)
                if _ival(c.get("h"), 0) > 0:
                    o["h"] = _ival(c.get("h"), 0)
                if "visible" in c:
                    o["visible"] = bool(c.get("visible"))

    # -------------------------------------------------------- 组件树模型
    def node_of(self, key, path):
        """按索引路径取节点（不含方块本身）；path=='' 返回 None"""
        if path == "" or key not in self.blocks:
            return None
        lst = self.blocks[key]["kids"]
        node = None
        for seg in path.split("/"):
            node = next((c for c in lst if str(c["i"]) == seg), None)
            if node is None:
                return None
            lst = node["children"]
        return node

    def kids_of(self, key, path):
        """某个容器的直接子节点列表（path=='' 即方块）"""
        if key is None:
            return []
        if path == "":
            return self.blocks[key]["kids"]
        node = self.node_of(key, path)
        return node["children"] if node else []

    def eff(self, key, path):
        """某个组件当前生效的几何（相对父组件）+ 可见性。覆盖值优先，否则默认值"""
        if key not in self.blocks:
            return {"x": 0, "y": 0, "w": 1, "h": 1, "visible": True}
        if path == "":
            b = self.blocks[key]
            return {"x": b["x"], "y": b["y"], "w": b["w"], "h": b["h"],
                    "visible": b["visible"]}
        node = self.node_of(key, path)
        base = {"x": node["x"], "y": node["y"], "w": node["w"], "h": node["h"],
                "visible": node["visible"]} if node else {"x": 0, "y": 0, "w": 1,
                                                          "h": 1, "visible": True}
        base.update(self.ovr.get(key, {}).get(path) or {})
        return base

    def abs_rect(self, key, path):
        """组件在页面里的绝对矩形（逐层累加父容器位置）"""
        b = self.blocks[key]
        if path == "":
            return b["x"], b["y"], b["w"], b["h"]
        ox, oy = b["x"], b["y"]
        segs = path.split("/")
        for depth in range(1, len(segs)):
            e = self.eff(key, "/".join(segs[:depth]))
            ox += e["x"]
            oy += e["y"]
        e = self.eff(key, path)
        return ox + e["x"], oy + e["y"], e["w"], e["h"]

    def set_geom(self, key, path, **changes):
        """写回坐标/尺寸/可见性（只记改过的字段，其余保持模拟器默认）"""
        if key is None:
            return
        if path == "":
            b = self.blocks[key]
            for k, v in changes.items():
                b[k] = _ival(v, 0) if k in ("x", "y", "w", "h") else bool(v)
            return
        o = self.ovr.setdefault(key, {}).setdefault(path, {})
        for k, v in changes.items():
            o[k] = _ival(v, 0) if k in ("x", "y", "w", "h") else bool(v)

    # ------------------------------------------------------------ 视图
    def container_rect(self):
        """当前画布显示的容器在页面里的矩形"""
        if self.view_key is None:
            return 0, 0, self.canvas_size[0], self.canvas_size[1]
        return self.abs_rect(self.view_key, self.view_path)

    def view_children(self):
        """当前容器里要画的直接子组件 → [(blockKey, path)]"""
        if self.view_key is None:
            return [(k, "") for k in self.order]
        kids = self.kids_of(self.view_key, self.view_path)
        prefix = "" if self.view_path == "" else self.view_path + "/"
        return [(self.view_key, prefix + str(c["i"])) for c in kids]

    def name_of(self, key, path):
        if path == "":
            b = self.blocks.get(key)
            return b["label"] if b else str(key)
        node = self.node_of(key, path)
        if not node:
            return "#" + path
        return node["label"] or node["cls"] or ("#%s" % path)

    def crumb(self):
        """面包屑：页面 > 方块 > 子面板 > ...（当前容器）"""
        parts = [self.pages[self.page_idx]["title"]]
        if self.view_key is not None:
            parts.append(self.name_of(self.view_key, ""))
            if self.view_path:
                segs = self.view_path.split("/")
                for d in range(1, len(segs) + 1):
                    parts.append(self.name_of(self.view_key, "/".join(segs[:d])))
        return " > ".join(parts)

    def drill_to(self, key, path, refit=True):
        """钻进某个组件内部（画布改画它的子组件）"""
        if key is None:
            self.view_key, self.view_path = None, None
        else:
            if (key, path) not in self._all_nodes():
                return
            self.view_key, self.view_path = key, path
        self.sel = (key, path) if key is not None else None
        if refit:
            self.fit_view()
        self.redraw()
        self.sync_tree_selection()
        self.sync_props()
        self.update_status()

    def _all_nodes(self):
        """(key, path) 全集：方块 + 所有子孙（用于校验钻取目标）"""
        out = set()
        for key in self.order:
            out.add((key, ""))

            def walk(kids, prefix):
                for c in kids:
                    p = prefix + str(c["i"])
                    out.add((key, p))
                    walk(c["children"], p + "/")
            walk(self.blocks[key]["kids"], "")
        return out

    def go_up(self):
        if self.view_key is None:
            return
        if self.view_path == "":
            self.sel = (self.view_key, "")
            self.view_key, self.view_path = None, None
        else:
            parent = _parent_path(self.view_path)
            self.sel = (self.view_key, self.view_path)
            self.view_key, self.view_path = self.view_key, parent
        self.fit_view()
        self.redraw()
        self.sync_tree_selection()
        self.sync_props()
        self.update_status()

    def fit_view(self):
        """把当前容器缩放到画布可见范围"""
        try:
            self.root.update_idletasks()
        except Exception:
            pass
        cw, ch = self.container_rect()[2:]
        vw = self.canvas.winfo_width() if hasattr(self, "canvas") else 0
        vh = self.canvas.winfo_height() if hasattr(self, "canvas") else 0
        if vw < 200 or vh < 150:
            vw, vh = 1100, 640
        s = min((vw - 2 * MARGIN) / max(cw, 1), (vh - 2 * MARGIN) / max(ch, 1))
        self.scale = max(ZOOM_MIN, min(ZOOM_MAX, s))

    # ------------------------------------------------------------------ UI
    def _build_ui(self):
        root = self.root
        root.title("RBMK-1500 布局编辑器 — " + self.out_path)

        bar = tk.Frame(root)
        bar.pack(side=tk.TOP, fill=tk.X)
        tk.Button(bar, text="保存", width=6, command=self.save_manual).pack(side=tk.LEFT, padx=3, pady=3)
        tk.Button(bar, text="重置本页", width=9, command=self.reset_defaults).pack(side=tk.LEFT, padx=3, pady=3)
        tk.Button(bar, text="删除布局文件", width=12, command=self.delete_file).pack(side=tk.LEFT, padx=3, pady=3)
        tk.Button(bar, text="重新载入", width=8, command=self.reload_file).pack(side=tk.LEFT, padx=3, pady=3)
        self.up_btn = tk.Button(bar, text="◂ 返回上级", width=10, command=self.go_up)
        self.up_btn.pack(side=tk.LEFT, padx=3, pady=3)
        tk.Button(bar, text="适应窗口", width=9, command=self.fit_and_redraw).pack(side=tk.LEFT, padx=3, pady=3)
        tk.Checkbutton(bar, text="自动保存", variable=self.autosave).pack(side=tk.LEFT, padx=8)
        tk.Label(bar, text="拖动=移动 · 右下角=改大小 · 双击=进入内部",
                 fg="#666666", font=FONT_SMALL).pack(side=tk.LEFT, padx=8)

        # 页面选择（每个窗口一页）
        bar_page = tk.Frame(root)
        bar_page.pack(side=tk.TOP, fill=tk.X)
        tk.Label(bar_page, text="页面：", font=FONT_BOLD).pack(side=tk.LEFT, padx=(6, 2))
        self.page_var = tk.StringVar(value=self.pages[self.page_idx]["title"])
        self.page_box = ttk.Combobox(bar_page, textvariable=self.page_var, state="readonly",
                                     values=[p["title"] for p in self.pages], width=22)
        self.page_box.current(self.page_idx)
        self.page_box.pack(side=tk.LEFT, padx=2, pady=2)
        self.page_box.bind("<<ComboboxSelected>>", self.on_page_changed)
        tk.Button(bar_page, text="刷新页面清单", width=12,
                  command=self.reload_pages).pack(side=tk.LEFT, padx=6)
        self.crumb_lbl = tk.Label(bar_page, text="", fg="#3355aa", font=FONT_SMALL)
        self.crumb_lbl.pack(side=tk.LEFT, padx=8)

        # 内容缩放（单独调整选中方块内部的按键 / 文字大小）
        bar2 = tk.Frame(root)
        bar2.pack(side=tk.TOP, fill=tk.X)
        tk.Label(bar2, text="内容缩放（按键/文字）：", font=FONT).pack(side=tk.LEFT, padx=(6, 2), pady=(0, 4))
        self.scale_var = tk.DoubleVar(value=1.0)
        self.scale_slider = tk.Scale(bar2, from_=SCALE_MIN, to=SCALE_MAX, resolution=0.05,
                                     orient=tk.HORIZONTAL, variable=self.scale_var, length=210,
                                     command=self.on_scale_changed, state=tk.DISABLED,
                                     digits=2, font=FONT_SMALL)
        self.scale_slider.pack(side=tk.LEFT, pady=(0, 4))
        tk.Label(bar2, text="作用于选中方块（1.00 = 仅随方块尺寸自动缩放；[ / ] 微调，Shift=大步）",
                 fg="#666666", font=FONT_SMALL).pack(side=tk.LEFT, padx=8)

        body = ttk.PanedWindow(root, orient=tk.HORIZONTAL)
        body.pack(side=tk.TOP, fill=tk.BOTH, expand=True)

        # 左：组件树（任意层级）
        left = tk.Frame(body, width=330)
        body.add(left, weight=1)
        tk.Label(left, text="组件树（双击进入内部）", font=FONT_BOLD, anchor="w").pack(
            side=tk.TOP, fill=tk.X, padx=4)
        tv_wrap = tk.Frame(left)
        tv_wrap.pack(side=tk.TOP, fill=tk.BOTH, expand=True)
        self.tree = ttk.Treeview(tv_wrap, show="tree", selectmode="browse")
        tv_sb = tk.Scrollbar(tv_wrap, orient=tk.VERTICAL, command=self.tree.yview)
        self.tree.configure(yscrollcommand=tv_sb.set)
        self.tree.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
        tv_sb.pack(side=tk.RIGHT, fill=tk.Y)
        self.tree.bind("<<TreeviewSelect>>", self.on_tree_select)
        self.tree.bind("<Double-1>", self.on_tree_double)
        self.tree.tag_configure("hidden", foreground="#aa4444")

        # 右：画布
        right = tk.Frame(body)
        body.add(right, weight=4)
        self.canvas = tk.Canvas(right, bg="#2b2b30", highlightthickness=0)
        self.hbar = tk.Scrollbar(right, orient=tk.HORIZONTAL, command=self.canvas.xview)
        self.vbar = tk.Scrollbar(right, orient=tk.VERTICAL, command=self.canvas.yview)
        self.canvas.configure(xscrollcommand=self.hbar.set, yscrollcommand=self.vbar.set)
        self.canvas.grid(row=0, column=0, sticky="nsew")
        self.vbar.grid(row=0, column=1, sticky="ns")
        self.hbar.grid(row=1, column=0, sticky="ew")
        right.rowconfigure(0, weight=1)
        right.columnconfigure(0, weight=1)

        # 属性栏（直接输入）
        props = tk.Frame(root)
        props.pack(side=tk.TOP, fill=tk.X)
        self.prop_vars = {}
        for name, w in (("X", 6), ("Y", 6), ("W", 6), ("H", 6)):
            tk.Label(props, text=name + "：", font=FONT).pack(side=tk.LEFT, padx=(6, 0), pady=3)
            var = tk.StringVar(value="")
            ent = tk.Entry(props, textvariable=var, width=w, font=FONT)
            ent.pack(side=tk.LEFT, pady=3)
            ent.bind("<Return>", lambda e: self.apply_props())
            self.prop_vars[name] = var
        self.prop_vis = tk.BooleanVar(value=True)
        tk.Checkbutton(props, text="可见", variable=self.prop_vis).pack(side=tk.LEFT, padx=8)
        tk.Button(props, text="应用", width=6, command=self.apply_props).pack(side=tk.LEFT, padx=4)
        self.prop_name = tk.Label(props, text="", fg="#3355aa", font=FONT_SMALL, anchor="w")
        self.prop_name.pack(side=tk.LEFT, padx=10)

        status = tk.Frame(root)
        status.pack(side=tk.BOTTOM, fill=tk.X)
        self.status = tk.Label(status, text="就绪", anchor="w", font=FONT)
        self.status.pack(side=tk.LEFT, fill=tk.X, padx=6)
        self.save_label = tk.Label(status, text="尚未保存", anchor="e", fg="#666666", font=FONT_SMALL)
        self.save_label.pack(side=tk.RIGHT, padx=6)

        c = self.canvas
        c.bind("<ButtonPress-1>", self.on_press)
        c.bind("<B1-Motion>", self.on_motion)
        c.bind("<ButtonRelease-1>", self.on_release)
        c.bind("<Motion>", self.on_hover)
        c.bind("<MouseWheel>", self.on_wheel)
        c.bind("<Double-Button-1>", self.on_double)
        # 方向键/键盘微调绑在画布上：焦点在输入框时不抢键
        c.bind("<Left>", lambda e: self.nudge(-1, 0, e.state))
        c.bind("<Right>", lambda e: self.nudge(1, 0, e.state))
        c.bind("<Up>", lambda e: self.nudge(0, -1, e.state))
        c.bind("<Down>", lambda e: self.nudge(0, 1, e.state))
        c.bind("<bracketleft>", lambda e: self.adj_scale(-1, e.state))
        c.bind("<bracketright>", lambda e: self.adj_scale(1, e.state))
        c.bind("<BackSpace>", lambda e: self.go_up())
        root.bind("<plus>", lambda e: self.zoom(1.15))
        root.bind("<minus>", lambda e: self.zoom(1 / 1.15))

        # 窗口尺寸：尽量容纳整个画布，超出则滚动
        need_w = int((DEFAULT_CANVAS[0] + MARGIN * 2) * 0.9) + 380
        need_h = int((DEFAULT_CANVAS[1] + MARGIN * 2) * 0.9) + 160
        sw = root.winfo_screenwidth()
        sh = root.winfo_screenheight()
        w = min(need_w, sw - 40)
        h = min(need_h, sh - 80)
        root.geometry("%dx%d+%d+%d" % (w, h, max((sw - w) // 2, 0), max((sh - h) // 3, 0)))
        self.update_crumb()

    # ---------------------------------------------------------- 组件树
    def build_tree(self):
        t = self.tree
        t.delete(*t.get_children())
        self._tree_map = {}
        self._tree_rev = {}
        root_iid = t.insert("", "end", text="📄 " + self.pages[self.page_idx]["title"], open=True)
        self._tree_map[root_iid] = (None, None)
        for key in self.order:
            b = self.blocks[key]
            txt = "▣ %s  [%s]" % (b["label"], b["cls"] or key)
            iid = t.insert(root_iid, "end", text=txt, open=(len(self.order) <= 24))
            self._tree_map[iid] = (key, "")
            self._tree_rev[(key, "")] = iid
            if not b["visible"]:
                t.item(iid, tags=("hidden",))
            self._tree_children(iid, key, "", b["kids"])

    def _tree_children(self, parent_iid, key, prefix, kids):
        for c in kids:
            path = prefix + str(c["i"])
            name = c["label"] or c["cls"] or ("#%s" % path)
            txt = "%s  [%s %s]" % (name, c["cls"] or "?", path)
            iid = self.tree.insert(parent_iid, "end", text=txt, open=False)
            self._tree_map[iid] = (key, path)
            self._tree_rev[(key, path)] = iid
            if not c["visible"]:
                self.tree.item(iid, tags=("hidden",))
            if c["children"]:
                self._tree_children(iid, key, path + "/", c["children"])

    def sync_tree_selection(self):
        """把当前选中项同步到树（并展开到可见）"""
        if not hasattr(self, "tree"):
            return
        iid = self._tree_rev.get(self.sel) if self.sel else None
        if not iid:
            return
        try:
            self._syncing_tree = True
            self.tree.selection_set(iid)
            self.tree.see(iid)
        except Exception:
            pass
        finally:
            self._syncing_tree = False

    def on_tree_select(self, _ev=None):
        if self._syncing_tree:
            return
        sel = self.tree.selection()
        if not sel:
            return
        target = self._tree_map.get(sel[0])
        if not target or target[0] is None:
            return                       # 点的是页面根
        key, path = target
        self.sel = (key, path)
        parent = _parent_path(path)
        self.view_key = key
        self.view_path = "" if parent is None else parent
        self.fit_view()
        self.redraw()
        self.sync_props()
        self.update_status()

    def on_tree_double(self, _ev=None):
        sel = self.tree.selection()
        if not sel:
            return
        target = self._tree_map.get(sel[0])
        if not target or target[0] is None:
            return
        key, path = target
        if self.kids_of(key, path):
            self.drill_to(key, path)
        else:
            self.status.configure(text="该组件没有子组件，只能调它自己的位置/尺寸。")

    # ---------------------------------------------------------- 属性栏
    def sync_props(self):
        if not self.sel:
            for v in self.prop_vars.values():
                v.set("")
            self.prop_vis.set(True)
            self.prop_name.configure(text="")
            return
        key, path = self.sel
        e = self.eff(key, path)
        self.prop_vars["X"].set(str(e["x"]))
        self.prop_vars["Y"].set(str(e["y"]))
        self.prop_vars["W"].set(str(e["w"]))
        self.prop_vars["H"].set(str(e["h"]))
        self.prop_vis.set(bool(e["visible"]))
        cls = self.blocks[key]["cls"] if path == "" else (self.node_of(key, path) or {}).get("cls", "")
        self.prop_name.configure(text="%s  ·  %s  ·  %s" % (self.name_of(key, path), cls or key, path or "(方块)"))

    def apply_props(self):
        if not self.sel:
            return
        key, path = self.sel
        cur = self.eff(key, path)
        try:
            x = int(float(self.prop_vars["X"].get()))
            y = int(float(self.prop_vars["Y"].get()))
            w = int(float(self.prop_vars["W"].get()))
            h = int(float(self.prop_vars["H"].get()))
        except (TypeError, ValueError):
            messagebox.showwarning("输入无效", "X / Y / W / H 必须是整数。")
            self.sync_props()
            return
        if path == "":
            w = max(40, w)
            h = max(24, h)
        else:
            w = max(2, w)
            h = max(2, h)
        changes = {}
        if x != cur["x"]:
            changes["x"] = x
        if y != cur["y"]:
            changes["y"] = y
        if w != cur["w"]:
            changes["w"] = w
        if h != cur["h"]:
            changes["h"] = h
        if bool(self.prop_vis.get()) != bool(cur["visible"]):
            changes["visible"] = bool(self.prop_vis.get())
        if changes:
            self.set_geom(key, path, **changes)
            self.redraw()
            self.update_status()
            self.autosave_now()
        self.sync_props()

    def fit_and_redraw(self):
        self.fit_view()
        self.redraw()
        self.canvas.xview_moveto(0)
        self.canvas.yview_moveto(0)

    # ------------------------------------------------------------- 坐标换算
    def m2c(self, x, y):
        return (x + MARGIN) * self.scale, (y + MARGIN) * self.scale

    def c2m(self, cx, cy):
        return cx / self.scale - MARGIN, cy / self.scale - MARGIN

    # --------------------------------------------------------------- 绘制
    def redraw(self):
        c = self.canvas
        c.delete("all")
        S = self.scale
        _cx, _cy, cw, ch = self.container_rect()
        c.configure(scrollregion=(0, 0, int((cw + MARGIN * 2) * S), int((ch + MARGIN * 2) * S)))
        x0, y0 = self.m2c(0, 0)
        x1, y1 = self.m2c(cw, ch)

        # 背景网格（每 50px 一格）
        step = 50
        gx = 0
        while gx <= cw:
            px, _ = self.m2c(gx, 0)
            c.create_line(px, y0, px, y1, fill="#33333a")
            gx += step
        gy = 0
        while gy <= ch:
            _, py = self.m2c(0, gy)
            c.create_line(x0, py, x1, py, fill="#33333a")
            gy += step

        # 容器边框 + 标题
        c.create_rectangle(x0, y0, x1, y1, outline="#6f7f9f", width=2)
        c.create_text(x0 + 6, y0 + 6, anchor="nw",
                      text="%s     容器 %d×%d" % (self.crumb(), cw, ch),
                      fill="#9fb0c8", font=FONT_SMALL)

        boxes = self.view_children()
        if not boxes:
            c.create_text((x0 + x1) / 2, (y0 + y1) / 2,
                          text="该容器没有可调的子组件\n（返回上级继续调整）",
                          fill="#c8c8d0", font=FONT, justify="center")
        for key, path in boxes:
            e = self.eff(key, path)
            px0, py0 = self.m2c(e["x"], e["y"])
            px1, py1 = self.m2c(e["x"] + e["w"], e["y"] + e["h"])
            sel = (self.sel == (key, path))
            vis = bool(e["visible"])
            fill = self.blocks[key]["color"] if path == "" else "#23232b"
            if not vis:
                fill = "#3a3a40"
            outline = "#ffffff" if sel else ("#ff6b6b" if not vis else "#0d0d10")
            if not vis:
                c.create_rectangle(px0, py0, px1, py1, fill=fill, outline=outline,
                                   width=2, dash=(4, 3))
            else:
                c.create_rectangle(px0, py0, px1, py1, fill=fill, outline=outline,
                                   width=3 if sel else 2)

            label = self.name_of(key, path)
            mid_x, mid_y = (px0 + px1) / 2, (py0 + py1) / 2
            if (py1 - py0) > 14:
                c.create_text(mid_x, mid_y - 7, text=str(label)[:16], fill="#ffffff", font=FONT_BOLD)
            info = "%d,%d  %d×%d" % (e["x"], e["y"], e["w"], e["h"])
            if (py1 - py0) > 34 and (px1 - px0) > 70:
                if path == "":
                    cs = self.blocks[key]["cs"]
                    if abs(cs - 1.0) > 1e-9:
                        info += "  ×%.2f" % cs
                nkid = len(self.kids_of(key, path))
                if nkid:
                    info += "   ▸%d" % nkid
                c.create_text(mid_x, mid_y + 9, text=info, fill="#e8e8e8", font=FONT_SMALL)
            if sel:      # 右下角把手
                s = 7
                c.create_rectangle(px1 - s, py1 - s, px1, py1, fill="#ffffff", outline="#101014")

        c.create_text(x0 - 4, y0 - 4, text="(0,0)", anchor="se", fill="#9a9aa8", font=FONT_SMALL)
        self.update_crumb()

    def update_crumb(self):
        try:
            self.crumb_lbl.configure(text=self.crumb() + "   输出 " +
                                     os.path.basename(self.out_path))
        except Exception:
            pass

    # --------------------------------------------------------------- 交互
    def hit(self, mx, my):
        """命中的子组件 → (key, path, mode)，mode = move / resize"""
        handle = 9 / max(self.scale, 0.05)
        boxes = self.view_children()
        for key, path in reversed(boxes):
            e = self.eff(key, path)
            if (e["x"] + e["w"] - handle <= mx <= e["x"] + e["w"]
                    and e["y"] + e["h"] - handle <= my <= e["y"] + e["h"]):
                return key, path, "resize"
        for key, path in reversed(boxes):
            e = self.eff(key, path)
            if e["x"] <= mx <= e["x"] + e["w"] and e["y"] <= my <= e["y"] + e["h"]:
                return key, path, "move"
        return None, None, None

    def on_press(self, ev):
        self.canvas.focus_set()
        mx, my = self.c2m(self.canvas.canvasx(ev.x), self.canvas.canvasy(ev.y))
        key, path, mode = self.hit(mx, my)
        self.sel = (key, path) if key else None
        if key:
            e = self.eff(key, path)
            if mode == "move":
                self.drag = dict(mode="move", key=key, path=path,
                                 sx=mx, sy=my, base=(e["x"], e["y"], e["w"], e["h"]))
            else:
                self.drag = dict(mode="resize", key=key, path=path,
                                 sx=mx, sy=my, base=(e["x"], e["y"], e["w"], e["h"]))
        else:
            self.drag = None
        self.sync_tree_selection()
        self.sync_props()
        self.redraw()
        self.update_status()

    def on_double(self, ev):
        mx, my = self.c2m(self.canvas.canvasx(ev.x), self.canvas.canvasy(ev.y))
        key, path, _mode = self.hit(mx, my)
        if key and self.kids_of(key, path):
            self.drill_to(key, path)
        elif key:
            self.status.configure(text="%s 没有子组件；它自己的位置/尺寸可在属性栏改。"
                                       % self.name_of(key, path))

    def on_motion(self, ev):
        mx, my = self.c2m(self.canvas.canvasx(ev.x), self.canvas.canvasy(ev.y))
        if self.drag:
            d = self.drag
            bx, by, bw, bh = d["base"]
            dx, dy = mx - d["sx"], my - d["sy"]
            if d["mode"] == "move":
                nx = max(0, int(round(bx + dx)))
                ny = max(0, int(round(by + dy)))
                if (nx, ny) != (bx, by):
                    self.set_geom(d["key"], d["path"], x=nx, y=ny)
            else:
                if d["path"] == "":
                    nw = max(40, int(round(bw + dx)))
                    nh = max(24, int(round(bh + dy)))
                else:
                    nw = max(2, int(round(bw + dx)))
                    nh = max(2, int(round(bh + dy)))
                if (nw, nh) != (bw, bh):
                    self.set_geom(d["key"], d["path"], w=nw, h=nh)
            self.redraw()
            self.sync_props()
            self.update_status()
            return
        key, path, mode = self.hit(mx, my)
        self.canvas.configure(cursor="sizing" if mode == "resize" else
                              ("fleur" if mode == "move" else ""))

    def on_release(self, _ev):
        if self.drag:
            self.drag = None
            self.autosave_now()

    def on_hover(self, ev):
        mx, my = self.c2m(self.canvas.canvasx(ev.x), self.canvas.canvasy(ev.y))
        self.status.configure(text="鼠标 (%d, %d)" % (mx, my))

    def on_wheel(self, ev):
        if ev.state & 0x0004:      # Ctrl = 缩放
            self.zoom(1.15 if ev.delta > 0 else 1 / 1.15)
        else:                       # 否则滚动
            self.canvas.yview_scroll(-1 if ev.delta > 0 else 1, "units")

    def zoom(self, factor):
        self.scale = max(ZOOM_MIN, min(ZOOM_MAX, self.scale * factor))
        self.redraw()

    # ----------------------------------------------------------- 内容缩放
    def sel_block_key(self):
        return self.sel[0] if self.sel else None

    def sync_scale_slider(self):
        key = self.sel_block_key()
        if not key:
            self.scale_slider.configure(state=tk.DISABLED)
            return
        self.scale_slider.configure(state=tk.NORMAL)
        self._updating_scale = True
        try:
            self.scale_var.set(round(self.blocks[key]["cs"], 2))
        finally:
            self._updating_scale = False

    def on_scale_changed(self, value):
        if self._updating_scale:
            return
        key = self.sel_block_key()
        if not key:
            return
        s = _clamp_scale(value)
        if abs(self.blocks[key]["cs"] - s) < 1e-9:
            return
        self.blocks[key]["cs"] = s
        self.redraw()
        self.update_status()
        self.autosave_now()

    def adj_scale(self, sign, state):
        key = self.sel_block_key()
        if not key:
            return
        step = 0.25 if (state & 0x0001) else 0.05
        s = _clamp_scale(self.blocks[key]["cs"] + sign * step)
        self.blocks[key]["cs"] = s
        self.sync_scale_slider()
        self.redraw()
        self.update_status()
        self.autosave_now()

    def nudge(self, dx, dy, state):
        if not self.sel:
            return
        step = 10 if (state & 0x0001) else 1     # Shift = 10px
        key, path = self.sel
        e = self.eff(key, path)
        self.set_geom(key, path,
                      x=max(0, e["x"] + dx * step), y=max(0, e["y"] + dy * step))
        self.redraw()
        self.sync_props()
        self.update_status()
        self.autosave_now()

    # --------------------------------------------------------------- 保存
    def to_json(self):
        panels = {}
        for key in self.order:
            b = self.blocks[key]
            entry = {"x": int(b["x"]), "y": int(b["y"]), "w": int(b["w"]), "h": int(b["h"]),
                     "content_scale": round(float(b["cs"]), 2)}
            if not b["visible"]:
                entry["visible"] = False
            ov = self.ovr.get(key) or {}
            kids_out, nodes_out = {}, {}
            for path, o in ov.items():
                node = self.node_of(key, path)
                cls = (node or {}).get("cls", "")
                full = self.eff(key, path)
                item = {"cls": cls, "x": int(full["x"]), "y": int(full["y"]),
                        "visible": bool(full["visible"])}
                if "w" in o:
                    item["w"] = int(full["w"])
                if "h" in o:
                    item["h"] = int(full["h"])
                nodes_out[path] = item
                if "/" not in path:      # 第一层同时写进 children（模拟器靠它切成绝对布局）
                    kids_out[path] = {"cls": cls, "x": int(full["x"]), "y": int(full["y"])}
            if kids_out:
                entry["children"] = kids_out
            if nodes_out:
                entry["nodes"] = nodes_out
            panels[key] = entry
        page = self.pages[self.page_idx]
        return {
            "mode": "absolute",
            # v2：模拟器按这个版本号判断"方块 id 语义"。没有它的一律当旧文件忽略
            # （旧版的方块 id 是滚动面板的视口/滚动条，套到新 id 上会摆错组件）
            "layoutVersion": 2,
            "generator": "RBMK-1500 layout editor (python)",
            "page": page["id"],
            "saved_at": time.strftime("%Y-%m-%d %H:%M:%S"),
            "canvas": {"w": self.canvas_size[0], "h": self.canvas_size[1]},
            "panels": panels,
        }

    def write_file(self):
        tmp = self.out_path + ".tmp"
        with open(tmp, "w", encoding="utf-8", newline="\n") as f:
            json.dump(self.to_json(), f, ensure_ascii=False, indent=2)
            f.write("\n")
        os.replace(tmp, self.out_path)   # 原子替换：模拟器读到的要么旧要么新，不会是半截
        self.save_label.configure(text="已保存 %s" % time.strftime("%H:%M:%S"))

    def autosave_now(self):
        if self.autosave.get():
            try:
                self.write_file()
            except Exception as e:
                self.save_label.configure(text="保存失败: %s" % e)

    def save_manual(self):
        try:
            self.write_file()
        except Exception as e:
            messagebox.showerror("保存失败", str(e))

    # ------------------------------------------------------------ 其它操作
    def reset_defaults(self):
        """方块与所有层级组件回到出厂默认（删掉本页所有覆盖值）"""
        page = self.pages[self.page_idx]
        for b in page["blocks"]:
            if b["key"] in self.blocks:
                self.blocks[b["key"]].update(b["d"])
                self.blocks[b["key"]]["visible"] = True
        self.ovr.clear()
        self.sync_scale_slider()
        self.redraw()
        self.sync_props()
        if os.path.isfile(self.out_path):
            self.autosave_now()      # 已有文件时写出，让模拟器实时恢复
        self.update_status()

    def delete_file(self):
        if not os.path.isfile(self.out_path):
            return
        if messagebox.askyesno("删除布局文件",
                               "删除后模拟器将实时恢复默认 GroupLayout 布局。\n确定删除？"):
            os.remove(self.out_path)
            self.save_label.configure(text="已删除文件（模拟器恢复默认布局）")

    def reload_file(self):
        """重新读布局文件 + 重新读页面清单（模拟器可能刚导出新的组件树）"""
        self.pages = _load_pages(os.path.join(self.base_dir, "ui_layout.json"))
        cur = self.pages[self.page_idx]["id"] if self.pages else "main"
        # 方块/组件树可能变了，整页状态重建（保留用户的页面选择）
        self.page_state.clear()
        self._loaded.clear()
        idx = next((i for i, p in enumerate(self.pages) if p["id"] == cur), 0)
        self._apply_page(idx, initial=True)
        self.page_box.configure(values=[p["title"] for p in self.pages])
        self.page_box.current(idx)
        self.page_var.set(self.pages[idx]["title"])
        self.build_tree()
        self.fit_view()
        self.redraw()
        self.sync_props()
        self.update_status()
        self.save_label.configure(text="已重新载入（%d 页）" % len(self.pages))

    def reload_pages(self):
        """只刷新页面清单（新开过窗口后点这个）"""
        self.reload_file()

    def on_page_changed(self, _ev=None):
        idx = self.page_box.current()
        if idx < 0 or idx == self.page_idx:
            return
        self._apply_page(idx)
        self.page_var.set(self.pages[idx]["title"])
        self.build_tree()
        self.sync_scale_slider()
        self.fit_view()
        self.redraw()
        self.update_status()

    def update_status(self):
        self.sync_scale_slider()
        if self.sel:
            key, path = self.sel
            e = self.eff(key, path)
            node = self.node_of(key, path) or {}
            cls = node.get("cls") or self.blocks[key]["cls"] or ""
            nkid = len(self.kids_of(key, path))
            extra = ""
            if path == "":
                cs = self.blocks[key]["cs"]
                extra = "   内容缩放 ×%.2f%s" % (cs, "（默认）" if abs(cs - 1.0) < 1e-9 else "")
            self.status.configure(
                text="%s · %s [%s]   x=%d y=%d %d×%d%s%s   （方向键微调 / 双击进入内部）"
                     % (self.crumb(), self.name_of(key, path), cls or "?",
                        e["x"], e["y"], e["w"], e["h"],
                        "" if e["visible"] else "  [已隐藏]",
                        ("   ▸%d 个子组件" % nkid) if nkid else "") + extra)
        else:
            self.status.configure(
                text="未选中（左侧树或画布单击选中；拖动改位置、右下角改尺寸、双击进入内部）")


def main():
    argv = sys.argv[1:]
    debug_info = "--debug-info" in argv
    out = DEFAULT_OUT
    if "--out" in argv:
        i = argv.index("--out")
        if i + 1 < len(argv):
            out = argv[i + 1]
    else:
        rest = [a for a in argv if not a.startswith("--")]
        if rest:
            out = rest[0]

    if "--selftest" in argv:
        # 无窗口自检：验证清单解析 / 组件树 / 写盘结构（供自动化检查用）
        root = tk.Tk()
        root.withdraw()
        ed = LayoutEditor(root, out)

        def count_nodes(kids):
            return sum(1 + count_nodes(c["children"]) for c in kids)

        total_nodes = sum(count_nodes(b["kids"])
                          for page in ed.pages for b in page["blocks"])
        # 找一个"第二层"节点，造一次覆盖，验证写盘结构（children + nodes）后撤销
        target = None
        for i, page in enumerate(ed.pages):
            for b in page["blocks"]:
                for c in b["kids"]:
                    for gc in c["children"]:
                        target = (i, b["key"], "%s/%s" % (c["i"], gc["i"]))
                        break
                    if target:
                        break
                if target:
                    break
            if target:
                break
        sample = "(no deep node)"
        if target:
            i, key, path = target
            ed._apply_page(i, initial=False)
            ed.build_tree()
            node = ed.node_of(key, path)
            ed.set_geom(key, path, x=node["x"] + 3, y=node["y"] + 2,
                        w=node["w"], h=node["h"])
            js = ed.to_json()["panels"][key]
            sample = "%s/%s children=%d nodes=%d %s" % (
                key, path, len(js.get("children") or {}), len(js.get("nodes") or {}),
                (js.get("nodes") or {}).get(path))
            ed.ovr.get(key, {}).pop(path, None)
        print("SELFTEST_OK pages=%d active=%s blocks=%d nodes=%d %s out=%s exists=%s"
              % (len(ed.pages), ed.pages[ed.page_idx]["id"], len(ed.blocks),
                 total_nodes, sample,
                 os.path.abspath(ed.out_path), os.path.isfile(ed.out_path)), flush=True)
        root.destroy()
        return

    root = tk.Tk()
    ed = LayoutEditor(root, out)
    if "--page" in argv:
        i = argv.index("--page")
        if i + 1 < len(argv):
            want = argv[i + 1]
            for k, page in enumerate(ed.pages):
                if page["id"] == want or page["title"] == want:
                    ed.page_box.current(k)
                    ed.on_page_changed()
                    break
    drill = None
    if "--drill" in argv:
        i = argv.index("--drill")
        if i + 1 < len(argv):
            drill = argv[i + 1]
    elif "--expand" in argv:      # 兼容旧参数：--expand 方块id
        i = argv.index("--expand")
        if i + 1 < len(argv):
            drill = argv[i + 1]
    if drill:
        segs = drill.split("/")
        key, path = segs[0], "/".join(segs[1:])
        if key in ed.blocks:
            if ed.kids_of(key, path):
                ed.drill_to(key, path)
            else:
                ed.sel = (key, path)
                ed.view_key = key
                ed.view_path = _parent_path(path)
                ed.sync_tree_selection()
                ed.fit_view()
                ed.redraw()
    if debug_info:
        def _dump():
            root.update_idletasks()
            c = ed.canvas
            print("DEBUG canvas_origin=%d,%d scale=%.3f margin=%d view=%s/%s"
                  % (c.winfo_rootx(), c.winfo_rooty(), ed.scale, MARGIN,
                     ed.view_key, ed.view_path), flush=True)
        root.after(700, _dump)
    root.mainloop()


if __name__ == "__main__":
    main()
