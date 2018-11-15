package de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.tudarmstadt.maki.simonstrator.api.common.UniqueID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IElement;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;

/**
 * This is a generic superclass for constraints.
 *
 * Concrete constraints should implement the {@link #checkCandidates(Collection)} method.
 * The constraint may be checked by calling {@link #isFulfilled(Collection)} on a list binding candidates for the variables of the constraint.
 */
public abstract class GraphElementConstraint implements Serializable {

	private final List<? extends UniqueID> variables;

	public GraphElementConstraint(final List<? extends UniqueID> variables) {
		this.variables = new ArrayList<>(variables);
	}

	/**
	 * This method implements the logic to check the given candidates.
	 *
	 * The candidates are guaranteed to be of compatible types and
	 */
	protected abstract boolean checkCandidates(final Collection<? extends IElement> bindingCandidates);

	public List<UniqueID> getVariables() {
		return Collections.unmodifiableList(variables);
	}

	/**
	 * This method should be called to check whether a set of candidates
	 * fulfills this constraint.
	 *
	 * There should be as many candidates in the list as variables,
	 * and the candidates should have compatible types.
	 *
	 * @see #isCompatible(UniqueID, IElement)
	 *
	 */
	public final boolean isFulfilled(final Collection<? extends IElement> bindingCandidates) {
		this.validateCandidateCount(bindingCandidates);
		this.validateCandidateTypes(bindingCandidates);
		return checkCandidates(bindingCandidates);
	}

	/**
	 * Returns whether the given variable and the given candidate are of compatible types.
	 * More precisely, a {@link INodeID} ({@link EdgeID}) is compatible to a candidate
	 * that is an instance of {@link INode} ({@link IEdge}) at position X.
	 *
	 * @param var
	 * @param candidate
	 * @return
	 */
	public boolean isCompatible(final UniqueID var, final IElement candidate) {
		if (var instanceof INodeID && candidate instanceof INode) {
			return true;
		} else if (var instanceof EdgeID && candidate instanceof IEdge) {
			return true;
		} else {
			return false;
		}
	}

	private void validateCandidateCount(final Collection<? extends IElement> bindingCandidates) {
		if (this.variables.size() != bindingCandidates.size()) {
			throw new IllegalArgumentException(
					String.format("Different number of candidates (%d)and variables (%d)", bindingCandidates.size(), this.variables.size()));
		}
	}

	private void validateCandidateTypes(final Collection<? extends IElement> bindingCandidates) {
		final Iterator<? extends UniqueID> variableIter = this.variables.iterator();
		final Iterator<? extends IElement> candidateIter = bindingCandidates.iterator();
		while (variableIter.hasNext()) {
			final UniqueID var = variableIter.next();
			final IElement candidate = candidateIter.next();
			if (!isCompatible(var, candidate)) {
				throw new IllegalArgumentException(String.format("Incompatible types of variable (%s) and candidate (%s)", var, candidate));
			}

		}
	}

	@Override
	public String toString() {
		return String.format("Constraint [variable=%s]", this.getVariables());
	}
	
	private static final long serialVersionUID = -4056572756070565908L;
}
