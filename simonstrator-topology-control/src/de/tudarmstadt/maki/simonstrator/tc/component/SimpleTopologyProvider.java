package de.tudarmstadt.maki.simonstrator.tc.component;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.topology.AdaptableTopologyProvider;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyID;

public interface SimpleTopologyProvider
{

   TopologyID getTopologyID();

   Graph getTopology();

   AdaptableTopologyProvider getTopologyComponent(INodeID nodeId);

}
