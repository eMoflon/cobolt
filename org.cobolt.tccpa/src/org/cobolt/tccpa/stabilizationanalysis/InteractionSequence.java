package org.cobolt.tccpa.stabilizationanalysis;

import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Represents a sequence of interactions that may eventually lead to a continuation of the given loop condition
 *
 * @author Roland Kluge - Initial implementation
 */
public class InteractionSequence implements Comparable<InteractionSequence>
{
   private final Deque<Interaction> interactions;

   private final LoopCondition loopCondition;

   /**
    * Creates an interaction sequence of size one that relates to the given loop condition
    *
    * The condition rule name must match the RHS rule of the continuation interaction
    *
    * @param continuationInteraction the continuation interaction
    * @param loopCondition the loop condition
    */
   public InteractionSequence(final Interaction continuationInteraction, final LoopCondition loopCondition)
   {
      if (!loopCondition.getConditionRuleName().equals(continuationInteraction.getRhsRule()))
         throw new IllegalArgumentException(
               String.format("Mismatch of continuation interaction '%s' and loop condition '%s'", continuationInteraction, loopCondition));

      this.interactions = new LinkedList<>();
      this.interactions.add(continuationInteraction);
      this.loopCondition = loopCondition;
   }

   /**
    * Prepends the given interaction to the sequence.
    *
    * The RHS rule of the given interaction must match the LHS rule of the current first element of the sequence
    * @param interaction the interaction to add
    */
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

   /**
    * Returns the continuation interaction at the end of this interaction sequence
    */
   public Interaction getContinuationInteraction()
   {
      return this.interactions.getLast();
   }

   /**
    * Returns the loop condition at the end of this interaction sequence
    */
   public LoopCondition getLoopCondition()
   {
      return loopCondition;
   }

   /**
    * Returns an ordered view of the stored interactions
    * @return view of the interactions of this sequence
    */
   public Iterable<Interaction> getInteractions()
   {
      return Collections.unmodifiableCollection(this.interactions);
   }

   /**
    * Returns a compact string representation of this interaction sequence
    */
   public String format()
   {
      final StringBuilder sb = new StringBuilder();
      final Iterator<Interaction> iter = this.interactions.iterator();
      while (iter.hasNext())
      {
         final Interaction interaction = iter.next();
         sb.append("(").append(interaction.getLhsRule()).append(")  ---[").append(interaction.formatTypeAndLocality()).append("]--->  ");
         if (!iter.hasNext())
         {
            sb.append("(").append(interaction.getRhsRule()).append(")");
         }
      }
      return sb.toString();
   }

   /**
    * Returns true if all interactions are local
    * @see Interaction#isLocal()
    */
   public boolean isPurelyLocal()
   {
      return this.interactions.stream().allMatch(Interaction::isLocal);
   }

   /**
    * Convenience method that negates {@link #containsLocalInteractionsWithProgress()}
    * @return NOT {@link #containsLocalInteractionsWithProgress()}
    */
   public boolean containsNoLocalInteractionsWithProgress()
   {
      return !containsLocalInteractionsWithProgress();
   }

   /**
    * Returns true if at least one interaction is a local interaction whose involved rules are located in differen process regions
    * @return whether a process-region-crossing interaction is contained in this sequence
    */
   public boolean containsLocalInteractionsWithProgress()
   {
      final Iterator<Interaction> iter = this.interactions.descendingIterator();
      while (iter.hasNext())
      {
         final Interaction interaction = iter.next();
         if (interaction.isLocal() && !RuleNames.areInSameProcessRegion(interaction.getLhsRule(), interaction.getRhsRule()))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * Convenience method that negates the result of {@link #containsContextEventRecreationInteraction()}
    * @return NOT #containsContextEventRecreationInteraction
    */
   public boolean containsNoContextEventRecreationInteraction()
   {
      return !containsContextEventRecreationInteraction();
   }

   /**
    * Returns true if this sequence contains a recreation interaction that involves a context event rule.
    * This means that all but the last interaction in the sequence are analyzed
    * @return whether a context event rule is involved in some recreation interaction
    */
   public boolean containsContextEventRecreationInteraction()
   {
      final Iterator<Interaction> iter = this.interactions.descendingIterator();
      // We skip the continuation interaction
      iter.next();
      while (iter.hasNext())
      {
         final Interaction interaction = iter.next();
         final String lhsRule = interaction.getLhsRule();
         final String rhsRule = interaction.getRhsRule();
         if (Arrays.asList(lhsRule, rhsRule).stream().anyMatch(RuleNames::isContextEventRule))
            return true;
      }
      return false;
   }

   /**
    * Inverse lexicographic ordering based on formatted interaction names.
    */
   @Override
   public int compareTo(final InteractionSequence other)
   {
      final Iterator<Interaction> myIter = this.interactions.descendingIterator();
      final Iterator<Interaction> othersIter = other.interactions.descendingIterator();
      while (myIter.hasNext() && othersIter.hasNext())
      {
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
