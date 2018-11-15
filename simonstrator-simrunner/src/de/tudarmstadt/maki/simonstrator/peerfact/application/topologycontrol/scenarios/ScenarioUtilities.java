package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios;

import java.util.Set;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.network.SimNetInterface;
import de.tud.kom.p2psim.impl.topology.views.LogicalWifiTopologyView;
import de.tud.kom.p2psim.impl.topology.views.LogicalWifiTopologyView.LogicalWiFiTopology;
import de.tud.kom.p2psim.impl.util.oracle.GlobalOracle;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

public class ScenarioUtilities {

	public static Set<INodeID> getNeighbors(final Host host) {
		Graph topology = GlobalOracle.getTopology(LogicalWiFiTopology.class,
				LogicalWifiTopologyView.getUDGTopologyID());
		return topology.getNeighbors(getNode(host));
	}

	public static INode getNode(final Host host) {
		try {
			return host.getComponent(LogicalWiFiTopology.class).getNode(LogicalWifiTopologyView.getUDGTopologyID());
		} catch (ComponentNotAvailableException e) {
			// Should never happen
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	public static SimHost findHost(final INodeID neighbor) {
		for (final SimHost host : GlobalOracle.getHosts()) {
			if (neighbor.equals(getNode(host).getId())) {
				return host;
			}
		}
		return null;
	}

	public static NetID getNetID(SimHost host) {
		SimNetInterface networkInterface = host.getNetworkComponent().getSimNetworkInterfaces().iterator().next();
		NetID netId = networkInterface.getNetID();
		return netId;
	}

}
