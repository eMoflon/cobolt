package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.alltoall;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.util.oracle.GlobalOracle;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransInfo;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.ScenarioUtilities;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.TopologyControlEvaluationApplicationConfig;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.TopologyControlEvaluationApplication_ImplBase;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.TopologyControlEvaluationMessage;

public class PointToPointTransmissionApplication extends TopologyControlEvaluationApplication_ImplBase {

	public PointToPointTransmissionApplication(final TopologyControlEvaluationApplicationConfig config) {
		super(updateConfig(config));
	}

	private static TopologyControlEvaluationApplicationConfig updateConfig(
			TopologyControlEvaluationApplicationConfig config) {
		config.setSenderAndReceiver(true);
		return config;
	}

	@Override
	protected void doSendMessages() {
		if (random.nextDouble() <= c().transmissionProbability) {
			final Host randomEndpoint = selectRandomHost();

			final TopologyControlEvaluationMessage message = new TopologyControlEvaluationMessage();
			final NetID targetId = ScenarioUtilities.getNetID((SimHost) randomEndpoint);
			final NetID myId = ScenarioUtilities.getNetID(getHost());
			this.config.transportProtocol.send(message, targetId, this.config.port);
			this.notifyMessageSent(message);

			Monitor.log(getClass(), Level.DEBUG, "[%s] Sending a message from %s to %s",
					Simulator.getFormattedTime(Simulator.getCurrentTime()), myId, targetId);
		}

	}

	@Override
	protected void doReceiveMessage(Message msg, TransInfo sender, int commID) {
		Monitor.log(getClass(), Level.DEBUG, "Message arrived");
		this.notifyMessageReceived(msg);
	}

	private Host selectRandomHost() {
		Host selectedTarget = GlobalOracle.getHostForNetID(GlobalOracle.getRandomHost());
		while (selectedTarget == config.host) {
			selectedTarget = GlobalOracle.getHostForNetID(GlobalOracle.getRandomHost());
		}
		return selectedTarget;
	}

}
