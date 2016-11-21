package de.tudarmstadt.maki.tc.cbctc.algorithms.facade;

import static de.tudarmstadt.maki.tc.cbctc.algorithms.TopologyControlAlgorithmsTestUtils.getPathToEnergyTestGraph;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;
import de.tudarmstadt.maki.tc.cbctc.model.TopologyTestUtils;


/**
 * Tests for implementation of {@link UnderlayTopologyControlAlgorithms#MAXPOWER_TC}
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class EMoflonFacadeTestForMaxpower extends AbstractEMoflonFacadeTest
{

   @Override
   protected TopologyControlAlgorithmID getAlgorithmID()
   {
      return UnderlayTopologyControlAlgorithms.MAXPOWER_TC;
   }

   @Test
   public void testWithTestgraphE1() throws Exception
   {
      reader.read(this.facade, new FileInputStream(new File(getPathToEnergyTestGraph(1))));

      TopologyTestUtils.assertUnclassified(this.facade.getTopology());

      this.facade.run(-1.0);

      TopologyTestUtils.assertActiveWithExceptions(this.facade.getTopology(), true);
   }
}
