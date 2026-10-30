"""Python 工具链冒烟测试：验证解释器与常用标准库可用。"""
import sys
import json
import platform
from pathlib import Path


def main() -> int:
    print("Python toolchain OK")
    print(f"  version : {sys.version.split()[0]}")
    print(f"  impl    : {platform.python_implementation()}")
    print(f"  exe     : {sys.executable}")

    # 验证标准库与项目真实资源可读
    res = Path(__file__).resolve().parent.parent / "src/main/java/res"
    json_files = sorted(res.glob("*.json")) if res.is_dir() else []
    print(f"  project res json: {[p.name for p in json_files]}")

    payload = {"lang": "Python", "ok": True}
    print(f"  json roundtrip: {json.loads(json.dumps(payload))}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
