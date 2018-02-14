package org.cobolt.algorithms.facade;

import org.cobolt.model.Edge;
import org.cobolt.model.ModelPackage;
import org.cobolt.model.listener.GraphContentAdapter;
import org.eclipse.emf.ecore.EAttribute;

import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.tc.facade.ILinkStateListener;
import de.tudarmstadt.maki.simonstrator.tc.underlay.EdgeState;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;

/**
 * This content adapter listens for link state modifications and notifies the
 * registered {@link ILinkStateListener}s.
 */
class LinkActivationContentAdapter extends GraphContentAdapter {
	private final EMoflonFacade facade;

	LinkActivationContentAdapter(final EMoflonFacade facade) {
		this.facade = facade;
	}

	@Override
	protected void edgeAttributeChanged(final Edge edge, final EAttribute attribute, final Object oldValue) {
		if (!(edge instanceof Edge))
			throw new IllegalArgumentException("Expected an instance of " + Edge.class.getName());
		
		super.edgeAttributeChanged(edge, attribute, oldValue);

		final IEdge simEdge = facade.getSimonstratorLinkForTopologyModelLink(edge);
		// We may be in the initialization phase - no events should be
		// triggered here.
		if (simEdge == null) {
			return;
		}

		switch (attribute.getFeatureID()) {
		case ModelPackage.EDGE__STATE:
			for (final ILinkStateListener listener : facade.getLinkStateListeners()) {
				Object newAttributeValue = edge.eGet(attribute);
				if (org.cobolt.model.EdgeState.ACTIVE.equals(newAttributeValue)) {
					simEdge.setProperty(UnderlayTopologyProperties.EDGE_STATE, EdgeState.ACTIVE);
					if (!facade.areLinkStateModificationListenersMuted()) {
						listener.linkActivated(simEdge);
					}
				} else if (org.cobolt.model.EdgeState.INACTIVE.equals(newAttributeValue)) {
					simEdge.setProperty(UnderlayTopologyProperties.EDGE_STATE, EdgeState.INACTIVE);
					if (!facade.areLinkStateModificationListenersMuted()) {
						listener.linkInactivated(simEdge);
					}
				} else if (org.cobolt.model.EdgeState.UNCLASSIFIED.equals(newAttributeValue)) {
					simEdge.setProperty(UnderlayTopologyProperties.EDGE_STATE, EdgeState.UNCLASSIFIED);
					if (!facade.areLinkStateModificationListenersMuted()) {
						listener.linkUnclassified(simEdge);
					}
				} else {
					throw new IllegalArgumentException(String.format("Unsupported edge state: %s", newAttributeValue));
				}
			}
			break;
		}
	}
}