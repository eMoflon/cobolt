package de.tudarmstadt.maki.tc.cbctc.algorithms.facade.preprocessing;

import de.tudarmstadt.maki.tc.cbctc.algorithms.impl.NodePreprocessorImpl;
import de.tudarmstadt.maki.tc.cbctc.model.Node;

/**
 * Null implementation of {@link NodePreprocessorImpl}
 * @author Roland Kluge - Initial implementation
 *
 */
public class NullNodePreprocessor extends NodePreprocessorImpl
{
   @Override
   public void preprocess(Node node)
   {
      // Nop
   }
}
