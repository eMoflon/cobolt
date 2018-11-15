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

package de.tud.kom.p2psim.api.application;

/**
 * This is the Listener for an application, to get the call to do something.
 * This means, that an application can implement this listener to receive calls,
 * to do an action like publish something or request an item. Further exists to
 * do an interaction with the given host. This could be a chat, a comment on a
 * post of the host or something else. Additionally it exists a react method,
 * which will be called if a friend does an action.
 * 
 * <p>
 * Please note, that not every useful workload can be derived with this
 * interface!
 * 
 * @author Christoph Muenker
 * @version 1.0, 14.06.2013
 */
public interface WorkloadListener {
	/**
	 * Gets the Application, which uses this Listener
	 * 
	 * @return The application which is using the listener.
	 */
	public Application getApplication();

}
