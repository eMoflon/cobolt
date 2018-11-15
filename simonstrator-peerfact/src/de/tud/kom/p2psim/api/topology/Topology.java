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

package de.tud.kom.p2psim.api.topology;

import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.topology.obstacles.ObstacleModel;
import de.tud.kom.p2psim.api.topology.social.SocialView;
import de.tud.kom.p2psim.api.topology.views.TopologyView;
import de.tud.kom.p2psim.api.topology.waypoints.WaypointModel;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tudarmstadt.maki.simonstrator.api.component.GlobalComponent;

/**
 * We provide a global Topology-Object (ie. this object is only created once in
 * the simulator and holds all hosts). The topology is just maintaining the
 * places of the hosts as well as obstacles. Connectivity is maintained by the
 * corresponding {@link TopologyView}.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 21.02.2012
 */
public interface Topology extends GlobalComponent {

	/**
	 * This Position Vector contains the upper bounds for each dimension used in
	 * the Topology. This information is needed by placement and movement models
	 * 
	 * @return
	 */
	public PositionVector getWorldDimensions();

	/**
	 * Add an obstacle to the Topology
	 * 
	 * @param obstacle
	 */
	/* public void addObstacle(Obstacle obstacle); */

	/**
	 * Add a host to the topology. Hosts are added at the very beginning of the
	 * simulation, later additions are not supported.
	 * 
	 * @param comp
	 */
	public void addComponent(TopologyComponent comp);

	/**
	 * Set the waypoint model that describes the (preferred) movements
	 * 
	 * @param model
	 */
	public void setWaypointModel(WaypointModel model);

	/**
	 * Set the obstacle model that describes obstacles placed in the world
	 * 
	 * @param model
	 */
	public void setObstacleModel(ObstacleModel model);

	/**
	 * Ensure that if a new topologyListener is added, it is first of all
	 * informed of <b>all components</b> and <b>all obstacles already added</b>!
	 * 
	 * @param listener
	 */
	public void addTopologyListener(TopologyListener listener);

	/**
	 * 
	 * @param listener
	 */
	public void removeTopologyListener(TopologyListener listener);

	/**
	 * Add a global {@link TopologyView}, this will also register it as a
	 * {@link TopologyListener}
	 * 
	 * @param tView
	 */
	public void addTopologyView(TopologyView tView);

	/**
	 * Returns the {@link TopologyView} for the given {@link PhyType}
	 * 
	 * @param phy
	 * @return
	 */
	public TopologyView getTopologyView(PhyType phy);

	/**
	 * Adds a global {@link SocialView}, this will also register it as a
	 * {@link TopologyListener}. Also, the identifier from {@link SocialView} is
	 * used to access a {@link SocialView}.
	 * 
	 * @param sView
	 *            The {@link SocialView}, which should be added
	 */
	public void addSocialView(SocialView sView);

	/**
	 * Gets the {@link SocialView} for the given identifier.
	 * 
	 * @param id
	 *            The identifier of the SocialView.
	 * @return The {@link SocialView} or <code>null</code> if the identifier not
	 *         exists
	 */
	public SocialView getSocialView(String id);

	/**
	 * Should be called after all hosts are generated! This will be initialize
	 * the {@link SocialView}s for all possible Hosts. A multiple call should be
	 * do nothing!
	 */
	public void initializeSocial();

	/**
	 * Gets the WaypointModel, which is configured (Please note, that must not
	 * be the final configured WaypointModel!)
	 * 
	 * @return
	 */
	public WaypointModel getWaypointModel();

	/**
	 * Gets the ObstacleModel, which is configured (Please note, that must not
	 * be the final configured ObstacleModel!)
	 * 
	 * @return
	 * */
	public ObstacleModel getObstacleModel();

}
