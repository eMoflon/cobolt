package de.tudarmstadt.maki.simonstrator.tc.component;

import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphElementProperties;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSTypes;
import de.tudarmstadt.maki.simonstrator.tc.TopologyControlUtils;
import de.tudarmstadt.maki.simonstrator.tc.facade.ITopologyControlFacade;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.TopologyControlInformationStoreComponent;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.BatteryLevelMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.BatteryPercentageMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.HopCountToBaseStationMetric;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.TopologyControlComponentConfig;
import de.tudarmstadt.maki.simonstrator.tc.underlay.EdgeState;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;
import de.tudarmstadt.maki.simonstrator.tc.weighting.EdgeWeightProvider;

public class TopologyControlComponentAttributeHelper {

	public static final double INVALID_DATAPOINT_MARKER = Double.NaN;

	private static final double MAXIMUM_POSSIBLE_LIFETIME = 1e100;

	/**
	 * The default value that is used to determine whether an attribute value has
	 * changed
	 */
	public static final double DEFAULT_THRESHOLD_FOR_ATTRIBUTE_COMPARISON = 0.05;

	private final TopologyControlComponent component;

	private final double thresholdForAttributeComparison = DEFAULT_THRESHOLD_FOR_ATTRIBUTE_COMPARISON;

	public TopologyControlComponentAttributeHelper(final TopologyControlComponent component) {
		this.component = component;
	}

	Double getStoredWeight(final IEdge edge) {
		return extractAndSetPropertyFromFacade(edge, UnderlayTopologyProperties.WEIGHT);
	}

	Double getStoredRemainingEnergy(final INode node) {
		return extractAndSetPropertyFromFacade(node, UnderlayTopologyProperties.REMAINING_ENERGY);
	}

	Integer getStoredHopCount(final INode node) {
		return extractAndSetPropertyFromFacade(node, UnderlayTopologyProperties.HOP_COUNT);
	}

	Double getStoredLatitude(final INode node) {
		return extractAndSetPropertyFromFacade(node, UnderlayTopologyProperties.LATITUDE);
	}

	Double getStoredLongitude(final INode node) {
		return extractAndSetPropertyFromFacade(node, UnderlayTopologyProperties.LONGITUDE);
	}

	Double getStoredAngle(final IEdge edge) {
		return extractAndSetPropertyFromFacade(edge, UnderlayTopologyProperties.ANGLE);
	}

	Double getStoredEstimatedTransmissionPower(final IEdge edge) {
		return extractAndSetPropertyFromFacade(edge, UnderlayTopologyProperties.REQUIRED_TRANSMISSION_POWER);
	}

	Double getStoredEstimatedLifetime(final IEdge edge) {
		return extractAndSetPropertyFromFacade(edge, UnderlayTopologyProperties.EXPECTED_LIFETIME_PER_EDGE);
	}

	Double getStoredDistance(final IEdge edge) {
		return extractAndSetPropertyFromFacade(edge, UnderlayTopologyProperties.DISTANCE);
	}

	double calculateRequiredTransmissionPower(final IEdge edge, final Graph graph,
			final TopologyControlComponentAttributeHelper attributeHelper) {
		final EdgeWeightProvider distanceToTransmissionPowerFunction = this
				.getConfiguration().distanceToTransmissionPowerFunction;
		if (distanceToTransmissionPowerFunction != null) {
			return distanceToTransmissionPowerFunction.calculateWeight(edge, graph);
		} else {
			return Math.pow(edge.getProperty(UnderlayTopologyProperties.DISTANCE),
					this.getConfiguration().requiredTransmissionPowerExponent);
		}
	}

	TopologyControlComponentConfig getConfiguration() {
		return this.component.getConfiguration();
	}

