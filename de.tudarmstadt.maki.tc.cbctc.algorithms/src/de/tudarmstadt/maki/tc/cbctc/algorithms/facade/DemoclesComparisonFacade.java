package de.tudarmstadt.maki.tc.cbctc.algorithms.facade;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Iterables;

import de.tudarmstadt.maki.simonstrator.api.Graphs;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphBuilder;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphElementProperties;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IElement;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.tc.democles.integrated.DemoclesTopologyPatternMatcher;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmParamters;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.GraphElementConstraint;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching.TopologyPatternMatch;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching.TopologyPatternMatcher;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching.TopologyPatternMatcher_Impl;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.pattern.PatternBuilder;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.pattern.TopologyPattern;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;
import de.tudarmstadt.maki.tc.cbctc.analysis.AbstractPatternMatcher;
import de.tudarmstadt.maki.tc.cbctc.analysis.AnalysisFactory;
import de.tudarmstadt.maki.tc.cbctc.analysis.FiveCliquePatternMatcher;
import de.tudarmstadt.maki.tc.cbctc.analysis.FourChainWithoutShortcutsPatternMatcher;
import de.tudarmstadt.maki.tc.cbctc.analysis.KTCPatternMatcher;
import de.tudarmstadt.maki.tc.cbctc.analysis.TrianglePatternMatcher;
import de.tudarmstadt.maki.tc.cbctc.model.EdgeState;
import de.tudarmstadt.maki.tc.cbctc.model.Topology;

public class DemoclesComparisonFacade extends EMoflonFacade
{

   private static final String FIVE_CLIQUE_ID = "5-clique";

   private static final String KTC_ID = "kTC";

   private static final String FOUR_CHAIN_ID = "4-chain";

   private static final String TRIANGLE_ID = "triangle";

   private static final double KTC_K = 1.41;

   private static final List<String> CSV_HEADER = Arrays.asList(//
         "Time", "NodeCount", "EdgeCount", "GraphSize", //
         "Pattern", //
         "TimeSim", "TimeDemocles", "TimeEMoflon", //
         "MatchCountSim", "MatchCountDemocles", "MatchCountEMoflon");

   private static final String CSV_SEP = ";";

   private static final String SIM_PM_ID = "Default";

   private static final String DEMOCLES_PM_ID = "Democles";

   private static final String EMOFLON_PM_ID = "eMoflon";

   private static final List<String> PATTERN_MATCHERS = Arrays.asList(DEMOCLES_PM_ID, SIM_PM_ID, EMOFLON_PM_ID);

   private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HHmmss");

   private final File outputFile;

   private TopologyPatternMatcher patternMatcher;

   private final TopologyPattern trianglePattern;

   private final TopologyPattern ktcPattern;

   private final TopologyPattern fourChainWithoutShortcutsPattern;

   private final TopologyPattern fiveCliquePattern;

   private final TrianglePatternMatcher triangleMatcher;

   private final KTCPatternMatcher kTCMatchCounter;

   private final FiveCliquePatternMatcher fiveCliqueMatchCounter;

   private final FourChainWithoutShortcutsPatternMatcher fourChainWithoutShortcutsMatcher;

   public DemoclesComparisonFacade()
   {
      outputFile = new File(String.format("./output/rkluge/democles/PatternMatcherEvaluationData_%s.csv", dateFormat.format(new Date())));
      trianglePattern = createTrianglePattern();
      ktcPattern = createKtcPattern();
      fourChainWithoutShortcutsPattern = createFourChainWithoutShortcutsMatcher();
      fiveCliquePattern = createFiveCliquePattern();

      triangleMatcher = AnalysisFactory.eINSTANCE.createTrianglePatternMatcher();
      kTCMatchCounter = AnalysisFactory.eINSTANCE.createKTCPatternMatcher();
      kTCMatchCounter.setK(KTC_K);
      fourChainWithoutShortcutsMatcher = AnalysisFactory.eINSTANCE.createFourChainWithoutShortcutsPatternMatcher();
      fiveCliqueMatchCounter = AnalysisFactory.eINSTANCE.createFiveCliquePatternMatcher();
   }

