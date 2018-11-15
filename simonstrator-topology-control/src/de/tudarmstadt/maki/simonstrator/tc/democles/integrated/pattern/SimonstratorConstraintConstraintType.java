package de.tudarmstadt.maki.simonstrator.tc.democles.integrated.pattern;

import org.gervarro.democles.specification.ConstraintType;

import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.GraphElementConstraint;

public class SimonstratorConstraintConstraintType implements ConstraintType {

	private GraphElementConstraint simConstraint;

	public SimonstratorConstraintConstraintType(GraphElementConstraint simConstraint) {
		this.simConstraint = simConstraint;
	}

	public GraphElementConstraint getSimonstratorConstraint() {
		return this.simConstraint;
	}

}
