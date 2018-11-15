/*
 * Author: Sami Salkosuo, sami.salkosuo@fi.ibm.com
 *
 * (c) Copyright IBM Corp. 2007
 */
package de.tud.kom.p2psim.impl.util.geo.maps.osm;

import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.tan;

import de.tud.kom.p2psim.impl.topology.PositionVector;

/**
 * This class can be used to convert lat/lon coordinates to
 * utm coordinates.
 * 
 * It is a stripped down version of the one found at
 * http://www.ibm.com/developerworks/java/library/j-coordconvert/
 * Original author: Sami Salkosuo, sami.salkosuo@fi.ibm.com
 * 
 * @author Fabio Zöllner
 * @version 1.0, 09.05.2012
 */
@SuppressWarnings("synthetic-access")
public class UTMConversion {
	private static final double r2d = 180 / Math.PI;
	private static final double d2r = Math.PI / 180;
	
	public UTMConversion() {

	}

	public static UTM latLon2UTM(double latitude, double longitude) {
		LatLon2UTM c = new LatLon2UTM();
		
		return c.convertLatLonToUTM(latitude, longitude);
	}

	private static void validate(double latitude, double longitude) {
		if (latitude < -90.0 || latitude > 90.0 || longitude < -180.0
				|| longitude >= 180.0) {
			throw new IllegalArgumentException(
					"Legal ranges: latitude [-90,90], longitude [-180,180).");
		}

	}

	public static double degreeToRadian(double degree) {
		return degree * d2r;
	}

	public double radianToDegree(double radian) {
		return radian * r2d;
	}

	private static class LatLon2UTM {
		public UTM convertLatLonToUTM(double latitude, double longitude) {
			validate(latitude, longitude);
			UTM utm = null;

			setVariables(latitude, longitude);

			String longZone = getLongZone(longitude);
			LatZones latZones = new LatZones();
			String latZone = latZones.getLatZone(latitude);

			double _easting = getEasting();
			double _northing = getNorthing(latitude);

			utm = new UTM(new PositionVector(_easting, _northing), longZone, latZone);

			return utm;
		}

		protected void setVariables(double latitude, double longitude) {
			latitude = degreeToRadian(latitude);
			
			rho = equatorialRadius * (1 - e * e) / pow(1 - pow(e * sin(latitude), 2), 3 / 2.0);
			nu = equatorialRadius / pow(1 - pow(e * sin(latitude), 2), (1 / 2.0));

			double var1;
			
			if (longitude < 0.0) {
				var1 = ((int) ((180 + longitude) / 6.0)) + 1;
			} else {
				var1 = ((int) (longitude / 6)) + 31;
			}
			
			double var2 = (6 * var1) - 183;
			double var3 = longitude - var2;
			
			p = var3 * 3600 / 10000;

			S = A0 * latitude - B0 * sin(2 * latitude) + C0 * sin(4 * latitude) - D0 * sin(6 * latitude) + E0 * sin(8 * latitude);

			K1 = S * k0;
			K2 = nu * sin(latitude) * cos(latitude) * pow(sin1, 2) * k0 * (100000000) / 2;
			K3 = ((pow(sin1, 4) * nu * sin(latitude) * Math.pow(cos(latitude),
					3)) / 24)
					* (5 - pow(tan(latitude), 2) + 9 * e1sq
							* pow(cos(latitude), 2) + 4 * pow(e1sq, 2)
							* pow(cos(latitude), 4))
					* k0
					* (10000000000000000L);

			K4 = nu * cos(latitude) * sin1 * k0 * 10000;

			K5 = pow(sin1 * cos(latitude), 3)
					* (nu / 6)
					* (1 - pow(tan(latitude), 2) + e1sq * pow(cos(latitude), 2))
					* k0 * 1000000000000L;

			A6 = (pow(p * sin1, 6) * nu * sin(latitude) * pow(cos(latitude), 5) / 720)
					* (61 - 58 * pow(tan(latitude), 2) + pow(tan(latitude), 4)
							+ 270 * e1sq * pow(cos(latitude), 2) - 330 * e1sq
							* pow(sin(latitude), 2)) * k0 * (1E+24);

		}

