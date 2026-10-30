package com.darwish.nppsim;

import java.util.Locale;

/**
 * 电功率提升方案扫描（2026-10-19）。
 *
 * 纯热力学核算，不跑全厂瞬态：给定二次侧吸热量 Q、主蒸汽压力 p_s、
 * 凝汽器压力 p_c、给水加热级数与抽汽压力，按质量/能量平衡算电功率。
 * 结果用于排序"哪个杠杆收益最大"，再用 SecondaryV2Check 验证最终选中的方案。
 *
 * 循环口径（与 TG 的熵法一致）：
 *   m_s      = Q / (h_g(p_s) − h_f(T_fw))              给水→饱和汽
 *   m_e,i    = 给水在第 i 级加热器吸收的热量 / 该级 h_fg   抽汽量
 *   P_elec   = [ (m_s·(1−carry) − Σm_e − m_nl)·w(p_s→p_c)
 *                + Σ m_e,i·w(p_s→p_e,i) ] · η_gen
 */
public class PowerOptions {

    static double Q = 4822.0;          // 二次侧吸热 MW（= 堆芯 4800 + 主泵功的量级）
    static double T_DRUM = 284.0;      // 一回路饱和温度
    static double T_COND_IN = 20.0;    // 冷却水入口
    static double CARRY = 0.02;        // 分离器机械携带水率
    static double GEN_EFF = 0.98;
    static double ISO_EFF = 0.88;
    static double NO_LOAD = 40.0;      // 两台机组合计空载汽耗 kg/s
    static double P_EXTRACT = 0.60;    // 除氧器抽汽压力 MPa
    static double COND_APPROACH = 3.0; // 凝汽器端差 K

    static double hf(double t) { return Loader.tables.getWaterEnthalpyByTemperature(t); }
    static double hg(double t) { return Loader.tables.getSteamEnthalpyByTemperature(t); }
    static double tsat(double p) { return Loader.tables.getSteamTemperatureByPressure(p); }

    /** 一次抽汽（单级给水加热）：返回 {m_s, m_e, P_MW, w_full, w_ext, T_fw} */
    static double[] cycle(double pS, double pC, double pExt, double tFwTarget) {
        double tS = tsat(pS);
        double hgS = hg(tS);
        double hfFw = hf(tFwTarget);
        double mS = Q * 1000.0 / Math.max(1.0, hgS - hfFw);     // kg/s

        double tCond = tsat(pC);
        double tExt = tsat(pExt);
        double hfgExt = Math.max(1.0, hg(tExt) - hf(tExt));
        double mE = mS * Math.max(0.0, hf(tFwTarget) - hf(tCond)) / hfgExt;

        double wFull = TG.specificWorkKjPerKg(tS, pC);
        double wExt = TG.specificWorkKjPerKg(tS, pExt);
        double mTurb = Math.max(0.0, mS * (1 - CARRY) - mE - NO_LOAD);
        double pMW = (mTurb * wFull + mE * wExt) * GEN_EFF / 1000.0;
        return new double[]{mS, mE, pMW, wFull, wExt, tFwTarget};
    }

    /** 两级给水加热：低压加热器 pLP 把给水加热到 Tsat(pLP)−5，再进除氧器 */
    static double[] cycle2(double pS, double pC, double pLP, double pExt, double tFwTarget) {
        double tS = tsat(pS);
        double hgS = hg(tS);
        double mS = Q * 1000.0 / Math.max(1.0, hgS - hf(tFwTarget));
        double tCond = tsat(pC);

        double tLP = tsat(pLP) - 5.0;                     // 低压加热器出口给水温度（端差 5 K）
        double hfgLP = Math.max(1.0, hg(tsat(pLP)) - hf(tsat(pLP)));
        double mLP = mS * Math.max(0.0, hf(tLP) - hf(tCond)) / hfgLP;

        double tExt = tsat(pExt);
        double hfgExt = Math.max(1.0, hg(tExt) - hf(tExt));
        double mExt = mS * Math.max(0.0, hf(tFwTarget) - hf(tLP)) / hfgExt;

        double wFull = TG.specificWorkKjPerKg(tS, pC);
        double wLP = TG.specificWorkKjPerKg(tS, pLP);
        double wExt = TG.specificWorkKjPerKg(tS, pExt);
        double mTurb = Math.max(0.0, mS * (1 - CARRY) - mLP - mExt - NO_LOAD);
        double pMW = (mTurb * wFull + mLP * wLP + mExt * wExt) * GEN_EFF / 1000.0;
        return new double[]{pMW, mS, mLP, mExt, wFull, wLP, wExt};
    }

