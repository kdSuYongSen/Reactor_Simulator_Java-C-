package com.formdev.flatlaf.ui;

import java.util.function.BiPredicate;

class StackUtilsImpl extends StackUtils {
   @Override
   boolean wasInvokedFromImpl(BiPredicate<String, String> predicate, int limit) {
      int count = -2;
      StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

      for (StackTraceElement stackTraceElement : stackTrace) {
         if (predicate.test(stackTraceElement.getClassName(), stackTraceElement.getMethodName())) {
            return true;
         }

         if (limit > 0 && ++count > limit) {
            return false;
         }
      }

      return false;
   }
}
