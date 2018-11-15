/*
 * Copyright (c) 2005-2010 KOM - Multimedia Communications Lab
 *
 * This file is part of PeerfactSim.KOM.
 * 
 * PeerfactSim.KOM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * PeerfactSim.KOM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PeerfactSim.KOM.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.wsntraces;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerRepository;

import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.TopologyControlSimulationTask;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.evaluation.TaskExecutorUtils;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.util.run.SimulationTask;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.util.run.TaskExecutor;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.TopologyControlComponentConfig;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.TopologyControlFrequencyMode;
import de.tudarmstadt.maki.simonstrator.tc.scenario.ScenarioType;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;
import de.tudarmstadt.maki.simonstrator.tc.utils.DateHelper;
import de.tudarmstadt.maki.simonstrator.tc.weighting.DistanceEdgeWeightProvider;

/**
 * This class configures the different combinations of parameters for running
 * the evaluation of the incremental TC algorithms.
 * 
 * @author Roland Kluge - Initial implementation
 */
public class TopologySelectionExecutor {

	private static Logger logger;

	/**
	 * Runs the evaluation setup.
	 * 
	 * For reproducibility, every simulation is started in a separate process.
	 * 
	 * To stop the whole evaluation, open this process's console and type 'quit'
	 * + ENTER.
	 * 
	 * @param args
	 *            ignored
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HHmmss.SSS");
		System.setProperty("logfile.name", "rkluge/simrunnerlog_" + dateFormat.format(new Date()) + ".log");
		Locale.setDefault(Locale.US);
		LoggerRepository loggerRepository = LogManager.getLoggerRepository();
		loggerRepository.setThreshold(org.apache.log4j.Level.INFO);
		logger = Logger.getLogger(TopologySelectionExecutor.class);
		runEvaluation();
	}

	private static void runEvaluation() {
		final String formattedDate = DateHelper.getFormattedDate();
		final File outputFolder = new File("./output/topoeval/batchrun_" + formattedDate);
		final File tracesOutputFolder = new File("./output/topoeval/batchrun_" + formattedDate);
		final String simulationConfigurationFile = "config/wsntraces/wsntraces.xml";
		try {
			FileUtils.copyFileToDirectory(new File(simulationConfigurationFile), outputFolder);
		} catch (IOException e) {
			Monitor.log(TopologySelectionExecutor.class, Level.ERROR,
					"Failed to copy %s to %s (see the following stacktrace)", simulationConfigurationFile,
					outputFolder);
			e.printStackTrace();
		}

		int configurationNumber = 0;
		final boolean shallPersistConfiguration = true;

		final List<TopologyControlComponentConfig> configs = new ArrayList<>();

		final List<ScenarioType> scenarioTypes = Arrays.asList(ScenarioType.SILENCE);
		for (final ScenarioType scenario : scenarioTypes) {

			final Object[][] nodeCountsPlusWorldSizesPlusSeeds = { //
					{ 99, 200, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) }, //
					{ 99, 100, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) }, //
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
				final List<Integer> seeds = completeSeedList.subList(0, completeSeedList.size());

				final List<TopologyControlAlgorithmID> algorithms = asModifiableList( //
						UnderlayTopologyControlAlgorithms.MAXPOWER_TC);
				for (final TopologyControlAlgorithmID algorithm : algorithms) {

					final Double[] kParameters = TopologyControlComponentConfig.NOT_SET_DOUBLE_ARRAY;
					for (final double tcParamterK : kParameters) {

						final Double[] aParameters = TopologyControlComponentConfig.NOT_SET_DOUBLE_ARRAY;
						for (final double tcParameterA : aParameters) {

							final TopologyControlFrequencyMode topologyControlFrequencyMode = TopologyControlFrequencyMode.PERIODIC;

							final Double[] topologyControlIntervalInMinutesList = asArray(1.0);
							for (final double topologyControlIntervalInMinutes : topologyControlIntervalInMinutesList) {

								final List<Double> minimalDistancesInMeters = Arrays.asList(0.0);
								for (final double minimalDistanceInMeters : minimalDistancesInMeters) {

									final Double[] movementSpeeds = asArray(0.0);
									for (final double movementSpeed : movementSpeeds) {

										for (final int seed : seeds) {

											final int batteryCapacitySensor = 130;
											final int batteryCapacityMaster = 130;
											final double requiredTransmissionPowerExponent = 2.0;
											final DistanceEdgeWeightProvider weightProvider = DistanceEdgeWeightProvider
													.getInstance();
											final String endTime = "61s";
											final int coneCount = 8;
											final String outputPrefix = "wsntraces";
											final String movementModelId = movementSpeed != 0.0 ? "GaussMarkov"
													: "None";

											++configurationNumber;
											final TopologyControlComponentConfig config = new TopologyControlComponentConfig();
											config.configurationNumber = configurationNumber;
											config.simulationConfigurationFile = simulationConfigurationFile;
											config.seed = seed;
											config.topologyControlAlgorithmID = algorithm;
											config.topologyControlIntervalInMinutes = topologyControlIntervalInMinutes;
											config.worldSize = worldSize;
											config.nodeCount = nodeCount;
											config.topologyControlAlgorithmParamters
													.put(UnderlayTopologyControlAlgorithms.KTC_PARAM_K, tcParamterK);
											config.topologyControlAlgorithmParamters.put(
													UnderlayTopologyControlAlgorithms.LSTAR_KTC_PARAM_A, tcParameterA);
											config.topologyControlAlgorithmParamters.put(
													UnderlayTopologyControlAlgorithms.YAO_PARAM_CONE_COUNT, coneCount);
											config.topologyControlIntervalInMinutes = topologyControlIntervalInMinutes;
											config.topologyControlFrequencyMode = topologyControlFrequencyMode;
											config.topologyMonitoringLocalViewSize = 2;
											config.batteryCapacitySensor = batteryCapacitySensor;
											config.batteryCapacityMaster = batteryCapacityMaster;
											config.outputFolder = outputFolder;
											config.tracesOutputFolder = tracesOutputFolder;
											config.outputFilePrefix = outputPrefix;
											config.requiredTransmissionPowerExponent = requiredTransmissionPowerExponent;
											config.edgeWeightCalculatingFunction = weightProvider;
											config.movementModel = movementModelId;
											config.movementModelMaster = movementModelId;
											config.movementMaxSpeedInMetersPerSecond = movementSpeed;
											config.movementInterval = topologyControlIntervalInMinutes + "m";
											config.minimumDistanceThresholdInMeters = minimalDistanceInMeters;
											config.end = endTime;
											config.scenario = scenario;

											if (shallPersistConfiguration) {
												final File targetFileForConfiguration = new File(outputFolder,
														String.format("parameters%s%04d_parameters.txt", File.separator,
																configurationNumber));
												try {
													FileUtils.writeStringToFile(targetFileForConfiguration,
															config.toString());
												} catch (IOException e) {
													Monitor.log(TopologySelectionExecutor.class, Level.ERROR,
															"Failed to write configuration number %d to %s (see the following stacktrace)",
															configurationNumber, targetFileForConfiguration);
													e.printStackTrace();
												}
											}

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

		final List<SimulationTask> simulations = configs.stream()
				.map(config -> new TopologyControlSimulationTask(config)).collect(Collectors.toList());

		final int parallelTasks = 4;
		final TaskExecutor taskExecutor = new TaskExecutor(simulations, parallelTasks);
		taskExecutor.setJvmOptionXmx("1500m");
		taskExecutor.start();

		logger.info("Enter 'quit' to abort simulation.");
		TaskExecutorUtils.waitForQuitOrTerminationOfSubprocesses(taskExecutor);
		logger.info("Simulation aborted - closing all threads");
		taskExecutor.stop();
	}

	@SafeVarargs
	private static <T> T[] asArray(final T... values) {
		return values;
	}

	private static List<TopologyControlAlgorithmID> asModifiableList(TopologyControlAlgorithmID... algorithmIDs) {
		return new ArrayList<>(Arrays.asList(algorithmIDs));
	}
}
