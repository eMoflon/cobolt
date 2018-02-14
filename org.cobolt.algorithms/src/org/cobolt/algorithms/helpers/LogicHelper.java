package org.cobolt.algorithms.helpers;

public class LogicHelper
{

   /**
    * Returns the logic implication of the given premise and conclusion variables
    * @param premise 
    * @param conclusion
    * @return premise => conclusion
    */
   public static boolean implies(boolean premise, boolean conclusion)
   {
      return !premise || conclusion;
   }

   /**
    * Implementation of the biimplication operator
    * 
    * The result is (<code>a</code> AND <code>b</code>) OR (!<code>a</code> AND !<code>b</code>) 
    * @param a the premise value
    * @param b the conclusion value
    * @return whether implication holds
    */
   public static boolean biimplies(boolean a, boolean b)
   {
      return a == b;
   }
}
