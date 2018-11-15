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

package de.tud.kom.p2psim.impl.linklayer;

import java.util.List;
import java.util.Vector;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.linklayer.LinkLayer;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.MacConfiguration;
import de.tud.kom.p2psim.api.linklayer.mac.MacLayer;
import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponentFactory;

/**
 * This is the factory for the LinkLayer.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 21.02.2012
 */
public class LinkLayerFactory implements HostComponentFactory {

	/**
	 * A List of the currently active MAC-Layer configurations
	 */
	private List<MacConfiguration> macConfigurations;

	/**
	 * This is used to reset macConfigurations if a new Group of Hosts is to be
	 * configured. We imply that the MAC is fully configured for each Group or
	 * not at all to keep the settings of the previous group.
	 * 
	 * If there exists a MAC-Tag, all previous macConfigurations are deleted.
	 */
	private boolean configurationsUsed = false;

	/**
	 * A new LinkLayerFactory
	 */
	public LinkLayerFactory() {
		macConfigurations = new Vector<MacConfiguration>();
	}

	@Override
	public LinkLayer createComponent(Host pHost) {
		SimHost host = (SimHost) pHost;
		if (macConfigurations.isEmpty()) {
			throw new ConfigurationException(
					"No MAC was specified within the LinkLayer!\n"
							+ "Add at least one child element <Mac class=\"...\" /> pointing to a MacConfiguration.");
		}
		configurationsUsed = true;

		/*
		 * Configure the LL-Instance with the MAC-Layer instances
		 */
		ModularLinkLayer ll = new ModularLinkLayer(host);
		int macSequence = 0;
		for (MacConfiguration macConfig : macConfigurations) {
			macSequence++;
			// create a unique MacAddress
			long macAddrLong = host.getHostId() * 10 + macSequence;
			if (macAddrLong > Integer.MAX_VALUE) {
				throw new AssertionError("Range overflow for Mac-Addresses!");
			}
			MacAddress macAddress = new MacAddress((int) macAddrLong);
			MacLayer macLayer = macConfig.getConfiguredMacLayer(host,
					macAddress);
			ll.addMacLayer(macLayer);
		}
		return ll;
	}

	/**
	 * Sets the {@link MacConfiguration} for the following hosts. You are able
	 * to specify multiple MAC-Layers by providing multiple MAC-tags.
	 * 
	 * @param macFactory
	 */
	public void setMac(MacConfiguration macConfiguration) {
		if (configurationsUsed) {
			macConfigurations.clear();
			configurationsUsed = false;
		}
		// check configuration
		if (!macConfiguration.isWellConfigured()) {
			throw new ConfigurationException(
					"The MAC was not well configured.\n"
							+ macConfiguration.getHelp());
		}
		// just log the information
		Monitor.log(LinkLayerFactory.class, Level.INFO,
				"MAC created - some information:\n %s",
				macConfiguration.getHelp());
		macConfigurations.add(macConfiguration);
	}

}
