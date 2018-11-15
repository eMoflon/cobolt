package de.tud.kom.p2psim.impl.util.positioning;

import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

import de.tud.kom.p2psim.impl.network.modular.common.GeoToolkit;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.common.Transmitable;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;

/** Immutable geographical position implementation based on a spherical Earth model.
 *
 * Latitude and Longitude are internally stored in radians to avoid
 * unnecessary conversions between degrees and radians.
 *
 * @author Andreas Hemel
 */
public class GeoSpherePosition implements Transmitable, Location {

	/** Latitude in radians */
	private final double latitude;

	/** Longitude in radians */
	private final double longitude;

	/** Earth's mean radius in meters according to Wikipedia */
	private static final double earthRadius = 6371000;

	// FIXME: try to set this to zero
	private static final double deviation = 0.0000001;

	//private static final Logger log = SimLogger.getLogger(GeoPosition.class);

	private static final Map<GeoSpherePosition, Map<GeoSpherePosition, Double>> distanceCache =
			new WeakHashMap<GeoSpherePosition, Map<GeoSpherePosition, Double>>();
	private static final Map<GeoSpherePosition, Map<GeoSpherePosition, Double>> bearingCache =
			new WeakHashMap<GeoSpherePosition, Map<GeoSpherePosition, Double>>();

	private static long distanceCacheHits = 0;
	private static long distanceCacheMisses = 0;

	private static long bearingCacheHits = 0;
	private static long bearingCacheMisses = 0;

	private static final boolean enableDistanceCache = true;
	private static final boolean enableBearingCache = false;

	public GeoSpherePosition(double latitude, double longitude, boolean isRadians) {
		if (isRadians) {
			this.latitude = latitude;
			this.longitude = longitude;
		} else {
			if (latitude > 90 || latitude < -90)
				throw new AssertionError("invalid latitude: "+latitude);
			if (longitude > 180 || longitude < -180)
				throw new AssertionError("invalid longitude: "+longitude);
			this.latitude = Math.toRadians(latitude);
			this.longitude = Math.toRadians(longitude);
		}
		if (this.latitude > Math.PI/2 + deviation || this.latitude < -(Math.PI/2) - deviation)
			throw new AssertionError("invalid latitude: "+latitude);
		if (this.longitude > Math.PI + deviation || this.longitude < -Math.PI - deviation)
			throw new AssertionError("invalid longitude: "+longitude);
	}

	/** Constructor for degrees
	 *
	 * @param latitude Latitude in degrees
	 * @param longitude Longitude in degrees
	 */
	public GeoSpherePosition(double latitude, double longitude) {
		this(latitude, longitude, false);
	}

	public static GeoSpherePosition createRandom() {
		return createRandom(0);
	}

	public static GeoSpherePosition createRandom(double poleExclusion) {
		Random rnd = Randoms.getRandom(GeoSpherePosition.class);

		double latRange = 180 - poleExclusion; // leave out the area near the poles
		double longRange = 360;
		double latitude = rnd.nextDouble() * latRange - (latRange / 2);
		double longitude = rnd.nextDouble() * longRange - (longRange / 2);

		return new GeoSpherePosition(latitude, longitude);
	}

	/** Get the latitude in degrees */
	@Override
	public double getLatitude() {
		return Math.toDegrees(latitude);
	}

	/** Get the longitude in degrees */
	@Override
	public double getLongitude() {
		return Math.toDegrees(longitude);
	}

	/** Get the latitude in radians */
	public double getLatitudeRad() {
		return latitude;
	}

	/** Get the longitude in radians */
	public double getLongitudeRad() {
		return longitude;
	}

	@Override
	public int getTransmissionSize() {
		return 16; // 2 * sizeof(double)
	}

	private double square(double x) { return x*x; }

