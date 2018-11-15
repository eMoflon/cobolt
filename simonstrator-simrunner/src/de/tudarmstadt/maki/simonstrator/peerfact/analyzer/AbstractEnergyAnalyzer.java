/*
 * Copyright (c) 2005-2013 KOM - Multimedia Communications Lab
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
 */

package de.tudarmstadt.maki.simonstrator.peerfact.analyzer;

import java.io.Writer;

import de.tud.kom.p2psim.api.analyzer.EnergyAnalyzer;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.energy.EnergyComponent;
import de.tud.kom.p2psim.api.energy.EnergyState;

/**
 * @author Fabio Zöllner
 * @version 1.0, 25.01.13
 */
public class AbstractEnergyAnalyzer implements EnergyAnalyzer {

	@Override
	public void start() {
	}

	@Override
	public void stop(Writer out) {
	}

	@Override
	public void consumeEnergy(SimHost host, double energy, EnergyComponent consumer, EnergyState energyState) {
	}

	@Override
	public void batteryIsEmpty(SimHost host) {
	}

	@Override
	public void highPowerMode(SimHost host, long time, double consumedEnergy, EnergyComponent component) {
	}

	@Override
	public void lowPowerMode(SimHost host, long time, double consumedEnergy, EnergyComponent component) {
	}

	@Override
	public void tailMode(SimHost host, long time, double consumedEnergy, EnergyComponent component) {
	}

	@Override
	public void offMode(SimHost host, long time, double consumedEnergy, EnergyComponent component) {
	}
}
