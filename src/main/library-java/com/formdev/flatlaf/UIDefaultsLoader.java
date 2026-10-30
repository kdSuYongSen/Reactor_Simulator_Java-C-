package com.formdev.flatlaf;

import com.formdev.flatlaf.ui.FlatEmptyBorder;
import com.formdev.flatlaf.ui.FlatLineBorder;
import com.formdev.flatlaf.util.ColorFunctions;
import com.formdev.flatlaf.util.DerivedColor;
import com.formdev.flatlaf.util.GrayFilter;
import com.formdev.flatlaf.util.HSLColor;
import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.SoftCache;
import com.formdev.flatlaf.util.StringUtils;
import com.formdev.flatlaf.util.SystemInfo;
import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.function.Function;
import javax.swing.Icon;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UIDefaults.ActiveValue;
import javax.swing.UIDefaults.LazyValue;
import javax.swing.border.Border;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.plaf.InsetsUIResource;

class UIDefaultsLoader {
   private static final String TYPE_PREFIX = "{";
   private static final String TYPE_PREFIX_END = "}";
   private static final String VARIABLE_PREFIX = "@";
   private static final String PROPERTY_PREFIX = "$";
   private static final String OPTIONAL_PREFIX = "?";
   private static final String WILDCARD_PREFIX = "*.";
   static final String KEY_VARIABLES = "FlatLaf.internal.variables";
   private static int parseColorDepth;
   private static Map<String, ColorUIResource> systemColorCache;
   private static final SoftCache<String, Object> fontCache = new SoftCache<>();
   private static final UIDefaultsLoader.ValueType[] tempResultValueType = new UIDefaultsLoader.ValueType[1];
   private static Map<Class<?>, UIDefaultsLoader.ValueType> javaValueTypes;
   private static Map<String, UIDefaultsLoader.ValueType> knownValueTypes;

   static void loadDefaultsFromProperties(
      Class<?> lookAndFeelClass, List<FlatDefaultsAddon> addons, Properties additionalDefaults, boolean dark, UIDefaults defaults
   ) {
      ArrayList<Class<?>> lafClasses = new ArrayList<>();

      for (Class<?> lafClass = lookAndFeelClass; FlatLaf.class.isAssignableFrom(lafClass); lafClass = lafClass.getSuperclass()) {
         lafClasses.add(0, lafClass);
      }

      loadDefaultsFromProperties(lafClasses, addons, additionalDefaults, dark, defaults);
   }

