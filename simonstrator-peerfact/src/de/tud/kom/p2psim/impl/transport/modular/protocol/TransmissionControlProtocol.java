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

package de.tud.kom.p2psim.impl.transport.modular.protocol;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.tud.kom.p2psim.api.analyzer.TransportAnalyzer;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.network.NetProtocol;
import de.tud.kom.p2psim.api.network.SimNetInterface;
import de.tud.kom.p2psim.api.transport.TransProtocol;
import de.tud.kom.p2psim.impl.linklayer.mac.EnqueuingMac;
import de.tud.kom.p2psim.impl.network.IPv4NetID;
import de.tud.kom.p2psim.impl.network.routed.FragmentReceivedInfo;
import de.tud.kom.p2psim.impl.transport.AbstractTransMessage;
import de.tud.kom.p2psim.impl.transport.DefaultTransInfo;
import de.tud.kom.p2psim.impl.transport.TCPMessage;
import de.tud.kom.p2psim.impl.transport.modular.AbstractTransProtocol;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransInfo;

/**
 * This implementation provides basic TCP-functionality for larger files. It
 * makes use of global knowledge to prevent sending real ACK-Messages. This is
 * to support sending large messages, it is still not a stream-based approach.
 * The receiver is informed, as soon as the complete (!) message arrived.
 * 
 * Simplifications in this model: ACKs have a transmission time of 0s and are
 * never dropped. Selective ACKs are used, which means that only packets that
 * were dropped are actually retransmitted. As soon as no packets are left in
 * the queue, the session is considered closed. A closed session is restarted
 * when new packets are added by the application. before sending actual payload,
 * a simple 1-way-callback-Handshake is sent to determine, if the connection is
 * possible at all without sending that much payload.
 * 
 * You should use at least the {@link EnqueuingMac} with this model to ensure
 * proper bandwidth sharing if multiple sources send to one receiver.
 * 
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 07.05.2012
 */
public class TransmissionControlProtocol extends AbstractTransProtocol {

	/**
	 * Better scalability with only a minor trade off in accuracy: assume a
	 * larger MTU. This has effects on the bandwidth depending on whether
	 * NetLayer-Fragmenting is enabled or not.
	 * 
	 * Set this to 1 to achieve "real" TCP behavior. In such a case, the
	 * NetLayer will never fragment a packet by this layer, as the packets
	 * issued by this layer are smaller than the MTU.
	 */
	public static int SCALING_FACTOR = 1;

	/**
	 * Size of a fragment
	 */
	private static int FRAGMENT_SIZE = (PhyType.ETHERNET.getDefaultMTU()
			- NetProtocol.IPv4.getHeaderSize() - TransProtocol.TCP
				.getHeaderSize()) * SCALING_FACTOR;

	public static int MAX_RETRANSMISSIONS = 12;

	/**
	 * cf. http://support.microsoft.com/kb/2786464
	 */
	public static int MAX_SYN_RETRANSMISSIONS = 2;

	/**
	 * Flag for using real TCP acknowledgment. Every second message will be
	 * acknowledged with a real Message, otherwise all messages will be directly
	 * acknowledged (directly call of the opposite layer)
	 */
	public static boolean USE_REAL_TCP_ACK = false;

	/**
	 * Global Knowledge: a Map of all TCP-Hosts to simulate ACKs without
	 * actually sending them.
	 */
	public static Map<NetID, TransmissionControlProtocol> tcps = new LinkedHashMap<NetID, TransmissionControlProtocol>();

	/**
	 * TCP Session Handler
	 */
	private TCPSessionHandler sessionHandler = new TCPSessionHandler();

	/**
	 * TCP Listeners
	 */
	// private Map<Short, ITransProtocolListener> protocolListeners = new
	// LinkedHashMap<Short, ITransProtocolListener>();

	/**
	 * TCP (simple)
	 */
	public TransmissionControlProtocol(SimHost host, SimNetInterface netLayer) {
		super(host, netLayer, NetProtocol.IPv4);
		tcps.put(netLayer.getLocalInetAddress(), this);
	}

