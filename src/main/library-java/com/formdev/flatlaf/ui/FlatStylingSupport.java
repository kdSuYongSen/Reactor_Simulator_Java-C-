package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.util.StringUtils;
import com.formdev.flatlaf.util.SystemInfo;
import java.beans.PropertyChangeListener;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;

public class FlatStylingSupport {
   public static Object getStyle(JComponent c) {
      return c.getClientProperty("FlatLaf.style");
   }

   public static Object getStyleClass(JComponent c) {
      return c.getClientProperty("FlatLaf.styleClass");
   }

   static boolean hasStyleProperty(JComponent c) {
      return getStyle(c) != null || getStyleClass(c) != null;
   }

   public static Object getResolvedStyle(JComponent c, String type) {
      Object style = getStyle(c);
      Object styleClass = getStyleClass(c);
      Object styleForClasses = getStyleForClasses(styleClass, type);
      return joinStyles(styleForClasses, style);
   }

   public static Object getStyleForClasses(Object styleClass, String type) {
      if (styleClass == null) {
         return null;
      }

      if (styleClass instanceof String && ((String)styleClass).indexOf(32) >= 0) {
         styleClass = StringUtils.split((String)styleClass, ' ', true, true);
      }

      if (styleClass instanceof String) {
         return getStyleForClass(((String)styleClass).trim(), type);
      }

      if (styleClass instanceof String[]) {
         Object style = null;

         for (String cls : (String[])styleClass) {
            style = joinStyles(style, getStyleForClass(cls, type));
         }

         return style;
      } else {
         if (!(styleClass instanceof List)) {
            return null;
         }

         Object style = null;

         for (Object cls : (List)styleClass) {
            style = joinStyles(style, getStyleForClass((String)cls, type));
         }

         return style;
      }
   }

   private static Object getStyleForClass(String styleClass, String type) {
      return joinStyles(UIManager.get("[style]." + styleClass), UIManager.get("[style]" + type + '.' + styleClass));
   }

   public static Object joinStyles(Object style1, Object style2) {
      if (style1 == null) {
         return style2;
      }

      if (style2 == null) {
         return style1;
      }

      if (style1 instanceof String && style2 instanceof String) {
         return style1 + "; " + style2;
      }

      Map<String, Object> map1 = style1 instanceof String ? parse((String)style1) : (Map)style1;
      if (map1 == null) {
         return style2;
      }

      Map<String, Object> map2 = style2 instanceof String ? parse((String)style2) : (Map)style2;
      if (map2 == null) {
         return style1;
      }

      Map<String, Object> map = new HashMap<>(map1);
      map.putAll(map2);
      return map;
   }

   public static String concatStyles(String style1, String style2) {
      if (style1 == null) {
         return style2;
      } else {
         return style2 == null ? style1 : style1 + "; " + style2;
      }
   }

   public static Map<String, Object> parseAndApply(Map<String, Object> oldStyleValues, Object style, BiFunction<String, Object, Object> applyProperty) throws FlatStylingSupport.UnknownStyleException, IllegalArgumentException {
      if (oldStyleValues != null) {
         for (Entry<String, Object> e : oldStyleValues.entrySet()) {
            applyProperty.apply(e.getKey(), e.getValue());
         }
      }

      if (style == null) {
         return null;
      } else if (style instanceof String) {
         String str = (String)style;
         return StringUtils.isTrimmedEmpty(str) ? null : applyStyle(parse(str), applyProperty);
      } else if (style instanceof Map) {
         Map<String, Object> map = (Map<String, Object>)style;
         return applyStyle(map, applyProperty);
      } else {
         return null;
      }
   }

   private static Map<String, Object> applyStyle(Map<String, Object> style, BiFunction<String, Object, Object> applyProperty) {
      if (style.isEmpty()) {
         return null;
      }

      Map<String, Object> oldValues = new HashMap<>();

      for (Entry<String, Object> e : style.entrySet()) {
         String key = e.getKey();
         Object newValue = e.getValue();
         if (key.startsWith("[")) {
            if ((!SystemInfo.isWindows || !key.startsWith("[win]"))
               && (!SystemInfo.isMacOS || !key.startsWith("[mac]"))
               && (!SystemInfo.isLinux || !key.startsWith("[linux]"))
               && (!key.startsWith("[light]") || FlatLaf.isLafDark())
               && (!key.startsWith("[dark]") || !FlatLaf.isLafDark())) {
               continue;
            }

            key = key.substring(key.indexOf(93) + 1);
         }

         Object oldValue = applyProperty.apply(key, newValue);
         oldValues.put(key, oldValue);
      }

      return oldValues;
   }

