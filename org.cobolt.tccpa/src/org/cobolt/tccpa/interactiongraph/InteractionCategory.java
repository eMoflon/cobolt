package org.cobolt.tccpa.interactiongraph;

public enum InteractionCategory {
   SAME_MATCH("m"), LOCAL("l"), REMOTE("r");
   private String mnemonic;

   private InteractionCategory(final String mnemonic)
   {
      this.mnemonic = mnemonic;
   }

   public String getMnemonic()
   {
      return mnemonic;
   }

}