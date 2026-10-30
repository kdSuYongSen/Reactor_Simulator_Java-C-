package com.darwish.nppsim;

/**
 * 控制棒移动速度档位（全局）。
 *
 * <p>只影响"正常驱动"的棒速（手动选择后插入/拔出、程序控制），
 * **不影响紧急停堆**（AZ 跳堆仍走 {@code ControlRodChannel.scramSpeed}）。</p>
 *
 * <p>档位用"全行程时间"标定：0 档是原版速度 17.5 s（0.002857/tick × 350 tick），
 * 最高档 4.0 s —— 这就是"AZ 棒以最大速度约 4 秒完全拔出"的定标。</p>
 */
final class RodSpeed {

    /** 全行程时间（秒），从慢到快；下标即档位 */
    static final double[] FULL_TRAVEL_S = {17.5, 12.0, 8.0, 6.0, 4.0};

    private static int level = 0;      // 默认最低档，与改动前的手感一致

    private RodSpeed() {
    }

    static int level() {
        return level;
    }

    static int levels() {
        return FULL_TRAVEL_S.length;
    }

    /** 降一档（更慢）；返回是否真的变了 */
    static boolean slower() {
        if (level <= 0) {
            return false;
        }
        level--;
        return true;
    }

    /** 升一档（更快）；返回是否真的变了 */
    static boolean faster() {
        if (level >= FULL_TRAVEL_S.length - 1) {
            return false;
        }
        level++;
        return true;
    }

    static double fullTravelSeconds() {
        return FULL_TRAVEL_S[level];
    }

    /** 每 tick（50 ms）的行程，喂给 {@link NPPMath#updatePositionFromState} */
    static float stepPerTick() {
        return (float) (0.05 / FULL_TRAVEL_S[level]);
    }

    /** 显示用："4.0 s" */
    static String label() {
        return String.format("%.1f s", fullTravelSeconds());
    }
}
