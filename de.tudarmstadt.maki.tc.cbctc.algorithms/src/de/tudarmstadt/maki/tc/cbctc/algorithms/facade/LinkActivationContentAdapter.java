package de.tudarmstadt.maki.tc.cbctc.algorithms.facade;

import org.eclipse.emf.ecore.EAttribute;

import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.tc.facade.ILinkStateListener;
import de.tudarmstadt.maki.simonstrator.tc.ktc.EdgeState;
import de.tudarmstadt.maki.simonstrator.tc.ktc.UnderlayTopologyProperties;
import de.tudarmstadt.maki.tc.cbctc.algorithms.AlgorithmsPackage;
import de.tudarmstadt.maki.tc.cbctc.algorithms.KTCLink;
import de.tudarmstadt.maki.tc.cbctc.model.Edge;
import de.tudarmstadt.maki.tc.cbctc.model.listener.GraphContentAdapter;

/**
 * This content adapter listens for link state modifications and notifies the
 * registered {@link ILinkStateListener}s.
 */
class LinkActivationContentAdapter extends GraphContentAdapter {
	private final EMoflonFacade facade;

	/**
	 * @param facade
	 */
	LinkActivationContentAdapter(final EMoflonFacade facade) {
		this.facade = facade;
	}

	@Override
	protected void edgeAttributeChanged(final Edge edge, final EAttribute attribute, final Object oldValue) {
		if (!(edge instanceof KTCLink))
			throw new IllegalArgumentException("Expected an instance of " + KTCLink.class.getName());
		
		super.edgeAttributeChanged(edge, attribute, oldValue);

		final IEdge simEdge = facade.getSimonstratorLinkForTopologyModelLink((KTCLink)edge);
		// We may be in the initialization phase - no events should be
		// triggered here.
		if (simEdge == null) {
			return;
		}

		switch (attribute.getFeatureID()) {
		case AlgorithmsPackage.KTC_LINK__STATE:
			for (final ILinkStateListener listener : facade.getLinkStateListeners()) {
				Object newAttributeValue = edge.eGet(attribute);
				if (de.tudarmstadt.maki.tc.cbctc.model.EdgeState.ACTIVE.equals(newAttributeValue)) {
					simEdge.setProperty(UnderlayTopologyProperties.EDGE_STATE, EdgeState.ACTIVE);
					if (!facade.areLinkStateModificationListenersMuted()) {
						listener.linkActivated(simEdge);
					}
				} else if (de.tudarmstadt.maki.tc.cbctc.model.EdgeState.INACTIVE.equals(newAttributeValue)) {
					simEdge.setProperty(UnderlayTopologyProperties.EDGE_STATE, EdgeState.INACTIVE);
					if (!facade.areLinkStateModificationListenersMuted()) {
						listener.linkInactivated(simEdge);
					}
				} else if (de.tudarmstadt.maki.tc.cbctc.model.EdgeState.UNCLASSIFIED.equals(newAttributeValue)) {
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