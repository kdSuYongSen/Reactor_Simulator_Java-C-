# -*- coding: utf-8 -*-
"""检查 UI.java 中每个 GroupLayout 的横向/竖向组组件集是否一致（防 'not attached to a horizontal group' 崩溃）。
用法：python check_grouplayout.py [UI.java 路径]，不一致时退出码 1。"""
import re, sys, io

def block_of(src, layout_var, axis):
    marker = layout_var + ".set" + axis + "Group("
    start = src.find(marker)
    if start < 0:
        return None
    i = start + len(marker) - 1   # 指向 "("
    depth = 0
    j = i
    while j < len(src):
        c = src[j]
        if c == "(":
            depth += 1
        elif c == ")":
            depth -= 1
            if depth == 0:
                return src[start:j + 1]
        j += 1
    return None

def components_of(block):
    return set(re.findall(r'addComponent\((\w+)[,)]', block))

def main(path):
    src = io.open(path, encoding="utf-8").read()
    layout_vars = sorted(set(re.findall(r'(\w+Layout)\.setHorizontalGroup', src)))
    bad = 0
    for lv in layout_vars:
        h_block, v_block = block_of(src, lv, "Horizontal"), block_of(src, lv, "Vertical")
        if h_block is None or v_block is None:
            print("[MISS] %s 缺少横向或竖向组" % lv)
            bad += 1
            continue
        h, v = components_of(h_block), components_of(v_block)
        only_h, only_v = h - v, v - h
        if only_h or only_v:
            print("[FAIL] %s：横向独有=%s 竖向独有=%s" % (lv, sorted(only_h), sorted(only_v)))
            bad += 1
        else:
            print("[OK] %s：%d 个组件横竖一致" % (lv, len(h)))
    return bad

if __name__ == "__main__":
    path = sys.argv[1] if len(sys.argv) > 1 else "src/main/java/com/darwish/nppsim/UI.java"
    sys.exit(1 if main(path) else 0)
