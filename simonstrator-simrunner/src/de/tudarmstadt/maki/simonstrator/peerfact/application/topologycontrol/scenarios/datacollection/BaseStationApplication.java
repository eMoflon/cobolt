package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.datacollection;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.network.SimNetInterface;
import de.tud.kom.p2psim.impl.topology.DefaultTopologyComponent;
import de.tud.kom.p2psim.impl.util.oracle.GlobalOracle;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetworkComponent.NetInterfaceName;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyID;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.TopologyControlEvaluationApplicationConfig;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.TopologyControlEvaluationApplication_ImplBase;

public class BaseStationApplication extends TopologyControlEvaluationApplication_ImplBase {

	private static NetID baseStationAddress = null;
	private static SimHost baseStationHost = null;

	public BaseStationApplication(final TopologyControlEvaluationApplicationConfig config) {
		super(updateConfig(config));
	}

	@Override
	public void initialize() {
		super.initialize();
		SimNetInterface networkInterface = this.getHost().getNetworkComponent().getSimNetworkInterfaces().iterator()
				.next();
		BaseStationApplication.baseStationAddress = networkInterface.getNetID();
	}

	@Override
	protected void doReceiveMessage(Message msg,
			de.tudarmstadt.maki.simonstrator.api.component.transport.TransInfo sender, int commID) {
		Monitor.log(BaseStationApplication.class, Level.DEBUG,
				String.format("Message recieved at the base station [source=%s:%s,commID=%d]", sender.getNetId(),
						sender.getPort(), commID));
		this.notifyMessageReceived(msg);

		final int numReceivedMessages = getTotalMessageReceivedCount();
		if (0 == numReceivedMessages % 10000) {
			Monitor.log(BaseStationApplication.class, Level.INFO,
					String.format("Messaged recieved at the base station: %,d", numReceivedMessages));
		}
	}

	public static NetID getBaseStationAddress() {
		return baseStationAddress;
	}

	public static SimHost getBaseStationHost() {
		if (baseStationHost == null) {
			baseStationHost = identifyBaseStationHost();
		}
		return baseStationHost;
	}

	/**
	 * Returns the base station node (if exists) and null otherwise.
	 * 
	 * @return
	 */
	public static INode getBaseStationNode() {
		try {
			final SimHost baseStationHost = getBaseStationHost();
			if (baseStationHost == null)
				return null;
			final DefaultTopologyComponent topoComponent = baseStationHost.getComponent(DefaultTopologyComponent.class);
			final TopologyID identifier = topoComponent.getTopologyID(NetInterfaceName.WIFI, false);
			final INode selfNode = topoComponent.getNode(identifier);
			return selfNode;
		} catch (ComponentNotAvailableException e) {
			return null;
		}
	}

	private static TopologyControlEvaluationApplicationConfig updateConfig(
			TopologyControlEvaluationApplicationConfig config) {
		config.setReceiver(true);
		return config;
	}

	private static SimHost identifyBaseStationHost() {
		for (final SimHost host : GlobalOracle.getHosts()) {
			try {
				host.getComponent(BaseStationApplication.class);
				return host;
			} catch (ComponentNotAvailableException e) {
				// ignore - just try the next host
			}
		}
		return null;
	}
}
