package de.tudarmstadt.maki.simonstrator.tc.democles.integrated.pattern;

import org.gervarro.democles.specification.VariableType;

public class GraphVariableType implements VariableType {
	
private static final GraphVariableType instance = new GraphVariableType();
	
	public static GraphVariableType getInstance() {
		return instance;
	}

}
