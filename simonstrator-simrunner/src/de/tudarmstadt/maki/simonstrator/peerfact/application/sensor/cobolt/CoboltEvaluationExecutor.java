package de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.cobolt;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.AbstractTopologyControlReconfigurationExecutor;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlOperationMode;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.TopologyControlComponentConfig;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.TopologyControlFrequencyMode;
import de.tudarmstadt.maki.simonstrator.tc.scenario.ScenarioType;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;
import de.tudarmstadt.maki.simonstrator.tc.weighting.DistanceEdgeWeightProvider;

public class CoboltEvaluationExecutor extends AbstractTopologyControlReconfigurationExecutor {

	private static final double MOVEMENT_SPEED_PEDESTRIAN = 1.4;
	private static final double MOVEMENT_SPEED_STATIC = 0.0;
	private static final String EXECUTOR_EXTENDED = "extended";
	private static final String EXECUTOR_DEFAULT = "default";
	private static final int WORLD_SIZE_SMALL = 300;
	private static final int WORLD_SIZE_MEDIUM = 600;
	private static final int WORLD_SIZE_LARGE = 900;
	private static final int MOTE_COUNT_FEW = 100;
	private static final int MOTE_COUNT_MANY = 250;

	/**
	 * Runs the runtime evaluation for the 'Topology Control Reconfiguration'
	 * project.
	 *
	 * For reproducibility, every simulation is started in a separate process.
	 *
	 * To stop the whole evaluation, open this process's console and type 'quit' +
	 * ENTER.
	 */
	public static void main(final String[] args) {
		final int exitCode = new CoboltEvaluationExecutor().run(args);
		System.exit(exitCode);
	}

	public CoboltEvaluationExecutor() {
		this.simulationConfigurationFile = new File("config/dissertation/cobolt.xml");
		this.outputFolder = new File("output/cobolt/");
	}

	@Override
	protected List<String> getSupportedExecutors() {
		return Arrays.asList(EXECUTOR_DEFAULT, EXECUTOR_EXTENDED);
	}

	@Override
	protected List<TopologyControlComponentConfig> generateSimulationConfigurations() {
		switch (this.getExecutor()) {
		case EXECUTOR_DEFAULT:
			return getDefaultEvaluationConfigurations();
		case EXECUTOR_EXTENDED:
			return getExtendedEvaluationConfigurations();
		default:
			throw new IllegalArgumentException("Unsupported executor chosen: " + this.getExecutor());
		}
	}

