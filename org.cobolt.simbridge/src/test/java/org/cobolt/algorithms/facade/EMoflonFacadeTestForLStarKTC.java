package org.cobolt.algorithms.facade;

import static org.cobolt.algorithms.TopologyControlAlgorithmsTestUtils.getPathToHopCountTestGraph;

import java.io.FileInputStream;

import org.cobolt.model.TopologyModelTestUtils;
import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;
import org.cobolt.algorithms.TopologyControlAlgorithmsTestUtils;

/**
 * Tests for implementation of
 * {@link UnderlayTopologyControlAlgorithms#GABRIEL_GRAPH}
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class EMoflonFacadeTestForLStarKTC extends AbstractEMoflonFacadeTest {

	@Override
	protected TopologyControlAlgorithmID getAlgorithmID() {
		return UnderlayTopologyControlAlgorithms.LSTAR_KTC;
	}

	@Test
	public void testWithTestgraphH1() throws Exception {
		this.reader.read(this.facade, new FileInputStream(getPathToHopCountTestGraph(1)));

		TopologyModelTestUtils.assertUnclassified(this.facade.getTopology());
		TopologyControlAlgorithmsTestUtils.assertWeightSet(this.facade);

		TopologyControlAlgorithmsTestUtils.runFacadeLStarKTC(this.facade, 3.0, 1.0);

		TopologyModelTestUtils.assertActiveWithExceptions(this.facade.getTopology(), false, "e12");

		this.facade.checkConstraintsAfterTopologyControl();
		Assert.assertEquals(0, this.facade.getConstraintViolationCount());
	}

	@Test
	public void testWithTestgraphH2_Negative() throws Exception {
		this.reader.read(this.facade, new FileInputStream(getPathToHopCountTestGraph(2)));

		TopologyModelTestUtils.assertUnclassified(this.facade.getTopology());
		TopologyControlAlgorithmsTestUtils.assertWeightSet(this.facade);

		TopologyControlAlgorithmsTestUtils.runFacadeLStarKTC(this.facade, 3.0, 1.2);

		TopologyModelTestUtils.assertActiveWithExceptions(this.facade.getTopology(), false);

		this.facade.checkConstraintsAfterTopologyControl();
		Assert.assertEquals(0, this.facade.getConstraintViolationCount());
	}

	@Test
	public void testWithTestgraphH2_Positive() throws Exception {
		this.reader.read(this.facade, new FileInputStream(getPathToHopCountTestGraph(2)));

		TopologyModelTestUtils.assertUnclassified(this.facade.getTopology());
		TopologyControlAlgorithmsTestUtils.assertWeightSet(this.facade);

		TopologyControlAlgorithmsTestUtils.runFacadeLStarKTC(this.facade, 3.0, 1.3);

		TopologyModelTestUtils.assertActiveWithExceptions(this.facade.getTopology(), false, "e12");

		this.facade.checkConstraintsAfterTopologyControl();
		Assert.assertEquals(0, this.facade.getConstraintViolationCount());
	}

	@Test
	public void testWithTestgraphH3() throws Exception {
		this.reader.read(this.facade, new FileInputStream(getPathToHopCountTestGraph(3)));

		TopologyModelTestUtils.assertUnclassified(this.facade.getTopology());
		TopologyControlAlgorithmsTestUtils.assertWeightSet(this.facade);

		TopologyControlAlgorithmsTestUtils.runFacadeLStarKTC(this.facade, 3.0, 1.11);

		TopologyModelTestUtils.assertActiveWithExceptions(this.facade.getTopology(), false);

		this.facade.checkConstraintsAfterTopologyControl();
		Assert.assertEquals(0, this.facade.getConstraintViolationCount());
	}
}
