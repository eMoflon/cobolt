/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
 *
 * This file is part of PeerfactSim.KOM.
 * 
 * PeerfactSim.KOM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * PeerfactSim.KOM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PeerfactSim.KOM.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package de.tud.kom.p2psim.impl.linklayer.mac;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import de.tud.kom.p2psim.api.analyzer.LinklayerAnalyzer;
import de.tud.kom.p2psim.api.analyzer.MessageAnalyzer;
import de.tud.kom.p2psim.api.analyzer.MessageAnalyzer.Reason;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.energy.ComponentType;
import de.tud.kom.p2psim.api.energy.EnergyCommunicationComponent;
import de.tud.kom.p2psim.api.energy.EnergyEventListener;
import de.tud.kom.p2psim.api.linklayer.LinkLayerMessage;
import de.tud.kom.p2psim.api.linklayer.LinkMessageEvent;
import de.tud.kom.p2psim.api.linklayer.LinkMessageListener;
import de.tud.kom.p2psim.api.linklayer.mac.Link;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.MacEventInformation;
import de.tud.kom.p2psim.api.linklayer.mac.MacLayer;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.network.BandwidthImpl;
import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.api.topology.views.TopologyView;
import de.tud.kom.p2psim.impl.linklayer.ModularLinkLayer;
import de.tud.kom.p2psim.impl.network.BandwidthEstimator;
import de.tud.kom.p2psim.impl.util.LiveMonitoring;
import de.tud.kom.p2psim.impl.util.LiveMonitoring.ProgressValue;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent.AnalyzerNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

/**
 * Basic implementation of a MacLayer running on a PHY-CommunicationComponent.
 * As with the {@link ModularLinkLayer} you should extend this class to add a
 * more advanced MAC to ensure basic functionality and consistent behavior. For
 * this reason, some methods in this class are marked as final...
 * 
 * If a MAC wants to implement a protocol (ie. send messages to other MACs
 * without triggering a deliver to higher layers) it can do so by implementing a
 * corresponding Message type and/or {@link MacEventInformation}. <b>A Message
 * is only delivered to the higher layers if notifyLinkLayer is called</b>.
 * 
 * This Layer takes care of energy consumption, as long as the sendUnicast and
 * sendBroadcast-methods are used to dispatch all messages (also the
 * control-messages, if a protocol is implemented)
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 21.02.2012
 */
public abstract class AbstractMacLayer implements MacLayer {

	public static enum DropReason {
		LINK_DROP, QUEUE_FULL, QUEUE_TIMEOUT, NO_LINK, SENDING_MAC_OFFLINE, RECEIVING_MAC_OFFLINE
	}

	/**
	 * The PHY we operate on
	 */
	private PhyType phy;

	/**
	 * The {@link TopologyView} we operate on
	 */
	private TopologyView topoView;

	/**
	 * Our own Host
	 */
	private SimHost host;

	/**
	 * Our own MacAddress on this PHY
	 */
	private MacAddress macAddress;
	
	/**
	 * Our own IP used by the NetLayer on this PHY. Not available during initialization.
	 */
	private NetID netID;

	/**
	 * Each MAC has a Message queue for outgoing messages
	 */
	private LinkedList<QueueEntry> queue;

	/**
	 * The LinkLayer registers itself as a MessageListener
	 */
	private LinkMessageListener messageListener;

	/**
	 * Number of retransmissions before the message is dropped.
	 * provideErrorControl must be true.
	 */
	private final int maxRetransmissions;

	/**
	 * If this is set to true, the MAC will request broken packets
	 * maxRetransmission times.
	 */
	private final boolean enableErrorControl;

	/**
	 * 
	 */
	private boolean isOnline;

	/**
	 * The maximum length of the outgoing queue (if 0, queue has no limit)
	 */
	private final int maxQueueLength;

	/**
	 * The maximum time a message is kept in the queue before it is dropped (if
	 * 0, there is no limit)
	 */
	private final long maxTimeInQueue;

