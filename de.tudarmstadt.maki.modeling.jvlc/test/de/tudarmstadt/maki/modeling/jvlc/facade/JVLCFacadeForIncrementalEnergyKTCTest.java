package de.tudarmstadt.maki.modeling.jvlc.facade;

import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.EPS_0;
import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.getPathToEnergyTestGraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.maki.modeling.graphmodel.GraphModelTestHelper;
import de.tudarmstadt.maki.modeling.jvlc.EnergyAwareKTC;
import de.tudarmstadt.maki.modeling.jvlc.KTCNode;
import de.tudarmstadt.maki.modeling.jvlc.Topology;
import de.tudarmstadt.maki.modeling.jvlc.io.GraphTFileReader;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlFacadeFactory;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlOperationMode;
import de.tudarmstadt.maki.simonstrator.tc.ktc.UnderlayTopologyControlConstants;

/**
 * Unit tests for {@link JVLCFacade}, using {@link EnergyAwareKTC}.
 */
public class JVLCFacadeForIncrementalEnergyKTCTest {

	private JVLCFacade facade;
	private GraphTFileReader reader;
	private TopologyControlAlgorithmID algorithmID = UnderlayTopologyControlConstants.IE_KTC;

	@Before
	public void setup() {

		this.facade = (JVLCFacade) TopologyControlFacadeFactory
				.create("de.tudarmstadt.maki.modeling.jvlc.facade.JVLCFacade");
		this.facade.setOperationMode(TopologyControlOperationMode.INCREMENTAL);
		this.facade.configureAlgorithm(algorithmID);
		this.reader = new GraphTFileReader();
	}

	@Test
	public void testWithTestgraphE1() throws Exception {
		readTestCase(1);

		this.facade.run(1.5);

		this.facade.checkConstraintsAfterTopologyControl();
	}

	@Test
	public void testWithTestgraphE1_OneContextEvent() throws Exception {
		readTestCase(1);

		this.facade.run(1.5);
		this.facade.checkConstraintsAfterTopologyControl();

		final Topology topology = this.facade.getTopology();

		final KTCNode n3 = topology.getKTCNodeById("n3");
		Assert.assertEquals(60, n3.getEnergyLevel(), EPS_0);
		this.facade.updateNodeAttribute(n3, UnderlayTopologyControlConstants.REMAINING_ENERGY, 15.0);
		Assert.assertEquals(15, topology.getKTCNodeById("n3").getEnergyLevel(), EPS_0);

		GraphModelTestHelper.assertIsUnclassified(topology.getKTCLinkById("e31"));
		GraphModelTestHelper.assertIsUnclassified(topology.getKTCLinkById("e32"));

		this.facade.run(1.5);
		this.facade.checkConstraintsAfterTopologyControl();
	}

	/**
	 * This test illustrates that in a triangle that contains two equally long
	 * 'longest' links (in terms of remaining lifetime), only the link with the
	 * larger ID ('e13' in this case) is inactivated.
	 */
	@Ignore // TODO@rkluge: Refine test cases
	@Test
	public void testTriangleWithEquisecles() throws Exception {
		readTestCase(2);
		facade.run(1.1);

		GraphModelTestHelper.assertThatAllLinksAreActiveWithExceptions(facade.getTopology(), false, "e13");
	}

	private void readTestCase(int id) throws FileNotFoundException {
		reader.read(this.facade, new FileInputStream(new File(getPathToEnergyTestGraph(id))));
	}
}
