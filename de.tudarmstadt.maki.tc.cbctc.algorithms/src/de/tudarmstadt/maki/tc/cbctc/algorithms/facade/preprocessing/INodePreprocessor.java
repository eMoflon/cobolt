package de.tudarmstadt.maki.tc.cbctc.algorithms.facade.preprocessing;

import de.tudarmstadt.maki.tc.cbctc.algorithms.AbstractTopologyControlAlgorithm;
import de.tudarmstadt.maki.tc.cbctc.model.Node;

public interface INodePreprocessor
{
   /**
    * This method shall be invoked prior to processing the next unclassified link of a node.
    * @param node the node to preprocess
    */
   void preprocess(Node node);
   
   /**
    * Sets the algorithm that may serve as a reference for the edge sorting
    * @param algorithm the algorithm to refer to while sorting
    */
   void setAlgorithm(AbstractTopologyControlAlgorithm algorithm);
   
   /**
    * Indicates that edges should be preprocessed in reverse order
    * @param shallReverseOrder
    */
   void setShallReverseOrder(boolean shallReverseOrder);
}
