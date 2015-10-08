package de.tudarmstadt.maki.modeling.jvlc.facade;

import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.EPS_0;
import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.assertIsActive;
import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.assertIsInactive;
import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.getPathToEnergyTestGraph;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.maki.modeling.jvlc.IncrementalEnergyKTC;
import de.tudarmstadt.maki.modeling.jvlc.KTCNode;
import de.tudarmstadt.maki.modeling.jvlc.Topology;
import de.tudarmstadt.maki.modeling.jvlc.constraints.AssertConstraintViolationEnumerator;

/**
 * Unit tests for {@link JVLCFacade}, using {@link IncrementalEnergyKTC}.
 */
public class JVLCFacadeForIncrementalEnergyKTCTest {

	private JVLCFacade facade;

	@Before
	public void setup() {

		this.facade = JVLCFacade.createFacadeForIncrementalEnergyKTC();
	}

	@Test
	public void testWithTestgraphE1() throws Exception {
		this.facade.loadAndSetTopologyFromFile(getPathToEnergyTestGraph(1));

		this.facade.run(1.5);

		final Topology topology = this.facade.getTopology();
		assertIsInactive(topology, "e13");
		assertIsActive(topology, "e32", "e21", "e31", "e12", "e23");

		AssertConstraintViolationEnumerator.getInstance().checkPredicate(this.facade.getTopology(), this.facade.getAlgorithm());
	}

	@Test
	public void testWithTestgraphE1_OneContextEvent() throws Exception {
		this.facade.loadAndSetTopologyFromFile(getPathToEnergyTestGraph(1));

		this.facade.run(1.5);

		final Topology topology = this.facade.getTopology();
		assertIsInactive(topology.getEdgeById("e13"));
		assertIsActive(topology, "e32", "e21", "e31", "e12", "e23");

		final KTCNode n3 = topology.getKTCNodeById("n3");
		Assert.assertEquals(60, n3.getRemainingEnergy(), EPS_0);
		n3.setRemainingEnergy(15);

		this.facade.run(1.5);

		assertIsInactive(topology, "e13", "e32");
		assertIsActive(topology, "e32", "e21", "e31", "e12", "e23");

		AssertConstraintViolationEnumerator.getInstance().checkPredicate(this.facade.getTopology(), this.facade.getAlgorithm());
	}
}
