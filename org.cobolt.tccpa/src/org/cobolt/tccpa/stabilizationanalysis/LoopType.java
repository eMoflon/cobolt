package org.cobolt.tccpa.stabilizationanalysis;

public enum LoopType {
   SUCCESS("[S]"), FAILURE("[F]");

   private String mnemonic;

   private LoopType(final String mnemonic)
   {
      this.mnemonic = mnemonic;
   }

   /**
    * Returns the required {@link InteractionType} that may cause a continuation of a loop of this type
    * @return the appropriate {@link InteractionType}
    */
   public InteractionType getContinuationInteractionType()
   {
      switch (this)
      {
      case SUCCESS:
         return InteractionType.DEPENDENCY;
      case FAILURE:
         return InteractionType.CONFLICT;
      default:
         throw new IllegalArgumentException("Cannot handle: " + this);
      }
   }

   @Override
   public String toString()
   {
      return this.mnemonic;
   }
}