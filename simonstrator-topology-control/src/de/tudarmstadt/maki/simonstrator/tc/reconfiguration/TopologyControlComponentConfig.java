package de.tudarmstadt.maki.simonstrator.tc.reconfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.tc.component.ITopologyControlMonitoringComponent;
import de.tudarmstadt.maki.simonstrator.tc.component.SimpleTopologyProvider;
import de.tudarmstadt.maki.simonstrator.tc.facade.ITopologyControlFacade;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmParamters;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlOperationMode;
import de.tudarmstadt.maki.simonstrator.tc.filtering.EdgeFilterFactory;
import de.tudarmstadt.maki.simonstrator.tc.scenario.ScenarioType;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;
import de.tudarmstadt.maki.simonstrator.tc.utils.DateHelper;
import de.tudarmstadt.maki.simonstrator.tc.weighting.EdgeWeightProvider;

/**
 * Represents all configuration options for
 *
 * @author Roland Kluge - Initial implementation
 *
 */
public class TopologyControlComponentConfig {

	// === Markers for uninitialized fields ===
	public static final String NOT_SET_STR = "NOT_SET";

	public static final double NOT_SET_DOUBLE = Double.NaN;

	public static final int NOT_SET_INT = Integer.MAX_VALUE;

	public static final long NOT_SET_LONG = Long.MAX_VALUE;

	public static final Double[] NOT_SET_DOUBLE_ARRAY = new Double[] { NOT_SET_DOUBLE };

	public static final int DEFAULT_TOPOLOGY_MONITORING_MAXIMUM_LOCAL_VIEW_SIZE = 2;

	public static final String DEFAULT_OUTPUT_PREFIX = "";

	/**
	 * Indicates that the largest possible/best line in the SPLConqueror logfile
	 * should be used.
	 */
	public static final int SPLC_LINE_NUMBER_USE_MAXIMUM = 0;

	// === FILES / FOLDERS ===
	public String simulationConfigurationFile = NOT_SET_STR;

	public File outputFolder = null;

	public File tracesOutputFolder = null;

	public String outputFilePrefix = DEFAULT_OUTPUT_PREFIX;

	public String evaluationDataFilename = NOT_SET_STR;

	public String logfileName = NOT_SET_STR;

	// === METADATA ===
	public long seed = NOT_SET_LONG;

	public int nodeCount = NOT_SET_INT;

	public int worldSize = NOT_SET_INT;

	public String end;

	public double batteryCapacitySensor = NOT_SET_DOUBLE;

	public double batteryCapacityMaster = NOT_SET_DOUBLE;

	public Host host = null;

	/**
	 * A descriptive name of this configuration
	 */
	public String name = NOT_SET_STR;

	/**
	 * An integer that signifies the number of this configuration in a batch run of
	 * multiple configurations
	 */
	public int configurationNumber;

	// === MOVEMENT ===
	public String movementModel = NOT_SET_STR;

	public String movementModelMaster = NOT_SET_STR;

	// A valid 'time string', e.g., 1h, 5m, 3s
	public String movementInterval = NOT_SET_STR;

	// Speed in meter/second
	public double movementMaxSpeedInMetersPerSecond = NOT_SET_DOUBLE;

	// === Underlay / Topology Control ===
	public ITopologyControlFacade incrementalTopologyControlFacade = null;

	public TopologyControlOperationMode topologyControlOperationMode = TopologyControlOperationMode.NOT_SET;

	public ITopologyControlFacade eventRecordingFacade = null;

	public SimpleTopologyProvider inputTopologyProvider;

	public SimpleTopologyProvider outputTopologyProvider;

	public TopologyControlAlgorithmID topologyControlAlgorithmID;

	public TopologyControlAlgorithmParamters topologyControlAlgorithmParamters = new TopologyControlAlgorithmParamters();

	public TopologyControlFrequencyMode topologyControlFrequencyMode = TopologyControlFrequencyMode.PERIODIC;

	public double topologyControlIntervalInMinutes = NOT_SET_DOUBLE;

	public int topologyMonitoringLocalViewSize = DEFAULT_TOPOLOGY_MONITORING_MAXIMUM_LOCAL_VIEW_SIZE;

	public double requiredTransmissionPowerExponent = NOT_SET_DOUBLE;

	public EdgeWeightProvider edgeWeightCalculatingFunction = null;

	public EdgeWeightProvider distanceToTransmissionPowerFunction = null;

	public List<EdgeFilterFactory> edgeFilterFactories = new ArrayList<>();

	public double minimumDistanceThresholdInMeters = NOT_SET_DOUBLE;

