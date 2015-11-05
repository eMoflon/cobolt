package de.tudarmstadt.maki.modeling.jvlc.facade;

import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.getPathToEnergyTestGraph;

import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlFacadeFactory;

public class JVLCFacadeForNullTCTest {
	private JVLCFacade facade;
	private static TopologyControlAlgorithmID ALGO_ID = TopologyControlAlgorithmID.NULL_TC;

	@Before
	public void setup() {

		this.facade = (JVLCFacade) TopologyControlFacadeFactory.create("de.tudarmstadt.maki.modeling.jvlc.facade.JVLCFacade");
		this.facade.configureAlgorithm(ALGO_ID);
	}

	@Test
	public void testWithTestgraphE1() throws Exception {
		this.facade.loadAndSetTopologyFromFile(getPathToEnergyTestGraph(1));

		this.facade.run(-1.0);

		JvlcTestHelper.assertAllActiveWithExceptions(this.facade.getTopology(), true);
	}
}
