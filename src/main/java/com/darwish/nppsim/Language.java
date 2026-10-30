package com.darwish.nppsim;

import java.util.Locale;

/** 支持的界面语言。新增语言只需：加一个枚举项 + 一个 i18n/Messages_xx.properties。 */
public enum Language {
    ZH_CN("zh", "CN", "简体中文"),
    ZH_TW("zh", "TW", "繁體中文"),
    EN("en", "", "English"),
    RU("ru", "", "Русский"),
    FR("fr", "", "Français"),
    JA("ja", "", "日本語"),
    KO("ko", "", "한국어");

    public final String code;
    public final String country;
    /** 语言的自称（列表里显示用，不随界面语言变化） */
    public final String nativeName;

    Language(String code, String country, String nativeName) {
        this.code = code;
        this.country = country;
        this.nativeName = nativeName;
    }

    public Locale toLocale() {
        return country.isEmpty() ? new Locale(code) : new Locale(code, country);
    }

    /** 持久化用的标识，如 zh_CN / en */
    public String id() {
        return country.isEmpty() ? code : code + "_" + country;
    }

    public static Language fromId(String id) {
        if (id != null) {
            for (Language l : values()) {
                if (l.id().equalsIgnoreCase(id)) {
                    return l;
                }
            }
        }
        return EN;
    }

    /** 按系统语言猜一个默认值（首次运行时用） */
    public static Language detectFromSystem() {
        Locale sys = Locale.getDefault();
        for (Language l : values()) {
            if (l.code.equalsIgnoreCase(sys.getLanguage())) {
                if (l.country.isEmpty() || l.country.equalsIgnoreCase(sys.getCountry())) {
                    return l;
                }
            }
        }
        return EN;
    }
}
