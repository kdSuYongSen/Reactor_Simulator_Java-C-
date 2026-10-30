import com.darwish.nppsim.layeditor.Editor;
import com.darwish.nppsim.layeditor.L;
import com.darwish.nppsim.layeditor.Manifest;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;

/**
 * 编辑器 jar 的行为自检：
 * ① 点选组件后尺寸/位置会不会被串台（老 bug：点一下就消失 / 随机变大变小）；
 * ② 中英文切换是否真的换掉界面文字；
 * ③ 方向键微调是否生效。
 */
public class EditorBehaviorCheck {

    static int fails = 0;

    static void check(String what, boolean ok, String detail) {
        if (!ok) {
            fails++;
        }
        System.out.printf("  [%s] %-42s %s%n", ok ? "OK" : "FAIL", what, detail);
    }

    public static void main(String[] a) throws Exception {
        Manifest m = Manifest.load(new File(a.length > 0 ? a[0] : "target"));
        final Editor[] h = new Editor[1];
        SwingUtilities.invokeAndWait(() -> {
            Editor ed = new Editor(m);
            ed.setVisible(true);
            h[0] = ed;
        });
        Thread.sleep(700);
        Editor ed = h[0];

        // ---------- ① 选组件不会串台 ----------
        Manifest.Page page = m.pages.get(0);
        Manifest.Node big = page.blocks.get(0);
        Manifest.Node small = null;
        for (Manifest.Node b : page.blocks) {
            if (!b.children.isEmpty()) {
                small = b.children.get(0);
                break;
            }
        }
        if (small == null) {
            System.out.println("清单里没有子组件，跳过串台检查");
        } else {
            final int sw = small.w;
            final int sh = small.h;
            final int sx = small.x;
            final Manifest.Node nBig = big;
            final Manifest.Node nSmall = small;
            SwingUtilities.invokeAndWait(() -> {
                try {
                    Method sel = Editor.class.getDeclaredMethod("select", Manifest.Node.class);
                    sel.setAccessible(true);
                    sel.invoke(ed, nBig);      // 先选大方块
                    sel.invoke(ed, nSmall);    // 再选小组件
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            Thread.sleep(250);
            check("点选后小组件尺寸不被上一个组件带偏",
                    small.w == sw && small.h == sh && small.x == sx,
                    "before=" + sw + "x" + sh + "@" + sx + "  after=" + small.w + "x" + small.h + "@" + small.x);
            JSpinner spw = (JSpinner) get(ed, "spW");
            JSpinner sph = (JSpinner) get(ed, "spH");
            check("属性栏显示的是选中组件自己的宽高",
                    ((Integer) spw.getValue()) == small.w && ((Integer) sph.getValue()) == small.h,
                    "spinner=" + spw.getValue() + "x" + sph.getValue() + "  node=" + small.w + "x" + small.h);
        }

        // ---------- ② 语言切换 ----------
        List<String> zh = buttons(ed);
        check("中文界面有中文按钮", zh.contains("保存") && zh.contains("提示"),
                String.join(" | ", zh));
        L.setLang(L.Lang.EN);
        final Editor[] h2 = new Editor[1];
        SwingUtilities.invokeAndWait(() -> {
            Editor e2 = new Editor(m);
            e2.setVisible(true);
            h2[0] = e2;
        });
        Thread.sleep(600);
        List<String> en = buttons(h2[0]);
        check("切到英文后界面文字全变英文",
                en.contains("Save") && en.contains("Help") && en.contains("Reload manifest"),
                String.join(" | ", en));
        check("英文界面不再出现中文按钮",
                !en.contains("保存") && !en.contains("提示") && !en.contains("重新读取清单"),
                String.join(" | ", en));

        // ---------- ③ 方向键微调 ----------
        final Manifest.Node target = page.blocks.get(0);
        final int x0 = target.x;
        SwingUtilities.invokeAndWait(() -> {
            try {
                Method sel = Editor.class.getDeclaredMethod("select", Manifest.Node.class);
                sel.setAccessible(true);
                sel.invoke(ed, target);
                Method nudge = Editor.class.getDeclaredMethod("nudge", int.class, int.class, int.class);
                nudge.setAccessible(true);
                nudge.invoke(ed, 1, 0, 10);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        Thread.sleep(200);
        check("方向键微调移动 10 px", target.x == x0 + 10, "x " + x0 + " -> " + target.x);

        System.out.println(fails == 0 ? "EDITOR_BEHAVIOR_OK" : "EDITOR_BEHAVIOR_FAIL (" + fails + ")");
        System.exit(fails == 0 ? 0 : 1);
    }

    static Object get(Object o, String field) throws Exception {
        Field f = o.getClass().getDeclaredField(field);
        f.setAccessible(true);
        return f.get(o);
    }

    static List<String> buttons(Container c) {
        List<String> out = new ArrayList<>();
        collect(c, out);
        return out;
    }

    static void collect(Container c, List<String> out) {
        for (Component x : c.getComponents()) {
            if (x instanceof AbstractButton) {
                String t = ((AbstractButton) x).getText();
                if (t != null && !t.isEmpty()) {
                    out.add(t);
                }
            }
            if (x instanceof JSpinner) {
                out.add("spinner");
            }
            if (x instanceof Container) {
                collect((Container) x, out);
            }
        }
    }
}
