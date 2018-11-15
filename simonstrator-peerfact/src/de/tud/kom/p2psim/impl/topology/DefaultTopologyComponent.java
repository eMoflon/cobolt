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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import de.tud.kom.p2psim.api.common.HostProperties;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.topology.Topology;
import de.tud.kom.p2psim.api.topology.TopologyComponent;
import de.tud.kom.p2psim.api.topology.movement.MovementModel;
import de.tud.kom.p2psim.api.topology.placement.PlacementModel;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.topology.movement.modular.attraction.AttractionPoint;
import de.tud.kom.p2psim.impl.topology.views.LogicalWifiTopologyView;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetworkComponent.NetInterfaceName;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.LocationListener;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.LocationRequest;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSComponent;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSDataCallback;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSInfoProperties;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSInformationProvider.SiSProviderHandle;
import de.tudarmstadt.maki.simonstrator.api.component.sis.exception.InformationNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSTypes;
import de.tudarmstadt.maki.simonstrator.api.component.sis.util.SiSTopologyProvider;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyID;

/**
 * Default implementation of a {@link TopologyComponent}.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 29.02.2012
 */
public class DefaultTopologyComponent implements TopologyComponent {

	protected static Random rnd = Randoms.getRandom(AttractionPoint.class);

	private SimHost host;

	private final PositionVector position;

	private Topology topology;

	private double currentMovementSpeed = -1;

	private Map<LocationListener, LocationRequestImpl> openRequests = new LinkedHashMap<>();

	private List<LocationListener> listeners = new LinkedList<>();

	private MovementModel movementModel;

	private PlacementModel placementModel;

	/**
	 * Create a TopologyComponent for the current host.
	 * 
	 * @param host
	 * @param topology
	 * @param movementModel
	 */
	public DefaultTopologyComponent(SimHost host, Topology topology,
			MovementModel movementModel, PlacementModel placementModel) {
		this.topology = topology;
		this.host = host;
		this.position = new PositionVector(0, 0);

		this.movementModel = movementModel;
		if (this.movementModel != null) {
			this.movementModel.addComponent(this);
		}

		this.placementModel = placementModel;
		if (this.placementModel != null) {
			this.placementModel.addComponent(this);
		}
	}

	@Override
	public void initialize() {
		/*
		 * Set the component's initial position and notify listeners of the
		 * Topology that this component is initialized.
		 */
		topology.addComponent(this);
		movementModel.placeComponent(this);

		if (placementModel != null) {
			/*
			 * Legacy support for placement models.
			 */
			position.set(placementModel.place(this));
		}

		try {
			final SiSComponent sis = host.getComponent(SiSComponent.class);
			sis.provide().nodeState(SiSTypes.PHY_LOCATION,
					new SiSDataCallback<Location>() {

						Set<INodeID> localID = INodeID
								.getSingleIDSet(getHost().getId());

						@Override
						public Location getValue(INodeID nodeID,
								SiSProviderHandle providerHandle)
								throws InformationNotAvailableException {
							if (nodeID.equals(getHost().getId())) {
								return getLastLocation();
							} else {
								throw new InformationNotAvailableException();
							}
						}

						@Override
						public Set<INodeID> getObservedNodes() {
							return localID;
						}

						@Override
						public SiSInfoProperties getInfoProperties() {
							return new SiSInfoProperties();
						}
					});

			// Provide Underlay topology
			Event.scheduleImmediately(new EventHandler() {

				@Override
				public void eventOccurred(Object content, int type) {
					if (getHost().getLinkLayer().hasPhy(PhyType.WIFI)) {
						new SiSTopologyProvider(sis, SiSTypes.NEIGHBORS_WIFI,
								DefaultTopologyComponent.this,
								getTopologyID(NetInterfaceName.WIFI, true),
								DefaultTopologyComponent.class);
					}
				}
			}, null, 0);

		} catch (ComponentNotAvailableException e) {
			// OK
		}
	}