	double calculateExpectedRemainingLifetime(final IEdge edge, final Graph graph) {
		final Double batteryLevelInMicrojoule = graph.getNode(edge.fromId())
				.getProperty(UnderlayTopologyProperties.REMAINING_ENERGY);
		if (null == batteryLevelInMicrojoule)
			throw new IllegalStateException("Node " + graph.getNode(edge.fromId()) + " is missing the property "
					+ UnderlayTopologyProperties.REMAINING_ENERGY);

		final Double estimatedRequiredTransmissionPower = edge
				.getProperty(UnderlayTopologyProperties.REQUIRED_TRANSMISSION_POWER);
		if (null == estimatedRequiredTransmissionPower)
			throw new IllegalStateException("Edge " + edge + " is missing the property "
					+ UnderlayTopologyProperties.REQUIRED_TRANSMISSION_POWER);

		final double expectedLifetime = batteryLevelInMicrojoule / 1e6 / estimatedRequiredTransmissionPower;

		return expectedLifetime;
	}

	Double calculatePhysicalDistance(final IEdge edge) {
		final SiSType<Double> weightProperty = SiSTypes.PHY_DISTANCE;
		final Double weightInInputTopology = edge.getProperty(weightProperty);

		GraphElementProperties.validateThatPropertyIsPresent(edge, weightProperty);

		return weightInInputTopology;
	}

	/**
	 * Returns the current energy level of the battery of the given node in uJoule
	 */
	double calculateEnergyLevel(final INodeID nodeID) {
		return (double) getPerNodeMetric(nodeID, getInformationStore().getLatestByType(BatteryLevelMetric.class));
	}

	boolean isBatteryEmpty(final INodeID nodeID) {
		return calculateEnergyLevel(nodeID) == 0.0;
	}

	private TopologyControlInformationStoreComponent getInformationStore() {
		return this.component.getInformationStore();
	}

	/**
	 * Returns the relative current energy level of the battery in percent
	 */
	public double calculateEnergyPercentage(final INodeID nodeID) {
		return (double) getPerNodeMetric(nodeID, getInformationStore().getLatestByType(BatteryPercentageMetric.class));
	}

	/**
	 * Returns the hop count to the configured reference node
	 */
	int calculateHopCount(final INodeID nodeID) {
		return (int) getPerNodeMetric(nodeID, getInformationStore().getLatestByType(HopCountToBaseStationMetric.class));
	}

	private Object getPerNodeMetric(final INodeID nodeID, final Metric<MetricValue<?>> data) {
		return data.getPerHostMetric(nodeID).getValue();
	}

	double calculateLatitude(final INode node) {
		final Location location = node.getProperty(SiSTypes.PHY_LOCATION);
		if (location != null)
			return location.getLatitude();
		else
			return Double.NaN;
	}

	double calculateLongitude(final INode node) {
		final Location location = node.getProperty(SiSTypes.PHY_LOCATION);
		if (location != null)
			return location.getLongitude();
		else
			return Double.NaN;
	}

	Double extractAndSetPropertyFromFacade(final IEdge edge, final SiSType<Double> property) {
		final Double value = getIncrementalFacade().getGraph().getEdge(edge.getId()).getProperty(property);
		if (value != null)
			edge.setProperty(property, value);
		return value;
	}

	<T> T extractAndSetPropertyFromFacade(final INode node, final SiSType<T> property) {
		final T value = getIncrementalFacade().getGraph().getNode(node.getId()).getProperty(property);
		if (value != null)
			node.setProperty(property, value);
		return value;
	}

	<T> T extractAndSetPropertyFromFacade(final TopologyControlComponent topologyControlComponent, final INode node,
			final SiSType<T> property) {
		final T value = getIncrementalFacade().getGraph().getNode(node.getId()).getProperty(property);
		if (value != null)
			node.setProperty(property, value);
		return value;
	}

	ITopologyControlFacade getIncrementalFacade() {
		return this.component.getTopologyControlFacade();
	}

	/**
	 * Determines the angle in degree
	 *
	 * @param edge
	 * @param inputTopology
	 * @return
	 */
	double calculateAngle(final IEdge edge, final Graph inputTopology) {

		final Location sourceLocation = inputTopology.getNode(edge.fromId()).getProperty(SiSTypes.PHY_LOCATION);
		final Location targetLocation = inputTopology.getNode(edge.toId()).getProperty(SiSTypes.PHY_LOCATION);

		if (sourceLocation != null && targetLocation != null) {
			final double sourceLat = sourceLocation.getLatitude();
			final double sourceLng = sourceLocation.getLongitude();
			final double targetLat = targetLocation.getLatitude();
			final double targetLng = targetLocation.getLongitude();
			return Math.atan2(sourceLat - targetLat, sourceLng - targetLng) * (180 / Math.PI);
		} else {
			return INVALID_DATAPOINT_MARKER;
		}
	}

