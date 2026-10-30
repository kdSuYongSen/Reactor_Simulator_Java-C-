/*
 * rbmk_core.cpp - RBMK-1500 真实物理内核实现
 *
 * 数值方案:
 *   - 刚性子系统 (n, Tf, Tc, Tcool, Tmod; 40 维) 用 TR-BDF2 (gamma = 2-sqrt2) 隐式积分
 *   - 先驱核 / I-135 / Xe-135 用解析指数算子分裂 (文档 6.2)
 *   - Jacobian 用中心差分数值求取 (保证与 rhs 严格一致, 等价于文档 6.4 解析式)
 *   - 每步用稠密高斯消元 (部分选主元) 解 (I - c*dt*J) dx = r
 */
#include "rbmk_core.h"

#include <cmath>
#include <cstring>
#include <algorithm>

namespace rbmk {

/* 空泡反馈统一用**体积空泡份额**（见 rbmk_params.h 说明）。 */

/* ---------------- 文件内小工具 ---------------- */

static inline double clampd(double v, double lo, double hi)
{
    return v < lo ? lo : (v > hi ? hi : v);
}

/* 节点区间重叠长度 */
static inline double overlapLen(double lo1, double hi1, double lo2, double hi2)
{
    double lo = (lo1 > lo2) ? lo1 : lo2;
    double hi = (hi1 < hi2) ? hi1 : hi2;
    double d = hi - lo;
    return d > 0.0 ? d : 0.0;
}

/* 由冷却剂焓反查温度 (与 enthalpyOf 严格互逆) */
static double tempFromEnthalpy(double h, double tSat)
{
    double hSat = CP_WATER * (tSat - T_COOL_REF);
    if (h <= hSat) return T_COOL_REF + h / CP_WATER;
    return tSat + (h - hSat) * DT_BOIL / H_FG;
}

/* ---------------- 构造 / 初始化 ---------------- */

RbmkCore::RbmkCore()
{
    rodSpeed_      = ROD_SPEED_NORM;
    rodPos_        = P_ROD_NOMINAL;
    rodTarget_     = P_ROD_NOMINAL;
    scramLatch_    = false;
    useScramSpeed_ = false;
    frozen_        = false;
    flow_          = MDOT_NOM;
    for (int j = 0; j < N; ++j) rhoTrim_[j] = 0.0;
    rhoExt_        = 0.0;
    regIntegral_   = 0.0;
    regEnabled_    = true;
    pSet_          = P_THERMAL_NOM;
    regKp_         = 0.030;       /* dk/k per 单位相对功率偏差 */
    regKi_         = 0.00050;     /* 每 s                      */
    regRate_       = 2.0e-3;      /* 反应性变化速率上限 /s      */
    regLimit_      = 0.0100;      /* 调节器权限 +/- 1000 pcm    */
    tShutdown_     = 0.0;
    wasScrammed_   = false;
    xenonAlpha_    = XENON_WORTH_EQ / XE_EQ_NOM;
    resetToNominal();
}

void RbmkCore::resetToNominal()
{
    t_             = 0.0;
    rodPos_        = P_ROD_NOMINAL;
    rodTarget_     = P_ROD_NOMINAL;
    rodSpeed_      = ROD_SPEED_NORM;
    scramLatch_    = false;
    useScramSpeed_ = false;
    frozen_        = false;
    flow_          = MDOT_NOM;
    tSat_          = T_SAT;
    hIn_           = 0.0;
    wasScrammed_   = false;
    tShutdown_     = 0.0;
    rhoExt_        = 0.0;
    regIntegral_   = 0.0;
    xenonAlpha_    = XENON_WORTH_EQ / XE_EQ_NOM;

    /* --- 冷却剂轴向剖面: 自下而上按焓积分（总功率 = 燃料 564 + 石墨 36 MW） --- */
    double dhNode = P_NODE_NOM / flow_;                                  /* J/kg */
    double hIn = 0.0;                                                    /* h(270C)=0 */
    for (int j = 0; j < N; ++j) {
        u_[3 * N + j] = tempFromEnthalpy(hIn + 0.5 * dhNode, tSat_);
        hIn += dhNode;
    }

    /* --- 其余温度、中子、裂变产物 --- */
    for (int j = 0; j < N; ++j) {
        double Tcool = u_[3 * N + j];
        u_[j]         = 1.0;                                        /* n  相对值 */
        u_[2 * N + j] = Tcool + P_FUEL_NODE / H_CM;                 /* 包壳       */
        u_[N + j]     = u_[2 * N + j] + P_FUEL_NODE / H_FC;         /* 燃料       */
        u_[4 * N + j] = Tcool + (Q_MOD_FRACTION * P_NODE_NOM) / H_MOD; /* 石墨    */

        for (int g = 0; g < N_PRECURSOR; ++g)
            C_[j][g] = BETA_G[g] / (LAMBDA_PROMPT * LAMBDA_G[g]);
        In_[j]  = I_EQ_NOM;
        Xen_[j] = XE_EQ_NOM;
    }

    /* --- 逐节点配平反应性 ---
     * 令额定工况下每个节点的 rho_j 都严格为 0, 则均匀通量 (n_j = 1) 是
     * 节点平衡方程的精确解, 初始状态真正自洽。
     * 物理含义: 真实堆芯靠棒位分布 / 燃耗分布展平轴向功率, 这里把它折算成
     * 一个逐节点的静态反应性偏置 (代表分布式的反应性控制)。           */
    double vmNom = voidMean(u_);
    for (int j = 0; j < N; ++j) {
        double Tf    = u_[N + j];
        double Tmod  = u_[4 * N + j];
        rhoTrim_[j] = -( rodReactivity(j, rodPos_)
                       + ALPHA_DOPPLER * (std::sqrt(Tf + 273.15) - std::sqrt(T_FUEL_NOM + 273.15))
                       + ALPHA_MOD * (Tmod - T_MOD_NOM)
                       + VOID_WORTH * vmNom
                       + xenonAlpha_ * Xen_[j] );
    }
}

/* ---------------- 基础物性 ---------------- */

double RbmkCore::clamp(double v, double lo, double hi)
{
    return clampd(v, lo, hi);
}

/* 热力学干度 x = (h - h_sat) / h_fg。
 * 单相过冷段 x=0；两相段 0..1。注意这**不是**空泡份额。 */
double RbmkCore::qualityOf(double Tcool) const
{
    double hSat = CP_WATER * (tSat_ - T_COOL_REF);
    double h = enthalpyOf(Tcool);
    if (h <= hSat) return 0.0;
    return clampd((h - hSat) / H_FG, 0.0, 1.0);
}

/* 干度 -> 体积空泡份额（含滑速比 S）：
 *     α = x·v_g / ( x·v_g + S·(1-x)·v_f )
 * S=1 为均匀流；S=2 是通道平均的常用值。α 在 x→1 时饱和，
 * 这正是让空泡反应性随功率"减速增长"的原因。 */
double RbmkCore::voidFracOf(double x) const
{
    if (x <= 0.0) return 0.0;
    double num = x * VG_SAT;
    double den = num + SLIP_RATIO * (1.0 - x) * VF_SAT;
    return (den > 0.0) ? clampd(num / den, 0.0, 1.0) : 0.0;
}

double RbmkCore::voidOf(double Tcool) const
{
    return voidFracOf(qualityOf(Tcool));
}

/* 堆芯平均空泡份额：8 节点算术平均（体积平均）。
 * 反应性反馈与显示都走这个口径——单节点全空泡只等于 1/8 的全堆价值。 */
double RbmkCore::voidMean(const double* u) const
{
    const double* TcoolA = u + 3 * N;
    double s = 0.0;
    for (int j = 0; j < N; ++j) s += voidOf(TcoolA[j]);
    return s / (double)N;
}

double RbmkCore::enthalpyOf(double T) const
{
    double hSat = CP_WATER * (tSat_ - T_COOL_REF);
    if (T <= tSat_) return CP_WATER * (T - T_COOL_REF);
    return hSat + (T - tSat_) / DT_BOIL * H_FG;
}

/* dh/dT 由 enthalpyOf 的中心差分给出, 保证与焓函数自洽,
 * 并在饱和点附近自动平滑过渡 (避免 Jacobian 出现刚性跳变)。 */
double RbmkCore::dhdTOf(double T) const
{
    const double e = 0.25;                       /* K */
    return (enthalpyOf(T + e) - enthalpyOf(T - e)) / (2.0 * e);
}

double RbmkCore::inletTemp(int j, const double* u) const
{
    /* 用**焓**做上游递推：模型的守恒量是焓，节点平均焓恒等于 (h_in+h_out)/2。
     * 旧实现直接在温度空间做 T_out = 2*T_avg - T_in：在过冷→沸腾的转折节点，
     * T(h) 有折点，T_avg ≠ (T_in+T_out)/2，会注入几 K 的误差；而该递推的
     * 误差传递因子是 -2（交替放大），到第 8 节点已放大 ~2^8 倍 →
     * 冷却剂温度剖面出现锯齿、并触发“空泡→功率”的瞬时正反馈
     * （实测额定态 0.05 s 内凭空多出 +40 pcm）。 */
    const double* TcoolA = u + 3 * N;
    double hIn = hIn_;                      /* 入口焓（相对 h(270C)=0，Java 注入） */
    for (int k = 0; k < j; ++k)
        hIn = 2.0 * enthalpyOf(TcoolA[k]) - hIn;
    return tempFromEnthalpy(hIn, tSat_);
}

double RbmkCore::decayHeatFraction() const
{
    /* 开机停堆态 (resetToShutdown) 用 tShutdown_ < 0 标记"已停堆很久"，
     * 衰变热严格为 0，界面初始功率才是 0 MW。
     * 真正 SCRAM 时 scram() 把 tShutdown_ 设为当前 t_ (>=0)，衰变热正常取 6.3% 并衰减。 */
    if (tShutdown_ < 0.0) return 0.0;
    double ts = wasScrammed_ ? (t_ - tShutdown_) : 1.0;
    if (ts < 1.0) ts = 1.0;
    double v = DECAY_COEFF * (std::pow(ts, -0.2) - std::pow(ts + T_OPERATION, -0.2));
    return v > 0.0 ? v : 0.0;
}

/* ---------------- 控制棒 (石墨位移器) ---------------- */

/* 单节点分摊价值：把整根棒的总价值按节点重叠长度分摊到该节点（未乘 N）。
 * 注意返回值只代表"这个节点看到了多少"，全堆芯的有效量要取 8 节点平均。
 *
 * 真实几何（2026-10-17 修正）：组件自下而上为
 *   石墨位移段 4.56 m → 水隙 1.25 m → 吸收段 6.2 m，全行程 6.55 m。
 * 用“距堆芯底部高度 z”作坐标（0=底部，L_CORE=顶部）：
 *   全提(Δ=0)：位移段位于堆芯中部，上下各留约 1.22 m 水柱；吸收段整体在堆芯之上。
 *   全插(Δ=6.55)：位移段落到堆芯以下，吸收段覆盖 0.48~6.68 m。
 */
double RbmkCore::rodShare(int j, double position) const
{
    const double nodeLen = L_CORE / (double)N;
    const double z0 = (double)j       * nodeLen;
    const double z1 = (double)(j + 1) * nodeLen;

    /* 插入行程 Δ: position=1 -> 全提 (Δ=0), position=0 -> 全插 (Δ=ROD_TRAVEL) */
    const double dz = (1.0 - position) * ROD_TRAVEL;

    const double dispLo = (L_CORE - L_DISPLACER) * 0.5 - dz;
    const double dispHi = dispLo + L_DISPLACER;
    const double absLo  = dispHi + L_GAP;
    const double absHi  = absLo + L_ABSORBER;

    double tipOverlap = overlapLen(dispLo, dispHi, z0, z1);
    double absOverlap = overlapLen(absLo,  absHi,  z0, z1);

    double tipDensity = ROD_TIP_WORTH / L_DISPLACER;
    double absDensity = ROD_ABS_WORTH / L_ABSORBER;

    /* 标定说明（重要）：
     * ROD_TIP_WORTH / ROD_ABS_WORTH 给的是**整根棒的总价值**，这里按节点重叠长度
     * 分摊到 8 个节点。点堆方程里每个节点用的是自己的 rho_j，而全堆芯的有效反应性
     * 是 reactivityMean()（通量加权**平均**），所以这里必须再乘 N，
     * 才能让"平均"等于"总价值"—— 和空泡项（VOID_WORTH × 平均 α）的处理方式一致。
     * 若不乘 N，全插棒只等效约 -700 pcm（差 8 倍），堆芯根本停不下来。 */
    return (double)N * (tipDensity * tipOverlap + absDensity * absOverlap);
}

/* 8 节点平均 = 整根棒的总价值 (rodShare 乘过 N, 平均刚好还原) */
double RbmkCore::rodShareMean(double position) const
{
    double s = 0.0;
    for (int j = 0; j < N; ++j) s += rodShare(j, position);
    return s / (double)N;
}

/* 实际施加到节点 j 的棒反应性: 总价值 + 阻尼轴向形状。
 * 8 节点粗网格下, 石墨位移段会让顶部节点出现 +1000 pcm 级的局部尖峰
 * (全插时 rodShare 顶部节点 = +1062 pcm), 通量一旦向顶部偏移就会
 * 正反馈失控 (实测全插停堆态 rho 从 -5810 pcm 跳到 +981 pcm)。
 * 这里保留总量、阻尼形状: 节点值 = 总价值 x (1 + DAMP x (形状比 - 1)),
 * 均匀通量下全堆平均仍严格等于总价值, 石墨位移器的非单调特性保留。 */
double RbmkCore::rodReactivity(int j, double position) const
{
    /* 整组棒（211 根压成一个平均棒位）的两个部分分开处理：
     *  - 吸收段 6.2 m：按轴向**均匀**处理，只贡献总量；
     *  - 石墨位移段：保留逐节点几何，保留“底部挤水→正反应性”的正停堆效应。
     * 若吸收段也按几何分摊，深插时节点 4~7 被整段覆盖、节点 0~3 完全没有，
     * 8 节点网格里会出现 3000+ pcm 的轴向台阶 → 通量被压出空洞
     * （实测 n=[1.5 1.5 1.4 1.3 0.05 0.05 1.1 1.5]）。 */
    const double nodeLen = L_CORE / (double)N;
    const double dz = (1.0 - position) * ROD_TRAVEL;
    const double dispLo = (L_CORE - L_DISPLACER) * 0.5 - dz;
    const double dispHi = dispLo + L_DISPLACER;

    const double tipDensity = ROD_TIP_WORTH / L_DISPLACER;
    double tipMean = 0.0;
    for (int k = 0; k < N; ++k)
        tipMean += (double)N * tipDensity
                 * overlapLen(dispLo, dispHi, k * nodeLen, (k + 1) * nodeLen);
    tipMean /= (double)N;

    double tipShare = (double)N * tipDensity
                    * overlapLen(dispLo, dispHi, j * nodeLen, (j + 1) * nodeLen);

    return rodShareMean(position) + (tipShare - tipMean);
}

void RbmkCore::updateRodPosition(double dt)
{
    double speed = useScramSpeed_ ? (1.0 / SCRAM_TIME) : rodSpeed_;
    double d = rodTarget_ - rodPos_;
    double step = speed * dt;
    if (std::fabs(d) <= step) rodPos_ = rodTarget_;
    else rodPos_ += (d > 0.0 ? step : -step);
    rodPos_ = clampd(rodPos_, 0.0, 1.0);
}

/* ---------------- 裂变产物解析推进 ---------------- */

void RbmkCore::advanceFissionProducts(double dt)
{
    for (int j = 0; j < N; ++j) {
        double n = u_[j];

        /* 六群先驱核 */
        for (int g = 0; g < N_PRECURSOR; ++g) {
            double lam = LAMBDA_G[g];
            double e   = std::exp(-lam * dt);
            double beta = BETA_G[g];
            C_[j][g] = C_[j][g] * e
                     + (beta / LAMBDA_PROMPT) * (n / lam) * (1.0 - e);
        }

        /* 碘-135 */
        double Iold = In_[j];
        double eI   = std::exp(-LAMBDA_I * dt);
        double RI   = GAMMA_I * FISSION_RATE_NOM * n;
        double Inew = Iold * eI + (RI / LAMBDA_I) * (1.0 - eI);

        /* 氙-135 (a = lambda_Xe + sigma_Xe * phi) */
        double phi = FISSION_RATE_NOM * n / SIGMA_F;
        double a   = LAMBDA_XE + SIGMA_XE * phi;
        double eX  = std::exp(-a * dt);
        double RX  = GAMMA_XE * FISSION_RATE_NOM * n;
        double Xenew = Xen_[j] * eX
                     + ((RX + LAMBDA_I * Iold) / a) * (1.0 - eX);

        if (Inew < 0.0) Inew = 0.0;
        if (Xenew < 0.0) Xenew = 0.0;
        In_[j]  = Inew;
        Xen_[j] = Xenew;
    }
}

/* ---------------- 右端函数 ---------------- */

void RbmkCore::rhs(const double* u, double* du) const
{
    const double* nA    = u;
    const double* TfA   = u +     N;
    const double* TcA   = u + 2 * N;
    const double* TcoolA= u + 3 * N;
    const double* TmodA = u + 4 * N;

    const double decay = decayHeatFraction();
    const double vmNow = voidMean(u);      /* 堆芯平均空泡（体积平均） */

    for (int j = 0; j < N; ++j) {
        double n     = nA[j];
        double Tf    = TfA[j];
        double Tc    = TcA[j];
        double Tcool = TcoolA[j];
        double Tmod  = TmodA[j];

        /* --- 反应性 --- */
        double rho = rhoTrim_[j] + rhoExt_
                   + rodReactivity(j, rodPos_)
                   + ALPHA_DOPPLER * (std::sqrt(Tf + 273.15) - std::sqrt(T_FUEL_NOM + 273.15))
                   + ALPHA_MOD * (Tmod - T_MOD_NOM)
                   + VOID_WORTH * vmNow
                   + xenonAlpha_ * Xen_[j]
                   + shutdownMargin_ * shutdownMarginWeight();   /* 停堆裕度随提棒消失 */

        /* --- 缓发中子源 --- */
        double src = 0.0;
        for (int g = 0; g < N_PRECURSOR; ++g)
            src += LAMBDA_G[g] * C_[j][g];

        /* --- 轴向空间耦合 W_jk (n_k - n_j) --- */
        double leak = 0.0;
        if (j > 0)     leak += W_AXIAL * (nA[j - 1] - n);
        if (j < N - 1) leak += W_AXIAL * (nA[j + 1] - n);

        /* 第三项 SRC_NEUTRON 是外源中子, 保证停堆状态下仍有源量程通量。
         * 进入中子方程的 ρ 做瞬发临界夹紧（见 rbmk_params.h 说明）：
         * 真实的 ρ>1.2β 在 50 ms 步长下不可解析，不夹紧会直接数值溢出。 */
        double rhoN = rho;
        if (rhoN > RHO_PROMPT_CLAMP) rhoN = RHO_PROMPT_CLAMP;
        du[j] = (rhoN - BETA_EFF) / LAMBDA_PROMPT * n + src + leak + SRC_NEUTRON;

        /* --- 燃料区 (裂变功率 + 衰变热; 燃料只占 94%) --- */
        double Pf = P_FUEL_NODE * (1.0 - DECAY_NOM_FRAC) * n;
        double Pd = P_FUEL_NODE * decay;
        du[N + j] = (Pf + Pd - H_FC * (Tf - Tc)) / (M_FUEL_NODE * CP_FUEL);

        /* --- 包壳区 --- */
        du[2 * N + j] = (H_FC * (Tf - Tc) - H_CM * (Tc - Tcool))
                      / (M_CLAD_NODE * CP_CLAD);

        /* --- 冷却剂区 (焓平衡, 沸腾段隐含潜热) --- */
        double Tin   = inletTemp(j, u);
        double qin   = H_CM * (Tc - Tcool) + H_MOD * (Tmod - Tcool);
        double qflow = 2.0 * flow_ * (enthalpyOf(Tcool) - enthalpyOf(Tin));
        double Ccool = M_COOL_NODE * dhdTOf(Tcool);
        /* 离线冷却：外部河水经换热器冷却一回路（20C 冷源置换热沉，开式不回收）
         * 注意 offlineFlow_ 是**全堆总流量**，这里按节点均分（总流量/8）——
         * 旧实现每个节点都吃总流量，等效放大 8 倍，会把冷却剂冷到入口温度以下。 */
        double qoffline = (offlineFlow_ / (double)N) * CP_WATER * (Tcool - T_RIVER);
        if (qoffline < 0.0) qoffline = 0.0;
        du[3 * N + j] = (qin - qflow - qoffline) / Ccool;

        /* --- 慢化剂(石墨)区 --- */
        double Qmod = Q_MOD_FRACTION * P_NODE_NOM * n;
        du[4 * N + j] = (Qmod + H_MOD * (Tcool - Tmod)) / (M_MOD_NODE * CP_MOD);
    }
}

/* ---------------- Jacobian (中心差分) ---------------- */

void RbmkCore::jacobian(const double* u, double A[NDIM][NDIM]) const
{
    double up[NDIM], um[NDIM], fp[NDIM], fm[NDIM];
    for (int k = 0; k < NDIM; ++k) {
        double h = 1e-6 * std::max(1.0, std::fabs(u[k]));
        std::memcpy(up, u, sizeof(double) * NDIM);
        std::memcpy(um, u, sizeof(double) * NDIM);
        up[k] += h;
        um[k] -= h;
        rhs(up, fp);
        rhs(um, fm);
        for (int i = 0; i < NDIM; ++i)
            A[i][k] = (fp[i] - fm[i]) / (2.0 * h);
    }
}

/* ---------------- 稠密线性求解 (部分选主元高斯消元) ---------------- */

bool RbmkCore::solveLinear(double A[NDIM][NDIM], double b[NDIM], double x[NDIM]) const
{
    for (int c = 0; c < NDIM; ++c) {
        int piv = c;
        double best = std::fabs(A[c][c]);
        for (int r = c + 1; r < NDIM; ++r) {
            double v = std::fabs(A[r][c]);
            if (v > best) { best = v; piv = r; }
        }
        if (best < 1e-14) return false;
        if (piv != c) {
            for (int k = 0; k < NDIM; ++k) std::swap(A[c][k], A[piv][k]);
            std::swap(b[c], b[piv]);
        }
        double dA = A[c][c];
        for (int r = c + 1; r < NDIM; ++r) {
            double m = A[r][c] / dA;
            if (m == 0.0) continue;
            for (int k = c; k < NDIM; ++k) A[r][k] -= m * A[c][k];
            b[r] -= m * b[c];
        }
    }
    for (int i = NDIM - 1; i >= 0; --i) {
        double s = b[i];
        for (int k = i + 1; k < NDIM; ++k) s -= A[i][k] * x[k];
        x[i] = s / A[i][i];
    }
    return true;
}

/* ---------------- 单步推进: TR-BDF2 ---------------- */

void RbmkCore::step(double dt)
{
    if (dt <= 0.0 || frozen_) return;

    updateRodPosition(dt);

    /* ---- AR 自动调节器: 速率受限 PI, 代表自动调节棒组 ----
     * 一旦 SCRAM 动作, 调节器立即被保护系统旁通 (与真实机组一致)。 */
    if (regEnabled_ && !scramLatch_) {
        double err = (pSet_ - totalPowerW()) / pSet_;
        double di  = err * regKi_ * dt;
        regIntegral_ += di;
        double target = regKp_ * err + regIntegral_;
        if (target >  regLimit_) target =  regLimit_;
        if (target < -regLimit_) target = -regLimit_;
        double d = target - rhoExt_;
        double maxStep = regRate_ * dt;
        if (d >  maxStep) d =  maxStep;
        if (d < -maxStep) d = -maxStep;
        rhoExt_ += d;
        if (rhoExt_ >  regLimit_) rhoExt_ =  regLimit_;
        if (rhoExt_ < -regLimit_) rhoExt_ = -regLimit_;
        /* 抗积分饱和 */
        if ((rhoExt_ >= regLimit_ && err > 0.0) || (rhoExt_ <= -regLimit_ && err < 0.0))
            regIntegral_ -= di;
    }

    /* 解析推进裂变产物 (用步初中子密度) */
    advanceFissionProducts(dt);

    double u0[NDIM];
    std::memcpy(u0, u_, sizeof(u0));
    double f0[NDIM];
    rhs(u0, f0);

    const double g  = TRBDF2_GAMMA;
    const double c1 = 0.5 * g * dt;

    /* ---------- 第一步: TR (梯形法则) ---------- */
    double us[NDIM];
    std::memcpy(us, u0, sizeof(us));
    double A[NDIM][NDIM];
    double dx[NDIM];
    for (int it = 0; it < NEWTON_MAX_IT; ++it) {
        double fs[NDIM];
        rhs(us, fs);
        double r[NDIM];
        double norm = 0.0;
        for (int i = 0; i < NDIM; ++i) {
            r[i] = us[i] - u0[i] - c1 * (f0[i] + fs[i]);
            norm = std::max(norm, std::fabs(r[i]));
        }
        if (norm < NEWTON_TOL) break;
        jacobian(us, A);
        for (int i = 0; i < NDIM; ++i)
            for (int k = 0; k < NDIM; ++k)
                A[i][k] = (i == k ? 1.0 : 0.0) - c1 * A[i][k];
        if (!solveLinear(A, r, dx)) break;
        for (int i = 0; i < NDIM; ++i) us[i] -= dx[i];
    }

    /* ---------- 第二步: BDF2 ---------- */
    const double a1 = 1.0 / (g * (2.0 - g));
    const double a2 = -((1.0 - g) * (1.0 - g)) / (g * (2.0 - g));
    const double a3 = (1.0 - g) / (2.0 - g);

    double un[NDIM];
    std::memcpy(un, us, sizeof(un));
    for (int it = 0; it < NEWTON_MAX_IT; ++it) {
        double fn[NDIM];
        rhs(un, fn);
        double r[NDIM];
        double norm = 0.0;
        for (int i = 0; i < NDIM; ++i) {
            r[i] = un[i] - a1 * us[i] - a2 * u0[i] - a3 * dt * fn[i];
            norm = std::max(norm, std::fabs(r[i]));
        }
        if (norm < NEWTON_TOL) break;
        jacobian(un, A);
        for (int i = 0; i < NDIM; ++i)
            for (int k = 0; k < NDIM; ++k)
                A[i][k] = (i == k ? 1.0 : 0.0) - a3 * dt * A[i][k];
        if (!solveLinear(A, r, dx)) break;
        for (int i = 0; i < NDIM; ++i) un[i] -= dx[i];
    }

    /* ---------- 物理限幅与提交 ---------- */
    for (int j = 0; j < N; ++j) {
        if (!(un[j] > 1e-12)) un[j] = 1e-12;                 /* 中子密度非负（NaN 也归到这里） */
        if (un[j] > N_MAX)    un[j] = N_MAX;                 /* 瞬发冲击上限，见 params 说明 */
        if (!(un[N + j] > 0.0))      un[N + j] = 1.0;        /* 燃料温度      */
        if (!(un[2 * N + j] > 0.0))  un[2 * N + j] = 1.0;    /* 包壳温度      */
        if (!(un[3 * N + j] > 0.0))  un[3 * N + j] = 1.0;    /* 冷却剂温度    */
        if (!(un[4 * N + j] > 0.0))  un[4 * N + j] = 1.0;    /* 石墨温度      */
    }
    std::memcpy(u_, un, sizeof(un));
    t_ += dt;
}

/* ---------------- 控制 ---------------- */

void RbmkCore::setRodPosition(double p)
{
    rodPos_ = clampd(p, 0.0, 1.0);
    rodTarget_ = rodPos_;
}

void RbmkCore::setRodTarget(double p)
{
    rodTarget_ = clampd(p, 0.0, 1.0);
}

void RbmkCore::setRodSpeed(double perSec)
{
    rodSpeed_ = (perSec > 1e-9) ? perSec : 1e-9;
}

void RbmkCore::useScramSpeed(bool on) { useScramSpeed_ = on; }

/* 停堆初值: 建立额定物性, 然后
 *   - 控制棒全部插入 (position = 0)
 *   - 中子密度压到源量程 (FLUX_SHUTDOWN)
 *   - AR 自动调节器退出
 * 这样开机就是"停堆状态", 界面显示 0 MWt / N ~ 5e-11, 与模拟器原版一致。
 * 之后再靠操作员提棒启动。                                          */
void RbmkCore::resetToShutdown()
{
    resetToNominal();                  /* 先拿到自洽的裂变产物分布 */

    for (int j = 0; j < N; ++j) {
        u_[j] = FLUX_SHUTDOWN;
        for (int g = 0; g < N_PRECURSOR; ++g) {
            /* 与当前中子密度相称的先驱核平衡值 */
            C_[j][g] = (BETA_G[g] / (LAMBDA_PROMPT * LAMBDA_G[g])) * FLUX_SHUTDOWN;
        }
        /* 燃料/包壳压到停堆平衡温度（≈冷却剂温度）。
         * 若保留满功率燃料温度, 随后燃料冷却会给多普勒送 +2700 pcm,
         * 堆会在不提棒的情况下自发临界 (实测 20 s 内 1.6 -> 2928 MWt)。 */
        /* 冷停堆 = 单相、无空泡、无储热：冷却剂/燃料/包壳/石墨全部压到入口温度。
         * 旧实现只把燃料压到冷却剂温度，却保留额定的沸腾剖面（ᾱ≈0.40）和 650°C
         * 石墨：零功率下入口 270°C 的水 60 s 内就把两相段冷凝掉（空泡项 -790 pcm），
         * 随后石墨再向冷却剂放热（-280 pcm / 6 min）——冷启动还没提棒就掉了 1000 pcm，
         * 实测提棒到 0.50 也只有 -963 pcm，根本起不来。
         * 现在改成与"长期停堆"语义一致的冷态：全堆 270°C，α=0，石墨无储热。 */
        const double Tcold = T_INLET_NOM;
        u_[3 * N + j] = Tcold;           /* Tcool */
        u_[N + j]     = Tcold;           /* Tf    */
        u_[2 * N + j] = Tcold;           /* Tc    */
        u_[4 * N + j] = Tcold;           /* Tmod  */
    }

    rodPos_        = 0.0;
    rodTarget_     = 0.0;
    rodSpeed_      = ROD_SPEED_NORM;
    scramLatch_    = false;
    useScramSpeed_ = false;
    /* 表示"已停堆很久": wasScrammed_ = true 且 tShutdown_ 取很久以前，
     * 这样 decayHeatFraction() 里 ts 很大 -> 衰变热趋近 0,
     * 界面才会显示 0 MWt 而不是 6.3% 的堆芯余热。
     * 之后若真的触发 SCRAM, scram() 会把 tShutdown_ 重置为当前时刻,
     * 衰变热立刻回到 6.3%, 行为正确。 */
    wasScrammed_   = true;
    tShutdown_     = -1.0e7;
    rhoExt_        = 0.0;
    regIntegral_   = 0.0;
    regEnabled_    = false;
    t_             = 0.0;

    /* 氙/碘压到"停堆很久"的平衡（≈0）: 与 tShutdown_ = -1e7 的语义一致。
     * 若保留满功率氙毒 (-2800 pcm), 全提棒后 ρ 仍差 ~3000 pcm, 堆无法启动。 */
    for (int j = 0; j < N; ++j) {
        In_[j]  = 0.0;
        Xen_[j] = 0.0;
    }

    /* 停堆深度：目标 -7500 pcm（用户口径 -6000~-9000）。
     * 冷态下棒价值本身只能把 ρ 压到 -3473 pcm（冷态多普勒 +1550、未积累氙 +2800、
     * 空泡正反应性把 -10000 pcm 的棒价值抵消掉一多半），所以用 shutdownMargin_
     * 补一层裕度；它按 (1-棒位) 衰减，全提棒时为 0，冷启动曲线与启动余量
     * （+6982 pcm）完全不受影响。 */
    shutdownMargin_ = 0.0;
    const double SHUTDOWN_TARGET = -0.075;       /* -7500 pcm */
    double rhoAllIn = reactivityMean();          /* rodPos_ = 0 */
    if (rhoAllIn > SHUTDOWN_TARGET) {
        shutdownMargin_ = SHUTDOWN_TARGET - rhoAllIn;
    }
}

void RbmkCore::scram()
{
    if (scramLatch_) return;
    scramLatch_    = true;
    useScramSpeed_ = true;
    rodTarget_     = 0.0;
    tShutdown_     = t_;
    wasScrammed_   = true;
}

void RbmkCore::resetScram()
{
    scramLatch_    = false;
    useScramSpeed_ = false;
    rodTarget_     = rodPos_;
}

void RbmkCore::setFlow(double kgs)        { flow_ = (kgs > 1.0) ? kgs : 1.0; }

void RbmkCore::setOfflineFlow(double kgs)  { offlineFlow_ = (kgs > 0.0) ? kgs : 0.0; }
void RbmkCore::setSaturationTemp(double c) { tSat_ = clampd(c, 100.0, 370.0); }
void RbmkCore::setInletEnthalpy(double jPerKg) { hIn_ = jPerKg; }
void RbmkCore::setFlowFraction(double f)  { setFlow(MDOT_NOM * clampd(f, 0.0, 2.0)); }
void RbmkCore::freeze()                   { frozen_ = true; }

void RbmkCore::setRegulatorEnabled(bool on)
{
    regEnabled_ = on;
    if (!on) { rhoExt_ = 0.0; regIntegral_ = 0.0; }
}

void RbmkCore::setPowerSetpoint(double watts)
{
    pSet_ = (watts > 1.0e6) ? watts : 1.0e6;
}

void RbmkCore::setRegulatorGains(double kp, double ki, double ratePerSec, double limit)
{
    regKp_    = kp;
    regKi_    = ki;
    regRate_  = ratePerSec;
    regLimit_ = limit;
}

/* ---------------- 输出量 ---------------- */

double RbmkCore::nodePowerW(int j) const
{
    /* 总功率 = 燃料裂变+衰变(94%) + 石墨沉积(6%)；n=1 时严格 600 MW。 */
    return P_FUEL_NODE * ((1.0 - DECAY_NOM_FRAC) * u_[j] + decayHeatFraction())
         + Q_MOD_FRACTION * P_NODE_NOM * u_[j];
}

double RbmkCore::totalPowerW() const
{
    double s = 0.0;
    for (int j = 0; j < N; ++j) s += nodePowerW(j);
    return s;
}

double RbmkCore::totalElectricW() const
{
    return totalPowerW() * (P_ELECTRIC_NOM / P_THERMAL_NOM);
}

/* 停堆裕度的棒位权重：全插(0) = 1，提到 0.30 以后 = 0。
 * 之所以要衰减，是因为这一层裕度是"附加"的（冷态多普勒/氙/空泡把棒价值抵消掉
 * 一多半，光靠棒价值到不了 -6000 pcm）。若按 (1-棒位) 线性衰减，临界点会被推到
 * 棒位 0.9 以外（实测 rod=0.46 时 ρ=-2175 pcm，永远临不了一界）。
 * 现在只在深插区有效：开机/ scram 后全插 → -7500 pcm；操作员提到 0.30 以上后
 * 与加这一层之前完全一致，冷启动曲线和临界棒位不变。 */
double RbmkCore::shutdownMarginWeight() const
{
    const double FADE = 0.30;
    double w = (FADE - rodPos_) / FADE;
    return (w < 0.0) ? 0.0 : ((w > 1.0) ? 1.0 : w);
}

double RbmkCore::nodeRho(int j) const
{
    double Tf    = u_[N + j];
    double Tmod  = u_[4 * N + j];
    return rhoTrim_[j] + rhoExt_
         + rodReactivity(j, rodPos_)
         + ALPHA_DOPPLER * (std::sqrt(Tf + 273.15) - std::sqrt(T_FUEL_NOM + 273.15))
         + ALPHA_MOD * (Tmod - T_MOD_NOM)
         + VOID_WORTH * voidMean(u_)
         + xenonAlpha_ * Xen_[j]
         + shutdownMargin_ * shutdownMarginWeight();   /* 停堆裕度随提棒消失 */
}

double RbmkCore::nodeRhoDoppler(int j) const
{
    double Tf = u_[N + j];
    return ALPHA_DOPPLER * (std::sqrt(Tf + 273.15) - std::sqrt(T_FUEL_NOM + 273.15));
}

double RbmkCore::nodeRhoMod(int j) const
{
    return ALPHA_MOD * (u_[4 * N + j] - T_MOD_NOM);
}

double RbmkCore::nodeRhoVoid(int j) const
{
    (void)j;
    return VOID_WORTH * voidMean(u_);     /* 堆芯平均空泡项，各节点相同 */
}

double RbmkCore::nodeRhoXe(int j) const
{
    return xenonAlpha_ * Xen_[j];
}

double RbmkCore::nodeRhoRod(int j) const
{
    return rodReactivity(j, rodPos_);
}

double RbmkCore::reactivityMean() const
{
    double s = 0.0, w = 0.0;
    for (int j = 0; j < N; ++j) {
        double nj = u_[j];
        s += nodeRho(j) * nj;
        w += nj;
    }
    return (w > 0.0) ? (s / w) : 0.0;
}

double RbmkCore::trimReactivity() const
{
    double s = 0.0;
    for (int j = 0; j < N; ++j) s += rhoTrim_[j];
    return s / (double)N;
}

double RbmkCore::kEff() const
{
    double rho = reactivityMean();
    if (rho >= 1.0) return 1e6;
    return 1.0 / (1.0 - rho);
}

double RbmkCore::period() const
{
    /* 反应堆周期：inhour 方程（6 群缓发中子）
     *     rho = L/T + sum_i beta_i / (1 + lambda_i * T)
     * 旧实现返回瞬发周期 L/(rho-beta)：rho<beta 时恒为负 → UI 映射成 ∞，
     * 周期表永远没有读数，AZ-2 的“周期<20 s”也永远不会动作。
     * 这里对 x = 1/T 做二分（rho(x) 单调递增，x∈(0,∞)），rho<=0 视为无发散周期。 */
    double rho = reactivityMean();
    if (rho <= 1e-9) return 1e9;

    const double lo0 = 1e-12, hi0 = 1e6;
    double lo = lo0, hi = hi0;
    for (int it = 0; it < 200; ++it) {
        double x = std::sqrt(lo * hi);
        double f = LAMBDA_PROMPT * x;
        for (int g = 0; g < N_PRECURSOR; ++g)
            f += BETA_G[g] * x / (x + LAMBDA_G[g]);
        if (f < rho) lo = x; else hi = x;
    }
    double x = std::sqrt(lo * hi);
    if (x <= 0.0) return 1e9;
    return 1.0 / x;
}

double RbmkCore::nodeN(int j)      const { return u_[j]; }
double RbmkCore::nodeVoid(int j)   const { return voidOf(u_[3 * N + j]); }
double RbmkCore::nodeTf(int j)     const { return u_[N + j]; }
double RbmkCore::nodeTc(int j)     const { return u_[2 * N + j]; }
double RbmkCore::nodeTcool(int j)  const { return u_[3 * N + j]; }
double RbmkCore::nodeTmod(int j)   const { return u_[4 * N + j]; }
double RbmkCore::nodeTin(int j)    const { return inletTemp(j, u_); }
double RbmkCore::nodeI(int j)      const { return In_[j]; }
double RbmkCore::nodeXe(int j)     const { return Xen_[j]; }

double RbmkCore::nodeXeWorthPcm(int j) const
{
    return xenonAlpha_ * Xen_[j] * 1.0e5;
}

/* ---------------- Golden Test 探针 ---------------- */

double RbmkCore::probeVoidCoefficientPcm()
{
    /* dρ/dα = VOID_WORTH（对 α 线性），每 1% 空泡份额 = VOID_WORTH×0.01 */
    return VOID_WORTH * 0.01 * 1.0e5;
}

double RbmkCore::probeFullVoidInsertionPcm()
{
    /* 空泡 0 -> 100% 的堆芯反应性插入 */
    return VOID_WORTH * 1.0 * 1.0e5;
}

double RbmkCore::probeQuality(int j) const
{
    return qualityOf(u_[3 * N + j]);
}

double RbmkCore::probeDopplerCoefficient()
{
    return ALPHA_DOPPLER * 1.0e5;            /* pcm / sqrt(K) */
}

double RbmkCore::probeModeratorCoefficient()
{
    return ALPHA_MOD * 1.0e5;                /* pcm / K */
}

double RbmkCore::probeRodWorth(double position, int j)
{
    return rodReactivity(j, position) * 1.0e5;   /* pcm */
}

double RbmkCore::probeFuelChannelPowerMW() const
{
    return totalPowerW() / N_CHANNELS / 1.0e6;
}

double RbmkCore::probeXenonPeakPcm(double* tPeakSeconds)
{
    /* 停堆后通量归零, 氙浓度的闭式解 (从平衡值出发) */
    double Xe0 = XE_EQ_NOM;
    double I0  = I_EQ_NOM;
    double A   = LAMBDA_I * I0 / (LAMBDA_I - LAMBDA_XE);
    double ratio = (LAMBDA_XE * (Xe0 + A)) / (A * LAMBDA_I);
    double tpk = 0.0;
    if (ratio > 0.0 && ratio < 1.0)
        tpk = std::log(ratio) / (LAMBDA_XE - LAMBDA_I);
    if (tpk < 0.0) tpk = 0.0;
    double e1 = std::exp(-LAMBDA_XE * tpk);
    double e2 = std::exp(-LAMBDA_I  * tpk);
    double Xepk = Xe0 * e1 + A * (e1 - e2);
    if (tPeakSeconds) *tPeakSeconds = tpk;
    return xenonAlpha_ * Xepk * 1.0e5;
}

/* ---------------- 存档状态 ----------------
 * 打包顺序必须与 RbmkCore.java 的 STATE_SIZE 注释一致。
 * 这里保存的是完整动力学状态（而不是 UI 读数），因此读档后
 * 功率/氙/温度/棒位/衰变热历史都能从同一时刻继续推进。 */
void RbmkCore::saveState(double* out) const
{
    int k = 0;
    for (int i = 0; i < NDIM; ++i) out[k++] = u_[i];
    for (int j = 0; j < N; ++j)
        for (int g = 0; g < N_PRECURSOR; ++g) out[k++] = C_[j][g];
    for (int j = 0; j < N; ++j) out[k++] = In_[j];
    for (int j = 0; j < N; ++j) out[k++] = Xen_[j];

    out[k++] = t_;
    out[k++] = rodPos_;
    out[k++] = rodTarget_;
    out[k++] = rodSpeed_;
    out[k++] = flow_;
    out[k++] = offlineFlow_;
    for (int j = 0; j < N; ++j) out[k++] = rhoTrim_[j];
    out[k++] = shutdownMargin_;
    out[k++] = rhoExt_;
    out[k++] = regIntegral_;
    out[k++] = tShutdown_;
    out[k++] = scramLatch_ ? 1.0 : 0.0;
    out[k++] = useScramSpeed_ ? 1.0 : 0.0;
    out[k++] = wasScrammed_ ? 1.0 : 0.0;
}

bool RbmkCore::loadState(const double* in)
{
    if (!in) return false;

    /* 第一遍：只校验，不写入。损坏的存档必须整体拒绝，不能半恢复。 */
    const int rodPosIdx = NDIM + N * N_PRECURSOR + 2 * N + 1;
    for (int i = 0; i < STATE_SIZE; ++i)
        if (!std::isfinite(in[i])) return false;
    for (int i = NDIM; i < NDIM + N * N_PRECURSOR; ++i)
        if (in[i] < 0.0) return false;                    /* 先驱核浓度 */
    for (int i = NDIM + N * N_PRECURSOR;
         i < NDIM + N * N_PRECURSOR + 2 * N; ++i)
        if (in[i] < 0.0) return false;                    /* 碘 / 氙    */
    if (in[rodPosIdx] < -0.01 || in[rodPosIdx] > 1.01) return false;
    if (in[rodPosIdx + 1] < -0.01 || in[rodPosIdx + 1] > 1.01) return false;

    /* 第二遍：写入。 */
    int k = 0;
    for (int i = 0; i < NDIM; ++i) u_[i] = in[k++];
    for (int j = 0; j < N; ++j)
        for (int g = 0; g < N_PRECURSOR; ++g) C_[j][g] = in[k++];
    for (int j = 0; j < N; ++j) In_[j] = in[k++];
    for (int j = 0; j < N; ++j) Xen_[j] = in[k++];

    t_ = in[k++];
    rodPos_ = in[k++];
    rodTarget_ = in[k++];
    rodSpeed_ = in[k++];
    flow_ = in[k++];
    offlineFlow_ = in[k++];
    for (int j = 0; j < N; ++j) rhoTrim_[j] = in[k++];
    shutdownMargin_ = in[k++];
    rhoExt_ = in[k++];
    regIntegral_ = in[k++];
    tShutdown_ = in[k++];
    scramLatch_ = in[k++] > 0.5;
    useScramSpeed_ = in[k++] > 0.5;
    wasScrammed_ = in[k++] > 0.5;

    frozen_ = false;
    return k == STATE_SIZE;
}

} /* namespace rbmk */
