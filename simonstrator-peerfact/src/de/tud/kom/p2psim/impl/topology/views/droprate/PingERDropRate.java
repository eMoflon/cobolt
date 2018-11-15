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

package de.tud.kom.p2psim.impl.topology.views.droprate;

import java.util.LinkedHashMap;
import java.util.Map;

import de.tud.kom.p2psim.api.linklayer.mac.Link;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.MacLayer;
import de.tud.kom.p2psim.api.topology.views.DropProbabilityDeterminator;
import de.tud.kom.p2psim.api.topology.views.TopologyView;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB.SummaryRelation;
import de.tud.kom.p2psim.impl.network.modular.st.ploss.PingERPacketLoss;
import de.tud.kom.p2psim.impl.topology.TopologyFactory;

/**
 * This mimics the behavior of the {@link PingERPacketLoss}
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 24.07.2012
 */
public class PingERDropRate implements DropProbabilityDeterminator {

	private final Map<MacAddress, NetMeasurementDB.Host> hostMetas = new LinkedHashMap<MacAddress, NetMeasurementDB.Host>();

	
	private double packetDropFactor = 1.0;
	
	@Override
	public void onMacAdded(MacLayer mac, TopologyView viewParent) {
		hostMetas.put(mac.getMacAddress(),
				TopologyFactory.getMeasurementDBHost(mac.getHost()));
	}

	@Override
	public double getDropProbability(TopologyView view, MacAddress source,
			MacAddress destination, Link link) {

		if (link != null) {
			// No dynamic updates.
			return link.getDropProbability();
		}

		SummaryRelation sumRel = TopologyFactory.getMeasurementDB()
				.getMostAccurateSummaryRelation(hostMetas.get(source),
						hostMetas.get(destination));

		if (sumRel == null) {
			throw new AssertionError("No summary relation could be found!");
		}
		
		if (packetDropFactor < 0) {
			throw new AssertionError("Packet loss factor cannot be < 0!");
		}
		
		// ploss is returned in percent!
		return sumRel.getpLoss() * 0.01 * packetDropFactor;
	}
	
	
	public void setPacketDropFactor(double packetDropFactor) {
		this.packetDropFactor = packetDropFactor;
	}
}
