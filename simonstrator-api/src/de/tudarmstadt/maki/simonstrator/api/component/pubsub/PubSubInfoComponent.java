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

package de.tudarmstadt.maki.simonstrator.api.component.pubsub;

import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;

/**
 * Methods and getters for information that is relevant for analyzing, oracles
 * and visualizations. These should not be functional part of an application.
 * 
 * @author Bjoern Richerzhagen
 *
 */
public interface PubSubInfoComponent extends HostComponent {

	/**
	 * Returns true, if the local node is subscribed to the given notification
	 * 
	 * @param notification
	 * @return
	 */
	public boolean isSubscribedTo(Notification notification);

}
