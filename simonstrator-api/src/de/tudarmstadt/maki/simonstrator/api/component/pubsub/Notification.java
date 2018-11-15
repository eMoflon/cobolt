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

import java.util.List;

import de.tudarmstadt.maki.simonstrator.api.common.Transmitable;
import de.tudarmstadt.maki.simonstrator.api.component.pubsub.analyzer.NotificationInfo;
import de.tudarmstadt.maki.simonstrator.api.component.pubsub.attribute.Attribute;
import de.tudarmstadt.maki.simonstrator.api.component.pubsub.attribute.Topic;

/**
 * A Notification, consisting of a "pub/sub-parseable" topic and attributes and
 * additional application payload.
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public interface Notification extends Transmitable {

	/**
	 * This method should only be used in simulations. It enables to store
	 * additional properties with the given notification for analyzing. All
	 * Information should be <strong>read only</strong>, as the notification
	 * object is not to be cloned() within any overlay - therefore, changes to
	 * the object would immediately appear on all nodes.
	 * 
	 * @param info
	 *            the info object used to initialize or null on further calls.
	 * @return the info object
	 */
	public NotificationInfo _getNotificationInfo(NotificationInfo info);

	/**
	 * A Notification is tied to a specific topic (i.e., is delivered to a
	 * domain defined by the given Topic-URI). If you want to adress all
	 * subscribers, you could use the topic-domain <code>/*</code> (or, better:
	 * define a common prefix for your application/service, i.e.,
	 * <code>/monitoring/*</code>.
	 * 
	 * @return
	 */
	public Topic getTopic();
	
	/**
	 * Additional attributes (i.e., content that is understood by the pub/sub
	 * and can be used for filtering). This is optional, and not used in a basic
	 * channel-based pub/sub - it will return only the topic in this case.
	 * 
	 * @return
	 */
	public List<Attribute<?>> getAttributes();

	/**
	 * TYped access to a single attribute via the name.
	 * 
	 * @param name
	 * @param valueType
	 * @return the attribute or null.
	 */
	public <T> Attribute<T> getAttribute(String name, Class<T> valueType);

	/**
	 * The application-payload
	 * 
	 * @return
	 */
	public byte[] getPayload();

}
