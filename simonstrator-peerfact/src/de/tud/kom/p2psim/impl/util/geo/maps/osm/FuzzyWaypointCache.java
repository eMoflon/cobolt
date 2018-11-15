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

package de.tud.kom.p2psim.impl.util.geo.maps.osm;

import java.util.Map;

import com.google.common.collect.Maps;

import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.Waypoint;

class FuzzyWaypointCache {
	private Map<FuzzyWaypointCache.PositionWrapper, Waypoint> waypointCache = Maps
			.newHashMap();

	public FuzzyWaypointCache() {
		//
	}

	public void addWaypoint(PositionVector position, Waypoint waypoint) {
		if (getWaypoint(position) == null) {
			waypointCache.put(new PositionWrapper(position), waypoint);
		}
	}

	public Waypoint getWaypoint(PositionVector position) {
		return waypointCache.get(new PositionWrapper(position));
	}

	public void clear() {
		waypointCache.clear();
	}

	private static class PositionWrapper {
		private PositionVector position;

		public PositionWrapper(PositionVector position) {
			this.position = position;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((position == null) ? 0 : addTolerance(position)
							.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof FuzzyWaypointCache.PositionWrapper))
				return false;

			PositionVector pw1 = addTolerance(position);
			PositionVector pw2 = addTolerance(((FuzzyWaypointCache.PositionWrapper) obj).position);

			if (!pw1.equals(pw2))
				return false;

			return true;
		}

		private PositionVector addTolerance(PositionVector position) {
			int count = 1;
			PositionVector tpos = new PositionVector(round(position.getX(),
					count), round(position.getY(), count));

			return tpos;
		}

		public static final double round(double value, int count) {
			double shift = Math.pow(10, count);
			return Math.round(value * shift) / shift;
		}
	}
}