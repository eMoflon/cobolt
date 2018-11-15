/*
 * Copyright (c) 2005-2011 KOM - Multimedia Communications Lab
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


/**
 * 
 */
package de.tud.kom.p2psim.impl.util.timeoutcollections;

/**
 * Listens to element timeouts of timeout sets. For efficiency reasons, an event is
 * fired only when the set is cleaned up, not in real time. This is done after every
 * cleanup process in the timeout set. So the firing of the timeout event may occur after
 * the element would literally timeout.
 * 
 * @author Leo Nobach
 *
 * @param <E>
 */
public interface ITimeoutSetListener<E> {

	/**
	 * Called when an element in the timeout set has timed out.
	 * @param set , the timeout set where the event occurred.
	 * @param element , the element that was removed.
	 * @param timeoutTime , the time when the event's timeout was actually set, this may not be the current time.
	 */
	public void elementTimeouted(TimeoutSet set, E element, long timeoutTime);

}