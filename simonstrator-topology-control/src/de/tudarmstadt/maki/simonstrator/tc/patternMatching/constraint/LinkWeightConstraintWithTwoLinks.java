package de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint;

import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GenericGraphElementProperties;

public class LinkWeightConstraintWithTwoLinks extends GraphElementAttributeConstraint {

	private static final long serialVersionUID = -3166710660244757198L;

	public LinkWeightConstraintWithTwoLinks(final EdgeID lhs, final EdgeID rhs, final ComparisonOperator operator) {
		super(lhs, GenericGraphElementProperties.WEIGHT, rhs, GenericGraphElementProperties.WEIGHT, operator);
	}

	public EdgeID getLhs() {
		return (EdgeID) this.getVariables().get(0);
	}

	public EdgeID getRhs() {
		return (EdgeID) this.getVariables().get(1);
	}
}
