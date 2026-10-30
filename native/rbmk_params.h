/*
 * rbmk_params.h - RBMK-1500 真实物理内核参数表
 *
 * 参考文档: "RBMK-1500 反应堆 · 核心公式集"
 * 只有功率、流量、温度水平按 1.5 倍放大; 反应性系数 / 时间常数 / SCRAM 时序
 * 与 RBMK-1000 完全相同。
 *
 * 反应性一律使用绝对单位 dk/k (1 pcm = 1e-5)。
 */
#ifndef RBMK_PARAMS_H
#define RBMK_PARAMS_H

namespace rbmk {

/* ---------- 堆型总体 ---------- */
static const int    N            = 8;              /* 轴向节点数             */
static const int    NDIM         = 5 * N;          /* TR-BDF2 状态维数 = 40  */
static const int    N_PRECURSOR  = 6;              /* 缓发中子先驱核群数     */

static const double P_THERMAL_NOM   = 4800.0e6;    /* 额定热功率 W           */
static const double P_ELECTRIC_NOM  = 1500.0e6;    /* 额定电功率 W           */
static const double N_CHANNELS      = 1661.0;      /* 燃料通道总数           */
static const double MDOT_NOM        = 12000.0;     /* 冷却剂总流量 kg/s      */
static const double P_SYSTEM        = 6.9e6;       /* 压力管压力 Pa          */

static const double T_INLET_NOM     = 270.0;
static const double T_RIVER         = 20.0;        /* 离线冷却河水温度 C     */       /* 入口温度 C             */
static const double T_SAT           = 284.0;       /* 6.9 MPa 饱和温度 C     */
static const double DT_BOIL         = 40.0;        /* 空泡-温度拉伸尺度 K    */

/* 石墨温度带 600-700 C, 取名义 650 C */
static const double T_MOD_NOM       = 650.0;
static const double T_FUEL_NOM      = 650.0;
static const double T_CLAD_NOM      = 400.0;
static const double T_COOL_REF      = 270.0;       /* 冷却剂焓参考温度 C     */

/* ---------- 中子动力学 ---------- */
static const double BETA_EFF        = 0.00650;     /* 650 pcm                */
/* 瞬发中子代时间：RBMK 约 0.7 ms（不是 20 µs）。20 µs 会让瞬发超临界演化
 * 快约 35 倍，SCRAM/功率冲击的时间尺度完全不真实。 */
static const double LAMBDA_PROMPT   = 7.0e-4;      /* 瞬发中子代时间 s       */

/* 外源中子 (自发裂变 / (alpha,n) 反应的等效源)。
 * 没有它, 停堆后通量衰减到 0 就再也起不来 —— 无法从停堆状态启动。
 * 按 n_eq = S * LAMBDA_PROMPT / |rho| 反标定：全插棒 |rho|≈0.055 时
 * 源量程平衡通量约 5e-11（与界面初始值一致）。 */
static const double SRC_NEUTRON     = 4.0e-9;      /* 相对额定通量 / s       */
/* 停堆初始通量 (源量程), 对应原版界面上的 N = 4.8e-11 */
static const double FLUX_SHUTDOWN   = 5.0e-11;

/* 六群缓发中子 (Keepin 1957, U-235 热谱), 单位: 绝对 dk/k */
static const double BETA_G[6] = {
    21.5e-5, 142.4e-5, 127.4e-5, 256.8e-5, 74.8e-5, 27.3e-5
};
static const double LAMBDA_G[6] = {
    0.0124, 0.0305, 0.111, 0.301, 1.14, 3.01
};

/* 空间耦合系数 W_jk = (D*v)*A / (V*delta), 量纲 s^-1
 * D ~ 1.0 cm, 扩散长度 ~7 cm, 节点高度 h = 700/8 = 87.5 cm
 * 取 D_B = D * v_thermal 的等效值使 W = D_B / h^2 落在合理量级          */
static const double W_AXIAL        = 28.7;         /* s^-1, 相邻节点耦合    */

/* ---------- 反应性系数 ---------- */
/* 多普勒 (sqrt(T) 形式, T 用绝对温度 K), 0 at T_FUEL_NOM。
 * 目标：额定点 dρ/dTf = -1.1 pcm/K（公开 RBMK 约 -1.0~-1.2 pcm/K）。
 * -1.1e-5 /K × 2√923.15 K = -6.684e-4 per √K。 */
static const double ALPHA_DOPPLER   = -6.684e-4;   /* per sqrt(K)            */
/* 石墨温度反馈, 0 at T_MOD_NOM。RBMK 石墨温度系数为正：
 * +3.2e-5 ~ +6.0e-5 /K（+3.2 ~ +6.0 pcm/K），取中值 +4.0 pcm/K。
 * 旧值 -1.5e-5 /K 不仅偏大，符号也是错的。 */
static const double ALPHA_MOD       = 4.0e-5;      /* per K, +4 pcm/K        */

/* 空泡反馈（2026-10-19 重构）
 *
 * 2026-10-17 的版本把"空泡"直接当成焓差比例 (h-h_sat)/h_fg —— 那其实是**热力学干度 x**，
 * 不是体积空泡份额；而且反应性对 x 线性、斜率恒定 32.5 pcm/%，在 x→1 之前不饱和。
 * 后果（实测）：额定点净功率系数 ≈ 0（略正），+5 pcm 的小扰动 500 s 内冲到 900 GW。
 *
 * 现在分两步算：
 *   1) 干度 x -> 体积空泡份额 α（含滑速比 S）：α = x·v_g / (x·v_g + S·(1-x)·v_f)
 *   2) 反应性 ρ_void = VOID_WORTH · ᾱ（ᾱ = 8 节点算术平均）
 *
 * 标定依据（INSAG-7 表 / NRC NUREG-1250 Table 2.4）：
 *   RBMK 工作点空泡系数 +2.0e-4 per % void = +20 pcm per 1% 空泡份额；
 *   0→100% 空泡总价值 = 2% Δk/k = 2000 pcm（= 4.2β @ β_eff=480 pcm 的 10.3 MWd/kg 燃料；
 *   本模型取新燃料 β=650 pcm，故折合 3.1β）。
 *   旧值 3250 pcm（5β）是把"5β"直接当总量，比公开值高 1.6 倍。
 *
 * 由于 α(x) 在 x→1 时自然饱和，ρ_void 对功率的导数会随功率升高而减小，
 * 净功率系数在额定点回到负值（实测 ≈ -0.9 pcm/%）。
 */
static const double VF_SAT       = 1.352e-3;      /* 饱和水比容 m³/kg @6.9 MPa (739.5 kg/m³) */
static const double VG_SAT       = 2.740e-2;      /* 饱和汽比容 m³/kg @6.9 MPa (36.5 kg/m³)  */
static const double SLIP_RATIO   = 2.0;           /* 滑速比 u_g/u_f，通道平均取值            */
static const double VOID_WORTH    = 0.02000;      /* dk/k for α: 0 -> 1 (= 2000 pcm)         */

/* 氙毒: 平衡氙反应性 (0 空泡基准), 负值 */
static const double XENON_WORTH_EQ  = -0.02800;    /* -2800 pcm              */

/* ---------- 控制棒 (石墨位移器) ----------
 * 2026-10-17 修正为真实几何（原先 3 m 位移 + 4 m 吸收、无中间水隙，导致
 * 棒位 0.75 时吸收段整个还在堆芯之上，冷态可用反应性只有 +446 pcm，
 * 冷启动根本上不到额定功率）：
 *   整根组件自下而上 = 位移段 4.56 m → 水隙 1.25 m → 吸收段 6.2 m
 *   全提时位移段位于堆芯中部（上下各 1.25 m 水柱），吸收段在堆芯之上
 *   全行程 6.55 m（全插时吸收段覆盖堆芯 0.45~6.65 m）
 */
static const double L_CORE          = 7.0;         /* 堆芯轴向长度 m         */
static const double L_DISPLACER     = 4.56;        /* 石墨位移段长度 m       */
static const double L_GAP           = 1.25;        /* 位移段-吸收段水隙 m    */
static const double L_ABSORBER      = 6.2;         /* 吸收段长度 m           */
static const double ROD_TRAVEL      = 6.55;        /* 控制棒全行程 m         */
/* 位移段全部进入堆芯时的正反应性总量 (= 0.7 beta) */
static const double ROD_TIP_WORTH   = 0.00455;     /* +455 pcm               */
/* 吸收段全部进入堆芯时的负反应性总量
 * 211 根棒整组约 -10000 pcm（单根 ~47 pcm，含棒间遮蔽），与公开数据同量级；
 * 旧值 -6000 太浅，冷态停堆裕度不足。                                  */
static const double ROD_ABS_WORTH   = -0.10000;    /* -10000 pcm             */
static const double SCRAM_TIME      = 18.0;        /* s, 全行程              */
static const double ROD_SPEED_NORM  = 1.0 / 120.0; /* 正常调速 s^-1          */
static const double P_ROD_NOMINAL   = 0.75;        /* 额定运行棒位           */

/* ---------- 碘-135 / 氙-135 ---------- */
static const double GAMMA_I         = 0.0639;
static const double GAMMA_XE        = 0.00237;
static const double LAMBDA_I        = 2.87e-5;     /* s^-1                   */
static const double LAMBDA_XE       = 2.12e-5;     /* s^-1                   */
static const double SIGMA_XE        = 2.65e-18;    /* cm^2                   */
/* 额定裂变率密度: 堆芯体积 58.45 m^3, 功率密度 82.1 W/cm^3,
 * 每次裂变 200 MeV = 3.204e-11 J  ->  R = 2.563e12 fissions/cm^3/s         */
static const double FISSION_RATE_NOM = 2.563e12;   /* 1/cm^3/s               */
static const double SIGMA_F          = 0.10;       /* 宏观裂变截面 1/cm      */
static const double PHI_NOM          = FISSION_RATE_NOM / SIGMA_F; /* 2.563e13 */
static const double I_EQ_NOM         = GAMMA_I * FISSION_RATE_NOM / LAMBDA_I; /* 5.71e15 */
static const double XE_EQ_NOM        = (GAMMA_XE * FISSION_RATE_NOM
                                        + LAMBDA_I * I_EQ_NOM)
                                       / (LAMBDA_XE + SIGMA_XE * PHI_NOM); /* 1.91e15 */

/* ---------- 衰变热 (ANS 5.1 / Way-Wigner) ---------- */
static const double DECAY_COEFF     = 0.066;
static const double T_OPERATION     = 1.0e7;       /* 长期运行等效时间 s     */

/* ---------- 热工水力标定 ----------
 * 各节点功率 P_node = 4800 MW / 8 = 600 MW
 * 稳态: H_FC*(Tf-Tc)=P_node, H_CM*(Tc-Tcool)=P_node, H_MOD*(Tmod-Tcool)=Q_mod
 */
static const double P_NODE_NOM      = P_THERMAL_NOM / N;   /* 600 MW/node    */
static const double Q_MOD_FRACTION  = 0.06;                /* 石墨沉积份额   */
/* 燃料节点功率 = 总功率 - 石墨沉积份额（保证燃料+石墨=600 MW，而不是 636 MW）。
 * 旧实现把 Q_MOD 当成额外热源，冷却剂实际收到 636 MW，而 totalPowerW() 只报 600 MW，
 * 相当于凭空多出 6% 热量。 */
static const double P_FUEL_NODE     = P_NODE_NOM * (1.0 - Q_MOD_FRACTION); /* 564 MW */
static const double DECAY_NOM_FRAC  = 0.0634;              /* 稳态衰变份额   */

static const double H_FC            = 2.40e6;      /* W/K 燃料->包壳         */
static const double H_CM            = 5.22e6;      /* W/K 包壳->冷却剂       */
static const double H_MOD           = 9.86e4;      /* W/K 石墨<->冷却剂      */

static const double M_FUEL_NODE     = 24000.0;     /* kg 每节点 UO2（全堆 192 t） */
static const double CP_FUEL         = 300.0;       /* J/kg/K                 */
static const double M_CLAD_NODE     = 6230.0;      /* kg 每节点 Zr           */
static const double CP_CLAD         = 300.0;       /* J/kg/K                 */
static const double M_COOL_NODE     = 5000.0;      /* kg 每节点水（全堆 ~40 t） */
static const double CP_WATER        = 5500.0;      /* J/kg/K 液相            */
static const double H_FG            = 1.509e6;     /* J/kg 6.9 MPa 汽化潜热  */
static const double M_MOD_NODE      = 240000.0;    /* kg 每节点石墨          */
static const double CP_MOD          = 710.0;       /* J/kg/K                 */

/* ---------- 积分器 ---------- */
static const double TRBDF2_GAMMA    = 0.58578643762690485; /* 2 - sqrt(2)    */
static const int    NEWTON_MAX_IT   = 25;
static const double NEWTON_TOL      = 1e-10;

/* ---------- 瞬发临界的数值护栏 ----------
 * dt=50 ms 无法解析 ρ>β 的瞬发倍增（Λ=20 µs，一步内 n 会涨 e^上百倍）。
 * 没有护栏时 Newton 溢出成 NaN，通量被夹回 1e-12，表现为“棒拉过头功率仍是 0”。
 * 这里把进入中子方程的 ρ 限制在 1.2β，并给相对中子密度一个上限；
 * 真实后果（燃料瞬时汽化/解体）超出本模型表达能力，护栏只用于给出可见的功率冲击。 */
static const double RHO_PROMPT_CLAMP = 1.2 * BETA_EFF;
static const double N_MAX           = 200.0;      /* 相对额定中子密度上限 */

} /* namespace rbmk */

#endif /* RBMK_PARAMS_H */
