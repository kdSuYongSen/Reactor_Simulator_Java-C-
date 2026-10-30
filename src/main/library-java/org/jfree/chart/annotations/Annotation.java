package org.jfree.chart.annotations;

import org.jfree.chart.event.AnnotationChangeListener;

public interface Annotation {
   void addChangeListener(AnnotationChangeListener var1);

   void removeChangeListener(AnnotationChangeListener var1);
}
