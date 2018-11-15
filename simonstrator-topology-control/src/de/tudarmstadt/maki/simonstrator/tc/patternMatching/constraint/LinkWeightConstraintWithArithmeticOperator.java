package de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint;

import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GenericGraphElementProperties;

public class LinkWeightConstraintWithArithmeticOperator extends GraphElementAttributeConstraint {

	private static final long serialVersionUID = 1L;

	public LinkWeightConstraintWithArithmeticOperator(final EdgeID lhs, final EdgeID rhs, final double value,
			final ComparisonOperator comparisonOperator, final ArithmeticOperator arithmeticOperator) {
		super(lhs, GenericGraphElementProperties.WEIGHT, rhs, GenericGraphElementProperties.WEIGHT, value, comparisonOperator, arithmeticOperator);
	}

	public EdgeID getLhs() {
		return (EdgeID) getVariables().get(0);
	}

	public EdgeID getRhs() {
		return (EdgeID) getVariables().get(1);
	}
}