   static void loadDefaultsFromProperties(
      List<Class<?>> lafClasses, List<FlatDefaultsAddon> addons, Properties additionalDefaults, boolean dark, UIDefaults defaults
   ) {
      try {
         systemColorCache = FlatLaf.getSystemColorGetter() != null ? new HashMap<>() : null;
         Properties properties = new Properties();

         for (Class<?> lafClass : lafClasses) {
            String propertiesName = '/' + lafClass.getName().replace('.', '/') + ".properties";
            InputStream in = lafClass.getResourceAsStream(propertiesName);

            try {
               if (in != null) {
                  properties.load(in);
               }
            } catch (Throwable var31) {
               if (in != null) {
                  try {
                     in.close();
                  } catch (Throwable var23) {
                     var31.addSuppressed(var23);
                  }
               }

               throw var31;
            }

            if (in != null) {
               in.close();
            }
         }

         for (FlatDefaultsAddon addon : addons) {
            for (Class<?> lafClass : lafClasses) {
               InputStream in = addon.getDefaults(lafClass);

               try {
                  if (in != null) {
                     properties.load(in);
                  }
               } catch (Throwable var30) {
                  if (in != null) {
                     try {
                        in.close();
                     } catch (Throwable var22) {
                        var30.addSuppressed(var22);
                     }
                  }

                  throw var30;
               }

               if (in != null) {
                  in.close();
               }
            }
         }

         List<ClassLoader> addonClassLoaders = new ArrayList<>();

         for (FlatDefaultsAddon addon : addons) {
            ClassLoader addonClassLoader = addon.getClass().getClassLoader();
            if (!addonClassLoaders.contains(addonClassLoader)) {
               addonClassLoaders.add(addonClassLoader);
            }
         }

         List<Object> customDefaultsSources = FlatLaf.getCustomDefaultsSources();
         int size = customDefaultsSources != null ? customDefaultsSources.size() : 0;

         for (int i = 0; i < size; i++) {
            Object source = customDefaultsSources.get(i);
            if (source instanceof String && i + 1 < size) {
               String packageName = (String)source;
               ClassLoader classLoader = (ClassLoader)customDefaultsSources.get(++i);
               if (classLoader != null && !addonClassLoaders.contains(classLoader)) {
                  addonClassLoaders.add(classLoader);
               }

               packageName = packageName.replace('.', '/');
               if (classLoader == null) {
                  classLoader = FlatLaf.class.getClassLoader();
               }

               for (Class<?> lafClass : lafClasses) {
                  String propertiesName = packageName + '/' + lafClass.getSimpleName() + ".properties";
                  InputStream in = classLoader.getResourceAsStream(propertiesName);

                  try {
                     if (in != null) {
                        properties.load(in);
                     }
                  } catch (Throwable var29) {
                     if (in != null) {
                        try {
                           in.close();
                        } catch (Throwable var21) {
                           var29.addSuppressed(var21);
                        }
                     }

                     throw var29;
                  }

                  if (in != null) {
                     in.close();
                  }
               }
            } else if (source instanceof URL) {
               URL packageUrl = (URL)source;

               for (Class<?> lafClass : lafClasses) {
                  URL propertiesUrl = new URL(packageUrl + lafClass.getSimpleName() + ".properties");

                  try {
                     InputStream in = propertiesUrl.openStream();

                     try {
                        properties.load(in);
                     } catch (Throwable var27) {
                        if (in != null) {
                           try {
                              in.close();
                           } catch (Throwable var26) {
                              var27.addSuppressed(var26);
                           }
                        }

                        throw var27;
                     }

                     if (in != null) {
                        in.close();
                     }
                  } catch (FileNotFoundException var28) {
                  }
               }
            } else if (source instanceof File) {
               File folder = (File)source;

               for (Class<?> lafClass : lafClasses) {
                  File propertiesFile = new File(folder, lafClass.getSimpleName() + ".properties");
                  if (propertiesFile.isFile()) {
                     InputStream in = new FileInputStream(propertiesFile);

                     try {
                        properties.load(in);
                     } catch (Throwable var25) {
                        try {
                           in.close();
                        } catch (Throwable var20) {
                           var25.addSuppressed(var20);
                        }

                        throw var25;
                     }

                     in.close();
                  }
               }
            }
         }

         if (additionalDefaults != null) {
            properties.putAll(additionalDefaults);
         }

         ArrayList<String> platformSpecificKeys = new ArrayList<>();

         for (Object okey : properties.keySet()) {
            String key = (String)okey;
            if (key.startsWith("[")
               && (key.startsWith("[win]") || key.startsWith("[mac]") || key.startsWith("[linux]") || key.startsWith("[light]") || key.startsWith("[dark]"))) {
               platformSpecificKeys.add(key);
            }
         }

         if (!platformSpecificKeys.isEmpty()) {
            String lightOrDarkPrefix = dark ? "[dark]" : "[light]";

            for (String key : platformSpecificKeys) {
               if (key.startsWith(lightOrDarkPrefix)) {
                  properties.put(key.substring(lightOrDarkPrefix.length()), properties.remove(key));
               }
            }

            String platformPrefix = SystemInfo.isWindows ? "[win]" : (SystemInfo.isMacOS ? "[mac]" : (SystemInfo.isLinux ? "[linux]" : "[unknown]"));

            for (String key : platformSpecificKeys) {
               Object value = properties.remove(key);
               if (key.startsWith(platformPrefix)) {
                  properties.put(key.substring(platformPrefix.length()), value);
               }
            }
         }

         HashMap<String, String> wildcards = new HashMap<>();
         Iterator<Entry<Object, Object>> it = properties.entrySet().iterator();

         while (it.hasNext()) {
            Entry<Object, Object> e = it.next();
            String key = (String)e.getKey();
            if (key.startsWith("*.")) {
               wildcards.put(key.substring("*.".length()), (String)e.getValue());
               it.remove();
            }
         }

         for (Object key : defaults.keySet()) {
            int dot;
            if (key instanceof String && !properties.containsKey(key) && (dot = ((String)key).lastIndexOf(46)) >= 0) {
               String wildcardKey = ((String)key).substring(dot + 1);
               String wildcardValue = wildcards.get(wildcardKey);
               if (wildcardValue != null) {
                  properties.put(key, wildcardValue);
               }
            }
         }

         Function<String, String> propertiesGetter = keyx -> properties.getProperty(keyx);
         Function<String, String> resolver = valuex -> resolveValue(valuex, propertiesGetter);
         Map<String, String> variables = new HashMap<>(50);

         for (Entry<Object, Object> e : properties.entrySet()) {
            String key = (String)e.getKey();
            if (key.startsWith("@")) {
               variables.put(key, (String)e.getValue());
            } else {
               String value = resolveValue((String)e.getValue(), propertiesGetter);

               try {
                  defaults.put(key, parseValue(key, value, null, null, resolver, addonClassLoaders));
               } catch (RuntimeException ex) {
                  logParseError(key, value, ex, true);
               }
            }
         }

         defaults.put("FlatLaf.internal.variables", variables);
         systemColorCache = null;
      } catch (IOException ex) {
         LoggingFacade.INSTANCE.logSevere("FlatLaf: Failed to load properties files.", ex);
      }
   }

   static void logParseError(String key, String value, RuntimeException ex, boolean severe) {
      String message = "FlatLaf: Failed to parse: '" + key + '=' + value + '\'';
      if (severe) {
         LoggingFacade.INSTANCE.logSevere(message, ex);
      } else {
         LoggingFacade.INSTANCE.logConfig(message, ex);
      }
   }

   static String resolveValue(String value, Function<String, String> propertiesGetter) {
      value = value.trim();
      String value0 = value;
      if (value.startsWith("$")) {
         value = value.substring("$".length());
      } else if (!value.startsWith("@")) {
         return value;
      }

      boolean optional = false;
      if (value.startsWith("?")) {
         value = value.substring("?".length());
         optional = true;
      }

      String newValue = propertiesGetter.apply(value);
      if (newValue == null) {
         if (optional) {
            return "null";
         } else {
            throw new IllegalArgumentException("variable or property '" + value + "' not found");
         }
      } else if (newValue.equals(value0)) {
         throw new IllegalArgumentException("endless recursion in variable or property '" + value + "'");
      } else {
         return resolveValue(newValue, propertiesGetter);
      }
   }

   static String resolveValueFromUIManager(String value) {
      if (value.startsWith("@")) {
         Map<String, String> variables = (Map<String, String>)UIManager.get("FlatLaf.internal.variables");
         String newValue = variables != null ? variables.get(value) : null;
         if (newValue == null) {
            throw new IllegalArgumentException("variable '" + value + "' not found");
         } else {
            return newValue;
         }
      } else if (!value.startsWith("$")) {
         return value;
      } else {
         String key = value.substring("$".length());
         Object newValue = UIManager.get(key);
         if (newValue == null) {
            throw new IllegalArgumentException("property '" + key + "' not found");
         } else if (newValue instanceof Color) {
            Color color = (Color)newValue;
            int alpha = color.getAlpha();
            return String.format(alpha != 255 ? "#%06x%02x" : "#%06x", color.getRGB() & 16777215, alpha);
         } else {
            throw new IllegalArgumentException("property value type '" + newValue.getClass().getName() + "' not supported in references");
         }
      }
   }

