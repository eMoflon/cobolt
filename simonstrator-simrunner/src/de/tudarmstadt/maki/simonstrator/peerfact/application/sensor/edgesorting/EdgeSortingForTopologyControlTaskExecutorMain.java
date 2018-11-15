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

package de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.edgesorting;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.TopologyControlSimulationTask;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.evaluation.TaskExecutorUtils;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.util.run.SimulationTask;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.util.run.TaskExecutor;
import de.tudarmstadt.maki.simonstrator.tc.component.EvaluationStatistics;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.TopologyControlComponentConfig;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;
import de.tudarmstadt.maki.simonstrator.tc.weighting.DistanceEdgeWeightProvider;

/**
 * This class configures the different combinations of parameters for running
 * the evaluation of the incremental TC algorithms.
 * 
 * @author Roland Kluge - Initial implementation
 */
public class EdgeSortingForTopologyControlTaskExecutorMain {

	private static final String CSV_SEP = ";";
	private static final String NULL_EDGE_SORTER = "org.cobolt.algorithms.facade.preprocessing.NullNodePreprocessor";
	private static final String DEFAULT_EDGE_SORTER = "org.cobolt.algorithms.facade.preprocessing.DefaultEdgeOrderNodePreprocessor";
	private File outputFolder;

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
		new EdgeSortingForTopologyControlTaskExecutorMain().run();
	}

	private void run() {

		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HHmmss");
		final String formattedDate = dateFormat.format(new Date());
		outputFolder = new File("./output/edgesorting/main/batchrun_" + formattedDate);

		new File(outputFolder.getAbsolutePath() + "\\log\\").mkdirs();
		final String simrunngerLogfilePath = "\\..\\" + outputFolder + "\\log\\simrunnerlog_"
				+ formattedDate
				+ ".log";
		System.setProperty("logfile.name", simrunngerLogfilePath);
		Locale.setDefault(Locale.US);
		runEvaluation();
	}

	private void runEvaluation() {
		final LinkedList<SimulationTask> simulations = new LinkedList<SimulationTask>();

		final int parallelTasks = 1;

		final int[][] nodeCountsWithWorldSizes = { { 50, 200 } };// { 999, 2000
																	// } };
		final double[] minimalDistancesInMeters = { 0 };
		final TopologyControlAlgorithmID[] algorithms = { UnderlayTopologyControlAlgorithms.D_KTC };
		final int[] seeds = { 1, 2, 3, 4, 5 };
		final int batteryCapacity = 130;
		final double requiredTransmissionPowerExponent = 2.0;
		final double topologyControlInterval = 10.0;
		final DistanceEdgeWeightProvider weightProvider = DistanceEdgeWeightProvider.getInstance();
		final String endTime = "20h";
		final String simulationConfigurationFile = "config/edgesorting/edgesorting_config.xml";
		final String[][] nodePreprocessorConfigs = { //
				{ DEFAULT_EDGE_SORTER, "false" }, //
				{ DEFAULT_EDGE_SORTER, "true" }, //
				{ NULL_EDGE_SORTER, "true" }, };

		try {
			FileUtils.copyFileToDirectory(new File(simulationConfigurationFile), outputFolder);
		} catch (IOException e) {
			Monitor.log(EdgeSortingForTopologyControlTaskExecutorMain.class, Level.ERROR,
					"Failed to copy %s to %s (see the following stacktrace)", simulationConfigurationFile,
					outputFolder);
			e.printStackTrace();
		}

		final File targetFileForJointConfigurations = new File(outputFolder,
				String.format("parameters%sparameters.csv", File.separator));

		try {
			FileUtils.writeLines(targetFileForJointConfigurations,
					Arrays.asList(StringUtils.join(TopologyControlComponentConfig.getCSVHeader(), CSV_SEP)));
		} catch (final IOException e) {
			Monitor.log(EdgeSortingForTopologyControlTaskExecutorMain.class, Level.ERROR,
					"Failed to write configuration header to %s (see the following stacktrace)",
					targetFileForJointConfigurations);
			e.printStackTrace();
		}

		int configurationNumber = 0;

		for (final String[] nodePreprocessorConfig : nodePreprocessorConfigs) {
			final String nodePreprocessorClass = nodePreprocessorConfig[0];
			final boolean nodePreprocessorShallReverseOrder = Boolean.parseBoolean(nodePreprocessorConfig[1]);

			for (final int[] nodeCountsWithWorldSize : nodeCountsWithWorldSizes) {
				final int nodeCount = nodeCountsWithWorldSize[0];
				final int worldSize = nodeCountsWithWorldSize[1];

				final double[] kParameters = { 1.41 };

				for (final TopologyControlAlgorithmID algorithm : algorithms) {
					for (final double k : kParameters) {
						for (final double minimalDistanceInMeters : minimalDistancesInMeters) {
							for (final int seed : seeds) {

								++configurationNumber;

								final TopologyControlComponentConfig config = new TopologyControlComponentConfig();
								config.simulationConfigurationFile = simulationConfigurationFile;
								config.seed = seed;
								config.topologyControlAlgorithmID = algorithm;
								config.topologyControlIntervalInMinutes = topologyControlInterval;
								config.worldSize = worldSize;
								config.nodeCount = nodeCount;
								config.topologyControlAlgorithmParamters
										.put(UnderlayTopologyControlAlgorithms.KTC_PARAM_K, k);
								config.batteryCapacitySensor = batteryCapacity;
								config.batteryCapacityMaster = batteryCapacity;
								config.outputFolder = outputFolder;
								config.outputFilePrefix = "sosym";
								config.requiredTransmissionPowerExponent = requiredTransmissionPowerExponent;
								config.edgeWeightCalculatingFunction = weightProvider;
								config.movementModel = "GaussMarkov";
								config.movementModelMaster = config.movementModel;
								config.movementMaxSpeedInMetersPerSecond = 0.005;
								config.movementInterval = (int) topologyControlInterval + "m";
								config.minimumDistanceThresholdInMeters = minimalDistanceInMeters;
								config.end = endTime;
								config.nodePreprocessorClass = nodePreprocessorClass;
								config.nodePreprocessorShallReverseOrder = nodePreprocessorShallReverseOrder;
								config.name = calculateName(config);
								config.configurationNumber = configurationNumber;

								final File targetFileForConfiguration = new File(outputFolder, String.format(
										"parameters%s%04d_parameters.txt", File.separator, configurationNumber));
								try {
									FileUtils.writeStringToFile(targetFileForConfiguration, config.toString());
								} catch (final IOException e) {
									Monitor.log(EdgeSortingForTopologyControlTaskExecutorMain.class, Level.ERROR,
											"Failed to write configuration number %d to %s (see the following stacktrace)",
											configurationNumber, targetFileForConfiguration);
									e.printStackTrace();
								}

								try {
									FileUtils.writeLines(targetFileForJointConfigurations,
											Arrays.asList(StringUtils.join(config.getCSVLine(), CSV_SEP)), true);
								} catch (final IOException e) {
									Monitor.log(EdgeSortingForTopologyControlTaskExecutorMain.class, Level.ERROR,
											"Failed to write configuration %d to %s (see the following stacktrace)",
											configurationNumber, targetFileForJointConfigurations);
									e.printStackTrace();
								}

								simulations.add(new TopologyControlSimulationTask(config));
							}
						}
					}
				}
			}
		}
		final TaskExecutor taskExecutor = new TaskExecutor(simulations, parallelTasks);
		taskExecutor.addTerminationHook(() -> {
			File dataFolder = new File(outputFolder, "data");
			List<File> csvFiles = Arrays.asList(dataFolder.listFiles((File dir, String name) -> {
				return name.endsWith(".csv");
			}));
			List<String> joinedLines = new ArrayList<>();
			joinedLines.add(EvaluationStatistics.createHeaderOfEvaluationDataFile(CSV_SEP));
			for (final File csvFile : csvFiles) {
				try {
					List<String> readLines = FileUtils.readLines(csvFile);
					joinedLines.addAll(readLines.subList(1, readLines.size()));
				} catch (IOException e) {
					Monitor.log(getClass(), Level.ERROR, "Failed to read file %s. Reason: %s", csvFile, e.getMessage());
				}
			}

			final File outputFile = new File(outputFolder, "data/data.csv");
			try {
				FileUtils.writeLines(outputFile, joinedLines);
			} catch (IOException e) {
				Monitor.log(getClass(), Level.ERROR, "Failed to write file %s. Reason: %s", outputFile, e.getMessage());
			}
		});
		taskExecutor.setJvmOptionXmx("1500m");
		taskExecutor.start();

		Monitor.log(EdgeSortingForTopologyControlTaskExecutorMain.class, Level.INFO,
				"Enter 'quit' to abort simulation.");
		TaskExecutorUtils.waitForQuitOrTerminationOfSubprocesses(taskExecutor);
		Monitor.log(EdgeSortingForTopologyControlTaskExecutorMain.class, Level.INFO,
				"Simulation aborted - closing all threads");
		taskExecutor.stop();
	}

	/**
	 * Calculates the descriptive name for the given configuration
	 * 
	 * @param config
	 *            the configuration
	 * @return the descriptive name
	 */
	private static String calculateName(final TopologyControlComponentConfig config) {
		StringBuilder result = new StringBuilder(config.topologyControlAlgorithmID.getName());
		switch (config.nodePreprocessorClass) {
		case NULL_EDGE_SORTER:
			result.append("_NoSorting");
			break;
		case DEFAULT_EDGE_SORTER:
			result.append(config.nodePreprocessorShallReverseOrder ? "Descending" : "Ascending");
		}
		return result.toString();
	}

}
