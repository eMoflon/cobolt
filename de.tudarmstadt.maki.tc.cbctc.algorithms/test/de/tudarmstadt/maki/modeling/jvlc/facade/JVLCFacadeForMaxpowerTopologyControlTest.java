package de.tudarmstadt.maki.modeling.jvlc.facade;

import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.getPathToEnergyTestGraph;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlFacadeFactory;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlOperationMode;
import de.tudarmstadt.maki.simonstrator.tc.ktc.UnderlayTopologyControlAlgorithms;
import de.tudarmstadt.maki.tc.cbctc.algorithms.facade.EMoflonFacade;
import de.tudarmstadt.maki.tc.cbctc.algorithms.io.GraphTFileReader;
import de.tudarmstadt.maki.tc.cbctc.model.TopologyModelTestHelper;

public class JVLCFacadeForMaxpowerTopologyControlTest {
	private EMoflonFacade facade;
	private GraphTFileReader reader;
	private static TopologyControlAlgorithmID ALGO_ID = UnderlayTopologyControlAlgorithms.MAXPOWER_TC;

	@Before
	public void setup() {

		this.facade = (EMoflonFacade) TopologyControlFacadeFactory.create("de.tudarmstadt.maki.tc.cbctc.algorithms.facade.EMoflonFacade");
		this.facade.setOperationMode(TopologyControlOperationMode.BATCH);
		this.facade.configureAlgorithm(ALGO_ID);
		this.reader = new GraphTFileReader();
	}

	@Test
	public void testWithTestgraphE1() throws Exception {
		reader.read(this.facade, new FileInputStream(new File(getPathToEnergyTestGraph(1))));

		TopologyModelTestHelper.assertThatAllLinksAreUnclassified(this.facade.getTopology());

		this.facade.run(-1.0);

		TopologyModelTestHelper.assertThatAllLinksAreActiveWithExceptions(this.facade.getTopology(), true);
	}
}
