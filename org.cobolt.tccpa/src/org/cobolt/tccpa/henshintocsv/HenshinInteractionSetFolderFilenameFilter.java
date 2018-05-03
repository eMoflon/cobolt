package org.cobolt.tccpa.henshintocsv;

import java.io.File;
import java.io.FilenameFilter;

class HenshinInteractionSetFolderFilenameFilter implements FilenameFilter
{
   /**
    * Group 1: LHS rule name
    * Group 2: RHS rule name
    */
   static final String REGEX = "^(.*)_AND_(.*)$";

   @Override
   public boolean accept(File dir, String name)
   {
      return name.matches(REGEX);
   }

}