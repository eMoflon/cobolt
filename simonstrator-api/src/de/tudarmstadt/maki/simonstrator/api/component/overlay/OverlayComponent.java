/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
 *
 * This file is part of Simonstrator.KOM.
 * 
 * Simonstrator.KOM is free software: you can redistribute it and/or modify
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

package de.tudarmstadt.maki.simonstrator.api.component.overlay;

import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;

/**
 * An overlay-component
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public interface OverlayComponent extends HostComponent {

	/**
	 * The possible overlay states
	 */
	public enum PeerStatus {
		/**
		 * The peer is not connected to the overlay
		 */
		ABSENT,
		/**
		 * The peer is connected to the overlay
		 */
		PRESENT,
		/**
		 * The peer is about to join the overlay
		 */
		TO_JOIN
	}

	/**
	 * Is the node online and "connected" with the overlay? This method does not
	 * check for real connectivity, it just returns the state that the overlay
	 * assumes it is in.
	 * 
	 * @return <code>true</code> if the node thinks that it is connected with an
	 *         overlay, otherwise <code>false</code>.
	 */
	public boolean isPresent();

	/**
	 * The current peer status
	 * 
	 * @return
	 */
	public PeerStatus getPeerStatus();

	/**
	 * Returns the local overlay contact of the current component.
	 * 
	 * @return
	 */
	public OverlayContact getLocalOverlayContact();

	/**
	 * This listener is informed if the peer status changes
	 * 
	 * @param l
	 */
	public void addPeerStatusListener(IPeerStatusListener l);

	public void removePeerStatusListener(IPeerStatusListener l);

}
