package org.jfree.data;

import java.util.List;

public interface KeyedValues<K extends Comparable<K>> extends Values {
   K getKey(int var1);

   int getIndex(K var1);

   List<K> getKeys();

   Number getValue(K var1);
}
