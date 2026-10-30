package org.jfree.data.general;

import java.util.EventObject;

public class DatasetChangeEvent extends EventObject {
   private Dataset dataset;

   public DatasetChangeEvent(Object source, Dataset dataset) {
      super(source);
      this.dataset = dataset;
   }

   public Dataset getDataset() {
      return this.dataset;
   }
}
