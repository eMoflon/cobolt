package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.wildfire;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.topology.views.visualization.ui.PlottingView;
import de.tud.kom.p2psim.impl.util.oracle.GlobalOracle;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;
import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent.Analyzer;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.ComponentFinder;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.MetricPlottingAdapter;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.MetricPlottingsAdapterManager;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.TotalLatencyMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.TopologyControlInformationStoreComponent;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.MetricUtils;
import de.tudarmstadt.maki.simonstrator.tc.scenario.ScenarioType;

public class WildfireMonitoringAnalyzer implements Analyzer {

	private boolean isEnabled;
	private long updateInterval;
	public final MetricPlottingsAdapterManager plottingsAdapterManager = new MetricPlottingsAdapterManager();
	private MetricPlottingsAdapterManager plottingAdapterManager;
	private TopologyControlInformationStoreComponent informationStore;

	public WildfireMonitoringAnalyzer() {

		this.isEnabled = ComponentFinder.findTopologyControlComponent()
				.getConfiguration().scenario == ScenarioType.WILDFIRE;
	}

	@Override
	public void start() {

		this.plottingAdapterManager = new MetricPlottingsAdapterManager();
		this.informationStore = ComponentFinder.findInformationStoreComponent();

		if (this.isEnabled) {
			final long initialDelay = (long) ComponentFinder.findTopologyControlComponent()
					.getConfiguration().topologyControlIntervalInMinutes * Time.MINUTE
					+ 100 * Simulator.MILLISECOND_UNIT;

			PlottingView plottingView = new PlottingView("Analyzer of " + getClass().getSimpleName());
			Simulator.getScheduler().scheduleIn(initialDelay, new EventHandler() {

				@Override
				public void eventOccurred(Object content, int type) {

					final List<Host> hosts = GlobalOracle.getHosts().stream().map(simHost -> (Host) simHost)
							.collect(Collectors.toList());
					{
						final Freshness metric = new Freshness();
						metric.initialize(hosts);
						informationStore.put(Time.getCurrentTime(), metric);
						final MetricPlottingAdapter adapter = plottingAdapterManager
								.registerMetricAdapterIfNecessary(plottingView, metric);
						adapter.newMetricValue(Time.getCurrentTime(), metric, TotalLatencyMetric.NOT_AVAILABLE);
					}

					reschedule();
				}

				private void reschedule() {
					Simulator.getScheduler().scheduleIn(updateInterval, this, null, 0);
				}
			}, null, 0);
		}
	}

	@Override
	public void stop(final Writer out) {
		if (this.isEnabled) {
			try {
				out.write("[WILDFIRE STATISTICS]\n");
				final DescriptiveStatistics globalFreshnessStatistics = new DescriptiveStatistics();

				for (final Metric<MetricValue<?>> freshness : this.informationStore
						.getMetricsByName(MetricUtils.getDefaultName(Freshness.class))) {
					final DescriptiveStatistics perTimestampStatistics = MetricUtils.getStatisticsOverHosts(freshness);
					final double meanFreshness = perTimestampStatistics.getMean();
					globalFreshnessStatistics.addValue(meanFreshness);
					out.write(String.format("%.2f\n", meanFreshness));
				}

				out.write("\n\n");
				out.write(String.format("Overall mean freshness (in minutes): %.1f\n",
						globalFreshnessStatistics.getMean()));

			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void setUpdateInterval(long updateInterval) {
		this.updateInterval = updateInterval;
	}
}
