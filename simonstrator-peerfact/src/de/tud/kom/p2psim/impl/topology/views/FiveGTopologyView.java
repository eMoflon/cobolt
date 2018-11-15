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

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.tud.kom.p2psim.api.common.HostProperties;
import de.tud.kom.p2psim.api.linklayer.mac.Link;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.MacLayer;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.topology.obstacles.ObstacleModel;
import de.tud.kom.p2psim.api.topology.waypoints.WaypointModel;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.views.FiveGTopologyView.CellLink;
import de.tud.kom.p2psim.impl.topology.views.fiveg.FiveGTopologyDatabase;
import de.tud.kom.p2psim.impl.topology.views.fiveg.FiveGTopologyDatabase.Entry;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.handover.HandoverSensor;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * This topology view offers a topology for mobile apps - mobile clients
 * maintain one to one connections to one or multiple cloud servers or cloudlet
 * instances. This view abstracts from real antennas in that the actual range of
 * an antenna etc. is just modeled as a property of the link between mobile
 * client and the endpoint (cloud/cloudlet) based on a client's position. This
 * way, measurements such as the ones by Kaup et al. can be easily incorporated.
 * 
 * TODO add a property to enable usage of access points (lower latency, higher
 * BW at certain client positions around the configured positions of APs)
 * <code><Properties canUseAccessPoints="true" /></code>. NO, just make this
 * configurable via the topology view properties (e.g., a percentage of nodes
 * can use such APs) and THEN set the corresponding host property --> if overlay
 * code wants to access this information, we might need to add
 * {@link HostProperties} to the Simonstrator API.
 * 
 * TODO add distinction for cloudlets and cloud (assuming that cloudlets exhibit
 * lower latency), ideally with yet another (TM) host property on the base
 * station side: <code><Properties isCloudlet="true" /></code>. NO, again just
 * use the groupID of a host as a config setting for this view: e.g., all
 * specified groupIDs are Backend hosts vs. direct cloudlets.
 * 
 * FIXME OR: just base this configuration options on the group-ID in the host
 * builder?
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, Nov 4, 2015
 */
public class FiveGTopologyView extends AbstractTopologyView<CellLink> {

	private Set<MacAddress> cloudlets = new LinkedHashSet<>();

	private Set<MacAddress> clouds = new LinkedHashSet<>();

	private Set<MacAddress> mobileClients = new LinkedHashSet<>();

	/**
	 * A subset of mobileClients -> contains all clients that may use access
	 * points.
	 */
	private Set<MacAddress> mobileClientsUsingAccessPoints = new LinkedHashSet<>();

	private List<MacAddress> mobileClientsList = new LinkedList<>();

	private List<MacAddress> cloudsAndCloudletsList = new LinkedList<>();

	private FiveGTopologyDatabase database = null;

	private FiveGTopologyDatabase databaseAccessPoints = null;

	/**
	 * Configuration setting: all group IDs of nodes that act as cloudlets
	 * (e.g., act as basestations with a lower delay, whatever that means w.r.t.
	 * the specified model)
	 */
	private Set<String> groupIDsCloudlet = new LinkedHashSet<>();

	/**
	 * Configuration setting: all group IDs that act as cloud (e.g., backend
	 * servers). Here, the full latency of a connection from a mobile client to
	 * a network sever somewhere in the Internet is assumed (e.g., as measured
	 * by Android end user devices)
	 */
	private Set<String> groupIDsCloud = new LinkedHashSet<>();

	/**
	 * Configuration setting: all group IDs that act as clients but have access
	 * to access points.
	 */
	private Set<String> groupIDsAccessPointUsage = new LinkedHashSet<>();

	private List<HandoverSensor5G> handoverSensors = new LinkedList<>();

	/**
	 * 
	 * @param phy
	 */
	public FiveGTopologyView(PhyType phy) {
		super(phy, true);
		setHasRealLinkLayer(false);
		/*
		 * AP state is only set, after components moved. In scenarios without
		 * active movement, this will never happen. Therefore, we trigger the
		 * respective event ONCE at the start of the simulation
		 */
		Event.scheduleImmediately(new EventHandler() {
			@Override
			public void eventOccurred(Object content, int type) {
				checkAPAssociations();
			}
		}, null, 0);
	}

