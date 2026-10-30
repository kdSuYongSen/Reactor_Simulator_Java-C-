/*
 * rbmk_jni.cpp - C++ 真实物理内核的 JNI 桥
 *
 * 对应 Java 类 com.rbmk.real.RbmkCore。
 * 所有状态通过一个 double[] 打包返回, 布局见下方常量 (与 RbmkCore.java 对齐)。
 */
#include <jni.h>
#include "rbmk_core.h"

using namespace rbmk;

#define CORE(ptr) reinterpret_cast<RbmkCore*>(static_cast<intptr_t>(ptr))

/* ---- 打包布局 (必须与 Java 侧常量一致) ---- */
static const int IDX_TIME    = 0;
static const int IDX_POWER   = 1;   /* W      */
static const int IDX_ELEC    = 2;   /* W      */
static const int IDX_RHO     = 3;   /* dk/k   */
static const int IDX_KEFF    = 4;
static const int IDX_PERIOD  = 5;   /* s      */
static const int IDX_RODPOS  = 6;
static const int IDX_RODTGT  = 7;
static const int IDX_SCRAM   = 8;   /* 0/1    */
static const int IDX_FLOW    = 9;   /* kg/s   */
static const int IDX_RHOAR   = 10;  /* dk/k   */
static const int IDX_TOUT    = 11;  /* C      */
static const int IDX_TIN     = 12;  /* C      */
static const int IDX_DECAY   = 13;
static const int IDX_NNODE   = 14;
static const int IDX_TRIM    = 15;

static const int NODE_BASE   = 16;
static const int NODE_STRIDE = 8;
/* node: 0=n 1=void 2=Tf 3=Tc 4=Tcool 5=Tmod 6=rho 7=powerW */

static const int RB_BASE     = NODE_BASE + NODE_STRIDE * 8;   /* 80 */
static const int RB_STRIDE   = 8;
/* rb: 0=doppler 1=void 2=mod 3=xe 4=rod 5=Tin 6=xeWorth(k) 7=I */

static const int N_OUT       = RB_BASE + RB_STRIDE * 8;       /* 144 */

static void fill(RbmkCore* c, jdoubleArray out, JNIEnv* env)
{
    double buf[N_OUT];
    for (int i = 0; i < N_OUT; ++i) buf[i] = 0.0;

    buf[IDX_TIME]  = c->time();
    buf[IDX_POWER] = c->totalPowerW();
    buf[IDX_ELEC]  = c->totalElectricW();
    buf[IDX_RHO]   = c->reactivityMean();
    buf[IDX_KEFF]  = c->kEff();
    buf[IDX_PERIOD]= c->period();
    buf[IDX_RODPOS]= c->rodPosition();
    buf[IDX_RODTGT]= c->rodTarget();
    buf[IDX_SCRAM] = c->scrammed() ? 1.0 : 0.0;
    buf[IDX_FLOW]  = c->flow();
    buf[IDX_RHOAR] = c->regulatorReactivity();
    buf[IDX_TIN]   = c->nodeTin(0);
    buf[IDX_TOUT]  = 2.0 * c->nodeTcool(N - 1) - c->nodeTin(N - 1);
    buf[IDX_DECAY] = c->decayHeatFraction();
    buf[IDX_NNODE] = (double)N;
    buf[IDX_TRIM]  = c->trimReactivity();

    for (int j = 0; j < N; ++j) {
        int b = NODE_BASE + j * NODE_STRIDE;
        buf[b + 0] = c->nodeN(j);
        buf[b + 1] = c->nodeVoid(j);
        buf[b + 2] = c->nodeTf(j);
        buf[b + 3] = c->nodeTc(j);
        buf[b + 4] = c->nodeTcool(j);
        buf[b + 5] = c->nodeTmod(j);
        buf[b + 6] = c->nodeRho(j);
        buf[b + 7] = c->nodePowerW(j);

        int r = RB_BASE + j * RB_STRIDE;
        buf[r + 0] = c->nodeRhoDoppler(j);
        buf[r + 1] = c->nodeRhoVoid(j);
        buf[r + 2] = c->nodeRhoMod(j);
        buf[r + 3] = c->nodeRhoXe(j);
        buf[r + 4] = c->nodeRhoRod(j);
        buf[r + 5] = c->nodeTin(j);
        buf[r + 6] = c->nodeXeWorthPcm(j);
        buf[r + 7] = c->nodeI(j);
    }

    env->SetDoubleArrayRegion(out, 0, N_OUT, buf);
}

