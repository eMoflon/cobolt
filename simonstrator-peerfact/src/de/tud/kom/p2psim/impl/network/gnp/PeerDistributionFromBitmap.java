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


package de.tud.kom.p2psim.impl.network.gnp;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;

import de.tudarmstadt.maki.simonstrator.api.Randoms;

/**
 * Assigns peers to x,y coordinates accoring to the grey colors of a 8bit
 * bitmap. PeerDistributionFromBitmap must be initialized first. Then,
 * getNextPeerLocation returns a peers location accoring to the ditribution
 * based on the bitmap.
 * 
 * @author Andre Mink, Sebastian Kaune
 */
public class PeerDistributionFromBitmap {
	private static PeerDistributionFromBitmap instance;

	private int mapWidth;

	private int mapHeight;

	private boolean initialized;

	private ArrayList map;

	private Random rnd = Randoms.getRandom(PeerDistributionFromBitmap.class);

	public static PeerDistributionFromBitmap getInstance() {
		if (instance == null)
			instance = new PeerDistributionFromBitmap();
		return instance;
	}

	/**
	 * Sets the path to the bitmap and the number of peers of this experiment
	 * 
	 * @param pathToBitmap
	 *            path to the bitmap
	 * @param numberOfPeers
	 *            number of peers of this experiment
	 */
	public void initialize(String pathToBitmap, int numberOfPeers) {
		BitmapLoader bl = new BitmapLoader(pathToBitmap);

		map = bl.assignPeers(numberOfPeers, bl.getDistributionFromBitmap());

		mapWidth = bl.width;
		mapHeight = bl.height;

		initialized = true;
		instance = this;
	}

	/**
	 * Returns the next x,y coordinate of a peer based on the bitmap
	 * 
	 * @return the next x,y coordinate of a peer
	 */
	public Point2D.Double getNextPeerLocation() {
		Point p = instance.getNextLocation();
		double x = -1;
		double y = -1;
		while (x < 0.0 || x > mapWidth || y < 0.0 || y > mapHeight) {
			x = p.x - rnd.nextDouble() + 0.5;
			y = p.y - rnd.nextDouble() + 0.5;
		}
		return new Point2D.Double(x, y);
	}

	/**
	 * Returns the status
	 * 
	 * @return <code>true</code> if PeerDistributionFromBitmap has been
	 *         initilaized, <code>false</code> otherwise
	 */
	public boolean isInitialized() {
		return initialized;
	}

	public PeerDistributionFromBitmap() {
		this.map = new ArrayList();
		this.mapHeight = 0;
		this.mapWidth = 0;
		this.initialized = false;
	}

	private Point getNextLocation() {
		int i = rnd.nextInt(map.size());
		return (Point) map.remove(i);
	}

}
