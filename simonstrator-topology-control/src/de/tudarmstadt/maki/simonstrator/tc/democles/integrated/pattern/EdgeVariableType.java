package de.tudarmstadt.maki.simonstrator.tc.democles.integrated.pattern;

import org.gervarro.democles.specification.VariableType;

public class EdgeVariableType implements VariableType {
	
private static final EdgeVariableType instance = new EdgeVariableType();
	
	public static EdgeVariableType getInstance() {
		return instance;
	}

}
