package org.cobolt.tccpa;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
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
   private static final String NODE_ID_SEPARATOR = "->";

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

   private Engine engine;

   /**
    * Creates and saves the test input model
    * @param args ignored
    * @throws IOException
    */
   public static void main(final String[] args) throws IOException
   {
      final HenshinResourceSet resourceSet = new HenshinResourceSet(WORKING_DIRECTORY);

      final Resource generatedCodeResource = resourceSet.createResource(TEMP_XMI_FILE);

      // The schema location is only written if the physical and logical URI differ
      // https://www.eclipse.org/forums/index.php?t=msg&th=126980&goto=390657&#msg_390657
      TccpaPackage package1 = TccpaPackage.eINSTANCE;
      package1.eResource().setURI(URI.createFileURI("tccpa.ecore"));

      final TccpaFactory factory = TccpaFactory.eINSTANCE;
      final Topology topology = factory.createTopology();
      generatedCodeResource.getContents().add(topology);
      final List<Node> nodes = IntStream.rangeClosed(1, 7).boxed().map(i -> createNode("n" + i, topology)).collect(toList());

      //@formatter:off
      final List<Link> forwardLinks = Arrays.asList(
            createLinkSpec(1, 2, 3),
            createLinkSpec(1, 3, 10),
            createLinkSpec(2, 3, 5),
            createLinkSpec(3, 4, 5),
            createLinkSpec(4, 5, 7),
            createLinkSpec(4, 6, 10),
            createLinkSpec(4, 7, 6),
            createLinkSpec(5, 6, 9),
            createLinkSpec(6, 7, 6)
            ).stream().map(linkSpec -> createLink(nodes, linkSpec, topology)).collect(toList());
      //@formatter:on

      forwardLinks.stream().map(forwardLink -> {
         final Node fromNode = forwardLink.getTarget();
         final Node toNode = forwardLink.getSource();
         final double weight = forwardLink.getWeight();
         return createLink(fromNode, toNode, weight, topology);
      }).collect(toList());

      Assert.assertEquals(6, topology.getNodes().size());
      Assert.assertEquals(2 * 7, topology.getLinks().size());

      final Map<String, Object> options = new HashMap<>();
      options.put(XMLResource.OPTION_ENCODING, "UTF-8");
      options.put(XMIResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
      generatedCodeResource.save(options);
   }

   @BeforeEach
   void setUp()
   {
      resourceSet = new HenshinResourceSet(WORKING_DIRECTORY);
      rulesModule = resourceSet.getModule("tccpa.henshin", false);
      engine = new EngineImpl();

      // Load prior to working with generated code
      new EGraphImpl(resourceSet.getResource("topology-input.xmi"));

      //TccpaPackage.eINSTANCE.eResource().setURI(URI.createFileURI("tccpa.ecore"));
      //resourceSet.getPackageRegistry().put(TccpaPackage.eINSTANCE.getNsURI(), TccpaPackage.eINSTANCE);

      testTopologyResource = resourceSet.getResource(TEMP_XMI_FILE);
   }

   @Test
   void testDynamicVsGeneratedCode()
   {
      EGraph graphDynamic = new EGraphImpl(resourceSet.getResource("topology-input.xmi"));
      EGraph graphGencode = new EGraphImpl(testTopologyResource);

      // Create an engine and a rule application:

      for (final EGraph graph : Arrays.asList(graphDynamic, graphGencode))
      {
         final EObject topology = graph.getRoots().get(0);
         System.out.println(graph);
         {
            UnitApplication removeLink = new UnitApplicationImpl(engine);
            removeLink.setEGraph(graph);
            removeLink.setUnit(rulesModule.getUnit("removeLink"));
            removeLink.setParameterValue("linkId", "n1" + NODE_ID_SEPARATOR + "n2");
            removeLink.setParameterValue("topology", topology);
            Assert.assertTrue(removeLink.execute(null));
         }
         {
            UnitApplication addLink = new UnitApplicationImpl(engine);
            addLink.setEGraph(graph);
            addLink.setUnit(rulesModule.getUnit("addLink"));
            addLink.setParameterValue("srcId", "n2");
            addLink.setParameterValue("trgId", "n1");
            addLink.setParameterValue("linkId", "n1" + NODE_ID_SEPARATOR + "n2");
            addLink.setParameterValue("weight", 3.0);
            addLink.setParameterValue("topology", topology);
            Assert.assertTrue(addLink.execute(null));
         }
      }
   }

   @ParameterizedTest
   @MethodSource("testLinkRemovalPositive")
   void testLinkRemovalPositive(final String linkIdToRemove) throws Exception
   {
      final EGraph graph = new EGraphImpl(testTopologyResource);
      final EObject topology = graph.getRoots().get(0);

      Assert.assertTrue(containsLinkWithId(topology, linkIdToRemove));
      final UnitApplication unit = prepareLinkRemoval(linkIdToRemove, graph, topology, engine, rulesModule);
      Assert.assertTrue(unit.execute(null));
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

      Assert.assertFalse(containsLinkWithId(topology, linkIdToRemove));
      final UnitApplication unit = prepareLinkRemoval(linkIdToRemove, graph, topology, engine, rulesModule);
      int hashCodeBefore = graph.hashCode();
      unit.execute(null);
      int hashCodeAfter = graph.hashCode();
      Assert.assertEquals(hashCodeBefore, hashCodeAfter);
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

   @Test
   void testHandleLinkRemoval1_e13() throws Exception
   {
      final EGraph graph = new EGraphImpl(testTopologyResource);
      final EObject topology = graph.getRoots().get(0);

      final String e13 = createLinkId(1, 3);
      final String e12 = createLinkId(1, 2);
      Assert.assertTrue(containsUnmarkedLinkWithId(topology, e13));
      Assert.assertTrue(prepareLinkInactivation(e13, graph, topology, engine, rulesModule).execute(null));
      Assert.assertTrue(containsInactiveLinkWithId(topology, e13));
      Assert.assertTrue(prepareLinkRemovalHandlerSelf(e12, graph, topology, engine, rulesModule).execute(null));
      Assert.assertTrue(containsUnmarkedLinkWithId(topology, e13));
   }

   @Test
   void testHandleLinkRemoval2_e13() throws Exception
   {
      final EGraph graph = new EGraphImpl(testTopologyResource);
      final EObject topology = graph.getRoots().get(0);

      final String e13 = createLinkId(1, 3);
      final String e23 = createLinkId(2, 3);
      Assert.assertTrue(containsUnmarkedLinkWithId(topology, e13));
      Assert.assertTrue(prepareLinkInactivation(e13, graph, topology, engine, rulesModule).execute(null));
      Assert.assertTrue(containsInactiveLinkWithId(topology, e13));
      Assert.assertTrue(prepareLinkRemovalHandlerRemote(e23, graph, topology, engine, rulesModule).execute(null));
      Assert.assertTrue(containsUnmarkedLinkWithId(topology, e13));
   }

   @Test
   void testHandleLinkRemoval1_e46() throws Exception
   {
      final EGraph graph = new EGraphImpl(testTopologyResource);
      final EObject topology = graph.getRoots().get(0);

      final String e46 = createLinkId(4, 6);
      final String e45 = createLinkId(4, 5);
      Assert.assertTrue(containsUnmarkedLinkWithId(topology, e46));
      Assert.assertTrue(prepareLinkInactivation(e46, graph, topology, engine, rulesModule).execute(null));
      Assert.assertTrue(containsInactiveLinkWithId(topology, e46));
      Assert.assertFalse(prepareLinkRemovalHandlerSelf(e45, graph, topology, engine, rulesModule).execute(null));
      Assert.assertTrue(containsInactiveLinkWithId(topology, e46));
   }

   @Test
   void testHandleLinkRemoval2_e46() throws Exception
   {
      final EGraph graph = new EGraphImpl(testTopologyResource);
      final EObject topology = graph.getRoots().get(0);

      final String e46 = createLinkId(4, 6);
      final String e56 = createLinkId(5, 6);
      Assert.assertTrue(containsUnmarkedLinkWithId(topology, e46));
      Assert.assertTrue(prepareLinkInactivation(e46, graph, topology, engine, rulesModule).execute(null));
      Assert.assertTrue(containsInactiveLinkWithId(topology, e46));
      Assert.assertFalse(prepareLinkRemovalHandlerRemote(e56, graph, topology, engine, rulesModule).execute(null));
      Assert.assertTrue(containsInactiveLinkWithId(topology, e46));
   }

   @ParameterizedTest
   @MethodSource("testLinkAdditionPositive")
   void testLinkAdditionPositive(final String linkIdToAdd) throws Exception
   {
      final EGraph graph = new EGraphImpl(testTopologyResource);
      final EObject topology = graph.getRoots().get(0);

      Assert.assertFalse(containsLinkWithId(topology, linkIdToAdd));
      final UnitApplication unit = prepareLinkAddition(linkIdToAdd, graph, topology, engine, rulesModule);
      Assert.assertTrue(unit.execute(null));
   }

   @SuppressWarnings("unused")
   private static Stream<String> testLinkAdditionPositive()
   {
      //@formatter:off
      return buildLinkIdStream(new Integer[][] {
         {1, 6}, {6, 1}
      });
      //@formatter:on
   }

   @ParameterizedTest
   @MethodSource("testLinkAdditionNegative")
   void testLinkAdditionNegative(final String linkIdToAdd) throws Exception
   {
      final EGraph graph = new EGraphImpl(testTopologyResource);
      final EObject topology = graph.getRoots().get(0);

      Assert.assertTrue(containsLinkWithId(topology, linkIdToAdd));
      final UnitApplication unit = prepareLinkAddition(linkIdToAdd, graph, topology, engine, rulesModule);
      Assert.assertFalse(unit.execute(null));
   }

   @SuppressWarnings("unused")
   private static Stream<String> testLinkAdditionNegative()
   {
      //@formatter:off
      return buildLinkIdStream(new Integer[][] {
         {1, 2}, {2, 1},
         {3, 4}, {4, 3}
      });
      //@formatter:on
   }

   @ParameterizedTest
   @ValueSource(strings = { "nFresh" })
   void testNodeAdditionPositive(final String nodeIdToAdd) throws Exception
   {
      final EGraph graph = new EGraphImpl(testTopologyResource);
      final EObject topology = graph.getRoots().get(0);

      final UnitApplication unit = prepareNodeAddition(nodeIdToAdd, graph, topology, engine, rulesModule);
      if (!unit.execute(null))
      {
         Assert.fail();
      }
      Assert.assertTrue(containsNodeWithId(topology, nodeIdToAdd));
   }

   @ParameterizedTest
   @ValueSource(strings = { "n1", "n2", "n3", "n4", "n5", "n6" })
   void testNodeAdditionNegative(final String nodeIdToAdd) throws Exception
   {
      final EGraph graph = new EGraphImpl(testTopologyResource);
      final EObject topology = graph.getRoots().get(0);

      final UnitApplication unit = prepareNodeAddition(nodeIdToAdd, graph, topology, engine, rulesModule);
      if (unit.execute(null))
      {
         Assert.fail();
      }
   }

   @Test
   void testNodeRemovalPositive_Node1() throws Exception
   {
      final EGraph graph = new EGraphImpl(testTopologyResource);
      final EObject topology = graph.getRoots().get(0);

      Assert.assertTrue(prepareLinkRemoval("n1->n2", graph, topology, engine, rulesModule).execute(null));
      Assert.assertTrue(prepareLinkRemoval("n1->n3", graph, topology, engine, rulesModule).execute(null));
      Assert.assertTrue(prepareLinkRemoval("n2->n1", graph, topology, engine, rulesModule).execute(null));
      Assert.assertTrue(prepareLinkRemoval("n3->n1", graph, topology, engine, rulesModule).execute(null));
      Assert.assertFalse(containsLinkWithId(topology, "n1->n2"));
      Assert.assertFalse(containsLinkWithId(topology, "n2->n1"));
      Assert.assertFalse(containsLinkWithId(topology, "n1->n2"));
      Assert.assertFalse(containsLinkWithId(topology, "n1->n3"));
      final UnitApplication unit = prepareNodeRemoval("n1", graph, topology, engine, rulesModule);
      Assert.assertTrue(unit.execute(null));
      Assert.assertFalse(containsNodeWithId(topology, "n1"));
   }

   @ParameterizedTest
   @ValueSource(strings = { "nFresh" })
   void testNodeRemovalPositive_FreshNode(final String isolatedNodeId) throws Exception
   {
      final EGraph graph = new EGraphImpl(testTopologyResource);
      final EObject topology = graph.getRoots().get(0);
      Assert.assertTrue(prepareNodeAddition(isolatedNodeId, graph, topology, engine, rulesModule).execute(null));
      Assert.assertTrue(containsNodeWithId(topology, isolatedNodeId));
      final UnitApplication unit = prepareNodeRemoval(isolatedNodeId, graph, topology, engine, rulesModule);
      Assert.assertTrue(unit.execute(null));
      Assert.assertFalse(containsNodeWithId(topology, isolatedNodeId));
   }

   @ParameterizedTest
   @ValueSource(strings = { "n1", "n2", "n3", "n4", "n5", "n6" })
   void testNodeRemovalNegative(final String nodeIdToRemove) throws Exception
   {
      final EGraph graph = new EGraphImpl(testTopologyResource);
      final EObject topology = graph.getRoots().get(0);

      final UnitApplication unit = prepareNodeRemoval(nodeIdToRemove, graph, topology, engine, rulesModule);
      Assert.assertFalse(unit.execute(null));
   }

   @Test
   void testModifyLinkWeight_e34() throws Exception
   {
      final EGraph graph = new EGraphImpl(testTopologyResource);
      final EObject topology = graph.getRoots().get(0);

      final String linkId = "n3->n4";
      Assert.assertTrue(containsLinkWithId(topology, linkId));
      final UnitApplication unit = prepareLinkWeightModification(linkId, 7, graph, topology, engine, rulesModule);
      Assert.assertTrue(unit.execute(null));
      Assert.assertTrue(containsLinkWithIdAndWeight(topology, linkId, 7));
   }

   @ParameterizedTest
   @MethodSource("testActivateLinkPositive")
   void testActivateLinkPositive(final String linkToBeActivated) throws Exception
   {
      final EGraph graph = new EGraphImpl(testTopologyResource);
      final EObject topology = graph.getRoots().get(0);

      Assert.assertTrue(containsLinkWithId(topology, linkToBeActivated));
      final UnitApplication unit = prepareLinkActivation(linkToBeActivated, graph, topology, engine, rulesModule);
      Assert.assertTrue(unit.execute(null));
      Assert.assertTrue(containsActiveLinkWithId(topology, linkToBeActivated));
   }

   static Stream<String> testActivateLinkPositive()
   {
      //@formatter:off
      return buildLinkIdStream(new Integer[][] {
            {1, 2}, {2, 1},
            {2, 3}, {3, 2},
            {3, 4}, {4, 3},
            {4, 5}, {5, 4},
            {5, 6}, {6, 5},
      });
      //@formatter:on
   }

   @ParameterizedTest
   @MethodSource("testActivateLinkNegative")
   void testActivateLinkNegative(final String linkToBeActivated) throws Exception
   {
      final EGraph graph = new EGraphImpl(testTopologyResource);
      final EObject topology = graph.getRoots().get(0);

      Assert.assertTrue(containsLinkWithId(topology, linkToBeActivated));
      final UnitApplication unit = prepareLinkActivation(linkToBeActivated, graph, topology, engine, rulesModule);
      Assert.assertFalse(unit.execute(null));
   }

   static Stream<String> testActivateLinkNegative()
   {
      //@formatter:off
      return buildLinkIdStream(new Integer[][] {
         {1, 3}, {3, 1},
         {4, 6}, {6, 4}
      });
      //@formatter:on
   }

   @ParameterizedTest
   @MethodSource("testInactivateLinkPositive")
   void testInactivateLinkPositive(final String linkToBeInactivated) throws Exception
   {
      final EGraph graph = new EGraphImpl(testTopologyResource);
      final EObject topology = graph.getRoots().get(0);

      Assert.assertTrue(containsLinkWithId(topology, linkToBeInactivated));
      final UnitApplication unit = prepareLinkInactivation(linkToBeInactivated, graph, topology, engine, rulesModule);
      Assert.assertTrue(unit.execute(null));
      Assert.assertTrue(containsInactiveLinkWithId(topology, linkToBeInactivated));
   }

   static Stream<String> testInactivateLinkPositive()
   {
      //@formatter:off
      return buildLinkIdStream(new Integer[][] {
         {1, 3}, {3, 1},
         {4, 6}, {6, 4}
      });
      //@formatter:on
   }

   @ParameterizedTest
   @MethodSource("testInactivateLinkNegative")
   void testInactivateLinkNegative(final String linkIdToBeInactivated) throws Exception
   {
      final EGraph graph = new EGraphImpl(testTopologyResource);
      final EObject topology = graph.getRoots().get(0);

      Assert.assertTrue(containsLinkWithId(topology, linkIdToBeInactivated));
      final UnitApplication unit = prepareLinkInactivation(linkIdToBeInactivated, graph, topology, engine, rulesModule);
      Assert.assertFalse(unit.execute(null));
   }

   static Stream<String> testInactivateLinkNegative()
   {
      //@formatter:off
      return buildLinkIdStream(new Integer[][] {
         {1, 2}, {2, 1},
         {2, 3}, {3, 2},
         {3, 4}, {4, 3},
         {4, 5}, {5, 4},
         {5, 6}, {6, 5},
      });
      //@formatter:on
   }

   private static boolean containsNodeWithId(final EObject topology, final String nodeIdToCheck)
   {
      final List<? extends EObject> nodes = getNodes(topology);
      return nodes.stream().map(o -> o.eGet(findIdStructuralFeature(o))).anyMatch(nodeId -> {
         return nodeIdToCheck.equals(nodeId);
      });
   }

   private static boolean containsLinkWithId(final EObject topology, final String linkId)
   {
      return getLinks(topology).stream().map(o -> o.eGet(findIdStructuralFeature(o))).anyMatch(id -> {
         return linkId.equals(id);
      });
   }

   private static boolean containsLinkWithIdAndWeight(final EObject topology, final String linkId, final Integer weight)
   {
      return getLinks(topology).stream().filter(o -> linkId.equals(o.eGet(findIdStructuralFeature(o)))).filter(o -> {
         return Math.abs(weight - (Double) o.eGet(findWeightStructuralFeature(o))) < 1e-7;
      }).findAny().isPresent();
   }

   private boolean containsActiveLinkWithId(final EObject topology, final String linkId)
   {
      return containsLinkWithStateAndId(topology, linkId, LinkState.ACTIVE);
   }

   private boolean containsInactiveLinkWithId(final EObject topology, final String linkId)
   {
      return containsLinkWithStateAndId(topology, linkId, LinkState.INACTIVE);
   }

   private boolean containsUnmarkedLinkWithId(final EObject topology, final String linkId)
   {
      return containsLinkWithStateAndId(topology, linkId, LinkState.UNMARKED);
   }

   private boolean containsLinkWithStateAndId(final EObject topology, final String linkId, final int state)
   {
      return getLinks(topology).stream().filter(o -> linkId.equals(o.eGet(findIdStructuralFeature(o)))).filter(link -> hasState(link, state)).findAny()
            .isPresent();
   }

   private boolean hasState(final EObject link, final Integer expectedState)
   {
      return expectedState.equals(link.eGet(findStateStructuralFeature(link)));
   }

   @SuppressWarnings("unchecked")
   private static List<? extends EObject> getNodes(final EObject topology)
   {
      return (List<EObject>) topology.eGet(findFeatureByName(topology, "nodes"));
   }

   @SuppressWarnings("unchecked")
   private static List<? extends EObject> getLinks(final EObject topology)
   {
      return (List<EObject>) topology.eGet(findFeatureByName(topology, "links"));
   }

   private static EStructuralFeature findIdStructuralFeature(final EObject o)
   {
      return findFeatureByName(o, "id");
   }

   private static EStructuralFeature findWeightStructuralFeature(final EObject o)
   {
      return findFeatureByName(o, "weight");
   }

   private static EStructuralFeature findStateStructuralFeature(final EObject o)
   {
      return findFeatureByName(o, "state");
   }

   private static EStructuralFeature findFeatureByName(final EObject o, final String featureName)
   {
      final Optional<EStructuralFeature> maybeFeature = o.eClass().getEAllStructuralFeatures().stream().filter(f -> featureName.equals(f.getName())).findAny();
      if (maybeFeature.isPresent())
      {
         return maybeFeature.get();
      } else
      {
         throw new IllegalArgumentException(String.format("Cannot find feature '%s' in class '%s' of %s", featureName, o.eClass(), o));
      }
   }

   private static UnitApplication prepareNodeAddition(final String nodeIdToAdd, final EGraph graph, final EObject topology, final Engine engine,
         Module rulesModule)
   {
      final UnitApplication unit = new UnitApplicationImpl(engine);
      unit.setEGraph(graph);
      unit.setUnit(rulesModule.getUnit("addNode"));
      unit.setParameterValue("nodeId", nodeIdToAdd);
      unit.setParameterValue("topology", topology);
      return unit;
   }

   private static UnitApplication prepareNodeRemoval(final String nodeIdToRemove, final EGraph graph, final EObject topology, final Engine engine,
         Module rulesModule)
   {
      final UnitApplication unit = new UnitApplicationImpl(engine);
      unit.setEGraph(graph);
      unit.setUnit(rulesModule.getUnit("removeNode"));
      unit.setParameterValue("nodeId", nodeIdToRemove);
      unit.setParameterValue("topology", topology);
      return unit;
   }

   private static UnitApplication prepareLinkRemoval(final String linkIdToRemove, final EGraph graph, final EObject topology, final Engine engine,
         Module rulesModule)
   {
      final UnitApplication unit = new UnitApplicationImpl(engine);
      unit.setEGraph(graph);
      unit.setUnit(rulesModule.getUnit("removeLink"));
      unit.setParameterValue("linkId", linkIdToRemove);
      unit.setParameterValue("topology", topology);
      return unit;
   }

   private static UnitApplication prepareLinkRemovalHandlerSelf(final String linkIdToRemove, final EGraph graph, final EObject topology, final Engine engine,
         Module rulesModule)
   {
      final UnitApplication unit = new UnitApplicationImpl(engine);
      unit.setEGraph(graph);
      unit.setUnit(rulesModule.getUnit("handleLinkRemoval1"));
      unit.setParameterValue("linkId", linkIdToRemove);
      unit.setParameterValue("topology", topology);
      return unit;
   }

   private static UnitApplication prepareLinkRemovalHandlerRemote(final String linkIdToRemove, final EGraph graph, final EObject topology, final Engine engine,
         Module rulesModule)
   {
      final UnitApplication unit = new UnitApplicationImpl(engine);
      unit.setEGraph(graph);
      unit.setUnit(rulesModule.getUnit("handleLinkRemoval2"));
      unit.setParameterValue("linkId", linkIdToRemove);
      unit.setParameterValue("topology", topology);
      return unit;
   }

   private static UnitApplication prepareLinkAddition(final String linkIdToAdd, final EGraph graph, final EObject topology, final Engine engine,
         Module rulesModule)
   {
      final String srcId = extractNodeIds(linkIdToAdd)[0];
      final String trgId = extractNodeIds(linkIdToAdd)[1];
      final double weight = 1.0;
      final UnitApplication unit = new UnitApplicationImpl(engine);
      unit.setEGraph(graph);
      unit.setUnit(rulesModule.getUnit("addLink"));
      unit.setParameterValue("srcId", srcId);
      unit.setParameterValue("trgId", trgId);
      unit.setParameterValue("linkId", linkIdToAdd);
      unit.setParameterValue("weight", weight);
      unit.setParameterValue("topology", topology);
      return unit;
   }

   private static UnitApplication prepareLinkWeightModification(final String linkIdToBeModified, final int newWeight, final EGraph graph,
         final EObject topology, final Engine engine, Module rulesModule)
   {
      final UnitApplication unit = new UnitApplicationImpl(engine);
      unit.setEGraph(graph);
      unit.setUnit(rulesModule.getUnit("modifyLinkWeight"));
      unit.setParameterValue("linkId", linkIdToBeModified);
      unit.setParameterValue("newWeight", newWeight);
      unit.setParameterValue("topology", topology);
      return unit;
   }

   private static UnitApplication prepareLinkActivation(final String linkIdToBeModified, final EGraph graph, final EObject topology, final Engine engine,
         Module rulesModule)
   {
      final UnitApplication unit = new UnitApplicationImpl(engine);
      unit.setEGraph(graph);
      unit.setUnit(rulesModule.getUnit("activateLink"));
      unit.setParameterValue("linkId", linkIdToBeModified);
      unit.setParameterValue("topology", topology);
      return unit;
   }

   private static UnitApplication prepareLinkInactivation(final String linkIdToBeModified, final EGraph graph, final EObject topology, final Engine engine,
         Module rulesModule)
   {
      final UnitApplication unit = new UnitApplicationImpl(engine);
      unit.setEGraph(graph);
      unit.setUnit(rulesModule.getUnit("inactivateLink"));
      unit.setParameterValue("linkId", linkIdToBeModified);
      unit.setParameterValue("topology", topology);
      return unit;
   }

   private static Stream<String> buildLinkIdStream(final Integer[][] nodePairs)
   {
      return Arrays.asList(nodePairs).stream().map(pair -> createLinkId(pair));
   }

   private static String createLinkId(Integer[] pair)
   {
      final Integer sourceNode = pair[0];
      final Integer targetNode = pair[1];
      return createLinkId(sourceNode, targetNode);
   }

   private static String createLinkId(final int sourceNode, final int targetNode)
   {
      return String.format("n%d%sn%d", sourceNode, NODE_ID_SEPARATOR, targetNode);
   }

   private static Node createNode(final String nodeId, final Topology topology)
   {
      final Node node = TccpaFactory.eINSTANCE.createNode();
      topology.getNodes().add(node);
      node.setTopology(topology);
      node.setId(nodeId);
      return node;
   }

   private static Link createLink(final Node fromNode, final Node toNode, final double weight, final Topology topology)
   {
      final Link link = TccpaFactory.eINSTANCE.createLink();
      topology.getLinks().add(link);
      link.setTopology(topology);
      link.setId(String.format("%s%s%s", fromNode.getId(), NODE_ID_SEPARATOR, toNode.getId()));
      link.setSource(fromNode);
      link.setTarget(toNode);
      link.setWeight(weight);
      return link;
   }

   private static Link createLink(final List<Node> nodes, List<Integer> linkSpec, final Topology topology)
   {
      final Node fromNode = nodes.get(linkSpec.get(0) - 1);
      final Node toNode = nodes.get(linkSpec.get(1) - 1);
      final int weight = linkSpec.get(2);
      return createLink(fromNode, toNode, weight, topology);
   }

   private static List<Integer> createLinkSpec(int fromNodeId, int toNodeId, int weight)
   {
      return Arrays.asList(fromNodeId, toNodeId, weight);
   }

   private static String[] extractNodeIds(final String linkId)
   {
      return linkId.split(Pattern.quote(NODE_ID_SEPARATOR));
   }
}