   static Object parseValue(String key, String value, Class<?> valueType) {
      return parseValue(key, value, valueType, null, v -> v, Collections.emptyList());
   }

   static Object parseValue(
      String key,
      String value,
      Class<?> javaValueType,
      UIDefaultsLoader.ValueType[] resultValueType,
      Function<String, String> resolver,
      List<ClassLoader> addonClassLoaders
   ) {
      if (resultValueType == null) {
         resultValueType = tempResultValueType;
      }

      if (key.startsWith("[style]")) {
         resultValueType[0] = UIDefaultsLoader.ValueType.STRING;
         return value;
      }

      value = value.trim();
      if (value.equals("null") || value.isEmpty()) {
         resultValueType[0] = UIDefaultsLoader.ValueType.NULL;
         return null;
      }

      if (value.startsWith("if(") && value.endsWith(")")) {
         List<String> params = splitFunctionParams(value.substring(3, value.length() - 1), ',');
         if (params.size() != 3) {
            throwMissingParametersException(value);
         }

         boolean ifCondition = parseCondition(params.get(0), resolver, addonClassLoaders);
         String ifValue = params.get(ifCondition ? 1 : 2);
         return parseValue(key, resolver.apply(ifValue), javaValueType, resultValueType, resolver, addonClassLoaders);
      } else {
         UIDefaultsLoader.ValueType valueType = UIDefaultsLoader.ValueType.UNKNOWN;
         if (javaValueType != null) {
            if (javaValueTypes == null) {
               javaValueTypes = new HashMap<>();
               javaValueTypes.put(String.class, UIDefaultsLoader.ValueType.STRING);
               javaValueTypes.put(boolean.class, UIDefaultsLoader.ValueType.BOOLEAN);
               javaValueTypes.put(Boolean.class, UIDefaultsLoader.ValueType.BOOLEAN);
               javaValueTypes.put(char.class, UIDefaultsLoader.ValueType.CHARACTER);
               javaValueTypes.put(Character.class, UIDefaultsLoader.ValueType.CHARACTER);
               javaValueTypes.put(int.class, UIDefaultsLoader.ValueType.INTEGER);
               javaValueTypes.put(Integer.class, UIDefaultsLoader.ValueType.INTEGER);
               javaValueTypes.put(float.class, UIDefaultsLoader.ValueType.FLOAT);
               javaValueTypes.put(Float.class, UIDefaultsLoader.ValueType.FLOAT);
               javaValueTypes.put(Border.class, UIDefaultsLoader.ValueType.BORDER);
               javaValueTypes.put(Icon.class, UIDefaultsLoader.ValueType.ICON);
               javaValueTypes.put(Insets.class, UIDefaultsLoader.ValueType.INSETS);
               javaValueTypes.put(Dimension.class, UIDefaultsLoader.ValueType.DIMENSION);
               javaValueTypes.put(Color.class, UIDefaultsLoader.ValueType.COLOR);
               javaValueTypes.put(Font.class, UIDefaultsLoader.ValueType.FONT);
            }

            valueType = javaValueTypes.get(javaValueType);
            if (valueType == null) {
               throw new IllegalArgumentException("unsupported value type '" + javaValueType.getName() + "'");
            }

            if (valueType == UIDefaultsLoader.ValueType.STRING && value.startsWith("\"") && value.endsWith("\"")) {
               value = value.substring(1, value.length() - 1);
            }
         } else {
            switch (value) {
               case "false":
                  resultValueType[0] = UIDefaultsLoader.ValueType.BOOLEAN;
                  return false;
               case "true":
                  resultValueType[0] = UIDefaultsLoader.ValueType.BOOLEAN;
                  return true;
            }

            if (value.startsWith("lazy(") && value.endsWith(")")) {
               resultValueType[0] = UIDefaultsLoader.ValueType.LAZY;
               String uiKey = StringUtils.substringTrimmed(value, 5, value.length() - 1);
               return (LazyValue)t -> lazyUIManagerGet(uiKey);
            }

            if (value.startsWith("#")) {
               valueType = UIDefaultsLoader.ValueType.COLOR;
            } else if (value.startsWith("{")) {
               int end = value.indexOf("}");
               if (end != -1) {
                  try {
                     String typeStr = value.substring("{".length(), end);
                     valueType = UIDefaultsLoader.ValueType.valueOf(typeStr.toUpperCase(Locale.ENGLISH));
                     value = value.substring(end + "}".length());
                  } catch (IllegalArgumentException var9) {
                  }
               }
            }

            if (valueType == UIDefaultsLoader.ValueType.UNKNOWN) {
               if (knownValueTypes == null) {
                  knownValueTypes = new HashMap<>();
                  knownValueTypes.put("activeCaptionBorder", UIDefaultsLoader.ValueType.COLOR);
                  knownValueTypes.put("inactiveCaptionBorder", UIDefaultsLoader.ValueType.COLOR);
                  knownValueTypes.put("windowBorder", UIDefaultsLoader.ValueType.COLOR);
                  knownValueTypes.put("SplitPane.dividerSize", UIDefaultsLoader.ValueType.INTEGER);
                  knownValueTypes.put("SplitPaneDivider.gripDotSize", UIDefaultsLoader.ValueType.INTEGER);
                  knownValueTypes.put("dividerSize", UIDefaultsLoader.ValueType.INTEGER);
                  knownValueTypes.put("gripDotSize", UIDefaultsLoader.ValueType.INTEGER);
                  knownValueTypes.put("TabbedPane.closeCrossPlainSize", UIDefaultsLoader.ValueType.FLOAT);
                  knownValueTypes.put("TabbedPane.closeCrossFilledSize", UIDefaultsLoader.ValueType.FLOAT);
                  knownValueTypes.put("closeCrossPlainSize", UIDefaultsLoader.ValueType.FLOAT);
                  knownValueTypes.put("closeCrossFilledSize", UIDefaultsLoader.ValueType.FLOAT);
                  knownValueTypes.put("Table.intercellSpacing", UIDefaultsLoader.ValueType.DIMENSION);
                  knownValueTypes.put("intercellSpacing", UIDefaultsLoader.ValueType.DIMENSION);
               }

               valueType = knownValueTypes.getOrDefault(key, UIDefaultsLoader.ValueType.UNKNOWN);
            }

            if (valueType == UIDefaultsLoader.ValueType.UNKNOWN) {
               if (key.endsWith("UI")) {
                  valueType = UIDefaultsLoader.ValueType.STRING;
               } else if (!key.endsWith("Color")
                  && (
                     !key.endsWith("ground")
                        || !key.endsWith(".background")
                           && !key.endsWith("Background")
                           && !key.equals("background")
                           && !key.endsWith(".foreground")
                           && !key.endsWith("Foreground")
                           && !key.equals("foreground")
                  )) {
                  if (key.endsWith(".font") || key.endsWith("Font") || key.equals("font")) {
                     valueType = UIDefaultsLoader.ValueType.FONT;
                  } else if (key.endsWith(".border") || key.endsWith("Border") || key.equals("border")) {
                     valueType = UIDefaultsLoader.ValueType.BORDER;
                  } else if (key.endsWith(".icon") || key.endsWith("Icon") || key.equals("icon")) {
                     valueType = UIDefaultsLoader.ValueType.ICON;
                  } else if (key.endsWith(".margin")
                     || key.equals("margin")
                     || key.endsWith(".padding")
                     || key.equals("padding")
                     || key.endsWith("Margins")
                     || key.endsWith("Insets")) {
                     valueType = UIDefaultsLoader.ValueType.INSETS;
                  } else if (key.endsWith("Size")) {
                     valueType = UIDefaultsLoader.ValueType.DIMENSION;
                  } else if (key.endsWith("Width") || key.endsWith("Height")) {
                     valueType = UIDefaultsLoader.ValueType.INTEGERORFLOAT;
                  } else if (key.endsWith("Char")) {
                     valueType = UIDefaultsLoader.ValueType.CHARACTER;
                  } else if (key.endsWith("grayFilter")) {
                     valueType = UIDefaultsLoader.ValueType.GRAYFILTER;
                  }
               } else {
                  valueType = UIDefaultsLoader.ValueType.COLOR;
               }
            }
         }

         resultValueType[0] = valueType;
         switch (valueType) {
            case STRING:
               return value;
            case BOOLEAN:
               return parseBoolean(value);
            case CHARACTER:
               return parseCharacter(value);
            case INTEGER:
               return parseInteger(value);
            case INTEGERORFLOAT:
               return parseIntegerOrFloat(value);
            case FLOAT:
               return parseFloat(value);
            case BORDER:
               return parseBorder(value, resolver, addonClassLoaders);
            case ICON:
               return parseInstance(value, addonClassLoaders);
            case INSETS:
               return parseInsets(value);
            case DIMENSION:
               return parseDimension(value);
            case COLOR:
               return parseColorOrFunction(value, resolver);
            case FONT:
               return parseFont(value);
            case SCALEDINTEGER:
               return parseScaledInteger(value);
            case SCALEDFLOAT:
               return parseScaledFloat(value);
            case SCALEDINSETS:
               return parseScaledInsets(value);
            case SCALEDDIMENSION:
               return parseScaledDimension(value);
            case INSTANCE:
               return parseInstance(value, addonClassLoaders);
            case CLASS:
               return parseClass(value, addonClassLoaders);
            case GRAYFILTER:
               return parseGrayFilter(value);
            case UNKNOWN:
            default:
               if (value.startsWith("\"") && value.endsWith("\"")) {
                  resultValueType[0] = UIDefaultsLoader.ValueType.STRING;
                  return value.substring(1, value.length() - 1);
               } else if (!value.startsWith("#") && !value.endsWith(")")) {
                  char firstChar = value.charAt(0);
                  if (firstChar >= '0' && firstChar <= '9' || firstChar == '-' || firstChar == '+' || firstChar == '.') {
                     try {
                        Integer integer = parseInteger(value);
                        resultValueType[0] = UIDefaultsLoader.ValueType.INTEGER;
                        return integer;
                     } catch (NumberFormatException var11) {
                        try {
                           Float f = parseFloat(value);
                           resultValueType[0] = UIDefaultsLoader.ValueType.FLOAT;
                           return f;
                        } catch (NumberFormatException var10) {
                        }
                     }
                  }

                  resultValueType[0] = UIDefaultsLoader.ValueType.STRING;
                  return value;
               } else {
                  Object color = parseColorOrFunction(value, resolver);
                  resultValueType[0] = color != null ? UIDefaultsLoader.ValueType.COLOR : UIDefaultsLoader.ValueType.NULL;
                  return color;
               }
         }
      }
   }

