package de.tudarmstadt.maki.modeling.jvlc.facade;

import org.eclipse.emf.ecore.EAttribute;

import de.tudarmstadt.maki.modeling.graphmodel.Edge;
import de.tudarmstadt.maki.modeling.graphmodel.listener.GraphContentAdapter;
import de.tudarmstadt.maki.modeling.jvlc.JvlcPackage;
import de.tudarmstadt.maki.modeling.jvlc.KTCLink;
import de.tudarmstadt.maki.modeling.jvlc.LinkState;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.tc.facade.ILinkStateListener;
import de.tudarmstadt.maki.simonstrator.tc.ktc.EdgeState;
import de.tudarmstadt.maki.simonstrator.tc.ktc.KTCConstants;

/**
 * This content adapter listens for link state modifications and notifies the
 * registered {@link ILinkStateListener}s.
 */
class LinkActivationContentAdapter extends GraphContentAdapter {
	private final JVLCFacade facade;

	/**
	 * @param facade
	 */
	LinkActivationContentAdapter(final JVLCFacade facade) {
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
		case JvlcPackage.KTC_LINK__STATE:
			for (final ILinkStateListener listener : facade.getLinkStateListeners()) {
				Object newAttributeValue = edge.eGet(attribute);
				if (LinkState.ACTIVE.equals(newAttributeValue)) {
					simEdge.setProperty(KTCConstants.EDGE_STATE, EdgeState.ACTIVE);
					if (!facade.areLinkStateModificationListenersMuted()) {
						listener.linkActivated(simEdge);
					}
				} else if (LinkState.INACTIVE.equals(newAttributeValue)) {
					simEdge.setProperty(KTCConstants.EDGE_STATE, EdgeState.INACTIVE);
					if (!facade.areLinkStateModificationListenersMuted()) {
						listener.linkInactivated(simEdge);
					}
				} else if (LinkState.UNCLASSIFIED.equals(newAttributeValue)) {
					simEdge.setProperty(KTCConstants.EDGE_STATE, EdgeState.UNCLASSIFIED);
					if (!facade.areLinkStateModificationListenersMuted()) {
						listener.linkUnclassified(simEdge);
					}
				}
			}
			break;
		}
	}
}