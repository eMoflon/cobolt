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

package de.tud.kom.p2psim.api.analyzer;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.energy.EnergyComponent;
import de.tud.kom.p2psim.api.energy.EnergyState;
import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent.Analyzer;

/**
 * The {@link EnergyAnalyzer} receives notification, about important changes in
 * the energy-level.
 * 
 * @author Christoph Muenker
 * @version 1.0, 06/07/2011
 */
public interface EnergyAnalyzer extends Analyzer {

	/**
	 * Informs about Energy consuming from the battery.
	 * 
	 * @param host
	 *            The host, which consume this energy.
	 * @param energy
	 *            The amount of energy in µJ.
	 * @param consumer
	 *            The consumer, which consumed this amount of energy
	 * @param energyState 
	 *			  The state in which the energy was consumed
	 */
	public void consumeEnergy(SimHost host, double energy, EnergyComponent consumer, EnergyState energyState);

	/**
	 * Informs about an empty battery.
	 * 
	 * @param host
	 *            The Host, which has an empty energy.
	 */
	public void batteryIsEmpty(SimHost host);

	/**
	 * Gets the time, how long the component was in high power mode.
	 * 
	 * @param host
	 *            The host, which component is in high power mode
	 * @param time
	 *            The duration in high power mode
	 * @param consumedEnergy
	 *            The consumedEnergy by this mode.
	 * @param consumer
	 *            An unique description for the component which is in high power
	 *            mode.
	 */
	public void highPowerMode(SimHost host, long time, double consumedEnergy,
			EnergyComponent component);

	/**
	 * Gets the time, how long the component was in low power mode
	 * 
	 * @param host
	 *            The host, which component is in low power mode
	 * @param time
	 *            The duration in low power mode
	 * @param consumedEnergy
	 *            The consumedEnergy by this mode.
	 * @param consumer
	 *            An unique description for the component which is in low power
	 *            mode.
	 */
	public void lowPowerMode(SimHost host, long time, double consumedEnergy,
			EnergyComponent component);

	/**
	 * Gets the time, how long the component was in tail mode
	 * 
	 * @param host
	 *            The host, which component is in tail mode
	 * @param time
	 *            The duration in low power mode
	 * @param consumedEnergy
	 *            The consumedEnergy by this mode.
	 * @param consumer
	 *            An unique description for the component which is in low power
	 *            mode.
	 */
	public void tailMode(SimHost host, long time, double consumedEnergy,
			EnergyComponent component);

	/**
	 * 
	 * @param host
	 * @param time
	 * @param consumedEnergy
	 * @param component
	 */
	public void offMode(SimHost host, long time, double consumedEnergy,
			EnergyComponent component);
}