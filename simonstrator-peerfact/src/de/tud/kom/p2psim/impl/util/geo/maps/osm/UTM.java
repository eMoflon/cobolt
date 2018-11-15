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

package de.tud.kom.p2psim.impl.util.geo.maps.osm;

import de.tud.kom.p2psim.impl.topology.PositionVector;

/**
 * A simple bean that holds information about a UTM coordinate
 * including position, northing and easting zone.
 * 
 * @author Fabio Zöllner
 * @version 1.0, 09.05.2012
 */
public class UTM {
	private PositionVector position;
	private String northingZone = "";
	private String eastingZone = "";
	
	public UTM(PositionVector position, String longZone, String latZone) {
		this.setPosition(position);
		this.setNorthingZone(longZone);
		this.setNorthingZone(latZone);
	}

	public PositionVector getPosition() {
		return position;
	}

	public void setPosition(PositionVector position) {
		this.position = position;
	}

	public String getNorthingZone() {
		return northingZone;
	}

	public void setNorthingZone(String northingZone) {
		this.northingZone = northingZone;
	}

	public String getEastingZone() {
		return eastingZone;
	}

	public void setEastingZone(String eastingZone) {
		this.eastingZone = eastingZone;
	}
	
	public String toString() {
		return "UTM[" + position + ", " + northingZone + ", " + eastingZone + "]";
	}
}
