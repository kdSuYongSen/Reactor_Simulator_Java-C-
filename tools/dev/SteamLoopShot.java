package com.darwish.nppsim;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/** 蒸汽环路窗口截图（确认新增的泄压阀/水汽总量行正常显示）。 */
public class SteamLoopShot {
    public static void main(String[] a) throws Exception {
        Localization.init();
        Localization.setLanguage(Language.ZH_CN);
        UiStyle.applyLaf();
        Fonts.init();
        UiTheme.install();
        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();
        new NPPSim();
        Thread.sleep(800);
        SteamLoopUI w = new SteamLoopUI();
        SwingUtilities.invokeAndWait(() -> {
            w.setSize(1100, 760);
            w.setVisible(true);
            w.validate();
            w.update();
        });
        Thread.sleep(1200);
        BufferedImage img = new BufferedImage(w.getWidth(), w.getHeight(), BufferedImage.TYPE_INT_RGB);
        SwingUtilities.invokeAndWait(() -> {
            Graphics2D g = img.createGraphics();
            w.paintAll(g);
            g.dispose();
        });
        File out = new File(a.length > 0 ? a[0] : "target/shots/steam_loop.png");
        out.getParentFile().mkdirs();
        ImageIO.write(img, "png", out);
        System.out.println("saved " + out.getAbsolutePath() + " " + w.getWidth() + "x" + w.getHeight());
        System.exit(0);
    }
}
