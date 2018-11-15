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

package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.energyanalysis;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

import org.apache.commons.io.FileUtils;

import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.evaluation.TaskExecutorUtils;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.util.run.SimulationTask;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.util.run.TaskExecutor;
import de.tudarmstadt.maki.simonstrator.tc.utils.DateHelper;

/**
 * This class configures the different combinations of parameters for running
 * the evaluation of the incremental TC algorithms.
 */
public class EnergyMeasurementTaskExecutor {

	public static void main(String[] args) throws IOException, InterruptedException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HHmmss.SSS");
		System.setProperty("logfile.name", "rkluge/simrunnerlog_" + dateFormat.format(new Date()) + ".log");
		Locale.setDefault(Locale.US);

		final LinkedList<SimulationTask> simulations = new LinkedList<SimulationTask>();
		final String date = DateHelper.getFormattedDate();

		final int numberOfParallelTasks = 3;
		final int numberOfRepetitions = 5;
		final int batteryCapacityInJoule = 10000;
		final File configFile = new File("config/energy_measurement/energy_measurement.xml");
		final int messageSizeInByte = 1000;

		for (PhyType phyType : Arrays.asList(PhyType.WIFI)) {
			for (final double transmissionIntervalAverageInSeconds : Arrays.asList(1e-3, 3e-3, 7e-3, 1e-2, 3e-2, 7e-2,
					1e-1, 3e-1, 7e-1, 1.0)) {
				for (int seed = 1; seed <= numberOfRepetitions; seed++) {

					final double rateInMBPerSec = messageSizeInByte / transmissionIntervalAverageInSeconds;
					final File outputFileReceiver = new File(EnergyMeasurementConfiguration.DEFAULT_OUTPUT_ROOT,
							String.format("energy-calib_%s_phyType%s/rateInMBPerSec%.3f/[ROLE]/s%04d.csv", date,
									phyType, rateInMBPerSec, seed));

					final EnergyMeasurementConfiguration config = new EnergyMeasurementConfiguration();
					config.configFile = configFile;
					config.seed = seed;
					config.batteryCapacityInJoule = batteryCapacityInJoule;
					config.outputFile = outputFileReceiver;
					config.phyType = phyType;
					config.transmissionFrequencyAverageInSeconds = transmissionIntervalAverageInSeconds;
					config.messageSizeInBytes = messageSizeInByte;
					config.initialDistanceInMeters = 15;
					config.movementStepSizeInMeters = 5;
					config.movementTimeInterval = 10 * Time.MINUTE;

					FileUtils.writeStringToFile(
							new File(EnergyMeasurementConfiguration.DEFAULT_OUTPUT_ROOT,
									String.format("energy-calib_%s_phyType%s/basic_config.xml", date, phyType)),
							FileUtils.readFileToString(configFile));

					simulations.add(new EnergyMeasurementTask(config));
				}
			}
		}
		TaskExecutor taskExecutor = new TaskExecutor(simulations, numberOfParallelTasks);
		taskExecutor.start();

		Monitor.log(EnergyMeasurementTaskExecutor.class, Level.INFO, "Enter 'quit' to abort simulation.");
		TaskExecutorUtils.waitForQuitOrTerminationOfSubprocesses(taskExecutor);
		Monitor.log(EnergyMeasurementTaskExecutor.class, Level.INFO, "Simulation finished - closing all threads");
		taskExecutor.stop();
	}

}
