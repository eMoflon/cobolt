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

package de.tud.kom.p2psim.impl.network.realNetworking;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.tud.kom.p2psim.api.simengine.SimulatorObserver;
import de.tud.kom.p2psim.impl.network.IPv4Message;
import de.tud.kom.p2psim.impl.network.IPv4NetID;
import de.tud.kom.p2psim.impl.network.realNetworking.RealNetworkRoutingTable.InetAddrAndPort;
import de.tud.kom.p2psim.impl.network.realNetworking.messages.RealNetworkingMessage;
import de.tud.kom.p2psim.impl.network.realNetworking.socketListener.RealNetworkingTcpListener;
import de.tud.kom.p2psim.impl.network.realNetworking.socketListener.RealNetworkingUdpListener;
import de.tud.kom.p2psim.impl.simengine.Scheduler;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

public class RealNetworkingNetLayerManager implements EventHandler {
	
	
	public static final int GLOBAL_SOCKET_PORT_TCP = 13000;
	public static final int GLOBAL_SOCKET_PORT_UDP = 13001;
	
	private static RealNetworkingNetLayerManager instance;

	private ServerSocket tcpSocket;
	private DatagramSocket udpSocket;

	private RealNetworkingTcpListener listenerThreadTCP;
	private RealNetworkingUdpListener listenerThreadUDP;
	
	private SenderThreadUDP senderThreadUDP;
	private ConcurrentLinkedQueue<UDPTransmission> udpTransmissionQueue;
	
	private HashMap<NetID, RealNetworkingNetLayer> hostsAlive;

	private final static int EVENT_DUMMY = 1;

	private final static int EVENT_RECEIVE = 2;


	/**
	 * Instantiates a new real networking net layer manager.
	 */
	private RealNetworkingNetLayerManager() {
		
		hostsAlive = new HashMap<NetID, RealNetworkingNetLayer>();
		udpTransmissionQueue = new ConcurrentLinkedQueue<UDPTransmission>();
		
		/* Go Offline once Simulation is done. */
		Simulator.getInstance().addObserver(new SimulatorObserver() {
			
			@Override
			public void simulationFinished() {
				RealNetworkingNetLayerManager.getInstance().goOffline();
				
			}
		});
		
		/* Schedule starting message, so simulator doesn't run away imediately. */
		Event.scheduleImmediately(this, null, EVENT_DUMMY);
		
	}

	/**
	 * Gets the single instance of RealNetworkingNetLayerManager.
	 *
	 * @return single instance of RealNetworkingNetLayerManager
	 */
	public static RealNetworkingNetLayerManager getInstance() {

		if (null == instance) {
			instance = new RealNetworkingNetLayerManager();
		}

		return instance;

	}
	
	/**
	 * Go online globally.
	 */
	void goOnline() {
		
		/* Create and open up socket. */
		try {
					
			/* Start up TCP! */
			Monitor.log(RealNetworkingNetLayerManager.class, Level.DEBUG,
					"Starting TCP listener on port " + GLOBAL_SOCKET_PORT_TCP);
			this.tcpSocket = new ServerSocket(GLOBAL_SOCKET_PORT_TCP);
			assert this.tcpSocket.isBound();
			listenerThreadTCP = new RealNetworkingTcpListener(this.tcpSocket, this);
			new Thread(listenerThreadTCP).start();
			
			/* Start up UDP! */
			Monitor.log(RealNetworkingNetLayerManager.class, Level.DEBUG,
					"Starting UDP listener on port " + GLOBAL_SOCKET_PORT_UDP);
			this.udpSocket = new DatagramSocket(GLOBAL_SOCKET_PORT_UDP);
			listenerThreadUDP = new RealNetworkingUdpListener(this.udpSocket, this);
			new Thread(listenerThreadUDP).start();
			senderThreadUDP = new SenderThreadUDP(this.udpTransmissionQueue, this.udpSocket);
			new Thread(senderThreadUDP).start();
			
			/* 
			 * Block till both threads are alive. 
			 */
			while( !( listenerThreadTCP.isAlive() && listenerThreadUDP.isAlive()) ) {
				try {
					Thread.sleep(2);
				} catch (InterruptedException e) {
					// None.
				}	
			}
				
			
		} catch (BindException e) {
			Monitor.log(RealNetworkingNetLayerManager.class, Level.ERROR,
					"Socket already in use. Second simulator running in background?");
			System.exit(-1);
		} catch (IOException e) {
			Monitor.log(RealNetworkingNetLayerManager.class, Level.ERROR,
					"Socket already in use. Second simulator running in background?");
			System.exit(-1);
		}
		
	}
	
	/**
	 * Go offline.
	 */
	public void goOffline() {
		
		if( listenerThreadTCP != null ) {
			listenerThreadTCP.stop();
		}

		if( listenerThreadUDP != null ) {
			listenerThreadUDP.stop();	
		}
		
		if( senderThreadUDP != null ) {
			senderThreadUDP.stop();
		}
		
		try {
			if (tcpSocket != null) { this.tcpSocket.close();  }
			if (udpSocket != null) { this.udpSocket.close();  }
		} catch (IOException e) {
			Monitor.log(RealNetworkingNetLayerManager.class, Level.ERROR,
					"Could not close socket on TCP/UDP port "
							+ GLOBAL_SOCKET_PORT_TCP + "/"
							+ GLOBAL_SOCKET_PORT_UDP);
		}
		
		this.tcpSocket = null;
		this.udpSocket = null;
		
		
	}

