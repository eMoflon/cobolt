package de.tudarmstadt.maki.simonstrator.peerfact.application.sensor;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tudarmstadt.maki.simonstrator.tc.utils.DateHelper;

/**
 *
 * @author Roland Kluge - Initial implementation
 *
 */
public class CoalaVizDemoTest {
	private static final int simulationSpeed = 1;
	private static final String CONFIG_FILE = "config/coalaviz/coalaviz.xml";
	private static Simulator sim;

	@Before
	public void setUp() throws Exception {
		String outputFolderForResults = "output/test";
		System.setProperty("logfile.name",
				new File(outputFolderForResults, "simrunnerlog_" + DateHelper.getFormattedDate() + ".log").getPath());
		Locale.setDefault(Locale.US);
		// new DefaultConfigurator(CONFIG_FILE);
		sim = Simulator.getInstance();
		sim.setTimeSkew(simulationSpeed);
		Map<String, String> variables = new LinkedHashMap<>();
		variables.put("enableVisualization", "true");
		List<String> variations = new LinkedList<String>();
		sim.configure(CONFIG_FILE, variables, variables, variations);
	}

	@After
	public void tearDown() throws Exception {
		sim = null;
	}

	// @Ignore
	@Test
	public void testFunctionality() throws Exception {
		sim.start(true);
	}
}