	@Override
	public int getHeaderSize() {
		return 20;
	}

	@Override
	public void send(Message msg, NetID receiverNet, int receiverPort,
			int senderPort, int commId, boolean isReply) {
		if (receiverNet.equals(IPv4NetID.LOCAL_BROADCAST)) {
			throw new AssertionError("Broadcasting over TCP is not possible!");
		}

		/*
		 * Find Session
		 */
		TCPSession session = sessionHandler.getSession(receiverNet,
				receiverPort);
		if (!session.idle)
			session.cwnd = 1;
		/*
		 * Fragmenting
		 */
        long messageSize = msg.getSize();
		int numberOfFragments = (int) Math.ceil((double) messageSize
				/ (double) FRAGMENT_SIZE);
		long lastFragmentSize = messageSize % FRAGMENT_SIZE;
		FragmentedTCPMessage tcpMsg = null;
		FragmentReceivedInfo fragmentInfo = new FragmentReceivedInfo();
		for (int i = 1; i <= numberOfFragments; i++) {
			if (i == numberOfFragments) {
				// Last Fragment
				tcpMsg = new FragmentedTCPMessage(msg, senderPort,
						receiverPort, commId, isReply,
						lastFragmentSize, fragmentInfo, i, numberOfFragments);
			} else {
				tcpMsg = new FragmentedTCPMessage(msg, senderPort,
						receiverPort, commId, isReply, FRAGMENT_SIZE,
						fragmentInfo, i, numberOfFragments);
			}
			// add each fragment to the session
			session.addMessage(tcpMsg);
		}

		/*
		 * Pseudo-message for the monitor (no fragmenting)
		 */
		if (hasAnalyzer) {
			TCPMessage unfragmentedMessage = new TCPMessage(msg, senderPort,
					receiverPort, commId, isReply, 0);
			transportAnalyzerProxy.transMsgEvent(unfragmentedMessage,
					getHost(), TransportAnalyzer.Reason.SEND);
		}

	}

	/**
	 * Called as soon as a message originating from this TCP is received. The
	 * receiverInfo is the identifier of the session
	 *
     * @param receiverInfo
     * @param transMsg
	 */
	public void receivedAck(TransInfo receiverInfo,
			FragmentedTCPMessage transMsg) {
		/*
		 * notify the corresponding session
		 */
		TCPSession session = sessionHandler.getSession(receiverInfo.getNetId(),
				receiverInfo.getPort());
		session.gotAck(transMsg);
	}

	@Override
	public TCPMessage receive(Message transMsg, TransInfo senderTransInfo) {
		/*
		 * Simulate the ACK
		 */
		FragmentedTCPMessage msg = (FragmentedTCPMessage) transMsg;
		boolean complete = msg.getFragmentInfo().receivedFragment(
				msg.getFragmentNumber(), msg.getTotalNumberOfFragments());

		if (msg instanceof TCPAckMessage) {
			receivedAck(senderTransInfo,
					((TCPAckMessage) transMsg).getOriginalMsg());
			return null;
		} else {
			if (USE_REAL_TCP_ACK && msg.getFragmentNumber() % 2 == 0) {
				doSend(new TCPAckMessage(msg), senderTransInfo.getNetId());
			} else {
				// call direct the opposite receive method with a TCPAckMessage
				TCPAckMessage ackMsg = new TCPAckMessage(msg);
				tcps.get(senderTransInfo.getNetId()).receive(
						ackMsg,
						DefaultTransInfo.getTransInfo(getNetInterface()
								.getLocalInetAddress(), msg.getReceiverPort()));
			}

		}
		if (msg instanceof TCPHandshakeMessage) {
			/*
			 * Do not pass TCP Handshakes to higher layers, we just want the ACK
			 */
			return null;
		}
		/*
		 * Account for fragmenting, only return true if all fragments arrived -
		 * and only do this once (with a complete Message)
		 */
		if (complete && !msg.getFragmentInfo().isListenerNotified()) {
			msg.getFragmentInfo().setListenerNotified();
			/*
			 * We mask the Fragmentation-behavior for the applications and the
			 * analyzer
			 */
			return new TCPMessage(msg.getPayload(), msg.getSenderPort(),
					msg.getReceiverPort(), msg.getCommId(), msg.isReply(), 0);
		}
		return null;
	}

