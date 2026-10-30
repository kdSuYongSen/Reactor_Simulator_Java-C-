# -*- coding: utf-8 -*-
"""把 tools/i18n/dict_<lang>.txt（制表符分隔：英文原文<TAB>译文）转成 dict_<lang>.json。
这样维护翻译时只需编辑 txt（每行一条，带 # 的行为注释）。"""
import json, os, sys

BASE = os.path.dirname(os.path.abspath(__file__))


def main(lang):
    src = os.path.join(BASE, f'dict_{lang}.txt')
    dst = os.path.join(BASE, f'dict_{lang}.json')
    if not os.path.exists(src):
        print("缺少", src)
        return 1
    d = {}
    bad = 0
    for i, ln in enumerate(open(src, encoding='utf-8'), 1):
        ln = ln.rstrip('\n')
        if not ln.strip() or ln.lstrip().startswith('#'):
            continue
        if '|' not in ln:
            print(f"  第{i}行缺少 | 分隔符，已跳过: {ln[:60]!r}")
            bad += 1
            continue
        en, zh = ln.split('|', 1)
        # 注意：key 不能 strip —— 有些界面文本本身以空格结尾（如 "AZ-1 Trip Signal: "），
        # strip 掉就会与运行时键不匹配，导致该条永远翻译不上。
        d[en] = zh.strip()
    json.dump(d, open(dst, 'w', encoding='utf-8'), ensure_ascii=False, indent=0, sort_keys=True)
    print(f"{lang}: {len(d)} 条 -> {os.path.basename(dst)}" + (f"（{bad} 行有问题）" if bad else ""))
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1] if len(sys.argv) > 1 else 'zh_CN'))