	/**
	 * An {@link EnergyCommunicationComponent} for this MAC
	 */
	private EnergyCommunicationComponent energyComponent;

	/**
	 * Contains the configured bandwidth for this MAC
	 */
	protected final BandwidthImpl maximumBandwidth;

	/**
	 * Returns an approximation of the current bandwidth
	 */
	protected final BandwidthEstimator currentBandwidth;
	
	private final Random random = Randoms.getRandom(AbstractMacLayer.class);

	/*
	 * Analyzing
	 */
	public static boolean[] _analyzersInitialized = new boolean[PhyType
			.values().length];

	public static MacMessagesDropReasonProgress[] _dropReasonAnalyzer = new MacMessagesDropReasonProgress[PhyType
			.values().length];

	public static final int MESSAGE_DROPPED = 1;

	public static final int MESSAGE_SENT = 2;

	public static final int MESSAGE_RECEIVED = 3;

	public static final int MESSAGE_AT_SUBNET = 4;

	/**
	 * Create a new MacLayer without bounds on the outgoing message queue (ie.
	 * endless size, no limit on the waiting time).
	 * 
	 * @param ownMacAddress
	 * @param phy
	 * @param maxRetransmissions
	 *            before the message is dropped. note: Broadcasts are never
	 *            retransmitted. Set this to zero to enable ErrorControl at the
	 *            MAC
	 * @param bandwidth
	 *            the maximum BW of this MAC (i.e. the BW that would be achieved
	 *            if messages are not queued)
	 */
	public AbstractMacLayer(SimHost host, MacAddress ownMacAddress,
			PhyType phy, int maxRetransmissions, BandwidthImpl bandwidth) {
		this(host, ownMacAddress, phy, 0, 0, maxRetransmissions, bandwidth);
	}

	/**
	 * Create a new MacLayer with a bounded outgoing queue (max length) and a
	 * timeout for messages in said queue.
	 * 
	 * @param ownMacAddress
	 * @param phy
	 * @param maxQueueLength
	 *            if 0, the queue is not limited
	 * @param maxTimeInQueue
	 *            if 0, there is no timeout for messages in the queue
	 * @param maxRetransmissions
	 *            before the message is dropped. note: Broadcasts are never
	 *            retransmitted
	 * @param bandwidth
	 *            the maximum BW of this MAC (i.e. the BW that would be achieved
	 *            if messages are not queued)
	 */
	public AbstractMacLayer(SimHost host, MacAddress ownMacAddress,
			PhyType phy,
			int maxQueueLength, long maxTimeInQueue, int maxRetransmissions,
			BandwidthImpl bandwidth) {
		this.host = host;
		this.enableErrorControl = maxRetransmissions > 0;
		this.macAddress = ownMacAddress;
		this.phy = phy;
		this.maxQueueLength = maxQueueLength;
		this.maxTimeInQueue = maxTimeInQueue;
		this.maxRetransmissions = maxRetransmissions;
		this.maximumBandwidth = bandwidth;
		// ensure, that max and current are NOT the same object instance!
		this.currentBandwidth = new BandwidthEstimator(5); // 5s historical data
		this.queue = new LinkedList<AbstractMacLayer.QueueEntry>();

		if (!_analyzersInitialized[phy.ordinal()]) {
			_dropReasonAnalyzer[phy.ordinal()] = new MacMessagesDropReasonProgress();
			LiveMonitoring.addProgressValueIfNotThere(_dropReasonAnalyzer[phy
					.ordinal()]);
			_analyzersInitialized[phy.ordinal()] = true;
		}
	}

