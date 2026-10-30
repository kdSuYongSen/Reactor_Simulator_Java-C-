package com.darwish.nppsim;

import java.io.File;

/** 只测解析：编辑器写的 nodes 能不能被 UiLayoutFile 读出来。 */
public class ParseCheck {
    public static void main(String[] a) throws Exception {
        File f = new File(a.length > 0 ? a[0] : "target/CondensateUI.layout.json");
        System.out.println("file=" + f.getAbsolutePath() + "  exists=" + f.isFile()
                + "  hasVersion=" + (f.isFile() && UiLayoutFile.hasVersion(f)));
        java.util.Map<String, UiLayoutFile.Entry> e = UiLayoutFile.parse(f);
        System.out.println("panels=" + e.size());
        UiLayoutFile.Entry b = e.get("5");
        System.out.println("panel5=" + (b == null ? "null"
                : ("x=" + b.x + " y=" + b.y + " w=" + b.w + " h=" + b.h
                   + " children=" + (b.children == null ? 0 : b.children.size())
                   + " nodes=" + (b.nodes == null ? 0 : b.nodes.size()))));
        if (b != null && b.nodes != null) {
            UiLayoutFile.Child c = b.nodes.get("0/2/2");
            System.out.println("node 0/2/2 = " + (c == null ? "null"
                    : (c.cls + " " + c.x + "," + c.y + " " + c.w + "x" + c.h)));
        }
        System.exit(0);
    }
}
