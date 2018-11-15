package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common;

import java.util.Random;

import de.tud.kom.p2psim.api.application.Application;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.network.SimNetInterface;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.transport.MessageBasedTransport;
import de.tudarmstadt.maki.simonstrator.api.component.transport.ProtocolNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransInfo;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransMessageListener;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransportComponent;
import de.tudarmstadt.maki.simonstrator.api.component.transport.protocol.UDP;
import de.tudarmstadt.maki.simonstrator.api.operation.AbstractOperation;
import de.tudarmstadt.maki.simonstrator.api.operation.PeriodicOperation;
import de.tudarmstadt.maki.simonstrator.api.util.Distribution;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.ScenarioUtilities;

public abstract class TopologyControlEvaluationApplication_ImplBase implements Application {

	protected final TopologyControlEvaluationApplicationConfig config;
	protected final Random random;

	private int messageSentCountSinceLastCheckpoint;
	private int messageReceivedCountSinceLastCheckpoint;
	private int messageSentSizeSinceLastCheckpoint;
	private int messageReceivedSizeSinceLastCheckpoint;
	private long latencySinceLastCheckpoint;

	private int totalMessageSentCount;
	private int totalMessageSentSize;
	private int totalMessageReceivedCount;
	private int totalMessageReceivedSize;
	private long totalLatency;

	public TopologyControlEvaluationApplication_ImplBase(final TopologyControlEvaluationApplicationConfig config) {
		try {
			this.config = config.clone();
		} catch (final CloneNotSupportedException e) {
			throw new IllegalStateException();
			// This never happens, actually, but is required for final fields
		}
		this.random = Randoms.getRandom(getClass());
	}

	@Override
	public final SimHost getHost() {
		return SimHost.class.cast(this.c().host);
	}

	@Override
	public void initialize() {
		new InitalizationOperation().scheduleWithDelay(getInitializationDelay());
		new HostTerminatingOperation().scheduleWithDelay(Simulator.getEndTime());
	}

	@Override
	public void shutdown() {
		// nop
	}

	public static TopologyControlEvaluationApplication_ImplBase find(final Host host) {
		try {
			return host.getComponent(TopologyControlEvaluationApplication_ImplBase.class);
		} catch (final ComponentNotAvailableException e) {
			return null;
		}
	}

	/**
	 * Reset the inter-checkpoint metrics of this component
	 * 
	 * @see #getMessageReceivedCountSinceCheckpoint()
	 * @see #getMessageSentCountSinceCheckpoint()
	 * @see #getAverageLatencySinceCheckpoint()
	 */
	public void checkpointReached() {
		this.messageReceivedCountSinceLastCheckpoint = 0;
		this.messageReceivedSizeSinceLastCheckpoint = 0;
		this.messageSentCountSinceLastCheckpoint = 0;
		this.messageSentSizeSinceLastCheckpoint = 0;
		this.latencySinceLastCheckpoint = 0;
	}

	/**
	 * Returns the number of received messages of this component
	 * 
	 * @return
	 */
	public int getMessageReceivedCountSinceCheckpoint() {
		return this.messageReceivedCountSinceLastCheckpoint;
	}

	/**
	 * Returns the number of sent messages of this component
	 * 
	 * @return
	 */
	public int getMessageSentCountSinceCheckpoint() {
		return this.messageSentCountSinceLastCheckpoint;
	}

	/**
	 * Returns the size of received messages of this component
	 * 
	 * @return
	 */
	public int getMessageReceivedSizeSinceCheckpoint() {
		return this.messageReceivedSizeSinceLastCheckpoint;
	}

	/**
	 * Returns the size of sent messages of this component
	 * 
	 * @return
	 */
	public int getMessageSentSizeSinceCheckpoint() {
		return this.messageSentSizeSinceLastCheckpoint;
	}

	/**
	 * Returns the average latency over all
	 * {@link TopologyControlEvaluationMessage}s that have been received (in
	 * simulation time units)
	 * 
	 * @return
	 */
	public double getAverageLatencySinceCheckpoint() {
		return 1.0 * this.latencySinceLastCheckpoint / this.messageReceivedCountSinceLastCheckpoint;
	}

	/**
	 * Returns the number of received messages of this component
	 * 
	 * @return
	 */
	public int getTotalMessageReceivedCount() {
		return totalMessageReceivedCount;
	}

	/**
	 * Returns the number of sent messages of this component
	 * 
	 * @return
	 */
	public int getTotalMessageSentCount() {
		return totalMessageSentCount;
	}

	/**
	 * Returns the size of received messages of this component
	 * 
	 * @return
	 */
	public int getTotalMessageReceivedSize() {
		return totalMessageReceivedSize;
	}

	/**
	 * Returns the size of sent messages of this component
	 * 
	 * @return
	 */
	public int getTotalMessageSentSize() {
		return totalMessageSentSize;
	}

	/**
	 * Returns the average latency over all
	 * {@link TopologyControlEvaluationMessage}s that have been received (in
	 * simulation time units)
	 * 
	 * @return
	 */
	public double getTotalAverageLatency() {
		return 1.0 * this.totalLatency / this.totalMessageReceivedCount;
	}

	/**
	 * This message is invoked whenever a send operation is scheduled according to
	 * the send distribution and the distribution scale.
	 * 
	 * When this method is called, the battery of this host is not empty, yet.
	 * 
	 * This implementation is empty. Therefore, subclasses need not call
	 * super.doSendMessages().
	 */
	protected void doSendMessages() {
		// default empty implementation
	}

