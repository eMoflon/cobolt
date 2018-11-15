package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common;

import de.tud.kom.p2psim.impl.topology.views.LogicalWifiTopologyView;
import de.tud.kom.p2psim.impl.topology.views.LogicalWifiTopologyView.LogicalWiFiTopology;
import de.tud.kom.p2psim.impl.util.oracle.GlobalOracle;
import de.tudarmstadt.maki.simonstrator.api.Oracle;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.topology.AdaptableTopologyProvider;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyID;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;
import de.tudarmstadt.maki.simonstrator.tc.component.SimpleTopologyProvider;

public class GlobalOracleTopologyProvider implements SimpleTopologyProvider {

	private final TopologyID topologyID;

	@XMLConfigurableConstructor({ "role" })
	public GlobalOracleTopologyProvider(final String role) {
		this.topologyID = calculateTopologyId(role);
	}

	public TopologyID calculateTopologyId(final String role) {
		switch (role) {
		case "UDG":
			return LogicalWifiTopologyView.getUDGTopologyID();
		case "Logical":
			return LogicalWifiTopologyView.getAdaptableTopologyID();
		default:
			throw new IllegalArgumentException("Unsupported topology role: '" + role + "'");
		}
	}

	@Override
	public TopologyID getTopologyID() {
		return topologyID;
	}

	@Override
	public Graph getTopology() {
		return GlobalOracle.getTopology(LogicalWiFiTopology.class, this.topologyID);
	}

	@Override
	public AdaptableTopologyProvider getTopologyComponent(final INodeID node) {
		try {
			return Oracle.getHostByID(node).getComponent(LogicalWiFiTopology.class);
		} catch (ComponentNotAvailableException e) {
			throw new AssertionError("No TopologyComponent on node " + node.toString());
		}
	}

}
