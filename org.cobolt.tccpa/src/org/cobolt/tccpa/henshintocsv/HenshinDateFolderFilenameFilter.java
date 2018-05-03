package org.cobolt.tccpa.henshintocsv;

import java.io.File;
import java.io.FilenameFilter;

final class HenshinDateFolderFilenameFilter implements FilenameFilter
{
   private static final String REGEX = "^\\d+.\\d+\\.\\d+[-]\\d+$";

   @Override
   public boolean accept(File dir, String name)
   {
      return name.matches(REGEX);
   }
}