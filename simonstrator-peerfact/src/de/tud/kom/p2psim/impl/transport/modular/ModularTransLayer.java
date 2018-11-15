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

package de.tud.kom.p2psim.impl.transport.modular;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.tud.kom.p2psim.api.analyzer.MessageAnalyzer.Reason;
import de.tud.kom.p2psim.api.analyzer.TransportAnalyzer;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.common.SimHostComponent;
import de.tud.kom.p2psim.api.network.FlowBasedNetlayer;
import de.tud.kom.p2psim.api.network.NetMessageEvent;
import de.tud.kom.p2psim.api.network.NetMessageListener;
import de.tud.kom.p2psim.api.network.SimNetInterface;
import de.tud.kom.p2psim.api.transport.TransMessage;
import de.tud.kom.p2psim.api.transport.TransProtocol;
import de.tud.kom.p2psim.impl.transport.DefaultTransInfo;
import de.tud.kom.p2psim.impl.transport.modular.protocol.TransmissionControlProtocol;
import de.tud.kom.p2psim.impl.transport.modular.protocol.TransmissionControlProtocolDummy;
import de.tud.kom.p2psim.impl.transport.modular.protocol.UserDatagramProtocol;
import de.tud.kom.p2psim.impl.util.LiveMonitoring;
import de.tud.kom.p2psim.impl.util.livemon.AvgAccumulatorDouble;
import de.tud.kom.p2psim.impl.util.livemon.AvgAccumulatorTime;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent.AnalyzerNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetInterface;
import de.tudarmstadt.maki.simonstrator.api.component.transport.MessageBasedTransport;
import de.tudarmstadt.maki.simonstrator.api.component.transport.ProtocolNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.transport.ServiceNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransInfo;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransMessageCallback;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransMessageListener;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransportComponent;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransportProtocol;
import de.tudarmstadt.maki.simonstrator.api.component.transport.protocol.TCPMessageBased;
import de.tudarmstadt.maki.simonstrator.api.component.transport.protocol.UDP;
import de.tudarmstadt.maki.simonstrator.api.component.transport.service.FirewallService;
import de.tudarmstadt.maki.simonstrator.api.component.transport.service.PiggybackMessageService;
import de.tudarmstadt.maki.simonstrator.api.component.transport.service.TransportService;

/**
 * This TransLayer provides flow control and congestion control as modular
 * extensions - which could be as complex as a real TCP implementation. Main
 * goal: provide sufficient support for large files via TCP by scheduling
 * appropriate fragments instead of one big message.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 07.05.2012
 */