   @Override
   public void run(TopologyControlAlgorithmParamters parameters)
   {
      final Graph graph = this.getGraph();
//      Topology topology2 = this.getTopology();
//      if (topology2.getEdgeCount() != graph.getEdgeCount())
//      {
//         throw new IllegalStateException(String.format("Edge count mismatch: Sim. %d <-> Model: %d", graph.getEdgeCount(), topology2.getEdgeCount()));
//      }
//      if (topology2.getNodeCount() != graph.getNodeCount())
//      {
//         throw new IllegalStateException(String.format("Node count mismatch: Sim. %d <-> Model: %d", graph.getNodeCount(), topology2.getNodeCount()));
//      }
//      for (final INode node : graph.getNodes())
//      {
//         Node foundNode = topology2.getNodes().stream().filter(n -> n.getId().equals(node.getId().valueAsString())).findFirst().orElse(null);
//         if (foundNode == null)
//         {
//            throw new IllegalStateException(String.format("No counterpart for Sim-node %s", node));
//         }
//      }
//      for (final Node node : topology2.getNodes())
//      {
//         INode foundNode = graph.getNodes().stream().filter(n -> n.getId().valueAsString().equals(node.getId())).findFirst().orElse(null);
//         if (foundNode == null)
//         {
//            throw new IllegalStateException(String.format("No counterpart for model node %s", node));
//         }
//      }
//      for (final IEdge edge : graph.getEdges())
//      {
//         Edge foundEdge = topology2.getEdges().stream().filter(e -> e.getId().equals(edge.getId().valueAsString())).findFirst().orElse(null);
//         if (foundEdge == null)
//         {
//            throw new IllegalStateException(String.format("No counterpart for Sim-edge %s", edge));
//         }
//      }
//      for (final Edge edge : topology2.getEdges())
//      {
//         IEdge foundEdge = graph.getEdges().stream().filter(e -> e.getId().valueAsString().equals(edge.getId())).findFirst().orElse(null);
//         if (foundEdge == null)
//         {
//            throw new IllegalStateException(String.format("No counterpart for model node %s", edge));
//         } else
//         {
//            if (edge.getWeight() != foundEdge.getProperty(UnderlayTopologyProperties.WEIGHT))
//            {
//               throw new IllegalStateException(String.format("Weight mismatch for edges with ID %s: sim: %f, model: %f", foundEdge.getId(),
//                     foundEdge.getProperty(UnderlayTopologyProperties.WEIGHT), edge.getWeight()));
//            }
//         }
//      }
      final int nodeCount = graph.getNodeCount();
      final int edgeCount = graph.getEdgeCount();
      final int graphSize = nodeCount + edgeCount;
      Monitor.log(getClass(), Level.INFO, "Graph: n=%d, m=%d, n+m=%d", nodeCount, edgeCount, graphSize);
      if (!outputFile.exists())
      {
         try
         {
            FileUtils.writeLines(outputFile, Arrays.asList(StringUtils.join(CSV_HEADER, CSV_SEP)));
         } catch (final IOException e)
         {
            throw new IllegalStateException(e);
         }
      }

      for (final String patternID : Arrays.asList(TRIANGLE_ID, KTC_ID, FOUR_CHAIN_ID, FIVE_CLIQUE_ID))
      {
         final TopologyPattern pattern = this.getPattern(patternID);
         final Map<String, Long> times = new HashMap<>();
         final Map<String, Integer> matchCounts = new HashMap<>();
         for (final String patternMatcherID : PATTERN_MATCHERS)
         {
            long startTime = System.currentTimeMillis();
            final int matchCount;
            switch (patternMatcherID)
            {
            case SIM_PM_ID:
            case DEMOCLES_PM_ID:
               this.setPatternMatcher(patternMatcherID);
               final TopologyPatternMatcher patternMatcher = this.getPatternMatcher();
               patternMatcher.setPattern(pattern);
               final Iterable<TopologyPatternMatch> matches = patternMatcher.match(graph);
               matchCount = Iterables.size(matches);
               break;
            case EMOFLON_PM_ID:
               final AbstractPatternMatcher matchCounter = createEMoflonPatternMatcher(patternID);
               final Topology topology = getTopology();
               matchCount = matchCounter.match(topology).getMatches().size();
               break;
            default:
               throw new IllegalStateException("Invalid PM: " + patternMatcherID);
            }
            final long durationInMillis = System.currentTimeMillis() - startTime;
            times.put(patternMatcherID, durationInMillis);
            matchCounts.put(patternMatcherID, matchCount);
            Monitor.log(getClass(), Level.INFO, "[%10s][%10s]  Match count: %6d, Time in ms: %10d", patternID, patternMatcherID, matchCount, durationInMillis);
         }
         final long simTime = times.get(SIM_PM_ID);
         final long democlesTime = times.get(DEMOCLES_PM_ID);
         final long eMoflonTime = times.get(EMOFLON_PM_ID);
         final Integer simMatchCount = matchCounts.get(SIM_PM_ID);
         final Integer democlesMatchCount = matchCounts.get(DEMOCLES_PM_ID);
         final Integer eMoflonMatchCount = matchCounts.get(EMOFLON_PM_ID);
         Monitor.log(getClass(), Level.INFO, "[%10s] t: %5d | %5d | %5d || count: %5d | %5d | %5d : ", //
               patternID, //
               simTime, democlesTime, eMoflonTime, //
               simMatchCount, democlesMatchCount, eMoflonMatchCount//
         );

         try
         {
            final String formattedDate = dateFormat.format(new Date());
            final List<String> lineEntries = Arrays.asList(formattedDate, //
                  Integer.toString(nodeCount), //
                  Integer.toString(edgeCount), //
                  Integer.toString(graphSize), //
                  patternID, Long.toString(simTime), //
                  Long.toString(democlesTime), //
                  Long.toString(eMoflonTime), //
                  Integer.toString(simMatchCount), //
                  Integer.toString(democlesMatchCount), //
                  Integer.toString(eMoflonMatchCount)//
            );

            if (lineEntries.size() != CSV_HEADER.size())
               throw new IllegalStateException("Length mismatch");

            FileUtils.writeLines(outputFile, Arrays.asList(StringUtils.join(lineEntries//
                  , CSV_SEP)), true);
         } catch (final IOException e)
         {
            throw new IllegalStateException(e);
         }
      }

      getTopology().getEdges().stream().forEach(e -> e.setState(EdgeState.ACTIVE));

   }

