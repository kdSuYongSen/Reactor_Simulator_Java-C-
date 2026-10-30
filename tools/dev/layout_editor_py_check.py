# -*- coding: utf-8 -*-
"""Python 布局编辑器的自动化核对：写盘 → 交给模拟器验证。

用法（在 SourceCode 目录下）：
    <python> tools/dev/layout_editor_py_check.py

做的事：
  1) 用 tools/layout_editor/layout_editor.py 的模型，切到 CondensateUI 页；
  2) 给方块 5 设一个第一层子组件位置 + 一个深层节点 0/2/2 = (32,10,21,22)；
  3) 写出 target/CondensateUI.layout.json（DeepApplyCheck 读的就是它）；
  4) 打印写出的 children / nodes 结构，供人核对。
"""
import os
import sys
import json

HERE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.abspath(os.path.join(HERE, "..", ".."))
sys.path.insert(0, os.path.join(SRC, "tools", "layout_editor"))

import layout_editor as le  # noqa: E402

OUT = os.path.join(SRC, "target", "ui_layout.json")


def main():
    import tkinter as tk
    root = tk.Tk()
    root.withdraw()
    ed = le.LayoutEditor(root, OUT)

    idx = next(i for i, p in enumerate(ed.pages) if p["id"] == "CondensateUI")
    ed.page_box.current(idx)
    ed.on_page_changed()

    assert "5" in ed.blocks, "CondensateUI 缺少方块 5"
    # 第一层子组件：把 0 号挪到 (2,2)
    first = ed.blocks["5"]["kids"][0]
    ed.set_geom("5", str(first["i"]), x=2, y=2)
    # 深层节点：0/2/2 摆到 (32,10) 21×22
    node = ed.node_of("5", "0/2/2")
    assert node is not None, "清单里没有 0/2/2"
    ed.set_geom("5", "0/2/2", x=32, y=10, w=21, h=22)
    ed.write_file()

    data = json.load(open(ed.out_path, encoding="utf-8"))
    panel = data["panels"]["5"]
    print("written:", ed.out_path)
    print("layoutVersion:", data.get("layoutVersion"), "page:", data.get("page"))
    print("children:", json.dumps(panel.get("children"), ensure_ascii=False))
    print("nodes:", json.dumps(panel.get("nodes"), ensure_ascii=False))
    print("blocks:", len(data["panels"]))
    print("PY_WRITE_OK")
    root.destroy()


if __name__ == "__main__":
    main()
