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

package de.tud.kom.p2psim.impl.network.routed;

import java.util.List;
import java.util.Vector;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.common.SimHostComponent;
import de.tud.kom.p2psim.api.linklayer.LinkLayer;
import de.tud.kom.p2psim.api.network.NetProtocol;
import de.tud.kom.p2psim.api.network.routing.RoutingAlgorithm;
import de.tud.kom.p2psim.api.network.routing.RoutingConfiguration;
import de.tud.kom.p2psim.impl.network.IPv4NetID;
import de.tud.kom.p2psim.impl.network.routed.routing.GlobalKnowledgeRouting;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponentFactory;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

/**
 * This factory creates {@link RoutedNetLayer}s which support {@link LinkLayer}s
 * and the new Topology/Energy models.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 27.02.2012
 */
public class RoutedNetLayerFactory implements HostComponentFactory {

	private long fragmentSize = 1500;

	private boolean enableFragmenting = false;

	private boolean newHostGroup = false;
	
	private boolean startOffline = false;

	private List<RoutingConfiguration> routingConfigs = new Vector<RoutingConfiguration>();
	
		
	public RoutedNetLayerFactory() {
		// empty
	}

	@Override
	public SimHostComponent createComponent(Host pHost) {
		SimHost host = (SimHost) pHost;
		if (!newHostGroup) {
			newHostGroup = true;
		}
		RoutedNetLayer nl = new RoutedNetLayer(host, enableFragmenting,
				fragmentSize, startOffline);

		int netIds = 0;
		for (RoutingConfiguration routingConfig : routingConfigs) {
			RoutingAlgorithm routing = routingConfig
					.getConfiguredRoutingAlgorithm(host);
			NetID netId = new IPv4NetID(host.getHostId() * 100
					+ Randoms.getRandom(RoutedNetLayer.class).nextInt(10) * 10
					+ netIds);
			netIds++;
			nl.addRoutingAlgorithm(routing, netId);
		}

		return nl;
	}

	/**
	 * Enable Fragmenting and rely on the default FragmentSize (currently 1500
	 * byte) or set the FragmentSize as well.
	 * 
	 * @param enableFragmenting
	 */
	public void setEnableFragmenting(boolean enableFragmenting) {
		this.enableFragmenting = enableFragmenting;
	}

	/**
	 * If this is set to true, hosts will start as "offline"
	 * 
	 * @param startHostsOffline
	 */
	public void setStartHostsOffline(boolean startHostsOffline) {
		this.startOffline = startHostsOffline;
	}

	/**
	 * Set a FragmentSize in byte, this will automatically enable fragmenting.
	 * 
	 * @param fragmentSize
	 */
	public void setFragmentSize(long fragmentSize) {
		this.fragmentSize = fragmentSize;
		this.enableFragmenting = true;
	}

	/**
	 * Add a {@link RoutingConfiguration} for this Group of Hosts (if a new
	 * group starts, the List of Routing Algorithms is cleared for the group).
	 * If you do not specify any routing Algorithm
	 * {@link GlobalKnowledgeRouting} is used for IPv4, all other
	 * {@link NetProtocol}s are not enabled by default.
	 * 
	 * @param routingAlgorithm
	 */
	public void setRouting(RoutingConfiguration config) {
		if (newHostGroup) {
			routingConfigs.clear();
			newHostGroup = false;
		}
		routingConfigs.add(config);
	}

	public void setUpBandwidth(long bw) {
		// not used
	}

	public void setDownBandwidth(long bw) {
		// not used
	}

}
