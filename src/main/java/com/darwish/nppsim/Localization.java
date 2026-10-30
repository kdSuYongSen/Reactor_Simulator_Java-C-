package com.darwish.nppsim;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.prefs.Preferences;
import org.json.JSONObject;

/**
 * 界面语言与文本查找（JSON 存储）。
 *
 *  文案文件：com/darwish/nppsim/i18n/&lt;lang&gt;.json（UTF-8，扁平 key→text）
 *      - 结构化键（来自原 NetBeans Bundle）：如 "UI.jMenu1.text"
 *      - 直接以英文原文为键（代码里的字面量）：如 "Power"
 *  查找顺序：当前语言 → 英文(en.json) → 键本身（保证不会出现空白）
 *
 *  用 JSON 而非 .properties 的原因：.properties 有 ISO-8859-1/UTF-8 的历史包袱，
 *  中/日/韩等非拉丁字符容易出现乱码；JSON 规范就是 UTF-8，且 org.json 已在本工程依赖中。
 */
public final class Localization {

    private static final String DIR = "/com/darwish/nppsim/i18n/";
    private static final Preferences PREFS = Preferences.userRoot().node("com/darwish/nppsim");
    private static final String PREF_KEY = "language";

    private static Language current = Language.EN;
    private static Map<String, String> table = new HashMap<>();
    private static Map<String, String> fallback = new HashMap<>();
    private static boolean initialized = false;

    private Localization() {
    }

    /** 程序启动时调用一次（幂等） */
    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        fallback = load(Language.EN);
        String saved = PREFS.get(PREF_KEY, null);
        Language lang = (saved == null) ? Language.detectFromSystem() : Language.fromId(saved);
        current = lang;
        table = (lang == Language.EN) ? fallback : load(lang);
        Locale.setDefault(lang.toLocale());
    }

    private static void ensureInit() {
        if (!initialized) {
            init();
        }
    }

    public static Language getLanguage() {
        ensureInit();
        return current;
    }

    /** 切换语言并保存（新创建的窗口立即使用新语言；已存在的窗口可调用各自的刷新方法） */
    public static void setLanguage(Language lang) {
        ensureInit();
        if (lang == null || lang == current) {
            return;
        }
        current = lang;
        table = (lang == Language.EN) ? fallback : load(lang);
        Locale.setDefault(lang.toLocale());
        try {
            PREFS.put(PREF_KEY, lang.id());
            PREFS.flush();
        } catch (Exception ignored) {
            // 首选项不可用时忽略（仅本次运行生效）
        }
    }

    /** 查找文案：当前语言 → 英文 → 键本身。
     *  注意：命中但值为空串是"合法结果"（原 NetBeans Bundle 里就有值为空的键，
     *  这些键不能退回键名，否则界面上会显示 "XXX.jTextField11.text" 这种字样）。 */
    public static String t(String key) {
        ensureInit();
        if (key == null) {
            return "";
        }
        String v = table.get(key);
        if (v != null) {
            return v;
        }
        v = fallback.get(key);
        if (v != null) {
            return v;
        }
        return key;
    }

    /** 读取 i18n/<lang>.json（UTF-8，Java 8 兼容的读法） */
    private static Map<String, String> load(Language lang) {
        Map<String, String> map = new HashMap<>();
        String path = DIR + lang.id() + ".json";
        try (InputStream in = Localization.class.getResourceAsStream(path)) {
            if (in == null) {
                return map;
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) {
                bos.write(buf, 0, n);
            }
            JSONObject json = new JSONObject(new String(bos.toByteArray(), StandardCharsets.UTF_8));
            for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                String k = it.next();
                map.put(k, json.optString(k, ""));
            }
        } catch (Exception e) {
            // 读取失败则退回英文/键名
        }
        return map;
    }
}
