package com.darwish.nppsim;

import java.awt.Font;

/** 仪表字体（steelseries TextFonts）在切换语言后能否显示中文。 */
public class GaugeFontCheck {
    public static void main(String[] a) {
        Localization.init();
        for (Language l : new Language[]{Language.EN, Language.ZH_CN, Language.RU}) {
            Localization.setLanguage(l);
            Fonts.init();
            Font f = eu.hansolo.steelseries.tools.TextFonts.get("Verdana", Font.PLAIN, 14);
            String probe = "主蒸汽压力 凝汽器真空";
            System.out.printf("%-6s gaugeFont=%-18s %s%n", l, f.getFamily(),
                    f.canDisplayUpTo(probe) == -1 ? "OK" : "方框");
        }
        System.exit(0);
    }
}
