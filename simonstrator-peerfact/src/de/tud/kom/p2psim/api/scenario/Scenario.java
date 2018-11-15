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



package de.tud.kom.p2psim.api.scenario;

import java.util.List;
import java.util.Map;

import de.tud.kom.p2psim.api.common.SimHost;

/**
 * Scenario configures hosts to make them usable for simulator, i.e. mainly
 * creating some initial events. Different implementations may offer different
 * ways to prepare a simulation run.
 * 
 * @author Sebastian Kaune
 * @author Konstantin Pussep
 * @version 3.0, 03.12.2007
 */
public interface Scenario {

	/**
	 * Prepare the scenario to be started. Typically, a scenario would schedule
	 * some initial simulation events.
	 * 
	 */
	public void prepare();

	/**
	 * Set all hosts available for this scenario.
	 * 
	 * @param allHosts
	 *            - groups of hosts indexed by group ids
	 */
	public void setHosts(Map<String, List<SimHost>> allHosts);

	/**
	 * Returns all hosts of the scenario, ordered by groups.
	 * 
	 * @return
	 */
	public Map<String, List<SimHost>> getHosts();

	/**
	 * Create (one or many) actions for a group of hosts (or a single host if
	 * group size is one).
	 * 
	 * @param groupID
	 *            - id of the host group
	 * @param timeInterval
	 *            - absolute point of time or an interval (then as
	 *            "starttime-endtime")
	 * @param methodName
	 *            - method name to execute
	 * @param params
	 *            - list of method params (as strings which will be converted to
	 *            appropriate types)
	 * @return number of actions created
	 */
	public int createActions(String groupID, String timeInterval,
			String methodName, String[] params);

}
