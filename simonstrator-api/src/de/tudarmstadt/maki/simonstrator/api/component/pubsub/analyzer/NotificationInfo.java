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

package de.tudarmstadt.maki.simonstrator.api.component.pubsub.analyzer;

/**
 * An object that can be attached to a notification to enable easier analyzing
 * in simulation mode.
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public interface NotificationInfo {

	/**
	 * In simulations, the size of a notification is calculated using this
	 * object - this is preferred over the creation of fake data (byte arrays),
	 * which would consume significant memory, especially if sizes are not fixed
	 * to one value.
	 * 
	 * Please note: this is added upon the sizes calculated from the included
	 * attributes / topics. It should therefore just return the payload size
	 * that would otherwise be determined by the byte[] payload.
	 * 
	 * @return
	 */
	public long getNotificationPayloadSize();

	/**
	 * host-ID of the originator of this notification
	 * 
	 * @return
	 */
	public long getOriginatorHostId();

	/**
	 * Time this notification was created (!) by the overlay. If it is not
	 * immediately published by the app, this can not be used to calculate
	 * delays.
	 * 
	 * @return
	 */
	public long getTimestampOfCreation();

	/**
	 * A sequence number for notifications, this can be used to detect missing
	 * notifications. Only to be used for analyzing.
	 * 
	 * @return
	 */
	public long getSequenceNumber();

	/**
	 * Generic marker interface: sets a boolean flag for the given marker
	 * number. Useful for analyzing, where the marker can for example correspond
	 * to a host-ID.
	 * 
	 * @param markerClass
	 * @param marker
	 */
	public void setMarker(Class<?> markerClass, long marker);

	/**
	 * Returns true, if the marker with the given number is set.
	 * 
	 * @param markerClass
	 * @param marker
	 * @return
	 */
	public boolean hasMarker(Class<?> markerClass, long marker);

}