   private AbstractPatternMatcher createEMoflonPatternMatcher(final String patternID)
   {
      switch (patternID)
      {
      case TRIANGLE_ID:
         return triangleMatcher;
      case KTC_ID:
         return kTCMatchCounter;
      case FOUR_CHAIN_ID:
         return fourChainWithoutShortcutsMatcher;
      case FIVE_CLIQUE_ID:
         return fiveCliqueMatchCounter;
      default:
         throw new IllegalArgumentException("Unknown pattern " + patternID);
      }
   }

   private TopologyPatternMatcher getPatternMatcher()
   {
      return this.patternMatcher;
   }

   private void setPatternMatcher(final String patternMatcherID)
   {
      switch (patternMatcherID)
      {
      case DEMOCLES_PM_ID:
         this.patternMatcher = new DemoclesTopologyPatternMatcher();
         break;
      case SIM_PM_ID:
         this.patternMatcher = new TopologyPatternMatcher_Impl();
         break;
      }
   }

   private TopologyPattern getPattern(final String patternID)
   {
      switch (patternID)
      {
      case TRIANGLE_ID:
         return trianglePattern;
      case KTC_ID:
         return ktcPattern;
      case FOUR_CHAIN_ID:
         return fourChainWithoutShortcutsPattern;
      case FIVE_CLIQUE_ID:
         return fiveCliquePattern;
      default:
         throw new IllegalArgumentException("Unknown pattern: " + patternID);
      }
   }

   public static TopologyPattern createFiveCliquePattern()
   {
      TopologyPattern pattern;
      pattern = PatternBuilder.create().setLocalNode("pn1").addDirectedEdge("pn1", "pn2").addDirectedEdge("pn1", "pn3").addDirectedEdge("pn1", "pn4")
            .addDirectedEdge("pn1", "pn5").addDirectedEdge("pn2", "pn3").addDirectedEdge("pn2", "pn4").addDirectedEdge("pn2", "pn5")
            .addDirectedEdge("pn3", "pn4").addDirectedEdge("pn3", "pn5").addDirectedEdge("pn4", "pn5").addBinaryConstraint(
                  new GraphElementConstraint(Arrays.asList("pn1", "pn2", "pn3", "pn4", "pn5").stream().map(s -> INodeID.get(s)).collect(Collectors.toList())) {
                     private static final long serialVersionUID = 1L;

                     @Override
                     protected boolean checkCandidates(final Collection<? extends IElement> bindingCandidates)
                     {
                        final Iterator<? extends IElement> iter = bindingCandidates.iterator();
                        final INode pn1 = INode.class.cast(iter.next());
                        final INode pn2 = INode.class.cast(iter.next());
                        final INode pn3 = INode.class.cast(iter.next());
                        final INode pn4 = INode.class.cast(iter.next());
                        final INode pn5 = INode.class.cast(iter.next());

                        final boolean isValid = pn1.getId().compareTo(pn2.getId()) > 0 && pn2.getId().compareTo(pn3.getId()) > 0
                              && pn3.getId().compareTo(pn4.getId()) > 0 && pn4.getId().compareTo(pn5.getId()) > 0;
                        return isValid;
                     }
                  })
            .done();
      return pattern;
   }

   public static TopologyPattern createTrianglePattern()
   {
      TopologyPattern pattern;
      pattern = PatternBuilder.create().setLocalNode("pn1").addDirectedEdge("pn1", "pe12", "pn2").addDirectedEdge("pn1", "pe13", "pn3")
            .addDirectedEdge("pn2", "pe23", "pn3").done();
      return pattern;
   }

