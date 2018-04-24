package org.cobolt.tccpa.interactiongraph;

public enum Interaction {
   CM(InteractionType.CONFLICT, InteractionCategory.SAME_MATCH), CS(InteractionType.CONFLICT, InteractionCategory.SELF), CR(InteractionType.CONFLICT,
         InteractionCategory.REMOTE), DM(InteractionType.DEPENDENCY, InteractionCategory.SAME_MATCH), DS(InteractionType.DEPENDENCY,
               InteractionCategory.SELF), DR(InteractionType.DEPENDENCY, InteractionCategory.REMOTE);

   public static final Interaction[] NONE = new Interaction[] {};

   private InteractionType interactionType;

   private InteractionCategory interactionCategory;

   private Interaction(final InteractionType interactionType, final InteractionCategory interactionCategory)
   {
      this.interactionType = interactionType;
      this.interactionCategory = interactionCategory;
   }

   public InteractionCategory getInteractionCategory()
   {
      return interactionCategory;
   }

   public InteractionType getInteractionType()
   {
      return interactionType;
   }

   public String format()
   {
      return String.format("%s%s", this.getInteractionType().getMnemonic(), this.getInteractionCategory().getMnemonic());
   }

   @Override
   public String toString()
   {
      return format();
   }
}