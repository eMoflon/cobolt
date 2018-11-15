package de.tudarmstadt.maki.simonstrator.tc.underlay;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;

public class UnderlayTopologyControlAlgorithmsTest
{

   @Test
   public void testNameToIdMapping() throws Exception
   {
      for (final TopologyControlAlgorithmID id : UnderlayTopologyControlAlgorithms.getAlgorithms())
      {
         Assert.assertEquals(id, UnderlayTopologyControlAlgorithms.mapToTopologyControlID(id.getName()));
      }
   }
   
   @Test
   public void testAlgorithmIdsAreUnique() throws Exception
   {
      final Set<Integer> knownIds = new HashSet<>();
      for (final TopologyControlAlgorithmID id : UnderlayTopologyControlAlgorithms.getAlgorithms())
      {
         final Integer uniqueId = id.getUniqueId();
         if (!knownIds.add(uniqueId))
         {
            Assert.fail("Non-unique ID found: " + id);
         }
      }
   }
}