	@Override
	public void initialize() throws ConfigurationException {
		/*
		 * fetch an energy-component if one is present
		 */
		if (host.getEnergyModel() != null) {
			List<EnergyCommunicationComponent> energyComponents = host
					.getEnergyModel().getComponents(
							ComponentType.COMMUNICATION,
							EnergyCommunicationComponent.class);
			for (EnergyCommunicationComponent comp : energyComponents) {
				if (comp.getPhyType().equals(phy)) {
					energyComponent = comp;
				}
			}
		}

		if (energyComponent == null) {
			// Log.warn("The MAC-Layer did not find an Energy-Component for "
			// + phy.toString()
			// +
			// ". This is fine, if you did not intend to measure energy consumption. Otherwise, please check your configuration!");
			energyComponent = new EnergyComponentStub(phy);
		}

		/*
		 * Fetch a TopologyView for this PHY
		 */
		if (host.getTopologyComponent() == null
				|| host.getTopologyComponent().getTopology() == null) {
			throw new ConfigurationException(
					"In order to use the LinkLayer you have to specify a Topology as a seperate component of your hosts.");
		}
		topoView = host.getTopologyComponent().getTopology()
				.getTopologyView(phy);
		if (topoView == null) {
			throw new ConfigurationException(
					"There is no TopologyView for the PHY " + phy.toString()
							+ " configured!");
		}
	}

	@Override
	public void shutdown() {
		throw new AssertionError("You are not supposed to shutdown a MAC.");
	}

	@Override
	public final TopologyView getTopologyView() {
		return topoView;
	}

	/**
	 * Use this component to account for energy consumption. This is safe to
	 * use, as it is always backed by a stub - it is never null.
	 * 
	 * @return
	 */
	@Override
	public EnergyCommunicationComponent getEnergyComponent() {
		return energyComponent;
	}

	/**
	 * Current size of the outgoing Queue
	 * 
	 * @return
	 */
	protected final int getQueueSize() {
		return queue.size();
	}

	/**
	 * Maximum length of the outgoing queue, if equal to zero there is no limit
	 * 
	 * @return
	 */
	protected final int getMaxQueueLength() {
		return maxQueueLength;
	}

	/**
	 * Gets the maximal retransmissions.
	 * 
	 * @return the maximal retransmissions.
	 */
	public final int getMaxRetransmissions() {
		return maxRetransmissions;
	}

	/**
	 * Maximum time a message is kept in the outgoing queue. If equal to zero
	 * there is no limit.
	 * 
	 * @return
	 */
	protected final long getMaxTimeInQueue() {
		return maxTimeInQueue;
	}

	/**
	 * Retrieves and <b>removes</b> the first element in the Queue, after all
	 * entries that already timed out are deleted.
	 * 
	 * @return the current head of the queue or null if the queue is empty
	 */
	protected final QueueEntry getQueueHead() {
		removeOutdatedQueueEntries();
		return queue.poll();
	}

	/**
	 * Removes all messages that are already waiting longer than maxTimeInQueue,
	 * notifying messageDropped() for each dropped message. The reason will be
	 * QUEUE_TIMEOUT in this case.
	 * 
	 */
	private void removeOutdatedQueueEntries() {
		if (maxTimeInQueue == 0) {
			return;
		}

		long currentTime = Time.getCurrentTime();
		QueueEntry peek = queue.peek();
		while (peek != null
				&& peek.getTimeEntered() + maxTimeInQueue < currentTime) {
			queue.poll();
			messageDropped(DropReason.QUEUE_TIMEOUT, peek.getMessage());
			peek = queue.peek();
		}
	}

	/**
	 * This is called whenever a drop occurs within the MAC/PHY. A great
	 * opportunity to add an Analyzer or some error handling for higher layers.
	 * 
	 * Please note, that this might happen asynchronously - the messages are not
	 * removed from the queue the second their timeout expires but instead as
	 * soon as a new send() or a new getQueueHead() is issued.
	 * 
	 * @param reason
	 * @param msg
	 */
	protected void messageDropped(DropReason reason, Message msg) {
		ModularLinkLayer._linkDropped++;
		_dropReasonAnalyzer[phy.ordinal()].increment(reason);
		// System.out.println(Simulator.getFormattedTime(Simulator
		// .getCurrentTime())
		// + " LinkLayer DROP "
		// + reason.toString()
		// + " of " + msg.toString());

		if (msg instanceof LinkLayerMessage) {
			_linkMsgEvent((LinkLayerMessage) msg, getHost(),
					MessageAnalyzer.Reason.DROP);
		}

	}

