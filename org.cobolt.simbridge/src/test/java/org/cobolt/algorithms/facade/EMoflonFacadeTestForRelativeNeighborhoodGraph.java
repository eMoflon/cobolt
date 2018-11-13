package org.cobolt.algorithms.facade;

import static org.cobolt.algorithms.TopologyControlAlgorithmsTestUtils.getPathToEnergyTestGraph;

import java.io.File;
import java.io.FileInputStream;

import org.cobolt.algorithms.TopologyControlAlgorithmsTestUtils;
import org.cobolt.model.TopologyModelTestUtils;
import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;
import de.tudarmstadt.maki.simonstrator.tc.weighting.EdgeWeightProviders;
import de.tudarmstadt.maki.simonstrator.tc.weighting.InverseEstimatedRemainingLifetimeWeightProvider;

/**
 * Tests for implementation of
 * {@link UnderlayTopologyControlAlgorithms#RELATIVE_NEIGHBORHOOD_GRAPH}
 *
 * @author Roland Kluge - Initial implementation
 *
 */
public class EMoflonFacadeTestForRelativeNeighborhoodGraph extends AbstractEMoflonFacadeTest {

	@Override
	protected TopologyControlAlgorithmID getAlgorithmID() {
		return UnderlayTopologyControlAlgorithms.RELATIVE_NEIGHBORHOOD_GRAPH;
	}

	@Test
	public void testWithTestgraphE1() throws Exception {
		reader.read(this.facade, new FileInputStream(new File(getPathToEnergyTestGraph(1))));
		EdgeWeightProviders.apply(this.facade, InverseEstimatedRemainingLifetimeWeightProvider.INSTANCE);

		TopologyModelTestUtils.assertUnclassified(this.facade.getTopology());
		TopologyControlAlgorithmsTestUtils.assertWeightSet(this.facade);

		TopologyControlAlgorithmsTestUtils.runFacadeKTC(this.facade, -1.0);

		TopologyModelTestUtils.assertAllActiveWithExceptions(this.facade.getTopology(), "e13");

		this.facade.checkConstraintsAfterTopologyControl();
		Assert.assertEquals(0, this.facade.getConstraintViolationCount());
	}
}
