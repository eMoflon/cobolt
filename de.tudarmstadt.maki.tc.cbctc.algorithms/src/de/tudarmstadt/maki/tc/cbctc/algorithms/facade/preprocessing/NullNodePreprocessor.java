package de.tudarmstadt.maki.tc.cbctc.algorithms.facade.preprocessing;

import de.tudarmstadt.maki.tc.cbctc.algorithms.AbstractTopologyControlAlgorithm;
import de.tudarmstadt.maki.tc.cbctc.model.Node;

/**
 * Null implementation of {@link INodePreprocessor}
 * 
 * @author Roland Kluge - Initial implementation
 */
public class NullNodePreprocessor implements INodePreprocessor
{

   @Override
   public void preprocess(Node node)
   {
      // nop
   }

   @Override
   public void setAlgorithm(AbstractTopologyControlAlgorithm algorithm)
   {
      // nop
   }

   @Override
   public void setShallReverseOrder(boolean shallReverseOrder)
   {
      // nop
   }

}
