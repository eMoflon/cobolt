package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.gossip;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Random;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransInfo;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.ScenarioUtilities;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.TopologyControlEvaluationApplicationConfig;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.TopologyControlEvaluationApplication_ImplBase;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.TopologyControlEvaluationMessage;

/**
 * Load profile: - Upon each 'send event', each host performs two steps (1) With
 * probability {@link #getNewMessageGenerationProbability()}, a new message is
 * produced and enqueued in the send queue (2) All messages in the send queue
 * are distributed to all immediate neighbors with a probability of
 * {@link #getForwardingProbability()}. The decision whether to send or not to
 * send is evaluated for each neighbor
 */
public class GossipTransmissionApplication extends TopologyControlEvaluationApplication_ImplBase {

	private final Deque<Message> messageQueue;
	private final Random random;

	public GossipTransmissionApplication(TopologyControlEvaluationApplicationConfig config) {
		super(updateConfig(config));
		this.messageQueue = new LinkedList<>();
		this.random = Randoms.getRandom(getClass());
	}

	@Override
	protected void doSendMessages() {
		if (drawRandomDouble() < this.getNewMessageGenerationProbability())
			this.generateAndEnqueueMessage();
		if (drawRandomDouble() < this.getForwardingProbability()) {
			for (final INodeID neighbor : ScenarioUtilities.getNeighbors(this.getHost())) {
				this.forwardWholeMessageQueue(neighbor);
			}
		}
		this.clearMessageQueue();
	}

	@Override
	protected void doReceiveMessage(Message msg, TransInfo sender, int commID) {
		// this.messageQueue.add(msg);
		this.notifyMessageReceived(msg);
	}

	private static TopologyControlEvaluationApplicationConfig updateConfig(
			TopologyControlEvaluationApplicationConfig config) {
		config.setSenderAndReceiver(true);
		return config;
	}

	private void clearMessageQueue() {
		this.messageQueue.clear();
	}

	private void forwardWholeMessageQueue(final INodeID neighbor) {
		final SimHost neighborHost = ScenarioUtilities.findHost(neighbor);
		final NetID neigbhorNetID = ScenarioUtilities.getNetID(neighborHost);
		for (final Message message : this.messageQueue) {
			c().transportProtocol.send(message, neigbhorNetID, c().port);
			this.notifyMessageSent(message);
		}
	}

	private void generateAndEnqueueMessage() {
		this.messageQueue.push(new TopologyControlEvaluationMessage());
	}

	private double drawRandomDouble() {
		return this.random.nextDouble();
	}

	private double getForwardingProbability() {
		return this.config.forwardingProbability;
	}

	private double getNewMessageGenerationProbability() {
		return this.config.newMessageGenerationProbability;
	}

}
