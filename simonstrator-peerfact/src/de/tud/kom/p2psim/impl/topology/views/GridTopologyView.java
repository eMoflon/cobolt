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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import de.tud.kom.p2psim.api.linklayer.mac.Link;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.MacLayer;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.topology.obstacles.ObstacleModel;
import de.tud.kom.p2psim.api.topology.waypoints.WaypointModel;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * This topology builds a grid of hosts (each host has a maximum of 4 neighbors)
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 06.03.2012
 */
public class GridTopologyView extends AbstractTopologyView<DefaultLink> {

	/*
	 * A 2D-Array of MacAddresses, used to build the caching-Maps
	 */
	private ArrayList<ArrayList<MacAddress>> matrix = new ArrayList<ArrayList<MacAddress>>();

	private int actx = 0;
	
	private int acty = 0;

	/*
	 * A LOT of caching maps for faster access - it has proven to be a good idea
	 * to consume a bit more memory rather than to perform calculations every
	 * time a Link is requested
	 */

	private Map<MacAddress, Integer> xcoords = new HashMap<MacAddress, Integer>();

	private Map<MacAddress, Integer> ycoords = new HashMap<MacAddress, Integer>();

	private Map<MacAddress, MacAddress> top = new HashMap<MacAddress, MacAddress>();

	private Map<MacAddress, MacAddress> right = new HashMap<MacAddress, MacAddress>();

	private Map<MacAddress, MacAddress> bottom = new HashMap<MacAddress, MacAddress>();

	private Map<MacAddress, MacAddress> left = new HashMap<MacAddress, MacAddress>();

	private Map<MacAddress, List<MacAddress>> initialNeighbors = new HashMap<MacAddress, List<MacAddress>>();

	public GridTopologyView(PhyType phy) {
		super(phy);
	}

	@XMLConfigurableConstructor({ "phy" })
	public GridTopologyView(String phy) {
		this(PhyType.WIFI);
		setPhy(phy);
	}


	@Override
	protected void addedMac(MacLayer mac) {
		if (matrix.size() == actx && acty == 0) {
			// first in new column
			matrix.add(new ArrayList<MacAddress>());
		}

		addMacToMatrix(actx, acty, mac.getMacAddress());

		if (acty + 1 == matrix.size()) {
			// start adding a new row in first column
			actx = 0;
		}

		if (acty == matrix.size()) {
			// switch to next column in same row
			actx++;
			if (actx == matrix.size()) {
				// begin a new column in first row
				acty = 0;
			}
		} else {
			acty++;
		}
	}

	/**
	 * Add a Mac and update all neighbors
	 * 
	 * @param x
	 * @param y
	 * @param macAddr
	 */
	private void addMacToMatrix(int x, int y, MacAddress macAddr) {
		matrix.get(x).add(macAddr);
		initialNeighbors.put(macAddr, new Vector<MacAddress>());
		xcoords.put(macAddr, x);
		ycoords.put(macAddr, y);

		// Has Top-Neighbor
		if (acty > 0) {
			top.put(macAddr, matrix.get(x).get(y - 1));
			bottom.put(matrix.get(x).get(y - 1), macAddr);
			initialNeighbors.get(macAddr).add(matrix.get(x).get(y - 1));
			initialNeighbors.get(matrix.get(x).get(y - 1)).add(macAddr);
		}
		// Has Left-Neighbor
		if (actx > 0) {
			left.put(macAddr, matrix.get(x - 1).get(y));
			right.put(matrix.get(x - 1).get(y), macAddr);
			initialNeighbors.get(macAddr).add(matrix.get(x - 1).get(y));
			initialNeighbors.get(matrix.get(x - 1).get(y)).add(macAddr);
		}
	}

	@Override
	public Link getBestNextLink(MacAddress source, MacAddress lastHop,
			MacAddress currentHop, MacAddress destination) {
		int srcx = xcoords.get(currentHop);
		int srcy = ycoords.get(currentHop);
		int dstx = xcoords.get(destination);
		int dsty = ycoords.get(destination);

		boolean horizontal;
		if (srcx == dstx) {
			horizontal = false;
		} else if (srcy == dsty) {
			horizontal = true;
		} else {
			horizontal = Randoms.getRandom(GridTopologyView.class)
					.nextBoolean();
		}

		do {
			if (horizontal && srcx < dstx) {
				// right
				if (right.containsKey(currentHop)) {
					return getLinkBetween(currentHop, right.get(currentHop));
				} else {
					horizontal = false;
				}
			}
			if (horizontal && srcx > dstx) {
				// left
				return getLinkBetween(currentHop, left.get(currentHop));
			}
			if (!horizontal && srcy < dsty) {
				// bottom
				if (bottom.containsKey(currentHop)) {
					return getLinkBetween(currentHop, bottom.get(currentHop));
				} else {
					horizontal = true;
				}
			}
			if (!horizontal && srcy > dsty) {
				// top
				return getLinkBetween(currentHop, top.get(currentHop));
			}
			/*
			 * This stops after 2 iterations!
			 */
		} while (true);
	}

	@Override
	protected void updateOutdatedLink(DefaultLink link) {
		/*
		 * nothing to do, as this will never be called
		 */
	}

	@Override
	protected List<MacAddress> updateNeighborhood(MacAddress source) {
		/*
		 * called once for every source, therefore we free some memory
		 */
		return initialNeighbors.remove(source);
	}

	@Override
	protected DefaultLink createLink(MacAddress source, MacAddress destination) {
		boolean connected = true;
		if (!getNeighbors(source).contains(destination)
				|| !getNeighbors(destination).contains(source)) {
			connected = false;
		}
		return new DefaultLink(source, destination, connected,
				determineLinkDropProbability(source, destination),
				determineLinkBandwidth(source, destination),
				determineLinkLatency(source, destination), getPhyType()
						.getDefaultMTU());
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
