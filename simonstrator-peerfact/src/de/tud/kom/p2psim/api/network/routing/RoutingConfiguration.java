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

package de.tud.kom.p2psim.api.network.routing;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.impl.network.routed.RoutedNetLayer;

/**
 * Configuration interface for the routing-Algorithms inside the
 * {@link RoutedNetLayer}. Implementations of this Interface should be used
 * inside the config-xml to create the components for each host.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 26.04.2012
 */
public interface RoutingConfiguration {

	/**
	 * Return a fully configured {@link RoutingAlgorithm} for the given Host.
	 * 
	 * @param host
	 * @return
	 */
	public RoutingAlgorithm getConfiguredRoutingAlgorithm(SimHost host);

}
