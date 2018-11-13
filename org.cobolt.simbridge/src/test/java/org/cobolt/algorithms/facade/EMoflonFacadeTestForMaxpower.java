package org.cobolt.algorithms.facade;

import static org.cobolt.algorithms.TopologyControlAlgorithmsTestUtils.getPathToEnergyTestGraph;

import java.io.File;
import java.io.FileInputStream;

import org.cobolt.algorithms.TopologyControlAlgorithmsTestUtils;
import org.cobolt.model.TopologyModelTestUtils;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;

/**
 * Tests for implementation of
 * {@link UnderlayTopologyControlAlgorithms#MAXPOWER_TC}
 *
 * @author Roland Kluge - Initial implementation
 *
 */
public class EMoflonFacadeTestForMaxpower extends AbstractEMoflonFacadeTest {

	@Override
	protected TopologyControlAlgorithmID getAlgorithmID() {
		return UnderlayTopologyControlAlgorithms.MAXPOWER_TC;
	}

	@Test
	public void testWithTestgraphE1() throws Exception {
		reader.read(this.facade, new FileInputStream(new File(getPathToEnergyTestGraph(1))));

		TopologyModelTestUtils.assertUnclassified(this.facade.getTopology());

		TopologyControlAlgorithmsTestUtils.runFacadeKTC(this.facade, -1.0);

		TopologyModelTestUtils.assertAllActiveWithExceptions(this.facade.getTopology());
	}
}
