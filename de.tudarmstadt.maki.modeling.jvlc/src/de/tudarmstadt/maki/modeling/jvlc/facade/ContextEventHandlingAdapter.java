package de.tudarmstadt.maki.modeling.jvlc.facade;

import org.eclipse.emf.ecore.EAttribute;

import de.tudarmstadt.maki.modeling.graphmodel.Edge;
import de.tudarmstadt.maki.modeling.graphmodel.Node;
import de.tudarmstadt.maki.modeling.graphmodel.listener.GraphContentAdapter;
import de.tudarmstadt.maki.modeling.jvlc.IncrementalKTC;
import de.tudarmstadt.maki.modeling.jvlc.KTCLink;
import de.tudarmstadt.maki.modeling.jvlc.KTCNode;

public class ContextEventHandlingAdapter extends GraphContentAdapter {

	private final IncrementalKTC algorithm;
	private boolean locked;

	public ContextEventHandlingAdapter(final IncrementalKTC algorithm) {
		this.algorithm = algorithm;
		this.locked = false;
	}

	@Override
	protected void nodeAttributeChanged(final Node node, final EAttribute attribute, final Object oldValue) {
		super.nodeAttributeChanged(node, attribute, oldValue);

		if (!this.locked) {
			this.locked = true;
			this.algorithm.handleNodeAttributeModification((KTCNode) node);
			this.locked = false;
		}
	}

	@Override
	protected void edgeAttributeChanged(final Edge edge, final EAttribute attribute, final Object oldValue) {
		super.edgeAttributeChanged(edge, attribute, oldValue);

		if (!this.locked) {
			this.locked = true;
			this.algorithm.handleLinkAttributeModification((KTCLink) edge);
			this.locked = false;
		}
	}

	@Override
	protected void nodeAdded(final Node newNode) {
		super.nodeAdded(newNode);

		if (!this.locked) {
			this.locked = true;
			this.algorithm.handleNodeAddition((KTCNode) newNode);
			this.locked = false;
		}
	}

	@Override
	protected void nodeRemoved(final Node removedNode) {
		super.nodeRemoved(removedNode);

		if (!this.locked) {
			this.locked = true;
			this.algorithm.handleNodeDeletion((KTCNode) removedNode);
			this.locked = false;
		}
	}

	@Override
	protected void edgeAdded(final Edge newEdge) {
		super.edgeAdded(newEdge);

		if (!this.locked) {
			this.locked = true;
			this.algorithm.handleLinkAddition((KTCLink) newEdge);
			this.locked = false;
		}
	}

	@Override
	protected void edgeRemoved(final Edge oldEdge) {
		super.edgeRemoved(oldEdge);

		if (!this.locked) {
			this.locked = true;
			this.algorithm.handleLinkDeletion((KTCLink) oldEdge);
			this.locked = false;
		}
	}

}
