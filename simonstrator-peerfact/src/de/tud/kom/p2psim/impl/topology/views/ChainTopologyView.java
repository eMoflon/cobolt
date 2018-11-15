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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import de.tud.kom.p2psim.api.linklayer.mac.Link;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.MacLayer;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.topology.obstacles.ObstacleModel;
import de.tud.kom.p2psim.api.topology.waypoints.WaypointModel;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * This topology view assumes that all hosts are connected as one chain. It is
 * used to test routing algorithms in a well-defined environment.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 04.03.2012
 */
public class ChainTopologyView extends AbstractTopologyView<DefaultLink> {

	private LinkedList<MacAddress> chain;

	private Map<MacAddress, MacAddress> rightNeighbor;

	private Map<MacAddress, MacAddress> leftNeighbor;

	private Map<MacAddress, Integer> positionIndex;

	private Map<MacAddress, List<MacAddress>> directNeighbors;

	private int idx = 0;

	private MacAddress lastLeftNeighbor = null;

	public ChainTopologyView(PhyType phy) {
		super(phy);
		chain = new LinkedList<MacAddress>();
		positionIndex = new HashMap<MacAddress, Integer>();
		leftNeighbor = new HashMap<MacAddress, MacAddress>();
		rightNeighbor = new HashMap<MacAddress, MacAddress>();
		directNeighbors = new HashMap<MacAddress, List<MacAddress>>();
	}

	@XMLConfigurableConstructor({ "phy" })
	public ChainTopologyView(String phy) {
		this(PhyType.WIFI);
		setPhy(phy);
	}

	@Override
	protected void addedMac(MacLayer mac) {
		chain.add(mac.getMacAddress());
		positionIndex.put(mac.getMacAddress(), idx);
		leftNeighbor.put(mac.getMacAddress(), lastLeftNeighbor);
		directNeighbors.put(mac.getMacAddress(), new Vector<MacAddress>());

		if (lastLeftNeighbor != null) {
			rightNeighbor.put(lastLeftNeighbor, mac.getMacAddress());
			rightNeighbor.put(mac.getMacAddress(), null);
			directNeighbors.get(mac.getMacAddress()).add(lastLeftNeighbor);
			directNeighbors.get(lastLeftNeighbor).add(mac.getMacAddress());
		}
		lastLeftNeighbor = mac.getMacAddress();

		idx++;
	}

	@Override
	protected DefaultLink createLink(MacAddress source, MacAddress destination) {
		if (!getNeighbors(source).contains(destination)
				|| !getNeighbors(destination).contains(source)) {
			return new DefaultLink(source, destination, false,
					determineLinkDropProbability(source, destination),
					determineLinkBandwidth(source, destination),
					determineLinkLatency(source, destination), getPhyType()
							.getDefaultMTU());
		}
		return new DefaultLink(source, destination, true,
				determineLinkDropProbability(source, destination),
				determineLinkBandwidth(source, destination),
				determineLinkLatency(source, destination), getPhyType()
						.getDefaultMTU());
	}

	@Override
	protected List<MacAddress> updateNeighborhood(MacAddress source) {
		/*
		 * This is called once for every source, free memory
		 */
		return directNeighbors.remove(source);
	}

	@Override
	protected void updateOutdatedLink(DefaultLink link) {
		// this will not happen
	}

	@Override
	public Link getBestNextLink(MacAddress source, MacAddress lastHop,
			MacAddress currentHop, MacAddress destination) {
		int srcIdx = positionIndex.get(currentHop); // chain.indexOf(source);
		int destIdx = positionIndex.get(destination); // chain.indexOf(destination);
		if (srcIdx < destIdx) {
			return getLinkBetween(currentHop, rightNeighbor.get(currentHop));
		} else {
			return getLinkBetween(currentHop, leftNeighbor.get(currentHop));
		}
	}

	@Override
	public void changedWaypointModel(WaypointModel model) {
		//
	}

	@Override
	public void changedObstacleModel(ObstacleModel model) {
		//
	}

}
