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

package de.tud.kom.p2psim.impl.network.modular.common;

import java.awt.geom.Point2D;

import de.tud.kom.p2psim.impl.network.gnp.HaversineHelpers;

public class GeoToolkit {

	public static Point2D.Double STANDARD_REF_POINT_GERMANY = new Point2D.Double(
			5, 47);

	// Source: http://en.wikipedia.org/wiki/Longitude
	// lat / Dist per 1 Deg lat / Dist per 1 Deg long
	// 0° 110.574 km 111.320 km
	// 15° 110.649 km 107.551 km
	// 30° 110.852 km 96.486 km
	// 45° 111.132 km 78.847 km
	// 60° 111.412 km 55.800 km
	// 75° 111.618 km 28.902 km
	// 90° 111.694 km 0.000 km

	static double[] distlat = { 110574, 110649, 110852, 111132, 111412, 111618,
			111694 };

	static double[] distlong = { 111320, 107551, 96486, 78847, 55800, 28902, 0 };

	public static void main(String[] args) {
		calcCoordinate(new Point2D.Double(10, 50), 90, 100000);
		calcCoordinate(new Point2D.Double(10, 50), 0, 100000);
		calcCoordinate(new Point2D.Double(10, 50), -45, 1 * Math.sqrt(2));
		calcCoordinate(new Point2D.Double(10.15, 50.45), 135, 1);

		System.out.println(GeoToolkit.getDistance(180, 0, 180, 90));
		System.out.println(GeoToolkit.getDistance(180, 90, 180, 0));
		System.out.println(GeoToolkit.getDistance(13, 18, 54, 72));
		System.out.println(GeoToolkit.getDistance(52, 9, 52.5, 9));
		System.out.println(GeoToolkit.getDistance(120, 30, 100, 30));
	}

	public static Point2D.Double calcCoordinate(Point2D.Double point,
			double bearing, double distance) {
		double dist = distance / 6371000.0;
		double brng = Math.toRadians(bearing);
		double lat1 = Math.toRadians(point.y);
		double lon1 = Math.toRadians(point.x);

		double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist)
				+ Math.cos(lat1) * Math.sin(dist) * Math.cos(brng));
		double a = Math.atan2(Math.sin(brng) * Math.sin(dist) * Math.cos(lat1),
				Math.cos(dist) - Math.sin(lat1) * Math.sin(lat2));
		double lon2 = lon1 + a;

		lon2 = (lon2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

		return new Point2D.Double(Math.toDegrees(lon2), Math.toDegrees(lat2));
	}

	/**
	 * Returns the distance between two points in meters.
	 * 
	 * @param latitudeA
	 * @param longitudeA
	 * @param latitudeB
	 * @param longitudeB
	 * @return the distance between two points in meters.
	 */
	public static double getDistance(double latitudeA, double longitudeA,
			double latitudeB, double longitudeB) {
		double lat1 = Math.toRadians(latitudeA);
		double lat2 = Math.toRadians(latitudeB);
		double dlat = lat2 - lat1;
		double long1 = Math.toRadians(longitudeA);
		double long2 = Math.toRadians(longitudeB);

		double dlong = long1 - long2;

		double a = Math.pow((Math.sin(dlat / 2)), 2) + Math.cos(lat1)
				* Math.cos(lat2) * Math.pow((Math.sin(dlong / 2)), 2);
		// angle in radians formed by start point, earth's center, & end point
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		// radius of earth at midpoint of route
		double r = HaversineHelpers.globeRadiusOfCurvature((lat1 + lat2) / 2);
		return r * c;
	}

	/**
	 * Transforms a given longitude latitude coordinate to a point in the
	 * Cartesian space
	 * 
	 * @param referencePoint
	 *            the reference point in longitude latitude coordinates
	 * @param longLat
	 *            the point to be transformed in longitude latitude coordinates
	 * @return the XY coordinates in meters form the reference point
	 */
	public static Point2D.Double transformToXY(Point2D referencePoint,
			Point2D longLat) {

		return new Point2D.Double((longLat.getX() - referencePoint.getX())
				* distlong[(int) referencePoint.getX() / 15],
				(longLat.getY() - referencePoint.getY())
						* distlat[(int) referencePoint.getY() / 15]);
	}

	/**
	 * Transforms a given X/Y coordinate in meters into a long latitude
	 * coordinate in degrees based on a given reference point given in long lat
	 * 
	 * @param referencePoint
	 *            the reference point as longitude latitude coordinates
	 * @param XY
	 *            the point to be transformed in meters
	 * @return the longitude / latitude
	 */
	public static Point2D.Double transformToLongLat(Point2D referencePoint,
			Point2D XY) {

		return new Point2D.Double(
				XY.getX() / distlong[(int) referencePoint.getX() / 15]
						+ referencePoint.getX(),
				(XY.getY() / distlat[(int) referencePoint.getY() / 15] + referencePoint
						.getY()));
	}

}
