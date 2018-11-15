package de.tudarmstadt.maki.simonstrator.tc.democles.integrated.pattern;

import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.GraphElementConstraint;

public class SimonstratorConstraintTypeModule  {

	private static final SimonstratorConstraintTypeModule instance = new SimonstratorConstraintTypeModule();
	
	public static final SimonstratorConstraintType OUTGOING_EDGE = new SimonstratorConstraintType("Sim-OutgoingEdge");
	public static final SimonstratorConstraintType INCOMING_EDGE = new SimonstratorConstraintType("Sim-IncomingEdge");
	public static final SimonstratorConstraintType GRAPH_NODES = new SimonstratorConstraintType("Sim-GraphNodes");
	public static final SimonstratorConstraintType GRAPH_EDGES = new SimonstratorConstraintType("Sim-GraphEdges");	
	
	public SimonstratorConstraintConstraintType getConstraintType(final GraphElementConstraint simConstraint) {
		return new SimonstratorConstraintConstraintType(simConstraint);
	}

	public static SimonstratorConstraintTypeModule getInstance() {
		return instance;
	}


	public String getName() {
		return "SimConstraitTypeModule";
	}
}
