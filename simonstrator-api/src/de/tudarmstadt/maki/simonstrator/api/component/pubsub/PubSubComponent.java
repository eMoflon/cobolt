package de.tudarmstadt.maki.simonstrator.api.component.pubsub;

import java.util.List;

import de.tudarmstadt.maki.simonstrator.api.component.overlay.OverlayComponent;
import de.tudarmstadt.maki.simonstrator.api.component.pubsub.attribute.Attribute;
import de.tudarmstadt.maki.simonstrator.api.component.pubsub.attribute.AttributeFilter;
import de.tudarmstadt.maki.simonstrator.api.component.pubsub.attribute.FilterOperator;
import de.tudarmstadt.maki.simonstrator.api.component.pubsub.attribute.OperatorImplementation;
import de.tudarmstadt.maki.simonstrator.api.component.pubsub.attribute.Topic;

/**
 * Component that can be used to access the pub/sub service. This interface
 * provides a content-based pub/sub via attributes and filters. A simpler,
 * channel/topic-based interface is provided as well via the concept of
 * {@link Topic}s. Topics are also used to distinguish application domains -
 * i.e., an overlay might subscribe to the notifications of the monitoring
 * service.
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public interface PubSubComponent extends OverlayComponent {

	/*
	 * publisher - advertisements are optional.
	 */

	/**
	 * Publish a Notification - The notification has to be created using this
	 * component
	 * 
	 * @param notification
	 */
	public void publish(Notification notification);

	/**
	 * 
	 * @param filter
	 */
	public void advertise(Filter filter);

	/**
	 * 
	 * @param filter
	 */
	public void unadvertise(Filter filter);

	/*
	 * subscriber
	 */

	/**
	 * Subscribe and receive matching notifications via the provided
	 * {@link PubSubListener}
	 * 
	 * @param sub
	 * @param listener
	 */
	public void subscribe(Subscription sub, PubSubListener listener);

	/**
	 * Unsubscribe
	 * 
	 * @param sub
	 */
	public void unsubscribe(Subscription sub);

	/*
	 * application - has to define semantics (i.e., topics, attributes, and
	 * filters)
	 */

	/**
	 * A Topic is a channel-based simplification of filters and attributes
	 * 
	 * @param uri
	 * @return
	 */
	public Topic createTopic(String uri);

	/**
	 * Create notifications in a content-based pub/sub
	 * 
	 * @param topic
	 * @param attributes
	 * @param payload
	 * @return
	 */
	public Notification createNotification(Topic topic,
			List<Attribute<?>> attributes, byte[] payload);

	/**
	 * Shorthand for channel/topic-based pub/sub
	 * 
	 * @param topic
	 * @param payload
	 * @return
	 */
	public Notification createNotification(Topic topic, byte[] payload);

	/**
	 * Create an attribute, if the given type is supported by the pub/sub
	 * service semantics. Attributes, that are not supported will still be
	 * included, but can not be used for filtering and/or merging of filters
	 * (i.e., they are only allowed in notifications!)
	 * 
	 * @param type
	 * @param name
	 * @param value
	 * @return
	 */
	public <T> Attribute<T> createAttribute(Class<T> cls, String name, T value);

	/**
	 * Create an attribute filter based on an attribute and an operator
	 * 
	 * @param attribute
	 * @param operator
	 * @return
	 */
	public <T> AttributeFilter<T> createAttributeFilter(Attribute<T> attribute,
			FilterOperator operator);

	/**
	 * Overwrite the Operator implementation for a given Type
	 */
	public <T> void setOperatorImplementation(FilterOperator operator,
			Class<T> operandType, OperatorImplementation implementation);

	/**
	 * Create a filter based on multiple attribute filters
	 * 
	 * @param attributeFilters
	 * @return
	 */
	public Filter createFilter(List<AttributeFilter<?>> attributeFilters);

	/**
	 * Create a subscription based on a topic and a filter
	 * 
	 * @param topic
	 * @param filter
	 * @return
	 */
	public Subscription createSubscription(Topic topic, Filter filter);

	/**
	 * Create a subscription on a given topic
	 * 
	 * @param topic
	 * @return
	 */
	public Subscription createSubscription(Topic topic);

	/**
	 * Matching-function as implemented by the overlay.
	 * 
	 * @param notification
	 * @param subscription
	 * 
	 * @deprecated rely on isSubscribedTo instead, this method should not be
	 *             public outside of the overlay anymore.
	 * @return
	 */
	@Deprecated
	public boolean matches(Notification notification, Subscription subscription);

}
