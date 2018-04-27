package org.cobolt.tccpa.interactiongraph;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

public class InteractionSequence implements Comparable<InteractionSequence>
{
   private Deque<Interaction> interactions;

   private LoopCondition loopCondition;

   public InteractionSequence(final Interaction continuationInteraction, final LoopCondition loopCondition)
   {
      if (!loopCondition.getConditionRuleName().equals(continuationInteraction.getRhsRule()))
         throw new IllegalArgumentException(
               String.format("Mismatch of continuation interaction '%s' and loop condition '%s'", continuationInteraction, loopCondition));

      this.interactions = new LinkedList<>();
      this.interactions.add(continuationInteraction);
      this.loopCondition = loopCondition;
   }

   public void extend(final Interaction interaction)
   {
      final Interaction currentFirst = this.interactions.getFirst();
      if (!currentFirst.getLhsRule().equals(interaction.getRhsRule()))
      {
         throw new IllegalArgumentException(String.format("New interaction's RHS rule '%s' does not match current tail's LHS rule '%s'",
               interaction.getRhsRule(), currentFirst.getLhsRule()));
      }

      this.interactions.addFirst(interaction);
   }

   public Interaction getContinuationInteraction()
   {
      return this.interactions.getLast();
   }

   public LoopCondition getLoopCondition()
   {
      return loopCondition;
   }

   public String format()
   {
      final StringBuilder sb = new StringBuilder();
      final Iterator<Interaction> iter = this.interactions.iterator();
      while (iter.hasNext())
      {
         final Interaction interaction = iter.next();
         sb.append("(");
         sb.append(interaction.getLhsRule());
         sb.append(") --[");
         sb.append(interaction.formatType());
         sb.append("]--> ");
         if (!iter.hasNext())
         {
            sb.append("(");
            sb.append(interaction.getRhsRule());
            sb.append(")");
         }
      }
      return sb.toString();
   }

   public boolean isPurelyLocal()
   {
      return this.interactions.stream().allMatch(interaction -> interaction.isLocal());
   }

   /**
    * Inverse lexicographic ordering based on formatted interaction names.
    */
   @Override
   public int compareTo(final InteractionSequence other)
   {
      final Iterator<Interaction> myIter = this.interactions.descendingIterator();
      final Iterator<Interaction> othersIter = other.interactions.descendingIterator();
      while (myIter.hasNext() && othersIter.hasNext()) {
         final Interaction myInteraction = myIter.next();
         final Interaction othersInteraction = othersIter.next();
         final int interactionComparisonResult = myInteraction.format().compareTo(othersInteraction.format());
         if (interactionComparisonResult != 0)
            return interactionComparisonResult;
      }

      if (myIter.hasNext())
         return 1;

      if (othersIter.hasNext())
         return -1;

      return 0;
   }
}
