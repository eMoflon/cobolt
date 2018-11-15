package de.tudarmstadt.maki.simonstrator.tc.facade;

public class TopologyControlAlgorithmParameterId<T>
{
   private final String name;
   private final Class<T> type;
   
   public TopologyControlAlgorithmParameterId(final String name, final Class<T> type)
   {
      this.name = name;
      this.type = type;
   }
   
   public String getName()
   {
      return name;
   }
   
   public Class<T> getType()
   {
      return type;
   }
   
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((type == null) ? 0 : type.hashCode());
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
      final TopologyControlAlgorithmParameterId<?> other = (TopologyControlAlgorithmParameterId<?>) obj;
      if (name == null)
      {
         if (other.name != null)
            return false;
      } else if (!name.equals(other.name))
         return false;
      if (type == null)
      {
         if (other.type != null)
            return false;
      } else if (!type.equals(other.type))
         return false;
      return true;
   }



   @Override
   public String toString()
   {
      return String.format("'%s'(type='%s')", this.name, this.type);
   }

}