	/**
	 * Notification: a new Entry was added to the outgoing queue.
	 */
	abstract protected void handleNewQueueEntry();

	/**
	 * Notification: a message arrived!
	 * 
	 * @param message
	 *            the message itself
	 * @param info
	 *            additional eventInformation
	 */
	abstract protected void handleReceivedMessage(MacEventInformation eventInfo);

	/**
	 * This is to be called by the MAC if a Message should be passed to upper
	 * layers (ie. it is no MAC_ctrl-Message)
	 * 
	 * @param eventInfo
	 */
	protected final void notifyLinkLayer(LinkMessageEvent eventInfo) {
		if (this.isOnline()) {
			messageListener.messageArrived(eventInfo);
		}
	}

	/**
	 * Send a Message to the MAC on the receiver-side (event scheduling).
	 * 
	 * @param receiver
	 * @param eventInformation
	 * @param delay
	 *            time it takes the message to reach the receiver
	 * @param dropped
	 *            receive-events are also used in the case of a message being
	 *            dropped. This will enable a persistent callback on the sending
	 *            mac as soon as the message is or would be received.
	 */
	protected final void scheduleReceive(MacLayer receiver,
			MacEventInformation eventInformation, long delay, boolean dropped) {
		// Event
		assert delay > 0 : "Delay is equal to or less than 0!";
		if (dropped) {
			Event.scheduleWithDelay(delay, receiver, eventInformation,
					MESSAGE_DROPPED);
		} else {
			Event.scheduleWithDelay(delay, receiver, eventInformation,
					MESSAGE_RECEIVED);
		}
	}

	protected void scheduleSendDone(long timeUntilSendDone) {
		// not used in this MAC
	}

	/**
	 * Use this method to dispatch a broadcast message in your MAC-Layer. It
	 * will take care of energy consumption and scheduling.
	 * 
	 * @param eventInfo
	 *            an implementation of {@link MacEventInformation} containing
	 *            information about the message to send as well as the message
	 *            itself.
	 */
	protected final long sendBroadcast(MacEventInformation eventInfo) {
		List<MacAddress> txNeighbors = getTopologyView().getNeighbors(
				getMacAddress());
		/*
		 * For each Link we have to check whether the message arrives or not.
		 * There are NO retransmissions of broadcasts, as there are no ACKs of
		 * broadcast either.
		 */
		Message msg = eventInfo.getMessage();
		long timeToSend = -1;

		for (MacAddress receiver : txNeighbors) {
			Link l = getTopologyView()
					.getLinkBetween(getMacAddress(), receiver);

			assert !receiver.equals(getMacAddress());
			assert l.isConnected();

			/*
			 * Broadcasts do not depend on an ACK or CTS by a receiver, so we do
			 * not care about the link back to the sender.
			 */
			long thisTimeToSend = tryToSend(eventInfo, l, null);

			if (timeToSend == -1) {
				timeToSend = thisTimeToSend;
			}
			if (timeToSend != thisTimeToSend) {
				throw new AssertionError(
						"Inconsitency in the MAC: a broadcast timeToSend is not constant across all links!");
			}
		}

		if (timeToSend == -1) {
			/*
			 * We have no neighbors, but we still sent the message - get the
			 * Broadcast-BW and account the corresponding energy consumption.
			 */
			Link selfLink = getTopologyView().getLinkBetween(getMacAddress(),
					getMacAddress());
			timeToSend = getUploadTime(msg, selfLink, true);
			scheduleReceive(this, eventInfo, timeToSend, true);
		}

		if (eventInfo.getMessage() instanceof LinkLayerMessage) {
			_linkMsgEvent((LinkLayerMessage) eventInfo.getMessage(), getHost(),
					MessageAnalyzer.Reason.SEND);
		}

		getEnergyComponent().send(timeToSend, msg, true);

		return timeToSend;
	}

