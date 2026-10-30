package org.jfree.chart.labels;

import java.io.Serializable;
import java.util.Formatter;
import java.util.Objects;
import org.jfree.chart.util.Args;
import org.jfree.data.flow.FlowDataset;
import org.jfree.data.flow.FlowKey;

public class StandardFlowLabelGenerator implements FlowLabelGenerator, Serializable {
   public static final String DEFAULT_TEMPLATE = "%2$s to %3$s = %4$,.2f";
   private String template;

   public StandardFlowLabelGenerator() {
      this("%2$s to %3$s = %4$,.2f");
   }

   public StandardFlowLabelGenerator(String template) {
      Args.nullNotPermitted(template, "template");
      this.template = template;
   }

   @Override
   public String generateLabel(FlowDataset dataset, FlowKey key) {
      Args.nullNotPermitted(dataset, "dataset");
      Args.nullNotPermitted(key, "key");
      Formatter formatter = new Formatter(new StringBuilder());

      String result;
      try {
         Number value = dataset.getFlow(key.getStage(), key.getSource(), key.getDestination());
         formatter.format(this.template, key.getStage(), key.getSource(), key.getDestination(), value);
         result = formatter.toString();
      } catch (Throwable var8) {
         try {
            formatter.close();
         } catch (Throwable var7) {
            var8.addSuppressed(var7);
         }

         throw var8;
      }

      formatter.close();
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof StandardFlowLabelGenerator)) {
         return false;
      }

      StandardFlowLabelGenerator that = (StandardFlowLabelGenerator)obj;
      return this.template.equals(that.template);
   }

   @Override
   public int hashCode() {
      int hash = 3;
      return 97 * hash + Objects.hashCode(this.template);
   }
}
