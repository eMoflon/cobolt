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
import de.tud.kom.p2psim.api.energy.EnergyConfiguration;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.impl.energy.SmartphoneCommunicationEnergyComponent;

public class TimeBased implements
		EnergyConfiguration<SmartphoneCommunicationEnergyComponent> {

	private PhyType phy;

	private boolean error = false;

	private boolean warning = false;

	private final String help = "This is the Time-Based Configuration for the EnergyModel of a Wi-FI ad hoc connection."
			+ " It creates a component for the chosen PHY-Type and each time a message is being sent or received, energy is consumed for the corresponding amount of time, it took to send or receiver the message."
			+ " The default parameters for the calculation of the energy consumption are taken from Fabian Kaub's Diploma Thesis.";

	private String errorMessage = "";

	private String warningMessage = "";

	@Override
	public SmartphoneCommunicationEnergyComponent getConfiguredEnergyComponent(
			SimHost host) {
		return new SmartphoneCommunicationEnergyComponent(phy);
	}

	@Override
	public String getHelp() {
		if (error) {
			return errorMessage;
		}
		if (warning) {
			return warningMessage;
		}
		return help;
	}

	@Override
	public boolean isWellConfigured() {
		if (phy == null) {
			error = true;
			errorMessage += "\nYou did not specify a PHY-Type for the Time-based Energy Component. Please set the config-parameter \"phy\" to one of "
					+ PhyType.printTypes();
		}
		if (phy != PhyType.WIFI) {
			warning = true;
			warningMessage += "\nThe Smartphone-Model is only suitable for WIFI-Ad-Hoc-Simulations. Consider using a state-based model for other types of communication.";
		}
		return !error;
	}

	public void setPhy(String phy) {
		try {
			this.phy = Enum.valueOf(PhyType.class, phy.toUpperCase());
		} catch (IllegalArgumentException e) {
			error = true;
			errorMessage += "\nYou did specify an invalid PHY-Type. Allowed types are "
					+ PhyType.printTypes();
		}
	}

}
