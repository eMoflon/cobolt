package org.cobolt.tccpa;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.interpreter.UnitApplication;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.interpreter.impl.EngineImpl;
import org.eclipse.emf.henshin.interpreter.impl.UnitApplicationImpl;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import tccpa.Link;
import tccpa.Node;
import tccpa.TccpaFactory;
import tccpa.TccpaPackage;
import tccpa.Topology;

public class TopologyControlRuleTests
{
   /**
    * Project-relative path to the folder containing models and metamodels
    */
   private static final String WORKING_DIRECTORY = "src/org/cobolt/tccpa";

   /**
    * Path for storing generated outputs (relative to {@link #WORKING_DIRECTORY})
    */
   private static final String OUTPUT_PATH = "../../../../output/";

   private static final String TEMP_XMI_FILE = OUTPUT_PATH + "topology-input-code.xmi";

   private HenshinResourceSet resourceSet;

   private Resource testTopologyResource;

   private Module rulesModule;

   //@BeforeAll
   static void setUpClass()
   {
      final HenshinResourceSet resourceSet = new HenshinResourceSet(WORKING_DIRECTORY);

      // Load prior to working with generated code
      new EGraphImpl(resourceSet.getResource("topology-input.xmi"));

      final Resource generatedCodeResource = resourceSet.createResource(TEMP_XMI_FILE);

      // The schema location is only written if the physical and logical URI differ
      // https://www.eclipse.org/forums/index.php?t=msg&th=126980&goto=390657&#msg_390657
      TccpaPackage package1 = TccpaPackage.eINSTANCE;
      package1.eResource().setURI(URI.createFileURI("tccpa.ecore"));

      final TccpaFactory factory = TccpaFactory.eINSTANCE;
      final Topology topology = factory.createTopology();
      generatedCodeResource.getContents().add(topology);
      final List<Node> nodes = IntStream.rangeClosed(1, 6).boxed().map(i -> createNode("n" + i, topology)).collect(toList());

      final List<Link> forwardEdges = Arrays.asList(getlinkSpec(1, 2, 3), getlinkSpec(1, 3, 10), getlinkSpec(2, 3, 5), getlinkSpec(3, 4, 5),
            getlinkSpec(4, 5, 7), getlinkSpec(4, 6, 10), getlinkSpec(5, 6, 9)).stream().map(linkSpec -> {
               final Node fromNode = nodes.get(linkSpec.get(0) - 1);
               final Node toNode = nodes.get(linkSpec.get(1) - 1);
               final int weight = linkSpec.get(2);
               return createLink(fromNode, toNode, weight, topology);
            }).collect(toList());

      forwardEdges.stream().map(forwardLink -> {
         final Node fromNode = forwardLink.getTarget();
         final Node toNode = forwardLink.getSource();
         final double weight = forwardLink.getWeight();
         return createLink(fromNode, toNode, weight, topology);
      }).collect(toList());

      Assert.assertEquals(6, topology.getNodes().size());
      Assert.assertEquals(2 * 7, topology.getLinks().size());

      try
      {
         final Map<String, Object> options = new HashMap<>();
         options.put(XMLResource.OPTION_ENCODING, "UTF-8");
         options.put(XMIResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
         generatedCodeResource.save(options);
      } catch (final IOException e)
      {
         Assert.fail(e.getMessage());
      }
   }

   @BeforeEach
   void setUp()
   {
      resourceSet = new HenshinResourceSet(WORKING_DIRECTORY);
      rulesModule = resourceSet.getModule("tccpa.henshin", false);

      // Load prior to working with generated code
      new EGraphImpl(resourceSet.getResource("topology-input.xmi"));

      testTopologyResource = resourceSet.getResource(TEMP_XMI_FILE);
   }

   @Test
   void testDynamicVsGeneratedCode()
   {
      EGraph graphDynamic = new EGraphImpl(resourceSet.getResource("topology-input.xmi"));
      EGraph graphGencode = new EGraphImpl(testTopologyResource);

      // Create an engine and a rule application:
      Engine engine = new EngineImpl();
      for (final EGraph graph : Arrays.asList(graphDynamic, graphGencode))
      {
         final EObject topology = graph.getRoots().get(0);
         System.out.println(graph);
         {
            UnitApplication removeLink = new UnitApplicationImpl(engine);
            removeLink.setEGraph(graph);
            removeLink.setUnit(rulesModule.getUnit("removeLink"));
            removeLink.setParameterValue("linkId", "n1->n2");
            removeLink.setParameterValue("topology", topology);
            if (!removeLink.execute(null))
            {
               throw new RuntimeException("Link removal failed");
            }
         }
         {
            UnitApplication addLink = new UnitApplicationImpl(engine);
            addLink.setEGraph(graph);
            addLink.setUnit(rulesModule.getUnit("addLink"));
            addLink.setParameterValue("srcId", "n2");
            addLink.setParameterValue("trgId", "n1");
            addLink.setParameterValue("linkId", "n1->n2");
            addLink.setParameterValue("weight", 3.0);
            addLink.setParameterValue("topology", topology);
            if (!addLink.execute(null))
            {
               throw new RuntimeException("Link addition failed");
            }
         }
      }
   }

   @ParameterizedTest
   @MethodSource("testLinkRemovalPositive")
   void testLinkRemovalPositive(final String linkIdToRemove) throws Exception
   {
      final EGraph graph = new EGraphImpl(testTopologyResource);
      final EObject topology = graph.getRoots().get(0);
      final Engine engine = new EngineImpl();
      final UnitApplication removeLink = prepareLinkRemoval(linkIdToRemove, graph, topology, engine);
      final boolean executionSuccessfull = removeLink.execute(null);
      if (!executionSuccessfull)
      {
         Assert.fail("Rule not applicable");
      }
   }

   @SuppressWarnings("unused")
   private static Stream<String> testLinkRemovalPositive()
   {
      //@formatter:off
      return buildLinkIdStream(new Integer[][] {
            {1, 2}, {2, 1},
            {1, 3}, {3, 1},
            {2, 3}, {3, 2},
            {3, 4}, {4, 3},
            {4, 5}, {5, 4},
            {4, 6}, {6, 4},
            {5, 6}, {6, 5},
      });
      //@formatter:on
   }

   @ParameterizedTest
   @MethodSource("testLinkRemovalNegative")
   void testLinkRemovalNegative(final String linkIdToRemove) throws Exception
   {
      final EGraph graph = new EGraphImpl(testTopologyResource);
      final EObject topology = graph.getRoots().get(0);
      final Engine engine = new EngineImpl();
      final UnitApplication removeLink = prepareLinkRemoval(linkIdToRemove, graph, topology, engine);
      removeLink.execute(null);
   }

   @SuppressWarnings("unused")
   private static Stream<String> testLinkRemovalNegative()
   {
      //@formatter:off
      return buildLinkIdStream(new Integer[][] {
         {1, 6}, {6, 1}
      });
      //@formatter:on
   }

   @ParameterizedTest
   @ValueSource(strings = {"n7"})
   void testNodeAdditionPositive(final String nodeIdToAdd) throws Exception
   {
      final EGraph graph = new EGraphImpl(testTopologyResource);
      final EObject topology = graph.getRoots().get(0);
      final Engine engine = new EngineImpl();
      final UnitApplication removeLink = prepareNodeAddition(nodeIdToAdd, graph, topology, engine);
      removeLink.execute(null);
   }

   private UnitApplication prepareNodeAddition(final String nodeIdToAdd, final EGraph graph, final EObject topology, final Engine engine)
   {
      final UnitApplication removeLink = new UnitApplicationImpl(engine);
      removeLink.setEGraph(graph);
      removeLink.setUnit(rulesModule.getUnit("addNode"));
      removeLink.setParameterValue("nodeId", nodeIdToAdd);
      removeLink.setParameterValue("topology", topology);
      return removeLink;
   }

   private UnitApplication prepareLinkRemoval(final String linkIdToRemove, final EGraph graph, final EObject topology, final Engine engine)
   {
      final UnitApplication removeLink = new UnitApplicationImpl(engine);
      removeLink.setEGraph(graph);
      removeLink.setUnit(rulesModule.getUnit("removeLink"));
      removeLink.setParameterValue("linkId", linkIdToRemove);
      removeLink.setParameterValue("topology", topology);
      return removeLink;
   }

   private static Stream<String> buildLinkIdStream(final Integer[][] nodePairs)
   {
      return Arrays.asList(nodePairs).stream().map(pair -> String.format("n%d->n%d", pair[0], pair[1]));
   }

   private static Node createNode(final String nodeId, final Topology topology)
   {
      final Node node = TccpaFactory.eINSTANCE.createNode();
      node.setTopology(topology);
      node.setId(nodeId);
      return node;
   }

   private static Link createLink(final Node fromNode, final Node toNode, final double weight, final Topology topology)
   {
      final Link link = TccpaFactory.eINSTANCE.createLink();
      link.setTopology(topology);
      link.setId(String.format("%s->%s", fromNode.getId(), toNode.getId()));
      link.setSource(fromNode);
      link.setTarget(toNode);
      link.setWeight(weight);
      return link;
   }

   private static List<Integer> getlinkSpec(int fromNodeId, int toNodeId, int weight)
   {
      return Arrays.asList(fromNodeId, toNodeId, weight);
   }
}
