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

import de.tud.kom.p2psim.api.network.NetLayer;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

public class GeoLocationOracle {

	private static GnpSubnet subnet;

	private static GnpLatencyModel lm;

	GeoLocationOracle(GnpSubnet subnet) {
		this.subnet = subnet;
	}

	void setLatencyModel(GnpLatencyModel lm) {
		this.lm = lm;
	}

	/**
	 * This method determines the priority level of the specified remote host by
	 * considering the geographical(-regional) underlay awareness between this
	 * host and the specified local host.
	 * 
	 * The priority level is calculated as follows. Both hosts are located in
	 * the same: - city => Priority 4 - region => Priority 3 - country =>
	 * Priority 2 - "continental" region => Priority 1 - world/untraceable =>
	 * Priority 0
	 * 
	 * @param local
	 *            IP-address of the local host
	 * @param remote
	 *            IP-address of the remote host
	 * @return the priority level
	 */

	public static int getGeoPriority(NetID local, NetID remote) {
		GeoLocation localGeo = subnet.getNetLayer(local).getGeoLocation();
		GeoLocation remoteGeo = subnet.getNetLayer(remote).getGeoLocation();
		if (!remoteGeo.getCity().equals("--")
				&& localGeo.getCity().equals(remoteGeo.getCity()))
			return 4;
		else if (!remoteGeo.getRegion().equals("--")
				&& localGeo.getRegion().equals(remoteGeo.getRegion()))
			return 3;
		else if (!remoteGeo.getCountryCode().equals("--")
				&& localGeo.getCountryCode().equals(remoteGeo.getCountryCode()))
			return 2;
		else if (!remoteGeo.getContinentalArea().equals("--")
				&& localGeo.getContinentalArea().equals(
						remoteGeo.getContinentalArea()))
			return 1;
		else
			return 0;
	}

	/**
	 * Normally, the propagation of messages through channels and routers of the
	 * Internet is affected by the propagation delays (of the physical media),
	 * and the processing-, queuing-, and transmission delays of the routers.
	 * The so called Internet propagation delay is modeled as the sum of a fixed
	 * part that combines the aforementioned router and propagation delays, and
	 * a variable part to reproduce the jitter.
	 * 
	 * Invoking this method returns the Internet propagation delay (in ms)
	 * between two hosts in the Internet. Note that this delay is derived from
	 * measurement data, and it therefore estimates the one-way delay of the
	 * measured round-trip-times between the specified hosts.
	 * 
	 * @param local
	 *            IP-address of the local host
	 * @param remote
	 *            IP-address of the remote host
	 * @return the Internet propagation delay in ms
	 */
	public static double getInternetPropagationDelay(NetID local, NetID remote) {
		NetLayer localNet = subnet.getNetLayer(local);
		NetLayer remoteNet = subnet.getNetLayer(remote);
		return lm.getPropagationDelay((GnpNetLayer) localNet,
				(GnpNetLayer) remoteNet) / (double) Time.MILLISECOND;
	}

	/**
	 * Calculates the distance in kilometers (km) from one host to another,
	 * using the Haversine formula. The squashed shape of the earth into account
	 * (approximately)
	 * 
	 * @param local
	 *            IP-address of the local host
	 * @param remote
	 *            IP-address of the remote host
	 * @return the distance between the specified hosts in km
	 * 
	 */
	public static double getGeographicalDistance(NetID local, NetID remote) {
		GeoLocation localGeo = subnet.getNetLayer(local).getGeoLocation();
		GeoLocation remoteGeo = subnet.getNetLayer(remote).getGeoLocation();

		double lat1 = HaversineHelpers.radians(localGeo.getLatitude());
		double lat2 = HaversineHelpers.radians(remoteGeo.getLatitude());
		double dlat = lat2 - lat1;
		double dlong = HaversineHelpers.radians(remoteGeo.getLongitude())
				- HaversineHelpers.radians(localGeo.getLongitude());

		double a = HaversineHelpers.square(Math.sin(dlat / 2)) + Math.cos(lat1)
				* Math.cos(lat2) * HaversineHelpers.square(Math.sin(dlong / 2));

		// angle in radians formed by start point, earth's center, & end point
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		// radius of earth at midpoint of route
		double r = HaversineHelpers.globeRadiusOfCurvature((lat1 + lat2) / 2);
		return (r * c) / 1000d;
	}

}
