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

package de.tud.kom.p2psim.api.network;

import de.tudarmstadt.maki.simonstrator.api.component.network.NetInterface;

/**
 * 
 * An extension of the {@link NetInterface} to support access to various methods
 * required by the simulator-core (i.e., churn, energy model). In the simulator,
 * an individual {@link NetInterface} also acts as a {@link NetLayer}.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, Aug 5, 2013
 */
public interface SimNetInterface extends NetInterface, NetLayer {

	// marker, union of NetInterface and NetLayer within the simulator.

}
