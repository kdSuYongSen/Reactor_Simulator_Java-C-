/*
 * Dark Metal 主题（源自 NetBeans Dark Look And Feel Themes 模块，Apache License 2.0）。
 * 本文件由原始 RBMK-1500-Simulator.jar 反编译结果移植而来，去除了对
 * org.openide.util.NbBundle 等 NetBeans 平台模块的依赖，使其可独立编译。
 */
package org.netbeans.swing.laf.dark;

import java.awt.Color;
import java.awt.Font;
import javax.swing.UIDefaults;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.MetalTheme;

public class DarkMetalTheme extends MetalTheme {
    private final ColorUIResource primary1 = new ColorUIResource(121, 121, 125);
    private final ColorUIResource primary2 = new ColorUIResource(71, 71, 75);
    private final ColorUIResource primary3 = new ColorUIResource(99, 99, 99);
    private final ColorUIResource secondary1 = new ColorUIResource(113, 113, 113);
    private final ColorUIResource secondary2 = new ColorUIResource(91, 91, 95);
    private final ColorUIResource secondary3 = new ColorUIResource(51, 51, 55);
    private final ColorUIResource black = new ColorUIResource(222, 222, 222);
    private final ColorUIResource white = new ColorUIResource(18, 30, 49);
    private static final FontUIResource DEFAULT_FONT = new FontUIResource("Dialog", 0, 11);

    @Override
    public String getName() {
        return "NetBeans Dark Theme";
    }

    @Override
    public void addCustomEntriesToTable(UIDefaults table) {
        super.addCustomEntriesToTable(table);
        table.put("nb.dark.theme", Boolean.TRUE);
        table.put("nb.preferred.color.profile", "Norway Today");
        table.put("PropSheet.setBackground", new Color(this.primary2.getRGB()));
        table.put("PropSheet.selectedSetBackground", new Color(this.primary1.getRGB()));
        table.put("PropSheet.selectedSetForeground", this.black);
        table.put("PropSheet.setForeground", this.black);
        table.put("PropSheet.selectionBackground", this.primary3);
        table.put("PropSheet.selectionForeground", this.black);
        table.put("selection.highlight", new Color(202, 152, 0));
        table.put("textArea.background", new Color(51, 51, 55));
        table.put("MenuItem.acceleratorForeground", new Color(198, 198, 198));
        table.put("CheckBoxMenuItem.acceleratorForeground", new Color(198, 198, 198));
        table.put("RadioButtonMenuItem.acceleratorForeground", new Color(198, 198, 198));
    }

    @Override
    protected ColorUIResource getPrimary1() {
        return this.primary1;
    }

    @Override
    protected ColorUIResource getPrimary2() {
        return this.primary2;
    }

    @Override
    protected ColorUIResource getPrimary3() {
        return this.primary3;
    }

    @Override
    protected ColorUIResource getSecondary1() {
        return this.secondary1;
    }

    @Override
    protected ColorUIResource getSecondary2() {
        return this.secondary2;
    }

    @Override
    protected ColorUIResource getSecondary3() {
        return this.secondary3;
    }

    @Override
    protected ColorUIResource getWhite() {
        return this.white;
    }

    @Override
    protected ColorUIResource getBlack() {
        return this.black;
    }

    @Override
    public FontUIResource getControlTextFont() {
        return DEFAULT_FONT;
    }

    @Override
    public FontUIResource getSystemTextFont() {
        return DEFAULT_FONT;
    }

    @Override
    public FontUIResource getUserTextFont() {
        return DEFAULT_FONT;
    }

    @Override
    public FontUIResource getMenuTextFont() {
        return DEFAULT_FONT;
    }

    @Override
    public FontUIResource getWindowTitleFont() {
        return DEFAULT_FONT;
    }

    @Override
    public FontUIResource getSubTextFont() {
        return DEFAULT_FONT;
    }
}
