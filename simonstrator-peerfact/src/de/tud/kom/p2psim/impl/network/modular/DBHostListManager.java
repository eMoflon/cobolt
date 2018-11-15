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


package de.tud.kom.p2psim.impl.network.modular;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB.Host;
import de.tudarmstadt.maki.simonstrator.api.Randoms;

/**
 * If the hosts in the XML configuration file are not grouped by region names (useRegionGroups="false"),
 * this component will allow the net layer factory to get random hosts from the network measurement
 * database.
 * 
 * @author Leo Nobach
 *
 */
public class DBHostListManager {
	
	private List<Host> hostList;
	
	int hostPointer = 0;


	public DBHostListManager(NetMeasurementDB db) {
		this.hostList = getShuffledHostList(db);
	}
	
	/**
	 * Returns a shuffled host list from the given network measurement database.
	 * @param db
	 * @return
	 */
	public static List<NetMeasurementDB.Host> getShuffledHostList(NetMeasurementDB db) {
		List<NetMeasurementDB.Host> result = new ArrayList<NetMeasurementDB.Host>(db.getAllObjects(NetMeasurementDB.Host.class));
		Collections.shuffle(result, Randoms.getRandom(DBHostListManager.class));
		return result;
	}

	/**
	 * Returns the next available host from a shuffled host list.
	 * @return
	 */
	public NetMeasurementDB.Host getNextHost() {
		if (hostPointer >= hostList.size()) throw new IllegalStateException("No more hosts in the host list database.");
		Host result = hostList.get(hostPointer);
		hostPointer++;
		return result;
	}

}
