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

package de.tud.kom.p2psim.impl.network.fairshareng;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.impl.network.AbstractNetLayerFactory;
import de.tud.kom.p2psim.impl.network.IPv4NetID;
import de.tud.kom.p2psim.impl.network.fairshareng.livemon.FairshareLiveMonitoring;
import de.tud.kom.p2psim.impl.network.modular.DBHostListManager;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;
import de.tud.kom.p2psim.impl.network.modular.st.LatencyStrategy;
import de.tud.kom.p2psim.impl.network.modular.st.PLossStrategy;
import de.tud.kom.p2psim.impl.network.modular.st.PositioningStrategy;
import de.tud.kom.p2psim.impl.network.modular.st.positioning.GNPPositioning;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Randoms;

/**
 * A factory for creating FairshareNetLayer objects.
 */

public class FairshareNetLayerFactory extends AbstractNetLayerFactory {

	/** The subnet. */
	private final FairshareSubnet subnet;

	private DBHostListManager dbHostList;

	private PositioningStrategy strategy_positioning;

	private boolean useRegionGroups = true;

	/**
	 * Instantiates a new fairshare net layer factory.
	 */
	public FairshareNetLayerFactory() {
		this.subnet = new FairshareSubnet();
		this.strategy_positioning = new GNPPositioning();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.tud.kom.p2psim.api.common.ComponentFactory#createComponent(de.tud.
	 * kom.p2psim.api.common.Host)
	 */
	@Override
	public FairshareNode createComponent(Host phost) {

		SimHost host = (SimHost) phost;

		final String groupStr = host.getProperties().getGroupID();

		NetMeasurementDB.Host hostMeta;
		if (db != null) {
			subnet.setDB(db);
			if (useRegionGroups) {
				// In case of a DB presence, look up the host's specific
				// metadata there
				NetMeasurementDB.Group g = db.getStringAddrObjFromStr(
						NetMeasurementDB.Group.class, groupStr);
				if (g == null)
					throw new IllegalArgumentException(
							"There is no group named '" + groupStr + "'");
				hostMeta = g.tGetNextMember();
			} else {
				// The hosts are not grouped by their region name, we will
				// return random hosts in the world for each group.
				if (dbHostList == null)
					dbHostList = new DBHostListManager(db);
				hostMeta = dbHostList.getNextHost();
			}
		} else {
			hostMeta = null;
		}

		IPv4NetID id;
		if (hostMeta != null) {
			id = new IPv4NetID(IPv4NetID.intToLong(hostMeta.getId()));
		} else {
			id = new IPv4NetID(IPv4NetID.intToLong(Randoms.getRandom(
					FairshareNetLayerFactory.class).nextInt()));
		}

		final FairshareNode newHost = new FairshareNode(host, this.subnet, id,
				getBandwidth(id), strategy_positioning.getPosition(host, db,
						hostMeta), hostMeta);
		this.subnet.registerNetLayer(newHost);

		return newHost;

	}

	/**
	 * Sets the latency strategy.
	 * 
	 * @param latency
	 *            the new latency
	 */
	public void setLatency(LatencyStrategy latency) {
		this.subnet.setLatencyStrategy(latency);
	}

	/**
	 * Sets the positioning strategy.
	 * 
	 * @param positioning
	 *            the new positioning
	 */
	public void setPositioning(PositioningStrategy positioning) {
		strategy_positioning = positioning;
	}

	/**
	 * Sets the packet loss strategy.
	 * 
	 * @param pLoss
	 *            the new ploss
	 */
	public void setUDPPLoss(PLossStrategy pLoss) {
		this.subnet.setPLossStrategy(pLoss);
	}

	/**
	 * Sets the use subgraph discovery.
	 * 
	 * @param use
	 *            the new use subgraph discovery
	 */
	public void setUseSubgraphDiscovery(boolean use) {
		subnet.useSubgraphDiscovery(use);
	}

	/**
	 * Sets the use region groups.
	 * 
	 * @param useRegionGroups
	 *            the new use region groups
	 */
	public void setUseRegionGroups(boolean useRegionGroups) {
		this.useRegionGroups = useRegionGroups;
	}

	/**
	 * Sets the use of monitor.
	 * 
	 * @param use
	 *            the new use monitor
	 */
	public void setUseMonitor(boolean use) {

		if (use) {
			FairshareLiveMonitoring.register();
		}

		subnet.useMonitor(use);
	}

	@Override
	public void writeBackToXML(BackWriter bw) {
		super.writeBackToXML(bw);
		bw.writeSimpleType("useRegionGroups", useRegionGroups);
		bw.writeComplexType("Subnet", subnet);
		// bw.writeComplexType("PLoss", strategies.pLoss);
		// bw.writeComplexType("Latency", st);
		bw.writeComplexType("Positioning", strategy_positioning);
	}

}
