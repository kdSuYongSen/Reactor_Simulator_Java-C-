# Build the JNI physics core (x64 + x86)
#
#   SourceCode/native/*.cpp  ->  SourceCode/src/main/resources/res/native/rbmk_core_x64.dll
#                            ->  SourceCode/src/main/resources/res/native/rbmk_core_x86.dll
#
# Requirements:
#   * TDM-GCC 9.2 (Dev-Cpp) or any MinGW-w64 g++ with C++14 and 32-bit multilib
#   * A JDK with include/jni.h and include/win32/jni_md.h
#
# Usage (from anywhere):
#   powershell -ExecutionPolicy Bypass -File SourceCode\tools\native\build-native.ps1
# Optional environment overrides: RBMK_GXX, RBMK_JDK
#
# NOTE: both DLLs must come from the same source tree. Historically x64 was
#       rebuilt alone while x86 stayed on an old binary, so 32-bit JVMs still ran
#       the "coolant receives 6% extra heat" core.
$ErrorActionPreference = 'Stop'

$project = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)   # SourceCode
$native  = Join-Path $project 'native'
$resDir  = Join-Path $project 'src\main\resources\res\native'

$gxx = if ($env:RBMK_GXX) { $env:RBMK_GXX } else { 'E:\Dev-Cpp\TDM-GCC-64\bin\g++.exe' }
$jdk = if ($env:RBMK_JDK) { $env:RBMK_JDK } else { 'C:\Program Files\Java\jdk-24' }

if (-not (Test-Path $gxx)) { throw "g++ not found: $gxx (set RBMK_GXX)" }
if (-not (Test-Path (Join-Path $jdk 'include\jni.h'))) { throw "jni.h not found: $jdk (set RBMK_JDK)" }

Write-Host "[native] g++ = $gxx"
Write-Host "[native] jdk = $jdk"

# 本地修补版 jni.h：JDK 17+ 把 JNICALL 定义成空宏，32 位下必须补回 __stdcall，
# 否则 x86 DLL 导出的是 cdecl，32 位 JVM 调用后栈失衡（EXCEPTION_ACCESS_VIOLATION）。
$localInc = Join-Path $native 'include'

$common = @(
    '-shared', '-O2', '-std=c++14', '-Wall', '-static-libgcc', '-static-libstdc++',
    "-I$localInc",
    "-I$(Join-Path $jdk 'include')",
    "-I$(Join-Path $jdk 'include\win32')",
    (Join-Path $native 'rbmk_core.cpp'),
    (Join-Path $native 'rbmk_jni.cpp')
)

$targets = @(
    @{ Name = 'rbmk_core_x64.dll'; Arch = @() },
    # 32 位：stdcall 会把导出名装饰成 _Java_xxx@20，--kill-at 去掉 @N，
    # 否则 JVM 的 GetProcAddress 找不到方法（UnsatisfiedLinkError）。
    @{ Name = 'rbmk_core_x86.dll'; Arch = @('-m32', '-Wl,--kill-at') }
)

foreach ($t in $targets) {
    $out = Join-Path $resDir $t.Name
    Write-Host "[native] out = $out"
    $args = @($t.Arch) + @('-o', $out) + $common
    & $gxx $args
    if ($LASTEXITCODE -ne 0) { throw "g++ failed for $($t.Name), exit=$LASTEXITCODE" }
    $dll = Get-Item $out
    Write-Host ("[native] OK -> {0} ({1} bytes, {2})" -f $dll.Name, $dll.Length, $dll.LastWriteTime)
}

Write-Host "[native] done. Remember to run 'mvn package' so the new DLLs ship in the jar."
