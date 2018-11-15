package de.tudarmstadt.maki.simonstrator.tc.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GenericGraphElementProperties;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphBuilder;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSTypes;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyID;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyProvider;
import de.tudarmstadt.maki.simonstrator.tc.testing.GraphTestUtil;

/**
 * Unit tests for {@link TopologyProviders}
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class TopologyProvidersTest
{
   @Test
   public void testGetLocalView_DisjointNodes_NoEdges() throws Exception
   {
      final List<TopologyID> ids = mapToTopologyIds(Arrays.asList("UDG", "Transit"));
      final Graph gUdg = GraphBuilder.create().n("nU1").n("nU2").done();
      final Graph gTransit = GraphBuilder.create().n("nT1").n("nT2").done();
      final List<Graph> graphs = Arrays.asList(gUdg, gTransit);
      final ListInitializedTopologyProvider topologyProvider = new ListInitializedTopologyProvider(ids, graphs);

      final Graph graph = TopologyProviders.getLocalView(topologyProvider);

      GraphTestUtil.assertNodeAndEdgeCount(4, 0, graph);
   }

   @Test
   public void testGetLocalView_OverlappingNodes_NoEdges() throws Exception
   {
      final List<TopologyID> ids = mapToTopologyIds(Arrays.asList("UDG", "Transit"));
      final Graph gUdg = GraphBuilder.create().n("nU1").n("nU2").done();
      final Graph gTransit = GraphBuilder.create().n("nU1").n("nU2").n("T1").done();
      final List<Graph> graphs = Arrays.asList(gUdg, gTransit);
      final ListInitializedTopologyProvider topologyProvider = new ListInitializedTopologyProvider(ids, graphs);

      final Graph graph = TopologyProviders.getLocalView(topologyProvider);

      GraphTestUtil.assertNodeAndEdgeCount(3, 0, graph);
   }

   @Test
   public void testGetLocalView_OverlappingNodes_DisjointEdges() throws Exception
   {
      final List<TopologyID> ids = mapToTopologyIds(Arrays.asList("UDG", "Transit"));
      final Graph gUdg = GraphBuilder.create().n("nU1").n("nU2").e("nU1", "nU2", "eU1-U2").done();
      final Graph gTransit = GraphBuilder.create().n("nU1").n("nU2").n("nT1").e("nU2", "nT1").done();
      final List<Graph> graphs = Arrays.asList(gUdg, gTransit);
      final ListInitializedTopologyProvider topologyProvider = new ListInitializedTopologyProvider(ids, graphs);

      final Graph graph = TopologyProviders.getLocalView(topologyProvider);

      GraphTestUtil.assertNodeAndEdgeCount(3, 2, graph);
   }

   @Test
   public void testGetLocalView_OverlappingNodes_OverlappingEdges() throws Exception
   {
      final List<TopologyID> ids = mapToTopologyIds(Arrays.asList("UDG", "Transit"));
      final Graph gUdg = GraphBuilder.create().n("nU1").n("nU2").e("nU1", "nU2", "e12-U", 1.01).done();
      final Graph gTransit = GraphBuilder.create().n("nU1").n("nU2").n("nT3").e("nU1", "nU2", "e12-T", 1.02).e("nU2", "nT3", "e23-T", 1.03).done();
      final List<Graph> graphs = Arrays.asList(gUdg, gTransit);
      final ListInitializedTopologyProvider topologyProvider = new ListInitializedTopologyProvider(ids, graphs);

      final Graph graph = TopologyProviders.getLocalView(topologyProvider);

      GraphTestUtil.assertNodeAndEdgeCount(3, 3, graph);
      Assert.assertEquals(ids.get(0), graph.getEdge(EdgeID.get("e12-U")).getProperty(SiSTypes.TOPOLOGY_ID));
      Assert.assertEquals(1.01, graph.getEdge(EdgeID.get("e12-U")).getProperty(GenericGraphElementProperties.WEIGHT), 0.0);
      Assert.assertEquals(ids.get(1), graph.getEdge(EdgeID.get("e12-T")).getProperty(SiSTypes.TOPOLOGY_ID));
      Assert.assertEquals(1.02, graph.getEdge(EdgeID.get("e12-T")).getProperty(GenericGraphElementProperties.WEIGHT), 0.0);
      Assert.assertEquals(ids.get(1), graph.getEdge(EdgeID.get("e23-T")).getProperty(SiSTypes.TOPOLOGY_ID));
      Assert.assertEquals(1.03, graph.getEdge(EdgeID.get("e23-T")).getProperty(GenericGraphElementProperties.WEIGHT), 0.0);
      
      // Write-through test (check that the calculated graph is just a view
      gUdg.getEdge(EdgeID.get("e12-U")).setProperty(GenericGraphElementProperties.WEIGHT, 10.3);
      Assert.assertEquals(10.3, graph.getEdge(EdgeID.get("e12-U")).getProperty(GenericGraphElementProperties.WEIGHT), 0.0);
   }
   
   @Test
   public void testGetLocalView_WriteThroughTest() throws Exception
   {
      final List<TopologyID> ids = mapToTopologyIds(Arrays.asList("UDG", "Transit"));
      final Graph gUdg = GraphBuilder.create().n("nU1").n("nU2").e("nU1", "nU2", "e12-U", 1.01).done();
      final Graph gTransit = GraphBuilder.create().n("nU1").n("nU2").n("nT3").e("nU1", "nU2", "e12-T", 1.02).e("nU2", "nT3", "e23-T", 1.03).done();
      final List<Graph> graphs = Arrays.asList(gUdg, gTransit);
      final ListInitializedTopologyProvider topologyProvider = new ListInitializedTopologyProvider(ids, graphs);
      
      final Graph graph = TopologyProviders.getLocalView(topologyProvider);
      
      gUdg.getEdge(EdgeID.get("e12-U")).setProperty(GenericGraphElementProperties.WEIGHT, 10.3);
      Assert.assertEquals(10.3, graph.getEdge(EdgeID.get("e12-U")).getProperty(GenericGraphElementProperties.WEIGHT), 0.0);
   }

   @Test
   public void testExtractLocalView() throws Exception
   {
      final List<TopologyID> ids = mapToTopologyIds(Arrays.asList("UDG", "Transit"));
      final Graph gUdg = GraphBuilder.create().n("nU1").n("nU2").e("nU1", "nU2", "e12-U", 1.01).done();
      final Graph gTransit = GraphBuilder.create().n("nU1").n("nU2").n("nT3").e("nU1", "nU2", "e12-T", 1.02).e("nU2", "nT3", "e23-T", 1.03).done();
      final List<Graph> graphs = Arrays.asList(gUdg, gTransit);
      final ListInitializedTopologyProvider topologyProvider = new ListInitializedTopologyProvider(ids, graphs);
      final Graph graph = TopologyProviders.getLocalView(topologyProvider);
      
      final Graph udgView = TopologyProviders.extractLocalView(graph, ids.get(0));
      GraphTestUtil.assertNodeAndEdgeCount(3, 1, udgView);
      Assert.assertNotNull(udgView.getEdge(EdgeID.get("e12-U")));
      
      final Graph transitView = TopologyProviders.extractLocalView(graph, ids.get(1));
      GraphTestUtil.assertNodeAndEdgeCount(3, 2, transitView);
      Assert.assertNotNull(transitView.getEdge(EdgeID.get("e12-T")));
      Assert.assertNotNull(transitView.getEdge(EdgeID.get("e23-T")));
   }
   
   
   private static List<TopologyID> mapToTopologyIds(List<String> ids)
   {
      return ids.stream().map(i -> TopologyID.getIdentifier(i, ListInitializedTopologyProvider.class)).collect(Collectors.toList());
   }

   private static final class ListInitializedTopologyProvider implements TopologyProvider
   {
      private List<TopologyID> identifiers = new ArrayList<>();

      private Map<TopologyID, Graph> identifiersToView = new LinkedHashMap<>();

      public ListInitializedTopologyProvider(final List<TopologyID> list, final List<Graph> localViews)
      {
         final Iterator<Graph> localViewIterator = localViews.iterator();
         list.stream().forEach(tid -> {
            identifiers.add(tid);
            identifiersToView.put(tid, localViewIterator.next());
         });

      }

      @Override
      public void shutdown()
      {
         // nop
      }

      @Override
      public void initialize()
      {
         // nop
      }

      @Override
      public Host getHost()
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public Iterable<TopologyID> getTopologyIdentifiers()
      {
         return identifiers;
      }

      @Override
      public Graph getLocalView(TopologyID topologyIdentifier)
      {
         if (!this.identifiers.contains(topologyIdentifier))
            throw new IllegalArgumentException();

         return this.identifiersToView.get(topologyIdentifier);
      }

      @Override
      public INode getNode(TopologyID identifier)
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public Set<IEdge> getNeighbors(TopologyID topologyIdentifier)
      {
         throw new UnsupportedOperationException();
      }
   }
}
