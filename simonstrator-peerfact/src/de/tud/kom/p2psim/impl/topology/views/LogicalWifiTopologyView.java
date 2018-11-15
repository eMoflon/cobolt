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

package de.tud.kom.p2psim.impl.topology.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.MacLayer;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.impl.topology.LocalGraphView;
import de.tud.kom.p2psim.impl.topology.LocalGraphView.NeighborProvider;
import de.tud.kom.p2psim.impl.topology.views.wifi.WifiTopologyView;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Node;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetworkComponent.NetInterfaceName;
import de.tudarmstadt.maki.simonstrator.api.component.topology.AdaptableTopologyProvider;
import de.tudarmstadt.maki.simonstrator.api.component.topology.ObservableTopologyProvider;
import de.tudarmstadt.maki.simonstrator.api.component.topology.OperationalEdge;
import de.tudarmstadt.maki.simonstrator.api.component.topology.OperationalEdge.EdgeOperationType;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyID;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyObserver;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyProvider;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * This TopologyView is be to filter some neighbors from the perspective of
 * individual nodes. This is a requirement for underlay topology control, like
 * kTC.
 *
 * @author Michael Stein
 * @version 1.0, 23.07.2014
 */
public class LogicalWifiTopologyView extends WifiTopologyView
		implements NeighborProvider {

	protected static final TopologyID UDG_TOPOLOGY_ID = TopologyID
			.getIdentifier("UDG_Topology", LogicalWiFiTopology.class);

	protected static final TopologyID LOGICAL_TOPOLOGY_ID = TopologyID
			.getIdentifier("Adaptable_Topology", LogicalWiFiTopology.class);

	/**
	 * Graph view containing all "real" neighbors, e.g., not including any
	 * topology modifications.
	 */
	protected LocalGraphView udgGraphView = null;

	/**
	 * Graph view for the logical graph (relying on the implementation of
	 * getNeighbors in this view).
	 */
	protected LocalGraphView adaptedGraphView = null;

	/**
	 * This data structure maps nodes to all the nodes that are filtered for
	 * this node.
	 */
	private LinkedHashMap<MacAddress, Set<MacAddress>> logicalNeighborFilter = null;

	/**
	 * This data structure keeps track of the physical topology because we
	 * cannot access it anymore by using the super class.
	 */
	private LinkedHashMap<MacAddress, Set<MacAddress>> physicalTopology = null;

	private Map<INode, MacAddress> nodeToMacAddress = new LinkedHashMap<>();

	private LinkedHashMap<MacAddress, INode> macAddressToNode = new LinkedHashMap<>();

	public LogicalWifiTopologyView(PhyType phy) {
		super(phy);
	}

	@XMLConfigurableConstructor({ "phy" })
	public LogicalWifiTopologyView(String phy) {
		super(phy);
	}

	/**
	 * A {@link TopologyID} providing access to the UDG-Graph used by the
	 * Logical WiFi View (this graph contains all edges, even if they are
	 * disabled by the topology interactions)
	 */
	public static TopologyID getUDGTopologyID() {
		return UDG_TOPOLOGY_ID;
	}

	/**
	 * A {@link TopologyID} providing access to the adapted topology, which is
	 * subgraph of the topology with ID {@link #getUDGTopologyID()}.
	 */
	public static TopologyID getAdaptableTopologyID() {
		return LOGICAL_TOPOLOGY_ID;
	}

	@Override
	public List<MacAddress> getCustomNeighbors(MacAddress macAddress) {
		/*
		 * Has to return the UDG list of neighbors
		 */
		if (physicalTopology == null) {
			// the logical neighborhood has not yet been changed. Thus, I can
			// just return the superclass neighborhood
			return this.getNeighbors(macAddress);
		} else {
			// udpate neighborhood (this will implicitly also update the
			// physical neighborhood, because updateOutdatedLink(...) will be
			// called
			this.updateNeighborhood(macAddress);

			ArrayList<MacAddress> filteredNeighbors = new ArrayList<>(
					physicalTopology.get(macAddress));

			return filteredNeighbors;
		}
	}

	@Override
	protected void addedMac(MacLayer mac) {
		super.addedMac(mac);
		LogicalWiFiTopology udgTopo = new LogicalWiFiTopology(mac.getHost());
		mac.getHost().registerComponent(udgTopo);
		createNodeToMacAddressMapping(udgTopo.getNode(LOGICAL_TOPOLOGY_ID),
				mac.getMacAddress());
	}

	/**
	 * This method is called after movement or if link.setOutdated(true) has
	 * been called and updateNeighborhood(...) was called. Here, we filter
	 * physical links, but still keep track the links for the physicalTopology
	 * data structure
	 */
	@Override
	protected void updateOutdatedLink(RangedLink link) {

		// let super class implementation decide on connectivity
		super.updateOutdatedLink(link);

		// update physical topology here. (note: we might not change anything
		// here)
		if (physicalTopology != null) {
			assert logicalNeighborFilter != null;

			if (link.isConnected()) {
				MacAddress source = link.getSource();
				MacAddress destination = link.getDestination();

				assert source != null && link != null;

				Set<MacAddress> set = physicalTopology.get(source);
				set.add(destination);
			} else {
				physicalTopology.get(link.getSource())
						.remove(link.getDestination());
			}
		}

		// now check, if the link is still connected according to our logical
		// topology
		if (link.isConnected() && isFiltered(link)) {
			link.setConnected(false);

			// the physical topology must have been initialized because the
			// logical neighborhood was modified
			assert physicalTopology != null;
		}
	}

	/**
	 * Adds a directed edge incrementally to the topology
	 */
	public void addEdge(MacAddress source, MacAddress target) {

		this.markNeighborsOutdated(source, true);

		initilizePhysicalTopologyOnce();

		// remove target from neighbor filter
		getFilteredNeighbors(source).remove(target);

		// make sure that the internal data structures are updated
		RangedLink link = getLinkBetween(source, target);
		updateOutdatedLink(link);

		updateNeighborCache(source);
	}

	/**
	 * Removes a directed edge incrementally from the topology
	 */
	public void removeEdge(MacAddress source, MacAddress target) {

		this.markNeighborsOutdated(source, true);

		initilizePhysicalTopologyOnce();

		// add target to neighbor filter
		Set<MacAddress> filteredNeighbors = getFilteredNeighbors(source);
		filteredNeighbors.add(target);

		assert getFilteredNeighbors(source).contains(target);

		// make sure that the internal data structures are updated
		RangedLink link = getLinkBetween(source, target);
		updateOutdatedLink(link);

		updateNeighborCache(source);

		assert !link.isConnected();
	}

	/**
	 * {@link MacAddress} for the given {@link Node}.
	 *
	 * In all topologies of this view, the nodes are the same.
	 *
	 * @param node
	 * @return
	 */
	public MacAddress getMacAddressForNode(INode node) {
		final MacAddress macAddress = nodeToMacAddress.get(node);
		if (macAddress == null)
			throw new IllegalArgumentException("No MAC address for " + node);
		return macAddress;
	}

	public MacAddress getMacAddressForNode(INodeID nodeID) {
		final INode nodeWithId = nodeToMacAddress.keySet().stream()
				.filter(n -> n.getId().equals(nodeID)).findAny().orElse(null);
		if (nodeWithId == null)
			throw new IllegalArgumentException("No node with ID " + nodeID
					+ " known. Known nodes: " + nodeToMacAddress.toString());
		return nodeToMacAddress.get(nodeWithId);
	}

	/**
	 * Returns the {@link INode} that maps to the given {@link MacAddress}
	 */
	public INode getNodeForMacAddress(MacAddress macAddress) {
		final INode node = macAddressToNode.get(macAddress);
		if (node == null)
			throw new IllegalArgumentException("No node for " + macAddress);
		return node;
	}

	private void createNodeToMacAddressMapping(INode node,
			MacAddress macAddress) {
		nodeToMacAddress.put(node, macAddress);
		macAddressToNode.put(macAddress, node);
	}

	/**
	 * Returns true if the link is filtered by the logical view. This does _not_
	 * imply that the link would be connected otherwise.
	 */
	private boolean isFiltered(RangedLink link) {
		return getFilteredNeighbors(link.getSource())
				.contains(link.getDestination());
	}

	private Set<MacAddress> getFilteredNeighbors(MacAddress source) {
		if (this.logicalNeighborFilter == null)
			return new LinkedHashSet<>();

		if (!logicalNeighborFilter.containsKey(source))
			logicalNeighborFilter.put(source, new LinkedHashSet<MacAddress>());

		return logicalNeighborFilter.get(source);
	}

	// when called the first time, this method initializes the physical topology
	// data structure with the initial topology. Afterwards, it is updated when
	// the link connectivity changes
	private void initilizePhysicalTopologyOnce() {

		if (physicalTopology == null) {

			// the logical neighborhood must not have been configured before the
			// physical topology was extracted
			assert logicalNeighborFilter == null;

			logicalNeighborFilter = new LinkedHashMap<>();

			// we mustn't set physicalTopology variable here because this would
			// change behavior of updateOutdatedLink(...)
			LinkedHashMap<MacAddress, Set<MacAddress>> tempPhysicalTopology = new LinkedHashMap<>();

			// set physical topology to the current Wifi topology (which is
			// still unfiltered!). From now on, updatedOutdatedLink(..) will
			// adapt the physicalTopology to link changes
			for (MacAddress macAddress : allMacAddresses) {
				LinkedHashSet<MacAddress> neighbors = new LinkedHashSet<>(
						getNeighbors(macAddress));
				tempPhysicalTopology.put(macAddress, neighbors);
			}

			physicalTopology = new LinkedHashMap<>(tempPhysicalTopology);
		}
	}

	private void updateNeighborCache(MacAddress source) {
		// explicitely change neighborhood relation because
		// getCachedNeighbors(...) does not update its cache often enough. call
		// getNeighbors() first in order
		// to resolve outdated relations after movement. this is necessary,
		// because
		// setCachedNeighborhood() removes all outdated-flags
		getNeighbors(source);
		Set<MacAddress> resultingNeighbors = new LinkedHashSet<>(
				getNeighbors(source));
		resultingNeighbors.removeAll(getFilteredNeighbors(source));
		setCachedNeighborhood(source, new ArrayList<>(resultingNeighbors));
	}

	/**
	 * Per-host access to the "Global Knowledge" UDG topology (i.e., an
	 * unmodified view on the WiFi topology)
	 *
	 * @author Bjoern Richerzhagen
	 * @version 1.0, Aug 16, 2016
	 */
	public class LogicalWiFiTopology implements TopologyProvider,
			AdaptableTopologyProvider, ObservableTopologyProvider {

		private final List<TopologyID> topoIds = Arrays.asList(UDG_TOPOLOGY_ID,
				LOGICAL_TOPOLOGY_ID);

		private final SimHost host;

		private final List<TopologyObserver> topologyObservers = new LinkedList<>();

		public LogicalWiFiTopology(SimHost host) {
			if (udgGraphView == null && adaptedGraphView == null) {
				udgGraphView = new LocalGraphView(NetInterfaceName.WIFI, false,
						LogicalWifiTopologyView.this);
				adaptedGraphView = new LocalGraphView(NetInterfaceName.WIFI,
						false);
			}
			this.host = host;
		}

		@Override
		public void initialize() {
			//
		}

		@Override
		public void shutdown() {
			//
		}

		@Override
		public Host getHost() {
			return host;
		}

		@Override
		public INode getNode(TopologyID identifier) {
			if (identifier.equals(UDG_TOPOLOGY_ID)) {
				return udgGraphView.getOwnNode(host);
			} else if (identifier.equals(LOGICAL_TOPOLOGY_ID)) {
				return adaptedGraphView.getOwnNode(host);
			}
			throw new AssertionError();
		}

		@Override
		public Set<IEdge> getNeighbors(TopologyID topologyIdentifier) {
			if (topologyIdentifier.equals(UDG_TOPOLOGY_ID)) {
				return udgGraphView.getNeighbors(host);
			} else if (topologyIdentifier.equals(LOGICAL_TOPOLOGY_ID)) {
				return adaptedGraphView.getNeighbors(host);
			}
			throw new AssertionError();
		}

		@Override
		public Graph getLocalView(TopologyID topologyIdentifier) {
			if (topologyIdentifier.equals(UDG_TOPOLOGY_ID)) {
				return udgGraphView.getLocalView();
			} else if (topologyIdentifier.equals(LOGICAL_TOPOLOGY_ID)) {
				return adaptedGraphView.getLocalView();
			}
			throw new AssertionError();
		}

		@Override
		public Iterable<TopologyID> getTopologyIdentifiers() {
			return topoIds;
		}

		@Override
		public void addNeighbor(TopologyID topologyIdentifier,
				INode otherNode) {
			if (!topologyIdentifier.equals(LOGICAL_TOPOLOGY_ID)) {
				throw new AssertionError();
			}
			MacAddress thisMac = getMacAddressForNode(
					getNode(topologyIdentifier));
			MacAddress otherMac = getMacAddressForNode(otherNode);
			addEdge(thisMac, otherMac);
		}

		@Override
		public void removeNeighbor(TopologyID topologyIdentifier,
				INode otherNode) {
			if (!topologyIdentifier.equals(LOGICAL_TOPOLOGY_ID)) {
				throw new AssertionError();
			}
			MacAddress thisMac = getMacAddressForNode(
					getNode(topologyIdentifier));
			MacAddress otherMac = getMacAddressForNode(otherNode);
			removeEdge(thisMac, otherMac);
		}

		@Override
		public Collection<OperationalEdge> getPossibleEdgeOperations(
				TopologyID topologyIdentifier) {
			if (!topologyIdentifier.equals(LOGICAL_TOPOLOGY_ID)) {
				throw new AssertionError();
			}
			final LinkedList<OperationalEdge> possibleOperation = new LinkedList<>();

			// can remove each incident edge in logical topology
			for (final IEdge neighbor : getNeighbors(topologyIdentifier)) {
				final OperationalEdge op = new OperationalEdge(neighbor,
						EdgeOperationType.Remove);
				possibleOperation.add(op);
			}

			// can add edges that are physical but currently not logical
			final Set<IEdge> physicalNeighbors = getNeighbors(UDG_TOPOLOGY_ID);
			for (final IEdge directedEdge : physicalNeighbors) {
				if (!hasNeighbor(topologyIdentifier, directedEdge.toId())) {
					final OperationalEdge op = new OperationalEdge(directedEdge,
							EdgeOperationType.Add);
					possibleOperation.add(op);
				}
			}

			return possibleOperation;
		}

		private boolean hasNeighbor(final TopologyID topologyIdentifier,
				final INodeID nodeID) {
			for (final IEdge neighbor : getNeighbors(topologyIdentifier)) {
				if (neighbor.toId().equals(nodeID)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void performOperation(TopologyID topologyIdentifier,
				OperationalEdge edgeOperation) {
			if (!topologyIdentifier.equals(LOGICAL_TOPOLOGY_ID)) {
				throw new AssertionError();
			}

			if (!edgeOperation.fromId()
					.equals(this.getNode(topologyIdentifier).getId())) {
				throw new IllegalArgumentException();
			}

			if (edgeOperation.getType() == EdgeOperationType.Add) {
				this.addNeighbor(topologyIdentifier,
						new Node(edgeOperation.toId()));
			} else { // Remove
				this.removeNeighbor(topologyIdentifier,
						new Node(edgeOperation.toId()));
			}
		}

		@Override
		public void addTopologyObserver(TopologyObserver observer) {
			topologyObservers.add(observer);
		}

	}
}