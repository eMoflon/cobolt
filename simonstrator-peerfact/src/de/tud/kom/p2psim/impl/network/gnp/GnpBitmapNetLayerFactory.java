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

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.network.BandwidthImpl;
import de.tud.kom.p2psim.impl.network.IPv4NetID;
import de.tud.kom.p2psim.impl.network.gnp.topology.GnpPosition;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponentFactory;

public class GnpBitmapNetLayerFactory implements HostComponentFactory {

	private final GnpSubnet subnet;

	private final static long DEFAULT_DOWN_BANDWIDTH = 1000l;

	private final static long DEFAULT_UP_BANDWIDTH = 1000l;

	private long downBandwidth;

	private long upBandwidth;
	
	private int experimentSize = 0;
	
	private PeerDistributionFromBitmap peerDistribution;
	
	private Long idCounter = 0l;
	
	private Set<Long> usedIds = new HashSet<Long>();

	public GnpBitmapNetLayerFactory() {
		subnet = new GnpSubnet();
		this.downBandwidth = DEFAULT_DOWN_BANDWIDTH;
		this.upBandwidth = DEFAULT_UP_BANDWIDTH;
	}

	public GnpNetLayer createComponent(Host pureHost) {
		SimHost host = (SimHost) pureHost;
		GnpNetLayer netLayer = newNetLayer(host, host.getProperties()
				.getGroupID());
		return netLayer;
	}

	/**
	 * random node form group
	 * 
	 * @param id
	 * @return
	 */
	public GnpNetLayer newNetLayer(SimHost host, String id) {
		IPv4NetID netId = createNewID();
		return newNetLayer(host, netId);
	}

	/**
	 * Create new GnpNetLayer based on the distribution from the bitmap
	 * 
	 * @param netID
	 * @return
	 */
	private GnpNetLayer newNetLayer(SimHost host, IPv4NetID netID) {
		// Get next position from peer distribution
		Point2D.Double p = peerDistribution.getNextPeerLocation(); 
		
		// Create an instance of GnpPosition
		double[] coordinates = {p.x, p.y};
		GnpPosition gnpPos = new GnpPosition(coordinates);
		
		// Create stub GeoLocation
		GeoLocation geoLoc = new GeoLocation("","","","","",0,0);
		
		// Create the NetLayer
		GnpNetLayer nw = new GnpNetLayer(host, this.subnet, netID, gnpPos,
				geoLoc, new BandwidthImpl(this.downBandwidth, this.upBandwidth));

		return nw;
	}

	public void setDownBandwidth(long downBandwidth) {
		this.downBandwidth = downBandwidth;
	}

	public void setUpBandwidth(long upBandwidth) {
		this.upBandwidth = upBandwidth;
	}

	public void setLatencyModel(GnpLatencyModel model) {
		subnet.setLatencyModel(model);
	}
	
	public void setBandwidthManager(AbstractGnpNetBandwidthManager bm) {
		subnet.setBandwidthManager(bm);
	}

	public void setPbaPeriod(double seconds) {
		subnet.setPbaPeriod(Math.round(seconds * Time.SECOND));
	}
	
	public void setExperimentSize(int size) {
		this.experimentSize = size;
	}
	
	/**
	 * Setup PeerDistributionFromBitmap with image from given path
	 * 
	 * @param path
	 */
	public void setBitmapPath(String path) {
		this.peerDistribution = new PeerDistributionFromBitmap();
		
		/* Initialize the peer distribution with the path of the bitmap and 
		 * the number of peers.
		 */
		peerDistribution.initialize(path, this.experimentSize);
	}

	private class GnpHostInfo {

		private GnpPosition gnpPosition;

		private GeoLocation geoLoc;

		public GnpHostInfo(GeoLocation geoLoc, GnpPosition gnpPos) {
			this.gnpPosition = gnpPos;
			this.geoLoc = geoLoc;
		}

	}
	
	/** 
	 * @return a new NetID
	 */
	private IPv4NetID createNewID() {
		while (usedIds.contains(idCounter))
			idCounter++;
		IPv4NetID nextId = new IPv4NetID(idCounter);
		usedIds.add(idCounter++);
		return nextId;
	}

}