	/** Calculate the distance to target on a great circle using the Haversine
	 * Formula.
	 *
	 * This formula assumes a spherical Earth, so this calculation has
	 * a slight error, but it should be consistent with getDestination().
	 *
	 * based on:
	 * {@link GeoToolkit}
	 * http://en.wikipedia.org/wiki/Haversine_formula
	 *
	 * @return The distance in meters.
	 */
	@Override
	public double distanceTo(Location destination) {
		GeoSpherePosition dest = (GeoSpherePosition) destination;
		if (enableDistanceCache) {
			Double cached = checkDistanceCache(this, dest);
			if (cached != null)
				return cached;
		}

		double lat1 = this.latitude;
		double lat2 = dest.latitude;
		double dlat = lat2 - lat1;
		double dlong = dest.longitude - this.longitude;

		double a =
				square(Math.sin(dlat / 2d)) +
				Math.cos(lat1) * Math.cos(lat2) * square(Math.sin(dlong / 2d));

		// This is the formula from Wikipedia. It is slightly faster for short
		// distances, e.g. mainz -> ffm
		//double distance = Math.asin(Math.sqrt(a)) * 2d * earthRadius;

		// This is the formula from GeoToolkit. It up to two times faster for long
		// distances, e.g. nyc -> tokyo
		double distance = Math.atan2(Math.sqrt(a), Math.sqrt(1d - a)) * 2d * earthRadius;

		if (enableDistanceCache)
			putDistanceCache(this, dest, distance);

		return distance;
	}

	@Override
	public float bearingTo(Location target) {
		return (float) (-getBearing(target) + 180);
	}

	/** Calculate the initial bearing to target on a great circle in degrees.
	 *
	 * Range is between 0 and 360 degrees.
	 * 0° is north, 90° is east, 180° is south and 270° is west.
	 *
	 * based on:
	 * http://www.movable-type.co.uk/scripts/latlong.html
	 *
	 * @return The initial bearing in degrees.
	 */
	public double getBearing(Location target) {
		return Math.toDegrees(getBearingRad(target));
	}

	/** Calculate the initial bearing to target on a great circle in radians.
	 *
	 * based on:
	 * http://www.movable-type.co.uk/scripts/latlong.html
	 *
	 * @return The initial bearing in radians.
	 */
	public double getBearingRad(Location destination) {
		GeoSpherePosition dest = (GeoSpherePosition) destination;
		if (enableBearingCache) {
			Double cached = checkBearingCache(this, dest);
			if (cached != null)
				return cached;
		}

		double lat1 = this.latitude;
		double lat2 = dest.latitude;
		double dLon = dest.longitude - this.longitude;

		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1) * Math.sin(lat2) -
					Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
		double bearing = Math.atan2(y, x);

		bearing = (bearing + 2*Math.PI) % (2*Math.PI);

		if (enableBearingCache)
			putBearingCache(this, dest, bearing);