	private List<TopologyControlComponentConfig> getDefaultEvaluationConfigurations() {
		final List<TopologyControlComponentConfig> configs = new ArrayList<>();
		final List<ScenarioType> scenarioTypes = Arrays.asList(ScenarioType.DATACOLLECTION);
		for (final ScenarioType scenario : scenarioTypes) {

			final Object[][] nodeCountsPlusWorldSizesPlusSeeds = { //
					// { MOTE_COUNT_FEW, 100, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) }, //
					// { MOTE_COUNT_FEW, 200, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
					// }, //
					{ MOTE_COUNT_FEW, WORLD_SIZE_SMALL, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) }, //
					{ MOTE_COUNT_MANY, WORLD_SIZE_SMALL, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) }, //
					// { MOTE_COUNT_FEW, 400, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) }, //
					// { MOTE_COUNT_FEW, 500, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) }, //
					{ MOTE_COUNT_FEW, WORLD_SIZE_MEDIUM, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) }, //
					{ MOTE_COUNT_MANY, WORLD_SIZE_MEDIUM, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) }, //
					// { MOTE_COUNT_FEW, 700, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 13, 15,
					// 16, 17, 18,
					// 19, 20) }, //
					// { MOTE_COUNT_FEW, 800, Arrays.asList(2, 4, 5, 10, 11, 12, 13, 14, 15, 16, 17,
					// 18, 19, 20)
					// }, //
					{ MOTE_COUNT_FEW, WORLD_SIZE_LARGE, Arrays.asList(4, 11, 12, 13, 16, 19, 22, 27, 35, 38) }, //
					{ MOTE_COUNT_MANY, WORLD_SIZE_LARGE, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) }, //
			};
			for (final Object[] nodeCountsWithWorldSize : nodeCountsPlusWorldSizesPlusSeeds) {
				final int nodeCount = (int) nodeCountsWithWorldSize[0];
				final int worldSize = (int) nodeCountsWithWorldSize[1];
				@SuppressWarnings("unchecked")
				final List<Integer> completeSeedList = (List<Integer>) nodeCountsWithWorldSize[2];
				final List<Integer> seeds = completeSeedList.subList(0,
						Math.min(numberOfSeeds, completeSeedList.size()));
				final Double[] movementSpeeds = asArray(0.0, 1.4);
				for (final double movementSpeed : movementSpeeds) {

					final List<TopologyControlAlgorithmID> algorithms = asModifiableList( //
							// UnderlayTopologyControlAlgorithms.MAXPOWER_TC,
							UnderlayTopologyControlAlgorithms.D_KTC);
					for (final TopologyControlAlgorithmID algorithm : algorithms) {

						for (final TopologyControlOperationMode tcOperationMode : Arrays
								.asList(TopologyControlOperationMode.BATCH, TopologyControlOperationMode.INCREMENTAL)) {

							final Double[] kParameters = isKTCLikeAlgorithm(algorithm) ? asArray(1.41)
									: TopologyControlComponentConfig.NOT_SET_DOUBLE_ARRAY;
							for (final double tcParamterK : kParameters) {

								final TopologyControlFrequencyMode topologyControlFrequencyMode = TopologyControlFrequencyMode.PERIODIC;
								final Double[] topologyControlIntervalInMinutesList = { 1.0 };
								for (final double topologyControlIntervalInMinutes : topologyControlIntervalInMinutesList) {

									for (final int seed : seeds) {

										final int batteryCapacitySensor = 130;
										final int batteryCapacityMaster = isScenarioWithBaseStation(scenario) ? 1000000
												: 130;
										final double requiredTransmissionPowerExponent = 2.0;
										final DistanceEdgeWeightProvider weightProvider = DistanceEdgeWeightProvider
												.getInstance();
										final String endTime = "1h";
										final String outputPrefix = "cobolt";
										final String movementModelId = movementSpeed != 0.0 ? "GaussMarkov" : "None";

										final TopologyControlComponentConfig config = new TopologyControlComponentConfig();
										config.configurationNumber = configs.size() + 1;
										config.simulationConfigurationFile = simulationConfigurationFile
												.getAbsolutePath();
										config.seed = seed;
										config.topologyControlAlgorithmID = algorithm;
										config.topologyControlOperationMode = tcOperationMode;
										config.worldSize = worldSize;
										config.nodeCount = nodeCount;
										config.topologyControlAlgorithmParamters
												.put(UnderlayTopologyControlAlgorithms.KTC_PARAM_K, tcParamterK);
										config.topologyControlIntervalInMinutes = topologyControlIntervalInMinutes;
										config.topologyControlFrequencyMode = topologyControlFrequencyMode;
										config.topologyMonitoringLocalViewSize = 2;
										config.batteryCapacitySensor = batteryCapacitySensor;
										config.batteryCapacityMaster = batteryCapacityMaster;
										config.outputFolder = outputFolderForResults;
										config.tracesOutputFolder = outputFolderForResults;
										config.outputFilePrefix = outputPrefix;
										config.requiredTransmissionPowerExponent = requiredTransmissionPowerExponent;
										config.edgeWeightCalculatingFunction = weightProvider;
										config.movementModel = movementModelId;
										config.movementModelMaster = isScenarioWithBaseStation(scenario) ? "None"
												: movementModelId;
										config.movementMaxSpeedInMetersPerSecond = movementSpeed;
										config.movementInterval = topologyControlIntervalInMinutes >= 1.0
												? ((int) topologyControlIntervalInMinutes) + "m"
												: ((int) (topologyControlIntervalInMinutes * 60)) + "s";
										config.minimumDistanceThresholdInMeters = 0.0;
										config.end = endTime;
										config.scenario = scenario;
										config.datacollectionProbability = 1.0;
										config.reconfigurationEnabled = false;

										configs.add(config);
									}
								}
							}
						}
					}
				}
			}
		}

		return configs;
	}

	private List<TopologyControlComponentConfig> getExtendedEvaluationConfigurations() {
		final List<TopologyControlComponentConfig> configs = new ArrayList<>();
		final List<ScenarioType> scenarioTypes = Arrays.asList(ScenarioType.DATACOLLECTION);
		for (final ScenarioType scenario : scenarioTypes) {

			final Object[][] nodeCountsPlusWorldSizesPlusSeeds = { //
					{ MOTE_COUNT_FEW, 100, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) }, //
					// Many motes do not make sense here because the area is already crowded
					// { MOTE_COUNT_FEW, 200, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
					// }, //
					{ MOTE_COUNT_FEW, WORLD_SIZE_SMALL, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) }, //
					{ MOTE_COUNT_MANY, WORLD_SIZE_SMALL, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) }, //

					{ MOTE_COUNT_FEW, 400, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) }, //
					{ MOTE_COUNT_MANY, 400, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) }, //
					// { MOTE_COUNT_FEW, 500, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) }, //
					{ MOTE_COUNT_FEW, WORLD_SIZE_MEDIUM, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) }, //
					{ MOTE_COUNT_MANY, WORLD_SIZE_MEDIUM, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) }, //

					{ MOTE_COUNT_FEW, 700, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 13, 15, 16, 17, 18, 19, 20) }, //
					{ MOTE_COUNT_MANY, 700, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 13, 15, 16, 17, 18, 19, 20) }, //
					// { MOTE_COUNT_FEW, 800, Arrays.asList(2, 4, 5, 10, 11, 12, 13, 14, 15, 16, 17,
					// 18, 19, 20)
					// }, //
					{ MOTE_COUNT_FEW, WORLD_SIZE_LARGE, Arrays.asList(4, 11, 12, 13, 16, 19, 22, 27, 35, 38) }, //
					{ MOTE_COUNT_MANY, WORLD_SIZE_LARGE, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) }, //
			};
			for (final Object[] nodeCountsWithWorldSize : nodeCountsPlusWorldSizesPlusSeeds) {
				final int nodeCount = (int) nodeCountsWithWorldSize[0];
				final int worldSize = (int) nodeCountsWithWorldSize[1];
				@SuppressWarnings("unchecked")
				final List<Integer> completeSeedList = (List<Integer>) nodeCountsWithWorldSize[2];
				final List<Integer> seeds = completeSeedList.subList(0,
						Math.min(numberOfSeeds, completeSeedList.size()));
				final Double[] movementSpeeds = asArray(MOVEMENT_SPEED_STATIC, 0.5, 1.0, MOVEMENT_SPEED_PEDESTRIAN,
						2.0);
				for (final double movementSpeed : movementSpeeds) {

					if (isDefaultEvaluationConfiguration(nodeCount, worldSize, movementSpeed)) {
						continue;
					}

					final List<TopologyControlAlgorithmID> algorithms = asModifiableList( //
							// UnderlayTopologyControlAlgorithms.MAXPOWER_TC,
							UnderlayTopologyControlAlgorithms.D_KTC);
					for (final TopologyControlAlgorithmID algorithm : algorithms) {

						for (final TopologyControlOperationMode tcOperationMode : Arrays
								.asList(TopologyControlOperationMode.BATCH, TopologyControlOperationMode.INCREMENTAL)) {

							final Double[] kParameters = isKTCLikeAlgorithm(algorithm) ? asArray(1.41)
									: TopologyControlComponentConfig.NOT_SET_DOUBLE_ARRAY;
							for (final double tcParamterK : kParameters) {

								final TopologyControlFrequencyMode topologyControlFrequencyMode = TopologyControlFrequencyMode.PERIODIC;
								final Double[] topologyControlIntervalInMinutesList = { 1.0 };
								for (final double topologyControlIntervalInMinutes : topologyControlIntervalInMinutesList) {

									for (final int seed : seeds) {

										final int batteryCapacitySensor = 130;
										final int batteryCapacityMaster = isScenarioWithBaseStation(scenario) ? 1000000
												: 130;
										final double requiredTransmissionPowerExponent = 2.0;
										final DistanceEdgeWeightProvider weightProvider = DistanceEdgeWeightProvider
												.getInstance();
										final String endTime = "1h";
										final String outputPrefix = "cobolt";
										final String movementModelId = movementSpeed != 0.0 ? "GaussMarkov" : "None";

										final TopologyControlComponentConfig config = new TopologyControlComponentConfig();
										config.configurationNumber = configs.size() + 1;
										config.simulationConfigurationFile = simulationConfigurationFile
												.getAbsolutePath();
										config.seed = seed;
										config.topologyControlAlgorithmID = algorithm;
										config.topologyControlOperationMode = tcOperationMode;
										config.worldSize = worldSize;
										config.nodeCount = nodeCount;
										config.topologyControlAlgorithmParamters
												.put(UnderlayTopologyControlAlgorithms.KTC_PARAM_K, tcParamterK);
										config.topologyControlIntervalInMinutes = topologyControlIntervalInMinutes;
										config.topologyControlFrequencyMode = topologyControlFrequencyMode;
										config.topologyMonitoringLocalViewSize = 2;
										config.batteryCapacitySensor = batteryCapacitySensor;
										config.batteryCapacityMaster = batteryCapacityMaster;
										config.outputFolder = outputFolderForResults;
										config.tracesOutputFolder = outputFolderForResults;
										config.outputFilePrefix = outputPrefix;
										config.requiredTransmissionPowerExponent = requiredTransmissionPowerExponent;
										config.edgeWeightCalculatingFunction = weightProvider;
										config.movementModel = movementModelId;
										config.movementModelMaster = isScenarioWithBaseStation(scenario) ? "None"
												: movementModelId;
										config.movementMaxSpeedInMetersPerSecond = movementSpeed;
										config.movementInterval = topologyControlIntervalInMinutes >= 1.0
												? ((int) topologyControlIntervalInMinutes) + "m"
												: ((int) (topologyControlIntervalInMinutes * 60)) + "s";
										config.minimumDistanceThresholdInMeters = 0.0;
										config.end = endTime;
										config.scenario = scenario;
										config.datacollectionProbability = 1.0;
										config.reconfigurationEnabled = false;

										configs.add(config);
									}
								}
							}
						}
					}
				}
			}
		}

		return configs;
	}

	private boolean isDefaultEvaluationConfiguration(final int nodeCount, final int worldSize,
			final double movementSpeed) {
		return Arrays.asList(MOTE_COUNT_FEW, MOTE_COUNT_MANY).contains(nodeCount)
				&& Arrays.asList(WORLD_SIZE_SMALL, WORLD_SIZE_MEDIUM, WORLD_SIZE_LARGE).contains(worldSize)
				&& Arrays.asList(MOVEMENT_SPEED_STATIC, MOVEMENT_SPEED_PEDESTRIAN).contains(movementSpeed);
	}

}