   public static Map<String, Object> parse(String style) throws IllegalArgumentException {
      if (style != null && !StringUtils.isTrimmedEmpty(style)) {
         Map<String, Object> map = null;

         for (String part : StringUtils.split(style, ';', true, true)) {
            int sepIndex = part.indexOf(58);
            if (sepIndex < 0) {
               throw new IllegalArgumentException("missing colon in '" + part + "'");
            }

            String key = StringUtils.substringTrimmed(part, 0, sepIndex);
            String value = StringUtils.substringTrimmed(part, sepIndex + 1);
            if (key.isEmpty()) {
               throw new IllegalArgumentException("missing key in '" + part + "'");
            }

            if (value.isEmpty()) {
               throw new IllegalArgumentException("missing value in '" + part + "'");
            }

            if (map == null) {
               map = new LinkedHashMap<>();
            }

            map.put(key, parseValue(key, value));
         }

         return map;
      } else {
         return null;
      }
   }

   private static Object parseValue(String key, String value) {
      if (value.startsWith("$")) {
         return UIManager.get(value.substring(1));
      }

      if (key.startsWith("[")) {
         key = key.substring(key.indexOf(93) + 1);
      }

      return FlatLaf.parseDefaultsValue(key, value, null);
   }

   public static Object applyToAnnotatedObject(Object obj, String key, Object value) throws FlatStylingSupport.UnknownStyleException, IllegalArgumentException {
      String fieldName = keyToFieldName(key);
      return applyToField(obj, fieldName, key, value, field -> {
         FlatStylingSupport.Styleable styleable = field.getAnnotation(FlatStylingSupport.Styleable.class);
         return styleable != null && styleable.dot() == (fieldName != key);
      });
   }

   private static String keyToFieldName(String key) {
      int dotIndex = key.indexOf(46);
      return dotIndex < 0 ? key : key.substring(0, dotIndex) + Character.toUpperCase(key.charAt(dotIndex + 1)) + key.substring(dotIndex + 2);
   }

   static Object applyToField(Object obj, String fieldName, String key, Object value) throws FlatStylingSupport.UnknownStyleException, IllegalArgumentException {
      return applyToField(obj, fieldName, key, value, null);
   }

   private static Object applyToField(Object obj, String fieldName, String key, Object value, Predicate<Field> predicate) throws FlatStylingSupport.UnknownStyleException, IllegalArgumentException {
      Class<?> cls = obj.getClass();

      while (true) {
         try {
            Field f = cls.getDeclaredField(fieldName);
            if (predicate == null || predicate.test(f)) {
               return applyToField(f, obj, value, false);
            }
         } catch (NoSuchFieldException var10) {
         }

         for (FlatStylingSupport.StyleableField styleableField : cls.getAnnotationsByType(FlatStylingSupport.StyleableField.class)) {
            if (key.equals(styleableField.key())) {
               return applyToField(getStyleableField(styleableField), obj, value, true);
            }
         }

         cls = cls.getSuperclass();
         if (cls == null) {
            throw new FlatStylingSupport.UnknownStyleException(key);
         }

         if (predicate != null) {
            String superclassName = cls.getName();
            if (superclassName.startsWith("java.") || superclassName.startsWith("javax.")) {
               throw new FlatStylingSupport.UnknownStyleException(key);
            }
         }
      }
   }

   private static Object applyToField(Field f, Object obj, Object value, boolean useMethodHandles) {
      checkValidField(f);
      if (useMethodHandles && obj instanceof FlatStylingSupport.StyleableLookupProvider) {
         try {
            Lookup lookup = ((FlatStylingSupport.StyleableLookupProvider)obj).getLookupForStyling();
            Object oldValue = (Object)lookup.unreflectGetter(f).invoke((Object)obj);
            lookup.unreflectSetter(f).invoke((Object)obj, (Object)convertToEnum(value, f.getType()));
            return oldValue;
         } catch (Throwable ex) {
            throw newFieldAccessFailed(f, ex);
         }
      } else {
         try {
            f.setAccessible(true);
            Object oldValue = f.get(obj);
            f.set(obj, convertToEnum(value, f.getType()));
            return oldValue;
         } catch (IllegalAccessException ex) {
            throw newFieldAccessFailed(f, ex);
         }
      }
   }