   private static boolean parseCondition(String condition, Function<String, String> resolver, List<ClassLoader> addonClassLoaders) {
      try {
         Object conditionValue = parseValue("", resolver.apply(condition), null, null, resolver, addonClassLoaders);
         return conditionValue != null && !conditionValue.equals(false) && !conditionValue.equals(0);
      } catch (IllegalArgumentException ex) {
         return false;
      }
   }

   private static Object parseBorder(String value, Function<String, String> resolver, List<ClassLoader> addonClassLoaders) {
      if (value.indexOf(44) >= 0) {
         List<String> parts = splitFunctionParams(value, ',');
         Insets insets = parseInsets(value);
         ColorUIResource lineColor = parts.size() >= 5 ? (ColorUIResource)parseColorOrFunction(resolver.apply(parts.get(4)), resolver) : null;
         float lineThickness = parts.size() >= 6 && !parts.get(5).isEmpty() ? parseFloat(parts.get(5)) : 1.0F;
         int arc = parts.size() >= 7 ? parseInteger(parts.get(6)) : 0;
         return (LazyValue)t -> lineColor != null ? new FlatLineBorder(insets, lineColor, lineThickness, arc) : new FlatEmptyBorder(insets);
      } else {
         return parseInstance(value, addonClassLoaders);
      }
   }

