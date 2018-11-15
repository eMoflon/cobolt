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

package de.tud.kom.p2psim.impl.util.db.dao.metric;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.impl.util.db.dao.DAO;
import de.tud.kom.p2psim.impl.util.db.metric.HostMetric;
import de.tud.kom.p2psim.impl.util.db.metric.Metric;
import de.tud.kom.p2psim.impl.util.oracle.GlobalOracle;

/**
 * Data Access Object to store and retrieve {@link HostMetric} instances.
 * 
 * @author Andreas Hemel
 */
public class HostMetricDAO extends DAO {

	/** Cache of {@link HostMetric} objects to avoid database lookups. */
	private static Map<Metric, Map<Long, HostMetric>> hostMetricCache = new HashMap<Metric, Map<Long, HostMetric>>();

	/**
	 * Retrieve the {@link HostMetric} object for the given metric and host id.
	 * 
	 * If there is no matching Metric object, it is created, persisted, and
	 * cached automatically.
	 */
	public static HostMetric lookupHostMetric(Metric metric, long hostId) {
		Map<Long, HostMetric> metricMap = hostMetricCache.get(metric);

		if (metricMap == null) {
			metricMap = new HashMap<Long, HostMetric>();
			if (hostId != -1) {
				List<SimHost> allPeers = GlobalOracle.getHosts();
				for (SimHost h : allPeers) {
					HostMetric hostMetric = new HostMetric(metric, h.getHostId(), h.getProperties().getGroupID());
					metricMap.put(h.getHostId(), hostMetric);
					addToPersistQueue(hostMetric);
				}
			} else{
				HostMetric hostMetric = new HostMetric(metric, hostId, "__GLOBAL__");
				metricMap.put(hostId, hostMetric);
				addToPersistQueue(hostMetric);
			}
			
			hostMetricCache.put(metric, metricMap);
			// TODO: block commit and avoid multiple threads for commit of first
			// object definition
			commitQueue();
			// finishCommits();

		}

		return metricMap.get(hostId);

	}
}
