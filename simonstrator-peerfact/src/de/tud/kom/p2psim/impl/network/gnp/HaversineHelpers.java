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

public class HaversineHelpers {

	/** Radius of the earth, in meters, at the equator. */
	private static final double GLOBE_RADIUS_EQUATOR = 6378000;

	/** Radius of the earth, in meters, at the poles. */
	private static final double GLOBE_RADIUS_POLES = 6357000;

	public static final double radians(double degrees) {
		return degrees * (2 * Math.PI) / 360;
	}

	public static final double degrees(double radians) {
		return radians * 360 / (2 * Math.PI);
	}

	public static final double square(double d) {
		return d * d;
	}

	/**
	 * Computes the earth's radius of curvature at a particular latitude,
	 * assuming that the earth is a squashed sphere with elliptical
	 * cross-section.
	 * 
	 * @param lat
	 *            - latitude in radians. This is the angle that a point at this
	 *            latitude makes with the horizontal.
	 */
	public static final double globeRadiusOfCurvature(double lat) {
		double a = GLOBE_RADIUS_EQUATOR; // major axis
		double b = GLOBE_RADIUS_POLES; // minor axis
		double e = Math.sqrt(1 - square(b / a)); // eccentricity
		return a * Math.sqrt(1 - square(e)) / (1 - square(e * Math.sin(lat)));
	}
}
