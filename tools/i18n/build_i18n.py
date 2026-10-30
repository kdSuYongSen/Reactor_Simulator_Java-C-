# -*- coding: utf-8 -*-
"""由英文原文 + 各语言词典生成 i18n/<lang>.json（UTF-8）。

输入：
  tools/i18n/strings_en.json   —— 英文原文总表（键 -> 英文）
  tools/i18n/dict_<lang>.json  —— 词典（英文原文 -> 译文），按文本去重，翻译量最小
输出：
  src/main/java/com/darwish/nppsim/i18n/<lang>.json —— 键 -> 译文（供 Localization 直接查表）
"""
import json, os, re, sys

BASE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.abspath(os.path.join(BASE, '..', '..', 'src', 'main', 'java', 'com', 'darwish', 'nppsim'))
OUT = os.path.join(SRC, 'i18n')
LANGS = ['en', 'zh_CN', 'zh_TW', 'ru', 'fr', 'ja', 'ko']


def unesc(s):
    """properties 里的 \\uXXXX 转义还原成真实字符"""
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
    import subprocess, sys as _sys
    # 1) 先扫描源码，得到权威键清单 tools/i18n/keys.json
    subprocess.run([_sys.executable, os.path.join(BASE, 'collect_keys.py')], check=False)
    key_en = json.load(open(os.path.join(BASE, 'keys.json'), encoding='utf-8'))
    bundle = read_props(os.path.join(SRC, 'Bundle.properties'))
    print(f"键总数 = {len(key_en)}（其中 Bundle 键 {sum(1 for k in key_en if k in bundle)}）")

    os.makedirs(OUT, exist_ok=True)
    for lang in LANGS:
        dic = {}
        dp = os.path.join(BASE, f'dict_{lang}.json')
        if lang != 'en' and os.path.exists(dp):
            dic = json.load(open(dp, encoding='utf-8'))
        out = {}
        for k, en in key_en.items():
            # 注意：英文原文为空的键也要写进 JSON（值给空串），
            # 否则运行时会回退成"显示键名"这种丑陋结果。
            out[k] = en if lang == 'en' else (dic.get(en, en) if en.strip() else '')
        with open(os.path.join(OUT, f'{lang}.json'), 'w', encoding='utf-8') as f:
            json.dump(out, f, ensure_ascii=False, indent=0, sort_keys=True)
        untranslated = sum(1 for k, en in key_en.items() if en.strip() and lang != 'en' and dic.get(en, en) == en)
        print(f"  {lang}.json: {len(out)} 条, 词典 {len(dic)} 条, 未翻译(回退英文) {untranslated} 条")

    # 报告缺失的词典条目（需要翻译但词典里没有的英文原文）
    en_texts = sorted({v for v in key_en.values() if v.strip()})
    for lang in LANGS:
        if lang == 'en':
            continue
        dp = os.path.join(BASE, f'dict_{lang}.json')
        dic = json.load(open(dp, encoding='utf-8')) if os.path.exists(dp) else {}
        miss = [t for t in en_texts if t not in dic]
        if miss:
            print(f"\n[{lang}] 词典缺少 {len(miss)} 条:")
            for m in miss[:400]:
                print("   ", repr(m))


if __name__ == '__main__':
    main()
