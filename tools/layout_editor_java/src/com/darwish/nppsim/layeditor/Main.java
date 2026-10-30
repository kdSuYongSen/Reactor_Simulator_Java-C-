package com.darwish.nppsim.layeditor;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * 入口：{@code java -jar RBMK-LayoutEditor.jar [布局文件所在目录]}
 *
 * <p>参数：{@code [布局目录] [--help]}——{@code --help} 启动即打开“操作说明”。</p>
 *
 * <p>不传目录时按"当前目录 → jar 所在目录 → 上级目录"找 {@code ui_layout_pages.json}；
 * 都找不到就弹目录选择框，选到 {@code ui_layout.json} 所在的那个文件夹即可。</p>
 */
public final class Main {

    public static void main(String[] args) {
        // --lang=en|zh：启动语言（也可以用 -Dlayouteditor.lang=en）
        for (String a : args) {
            if (a.toLowerCase().startsWith("--lang=")) {
                String v = a.substring(a.indexOf('=') + 1).trim();
                L.setLang(v.toLowerCase().startsWith("en") ? L.Lang.EN : L.Lang.ZH);
            }
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> {
            Manifest m;
            try {
                m = Manifest.find(args);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, L.t("dlg.readFail") + e, L.t("dlg.error"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (m == null || m.pages.isEmpty()) {
                // 退化：没有清单时，至少能从 ui_layout.json / ui_layout_children.json 拼出主窗口
                try {
                    m = Manifest.fromLayoutFiles(new File(System.getProperty("user.dir", ".")));
                } catch (Exception ignored) {
                    // 读不出来就继续走目录选择
                }
            }
            if (m == null || m.pages.isEmpty()) {
                m = chooseDir(args);
            }
            if (m == null) {
                JOptionPane.showMessageDialog(null, L.t("dlg.noManifestLong"),
                        L.t("dlg.noManifestList"), JOptionPane.WARNING_MESSAGE);
                return;
            }
            Editor ed = new Editor(m);
            ed.setVisible(true);
            // --help / -h：启动后直接打开“操作说明”（也可以随时点界面右上角“提示”或按 F1）
            if (wantsHelp(args)) {
                ed.showHelp();
            }
        });
    }

    private static boolean wantsHelp(String[] args) {
        if (args == null) {
            return false;
        }
        for (String a : args) {
            if ("--help".equalsIgnoreCase(a) || "-h".equalsIgnoreCase(a) || "/?".equals(a)) {
                return true;
            }
        }
        return false;
    }

    private static Manifest chooseDir(String[] args) {
        JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir", ".")));
        fc.setDialogTitle(L.t("dlg.chooseDir"));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File dir = fc.getSelectedFile();
        try {
            Manifest m = Manifest.load(dir);
            if (m == null) {
                m = Manifest.fromLayoutFiles(dir);
            }
            if (m == null) {
                JOptionPane.showMessageDialog(null,
                        L.t("dlg.noManifestInDir"), L.t("dlg.notFound"),
                        JOptionPane.WARNING_MESSAGE);
            }
            return m;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, L.t("dlg.readFail") + e, L.t("dlg.error"),
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}
