package de.tudarmstadt.maki.simonstrator.api.component.topology;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import de.tudarmstadt.maki.simonstrator.api.common.UniqueID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.DirectedEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IElement;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Node;

/**
 * This class represents a binding of pattern variables to graph elements
 */
public final class VariableAssignment implements Serializable {

	private static final long serialVersionUID = -3563666352394644565L;
	private final BiMap<INodeID, INode> nodeBinding;
	private final Map<EdgeID, IEdge> linkBinding;

	public VariableAssignment() {
		this.nodeBinding = HashBiMap.create();
		this.linkBinding = new HashMap<>();
	}

	public VariableAssignment(final VariableAssignment other) {
		this();
		this.nodeBinding.putAll(other.nodeBinding);
		this.linkBinding.putAll(other.linkBinding);
	}

	public INodeID getNodeVariableBinding(final INodeID variable) {
		final INode binding = this.nodeBinding.get(variable);
		return binding != null ? binding.getId() : null;
	}

	public INode getBindingNodeForNodeVariable(final INodeID variable) {
		return this.nodeBinding.get(variable);
	}

	public INodeID getInverseVariableBinding(final INode variable) {
		return this.nodeBinding.inverse().get(variable);
	}

	public INodeID getNodeVariableBinding(final String key) {
		return nodeBinding.get(INodeID.get(key)).getId();
	}

	public Collection<IEdge> getLinkBindingValueSet() {
		return linkBinding.values();
	}

	public Set<Entry<INodeID, INode>> getNodeBindingEntrySet() {
		return nodeBinding.entrySet();
	}

	public Set<Entry<EdgeID, IEdge>> getLinkBindingEntrySet() {
		return linkBinding.entrySet();
	}

	public void bindVariable(final UniqueID variable, final IElement candidate) {
		if (variable instanceof INodeID) {
			this.bindNodeVariable((INodeID) variable, (INodeID) candidate);
		} else if (variable instanceof EdgeID) {
			this.bindLinkVariable((EdgeID) variable, (EdgeID) candidate);
		} else {
			throw new IllegalArgumentException("Cannot handle variable type: " + variable);
		}
	}

	/**
	 * @deprecated Use {@link #bindNodeVariable(INodeID, INode)} instead
	 */
	@Deprecated
	public void bindNodeVariable(final INodeID nodeVariable, final INodeID value) {
		nodeBinding.put(nodeVariable, new Node(value));
	}

	public void bindNodeVariable(final INodeID nodeVariable, final INode value) {
		nodeBinding.put(nodeVariable, value);
	}

	public void unbindNodeVariable(final INodeID nodeVariable) {
		nodeBinding.remove(nodeVariable);
	}

	public boolean isBound(final INodeID variable) {
		return nodeBinding.containsKey(variable);
	}

	public boolean isUnbound(final INodeID variable) {
		return !this.isBound(variable);
	}

	public boolean isBoundInverse(final INode variable) {
		return nodeBinding.inverse().containsKey(variable);
	}

	public boolean isBoundInverse(final INodeID variable) {
		return nodeBinding.inverse().containsKey(new Node(variable));
	}

	public boolean isBound(final EdgeID variable) {
		return linkBinding.containsKey(variable);
	}

	public boolean isUnbound(final EdgeID variable) {
		return !this.isBound(variable);
	}

	/**
	 * Returns whether there exists some variable that is bound by the given nodeId.
	 * 
	 * @deprecated Use {@link #isBindingForSomeVariable(INode)}
	 */
	@Deprecated
	public boolean isBindingForSomeVariable(final INodeID bindingValue) {
		return nodeBinding.inverse().containsKey(new Node(bindingValue));
	}

	public boolean isBindingForSomeVariable(final INode node) {
		return nodeBinding.inverse().containsKey(node);
	}

	/**
	 * @deprecated Use {@link #getBindingLink(EdgeID)}
	 */
	@Deprecated
	public EdgeID getLinkVariableBinding(final EdgeID edgeVariable) {
		for (final EdgeID linkVariable : this.linkBinding.keySet()) {
			if (linkVariable.equals(edgeVariable))
				return this.linkBinding.get(linkVariable).getId();
		}
		return null;
	}

