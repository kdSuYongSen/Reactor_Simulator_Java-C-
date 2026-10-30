/*
 * Dark Metal 外观（源自 NetBeans Dark Look And Feel Themes 模块，Apache License 2.0）。
 * 本文件由原始 RBMK-1500-Simulator.jar 反编译结果移植而来，去除了对
 * org.openide.util.NbBundle 的依赖，使其可独立编译。
 */
package org.netbeans.swing.laf.dark;

import javax.swing.UIDefaults;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * 基于 {@link MetalLookAndFeel} 的深色金属外观。
 * 继承 Metal 的 3D 立体/金属质感渲染，只是套用 {@link DarkMetalTheme} 的深灰配色，
 * 因此保留了原生按键、边框、滚动条等组件的“金属感”。
 */
public class DarkMetalLookAndFeel extends MetalLookAndFeel {

    @Override
    public String getName() {
        return "Dark Metal";
    }

    @Override
    protected void createDefaultTheme() {
        super.createDefaultTheme();
        if (!(getCurrentTheme() instanceof DarkMetalTheme)) {
            setCurrentTheme(new DarkMetalTheme());
        }
    }

    @Override
    public UIDefaults getDefaults() {
        return super.getDefaults();
    }

    @Override
    public void initialize() {
        super.initialize();
    }
}
