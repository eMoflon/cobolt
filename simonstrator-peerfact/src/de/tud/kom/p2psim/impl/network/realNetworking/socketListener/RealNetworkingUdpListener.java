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

package de.tud.kom.p2psim.impl.network.realNetworking.socketListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import de.tud.kom.p2psim.impl.network.IPv4Message;
import de.tud.kom.p2psim.impl.network.realNetworking.RealNetworkingNetLayerManager;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;

/**
 * The listener interface for receiving udp events.
 * The class that is interested in processing a udp
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addUdpListener<code> method. When
 * the udp event occurs, that object's appropriate
 * method is invoked.
 *
 * @see UdpEvent
 */
public class RealNetworkingUdpListener implements Runnable {
	
	/** The socket to listen on. */
	private DatagramSocket socket;
	
	/** The assosicated netLayer. */
	private RealNetworkingNetLayerManager netLayerManager;
	
	/** Is thread still supposed to be running. */
	private boolean running = false;
	
	/** Indicate weather thread is alive already. */
	private boolean alive = false;

	/**
	 * Instantiates a new UDP listener.
	 *
	 * @param udpSocket the udp socket
	 * @param realNetworkingNetLayerManager the real networking net layer
	 */
	public RealNetworkingUdpListener(DatagramSocket udpSocket, RealNetworkingNetLayerManager realNetworkingNetLayerManager) {
		this.socket = udpSocket;
		this.netLayerManager = realNetworkingNetLayerManager;
		this.running = true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		
		if( running ) {
			Monitor.log(RealNetworkingUdpListener.class, Level.DEBUG,
					"UdpListener thread running.");
			this.alive = true;
		}
		
		while( running ) {
			
			try {
				byte[] buf = new byte[65507];
				DatagramPacket p = new DatagramPacket(buf, buf.length);					
				socket.receive(p);
				
				/* 
				 * 
				 * Block HERE till data packet is received. 
				 * 
				 * 
				 * */
				
				/* *Yeay* Got a msg! */
							
				
				/* Unserialize. */
				Object inputObject = null;
				try {
					
					ByteArrayInputStream baos = new ByteArrayInputStream(buf);
					ObjectInputStream oiStream = new ObjectInputStream(baos);
					
					inputObject = oiStream.readObject();
					
					oiStream.close();
					baos.close();
					
				} catch (ClassNotFoundException e) {
					// None. 
				}
				

				assert( inputObject != null );
				assert( inputObject instanceof IPv4Message );
				IPv4Message inputMessage = (IPv4Message) inputObject;
				
				/* Deliver msg to simulator. */
				netLayerManager.receiveThreadSafe(inputMessage);				
				
			} catch (SocketException e) {
				/* Nothing. Socket was closed, thread will end now. */
			} catch (IOException e) {
				Monitor.log(RealNetworkingUdpListener.class, Level.WARN,
						"Socket IOException: " + e.getMessage());
			} catch (AssertionError e) {
				Monitor.log(RealNetworkingUdpListener.class, Level.WARN,
						"Assertion: " + e.getMessage());
			}
			
		}

	}		
	
	/**
	 * Stop.
	 */
	public void stop() {
		Monitor.log(RealNetworkingUdpListener.class, Level.DEBUG,
				"UdpListener thread stopping.");
		running = false;
	}
	
	/**
	 * Checks if this thread is alive.
	 *
	 * @return true, if is alive
	 */
	public boolean isAlive() {
		return alive ;
	}
	
}
