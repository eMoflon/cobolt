package de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.wsntraces;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.coala.adaptationlogic.api.PlannerType;
import org.coala.util.SplConquererLogFileReader;

import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.AbstractTopologyControlReconfigurationExecutor;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.NonfunctionalProperties;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.NonfunctionalProperty;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.TopologyControlComponentConfig;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.TopologyControlFrequencyMode;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.analysis.SplConquerorHelper;
import de.tudarmstadt.maki.simonstrator.tc.scenario.ScenarioType;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;
import de.tudarmstadt.maki.simonstrator.tc.weighting.DistanceEdgeWeightProvider;

public class TopologyControlReconfigurationPaperEvaluationExecutor
		extends AbstractTopologyControlReconfigurationExecutor {

	private static final String SUBEXECUTOR_EVAL_TRAININGCOST = "eval-trainingcost";

	private static final String SUBEXECUTOR_EVAL_TRAININGSETSIZE = "eval-trainingsetsize";

	private static final List<String> EVALUATION_TYPES_REQUIRING_PERFORMANCE_INFLUENCE_MODEL = Arrays
			.asList(SUBEXECUTOR_EVAL_TRAININGCOST, SUBEXECUTOR_EVAL_TRAININGSETSIZE);

	private static final String SUBEXECUTOR_TRAIN = "gentrainset";

	private static final List<String> EVALUATION_TYPES = Arrays.asList(SUBEXECUTOR_TRAIN,
			SUBEXECUTOR_EVAL_TRAININGSETSIZE, SUBEXECUTOR_EVAL_TRAININGCOST);

	private static final String CMD_OPTION_INFLUENCE_MODELS_FOLDER_LONG = "influenceModelsFolder";

	private static final String CMD_OPTION_INFLUENCE_MODELS_FOLDER = "i";

	private File performanceInfluenceModelsFolder;
	private int featureInteractionCoarseStepSize = 5;

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
		final int exitCode = new TopologyControlReconfigurationPaperEvaluationExecutor().run(args);
		System.exit(exitCode);
	}

	@Override
	protected Options createCommandLineOptionSpecification() {
		final Options options = super.createCommandLineOptionSpecification();
		options.addOption(CMD_OPTION_INFLUENCE_MODELS_FOLDER, CMD_OPTION_INFLUENCE_MODELS_FOLDER_LONG, true,
				"Folder containing SPLC logfile (aka. performance-influence models)");
		return options;
	}

	@Override
	protected int processCommandLine(CommandLine parsedCommandLineOptions, Options possibleOptions) {
		final int superExitCode = super.processCommandLine(parsedCommandLineOptions, possibleOptions);
		if (superExitCode != 0)
			return superExitCode;

		if (parsedCommandLineOptions.hasOption(CMD_OPTION_INFLUENCE_MODELS_FOLDER)) {
			performanceInfluenceModelsFolder = new File(
					parsedCommandLineOptions.getOptionValue(CMD_OPTION_INFLUENCE_MODELS_FOLDER));
			if (!performanceInfluenceModelsFolder.isDirectory() //
					|| performanceInfluenceModelsFolder
							.listFiles((FilenameFilter) (dir, name) -> name.endsWith(".log")).length == 0) {
				throw new IllegalArgumentException(MessageFormat.format(
						"Option ''{0}'' must point to a directory containing SPLConqueror logfiles",
						CMD_OPTION_INFLUENCE_MODELS_FOLDER));
			}
		}

		if (executor == null) {
			throw new IllegalArgumentException(MessageFormat.format(
					"Please select an evaluation type using option -{0}. Valid evaluation types are {1}.",
					CMD_OPTION_EXECUTOR, EVALUATION_TYPES));
		}
		if (EVALUATION_TYPES_REQUIRING_PERFORMANCE_INFLUENCE_MODEL.contains(this.executor)
				&& performanceInfluenceModelsFolder == null)
			throw new IllegalArgumentException(MessageFormat.format("Option -{0} is required for executor {1}",
					CMD_OPTION_INFLUENCE_MODELS_FOLDER, this.executor));

		return 0;
	}

	@Override
	protected String getSummaryOfConfiguration() {
		return String.format("%s\nSummary of effective command-line configuration (from %s):\n" //
				+ "  performanceInfluenceModel:                 %s\n", //
				super.getSummaryOfConfiguration(),
				TopologyControlReconfigurationPaperEvaluationExecutor.class.getSimpleName(), //
				this.performanceInfluenceModelsFolder //
		);
	}

	@Override
	protected List<String> getSupportedExecutors() {
		return Arrays.asList(SUBEXECUTOR_TRAIN, SUBEXECUTOR_EVAL_TRAININGSETSIZE, SUBEXECUTOR_EVAL_TRAININGCOST);
	}

	@Override
	protected List<TopologyControlComponentConfig> generateSimulationConfigurations() {
		switch (this.executor) {
		case SUBEXECUTOR_TRAIN:
			return generateConfigurationsForTrainingSetGeneration();
		case SUBEXECUTOR_EVAL_TRAININGSETSIZE:
			return runFoldBasedEvaluation();
		case SUBEXECUTOR_EVAL_TRAININGCOST:
			return runIterationsBasedEvaluation();
		default:
			throw new IllegalArgumentException(String.format("Cannot handle evaluation type: %s", executor));
		}
	}

	/**
	 * This set of configurations is responsible for creating the training dataset
	 * of the "TC reconfiguration" case study.
	 */
	private List<TopologyControlComponentConfig> generateConfigurationsForTrainingSetGeneration() {
		final List<TopologyControlComponentConfig> configs = new ArrayList<>();
		final List<ScenarioType> scenarioTypes = Arrays.asList(ScenarioType.DATACOLLECTION, ScenarioType.GOSSIP,
				ScenarioType.POINTTOPOINT);
		for (final ScenarioType scenario : scenarioTypes) {

			final Object[][] nodeCountsPlusWorldSizesPlusSeeds = { //
					// { 99, 100, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
					// }, //
					// { 99, 200, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
					// }, //
					{ 99, 300, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) }, //
					{ 99, 400, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) }, //
					{ 99, 500, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) }, //
					{ 99, 600, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) }, //
					{ 99, 700, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 13, 15, 16, 17, 18, 19, 20) }, //
					{ 99, 800, Arrays.asList(2, 4, 5, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20) }, //
					{ 99, 900, Arrays.asList(4, 11, 12, 13, 16, 19) }, //
			};
			for (final Object[] nodeCountsWithWorldSize : nodeCountsPlusWorldSizesPlusSeeds) {
				final int nodeCount = (int) nodeCountsWithWorldSize[0];
				final int worldSize = (int) nodeCountsWithWorldSize[1];
				@SuppressWarnings("unchecked")
				final List<Integer> completeSeedList = (List<Integer>) nodeCountsWithWorldSize[2];
				final List<Integer> seeds = completeSeedList.subList(0,
						Math.min(numberOfSeeds, completeSeedList.size()));

				final List<TopologyControlAlgorithmID> algorithms = asModifiableList( //
						UnderlayTopologyControlAlgorithms.MAXPOWER_TC, //
						UnderlayTopologyControlAlgorithms.D_KTC, //
						UnderlayTopologyControlAlgorithms.E_KTC, //
						UnderlayTopologyControlAlgorithms.LMST, //
						UnderlayTopologyControlAlgorithms.GMST);
				if (scenario == ScenarioType.DATACOLLECTION)
					algorithms.add(UnderlayTopologyControlAlgorithms.LSTAR_KTC);

				for (final TopologyControlAlgorithmID algorithm : algorithms) {

					final Double[] kParameters = isKTCLikeAlgorithm(algorithm) ? asArray(1.0, 1.41)
							: TopologyControlComponentConfig.NOT_SET_DOUBLE_ARRAY;
					for (final double tcParamterK : kParameters) {

						final Double[] aParameters = isLStarKTC(algorithm) ? asArray(1.1, 2.0)
								: TopologyControlComponentConfig.NOT_SET_DOUBLE_ARRAY;
						for (final double tcParameterA : aParameters) {

							final Double[] movementSpeeds = asArray(0.0, 0.5, 1.5);
							for (final double movementSpeed : movementSpeeds) {

								final TopologyControlFrequencyMode topologyControlFrequencyMode = movementSpeed == 0.0
										? TopologyControlFrequencyMode.SINGLESHOT
										: TopologyControlFrequencyMode.PERIODIC;

								final Double[] topologyControlIntervalInMinutesList = movementSpeed == 0.0
										? asArray(1.0)
										: asArray(0.5, 1.0, 2.0);
								for (final double topologyControlIntervalInMinutes : topologyControlIntervalInMinutesList) {

									for (final int seed : seeds) {

										final int batteryCapacitySensor = 130;
										final int batteryCapacityMaster = isScenarioWithBaseStation(scenario) ? 1000000
												: 130;
										final double requiredTransmissionPowerExponent = 2.0;
										final DistanceEdgeWeightProvider weightProvider = DistanceEdgeWeightProvider
												.getInstance();
										final String endTime = "1h";
										final int coneCount = 8;
										final String outputPrefix = "wsntraces";
										final String movementModelId = movementSpeed != 0.0 ? "GaussMarkov" : "None";

										final TopologyControlComponentConfig config = new TopologyControlComponentConfig();
										config.configurationNumber = configs.size() + 1;
										config.simulationConfigurationFile = simulationConfigurationFile
												.getAbsolutePath();
										config.seed = seed;
										config.topologyControlAlgorithmID = algorithm;
										config.worldSize = worldSize;
										config.nodeCount = nodeCount;
										config.topologyControlAlgorithmParamters
												.put(UnderlayTopologyControlAlgorithms.KTC_PARAM_K, tcParamterK);
										config.topologyControlAlgorithmParamters
												.put(UnderlayTopologyControlAlgorithms.LSTAR_KTC_PARAM_A, tcParameterA);
										config.topologyControlAlgorithmParamters
												.put(UnderlayTopologyControlAlgorithms.YAO_PARAM_CONE_COUNT, coneCount);
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

	private List<TopologyControlComponentConfig> runIterationsBasedEvaluation() {
		final List<File> allSplcFiles = Arrays.asList(
				performanceInfluenceModelsFolder.listFiles((FilenameFilter) (folder, name) -> name.endsWith(".log")));
		// Only use baseline + full dataset
		final List<File> filteredSplclogfiles = allSplcFiles.stream()
				.filter(file -> file.getName().matches(".*fraction[0,1]00.*")) //
				// .filter(file -> file.getName().matches(".*mEndToEndLatency.*")) //
				.collect(Collectors.toList());

		final List<Integer> splcLineCounts = filteredSplclogfiles.stream()
				.map(TopologyControlReconfigurationPaperEvaluationExecutor::getSplcCsvLineCount)
				.collect(Collectors.toList());
		final List<List<Integer>> splcLineCountsCoarse = splcLineCounts.stream()
				.map(count -> determineCoarseLineCounts(count)).collect(Collectors.toList());
		final List<List<Integer>> splcLineCountsFine = splcLineCounts.stream()
				.map(count -> determineFineLineCounts(count)).collect(Collectors.toList());

		final List<TopologyControlComponentConfig> configs = new ArrayList<>();
		final List<ScenarioType> scenarioTypes = Arrays.asList(ScenarioType.DATACOLLECTION, ScenarioType.GOSSIP,
				ScenarioType.POINTTOPOINT);

		for (final FeatureInteractionStepSizeMode featureInteractionStepSizeMode : Arrays
				.asList(FeatureInteractionStepSizeMode.COARSE)) {

			for (final ScenarioType scenario : scenarioTypes) {

				final List<Double> datacollectionProbabilities;
				if (scenario == ScenarioType.DATACOLLECTION) {
					datacollectionProbabilities = Arrays.asList(1.0, 0.5);
				} else {
					datacollectionProbabilities = Arrays.asList(0.0);
				}

				for (final double datacollectionProbability : datacollectionProbabilities) {

					final Object[][] nodeCountsPlusWorldSizesPlusSeeds = { //
							// { 99, 100, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8,
							// 9,
							// 10)
							// }, //
							// { 99, 200, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8,
							// 9,
							// 10)
							// }, //
							{ 99, 300, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) }, //
							// { 99, 400, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8,
							// 9,
							// 10) }, //
							// { 99, 500, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8,
							// 9,
							// 10)
							// }, //
							{ 99, 600, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) }, //
							// { 99, 700, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8,
							// 10,
							// 11,
							// 13, 15, 16, 17, 18, 19, 20) }, //
							// { 99, 800, Arrays.asList(2, 4, 5, 10, 11, 12, 13,
							// 14, 15, 16, 17, 18, 19, 20)
							// }, //
							{ 99, 900, Arrays.asList(4, 11, 12, 13, 16, 19) }, //
					};
					for (final Object[] nodeCountPlusWorldSizePlusSeed : nodeCountsPlusWorldSizesPlusSeeds) {
						final int nodeCount = (int) nodeCountPlusWorldSizePlusSeed[0];
						final int worldSize = (int) nodeCountPlusWorldSizePlusSeed[1];
						@SuppressWarnings("unchecked")
						final List<Integer> completeSeedList = (List<Integer>) nodeCountPlusWorldSizePlusSeed[2];
						final List<Integer> seeds = completeSeedList.subList(0,
								Math.min(numberOfSeeds, completeSeedList.size()));

						final Double[] movementSpeeds = asArray(0.0, 0.5, 1.5);
						for (final double movementSpeed : movementSpeeds) {
							for (int splcFileIndex = 0; splcFileIndex < filteredSplclogfiles.size(); ++splcFileIndex) {

								final File splcLogfile = filteredSplclogfiles.get(splcFileIndex);

								final List<Integer> featureInteractionLineNumbers;
								final List<PlannerType> planners;

								// For the baseline, we want to run both MiniSAT
								// and
								// ILP but with only 1 (artificial)
								// feature-interaction line number
								if (isBaselineSplcLogfile(splcLogfile)) {
									featureInteractionLineNumbers = Arrays
											.asList(TopologyControlComponentConfig.SPLC_LINE_NUMBER_USE_MAXIMUM);
									planners = Arrays.asList(PlannerType.MINISAT, PlannerType.ILP);
								}
								/*
								 * For non-baseline configurations, we only want to run ILP but with different
								 * feature-interaction-line numbers
								 */
								else {
									switch (featureInteractionStepSizeMode) {
									case COARSE:
										featureInteractionLineNumbers = splcLineCountsCoarse.get(splcFileIndex);
										break;
									case FINE:
										featureInteractionLineNumbers = splcLineCountsFine.get(splcFileIndex);
										break;
									default:
										throw new IllegalArgumentException(featureInteractionStepSizeMode.toString());
									}
									planners = Arrays.asList(PlannerType.ILP);
								}

								for (final PlannerType planner : planners) {

									for (final int featureInteractionLineNumber : featureInteractionLineNumbers) {

										for (final int seed : seeds) {

											final int batteryCapacitySensor = 130;
											final int batteryCapacityMaster = isScenarioWithBaseStation(scenario)
													? 1000000
													: 130;
											final double requiredTransmissionPowerExponent = 2.0;
											final DistanceEdgeWeightProvider weightProvider = DistanceEdgeWeightProvider
													.getInstance();
											final String endTime = "15m";
											final String outputPrefix = "wsntraces";
											final String movementModelId = movementSpeed != 0.0 //
													? "GaussMarkov" //
													: "None";
											final int topologyControlIntervalInMinutes = 1;

											final TopologyControlComponentConfig config = new TopologyControlComponentConfig();

											config.configurationNumber = configs.size() + 1;
											config.simulationConfigurationFile = simulationConfigurationFile
													.getAbsolutePath();
											config.seed = seed;
											config.worldSize = worldSize;
											config.end = endTime;
											config.nodeCount = nodeCount;

											config.outputFolder = outputFolderForResults;
											config.tracesOutputFolder = outputFolderForResults;
											config.outputFilePrefix = outputPrefix;

											config.batteryCapacitySensor = batteryCapacitySensor;
											config.batteryCapacityMaster = batteryCapacityMaster;
											config.requiredTransmissionPowerExponent = requiredTransmissionPowerExponent;

											config.movementModel = movementModelId;
											config.movementModelMaster = isScenarioWithBaseStation(scenario) ? "None"
													: movementModelId;
											config.movementMaxSpeedInMetersPerSecond = movementSpeed;
											config.movementInterval = createDurationDescriptor(
													topologyControlIntervalInMinutes);

											config.topologyControlAlgorithmID = UnderlayTopologyControlAlgorithms.MAXPOWER_TC;
											config.topologyControlIntervalInMinutes = topologyControlIntervalInMinutes;
											config.topologyControlFrequencyMode = TopologyControlFrequencyMode.PERIODIC;
											config.topologyMonitoringLocalViewSize = 2;
											config.edgeWeightCalculatingFunction = weightProvider;
											config.minimumDistanceThresholdInMeters = 0.0;

											config.scenario = scenario;
											config.datacollectionProbability = datacollectionProbability;

											config.setReconfigurationEnabled(true);
											config.goalNonfunctionalProperty = extractNfp(splcLogfile);
											config.adaptationLogicSeed = seed;
											config.adaptationLogicPlanner = planner.toString();
											config.splcOutputFile = splcLogfile.getAbsolutePath();
											config.splcFeatureInteractionLineNumber = featureInteractionLineNumber;

											configs.add(config);

										}
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

	private List<TopologyControlComponentConfig> runFoldBasedEvaluation() {
		final File adaptationProjectRoot = new File(
				"C:\\Users\\rkluge\\Documents\\data\\workspaces\\maki_a01\\git\\splc2018-code\\org.coala.adaptationlogic");
		final File splcFolder = new File(adaptationProjectRoot, "src\\main\\resources\\spl-conqueror\\D6-splc3\\");
		final List<File> allSplcFiles = Arrays
				.asList(splcFolder.listFiles((FilenameFilter) (folder, name) -> name.endsWith(".log")));
		final List<File> filteredSplcFiles = allSplcFiles.stream()
				.filter(file -> file.getName().matches(".*seed000[0-9].*") || file.getName().matches(".*fraction000.*"))
				.collect(Collectors.toList());
		final List<TopologyControlComponentConfig> configs = new ArrayList<>();
		final List<ScenarioType> scenarioTypes = Arrays.asList(ScenarioType.DATACOLLECTION, ScenarioType.GOSSIP,
				ScenarioType.POINTTOPOINT);

		for (final ScenarioType scenario : scenarioTypes) {

			final Object[][] nodeCountsPlusWorldSizesPlusSeeds = { //
					// { 99, 100, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9,
					// 10)
					// }, //
					// { 99, 200, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9,
					// 10)
					// }, //
					// { 99, 300, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9,
					// 10)
					// }, //
					// { 99, 400, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9,
					// 10) }, //
					// { 99, 500, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9,
					// 10)
					// }, //
					// { 99, 600, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9,
					// 10)
					// }, //
					// { 99, 700, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 10,
					// 11,
					// 13, 15, 16, 17, 18, 19, 20) }, //
					{ 99, 800, Arrays.asList(2, 4, 5, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20) }, //
					// { 99, 900, Arrays.asList(4, 11, 12, 13, 16, 19) }, //
			};
			for (final Object[] nodeCountPlusWorldSizePlusSeed : nodeCountsPlusWorldSizesPlusSeeds) {
				final int nodeCount = (int) nodeCountPlusWorldSizePlusSeed[0];
				final int worldSize = (int) nodeCountPlusWorldSizePlusSeed[1];
				@SuppressWarnings("unchecked")
				final List<Integer> completeSeedList = (List<Integer>) nodeCountPlusWorldSizePlusSeed[2];
				final List<Integer> seeds = completeSeedList.subList(0,
						Math.min(numberOfSeeds, completeSeedList.size()));

				final Double[] movementSpeeds = asArray(0.0, 1.5);
				for (final double movementSpeed : movementSpeeds) {

					final TopologyControlFrequencyMode topologyControlFrequencyMode = TopologyControlFrequencyMode.PERIODIC;
					for (final File splcOutputFile : filteredSplcFiles) {

						for (final int seed : seeds) {

							final int batteryCapacitySensor = 130;
							final int batteryCapacityMaster = isScenarioWithBaseStation(scenario) ? 1000000 : 130;
							final double requiredTransmissionPowerExponent = 2.0;
							final DistanceEdgeWeightProvider weightProvider = DistanceEdgeWeightProvider.getInstance();
							final String endTime = "30m";
							final String outputPrefix = "wsntraces";
							final String movementModelId = movementSpeed != 0.0 ? "GaussMarkov" : "None";
							final int topologyControlIntervalInMinutes = 1;

							final TopologyControlComponentConfig config = new TopologyControlComponentConfig();
							config.configurationNumber = configs.size() + 1;
							config.simulationConfigurationFile = simulationConfigurationFile.getAbsolutePath();
							config.seed = seed;
							config.topologyControlAlgorithmID = UnderlayTopologyControlAlgorithms.MAXPOWER_TC;
							config.worldSize = worldSize;
							config.nodeCount = nodeCount;
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
							config.movementModelMaster = isScenarioWithBaseStation(scenario) ? "None" : movementModelId;
							config.movementMaxSpeedInMetersPerSecond = movementSpeed;
							config.movementInterval = createDurationDescriptor(topologyControlIntervalInMinutes);
							config.minimumDistanceThresholdInMeters = 0.0;
							config.end = endTime;
							config.scenario = scenario;
							config.datacollectionProbability = 1.0;
							config.goalNonfunctionalProperty = extractNfp(splcOutputFile);
							config.adaptationLogicSeed = 13;
							config.splcOutputFile = splcOutputFile.getAbsolutePath();

							configs.add(config);

						}
					}
				}
			}
		}
		return configs;
	}

	private boolean isBaselineSplcLogfile(final File splcLogfile) {
		return splcLogfile.getName().matches(".*fraction000.*");
	}

	private List<Integer> determineCoarseLineCounts(final Integer totalLineCount) {
		final List<Integer> coarseGrainedLineCounts = IntStream.rangeClosed(1, totalLineCount)
				.filter(i -> isInCoarseGrainedLineCountList(totalLineCount, i)).boxed().collect(Collectors.toList());
		return coarseGrainedLineCounts;
	}

	private List<Integer> determineFineLineCounts(final Integer totalLineCount) {
		final List<Integer> fineGrainedLineCounts = IntStream.rangeClosed(1, totalLineCount)
				.filter(i -> !isInCoarseGrainedLineCountList(totalLineCount, i)).boxed().collect(Collectors.toList());
		return fineGrainedLineCounts;
	}

	/**
	 * Returns true if the given line count should be in the set of coarse-grained
	 * line counts. Returns false if the given line count should be in the set of
	 * fine-grained line counts.
	 *
	 * @param totalLineCount
	 *            the total line count of the corresponding SPLConqueror log file
	 * @param i
	 *            the line count to check
	 * @return the decision
	 */
	private boolean isInCoarseGrainedLineCountList(final Integer totalLineCount, int i) {
		return i <= 5 || i == totalLineCount || i % this.featureInteractionCoarseStepSize == 0;
	}

	/**
	 * Convenience method to safely call
	 * {@link SplConquererLogFileReader#getCsvLineCount(File)}, assuming that the
	 * given file is accessible
	 *
	 * @param file
	 *            the logfile
	 * @return the line count
	 */
	private static int getSplcCsvLineCount(final File file) {
		try {
			return SplConquererLogFileReader.getCsvLineCount(file);
		} catch (IOException e) {
			// Never happens because we check the files beforehand
			throw new RuntimeException(e);
		}
	}

	private NonfunctionalProperty extractNfp(File splcOutputFile) {
		final String filename = splcOutputFile.getName();
		final Optional<String> nfp = SplConquerorHelper.extractNonfunctionalProperty(filename);
		if (nfp.isPresent()) {
			final String name = nfp.get();
			final Optional<NonfunctionalProperty> byName = NonfunctionalProperties.getByName(name);
			if (byName.isPresent()) {
				return byName.get();
			} else {
				throw new IllegalArgumentException(String.format("Cannot find NFP from name: %s.", name));
			}
		} else {
			throw new IllegalArgumentException(String.format("Cannot extract NFP from filename: %s.", filename));
		}
	}

	private enum FeatureInteractionStepSizeMode {
		COARSE, FINE;
	}
}