    static void row(String tag, double pS, double pC, double[] r) {
        System.out.printf("%-26s p_s=%.2f p_c=%.4f | 产汽 %6.0f 抽汽 %5.0f (%4.1f%%) "
                        + "| w %4.0f/%4.0f | 电 %7.1f MW  η=%4.1f%%%n",
                tag, pS, pC, r[0], r[1], 100 * r[1] / Math.max(1, r[0]),
                r[3], r[4], r[2], 100 * r[2] / (Q));
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        NPPSim.tables = new SteamTables();
        Loader.tables = NPPSim.tables;

        System.out.println("=== 基准（当前构建：4.0 MPa / 6.1 kPa / 抽汽 0.60 MPa / 单级加热）===");
        double[] base = cycle(4.00, 0.0061, 0.60, 153.0);
        row("BASE", 4.00, 0.0061, base);

        System.out.println("\n=== A. 主蒸汽压力（阀门口径按需放大，换热器 UA 需重算）===");
        for (double p : new double[]{4.0, 4.5, 5.0, 5.5, 6.0, 6.5}) {
            row(String.format("p_s=%.1f MPa", p), p, 0.0061, cycle(p, 0.0061, 0.60, 153.0));
        }

        System.out.println("\n=== B. 凝汽器真空（加大循环水量 / 降低冷却水温）===");
        for (double p : new double[]{0.0081, 0.0061, 0.0050, 0.0042}) {
            double tc = tsat(p);
            row(String.format("p_c=%.2f kPa (%.0f°C)", p * 1000, tc), 4.00, p, cycle(4.00, p, 0.60, 153.0));
        }

        System.out.println("\n=== C. 每提高 1 K 真空的边际收益（p_s=4.0）===");
        for (double dT : new double[]{0, 3, 6, 9}) {
            double tc = tsat(0.0061) - dT;
            double p = Loader.tables.getSteamPressureByTemp(tc);
            double[] r = cycle(4.00, p, 0.60, 153.0);
            System.out.printf("  凝结温度 %5.1f°C → p=%.4f MPa  电 %7.1f MW  (Δ %+6.1f)%n",
                    tc, p, r[2], r[2] - base[2]);
        }

        System.out.println("\n=== D. 加一级低压加热器（抽汽压力扫描，p_s=4.0）===");
        for (double pLP : new double[]{0.10, 0.15, 0.20, 0.30, 0.40}) {
            double[] r = cycle2(4.00, 0.0061, pLP, 0.60, 153.0);
            System.out.printf("  LP=%.2f MPa (Tsat %.0f°C) 电 %7.1f MW (Δ %+6.1f) | "
                            + "产汽 %5.0f 抽汽LP %4.0f + 除氧 %4.0f | w %4.0f/%4.0f/%4.0f%n",
                    pLP, tsat(pLP), r[0], r[0] - base[2], r[1], r[2], r[3], r[4], r[5], r[6]);
        }

        System.out.println("\n=== E. 两级加热 + 提到 5.0 MPa ===");
        for (double[] pr : new double[][]{{4.0, 6.1}, {5.0, 6.1}, {5.0, 4.2}, {6.0, 4.2}}) {
            double[] r = cycle2(pr[0], pr[1] / 1000.0, 0.20, 0.60, 153.0);
            System.out.printf("  p_s=%.1f p_c=%.1f kPa 电 %7.1f MW (Δ %+6.1f)  η=%4.1f%%%n",
                    pr[0], pr[1], r[0], r[0] - base[2], 100 * r[0] / Q);
        }

        System.out.println("\n=== F. 分离器携带水率（4.0 MPa）===");
        double oldCarry = CARRY;
        for (double c : new double[]{0.02, 0.015, 0.01, 0.0}) {
            CARRY = c;
            double[] r = cycle(4.00, 0.0061, 0.60, 153.0);
            System.out.printf("  carry=%.1f%% 电 %7.1f MW (Δ %+6.1f)%n", c * 100, r[2], r[2] - base[2]);
        }
        CARRY = oldCarry;

        System.out.println("\n=== G. 汽轮机等熵效率（4.0 MPa）===");
        for (double e : new double[]{0.85, 0.88, 0.90, 0.92}) {
            ISO_EFF = e;
            double[] r = cycle(4.00, 0.0061, 0.60, 153.0);
            System.out.printf("  η_s=%.2f 电 %7.1f MW (Δ %+6.1f)%n", e, r[2], r[2] - base[2]);
        }
        ISO_EFF = 0.88;

        System.out.println("\n=== H. 堆芯热功率上提（4.0 MPa，效率不变）===");
        double q0 = Q;
        for (double q : new double[]{4822, 5000, 5100, 5300}) {
            Q = q;
            double[] r = cycle(4.00, 0.0061, 0.60, 153.0);
            System.out.printf("  Q=%.0f MW 电 %7.1f MW (Δ %+6.1f, 相当于堆芯 %.0f MWt)%n",
                    q, r[2], r[2] - base[2], (q - 22) * 1.0);
        }
        Q = q0;

        System.out.println("\n=== I. 组合：5.0 MPa + 真空 4.2 kPa + 一级 LP 加热 ===");
        double[] combo = cycle2(5.00, 0.0042, 0.20, 0.60, 153.0);
        System.out.printf("  电 %7.1f MW  η=%4.1f%%  (基准 %.1f MW)%n", combo[0], 100 * combo[0] / Q, base[2]);
    }
}
