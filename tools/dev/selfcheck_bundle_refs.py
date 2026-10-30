# -*- coding: utf-8 -*-
"""自检：校验源码中所有 NbBundle.getMessage / getAccessibleBundleMessage 引用的键
是否都存在于 com/darwish/nppsim/Bundle.properties（新 jar 版）。"""
import io, re, os, sys

ROOT = r"D:\RBMK\RBMK1500\SourceCode\src\main\java\com\darwish\nppsim"
BUNDLE = r"D:\RBMK\RBMK1500\SourceCode\tools\dev\bundle-new.properties"

def load_keys(p):
    ks = {}
    for line in io.open(p, encoding="latin-1"):
        s = line.strip()
        if not s or s.startswith("#"):
            continue
        if "=" in s:
            ks[s.split("=", 1)[0].strip()] = True
    return ks

def main():
    keys = load_keys(BUNDLE)
    print("Bundle keys:", len(keys))

    pat = re.compile(r'NbBundle\.(?:getMessage|getAccessibleBundleMessage)\(\s*([A-Za-z0-9_$.]+)\.class\s*,\s*"([^"]+)"')
    refs = {}         # key -> [files]
    dynamic = []      # 动态拼接的调用
    for fn in sorted(os.listdir(ROOT)):
        if not fn.endswith(".java"):
            continue
        src = io.open(os.path.join(ROOT, fn), encoding="utf-8", errors="replace").read()
        # 去掉行注释避免误报（保守：只去掉整行注释）
        for m in pat.finditer(src):
            key = m.group(2)   # 源码中的第二参数即完整键（已含类名前缀）
            refs.setdefault(key, []).append(fn)
        # 动态拼接：NbBundle.getMessage(X.class, "prefix" + ...)
        for m in re.finditer(r'NbBundle\.(?:getMessage|getAccessibleBundleMessage)\(\s*([A-Za-z0-9_$.]+)\.class\s*,\s*([^)]+)\)', src):
            arg2 = m.group(2).strip()
            if not arg2.startswith('"'):
                dynamic.append((fn, arg2[:80]))

    missing = {k: v for k, v in refs.items() if k not in keys}
    print("静态引用键总数:", len(refs))
    print("缺失键数:", len(missing))
    for k in sorted(missing):
        print("  MISSING:", k, "<-", sorted(set(missing[k])))
    print("动态拼接调用:", len(dynamic))
    for fn, a in dynamic:
        print("  DYN:", fn, "->", a)
    return 1 if missing or dynamic else 0

if __name__ == "__main__":
    sys.exit(main())