		return bearing;
	}

	/** Calculate the destination position given a bearing and a distance.
	 *
	 * The formulae used here assume a spherical Earth, so this calculation has
	 * a slight error, but it should be consistent with getDistance().
	 *
	 * based on:
	 * http://www.movable-type.co.uk/scripts/latlong.html
	 *
	 * @param bearing Bearing in degrees
	 * @param distance Distance in meters
	 */
	public GeoSpherePosition getDestination(double bearing, double distance) {
		return getDestinationRad(Math.toRadians(bearing), distance);
	}

	/** Calculate the destination position given a bearing and a distance.
	 *
	 * The formulae used here assume a spherical Earth, so this calculation has
	 * a slight error, but it should be consistent with getDistance().
	 *
	 * based on:
	 * http://www.movable-type.co.uk/scripts/latlong.html
	 *
	 * @param bearing Bearing in radians
	 * @param distance Distance in meters
	 */
	public GeoSpherePosition getDestinationRad(double bearing, double distance) {
		double lat1 = latitude;
		double lon1 = longitude;
		double radDist = distance / earthRadius;

		double lat2 = Math.asin(Math.sin(lat1) * Math.cos(radDist) +
				Math.cos(lat1) * Math.sin(radDist) * Math.cos(bearing));
		double dlon = Math.atan2(
				Math.sin(bearing) * Math.sin(radDist) * Math.cos(lat1),
				Math.cos(radDist) - Math.sin(lat1) * Math.sin(lat2));
		double lon2 = lon1 + dlon;

		if (lon2 > Math.PI)
			lon2 -= 2*Math.PI;
		if (lon2 < -Math.PI)
			lon2 += 2*Math.PI;

		return new GeoSpherePosition(lat2, lon2, true);
	}

	@Override
	public String toString() {
		return "GeoSpherePos["+getLatitude()+";"+getLongitude()+"]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(latitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(longitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof GeoSpherePosition))
			return false;
		GeoSpherePosition other = (GeoSpherePosition) obj;
		if (Double.doubleToLongBits(latitude) != Double
				.doubleToLongBits(other.latitude))
			return false;
		if (Double.doubleToLongBits(longitude) != Double
				.doubleToLongBits(other.longitude))
			return false;
		return true;
	}

	/** This class is immutable, no clone needed. */
	@Override
	public GeoSpherePosition clone() {
		return this;
	}

	private static Double checkDistanceCache(GeoSpherePosition a, GeoSpherePosition b) {
		GeoSpherePosition first, second;
		if (a.longitude < b.longitude) {
			first = a;
			second = b;
		} else if (a.longitude > b.longitude) {
			first = b;
			second = a;
		} else if (a.latitude < b.latitude) {
			first = a;
			second = b;
		} else {
			first = b;
			second = a;
		}

		Double result;
		Map<GeoSpherePosition, Double> map = distanceCache.get(first);
		if (map == null)
			result = null;
		else
			result = map.get(second);

		if (result != null)
			distanceCacheHits++;
		else
			distanceCacheMisses++;

		return result;
	}

	private static void putDistanceCache(GeoSpherePosition a, GeoSpherePosition b, double dist) {
		GeoSpherePosition first, second;
		if (a.longitude < b.longitude) {
			first = a;
			second = b;
		} else if (a.longitude > b.longitude) {
			first = b;
			second = a;
		} else if (a.latitude < b.latitude) {
			first = a;
			second = b;
		} else {
			first = b;
			second = a;
		}

		Map<GeoSpherePosition, Double> map = distanceCache.get(first);
		if (map == null) {
			map = new WeakHashMap<GeoSpherePosition, Double>();
			distanceCache.put(first, map);
		}
		map.put(second, dist);
	}

	private static Double checkBearingCache(GeoSpherePosition a, GeoSpherePosition b) {
		GeoSpherePosition first, second;
		if (a.longitude < b.longitude) {
			first = a;
			second = b;
		} else if (a.longitude > b.longitude) {
			first = b;
			second = a;
		} else if (a.latitude < b.latitude) {
			first = a;
			second = b;
		} else {
			first = b;
			second = a;
		}

		Double result;
		Map<GeoSpherePosition, Double> map = bearingCache.get(first);
		if (map == null)
			result = null;
		else
			result = map.get(second);

		if (result != null)
			bearingCacheHits++;
		else
			bearingCacheMisses++;

		return result;
	}

	private static void putBearingCache(GeoSpherePosition a, GeoSpherePosition b, double dist) {
		GeoSpherePosition first, second;
		if (a.longitude < b.longitude) {
			first = a;
			second = b;
		} else if (a.longitude > b.longitude) {
			first = b;
			second = a;
		} else if (a.latitude < b.latitude) {
			first = a;
			second = b;
		} else {
			first = b;
			second = a;
		}

		Map<GeoSpherePosition, Double> map = bearingCache.get(first);
		if (map == null) {
			map = new WeakHashMap<GeoSpherePosition, Double>();
			bearingCache.put(first, map);
		}
		map.put(second, dist);
	}

	public static long getDistanceCacheHits() {
		return distanceCacheHits;
	}

	public static long getDistanceCacheMisses() {
		return distanceCacheMisses;
	}

	public static long getBearingCacheHits() {
		return bearingCacheHits;
	}

	public static long getBearingCacheMisses() {
		return bearingCacheMisses;
	}

	public static boolean isDistanceCacheEnabled() {
		return enableDistanceCache;
	}

	public static boolean isBearingCacheEnabled() {
		return enableBearingCache;
	}

	@Override
	public void set(Location l) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getAgeOfLocation() {
		throw new UnsupportedOperationException();
	}
}
