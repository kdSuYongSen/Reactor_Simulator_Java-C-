# -*- coding: utf-8 -*-
"""扫描源码里所有 Localization.t("...") 调用，得到权威的键清单。
输出 tools/i18n/keys.json（键 -> 英文原文）。
英文原文来源优先级：Bundle.properties > app_en.properties > 键本身（字面量键）。
"""
import os, re, json

BASE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.abspath(os.path.join(BASE, '..', '..', 'src', 'main', 'java', 'com', 'darwish', 'nppsim'))


def unesc(s):
    return re.sub(r'\\u([0-9a-fA-F]{4})', lambda m: chr(int(m.group(1), 16)), s)


def read_props(path):
    d = {}
    if not os.path.exists(path):
        return d
    for ln in open(path, encoding='utf-8', errors='replace'):
        ln = ln.rstrip('\n')
        if not ln or ln.lstrip().startswith('#') or '=' not in ln:
            continue
        k, v = ln.split('=', 1)
        d[k.strip()] = unesc(v)
    return d


def main():
    bundle = read_props(os.path.join(SRC, 'Bundle.properties'))
    app = read_props(os.path.join(BASE, 'app_en.properties'))

    # 匹配 Localization.t("...")，字符串里可能含 \uXXXX 转义
    pat = re.compile(r'Localization\.t\(\s*"((?:[^"\\]|\\u[0-9a-fA-F]{4})*)"')
    keys = set()
    per_file = {}
    for fn in sorted(os.listdir(SRC)):
        if not fn.endswith('.java'):
            continue
        t = open(os.path.join(SRC, fn), encoding='utf-8').read()
        found = {unesc(m.group(1)) for m in pat.finditer(t)}
        if found:
            per_file[fn] = len(found)
            keys |= found

    key_en = {}
    for k in sorted(keys):
        if not k.strip():
            continue
        # 注意：Bundle 中"值为空"的键（如 XXX.jTextField11.text=）必须保留空值，
        # 不能回退成键名，否则界面上会显示键名。
        if k in bundle:
            key_en[k] = bundle[k]
        elif k in app:
            key_en[k] = app[k]
        else:
            key_en[k] = k          # 字面量键：键即英文原文
    json.dump(key_en, open(os.path.join(BASE, 'keys.json'), 'w', encoding='utf-8'),
              ensure_ascii=False, indent=0, sort_keys=True)

    print(f"源码中 Localization.t(...) 键 = {len(key_en)}（来自 {len(per_file)} 个文件）")
    print("  其中 Bundle 键:", sum(1 for k in key_en if k in bundle and bundle[k].strip()))
    print("       app 键   :", sum(1 for k in key_en if k in app and app[k].strip()))
    print("       字面量键 :", sum(1 for k in key_en if k not in bundle and k not in app))
    print("  文件分布:", sorted(per_file.items(), key=lambda x: -x[1])[:10])
    missing_en = [k for k, v in key_en.items() if v == k and k in bundle]
    if missing_en:
        print("  ⚠ Bundle 里值为空的键:", missing_en[:10])


if __name__ == '__main__':
    main()