	/**
	 * This class manages open TCPSessions
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 08.05.2012
	 */
	public class TCPSessionHandler {

		private Map<NetID, List<TCPSession>> sessions = new LinkedHashMap<NetID, List<TCPSession>>();

		public TCPSessionHandler() {
		}

		public TCPSession getSession(NetID receiver, int port) {
			List<TCPSession> receiverSessions = sessions.get(receiver);
            if (receiverSessions == null) {
            	receiverSessions = new LinkedList<TCPSession>();
            	sessions.put(receiver, receiverSessions);
            }
            for (TCPSession session : receiverSessions) {
                if (session.getPort() == port) {
                    return session;
                }
            }
            TCPSession newSession = new TCPSession(receiver, port);
            receiverSessions.add(newSession);

            return newSession;
		}

	}

	/**
	 * A TCP Session is identified by a clients netID and the port. Sessions are
	 * used to keep track of the current flow and to retransmit packets if
	 * needed.
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 07.05.2012
	 */
	public class TCPSession implements EventHandler {

		private NetID receiver;

		private int port;

		private long srtt = 0;

		private long rttvar = 0;

		/**
		 * Timeout
		 */
		private long rto = 3 * Time.SECOND;

		private LinkedList<FragmentedTCPMessage> outgoingQueue = new LinkedList<FragmentedTCPMessage>();

		/**
		 * Number of possible parallel message transmissions
		 */
		protected int cwnd = 1;

		/**
		 * current parallel message transmissions (ie this lies between 0 and
		 * cwnd)
		 */
		private int openTransmissions = 0;

		private static final long SESSION_TIMEOUT = 5 * Time.SECOND;

		/**
		 * If a connection is IDLE, it will require a new Handshake after 5
		 * seconds of inactivity
		 */
		private long nextSessionTimeout = Time.getCurrentTime()
				+ SESSION_TIMEOUT;

		private final int CWND_MIN = 1;

		private int sstrsh = 1;

		private long lastIncrease = 0;

		private final double CWND_ALPHA = 0.125; // default 0.125

		private final double CWND_BETA = 0.25; // default 0.25

		private final long RTO_LOWER_BOUND = 1 * Time.SECOND;

		private final long RTO_UPPER_BOUND = 60 * Time.SECOND;

		protected boolean idle = true;

		private int synRetransmissions = 0;

		/**
		 * A new Session for the given receiver and port
		 * 
		 * @param receiver
		 * @param port
		 */
		public TCPSession(NetID receiver, int port) {
			this.receiver = receiver;
			this.port = port;
		}

		public int getPort() {
			return port;
		}

		@Override
		public void eventOccurred(Object se, int type) {
			/*
			 * Timeout
			 */
			gotTimeout((FragmentedTCPMessage) se);
		}

		/**
		 * Sends up to cwnd messages
		 */
		private void sendNextBatch() {
			for (int i = openTransmissions; i < cwnd; i++) {
				if (!sendAndStartTimeout()) {
					break;
				}
			}
			if (outgoingQueue.isEmpty() && openTransmissions == 0) {
				idle = true;
			}
		}

		/**
		 * Send the head of the queue, returns false, if the queue is empty
		 * 
		 * @return
		 */
		private boolean sendAndStartTimeout() {
			/*
			 * Send next Message
			 */
			FragmentedTCPMessage msg = outgoingQueue.poll();
			if (msg == null) {
				/*
				 * No message in queue, just wait.
				 */
				return false;
			}

			/*
			 * Schedule timeout-event
			 */
			Event.scheduleWithDelay(rto, this, msg, 0);
			openTransmissions++;

			msg.setSentTimestamp(Time.getCurrentTime());
			doSend(msg, receiver);
			return true;
		}

