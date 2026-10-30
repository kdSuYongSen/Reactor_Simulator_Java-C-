package com.darwish.nppsim;

/**
 * 反应堆物理引擎——C++ 计算核心（rbmk_core.dll，TR-BDF2 + Newton 隐式积分）的 Java 委托层。
 *
 * 物理全部在 C++ 侧完成：8 轴向节点点堆动力学（6 群缓发先驱核）、燃料/包壳/冷却剂/
 * 石墨四温度热块、氙碘链、多普勒/慢化剂/空泡反馈、石墨随动控制棒、停堆衰变热、
 * 逐节点反应性配平的停堆初值。本类只做：
 *   1. 接口适配：棒位/流量语义转换 + Core 需要的字段口径维持
 *   2. 开局状态：resetToShutdown（停堆初值）→ 功率 0 MW、周期 ∞、源量程中子（不人为注入衰变热）
 *
 * 口径差异（Tier 1 简化，均为 C++ 侧定值）：
 *  - 冷却剂入口温度/饱和压力使用 C++ 内置额定（T_INLET_NOM=270°C、T_SAT=284°C@6.9MPa），
 *    Core 采集的 pressureMPa/hInHeader 不再注入引擎（保留字段以维持外部接口）；
 *  - 棒位语义：Java rodInsertion 0=全抽出 1=全插入 → C++ position 1=全提 0=全插；
 *  - 离线冷却流量已由 Core 并入 mdotTotal（2×200 kg/s 直接接入堆芯侧）→ C++ setFlowFraction。
 */
final class ReactorKinetics {

    static final int N = 8;   // 轴向节点数（与 C++ 核心一致）

    private static final double MDOT_NOM = 12000.0;        // C++ 额定流量 kg/s
    private static final double N_NOMINAL = 4.9808177502e13; // 满功率中子数（UI 显示口径）
    private static final double I_FULL = 1.93458087e20;    // 满功率平衡碘原子数（通道表格 % 口径）
    private static final double XE_FULL = 4.6978565e19;    // 满功率平衡氙原子数
    private static final double XE_WORTH_EQ_PCM = 2600.0;  // 满氙价值 pcm（标定用）
    private static final double I_EQ_NOM_CPP = 5.71e15;    // C++ 额定平衡碘浓度 atoms/cm³（rbmk_params 派生：0.0639×2.563e12/2.87e-5）

    // ---- 输入（每 tick 由 Core 采集；pressureMPa/hInHeader 保留字段，不注入 C++）----
    double rodInsertion = 1.0;     // 0=全抽出 1=全插入
    double pressureMPa = 6.9;
    double mdotTotal = 400.0;      // 主泵 + 离线冷却总流量（直接接入堆芯侧）
    double hInHeader = 0.0;
    int fuelChannelCount = 1661;
    double mdotOffline = 400.0;    // 离线部分（诊断口径，已含在 mdotTotal）
    double hOfflineIn = 87.0;

    // ---- 输出 ----
    double totalPowerMW;           // 含衰减热
    double fissionPowerMW;
    double decayHeatMW;
    double reactivityAbs;          // dk/k
    double neutronTotal;           // 中子数（UI 口径）
    double periodSeconds = 1e9;
    double xenonWorthPcm;          // 诊断
    double rodWorthPcm;            // 诊断
    double i135AvgForUI, xe135AvgForUI;
    final double[] nodeVoid = new double[N];

    private transient com.rbmk.real.RbmkCore cc;
    private static volatile boolean attachFailed = false;

    ReactorKinetics() {
        attach();
    }

    private void attach() {
        try {
            cc = new com.rbmk.real.RbmkCore();
            cc.setRegulatorEnabled(false);   // AR 棒控由 AutoControl 驱动真实棒位，关闭 C++ 内部 ρ 注入调节器
            cc.resetToShutdown();            // 停堆初值：全插棒 + 源量程通量 + 燃料压到冷却剂温度 + 氙碘归零
                                             // tShutdown=-1e7 → 衰减热趋近 0 → 开局功率 0 MW（C++ 显示口径）
            cc.refresh();
            pushInputs();
            recomputeOutputs();
        } catch (Throwable t) {
            cc = null;
            attachFailed = true;
            System.err.println("[ReactorKinetics] C++ 物理核心加载失败： " + t);
            t.printStackTrace();
            // 一次性弹窗告知（EDT），避免「点击开始后没有窗口/黑屏无解释」
            java.awt.EventQueue.invokeLater(() -> {
                javax.swing.JOptionPane.showMessageDialog(null,
                        Localization.t("app.error.coreFailed") + "\n" + t
                                + Localization.t("app.error.coreSafeMode"),
                        Localization.t("app.error.coreTitle"), javax.swing.JOptionPane.ERROR_MESSAGE);
            });
        }
    }

