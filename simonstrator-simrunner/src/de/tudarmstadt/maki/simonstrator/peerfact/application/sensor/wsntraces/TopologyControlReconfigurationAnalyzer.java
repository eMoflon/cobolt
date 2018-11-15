package de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.wsntraces;

import java.io.File;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.coala.simonstrator.AdaptationLogicIntermediateDurationMetric;
import org.coala.simonstrator.AdaptationLogicMapeLoopDurationMetric;
import org.coala.simonstrator.AdaptationLogicObjectiveValueMetric;
import org.coala.simonstrator.AdaptationLogicPlanningDurationMetric;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.energy.EnergyComponent;
import de.tud.kom.p2psim.api.energy.EnergyState;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.topology.views.visualization.ui.PlottingView;
import de.tud.kom.p2psim.impl.util.oracle.GlobalOracle;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;
import de.tudarmstadt.maki.simonstrator.peerfact.analyzer.AbstractEnergyAnalyzer;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.ComponentFinder;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.CheckpointedEnergyConsumptionMetric;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.CheckpointedLatencyMetric;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.CheckpointedReceivedMessageSizeMetric;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.CheckpointedSentMessageSizeMetric;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.GeoFenceSizeMetric;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.MetricPlottingAdapter;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.MetricPlottingsAdapterManager;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.TotalLatencyMetric;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.TotalLinkDropRateMetric;
import de.tudarmstadt.maki.simonstrator.tc.analyzer.metrics.DifferenceMetric;
import de.tudarmstadt.maki.simonstrator.tc.analyzer.metrics.ICheckpointedLatencyMetric;
import de.tudarmstadt.maki.simonstrator.tc.component.TopologyControlComponent;
import de.tudarmstadt.maki.simonstrator.tc.component.TopologyControlComponentEvaluationDataHelper;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.TopologyControlInformationStoreComponent;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.ActiveTopologyControlAlgorithmMetric;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.analysis.ReconfigurationPerformanceReportingHelper;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;
import de.tudarmstadt.maki.simonstrator.tc.utils.DateHelper;
import edu.emory.mathcs.backport.java.util.Collections;

/**
 * This class collects and writes out the per-simulation statistics for the
 * Topology Control reconfiguration case study
 *
 * @author Roland Kluge - Initial implementation
 */
public class TopologyControlReconfigurationAnalyzer extends AbstractEnergyAnalyzer {

	private File outputFolder;

	private TopologyControlComponent topologyControlComponent;

	private static DescriptiveStatistics endOfLifetimeStatistics = new DescriptiveStatistics();
	static Map<Host, Double> totalEnergyConsumption;
	private static Map<Host, Double> checkpointedEnergyConsumption;

	private Set<Host> emptyHosts = new HashSet<>();
	private MetricPlottingsAdapterManager plottingAdapterManager;
	private boolean isLivePlottingEnabled = true;

	@Override
	public void start() {
		this.topologyControlComponent = ComponentFinder.findTopologyControlComponent();
		totalEnergyConsumption = new HashMap<>();
		checkpointedEnergyConsumption = new HashMap<>();
		this.plottingAdapterManager = new MetricPlottingsAdapterManager();
		GlobalOracle.getHosts().forEach(host -> totalEnergyConsumption.put(host, 0.0));
		GlobalOracle.getHosts().forEach(host -> checkpointedEnergyConsumption.put(host, 0.0));

		final long initialDelay = (long) ComponentFinder.findTopologyControlComponent()
				.getConfiguration().topologyControlIntervalInMinutes * Time.MINUTE + 100 * Simulator.MILLISECOND_UNIT;

		if (this.isLivePlottingEnabled) {
			PlottingView plottingView = new PlottingView("Analyzer of " + getClass().getSimpleName());
			Simulator.getScheduler().scheduleIn(initialDelay, new PlottingEventHandler(plottingView), null, 0);
		}
	}

