package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.alltoall;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.impl.util.oracle.GlobalOracle;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransInfo;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransMessageListener;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.ScenarioUtilities;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.TopologyControlEvaluationApplicationConfig;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.TopologyControlEvaluationApplication_ImplBase;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.TopologyControlEvaluationMessage;

public class AllToAllTransmissionApplication extends TopologyControlEvaluationApplication_ImplBase
		implements TransMessageListener {

	public AllToAllTransmissionApplication(final TopologyControlEvaluationApplicationConfig config) {
		super(createConfig(config));
		if (!(this.isSender() && this.isReceiver()))
			throw new IllegalStateException();
	}

	private static TopologyControlEvaluationApplicationConfig createConfig(
			TopologyControlEvaluationApplicationConfig config) {
		config.setSenderAndReceiver(true);
		return config;
	}

	@Override
	protected void doSendMessages() {

		Monitor.log(getClass(), Level.DEBUG, "Sending a message");
		for (final SimHost host : GlobalOracle.getHosts()) {
			if (!host.getLinkLayer().getMac(config.phyType).getMacAddress().equals(getOwnMacAddress())) {
				final TopologyControlEvaluationMessage message = new TopologyControlEvaluationMessage();
				config.transportProtocol.send(message, ScenarioUtilities.getNetID(host), config.port);
				this.notifyMessageSent(message);
			}
		}

	}

	@Override
	public void messageArrived(Message msg, TransInfo sender, int commID) {
		Monitor.log(getClass(), Level.DEBUG, "Message arrived");
		this.notifyMessageReceived(msg);
	}

}
