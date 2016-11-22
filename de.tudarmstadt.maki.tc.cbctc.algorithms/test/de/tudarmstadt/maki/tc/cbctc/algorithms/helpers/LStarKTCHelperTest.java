package de.tudarmstadt.maki.tc.cbctc.algorithms.helpers;

import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.maki.tc.cbctc.algorithms.helpers.LStarKTCHelper;


public class LStarKTCHelperTest
{

   @Test
   public void testCheckPredicateNotAllHopCountsSet() throws Exception
   {
      Assert.assertFalse(LStarKTCHelper.checkPredicate(-1, 1, 1, 1.5));
      Assert.assertFalse(LStarKTCHelper.checkPredicate(1, -1, 1, 1.5));
      Assert.assertFalse(LStarKTCHelper.checkPredicate(1, 1, -1, 1.5));
   }
   
   @Test
   public void testCheckPredicate_EqualHopCount() throws Exception
   {
      Assert.assertTrue(LStarKTCHelper.checkPredicate(1, 1, 100, 1.5));
   }
   
   @Test
   public void testCheckPredicate_Node2CloserThanNode1() throws Exception
   {
      // Positive: Path length stretch (1+1)/2 = 1 is < a=3 
      Assert.assertTrue(LStarKTCHelper.checkPredicate(2, 1, 1, 3));
      // Negative: Path length stretch (3+1)/2 = 1 is >= a=2
      Assert.assertFalse(LStarKTCHelper.checkPredicate(2, 1, 3, 2));
   }
   
   @Test
   public void testCheckPredicate_Node1CloserThanNode2() throws Exception
   {
      // Positive: Path length stretch (11+1)/10 = 1 is < a=1.3
      Assert.assertTrue(LStarKTCHelper.checkPredicate(8, 10, 11, 1.3));
      // Negative: Path length stretch (11+1)/10 = 1 is >= a=1.2
      Assert.assertFalse(LStarKTCHelper.checkPredicate(8, 10, 11, 1.2));
   }
}