	double calculateWeight(final TopologyControlComponent topologyControlComponent, final IEdge edge,
			final Graph graph) {
		return topologyControlComponent.getConfiguration().edgeWeightCalculatingFunction.calculateWeight(edge, graph);
	}

	void initializeNode(final INode node) {
		node.setProperty(UnderlayTopologyProperties.REMAINING_ENERGY, calculateEnergyLevel(node.getId()));
		node.setProperty(UnderlayTopologyProperties.HOP_COUNT, calculateHopCount(node.getId()));
		node.setProperty(UnderlayTopologyProperties.LATITUDE, calculateLatitude(node));
		node.setProperty(UnderlayTopologyProperties.LONGITUDE, calculateLongitude(node));
		node.setProperty(UnderlayTopologyProperties.LOCAL_VIEW_HORIZON,
				this.component.getConfiguration().topologyMonitoringLocalViewSize);
		if (this.isBaseStationNode(node)) {
			node.setProperty(UnderlayTopologyProperties.BASE_STATION_PROPERTY, true);
		}
	}

	private boolean isBaseStationNode(final INode node) {
		return component.getHost().getId().equals(node.getId());
	}

	List<SiSType<?>> updateNode(final INode node) {
		final List<SiSType<?>> changedProperties = new ArrayList<>();
		{
			final double newBatteryLevel = calculateEnergyLevel(node.getId());
			final Double oldBatteryValue = getStoredRemainingEnergy(node);
			if (TopologyControlUtils.isRelativeDifferenceLargerThanThreshold(oldBatteryValue, newBatteryLevel,
					getThresholdForAttributeComparison())) {
				node.setProperty(UnderlayTopologyProperties.REMAINING_ENERGY, newBatteryLevel);
				changedProperties.add(UnderlayTopologyProperties.REMAINING_ENERGY);
			}
		}

		{
			final int newHopCount = calculateHopCount(node.getId());
			final Integer oldHopCount = getStoredHopCount(node);
			if (TopologyControlUtils.isRelativeDifferenceLargerThanThreshold(oldHopCount, newHopCount,
					getThresholdForAttributeComparison())) {
				node.setProperty(UnderlayTopologyProperties.HOP_COUNT, newHopCount);
				changedProperties.add(UnderlayTopologyProperties.HOP_COUNT);
			}
		}

		{
			final Double newLatitude = calculateLatitude(node);
			final Double oldLatitude = getStoredLatitude(node);
			if (TopologyControlUtils.isRelativeDifferenceLargerThanThreshold(oldLatitude, newLatitude,
					getThresholdForAttributeComparison())) {
				node.setProperty(UnderlayTopologyProperties.LATITUDE, newLatitude);
				changedProperties.add(UnderlayTopologyProperties.LATITUDE);
			}
		}

		{
			final Double newLongitude = calculateLongitude(node);
			final Double oldLongitude = getStoredLongitude(node);
			if (TopologyControlUtils.isRelativeDifferenceLargerThanThreshold(oldLongitude, newLongitude,
					getThresholdForAttributeComparison())) {
				node.setProperty(UnderlayTopologyProperties.LONGITUDE, newLongitude);
				changedProperties.add(UnderlayTopologyProperties.LONGITUDE);
			}
		}
		return changedProperties;
	}

	public double calculateExpectedLifetime(final INodeID nodeId, final Graph graph) {
		double minEstimatedRequiredTransmissionPower = MAXIMUM_POSSIBLE_LIFETIME;
		for (final IEdge edge : getIncrementalFacade().getGraph().getOutgoingEdges(nodeId)) {
			final double expectedLifetime = calculateExpectedRemainingLifetime(edge, graph);
			minEstimatedRequiredTransmissionPower = Math.min(minEstimatedRequiredTransmissionPower, expectedLifetime);
		}
		return minEstimatedRequiredTransmissionPower;
	}

