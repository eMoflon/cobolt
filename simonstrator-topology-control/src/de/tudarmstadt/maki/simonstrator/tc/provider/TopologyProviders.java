package de.tudarmstadt.maki.simonstrator.tc.provider;

import java.util.Set;
import java.util.stream.Collectors;

import de.tudarmstadt.maki.simonstrator.api.Graphs;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSTypes;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyID;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyProvider;
import de.tudarmstadt.maki.simonstrator.api.util.UtilityClassNotInstantiableException;

/**
 * Utility functions for the {@link TopologyProvider} interface
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public final class TopologyProviders
{

   public TopologyProviders()
   {
      throw new UtilityClassNotInstantiableException();
   }

   /**
    * Returns the joint local view on the given 'topologyProvider', which results from combining the local views of each topology ID of the provider. 
    * @param topologyProvider
    * @return
    */
   public static Graph getLocalView(final TopologyProvider topologyProvider)
   {
      final Graph joinedGraph = Graphs.createGraph();
      for (final TopologyID id : topologyProvider.getTopologyIdentifiers())
      {
         final Graph localViewInToppology = topologyProvider.getLocalView(id);
         joinedGraph.addNodes(localViewInToppology.getNodes());
         final Set<? extends IEdge> edges = localViewInToppology.getEdges();
         edges.stream().forEach(e -> e.setProperty(SiSTypes.TOPOLOGY_ID, id));
         joinedGraph.addEdges(edges);
      }
      return joinedGraph;
   }

   /**
    * Returns the subgraph of the given 'graph' that results from removing all edges whose topology ID differs from the given 'topologyId'.
    * @param graph
    * @param topologyId
    * @return
    */
   public static Graph extractLocalView(final Graph graph, final TopologyID topologyId)
   {
      final Graph extractedGraph = Graphs.createGraph();
      extractedGraph.addNodes(graph.getNodes());
      extractedGraph.addEdges(graph.getEdges().stream().filter(e -> topologyId.equals(e.getProperty(SiSTypes.TOPOLOGY_ID))).collect(Collectors.toList()));
      return extractedGraph;
   }
}
