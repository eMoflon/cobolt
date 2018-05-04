package org.cobolt.tccpa;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for CSV processing
 * @author Roland Kluge - Initial implementation
 *
 */
public final class CsvUtils
{
   private static final String CSV_REPLACEMENT_FOR_SEP = ",";
   private static final String CSV_SEP = ";";

   private CsvUtils()
   {
      throw new UnsupportedOperationException("Utility class");
   }

   /**
    * Replaces all occurrences of the CSV field separator with a similar, but safe character.
    * @param rawString the string to be processed
    * @return the safe version of the string
    */
   public static String makeCsvSafe(String rawString)
   {
      return rawString.replaceAll(Pattern.quote(CSV_SEP), CSV_REPLACEMENT_FOR_SEP);
   }

   /**
    * Joins the given entries using the default CSV separator.
    * @param entries the entries
    * @return the formatted CSV line
    */
   public static String formatCsvLine(final List<String> entries)
   {
      return StringUtils.join(entries, CSV_SEP);
   }
}
