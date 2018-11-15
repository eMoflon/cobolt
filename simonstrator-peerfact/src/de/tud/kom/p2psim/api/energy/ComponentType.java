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

package de.tud.kom.p2psim.api.energy;

import de.tud.kom.p2psim.api.linklayer.mac.PhyType;

/**
 * Components used in the Energy-Model. Within the model there may exist several
 * implementations for each type - for example WIFI and ETHERNET for the
 * COMMUNICATION-Sector
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 25.02.2012
 */
public enum ComponentType {

	/**
	 * This bundles all COMM-interfaces (WIFI, BLUETOOTH...) as defined in
	 * {@link PhyType}
	 */
	COMMUNICATION,
	/**
	 * A positioning component (GPS)
	 */
	POSITIONING,
	/**
	 * General CPU
	 */
	CPU,
	/**
	 * The display (during user interaction)
	 */
	DISPLAY,
	/**
	 * Other components which consume energy
	 */
	BASIC

}
