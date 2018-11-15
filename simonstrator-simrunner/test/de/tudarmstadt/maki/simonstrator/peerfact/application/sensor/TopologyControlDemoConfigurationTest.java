package de.tudarmstadt.maki.simonstrator.peerfact.application.sensor;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.tud.kom.p2psim.impl.simengine.Simulator;

/**
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class TopologyControlDemoConfigurationTest {
	private static final String CONFIG_FILE = "config/topology_control_demo.xml";
	private static Simulator sim;

	@Before
	public void setUp() throws Exception {
		// new DefaultConfigurator(CONFIG_FILE);
		sim = Simulator.getInstance();
		Map<String, String> variables = new LinkedHashMap<>();
		variables.put("enableVisualization", "false");
		variables.put("end", "20m");
		List<String> variations = new LinkedList<String>();
		sim.configure(CONFIG_FILE, variables, variables, variations);
	}

	@After
	public void tearDown() throws Exception {
		sim = null;
	}

	@Test
	public void testFunctionality() throws Exception {
		sim.start(true);
	}
}
