package org.jfree.data;

public interface DomainInfo {
   double getDomainLowerBound(boolean var1);

   double getDomainUpperBound(boolean var1);

   Range getDomainBounds(boolean var1);
}
