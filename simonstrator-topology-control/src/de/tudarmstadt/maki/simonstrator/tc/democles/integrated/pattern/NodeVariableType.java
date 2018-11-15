package de.tudarmstadt.maki.simonstrator.tc.democles.integrated.pattern;

import org.gervarro.democles.specification.VariableType;

public class NodeVariableType implements VariableType {

	private static final NodeVariableType instance = new NodeVariableType();
	
	public static NodeVariableType getInstance() {
		return instance;
	}
}