	void initializeEdge(final TopologyControlComponent topologyControlComponent, final IEdge edge,
			final Graph inputTopology) {
		edge.setProperty(UnderlayTopologyProperties.EDGE_STATE, EdgeState.UNCLASSIFIED);
		edge.setProperty(UnderlayTopologyProperties.DISTANCE, calculatePhysicalDistance(edge));
		edge.setProperty(UnderlayTopologyProperties.REQUIRED_TRANSMISSION_POWER,
				calculateRequiredTransmissionPower(edge, inputTopology, this));
		edge.setProperty(UnderlayTopologyProperties.EXPECTED_LIFETIME_PER_EDGE,
				calculateExpectedRemainingLifetime(edge, inputTopology));
		edge.setProperty(UnderlayTopologyProperties.WEIGHT,
				calculateWeight(topologyControlComponent, edge, inputTopology));
		edge.setProperty(UnderlayTopologyProperties.ANGLE, calculateAngle(edge, inputTopology));
	}

	boolean updateEdge(final TopologyControlComponent topologyControlComponent, final IEdge edge,
			final Graph inputTopology) {
		final Graph facadeGraph = topologyControlComponent.getTopologyControlFacade().getGraph();
		boolean isLinkModified = false;
		{
			final double calculatedDistance = calculatePhysicalDistance(edge);
			final Double storedDistance = getStoredDistance(edge);
			if (TopologyControlUtils.isRelativeDifferenceLargerThanThreshold(storedDistance, calculatedDistance,
					getThresholdForAttributeComparison())) {
				edge.setProperty(UnderlayTopologyProperties.DISTANCE, calculatedDistance);
				isLinkModified = true;
			}
		}

		{
			final double calculatedTransmissionPower = calculateRequiredTransmissionPower(edge, inputTopology, this);
			final Double storedTransmissionPower = getStoredEstimatedTransmissionPower(edge);
			if (TopologyControlUtils.isRelativeDifferenceLargerThanThreshold(storedTransmissionPower,
					calculatedTransmissionPower, getThresholdForAttributeComparison())) {
				edge.setProperty(UnderlayTopologyProperties.REQUIRED_TRANSMISSION_POWER, calculatedTransmissionPower);
				isLinkModified = true;
			}
		}

		{
			final double calculatedExpectedLifetime = calculateExpectedRemainingLifetime(edge, inputTopology);
			final Double storedEstimatedLifetime = getStoredEstimatedLifetime(edge);

			if (TopologyControlUtils.isRelativeDifferenceLargerThanThreshold(storedEstimatedLifetime,
					calculatedExpectedLifetime, getThresholdForAttributeComparison())) {
				edge.setProperty(UnderlayTopologyProperties.EXPECTED_LIFETIME_PER_EDGE, calculatedExpectedLifetime);
				isLinkModified = true;
			}
		}

		{
			final double calculatedWeight = calculateWeight(topologyControlComponent, edge, facadeGraph);
			final Double storedWeight = getStoredWeight(edge);
			if (TopologyControlUtils.isRelativeDifferenceLargerThanThreshold(storedWeight, calculatedWeight,
					getThresholdForAttributeComparison())) {
				edge.setProperty(UnderlayTopologyProperties.WEIGHT, calculatedWeight);
				isLinkModified = true;
			}
		}

		{
			final double newAngle = calculateAngle(edge, facadeGraph);
			final Double oldAngle = getStoredAngle(edge);
			if (TopologyControlUtils.isRelativeDifferenceLargerThanThreshold(oldAngle, newAngle,
					getThresholdForAttributeComparison())) {
				edge.setProperty(UnderlayTopologyProperties.ANGLE, newAngle);
				isLinkModified = true;
			}
		}
		return isLinkModified;
	}

	private double getThresholdForAttributeComparison() {
		return this.thresholdForAttributeComparison;
	}

	public int calculateEdgeCountBetweenNonEmptyNodes(final Graph inputTopology) {
		int edgeCount = 0;
		for (final IEdge edge : inputTopology.getEdges()) {
			if (!isBatteryEmpty(edge.fromId()) && !isBatteryEmpty(edge.toId()))
				++edgeCount;
		}
		return edgeCount;
	}
}