	/**
	 * Writes out performance statistics of nonfunctional properties ('traces')
	 * and runtime performance metrics
	 */
	@Override
	public void stop(final Writer out) {
		final File tracesOutputFile = new File(outputFolder, "wsntraces.csv");
		TraceReportingHelper.writeTrace(this.topologyControlComponent, tracesOutputFile, out);

		final File performanceOutputFile = new File(outputFolder, "reconfiguration_performance.csv");
		ReconfigurationPerformanceReportingHelper.writeReconfigurationStatististic(this.topologyControlComponent,
				performanceOutputFile, out);
	}

	@Override
	public void consumeEnergy(SimHost host, double energy, EnergyComponent consumer, EnergyState energyState) {
		totalEnergyConsumption.put(host, totalEnergyConsumption.get(host) + energy);
		checkpointedEnergyConsumption.put(host, checkpointedEnergyConsumption.get(host) + energy);
	}

	@Override
	public void batteryIsEmpty(SimHost host) {
		endOfLifetimeStatistics.addValue(Time.getCurrentTime());
		emptyHosts.add(host);
	}

	public void setIsLivePlottingEnabled(final boolean isLivePlottingEnabled) {
		this.isLivePlottingEnabled = isLivePlottingEnabled;
	}

	public void setOutputFolder(final String outputFolderWithPlaceholders) {
		final String outputFolder = DateHelper.substitutePlaceholders(outputFolderWithPlaceholders);
		if (outputFolder == null || outputFolder.isEmpty())
			this.outputFolder = TopologyControlComponentEvaluationDataHelper.EVAL_ROOT_FOLDER;
		else
			this.outputFolder = new File(outputFolder);

		if (!this.outputFolder.exists() && !this.outputFolder.mkdirs()) {
			throw new IllegalArgumentException("Cannot create directory '" + this.outputFolder.getAbsolutePath() + "'");
		}
	}

	private final class PlottingEventHandler implements EventHandler {
		private final PlottingView plottingView;

		private PlottingEventHandler(PlottingView plottingView) {
			this.plottingView = plottingView;
		}

		@Override
		public void eventOccurred(Object content, int type) {
			plotMetrics(plottingView);
			reschedule();
		}