   private static Object getFieldValue(Field f, Object obj, boolean useMethodHandles) {
      checkValidField(f);
      if (useMethodHandles && obj instanceof FlatStylingSupport.StyleableLookupProvider) {
         try {
            Lookup lookup = ((FlatStylingSupport.StyleableLookupProvider)obj).getLookupForStyling();
            return (Object)lookup.unreflectGetter(f).invoke((Object)obj);
         } catch (Throwable ex) {
            throw newFieldAccessFailed(f, ex);
         }
      } else {
         try {
            f.setAccessible(true);
            return f.get(obj);
         } catch (IllegalAccessException ex) {
            throw newFieldAccessFailed(f, ex);
         }
      }
   }

   private static IllegalArgumentException newFieldAccessFailed(Field f, Throwable ex) {
      return new IllegalArgumentException("failed to access field '" + f.getDeclaringClass().getName() + "." + f.getName() + "'", ex);
   }

   private static void checkValidField(Field f) {
      if (!isValidField(f)) {
         throw new IllegalArgumentException("field '" + f.getDeclaringClass().getName() + "." + f.getName() + "' is final or static");
      }
   }

   private static boolean isValidField(Field f) {
      int modifiers = f.getModifiers();
      return (modifiers & 24) == 0 && !f.isSynthetic();
   }

   private static Field getStyleableField(FlatStylingSupport.StyleableField styleableField) {
      String fieldName = styleableField.fieldName();
      if (fieldName.isEmpty()) {
         fieldName = styleableField.key();
      }

      try {
         return styleableField.cls().getDeclaredField(fieldName);
      } catch (NoSuchFieldException ex) {
         throw new IllegalArgumentException("field '" + styleableField.cls().getName() + "." + fieldName + "' not found", ex);
      }
   }

   private static Object applyToProperty(Object obj, String name, Object value) throws FlatStylingSupport.UnknownStyleException, IllegalArgumentException {
      Class<?> cls = obj.getClass();
      String getterName = buildMethodName("get", name);
      String setterName = buildMethodName("set", name);

      try {
         Method getter;
         try {
            getter = cls.getMethod(getterName);
         } catch (NoSuchMethodException ex) {
            getter = cls.getMethod(buildMethodName("is", name));
         }

         Method setter = cls.getMethod(setterName, getter.getReturnType());
         Object oldValue = getter.invoke(obj);
         setter.invoke(obj, convertToEnum(value, getter.getReturnType()));
         return oldValue;
      } catch (NoSuchMethodException ex) {
         throw new FlatStylingSupport.UnknownStyleException(name);
      } catch (Exception ex) {
         throw new IllegalArgumentException("failed to invoke property methods '" + cls.getName() + "." + getterName + "()' or '" + setterName + "(...)'", ex);
      }
   }

   private static String buildMethodName(String prefix, String name) {
      int prefixLength = prefix.length();
      int nameLength = name.length();
      char[] chars = new char[prefixLength + nameLength];
      prefix.getChars(0, prefixLength, chars, 0);
      name.getChars(0, nameLength, chars, prefixLength);
      chars[prefixLength] = Character.toUpperCase(chars[prefixLength]);
      return new String(chars);
   }

   private static Object convertToEnum(Object value, Class<?> type) throws IllegalArgumentException {
      if (Enum.class.isAssignableFrom(type) && value instanceof String) {
         try {
            value = Enum.valueOf((Class)type, (String)value);
         } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unknown enum value '" + value + "' in enum '" + type.getName() + "'", ex);
         }
      }

