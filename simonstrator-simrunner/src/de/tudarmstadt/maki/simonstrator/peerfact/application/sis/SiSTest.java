package de.tudarmstadt.maki.simonstrator.peerfact.application.sis;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.tud.kom.p2psim.impl.scenario.DefaultConfigurator;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Oracle;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSComponent;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSDataCallback;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSInfoProperties;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSInfoProperties.SiSScope;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSInformationConsumer.SiSConsumerHandle;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSInformationProvider.SiSProviderHandle;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSRequest;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSResultCallback;
import de.tudarmstadt.maki.simonstrator.api.component.sis.exception.InformationNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSTypes;

/**
 * These test methods follow a story to prevent lengthy initialization phases
 * for each method. An implementation of a {@link SiSComponent} has to fulfill
 * all tests.
 * 
 * @author Bjoern Richerzhagen
 *
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SiSTest {

	private static Simulator sim;

	private static SiSType<Double> TYPE = SiSTypes.TEST_DOUBLE;

	private static List<SiSTestApplication> sisApps = new LinkedList<>();

	private static final int APP_ONE = 0;

	@BeforeClass
	public static void setUpEnvironment() {
		//
	}

	@Before
	public void setUp() throws Exception {
		new DefaultConfigurator("config/tests/sis_component_tests.xml");
		sim = Simulator.getInstance();
		Simulator.getScheduler().reset();
		Map<String, String> variables = new LinkedHashMap<>();
		List<String> variations = new LinkedList<String>();
		sim.configure("config/tests/sis_component_tests.xml", variables,
				variables, variations);
		sim.start(true);
		for (Host host : Oracle.getAllHosts()) {
			SiSTestApplication app = host
					.getComponent(SiSTestApplication.class);
			sisApps.add(app);
		}
	}

	@After
	public void tearDown() throws Exception {
		sim = null;
		sisApps.clear();
	}

	@AfterClass
	public static void tearDownEnvironment() {

	}

	@Test
	public void test001_JustLocal() {
		for (SiSTestApplication app : sisApps) {
			try {
				app.getSiS().get().localState(TYPE, SiSRequest.NONE);
				Assert.fail("Local sources should not available");
			} catch (InformationNotAvailableException e) {
				// OK
			}
		}
		registerLocalProvidersOnAllApps();
		for (SiSTestApplication app : sisApps) {
			try {
				double result = app.getSiS().get()
						.localState(TYPE, SiSRequest.NONE);
				Assert.assertEquals(app.getDummyValue(), result, 0);
			} catch (InformationNotAvailableException e) {
				Assert.fail("Local sources should be available");
			}
		}
	}

	@Test
	public void test002_RawGlobal() {
		registerLocalProvidersOnAllApps();
		SiSTestApplication app = sisApps.get(APP_ONE);
		final Map<INodeID, Double> expectedResult = new LinkedHashMap<>();
		for (SiSTestApplication other : sisApps) {
			INodeID id = other.getHost().getId();
			Double value = other.getDummyValue();
			expectedResult.put(id, value);
		}
		SiSRequest request = new SiSRequest().setScope(SiSScope.GLOBAL);
		app.getSiS()
				.get()
				.rawObservations(TYPE, request,
						new SiSResultCallback<Map<INodeID, Double>>() {

							@Override
							public void onResult(Map<INodeID, Double> result, SiSConsumerHandle consumerHandle) {
								Assert.assertNotNull(result);
								Assert.assertTrue(
										"Contains all Keys",
										result.keySet().containsAll(
												expectedResult.keySet()));
								Assert.assertTrue(
										"Contains all Keys",
										expectedResult.keySet().containsAll(
												result.keySet()));
								for (Entry<INodeID, Double> resultEntry : result
										.entrySet()) {
									double exp = expectedResult.get(resultEntry
											.getKey());
									Assert.assertEquals(exp,
											(double) resultEntry.getValue(), 0);
								}
							}

							@Override
							public void onAbort(
									SiSResultCallback.AbortReason reason) {
								Assert.fail();
							}
						});
	}

	@Test
	public void test010_UnderlayTopology() {
		// WiFi-Underlay provides the current topology.
		SiSTestApplication app = sisApps.get(APP_ONE);
		try {
			Graph neighbors = app.getSiS().get().localState(SiSTypes.NEIGHBORS_WIFI, SiSRequest.NONE);
			// neighbors.toString();
			Monitor.log(getClass(), Level.DEBUG, neighbors.toString());
		} catch (InformationNotAvailableException e) {
			Assert.fail();
		}
	}

	private static void registerLocalProvidersOnAllApps() {
		double ct = 0;
		for (SiSTestApplication app : sisApps) {
			SiSInfoProperties prop = new SiSInfoProperties().setScope(
					SiSScope.NODE_LOCAL).setSourceComponent(
					SiSTestApplication.class);
			app.setDummyValue(++ct);
			app.getSiS().provide()
					.nodeState(TYPE, new SiSTestDataCallback(app, prop));
		}
	}

	private static class SiSTestDataCallback implements SiSDataCallback<Double> {

		private final SiSTestApplication app;

		private final SiSInfoProperties infoProps;

		private final Set<INodeID> observedNodes = new LinkedHashSet<>();

		public SiSTestDataCallback(SiSTestApplication app,
				SiSInfoProperties infoProps) {
			this.app = app;
			this.infoProps = infoProps;
			this.observedNodes.add(app.getHost().getId());
		}

		@Override
		public Double getValue(INodeID nodeId, SiSProviderHandle providerHandle)
				throws InformationNotAvailableException {
			if (nodeId.equals(app.getHost().getId())) {
				return app.getDummyValue();
			}
			throw new InformationNotAvailableException();
		}

		@Override
		public Set<INodeID> getObservedNodes() {
			return observedNodes;
		}

		@Override
		public SiSInfoProperties getInfoProperties() {
			return infoProps;
		}

	}

}