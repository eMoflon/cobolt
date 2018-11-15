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

package de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.jvlc2015;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.evaluation.TaskExecutorUtils;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.util.run.SimulationTask;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.util.run.TaskExecutor;
import de.tudarmstadt.maki.simonstrator.tc.component.TopologyControlComponentEvaluationDataHelper;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;
import de.tudarmstadt.maki.simonstrator.tc.utils.DateHelper;

/**
 * This class configures the different combinations of parameters for running
 * the evaluation of the incremental TC algorithms.
 * 
 * @author Roland Kluge - Initial implementation
 */
public class JvlcEvaluationExecutor {

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
		secondEvaluationFINAL();
	}

	/**
	 * Evaluation for 'major revision' in April/May 2016
	 */
	private static void secondEvaluationFINAL() {
		final String formattedDate = DateHelper.getFormattedDate();
		final LinkedList<SimulationTask> simulations = new LinkedList<SimulationTask>();

		final int parallelTasks = 1;
		final int seedCount = 15;
		final List<int[]> nodeCountsWithWorldSizes = new ArrayList<>();
		nodeCountsWithWorldSizes.add(new int[] { 99, 750 });
		nodeCountsWithWorldSizes.add(new int[] { 99, 500 });
		nodeCountsWithWorldSizes.add(new int[] { 99, 250 });
		nodeCountsWithWorldSizes.add(new int[] { 999, 1000 });
		nodeCountsWithWorldSizes.add(new int[] { 999, 1500 });
		nodeCountsWithWorldSizes.add(new int[] { 999, 2000 });
		final double[] kParameters = new double[] { 1.41 };
		final int batteryCapacity = 130;
		final double requiredTransmissionPowerExponent = 2.0;
		final List<TopologyControlAlgorithmID> algorithms = new ArrayList<>();
		algorithms.add(UnderlayTopologyControlAlgorithms.D_KTC);

		for (final int[] nodeCountsWithWorldSize : nodeCountsWithWorldSizes) {
			final int nodeCount = nodeCountsWithWorldSize[0];
			final int worldSize = nodeCountsWithWorldSize[1];
			for (final double k : kParameters) {
				for (final TopologyControlAlgorithmID algoId : algorithms) {
					for (int seed = 1; seed <= seedCount; seed++) {
						final File outputFile = new File(TopologyControlComponentEvaluationDataHelper.EVAL_ROOT_FOLDER,
								"batchrun_" + formattedDate);
						simulations.add(new JvlcSimulationTask(seed, algoId, worldSize, nodeCount, k,
								requiredTransmissionPowerExponent, batteryCapacity, outputFile));
					}
				}
			}
		}
		final TaskExecutor taskExecutor = new TaskExecutor(simulations, parallelTasks);
		taskExecutor.setJvmOptionXmx("1500m");
		taskExecutor.start();

		Monitor.log(JvlcEvaluationExecutor.class, Level.INFO, "Enter 'quit' to abort simulation.");
		TaskExecutorUtils.waitForQuitOrTerminationOfSubprocesses(taskExecutor);
		Monitor.log(JvlcEvaluationExecutor.class, Level.INFO, "Simulation aborted - closing all threads");
		taskExecutor.stop();
	}

	/**
	 * Evaluation for 'initial submission' (Nov 2015)
	 */
	@SuppressWarnings("unused")
	private static void firstEvaluation() {
		final LinkedList<SimulationTask> simulations = new LinkedList<SimulationTask>();
		final String date = DateHelper.getFormattedDate();

		final int PARALLEL_TASKS = 4;
		final int REPETITION_COUNT = 3;
		final int[][] nodeCountsWithWorldSizes = new int[][] { { 50, 100 }, { 125, 250 }, { 250, 500 } };
		// , { 500, 1000 }, { 1000, 2000 }
		final double[] kParameters = new double[] { 1.41 };
		final int batteryCapacity = 130;
		final double requiredTransmissionPowerExponent = 2.0;
		final TopologyControlAlgorithmID[] algorithms = { UnderlayTopologyControlAlgorithms.MAXPOWER_TC,
				UnderlayTopologyControlAlgorithms.D_KTC,
				UnderlayTopologyControlAlgorithms.E_KTC };
		final String[] scenarios = { "datacollection", "alltoall", "gossip" };
		for (int[] nodeCountsWithWorldSize : nodeCountsWithWorldSizes) {
			final int nodeCount = nodeCountsWithWorldSize[0];
			final int worldSize = nodeCountsWithWorldSize[1];
			for (double k : kParameters) {
				for (final TopologyControlAlgorithmID algoId : algorithms) {
					for (int seed = 1; seed <= REPETITION_COUNT; seed++) {
						final File outputFolder = TopologyControlComponentEvaluationDataHelper.EVAL_ROOT_FOLDER;
						simulations.add(new JvlcSimulationTask(seed, algoId, worldSize, nodeCount, k,
								requiredTransmissionPowerExponent, batteryCapacity, outputFolder));
					}
				}
			}
		}
		TaskExecutor taskExecutor = new TaskExecutor(simulations, PARALLEL_TASKS);
		taskExecutor.start();

		Monitor.log(JvlcEvaluationExecutor.class, Level.INFO, "Enter 'quit' to abort simulation.");
		TaskExecutorUtils.waitForQuitOrTerminationOfSubprocesses(taskExecutor);
		Monitor.log(JvlcEvaluationExecutor.class, Level.INFO, "Simulation aborted - closing all threads");
		taskExecutor.stop();
	}

}
