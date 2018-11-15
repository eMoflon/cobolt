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

package de.tud.kom.p2psim.impl.topology;

import java.util.List;
import java.util.Set;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.MacLayer;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.topology.Topology;
import de.tud.kom.p2psim.api.topology.views.TopologyView;
import de.tudarmstadt.maki.simonstrator.api.Binder;
import de.tudarmstadt.maki.simonstrator.api.Graphs;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GenericGraphElementProperties;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetInterface;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetworkComponent.NetInterfaceName;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.LocationListener;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.LocationSensor;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSTypes;
import de.tudarmstadt.maki.simonstrator.api.component.transport.ConnectivityListener;

/**
 * This is calculated based on global knowledge. It only registers as
 * {@link LocationListener}, if a range is specified by the Provider.
 *
 * @author Bjoern Richerzhagen
 * @version 1.0, May 13, 2015
 */
public class LocalGraphView implements LocationListener, ConnectivityListener {

	/**
	 * Provider interface for neighbors, if not the default implementation is to
	 * be used.
	 *
	 * @author Bjoern Richerzhagen
	 * @version 1.0, Aug 16, 2016
	 */
	public static interface NeighborProvider {
		public List<MacAddress> getCustomNeighbors(MacAddress macAddress);
	}

	/**
	 * Marker: has there been any movement since the graph view was last
	 * requested? If so: recalculate! Otherwise, we ignore this object to not
	 * perform calculations if no one is interested...
	 */
	private final double distance;

	private final boolean isDistanceBased;

	private final NetInterfaceName medium;

	protected final TopologyView topoView;

	private final boolean onlyOnline;

	private Graph currentView;

	private boolean isInvalid = true;

	private final PhyType phy;

	private NeighborProvider neighborProvider = new NeighborProvider() {
		@Override
		public List<MacAddress> getCustomNeighbors(MacAddress macAddress) {
			return topoView.getNeighbors(macAddress);
		}
	};

	public LocalGraphView(NetInterfaceName medium, boolean onlyOnline) {
		this(medium, onlyOnline, -1);
	}

	/**
	 * Enables a custom neighbor provider
	 * @param medium
	 * @param onlyOnline
	 * @param neighborProvider
	 */
	public LocalGraphView(NetInterfaceName medium, boolean onlyOnline, NeighborProvider neighborProvider) {
		this(medium, onlyOnline, -1);
		this.neighborProvider = neighborProvider;
	}

	public LocalGraphView(NetInterfaceName medium, boolean onlyOnline,
			double distance) {
		this.medium = medium;
		PhyType localPhy = null;
		Topology topo = Binder.getComponentOrNull(Topology.class);
		for (PhyType currPhy : PhyType.values()) {
			if (currPhy.getNetInterfaceName() == medium
					&& topo.getTopologyView(currPhy) != null) {
				localPhy = currPhy;
				break;
			}
		}
		phy = localPhy;
		assert localPhy != null;
		this.topoView = topo.getTopologyView(localPhy);
		this.distance = distance;
		this.onlyOnline = onlyOnline;
		this.isDistanceBased = (distance > 0);
		assert !isDistanceBased || phy.isBroadcastMedium();
		if (phy.isBroadcastMedium()) {
			// register as listener for movement
			for (MacLayer mac : topoView.getAllMacs()) {
				try {
					mac.getHost().getComponent(LocationSensor.class)
							.requestLocationUpdates(null, this);
				} catch (ComponentNotAvailableException e) {
					throw new AssertionError("Expected a LocationSensor.");
				}
			}
		}
		// register as listener for online/offline events
		if (onlyOnline) {
			for (MacLayer mac : topoView.getAllMacs()) {
				mac.getHost().getNetworkComponent().getByName(medium)
						.addConnectivityListener(this);
			}
		}
	}

