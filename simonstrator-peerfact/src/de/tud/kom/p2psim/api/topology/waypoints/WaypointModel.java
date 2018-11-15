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

package de.tud.kom.p2psim.api.topology.waypoints;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.tud.kom.p2psim.api.topology.obstacles.ObstacleModel;
import de.tud.kom.p2psim.api.util.geo.maps.Map;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.DefaultWeightedEdgeRetrievableGraph;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.Path;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.Waypoint;
import de.tud.kom.p2psim.impl.util.Tuple;

public interface WaypointModel {

	/**
	 * Sets the current world dimensions which can be used by the
	 * waypoint model to correctly position the waypoints.
	 * 
	 * @param worldDimensions
	 */
	public abstract void setWorldDimensions(PositionVector worldDimensions);

	/**
	 * Returns all waypoints as a set.
	 * 
	 * @return
	 */
	public abstract Collection<Waypoint> getWaypoints();
	
	/**
	 * Returns all waypoints of type "type"
	 * 
	 * @param type
	 * @return
	 */
	public abstract Collection<Waypoint> getWaypoints(Class type);
	
	/**
	 * Returns the closest waypoint to the given position.
	 * 
	 * @param position
	 * @return
	 */
	public abstract Waypoint getClosestWaypoint(PositionVector position);
	
	/**
	 * Returns the closest waypoint of a specific type to the current position.
	 * 
	 * @param position
	 * @param type
	 * @return
	 */
	public abstract Waypoint getClosestWaypoint(PositionVector position, Class type);
	
	/**
	 * Returns a list of Tuple<Waypoint, Path> that are connected to the given waypoint.
	 * 
	 * @param waypoint
	 * @return
	 */
	public abstract List<Tuple<Waypoint, Path>> getConnectedWaypoints(Waypoint waypoint);
	
	/**
	 * Returns a list of Tuple<Waypoint, Path> of a specific type that are connected to the given waypoint.
	 * 
	 * @param waypoint
	 * @param type
	 * @return
	 */
	public abstract List<Tuple<Waypoint, Path>> getConnectedWaypoints(Waypoint waypoint, Class type);

	/**
	 * Returns all paths that are part of the waypoint model.
	 * 
	 * @return
	 */
	public abstract Set<Path> getPaths();
	
	/**
	 * Returns the shortest path between the two given waypoints.
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public abstract List<Path> getShortestPath(Waypoint start, Waypoint end);

	/**
	 * Returns the total number of waypoints.
	 * 
	 * @return
	 */
	public abstract int getNumberOfWaypoints();
	
	/**
	 * Return the total number of waypoints of a specific type.
	 * 
	 * @param type
	 * @return
	 */
	public abstract int getNumberOfWaypoints(Class type);

	public abstract void addListener(WaypointModelListener listener);

	public abstract void removeListener(WaypointModelListener listener);

	/**
	 * Starts the waypoint generation process. After this method has
	 * been called all waypoints should be placed.
	 */
	public abstract void generateWaypoints();
	
	/**
	 * Sets the obstacle model an may be called before or after the
	 * call to generateWaypoints, depending on the configuration.
	 * 
	 * Should the waypoint model support the placement of waypoints with
	 * regard to the obstacles it should print a warning if the obstacle
	 * model hasn't been called beforethe generation has been started.
	 * 
	 * @param model
	 */
	public abstract void setObstacleModel(ObstacleModel model);

	/**
	 * Sets the strong waypoint strategy that should be used to generate the
	 * strong waypoints.
	 * 
	 * @param strongWaypointStrategy
	 */
	/*
	public abstract void setStrongWaypointStrategy(StrongWaypointStrategy strongWaypointStrategy);
	 */
	public DefaultWeightedEdgeRetrievableGraph<Waypoint, Path> getGraph();

	/**
	 * Indicates if this model is scaled to the required world coordinates.
	 * 
	 * @return
	 */
	public boolean isScaled();
	
	/**
	 * Returns the metric dimensions of the underlying map. This dimensions may
	 * differ from the used world dimensions and can be used for scaling.
	 * 
	 * @return
	 */
	public abstract PositionVector getMetricDimensions();

	/**
	 * Returns the scaling factor if {@link isScaled} is true
	 * otherwise it should return 1.0
	 * 
	 * @return
	 */
	public abstract double getScaleFactor();

	public abstract Map getMap();

	public abstract void addWaypoint(Waypoint waypoint);
}