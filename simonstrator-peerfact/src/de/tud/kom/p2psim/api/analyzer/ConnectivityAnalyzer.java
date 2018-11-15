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

package de.tud.kom.p2psim.api.analyzer;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent.Analyzer;

/**
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, Jul 8, 2013
 */
public interface ConnectivityAnalyzer extends Analyzer {

	/**
	 * Invoking this method denotes that the given host does not have network
	 * connectivity
	 * 
	 * @param host
	 *            the churn affected host
	 */
	public void wentOffline(SimHost host);

	/**
	 * Invoking this method denotes that the given host does have network
	 * connectivity
	 * 
	 * @param host
	 *            the churn affected host
	 */
	public void wentOnline(SimHost host);

}
