package de.tudarmstadt.maki.modeling.jvlc.facade;

import org.eclipse.emf.ecore.EAttribute;

import de.tudarmstadt.maki.modeling.graphmodel.Edge;
import de.tudarmstadt.maki.modeling.graphmodel.listener.GraphContentAdapter;
import de.tudarmstadt.maki.modeling.jvlc.JvlcPackage;
import de.tudarmstadt.maki.modeling.jvlc.LinkState;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.tc.facade.ILinkStateListener;
import de.tudarmstadt.maki.simonstrator.tc.ktc.EdgeState;
import de.tudarmstadt.maki.simonstrator.tc.ktc.KTCConstants;

/**
 * This content adapter listens for link state modifications and notifies
 * the registered {@link ILinkStateListener}s.
 */
class LinkActivationContentAdapter extends GraphContentAdapter {
	private final JVLCFacade facade;

	/**
	 * @param jvlcFacade
	 */
	LinkActivationContentAdapter(final JVLCFacade jvlcFacade) {
		facade = jvlcFacade;
	}

	@Override
	protected void edgeAttributeChanged(final Edge edge, final EAttribute attribute, final Object oldValue) {
		super.edgeAttributeChanged(edge, attribute, oldValue);

		final IEdge simEdge = facade.getSimonstratorLinkForTopologyModelLink(edge);
		// We may be in the initialization phase - no events should be
		// triggered here.
		if (simEdge == null) {
			return;
		}

		switch (attribute.getFeatureID()) {
		case JvlcPackage.KTC_LINK__STATE:
			for (final ILinkStateListener listener : facade.getLinkStateListeners()) {
				if (LinkState.ACTIVE.equals(edge.eGet(attribute))) {
					simEdge.setProperty(KTCConstants.EDGE_STATE, EdgeState.ACTIVE);
					listener.linkActivated(simEdge);
				} else if (LinkState.INACTIVE.equals(edge.eGet(attribute))) {
					simEdge.setProperty(KTCConstants.EDGE_STATE, EdgeState.INACTIVE);
					listener.linkInactivated(simEdge);
				} else if (LinkState.UNCLASSIFIED.equals(edge.eGet(attribute))) {
					simEdge.setProperty(KTCConstants.EDGE_STATE, EdgeState.UNCLASSIFIED);
					listener.linkUnclassified(simEdge);
				}
			}
			break;
		}
	}
}