	@XMLConfigurableConstructor({ "phy" })
	public FiveGTopologyView(String phy) {
		// UMTS is reset with custom type next.
		this(PhyType.UMTS);
		setPhy(phy);
	}

	@Override
	public Link getBestNextLink(MacAddress source, MacAddress lastHop,
			MacAddress currentHop, MacAddress destination) {
		return getLinkBetween(source, destination);
	}

	@Override
	public void changedWaypointModel(WaypointModel model) {
		// I don't care, I love it.
	}

	@Override
	public void changedObstacleModel(ObstacleModel model) {
		// I don't care, I love it.
	}

	@Override
	protected void addedMac(MacLayer mac) {
		String groupId = mac.getHost().getProperties().getGroupID();
		if (groupIDsCloud.contains(groupId)) {
			clouds.add(mac.getMacAddress());
			cloudsAndCloudletsList.add(mac.getMacAddress());
		} else if (groupIDsCloudlet.contains(groupId)) {
			cloudlets.add(mac.getMacAddress());
			cloudsAndCloudletsList.add(mac.getMacAddress());
		} else {
			mobileClients.add(mac.getMacAddress());
			mobileClientsList.add(mac.getMacAddress());

			if (groupIDsAccessPointUsage.contains(groupId)) {
				assert databaseAccessPoints != null : "An AP Database is needed if AP-Functionality is desired.";
				mobileClientsUsingAccessPoints.add(mac.getMacAddress());
				HandoverSensor5G hs = new HandoverSensor5G(mac.getHost(),
						mac.getMacAddress());
				mac.getHost().registerComponent(hs);
				handoverSensors.add(hs);
			}
		}
	}

	@Override
	protected void updateOutdatedLink(CellLink link) {
		if (link.isInvalidLink()) {
			// cloud <-> cloud or client <-> client link
			return;
		}
		PositionVector pos = getCachedPosition(link.getMobileClient());

		int segId = database.getSegmentID(pos.getX(), pos.getY());
		int apSegId = -1;
		if (link.supportsAccessPoints()) {
			/*
			 * Check, if an AP-segment is available
			 */
			apSegId = databaseAccessPoints.getSegmentID(pos.getX(), pos.getY());
			if (link.getSegmentId() != segId
					|| link.getApSegmentId() != apSegId) {
				// Update
				link.setLinkData(database.getEntryFor(segId, link.isCloudlet()),
						databaseAccessPoints.getEntryFor(apSegId,
								link.isCloudlet()));
			}
		} else if (link.getSegmentId() != segId) {
			link.setLinkData(database.getEntryFor(segId, link.isCloudlet()),
					null);
		}
	}
	
	/**
	 * Check, if a node moved into a new segment. If so, we need to update
	 * the HandoverSensor to trigger the listeners. We only need to check
	 * nodes that are included in the list of ap-enabled nodes (i.e., they
	 * already have a HandoverSensor-instance).
	 */
	protected void checkAPAssociations() {
		for (HandoverSensor5G sensor : handoverSensors) {
			PositionVector pos = getCachedPosition(sensor.macAddr);
			int segId = databaseAccessPoints.getSegmentID(pos.getX(),
					pos.getY());
			if (segId != sensor.apSegmentId) {
				sensor.apSegmentId = segId;
				// Connected to AP, if segment is provided in the DB
				sensor.setConnectedToAccessPoint(
						databaseAccessPoints.getEntryFor(segId, false) != null);
			}
		}
		
		/*
		 * FIXME the not-so-elegant approach of updating all max-BWs
		 */
		for (MacAddress mobileClient : mobileClientsList) {
			updateMaxMacBandwidth(mobileClient);
		}
	}
	
	long lastMovementTimestamp = 0;
	
	@Override
	public void onLocationChanged(Host host, Location location) {
		super.onLocationChanged(host, location);
		if (lastMovementTimestamp != Time.getCurrentTime()) {
			lastMovementTimestamp = Time.getCurrentTime();
			checkAPAssociations();
		}
	}

