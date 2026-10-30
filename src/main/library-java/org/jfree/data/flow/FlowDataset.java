package org.jfree.data.flow;

import java.util.List;
import java.util.Set;
import org.jfree.data.general.Dataset;

public interface FlowDataset<K extends Comparable<K>> extends Dataset {
   int getStageCount();

   List<K> getSources(int var1);

   List<K> getDestinations(int var1);

   Set<NodeKey<K>> getAllNodes();

   Object getNodeProperty(NodeKey<K> var1, String var2);

   Number getFlow(int var1, K var2, K var3);

   Set<FlowKey<K>> getAllFlows();

   Object getFlowProperty(FlowKey<K> var1, String var2);
}
