package de.tudarmstadt.maki.simonstrator.api.component.pubsub;

import de.tudarmstadt.maki.simonstrator.api.common.Transmitable;
import de.tudarmstadt.maki.simonstrator.api.component.pubsub.analyzer.SubscriptionInfo;
import de.tudarmstadt.maki.simonstrator.api.component.pubsub.attribute.Topic;

/**
 * A Subscription, as defined by the pub/sub semantics.
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public interface Subscription extends Transmitable, Cloneable {

	/**
	 * This method should only be used in simulations. It enables to store
	 * additional properties with the given subscription for analyzing. All
	 * Information should be <strong>read only</strong>, as the subscription
	 * object is not to be cloned() within any overlay - therefore, changes to
	 * the object would immediately appear on all nodes.
	 * 
	 * @param setInfo
	 *            used to initialize the {@link SubscriptionInfo} if it was not
	 *            already set - on further calls, just pass null.
	 */
	public SubscriptionInfo _getSubscriptionInfo(SubscriptionInfo setInfo);

	/**
	 * Basic channel/topic
	 * 
	 * @return
	 */
	public Topic getTopic();

	/**
	 * Full filters for content-based pub/sub
	 * 
	 * @return may return null
	 */
	public Filter getFilter();

	/**
	 * Returns a clone of this subscription
	 * 
	 * @return
	 */
	public Subscription clone();

}
