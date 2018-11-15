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

/**
 * Interface for geolocation services
 * 
 * @author Gerald Klunker
 * @version 0.1, 09.01.2008
 * 
 */
public interface Geolocator {

	/**
	 * 
	 * @param IP
	 *            -address in a readable form like "130.83.47.128"
	 * @return true, if IP could be located
	 */
	public boolean search(String ip);

	/**
	 * 
	 * @param 32-bit IP-address
	 * @return true, if IP could be located
	 */
	public boolean search(int ip);

	/**
	 * 
	 * @return latitude of last search result, return nan, if ip could not be
	 *         located
	 */
	public double getLatitude();

	/**
	 * 
	 * @return longitude of last search result, return nan, if ip could not be
	 *         located
	 */
	public double getLongitude();

	/**
	 * 
	 * @return 2-digit country code of last search result, return null, if ip
	 *         could not be located
	 */
	public String getCountryCode();

	/**
	 * 
	 * @return country name of last search result , return null, if ip could not
	 *         be located
	 */
	public String getCountryName();

	/**
	 * 
	 * @return region name of last search result , return null, if ip could not
	 *         be located
	 */
	public String getRegionName();

	/**
	 * 
	 * @return city name of last search result , return null, if ip could not be
	 *         located
	 */
	public String getCityName();

}
