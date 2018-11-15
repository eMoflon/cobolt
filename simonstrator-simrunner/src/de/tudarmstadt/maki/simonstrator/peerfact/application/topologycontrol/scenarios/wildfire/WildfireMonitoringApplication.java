package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.wildfire;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.impl.linklayer.mac.wifi.Ieee80211AdHocMac;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransInfo;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.ScenarioUtilities;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.TopologyControlEvaluationApplicationConfig;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.TopologyControlEvaluationApplication_ImplBase;

public class WildfireMonitoringApplication extends TopologyControlEvaluationApplication_ImplBase {

	private final long cleanupInterval;
	private long timeOfLastCleanup;
	private final double periodOfMeasurementInMinutes;
	private final double amplitudeOfMeasurement;
	private final double offset;
	private final Map<INodeID, WildfireMonitoringMeasurement> measurementsInQueue;
	private final Set<WildfireMonitoringMeasurement> measurementsAlreadyForwarded;

	private final Map<INodeID, WildfireMonitoringMeasurement> dataStore;

	public WildfireMonitoringApplication(final TopologyControlEvaluationApplicationConfig config) {
		super(updateConfiguration(config));
		this.cleanupInterval = 10 * Time.MINUTE;
		this.timeOfLastCleanup = 0;
		this.offset = 10.0;
		this.amplitudeOfMeasurement = 10 * this.random.nextDouble();
		this.periodOfMeasurementInMinutes = 5 + 5 * this.random.nextDouble();
		this.measurementsInQueue = new HashMap<>();
		this.measurementsAlreadyForwarded = new HashSet<>();
		this.dataStore = new HashMap<>();
	}

	@Override
	public void initialize() {
		super.initialize();
		final WildfireMonitoringMeasurement ownMeasurement = performMesasurement();
		this.updateDataStore(ownMeasurement);
		this.measurementsInQueue.put(ownMeasurement.nodeId, ownMeasurement);
	}

	@Override
	protected void doSendMessages() {
		if (this.timeOfLastCleanup + this.cleanupInterval < Time.getCurrentTime()) {
			this.cleanupSentMessagesCache();
			this.timeOfLastCleanup = Time.getCurrentTime();
		}

		final WildfireMonitoringMeasurement ownMeasurement = performMesasurement();
		this.updateDataStore(ownMeasurement);
		this.measurementsInQueue.put(ownMeasurement.nodeId, ownMeasurement);
		final List<WildfireMonitoringMessage> messages = buildMessageSetToBeSent();

		for (final INodeID neighbor : ScenarioUtilities.getNeighbors(this.getHost())) {
			for (final WildfireMonitoringMessage message : messages) {
				final SimHost neighborHost = ScenarioUtilities.findHost(neighbor);
				final NetID neigbhorNetID = ScenarioUtilities.getNetID(neighborHost);
				c().transportProtocol.send(message, neigbhorNetID, c().port);
				this.notifyMessageSent(message);
			}
		}

		this.measurementsAlreadyForwarded.addAll(measurementsInQueue.values());
		this.measurementsInQueue.clear();
		super.doSendMessages();
	}

	private List<WildfireMonitoringMessage> buildMessageSetToBeSent() {
		final List<WildfireMonitoringMessage> messageQueue = new ArrayList<>();
		final Collection<WildfireMonitoringMeasurement> allMeasurementsToBeSent = measurementsInQueue.values();
		final long sizePerMessage = WildfireMonitoringMessage.getSizeContributionOfWildfireMonitoringMeasurement();
		final int numberOfMeasurementsPerMessage = (int) Math.floor(Ieee80211AdHocMac.MAX_FRAME_SIZE / sizePerMessage);
		final Collection<WildfireMonitoringMeasurement> pendingMeasurements = new ArrayList<>(
				numberOfMeasurementsPerMessage);
		final Iterator<WildfireMonitoringMeasurement> iterator = allMeasurementsToBeSent.iterator();

		while (iterator.hasNext()) {
			final WildfireMonitoringMeasurement nextMeasurement = iterator.next();

			// Current message is 'full'. Add it to the send queue and reset the
			// list
			if (pendingMeasurements.size() + 1 > numberOfMeasurementsPerMessage) {
				final WildfireMonitoringMessage message = new WildfireMonitoringMessage(pendingMeasurements);
				messageQueue.add(message);
				pendingMeasurements.clear();
			}

			pendingMeasurements.add(nextMeasurement);
		}

		if (!pendingMeasurements.isEmpty()) {
			final WildfireMonitoringMessage message = new WildfireMonitoringMessage(pendingMeasurements);
			messageQueue.add(message);
			pendingMeasurements.clear();
		}

		return messageQueue;
	}

