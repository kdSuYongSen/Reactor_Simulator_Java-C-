package com.darwish.nppsim;

import java.awt.image.BufferedImage;
import java.util.Locale;

/** 离屏打印 MTK，直接检查格子是否被绘制（绕过操作系统窗口） */
public class MTKPaintProbe {
    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);
        Loader.tables = new SteamTables();
        NPPSim.tables = Loader.tables;
        NPPSim.atmosphere = new Atmosphere(15);

        NPPSim.core = new Core();
        MTK mtk = new MTK();
        System.out.println("panel size = " + mtk.getSize() + " pref = " + mtk.getPreferredSize());
        System.out.println("children = " + mtk.getComponentCount());
        for (int y = 0; y < NPPSim.core.coreArray.size(); y++) {
            java.util.ArrayList<Channel> row = NPPSim.core.coreArray.get(y);
            for (int x = 0; x < row.size(); x++) {
                Channel c = row.get(x);
                if (c instanceof FuelChannel || c instanceof CPSChannel) {
                    double shape = MTKShot.shapeAt(y, x);
                    c.setNeutronPopulation(shape, shape);
                }
            }
        }
        mtk.setSize(460, 500);
        mtk.doLayout();
        mtk.update();
        BufferedImage img = new BufferedImage(460, 500, BufferedImage.TYPE_INT_RGB);
        mtk.paint(img.getGraphics());
        int black = 0, olive = 0, yellow = 0, white = 0, green = 0, other = 0;
        for (int yy = 0; yy < 500; yy += 2) {
            for (int xx = 0; xx < 460; xx += 2) {
                int rgb = img.getRGB(xx, yy);
                int r = (rgb >> 16) & 255, g = (rgb >> 8) & 255, b = rgb & 255;
                if (r == 0 && g == 0 && b == 0) black++;
                else if (r == 118 && g == 91 && b == 28) olive++;
                else if (r == 235 && g == 235 && b == 160) yellow++;
                else if (r == 255 && g == 255 && b == 255) white++;
                else if (g > 100 && r < 60 && b < 60) green++;
                else other++;
            }
        }
        System.out.printf("black=%d olive=%d yellow=%d white=%d green=%d other=%d%n",
                black, olive, yellow, white, green, other);
        javax.imageio.ImageIO.write(img, "PNG",
                new java.io.File("D:/RBMK/RBMK1500/SourceCode/target/shots/mtk_probe.png"));
        System.out.println("PROBE_DONE");
    }
}
