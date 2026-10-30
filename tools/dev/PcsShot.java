package com.darwish.nppsim;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/** PCS 窗口截图（确认新增的通用净化装置面板与排水阀报警灯没把布局挤坏）。 */
public class PcsShot {
    public static void main(String[] a) throws Exception {
        Localization.init();
        Localization.setLanguage(Language.ZH_CN);
        UiStyle.applyLaf();
        Fonts.init();
        UiTheme.install();
        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();
        new NPPSim();
        Thread.sleep(700);

        // 先把排水阀打开，截图里应能看到红色"排水阀开启"
        NPPSim.pcs.dwTankDrainValve.setState(2);
        Thread.sleep(2500);

        PCSUI w = new PCSUI();
        SwingUtilities.invokeAndWait(() -> {
            w.setSize(1500, 1000);
            w.setVisible(true);
            w.validate();
        });
        Thread.sleep(1500);
        SwingUtilities.invokeAndWait(() -> {
            w.validate();
            try {   // 滚到底部，让新增的通用净化装置面板完整可见
                java.lang.reflect.Field f = PCSUI.class.getDeclaredField("jScrollPane1");
                f.setAccessible(true);
                javax.swing.JScrollPane sp = (javax.swing.JScrollPane) f.get(w);
                sp.getVerticalScrollBar().setValue(sp.getVerticalScrollBar().getMaximum());
            } catch (Exception ignore) {
            }
        });
        Thread.sleep(500);
        BufferedImage img = new BufferedImage(w.getWidth(), w.getHeight(), BufferedImage.TYPE_INT_RGB);
        SwingUtilities.invokeAndWait(() -> {
            Graphics2D g = img.createGraphics();
            w.paintAll(g);
            g.dispose();
        });
        File out = new File(a.length > 0 ? a[0] : "target/shots/pcs.png");
        out.getParentFile().mkdirs();
        ImageIO.write(img, "png", out);
        System.out.println("saved " + out.getAbsolutePath() + " " + w.getWidth() + "x" + w.getHeight());
        System.exit(0);
    }
}
