package org.cobolt.tccpa.interactiongraph;

public class Interaction
{
   private final InteractionType interactionType;

   private final InteractionCategory interactionCategory;

   private final String lhsRule;

   private final String rhsRule;

   private final String reason;

   private Interaction(final String lhsRule, final String rhsRule, final InteractionType interactionType, final InteractionCategory interactionCategory,
         final String reason)
   {
      this.lhsRule = lhsRule;
      this.rhsRule = rhsRule;
      this.interactionType = interactionType;
      this.interactionCategory = interactionCategory;
      this.reason = reason;
   }

   public static Interaction create(final String lhsRule, final String rhsRule, final String interactionSpecification, final String reason)
   {
      return new Interaction(lhsRule, rhsRule, InteractionType.fromString(interactionSpecification.charAt(0)),
            InteractionCategory.fromString(interactionSpecification.charAt(1)), reason);
   }

   public String getLhsRule()
   {
      return lhsRule;
   }

   public String getRhsRule()
   {
      return rhsRule;
   }

   public InteractionCategory getCategory()
   {
      return interactionCategory;
   }

   public InteractionType getType()
   {
      return interactionType;
   }

   public boolean isLocal()
   {
      switch (this.getCategory())
      {
      case LOCAL:
      case SAME_MATCH:
         return true;
      case REMOTE:
         return false;
      default:
         throw new IllegalArgumentException("Cannot decide on locality for " + this.getCategory());
      }
   }

   public String getReason()
   {
      return reason;
   }

   public String formatType()
   {
      return String.format("%s%s", this.getType().getMnemonic(), this.getCategory().getMnemonic());
   }

   public String format()
   {
      return getLhsRule() + " --" + formatType() + "--> " + getRhsRule();
   }

   @Override
   public String toString()
   {
      return format() + " (Reason: " + getReason() + ")";
   }

}