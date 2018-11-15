package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.datacollection;

import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.TopologyControlEvaluationApplicationConfig;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.TopologyControlEvaluationApplication_ImplBase;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.TopologyControlEvaluationMessage;

/**
 * This application transmits data at regular intervals to the base station.
 */
public class DataCollectionTransmissionApplication extends TopologyControlEvaluationApplication_ImplBase {

	/**
	 * This flag indicates whether this node actively sends messages. The flag
	 * is evaluated based on {@link #random} and the threshold
	 * {@link TopologyControlEvaluationApplicationConfig#datacollectionProbability}
	 */
	private final boolean isSendingMessages;

	public DataCollectionTransmissionApplication(final TopologyControlEvaluationApplicationConfig config) {
		super(updateConfig(config));
		this.isSendingMessages = this.random.nextDouble() < c().datacollectionProbability;
		if (this.isSendingMessages)
			Monitor.log(getClass(), Level.INFO,
					"Node %s will send messages to base station.", c().host.getId().toString());
	}

	@Override
	protected void doSendMessages() {
		if (this.isSendingMessages) {
			final TopologyControlEvaluationMessage msg = new TopologyControlEvaluationMessage();
			this.config.transportProtocol.send(msg, BaseStationApplication.getBaseStationAddress(),
					TopologyControlEvaluationApplicationConfig.DEFAULT_PORT);
			this.notifyMessageSent(msg);
		}
	}

	private static TopologyControlEvaluationApplicationConfig updateConfig(
			TopologyControlEvaluationApplicationConfig config) {
		config.setSender(true);
		return config;
	}

}
