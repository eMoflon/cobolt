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

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent.Analyzer;
import de.tudarmstadt.maki.simonstrator.api.component.pubsub.Notification;

/**
 * A forwarding Analyzer (usually only useful in an Ad Hoc Scenario)
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public interface PubSubNotificationForwardingAnalyzer extends Analyzer {

	/**
	 * Invoked when the given host (localhost) forwarded a notification.
	 * 
	 * @param localhost
	 * @param notification
	 * @param receivedFrom
	 *            (optional) the ID of the host that forwarded this notification
	 *            to us.
	 * @param color
	 *            (optional) allows distinction between different types of
	 *            messages being forwarded.
	 */
	public void onForwardNotification(Host localhost, Notification notification,
			INodeID receivedFrom, int color);
	
	/**
	 * Invoked when the given host (localhost) decided to drop a notification
	 * instead of forwarding it further down the line.
	 * 
	 * @param localhost
	 * @param notification
	 * @param receivedFrom
	 *            (optional) the ID of the host that forwarded this notification
	 *            to us.
	 * @param color
	 *            (optional) allows distinction between different types of
	 *            messages being dropped.
	 */
	public void onDropNotification(Host localhost, Notification notification,
			INodeID receivedFrom, int color);

}
