package de.tudarmstadt.maki.simonstrator.tc.facade;

import java.util.Collection;

public class TopologyControlAlgorithmID
{

   private String name;

   private int uniqueId;

   private Collection<TopologyControlAlgorithmParameterId<?>> parameterNames;

   /**
    * 
    * @param name
    *            the descriptive name of the algorithm
    * @param uniqueId
    *            an integer ID that should be unique among all algorithms that
    *            may occur in the same context
    * @param parameterNames list of the names of parameters used by this algorithm
    */
   public TopologyControlAlgorithmID(final String name, final int uniqueId, final Collection<TopologyControlAlgorithmParameterId<?>> parameterNames)
   {
      this.name = name;
      this.uniqueId = uniqueId;
      this.parameterNames = parameterNames;
   }

   public String getName()
   {
      return this.name;
   }

   public int getUniqueId()
   {
      return uniqueId;
   }

   /**
    * Returns a subset of the given parameters that is relevant for the configured algorithm
    */
   public TopologyControlAlgorithmParamters extractRelevantParameters(final TopologyControlAlgorithmParamters inputParameters)
   {
      final TopologyControlAlgorithmParamters result = new TopologyControlAlgorithmParamters();
      inputParameters.stream() //
            .filter(parameter -> this.usesParamter(parameter.key))//
            .forEach(parameter -> result.put(parameter));
      return result;
      
   }

   /**
    * Returns true iff this algorithm uses a parameter with the given name
    */
   private boolean usesParamter(final TopologyControlAlgorithmParameterId<?> parameterName)
   {
      return this.parameterNames.contains(parameterName);
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + uniqueId;
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      TopologyControlAlgorithmID other = (TopologyControlAlgorithmID) obj;
      if (name == null)
      {
         if (other.name != null)
            return false;
      } else if (!name.equals(other.name))
         return false;
      if (uniqueId != other.uniqueId)
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return this.name + "(id=" + this.uniqueId + ")";
   }

}
