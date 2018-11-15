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

package de.tud.kom.p2psim.impl.energy.configs;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.energy.ComponentType;
import de.tud.kom.p2psim.api.energy.EnergyConfiguration;
import de.tud.kom.p2psim.impl.energy.OneStateEnergyComponent;

/**
 * This class creates an Energy Component for basic Energy Consumption. It
 * consumes continuous the given energy.
 * 
 * @author Christoph Muenker
 * @version 1.0, 07.03.2013
 */
public class BasicEnergyConsumptionConfig implements
		EnergyConfiguration<OneStateEnergyComponent> {

	double basicW = 0.046192; // measured on the Nexus one (Display off, Wifi
								// off)

	@Override
	public OneStateEnergyComponent getConfiguredEnergyComponent(SimHost host) {
		OneStateEnergyComponent basic = new OneStateEnergyComponent(
				basicW * 1000 * 1000, ComponentType.BASIC);
		return basic;
	}

	@Override
	public String getHelp() {
		return "Set the basicW in the configuration file. The default is: 0.046192";
	}

	@Override
	public boolean isWellConfigured() {
		return true;
	}

	public void setBasicW(double basicW) {
		this.basicW = basicW;
	}

}
