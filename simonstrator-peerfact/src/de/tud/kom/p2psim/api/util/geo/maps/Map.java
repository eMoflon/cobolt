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

package de.tud.kom.p2psim.api.util.geo.maps;

import java.util.Collection;
import java.util.List;

import de.tud.kom.p2psim.api.topology.obstacles.Obstacle;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.DefaultWeightedEdgeRetrievableGraph;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.Path;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.Waypoint;
import de.tud.kom.p2psim.impl.util.geo.maps.MapChangeListener;

public interface Map {
	public void loadMap();

	/**
	 * Returns the width(x) and height(y) of the map as a PositionVector.
	 * 
	 * @return
	 */
	public PositionVector getDimensions();

	// TODO: Extract required functionality
	public DefaultWeightedEdgeRetrievableGraph<Waypoint, Path> getGraph();

	public void addWaypoint(Waypoint waypoint);

	public Collection<Waypoint> getWaypoints(Class type);
	
	public List<Way> getWays();
	
	public String getName();
	public void setName(String name);

	public boolean isLoaded();

	public List<Obstacle> getObstacles();

	public void addMapChangeListener(MapChangeListener listener);
	public void removeMapChangeListener(MapChangeListener listener);

    PositionVector getMinPosition();

    PositionVector getMaxPosition();
}
