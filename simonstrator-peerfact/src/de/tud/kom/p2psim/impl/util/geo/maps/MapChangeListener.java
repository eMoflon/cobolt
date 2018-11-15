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

package de.tud.kom.p2psim.impl.util.geo.maps;

import de.tud.kom.p2psim.api.topology.obstacles.Obstacle;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.Path;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.Waypoint;
import de.tud.kom.p2psim.impl.util.geo.maps.AbstractMap.Axis;

public interface MapChangeListener {

	public void mapChanged(MapEvent event);
	
	public static interface MapEvent {
		//
	}

	public static class ObstacleEvent implements MapEvent {
		private Obstacle obstacle;
		public ObstacleEvent(Obstacle changedObstacle) {
			this.obstacle = changedObstacle;
		}
		public Obstacle getObstacle() {
			return obstacle;
		}
	}
	
	public static class PathEvent implements MapEvent {
		private Path path;
		public PathEvent(Path changedPath) {
			this.path = changedPath;
		}
		public Path getPath() {
			return path;
		}
	}
	
	public static class WaypointEvent implements MapEvent {
		private Waypoint waypoint;
		public WaypointEvent(Waypoint changedWaypoint) {
			this.waypoint = changedWaypoint;
		}
		public Waypoint getWaypoint() {
			return waypoint;
		}
	}
	
	public static class SwapMapEvent implements MapEvent {
		private Axis axis;
		private double max;
		public SwapMapEvent(Axis axis, double max) {
			this.axis = axis;
			this.max = max;
		}
		public Axis getAxis() {
			return axis;
		}
		public double getMax() {
			return max;
		}
	}
	
	public static class MapToWorldEvent implements MapEvent {
		private PositionVector world;
		public MapToWorldEvent(PositionVector world) {
			this.world = world;
		}
		public PositionVector getDimensions() {
			return world;
		}
	}
}
