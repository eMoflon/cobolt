package org.cobolt.tccpa.stabilizationanalysis;

public class LoopCondition implements Comparable<LoopCondition>
{
   private final String conditionRuleName;

   private final LoopType type;

   public LoopCondition(final String conditionRuleName, final LoopType type)
   {
      this.conditionRuleName = conditionRuleName;
      this.type = type;
   }

   public String getConditionRuleName()
   {
      return conditionRuleName;
   }

   public LoopType getType()
   {
      return type;
   }

   @Override
   public String toString()
   {
      return String.format("%s ==%s==> ...", this.getConditionRuleName(), this.type);
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((conditionRuleName == null) ? 0 : conditionRuleName.hashCode());
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
      LoopCondition other = (LoopCondition) obj;
      if (conditionRuleName == null)
      {
         if (other.conditionRuleName != null)
            return false;
      } else if (!conditionRuleName.equals(other.conditionRuleName))
         return false;
      if (type != other.type)
         return false;
      return true;
   }

   /**
    * Compares {@link LoopCondition}s based on their rule name
    */
   @Override
   public int compareTo(final LoopCondition other)
   {
      return this.getConditionRuleName().compareTo(other.getConditionRuleName());
   }


}