	public String nodePreprocessorClass = "org.cobolt.algorithms.facade.preprocessing.NullNodePreprocessor";

	public boolean nodePreprocessorShallReverseOrder = false;

	// === Overlay
	public ScenarioType scenario = null;

	public List<ITopologyControlReconfigurationComponent> reconfigurationComponents = new ArrayList<>();

	public List<ITopologyControlMonitoringComponent> monitoringComponents = new ArrayList<>();

	public double reconfigurationIntervalInMinutes = 15.0;

	public double monitoringIntervalInMinutes;

	public double datacollectionProbability = 0.0;

	// === Reconfiguration component ===
	/**
	 * If true, reconfiguration are enabled
	 */
	public boolean reconfigurationEnabled = true;

	public String splcOutputFile;

	/**
	 * The number of the line to be used for extracting weighted feature
	 * interactions Should be in the range of valid CSV line numbers in the
	 * configured {@link #splcOutputFile} or {@link #SPLC_LINE_NUMBER_USE_MAXIMUM}
	 * to always use the highest possible number
	 */
	public int splcFeatureInteractionLineNumber = SPLC_LINE_NUMBER_USE_MAXIMUM;

	/**
	 * The seed to be used by the adaptation logic (e.g., for random number
	 * generators).
	 */
	public int adaptationLogicSeed = 0;

	/**
	 * The planner type to be used during reconfiguration. The reconfiguration
	 * component should be able to interpret the given string For not configuring
	 * any specific planner, use <code>null</code>.
	 */
	public String adaptationLogicPlanner = null;

	public NonfunctionalProperty goalNonfunctionalProperty = null;

	public void validate() throws IllegalStateException {
		if (null == topologyControlAlgorithmID)
			throw new TopologyControlComponentConfigValidationException("Algorithm ID", "set", null, this);

		if (nodeCount <= 0)
			throw new TopologyControlComponentConfigValidationException("Node count", "positive", nodeCount, this);

		if (worldSize <= 0)
			throw new TopologyControlComponentConfigValidationException("World size", "positive", worldSize, this);

		if (batteryCapacitySensor <= 0)
			throw new TopologyControlComponentConfigValidationException("Battery capacity of sensor",
					"strictly positive", batteryCapacitySensor, this);

		if (batteryCapacityMaster <= 0)
			throw new TopologyControlComponentConfigValidationException("Battery capacity of master",
					"strictly positive", batteryCapacityMaster, this);

		if (!Double.isNaN(TopologyControlComponentConfig.NOT_SET_DOUBLE) && requiredTransmissionPowerExponent <= 0.0)
			throw new TopologyControlComponentConfigValidationException("Transmission power exponent",
					"strictly positive", batteryCapacityMaster, this);

		if (this.topologyControlIntervalInMinutes <= 0.0)
			throw new TopologyControlComponentConfigValidationException("Topology Control interval",
					"strictly positive", topologyControlIntervalInMinutes, this);

		if (null == this.edgeWeightCalculatingFunction)
			throw new TopologyControlComponentConfigValidationException("Edge weight provider", "set", null, this);

		if (this.topologyMonitoringLocalViewSize <= 0)
			throw new TopologyControlComponentConfigValidationException("Local view size", "strictly positive",
					this.topologyMonitoringLocalViewSize, this);

		if (this.isReconfigurationEnabled()) {
			if (this.goalNonfunctionalProperty == null)
				throw new TopologyControlComponentConfigValidationException("Goal NFP", "set", null, this);

			if (this.splcOutputFile != null && !new File(this.splcOutputFile).exists())
				throw new TopologyControlComponentConfigValidationException("SPLC output file",
						"pointing to an existing file", this.splcOutputFile, this);
		}
	}

	private static class TopologyControlComponentConfigValidationException extends IllegalStateException {

		private static final long serialVersionUID = 1L;

		private TopologyControlComponentConfigValidationException(final String propertyName,
				final String conditionDescription, final int actualValue, final TopologyControlComponentConfig config) {
			this(propertyName, conditionDescription, Integer.toString(actualValue), config);
		}

		private TopologyControlComponentConfigValidationException(final String propertyName,
				final String conditionDescription, final double actualValue,
				final TopologyControlComponentConfig config) {
			this(propertyName, conditionDescription, Double.toString(actualValue), config);
		}

		private TopologyControlComponentConfigValidationException(final String propertyName,
				final String conditionDescription, final Object actualValue,
				final TopologyControlComponentConfig config) {
			super(String.format("%s must be %s, but is actually %s for config. %s", propertyName, conditionDescription,
					actualValue, config));
		}
	}

