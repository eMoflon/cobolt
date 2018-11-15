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

package de.tud.kom.p2psim.impl.topology.views.latency;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.tud.kom.p2psim.api.linklayer.mac.Link;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.MacLayer;
import de.tud.kom.p2psim.api.topology.views.LatencyDeterminator;
import de.tud.kom.p2psim.api.topology.views.TopologyView;
import de.tud.kom.p2psim.impl.network.modular.common.GNPToolkit;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;
import de.tud.kom.p2psim.impl.topology.TopologyFactory;
import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * Latency as provided by the GNP-Model in {@link NetMeasurementDB}
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 24.07.2012
 */
public class GNPLatency implements LatencyDeterminator {

	private final Map<MacAddress, NetMeasurementDB.Host> hostMetas = new LinkedHashMap<MacAddress, NetMeasurementDB.Host>();

	@Override
	public void onMacAdded(MacLayer mac, TopologyView viewParent) {
		hostMetas.put(mac.getMacAddress(),
				TopologyFactory.getMeasurementDBHost(mac.getHost()));
	}

	@Override
	public long getLatency(TopologyView view, MacAddress source,
			MacAddress destination, Link link) {

		if (link != null) {
			return link.getLatency();
		}

		List<Double> sndCoords = hostMetas.get(source).getCoordinates();
		List<Double> rcvCoords = hostMetas.get(destination).getCoordinates();

		return (long) (GNPToolkit.getDistance(sndCoords, rcvCoords) * 0.5 * Time.MILLISECOND);
		// divided by 2 because we have the RTT, but want the delay

	}

}
