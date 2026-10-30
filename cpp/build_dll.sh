#!/bin/bash
# rbmk_core.dll 双架构构建 + 自检（防架构错标）
# 用法: bash build_dll.sh   （编译器: Dev-Cpp TDM-GCC 9.2）
set -e
GPP="E:/Dev-Cpp/TDM-GCC-64/bin/g++.exe"
OBJDUMP="E:/Dev-Cpp/TDM-GCC-64/bin/objdump.exe"
NATIVE="D:/RBMK/RBMK-1500-Simulator/native"
OUT="D:/RBMK/RBMK1500/SourceCode/src/main/resources/res/native"
JDK="C:/Program Files/Java/jdk-17"

COMMON=(-shared -O2 -std=c++11 -static -static-libgcc -static-libstdc++ -Wl,--kill-at
  "-I$JDK/include" "-I$JDK/include/win32" "-I$NATIVE")

# x64
"$GPP" "${COMMON[@]}" "$NATIVE/rbmk_core.cpp" "$NATIVE/rbmk_jni.cpp" -o "$OUT/rbmk_core_x64.dll"
# x86（注意 -m32 必须有，否则产物是 64 位——曾因漏 -m32 导致 32 位 JVM 崩溃）
"$GPP" -m32 "${COMMON[@]}" "$NATIVE/rbmk_core.cpp" "$NATIVE/rbmk_jni.cpp" -o "$OUT/rbmk_core_x86.dll"

# 自检：架构断言 + 导出计数
check() {
  local f="$1" want="$2"
  local arch=$("$OBJDUMP" -f "$f" | grep -o "pei-i386\|pei-x86-64")
  local n=$("$OBJDUMP" -p "$f" | grep -cE "Java_com_rbmk_real")
  echo "$f: arch=$arch exports=$n"
  [ "$arch" = "$want" ] || { echo "!! 架构错误: 期望 $want"; exit 1; }
  [ "$n" = "19" ] || { echo "!! 导出数错误: 期望 19"; exit 1; }
}
check "$OUT/rbmk_core_x64.dll" "pei-x86-64"
check "$OUT/rbmk_core_x86.dll" "pei-i386"
echo "OK: 双架构 DLL 构建并验证通过"
