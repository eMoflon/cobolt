package org.cobolt.ngctoac;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.interpreter.UnitApplication;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.interpreter.impl.EngineImpl;
import org.eclipse.emf.henshin.interpreter.impl.UnitApplicationImpl;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.Unit;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TopologyControlRuleTests
{
   private static final String RULES_FILE = "tc.henshin";

   private static final String NODE_ID_SEPARATOR = "->";

   private static final String TOPOLOGY_INSTANCE_TWO_NODES = "../../../../instance/Topology.xmi";

   private HenshinResourceSet resourceSet;

   private Resource testTopologyResource;

   private Module rulesModule;

   private Engine engine;


   @BeforeEach
   void setUp()
   {
      resourceSet = new HenshinResourceSet(HenshinRules.getRulesDirectory());
      rulesModule = resourceSet.getModule(RULES_FILE, false);
      engine = new EngineImpl();
      testTopologyResource = resourceSet.getResource(TOPOLOGY_INSTANCE_TWO_NODES);
   }

   @Test
   public void testAddLinkBetweenNodes() {
      final EGraph graph = new EGraphImpl(testTopologyResource);
      final EObject topology = graph.getRoots().get(0);

      final String linkIdToAdd = "1->2";

      Assert.assertFalse(containsLinkWithId(topology, linkIdToAdd));
      final UnitApplication unit = prepareLinkAddition(linkIdToAdd, graph, topology, engine, rulesModule);
      Assert.assertTrue(unit.execute(null));
      Assert.assertTrue(containsLinkWithId(topology, linkIdToAdd));
      Assert.assertTrue(containsLinkWithIdAndWeight(topology, linkIdToAdd, 1));
      Assert.assertTrue(containsLinkWithStateAndId(topology, linkIdToAdd, LinkState.UNMARKED));
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

   private static UnitApplication prepareLinkAddition(final String linkIdToAdd, final EGraph graph, final EObject topology, final Engine engine,
         final Module rulesModule)
   {
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

   private static String[] extractNodeIds(final String linkId)
   {
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
}