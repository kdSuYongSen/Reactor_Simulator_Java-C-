package com.rbmk.real;

/**
 * RbmkCore - RBMK-1500 真实物理内核的 Java 绑定（C++ 计算核心 rbmk_core.dll）。
 *
 * 物理计算全部在 C++ 侧完成（8 轴向节点点堆动力学 + 6 群缓发先驱核 + 氙碘链 +
 * 四温度热块 TR-BDF2/Newton + 控制棒 + 停堆衰变热），本类只负责：
 *   1. 从 classpath 释放 rbmk_core.dll 并加载（JNI 函数绑定本包名，勿改包/类名）
 *   2. 调用 native 接口推进仿真
 *   3. 把打包的 double[] 状态解码成访问器
 *
 * 打包布局必须与 native/rbmk_jni.cpp 保持一致。
 */
public final class RbmkCore implements AutoCloseable {

    /* ---------- 状态数组布局 ---------- */
    public static final int OUT_SIZE = 144;

    /** 存档状态维数（与 native/rbmk_core.h 的 STATE_SIZE 一致）：
     *  u(40) + C(48) + I(8) + Xe(8) + 21 个标量/标志。 */
    public static final int STATE_SIZE = 125;

    public static final int IDX_TIME   = 0;
    public static final int IDX_POWER  = 1;   /* W    */
    public static final int IDX_ELEC   = 2;   /* W    */
    public static final int IDX_RHO    = 3;   /* dk/k */
    public static final int IDX_KEFF   = 4;
    public static final int IDX_PERIOD = 5;
    public static final int IDX_RODPOS = 6;
    public static final int IDX_RODTGT = 7;
    public static final int IDX_SCRAM  = 8;
    public static final int IDX_FLOW   = 9;   /* kg/s */
    public static final int IDX_RHOAR  = 10;
    public static final int IDX_TOUT   = 11;  /* C    */
    public static final int IDX_TIN    = 12;  /* C    */
    public static final int IDX_DECAY  = 13;
    public static final int IDX_NNODE  = 14;
    public static final int IDX_TRIM   = 15;

    public static final int NODE_BASE   = 16;
    public static final int NODE_STRIDE = 8;
    /* node: 0=n 1=void 2=Tf 3=Tc 4=Tcool 5=Tmod 6=rho 7=powerW */

    public static final int RB_BASE   = NODE_BASE + NODE_STRIDE * 8;   /* 80 */
    public static final int RB_STRIDE = 8;
    /* rb: 0=doppler 1=void 2=mod 3=xe 4=rod 5=Tin 6=xeWorth(k) 7=I */

    public static final int NODES = 8;

    /* ---------- 库加载：按 JVM 位数选内置 DLL，释放到临时目录 → System.load ----------
     * 刻意不走 System.loadLibrary：Windows 会按 PATH/工作目录搜到环境里任意同名旧
     * rbmk_core.dll（版本/架构/导出集不可控，曾导致 nativeCreate 解析失败）。
     * 只信任 jar 内随包分发的 x64/x86 静态版。 */
    /** 非 null 表示加载失败的错误信息（调用方据此提示，而不是静默崩溃） */
    public static volatile String loadError = null;

