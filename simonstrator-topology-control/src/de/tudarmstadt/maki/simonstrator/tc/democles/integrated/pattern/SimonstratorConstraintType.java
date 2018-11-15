package de.tudarmstadt.maki.simonstrator.tc.democles.integrated.pattern;

import org.gervarro.democles.specification.ConstraintType;

/**
 * Marker interface for Simonstrator-specific constraint types
 */
public class SimonstratorConstraintType implements ConstraintType {

	private final String id;
	
	public SimonstratorConstraintType(final String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
}