	/**
	 * FIXME this is currently needed to keep the max upload bandwidth reported
	 * by MOBILE-mac (UMTS) consistent with the bandwidth of the given cell. It
	 * always reports the BW that can be achieved when sending via MOBILE to the
	 * cloud (NOT to cloudlets).
	 * 
	 * @param macAddr
	 */
	private void updateMaxMacBandwidth(MacAddress macAddr) {
		// FIXME workaround for MAC bandwidth updates.
		// Sets the initial max upload BW to the cloud (not to cloudlets!)
		MacLayer mac = getMac(macAddr);
		PositionVector pos = mac.getHost().getTopologyComponent()
				.getRealPosition();
		int segId = database.getSegmentID(pos.getX(), pos.getY());
		Entry usedEntry = database.getEntryFor(segId, false);
		if (mobileClientsUsingAccessPoints.contains(macAddr)) {
			int apSegId = databaseAccessPoints.getSegmentID(pos.getX(),
					pos.getY());
			Entry apEntry = databaseAccessPoints.getEntryFor(apSegId, false);
			usedEntry = (apEntry != null ? apEntry : usedEntry);
		}
		mac.getMaxBandwidth().setUpBW(usedEntry.getBandwidth(true));
		mac.getMaxBandwidth().setDownBW(usedEntry.getBandwidth(false));
	}

	@Override
	protected CellLink createLink(MacAddress source, MacAddress destination) {
		boolean sourceIsClient = mobileClients.contains(source);
		boolean destinationIsClient = mobileClients.contains(destination);
		if (sourceIsClient == destinationIsClient) {
			// both client or both not client -> not connected
			return new CellLink(source, destination);
		}
		MacAddress mobileClient = sourceIsClient ? source : destination;
		CellLink link = new CellLink(source, destination, true,
				getPhyType().getDefaultMTU(), mobileClient,
				cloudlets.contains(sourceIsClient ? destination : source),
				mobileClientsUsingAccessPoints.contains(mobileClient));
		updateOutdatedLink(link);
		return link;
	}

	@Override
	protected List<MacAddress> updateNeighborhood(MacAddress source) {
		if (cloudlets.contains(source) || clouds.contains(source)) {
			return mobileClientsList;
		} else {
			assert mobileClients.contains(source);
			// Is a client, has all clouds and cloudlets as neighbors
			return cloudsAndCloudletsList;
		}
	}

	/*
	 * Configuration options
	 */

	/**
	 * GroupIDs (as specified in the host builder section) that should act as
	 * clouds (e.g., full measured latency and bandwidth is applied)
	 * 
	 * @param cloudGroupIDs
	 */
	@SuppressWarnings("unchecked")
	public void setCloudGroups(String[] cloudGroupIDs) {
		this.groupIDsCloud = new LinkedHashSet<>(Arrays.asList(cloudGroupIDs));
	}

	/**
	 * GroupIDs that act as cloudlets (e.g., lower latency, higher BW) - models
	 * regional computations facilities.
	 * 
	 * @param cloudletGroups
	 */
	@SuppressWarnings("unchecked")
	public void setCloudletGroups(String[] cloudletGroups) {
		this.groupIDsCloudlet = new LinkedHashSet<>(
				Arrays.asList(cloudletGroups));
	}

	/**
	 * GroupIDs that act as clients and have access to access points.
	 * 
	 * @param cloudletGroups
	 */
	@SuppressWarnings("unchecked")
	public void setAccessPointGroups(String[] accessPointGroups) {
		this.groupIDsAccessPointUsage = new LinkedHashSet<>(
				Arrays.asList(accessPointGroups));
	}

	/**
	 * Set the measurement database providing link quality data for clients
	 * based on their position.
	 * 
	 * @param database
	 */
	public void setDatabase(FiveGTopologyDatabase database) {
		assert this.database == null;
		this.database = database;
	}

	/**
	 * Another layer of a database, this time for access points.
	 * 
	 * @param accessPoints
	 */
	public void setAccessPoints(FiveGTopologyDatabase accessPoints) {
		assert this.databaseAccessPoints == null;
		this.databaseAccessPoints = accessPoints;
	}

	/**
	 * A custom link object supporting dynamic updates of the properties without
	 * relying on a movement model notification - the client's position is used
	 * to determine the link performance.
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, Nov 4, 2015
	 */
	public class CellLink extends DefaultLink {

		/**
		 * Address of the mobile client
		 */
		private final MacAddress mobileClient;

		private FiveGTopologyDatabase.Entry linkData;

		private FiveGTopologyDatabase.Entry apLinkData;

