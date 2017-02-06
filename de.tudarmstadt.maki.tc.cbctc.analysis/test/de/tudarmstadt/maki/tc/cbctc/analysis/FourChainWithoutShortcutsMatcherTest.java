package de.tudarmstadt.maki.tc.cbctc.analysis;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.maki.tc.cbctc.model.Edge;
import de.tudarmstadt.maki.tc.cbctc.model.ModelFactory;
import de.tudarmstadt.maki.tc.cbctc.model.Node;
import de.tudarmstadt.maki.tc.cbctc.model.Topology;

public class FourChainWithoutShortcutsMatcherTest
{
   AnalysisFactory factory = AnalysisFactory.eINSTANCE;

   ModelFactory mf = ModelFactory.eINSTANCE;

   private FourChainWithoutShortcutsMatcher matcher;

   @Before
   public void setUp()
   {

      matcher = factory.createFourChainWithoutShortcutsMatcher();
   }

   @Test
   public void testSingleMatch() throws Exception
   {
      Topology t = createFourChainWithoutShortcuts();

      Assert.assertEquals(1, matcher.count(t));
   }

   @Test
   public void testNoMatchDueToShortcut() throws Exception
   {
      Topology topology = createFourChainWithoutShortcuts();

      Edge shortcut1 = mf.createEdge();
      topology.getEdges().add(shortcut1);
      shortcut1.setSource(topology.getNodes().get(0));
      shortcut1.setTarget(topology.getNodes().get(2));

      Assert.assertEquals(0, matcher.count(topology));
   }

   @Test
   public void testNoMatchDueToShortcut2() throws Exception
   {
      Topology topology = createFourChainWithoutShortcuts();

      Edge shortcut1 = mf.createEdge();
      topology.getEdges().add(shortcut1);
      shortcut1.setSource(topology.getNodes().get(0));
      shortcut1.setTarget(topology.getNodes().get(3));

      Assert.assertEquals(0, matcher.count(topology));
   }

   @Test
   public void testNoMatchDueToShortcut3() throws Exception
   {
      Topology topology = createFourChainWithoutShortcuts();

      {
         Edge shortcut1 = mf.createEdge();
         topology.getEdges().add(shortcut1);
         shortcut1.setSource(topology.getNodes().get(0));
         shortcut1.setTarget(topology.getNodes().get(2));

      }
      {
         Edge shortcut1 = mf.createEdge();
         topology.getEdges().add(shortcut1);
         shortcut1.setSource(topology.getNodes().get(0));
         shortcut1.setTarget(topology.getNodes().get(3));
      }

      Assert.assertEquals(0, matcher.count(topology));
   }

   private Topology createFourChainWithoutShortcuts()
   {
      Topology t = mf.createTopology();
      for (int i = 0; i < 4; ++i)
      {
         Node node = mf.createNode();
         t.getNodes().add(node);
         node.setId("n" + i);
      }
      for (int i = 0; i < 3; ++i)
      {
         Node node1 = t.getNodes().get(i);
         Node node2 = t.getNodes().get(i + 1);
         Edge edge = mf.createEdge();
         t.getEdges().add(edge);
         edge.setSource(node1);
         edge.setTarget(node2);
         edge.setId("e" + i + "" + (i + 1));
      }
      return t;
   }

}
