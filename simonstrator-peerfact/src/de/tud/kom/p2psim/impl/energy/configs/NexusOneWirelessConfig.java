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
import de.tud.kom.p2psim.impl.energy.StateEnergyCommunicationComponent;

/**
 * This class contains the measured values from the Nexus One in the AdHoc Mode.
 * It contains the CPU load of a simple application. The basic energy
 * consumption is not included. <br>
 * 
 * For interests:<br>
 * We have measured the energy consumption of the Nexus One in different states
 * (Wifi On, Wifi Send, Wifi Receive, Wifi off). After this, we have the energy
 * consumption of the system in different states. Now we derive the average
 * energy consumption of every state and take the result as power consumption.
 * 
 * TODO: The next step is, we derive on the sampled data the
 * "Mixture Density Estimation" to cluster the power states during the sending
 * of data.
 * 
 * @author Christoph Muenker
 * @version 1.0, 07.03.2013
 */
public class NexusOneWirelessConfig implements
		EnergyConfiguration<StateEnergyCommunicationComponent> {

	private PhyType phy = PhyType.WIFI;

	private static double volt = 3.3; // in volt

	private static double sendAmp = 1.1327413333 / volt; // in ampere

	private static double recvAmp = 0.9361746667 / volt; // in ampere

	private static double idleAmp = 0.254078 / volt; // in ampere

	private static double sleepAmp = 0.000125; // in ampere; Not measured but we
												// use the value from the
												// Datasheet of the BCM4329 Chip

	@Override
	public StateEnergyCommunicationComponent getConfiguredEnergyComponent(
			SimHost host) {

		StateEnergyCommunicationComponent.volt = volt;
		StateEnergyCommunicationComponent.sendAmp = sendAmp;
		StateEnergyCommunicationComponent.recvAmp = recvAmp;
		StateEnergyCommunicationComponent.idleAmp = idleAmp;
		StateEnergyCommunicationComponent.sleepAmp = sleepAmp;

		return new StateEnergyCommunicationComponent(phy);
	}

	@Override
	public String getHelp() {
		return "Nothing to do in this class!";
	}

	@Override
	public boolean isWellConfigured() {
		return true;
	}

}
