package de.tudarmstadt.maki.simonstrator.tc.testing;

import org.junit.Assert;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.util.UtilityClassNotInstantiableException;

public final class GraphTestUtil
{
   public GraphTestUtil()
   {
      throw new UtilityClassNotInstantiableException();
   }

   public static void assertNodeAndEdgeCount(final int expectedNodeCount, final int expectedEdgeCount, final Graph actualGraph)
   {
      assertNodeCount(expectedNodeCount, actualGraph);
      assertEdgeCount(expectedEdgeCount, actualGraph);
   }

   public static void assertEdgeCount(final int expectedEdgeCount, final Graph actualGraph)
   {
      Assert.assertEquals("Edge count mismatch!", expectedEdgeCount, actualGraph.getEdgeCount());
   }

   public static void assertNodeCount(final int expectedNodeCount, final Graph actualGraph)
   {
      Assert.assertEquals("Node count mismatch!", expectedNodeCount, actualGraph.getNodeCount());
   }
}
