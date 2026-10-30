package com.darwish.nppsim;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/** 截几个真实窗口，肉眼确认背板是否回到原版（橄榄底 + 深灰面板）。 */
public class WindowShot {
    public static void main(String[] a) throws Exception {
        Localization.init();
        Localization.setLanguage(Language.ZH_CN);
        UiStyle.applyLaf();
        Fonts.init();
        UiTheme.install();

        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();
        NPPSim sim = new NPPSim();
        System.out.println("NPPSim ok");

        CondensateUI cond = new CondensateUI();
        shoot(cond, "cond");
        // 定点放大到 150%，锚点取左中位置，看是否以该点为中心
        SwingUtilities.invokeAndWait(() -> { cond.setSize(980, 560); cond.setVisible(true); cond.validate(); });
        Thread.sleep(500);
        UiZoom.setWindowScale(cond, 1.5f, new java.awt.Point(240, 200));
        Thread.sleep(600);
        SwingUtilities.invokeAndWait(() -> {
            BufferedImage img = new BufferedImage(cond.getContentPane().getWidth(),
                    cond.getContentPane().getHeight(), BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = img.createGraphics();
            cond.getContentPane().paint(g);
            g.dispose();
            try {
                ImageIO.write(img, "png", new File("target/verify/win_cond_zoom150.png"));
                System.out.println("saved win_cond_zoom150.png " + img.getWidth() + "x" + img.getHeight());
            } catch (Exception e) { e.printStackTrace(); }
        });
        UiZoom.reset();          // 复位（上一步把会话默认缩放改成了 1.5）
        Thread.sleep(300);
        shoot(new TGUI(), "tg");
        shoot(new SteamLoopUI(), "loop");
        System.out.println("SHOT_OK");
        System.exit(0);
    }

    static void shoot(JFrame f, String tag) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            f.setSize(980, 560);
            f.setVisible(true);
            f.validate();
        });
        Thread.sleep(900);
        SwingUtilities.invokeAndWait(() -> {
            BufferedImage img = new BufferedImage(f.getContentPane().getWidth(),
                    f.getContentPane().getHeight(), BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = img.createGraphics();
            f.getContentPane().paint(g);
            g.dispose();
            try {
                ImageIO.write(img, "png", new File("target/verify/win_" + tag + ".png"));
                System.out.println("saved win_" + tag + ".png " + img.getWidth() + "x" + img.getHeight());
            } catch (Exception e) {
                e.printStackTrace();
            }
            f.dispose();
        });
    }
}
