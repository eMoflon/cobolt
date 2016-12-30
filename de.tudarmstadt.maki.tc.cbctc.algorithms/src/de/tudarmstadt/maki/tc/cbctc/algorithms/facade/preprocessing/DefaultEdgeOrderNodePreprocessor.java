package de.tudarmstadt.maki.tc.cbctc.algorithms.facade.preprocessing;

import de.tudarmstadt.maki.tc.cbctc.algorithms.AbstractTopologyControlAlgorithm;
import de.tudarmstadt.maki.tc.cbctc.algorithms.EnergyAwareKTC;
import de.tudarmstadt.maki.tc.cbctc.model.Edge;
import de.tudarmstadt.maki.tc.cbctc.model.Node;

/**
 * This preprocessor uses a standard, algorithm-specific sorting order for edges.
 * 
 * For all algorithms (apart from {@link EnergyAwareKTC}), the preprocessor sorts all outgoing edges of a node by increasing weight.
 * In case of {@link EnergyAwareKTC}, this preprocessor sorts the outgoing edges of a node by decreasing expected lifetime.
 * 
 * The sort order can be inverted by setting {@link #shallReverseOrder} to false.
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class DefaultEdgeOrderNodePreprocessor implements INodePreprocessor
{

   private AbstractTopologyControlAlgorithm algorithm;
   
   private boolean shallReverseOrder;

   @Override
   public void preprocess(Node node)
   {
      final long ticMillis = System.currentTimeMillis();
      org.eclipse.emf.common.util.ECollections.sort(node.getOutgoingEdges(), new java.util.Comparator<Edge>() {
         @Override
         public int compare(final Edge o1, final Edge o2)
         {
            final int signum = shallReverseOrder ? -1 : 1;
            if (algorithm instanceof EnergyAwareKTC)
            {
               return signum * -Double.compare(o1.getExpectedLifetime(), o2.getExpectedLifetime());
            } else
            {
               return signum * Double.compare(o1.getWeight(), o2.getWeight());
            }
         }

      });
      final long tocMillis = System.currentTimeMillis();
      final long durationInMillis = tocMillis - ticMillis;
      de.tudarmstadt.maki.simonstrator.api.Monitor.log(getClass(), de.tudarmstadt.maki.simonstrator.api.Monitor.Level.DEBUG,
            "Sorting during preprocessing of unclassified link identification took %dms", durationInMillis);
   }

   @Override
   public void setAlgorithm(AbstractTopologyControlAlgorithm algorithm)
   {
      this.algorithm = algorithm;
   }
   
   @Override
   public void setShallReverseOrder(boolean shallReverseOrder)
   {
      this.shallReverseOrder = shallReverseOrder;
   }

}
