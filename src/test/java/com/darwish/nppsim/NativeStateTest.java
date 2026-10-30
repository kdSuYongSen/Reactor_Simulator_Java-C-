package com.darwish.nppsim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.rbmk.real.RbmkCore;
import org.junit.Assume;
import org.junit.Test;

/**
 * native 内核状态保存/恢复的往返测试。
 *
 * <p>验证 {@code saveState()} 导出的 125 维状态能在另一个内核实例里完整恢复：
 * 恢复后的功率/氙/燃料温度/棒位必须与源内核一致，并且继续推进相同步数后
 * 仍然保持一致（说明先驱核、碘氙、温度、时间等状态都恢复了）。
 *
 * <p>旧版 DLL（没有 nativeStateSize 导出）会被 {@link Assume} 跳过，不判失败。
 */
public class NativeStateTest {

    @Test
    public void stateRoundTripContinuesFromSamePoint() {
        RbmkCore source = new RbmkCore();
        RbmkCore restored = new RbmkCore();
        try {
            Assume.assumeTrue("当前 DLL 不支持状态导出", source.supportsStateRestore());

            source.reset();
            for (int i = 0; i < 60; i++) {
                source.step(0.05);          // 3 s，离开初始稳态
            }
            source.refresh();               // getters 读取的是最近一次快照

            double power = source.powerMW();
            double xenon = source.avgXenon();
            double fuelT = source.nodeTf(3);
            double rod = source.rodPosition();

            double[] snapshot = source.saveState();
            assertNotNull("saveState() 不应返回 null", snapshot);
            assertEquals(RbmkCore.STATE_SIZE, snapshot.length);

            assertTrue("状态恢复失败", restored.restoreState(snapshot));
            assertEquals(power, restored.powerMW(), 1e-9);
            assertEquals(xenon, restored.avgXenon(), 1e-9);
            assertEquals(fuelT, restored.nodeTf(3), 1e-9);
            assertEquals(rod, restored.rodPosition(), 1e-9);
            assertEquals(source.time(), restored.time(), 1e-9);

            // 两个内核从同一状态继续推进，结果必须一致
            for (int i = 0; i < 20; i++) {
                source.step(0.05);
                restored.step(0.05);
            }
            source.refresh();
            restored.refresh();
            assertEquals(source.powerMW(), restored.powerMW(), 1e-6);
            assertEquals(source.avgXenon(), restored.avgXenon(), 1e-6);
            assertEquals(source.nodeTf(3), restored.nodeTf(3), 1e-6);
        } finally {
            source.close();
            restored.close();
        }
    }
}
