package org.cobolt.ngctoac;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.interpreter.UnitApplication;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.interpreter.impl.EngineImpl;
import org.eclipse.emf.henshin.interpreter.impl.UnitApplicationImpl;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.Unit;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TopologyControlRuleTests
{
   private static final String RULES_FILE = "tc.henshin";

   private static final String NODE_ID_SEPARATOR = "->";

   private static final String TOPOLOGY_INSTANCE_TWO_NODES = "../../../../instance/Topology.xmi";

   private HenshinResourceSet resourceSet;

   private Module rulesModule;

   private Engine engine;

   private EGraphImpl graph;

   @Before
   public void setUp()
   {
      this.resourceSet = new HenshinResourceSet(HenshinRules.getRulesDirectory());
      this.rulesModule = resourceSet.getModule(RULES_FILE, false);
      this.engine = new EngineImpl();
      final Resource testTopologyResource = resourceSet.getResource(TOPOLOGY_INSTANCE_TWO_NODES);
      this.graph = new EGraphImpl(testTopologyResource);
   }

   @Test
   public void testTestmodelValidity()
   {
      final EObject topology = getTopology();
      Assert.assertEquals(3, getNodes(topology).size());
      Assert.assertEquals(0, getLinks(topology).size());
   }

   @Test
   public void testAddLinkBetweenNodes()
   {
      final EObject topology = getTopology();

      final String linkIdToAdd = "1->2";

      Assert.assertFalse(containsLinkWithId(topology, linkIdToAdd));
      Assert.assertTrue(addLink(linkIdToAdd));
      Assert.assertTrue(containsLinkWithId(topology, linkIdToAdd));
      Assert.assertTrue(containsLinkWithIdAndWeight(topology, linkIdToAdd, 1));
      Assert.assertTrue(containsLinkWithStateAndId(topology, linkIdToAdd, LinkState.UNMARKED));
   }

   @Test
   public void testSetLinkState()
   {
      final EObject topology = getTopology();

      final String linkId = "1->2";

      Assert.assertTrue(addLink(linkId));
      Assert.assertTrue(setLinkState(linkId, LinkState.INACTIVE));
      Assert.assertTrue(containsLinkWithStateAndId(topology, linkId, LinkState.INACTIVE));
      Assert.assertTrue(setLinkState(linkId, LinkState.ACTIVE));
      Assert.assertTrue(containsLinkWithStateAndId(topology, linkId, LinkState.ACTIVE));
   }

   @Test
   public void testCreateTriangleSuccessful()
   {
      final EObject topology = getTopology();

      for (final String linkIdToAdd : Arrays.asList("1->2", "1->3", "3->2"))
      {
         Assert.assertTrue(addLink(linkIdToAdd));
      }
      Assert.assertEquals(3, getLinks(topology).size());
   }

   /**
    * This test case illustrates that closing creating a triangle on top of an inactive link is allowed
    */
   @Test
   public void testCreateTriangleAllowedByApplicationCondition()
   {
      Assert.assertTrue(addLinkWithTrianglePreventingAC("1->2", 7));
      Assert.assertTrue(setLinkState("1->2", LinkState.INACTIVE));
      Assert.assertTrue(addLinkWithTrianglePreventingAC("1->3", 2));

      Assert.assertTrue(addLinkWithTrianglePreventingAC("3->2", 3));
   }

   /**
    * This test case illustrates that closing creating a triangle on top of an active link is forbidden
    */
   @Test
   public void testCreateTriangleForbiddenByApplicationCondition()
   {
      Assert.assertTrue(addLinkWithTrianglePreventingAC("1->2", 7));
      Assert.assertTrue(setLinkState("1->2", LinkState.ACTIVE));
      Assert.assertTrue(addLinkWithTrianglePreventingAC("1->3", 1));

      Assert.assertFalse(addLinkWithTrianglePreventingAC("3->2", 6));
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

   private static boolean containsLinkWithStateAndId(final EObject topology, final String linkId, final int state)
   {
      return getLinks(topology).stream().filter(o -> linkId.equals(o.eGet(findIdStructuralFeature(o)))).filter(link -> hasState(link, state)).findAny()
            .isPresent();
   }

   private static boolean hasState(final EObject link, final Integer expectedState)
   {
      return expectedState.equals(link.eGet(findStateStructuralFeature(link)));
   }

   @SuppressWarnings("unchecked")
   private static List<? extends EObject> getLinks(final EObject topology)
   {
      return (List<EObject>) topology.eGet(findFeatureByName(topology, "links"));
   }

   @SuppressWarnings("unchecked")
   private static List<? extends EObject> getNodes(final EObject topology)
   {
      return (List<EObject>) topology.eGet(findFeatureByName(topology, "nodes"));
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

   private boolean addLink(final String linkIdToAdd)
   {
      return prepareLinkAddition(linkIdToAdd).execute(null);
   }

   private UnitApplication prepareLinkAddition(final String linkIdToAdd)
   {
      final EObject topology = getTopology();
      final String srcId = extractSourceNodeId(linkIdToAdd);
      final String trgId = extractTargetNodeId(linkIdToAdd);
      final double weight = 1.0;
      final UnitApplication unit = new UnitApplicationImpl(engine);
      unit.setEGraph(graph);
      unit.setUnit(getUnitChecked(rulesModule, "addLink"));
      unit.setParameterValue("srcId", srcId);
      unit.setParameterValue("trgId", trgId);
      unit.setParameterValue("linkId", linkIdToAdd);
      unit.setParameterValue("weight", weight);
      unit.setParameterValue("topology", topology);
      return unit;
   }

   private boolean addLinkWithTrianglePreventingAC(final String linkIdToAdd, final double weight)
   {
      return prepareLinkAdditionWithTrianglePreventingAC(linkIdToAdd, weight).execute(null);
   }

   private UnitApplication prepareLinkAdditionWithTrianglePreventingAC(final String linkIdToAdd, final double weight)
   {
      final EObject topology = getTopology();
      final String srcId = extractSourceNodeId(linkIdToAdd);
      final String trgId = extractTargetNodeId(linkIdToAdd);
      final UnitApplication unit = new UnitApplicationImpl(engine);
      unit.setEGraph(graph);
      unit.setUnit(getUnitChecked(rulesModule, "addLink_updated_No Triangle"));
      unit.setParameterValue("srcId", srcId);
      unit.setParameterValue("trgId", trgId);
      unit.setParameterValue("linkId", linkIdToAdd);
      unit.setParameterValue("weight", weight);
      unit.setParameterValue("topology", topology);
      return unit;
   }

   private boolean setLinkState(final String linkId, final int newState)
   {
      return prepareSetLinkState(linkId, newState).execute(null);
   }

   private UnitApplication prepareSetLinkState(final String linkId, final int newState)
   {
      final EObject topology = getTopology();
      final UnitApplication unit = new UnitApplicationImpl(engine);
      unit.setEGraph(graph);
      unit.setUnit(getUnitChecked(rulesModule, "setLinkState"));
      unit.setParameterValue("linkId", linkId);
      unit.setParameterValue("newState", newState);
      unit.setParameterValue("topology", topology);
      return unit;
   }

   private static String[] extractNodeIds(final String linkId)
   {
      if (!linkId.contains(NODE_ID_SEPARATOR))
         throw new IllegalArgumentException(String.format("Link ID %s should contain separator token %s", linkId, NODE_ID_SEPARATOR));
      return linkId.split(Pattern.quote(NODE_ID_SEPARATOR));
   }

   private static String extractSourceNodeId(final String linkId)
   {
      return extractNodeIds(linkId)[0];
   }

   private static String extractTargetNodeId(final String linkId)
   {
      return extractNodeIds(linkId)[1];
   }

   /**
    * Extracts the {@link Unit} with the given name from the given {@link Module} (if exists)
    *
    * If no such unit exists, an exception is thrown
    * @param rulesModule the module
    * @param unitName the unit
    * @return the {@link Unit} if exists
    */
   private static Unit getUnitChecked(final Module rulesModule, final String unitName)
   {
      final Unit unit = rulesModule.getUnit(unitName);
      if (unit == null)
         throw new IllegalArgumentException(String.format("No unit with name %s in module %s", unitName, rulesModule));
      else
         return unit;
   }

   /**
    * Extracts the topology object from the graph.
    * By convention, the topology is always the first root.
    * @return the extracted topology
    */
   private EObject getTopology()
   {
      return this.graph.getRoots().get(0);
   }
}