	/**
	 * Send an unicast message
	 * 
	 * @param toSend
	 */
	protected final long sendUnicast(MacEventInformation eventInfo) {
		Message msg = eventInfo.getMessage();
		long timeToSend = 0;

		Link l = getTopologyView().getLinkBetween(getMacAddress(),
				eventInfo.getReceiver());
		if (l.isConnected()) {
			if (enableErrorControl) {
				Link reverseLink = getTopologyView().getLinkBetween(
						eventInfo.getReceiver(), getMacAddress());
				timeToSend = tryToSend(eventInfo, l, reverseLink);
			} else {
				timeToSend = tryToSend(eventInfo, l, null);
			}
			assert timeToSend > 0;
			getEnergyComponent().send(timeToSend, msg, false);

			/*
			 * Account for energy consumption due to listening in a
			 * broadcast-medium. This will happen even if the Message was not
			 * delivered due to an asymmetric link.
			 */
			if (phy.isBroadcastMedium()) {

				List<MacAddress> neighbors = getTopologyView().getNeighbors(
						getMacAddress());

				/*
				 * saving some calls to equals
				 */
				boolean skippedReceiver = false;

				for (MacAddress neighbor : neighbors) {
					if (skippedReceiver
							|| neighbor.equals(eventInfo.getReceiver())) {
						skippedReceiver = true;
						continue;
					}
					assert !neighbor.equals(getMacAddress());

					MacLayer macReceiver = getTopologyView().getMac(neighbor);
					if (macReceiver.isOnline()) {
						macReceiver.getEnergyComponent().receive(timeToSend,
								msg, false, false);
					}
				}
			}
		} else {
			messageDropped(DropReason.NO_LINK, msg);
			/*
			 * We have no neighbors!
			 */
			eventInfo.arrivedAt(this, true);
			timeToSend = 1 * Time.MICROSECOND;
		}
		return timeToSend;
	}

	@Override
	public final BandwidthImpl getCurrentBandwidth() {
		return currentBandwidth.getEstimatedBandwidth();
	}

	@Override
	public final BandwidthImpl getMaxBandwidth() {
		return maximumBandwidth;
	}

