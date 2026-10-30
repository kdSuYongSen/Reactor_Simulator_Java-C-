package com.darwish.nppsim;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/** 打开凝结水窗口截图，看 TG-A / TG-B 两个区块是否都在。 */
public class ShotCondensate {
    public static void main(String[] a) throws Exception {
        Localization.init();
        Localization.setLanguage(Language.ZH_CN);
        UiStyle.applyLaf();
        Fonts.init();
        UiTheme.install();
        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();
        new NPPSim();
        javax.swing.JFrame w = (javax.swing.JFrame) Class.forName("com.darwish.nppsim." + (a.length > 1 ? a[1] : "CondensateUI")).getDeclaredConstructor().newInstance();
        SwingUtilities.invokeAndWait(() -> {
            w.setSize(1366, 768);
            w.setVisible(true);
            w.validate();
        });
        Thread.sleep(1500);
        BufferedImage img = new BufferedImage(w.getWidth(), w.getHeight(), BufferedImage.TYPE_INT_RGB);
        SwingUtilities.invokeAndWait(() -> {
            java.awt.Graphics2D g = img.createGraphics();
            w.paintAll(g);
            g.dispose();
        });
        File out = new File(a.length > 0 ? a[0] : "target/shots/cond_now.png");
        out.getParentFile().mkdirs();
        ImageIO.write(img, "png", out);
        System.out.println("saved " + out.getAbsolutePath() + "  size=" + w.getWidth() + "x" + w.getHeight());
        System.exit(0);
    }
}