	/**
	 * This method is invoked whenever a packet is received
	 * 
	 * This implementation is empty. Therefore, subclasses need not call
	 * super.doReceiveMessage().
	 *
	 * @see TransMessageListener#messageArrived(Message, TransInfo, int)
	 */
	protected void doReceiveMessage(final Message msg, final TransInfo sender, final int commID) {
		// default empty implementation
	}

	protected void doInitialization() {
		initializeStatistics();
		initializeNetworkInterface();
		if (isSender())
			new SendMessagePeriodicOperation(c().sendDistribution, c().sendDistributionScale).scheduleImmediately();
		if (isReceiver()) {
			c().transportProtocol.setTransportMessageListener(new MessageReceiver());
		}
	}

	protected boolean isReceiver() {
		return this.c().isReceiver();
	}

	protected boolean isSender() {
		return this.c().isSender();
	}

	protected TopologyControlEvaluationApplicationConfig c() {
		return this.config;
	}

	protected MacAddress getOwnMacAddress() {
		return getHost().getLinkLayer().getMac(c().phyType).getMacAddress();
	}

	protected void notifyMessageSent(final Message message) {
		this.totalMessageSentCount++;
		this.totalMessageSentSize += message.getSize();

		this.messageSentCountSinceLastCheckpoint++;
		this.messageSentSizeSinceLastCheckpoint += message.getSize();
	}

	protected void notifyMessageReceived(final Message message) {
		this.totalMessageReceivedCount++;
		this.totalMessageReceivedSize += message.getSize();

		this.messageReceivedCountSinceLastCheckpoint++;
		this.messageReceivedSizeSinceLastCheckpoint += message.getSize();

		if (message instanceof TopologyControlEvaluationMessage) {
			final TopologyControlEvaluationMessage evalMessage = TopologyControlEvaluationMessage.class.cast(message);
			final long latencyInMicros = Time.getCurrentTime() - evalMessage.getCreationTime();
			this.totalLatency += latencyInMicros;
			this.latencySinceLastCheckpoint += latencyInMicros;
		}
	}

	/**
	 * The initialization shall happen *after* the first TC iteration. We add an
	 * extra delay of 1 minute to ensure that the topology is stable.
	 */
	protected long getInitializationDelay() {
		return ComponentFinder.findTopologyControlComponent().getTopologyControlInterval() + 1 * Time.SECOND;
	}

	private void initializeStatistics() {
		this.totalMessageReceivedCount = 0;
		this.totalMessageReceivedSize = 0;

		this.totalMessageSentCount = 0;
		this.totalMessageSentSize = 0;

		this.totalLatency = 0L;

		this.messageReceivedCountSinceLastCheckpoint = 0;
		this.messageReceivedSizeSinceLastCheckpoint = 0;

		this.messageSentCountSinceLastCheckpoint = 0;
		this.messageSentSizeSinceLastCheckpoint = 0;

		this.latencySinceLastCheckpoint = 0L;
	}

	private void initializeNetworkInterface() {
		try {
			final TransportComponent transportComponent = getHost().getTransportComponent();
			final Class<? extends MessageBasedTransport> protocolInterface = UDP.class;
			c().transportProtocol = transportComponent.getProtocol(protocolInterface,
					ScenarioUtilities.getNetID(getHost()), c().port);
		} catch (final ProtocolNotAvailableException e) {
			throw new IllegalStateException("Unable to bind transport protocol: " + e);
		}
	}

	private final class HostTerminatingOperation
			extends AbstractOperation<TopologyControlEvaluationApplication_ImplBase, Void> {
		private HostTerminatingOperation() {
			super(TopologyControlEvaluationApplication_ImplBase.this);
		}

		@Override
		protected void execute() {
			for (final SimNetInterface net : getHost().getNetworkComponent().getSimNetworkInterfaces()) {
				net.goOffline();
			}
		}

		@Override
		public Void getResult() {
			return null;
		}
	}

	private class MessageReceiver implements TransMessageListener {

		@Override
		public void messageArrived(final Message msg, final TransInfo sender, final int commID) {
			doReceiveMessage(msg, sender, commID);
		}

	}

	private class SendMessagePeriodicOperation
			extends PeriodicOperation<TopologyControlEvaluationApplication_ImplBase, Void> {

		private boolean firstExecution = true;

		protected SendMessagePeriodicOperation(final Distribution intervalDistribution, final long simTimeScaling) {
			super(TopologyControlEvaluationApplication_ImplBase.this, null, intervalDistribution, simTimeScaling);
		}

		@Override
		protected void executeOnce() {
			// don't send a message at the first execution as this event is
			// triggered at each sensor at the same point in time (collisions!)
			if (firstExecution) {
				firstExecution = false;
				return;
			}

			if (!getHost().getEnergyModel().getInfo().getBattery().isEmpty()) {
				doSendMessages();
			}
		}

		@Override
		public Void getResult() {
			return null;
		}
	}

	private class InitalizationOperation
			extends AbstractOperation<TopologyControlEvaluationApplication_ImplBase, Void> {

		protected InitalizationOperation() {
			super(TopologyControlEvaluationApplication_ImplBase.this);
		}

		@Override
		protected void execute() {
			doInitialization();
		}

		@Override
		public Void getResult() {
			return null;
		}

	}

}
