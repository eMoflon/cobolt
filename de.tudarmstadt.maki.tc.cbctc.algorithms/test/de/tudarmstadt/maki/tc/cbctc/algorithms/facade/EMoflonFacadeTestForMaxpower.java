package de.tudarmstadt.maki.tc.cbctc.algorithms.facade;

import static de.tudarmstadt.maki.tc.cbctc.algorithms.JvlcTestHelper.getPathToEnergyTestGraph;

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
import de.tudarmstadt.maki.tc.cbctc.model.TopologyTestUtils;

public class EMoflonFacadeTestForMaxpower {
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

		TopologyTestUtils.assertUnclassified(this.facade.getTopology());

		this.facade.run(-1.0);

		TopologyTestUtils.assertActiveWithExceptions(this.facade.getTopology(), true);
	}
}
