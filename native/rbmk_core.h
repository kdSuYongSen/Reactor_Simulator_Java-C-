/*
 * rbmk_core.h - RBMK-1500 真实物理内核 (C++ 核心计算层)
 *
 * 状态向量 (每个轴向节点 j = 0..7, 共 13 维):
 *     y_j = [ n_j, {C_jg}_{g=1..6}, I_j, Xe_j, Tf_j, Tc_j, Tcool_j, Tmod_j ]
 * 总维数 8 x 13 = 104。
 *
 * TR-BDF2 隐式求解的刚性子系统 (40 维):
 *     u = [ n(8), Tf(8), Tc(8), Tcool(8), Tmod(8) ]
 * 先驱核 / 碘 / 氙用解析指数更新 (operator splitting), 见文档 6.2。
 */
#ifndef RBMK_CORE_H
#define RBMK_CORE_H

#include "rbmk_params.h"

namespace rbmk {

class RbmkCore {
public:
    RbmkCore();

    /* ---- 生命周期 ---- */
    void   resetToNominal();           /* 建立额定稳态初值            */
    void   resetToShutdown();          /* 停堆初值: 全插棒 + 源量程通量 */
    void   step(double dt);            /* 推进一个时间步 (TR-BDF2)    */

    /* ---- 存档状态 (完整动力学状态, 用于读档精确恢复) ----
     * 布局: u_[40] + C_[8][6] + In_[8] + Xen_[8] + 标量/标志 21 个 = 125。
     * 具体顺序见 rbmk_core.cpp 的 saveState()。 */
    static const int STATE_SIZE = NDIM + N * N_PRECURSOR + 2 * N + 21;
    int  stateSize() const { return STATE_SIZE; }
    void saveState(double* out) const;
    bool loadState(const double* in);

    /* ---- 控制输入 ---- */
    void   setRodPosition(double p);   /* 直接置棒位 1=全提 0=全插    */
    void   setRodTarget(double p);     /* 目标棒位 (按速度移动)       */
    void   setRodSpeed(double perSec); /* 棒速 s^-1                   */
    void   useScramSpeed(bool on);     /* 使用 SCRAM 棒速             */
    void   scram();                    /* 触发紧急停堆                */
    void   resetScram();               /* 复归                        */
    void   setFlow(double kgs);        /* 冷却剂总流量 kg/s           */
    void   setFlowFraction(double f);  /* 流量份额 0..1               */
    void   setOfflineFlow(double kgs); /* 离线冷却河水流量 kg/s（20C 置换热沉） */
    void   setSaturationTemp(double c);/* 由 Java 蒸汽表按当前压力给出 */
    void   setInletEnthalpy(double jPerKg); /* 入口焓，相对 h(270C)=0 */
    void   freeze();                   /* 冻结: 全部导数置零 (取系数用) */

    /* ---- AR 自动调节器 (代表自动调节棒组) ---- */
    void   setRegulatorEnabled(bool on);
    bool   regulatorEnabled() const { return regEnabled_; }
    void   setPowerSetpoint(double watts);
    void   setRegulatorGains(double kp, double ki, double ratePerSec, double limit);
    double regulatorReactivity() const { return rhoExt_; }

    /* ---- 状态输出 ---- */
    double time()            const { return t_; }
    double totalPowerW()     const;
    double totalElectricW()  const;
    double reactivityMean()  const;    /* 堆芯体积平均反应性          */
    double kEff()            const;
    double period()          const;
    double rodPosition()     const { return rodPos_; }
    double rodTarget()       const { return rodTarget_; }
    bool   scrammed()        const { return scramLatch_; }
    double flow()            const { return flow_; }

    int    nodes()           const { return N; }
    double nodeN(int j)      const;
    double nodePowerW(int j) const;
    double nodeVoid(int j)   const;
    double nodeTf(int j)     const;
    double nodeTc(int j)     const;
    double nodeTcool(int j)  const;
    double nodeTmod(int j)   const;
    double nodeTin(int j)    const;
    double nodeRho(int j)    const;
    double nodeRhoDoppler(int j) const;
    double nodeRhoVoid(int j)    const;
    double nodeRhoMod(int j)     const;
    double nodeRhoXe(int j)      const;
    double nodeRhoRod(int j)     const;
    double nodeI(int j)      const;
    double nodeXe(int j)     const;
    double nodeXeWorthPcm(int j) const;
    double decayHeatFraction() const;
    double trimReactivity()  const;    /* 平均配平反应性 */
    double trimReactivity(int j) const { return rhoTrim_[j]; }
    double shutdownMarginWeight() const;   /* 停堆裕度的棒位权重 0..1 */

