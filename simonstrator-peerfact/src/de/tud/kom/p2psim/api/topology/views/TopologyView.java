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

package de.tud.kom.p2psim.api.topology.views;

import java.util.Collection;
import java.util.List;

import de.tud.kom.p2psim.api.linklayer.mac.Link;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.MacLayer;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.topology.TopologyListener;
import de.tudarmstadt.maki.simonstrator.api.component.GlobalComponent;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.LocationListener;

/**
 * Each MAC has a view on the global topology of hosts (ie. the
 * interconnectivity of the hosts in the given Medium). This provides an adapter
 * between the Topology-class and the MAC that needs to know the neighbors of a
 * Host. The View itself is instantiated in the Topology-Section, there has to
 * be a view for each PHY that is used, otherwise an Exception is thrown
 * 
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 21.02.2012
 */
public interface TopologyView extends TopologyListener, GlobalComponent, LocationListener {

	/**
	 * The {@link PhyType} this View represents
	 * 
	 * @return
	 */
	public PhyType getPhyType();

	/**
	 * Return a Link-Object to use for data transmission between a source and a
	 * destination. For performance-reasons you will always receive a valid link
	 * object, even if from the topologies perspective the link is not available
	 * anymore. Before sending a message via the link we therefore have to check
	 * if Link.isConnected() returns true.
	 * 
	 * If source == destionation isConnected() of the Link has to return false!
	 * 
	 * @param source
	 * @param destination
	 * @return A Link-Object
	 */
	public Link getLinkBetween(MacAddress source, MacAddress destination);

	/**
	 * For some topology views it might be much faster to just return the next
	 * hop rather than to calculate the whole path every time. Therefore, it is
	 * encouraged to use this method when operating with global knowledge in
	 * routing. The method should return null, if no path between source and
	 * destination is found, otherwise we would still send a message, even if it
	 * is never reaching the destination.
	 * 
	 * @param source
	 *            the originator of the NetMessage
	 * @param lastHop
	 *            the originator of the LinkMessage (last Hop)
	 * @param currentHop
	 *            the current node
	 * @param destination
	 *            the target of the NetMessage
	 * @return
	 */
	public Link getBestNextLink(MacAddress source, MacAddress lastHop,
			MacAddress currentHop, MacAddress destination);

	/**
	 * Returns the MAC-Layer with the given MacAddress
	 * 
	 * @param address
	 * @return
	 */
	public MacLayer getMac(MacAddress address);

	/**
	 * Returns a List of 1-hop-neighbors of the given {@link MacAddress} (this
	 * should only return neighbors to which we have a TX-connection). It does
	 * <b>NOT</b> imply that we are also in the neighborhood of all nodes in the
	 * returned list, as links do not need to be symmetric!
	 * 
	 * @param address
	 * @return an <b>unmodifiable</b> view on the current neighbors
	 */
	public List<MacAddress> getNeighbors(MacAddress address);
	
	/**
	 * Returns all MACs that are currently in the TopologyView.
	 * @return
	 */
	public Collection<MacLayer> getAllMacs();

	/**
	 * Gets the real Position of the host.
	 * 
	 * @param address
	 *            The {@link MacAddress} of the host
	 * @return The real Position of the Host.
	 */
	public Location getPosition(MacAddress address);

	/**
	 * Gets the real distance between the two hosts.
	 * 
	 * @param addressA
	 *            The first {@link MacAddress}
	 * @param addressB
	 *            The second {@link MacAddress}
	 * @return The real distance between the two hosts.
	 */
	public double getDistance(MacAddress addressA, MacAddress addressB);

	/**
	 * Denotes whether this {@link TopologyView} has a real link layer (i.e.,
	 * hosts within this view act as Layer 2 elements, connected by a layer 2
	 * link). In "Internet-wide" topology views, this is usually NOT the case.
	 * 
	 * @return
	 */
	public boolean hasRealLinkLayer();

}
