package com.darwish.nppsim;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 功率保护阶梯的回归约束：
 *   - 所有功率 AZ 定值都必须高于额定热功率（4800 MW），否则正常升功率就会跳堆；
 *   - AZ-3 &lt; AZ-4 &lt; AZ-6 &lt; AZ-1 必须严格递增，否则高定值会被低定值遮蔽而永远不动作；
 *   - AZ-5 是**手动停堆**（没有自动功率定值），所以阶梯里不包含它。
 *
 * 该测试只读取静态常量，不启动仿真、不加载 native 库。
 */
public class AzProtectionTest {

    private static final double RATED_THERMAL_MW = 4800.0;

    @Test
    public void powerProtectionLadderIsAboveRatedAndStrictlyIncreasing() {
        assertTrue("AZ-3 必须高于额定功率",
                AutoControl.AZ3_POWER_LIMIT_MW > RATED_THERMAL_MW);
        assertTrue("AZ-4 必须高于 AZ-3",
                AutoControl.AZ4_POWER_LIMIT_MW > AutoControl.AZ3_POWER_LIMIT_MW);
        assertTrue("AZ-6 必须高于 AZ-4",
                AutoControl.AZ6_POWER_LIMIT_MW > AutoControl.AZ4_POWER_LIMIT_MW);
        assertTrue("AZ-6 必须高于额定功率",
                AutoControl.AZ6_POWER_LIMIT_MW > RATED_THERMAL_MW);
        assertTrue("AZ-1 高功率必须高于 AZ-6",
                AutoControl.AZ1_POWER_LIMIT_MW > AutoControl.AZ6_POWER_LIMIT_MW);
    }

    @Test
    public void az3SetpointStaysInRequestedBand() {
        // 需求区间 5100–5300 MW：既能避开额定 4800 MW，又不至于贴近 AZ-4。
        assertTrue("AZ-3 定值低于 5100 MW",
                AutoControl.AZ3_POWER_LIMIT_MW >= 5100.0);
        assertTrue("AZ-3 定值高于 5300 MW",
                AutoControl.AZ3_POWER_LIMIT_MW <= 5300.0);
    }
}