	/**
	 * Host go online.
	 *
	 * @param realNetworkingNetLayer the real networking net layer
	 */
	public void hostGoOnline(RealNetworkingNetLayer realNetworkingNetLayer) {
		hostsAlive.put(realNetworkingNetLayer.getNetID(), realNetworkingNetLayer);
	}

	/**
	 * Host go offline.
	 *
	 * @param realNetworkingNetLayer the real networking net layer
	 */
	public void hostGoOffline(RealNetworkingNetLayer realNetworkingNetLayer) {
		hostsAlive.remove(realNetworkingNetLayer.getNetID());		
	}

	/**
	 * Gets the UDP socket.
	 *
	 * @return the UDP socket
	 */
	public DatagramSocket getUDPSocket() {
		return udpSocket;
	}

	/**
	 * Receive thread safe.
	 * 	Has to be synchronized as TCP and UDP listener threads might call
	 * 	at the same time.
	 *
	 * @param netMsg the net msg
	 */
	synchronized public void receiveThreadSafe(IPv4Message netMsg) {

		/* Call once to update currentTime, so logs are correct. */
		if (Simulator.getScheduler().getRealTime()) {
			
			/* Wake scheduler up in case it's sleeping. */
			final Scheduler scheduler = Simulator.getScheduler();
			synchronized (scheduler) {
				/*
				 * FIXME: check, if this works thread-safe...
				 */
				// scheduler.wakeUpThreadSafeAndInsertEventImmediately(netMsg,
				// this, SimulationEvent.Type.MESSAGE_RECEIVED);
				Event.scheduleImmediately(this, netMsg, EVENT_RECEIVE);
			}
			
		} else {
			assert false: "Scheduler not set to realtime. NOT SUPPORTED.";
		}
				
	}

	@Override
	public void eventOccurred(Object content, int type) {
		if (type == EVENT_RECEIVE) {
			final IPv4Message msg = (IPv4Message) content;
			final NetID recipient = msg.getReceiver();

			/* Boardcast: Deliver msg to all hosts. */
			if (recipient.equals(IPv4NetID.LOCAL_BROADCAST)) {
				for (RealNetworkingNetLayer receiver : hostsAlive.values()) {
					if (!receiver.getNetID().equals(msg.getSender())) {
						receiver.receive(msg);
					}
				}
				/* Else: Unicast. */
			} else {
				final RealNetworkingNetLayer receivingNetLayer = hostsAlive
						.get(recipient);
				if (receivingNetLayer != null) {
					/* if == null, host is probably offline. */
					receivingNetLayer.receive(msg);
				}
			}
		}
	}

	/**
	 * Send udp packet.
	 *
	 * @param receiverId the receiver id
	 * @param realDestination the real destination
	 * @param realNetworkMessage the net layer msg
	 */
	public void sendUDPPacket(NetID receiverId, InetAddrAndPort realDestination, RealNetworkingMessage realNetworkMessage) {
		udpTransmissionQueue.add(new UDPTransmission(receiverId, realDestination, realNetworkMessage));
		synchronized(senderThreadUDP) {
			senderThreadUDP.notifyAll();			
		}
	}
	
	/**
	 * The Class UDPTransmission.
	 */
	private class UDPTransmission {
		private RealNetworkingMessage realNetworkMessage;
		NetID receiverId;
		
		/** The real destination. */
		InetAddrAndPort realDestination;

		/**
		 * Instantiates a new UDP transmission data set.
		 *
		 * @param receiverId the receiver id
		 * @param realDestination the real destination
		 * @param netLayerMsg the net layer msg
		 */
		public UDPTransmission(NetID receiverId, InetAddrAndPort realDestination, RealNetworkingMessage realNetworkMessage) {
			this.receiverId = receiverId;
			this.realDestination = realDestination;
			this.realNetworkMessage = realNetworkMessage;
		}

		/**
		 * Gets the real network message.
		 *
		 * @return the real network message
		 */
		public RealNetworkingMessage getRealNetworkMessage() {
			return realNetworkMessage;
		}
	}
	
	/**
	 * The Class SenderThreadUDP.
	 */
	private class SenderThreadUDP implements Runnable {

		private ConcurrentLinkedQueue<UDPTransmission> udpTransmissionQueue;
		private boolean running = true;
		private DatagramSocket udpSocket;

		public SenderThreadUDP(ConcurrentLinkedQueue<UDPTransmission> udpTransmissionQueue, DatagramSocket udpSocket) {
			this.udpTransmissionQueue = udpTransmissionQueue;
			this.udpSocket = udpSocket;
		}

		/**
		 * Stop.
		 */
		public void stop() {
			running  = false;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			
			while( running ) {
				
				while(this.udpTransmissionQueue.isEmpty()) {
					try {
						synchronized (this) {
							this.wait();					
						}
					} catch (InterruptedException e) {
						// None. Thanks.
					}
				}
				
				UDPTransmission nextUp = this.udpTransmissionQueue.poll();
				try {

					final ByteArrayOutputStream baos = new ByteArrayOutputStream();
					final ObjectOutputStream oos = new ObjectOutputStream(baos);

					oos.writeObject(nextUp.getRealNetworkMessage());
					oos.flush();
					
					final byte[] buf= baos.toByteArray();
					if( buf.length > 64000 ) {
						Monitor.log(RealNetworkingNetLayerManager.class,
								Level.WARN, "UDP message too long("
										+ buf.length
										+ " bytes), could not be sent to "
										+ nextUp.receiverId + ". Dropping.");
						return;
					}

					
					final DatagramPacket packet = new DatagramPacket(buf, buf.length, nextUp.realDestination.getInetAddr(), RealNetworkingNetLayerManager.GLOBAL_SOCKET_PORT_UDP);
					this.udpSocket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}				
			}
		}
	}
}
