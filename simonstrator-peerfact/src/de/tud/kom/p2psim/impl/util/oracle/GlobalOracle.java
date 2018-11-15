/*
 * Copyright (c) 2005-2011 KOM - Multimedia Communications Lab
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

package de.tud.kom.p2psim.impl.util.oracle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tudarmstadt.maki.simonstrator.api.Graphs;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.core.OracleComponent;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetInterface;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyID;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyProvider;

/**
 * This class gives access to the hosts of the scenario. To work, it has to be
 * referenced in the configuration file after the host builder.
 * 
 * The purpose of this class is to enable a global knowledge for analyzing. It
 * is not meant to be used within any functional parts of simulated systems.
 * 
 * @author Julius Rueckert
 * 
 */
public class GlobalOracle implements OracleComponent {

	private static HashMap<Long, SimHost> hostIDtoHosts = new LinkedHashMap<>();

	private static HashMap<NetID, SimHost> netIDtoHosts = new LinkedHashMap<>();

	private static List<SimHost> hosts = new LinkedList<>();

	private static List<NetID> bootstrapList = new LinkedList<>();

	private static GlobalOracle instance = new GlobalOracle();

	private GlobalOracle() {
		//
	}

	public static GlobalOracle getInstance() {
		return instance;
	}

	/**
	 * Initial population with hosts.
	 * 
	 * @param hostBuilder
	 */
	public static void populate(List<SimHost> allHosts) {
		hosts = allHosts;

		if (hosts == null || hosts.size() <= 0) {
			return;
		}

		for (SimHost host : hosts) {

			/* Might happen in case of FakeHost. */
			if (host.getNetworkComponent() == null) {
				continue;
			}

			hostIDtoHosts.put(host.getHostId(), host);
			for (NetInterface net : host.getNetworkComponent()
					.getNetworkInterfaces()) {
				netIDtoHosts.put(net.getLocalInetAddress(), host);
				bootstrapList.add(net.getLocalInetAddress());
			}
		}

		return;
	}

	/**
	 * Sets the bootstrap hosts. To be called by netLayer.
	 * 
	 * @param bootstrapList
	 *            the new bootstrap hosts
	 */
	public static void setBootstrapHosts(List<NetID> bootstrapList) {
		GlobalOracle.bootstrapList = bootstrapList;
	}

	/**
	 * Gets the bootstrap hosts.
	 * 
	 * @return the bootstrap hosts
	 */
	public static List<NetID> getBootstrapHosts() {
		return GlobalOracle.bootstrapList;
	}

	/**
	 * Gets the random host.
	 * 
	 * @return the random host
	 */
	public static NetID getRandomHost() {
		return bootstrapList.get(Randoms.getRandom(GlobalOracle.class)
				.nextInt(bootstrapList.size()));
	}

	/**
	 * Gets the first host.
	 * 
	 * @return the first host
	 */
	public static NetID getFirstHost() {

		if (bootstrapList.size() == 0) {
			assert (false) : "Bootstraplist is empty";
			return null;
		}

		return bootstrapList.get(0);

	}

	/**
	 * @param id
	 * @return the host with the given <code>NetID</code>
	 */
	public static SimHost getHostForNetID(NetID id) {
		return netIDtoHosts.get(id);
	}

	/**
	 * @param id
	 * @return the host with the given host ID
	 */
	public static SimHost getHostForHostID(Long id) {
		return hostIDtoHosts.get(id);
	}

	/**
	 * @return the list with all hosts of the scenario
	 */
	public static List<SimHost> getHosts() {
		synchronized (hosts) {
			return new ArrayList<>(hosts);
		}
	}

	@Override
	public List<Host> getAllHosts() {
		return new ArrayList<Host>(hosts);
	}

	/**
	 * Returns a global view of the topology for the specified mechanism. The
	 * mechanism must be a HostComponent that is registered at the local host.
	 * Otherwise, this method will not be able to find the local mechanism
	 * objects.
	 *
	 * @param component
	 * @param identifier
	 * @return
	 */
	public static <T extends TopologyProvider> Graph getTopology(
			final Class<T> component, final TopologyID identifier) {

		final HashMap<INode, TopologyProvider> nodeToProviderMapping = getNodeToTopologyProviderMapping(
				component, identifier);

		final Collection<INode> nodes = nodeToProviderMapping.keySet();
		final Collection<TopologyProvider> allProviders = nodeToProviderMapping
				.values();

		final HashSet<IEdge> edges = new LinkedHashSet<>();

		// Collect the local view of each topology provider
		for (final TopologyProvider topologyProvider : allProviders) {
			final Set<IEdge> neighbors = topologyProvider
					.getNeighbors(identifier);
			edges.addAll(neighbors);
		}

		// Construct the merged graph
		final Graph graph = Graphs.createGraph(nodes, edges);

		return graph;
	}

	private static <T extends TopologyProvider> HashMap<INode, TopologyProvider> getNodeToTopologyProviderMapping(
			final Class<T> component, final TopologyID identifier) {
		final HashMap<INode, TopologyProvider> nodeToProviderMapping = new HashMap<>();

		// Collect topology providers on all hosts
		for (final SimHost host : getHosts()) {
			try {
				final TopologyProvider topologyProvider = host
						.getComponent(component);

				final INode providerNode = topologyProvider.getNode(identifier);

				nodeToProviderMapping.put(providerNode, topologyProvider);

			} catch (final ComponentNotAvailableException e) {
				// if the component is not available on the host, we can't do
				// anything about it
				// no reason to crash the simulation as this might be the case
				// in various scenarios
			}
		}
		return nodeToProviderMapping;
	}

	@Override
	public boolean isSimulation() {
		return true;
	}

	/**
	 * Checks whether the host with the given NetID is online using a global
	 * list of all hosts in the current scenario.
	 * 
	 * @param receiver
	 * @return true if online
	 */
	public static boolean isHostOnline(NetID receiver) {
		if (netIDtoHosts.get(receiver) == null) {
			return false;
		}

		return netIDtoHosts.get(receiver).getNetworkComponent()
				.getByNetId(receiver).isUp();
	}
}
