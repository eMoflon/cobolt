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

/**
 * 
 */
package de.tud.kom.p2psim.impl.topology;

/**
 * @author Christian Gross
 * @version vom 26.02.2013
 */
public class ExtendedPositionVector extends PositionVector {

	
	private double lon;
	private double lat;

	public ExtendedPositionVector(double x, double y, double lon, double lat) {
		super(x,y);
		this.lon = lon;
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public double getLat() {
		return lat;
	}
	
	public String toString() {
		return "X/Y: [" + getX() + ", " + getY() + "], Long/Lat: [" + lon + "," + lat +"]";
	}
	
}