		private void plotMetrics(PlottingView plottingView) {
			TopologyControlInformationStoreComponent informationStore = ComponentFinder.findTopologyControlComponent()
					.getInformationStore();

			boolean plotLatency = true;
			if (plotLatency) {
				plot(TotalLatencyMetric.class, TotalLatencyMetric.NOT_AVAILABLE, plottingView, informationStore);
				plot(CheckpointedLatencyMetric.class, ICheckpointedLatencyMetric.NOT_AVAILABLE, plottingView,
						informationStore);
			}

			boolean plotTotalLinkDropRate = true;
			if (plotTotalLinkDropRate) {
				plot(TotalLinkDropRateMetric.class, TotalLinkDropRateMetric.NOT_AVAILABLE, plottingView,
						informationStore);
			}

			boolean plotEnergyConsumption = true;
			if (plotEnergyConsumption) {
				informationStore.put(new CheckpointedEnergyConsumptionMetric(checkpointedEnergyConsumption));
				plot(CheckpointedEnergyConsumptionMetric.class, TotalLinkDropRateMetric.NOT_AVAILABLE, plottingView,
						informationStore);
				GlobalOracle.getHosts().forEach(host -> checkpointedEnergyConsumption.put(host, 0.0));
			}

			boolean plotContext = false;
			if (plotContext) {
				plot(GeoFenceSizeMetric.class, GeoFenceSizeMetric.NOT_AVAILABLE, plottingView, informationStore);

				final List<Integer> ids = UnderlayTopologyControlAlgorithms.getAlgorithms().stream()
						.map(TopologyControlAlgorithmID::getUniqueId).collect(Collectors.toList());
				Collections.sort(ids);
				final Metric<MetricValue<?>> metric = informationStore
						.getLatestByType(ActiveTopologyControlAlgorithmMetric.class);
				final MetricPlottingAdapter adapter = plottingAdapterManager.registerMetricAdapterIfNecessary(
						plottingView, metric, ids.get(0), ids.get(ids.size() - 1), MetricPlottingAdapter.NO_AXIS_LABEL,
						MetricPlottingAdapter.NO_AXIS_LABEL);
				adapter.newMetricValue(Time.getCurrentTime(), metric,
						ActiveTopologyControlAlgorithmMetric.NOT_AVAILABLE);
			}

			boolean plotMessageStatistics = true;
			if (plotMessageStatistics) {
				plot(CheckpointedSentMessageSizeMetric.class, -1, plottingView, informationStore);
				plot(CheckpointedReceivedMessageSizeMetric.class, -1, plottingView, informationStore);
			}

			boolean plotMapeStatistics = false;
			if (plotMapeStatistics) {
				{
					final Metric<MetricValue<?>> metric = informationStore
							.getLatestByType(AdaptationLogicIntermediateDurationMetric.class);
					if (metric != null) {
						final MetricPlottingAdapter adapter = plottingAdapterManager.registerMetricAdapterIfNecessary(
								plottingView, metric, "Sim. time", "Inter-AL time [s]");
						adapter.newMetricValue(Time.getCurrentTime(), metric, -1, 1e-6);
					}
				}
				{
					final Metric<MetricValue<?>> metric = informationStore
							.getLatestByType(AdaptationLogicMapeLoopDurationMetric.class);
					final MetricPlottingAdapter adapter = plottingAdapterManager
							.registerMetricAdapterIfNecessary(plottingView, metric, "Sim. time", "AL MAPE time [ms]");
					adapter.newMetricValue(Time.getCurrentTime(), metric, -1, 1e-3);
				}
				{
					final Metric<MetricValue<?>> metric = informationStore
							.getLatestByType(AdaptationLogicPlanningDurationMetric.class);
					if (metric != null) {
						final MetricPlottingAdapter adapter = plottingAdapterManager.registerMetricAdapterIfNecessary(
								plottingView, metric, "Sim. time", "AL planning time [ms]");
						adapter.newMetricValue(Time.getCurrentTime(), metric, -1, 1e-3);
					}
				}

				{
					final Metric<MetricValue<?>> metric = informationStore
							.getLatestByType(AdaptationLogicObjectiveValueMetric.class);
					if (metric != null) {
						final MetricPlottingAdapter adapter = plottingAdapterManager
								.registerMetricAdapterIfNecessary(plottingView, metric, "Sim. time", "Objective value");
						adapter.newMetricValue(Time.getCurrentTime(), metric, -1, 1e-3);
					}
				}

				{
					final Metric<MetricValue<?>> mapeDuration = informationStore
							.getLatestByType(AdaptationLogicMapeLoopDurationMetric.class);
					final Metric<MetricValue<?>> planningDuration = informationStore
							.getLatestByType(AdaptationLogicPlanningDurationMetric.class);
					final Metric<MetricValue<?>> maeDuration = new DifferenceMetric(mapeDuration, planningDuration);
					final MetricPlottingAdapter adapter = plottingAdapterManager.registerMetricAdapterIfNecessary(
							plottingView, maeDuration, "Sim. time", "AL MAE time [ms]");
					adapter.newMetricValue(Time.getCurrentTime(), maeDuration, -1, 1e-3);
				}
			}

		}

		private void plot(final Class<? extends Metric<?>> metricType, final double markerForUnavailableData,
				PlottingView plottingView, TopologyControlInformationStoreComponent informationStore) {
			final Metric<MetricValue<?>> metric = informationStore.getLatestByType(metricType);
			final MetricPlottingAdapter adapter = plottingAdapterManager.registerMetricAdapterIfNecessary(plottingView,
					metric);
			adapter.newMetricValue(Time.getCurrentTime(), metric, markerForUnavailableData);
		}

		private void reschedule() {
			Simulator.getScheduler().scheduleIn(ComponentFinder.findTopologyControlComponent().getMonitoringInterval(),
					this, null, 0);
		}
	}
}
