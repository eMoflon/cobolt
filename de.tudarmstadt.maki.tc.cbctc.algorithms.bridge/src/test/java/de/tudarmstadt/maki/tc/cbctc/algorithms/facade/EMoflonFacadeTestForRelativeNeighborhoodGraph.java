package de.tudarmstadt.maki.tc.cbctc.algorithms.facade;

import static de.tudarmstadt.maki.tc.cbctc.algorithms.TopologyControlAlgorithmsTestUtils.getPathToEnergyTestGraph;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;
import de.tudarmstadt.maki.simonstrator.tc.weighting.EdgeWeightProviders;
import de.tudarmstadt.maki.simonstrator.tc.weighting.InverseEstimatedRemainingLifetimeWeightProvider;
import de.tudarmstadt.maki.tc.cbctc.algorithms.TopologyControlAlgorithmsTestUtils;
import de.tudarmstadt.maki.tc.cbctc.model.TopologyModelTestUtils;

/**
 * Tests for implementation of {@link UnderlayTopologyControlAlgorithms#RELATIVE_NEIGHBORHOOD_GRAPH}
 * @author Roland Kluge - Initial implementation
 *
 */
public class EMoflonFacadeTestForRelativeNeighborhoodGraph extends AbstractEMoflonFacadeTest
{

   @Override
   protected TopologyControlAlgorithmID getAlgorithmID()
   {
      return UnderlayTopologyControlAlgorithms.RELATIVE_NEIGHBORHOOD_GRAPH;
   }

   @Test
   public void testWithTestgraphE1() throws Exception
   {
      reader.read(this.facade, new FileInputStream(new File(getPathToEnergyTestGraph(1))));
      EdgeWeightProviders.apply(this.facade, InverseEstimatedRemainingLifetimeWeightProvider.INSTANCE);

      TopologyModelTestUtils.assertUnclassified(this.facade.getTopology());
      TopologyControlAlgorithmsTestUtils.assertWeightSet(this.facade);

      TopologyControlAlgorithmsTestUtils.runFacadeKTC(this.facade, -1.0);

      TopologyModelTestUtils.assertActiveWithExceptions(this.facade.getTopology(), false, "e13");
      
      this.facade.checkConstraintsAfterTopologyControl();
      Assert.assertEquals(0, this.facade.getConstraintViolationCount());
   }
}