   private static Object parseInstance(String value, List<ClassLoader> addonClassLoaders) {
      return (LazyValue)t -> {
         try {
            return findClass(value, addonClassLoaders).getDeclaredConstructor().newInstance();
         } catch (Exception ex) {
            LoggingFacade.INSTANCE.logSevere("FlatLaf: Failed to instantiate '" + value + "'.", ex);
            return null;
         }
      };
   }

   private static Object parseClass(String value, List<ClassLoader> addonClassLoaders) {
      return (LazyValue)t -> {
         try {
            return findClass(value, addonClassLoaders);
         } catch (ClassNotFoundException ex) {
            LoggingFacade.INSTANCE.logSevere("FlatLaf: Failed to find class '" + value + "'.", ex);
            return null;
         }
      };
   }

   private static Class<?> findClass(String className, List<ClassLoader> addonClassLoaders) throws ClassNotFoundException {
      try {
         return Class.forName(className);
      } catch (ClassNotFoundException ex) {
         for (ClassLoader addonClassLoader : addonClassLoaders) {
            try {
               return addonClassLoader.loadClass(className);
            } catch (ClassNotFoundException var6) {
            }
         }

         throw ex;
      }
   }

   private static Insets parseInsets(String value) {
      List<String> numbers = StringUtils.split(value, ',', true, false);

      try {
         return new InsetsUIResource(
            Integer.parseInt(numbers.get(0)), Integer.parseInt(numbers.get(1)), Integer.parseInt(numbers.get(2)), Integer.parseInt(numbers.get(3))
         );
      } catch (NumberFormatException ex) {
         throw new IllegalArgumentException("invalid insets '" + value + "'");
      }
   }

   private static Dimension parseDimension(String value) {
      List<String> numbers = StringUtils.split(value, ',', true, false);

      try {
         return new DimensionUIResource(Integer.parseInt(numbers.get(0)), Integer.parseInt(numbers.get(1)));
      } catch (NumberFormatException ex) {
         throw new IllegalArgumentException("invalid size '" + value + "'");
      }
   }

   private static Object parseColorOrFunction(String value, Function<String, String> resolver) {
      return value.endsWith(")") ? parseColorFunctions(value, resolver) : parseColor(value);
   }

   static ColorUIResource parseColor(String value) {
      int rgba = parseColorRGBA(value);
      return (rgba & 0xFF000000) == -16777216 ? new ColorUIResource(rgba) : new ColorUIResource(new Color(rgba, true));
   }

   static int parseColorRGBA(String value) {
      int len = value.length();
      if ((len == 4 || len == 5 || len == 7 || len == 9) && value.charAt(0) == '#') {
         int n = 0;

         for (int i = 1; i < len; i++) {
            char ch = value.charAt(i);
            int digit;
            if (ch >= '0' && ch <= '9') {
               digit = ch - '0';
            } else if (ch >= 'a' && ch <= 'f') {
               digit = ch - 'a' + 10;
            } else {
               if (ch < 'A' || ch > 'F') {
                  throw newInvalidColorException(value);
               }

               digit = ch - 'A' + 10;
            }

            n = n << 4 | digit;
         }

         if (len <= 5) {
            int n1 = n & 61440;
            int n2 = n & 3840;
            int n3 = n & 240;
            int n4 = n & 15;
            n = n1 << 16 | n1 << 12 | n2 << 12 | n2 << 8 | n3 << 8 | n3 << 4 | n4 << 4 | n4;
         }

         return len != 4 && len != 7 ? n >> 8 & 16777215 | (n & 0xFF) << 24 : 0xFF000000 | n;
      } else {
         throw newInvalidColorException(value);
      }
   }

   private static IllegalArgumentException newInvalidColorException(String value) {
      return new IllegalArgumentException("invalid color '" + value + "'");
   }

   private static Object parseColorFunctions(String value, Function<String, String> resolver) {
      int paramsStart = value.indexOf(40);
      if (paramsStart < 0) {
         throw new IllegalArgumentException("missing opening parenthesis in function '" + value + "'");
      }

      String function = StringUtils.substringTrimmed(value, 0, paramsStart);
      List<String> params = splitFunctionParams(value.substring(paramsStart + 1, value.length() - 1), ',');
      if (params.isEmpty()) {
         throwMissingParametersException(value);
      }

      if (parseColorDepth > 100) {
         throw new IllegalArgumentException("endless recursion in color function '" + value + "'");
      }

      parseColorDepth++;

      try {
         switch (function) {
            case "if":
               return parseColorIf(value, params, resolver);
            case "systemColor":
               return parseColorSystemColor(value, params, resolver);
            case "rgb":
               return parseColorRgbOrRgba(false, params, resolver);
            case "rgba":
               return parseColorRgbOrRgba(true, params, resolver);
            case "hsl":
               return parseColorHslOrHsla(false, params);
            case "hsla":
               return parseColorHslOrHsla(true, params);
            case "lighten":
               return parseColorHSLIncreaseDecrease(2, true, params, resolver);
            case "darken":
               return parseColorHSLIncreaseDecrease(2, false, params, resolver);
            case "saturate":
               return parseColorHSLIncreaseDecrease(1, true, params, resolver);
            case "desaturate":
               return parseColorHSLIncreaseDecrease(1, false, params, resolver);
            case "fadein":
               return parseColorHSLIncreaseDecrease(3, true, params, resolver);
            case "fadeout":
               return parseColorHSLIncreaseDecrease(3, false, params, resolver);
            case "fade":
               return parseColorFade(params, resolver);
            case "spin":
               return parseColorSpin(params, resolver);
            case "changeHue":
               return parseColorChange(0, params, resolver);
            case "changeSaturation":
               return parseColorChange(1, params, resolver);
            case "changeLightness":
               return parseColorChange(2, params, resolver);
            case "changeAlpha":
               return parseColorChange(3, params, resolver);
            case "mix":
               return parseColorMix(null, params, resolver);
            case "tint":
               return parseColorMix("#fff", params, resolver);
            case "shade":
               return parseColorMix("#000", params, resolver);
            case "contrast":
               return parseColorContrast(params, resolver);
            case "over":
               return parseColorOver(params, resolver);
         }
      } finally {
         parseColorDepth--;
      }

      throw new IllegalArgumentException("unknown color function '" + value + "'");
   }