   public static TopologyPattern createKtcPattern()
   {
      return PatternBuilder.create().setLocalNode("pn1").addDirectedEdge("pn1", "pe12", "pn2").addDirectedEdge("pn1", "pe13", "pn3")
            .addDirectedEdge("pn2", "pe23", "pn3")
            .addBinaryConstraint(new GraphElementConstraint(Arrays.asList(EdgeID.get("pe12"), EdgeID.get("pe13"), EdgeID.get("pe23"))) {
               private static final long serialVersionUID = 174690941617641136L;

               @Override
               protected boolean checkCandidates(final Collection<? extends IElement> bindingCandidates)
               {
                  final Iterator<? extends IElement> iter = bindingCandidates.iterator();
                  final IEdge pe12 = IEdge.class.cast(iter.next());
                  final IEdge pe13 = IEdge.class.cast(iter.next());
                  final IEdge pe23 = IEdge.class.cast(iter.next());

                  GraphElementProperties.validateThatPropertyIsPresent(pe12, UnderlayTopologyProperties.WEIGHT);
                  GraphElementProperties.validateThatPropertyIsPresent(pe13, UnderlayTopologyProperties.WEIGHT);
                  GraphElementProperties.validateThatPropertyIsPresent(pe23, UnderlayTopologyProperties.WEIGHT);

                  final double w12 = pe12.getProperty(UnderlayTopologyProperties.WEIGHT);
                  final double w13 = pe13.getProperty(UnderlayTopologyProperties.WEIGHT);
                  final double w23 = pe23.getProperty(UnderlayTopologyProperties.WEIGHT);
                  return w12 > Math.max(w13, w23) && w12 > KTC_K * Math.min(w13, w23);
               }
            }).done();
   }

   public static TopologyPattern createFourChainWithoutShortcutsMatcher()
   {
      final TopologyPattern nac1 = PatternBuilder.create().addDirectedEdge("pn1", "pe13", "pn3").doneWithoutLocalNode();
      final TopologyPattern nac2 = PatternBuilder.create().addDirectedEdge("pn1", "pe14", "pn4").doneWithoutLocalNode();
      return PatternBuilder.create().setLocalNode("pn1")//
            .addDirectedEdge("pn1", "pe12", "pn2")//
            .addDirectedEdge("pn2", "pe23", "pn3")//
            .addDirectedEdge("pn3", "pe34", "pn4")//
            .addNAC(nac1).addNAC(nac2).done();
   }
   
   @Test
   public void testFourChainSingleMatch() throws Exception
   {
      final Graph graph = createFourChain();
      TopologyPatternMatcher_Impl pm = new TopologyPatternMatcher_Impl(createFourChainWithoutShortcutsMatcher());
      Iterable<TopologyPatternMatch> match = pm.match(INodeID.get("n1"), graph);
      Assert.assertEquals(1, Iterables.size(match));
   }
   
   @Test
   public void testFourChainWithShortcut1() throws Exception
   {
      final Graph graph = createFourChain();
      graph.addEdge(Graphs.createDirectedEdge(EdgeID.get("e13"), INodeID.get("n1"), INodeID.get("n3")));
      TopologyPatternMatcher_Impl pm = new TopologyPatternMatcher_Impl(createFourChainWithoutShortcutsMatcher());
      Iterable<TopologyPatternMatch> match = pm.match(INodeID.get("n1"), graph);
      Assert.assertEquals(0, Iterables.size(match));
   }

   @Test
   public void testFourChainWithShortcut2() throws Exception
   {
      final Graph graph = createFourChain();
      graph.addEdge(Graphs.createDirectedEdge(EdgeID.get("e14"), INodeID.get("n1"), INodeID.get("n4")));
      TopologyPatternMatcher_Impl pm = new TopologyPatternMatcher_Impl(createFourChainWithoutShortcutsMatcher());
      Iterable<TopologyPatternMatch> match = pm.match(INodeID.get("n1"), graph);
      Assert.assertEquals(0, Iterables.size(match));
   }

   @Test
   public void testFourChainWithShortcuts12() throws Exception
   {
      final Graph graph = createFourChain();
      graph.addEdge(Graphs.createDirectedEdge(EdgeID.get("e13"), INodeID.get("n1"), INodeID.get("n3")));
      graph.addEdge(Graphs.createDirectedEdge(EdgeID.get("e14"), INodeID.get("n1"), INodeID.get("n4")));
      TopologyPatternMatcher_Impl pm = new TopologyPatternMatcher_Impl(createFourChainWithoutShortcutsMatcher());
      Iterable<TopologyPatternMatch> match = pm.match(INodeID.get("n1"), graph);
      Assert.assertEquals(0, Iterables.size(match));
   }

   private Graph createFourChain()
   {
      return GraphBuilder.create().n("n1").n("n2").n("n3").n("n4").e("n1", "n2", "e12").e("n2", "n3", "e23").e("n3", "n4", "e34").done();
   }

}