		/**
		 * Adds a message to this session (ie to the outgoing stream)
		 * 
		 * @param msg
		 */
		public void addMessage(FragmentedTCPMessage msg) {
			outgoingQueue.add(msg);
			if (idle) {
				if (nextSessionTimeout < Time.getCurrentTime()) {
					doHandshake(msg.getSenderPort());
					nextSessionTimeout = Time.getCurrentTime()
							+ SESSION_TIMEOUT;
				} else {
					sendAndStartTimeout();
				}
				idle = false;
			}
		}

		/**
		 * Perform a handshake with a very small message to detect, if a
		 * connection is possible at all
		 */
		public void doHandshake(int senderPort) {
			TCPHandshakeMessage handshake = new TCPHandshakeMessage(senderPort,
					port);
			outgoingQueue.addFirst(handshake);
			sendAndStartTimeout();
		}

		/**
		 * timeout
		 * 
		 * @param msg
		 */
		public void gotTimeout(FragmentedTCPMessage msg) {
			/*
			 * Timeout, check if a resend is needed - and if so, do it by adding
			 * the msg to the head of the queue and call sendAndStartTimout
			 */
			if (msg.isAcked() || idle) {
				// already acked or idle
				return;
			}
			openTransmissions--;

			msg.incrementRetransmissionCounter();
			if (msg.getRetransmissionCounter() > MAX_RETRANSMISSIONS) {
				/*
				 * A TCP-Drop occurred, so we must assume that the receiver is
				 * not online or reachable anymore. Queue is emptied, app is
				 * warned if it was registered as callback
				 */
				outgoingQueue.clear();
				openTransmissions = 0;
				idle = true;
				// notifyProtocolListener(msg.getSenderPort(),
				// DefaultTransInfo.getTransInfo(receiver, port));
				return;
			}
			outgoingQueue.addFirst(msg);

			if (msg instanceof TCPHandshakeMessage) {
				synRetransmissions++;
				if (synRetransmissions > MAX_SYN_RETRANSMISSIONS) {
					synRetransmissions = 0;
					outgoingQueue.clear();
					openTransmissions = 0;
					idle = true;
					return;
				}
				/*
				 * Resend just the Handshake
				 */
				sendAndStartTimeout();
			} else {
				/*
				 * Reset the cwnd to half its size
				 */
				sstrsh = cwnd / 2;
				cwnd = Math.max(1, CWND_MIN);
				sendNextBatch();
			}

		}

		/**
		 * acked
		 * 
		 * @param msg
		 */
		public void gotAck(FragmentedTCPMessage msg) {

			/*
			 * Prevent duplicate ACKs
			 */
			if (msg.isAcked() || idle) {
				return;
			}
			msg.setAcked();

			if (openTransmissions == 0) {
				/*
				 * Indicator: received an ACK after the timeout fired
				 */
				// throw new AssertionError("Not supposed to happen!");
			}

			openTransmissions--;

			/*
			 * Handshake-ACK? Then start sending the real stuff
			 */
			if (msg instanceof TCPHandshakeMessage) {
				sendNextBatch();
				return;
			}
			long current = Time.getCurrentTime();
			/*
			 * Estimate the new CWND/RTT
			 */
			if (srtt == 0) {
				rttvar = current - msg.getSentTimestamp();
				srtt = rttvar * 2;
				rto = 5 * srtt;
			} else {
				long newRtt = 2 * (current - msg.getSentTimestamp());
				srtt = (long) ((1 - CWND_ALPHA) * srtt + CWND_ALPHA * newRtt);
				rttvar = (long) ((1 - CWND_BETA) * rttvar + CWND_BETA
						* Math.abs(srtt - newRtt));
				rto = srtt + 4 * rttvar; // default: 4 times
			}

			// It exists a lower and a upper bound for RTO!
			rto = Math.min(Math.max(rto, RTO_LOWER_BOUND), RTO_UPPER_BOUND);

			/*
			 * If all previous fragments of this message have been received, we
			 * should increase the CWND, up to its maximum value.
			 */
			if (msg.getFragmentInfo().isAllReceivedUpTo(cwnd)
					&& (cwnd < sstrsh || lastIncrease + srtt < current)) {
				lastIncrease = current;
				cwnd++;
			} else if (!msg.getFragmentInfo().isAllReceivedUpTo(cwnd)) {
				sstrsh = cwnd / 2;
				cwnd = sstrsh;
			}
			sendNextBatch();

		}

