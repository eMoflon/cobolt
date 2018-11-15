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



package de.tud.kom.p2psim.api.churn;

import java.util.List;

import de.tud.kom.p2psim.api.common.SimHost;

/**
 * The churn model describes the churn behavior of peers within a simulation
 * run. For instance, simple distribution such as exponential, lognormal or even
 * more complex models are conceivable.
 * 
 * @author Sebastian Kaune
 * @author Konstantin Pussep
 * @version 3.0, 12/03/2007
 * 
 */
public interface ChurnModel {

	/**
	 * Gets the next uptime of a given host. The uptime defines the time how
	 * long the given host will stay online.
	 * 
	 * @param host
	 *            the specified host
	 * 
	 * @return time to stay online
	 */
	public long getNextUptime(SimHost host);

	/**
	 * Gets the next downtime of a given host. The downtime defines the time how
	 * long the given host will stay offline.
	 * 
	 * @param host
	 *            the specified host
	 * 
	 * @return time to stay offline
	 */
	public long getNextDowntime(SimHost host);

	/**
	 * Invoking this method prepares the churn model for its future activation.
	 * All relevant information can be accessed by the given list of existing
	 * hosts which will be affected by churn
	 * 
	 * @param churnHosts
	 *            the host which will be affected by churn
	 */
	public void prepare(List<SimHost> churnHosts);

}
