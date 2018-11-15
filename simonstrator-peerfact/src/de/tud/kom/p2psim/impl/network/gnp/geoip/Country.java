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

package de.tud.kom.p2psim.impl.network.gnp.geoip;

/**
 * Represents a country.
 * 
 * @author Matt Tucker
 */
public class Country {

	private String code;

	private String name;

	/**
	 * Creates a new Country.
	 * 
	 * @param code
	 *            the country code.
	 * @param name
	 *            the country name.
	 */
	public Country(String code, String name) {
		this.code = code;
		this.name = name;
	}

	/**
	 * Returns the ISO two-letter country code of this country.
	 * 
	 * @return the country code.
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Returns the name of this country.
	 * 
	 * @return the country name.
	 */
	public String getName() {
		return name;
	}
}
