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

package de.tudarmstadt.maki.simonstrator.peerfact.application.sensor;

import java.util.LinkedList;
import java.util.List;

import de.tudarmstadt.maki.simonstrator.peerfact.SimulatorRunner;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.util.run.SimulationTask;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.util.run.TaskExecutor;

/**
 *
 * @author Michael Stein
 *
 */
public class Execute {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// number of runs
		final int RUNS = 25;
		final String CONFIG_FILE = "config/sensor/sensor_basic.xml";
		final int START_SEED = 0;

		LinkedList<SimulationTask> simulations = new LinkedList<SimulationTask>();

		for (int i = START_SEED; i < RUNS; i++) {
			final int seed = i + 1;

			simulations.add(new SimulationTask() {

				@Override
				public Class<ExceptionSimulatorRunner> getSimulationClass() {
					return ExceptionSimulatorRunner.class;
				}

				@Override
				public List<String> getParams() {
					LinkedList<String> params = new LinkedList<String>();
					params.add(CONFIG_FILE);
					params.add("seed=" + seed);
					return params;
				}

				@Override
				public String toString() {
					return "Peerfact Simulation. Class: "
							+ getSimulationClass() + "; Params: " + getParams();
				}
			});
		}

		final int PARALLEL_TASKS = 2;
		TaskExecutor taskExecutor = new TaskExecutor(simulations, PARALLEL_TASKS);
//		TaskExecutor taskExecutor = new TaskExecutor(simulations);
		taskExecutor.start();
	}

	/**
	 * {@link SimulatorRunner} that throws Exceptions.
	 */
	public static class ExceptionSimulatorRunner extends SimulatorRunner {
		protected ExceptionSimulatorRunner(String[] args) {
			super(args);
		}

		@Override
		protected boolean shallThrowExceptions() {
			return true;
		}
	}
}