    /* ---- Golden Test 探针 ---- */
    double probeVoidCoefficientPcm();          /* pcm per 1% 体积空泡份额  */
    double probeQuality(int j) const;          /* 干度 x（0..1）           */
    double probeDopplerCoefficient();          /* dk/k per sqrt(K)         */
    double probeModeratorCoefficient();        /* dk/k per K               */
    double probeRodWorth(double position, int j); /* dk/k, 节点 j          */
    double probeFuelChannelPowerMW() const;    /* 单通道功率 MW            */
    double probeFullVoidInsertionPcm();        /* 满空泡总插入 pcm         */
    /* 氙瞬态: 从稳态停堆后跟踪峰值, 返回峰值 pcm, 输出达峰时间(秒)      */
    double probeXenonPeakPcm(double* tPeakSeconds);

private:
    /* 状态: 40 维刚性子系统 + 解析量 */
    double u_[NDIM];                 /* [n, Tf, Tc, Tcool, Tmod]         */
    double C_[N][N_PRECURSOR];       /* 先驱核浓度                       */
    double In_[N];                   /* 碘-135                           */
    double Xen_[N];                  /* 氙-135                           */

    double t_;                       /* 仿真时间 s                       */
    double rodPos_, rodTarget_, rodSpeed_;
    bool   scramLatch_, useScramSpeed_, frozen_;
    double flow_;
    double offlineFlow_ = 0.0;             /* 离线冷却河水流量 kg/s */
    double tSat_ = T_SAT;                  /* 当前压力对应的饱和温度 C（Java 注入） */
    double hIn_ = 0.0;                     /* 入口焓 J/kg，相对 h(270C)=0（Java 注入） */
    double rhoTrim_[N];              /* 逐节点配平反应性 (使额定 rho_j=0) */
    /* 停堆深度余量 (dk/k): 只在 resetToShutdown 里写入，并按 (1-棒位) 线性衰减。
     * 物理含义 = 冷态后备反应性之外的附加停堆裕度（真实机组全插棒的次临界度）。
     * 用户要求：开机默认（未启动）状态 ρ = -6000~-9000 pcm、周期 ∞；
     * 只靠棒价值在冷态只能到 -3473 pcm（冷态多普勒/氙/空泡正反应性抵消太多），
     * 因此显式补一层随提棒消失的裕度，提棒启动曲线不受影响。 */
    double shutdownMargin_ = 0.0;
    double rhoExt_;                  /* AR 调节器输出的外部反应性        */
    double regIntegral_;             /* AR 积分项                        */
    bool   regEnabled_;
    double pSet_;                    /* AR 功率设定值 W                  */
    double regKp_, regKi_, regRate_, regLimit_;
    double xenonAlpha_;              /* 单位氙浓度的反应性 dk/k          */
    double tShutdown_;               /* 停堆时刻 s                       */
    bool   wasScrammed_;

    /* ---- 内部工具 ---- */
    static double clamp(double v, double lo, double hi);
    double qualityOf(double Tcool) const;     /* 热力学干度 x（两相区）    */
    double voidFracOf(double x) const;        /* 干度 -> 体积空泡份额 α    */
    double voidOf(double Tcool) const;
    double voidMean(const double* u) const;   /* 堆芯平均空泡份额（体积平均） */
    double enthalpyOf(double T) const;
    double dhdTOf(double T) const;
    double inletTemp(int j, const double* u) const;
    void   advanceFissionProducts(double dt);   /* 解析推进 C / I / Xe      */
    void   rhs(const double* u, double* du) const;
    void   jacobian(const double* u, double A[NDIM][NDIM]) const;
    bool   solveLinear(double A[NDIM][NDIM], double b[NDIM], double x[NDIM]) const;
    double rodShare(int j, double position) const;      /* 单节点分摊价值   */
    double rodShareMean(double position) const;         /* 8 节点平均       */
    double rodReactivity(int j, double position) const; /* 实际施加到节点 j */
    void   updateRodPosition(double dt);
};

} /* namespace rbmk */

#endif /* RBMK_CORE_H */