		private final boolean isUpload;

		private final boolean supportsAccessPoints;

		private final boolean isCloudlet;

		private final boolean isInvalidLink;

		/**
		 * An unconnected link
		 * 
		 * @param source
		 * @param destination
		 */
		public CellLink(MacAddress source, MacAddress destination) {
			super(source, destination, false, -1, -1, -1, -1);
			this.mobileClient = null;
			this.isUpload = false;
			this.isCloudlet = false;
			this.isInvalidLink = true;
			this.supportsAccessPoints = false;
		}

		public CellLink(MacAddress source, MacAddress destination,
				boolean isConnected, int mtu, MacAddress mobileClient,
				boolean isCloudlet, boolean supportsAccessPoints) {
			super(source, destination, isConnected, -1, -1, -1, mtu);
			this.mobileClient = mobileClient;
			this.isCloudlet = isCloudlet;
			if (mobileClient.equals(source)) {
				isUpload = true;
			} else {
				assert mobileClient.equals(destination);
				isUpload = false;
			}
			this.isInvalidLink = false;
			this.supportsAccessPoints = supportsAccessPoints;
		}

		public boolean isInvalidLink() {
			return isInvalidLink;
		}

		public boolean supportsAccessPoints() {
			return supportsAccessPoints;
		}

		public int getSegmentId() {
			return linkData == null ? -1 : linkData.getSegmentID();
		}

		public int getApSegmentId() {
			assert supportsAccessPoints;
			return apLinkData == null ? -1 : apLinkData.getSegmentID();
		}

		public void setLinkData(FiveGTopologyDatabase.Entry linkData,
				FiveGTopologyDatabase.Entry apLinkData) {
			this.linkData = linkData;
			assert (apLinkData != null && supportsAccessPoints)
					|| apLinkData == null;
			this.apLinkData = apLinkData;
		}

		public MacAddress getMobileClient() {
			return mobileClient;
		}

		public boolean isCloudlet() {
			return isCloudlet;
		}

		public boolean isUpload() {
			return isUpload;
		}

		@Override
		public long getBandwidth(boolean isBroadcast) {
			assert (apLinkData != null && supportsAccessPoints)
					|| apLinkData == null;
			return apLinkData != null ? apLinkData.getBandwidth(isUpload)
					: linkData.getBandwidth(isUpload);
		}

		@Override
		public double getDropProbability() {
			assert (apLinkData != null && supportsAccessPoints)
					|| apLinkData == null;
			return apLinkData != null ? apLinkData.getDropProbability(isUpload)
					: linkData.getDropProbability(isUpload);
		}

		@Override
		public long getLatency() {
			assert (apLinkData != null && supportsAccessPoints)
					|| apLinkData == null;
			return apLinkData != null ? apLinkData.getLatency(isUpload)
					: linkData.getLatency(isUpload);
		}

	}

	/**
	 * Implementation of a {@link HandoverSensor} within the 5G topo-view.
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, Nov 20, 2015
	 */
	public class HandoverSensor5G implements HandoverSensor {

		private List<HandoverListener> listeners = new LinkedList<>();

		private boolean isConnectedToAccessPoint;

		public int apSegmentId = -1;

		public final MacAddress macAddr;

		private Host host;

		public HandoverSensor5G(Host host, MacAddress macAddr) {
			this.host = host;
			this.macAddr = macAddr;
		}

		@Override
		public boolean isConnectedToAccessPoint() {
			return isConnectedToAccessPoint;
		}

		public void setConnectedToAccessPoint(
				boolean isConnectedToAccessPoint) {
			if (this.isConnectedToAccessPoint != isConnectedToAccessPoint) {
				this.isConnectedToAccessPoint = isConnectedToAccessPoint;
				for (HandoverListener listener : listeners) {
					listener.onHandover(isConnectedToAccessPoint);
				}
			}
		}

		@Override
		public void addHandoverListener(HandoverListener listener) {
			listeners.add(listener);
		}

		@Override
		public boolean removeHandoverListener(
				HandoverListener listenerToRemove) {
			return listeners.remove(listenerToRemove);
		}

		@Override
		public void initialize() {
			// not needed
		}

		@Override
		public void shutdown() {
			// not needed
		}

		@Override
		public Host getHost() {
			return host;
		}

	}

}
