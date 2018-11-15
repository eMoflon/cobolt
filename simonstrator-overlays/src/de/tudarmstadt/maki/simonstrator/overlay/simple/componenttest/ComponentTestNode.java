package de.tudarmstadt.maki.simonstrator.overlay.simple.componenttest;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetInterface;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetworkComponent.NetInterfaceName;
import de.tudarmstadt.maki.simonstrator.api.component.overlay.OverlayContact;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.LocationListener;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.LocationRequest;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.LocationSensor;
import de.tudarmstadt.maki.simonstrator.api.component.transport.ProtocolNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.transport.ServiceNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransInfo;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransMessageListener;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransportProtocol;
import de.tudarmstadt.maki.simonstrator.api.component.transport.protocol.UDP;
import de.tudarmstadt.maki.simonstrator.api.component.transport.service.FirewallService;
import de.tudarmstadt.maki.simonstrator.api.component.transport.service.PiggybackMessageService;
import de.tudarmstadt.maki.simonstrator.overlay.AbstractOverlayNode;

/**
 * This node performs a range of functional tests on host components.
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public class ComponentTestNode extends AbstractOverlayNode {

	private final Random rnd;

	private final int PORT = 1337;

	public ComponentTestNode(Host host) {
		super(host);
		rnd = Randoms.getRandom(ComponentTestNode.class);
	}

	@Override
	public OverlayContact getLocalOverlayContact() {
		return null;
	}

	@Override
	public void initialize() {
		super.initialize();
	}

	/**
	 * Triggered through action files
	 */
	public void doPerformTests() {
		System.out.println("Starting component tests...");
		Event.scheduleWithDelay(1 * Time.MINUTE, new EventHandler() {
			@Override
			public void eventOccurred(Object content, int type) {
				doLocationTests();
			}
		}, null, 0);
		Event.scheduleWithDelay(20 * Time.MINUTE, new EventHandler() {
			@Override
			public void eventOccurred(Object content, int type) {
				doTransportTests();
			}
		}, null, 0);
	}

	private void doTransportTests() {
		System.out.println("\n\n========================================");
		System.out
				.println("==== Testing the transport layer and services... ====");
		System.out.println("========================================");

		System.out.println("Binding WIFI...");
		NetInterface net = getHost().getNetworkComponent().getByName(
				NetInterfaceName.WIFI);
		UDP udp = null;
		try {
			udp = getAndBindUDP(net.getLocalInetAddress(), PORT, null);
			getHost().getTransportComponent().registerService(
					FirewallService.class, new FirewallServiceTest());
			getHost().getTransportComponent().registerService(
					PiggybackMessageService.class, new PiggybackingTest());
			udp.setTransportMessageListener(new TransportListenerTest());
			
			udp.send(new TestMessage(), udp.getNetInterface()
					.getBroadcastAddress(), PORT);

		} catch (ProtocolNotAvailableException e) {
			System.err.println("Protocol binding failed...");
		} catch (ServiceNotAvailableException e) {
			System.err.println("Service registration failed...");
		}

		System.out.println("Binding ETHERNET...");
		NetInterface net2 = getHost().getNetworkComponent().getByName(
				NetInterfaceName.ETHERNET);
		UDP udp2 = null;
		try {
			udp2 = getAndBindUDP(net2.getLocalInetAddress(), PORT, null);
			udp2.setTransportMessageListener(new TransportListenerTest());
			udp2.send(new TestMessage(), udp2.getNetInterface()
					.getBroadcastAddress(), PORT);

		} catch (ProtocolNotAvailableException e) {
			System.err.println("Protocol binding failed...");
		}

	}

	private void doLocationTests() {
		System.out.println("\n\n========================================");
		System.out.println("==== Testing the location sensor... ====");
		System.out.println("========================================");
		try {
			final LocationSensor loc = getHost().getComponent(
					LocationSensor.class);
			LocationRequest req = loc.getLocationRequest();
			req.setInterval(10 * Time.SECOND);
			req.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
			final LocationListener locL = new LocationListener() {

				@Override
				public void onLocationChanged(Host host, Location location) {
					System.out
							.println("Got a location update - new position is: "
									+ location.toString());
				}
			};
			// start requesting updates
			loc.requestLocationUpdates(req, locL);

			// un-register location updates after a given time
			Event.scheduleWithDelay(45 * Time.SECOND, new EventHandler() {

				@Override
				public void eventOccurred(Object content, int type) {
					System.out
							.println("Removing location update request from the LocationSensor Component");
					loc.removeLocationUpdates(locL);
				}
			}, null, 0);

			// start a one-time request
			System.out
					.println("One time request on the LocationSensor Component");
			Location lastlocation = loc.getLastLocation();
			System.out.println("Last known location is " + lastlocation);
		} catch (ComponentNotAvailableException e) {
			System.out.println("No LocationSensor Component found.");
		}
	}

	@Override
	public void wentOnline(Host host, NetInterface netInterface) {
		// TODO Auto-generated method stub

	}

	@Override
	public void wentOffline(Host host, NetInterface netInterface) {
		// TODO Auto-generated method stub

	}

	private class PiggybackingTest implements PiggybackMessageService {

		@Override
		public Class<?>[] getSerializableTypes() {
			return new Class<?>[] { PiggybackTestMessage.class };
		}

		@Override
		public void serialize(OutputStream out, Message msg) {
			// TODO Auto-generated method stub

		}

		@Override
		public Message create(InputStream in) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public byte getPiggybackServiceID() {
			return 126;
		}

		@Override
		public Message piggybackOnSendMessage(NetID to, int receiverPort,
				TransportProtocol protocol) {
			System.out.println("Piggyback - sending message to "
					+ to.toString() + " on port " + receiverPort);
			return new PiggybackTestMessage((short) rnd.nextInt());
		}

		@Override
		public void onReceivedPiggybackedMessage(Message msg, TransInfo sender) {
			System.out.println("Received a piggybacked Message: "
					+ msg.toString() + " from " + sender.toString());
		}

	}

	private class PiggybackTestMessage implements Message {

		private static final long serialVersionUID = 1L;

		private final short test;

		public PiggybackTestMessage(short test) {
			this.test = test;
		}

		@Override
		public long getSize() {
			return 2;
		}

		@Override
		public Message getPayload() {
			return null;
		}

		@Override
		public String toString() {
			return "PiggybackTestMessage[" + test + "]";
		}

	}

	private class TestMessage implements Message {

		private static final long serialVersionUID = 1L;

		@Override
		public long getSize() {
			return 0;
		}

		@Override
		public Message getPayload() {
			return null;
		}

		@Override
		public String toString() {
			return "TestMessage";
		}

	}

	private class FirewallServiceTest implements FirewallService {

		@Override
		public boolean allowIncomingConnection(TransInfo from, int onPort) {
			System.out.println("Firewall: incoming connection from "
					+ from.toString() + " on port " + onPort);
			return true;
		}

		@Override
		public boolean allowOutgoingConnection(NetID to, int toPort, int onPort) {
			System.out.println("Firewall: outgoing connection to "
					+ to.toString() + "[" + toPort + "] on port " + onPort);
			return true;
		}

	}

	private class TransportListenerTest implements TransMessageListener {

		@Override
		public void messageArrived(Message msg, TransInfo sender, int commID) {
			System.out.println("Message arrived: " + msg.toString() + " from "
					+ sender.toString());
		}

	}

}
