package org.cobolt.tccpa.interactiongraph;

public class LoopCondition
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
}