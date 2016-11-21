package de.tudarmstadt.maki.tc.cbctc.algorithms.facade;

import static de.tudarmstadt.maki.tc.cbctc.algorithms.TopologyControlAlgorithmsTestUtils.getPathToEnergyTestGraph;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;
import de.tudarmstadt.maki.simonstrator.tc.weighting.EdgeWeightProviders;
import de.tudarmstadt.maki.simonstrator.tc.weighting.InverseEstimatedRemainingLifetimeWeightProvider;
import de.tudarmstadt.maki.tc.cbctc.algorithms.TopologyControlAlgorithmsTestUtils;
import de.tudarmstadt.maki.tc.cbctc.model.TopologyTestUtils;

public class EMoflonFacadeTestForGabrielGraph extends AbstractEMoflonFacadeTest
{

   @Override
   protected TopologyControlAlgorithmID getAlgorithmID()
   {
      return UnderlayTopologyControlAlgorithms.GABRIEL_GRAPH;
   }

   @Test
   public void testWithTestgraphE1() throws Exception
   {
      reader.read(this.facade, new FileInputStream(new File(getPathToEnergyTestGraph(1))));
      EdgeWeightProviders.apply(this.facade, InverseEstimatedRemainingLifetimeWeightProvider.INSTANCE);

      TopologyTestUtils.assertUnclassified(this.facade.getTopology());
      TopologyControlAlgorithmsTestUtils.assertWeightSet(this.facade);

      this.facade.run(-1.0);

      TopologyTestUtils.assertActiveWithExceptions(this.facade.getTopology(), true);
   }
}
