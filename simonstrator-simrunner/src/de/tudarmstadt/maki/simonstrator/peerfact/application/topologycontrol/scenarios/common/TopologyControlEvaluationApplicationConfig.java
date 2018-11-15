package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common;

import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.transport.MessageBasedTransport;
import de.tudarmstadt.maki.simonstrator.api.util.Distribution;

public class TopologyControlEvaluationApplicationConfig implements Cloneable {
	public static final int DEFAULT_PORT = 5000;
	public static final PhyType DEFAULT_PHY_TYPE = PhyType.WIFI;
	public static final long DEFAULT_DISTRIBUTION_SCALE = Time.MILLISECOND;

	public Host host;

	public MessageBasedTransport transportProtocol;

	public Distribution sendDistribution;

	public long sendDistributionScale = DEFAULT_DISTRIBUTION_SCALE;

	public PhyType phyType = DEFAULT_PHY_TYPE;

	public int port = DEFAULT_PORT;

	public boolean isReceiver = false;

	public boolean isSender = false;

	// ScenarioType.DATACOLLECTION
	// The probability that a node chooses to transmit data-collection messages
	public double datacollectionProbability = 1.0;

	// ScenarioType.GOSSIP
	// The probability that the stored messages are being forwarded to a
	// neighbor
	public double forwardingProbability = 1.0;
	// The probability of generating a new message in one round
	public double newMessageGenerationProbability = 1.0;

	// ScenarioType.POINTTOPOINT
	// Transmission probability for point-to-point scenarios
	public double transmissionProbability = 1.0;

	@Override
	protected TopologyControlEvaluationApplicationConfig clone() throws CloneNotSupportedException {
		return (TopologyControlEvaluationApplicationConfig) super.clone();
	}

	public TopologyControlEvaluationApplicationConfig() {
	}

	public boolean isReceiver() {
		return this.isReceiver;
	}

	public boolean isSender() {
		return isSender;
	}

	public boolean isSenderAndReceiver() {
		return this.isSender() && this.isReceiver();
	}

	public void setDatacollectionProbability(double datacollectionProbability) {
		this.datacollectionProbability = datacollectionProbability;
	}

	public void setSendDistribution(Distribution sendDistribution) {
		this.sendDistribution = sendDistribution;
	}

	public void setSendDistributionScale(long sendDistributionScale) {
		this.sendDistributionScale = sendDistributionScale;
	}

	public void setReceiver(boolean isReceiver) {
		this.isReceiver = isReceiver;
	}

	public void setSender(boolean isSender) {
		this.isSender = isSender;
	}

	public void setForwardingProbability(double forwardingProbability) {
		this.forwardingProbability = forwardingProbability;
	}

	public void setNewMessageGenerationProbability(double newMessageGenerationProbability) {
		this.newMessageGenerationProbability = newMessageGenerationProbability;
	}

	public void setSenderAndReceiver(boolean isSenderAndReceiver) {
		this.setSender(isSenderAndReceiver);
		this.setReceiver(isSenderAndReceiver);
	}

	public void setTransmissionProbability(double transmissionProbability) {
		this.transmissionProbability = transmissionProbability;
	}

}
