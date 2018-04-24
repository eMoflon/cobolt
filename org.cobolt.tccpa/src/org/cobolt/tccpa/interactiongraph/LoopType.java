package org.cobolt.tccpa.interactiongraph;

public enum LoopType {
   SUCCESS("[S]"), FAILURE("[F]");

   private String mnemonic;

   private LoopType(final String mnemonic)
   {
      this.mnemonic = mnemonic;
   }

   @Override
   public String toString()
   {
      return this.mnemonic;
   }
}