	/**
	 * Try to deliver the message over the provided link. If the message is a
	 * broadcast, the BW should be constant across all links (we assume the PHY
	 * selects a fixed, more robust modulation for Broadcasts), as there is no
	 * handshaking. Therefore, Broadcasts have no retransmits.
	 * 
	 * This method <b>must be called exactly once</b> for every receiver of a
	 * message (in the unicast case, it will be called only once). The receiver
	 * is determined by the link-object. Access this method via the sendUnicast
	 * and sendBroadcast methods to ensure correct behavior of all callbacks and
	 * events.
	 * 
	 * @param eventInfo
	 *            containing the Message
	 * @param l
	 *            the Link from source to receiver
	 * @param backlink
	 *            the Link from receiver to source. This may be null, if you do
	 *            not want to take link asymmetries into account.
	 * @return the time it took to send the message (time the radio had to stay
	 *         active), which is for example used to account for energy
	 *         consumption. This is NOT the same as the time we waited before
	 *         scheduling the receive-event at the MAC, as latency on the link
	 *         is not included.
	 */
	private final long tryToSend(MacEventInformation eventInfo, Link l,
			Link backlink) {

		/*
		 * Time the MACs need to operate in HighPower (are active)
		 */
		long totalSendingTime = 0;

		boolean isBroadcast = eventInfo.isBroadcast();

		/*
		 * Delay is the time we wait before scheduling the receive-event at the
		 * other MAC.
		 */
		long delay = 0;
		long latency = l.getLatency();
		long uploadTime = getUploadTime(eventInfo.getMessage(), l, isBroadcast);

		/*
		 * The following assertions are to ensure proper working conditions,
		 * enable assertions during development if you extend this MAC.
		 */
		assert enableErrorControl && (backlink != null || isBroadcast)
				|| !enableErrorControl;
		assert l.isConnected();

		/*
		 * TODO: virtual MTU-behavior -> if l.getMTU < msgSize, calculate drop
		 * for every virtual MTU to determine, if a packet can be delivered.
		 */

		/*
		 * Does a message arrive at the receiver?
		 */
		boolean dropped = false;

		if (enableErrorControl) {
			/*
			 * Number of Retransmissions (message is sent retransmissions + 1
			 * times). We do not really send and receive ACKs here, this is up
			 * to more advanced MACs. Instead, assume zero-time ACKs that might
			 * get lost (drop), and if this is the case, we would resend the
			 * message which will add on the total delay.
			 */
			int allowedRetransmissions = 0;
			if (!isBroadcast) {
				allowedRetransmissions = maxRetransmissions;
			}
			int tries = 0;
			do {
				if (tries > allowedRetransmissions) {
					dropped = true;
					break;
				}

				double candidate = random.nextDouble();
				dropped = candidate <= l.getDropProbability();
				tries++;
			} while (dropped);

			/*
			 * Calculate the Retransmits that would occur due to ACKs not being
			 * received by the sender. This is not done for broadcasts.
			 */
			if (!dropped && tries <= maxRetransmissions) {
				int ackTransmits = 0;
				boolean ackDropped = false;
				do {
					if (ackTransmits + tries > allowedRetransmissions) {
						ackDropped = true;
						break;
					}

					double candidate = random.nextDouble();
					ackDropped = candidate <= backlink.getDropProbability();
					ackTransmits++;
				} while (ackDropped);

				assert (ackDropped && tries + ackTransmits == allowedRetransmissions + 1)
						|| (!ackDropped);

				tries += ackTransmits;
			}
			/*
			 * FIXME: ACK-timing? Do we need to take this into account or do we
			 * just assume zero-time ACKs?
			 */
			delay = (latency + uploadTime) * tries;
			totalSendingTime = uploadTime * tries;
		} else {
			/*
			 * Just send. ETHERNET-Style, no error control on lower layers.
			 */
			double candidate = random.nextDouble();
			dropped = candidate <= l.getDropProbability();
			delay = latency + uploadTime;
			totalSendingTime = uploadTime;
		}

		/*
		 * Consume energy at the receiver, even if the message was not
		 * successfully delivered - he had to listen all the time.
		 */
		MacLayer macReceiver = getTopologyView().getMac(l.getDestination());
		if (macReceiver.isOnline()) {
			macReceiver.getEnergyComponent().receive(totalSendingTime,
					eventInfo.getMessage(), eventInfo.isBroadcast(), true);
		}

		if (dropped) {
			/*
			 * Notify Mac, if the message has been dropped due to
			 * MAX_RETRANSMITS. We do not care for dropped Broadcasts
			 */
			if (!eventInfo.isBroadcast()) {
				messageDropped(DropReason.LINK_DROP, eventInfo.getMessage());
			}
		} else {
			if (!eventInfo.isBroadcast()
					&& eventInfo.getMessage() instanceof LinkLayerMessage) {
				_linkMsgEvent((LinkLayerMessage) eventInfo.getMessage(),
						getHost(), MessageAnalyzer.Reason.SEND);
			}
		}

		/*
		 * Schedule MAC-Events even for dropped Messages in order to get the
		 * callback as soon as the current MAC is able to send again.
		 */
		scheduleReceive(macReceiver, eventInfo, delay, dropped);
		currentBandwidth.outgoingTransmission(eventInfo.getMessage().getSize());

		/*
		 * Latency is not included, because we only have to stay active until
		 * the message is being sent, not until it is fully delivered. In a
		 * broadcast-scenario tries will always be one.
		 */
		assert totalSendingTime > 0;
		return totalSendingTime;
	}

	/**
	 * Calculate the time it takes to upload the message via the given link (if
	 * sending would be possible at the full RawBandwidth of the PHY). A more
	 * advanced MAC will add some coding (making the message larger) and access
	 * control scheme for multiple transmissions.
	 * 
	 * This time is later used to account for energy consumption at sender and
	 * receiver.
	 * 
	 * @param msg
	 * @param l
	 * @return
	 */
	protected long getUploadTime(Message msg, Link l, boolean isBroadcast) {
		// Size is in byte, bandwidth in bit/s
		long uploadTime = Math.max(
				msg.getSize() * 8 * Time.SECOND / l.getBandwidth(isBroadcast),
				Time.MICROSECOND);
		assert uploadTime > 0;
		return uploadTime;
	}