	public IEdge getBindingLink(final EdgeID linkVariable) {
		return this.linkBinding.get(linkVariable);
	}

	@Deprecated
	public EdgeID getLinkVariableBinding(final String linkVariableId) {
		return this.linkBinding.get(EdgeID.get(linkVariableId)).getId();
	}

	public UniqueID getVariableBinding(final UniqueID variable) {
		if (variable instanceof EdgeID)
			return getLinkVariableBinding((EdgeID) variable);
		else if (variable instanceof INodeID)
			return getNodeVariableBinding((INodeID) variable);
		return null;
	}

	/**
	 * @deprecated Use {@link #bindLinkVariable(EdgeID, IEdge)} instead
	 */
	@Deprecated
	public void bindLinkVariable(final EdgeID linkVariable, final EdgeID edgeId) {
		this.bindLinkVariable(linkVariable, new DirectedEdge(null, null, edgeId));
	}

	public void bindLinkVariable(final EdgeID linkVariable, final IEdge edge) {
		this.linkBinding.put(linkVariable, edge);
	}

	public void removeAllLinkBindings() {
		this.linkBinding.clear();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((linkBinding == null) ? 0 : linkBinding.hashCode());
		result = prime * result + ((nodeBinding == null) ? 0 : nodeBinding.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final VariableAssignment other = (VariableAssignment) obj;
		if (linkBinding == null) {
			if (other.linkBinding != null) {
				return false;
			}
		} else if (!linkBinding.equals(other.linkBinding)) {
			return false;
		}
		if (nodeBinding == null) {
			if (other.nodeBinding != null) {
				return false;
			}
		} else if (!nodeBinding.equals(other.nodeBinding)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "VA [node vars:" + this.nodeBinding + ", link vars: " + this.linkBinding.toString() + "]";
	}

	public boolean isMergeable(final VariableAssignment mergeAssignment) {
		for (final INodeID node : this.nodeBinding.keySet()) {
			// pattern node has the same binding (if there is one)
			if (mergeAssignment.isBound(node)) {
				if (!this.getBindingNodeForNodeVariable(node)
						.equals(mergeAssignment.getBindingNodeForNodeVariable(node))) {
					return false;
				}
			} else if (mergeAssignment.isBoundInverse(this.getBindingNodeForNodeVariable(node))) {
				return false;
			}
		}
		for (final EdgeID edge : this.linkBinding.keySet()) {
			// pattern link has the same binding (if there is one)
			if (mergeAssignment.isBound(edge)) {
				if (!this.getBindingLink(edge).equals(mergeAssignment.getBindingLink(edge))) {
					return false;
				}
			}
			// a link may be bound only once
			else if (mergeAssignment.getLinkBindingValueSet().contains(this.getBindingLink(edge))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * check if merge is possible in advance with isMergeable
	 * 
	 * @param mergeAssignment
	 * @return
	 */
	public VariableAssignment merge(final VariableAssignment mergeAssignment) {
		final VariableAssignment merged = new VariableAssignment(mergeAssignment);
		for (final INodeID node : this.nodeBinding.keySet()) {
			if (merged.isUnbound(node)) {
				merged.bindNodeVariable(node, getBindingNodeForNodeVariable(node));
			}
		}
		for (final EdgeID edge : this.linkBinding.keySet()) {
			if (merged.isUnbound(edge)) {
				merged.bindLinkVariable(edge, getBindingLink(edge));
			}
		}
		return merged;
	}

	// /**
	// * Combines this variable assignment with the given assignment.
	// *
	// * @param localPatternAssignments
	// * @return the new assignment containing all the combined assignments
	// */
	// public VariableAssignment merge(VariableAssignment
	// localPatternAssignments) {
	//
	// VariableAssignment newAssignment = new VariableAssignment(this);
	// newAssignment.nodeBinding.putAll(localPatternAssignments.nodeBinding);
	// newAssignment.linkBinding.putAll(localPatternAssignments.linkBinding);
	//
	// return newAssignment;
	// }

}
