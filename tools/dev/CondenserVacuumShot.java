package com.darwish.nppsim;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/**
 * 凝结水页面截图：看凝汽器真空从"常压"抽到"工作真空"的过程。
 * 输出 target/shots/cond_vacuum_t2.png / _t25.png / _t60.png，同时打印读数。
 */
public class CondenserVacuumShot {
    public static void main(String[] a) throws Exception {
        Localization.init();
        Localization.setLanguage(Language.ZH_CN);
        UiStyle.applyLaf();
        Fonts.init();
        UiTheme.install();
        Loader.tables = new SteamTables();
        Loader.soundProvider = new SoundProvider();
        new NPPSim();
        CondensateUI w = new CondensateUI();
        SwingUtilities.invokeAndWait(() -> {
            w.setSize(1366, 768);
            w.setVisible(true);
            w.validate();
        });
        shoot(w, "target/shots/cond_vacuum_t2.png", 2000);
        shoot(w, "target/shots/cond_vacuum_t25.png", 23000);
        shoot(w, "target/shots/cond_vacuum_t60.png", 35000);
        System.exit(0);
    }

    static void shoot(CondensateUI w, String path, long waitMs) throws Exception {
        Thread.sleep(waitMs);
        System.out.printf("t≈%.0fs  TG-1 真空 = %.2f kPa（真空度 %.1f%%）  TG-2 = %.2f kPa | 空气 %.0f kg%n",
                waitMs / 1000.0,
                NPPSim.tg1.condenser.getPressure() * 1000,
                (1 - NPPSim.tg1.condenser.getPressure() / 0.10142) * 100,
                NPPSim.tg2.condenser.getPressure() * 1000,
                NPPSim.tg1.condenser.getGasMassKg());
        BufferedImage img = new BufferedImage(w.getWidth(), w.getHeight(), BufferedImage.TYPE_INT_RGB);
        SwingUtilities.invokeAndWait(() -> {
            Graphics2D g = img.createGraphics();
            w.paintAll(g);
            g.dispose();
        });
        File out = new File(path);
        out.getParentFile().mkdirs();
        ImageIO.write(img, "png", out);
        System.out.println("  saved " + out.getAbsolutePath());
    }
}
