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

package de.tud.kom.p2psim.impl.network.routed.config;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.network.NetProtocol;
import de.tud.kom.p2psim.api.network.routing.RoutingAlgorithm;
import de.tud.kom.p2psim.api.network.routing.RoutingConfiguration;
import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.impl.network.routed.RoutedNetLayer;
import de.tud.kom.p2psim.impl.network.routed.routing.GlobalKnowledgeRouting;
import de.tud.kom.p2psim.impl.network.routed.routing.OneHopRouting;
import de.tud.kom.p2psim.impl.network.routed.routing.aodv.AodvRouting;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * Use this class to configure {@link RoutingAlgorithm}s to be used inside the
 * {@link RoutedNetLayer}. It provides the default config-values for each of the
 * algorithms.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 26.04.2012
 */
public class Routing implements RoutingConfiguration {
	
	public static enum RoutingAlgo {
		AODV, GLOBAL_KNOWLEDGE, ONE_HOP
	}
	
	/**
	 * The routing Algorithm to create
	 */
	private RoutingAlgo algorithm = null;

	/**
	 * The Net-Protocol to use
	 */
	private NetProtocol protocol = null;

	/**
	 * The PHY that is used by this routing Algorithm (some algorithms may
	 * define defaults or can only be used with a defined kind of PHY)
	 */
	private PhyType phy = null;

	/**
	 * Configure a {@link RoutingAlgorithm} for the given netProtocol. Available
	 * {@link RoutingAlgorithm}s are defined in the {@link RoutingAlgo}-Enum
	 * 
	 * @param netProtocol
	 * @param algorithm
	 */
	@XMLConfigurableConstructor({ "protocol", "algorithm" })
	public Routing(String protocol, String algorithm) {
		this.protocol = NetProtocol.valueOf(protocol);
		this.algorithm = RoutingAlgo.valueOf(algorithm);
	}

	/**
	 * Set the {@link PhyType} that should be used by the RoutingAlgorithm for
	 * outgoing messages.
	 * 
	 * @param phy
	 */
	public void setPhy(String phy) {
		this.phy = PhyType.valueOf(phy.toUpperCase());
		if (this.phy == null) {
			throw new ConfigurationException("PHY " + phy
					+ " is not supported. Valid options are "
					+ PhyType.printTypes());
		}
	}

	@Override
	public RoutingAlgorithm getConfiguredRoutingAlgorithm(SimHost host) {
		RoutingAlgorithm routing;
		switch (algorithm) {
		case ONE_HOP:
			routing = new OneHopRouting(host, phy);
			break;

		case GLOBAL_KNOWLEDGE:
			routing = new GlobalKnowledgeRouting(host, phy);
			break;

		case AODV:
			routing = new AodvRouting(host, phy);
			break;

		default:
			throw new ConfigurationException("Algorithm " + algorithm
					+ " is not supported.");
		}
		return routing;
	}

}