	@Override
	public PhyType getPhyType() {
		return phy;
	}

	/**
	 * Overwrite this method to implement additional event handling, if needed.
	 * 
	 * @param se
	 */
	protected void handleEvent(Object data, int type) {
		// not needed here.
	}

	@Override
	public final void eventOccurred(Object data, int type) {
		if (type == MESSAGE_RECEIVED) {
			if (data instanceof MacEventInformation) {
				MacEventInformation meInfo = (MacEventInformation) data;
				meInfo.arrivedAt(this, !isOnline());
				if (isOnline()) {
					currentBandwidth.incomingTransmission(meInfo.getMessage()
							.getSize());
					this.handleReceivedMessage(meInfo);
				} else {
					// receiving mac (our mac) is offline
					if (!meInfo.isBroadcast()) {
						this.messageDropped(DropReason.RECEIVING_MAC_OFFLINE,
								meInfo.getMessage());
					}
				}
			} else {
				throw new AssertionError(
						"A SimulationEvents getData() in the MAC has to return a MacEventInformation!");
			}
		} else if (type == MESSAGE_DROPPED) {
			/*
			 * We use MESSAGE_DROPPED to schedule events for messages that are
			 * dropped. This helps us to send messages "one after the other" by
			 * using a callback from the receiving MAC even if the message was
			 * not transmitted.
			 */
			if (data instanceof MacEventInformation) {
				MacEventInformation meInfo = (MacEventInformation) data;
				meInfo.arrivedAt(this, true);
			} else {
				throw new AssertionError(
						"A SimulationEvents getData() in the MAC has to return a MacEventInformation!");
			}
		}
		handleEvent(data, type);
	}

	@Override
	public boolean isOnline() {
		return isOnline;
	}

	@Override
	public void goOffline() {
		energyComponent.turnOff();
		isOnline = false;
		/*
		 * TODO Maybe its cooler to let the entries timeout instead...
		 * Suggestions?
		 */
		// drop messages in queue
		Iterator<QueueEntry> it = queue.iterator();
		while (it.hasNext()) {
			QueueEntry entry = it.next();
			it.remove();
			messageDropped(DropReason.SENDING_MAC_OFFLINE, entry.getMessage());
		}
	}

	@Override
	public void goOnline() {
		/*
		 * Only go online if the battery still permits it
		 */
		if (energyComponent.turnOn()) {
			isOnline = true;
		}
	}

	@Override
	public final void send(MacAddress receiver, LinkLayerMessage message) {
		if (!isOnline()) {
			// we are offline
			messageDropped(DropReason.SENDING_MAC_OFFLINE, message);
		} else {
			// clean up the queue
			removeOutdatedQueueEntries();
			if (maxQueueLength > 0 && queue.size() >= maxQueueLength) {
				// queue is full, even after clean up
				messageDropped(DropReason.QUEUE_FULL, message);

				// FIXME BR DEBUG Print it
				// int idx = 0;
				// for (QueueEntry qe : queue) {
				// Message msg = qe.getMessage();
				// while (msg.getPayload() != null) {
				// msg = msg.getPayload();
				// }
				// System.out.println(idx + " "
				// + msg.getClass().getSimpleName() + " "
				// + msg.toString());
				// idx++;
				// }
			} else {
				// still a spot in the queue, add message and notify MAC
				queue.add(new QueueEntry(receiver, message));
				handleNewQueueEntry();
			}
		}
	}

	@Override
	public final MacAddress getMacAddress() {
		return macAddress;
	}
	
