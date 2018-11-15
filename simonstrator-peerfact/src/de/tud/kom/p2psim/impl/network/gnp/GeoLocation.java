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

public class GeoLocation {

	private String countryCode;

	private String region;

	private String city;

	private String isp;

	private String continentalArea;

	private double latitude;

	private double longitude;

	public GeoLocation(String conArea, String countryCode, String region,
			String city, String isp, double latitude, double longitude) {
		super();
		this.continentalArea = conArea;
		this.countryCode = countryCode;
		this.region = region;
		this.city = city;
		this.isp = isp;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public String getContinentalArea() {
		return continentalArea;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public String getRegion() {
		return region;
	}

	public String getCity() {
		return city;
	}

	public String getIsp() {
		return isp;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

}
