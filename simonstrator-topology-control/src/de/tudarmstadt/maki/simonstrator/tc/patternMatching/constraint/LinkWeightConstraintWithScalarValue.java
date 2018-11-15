package de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint;

import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GenericGraphElementProperties;

public class LinkWeightConstraintWithScalarValue extends GraphElementAttributeConstraint {

	private static final long serialVersionUID = 6983295367653731239L;

	public LinkWeightConstraintWithScalarValue(final EdgeID lhsLinkVariable, final double rhsValue, final ComparisonOperator operator) {
		super(lhsLinkVariable, GenericGraphElementProperties.WEIGHT, rhsValue, operator);
	}

	public EdgeID getEdge() {
		return (EdgeID) this.getVariables().get(0);
	}
}
