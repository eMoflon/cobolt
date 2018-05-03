package org.cobolt.tccpa.henshintocsv;

import java.io.File;
import java.io.FilenameFilter;

class HenshinInteractionFolderFilenameFilter implements FilenameFilter
{

   /**
    * Group 1: Per-pair counter
    * Group 2: Reason (e.g., produce-use)
    * Group 3: Type (e.g., conflict)
    */
   static final String REGEX = "^[(](\\d+)[)][\\s](.*)-(conflict|dependency)$";

   @Override
   public boolean accept(File dir, String name)
   {
      return name.matches(REGEX);
   }

}