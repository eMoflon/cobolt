package de.tudarmstadt.maki.tc.cbctc.model.derivedfeatures;

import java.util.function.Function;

import org.eclipse.emf.ecore.EAttribute;

import de.tudarmstadt.maki.tc.cbctc.model.Edge;

public class EAttributeBasedEdgeWeightProvider implements EdgeWeightProvider
{

   private EAttribute eAttribute;

   private Function<Double, Double> function;

   public EAttributeBasedEdgeWeightProvider(final EAttribute eAttribute)
   {
      this(eAttribute, x -> x);
   }

   public EAttributeBasedEdgeWeightProvider(EAttribute eAttribute, Function<Double, Double> function)
   {
      this.eAttribute = eAttribute;
      this.function = function;
   }

   @Override
   public double getEdgeWeight(Edge edge)
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
