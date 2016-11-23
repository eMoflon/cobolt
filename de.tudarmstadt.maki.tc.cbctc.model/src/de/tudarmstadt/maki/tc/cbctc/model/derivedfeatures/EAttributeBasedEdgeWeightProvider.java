package de.tudarmstadt.maki.tc.cbctc.model.derivedfeatures;

import java.util.function.Function;

import org.eclipse.emf.ecore.EAttribute;

import de.tudarmstadt.maki.tc.cbctc.model.Edge;

/**
 * An {@link EdgeWeightProvider} that calculates the weight of an {@link Edge} based on a given (double-valued) {@link EAttribute} and an additional function
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class EAttributeBasedEdgeWeightProvider implements EdgeWeightProvider
{

   private EAttribute eAttribute;

   private Function<Double, Double> function;

   /**
    * Configures this weight provider to use the given eAttribute.
    * 
    * The applied function is the identity function
    * @param eAttribute the {@link EAttribute} to use
    */
   public EAttributeBasedEdgeWeightProvider(final EAttribute eAttribute)
   {
      this(eAttribute, x -> x);
   }

   /**
    * Configures this weight provider to use the given eAttribute and function
    * 
    * @param eAttribute the {@link EAttribute} to use
    * @param function the function to use
    */
   public EAttributeBasedEdgeWeightProvider(final EAttribute eAttribute, final Function<Double, Double> function)
   {
      this.eAttribute = eAttribute;
      this.function = function;
   }

   /**
    * Returns the edge weight, which is calculated by 
    * (i) extracting this provider's configured attribute from the given edge and 
    * (ii) applying the configured function to this value 
    * 
    * The given edge is not modified
    * 
    * @param edge the edge of which the weight shall be determined
    */
   @Override
   public double getEdgeWeight(final Edge edge)
   {
      final Object eFeature = edge.eGet(eAttribute);
      if (eFeature instanceof Number)
      {
         final Number number = (Number) eFeature;
         return function.apply(number.doubleValue());
      } else
      {
         throw new IllegalStateException("Cannot set weight attribute from attribute value " + eFeature);
      }
   }

}
