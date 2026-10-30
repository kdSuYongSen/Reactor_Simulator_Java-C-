package org.jfree.data.flow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.CloneUtils;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.data.general.AbstractDataset;

public class DefaultFlowDataset<K extends Comparable<K>> extends AbstractDataset implements FlowDataset<K>, PublicCloneable, Serializable {
   private List<List<K>> nodes = new ArrayList<>();
   private Map<NodeKey, Map<String, Object>> nodeProperties;
   private Map<FlowKey<K>, Number> flows;
   private Map<FlowKey, Map<String, Object>> flowProperties;

   public DefaultFlowDataset() {
      this.nodes.add(new ArrayList<>());
      this.nodes.add(new ArrayList<>());
      this.nodeProperties = new HashMap<>();
      this.flows = new HashMap<>();
      this.flowProperties = new HashMap<>();
   }

   @Override
   public List<K> getSources(int stage) {
      return new ArrayList<>(this.nodes.get(stage));
   }

   @Override
   public List<K> getDestinations(int stage) {
      return new ArrayList<>(this.nodes.get(stage + 1));
   }

   @Override
   public Set<NodeKey<K>> getAllNodes() {
      Set<NodeKey<K>> result = new HashSet<>();

      for (int s = 0; s <= this.getStageCount(); s++) {
         for (K key : this.getSources(s)) {
            result.add(new NodeKey<>(s, key));
         }
      }

      return result;
   }

   @Override
   public Object getNodeProperty(NodeKey<K> nodeKey, String propertyKey) {
      Map<String, Object> props = this.nodeProperties.get(nodeKey);
      return props != null ? props.get(propertyKey) : null;
   }

   public void setNodeProperty(NodeKey<K> nodeKey, String propertyKey, Object value) {
      Map<String, Object> props = this.nodeProperties.computeIfAbsent(nodeKey, k -> new HashMap<>());
      props.put(propertyKey, value);
      this.fireDatasetChanged();
   }

   @Override
   public Number getFlow(int stage, K source, K destination) {
      return this.flows.get(new FlowKey(stage, source, destination));
   }

   public void setFlow(int stage, K source, K destination, double flow) {
      Args.requireInRange(stage, "stage", 0, this.getStageCount());
      Args.nullNotPermitted(source, "source");
      Args.nullNotPermitted(destination, "destination");
      if (stage > this.nodes.size() - 2) {
         this.nodes.add(new ArrayList<>());
      }

      if (!this.getSources(stage).contains(source)) {
         this.nodes.get(stage).add(source);
      }

      if (!this.getDestinations(stage).contains(destination)) {
         this.nodes.get(stage + 1).add(destination);
      }

      this.flows.put(new FlowKey<>(stage, source, destination), flow);
      this.fireDatasetChanged();
   }

   @Override
   public Object getFlowProperty(FlowKey<K> flowKey, String propertyKey) {
      Map<String, Object> props = this.flowProperties.get(flowKey);
      return props != null ? props.get(propertyKey) : null;
   }

   public void setFlowProperty(FlowKey<K> flowKey, String propertyKey, Object value) {
      Map<String, Object> props = this.flowProperties.computeIfAbsent(flowKey, k -> new HashMap<>());
      props.put(propertyKey, value);
      this.fireDatasetChanged();
   }

   @Override
   public int getStageCount() {
      return this.nodes.size() - 1;
   }

   @Override
   public Set<FlowKey<K>> getAllFlows() {
      return new HashSet<>(this.flows.keySet());
   }

   public List<FlowKey<K>> getInFlows(NodeKey nodeKey) {
      Args.nullNotPermitted(nodeKey, "nodeKey");
      if (nodeKey.getStage() == 0) {
         return Collections.EMPTY_LIST;
      }

      List<FlowKey<K>> result = new ArrayList<>();

      for (FlowKey<K> flowKey : this.flows.keySet()) {
         if (flowKey.getStage() == nodeKey.getStage() - 1 && flowKey.getDestination().equals(nodeKey.getNode())) {
            result.add(flowKey);
         }
      }

      return result;
   }

   public List<FlowKey> getOutFlows(NodeKey nodeKey) {
      Args.nullNotPermitted(nodeKey, "nodeKey");
      if (nodeKey.getStage() == this.getStageCount()) {
         return Collections.EMPTY_LIST;
      }

      List<FlowKey> result = new ArrayList<>();

      for (FlowKey flowKey : this.flows.keySet()) {
         if (flowKey.getStage() == nodeKey.getStage() && flowKey.getSource().equals(nodeKey.getNode())) {
            result.add(flowKey);
         }
      }

      return result;
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      DefaultFlowDataset<K> clone = (DefaultFlowDataset<K>)super.clone();
      clone.flows = new HashMap<>(this.flows);
      clone.nodes = new ArrayList<>();

      for (List<?> list : this.nodes) {
         clone.nodes.add((List<K>)CloneUtils.cloneList(list));
      }

      return clone;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (!(obj instanceof FlowDataset)) {
         return false;
      }

      FlowDataset other = (FlowDataset)obj;
      if (other.getStageCount() != this.getStageCount()) {
         return false;
      }

      for (int stage = 0; stage < this.getStageCount(); stage++) {
         if (!Objects.equals(other.getSources(stage), this.getSources(stage))) {
            return false;
         }

         if (!Objects.equals(other.getDestinations(stage), this.getDestinations(stage))) {
            return false;
         }

         for (K source : this.getSources(stage)) {
            for (K destination : this.getDestinations(stage)) {
               if (!Objects.equals(other.getFlow(stage, source, destination), this.getFlow(stage, source, destination))) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   @Override
   public int hashCode() {
      int hash = 3;
      hash = 89 * hash + Objects.hashCode(this.getSources(0));
      return 89 * hash + Objects.hashCode(this.getDestinations(this.getStageCount() - 1));
   }
}
