package org.cobolt.algorithms.facade;

import static org.cobolt.algorithms.TopologyControlAlgorithmsTestUtils.getPathToAngleTestGraph;

import java.io.FileInputStream;

import org.cobolt.model.TopologyModelTestUtils;
import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;
import org.cobolt.algorithms.TopologyControlAlgorithmsTestUtils;

/**
 * Tests for implementation of {@link UnderlayTopologyControlAlgorithms#GABRIEL_GRAPH}
 * @author Roland Kluge - Initial implementation
 *
 */
public class EMoflonFacadeTestForYao extends AbstractEMoflonFacadeTest
{

   @Override
   protected TopologyControlAlgorithmID getAlgorithmID()
   {
      return UnderlayTopologyControlAlgorithms.YAO;
   }

   @Test
   public void testWithTestgraphA1_4Cones() throws Exception
   {
      this.reader.read(this.facade, new FileInputStream(getPathToAngleTestGraph(1)));

      TopologyModelTestUtils.assertUnclassified(this.facade.getTopology());
      TopologyControlAlgorithmsTestUtils.assertWeightSet(this.facade);

      TopologyControlAlgorithmsTestUtils.runFacadeYao(this.facade, 4);

      TopologyModelTestUtils.assertActiveWithExceptions(this.facade.getTopology(), false);
      
      this.facade.checkConstraintsAfterTopologyControl();
      Assert.assertEquals(0, this.facade.getConstraintViolationCount());
   }
   
   @Test
   public void testWithTestgraphA1_2Cones() throws Exception
   {
      this.reader.read(this.facade, new FileInputStream(getPathToAngleTestGraph(1)));
      
      TopologyModelTestUtils.assertUnclassified(this.facade.getTopology());
      TopologyControlAlgorithmsTestUtils.assertWeightSet(this.facade);
      
      TopologyControlAlgorithmsTestUtils.runFacadeYao(this.facade, 2);
      
      TopologyModelTestUtils.assertActiveWithExceptions(this.facade.getTopology(), false, "e13", "e15");
      
      this.facade.checkConstraintsAfterTopologyControl();
      Assert.assertEquals(0, this.facade.getConstraintViolationCount());
   }
   
   @Test
   public void testWithTestgraphA1_1Cone() throws Exception
   {
      this.reader.read(this.facade, new FileInputStream(getPathToAngleTestGraph(1)));
      
      TopologyModelTestUtils.assertUnclassified(this.facade.getTopology());
      TopologyControlAlgorithmsTestUtils.assertWeightSet(this.facade);
      
      TopologyControlAlgorithmsTestUtils.runFacadeYao(this.facade, 1);
      
      TopologyModelTestUtils.assertActiveWithExceptions(this.facade.getTopology(), false, "e13", "e14", "e15");
      
      this.facade.checkConstraintsAfterTopologyControl();
      Assert.assertEquals(0, this.facade.getConstraintViolationCount());
   }
}
