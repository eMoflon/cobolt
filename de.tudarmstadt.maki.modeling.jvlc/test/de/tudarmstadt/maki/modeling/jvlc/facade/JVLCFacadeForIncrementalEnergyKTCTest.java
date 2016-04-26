package de.tudarmstadt.maki.modeling.jvlc.facade;

import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.EPS_0;
import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.assertAllActiveWithExceptions;
import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.assertIsActive;
import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.assertIsInactive;
import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.assertIsUnclassified;
import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.getPathToEnergyTestGraph;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.maki.modeling.jvlc.IncrementalEnergyKTC;
import de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper;
import de.tudarmstadt.maki.modeling.jvlc.KTCNode;
import de.tudarmstadt.maki.modeling.jvlc.Topology;
import de.tudarmstadt.maki.modeling.jvlc.algorithm.AlgorithmHelper;
import de.tudarmstadt.maki.modeling.jvlc.constraints.AssertConstraintViolationEnumerator;
import de.tudarmstadt.maki.modeling.jvlc.io.GraphTFileReader;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlFacadeFactory;
import de.tudarmstadt.maki.simonstrator.tc.ktc.KTCConstants;

/**
 * Unit tests for {@link JVLCFacade}, using {@link IncrementalEnergyKTC}.
 */
public class JVLCFacadeForIncrementalEnergyKTCTest {

	private JVLCFacade facade;
	private GraphTFileReader reader;
	private static TopologyControlAlgorithmID ALGO_ID = KTCConstants.IE_KTC;

	@Before
	public void setup() {

		this.facade = (JVLCFacade) TopologyControlFacadeFactory.create("de.tudarmstadt.maki.modeling.jvlc.facade.JVLCFacade");
		this.facade.configureAlgorithm(ALGO_ID);
		this.reader = new GraphTFileReader();
	}

	@Test
	public void testWithTestgraphE1() throws Exception {
		reader.read(this.facade, new FileInputStream(new File(getPathToEnergyTestGraph(1))));

		this.facade.run(1.5);

		final Topology topology = this.facade.getTopology();
		assertIsInactive(topology, "e13");
		assertIsActive(topology, "e32", "e21", "e31", "e12", "e23");

		AssertConstraintViolationEnumerator.getInstance().checkPredicate(this.facade.getTopology(), AlgorithmHelper.createAlgorithmForID(ALGO_ID));
	}

	@Test
	public void testWithTestgraphE1_OneContextEvent() throws Exception {
		reader.read(this.facade, new FileInputStream(new File(getPathToEnergyTestGraph(1))));

		this.facade.run(1.5);

		final Topology topology = this.facade.getTopology();
		assertIsInactive(topology.getEdgeById("e13"));
		assertIsActive(topology, "e32", "e21", "e31", "e12", "e23");

		final KTCNode n3 = topology.getKTCNodeById("n3");
		Assert.assertEquals(60, n3.getRemainingEnergy(), EPS_0);
		this.facade.updateNodeAttribute(n3, KTCConstants.REMAINING_ENERGY, 15.0);
		Assert.assertEquals(15, topology.getKTCNodeById("n3").getRemainingEnergy(), EPS_0);

		assertIsUnclassified(topology.getKTCLinkById("e31"));
		assertIsUnclassified(topology.getKTCLinkById("e32"));

		this.facade.run(1.5);

		assertIsInactive(topology, "e13", "e32");
		assertIsActive(topology, "e21", "e31", "e12", "e23");

		AssertConstraintViolationEnumerator.getInstance().checkPredicate(this.facade.getTopology(), AlgorithmHelper.createAlgorithmForID(ALGO_ID));
	}

	/**
	 * This test illustrates that in a triangle that contains two equally long 'longest' links (in terms of remaining lifetime), only the link with the larger ID ('e13'  in this case) is inactivated.
	 */
	@Test
	public void testTriangleWithEquisecles() throws Exception {
		reader.read(facade, new FileInputStream(new File(JvlcTestHelper.getPathToEnergyTestGraph(2))));
		facade.run(1.1);

		Assert.assertTrue(
				facade.getTopology().getKTCLinkById("e12").hasSameEstimaedRemainingLifetimeAndSmallerID(facade.getTopology().getKTCLinkById("e13")));

		assertAllActiveWithExceptions(facade.getTopology(), false, "e13");
	}
}