public class ModularTransLayer implements SimHostComponent, TransportComponent,
		NetMessageListener {

	private SimHost host;

	/**
	 * Message Listeners (Overlays/Applications) grouped by port
	 */
	private Map<Integer, PortListener> portListeners = new LinkedHashMap<Integer, PortListener>();

	protected final List<FirewallService> firewallServices = new LinkedList<FirewallService>();

	protected final Map<Byte, PiggybackMessageService> piggybackServices = new LinkedHashMap<Byte, PiggybackMessageService>();

	private Map<NetID, AbstractTransProtocol> tcp;

	private Map<NetID, AbstractTransProtocol> udp;

	private boolean hasAnalyzer = false;

	private TransportAnalyzer transportAnalyzerProxy;

	public static AvgAccumulatorTime _avgSendAndWaitTime = new AvgAccumulatorTime(
			"Trans avg. SaW Delay", 1000);

	public static AvgAccumulatorDouble _avgSendAndWaitSuccess = new AvgAccumulatorDouble(
			"Trans avg. SaW Success", 1000);

	public static AvgAccumulatorDouble _avgMessageSize = new AvgAccumulatorDouble(
			"Trans avg. Message Size", 1000);

	private static boolean _analyzersInitialized = false;

	/**
	 * Create a new ModularTransLayer
	 */
	public ModularTransLayer(SimHost host) {
		this.host = host;
		if (!_analyzersInitialized) {
			LiveMonitoring.addProgressValueIfNotThere(_avgSendAndWaitTime);
			LiveMonitoring.addProgressValueIfNotThere(_avgSendAndWaitSuccess);
			LiveMonitoring.addProgressValueIfNotThere(_avgMessageSize);
			_analyzersInitialized = true;
		}
	}

	@Override
	public void initialize() {
		/*
		 * Binding all ITransProtocols
		 */
		tcp = new LinkedHashMap<NetID, AbstractTransProtocol>();
		udp = new LinkedHashMap<NetID, AbstractTransProtocol>();

		/*
		 * FIXME: this might not be the most elegant way, but for Flow-based
		 * Netlayers we use a dummy-TCP implementation that sends one giant TCP
		 * message and no ACKs.
		 */
		boolean usePrimitiveTCP = false;
		if (host.getNetworkComponent() instanceof FlowBasedNetlayer) {
			usePrimitiveTCP = true;
			Monitor.log(
					ModularTransLayer.class,
					Level.WARN,
					"Flow-based NetLayer detected - using Dummy-TCP implementation on the Transport layer.");
		}

		for (SimNetInterface net : host.getNetworkComponent()
				.getSimNetworkInterfaces()) {
			net.addNetMsgListener(this);

			if (usePrimitiveTCP) {
				tcp.put(net.getLocalInetAddress(),
						new TransmissionControlProtocolDummy(host, net));
			} else {
				tcp.put(net.getLocalInetAddress(),
						new TransmissionControlProtocol(host, net));
			}
			udp.put(net.getLocalInetAddress(), new UserDatagramProtocol(host,
					net));
		}

		/*
		 * Binding Analyzer-Proxy
		 */
		try {
			transportAnalyzerProxy = Monitor.get(TransportAnalyzer.class);
			hasAnalyzer = true;
		} catch (AnalyzerNotAvailableException e) {
			// no analyzer, no problem
		}

	}

	@Override
	public void shutdown() {
		tcp.clear();
		udp.clear();
	}

	@Override
	public SimHost getHost() {
		return host;
	}

	@Override
	public <T extends TransportProtocol> T getProtocol(
			Class<T> protocolInterface, NetID localAddress, int localPort)
			throws ProtocolNotAvailableException {
		/*
		 * Multi-Instantiation is not that much of a problem here... but an
		 * overlay should nevertheless chache this object!
		 */
		if (protocolInterface.equals(TCPMessageBased.class)) {
			return protocolInterface.cast(new ProtocolAdapter(localAddress,
					localPort, tcp.get(localAddress)));
		} else if (protocolInterface.equals(UDP.class)) {
			return protocolInterface.cast(new ProtocolAdapter(localAddress,
					localPort, udp.get(localAddress)));
		} else {
			throw new ProtocolNotAvailableException();
		}
	}

	@Override
	public <T extends TransportService> void registerService(
			Class<T> serviceInterface, T serviceImplementation)
			throws ServiceNotAvailableException {
		if (serviceInterface.equals(PiggybackMessageService.class)) {
			PiggybackMessageService ps = (PiggybackMessageService) serviceImplementation;
			if (piggybackServices.containsKey(ps.getPiggybackServiceID())) {
				throw new AssertionError("The Service ID is not unique!");
			}
			piggybackServices.put(ps.getPiggybackServiceID(), ps);
		} else if (serviceInterface.equals(FirewallService.class)) {
			firewallServices.add((FirewallService) serviceImplementation);
		} else {
			throw new ServiceNotAvailableException();
		}
	}

	/**
	 * Open Callbacks (used for the sendAndWait-method)
	 */
	protected Map<Integer, ReplyEvent> openCallbacks = new LinkedHashMap<Integer, ReplyEvent>();

	protected int currentCommId = 0;

	/**
	 * Adapter
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, May 14, 2013
	 */
	private class ProtocolAdapter implements MessageBasedTransport,
			TCPMessageBased, UDP, EventHandler {

		private static final int EVENT_SEND_AND_WAIT = 1;

		private final NetID localAddress;

		private final int localPort;

		private final ITransProtocol transport;
		
		private TransMessageListener listener;

		public ProtocolAdapter(NetID localAddress, int localPort,
				ITransProtocol transport) {
			this.transport = transport;
			this.localPort = localPort;
			this.localAddress = localAddress;
		}

		@Override
		public void setTransportMessageListener(TransMessageListener listener) {
			this.listener = listener;
			addTransMsgListener(listener, localPort);
		}

		/*
		 * FIXME add @Override lateron, as soon as the API is updated globally.
		 */
		public void removeTransportMessageListener() {
			assert listener != null;
			removeTransMsgListener(listener, localPort);
			listener = null;
		}

		@Override
		public int send(Message msg, NetID receiverNet, int receiverPort) {
			return this.sendAndWait(msg, receiverNet, receiverPort, null, 0);
		}

		@Override
		public int sendAndWait(Message msg, NetID receiverNet,
				int receiverPort, TransMessageCallback senderCallback,
				long timeout) {

			assert !receiverNet.equals(localAddress);
			/*
			 * send and wait: send the message, store the callback. After
			 * timeout, the callback is triggered, if no reply arrived in time
			 * via the sendReply-Method.
			 */
			currentCommId++;
			if ((senderCallback != null && timeout == 0)
					|| (senderCallback == null && timeout != 0)) {
				throw new AssertionError(
						"You can not use a TransMessageCallback if you did not specify a timeout > 0!");
			}

			_avgMessageSize.newVal(msg.getSize());

			/*
			 * Activate the Callback in a request-reply scenario
			 */
			if (senderCallback != null) {
				ReplyEvent replyEvent = new ReplyEvent(senderCallback,
						currentCommId);
				openCallbacks.put(currentCommId, replyEvent);
				Event.scheduleWithDelay(timeout, this, replyEvent,
						EVENT_SEND_AND_WAIT);
			}

			/*
			 * Check with the firewall-service, if the outgoing connection is
			 * permitted. If not, silently drop the message. in a sendAndWait,
			 * the timeout will fire after it expired.
			 */
			if (!firewallServices.isEmpty()) {
				for (FirewallService firewall : firewallServices) {
					if (!firewall.allowOutgoingConnection(receiverNet,
							receiverPort, localPort)) {
						return currentCommId;
					}
				}
			}

			/*
			 * Enable Piggybacking by encapsulating the message
			 */
			if (!piggybackServices.isEmpty()) {
				Message piggybacked = null;
				MessageWithPiggybackedData mPig = null;
				for (PiggybackMessageService pService : piggybackServices
						.values()) {
					piggybacked = pService.piggybackOnSendMessage(receiverNet,
							receiverPort, ProtocolAdapter.this);
					if (piggybacked != null) {
						if (mPig == null) {
							mPig = new MessageWithPiggybackedData(msg);
							msg = mPig;
						}
						mPig.addPiggybackedMessage(
								pService.getPiggybackServiceID(), piggybacked);
					}
				}
			}

			/*
			 * Send by dispatching it to the correct protocol
			 */
			transport.send(msg, receiverNet, receiverPort, localPort,
					currentCommId, false);
			return currentCommId;
		}

		@Override
		public int sendReply(Message reply, NetID receiver, int receiverPort,
				int commID) {
			transport.send(reply, receiver, receiverPort, localPort, commID,
					true);
			return commID;
		}

		@Override
		public int getLocalPort() {
			return localPort;
		}

		@Override
		public NetInterface getNetInterface() {
			return getHost().getNetworkComponent().getByNetId(localAddress);
		}

		@Override
		public TransInfo getTransInfo() {
			return DefaultTransInfo.getTransInfo(localAddress, localPort);
		}

		@Override
		public TransInfo getTransInfo(NetID net) {
			return DefaultTransInfo.getTransInfo(net, localPort);
		}

		@Override
		public TransInfo getTransInfo(NetID net, int port) {
			return DefaultTransInfo.getTransInfo(net, port);
		}

		@Override
		public void eventOccurred(Object content, int type) {
			/*
			 * Event for the Request-Reply scenario
			 */
			if (type == EVENT_SEND_AND_WAIT) {
				ReplyEvent event = (ReplyEvent) content;
				// this will check if the callback was already triggered
				event.failure();
				openCallbacks.remove(event.getCommId());
			} else {
				throw new AssertionError();
			}
		}

		@Override
		public int getHeaderSize() {
			return transport.getHeaderSize();
		}

	}

	protected void addTransMsgListener(TransMessageListener receiver, int port) {
		PortListener pl = portListeners.get(port);
		if (pl == null) {
			pl = new PortListener();
			portListeners.put(port, pl);
		}
		pl.addListener(receiver);
	}

	protected void removeTransMsgListener(TransMessageListener listener, int port) {
		PortListener pl = portListeners.get(port);
		pl.removeListener(listener);
		if (pl.isEmpty()) {
			portListeners.remove(port);
		}
	}

	@Override
	public void messageArrived(NetMessageEvent nme) {
		assert nme.getPayload() instanceof TransMessage;
		TransMessage msg = (TransMessage) nme.getPayload();

		TransInfo senderInfo = DefaultTransInfo.getTransInfo(nme.getSender(),
				msg.getSenderPort());

		/*
		 * If one or more Firewall-services are registered, check if the
		 * incoming connection is allowed.
		 */
		for (FirewallService firewall : firewallServices) {
			if (!firewall.allowIncomingConnection(senderInfo,
					msg.getReceiverPort())) {
				Monitor.log(FirewallService.class, Level.INFO,
						"Message %s blocked by Firewall.", msg);
				return;
			}
		}

		/*
		 * Notify the Protocol of a received Message
		 */
		TransMessage receivedMessage = null;
		Class<? extends MessageBasedTransport> protocol = null;
		if (msg.getProtocol() == TransProtocol.TCP) {
			receivedMessage = tcp.get(nme.getReceiver()).receive(msg,
					senderInfo);
			protocol = UDP.class;
		} else if (msg.getProtocol() == TransProtocol.UDP) {
			receivedMessage = udp.get(nme.getReceiver()).receive(msg,
					senderInfo);
			protocol = TCPMessageBased.class;
		} else {
			throw new AssertionError("No Protocol " + msg.getProtocol());
		}

		if (receivedMessage != null) {
			if (hasAnalyzer) {
				transportAnalyzerProxy.transMsgEvent(receivedMessage, host,
						Reason.RECEIVE);
			}

			/*
			 * Dispatching to the Overlays/Apps, if it is not a Protocol Message
			 */
			if (receivedMessage.isReply()) {
				ReplyEvent replyEvent = openCallbacks.get(receivedMessage
						.getCommId());
				if (replyEvent != null) {
					// success will ensure that the callback is only
					// triggered once

					if (receivedMessage.getPayload() instanceof MessageWithPiggybackedData) {
						MessageWithPiggybackedData pMsg = (MessageWithPiggybackedData) receivedMessage
								.getPayload();
						/*
						 * Deliver piggybacked messages
						 */
						for (Entry<Byte, Message> entry : pMsg.getPiggybacked()
								.entrySet()) {
							piggybackServices.get(entry.getKey())
									.onReceivedPiggybackedMessage(
											entry.getValue(), senderInfo);
						}
						// deliver real message
						replyEvent.success(pMsg.getPayload(), senderInfo);
					} else {
						replyEvent.success(receivedMessage.getPayload(),
								senderInfo);
					}
				}
				/*
				 * In the DefaultTransLayer messages are never dispatched to the
				 * "normal" message handler, if they are reply messages.
				 */
				return;
			}

			PortListener pl = portListeners.get(receivedMessage
					.getReceiverPort());
			if (pl != null) {

				if (receivedMessage.getPayload() instanceof MessageWithPiggybackedData) {
					MessageWithPiggybackedData pMsg = (MessageWithPiggybackedData) receivedMessage
							.getPayload();
					/*
					 * Deliver piggybacked messages, but only if we have a local
					 * listener. Discard otherwise.
					 */
					for (Entry<Byte, Message> entry : pMsg.getPiggybacked()
							.entrySet()) {
						PiggybackMessageService service = piggybackServices.get(entry.getKey());
						if (service != null) {
							service.onReceivedPiggybackedMessage(entry.getValue(), senderInfo);
						}
					}
					// deliver real message
					pl.dispatch(pMsg.getPayload(), protocol, senderInfo,
							receivedMessage.getCommId());
				} else {
					pl.dispatch(receivedMessage.getPayload(), protocol,
							senderInfo, receivedMessage.getCommId());
				}
			} else {
				Monitor.log(
						ModularTransLayer.class,
						Level.INFO,
						"%s - No TransListener is listening on node %s for message %s",
						Time.getFormattedTime(), host.getHostId(),
						receivedMessage);
			}

		}

	}

	/**
	 * A very simple data structure to allow multiple Listeners on one port (e.g., if TCP and UDP are 
	 * both instantiated on the same port)
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 07.05.2012
	 */
	public class PortListener {

		private List<TransMessageListener> transListeners;

		public PortListener() {
			transListeners = new LinkedList<TransMessageListener>();
		}

		public void addListener(TransMessageListener listener) {
			if (!transListeners.contains(listener)) {
				transListeners.add(listener);
			}
		}

		public void removeListener(TransMessageListener listener) {
			transListeners.remove(listener);
		}

		public <T extends MessageBasedTransport> void dispatch(Message msg,
				Class<T> protocol, TransInfo sender, int commId) {
			for (TransMessageListener listener : transListeners) {
				listener.messageArrived(msg, sender, commId);
			}
		}

		public boolean isEmpty() {
			return transListeners.isEmpty();
		}

	}

	/**
	 * Just a reply event for the sendAndWait-Scenario
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 08.05.2012
	 */
	public class ReplyEvent {

		private final TransMessageCallback callback;

		private final int commId;

		private boolean done = false;

		private final long sentTimestamp = Time.getCurrentTime();

		public ReplyEvent(TransMessageCallback callback, int commId) {
			this.callback = callback;
			this.commId = commId;
		}

		public int getCommId() {
			return commId;
		}

		public void success(Message msg, TransInfo senderInfo) {
			if (!done) {
				done = true;
				callback.receive(msg, senderInfo, commId);
				_avgSendAndWaitTime.newVal(Time.getCurrentTime()
						- sentTimestamp);
				_avgSendAndWaitSuccess.newVal(1);
			}
		}

		public void failure() {
			if (!done) {
				done = true;
				callback.messageTimeoutOccured(commId);
				_avgSendAndWaitSuccess.newVal(0);
			}
		}

	}

	/**
	 * Container to support {@link PiggybackMessageService}s
	 * 
	 * @author Bjoern
	 * @version 1.0, Jul 13, 2013
	 */
	public class MessageWithPiggybackedData implements Message {

		private final Message originalMessage;

		private final Map<Byte, Message> piggybacked = new LinkedHashMap<Byte, Message>();

		public MessageWithPiggybackedData(Message originalMessage) {
			this.originalMessage = originalMessage;
		}

		public void addPiggybackedMessage(byte serviceId, Message msg) {
			piggybacked.put(serviceId, msg);
		}

		public Map<Byte, Message> getPiggybacked() {
			return piggybacked;
		}

		@Override
		public long getSize() {
			long size = 0;
			for (Message msg : piggybacked.values()) {
				size += msg.getSize();
			}
			return originalMessage.getSize() + size;
		}

		@Override
		public Message getPayload() {
			return originalMessage;
		}

	}

}
