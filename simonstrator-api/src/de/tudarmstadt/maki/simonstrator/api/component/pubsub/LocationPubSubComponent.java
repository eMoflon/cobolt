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

import de.tudarmstadt.maki.simonstrator.api.component.pubsub.attribute.Attribute;
import de.tudarmstadt.maki.simonstrator.api.component.pubsub.attribute.Topic;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.LocationRequest;

/**
 * An updated approach to location-based pub/sub, adhering to the default
 * subscribe-interfaces and just modifying the createSubscription-call. When
 * creating a subscription, a context resolver is provided that enables the
 * pub/sub system to react to context changes.
 * 
 * Note: notifications are not treated differently - it is assumed that the
 * pub/sub system decides which context to include into notifications to enable
 * correct filtering. In the location-based case, this means that a node's
 * current position may or may not be included in a notification, and that
 * brokers may or may not use a dedicated protocol to resolve context values.
 * Refer to the respective implementation of this interface for further
 * documentation about the contract of the respective overlay.
 * 
 * @author Bjoern Richerzhagen
 *
 */
public interface LocationPubSubComponent extends PubSubComponent {

	/**
	 * Create a subscription which is limited to the given radius of interest
	 * (in meters) around our current location. The {@link LocationRequest}
	 * object enables the application to specify the granularity of context
	 * updates desired in the overlay.
	 * 
	 * If the radius of interest is to be changed, one needs to re-subscribe
	 * using the default methods provided in {@link PubSubComponent}.
	 * 
	 * @param topic
	 * @param filter
	 * @param locationRequest
	 * @param radiusOfInterest
	 */
	public Subscription createSubscription(Topic topic, Filter filter,
			LocationRequest locationRequest, double radiusOfInterest);

	/**
	 * Creates a notification that is valid at the provided location (and around
	 * it with the given RoI).
	 * 
	 * @param topic
	 * @param attributes
	 * @param location
	 * @param radiusOfInterest
	 * @param payload
	 *            optional, can be null
	 * @return
	 */
	public Notification createNotification(Topic topic,
			List<Attribute<?>> attributes, Location location,
			double radiusOfInterest, byte[] payload);

}
