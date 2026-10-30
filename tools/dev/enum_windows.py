# -*- coding: utf-8 -*-
"""枚举指定 PID 的所有顶层窗口（标题/可见性/类名）。"""
import ctypes
import sys
from ctypes import wintypes

user32 = ctypes.windll.user32

def enum_windows(pid):
    user32.SetProcessDPIAware()
    results = []

    EnumWindowsProc = ctypes.WINFUNCTYPE(ctypes.c_bool, wintypes.HWND, wintypes.LPARAM)

    def cb(hwnd, lparam):
        wpid = wintypes.DWORD()
        user32.GetWindowThreadProcessId(hwnd, ctypes.byref(wpid))
        if wpid.value == pid:
            n = user32.GetWindowTextLengthW(hwnd)
            buf = ctypes.create_unicode_buffer(n + 1)
            user32.GetWindowTextW(hwnd, buf, n + 1)
            cls = ctypes.create_unicode_buffer(256)
            user32.GetClassNameW(hwnd, cls, 256)
            results.append((hwnd, bool(user32.IsWindowVisible(hwnd)), cls.value, buf.value,
                            bool(user32.IsIconic(hwnd))))
        return True

    user32.EnumWindows(EnumWindowsProc(cb), 0)
    return results

def main():
    pid = int(sys.argv[1])
    ws = enum_windows(pid)
    print("PID %d 顶层窗口数: %d" % (pid, len(ws)))
    for hwnd, vis, cls, title, iconic in ws:
        print("  hwnd=0x%X visible=%s minimized=%s class=[%s] title=[%s]" % (hwnd, vis, iconic, cls, title))
    return 0

if __name__ == "__main__":
    sys.exit(main())