extern "C" {

JNIEXPORT jlong JNICALL Java_com_rbmk_real_RbmkCore_nativeCreate(JNIEnv*, jobject)
{
    return static_cast<jlong>(reinterpret_cast<intptr_t>(new RbmkCore()));
}

JNIEXPORT void JNICALL Java_com_rbmk_real_RbmkCore_nativeDestroy(JNIEnv*, jobject, jlong h)
{
    delete CORE(h);
}

JNIEXPORT void JNICALL Java_com_rbmk_real_RbmkCore_nativeReset(JNIEnv*, jobject, jlong h)
{
    CORE(h)->resetToNominal();
}

JNIEXPORT void JNICALL Java_com_rbmk_real_RbmkCore_nativeResetShutdown(JNIEnv*, jobject, jlong h)
{
    CORE(h)->resetToShutdown();
}

JNIEXPORT void JNICALL Java_com_rbmk_real_RbmkCore_nativeStep(JNIEnv*, jobject, jlong h, jdouble dt)
{
    CORE(h)->step(dt);
}

JNIEXPORT void JNICALL Java_com_rbmk_real_RbmkCore_nativeSetRodPosition(JNIEnv*, jobject, jlong h, jdouble p)
{
    CORE(h)->setRodPosition(p);
}

JNIEXPORT void JNICALL Java_com_rbmk_real_RbmkCore_nativeSetRodTarget(JNIEnv*, jobject, jlong h, jdouble p)
{
    CORE(h)->setRodTarget(p);
}

JNIEXPORT void JNICALL Java_com_rbmk_real_RbmkCore_nativeSetRodSpeed(JNIEnv*, jobject, jlong h, jdouble s)
{
    CORE(h)->setRodSpeed(s);
}

JNIEXPORT void JNICALL Java_com_rbmk_real_RbmkCore_nativeScram(JNIEnv*, jobject, jlong h)
{
    CORE(h)->scram();
}

JNIEXPORT void JNICALL Java_com_rbmk_real_RbmkCore_nativeResetScram(JNIEnv*, jobject, jlong h)
{
    CORE(h)->resetScram();
}

JNIEXPORT void JNICALL Java_com_rbmk_real_RbmkCore_nativeSetFlowFraction(JNIEnv*, jobject, jlong h, jdouble f)
{
    CORE(h)->setFlowFraction(f);
}

JNIEXPORT void JNICALL Java_com_rbmk_real_RbmkCore_nativeSetOfflineFlow(JNIEnv*, jobject, jlong h, jdouble kgs)
{
    CORE(h)->setOfflineFlow(kgs);
}

JNIEXPORT void JNICALL Java_com_rbmk_real_RbmkCore_nativeSetSaturationTemp(JNIEnv*, jobject, jlong h, jdouble c)
{
    CORE(h)->setSaturationTemp(c);
}

JNIEXPORT void JNICALL Java_com_rbmk_real_RbmkCore_nativeSetInletEnthalpy(JNIEnv*, jobject, jlong h, jdouble jPerKg)
{
    CORE(h)->setInletEnthalpy(jPerKg);
}

JNIEXPORT void JNICALL Java_com_rbmk_real_RbmkCore_nativeSetPowerSetpoint(JNIEnv*, jobject, jlong h, jdouble w)
{
    CORE(h)->setPowerSetpoint(w);
}

JNIEXPORT void JNICALL Java_com_rbmk_real_RbmkCore_nativeSetRegulatorEnabled(JNIEnv*, jobject, jlong h, jboolean on)
{
    CORE(h)->setRegulatorEnabled(on == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_com_rbmk_real_RbmkCore_nativeGetState(JNIEnv* env, jobject, jlong h, jdoubleArray out)
{
    fill(CORE(h), out, env);
}

/* ---- 存档状态（与 RbmkCore.STATE_SIZE 对齐） ---- */
JNIEXPORT jint JNICALL Java_com_rbmk_real_RbmkCore_nativeStateSize(JNIEnv*, jobject, jlong h)
{
    return CORE(h)->stateSize();
}

JNIEXPORT void JNICALL Java_com_rbmk_real_RbmkCore_nativeSaveState(JNIEnv* env, jobject, jlong h, jdoubleArray out)
{
    if (out == NULL) return;
    const int n = CORE(h)->stateSize();
    if (env->GetArrayLength(out) < n) return;
    double buf[256];
    CORE(h)->saveState(buf);
    env->SetDoubleArrayRegion(out, 0, n, buf);
}

JNIEXPORT jboolean JNICALL Java_com_rbmk_real_RbmkCore_nativeLoadState(JNIEnv* env, jobject, jlong h, jdoubleArray in)
{
    if (in == NULL) return JNI_FALSE;
    const int n = CORE(h)->stateSize();
    if (env->GetArrayLength(in) < n) return JNI_FALSE;
    double buf[256];
    env->GetDoubleArrayRegion(in, 0, n, buf);
    return CORE(h)->loadState(buf) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jdouble JNICALL Java_com_rbmk_real_RbmkCore_nativeProbeFullVoidPcm(JNIEnv*, jobject, jlong h)
{
    return CORE(h)->probeFullVoidInsertionPcm();
}

JNIEXPORT jdouble JNICALL Java_com_rbmk_real_RbmkCore_nativeProbeVoidCoeffPcm(JNIEnv*, jobject, jlong h)
{
    return CORE(h)->probeVoidCoefficientPcm();
}

JNIEXPORT jdouble JNICALL Java_com_rbmk_real_RbmkCore_nativeProbeDopplerCoeff(JNIEnv*, jobject, jlong h)
{
    return CORE(h)->probeDopplerCoefficient();
}

JNIEXPORT jdouble JNICALL Java_com_rbmk_real_RbmkCore_nativeProbeModeratorCoeff(JNIEnv*, jobject, jlong h)
{
    return CORE(h)->probeModeratorCoefficient();
}

JNIEXPORT jdouble JNICALL Java_com_rbmk_real_RbmkCore_nativeProbeXenonPeakPcm(JNIEnv* env, jobject, jlong h, jdoubleArray tPeakOut)
{
    double tpk = 0.0;
    double v = CORE(h)->probeXenonPeakPcm(&tpk);
    if (tPeakOut != NULL && env->GetArrayLength(tPeakOut) >= 1)
        env->SetDoubleArrayRegion(tPeakOut, 0, 1, &tpk);
    return v;
}

JNIEXPORT jdouble JNICALL Java_com_rbmk_real_RbmkCore_nativeProbeRodWorthPcm(JNIEnv*, jobject, jlong h, jdouble pos, jint j)
{
    return CORE(h)->probeRodWorth(pos, j);
}

} /* extern "C" */
