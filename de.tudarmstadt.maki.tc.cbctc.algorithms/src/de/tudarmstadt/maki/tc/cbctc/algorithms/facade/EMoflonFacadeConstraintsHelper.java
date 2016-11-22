package de.tudarmstadt.maki.tc.cbctc.algorithms.facade;

import de.tudarmstadt.maki.tc.cbctc.model.EdgeState;
import de.tudarmstadt.maki.tc.cbctc.model.constraints.ConstraintsFactory;
import de.tudarmstadt.maki.tc.cbctc.model.constraints.EdgeStateBasedConnectivityConstraint;

public class EMoflonFacadeConstraintsHelper
{

   public static EdgeStateBasedConnectivityConstraint createPhysicalConnectivityConstraint()
   {
      final EdgeStateBasedConnectivityConstraint constraint = ConstraintsFactory.eINSTANCE.createEdgeStateBasedConnectivityConstraint();
      constraint.getStates().add(EdgeState.ACTIVE);
      constraint.getStates().add(EdgeState.INACTIVE);
      constraint.getStates().add(EdgeState.UNCLASSIFIED);
      return constraint;
   }

   public static EdgeStateBasedConnectivityConstraint createWeakConnectivityConstraint()
   {
      final EdgeStateBasedConnectivityConstraint constraint = ConstraintsFactory.eINSTANCE.createEdgeStateBasedConnectivityConstraint();
      constraint.getStates().add(EdgeState.ACTIVE);
      constraint.getStates().add(EdgeState.UNCLASSIFIED);
      return constraint;
   }

}
