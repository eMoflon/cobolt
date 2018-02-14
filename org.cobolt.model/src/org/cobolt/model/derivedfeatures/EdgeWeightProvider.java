package org.cobolt.model.derivedfeatures;

import org.cobolt.model.Edge;

/**
 * Generic type for weight providers of {@link Edge}s
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public interface EdgeWeightProvider
{
   /**
    * Calculates the weight of the given edge
    * @param edge
    * @return
    */
   double getEdgeWeight(Edge edge);
}
