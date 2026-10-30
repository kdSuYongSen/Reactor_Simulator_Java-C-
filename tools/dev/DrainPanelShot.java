package com.darwish.nppsim;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/** 凝结水 / 给水窗口截图（确认新增的排水阀面板没有把布局挤坏）。 */
public class DrainPanelShot {
    public static void main(String[] a) throws Exception {
        Localization.init();
        Localization.setLanguage(Language.ZH_CN);
        UiStyle.applyLaf();
        Fonts.init();
        UiTheme.install();
        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();
        new NPPSim();
        Thread.sleep(900);

        // 打开排水阀，截图里报警灯应是红的
        NPPSim.condensateDrainValve.setState(2);
        NPPSim.feedwaterDrainValve.setState(2);
        Thread.sleep(2500);

        shot("CondensateUI", "target/shots/condensate_drain.png", 1400, 1000);
        shot("FeedwaterUI", "target/shots/feedwater_drain.png", 1400, 1000);
        System.out.println("DONE");
        System.exit(0);
    }

    static void shot(String cls, String path, int w, int h) throws Exception {
        JFrame f = (JFrame) Class.forName("com.darwish.nppsim." + cls).getDeclaredConstructor().newInstance();
        SwingUtilities.invokeAndWait(() -> {
            f.setSize(w, h);
            f.setVisible(true);
            f.validate();
        });
        Thread.sleep(1500);
        SwingUtilities.invokeAndWait(f::validate);
        Thread.sleep(400);
        BufferedImage img = new BufferedImage(f.getWidth(), f.getHeight(), BufferedImage.TYPE_INT_RGB);
        SwingUtilities.invokeAndWait(() -> {
            Graphics2D g = img.createGraphics();
            f.paintAll(g);
            g.dispose();
        });
        File out = new File(path);
        out.getParentFile().mkdirs();
        ImageIO.write(img, "png", out);
        System.out.println("saved " + out.getAbsolutePath() + " " + f.getWidth() + "x" + f.getHeight());
        SwingUtilities.invokeAndWait(f::dispose);
    }
}
