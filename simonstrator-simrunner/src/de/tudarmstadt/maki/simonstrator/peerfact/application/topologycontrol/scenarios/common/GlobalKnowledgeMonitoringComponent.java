package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.tud.kom.p2psim.api.energy.EnergyModel;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.impl.energy.Battery;
import de.tud.kom.p2psim.impl.topology.DefaultTopologyComponent;
import de.tud.kom.p2psim.impl.topology.views.wifi.WifiTopologyView;
import de.tud.kom.p2psim.impl.util.oracle.GlobalOracle;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Oracle;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.common.metric.AbstractMetric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.datacollection.BaseStationApplication;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.CheckpointedLatencyMetric;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.CheckpointedReceivedMessageCountMetric;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.CheckpointedReceivedMessageSizeMetric;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.CheckpointedSentMessageCountMetric;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.CheckpointedSentMessageSizeMetric;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.GeoFenceSizeMetric;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.TotalLatencyMetric;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.TotalLinkDropRateMetric;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.TotalReceivedMessageCountMetric;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.TotalReceivedMessageSizeMetric;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.TotalSentMessageCountMetric;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.TotalSentMessageSizeMetric;
import de.tudarmstadt.maki.simonstrator.tc.GraphUtil;
import de.tudarmstadt.maki.simonstrator.tc.analyzer.metrics.ICheckpointedLatencyMetric;
import de.tudarmstadt.maki.simonstrator.tc.component.ITopologyControlMonitoringComponent;
import de.tudarmstadt.maki.simonstrator.tc.component.TopologyControlComponent;
import de.tudarmstadt.maki.simonstrator.tc.events.dto.MetricValueEvent;
import de.tudarmstadt.maki.simonstrator.tc.events.io.SocketTopologyEventReportingFacade;
import de.tudarmstadt.maki.simonstrator.tc.events.io.coalaviz.CoalaVizConstants;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.TopologyControlInformationStoreComponent;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.BatteryLevelMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.BatteryPercentageMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.HopCountToBaseStationMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.MetricAggregationOperator;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.MetricUtils;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.NumberOfNodesReachableFromBaseStation;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.SimpleNumericMetricValue;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.TimestampMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.TransmissionRangeMetric;

public class GlobalKnowledgeMonitoringComponent implements ITopologyControlMonitoringComponent {

	private static final PhyType PHY_TYPE = PhyType.WIFI;
	private TopologyControlComponent component;
	private long recentSocketReportingTime = -1;
	private final double socketReportingIntervalInMinutes = 1;

	@Override
	public void setParentComponent(final TopologyControlComponent component) {
		this.component = component;
	}

	@Override
	public void performMeasurement() {
		final List<Host> hosts = Oracle.getAllHosts();

		final TopologyControlInformationStoreComponent informationStore = this.component.getInformationStore();
		informationStore.put(new TransmissionRangeMetric(determineTransmissionRange()));

		{
			final Map<Host, MetricValue<?>> batteryLevel = new HashMap<>();
			hosts.forEach(host -> batteryLevel.put(host,
					new SimpleNumericMetricValue<Double>(getBattery(host).getCurrentEnergyLevel())));
			informationStore.put(new BatteryLevelMetric(batteryLevel));
		}

		{
			final Map<Host, MetricValue<?>> batteryPercentage = new HashMap<>();
			hosts.forEach(host -> batteryPercentage.put(host,
					new SimpleNumericMetricValue<Double>(getBattery(host).getCurrentPercentage())));
			informationStore.put(new BatteryPercentageMetric(batteryPercentage));
		}

		{
			final Map<Host, MetricValue<?>> hopCount = new HashMap<>();
			hosts.forEach(host -> hopCount.put(host, new SimpleNumericMetricValue<Integer>(determineHopCount(host))));
			informationStore.put(new HopCountToBaseStationMetric(hopCount));
		}

		informationStore.put(new TotalLinkDropRateMetric());

		informationStore
				.put(new NumberOfNodesReachableFromBaseStation(determineNumberOfNodesReachableFromBaseStation()));

		informationStore.put(new GeoFenceSizeMetric());

		final List<Host> hostsWithEvaluationApplication = GlobalOracle.getHosts().stream()
				.filter(GlobalKnowledgeMonitoringComponent::hasEvaluationApplication).collect(Collectors.toList());
		for (final AbstractMetric<MetricValue<?>> metric : Arrays.asList(//
				new TotalReceivedMessageCountMetric(), //
				new TotalReceivedMessageSizeMetric(), //
				new TotalSentMessageCountMetric(), //
				new TotalSentMessageSizeMetric(), //
				new TotalLatencyMetric(), //
				new CheckpointedReceivedMessageCountMetric(), //
				new CheckpointedReceivedMessageSizeMetric(), //
				new CheckpointedSentMessageCountMetric(), //
				new CheckpointedSentMessageSizeMetric(), //
				new CheckpointedLatencyMetric()//
		)) {
			metric.initialize(hostsWithEvaluationApplication);
			informationStore.put(metric);
		}

		final Metric<MetricValue<?>> latencyMetric = informationStore.getLatestByType(CheckpointedLatencyMetric.class);
		final double latency = MetricUtils.aggregateAccordingToMetricTypeAndOperator(latencyMetric,
				ICheckpointedLatencyMetric.NOT_AVAILABLE, MetricAggregationOperator.MEAN);
		if (!Double.isNaN(latency) && this.hasSocketReportingPassed()) {
			this.recentSocketReportingTime = getCurrentTime();

			if (this.component.getConfiguration().eventRecordingFacade != null) {
				final Metric<MetricValue<?>> timeStampMetric = informationStore.getLatestByType(TimestampMetric.class);
				final Long timestamp = (Long) timeStampMetric.getOverallMetric().getValue();
				final MetricValueEvent event = new MetricValueEvent("Latency", latency, timestamp);

				SocketTopologyEventReportingFacade.writeEvent(event, CoalaVizConstants.DEFAULT_HOST,
						CoalaVizConstants.DEFAULT_PORT);
			}
		}

		hostsWithEvaluationApplication.stream().map(TopologyControlEvaluationApplication_ImplBase::find)
				.forEach(application -> application.checkpointReached());
	}

