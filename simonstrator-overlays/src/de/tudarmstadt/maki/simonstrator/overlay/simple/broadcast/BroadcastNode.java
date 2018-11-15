package de.tudarmstadt.maki.simonstrator.overlay.simple.broadcast;

import java.util.LinkedHashMap;
import java.util.Map;

import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.common.UniqueID;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetInterface;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetworkComponent.NetInterfaceName;
import de.tudarmstadt.maki.simonstrator.api.component.overlay.OverlayContact;
import de.tudarmstadt.maki.simonstrator.api.component.transport.ProtocolNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransInfo;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransMessageCallback;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransMessageListener;
import de.tudarmstadt.maki.simonstrator.api.component.transport.protocol.TCPMessageBased;
import de.tudarmstadt.maki.simonstrator.api.component.transport.protocol.UDP;
import de.tudarmstadt.maki.simonstrator.application.chat.ChatListener;
import de.tudarmstadt.maki.simonstrator.application.chat.ChatNode;
import de.tudarmstadt.maki.simonstrator.overlay.AbstractOverlayMessage;
import de.tudarmstadt.maki.simonstrator.overlay.AbstractOverlayNode;
import de.tudarmstadt.maki.simonstrator.overlay.BasicOverlayContact;

/**
 * A simple overlay for debugging of the Simonstrator-API
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public class BroadcastNode extends AbstractOverlayNode implements TransMessageListener, ChatNode {

	private UDP udp;

	private TCPMessageBased tcp;

	private OverlayContact ownOverlayContact;

	private final int PORT_UDP;

	private final int PORT_TCP;

	private static final boolean USE_TCP_FOR_UNICAST = true;

	private NetInterfaceName netName;

	private Map<UniqueID, OverlayContact> knownContacts = new LinkedHashMap<UniqueID, OverlayContact>();

	protected BroadcastNode(Host host, int port) {
		super(host);
		this.PORT_UDP = port;
		this.PORT_TCP = port;
	}

	@Override
	public OverlayContact getLocalOverlayContact() {
		return ownOverlayContact;
	}

	@Override
	public void initialize() {
		super.initialize();
		/*
		 * Bind the first Network Interface
		 */
		NetInterface net = getHost().getNetworkComponent().getNetworkInterfaces().iterator().next();
		netName = net.getName();
		try {
			udp = getAndBindUDP(net.getLocalInetAddress(), PORT_UDP, null);
			if (USE_TCP_FOR_UNICAST) {
				tcp = getAndBindTCP(net.getLocalInetAddress(), PORT_TCP, null);
			}
		} catch (ProtocolNotAvailableException e) {
			throw new AssertionError("Transport component not available!");
		}
		ownOverlayContact = new BasicOverlayContact(getHost().getId(), netName, net.getLocalInetAddress(), PORT_UDP);
		udp.setTransportMessageListener(this);
		if (USE_TCP_FOR_UNICAST) {
			tcp.setTransportMessageListener(this);
		}
	}

	@Override
	public void messageArrived(Message msg, TransInfo sender, int commID) {
		if (msg instanceof BroadcastMessage) {
			BroadcastMessage bMsg = (BroadcastMessage) msg;
			switch (bMsg.getType()) {
			case BroadcastMessage.TYPE_TEST:
				System.out.println("Message Arrived: " + msg.toString() + " from " + sender.toString());
				break;
			case BroadcastMessage.TYPE_CHAT:
				chatListener.receivedMessage(bMsg.getMessage(), bMsg.getSender().getNodeID());
				knownContacts.put(bMsg.getSender().getNodeID(), bMsg.getSender());
				break;
			default:
				throw new AssertionError("Unknown Type");
			}
		} else if (msg instanceof UnicastMessage) {
			UnicastMessage uMsg = (UnicastMessage) msg;
			chatListener.receivedMessage(uMsg.getMessage(), uMsg.getSender().getNodeID());
			knownContacts.put(uMsg.getSender().getNodeID(), uMsg.getSender());
			// send ACK
			if (!USE_TCP_FOR_UNICAST) {
				AckMessage ack = new AckMessage(getLocalOverlayContact(), uMsg.getSender());
				udp.sendReply(ack, sender.getNetId(), sender.getPort(), commID);
			}
		}
	}

	@Override
	public void wentOnline(Host host, NetInterface netInterface) {
		//
	}

	@Override
	public void wentOffline(Host host, NetInterface netInterface) {
		//
	}

	private ChatListener chatListener;

	/**
	 * 
	 * @param message
	 *            Application-layer chat message
	 * @param to
	 *            if null, the message will be broadcasted
	 */
	public void sendChatMessage(String message, UniqueID to) {
		if (to == null || !knownContacts.containsKey(to)) {
			BroadcastMessage bMsg = new BroadcastMessage(getLocalOverlayContact(), message, BroadcastMessage.TYPE_CHAT);
			udp.send(bMsg, udp.getNetInterface().getBroadcastAddress(), PORT_UDP);
		} else {
			UnicastMessage bMsg = new UnicastMessage(getLocalOverlayContact(), knownContacts.get(to), message);

			// TCP-variant (no send-and-wait needed)
			if (USE_TCP_FOR_UNICAST) {
				tcp.send(bMsg, knownContacts.get(to).getNetID(netName), knownContacts.get(to).getPort(netName));
			} else {
				udp.sendAndWait(bMsg, knownContacts.get(to).getNetID(netName), knownContacts.get(to).getPort(netName),
						new TransMessageCallback() {
							@Override
							public void receive(Message reply, TransInfo source, int commId) {
								System.out.println("Received ACK! " + reply.toString());
							}

							@Override
							public void messageTimeoutOccured(int commId) {
								System.err.println("Timeout occured!");
							}
						}, 2 * Time.SECOND);
			}
		}
	}

	/**
	 * 
	 * @param chatListener
	 */
	public void setChatListener(ChatListener chatListener) {
		this.chatListener = chatListener;
	}

	/**
	 * Start a periodic Broadcast
	 */
	public void startPeriodicBroadcast() {
		doBroadcastAndReschedule(5 * Time.SECOND);
	}

	protected void doBroadcastAndReschedule(final long after) {
		Event.scheduleWithDelay(after, new EventHandler() {

			@Override
			public void eventOccurred(Object content, int type) {
				doBroadcastAndReschedule(after);
			}
		}, null, 0);
		System.out.println("BroadcastNode: Sending Broadcast from " + udp.getNetInterface().getLocalInetAddress());
		udp.send(new BroadcastMessage(getLocalOverlayContact(), "", BroadcastMessage.TYPE_TEST),
				udp.getNetInterface().getBroadcastAddress(), PORT_UDP);
	}

	/**
	 * 
	 * @author Bjoern Richerzhagen
	 * 
	 */
	public static class BroadcastMessage extends AbstractOverlayMessage {

		private static final long serialVersionUID = 1L;

		public final static byte TYPE_CHAT = 1;

		public final static byte TYPE_TEST = 2;

		private byte type;

		private String message;

		@SuppressWarnings("unused")
		private BroadcastMessage() {
			// For Kryo
		}

		public BroadcastMessage(OverlayContact sender, String payload, byte type) {
			super(sender, null);
			this.message = payload;
			this.type = type;
		}

		@Override
		public Message getPayload() {
			return null;
		}

		public String getMessage() {
			return message;
		}

		public byte getType() {
			return type;
		}

		@Override
		public String toString() {
			return "BC type:" + type;
		}

	}

	public static class UnicastMessage extends AbstractOverlayMessage {

		private static final long serialVersionUID = 1L;

		private final String message;

		public UnicastMessage(OverlayContact sender, OverlayContact receiver, String message) {
			super(sender, receiver);
			this.message = message;
		}

		@Override
		public Message getPayload() {
			return null;
		}

		public String getMessage() {
			return message;
		}

	}

	public static class AckMessage extends AbstractOverlayMessage {

		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unused")
		private AckMessage() {
			// For Kryo
		}

		public AckMessage(OverlayContact sender, OverlayContact receiver) {
			super(sender, receiver);
		}

		@Override
		public Message getPayload() {
			return null;
		}

	}

}