	@Override
	public void shutdown() {
		topology = null;
		host = null;
		movementModel = null;
	}

	@Override
	public SimHost getHost() {
		return host;
	}

	@Override
	public PositionVector getRealPosition() {
		return position;
	}

	@Override
	public Topology getTopology() {
		return topology;
	}

	@Override
	public double getMinMovementSpeed() {
		HostProperties properties = getHost().getProperties();

		return properties.getMinMovementSpeed();
	}

	@Override
	public double getMaxMovementSpeed() {
		HostProperties properties = getHost().getProperties();
		return properties.getMaxMovementSpeed();
	}
	
	public MovementModel getMovementModel() {
		return movementModel;
	}

	private void calcRandomMovementSpeed() {
		double min_speed = getMinMovementSpeed();
		double max_speed = getMaxMovementSpeed();

		double value = rnd.nextDouble();
		this.currentMovementSpeed = (value * (max_speed - min_speed))
				+ min_speed;
	}

	@Override
	public double getMovementSpeed() {
		if (currentMovementSpeed == -1) {
			calcRandomMovementSpeed();
		}
		return this.currentMovementSpeed;
	}

	@Override
	public void setMovementSpeed(double speed) {
		this.currentMovementSpeed = speed;
	}

	@Override
	public Location getLastLocation() {
		/*
		 * As we want to mimic real world behavior, the current position
		 * snapshot is cloned to prevent information propagation due to Java.
		 */
		return position.clone();
	}

	@Override
	public void updateCurrentLocation(double longitude, double latitude) {
		position.setEntries(longitude, latitude);
		// notify "non-request" listeners
		for (LocationListener locationListener : listeners) {
			locationListener.onLocationChanged(getHost(), getLastLocation());
		}
	}

	@Override
	public void setNewTargetLocation(double longitude, double latitude) {
		movementModel.changeTargetLocation(this, longitude, latitude);
	}

	@Override
	public void requestLocationUpdates(LocationRequest request,
			LocationListener listener) {
		if (openRequests.containsKey(listener)) {
			throw new AssertionError(
					"This LocationListener is already in use.");
		}
		if (request == null) {
			/*
			 * This listener wants to be triggered on EVERY position update, but
			 * it does not want to request position updates.
			 */
			if (!listeners.contains(listener)) {
				listeners.add(listener);
			}
		} else {
			/*
			 * Listener has its own request timing.
			 */
			LocationRequestImpl req = (LocationRequestImpl) request;
			openRequests.put(listener, req);
			req.immunizeAndStart(listener);
		}
	}

	@Override
	public void removeLocationUpdates(LocationListener listener) {
		listeners.remove(listener);
		LocationRequestImpl impl = openRequests.remove(listener);
		if (impl != null) {
			impl.cancel(listener);
		}
	}

	@Override
	public LocationRequest getLocationRequest() {
		return new LocationRequestImpl();
	}

	/**
	 * Update 15.03.16 added support for multiple listeners (however, frequency
	 * etc. is immune after the first request is registered.)
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, Mar 15, 2016
	 */
	private class LocationRequestImpl implements LocationRequest, EventHandler {

		private boolean immune = false;

		private long interval = 1 * Simulator.MINUTE_UNIT;

		private int priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

		private Location lastLocation = null;

		private List<LocationListener> listeners = new LinkedList<>();

		public LocationRequestImpl() {
			// nothing to do
		}

		protected void cancel(LocationListener listener) {
			boolean removed = listeners.remove(listener);
			assert removed;
		}

		protected void immunizeAndStart(LocationListener listener) {
			immune = true;
			assert interval > 0;
			if (listeners.isEmpty()) {
				// Only start once!
				lastLocation = null;
				Event.scheduleImmediately(this, null, 0);
			} else {
				// Fire each new listener at least once
				listener.onLocationChanged(getHost(), getLastLocation());
			}
			listeners.add(listener);
		}

