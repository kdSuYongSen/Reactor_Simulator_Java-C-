import com.darwish.nppsim.layeditor.Manifest;
import com.darwish.nppsim.layeditor.LayoutWriter;
import java.io.File;

/** 编辑器模型自检：读清单 → 找一个 3 层节点 → 改坐标 → 写盘，并打印路径。 */
public class EditorModelTest {
    public static void main(String[] a) throws Exception {
        File dir = new File(a.length > 0 ? a[0] : "target");
        Manifest m = Manifest.load(dir);
        if (m == null) {
            System.out.println("清单读取失败");
            System.exit(1);
        }
        System.out.println("清单: " + m.manifestFile + "  页数=" + m.pages.size());
        for (Manifest.Page p : m.pages) {
            int nodes = 0;
            for (Manifest.Node b : p.blocks) nodes += count(b);
            System.out.printf("  %-16s blocks=%2d nodes=%3d%n", p.id, p.blocks.size(), nodes);
        }

        Manifest.Page page = null;
        for (Manifest.Page p : m.pages) {
            if ("CondensateUI".equals(p.id)) page = p;
        }
        if (page == null) {
            System.out.println("没有 CondensateUI 页");
            System.exit(1);
        }
        Manifest.Node deep = null;
        for (Manifest.Node b : page.blocks) {
            Manifest.Node d = findDeep(b, 2);
            if (d != null) { deep = d; break; }
        }
        if (deep == null) {
            System.out.println("没有 3 层节点");
            System.exit(1);
        }
        System.out.printf("选中: 方块 %s / 路径 %s  %s  bounds=[%d,%d,%d,%d]%n",
                deep.blockId, deep.path, deep.display(), deep.x, deep.y, deep.w, deep.h);

        deep.x += 30;
        deep.y += 8;
        deep.w += 10;
        deep.h += 6;
        deep.dirty = true;
        System.out.printf("改为: bounds=[%d,%d,%d,%d]%n", deep.x, deep.y, deep.w, deep.h);

        File out = LayoutWriter.save(page, dir);
        System.out.println("已写: " + out + "  (" + out.length() + " B)");
        System.out.println("期望路径(供 Java 侧核对): " + deep.blockId + "|" + deep.path
                + "|" + deep.x + "," + deep.y + "," + deep.w + "," + deep.h);
        System.out.println("EDITOR_MODEL_OK");
    }

    static int count(Manifest.Node n) {
        int c = 1;
        for (Manifest.Node k : n.children) c += count(k);
        return c;
    }

    static Manifest.Node findDeep(Manifest.Node n, int minDepth) {
        if (n.path.split("/").length >= minDepth + 1 && !n.children.isEmpty()) return n;
        for (Manifest.Node c : n.children) {
            Manifest.Node r = findDeep(c, minDepth);
            if (r != null) return r;
        }
        return null;
    }
}
