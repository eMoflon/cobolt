package org.cobolt.tccpa.interactiongraph;

public enum InteractionType {
   CONFLICT("c"), DEPENDENCY("d");

   private String mnemonic;

   private InteractionType(final String mnemonic)
   {
      this.mnemonic = mnemonic;
   }

   public String getMnemonic()
   {
      return mnemonic;
   }

   public String format() {
      return this.getMnemonic();
   }

   public static InteractionType fromString(final char mnemonic)
   {
      for (final InteractionType interactionType : values())
      {
         if (interactionType.getMnemonic().equals(Character.toString(mnemonic)))
            return interactionType;
      }
      throw new IllegalArgumentException("Cannot parse '" + mnemonic + "'.");
   }
}