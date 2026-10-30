package org.jfree.data;

public interface KeyedValue<K extends Comparable<K>> extends Value {
   K getKey();
}