	private void recalculateLocalView() {
		if (!isInvalid) {
			/*
			 * Graphs are invalidated (i) based on movement, IFF a range was
			 * specified, (ii) based on online/offline events, IFF only online
			 * hosts are to be considered.
			 */
			return;
		}
		/*
		 * Calculate a complete global connectivity graph
		 */

		// Create new, empty graph
		currentView = Graphs.createGraph();

		// Add all (online?) nodes
		for (MacLayer mac : topoView.getAllMacs()) {
			if (!onlyOnline || mac.isOnline()) {
				final INode node = Graphs.createNode(mac.getHost().getId());
				node.setProperty(SiSTypes.PHY_LOCATION,
						topoView.getPosition(mac.getMacAddress()).clone());
				currentView.addElement(node);
			}
		}

		if (isDistanceBased) {
			// Build neighbors solely based on an assumed range
			for (MacLayer mac : topoView.getAllMacs()) {
				// Fix Christoph Storm:
				// Do not take offline nodes into account, unless told to do
				// so...
				if (onlyOnline
						&& !currentView.containsNode(mac.getHost().getId())) {
					continue;
				}
				// Consider all nodes as potential neighbors
				for (MacLayer neighborMac : topoView.getAllMacs()) {
					// create, but do NOT add the node object
					INode neighbor = Graphs.createNode(neighborMac.getHost().getId());
					// only online nodes (already in graph)
					if (!onlyOnline
							|| currentView.containsNode(neighbor.getId())) {
						// Distance?
						double nodeDistance = topoView.getDistance(
								mac.getMacAddress(),
								neighborMac.getMacAddress());
						if (nodeDistance <= distance) {
							IEdge edge = currentView.createAndAddEdge(
									mac.getHost().getId(),
									neighborMac.getHost().getId());
							currentView.addElement(edge);
							edge.setProperty(SiSTypes.PHY_DISTANCE,
									nodeDistance);
							// TODO get rid of WEIGHT here.
							edge.setProperty(
									GenericGraphElementProperties.WEIGHT,
									nodeDistance);
						}
					}
				}
			}
		} else {
			// Build neighborhoods based on underlay neighbors (1-hop)
			for (MacLayer mac : topoView.getAllMacs()) {
				// Fix Christoph Storm:
				// Do not take offline nodes into account, unless told to do
				// so...
				if (onlyOnline
						&& !currentView.containsNode(mac.getHost().getId())) {
					continue;
				}
				// Rely on underlay for neighbors
				List<MacAddress> neighbors = neighborProvider
						.getCustomNeighbors(mac.getMacAddress());
				for (MacAddress neighborMac : neighbors) {
					// create, but do NOT add the node object
					INode neighbor = currentView.createAndAddNode(
							topoView.getMac(neighborMac).getHost().getId());
					// only online nodes (already in graph)
					if (!onlyOnline
							|| currentView.containsNode(neighbor.getId())) {
						IEdge edge = currentView.createAndAddEdge(
								mac.getHost().getId(),
								topoView.getMac(neighborMac).getHost().getId());
						currentView.addElement(edge);
						double nodeDistance = topoView
								.getDistance(mac.getMacAddress(), neighborMac);
						edge.setProperty(SiSTypes.PHY_DISTANCE, nodeDistance);
						// TODO get rid of WEIGHT here.
						edge.setProperty(GenericGraphElementProperties.WEIGHT,
								nodeDistance);
					}
				}
			}
		}

		/*
		 * TODO if topology observers (delta-based) are registered, compute diff
		 * against old graph! Replaces checkForTopologyModifications() and
		 * sub-methods
		 */

		isInvalid = false;
	}

	/**
	 * Invalidate the current view (after movement and/or online/offline
	 * events).
	 */
	public void invalidate() {
		this.isInvalid = true;
	}

	public INode getOwnNode(SimHost ownHost) {
		if (currentView == null) {
			currentView = Graphs.createGraph();
		}
		return currentView.createAndAddNode(ownHost.getId());
	}

	public Set<IEdge> getNeighbors(SimHost ownHost) {
		recalculateLocalView();
		INode ownNode = getOwnNode(ownHost);
		return currentView.getOutgoingEdges(ownNode.getId());
	}

	/**
	 * This is the global view, therefore we do not distinguish between hosts.
	 *
	 * @return
	 */
	public Graph getLocalView() {
		recalculateLocalView();
		return currentView;
	}

	@Deprecated
	public PhyType getPhy() {
		/*
		 * Consider using NetInterfaceName instead.
		 */
		return phy;
	}

	@Override
	public void onLocationChanged(Host host, Location location) {
		this.isInvalid = true;
	}

	@Override
	public void wentOnline(Host host, NetInterface netInterface) {
		assert netInterface.getName() == medium;
		this.isInvalid = true;
	}

	@Override
	public void wentOffline(Host host, NetInterface netInterface) {
		assert netInterface.getName() == medium;
		this.isInvalid = true;
	}

	/*
	 * TODO add methods for DIFF_Calculation and allow registration of
	 * diff-observers.
	 */

}
