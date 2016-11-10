package de.tudarmstadt.maki.tc.cbctc.model.derivedfeatures;

import de.tudarmstadt.maki.tc.cbctc.model.Edge;

public interface EdgeWeightProvider
{
   /**
    * Calculates the weight of the given edge
    * @param edge
    * @return
    */
   double getEdgeWeight(Edge edge);
}
