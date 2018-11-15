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
 * This class is a configuration for the
 * {@link StateEnergyCommunicationComponent}. It takes the Amperes and volts of
 * the Broadcom BCM4329 Chip, which is used in the Nexus One. The Values are
 * from the datasheet.
 * 
 * @author Christoph Muenker
 * @version 1.0, 19.02.2013
 */
public class WifiBCM4329Chip implements
		EnergyConfiguration<StateEnergyCommunicationComponent> {

	private PhyType phy = PhyType.WIFI;

	/*
	 * Parameters from SWB-B23 Datasheet - Broadcom BCM4329 WLAN+BT Solution
	 */

	private static double volt = 3.3; // in volt

	private static double sendAmp = 0.2525; // in ampere

	private static double recvAmp = 0.07783; // in ampere

	private static double idleAmp = 0.0004909; // in ampere

	private static double sleepAmp = 0.000125; // in ampere

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