    private void pushInputs() {
        if (cc == null) {
            return;
        }
        cc.setRodPosition(1.0 - rodInsertion);   // 语义转换：Java 全插=1 → C++ 全插=0
        cc.setFlowFraction(Math.min(2.0, Math.max(0.0, mdotTotal / MDOT_NOM)));
        // 离线冷却河水流量（20°C 冷源，经换热器冷却一回路）——与主流量独立，不并入
        cc.setOfflineFlow(Math.max(0.0, mdotOffline));
        // 压力 → 饱和温度；入口温度 → 入口焓（相对 h(270°C)=0）。
        // 旧实现把 284°C / 270°C 写死在 C++ 里，汽包压力变化不会影响空泡。
        double p = Math.max(0.1, Math.min(9.0, pressureMPa));
        double tsat = 284.0;
        if (Loader.tables != null) {
            double t = Loader.tables.getSteamTemperatureByPressure(p);
            if (t > 100.0 && t < 370.0) {
                tsat = t;
            }
        }
        cc.setSaturationTemp(tsat);
        cc.setInletEnthalpy(hInHeader * 1000.0);   // kJ/kg → J/kg
    }

    // ------------------------------------------------------------------
    // 每 tick 推进（Core 帧长 0.05 s；C++ 内部 TR-BDF2 单步隐式）
    // ------------------------------------------------------------------
    void update(double dtTick) {
        if (cc == null) {
            if (attachFailed) {
                return;   // 安全模式：保持停堆零功率，不再反复尝试
            }
            attach();   // 存档反序列化后重建 native 句柄
        }
        pushInputs();
        cc.step(dtTick);
        cc.refresh();
        recomputeOutputs();
    }

    // ------------------------------------------------------------------
    // 存档：完整 native 状态（u/C/I/Xe/温度/棒位/停堆历史）
    // ------------------------------------------------------------------
    /** 导出内核完整状态；没有内核（安全模式）或旧 DLL 不支持时返回 null。 */
    double[] captureNativeState() {
        return cc == null ? null : cc.saveState();
    }

    /** 用存档状态覆盖当前内核；成功返回 true。 */
    boolean restoreNativeState(double[] saved) {
        if (saved == null) {
            return false;
        }
        if (cc == null) {
            if (attachFailed) {
                return false;
            }
            attach();                 // 读档后重建 native 句柄（内部先 resetToShutdown）
        }
        if (cc == null || !cc.restoreState(saved)) {
            return false;
        }
        cc.refresh();
        recomputeOutputs();
        return true;
    }

    /** 内核里实际生效的冷却剂流量 kg/s（比 Java 侧采集值更可靠，读档时用它）。 */
    double actualFlowKgS() {
        if (cc == null) {
            return mdotTotal;
        }
        cc.refresh();
        return cc.flowKgS();
    }

    /** 内核里实际生效的 Java 语义棒位（0=全抽出，1=全插入）。 */
    double actualRodInsertion() {
        if (cc == null) {
            return rodInsertion;
        }
        cc.refresh();
        return 1.0 - cc.rodPosition();
    }

    private void recomputeOutputs() {
        if (cc == null) {
            totalPowerMW = 0;
            fissionPowerMW = 0;
            decayHeatMW = 0;
            reactivityAbs = -0.065;   // 深度停堆（安全模式显示口径）
            neutronTotal = 0;
            periodSeconds = 1e9;
            return;
        }
        totalPowerMW = cc.powerMW();
        decayHeatMW = cc.decayFraction() * 4800.0;
        fissionPowerMW = Math.max(0.0, totalPowerMW - decayHeatMW);
        // 开局停堆残余（tShutdown=-1e7 的衰变热尾数 <0.5MW）显示为 0 MW——
        // 用户口径：刚开机时功率表必须为 0
        if (totalPowerMW < 0.5) {
            totalPowerMW = 0;
            fissionPowerMW = 0;
            decayHeatMW = 0;
        }
        reactivityAbs = cc.reactivity();
        double nSum = 0;
        for (int j = 0; j < N; j++) {
            nodeVoid[j] = cc.nodeVoid(j);
            nSum += cc.nodeN(j);
        }
        neutronTotal = nSum * (N_NOMINAL / N);
        double p = cc.period();
        periodSeconds = (p < 0 || p > 1e9) ? 1e9 : p;   // 次临界的负 prompt 周期无物理意义 → ∞
        xenonWorthPcm = cc.avgXenon();
        rodWorthPcm = cc.avgRod();
        // C++ 氙碘是绝对浓度（atoms/cm³，额定平衡 I_EQ_NOM=5.71e15）：映射到通道表格的
        // 「满功率=满刻度原子数」口径（I135% / Xe135%）
        i135AvgForUI = cc.avgI() / I_EQ_NOM_CPP * I_FULL;
        xe135AvgForUI = Math.abs(cc.avgXenon()) / XE_WORTH_EQ_PCM * XE_FULL;
    }

    /** 物理核心是否处于加载失败的安全模式 */
    boolean isSafeMode() {
        return cc == null;
    }
}