	public static List<String> getCSVHeader() {
		final List<String> csvLine = new ArrayList<>();

		csvLine.add("configurationNumber");
		csvLine.add("name");
		//
		csvLine.add("nodeCount");
		csvLine.add("worldSize");
		csvLine.add("seed");
		csvLine.add("end");
		//
		csvLine.add("topologyControlAlgorithmID");
		csvLine.add("topologyControlAlgorithmParamters");
		csvLine.add("topologyControlExecutionMode");
		csvLine.add("topologyControlIntervalInMinutes");
		//
		csvLine.add("simulationConfigurationFile");
		csvLine.add("outputFolder");
		csvLine.add("outputFilePrefix");
		csvLine.add("evaluationDataFilename");
		//
		csvLine.add("nodePreprocessorClass");
		csvLine.add("nodePreprocessorShallReverseOrder");

		return Collections.unmodifiableList(csvLine);
	}

	public List<String> getCSVLine() {
		final List<String> csvHeader = getCSVHeader();
		final List<String> csvLine = new ArrayList<>(csvHeader.size());

		csvLine.add(Integer.toString(configurationNumber));
		csvLine.add(name);
		//
		csvLine.add(Integer.toString(nodeCount));
		csvLine.add(Integer.toString(worldSize));
		csvLine.add(Long.toString(seed));
		csvLine.add(end);
		//
		csvLine.add(topologyControlAlgorithmID.getName());
		csvLine.add(topologyControlAlgorithmParamters.toString());
		csvLine.add(topologyControlFrequencyMode.toString());
		csvLine.add(Double.toString(topologyControlIntervalInMinutes));
		//
		csvLine.add(simulationConfigurationFile);
		csvLine.add(outputFolder.toString());
		csvLine.add(outputFilePrefix);
		csvLine.add(evaluationDataFilename);
		//
		csvLine.add(nodePreprocessorClass);
		csvLine.add(Boolean.toString(nodePreprocessorShallReverseOrder));

		if (csvLine.size() != csvHeader.size())
			throw new IllegalStateException(
					String.format("CSV line and CSV header have different length. CSV line: %d, CSV header: %d",
							csvLine.size(), csvHeader.size()));

		return Collections.unmodifiableList(csvLine);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("TopologyControlComponentConfig[");
		builder.append(",\n     configurationNumber=").append(configurationNumber);
		builder.append(",\n     name=").append(name);
		//
		builder.append(",\n     nodeCount=").append(nodeCount);
		builder.append(",\n     worldSize=").append(worldSize);
		builder.append(",\n     seed=").append(seed);
		builder.append(",\n     end=").append(end);
		builder.append(",\n     host=").append(host);
		if (host != null)
			builder.append(" with ID=").append(host.getId().valueAsString());
		// Files / Folders
		builder.append(",\n     simulationConfigurationFile=").append(simulationConfigurationFile);
		builder.append(",\n     outputFolder=").append(outputFolder);
		builder.append(",\n     tracesOutputFolder=").append(tracesOutputFolder);
		builder.append(",\n     outputFilePrefix=").append(outputFilePrefix);
		builder.append(",\n     evaluationDataFilename=").append(evaluationDataFilename);
		builder.append(",\n     logfileName=").append(logfileName);
		// Node configuration
		builder.append(",\n     batteryCapacitySensor=").append(batteryCapacitySensor);
		builder.append(",\n     batteryCapacityMaster=").append(batteryCapacityMaster);
		//
		builder.append(",\n     movementModel=").append(movementModel);
		builder.append(",\n     movementModelMaster=").append(movementModelMaster);
		builder.append(",\n     movementInterval=").append(movementInterval);
		builder.append(",\n     movementMaxSpeedInMetersPerSecond=").append(movementMaxSpeedInMetersPerSecond);
		// Underlay / Topology Control
		builder.append(",\n     topologyControlAlgorithmID=").append(topologyControlAlgorithmID);
		builder.append(",\n     topologyControlAlgorithmParamters=").append(topologyControlAlgorithmParamters);
		builder.append(",\n     topologyControlExecutionMode=").append(topologyControlFrequencyMode);
		builder.append(",\n     topologyControlIntervalInMinutes=").append(topologyControlIntervalInMinutes);

		builder.append(",\n     incrementalTopologyControlFacade=").append(incrementalTopologyControlFacade);
		builder.append(",\n     topologyControlOperationMode=").append(topologyControlOperationMode);
		builder.append(",\n     eventRecordingFacade=").append(eventRecordingFacade);
		//
		builder.append(",\n     requiredTransmissionPowerExponent=").append(requiredTransmissionPowerExponent);
		builder.append(",\n     edgeWeightCalculatingFunction=").append(edgeWeightCalculatingFunction);
		builder.append(",\n     distanceToTransmissionPowerFunction=").append(distanceToTransmissionPowerFunction);
		builder.append(",\n     edgeFilterFactories=").append(edgeFilterFactories);
		builder.append(",\n     minimumDistanceThresholdInMeters=").append(minimumDistanceThresholdInMeters);
		builder.append(",\n     nodePreprocessorClass=").append(nodePreprocessorClass);
		builder.append(",\n     nodePreprocessorShallReverseOrder=").append(nodePreprocessorShallReverseOrder);
		// Overlay
		builder.append(",\n     scenarioType=").append(scenario);
		builder.append(",\n     datacollectionProbability=").append(datacollectionProbability);
		// Reconfiguration
		builder.append(",\n     reconfigurationEnabled=").append(isReconfigurationEnabled());
		builder.append(",\n     splcOutputFile=").append(splcOutputFile);
		builder.append(",\n     adaptationLogicSeed=").append(adaptationLogicSeed);
		builder.append(",\n     goalNonfunctionalProperty=").append(goalNonfunctionalProperty);
		// Markers for uninitialized fields
		builder.append(",\n     Constants:");
		builder.append(" NOT_SET_STR=").append(NOT_SET_STR);
		builder.append(", NOT_SET_DOUBLE=").append(NOT_SET_DOUBLE);
		builder.append(", NOT_SET_INT=").append(NOT_SET_INT);
		builder.append(", NOT_SET_LONG=").append(NOT_SET_LONG);
		builder.append("\n");
		builder.append("\n]");
		return builder.toString();
	}

