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


/**
 * This listener is registered when subscribing to a topic and is notified upon
 * matching events.
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public interface PubSubListener {

	/**
	 * Called, whenever a new notification arrives and matches a local
	 * subscription
	 * 
	 * @param matchedSubscription
	 *            use this if you want to unsubscribe
	 * @param notification
	 */
	public void onNotificationArrived(Subscription matchedSubscription,
			Notification notification);

	// /**
	// * Called, whenever a new notification arrives and matches a local
	// * subscription
	// *
	// * @param matchedSubscription
	// * use this if you want to unsubscribe
	// * @param notification
	// */
	// public void onNotificationArrived(Subscription matchedSubscription,
	// Notification notification, PayloadSource payloadSource);
	//
	// /**
	// *
	// * @param matchedSubscription
	// * @param notification
	// * @param payload
	// */
	// public void onPayloadArrived(Subscription matchedSubscription,
	// Notification notification, Message payload);
	//
	// /**
	// * Use this, if the pub/sub is also used to transport payload besides
	// * attributes. Example: a publication includes attributes (metadata) and a
	// * photo, but the notification does not include the photo, which is
	// * downloaded in a two-step process after the metadata is retrieved. The
	// * pub/sub might support such a two-step process, which is why this
	// * interface is provided.
	// *
	// * Internally, if the pub/sub does not use a two-step process, a call to
	// * this method will result in a direct callback on the listener.
	// *
	// * @author Bjoern Richerzhagen
	// *
	// */
	// public interface PayloadSource {
	//
	// public void getPayload(PubSubListener listener);
	//
	// }

}
