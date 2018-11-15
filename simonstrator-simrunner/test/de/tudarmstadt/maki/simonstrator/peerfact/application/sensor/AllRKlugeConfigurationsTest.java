package de.tudarmstadt.maki.simonstrator.peerfact.application.sensor;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import de.tud.kom.p2psim.impl.simengine.Simulator;

/**
 * The purpose of this test suite is to run all configurations by Roland Kluge
 * (for regression testing)
 * 
 * @author Roland Kluge - Initial implementation
 */
public class AllRKlugeConfigurationsTest {

	private Simulator sim;

	private static final List<String> NO_VARIATIONS = new LinkedList<String>();
	private static final String PREFIX = "config/";

	@Before
	public void setUpTestsuite() {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HHmmss.SSS");
		System.setProperty("logfile.name", "rkluge/simrunnerlog_" + dateFormat.format(new Date()) + ".log");
		sim = Simulator.getInstance();
	}

	@Test
	public void testConfigurationsWithinSimonstrator() throws Exception {
		for (final String configFile : Arrays.asList(//
				"events/EventRecordingDemo.xml", 
				"topology_control_demo.xml"//
		).stream().map(s -> PREFIX + s).collect(Collectors.toList())) {
			Map<String, String> variables = new LinkedHashMap<>();
			variables.put("enableVisualization", "false");
			variables.put("end", "20m");
			runSimulation(configFile, variables);
		}
	}

	/**
	 * This test requires dependencies to the projects
	 * org.cobolt.algorithms //
	 * de.tudarmstadt.maki.tc.cbctc.analysis //
	 * de.tudarmstadt.maki.tc.cbctc.model
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConfigurationsWithExternalDependencies() throws Exception {
		try {
			Class.forName("org.cobolt.algorithms.AlgortihmsFactory");
			Class.forName("de.tudarmstadt.maki.tc.cbctc.analysis.AnalysisFactory");
			Class.forName("de.tudarmstadt.maki.tc.cbctc.model.ModelFactory");
		} catch (final Exception e) {
			Assume.assumeNoException(e);
		}

		for (final String configFile : Arrays.asList(//
				"jvlc/jvlc_complete_evaluation.xml", //
				"democles/DemoclesPMComparisonDemo.xml"//
		).stream().map(s -> PREFIX + s).collect(Collectors.toList())) {
			Map<String, String> variables = new LinkedHashMap<>();
			variables.put("enableVisualization", "false");
			variables.put("end", "20m");
			runSimulation(configFile, variables);
		}

	}

	private void runSimulation(final String configFile, Map<String, String> variables) {
		sim.configure(configFile, variables, variables, NO_VARIATIONS);
		sim.start(true);
	}
}
