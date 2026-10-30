package com.formdev.flatlaf.util;

import java.util.Locale;
import java.util.StringTokenizer;

public class SystemInfo {
   public static final boolean isWindows;
   public static final boolean isMacOS;
   public static final boolean isLinux;
   public static final long osVersion = scanVersion(System.getProperty("os.version"));
   public static final boolean isWindows_10_orLater;
   public static final boolean isWindows_11_orLater;
   public static final boolean isMacOS_10_11_ElCapitan_orLater;
   public static final boolean isMacOS_10_14_Mojave_orLater;
   public static final boolean isMacOS_10_15_Catalina_orLater;
   public static final boolean isX86;
   public static final boolean isX86_64;
   public static final boolean isAARCH64;
   public static final long javaVersion = scanVersion(System.getProperty("java.version"));
   public static final boolean isJava_9_orLater = javaVersion >= toVersion(9, 0, 0, 0);
   public static final boolean isJava_11_orLater = javaVersion >= toVersion(11, 0, 0, 0);
   public static final boolean isJava_12_orLater = javaVersion >= toVersion(12, 0, 0, 0);
   public static final boolean isJava_15_orLater = javaVersion >= toVersion(15, 0, 0, 0);
   public static final boolean isJava_17_orLater = javaVersion >= toVersion(17, 0, 0, 0);
   public static final boolean isJava_18_orLater = javaVersion >= toVersion(18, 0, 0, 0);
   public static final boolean isJetBrainsJVM = System.getProperty("java.vm.vendor", "Unknown").toLowerCase(Locale.ENGLISH).contains("jetbrains");
   public static final boolean isJetBrainsJVM_11_orLater = isJetBrainsJVM && isJava_11_orLater;
   public static final boolean isKDE;
   public static final boolean isProjector = Boolean.getBoolean("org.jetbrains.projector.server.enable");
   public static final boolean isWebswing = System.getProperty("webswing.rootDir") != null;
   public static final boolean isWinPE;
   public static final boolean isMacFullWindowContentSupported = javaVersion >= toVersion(11, 0, 8, 0)
      || javaVersion >= toVersion(1, 8, 0, 292) && !isJava_9_orLater;

   public static long scanVersion(String version) {
      int major = 1;
      int minor = 0;
      int micro = 0;
      int patch = 0;

      try {
         StringTokenizer st = new StringTokenizer(version, "._-+");
         major = Integer.parseInt(st.nextToken());
         minor = Integer.parseInt(st.nextToken());
         micro = Integer.parseInt(st.nextToken());
         patch = Integer.parseInt(st.nextToken());
      } catch (Exception var6) {
      }

      return toVersion(major, minor, micro, patch);
   }

   public static long toVersion(int major, int minor, int micro, int patch) {
      return ((long)major << 48) + ((long)minor << 32) + ((long)micro << 16) + patch;
   }

   static {
      String osName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
      isWindows = osName.startsWith("windows");
      isMacOS = osName.startsWith("mac");
      isLinux = osName.startsWith("linux");
      isWindows_10_orLater = isWindows && osVersion >= toVersion(10, 0, 0, 0);
      isWindows_11_orLater = isWindows_10_orLater
         && osName.length() > "windows ".length()
         && scanVersion(osName.substring("windows ".length())) >= toVersion(11, 0, 0, 0);
      isMacOS_10_11_ElCapitan_orLater = isMacOS && osVersion >= toVersion(10, 11, 0, 0);
      isMacOS_10_14_Mojave_orLater = isMacOS && osVersion >= toVersion(10, 14, 0, 0);
      isMacOS_10_15_Catalina_orLater = isMacOS && osVersion >= toVersion(10, 15, 0, 0);
      String osArch = System.getProperty("os.arch");
      isX86 = osArch.equals("x86");
      isX86_64 = osArch.equals("amd64") || osArch.equals("x86_64");
      isAARCH64 = osArch.equals("aarch64");
      isKDE = isLinux && System.getenv("KDE_FULL_SESSION") != null;
      isWinPE = isWindows && "X:\\Windows\\System32".equalsIgnoreCase(System.getProperty("user.dir"));
   }
}
