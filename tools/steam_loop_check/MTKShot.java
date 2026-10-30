package com.darwish.nppsim;

import java.util.Locale;

/**
 * 中子通量图（MTK）离屏渲染验证：构造真实堆芯，灌入与 {@code OrigMTKShot}（原始 jar 版）
 * 完全相同的"中心高、边缘低"径向通量分布，截图对比移植是否忠实。
 */
public class MTKShot {

    /** 与原始 jar 版 harness 共用的径向形状（复刻 Core.computeRadialShape 的公式） */
    static double shapeAt(int y, int x) {
        final double cx = 27.5, cy = 27.5, rMax = 28.0;
        double dx = y - cy, dy = x - cy;
        double rNorm = Math.min(1.0, Math.sqrt(dx * dx + dy * dy) / rMax);
        return 1.45 - 0.9 * rNorm * rNorm;
    }

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);
        Loader.tables = new SteamTables();
        NPPSim.tables = Loader.tables;
        NPPSim.atmosphere = new Atmosphere(15);

        NPPSim.core = new Core();
        MTK mtk = new MTK();

        // 灌入径向通量分布（与 OrigMTKShot 同一公式、同一实现）
        for (int y = 0; y < NPPSim.core.coreArray.size(); y++) {
            java.util.ArrayList<Channel> row = NPPSim.core.coreArray.get(y);
            for (int x = 0; x < row.size(); x++) {
                Channel c = row.get(x);
                if (c instanceof FuelChannel || c instanceof CPSChannel) {
                    double shape = shapeAt(y, x);
                    c.setNeutronPopulation(shape, shape);
                }
            }
        }
        mtk.update();

        javax.swing.JFrame f = new javax.swing.JFrame("MTK");
        f.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        f.add(mtk);
        f.setSize(460, 500);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
        System.out.println("MTK_SHOT_READY " + f.getSize());
        Thread.sleep(25000);
        System.out.println("MTK_SHOT_DONE");
        System.exit(0);
    }
}