      return value;
   }

   public static Object applyToAnnotatedObjectOrComponent(Object obj, Object comp, String key, Object value) throws FlatStylingSupport.UnknownStyleException, IllegalArgumentException {
      try {
         return applyToAnnotatedObject(obj, key, value);
      } catch (FlatStylingSupport.UnknownStyleException ex) {
         try {
            if (comp != null) {
               return applyToProperty(comp, key, value);
            }
         } catch (FlatStylingSupport.UnknownStyleException var6) {
         }

         throw ex;
      }
   }

   static Object applyToAnnotatedObjectOrBorder(Object obj, String key, Object value, JComponent c, AtomicBoolean borderShared) {
      try {
         return applyToAnnotatedObject(obj, key, value);
      } catch (FlatStylingSupport.UnknownStyleException ex) {
         Border border = c.getBorder();
         if (border instanceof FlatStylingSupport.StyleableBorder) {
            if (borderShared.get()) {
               border = cloneBorder(border);
               c.setBorder(border);
               borderShared.set(false);
            }

            try {
               return ((FlatStylingSupport.StyleableBorder)border).applyStyleProperty(key, value);
            } catch (FlatStylingSupport.UnknownStyleException var9) {
            }
         }

         try {
            return applyToProperty(c, key, value);
         } catch (FlatStylingSupport.UnknownStyleException var8) {
            throw ex;
         }
      }
   }

   static PropertyChangeListener createPropertyChangeListener(JComponent c, Runnable installStyle, PropertyChangeListener superListener) {
      return e -> {
         if (superListener != null) {
            superListener.propertyChange(e);
         }

         switch (e.getPropertyName()) {
            case "FlatLaf.style":
            case "FlatLaf.styleClass":
               installStyle.run();
               c.revalidate();
               c.repaint();
         }
      };
   }

   static Border cloneBorder(Border border) {
      Class<? extends Border> borderClass = (Class<? extends Border>)border.getClass();

      try {
         return borderClass.getDeclaredConstructor().newInstance();
      } catch (Exception ex) {
         throw new IllegalArgumentException("failed to clone border '" + borderClass.getName() + "'", ex);
      }
   }

   static Icon cloneIcon(Icon icon) {
      Class<? extends Icon> iconClass = (Class<? extends Icon>)icon.getClass();

      try {
         return iconClass.getDeclaredConstructor().newInstance();
      } catch (Exception ex) {
         throw new IllegalArgumentException("failed to clone icon '" + iconClass.getName() + "'", ex);
      }
   }

   public static Map<String, Class<?>> getAnnotatedStyleableInfos(Object obj) {
      return getAnnotatedStyleableInfos(obj, null);
   }

   public static Map<String, Class<?>> getAnnotatedStyleableInfos(Object obj, Border border) {
      Map<String, Class<?>> infos = new FlatStylingSupport.StyleableInfosMap<>();
      collectAnnotatedStyleableInfos(obj, infos);
      collectStyleableInfos(border, infos);
      return infos;
   }

   public static void collectAnnotatedStyleableInfos(Object obj, Map<String, Class<?>> infos) {
      HashSet<String> processedFields = new HashSet<>();
      Class<?> cls = obj.getClass();

      String superclassName;
      do {
         for (Field f : cls.getDeclaredFields()) {
            if (isValidField(f)) {
               FlatStylingSupport.Styleable styleable = f.getAnnotation(FlatStylingSupport.Styleable.class);
               if (styleable != null) {
                  String name = f.getName();
                  Class<?> type = f.getType();
                  if (!processedFields.contains(name)) {
                     processedFields.add(name);
                     if (styleable.dot()) {
                        int len = name.length();

                        for (int i = 0; i < len; i++) {
                           if (Character.isUpperCase(name.charAt(i))) {
                              name = name.substring(0, i) + '.' + Character.toLowerCase(name.charAt(i)) + name.substring(i + 1);
                              break;
                           }
                        }
                     }

                     if (styleable.type() != Void.class) {
                        type = styleable.type();
                     }

                     infos.put(name, type);
                  }
               }
            }
         }

         for (FlatStylingSupport.StyleableField styleableField : cls.getAnnotationsByType(FlatStylingSupport.StyleableField.class)) {
            String name = styleableField.key();
            if (!processedFields.contains(name)) {
               processedFields.add(name);
               Field f = getStyleableField(styleableField);
               infos.put(name, f.getType());
            }
         }

         cls = cls.getSuperclass();
         if (cls == null) {
            return;
         }

         superclassName = cls.getName();
      } while (!superclassName.startsWith("java.") && !superclassName.startsWith("javax."));
   }

   public static void collectStyleableInfos(Border border, Map<String, Class<?>> infos) {
      if (border instanceof FlatStylingSupport.StyleableBorder) {
         infos.putAll(((FlatStylingSupport.StyleableBorder)border).getStyleableInfos());
      }
   }

   public static void putAllPrefixKey(Map<String, Class<?>> infos, String keyPrefix, Map<String, Class<?>> infos2) {
      for (Entry<String, Class<?>> e : infos2.entrySet()) {
         infos.put(keyPrefix.concat(e.getKey()), e.getValue());
      }
   }

   public static Object getAnnotatedStyleableValue(Object obj, String key) {
      String fieldName = keyToFieldName(key);
      Class<?> cls = obj.getClass();

      String superclassName;
      do {
         try {
            Field f = cls.getDeclaredField(fieldName);
            FlatStylingSupport.Styleable styleable = f.getAnnotation(FlatStylingSupport.Styleable.class);
            if (styleable != null) {
               if (styleable.dot() != (fieldName != key)) {
                  throw new IllegalArgumentException("'Styleable.dot' on field '" + fieldName + "' does not match key '" + key + "'");
               }

               if (styleable.type() != Void.class) {
                  throw new IllegalArgumentException("'Styleable.type' on field '" + fieldName + "' not supported");
               }

               return getFieldValue(f, obj, false);
            }
         } catch (NoSuchFieldException var8) {
         }

         for (FlatStylingSupport.StyleableField styleableField : cls.getAnnotationsByType(FlatStylingSupport.StyleableField.class)) {
            if (key.equals(styleableField.key())) {
               return getFieldValue(getStyleableField(styleableField), obj, true);
            }
         }

         cls = cls.getSuperclass();
         if (cls == null) {
            return null;
         }

         superclassName = cls.getName();
      } while (!superclassName.startsWith("java.") && !superclassName.startsWith("javax."));

      return null;
   }

   public static Object getAnnotatedStyleableValue(Object obj, Border border, String key) {
      if (border instanceof FlatStylingSupport.StyleableBorder) {
         Object value = ((FlatStylingSupport.StyleableBorder)border).getStyleableValue(key);
         if (value != null) {
            return value;
         }
      }

      return getAnnotatedStyleableValue(obj, key);
   }

   @Target(ElementType.FIELD)
   @Retention(RetentionPolicy.RUNTIME)
   public @interface Styleable {
      boolean dot() default false;

      Class<?> type() default Void.class;
   }

   public interface StyleableBorder {
      Object applyStyleProperty(String var1, Object var2);

      Map<String, Class<?>> getStyleableInfos();

      Object getStyleableValue(String var1);
   }

   @Target(ElementType.TYPE)
   @Retention(RetentionPolicy.RUNTIME)
   @Repeatable(FlatStylingSupport.StyleableFields.class)
   public @interface StyleableField {
      Class<?> cls();

      String key();

      String fieldName() default "";
   }

   @Target(ElementType.TYPE)
   @Retention(RetentionPolicy.RUNTIME)
   public @interface StyleableFields {
      FlatStylingSupport.StyleableField[] value();
   }

   static class StyleableInfosMap<K, V> extends LinkedHashMap<K, V> {
      @Override
      public V put(K key, V value) {
         V oldValue = super.put(key, value);
         if (oldValue != null) {
            throw new IllegalArgumentException("duplicate key '" + key + "'");
         } else {
            return oldValue;
         }
      }

      @Override
      public void putAll(Map<? extends K, ? extends V> m) {
         for (Entry<? extends K, ? extends V> e : m.entrySet()) {
            this.put((K)e.getKey(), (V)e.getValue());
         }
      }
   }

   public interface StyleableLookupProvider {
      Lookup getLookupForStyling();
   }

   public interface StyleableUI {
      Map<String, Class<?>> getStyleableInfos(JComponent var1);

      Object getStyleableValue(JComponent var1, String var2);
   }

   public static class UnknownStyleException extends IllegalArgumentException {
      public UnknownStyleException(String key) {
         super(key);
      }

      @Override
      public String getMessage() {
         return "unknown style '" + super.getMessage() + "'";
      }
   }
}
