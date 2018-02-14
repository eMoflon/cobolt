package org.cobolt.model.listener;

import org.cobolt.model.Edge;
import org.cobolt.model.Node;
import org.cobolt.model.Topology;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EContentAdapter;

/**
 * This class is a change listener for {@link Topology} instances.
 *
 * To register this content adapter, use the following code:
 *
 * <pre>
 * Topology graph = ...;
 * GraphContentAdapter adapter = ...;
 * graph.eAdapters().add(adapter);
 * </pre>
 *
 * @see EContentAdapter
 */
public class GraphContentAdapter extends EContentAdapter {
	private static final String EREFERENCE_GRAPH_TO_EDGE = "edges";
	private static final String EREFERENCE_GRAPH_TO_NODE = "nodes";

	/**
	 * 	Gets called, when a new node is added to the graph.
	 *
	 * This is an empty default implementation.
	 */
	protected void nodeAdded(final Node newNode) {
	}

	/**
	 * Gets called, when a node is removed from the graph.
	 *
	 * This is an empty default implementation.
	 */
	protected void nodeRemoved(final Node removedNode) {
	}

	/**
	 * Gets called, when a new edge is added to the graph.
	 *
	 * This is an empty default implementation.
	 */
	protected void edgeAdded(final Edge newEdge) {
	}

	/**
	 * Gets called, when an edge is removed from the graph.
	 *
	 * This is an empty default implementation.
	 */
	protected void edgeRemoved(final Edge oldEdge) {
	}

	/**
	 * Gets called when any immediate attribute of the given edge has changed. The old value is passed as final argument. The new value can be extracted from the attribute.
	 *
	 * This is an empty default implementation.
	 */
	protected void edgeAttributeChanged(final Edge edge, final EAttribute attribute, final Object oldValue) {
	}

	/**
	 * Gets called when any immediate attribute of the given node has changed. The old value is passed as final argument. The new value can be extracted from the attribute.
	 *
	 * This is an empty default implementation.
	 */
	protected void nodeAttributeChanged(final Node node, final EAttribute attribute, final Object oldValue) {
	}

	@Override
	public void notifyChanged(final Notification notification) {
		// Important! Only with this call to super, recursive modifications are detected
		super.notifyChanged(notification);

		final Object notifier = notification.getNotifier();
		final Object oldValue = notification.getOldValue();

		if (notifier instanceof Node && notification.getFeature() instanceof EAttribute && notification.getEventType() == Notification.SET) {
			nodeAttributeChanged((Node) notifier, (EAttribute) notification.getFeature(), notification.getOldValue());
		}

		if (notifier instanceof Edge && notification.getFeature() instanceof EAttribute && notification.getEventType() == Notification.SET) {
			edgeAttributeChanged((Edge) notifier, (EAttribute) notification.getFeature(), notification.getOldValue());
		}

		/*
		 * Detect structural changes
		 */
		if (notifier instanceof Topology) {
			if (notification.getFeature() instanceof EReference) {
				final EReference ereference = (EReference) notification.getFeature();
				switch (ereference.getName()) {
				case EREFERENCE_GRAPH_TO_NODE:
					switch (notification.getEventType()) {
					case Notification.ADD:
						nodeAdded((Node) notification.getNewValue());
						break;
					case Notification.REMOVE:
						nodeRemoved((Node) oldValue);
						break;
					}
					break;
				case EREFERENCE_GRAPH_TO_EDGE:
					switch (notification.getEventType()) {
					case Notification.ADD:
						edgeAdded((Edge) notification.getNewValue());
						break;
					case Notification.REMOVE:
						edgeRemoved((Edge) oldValue);
						break;
					}
					break;
				}
			}
		}
	}
}