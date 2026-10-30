# -*- coding: utf-8 -*-
"""自检：校验 .vscode 下所有 JSON/JSONC 配置文件的语法与重复键。"""
import json, glob, sys, collections, io

BASE = r"D:\RBMK\RBMK1500\.vscode"

def strip_jsonc(s):
    out, i, n, instr = [], 0, len(s), False
    while i < n:
        c = s[i]
        if instr:
            out.append(c)
            if c == '\\' and i + 1 < n:
                out.append(s[i + 1]); i += 2; continue
            if c == '"':
                instr = False
            i += 1; continue
        if c == '"':
            instr = True; out.append(c); i += 1; continue
        if c == '/' and i + 1 < n and s[i + 1] == '/':
            while i < n and s[i] != '\n':
                i += 1
            continue
        if c == '/' and i + 1 < n and s[i + 1] == '*':
            i += 2
            while i + 1 < n and not (s[i] == '*' and s[i + 1] == '/'):
                i += 1
            i += 2; continue
        out.append(c); i += 1
    return ''.join(out)

def main():
    ok = True
    files = sorted(glob.glob(BASE + r"\*.json"))
    for f in files:
        name = f.replace(BASE + "\\", "")
        try:
            raw = io.open(f, encoding="utf-8").read()
            cleaned = strip_jsonc(raw)
            dups = []
            def hook(pairs, dups=dups):
                keys = [k for k, _ in pairs]
                dups.extend([k for k, c in collections.Counter(keys).items() if c > 1])
                return dict(pairs)
            d = json.loads(cleaned, object_pairs_hook=hook)
            if dups:
                print("WARN %s: 重复键=%s" % (name, sorted(set(dups))))
                ok = False
            else:
                print("OK   %s: %d keys" % (name, len(d)))
        except Exception as e:
            print("FAIL %s: %s" % (name, e))
            ok = False
    return 0 if ok else 1

if __name__ == "__main__":
    sys.exit(main())
