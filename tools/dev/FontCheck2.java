package com.darwish.nppsim;

import java.awt.Font;

/** 占位页字体：各语言下 "未完待续" 是否真能显示。 */
public class FontCheck2 {
    public static void main(String[] a) {
        Localization.init();
        for (Language l : Language.values()) {
            Localization.setLanguage(l);
            Fonts.init();
            Font f = Fonts.text("Dialog", Font.BOLD, 36);
            String txt = Localization.t("To be continued");
            System.out.printf("%-6s family=%-22s text='%s'  %s%n", l, f.getFamily(), txt,
                    f.canDisplayUpTo(txt) == -1 ? "OK" : "方框");
        }
        System.exit(0);
    }
}
