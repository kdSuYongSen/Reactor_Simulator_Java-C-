import com.darwish.nppsim.layeditor.Editor;
import com.darwish.nppsim.layeditor.Manifest;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/** 离屏渲染布局编辑器窗口（确认 jar 里那套 UI 能正常长出来）。 */
public class EditorShot {
    public static void main(String[] a) throws Exception {
        javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        Manifest m = Manifest.load(new File(a.length > 0 ? a[0] : "target"));
        if (m == null) {
            System.out.println("清单读取失败");
            System.exit(1);
        }
        final Editor[] holder = new Editor[1];
        SwingUtilities.invokeAndWait(() -> {
            holder[0] = new Editor(m);
            holder[0].setSize(1180, 760);
            holder[0].setVisible(true);
            holder[0].validate();
        });
        Thread.sleep(1200);
        final Editor e = holder[0];
        SwingUtilities.invokeAndWait(() -> {
            Component page = e.getContentPane();
            BufferedImage img = new BufferedImage(Math.max(1, page.getWidth()),
                    Math.max(1, page.getHeight()), BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = img.createGraphics();
            page.paint(g);
            g.dispose();
            try {
                ImageIO.write(img, "png", new File("target/verify/editor.png"));
                System.out.println("saved editor.png " + img.getWidth() + "x" + img.getHeight());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        System.out.println("EDITOR_SHOT_OK");
        System.exit(0);
    }
}
