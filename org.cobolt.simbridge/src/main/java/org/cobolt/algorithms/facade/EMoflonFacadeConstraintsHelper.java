package org.cobolt.algorithms.facade;

import org.cobolt.model.EdgeState;
import org.cobolt.model.constraints.ConstraintsFactory;
import org.cobolt.model.constraints.EdgeStateBasedConnectivityConstraint;

/**
 * Helper class for graph constraints in the {@link EMoflonFacade}
 * 
 * @author Roland Kluge - Initial implementation
 */
public class EMoflonFacadeConstraintsHelper {

	public static EdgeStateBasedConnectivityConstraint createPhysicalConnectivityConstraint() {
		final EdgeStateBasedConnectivityConstraint constraint = ConstraintsFactory.eINSTANCE
				.createEdgeStateBasedConnectivityConstraint();
		constraint.getStates().add(EdgeState.ACTIVE);
		constraint.getStates().add(EdgeState.INACTIVE);
		constraint.getStates().add(EdgeState.UNCLASSIFIED);
		return constraint;
	}

	public static EdgeStateBasedConnectivityConstraint createWeakConnectivityConstraint() {
		final EdgeStateBasedConnectivityConstraint constraint = ConstraintsFactory.eINSTANCE
				.createEdgeStateBasedConnectivityConstraint();
		constraint.getStates().add(EdgeState.ACTIVE);
		constraint.getStates().add(EdgeState.UNCLASSIFIED);
		return constraint;
	}

}