	public void setConfigurationNumber(final int configurationNumber) {
		this.configurationNumber = configurationNumber;
	}

	public int getConfigurationNumber() {
		return configurationNumber;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setTopologyControlAlgorithmParamters(
			final TopologyControlAlgorithmParamters topologyControlAlgorithmParamters) {
		this.topologyControlAlgorithmParamters = topologyControlAlgorithmParamters;
	}

	public void setTopologyControlAlgorithm(final String topologyControlAlgorithm) {
		this.topologyControlAlgorithmID = UnderlayTopologyControlAlgorithms
				.mapToTopologyControlID(topologyControlAlgorithm);
	}

	public void setRequiredTransmissionPowerExponent(final double requiredTransmissionPowerExponent) {
		this.requiredTransmissionPowerExponent = requiredTransmissionPowerExponent;
	}

	public void setTopologyControlExecutionMode(final String topologyControlExecutionMode) {
		this.topologyControlFrequencyMode = TopologyControlFrequencyMode.valueOf(topologyControlExecutionMode);
	}

	public void setTopologyControlIntervalInMinutes(final double topologyControlIntervalInMinutes) {
		this.topologyControlIntervalInMinutes = topologyControlIntervalInMinutes;
	}

	public void setTopologyMonitoringLocalViewSize(final int topologyMonitoringLocalViewSize) {
		this.topologyMonitoringLocalViewSize = topologyMonitoringLocalViewSize;
	}

	public void setNodeCount(final int nodeCount) {
		this.nodeCount = nodeCount;
	}

	public void setSeed(final long seed) {
		this.seed = seed;
	}

	public void setWorldSize(final int worldSize) {
		this.worldSize = worldSize;
	}

	public void setBatteryCapacitySensor(final double batteryCapacitySensor) {
		this.batteryCapacitySensor = batteryCapacitySensor;
	}

	public void setBatteryCapacityMaster(final double batteryCapacityMaster) {
		this.batteryCapacityMaster = batteryCapacityMaster;
	}

	public void setOutputFolder(final String outputFolder) {
		this.outputFolder = outputFolder.isEmpty() ? null : new File(DateHelper.substitutePlaceholders(outputFolder));
	}

	public void setTracesOutputFolder(final String outputFolder) {
		this.tracesOutputFolder = outputFolder.isEmpty() ? null : new File(outputFolder);
	}

	public void setTopologyControlFacade(final ITopologyControlFacade incrementalTopologyControlFacade) {
		this.incrementalTopologyControlFacade = incrementalTopologyControlFacade;
	}

	public void setTopologyControlOperationMode(final String topologyControlOperationMode) {
		this.topologyControlOperationMode = TopologyControlOperationMode.valueOf(topologyControlOperationMode);
	}

	public void setEventRecordingFacade(final ITopologyControlFacade eventRecordingFacade) {
		this.eventRecordingFacade = eventRecordingFacade;
	}

	public void setOutputFilePrefix(final String outputFilePrefix) {
		this.outputFilePrefix = outputFilePrefix;
	}

	public void setEnd(final String end) {
		this.end = end;
	}

	public void setMinimumDistanceThresholdInMeters(final double minimumDistanceThresholdInMeters) {
		this.minimumDistanceThresholdInMeters = minimumDistanceThresholdInMeters;
	}

	public void setMovementModel(final String movementModel) {
		this.movementModel = movementModel;
	}

	public void setMovementModelMaster(final String movementModelMaster) {
		this.movementModelMaster = movementModelMaster;
	}

	public void setMovementInterval(final String movementInterval) {
		this.movementInterval = movementInterval;
	}

	public void setMovementMaxSpeed(final double movementMaxSpeed) {
		this.movementMaxSpeedInMetersPerSecond = movementMaxSpeed;
	}

	/**
	 * The function used to calculate {@link UnderlayTopologyProperties#WEIGHT}
	 */
	public void setEdgeWeightingFunction(final EdgeWeightProvider edgeWeightCalculatingFunction) {
		this.edgeWeightCalculatingFunction = edgeWeightCalculatingFunction;
	}

	public void setEdgeFilterFactory(final EdgeFilterFactory factory) {
		this.edgeFilterFactories.add(factory);
	}

	/**
	 * Sets the function to estimate
	 * {@link UnderlayTopologyProperties#REQUIRED_TRANSMISSION_POWER}
	 */
	public void setDistanceToTransmissionPowerFunction(final EdgeWeightProvider distanceToTransmissionPowerFunction) {
		this.distanceToTransmissionPowerFunction = distanceToTransmissionPowerFunction;
	}

	/**
	 * Enable (-> true) or disable (-> false) reconfiguration support
	 */
	public void setReconfigurationEnabled(final boolean reconfigurationEnabled) {
		this.reconfigurationEnabled = reconfigurationEnabled;
	}

	public boolean isReconfigurationEnabled() {
		return this.reconfigurationEnabled;
	}

	public void setSplcOutputFile(final String splcOutputFile) {
		if (splcOutputFile != null && !splcOutputFile.isEmpty())
			this.splcOutputFile = splcOutputFile;
	}

	public void setSplcFeatureInteractionLineNumber(final int splcFeatureInteractionLineNumber) {
		this.splcFeatureInteractionLineNumber = splcFeatureInteractionLineNumber;
	}

	public void setAdaptationLogicSeed(final int adaptationLogicSeed) {
		this.adaptationLogicSeed = adaptationLogicSeed;
	}

	public void setAdaptationLogicPlanner(final String adaptationLogicPlanner) {
		if (!"".equals(adaptationLogicPlanner))
			this.adaptationLogicPlanner = adaptationLogicPlanner;
	}

	public void setScenario(final String scenario) {
		this.scenario = ScenarioType.valueOf(scenario);
	}

	public void setDatacollectionProbability(final double datacollectionProbability) {
		this.datacollectionProbability = datacollectionProbability;
	}

	public void setGoalNonfunctionalProperty(final String goalNonfunctionalProperty) {
		final Optional<NonfunctionalProperty> byName = NonfunctionalProperties.getByName(goalNonfunctionalProperty);

		if (!byName.isPresent())
			throw new IllegalArgumentException(
					String.format("Cannot find nonfunctional property for name %s", goalNonfunctionalProperty));

		this.goalNonfunctionalProperty = byName.get();
	}

	public void setReconfigurationComponent(
			final ITopologyControlReconfigurationComponent topologyControlReconfigurationComponent) {
		this.reconfigurationComponents.add(topologyControlReconfigurationComponent);
	}

	public void setMonitoringComponent(final ITopologyControlMonitoringComponent monitoringComponent) {
		this.monitoringComponents.add(monitoringComponent);
	}

	public void setMonitoringIntervalInMinutes(final double monitoringIntervalInMinutes) {
		this.monitoringIntervalInMinutes = monitoringIntervalInMinutes;
	}

	public void setInputTopologyProvider(final SimpleTopologyProvider inputTopologyProvider) {
		this.inputTopologyProvider = inputTopologyProvider;
	}

	public void setOutputTopologyProvider(final SimpleTopologyProvider outputTopologyProvider) {
		this.outputTopologyProvider = outputTopologyProvider;
	}

	/**
	 * Prints the current configuration in a way that it may easily be reused in
	 * code/XML.
	 */
	public void printToLog() {
		Monitor.log(TopologyControlComponentConfig.class, Level.INFO, "Configuration: %s", this);
	}

}
