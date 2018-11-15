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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.tud.kom.p2psim.api.linklayer.LinkLayer;
import de.tud.kom.p2psim.api.linklayer.mac.Link;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.MacLayer;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.network.BandwidthImpl;
import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.api.topology.TopologyComponent;
import de.tud.kom.p2psim.api.topology.views.DropProbabilityDeterminator;
import de.tud.kom.p2psim.api.topology.views.LatencyDeterminator;
import de.tud.kom.p2psim.api.topology.views.TopologyView;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Rate;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;

/**
 * To ease implementation of new {@link TopologyView}s, this class provides
 * common methods for all topologies. It can be configured to support movement
 * and/or obstacles and will then provide a lot more convenient caching
 * functionalities.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 06.03.2012
 */
public abstract class AbstractTopologyView<L extends DefaultLink> implements
		TopologyView {

	private PhyType phy;

	/**
	 * An object that provides the latencies for each link.
	 */
	private LatencyDeterminator latencyDeterminator;

	/**
	 * An object that provides the drop rates for a link.
	 */
	private DropProbabilityDeterminator dropProbabilityDeterminator;

	/**
	 * All registered MACs
	 */
	private Map<MacAddress, MacLayer> macs = new HashMap<MacAddress, MacLayer>();

	/**
	 * This is used to cache all Link-Objects. Rather than destroying and
	 * recreating the Link-Object, its properties should be modified if the
	 * topology changes.
	 */
	private Map<MacAddress, Map<MacAddress, L>> linkCache = new HashMap<MacAddress, Map<MacAddress, L>>();

	/**
	 * A cache for the neighbors of a mac (unification of TX and RX)
	 */
	private Map<MacAddress, List<MacAddress>> neighborsCache = new HashMap<MacAddress, List<MacAddress>>();

	/**
	 * A marker for outdated neighborhoods (only used on movementSupported
	 * topologies)
	 */
	private Map<MacAddress, Boolean> neighborsOutdated;

	/**
	 * An access-list for the Positions of hosts (the objects will be updated by
	 * the MovementModels "automagically")
	 */
	private Map<MacAddress, PositionVector> positions = new HashMap<MacAddress, PositionVector>();

	/**
	 * Indicating that this View supports movement. This will enable some
	 * additional caching mechanisms that would be useless if movement is not
	 * used.
	 */
	private boolean movementSupported = false;

	/**
	 * Indicating, that this view is targeted towards the simulations of real
	 * link layers. See {@link TopologyView}.
	 */
	private boolean hasRealLinkLayer = false;

	/**
	 * Basic TopologyView, does not support movement
	 * 
	 * @param phy
	 */
	public AbstractTopologyView(PhyType phy) {
		this(phy, false);
	}

	/**
	 * A TopologyView that can be used to support movement/obstacles
	 * 
	 * @param phy
	 * @param movementSupported
	 * @param obstaclesSupported
	 */
	public AbstractTopologyView(PhyType phy, boolean movementSupported) {
		this.phy = phy;
		this.movementSupported = movementSupported;
		if (movementSupported) {
			neighborsOutdated = new HashMap<MacAddress, Boolean>();
		}
	}

	/**
	 * For the XML-configurable versions
	 * 
	 * @param phy
	 */
	public void setPhy(String phy) {
		phy = phy.toUpperCase();
		try {
			this.phy = PhyType.valueOf(phy);
		} catch (IllegalArgumentException e) {
			throw new ConfigurationException("The PHY " + phy
					+ " is unknown. Please select one of "
					+ PhyType.printTypes());
		}
		if (this.phy == null) {
			throw new ConfigurationException("The PHY " + phy
					+ " is unknown. Please select one of "
					+ PhyType.printTypes());
		}
	}

	@Override
	public final PhyType getPhyType() {
		return phy;
	}

	/**
	 * This object determines the latency on a link. If it is not set, the
	 * default value defined by the PHY will be used.
	 * 
	 * @param latencyDeterminator
	 */
	public void setLatency(LatencyDeterminator latencyDeterminator) {
		this.latencyDeterminator = latencyDeterminator;
	}

	/**
	 * This object determines the drop rate (packet loss) on a link. If it is
	 * not set, the default value defined by the PHY will be used.
	 * 
	 * @param dropRateDeterminator
	 */
	public void setDropRate(DropProbabilityDeterminator dropRateDeterminator) {
		this.dropProbabilityDeterminator = dropRateDeterminator;
	}
	
	/**
	 * Access the {@link LatencyDeterminator} of this View. If no
	 * {@link LatencyDeterminator} is configured, this will return the latency
	 * of the PHY.
	 * 
	 * @return
	 */
	protected LatencyDeterminator getLatencyDeterminator() {
		if (latencyDeterminator == null) {
			latencyDeterminator = new LatencyDeterminator() {

				@Override
				public void onMacAdded(MacLayer mac, TopologyView viewParent) {
					//
				}

				@Override
				public long getLatency(TopologyView view, MacAddress source,
						MacAddress destination, Link link) {
					return getPhyType().getDefaultLatency();
				}
			};
		}
		return latencyDeterminator;
	}

	/**
	 * Access the {@link LatencyDeterminator} of this View. If no
	 * {@link LatencyDeterminator} is configured, this will return the latency
	 * of the PHY.
	 * 
	 * @return
	 */
	protected DropProbabilityDeterminator getDropProbabilityDeterminator() {
		if (dropProbabilityDeterminator == null) {
			dropProbabilityDeterminator = new DropProbabilityDeterminator() {

				@Override
				public void onMacAdded(MacLayer mac, TopologyView viewParent) {
					//
				}

				@Override
				public double getDropProbability(TopologyView view,
						MacAddress source, MacAddress destination, Link link) {
					return getPhyType().getDefaultDropProbability();
				}
			};
		}
		return dropProbabilityDeterminator;
	}

	/**
	 * Default Bandwidth determination for a link: the minimum of the sources
	 * uplink and the destinations downlink.
	 * 
	 * @param source
	 * @param destination
	 * @return bandwidth in bit/s {@link Rate}
	 */
	protected long determineLinkBandwidth(MacAddress source,
			MacAddress destination) {
		BandwidthImpl sourceBandwidth = getMac(source).getMaxBandwidth();
		BandwidthImpl destinationBandwidth = getMac(destination).getMaxBandwidth();
		return Math.min(sourceBandwidth.getUpBW(),
				destinationBandwidth.getDownBW());
	}

	/**
	 * Latency determination for a link. This default implementation just asks
	 * the {@link LatencyDeterminator}.
	 * 
	 * @param source
	 * @param destination
	 * @return
	 */
	protected long determineLinkLatency(MacAddress source,
			MacAddress destination) {
		return getLatencyDeterminator().getLatency(this, source, destination,
				null);
	}

	/**
	 * Probability that a message is lost on this link.
	 * 
	 * @param source
	 * @param destination
	 * @return
	 */
	protected double determineLinkDropProbability(MacAddress source,
			MacAddress destination) {
		return dropProbabilityDeterminator.getDropProbability(this, source,
				destination, null);
	}

	@Override
	public final void addedComponent(TopologyComponent comp) {
		LinkLayer ll = comp.getHost().getLinkLayer();
		
		if( ll == null ) {
			/* No linklayer specified in config. */
			Monitor.log(AbstractTopologyView.class, Level.WARN,
					"No LinkLayer specified. Cannot add Topology.");
			return;
		}
		
		if (ll.hasPhy(phy)) {
			/*
			 * Collect all hosts that are part of this View
			 */
			MacLayer mac = comp.getHost().getLinkLayer().getMac(phy);
			macs.put(mac.getMacAddress(), mac);
			addedMac(mac);
			/*
			 * Initialize all cache-maps
			 */
			linkCache.put(mac.getMacAddress(), new HashMap<MacAddress, L>());
			if (movementSupported) {
				neighborsOutdated.put(mac.getMacAddress(), true);
			}
			positions.put(mac.getMacAddress(), comp.getHost()
					.getTopologyComponent().getRealPosition());
			getLatencyDeterminator().onMacAdded(mac, this);
			getDropProbabilityDeterminator().onMacAdded(mac, this);
		}
	}

	@Override
	public MacLayer getMac(MacAddress address) {
		return macs.get(address);
	}
	
	@Override
	public Collection<MacLayer> getAllMacs() {
		return macs.values();
	}
	
	long timeLastMovement = 0;
	
	@Override
	public void onLocationChanged(Host host, Location location) {
		if (Time.getCurrentTime() != timeLastMovement) {
			timeLastMovement = Time.getCurrentTime();
			/*
			 * again, topologies might or might not support movement. We do not
			 * force handling of this callback. The default implementation does
			 * nothing. If a topology uses this callback is should mark
			 * neighborhoods as outdated and re-calculate them on-demand as soon as
			 * the hosts first requests the neighborhood again.
			 */
			if (movementSupported) {
				/*
				 * mark all neighborhoods as outdated
				 */
				for (Entry<MacAddress, Boolean> entry : neighborsOutdated
						.entrySet()) {
					entry.setValue(true);
				}
			}
		}
	}

	@Override
	public final L getLinkBetween(MacAddress source, MacAddress destination) {
		return getCachedLink(source, destination);
	}

	@Override
	public final List<MacAddress> getNeighbors(MacAddress address) {
		return Collections.unmodifiableList(getCachedNeighborhood(address));
	}

	/*
	 * Methods to be implemented by Views
	 */

	/**
	 * This is called as soon as a new component is added to the Topology to
	 * allow the extending view to perform more advanced caching such as
	 * pre-calculation of neighborhoods and links in a fixed TopologyView.
	 * 
	 * @param mac
	 */
	protected abstract void addedMac(MacLayer mac);

	/**
	 * Called if a Link is outdated and was requested from cache. After this
	 * call the <i>outdated</i>-Flag will be set to false by the
	 * {@link AbstractTopologyView}. This will only be called if the View is
	 * movementSupported!
	 * 
	 * @param link
	 */
	protected abstract void updateOutdatedLink(L link);

	/**
	 * Called, if a Link is requested that has not been added to the cache
	 * already. You have to return a Link extending {@link DefaultLink}. If your
	 * View is movementSupported, updateOutdatedLink() will be called right
	 * after this method. <b>ALWAYS</b> return a valid Link-Object. If two hosts
	 * are not connected, you have to make sure that Link.isConnected() returns
	 * false.
	 * 
	 * @param source
	 * @param destination
	 * @return
	 */
	protected abstract L createLink(MacAddress source, MacAddress destination);

	/**
	 * Calculate an updated Neighborhood for the provided Source. This is called
	 * if no Neighborhood for a node is found in the cache or if your view is
	 * movementSupported and the neighborhood is outdated due to movement.
	 * 
	 * @param neighborhood
	 */
	protected abstract List<MacAddress> updateNeighborhood(MacAddress source);

	/*
	 * Caching-Methods
	 */

	/**
	 * Get a previously cached Link-Object. Rather than creating new objects you
	 * should update the properties of the cached object instead to speed up
	 * simulation. If there is no Link in the Cache, createLink is called and
	 * the newly created link is added to the cache and returned
	 * 
	 * @param source
	 * @param destination
	 * @return the Link
	 */
	private L getCachedLink(MacAddress source, MacAddress destination) {
		L link = linkCache.get(source).get(destination);
		if (link == null) {
			link = createLink(source, destination);
			assert link != null && source != null && destination != null : "Error!";
			linkCache.get(source).put(destination, link);
			if (movementSupported) {
				// mark as outdated, in order to trigger updateLink next!
				// linksOutdated.put(link, true);
				link.setOutdated(true);
			}
		}
		if (movementSupported && link.isOutdated()) {
			updateOutdatedLink(link);
			link.setOutdated(false);
		}
		return link;
	}

	/**
	 * Return the cached neighborhood for the source. This will call
	 * updateNeighborhood if there is no neighborhood in the cache or the
	 * neighborhood is outdated after a movement-operation.
	 * 
	 * @param source
	 * @return
	 */
	private List<MacAddress> getCachedNeighborhood(MacAddress source) {
		if (movementSupported && neighborsOutdated.get(source)
				|| !neighborsCache.containsKey(source)) {
			neighborsCache.put(source, updateNeighborhood(source));
			if (movementSupported) {
				neighborsOutdated.put(source, false);
			}
		}
		return neighborsCache.get(source);
	}

	/**
	 * If your topology is able to calculate neighborhoods in advance (ie. in a
	 * fixed topology) you might want to use this method to fill the cache
	 * rather than rely on the updateNeighborhood-call for on-demand
	 * calculation.
	 * 
	 * @param source
	 * @param neighbors
	 */
	protected void setCachedNeighborhood(MacAddress source,
			List<MacAddress> neighbors) {
		neighborsCache.put(source, neighbors);
		if (movementSupported) {
			neighborsOutdated.put(source, false);
		}
	}

	/**
	 * Not really a cache but a hashMap-Lookup for the real position object
	 * 
	 * @param source
	 * @return
	 */
	protected PositionVector getCachedPosition(MacAddress source) {
		return positions.get(source);
	}
	
	protected void markNeighborsOutdated(final MacAddress address, final boolean isOutdated) {
		final boolean currentOutdatedState = this.neighborsOutdated.containsKey(address) ? this.neighborsOutdated.get(address) : false;
		this.neighborsOutdated.put(address, currentOutdatedState || isOutdated);
	}

	@Override
	public Location getPosition(MacAddress address) {
		return getCachedPosition(address);
	}

	@Override
	public double getDistance(MacAddress addressA, MacAddress addressB) {
		return getCachedPosition(addressA)
				.distanceTo(getCachedPosition(addressB));
	}

	@Override
	public boolean hasRealLinkLayer() {
		return hasRealLinkLayer;
	}

	/**
	 * Mark that this {@link TopologyView} has a real link layer (latencies and
	 * drop rates are Layer 2 measurements!)
	 * 
	 * @param hasRealLinkLayer
	 */
	public void setHasRealLinkLayer(boolean hasRealLinkLayer) {
		this.hasRealLinkLayer = hasRealLinkLayer;
	}
}
