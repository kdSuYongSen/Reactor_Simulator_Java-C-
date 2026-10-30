package org.jfree.data.flow;

import org.jfree.chart.util.Args;

public class FlowDatasetUtils {
   private FlowDatasetUtils() {
   }

   public static <K extends Comparable<K>> double calculateInflow(FlowDataset<K> dataset, K node, int stage) {
      Args.nullNotPermitted(dataset, "dataset");
      Args.nullNotPermitted(node, "node");
      Args.requireInRange(stage, "stage", 0, dataset.getStageCount());
      if (stage == 0) {
         return 0.0;
      }

      double inflow = 0.0;

      for (K key : dataset.getSources(stage - 1)) {
         Number n = dataset.getFlow(stage - 1, key, node);
         if (n != null) {
            inflow += n.doubleValue();
         }
      }

      return inflow;
   }

   public static <K extends Comparable<K>> double calculateOutflow(FlowDataset<K> dataset, K source, int stage) {
      Args.nullNotPermitted(dataset, "dataset");
      Args.nullNotPermitted(source, "source");
      Args.requireInRange(stage, "stage", 0, dataset.getStageCount());
      if (stage == dataset.getStageCount()) {
         return 0.0;
      }

      double outflow = 0.0;

      for (K key : dataset.getDestinations(stage)) {
         Number n = dataset.getFlow(stage, source, key);
         if (n != null) {
            outflow += n.doubleValue();
         }
      }

      return outflow;
   }

   public static <K extends Comparable<K>> double calculateTotalFlow(FlowDataset<K> dataset, int stage) {
      Args.nullNotPermitted(dataset, "dataset");
      double total = 0.0;

      for (K source : dataset.getSources(stage)) {
         for (K destination : dataset.getDestinations(stage)) {
            Number flow = dataset.getFlow(stage, source, destination);
            if (flow != null) {
               total += flow.doubleValue();
            }
         }
      }

      return total;
   }

   public static <K extends Comparable<K>> boolean hasNodeSelections(FlowDataset<K> dataset) {
      Args.nullNotPermitted(dataset, "dataset");

      for (int stage = 0; stage < dataset.getStageCount() + 1; stage++) {
         for (K source : dataset.getSources(stage)) {
            NodeKey<K> nodeKey = new NodeKey<>(stage, source);
            if (Boolean.TRUE.equals(dataset.getNodeProperty(nodeKey, "selected"))) {
               return true;
            }
         }
      }

      return false;
   }

   public static <K extends Comparable<K>> int selectedNodeCount(FlowDataset<K> dataset) {
      Args.nullNotPermitted(dataset, "dataset");
      int result = 0;

      for (int stage = 0; stage < dataset.getStageCount() + 1; stage++) {
         for (K source : dataset.getSources(stage)) {
            NodeKey<K> nodeKey = new NodeKey<>(stage, source);
            if (Boolean.TRUE.equals(dataset.getNodeProperty(nodeKey, "selected"))) {
               result++;
            }
         }
      }

      return result;
   }

   public static <K extends Comparable<K>> boolean hasFlowSelections(FlowDataset<K> dataset) {
      Args.nullNotPermitted(dataset, "dataset");

      for (int s = 0; s < dataset.getStageCount(); s++) {
         for (K source : dataset.getSources(s)) {
            for (K destination : dataset.getDestinations(s)) {
               FlowKey<K> flowKey = new FlowKey<>(s, source, destination);
               if (Boolean.TRUE.equals(dataset.getFlowProperty(flowKey, "selected"))) {
                  return true;
               }
            }
         }
      }

      return false;
   }
}
