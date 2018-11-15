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

package de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.sosym2016;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

import org.apache.commons.io.FileUtils;

import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.TopologyControlSimulationTask;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.evaluation.TaskExecutorUtils;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.util.run.SimulationTask;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.util.run.TaskExecutor;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.TopologyControlComponentConfig;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;
import de.tudarmstadt.maki.simonstrator.tc.utils.DateHelper;
import de.tudarmstadt.maki.simonstrator.tc.weighting.DistanceEdgeWeightProvider;

/**
 * This class configures the different combinations of parameters for running
 * the evaluation of the incremental TC algorithms.
 * 
 * @author Roland Kluge - Initial implementation
 */
public class SoSyMEvaluationExecutor {

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
		runEvaluation();
	}

	/**
	 * Evaluation for first submission in May/June 2016
	 * 
	 * min-weights in {0, 20, 40, 60, 80} ---- (N,a) in {(100,750), (100,500)}
	 * ---- algo in {MAXPOWER_TC, D_KTC, E_KTC} ---- kTCParameterK in {1.0,
	 * 1.41} ---- seed in {1,2,3,4,5} ---- movement = Gauss-Markov (v =
	 * 0.005m/s), timeBetweenMovements=TC interval ---- TC interval = 10m ----
	 * simulation duration = 25h ----
	 */
	private static void runEvaluation() {
		final String formattedDate = DateHelper.getFormattedDate();
		final LinkedList<SimulationTask> simulations = new LinkedList<SimulationTask>();

		final int parallelTasks = 4;

		// First run - small topologies
		// final int[][] nodeCountsWithWorldSizes = { { 99, 750 }, { 99, 500 }
		// };
		// Second run - checking influence of ktc
		final int NODE_COUNT_1 = 99;
		final int[][] nodeCountsWithWorldSizes = { { NODE_COUNT_1, 750 }, { NODE_COUNT_1, 500 }, { 999, 2000 },
				{ 999, 1500 } };
		final double[] minimalDistancesInMeters = { 0, 20, 40, 60, 80 };
		final TopologyControlAlgorithmID[] algorithms = { UnderlayTopologyControlAlgorithms.MAXPOWER_TC,
				UnderlayTopologyControlAlgorithms.D_KTC, UnderlayTopologyControlAlgorithms.E_KTC };
		final int[] seeds = { 1, 2, 3, 4, 5 };
		final int batteryCapacity = 130;
		final double requiredTransmissionPowerExponent = 2.0;
		final double topologyControlInterval = 10.0;
		final DistanceEdgeWeightProvider weightProvider = DistanceEdgeWeightProvider.getInstance();
		final String endTime = "25h";
		final File outputFolder = new File("./output/sosym/main/batchrun_" + formattedDate);
		final String simulationConfigurationFile = "config/sosym/sosym_complete_evaluation.xml";

		try {
			FileUtils.copyFileToDirectory(new File(simulationConfigurationFile), outputFolder);
		} catch (IOException e) {
			Monitor.log(SoSyMEvaluationExecutor.class, Level.ERROR,
					"Failed to copy %s to %s (see the following stacktrace)", simulationConfigurationFile,
					outputFolder);
			e.printStackTrace();
		}

		int configurationNumber = 0;
		for (final int[] nodeCountsWithWorldSize : nodeCountsWithWorldSizes) {
			final int nodeCount = nodeCountsWithWorldSize[0];
			final int worldSize = nodeCountsWithWorldSize[1];

			final double[] kParameters;
			if (nodeCount == NODE_COUNT_1)
				kParameters = new double[] { 1.2, 1.3, 2.0 };
			else
				kParameters = new double[] { 1.0, 1.41, 1.2, 2.0 };

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
							config.requiredTransmissionPowerExponent = requiredTransmissionPowerExponent;
							config.edgeWeightCalculatingFunction = weightProvider;
							config.movementModel = "GaussMarkov";
							config.movementModelMaster = "GaussMarkov";
							config.movementMaxSpeedInMetersPerSecond = 0.005;
							config.movementInterval = (int) topologyControlInterval + "m";
							config.minimumDistanceThresholdInMeters = minimalDistanceInMeters;
							config.end = endTime;

							final File targetFileForConfiguration = new File(outputFolder, String
									.format("parameters%s%04d_parameters.txt", File.separator, configurationNumber));
							try {
								FileUtils.writeStringToFile(targetFileForConfiguration, config.toString());
							} catch (IOException e) {
								Monitor.log(SoSyMEvaluationExecutor.class, Level.ERROR,
										"Failed to write configuration number %d to %s (see the following stacktrace)",
										configurationNumber, targetFileForConfiguration);
								e.printStackTrace();
							}

							simulations.add(new TopologyControlSimulationTask(config));
						}
					}
				}
			}
		}
		final TaskExecutor taskExecutor = new TaskExecutor(simulations, parallelTasks);
		taskExecutor.setJvmOptionXmx("1500m");
		taskExecutor.start();

		Monitor.log(SoSyMEvaluationExecutor.class, Level.INFO, "Enter 'quit' to abort simulation.");
		TaskExecutorUtils.waitForQuitOrTerminationOfSubprocesses(taskExecutor);
		Monitor.log(SoSyMEvaluationExecutor.class, Level.INFO, "Simulation aborted - closing all threads");
		taskExecutor.stop();
	}

}
