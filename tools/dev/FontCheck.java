package com.darwish.nppsim;

import java.awt.Font;
import javax.swing.UIManager;

/** 语言列表是否会显示方框：当前 UI 字体能否显示各语言的自称。 */
public class FontCheck {
    public static void main(String[] a) {
        Localization.init();
        for (Language cur : new Language[]{Language.EN, Language.ZH_CN, Language.KO}) {
            Localization.setLanguage(cur);
            Fonts.init();
            Font ui = UIManager.getFont("RadioButton.font");
            StringBuilder sb = new StringBuilder("当前=" + cur + "  UI字体=" + ui.getFamily() + " → ");
            for (Language l : Language.values()) {
                sb.append(l.name()).append(l.nativeName)
                  .append(ui.canDisplayUpTo(l.nativeName) == -1 ? " OK  " : " 方框 ");
            }
            System.out.println(sb);
        }
        System.exit(0);
    }
}