    static {
        boolean x64 = System.getProperty("os.arch", "x86").contains("64");
        String resPath = x64 ? "/res/native/rbmk_core_x64.dll" : "/res/native/rbmk_core_x86.dll";
        String tmpPrefix = x64 ? "rbmk_core64_" : "rbmk_core86_";
        try (java.io.InputStream in = RbmkCore.class.getResourceAsStream(resPath)) {
            if (in != null) {
                java.io.File tmp = java.io.File.createTempFile(tmpPrefix, ".dll");
                tmp.deleteOnExit();
                java.nio.file.Files.copy(in, tmp.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                System.load(tmp.getAbsolutePath());
            } else {
                loadError = resPath + " 不在 classpath";
            }
        } catch (Throwable t) {
            loadError = "释放/加载 " + resPath + " 失败: " + t;
        }
        if (loadError != null) {
            System.err.println("[RbmkCore] os.arch=" + System.getProperty("os.arch") + "  " + loadError);
        }
    }

    /* ---------- native ---------- */
    private static native long   nativeCreate();
    private static native void   nativeDestroy(long h);
    private static native void   nativeReset(long h);
    private static native void   nativeResetShutdown(long h);
    private static native void   nativeStep(long h, double dt);
    private static native void   nativeSetRodPosition(long h, double p);
    private static native void   nativeSetRodTarget(long h, double p);
    private static native void   nativeSetRodSpeed(long h, double s);
    private static native void   nativeScram(long h);
    private static native void   nativeResetScram(long h);
    private static native void   nativeSetFlowFraction(long h, double f);
    private static native void   nativeSetOfflineFlow(long h, double kgs);
    private static native void   nativeSetSaturationTemp(long h, double c);
    private static native void   nativeSetInletEnthalpy(long h, double jPerKg);
    private static native void   nativeSetPowerSetpoint(long h, double w);
    private static native void   nativeSetRegulatorEnabled(long h, boolean on);
    private static native void   nativeGetState(long h, double[] out);
    private static native int    nativeStateSize(long h);
    private static native void   nativeSaveState(long h, double[] out);
    private static native boolean nativeLoadState(long h, double[] in);
    private static native double nativeProbeFullVoidPcm(long h);
    private static native double nativeProbeVoidCoeffPcm(long h);
    private static native double nativeProbeDopplerCoeff(long h);
    private static native double nativeProbeModeratorCoeff(long h);
    private static native double nativeProbeXenonPeakPcm(long h, double[] tPeakOut);
    private static native double nativeProbeRodWorthPcm(long h, double pos, int j);

    /* ---------- 实例 ---------- */
    private long handle;
    private final double[] state = new double[OUT_SIZE];

    public RbmkCore() {
        if (loadError != null) {
            throw new IllegalStateException("物理核心不可用：" + loadError);
        }
        handle = nativeCreate();
        refresh();
    }

    /** 读取一次状态快照（之后所有 getter 都基于该快照） */
    public void refresh() {
        if (handle != 0L) nativeGetState(handle, state);
    }

    public void step(double dt)          { nativeStep(handle, dt); }
    public void reset()                  { nativeReset(handle); refresh(); }
    /** 停堆初值：全插棒 + 源量程通量 + 燃料压到冷却剂温度 + 氙碘归零 + AR 退出 */
    public void resetToShutdown()        { nativeResetShutdown(handle); refresh(); }
    public void setRodPosition(double p) { nativeSetRodPosition(handle, p); }
    public void setRodTarget(double p)   { nativeSetRodTarget(handle, p); }
    public void setRodSpeed(double s)    { nativeSetRodSpeed(handle, s); }
    public void scram()                  { nativeScram(handle); }
    public void resetScram()             { nativeResetScram(handle); refresh(); }
    public void setFlowFraction(double f){ nativeSetFlowFraction(handle, f); }
    /** 离线冷却河水流量 kg/s（20°C 冷源置换热沉，经换热器冷却一回路） */
    public void setOfflineFlow(double kgs){ nativeSetOfflineFlow(handle, kgs); }
    /** 当前压力对应的饱和温度 °C（由 Java 蒸汽表给出，替代固定 284°C） */
    public void setSaturationTemp(double c){ nativeSetSaturationTemp(handle, c); }
    /** 入口焓 J/kg（相对 h(270°C)=0；由 Java 蒸汽表按入口温度差算出） */
    public void setInletEnthalpy(double jPerKg){ nativeSetInletEnthalpy(handle, jPerKg); }
    public void setPowerSetpoint(double w){ nativeSetPowerSetpoint(handle, w); }
    public void setRegulatorEnabled(boolean on){ nativeSetRegulatorEnabled(handle, on); }

    /* ---------- 全局量 ---------- */
    public double time()          { return state[IDX_TIME]; }
    public double powerW()        { return state[IDX_POWER]; }
    public double powerMW()       { return state[IDX_POWER] / 1.0e6; }
    public double electricMW()    { return state[IDX_ELEC]  / 1.0e6; }
    public double percentNominal(){ return state[IDX_POWER] / 4800.0e6 * 100.0; }
    public double reactivityPcm() { return state[IDX_RHO] * 1.0e5; }
    public double reactivity()    { return state[IDX_RHO]; }
    public double kEff()          { return state[IDX_KEFF]; }
    public double period()        { return state[IDX_PERIOD]; }
    public double rodPosition()   { return state[IDX_RODPOS]; }
    public double rodTarget()     { return state[IDX_RODTGT]; }
    public boolean scrammed()     { return state[IDX_SCRAM] > 0.5; }
    public double flowKgS()       { return state[IDX_FLOW]; }
    public double regulatorPcm()  { return state[IDX_RHOAR] * 1.0e5; }
    public double inletTemp()     { return state[IDX_TIN]; }
    public double outletTemp()    { return state[IDX_TOUT]; }
    public double decayFraction() { return state[IDX_DECAY]; }
    public int    nodeCount()     { return (int) state[IDX_NNODE]; }
    public double trimPcm()       { return state[IDX_TRIM] * 1.0e5; }

    /* ---------- 节点量 ---------- */
    public double nodeN(int j)      { return state[NODE_BASE + j * NODE_STRIDE + 0]; }
    public double nodeVoid(int j)   { return state[NODE_BASE + j * NODE_STRIDE + 1]; }
    public double nodeTf(int j)     { return state[NODE_BASE + j * NODE_STRIDE + 2]; }
    public double nodeTc(int j)     { return state[NODE_BASE + j * NODE_STRIDE + 3]; }
    public double nodeTcool(int j)  { return state[NODE_BASE + j * NODE_STRIDE + 4]; }
    public double nodeTmod(int j)   { return state[NODE_BASE + j * NODE_STRIDE + 5]; }
    public double nodeRhoPcm(int j) { return state[NODE_BASE + j * NODE_STRIDE + 6] * 1.0e5; }
    public double nodePowerMW(int j){ return state[NODE_BASE + j * NODE_STRIDE + 7] / 1.0e6; }

    /* ---------- 反应性分解 ---------- */
    public double rbDoppler(int j)  { return state[RB_BASE + j * RB_STRIDE + 0] * 1.0e5; }
    public double rbVoid(int j)     { return state[RB_BASE + j * RB_STRIDE + 1] * 1.0e5; }
    public double rbMod(int j)      { return state[RB_BASE + j * RB_STRIDE + 2] * 1.0e5; }
    public double rbXenon(int j)    { return state[RB_BASE + j * RB_STRIDE + 3] * 1.0e5; }
    public double rbRod(int j)      { return state[RB_BASE + j * RB_STRIDE + 4] * 1.0e5; }
    public double nodeTin(int j)    { return state[RB_BASE + j * RB_STRIDE + 5]; }
    public double xenonWorth(int j) { return state[RB_BASE + j * RB_STRIDE + 6]; }
    public double nodeI(int j)      { return state[RB_BASE + j * RB_STRIDE + 7]; }

    /* 全堆芯平均（用于总览） */
    public double avgDoppler() { double s=0; for(int j=0;j<NODES;j++) s+=rbDoppler(j); return s/NODES; }
    public double avgVoid()    { double s=0; for(int j=0;j<NODES;j++) s+=rbVoid(j);    return s/NODES; }
    public double avgMod()     { double s=0; for(int j=0;j<NODES;j++) s+=rbMod(j);     return s/NODES; }
    public double avgXenon()   { double s=0; for(int j=0;j<NODES;j++) s+=rbXenon(j);   return s/NODES; }
    public double avgRod()     { double s=0; for(int j=0;j<NODES;j++) s+=rbRod(j);     return s/NODES; }
    public double avgI()       { double s=0; for(int j=0;j<NODES;j++) s+=nodeI(j);     return s/NODES; }
    public double avgNodeN()   { double s=0; for(int j=0;j<NODES;j++) s+=nodeN(j);     return s/NODES; }

    /* ---------- 存档状态（完整动力学状态） ---------- */

    /** 当前内核是否支持状态保存/恢复。旧版 DLL（2026-10-19 之前构建的包）没有这三个符号。 */
    public boolean supportsStateRestore() {
        if (handle == 0L) {
            return false;
        }
        try {
            return nativeStateSize(handle) == STATE_SIZE;
        } catch (UnsatisfiedLinkError e) {
            return false;
        }
    }

    /** 导出完整状态；内核不支持或句柄无效时返回 null。 */
    public double[] saveState() {
        if (handle == 0L) {
            return null;
        }
        try {
            int n = nativeStateSize(handle);
            if (n <= 0 || n > 4096) {
                return null;
            }
            double[] out = new double[n];
            nativeSaveState(handle, out);
            return out;
        } catch (UnsatisfiedLinkError e) {
            return null;
        }
    }

    /** 从完整状态恢复（会覆盖当前内核的 u/C/I/Xe/温度/棒位/停堆历史）。 */
    public boolean restoreState(double[] in) {
        if (handle == 0L || in == null) {
            return false;
        }
        try {
            if (nativeStateSize(handle) != in.length) {
                return false;
            }
            boolean ok = nativeLoadState(handle, in);
            if (ok) {
                refresh();
            }
            return ok;
        } catch (UnsatisfiedLinkError e) {
            return false;
        }
    }

    /* ---------- 探针 ---------- */
    public double probeFullVoidPcm() { return nativeProbeFullVoidPcm(handle); }
    public double probeVoidCoeffPcm(){ return nativeProbeVoidCoeffPcm(handle); }
    /** pcm per sqrt(K) */
    public double probeDopplerCoeff(){ return nativeProbeDopplerCoeff(handle); }
    /** pcm per K（RBMK 石墨系数为正） */
    public double probeModeratorCoeff(){ return nativeProbeModeratorCoeff(handle); }
    public double probeRodWorthPcm(double pos, int j){ return nativeProbeRodWorthPcm(handle, pos, j); }
    public double probeXenonPeakPcm(double[] tPeakOut){ return nativeProbeXenonPeakPcm(handle, tPeakOut); }

    @Override
    public void close() {
        if (handle != 0L) { nativeDestroy(handle); handle = 0L; }
    }
}