		@Override
		public void setInterval(long interval) {
			if (!immune) {
				this.interval = interval;
			}
		}

		@Override
		public void setPriority(int priority) {
			if (!immune) {
				this.priority = priority;
			}
		}

		@Override
		public void eventOccurred(Object content, int type) {
			if (!listeners.isEmpty()) {
				// Only reschedule, if at least one listener is ... listening
				Location newLoc = getLastLocation();
				if (lastLocation == null
						|| lastLocation.distanceTo(newLoc) > 0) {
					listeners.forEach((LocationListener listener) -> listener
							.onLocationChanged(getHost(), newLoc));
					lastLocation = newLoc;
				}
				Event.scheduleWithDelay(interval, this, null, 0);
			}
		}

	}

	/*
	 * Methods for the Graph Interface
	 */

	/**
	 * Graph views: static, as we use global knowledge and maintain one shared
	 * graph (potentially with partitions!)
	 */
	private final static LinkedHashMap<TopologyID, LocalGraphView> graphViews = new LinkedHashMap<>();

	@Override
	public TopologyID getTopologyID(NetInterfaceName netName,
			boolean onlyOnline) {
		TopologyID id = TopologyID.getIdentifier(
				netName.toString() + (onlyOnline ? "-online" : "-all"),
				DefaultTopologyComponent.class);
		if (!this.graphViews.containsKey(id)) {
			this.graphViews.put(id, new LocalGraphView(netName, onlyOnline));
		}
		return id;
	}

	@Override
	public TopologyID getTopologyID(NetInterfaceName netName,
			boolean onlyOnline, double range) {
		TopologyID id = TopologyID.getIdentifier(
				netName.toString() + (onlyOnline ? "-online" : "-all")
						+ String.valueOf(range),
				DefaultTopologyComponent.class);
		if (!this.graphViews.containsKey(id)) {
			this.graphViews.put(id,
					new LocalGraphView(netName, onlyOnline, range));
		}
		return id;
	}

	@Override
	public INode getNode(TopologyID identifier) {
		assert graphViews.containsKey(identifier);
		return graphViews.get(identifier).getOwnNode(host);
	}

	@Override
	public Set<IEdge> getNeighbors(TopologyID topologyIdentifier) {
		assert graphViews.containsKey(topologyIdentifier);
		return graphViews.get(topologyIdentifier).getNeighbors(host);
	}

	@Override
	public Graph getLocalView(TopologyID topologyIdentifier) {
		/*
		 * TODO RKluge / MSt: this method returns a global view on the topology
		 * (!!) - if you need a subset from the viewport of one specific node
		 * (e.g., only one hop neighbors), add:
		 * Graph.topology.getLocalView(this.getNode(topologyIdentifier).getId(),
		 * DEFAULT_SIZE_OF_LOCAL_VIEW);
		 */
		assert graphViews.containsKey(topologyIdentifier);
		return graphViews.get(topologyIdentifier).getLocalView();
	}

	@Override
	public Iterable<TopologyID> getTopologyIdentifiers() {
		return graphViews.keySet();
	}

	@Override
	public String toString() {
		return "TopoComp: " + getHost().getId() + " at " + position.toString();
	}

	/**
	 * @deprecated Use {@link LogicalWifiTopologyView#getAdaptableTopologyID()}
	 */
	@Deprecated
	public TopologyID getWifiIdentifier() {
		/*
		 * Use LogicalWiFiTopology instead!
		 */
		return LogicalWifiTopologyView.getAdaptableTopologyID();
	}

	/**
	 * @deprecated {@link LogicalWifiTopologyView#getUDGTopologyID()}
	 */
	@Deprecated
	public static TopologyID getWifiUdgIdentifier() {
		/*
		 * Use LogicalWiFiTopology instead!
		 */
		return LogicalWifiTopologyView.getUDGTopologyID();
	}

}
