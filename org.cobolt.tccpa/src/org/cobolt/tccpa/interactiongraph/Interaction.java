package org.cobolt.tccpa.interactiongraph;

public enum Interaction {
   CM(InteractionType.CONFLICT, InteractionCategory.SAME_MATCH), CS(InteractionType.CONFLICT, InteractionCategory.LOCAL), CR(InteractionType.CONFLICT,
         InteractionCategory.REMOTE), DM(InteractionType.DEPENDENCY, InteractionCategory.SAME_MATCH), DS(InteractionType.DEPENDENCY,
               InteractionCategory.LOCAL), DR(InteractionType.DEPENDENCY, InteractionCategory.REMOTE);

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

   public static Interaction fromString(final String interactionStr)
   {
      for (final Interaction interaction : Interaction.values())
      {
         if (interaction.format().toLowerCase().equals(interactionStr))
            return interaction;
      }
   
      throw new IllegalArgumentException("Cannot find Interaction for string '"  + interactionStr + "'");
   }

   @Override
   public String toString()
   {
      return format();
   }
}