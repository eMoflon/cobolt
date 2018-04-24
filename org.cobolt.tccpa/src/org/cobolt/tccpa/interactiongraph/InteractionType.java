package org.cobolt.tccpa.interactiongraph;

public enum InteractionType {
   CONFLICT("C"), DEPENDENCY("D");

   private String mnemonic;

   private InteractionType(final String mnemonic)
   {
      this.mnemonic = mnemonic;
   }

   public String getMnemonic()
   {
      return mnemonic;
   }
}