	private void cleanupSentMessagesCache() {
		final int sizeBefore = this.measurementsAlreadyForwarded.size();
		final Set<WildfireMonitoringMeasurement> deletionCandidates = this.measurementsAlreadyForwarded.stream()
				.filter(measurements -> measurements.timestamp < Time.getCurrentTime() - this.cleanupInterval)
				.collect(Collectors.toSet());
		this.measurementsAlreadyForwarded.removeAll(deletionCandidates);
		final int sizeAfter = this.measurementsAlreadyForwarded.size();
		if (sizeBefore != sizeAfter)
			Monitor.log(getClass(), Level.DEBUG, "Cleanup of sent-messages cache from %d to %d messages.", sizeBefore,
					sizeAfter);
	}

	@Override
	protected void doReceiveMessage(Message msg, TransInfo sender, int commID) {
		super.doReceiveMessage(msg, sender, commID);
		this.notifyMessageReceived(msg);
		if (msg instanceof WildfireMonitoringMessage) {
			final WildfireMonitoringMessage message = (WildfireMonitoringMessage) msg;
			message.getMeasurements().stream()
					.filter(measurement -> !this.measurementsAlreadyForwarded.contains(measurement))
					.forEach(measurement -> {
						this.measurementsInQueue.put(measurement.nodeId, measurement);
						this.updateDataStore(measurement);
					});
		}
	}

	public WildfireMonitoringMeasurement getStoredOwnMeasurement() {
		return this.dataStore.get(ScenarioUtilities.getNode(getHost()).getId());
	}

	/**
	 * Create a data point for this sensor node
	 * 
	 * @return the new data point
	 */
	private WildfireMonitoringMeasurement performMesasurement() {
		final INode node = ScenarioUtilities.getNode(getHost());
		final long timestamp = Time.getCurrentTime();
		final double measurement = getCurrentMeasurement(timestamp);
		return new WildfireMonitoringMeasurement(node.getId(), timestamp, measurement);
	}

	private void updateDataStore(final WildfireMonitoringMeasurement measurement) {
		final WildfireMonitoringMeasurement storedMeasurement = this.dataStore.get(measurement.nodeId);
		if (storedMeasurement == null) {
			this.dataStore.put(measurement.nodeId, measurement);
		} else {
			final long storedTimestamp = storedMeasurement.timestamp;
			final long newTimestamp = measurement.timestamp;
			if (storedTimestamp < newTimestamp) {
				this.dataStore.put(measurement.nodeId, measurement);
			}
		}
	}

	/**
	 * The measurements follow a sine curve with amplitude
	 * {@link #amplitudeOfMeasurement} and period
	 * {@link #periodOfMeasurementInMinutes}
	 * 
	 * @param timestamp
	 *            the current timestamp (in Simulation time)
	 * @return the current measurement
	 */
	private double getCurrentMeasurement(final long timestamp) {
		final double timeInMinutes = 1.0 * timestamp / Time.MINUTE;
		return this.offset + this.amplitudeOfMeasurement
				* Math.sin(2 * Math.PI * timeInMinutes / this.periodOfMeasurementInMinutes);
	}

	/**
	 * Sets this node to be sender & receiver
	 * 
	 * @param config
	 *            the configuration to be updated
	 * @return same as the parameter for passing through
	 */
	private static TopologyControlEvaluationApplicationConfig updateConfiguration(
			TopologyControlEvaluationApplicationConfig config) {
		config.setSenderAndReceiver(true);
		return config;
	}

	/**
	 * Returns the node's local perspective of the entire network state
	 * 
	 * Keys: IDs of the nodes as returned via
	 * {@link ScenarioUtilities#getNode(de.tudarmstadt.maki.simonstrator.api.Host)}
	 * Value: The most recent measurement. Never null
	 * 
	 * It is possible that not all nodes of the network appear as keys in the
	 * returned map
	 * 
	 * @return this node's local perspective of the network
	 */
	public Map<INodeID, WildfireMonitoringMeasurement> getDataStore() {
		return this.dataStore;
	}

}
