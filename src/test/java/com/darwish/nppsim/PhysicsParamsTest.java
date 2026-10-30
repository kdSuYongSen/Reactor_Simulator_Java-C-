package com.darwish.nppsim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.rbmk.real.RbmkCore;
import org.junit.Assume;
import org.junit.Test;

/**
 * 堆芯物理参数的回归约束（对应 RBMK 公开数据量级）：
 *   - 额定稳态：4800 MW、ρ≈0、通道功率 2.89 MW；
 *   - Doppler：-1.1 pcm/K（sqrt 形式 -66.84 pcm/sqrt(K)）；
 *   - 石墨温度系数：正，+4.0 pcm/K（NRC NUREG-1250 表 2.4：+3.2~+6.0e-5/K）；
 *   - 空泡：按**体积空泡份额**计 +20.0 pcm/%，0→100% 共 +2000 pcm
 *     （INSAG-7：工作点 +2.0e-4 per % void；总价值与 +4~5β @ β_eff=480 一致）；
 *   - 快功率系数：+1~+3 pcm/%（INSAG-7：+0.6e-4 β/MW ≈ +0.039 pcm/MW ≈ +1.9 pcm/% @4800MW）。
 */
public class PhysicsParamsTest {

    private static final double CP = 5500.0, HFG = 1.509e6, TSAT = 284.0, HSAT = 5500.0 * 14.0;

    private static double h(double T) {
        return T <= TSAT ? CP * (T - 270.0) : HSAT + (T - TSAT) / 40.0 * HFG;
    }

    private static double tinv(double hv) {
        return hv <= HSAT ? 270.0 + hv / CP : TSAT + (hv - HSAT) * 40.0 / HFG;
    }

    @Test
    public void nominalCoreAndCoefficientsArePhysical() {
        RbmkCore core = new RbmkCore();
        try {
            Assume.assumeTrue("当前 DLL 不支持状态接口", core.supportsStateRestore());
            core.reset();
            for (int i = 0; i < 200; i++) {     // 10 s
                core.step(0.05);
            }
            core.refresh();

            assertEquals("额定功率", 4800.0, core.powerMW(), 50.0);
            assertEquals("额定反应性", 0.0, core.reactivityPcm(), 50.0);

            // probeDopplerCoefficient 返回 pcm per sqrt(K)；额定点斜率 -1.1 pcm/K
            assertEquals("Doppler 系数", -66.84, core.probeDopplerCoeff(), 1.0);
            assertEquals("石墨温度系数", 4.0, core.probeModeratorCoeff(), 0.2);
            assertEquals("空泡系数（per 1% 体积空泡）", 20.0, core.probeVoidCoeffPcm(), 0.5);
            assertEquals("满空泡价值", 2000.0, core.probeFullVoidPcm(), 1.0);

            // 空泡份额必须是真的体积份额（额定工况约 0.35~0.55），不是干度（约 0.09）
            double vMean = 0.0;
            for (int j = 0; j < RbmkCore.NODES; j++) {
                vMean += core.nodeVoid(j);
            }
            vMean /= RbmkCore.NODES;
            assertTrue("额定堆芯平均空泡份额应在 0.30~0.60，实测 " + vMean,
                    vMean > 0.30 && vMean < 0.60);

            // 快功率系数：按 +1% 功率对应的 Tf/Tc/Tcool 扰动状态，测 dρ/dP
            double[] s0 = core.saveState();
            double rho0 = core.reactivityPcm();
            double[] s = s0.clone();
            for (int j = 0; j < 8; j++) {
                s[j] *= 1.01;                                            // 中子密度
                s[8 + j] += 6.0e6 * (1 / 5.22e6 + 1 / 2.40e6);           // 燃料温度 +3.65 K
                s[16 + j] += 6.0e6 / 5.22e6;                             // 包壳
                s[24 + j] = tinv(h(s[24 + j]) + (j + 0.5) * 50e3 * 0.01); // 冷却剂焓升
            }
            assertTrue("状态恢复失败", core.restoreState(s));
            core.refresh();
            double powerCoeff = core.reactivityPcm() - rho0;             // pcm per 1% power
            assertTrue("快功率系数应略正（INSAG-7 +0.6e-4 β/MW ≈ +1.9 pcm/%），实测 "
                    + powerCoeff, powerCoeff > 0.0 && powerCoeff < 4.0);
            assertTrue("状态回滚失败", core.restoreState(s0));
            core.refresh();

            assertTrue("石墨温度系数必须为正", core.probeModeratorCoeff() > 0.0);

            // 压力 → 饱和温度反馈：同一冷却剂温度下提高饱和温度会把空泡压掉，
            // 必须产生负反应性（旧实现把 284°C 写死，压力完全不影响堆芯）。
            double rhoAt284 = core.reactivityPcm();
            core.setSaturationTemp(290.0);
            core.refresh();
            assertTrue("提高饱和温度应引入负反应性",
                    core.reactivityPcm() < rhoAt284 - 50.0);
            core.setSaturationTemp(284.0);
            core.refresh();

            // 停堆裕度必须真正进入中子方程（不能只用于显示）
            core.resetToShutdown();
            core.refresh();
            assertEquals("停堆态反应性", -7500.0, core.reactivityPcm(), 50.0);
            assertEquals("停堆态功率", 0.0, core.powerMW(), 1.0);
        } finally {
            core.close();
        }
    }
}
