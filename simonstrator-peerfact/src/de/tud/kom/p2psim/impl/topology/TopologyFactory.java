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
import java.util.Map;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.linklayer.LinkLayer;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.topology.Topology;
import de.tud.kom.p2psim.api.topology.TopologyComponent;
import de.tud.kom.p2psim.api.topology.movement.MovementModel;
import de.tud.kom.p2psim.api.topology.obstacles.ObstacleModel;
import de.tud.kom.p2psim.api.topology.placement.PlacementModel;
import de.tud.kom.p2psim.api.topology.social.SocialView;
import de.tud.kom.p2psim.api.topology.views.TopologyView;
import de.tud.kom.p2psim.api.topology.waypoints.WaypointModel;
import de.tud.kom.p2psim.impl.network.modular.DBHostListManager;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;
import de.tud.kom.p2psim.impl.topology.movement.AbstractWaypointMovementModel;
import de.tud.kom.p2psim.impl.topology.movement.NoMovement;
import de.tud.kom.p2psim.impl.topology.placement.GNPPlacement;
import de.tud.kom.p2psim.impl.topology.views.latency.GNPLatency;
import de.tudarmstadt.maki.simonstrator.api.Binder;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponentFactory;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * This factory is configured with one or more {@link TopologyView}s if the
 * {@link LinkLayer} is used.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 01.03.2012
 */
public class TopologyFactory implements HostComponentFactory {

	private Topology topo;

	/**
	 * Placement model for the current group of hosts
	 */
	private PlacementModel placement;

	/**
	 * Movement model for the current group of hosts
	 */
	private MovementModel movement = new NoMovement();

	private WaypointModel waypointModel;

	private ObstacleModel obstacleModel;

	private static NetMeasurementDB measurementDB = null;

	private static Map<SimHost, NetMeasurementDB.Host> measurementDBHosts = new LinkedHashMap<SimHost, NetMeasurementDB.Host>();

	private static DBHostListManager dbHostList = null;

	private static boolean useRegionGroups = false;

	private boolean alreadyCreatedInstances = false;

	private boolean alreadyAddedMovement = false;

	/**
	 * 
	 */
	@XMLConfigurableConstructor({ "worldX", "worldY" })
	public TopologyFactory(double worldX, double worldY) {
		topo = new DefaultTopology(new PositionVector(worldX, worldY));
		// Make the topology component available globally
		Binder.registerComponent(topo);
	}

	@Override
	public TopologyComponent createComponent(Host pHost) {
		alreadyCreatedInstances = true;
		SimHost host = (SimHost) pHost;
		if (measurementDB != null) {
			String groupStr = host.getProperties().getGroupID();
			NetMeasurementDB.Host hostMeta;
			if (useRegionGroups) {
				// In case of a DB presence, look up the host's specific
				// metadata there
				NetMeasurementDB.Group g = measurementDB
						.getStringAddrObjFromStr(NetMeasurementDB.Group.class,
								groupStr);
				if (g == null) {
					throw new IllegalArgumentException(
							"There is no group named '" + groupStr + "'");
				}
				hostMeta = g.tGetNextMember();
			} else {
				// The hosts are not grouped by their region name, we will
				// return random hosts in the world for each group.
				hostMeta = dbHostList.getNextHost();
			}
			measurementDBHosts.put(host, hostMeta);
		}

		/*
		 * Create a TopologyComponent and register it with the Topology and its
		 * movement model.
		 */
		TopologyComponent toCo = new DefaultTopologyComponent(host, topo,
				movement, placement);

		/*
		 * Need to register TopoViews as movement listeners, as they might need
		 * to update topologies after each movement.
		 */
		for (PhyType phy : PhyType.values()) {
			TopologyView view = topo.getTopologyView(phy);
			if (view != null) {
				toCo.requestLocationUpdates(null, view);
			}
		}

		Monitor.log(TopologyFactory.class, Level.INFO,
				"Topology Component for Host %s created. Placement: %s, Movement: %s",
				host.getHostId(), placement, movement);
		return toCo;
	}

	/**
	 * Set the {@link PlacementModel} for this group of hosts
	 * 
	 * @param placementModel
	 */
	public void setPlacement(PlacementModel placementModel) {
		// Here, we loose the previous model. Intended!
		placement = placementModel;
	}

	public void setWaypoints(WaypointModel waypointModel) {
		waypointModel.setWorldDimensions(topo.getWorldDimensions());
		this.waypointModel = waypointModel;

		this.waypointModel.setObstacleModel(this.obstacleModel);

		this.waypointModel.generateWaypoints();

		topo.setWaypointModel(waypointModel);
	}

	public void setObstacles(ObstacleModel obstacleModel) {
		obstacleModel.setWorldDimensions(topo.getWorldDimensions());
		this.obstacleModel = obstacleModel;

		this.obstacleModel.setWaypointModel(this.waypointModel);

		this.obstacleModel.generateObstacles();

		topo.setObstacleModel(obstacleModel);
	}

	/**
	 * Add a {@link TopologyView}
	 * 
	 * @param topologyView
	 */
	public void setView(TopologyView topologyView) {
		if (alreadyCreatedInstances || alreadyAddedMovement) {
			throw new AssertionError(
					"Topology Views have to be specified globally, not in a host-section! "
							+ "Furthermore, they have to be specified BEFORE the movement models!");
		}
		topo.addTopologyView(topologyView);
	}

	public void setSocialView(SocialView sView) {
		topo.addSocialView(sView);
	}

	/**
	 * Set the {@link MovementModel} for this group of hosts
	 * 
	 * @param movement
	 */
	public void setMovement(MovementModel movement) {
		alreadyAddedMovement = true;
		if (movement instanceof AbstractWaypointMovementModel) {
			((AbstractWaypointMovementModel) movement)
					.setWaypointModel(waypointModel);
		}
		this.movement = movement;
	}

	/**
	 * For the {@link GNPLatency} and the {@link GNPPlacement}, a
	 * {@link NetMeasurementDB} is needed.
	 * 
	 * @param db
	 */
	public void setMeasurementDB(NetMeasurementDB db) {
		if (this.measurementDB == null) {
			this.measurementDB = db;
			this.dbHostList = new DBHostListManager(measurementDB);
		} else {
			throw new AssertionError(
					"You are only allowed to set the MeasurementDB once!");
		}
	}

	public void setUseRegionGroups(boolean useRegionGroups) {
		TopologyFactory.useRegionGroups = useRegionGroups;
	}

	/**
	 * Allows GNP-based strategies to retrieve the unique
	 * {@link NetMeasurementDB.Host} - as this object should only be created
	 * once per host, it is maintained in this static manner.
	 * 
	 * @param host
	 * @return
	 */
	public static NetMeasurementDB.Host getMeasurementDBHost(SimHost host) {
		if (measurementDB == null) {
			throw new AssertionError();
		}
		return measurementDBHosts.get(host);
	}

	/**
	 * The Measurement-DB
	 * 
	 * @return
	 */
	public static NetMeasurementDB getMeasurementDB() {
		if (measurementDB == null) {
			throw new AssertionError();
		}
		return measurementDB;
	}

	public ObstacleModel getObstacleModel() {
		return obstacleModel;
	}
}
