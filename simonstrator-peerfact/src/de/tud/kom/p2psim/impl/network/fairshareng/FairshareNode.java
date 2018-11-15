package de.tud.kom.p2psim.impl.network.fairshareng;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.tud.kom.p2psim.api.analyzer.MessageAnalyzer.Reason;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.network.BandwidthImpl;
import de.tud.kom.p2psim.api.network.FlowBasedNetlayer;
import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.api.network.NetProtocol;
import de.tud.kom.p2psim.api.transport.TransProtocol;
import de.tud.kom.p2psim.impl.network.AbstractNetLayer;
import de.tud.kom.p2psim.impl.network.IPv4Message;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;
import de.tud.kom.p2psim.impl.transport.AbstractTransMessage;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;

/**
 * The Class Node.
 */
public class FairshareNode extends AbstractNetLayer implements
		FlowBasedNetlayer {


	/** The subnet. */
	private final FairshareSubnet subnet;

	/** The host queues. */
	private final Map<FairshareNode, LinkedList<NetMessage>> hostQueues;

	/** The Constant FLOAT_DELTA to correct Floats 9.999 to 10. */
	private final static float FLOAT_DELTA = 1e-7f;

	/** The hash code. */
	private final int hashCode;

	/**
	 * Instantiates a new node.
	 * @param netID
	 * @param geoLoc
	 */
	public FairshareNode(SimHost host, FairshareSubnet subnet, NetID netID,
			BandwidthImpl maxBandwidth, Location position,
			NetMeasurementDB.Host hostMeta) {
		super(host, netID, maxBandwidth, position, hostMeta);
		this.subnet = subnet;

		this.hostQueues = new LinkedHashMap<FairshareNode, LinkedList<NetMessage>>();

		this.hashCode = this.getNetID().hashCode();
	}

	/**
	 * Adds rate to the current down rate.
	 * 
	 * @param downRate
	 * 
	 *            the down rate
	 * @throws Exception
	 *             the exception
	 */
	public void addCurrentDownRate(long downRate) throws Exception {

		final long currentDownBW = this.getCurrentBandwidth().getDownBW();
		long realDownRate = currentDownBW - downRate;

		/* Fix float, in case we get 9.999 save 10. */
		if( Math.abs(Math.round(realDownRate) - realDownRate) < FLOAT_DELTA ) {
			realDownRate = Math.round(realDownRate);
		}

		this.getCurrentBandwidth().setDownBW(realDownRate);

	}

	/**
	 * Adds rate to the current up rate.
	 * 
	 * @param upRate
	 *            the up rate
	 * @throws Exception
	 *             the exception
	 */
	public void addCurrentUpRate(long upRate) throws Exception {

		final long currentUpBW = this.getCurrentBandwidth().getUpBW();
		long realUpRate = currentUpBW - upRate;

		/* Fix float, in case we get 9.999 save 10. */
		if( Math.abs(Math.round(realUpRate) - realUpRate) < FLOAT_DELTA ) {
			realUpRate = Math.round(realUpRate);
		}

		this.getCurrentBandwidth().setUpBW(realUpRate);

	}


	/**
	 * Resets the node by setting current rates to zero.
	 */
	public void reset() {
		this.setCurrentBandwidth(this.getMaxBandwidth().clone());
	}
	
	
	/* (non-Javadoc)
	 * @see de.tud.kom.p2psim.impl.network.AbstractNetLayer#goOffline()
	 */
	@Override
	public void goOffline() {
		super.goOffline();
		this.subnet.disconnectHost(this);
	}


	/* (non-Javadoc)
	 * @see de.tud.kom.p2psim.api.network.NetLayer#send(de.tud.kom.p2psim.api.common.Message, de.tud.kom.p2psim.api.network.NetID, de.tud.kom.p2psim.api.network.NetProtocol)
	 */
	@Override
	public void send(Message msg, NetID receiverId, NetProtocol protocol) {

		if (isOnline()) {
			assert (msg.getSize() >= 0);
			assert (isSupported(((AbstractTransMessage) msg).getProtocol()));

			final NetMessage netMsg = new IPv4Message(msg, receiverId, this.getNetID());
			final TransProtocol tpMsg = ((AbstractTransMessage) msg).getProtocol();
			if (tpMsg.equals(TransProtocol.UDP)) {

				if (hasAnalyzer) {
					netAnalyzerProxy
							.netMsgEvent(netMsg, getHost(), Reason.SEND);
				}
				this.subnet.sendUDP(netMsg);

			} else if (tpMsg.equals(TransProtocol.TCP)) {
				final FairshareNode receiver = this.subnet.getNetLayer(receiverId);

				LinkedList<NetMessage> queuedMessages = this.hostQueues.get(receiver);
				if (queuedMessages == null) {
					queuedMessages = new LinkedList<NetMessage>();
					this.hostQueues.put(receiver, queuedMessages);
				}

				if (hasAnalyzer) {
					netAnalyzerProxy
							.netMsgEvent(netMsg, getHost(), Reason.SEND);
				}
				if (queuedMessages.isEmpty()) {

					try {
						this.subnet.sendTCPMessage(netMsg);
					} catch (final Exception e) {

						/*
						 * Can't throw exception here as send(Message msg, NetID receiverId, NetProtocol protocol) is overwritten.
						 */

						Monitor.log(FairshareNode.class, Level.ERROR,
								"Exception..: sendTCP failed. %s", e);
						assert(false) : "sendTCP failed: " + e;

					}

				}
				queuedMessages.add(netMsg);

			} else {

				/*
				 * Can't throw exception here as send(Message msg, NetID receiverId, NetProtocol protocol) is overwritten.
				 */
				Monitor.log(FairshareNode.class, Level.ERROR,
						"Unsupported transport protocol " + tpMsg);
				assert (false) : "Unsupported transport protocol " + tpMsg;

			}
		} else {

			Monitor.log(FairshareNode.class, Level.WARN, "Host " + this
					+ " is offline.");

		}

	}

	/* (non-Javadoc)
	 * @see de.tud.kom.p2psim.impl.network.AbstractNetLayer#isSupported(de.tud.kom.p2psim.api.transport.TransProtocol)
	 */
	@Override
	protected boolean isSupported(TransProtocol protocol) {
		return (protocol.equals(TransProtocol.UDP) || protocol.equals(TransProtocol.TCP));
	}

	/**
	 * Checks if message queue is empty.
	 *
	 * @param receiver the receiver
	 * @return true, if is message queue empty
	 */
	public boolean isMessageQueueEmpty(FairshareNode receiver) {
		return this.hostQueues.get(receiver).isEmpty();
	}

	/**
	 * Peek message queue and return size of next expected arrival.
	 *
	 * @param receiver the receiver
	 * @return the double
	 */
	public double peekMessageQueue(FairshareNode receiver) {
		return this.hostQueues.get(receiver).get(0).getSize();
	}

	/**
	 * Gets a read-only view on message queue.
	 *
	 * @param receiver the receiver
	 * @return the view on message queue
	 */
	public List<NetMessage> getViewOnMessageQueue(FairshareNode receiver) {
		return Collections.unmodifiableList(this.hostQueues.get(receiver));
	}

	/**
	 * Removes the message from queue.
	 *
	 * @param receiver the receiver
	 * @return the net message
	 */
	public NetMessage removeMessageFromQueue(FairshareNode receiver) {
		return this.hostQueues.get(receiver).remove(0);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof FairshareNode) ? ((FairshareNode) obj).getNetID().hashCode() == this.getNetID().hashCode() : super.equals(obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getLocalInetAddress() + " (U:"
				+ this.getCurrentBandwidth().getUpBW() + "/D:"
				+ this.getCurrentBandwidth().getDownBW() + ")";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		/* Precomputed to save time. */
		return this.hashCode;
	}
}
