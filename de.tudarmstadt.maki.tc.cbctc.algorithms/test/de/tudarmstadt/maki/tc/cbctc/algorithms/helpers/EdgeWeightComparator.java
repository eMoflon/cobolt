package de.tudarmstadt.maki.tc.cbctc.algorithms.helpers;

import java.util.Comparator;

import de.tudarmstadt.maki.tc.cbctc.model.Edge;

/**
 * This {@link Comparator} compares {@link Edge}s by their weight.
 * 
 * Edges with small weight appear before edges with larger weight.
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class EdgeWeightComparator implements Comparator<Edge>
{

   @Override
   public int compare(final Edge o1, final Edge o2)
   {
      return Double.compare(o1.getWeight(), o2.getWeight());
   }

}