   private static Object parseColorIf(String value, List<String> params, Function<String, String> resolver) {
      if (params.size() != 3) {
         throwMissingParametersException(value);
      }

      boolean ifCondition = parseCondition(params.get(0), resolver, Collections.emptyList());
      String ifValue = params.get(ifCondition ? 1 : 2);
      return parseColorOrFunction(resolver.apply(ifValue), resolver);
   }

   private static Object parseColorSystemColor(String value, List<String> params, Function<String, String> resolver) {
      if (params.size() < 1) {
         throwMissingParametersException(value);
      }

      ColorUIResource systemColor = getSystemColor(params.get(0));
      if (systemColor != null) {
         return systemColor;
      }

      String defaultValue = params.size() > 1 ? params.get(1) : "";
      return !defaultValue.equals("null") && !defaultValue.isEmpty() ? parseColorOrFunction(resolver.apply(defaultValue), resolver) : null;
   }

   private static ColorUIResource getSystemColor(String name) {
      Function<String, Color> systemColorGetter = FlatLaf.getSystemColorGetter();
      if (systemColorGetter == null) {
         return null;
      }

      if (systemColorCache != null && systemColorCache.containsKey(name)) {
         return systemColorCache.get(name);
      }

      Color color = systemColorGetter.apply(name);
      ColorUIResource uiColor = color != null ? new ColorUIResource(color) : null;
      if (systemColorCache != null) {
         systemColorCache.put(name, uiColor);
      }

      return uiColor;
   }

   private static ColorUIResource parseColorRgbOrRgba(boolean hasAlpha, List<String> params, Function<String, String> resolver) {
      if (hasAlpha && params.size() == 2) {
         String colorStr = params.get(0);
         int alpha = parseInteger(params.get(1), 0, 255, true);
         ColorUIResource color = (ColorUIResource)parseColorOrFunction(resolver.apply(colorStr), resolver);
         return new ColorUIResource(new Color((alpha & 0xFF) << 24 | color.getRGB() & 16777215, true));
      } else {
         int red = parseInteger(params.get(0), 0, 255, true);
         int green = parseInteger(params.get(1), 0, 255, true);
         int blue = parseInteger(params.get(2), 0, 255, true);
         int alpha = hasAlpha ? parseInteger(params.get(3), 0, 255, true) : 255;
         return hasAlpha ? new ColorUIResource(new Color(red, green, blue, alpha)) : new ColorUIResource(red, green, blue);
      }
   }

   private static ColorUIResource parseColorHslOrHsla(boolean hasAlpha, List<String> params) {
      int hue = parseInteger(params.get(0), 0, 360, false);
      int saturation = parsePercentage(params.get(1));
      int lightness = parsePercentage(params.get(2));
      int alpha = hasAlpha ? parsePercentage(params.get(3)) : 100;
      float[] hsl = new float[]{hue, saturation, lightness};
      return new ColorUIResource(HSLColor.toRGB(hsl, alpha / 100.0F));
   }

   private static Object parseColorHSLIncreaseDecrease(int hslIndex, boolean increase, List<String> params, Function<String, String> resolver) {
      String colorStr = params.get(0);
      int amount = parsePercentage(params.get(1));
      boolean relative = false;
      boolean autoInverse = false;
      boolean lazy = false;
      boolean derived = false;
      if (params.size() > 2) {
         String options = params.get(2);
         relative = options.contains("relative");
         autoInverse = options.contains("autoInverse");
         lazy = options.contains("lazy");
         derived = options.contains("derived");
         if (derived && !options.contains("noAutoInverse")) {
            autoInverse = true;
         }
      }

      ColorFunctions.ColorFunction function = new ColorFunctions.HSLIncreaseDecrease(hslIndex, increase, amount, relative, autoInverse);
      return lazy ? (LazyValue)(t -> {
         Object color = lazyUIManagerGet(colorStr);
         return color instanceof Color ? new ColorUIResource(ColorFunctions.applyFunctions((Color)color, function)) : null;
      }) : parseFunctionBaseColor(colorStr, function, derived, resolver);
   }

   private static Object parseColorFade(List<String> params, Function<String, String> resolver) {
      String colorStr = params.get(0);
      int amount = parsePercentage(params.get(1));
      boolean derived = false;
      boolean lazy = false;
      if (params.size() > 2) {
         String options = params.get(2);
         derived = options.contains("derived");
         lazy = options.contains("lazy");
      }

      ColorFunctions.ColorFunction function = new ColorFunctions.Fade(amount);
      return lazy ? (LazyValue)(t -> {
         Object color = lazyUIManagerGet(colorStr);
         return color instanceof Color ? new ColorUIResource(ColorFunctions.applyFunctions((Color)color, function)) : null;
      }) : parseFunctionBaseColor(colorStr, function, derived, resolver);
   }

   private static Object parseColorSpin(List<String> params, Function<String, String> resolver) {
      String colorStr = params.get(0);
      int amount = parseInteger(params.get(1));
      boolean derived = false;
      if (params.size() > 2) {
         String options = params.get(2);
         derived = options.contains("derived");
      }

      ColorFunctions.ColorFunction function = new ColorFunctions.HSLIncreaseDecrease(0, true, amount, false, false);
      return parseFunctionBaseColor(colorStr, function, derived, resolver);
   }

