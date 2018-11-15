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


package de.tud.kom.p2psim.impl.network.gnp.topology;

import java.io.File;
import java.io.IOException;

import de.tud.kom.p2psim.impl.network.gnp.geoip.Location;
import de.tud.kom.p2psim.impl.network.gnp.geoip.LookupService;
import de.tud.kom.p2psim.impl.network.gnp.geoip.regionName;

/**
 * Implementation of the MaxMind GeoIP geolocation service
 * 
 * @author Gerald Klunker
 * @version 0.1, 09.01.2008
 * 
 */
class GeolocatorGeoIP implements Geolocator {

	LookupService cl;

	Location l1;

	/**
	 * Initialize the GeoIP wrapper class with a binary GeoIP database file.
	 * There are also free Databases (GPL/LGPL) "GeoLite City"
	 * 
	 * @param db
	 */
	public GeolocatorGeoIP(File db) {
		super();
		try {
			cl = new LookupService(db, LookupService.GEOIP_MEMORY_CACHE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.tud.kom.p2psim.impl.network.gnp.topology.Geolocator#getCountryCode()
	 */
	public String getCountryCode() {
		if (l1 == null)
			return null;
		String cc = l1.countryCode;
		if (cc.equals("--") || cc.equals("A1") || cc.equals("A2")
				|| cc.equals("01"))
			return null;
		else
			return cc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.tud.kom.p2psim.impl.network.gnp.topology.Geolocator#getCountryName()
	 */
	public String getCountryName() {
		if (l1 != null)
			return l1.countryName;
		else
			return null;
	}

	public String getRegionName() {
		if (l1 != null) {
			String result = regionName.regionNameByCode(l1.countryCode,
					l1.region);
			if (result != null)
				result.replace("ü", "");
			return result;
		} else
			return null;
	}

	public String getCityName() {
		if (l1 != null) {
			String result = l1.city;
			if (result != null)
				result.replace("ü", "");
			return result;
		} else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tud.kom.p2psim.impl.network.gnp.topology.Geolocator#getLatitude()
	 */
	public double getLatitude() {
		if (l1 != null)
			return l1.latitude;
		else
			return Double.NaN;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.tud.kom.p2psim.impl.network.gnp.topology.Geolocator#getLongitude()
	 */
	public double getLongitude() {
		if (l1 != null)
			return l1.longitude;
		else
			return Double.NaN;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.tud.kom.p2psim.impl.network.gnp.topology.Geolocator#search(java.lang
	 * .String)
	 */
	public boolean search(String ip) {
		l1 = cl.getLocation(ip);
		if (l1 != null)
			return true;
		else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.tud.kom.p2psim.impl.network.gnp.topology.Geolocator#search(java.lang
	 * .Long)
	 */
	public boolean search(int ip) {
		l1 = cl.getLocation(ip);
		if (l1 != null)
			return true;
		else
			return false;
	}

}
