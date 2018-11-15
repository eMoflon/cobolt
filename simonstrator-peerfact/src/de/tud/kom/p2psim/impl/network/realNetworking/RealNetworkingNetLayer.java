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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;

import de.tud.kom.p2psim.api.analyzer.MessageAnalyzer.Reason;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.network.BandwidthImpl;
import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.api.network.NetProtocol;
import de.tud.kom.p2psim.api.transport.TransProtocol;
import de.tud.kom.p2psim.impl.network.AbstractNetLayer;
import de.tud.kom.p2psim.impl.network.IPv4Message;
import de.tud.kom.p2psim.impl.network.realNetworking.RealNetworkRoutingTable.InetAddrAndPort;
import de.tud.kom.p2psim.impl.network.realNetworking.messages.RealNetworkingMessage;
import de.tud.kom.p2psim.impl.transport.AbstractTransMessage;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

public class RealNetworkingNetLayer extends AbstractNetLayer {

	
	private static RealNetworkingNetLayerManager netLayerManager = RealNetworkingNetLayerManager.getInstance();
	
	public RealNetworkingNetLayer(SimHost host, NetID netID) {
		super(host, netID, new BandwidthImpl(Long.MAX_VALUE, Long.MAX_VALUE),
				null, null);
		
		/* Go online as default behaviour. */
		this.goOnline();
	}

	/* (non-Javadoc)
	 * @see de.tud.kom.p2psim.api.network.NetLayer#send(de.tud.kom.p2psim.api.common.Message, de.tud.kom.p2psim.api.network.NetID, de.tud.kom.p2psim.api.network.NetProtocol)
	 */
	@Override
	public void send(Message transLayerMsg, NetID receiverId, NetProtocol protocol) {
		
		assert( transLayerMsg instanceof AbstractTransMessage );
		if( isOnline() ) {
			
			assert (isSupported(((AbstractTransMessage) transLayerMsg).getProtocol()));
			

			/* First: Get IP and PORT from Routing Table. */
			final RealNetworkRoutingTable routingTable = RealNetworkRoutingTable.getInstance();
			final InetAddrAndPort realDestination = routingTable.getRealInetAddrAndPortForVirtualIP(receiverId);
						
			/* Destination known at all? */
			if( realDestination == null ) {
				Monitor.log(RealNetworkingNetLayer.class, Level.DEBUG,
						"Destination " + receiverId + " not know to me ("
						+ this.getLocalInetAddress() + "). Dropping packet.");
				return;
			}
			
			final NetMessage netLayerMsg = new IPv4Message(transLayerMsg, receiverId, this.getNetID());
			final RealNetworkingMessage realNetworkMessage = new RealNetworkingMessage(netLayerMsg);
			
			final TransProtocol transLayerMsgProto = ((AbstractTransMessage) transLayerMsg).getProtocol();
			Monitor.log(RealNetworkingNetLayer.class, Level.DEBUG,
					"Trying to send packet from Virtual/" + this.getNetID()
							+ " to Virtual/" + receiverId + " via Real/"
							+ realDestination.getInetAddr() + " with "
							+ transLayerMsgProto.toString());
			
			if (transLayerMsgProto.equals(TransProtocol.UDP)) {
				if (hasAnalyzer) {
					netAnalyzerProxy.netMsgEvent(netLayerMsg, getHost(),
							Reason.SEND);
				}
				this.netLayerManager.sendUDPPacket(receiverId, realDestination, realNetworkMessage);

//			} else if (transLayerMsgProto.equals(TransProtocol.TCP)) {
//				Simulator.getMonitor().netMsgEvent(netLayerMsg, getHost(), Reason.SEND);
//				new Thread(this.new SenderThreadTCP(receiverId, realDestination, realNetworkMessage)).start();
			}
						
		} else {
			Monitor.log(RealNetworkingNetLayer.class, Level.WARN, "Host "
					+ this.getLocalInetAddress() + " is offline.");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tud.kom.p2psim.impl.network.AbstractNetLayer#goOnline()
	 */
	@Override
	public void goOnline() {

		if( this.isOnline() ) {
			return;
		}
		
		Monitor.log(RealNetworkingNetLayer.class, Level.DEBUG,
				"Host " + this.getLocalInetAddress() + " going online");
		netLayerManager.hostGoOnline(this);

		super.goOnline();
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tud.kom.p2psim.impl.network.AbstractNetLayer#goOffline()
	 */
	@Override
	public void goOffline() {

		Monitor.log(RealNetworkingNetLayer.class, Level.DEBUG,
				"Host " + this.getLocalInetAddress() + " going offline");
		netLayerManager.hostGoOffline(this);
		
		super.goOffline();

	}

	/* (non-Javadoc)
	 * @see de.tud.kom.p2psim.impl.network.AbstractNetLayer#isSupported(de.tud.kom.p2psim.api.transport.TransProtocol)
	 */
	@Override
	protected boolean isSupported(TransProtocol protocol) {
		return (protocol.equals(TransProtocol.UDP) || protocol.equals(TransProtocol.TCP));
	}

	
	/**
	 * The Class SenderThreadTCP.
	 */
	private class SenderThreadTCP implements Runnable {

		private NetMessage netLayerMsg;
		private NetID receiverId;
		private InetAddrAndPort realDestination;
		
		/**
		 * Instantiates a new sender thread tcp.
		 *
		 * @param receiverId the receiver id
		 * @param realDestination the real destination
		 * @param netLayerMsg the net layer msg
		 */
		public SenderThreadTCP(NetID receiverId, InetAddrAndPort realDestination, NetMessage netLayerMsg) {
			this.receiverId = receiverId;
			this.realDestination = realDestination;
			this.netLayerMsg = netLayerMsg;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {			
			try {
									
				final Socket tcpConnection = new Socket( realDestination.getInetAddr(),  realDestination.getPort() );
			    
			    final OutputStream oStream = tcpConnection.getOutputStream();
			    final ObjectOutputStream ooStream = new ObjectOutputStream(oStream);
			    
			    ooStream.writeObject(netLayerMsg);
			    
			    ooStream.close();
			    oStream.close();
				tcpConnection.close();
				
			} catch( ConnectException e ) {
				Monitor.log(
						RealNetworkingNetLayer.class,
						Level.ERROR,
						"Remote host not answering, dropping packet. Was supposed to go to Virtual/"
								+ receiverId + " on Real/"
								+ realDestination.getInetAddr() + ": "
								+ e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}