        public long getSize() {
            return outgoingQueue.size();
        }

		@Override
		public String toString() {
			return "TCP[-> " + receiver.toString() + ":" + port + "]";
		}

	}

	/**
	 * This message is used on an idle or newly created session to establish the
	 * connection and to detect whether a host is reachable at all.
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 09.05.2012
	 */
	public static class TCPHandshakeMessage extends FragmentedTCPMessage {

		public TCPHandshakeMessage(int senderPort, int receiverPort) {
			super(null, senderPort, receiverPort, -1, false, 0,
					new FragmentReceivedInfo(), 1, 1);
		}

	}

	/**
	 * This message is used to Acknowledge a received message!
	 * 
	 * @author Christoph Münker
	 * @version 1.0, 29.01.2013
	 */
	public static class TCPAckMessage extends FragmentedTCPMessage {

		FragmentedTCPMessage orgMsg;

		public TCPAckMessage(FragmentedTCPMessage msg) {
			super(null, msg.getReceiverPort(), msg.getSenderPort(), (short) -1,
					false, 0, new FragmentReceivedInfo(), 1, 1);
			this.orgMsg = msg;
		}

		public FragmentedTCPMessage getOriginalMsg() {
			return orgMsg;
		}

	}

	/**
	 * This is a TCP message that has been split into MTU-sized fragments to
	 * allow fair congestion control. It utilizes the
	 * {@link FragmentReceivedInfo} of the RoutedNetlayer.
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 08.05.2012
	 */
	public static class FragmentedTCPMessage extends AbstractTransMessage {

		private FragmentReceivedInfo fragmentInfo;

		private int fragmentNo;

		private int numberOfFragments;

		private long sizeOfFragment;

		private long sentTimestamp;

		/**
		 * Cheating: to lower complexity, each message knows its retransmission
		 * count
		 */
		private int retransmissionCounter = 0;

		private int timeoutCounter = 0;

		/**
		 * Cheating: to lower complexity, prevent duplicate ACKs
		 */
		private boolean acked = false;

		/**
		 * A fragment of a TCP-Message
		 * 
		 * @param payload
		 * @param senderPort
		 * @param receiverPort
		 * @param commId
		 * @param isReply
		 * @param sizeOfFragment
		 * @param fragmentInfo
		 * @param fragmentNo
		 * @param numberOfFragments
		 */
		public FragmentedTCPMessage(Message payload, int senderPort,
				int receiverPort, int commId, boolean isReply,
				long sizeOfFragment, FragmentReceivedInfo fragmentInfo,
				int fragmentNo, int numberOfFragments) {
			super(TransProtocol.TCP, payload, senderPort, receiverPort, commId,
					isReply);
			this.fragmentInfo = fragmentInfo;
			this.fragmentNo = fragmentNo;
			this.numberOfFragments = numberOfFragments;
			this.sizeOfFragment = sizeOfFragment;
		}

		public void setSentTimestamp(long sentTimestamp) {
			this.sentTimestamp = sentTimestamp;
		}

		public long getSentTimestamp() {
			return sentTimestamp;
		}

		@Override
		public long getSize() {
			return getProtocol().getHeaderSize() + sizeOfFragment;
		}

		public int getFragmentNumber() {
			return fragmentNo;
		}

		public int getTotalNumberOfFragments() {
			return numberOfFragments;
		}

		public FragmentReceivedInfo getFragmentInfo() {
			return fragmentInfo;
		}

		public int getRetransmissionCounter() {
			return retransmissionCounter;
		}

		public void incrementRetransmissionCounter() {
			retransmissionCounter++;
		}

		public boolean isAcked() {
			return acked;
		}

		public void setAcked() {
			acked = true;
		}

		public int getTimeoutCounter() {
			return timeoutCounter;
		}

		public void incrementTimeoutCounter() {
			timeoutCounter++;
		}

	}

}