		protected String getLongZone(double longitude) {
			double longZone = 0;
			
			if (longitude < 0.0) {
				longZone = ((180.0 + longitude) / 6) + 1;
			} else {
				longZone = (longitude / 6) + 31;
			}
			
			String val = String.valueOf((int) longZone);
			
			if (val.length() == 1) {
				val = "0" + val;
			}
			
			return val;
		}

		protected double getNorthing(double latitude) {
			double northing = K1 + K2 * p * p + K3 * pow(p, 4);
			
			if (latitude < 0.0) {
				northing = 10000000 + northing;
			}
			
			return northing;
		}

		protected double getEasting() {
			return 500000 + (K4 * p + K5 * pow(p, 3));
		}

		// Lat Lon to UTM variables

		// equatorial radius
		double equatorialRadius = 6378137;

		// polar radius
		double polarRadius = 6356752.314;

		// scale factor
		double k0 = 0.9996;

		// eccentricity
		double e = Math.sqrt(1 - pow(polarRadius / equatorialRadius, 2));

		double e1sq = e * e / (1 - e * e);

		// r curv 1
		double rho = 6368573.744;

		// r curv 2
		double nu = 6389236.914;

		// Calculate Meridional Arc Length
		// Meridional Arc
		double S = 5103266.421;

		double A0 = 6367449.146;

		double B0 = 16038.42955;

		double C0 = 16.83261333;

		double D0 = 0.021984404;

		double E0 = 0.000312705;

		// Calculation Constants
		// Delta Long
		double p = -0.483084;

		double sin1 = 4.84814E-06;

		// Coefficients for UTM Coordinates
		double K1 = 5101225.115;

		double K2 = 3750.291596;

		double K3 = 1.397608151;

		double K4 = 214839.3105;

		double K5 = -2.995382942;

		double A6 = -1.00541E-07;

	}

	private static class LatZones {
		private char[] letters = { 'A', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K',
				'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Z' };

		private int[] degrees = { -90, -84, -72, -64, -56, -48, -40, -32, -24,
				-16, -8, 0, 8, 16, 24, 32, 40, 48, 56, 64, 72, 84 };

		private char[] negLetters = { 'A', 'C', 'D', 'E', 'F', 'G', 'H', 'J',
				'K', 'L', 'M' };

		private int[] negDegrees = { -90, -84, -72, -64, -56, -48, -40, -32,
				-24, -16, -8 };

		private char[] posLetters = { 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
				'W', 'X', 'Z' };

		private int[] posDegrees = { 0, 8, 16, 24, 32, 40, 48, 56, 64, 72, 84 };

		private int arrayLength = 22;

		public LatZones() {
		}

		public String getLatZone(double latitude) {
			int latIndex = -2;
			int lat = (int) latitude;

			if (lat >= 0) {
				int len = posLetters.length;
				for (int i = 0; i < len; i++) {
					if (lat == posDegrees[i]) {
						latIndex = i;
						break;
					}

					if (lat > posDegrees[i]) {
						continue;
					} else {
						latIndex = i - 1;
						break;
					}
				}
			} else {
				int len = negLetters.length;
				for (int i = 0; i < len; i++) {
					if (lat == negDegrees[i]) {
						latIndex = i;
						break;
					}

					if (lat < negDegrees[i]) {
						latIndex = i - 1;
						break;
					} else {
						continue;
					}

				}

			}

			if (latIndex == -1) {
				latIndex = 0;
			}
			if (lat >= 0) {
				if (latIndex == -2) {
					latIndex = posLetters.length - 1;
				}
				return String.valueOf(posLetters[latIndex]);
			} else {
				if (latIndex == -2) {
					latIndex = negLetters.length - 1;
				}
				return String.valueOf(negLetters[latIndex]);

			}
		}

	}

}
