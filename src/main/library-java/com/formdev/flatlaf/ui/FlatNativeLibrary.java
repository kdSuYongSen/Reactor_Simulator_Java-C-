package com.formdev.flatlaf.ui;

import com.formdev.flatlaf.util.LoggingFacade;
import com.formdev.flatlaf.util.NativeLibrary;
import com.formdev.flatlaf.util.SystemInfo;
import java.io.File;

class FlatNativeLibrary {
   private static NativeLibrary nativeLibrary;

   static synchronized boolean isLoaded() {
      initialize();
      return nativeLibrary != null ? nativeLibrary.isLoaded() : false;
   }

   private static void initialize() {
      if (nativeLibrary == null) {
         String libraryName;
         if (!SystemInfo.isWindows_10_orLater || !SystemInfo.isX86 && !SystemInfo.isX86_64) {
            if (!SystemInfo.isLinux || !SystemInfo.isX86_64) {
               return;
            }

            libraryName = "flatlaf-linux-x86_64";
            loadJAWT();
         } else {
            libraryName = "flatlaf-windows-x86";
            if (SystemInfo.isX86_64) {
               libraryName = libraryName + "_64";
            }

            loadJAWT();
         }

         nativeLibrary = createNativeLibrary(libraryName);
      }
   }

   private static NativeLibrary createNativeLibrary(String libraryName) {
      String libraryPath = System.getProperty("flatlaf.nativeLibraryPath");
      if (libraryPath != null) {
         if ("system".equals(libraryPath)) {
            NativeLibrary library = new NativeLibrary(libraryName, true);
            if (library.isLoaded()) {
               return library;
            }

            LoggingFacade.INSTANCE.logSevere("Did not find library " + libraryName + " in java.library.path, using extracted library instead", null);
         } else {
            File libraryFile = new File(libraryPath, System.mapLibraryName(libraryName));
            if (libraryFile.exists()) {
               return new NativeLibrary(libraryFile, true);
            }

            LoggingFacade.INSTANCE.logSevere("Did not find external library " + libraryFile + ", using extracted library instead", null);
         }
      }

      return new NativeLibrary("com/formdev/flatlaf/natives/" + libraryName, null, true);
   }

   private static void loadJAWT() {
      try {
         System.loadLibrary("jawt");
      } catch (UnsatisfiedLinkError ex) {
         String message = ex.getMessage();
         if (message == null || !message.contains("already loaded in another classloader")) {
            LoggingFacade.INSTANCE.logSevere(message, ex);
         }
      } catch (Exception ex) {
         LoggingFacade.INSTANCE.logSevere(ex.getMessage(), ex);
      }
   }
}