	@Override
	public NetID getNetId() {
		if (netID == null) {
			// Wow... :)
			netID = host.getNetworkComponent().getByName(getPhyType().getNetInterfaceName()).getLocalInetAddress();
			if (netID == null) {
				throw new AssertionError();
			}
		}
		return netID;
	}

	@Override
	public final void setMessageListener(LinkMessageListener listener) {
		this.messageListener = listener;
	}

	@Override
	public final SimHost getHost() {
		return host;
	}

	@Override
	public String toString() {
		return "MacLayer: " + macAddress.toString() + " " + phy.toString();
	}

	/**
	 * An entry in the Message queue of the MAC
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 21.02.2012
	 */
	public class QueueEntry {

		/**
		 * The timestamp when the message was added to the queue
		 */
		private long timeEntered;

		private MacAddress receiver;

		private LinkLayerMessage message;

		public QueueEntry(MacAddress receiver, LinkLayerMessage message) {
			this.receiver = receiver;
			this.message = message;
			this.timeEntered = Time.getCurrentTime();
		}

		public LinkLayerMessage getMessage() {
			return message;
		}

		public MacAddress getReceiver() {
			return receiver;
		}

		public long getTimeEntered() {
			return timeEntered;
		}

	}

	/**
	 * A Stub for an {@link EnergyCommunicationComponent} to allow simulations
	 * without an energy model. Otherwise we would have to flood the code with
	 * if == null statements.
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 27.02.2012
	 */
	private class EnergyComponentStub implements EnergyCommunicationComponent {

		private PhyType phy;

		private boolean on = false;

		public EnergyComponentStub(PhyType phy) {
			this.phy = phy;
		}

		@Override
		public ComponentType getType() {
			return ComponentType.COMMUNICATION;
		}

		@Override
		public void setEnergyEventListener(EnergyEventListener listener) {
			// not interested
		}

		@Override
		public void eventOccurred(Object content, int type) {
			// will not happen
		}

		@Override
		public PhyType getPhyType() {
			return phy;
		}

		@Override
		public void send(long duration, Message msg, boolean isBrodcast) {
			// nothing to do
		}

		@Override
		public void receive(long duration, Message msg, boolean isBroadcast,
				boolean isIntendedReceiver) {
			// nothing to do
		}

		@Override
		public void turnOff() {
			on = false;
		}

		public boolean turnOn() {
			on = true;
			return true;
		}

		@Override
		public boolean isOn() {
			return on;
		}

		@Override
		public void doFakeStateChange() {
			// TODO Auto-generated method stub
			
		}

	}

	/**
	 * A Live-Analyzer that aggregates Message Drop reasons inside the MAC.
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 26.03.2012
	 */
	public class MacMessagesDropReasonProgress implements ProgressValue {

		public long[] _messageDropCounters;

		public final String name = "Link " + getPhyType().toString()
				+ " DropReason";

		public MacMessagesDropReasonProgress() {
			_messageDropCounters = new long[DropReason.values().length];
		}

		@Override
		public String getName() {
			return name;
		}

		public void increment(DropReason reason) {
			_messageDropCounters[reason.ordinal()]++;
		}

		@Override
		public String getValue() {
			StringBuffer out = new StringBuffer();
			for (DropReason reason : DropReason.values()) {
				out.append(reason.toString());
				out.append(": ");
				out.append(_messageDropCounters[reason.ordinal()]);
				out.append(", \n");
			}
			return out.toString();
		}

	}

	private static boolean _linkAnalyzerInitialized = false;

	private static LinklayerAnalyzer _linkAnalyzer = null;

	protected static void _linkMsgEvent(LinkLayerMessage msg, SimHost host,
			Reason reason) {
		if (!_linkAnalyzerInitialized) {
			try {
				_linkAnalyzer = Monitor.get(LinklayerAnalyzer.class);
			} catch (AnalyzerNotAvailableException e) {
				_linkAnalyzer = null;
			}
			_linkAnalyzerInitialized = true;
		}
		if (_linkAnalyzerInitialized && _linkAnalyzer != null) {
			_linkAnalyzer.linkMsgEvent(msg, host, reason);
		}
	}

}
