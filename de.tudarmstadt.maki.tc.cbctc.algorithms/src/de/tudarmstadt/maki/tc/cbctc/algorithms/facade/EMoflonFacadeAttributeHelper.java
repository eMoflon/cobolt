package de.tudarmstadt.maki.tc.cbctc.algorithms.facade;

import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;
import de.tudarmstadt.maki.tc.cbctc.model.EdgeState;

public class EMoflonFacadeAttributeHelper
{
   private static final EdgeState DEFAULT_VALUE_FOR_UNDEFINED_EDGE_STATE = EdgeState.UNCLASSIFIED;

   private EMoflonFacadeAttributeHelper()
   {
      throw new UnsupportedOperationException();
   }

   static EdgeState getEdgeStateSafe(IEdge prototype)
   {
      final de.tudarmstadt.maki.simonstrator.tc.underlay.EdgeState value = prototype.getProperty(UnderlayTopologyProperties.EDGE_STATE);
      if (value != null)
         return mapToModelEdgeState(value);
      else
         return DEFAULT_VALUE_FOR_UNDEFINED_EDGE_STATE;
   }

   private static EdgeState mapToModelEdgeState(de.tudarmstadt.maki.simonstrator.tc.underlay.EdgeState value)
   {
      switch(value)
      {
      case ACTIVE:
         return EdgeState.ACTIVE;
      case INACTIVE:
         return EdgeState.INACTIVE;
      case UNCLASSIFIED:
         return EdgeState.UNCLASSIFIED;
      default:
            throw new IllegalArgumentException("Unsupported edge state: " + value);
      }
   }

}