   private static Object parseColorChange(int hslIndex, List<String> params, Function<String, String> resolver) {
      String colorStr = params.get(0);
      int value = hslIndex == 0 ? parseInteger(params.get(1)) : parsePercentage(params.get(1));
      boolean derived = false;
      if (params.size() > 2) {
         String options = params.get(2);
         derived = options.contains("derived");
      }

      ColorFunctions.ColorFunction function = new ColorFunctions.HSLChange(hslIndex, value);
      return parseFunctionBaseColor(colorStr, function, derived, resolver);
   }

   private static Object parseColorMix(String color1Str, List<String> params, Function<String, String> resolver) {
      int i = 0;
      if (color1Str == null) {
         color1Str = params.get(i++);
      }

      String color2Str = params.get(i++);
      int weight = params.size() > i ? parsePercentage(params.get(i)) : 50;
      ColorUIResource color2 = (ColorUIResource)parseColorOrFunction(resolver.apply(color2Str), resolver);
      if (color2 == null) {
         return null;
      }

      ColorFunctions.ColorFunction function = new ColorFunctions.Mix(color2, weight);
      return parseFunctionBaseColor(color1Str, function, false, resolver);
   }

   private static Object parseColorContrast(List<String> params, Function<String, String> resolver) {
      String colorStr = params.get(0);
      String darkStr = params.get(1);
      String lightStr = params.get(2);
      int threshold = params.size() > 3 ? parsePercentage(params.get(3)) : 43;
      ColorUIResource color = (ColorUIResource)parseColorOrFunction(resolver.apply(colorStr), resolver);
      if (color == null) {
         return null;
      }

      String darkOrLightColor = ColorFunctions.luma(color) * 100.0F < threshold ? lightStr : darkStr;
      return parseColorOrFunction(resolver.apply(darkOrLightColor), resolver);
   }

   private static ColorUIResource parseColorOver(List<String> params, Function<String, String> resolver) {
      String foregroundStr = params.get(0);
      String backgroundStr = params.get(1);
      ColorUIResource foreground = (ColorUIResource)parseColorOrFunction(resolver.apply(foregroundStr), resolver);
      if (foreground != null && foreground.getAlpha() != 255) {
         ColorUIResource foreground2 = new ColorUIResource(foreground.getRGB());
         ColorUIResource background = (ColorUIResource)parseColorOrFunction(resolver.apply(backgroundStr), resolver);
         if (background == null) {
            return foreground2;
         }

         float weight = foreground.getAlpha() / 255.0F;
         return new ColorUIResource(ColorFunctions.mix(foreground2, background, weight));
      } else {
         return foreground;
      }
   }

   private static Object parseFunctionBaseColor(String colorStr, ColorFunctions.ColorFunction function, boolean derived, Function<String, String> resolver) {
      String resolvedColorStr = resolver.apply(colorStr);
      ColorUIResource baseColor = (ColorUIResource)parseColorOrFunction(resolvedColorStr, resolver);
      if (baseColor == null) {
         return null;
      }

      Color newColor = ColorFunctions.applyFunctions(baseColor, function);
      if (!derived) {
         return new ColorUIResource(newColor);
      }

      ColorFunctions.ColorFunction[] functions;
      if (baseColor instanceof DerivedColor && resolvedColorStr == colorStr) {
         ColorFunctions.ColorFunction[] baseFunctions = ((DerivedColor)baseColor).getFunctions();
         functions = new ColorFunctions.ColorFunction[baseFunctions.length + 1];
         System.arraycopy(baseFunctions, 0, functions, 0, baseFunctions.length);
         functions[baseFunctions.length] = function;
      } else {
         functions = new ColorFunctions.ColorFunction[]{function};
      }

      return new DerivedColor(newColor, functions);
   }

   private static Object parseFont(String value) {
      Object font = fontCache.get(value);
      if (font != null) {
         return font;
      }

      int style = -1;
      int styleChange = 0;
      int absoluteSize = 0;
      int relativeSize = 0;
      float scaleSize = 0.0F;
      List<String> families = null;
      String baseFontKey = null;
      StreamTokenizer st = new StreamTokenizer(new StringReader(value));
      st.resetSyntax();
      st.wordChars(33, 255);
      st.whitespaceChars(0, 32);
      st.whitespaceChars(44, 44);
      st.quoteChar(34);
      st.quoteChar(39);

      try {
         while (st.nextToken() != -1) {
            String param = st.sval;
            switch (param) {
               case "normal":
                  style = 0;
                  break;
               case "bold":
                  if (style == -1) {
                     style = 0;
                  }

                  style |= 1;
                  break;
               case "italic":
                  if (style == -1) {
                     style = 0;
                  }

                  style |= 2;
                  break;
               case "+bold":
                  styleChange |= 1;
                  break;
               case "-bold":
                  styleChange |= 65536;
                  break;
               case "+italic":
                  styleChange |= 2;
                  break;
               case "-italic":
                  styleChange |= 131072;
                  break;
               default:
                  char firstChar = param.charAt(0);
                  if (!Character.isDigit(firstChar) && firstChar != '+' && firstChar != '-') {
                     if (firstChar == '$') {
                        if (baseFontKey != null) {
                           throw new IllegalArgumentException("baseFontKey specified more than once in '" + value + "'");
                        }

                        baseFontKey = param.substring(1);
                     } else if (families == null) {
                        families = Collections.singletonList(param);
                     } else {
                        if (families.size() == 1) {
                           families = new ArrayList<>(families);
                        }

                        families.add(param);
                     }
                  } else {
                     if (absoluteSize != 0 || relativeSize != 0 || scaleSize != 0.0F) {
                        throw new IllegalArgumentException("size specified more than once in '" + value + "'");
                     }

                     if (firstChar == '+' || firstChar == '-') {
                        relativeSize = parseInteger(param);
                     } else if (param.endsWith("%")) {
                        scaleSize = parseInteger(param.substring(0, param.length() - 1)).intValue() / 100.0F;
                     } else {
                        absoluteSize = parseInteger(param);
                     }
                  }
            }
         }
      } catch (IOException ex) {
         throw new IllegalArgumentException(ex);
      }

      if (style != -1 && styleChange != 0) {
         throw new IllegalArgumentException("can not mix absolute style (e.g. 'bold') with derived style (e.g. '+italic') in '" + value + "'");
      }

      if (styleChange != 0) {
         if ((styleChange & 1) != 0 && (styleChange & 65536) != 0) {
            throw new IllegalArgumentException("can not use '+bold' and '-bold' in '" + value + "'");
         }

         if ((styleChange & 2) != 0 && (styleChange & 131072) != 0) {
            throw new IllegalArgumentException("can not use '+italic' and '-italic' in '" + value + "'");
         }
      }

      font = new FlatLaf.ActiveFont(baseFontKey, families, style, styleChange, absoluteSize, relativeSize, scaleSize);
      fontCache.put(value, font);
      return font;
   }

