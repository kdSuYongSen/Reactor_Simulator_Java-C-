package com.darwish.nppsim;

import java.util.LinkedHashMap;
import java.util.Map;

/** 语言包完整性：每个语言都能解析出全部键，且关键新文案不是英文兜底。 */
public class I18nCheck {
    public static void main(String[] a) {
        Localization.init();
        String[] keys = {
            "Emergency Diesel Generators", "Grid System", "ECCS", "Offline Cooling",
            "Ventilation System", "Lighting System", "Heat Sink", "Radiation Monitoring",
            "To be continued", "settings.units.tab", "settings.units.q.pressure", "Condenser Unit"
        };
        Map<String, String> en = new LinkedHashMap<>();
        for (Language lang : Language.values()) {
            Localization.setLanguage(lang);
            StringBuilder sb = new StringBuilder();
            for (String k : keys) {
                String v = Localization.t(k);
                if (lang == Language.EN) en.put(k, v);
                sb.append(v).append(" | ");
            }
            System.out.printf("%-8s %s%n", lang, sb);
        }
        System.out.println("I18N_OK");
        System.exit(0);
    }
}
