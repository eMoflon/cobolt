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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import de.tud.kom.p2psim.impl.network.IPv4Message;
import de.tud.kom.p2psim.impl.network.realNetworking.RealNetworkingNetLayerManager;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;

/**
 * The listener interface for receiving tcp events.
 * The class that is interested in processing a tcp
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addTcpListener<code> method. When
 * the tcp event occurs, that object's appropriate
 * method is invoked.
 *
 * @see TcpEvent
 */
public class RealNetworkingTcpListener implements Runnable {

	
	/** The socket to listen on. */
	private ServerSocket socket;
	
	/** The assosicated netLayer. */
	private RealNetworkingNetLayerManager netLayerManager;
	
	/** Is thread still supposed to be running. */
	private boolean running = false;

	/** Indicate weather thread is alive already. */
	private boolean alive = false;


	/**
	 * Instantiates a new real networking tcp listener.
	 *
	 * @param tcpSocket the tcp socket
	 * @param realNetworkingNetLayerManager the real networking net layer manager
	 */
	public RealNetworkingTcpListener(ServerSocket tcpSocket, RealNetworkingNetLayerManager realNetworkingNetLayerManager) {
		this.socket = tcpSocket;
		this.netLayerManager = realNetworkingNetLayerManager;
		this.running = true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		
		if( running ) {
			this.alive = true;
		}
		
		while( running ) {
			
			try {				
				Socket currentSocket = socket.accept();
				
				/* 
				 * 
				 * Block HERE till data packet is received. 
				 * 
				 * 
				 * */
				
				/* *Yeay* Got a msg! */
				Monitor.log(
						RealNetworkingTcpListener.class,
						Level.DEBUG,
						"Got a connection from: "
								+ currentSocket.getRemoteSocketAddress()
								+ " to: " + currentSocket.getLocalAddress()
								+ ":" + currentSocket.getLocalPort());
								
				Object inputObject = null;
				try {
					
					InputStream iStream = currentSocket.getInputStream();                
					ObjectInputStream oiStream = new ObjectInputStream(iStream);
					
					inputObject = oiStream.readObject();
					
					oiStream.close();
					iStream.close();
					currentSocket.close();
					
					
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                
				assert( inputObject != null );
				assert( inputObject instanceof IPv4Message );
				final IPv4Message netMsg = (IPv4Message) inputObject;
				
				/* Deliver msg to simulator. */
				netLayerManager.receiveThreadSafe(netMsg);
				
			} catch (SocketException e) {
				/* Nothing. Socket was closed, thread will end now. */
			} catch (IOException e) {
				Monitor.log(RealNetworkingTcpListener.class, Level.WARN,
						"Socket IOException: " + e.getMessage());
			} catch (AssertionError e) {
				Monitor.log(RealNetworkingTcpListener.class, Level.WARN,
						"Assertion: " + e.getMessage());
			}
			
		}

	}		
	
	/**
	 * Stop.
	 */
	public void stop() {
		Monitor.log(RealNetworkingTcpListener.class, Level.DEBUG,
				"TcpListener thread stopping.");
		running = false;
	}
	
	/**
	 * Checks if this thread is alive.
	 *
	 * @return true, if is alive
	 */
	public boolean isAlive() {
		return alive;
	}
	
}