   private static int parsePercentage(String value) {
      if (!value.endsWith("%")) {
         throw new NumberFormatException("invalid percentage '" + value + "'");
      }

      int val;
      try {
         val = Integer.parseInt(value.substring(0, value.length() - 1));
      } catch (NumberFormatException ex) {
         throw new NumberFormatException("invalid percentage '" + value + "'");
      }

      if (val >= 0 && val <= 100) {
         return val;
      } else {
         throw new IllegalArgumentException("percentage out of range (0-100%) '" + value + "'");
      }
   }

   private static Boolean parseBoolean(String value) {
      switch (value) {
         case "false":
            return false;
         case "true":
            return true;
         default:
            throw new IllegalArgumentException("invalid boolean '" + value + "'");
      }
   }

   private static Character parseCharacter(String value) {
      if (value.length() != 1) {
         throw new IllegalArgumentException("invalid character '" + value + "'");
      } else {
         return value.charAt(0);
      }
   }

   private static Integer parseInteger(String value, int min, int max, boolean allowPercentage) {
      if (allowPercentage && value.endsWith("%")) {
         int percent = parsePercentage(value);
         return max * percent / 100;
      } else {
         Integer integer = parseInteger(value);
         if (integer >= min && integer <= max) {
            return integer;
         } else {
            throw new NumberFormatException("integer '" + value + "' out of range (" + min + 45 + max + 41);
         }
      }
   }

   private static Integer parseInteger(String value) {
      try {
         return Integer.parseInt(value);
      } catch (NumberFormatException ex) {
         throw new NumberFormatException("invalid integer '" + value + "'");
      }
   }

   private static Number parseIntegerOrFloat(String value) {
      try {
         return Integer.parseInt(value);
      } catch (NumberFormatException ex) {
         try {
            return Float.parseFloat(value);
         } catch (NumberFormatException ex2) {
            throw new NumberFormatException("invalid integer or float '" + value + "'");
         }
      }
   }

   private static Float parseFloat(String value) {
      try {
         return Float.parseFloat(value);
      } catch (NumberFormatException ex) {
         throw new NumberFormatException("invalid float '" + value + "'");
      }
   }

   private static ActiveValue parseScaledInteger(String value) {
      int val = parseInteger(value);
      return t -> UIScale.scale(val);
   }

   private static ActiveValue parseScaledFloat(String value) {
      float val = parseFloat(value);
      return t -> UIScale.scale(val);
   }

   private static ActiveValue parseScaledInsets(String value) {
      Insets insets = parseInsets(value);
      return t -> UIScale.scale(insets);
   }

   private static ActiveValue parseScaledDimension(String value) {
      Dimension dimension = parseDimension(value);
      return t -> UIScale.scale(dimension);
   }

   private static Object parseGrayFilter(String value) {
      List<String> numbers = StringUtils.split(value, ',', true, false);

      try {
         int brightness = Integer.parseInt(numbers.get(0));
         int contrast = Integer.parseInt(numbers.get(1));
         int alpha = Integer.parseInt(numbers.get(2));
         return (LazyValue)t -> new GrayFilter(brightness, contrast, alpha);
      } catch (NumberFormatException ex) {
         throw new IllegalArgumentException("invalid gray filter '" + value + "'");
      }
   }

   private static List<String> splitFunctionParams(String str, char delim) {
      ArrayList<String> strs = new ArrayList<>();
      int nestLevel = 0;
      int start = 0;
      int strlen = str.length();

      for (int i = 0; i < strlen; i++) {
         char ch = str.charAt(i);
         if (ch == '(') {
            nestLevel++;
         } else if (ch == ')') {
            nestLevel--;
         } else if (nestLevel == 0 && ch == delim) {
            strs.add(StringUtils.substringTrimmed(str, start, i));
            start = i + 1;
         }
      }

      String s = StringUtils.substringTrimmed(str, start);
      if (!s.isEmpty() || !strs.isEmpty()) {
         strs.add(s);
      }

      return strs;
   }

   static Object lazyUIManagerGet(String uiKey) {
      boolean optional = false;
      if (uiKey.startsWith("?")) {
         uiKey = uiKey.substring("?".length());
         optional = true;
      }

      Object value = UIManager.get(uiKey);
      if (value == null && !optional) {
         LoggingFacade.INSTANCE.logSevere("FlatLaf: '" + uiKey + "' not found in UI defaults.", null);
      }

      return value;
   }

   private static void throwMissingParametersException(String value) {
      throw new IllegalArgumentException("missing parameters in function '" + value + "'");
   }

   enum ValueType {
      UNKNOWN,
      STRING,
      BOOLEAN,
      CHARACTER,
      INTEGER,
      INTEGERORFLOAT,
      FLOAT,
      BORDER,
      ICON,
      INSETS,
      DIMENSION,
      COLOR,
      FONT,
      SCALEDINTEGER,
      SCALEDFLOAT,
      SCALEDINSETS,
      SCALEDDIMENSION,
      INSTANCE,
      CLASS,
      GRAYFILTER,
      NULL,
      LAZY;
   }
}
