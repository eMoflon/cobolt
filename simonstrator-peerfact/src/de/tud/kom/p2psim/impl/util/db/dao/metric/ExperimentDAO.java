/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
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

package de.tud.kom.p2psim.impl.util.db.dao.metric;

import java.util.Date;

import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.util.db.dao.DAO;
import de.tud.kom.p2psim.impl.util.db.metric.Experiment;
import de.tud.kom.p2psim.impl.util.db.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * This class provides access to the single {@link Experiment} instance.
 * If it does not exist, it will be created automatically by the
 * getter. Additionally there is a method to update the end Date of the
 * experiment.<br>
 *
 * This class is used by {@link MetricDAO} because the {@link Metric} table contains
 * a reference to the {@link Experiment} table.
 *
 * @author Christoph Muenker
 * @author Andreas Hemel
 */
public class ExperimentDAO extends DAO {

	/**
	 * The singleton for the experiment!
	 */
	private static Experiment experiment;

	/**
	 * Gets a singleton of the experiment. For every run should exist one
	 * experiment!<br>
	 * <br>
	 * The needed information will be set.
	 *
	 * @return A singleton of the object {@link Experiment}.
	 */
	public static Experiment getExperiment() {
		if (experiment == null) {
			Date actTime = new Date();
			long seed = Simulator.getSeed();
			String system = Simulator.getInstance().getConfigurator()
					.getResolvedConfiguration();
			// FIXME: The following has to be uncommented again.
//			String workload = Simulator.getInstance().getConfigurator()
//					.getVariables().toString();
			String experimentDescription = Simulator.getMonitor()
					.getExperimentDescription();
			experiment = new Experiment(seed, actTime, experimentDescription,
					system, "");

			persistImmediately(experiment);
		}
		return experiment;
	}

	/** Called by the {@link Time} when the simulation is shut down. */
	public static void simulationFinished() {
		// If there is no experiment object, no measurements have been made,
		// and hence, there is no reason to contact the database.
		if (experiment == null)
			return;

		updateEndDate();
		commitQueue();
		finishCommits();
	}

	/**
	 * Update the end date of the experiment. Should be called at the end of the
	 * Simulation.
	 */
	private static void updateEndDate() {
		experiment.setEndDate(new Date());
		update(experiment);
	}
}
