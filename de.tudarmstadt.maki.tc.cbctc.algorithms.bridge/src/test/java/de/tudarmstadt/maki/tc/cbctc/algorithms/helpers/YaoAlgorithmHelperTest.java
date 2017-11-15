package de.tudarmstadt.maki.tc.cbctc.algorithms.helpers;

import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.maki.tc.cbctc.algorithms.helpers.YaoAlgorithmHelper;

/**
 * Unit tests for {@link YaoAlgorithmHelper}
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class YaoAlgorithmHelperTest
{
   
   @Test
   public void testCheckPredicate_DifferentCones() throws Exception
   {
      Assert.assertFalse(YaoAlgorithmHelper.checkPredicate(0, 180, 2));
      Assert.assertFalse(YaoAlgorithmHelper.checkPredicate(0, 180, 4));
      Assert.assertFalse(YaoAlgorithmHelper.checkPredicate(0, 180+360, 4));
   }
   
   @Test
   public void testCheckPredicate_SameCone() throws Exception
   {
      Assert.assertTrue(YaoAlgorithmHelper.checkPredicate(0, 179.99, 2));
      Assert.assertTrue(YaoAlgorithmHelper.checkPredicate(0, 89.99, 4));
      Assert.assertTrue(YaoAlgorithmHelper.checkPredicate(270, 315, 4));
      Assert.assertTrue(YaoAlgorithmHelper.checkPredicate(0, 360, 4));
   }
}