	private boolean hasSocketReportingPassed() {
		final double nextReportingTime = this.recentSocketReportingTime
				+ this.socketReportingIntervalInMinutes * Time.MINUTE;
		return this.recentSocketReportingTime < 0 || getCurrentTime() >= nextReportingTime;
	}

	private long getCurrentTime() {
		return Time.getCurrentTime();
	}

	private static boolean hasEvaluationApplication(final Host host) {
		return TopologyControlEvaluationApplication_ImplBase.find(host) != null;
	}

	private Double determineTransmissionRange() {
		try {
			final DefaultTopologyComponent topoComponent = component.getConfiguration().host
					.getComponent(DefaultTopologyComponent.class);
			final WifiTopologyView topologyView = (WifiTopologyView) topoComponent.getTopology()
					.getTopologyView(PHY_TYPE);
			return topologyView.getRange();
		} catch (final ComponentNotAvailableException e) {
			return Double.NaN;
		}
	}

	private static Battery getBattery(final Host host) {
		try {
			return host.getComponent(EnergyModel.class).getInfo().getBattery();
		} catch (final ComponentNotAvailableException e) {
			throw new AssertionError("No battery on " + host);
		}
	}

	/**
	 * Returns the hop count to the configured reference node or
	 * {@link HopCountToBaseStationMetric#NOT_AVAILABLE}
	 */
	private int determineHopCount(final Host host) {
		final INodeID nodeID = host.getId();
		final INode baseStationNode = BaseStationApplication.getBaseStationNode();
		if (baseStationNode != null) {
			final Graph wifiTopology = this.component.getInputTopology();
			final Map<INodeID, Integer> nodeDepths = GraphUtil.calculateNodeDepths(nodeID, wifiTopology, true);
			final Integer distance = nodeDepths.get(baseStationNode.getId());
			return distance;
		} else {
			return HopCountToBaseStationMetric.NOT_AVAILABLE;
		}
	}

	private int determineNumberOfNodesReachableFromBaseStation() {
		final Graph graph = this.component.getInputTopology();
		final INode baseStationNode = BaseStationApplication.getBaseStationNode();
		if (baseStationNode == null)
			return NumberOfNodesReachableFromBaseStation.NOT_AVAILABLE;

		int numNodesReachableFromBaseStation = 0;
		final Map<INodeID, Integer> nodeDepths = GraphUtil.calculateNodeDepths(baseStationNode.getId(), graph);
		for (final INodeID nodeId : nodeDepths.keySet()) {
			if (nodeDepths.get(nodeId) != GraphUtil.INFINITE_DISTANCE)
				++numNodesReachableFromBaseStation;
		}
		return numNodesReachableFromBaseStation;
	}
}
