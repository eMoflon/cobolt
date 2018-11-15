package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.wildfire;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.common.metric.AbstractMetric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.ScenarioUtilities;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.SimpleNumericMetricValue;

/**
 * The freshness is defined as the difference (in sim. seconds) between the time
 * at which a measurement is performed and the time at which it arrives at a
 * particular node
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class Freshness extends AbstractMetric<MetricValue<?>> {

	public static final double NOT_AVAILABLE = Double.NaN;

	public Freshness() {
		super("Wildfire-MeanFreshness", MetricUnit.NONE);
	}

	@Override
	public void initialize(final List<Host> hosts) {
		final Map<INodeID, WildfireMonitoringMeasurement> globalKnowledgeDataStore = buildGlobalKnowledge(hosts);
		hosts.forEach(host -> addHost(host, calculate(host, globalKnowledgeDataStore)));
	}

	private MetricValue<Double> calculate(final Host host,
			final Map<INodeID, WildfireMonitoringMeasurement> globalKnowledgeDataStore) {

		final WildfireMonitoringApplication application = hostToWildfireApplication(host);
		final DescriptiveStatistics perHostFreshnessStatistics = new DescriptiveStatistics();

		for (final Entry<INodeID, WildfireMonitoringMeasurement> entry : application.getDataStore().entrySet()) {
			final INodeID nodeId = entry.getKey();
			final double localTimestamp = entry.getValue().timestamp;
			final double globalTimestamp = globalKnowledgeDataStore.get(nodeId).timestamp;

			if (localTimestamp > globalTimestamp)
				throw new AssertionError(String.format(
						"Node %s has timestamp %d for data from node %s, which is newer than the original data (timestamp: %d)",
						ScenarioUtilities.getNode(host).getId(), localTimestamp, nodeId, globalTimestamp));

			perHostFreshnessStatistics.addValue(Math.abs(globalTimestamp) - localTimestamp);
		}

		final double safeValue = perHostFreshnessStatistics.getN() == 0 ? NOT_AVAILABLE
				: perHostFreshnessStatistics.getMean() / Time.SECOND;
		return new SimpleNumericMetricValue<Double>(safeValue);
	}

	private Map<INodeID, WildfireMonitoringMeasurement> buildGlobalKnowledge(List<Host> hosts) {
		final Map<INodeID, WildfireMonitoringMeasurement> dataStore = new HashMap<>();
		hosts.stream().filter(Freshness::hasWildfireApplication).map(Freshness::hostToWildfireApplication)
				.forEach(application -> {
					final WildfireMonitoringMeasurement storedOwnMeasurement = application.getStoredOwnMeasurement();
					dataStore.put(storedOwnMeasurement.nodeId, storedOwnMeasurement);
				});
		return dataStore;
	}

	private static boolean hasWildfireApplication(final Host host) {
		try {
			host.getComponent(WildfireMonitoringApplication.class);
			return true;
		} catch (ComponentNotAvailableException e) {
			return false;
		}
	}

	private static WildfireMonitoringApplication hostToWildfireApplication(final Host host) {
		try {
			return host.getComponent(WildfireMonitoringApplication.class);
		} catch (ComponentNotAvailableException e) {
			throw new AssertionError("Missing component", e);
		}
	}
}