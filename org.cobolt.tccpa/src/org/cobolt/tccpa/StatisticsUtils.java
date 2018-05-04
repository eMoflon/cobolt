package org.cobolt.tccpa;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.cobolt.tccpa.interactionanalysis.AnalysisGoal;
import org.cobolt.tccpa.interactionanalysis.TopologyControlCriticalPairAnalysisMain;
import org.eclipse.emf.henshin.cpa.result.CPAResult;
import org.eclipse.emf.henshin.model.Rule;

/**
 * Utility class for I/O of statistics
 * @author Roland Kluge - Initial implementation
 */
public final class StatisticsUtils
{

   /**
    * Target file basename
    */
   private static final String STATISTICS_FILE_NAME = "statistics.csv";

   private StatisticsUtils()
   {
      throw new UnsupportedOperationException("Utility class");
   }

   /**
    * Create a statistics file and add header
    * @param resultsDirectory parent directory of file
    * @return the initialized file
    * @throws IOException if writing fails
    */
   public static File initializeStatisticsFile(final File resultsDirectory) throws IOException
   {
      final List<String> headerEntries = Arrays.asList("InteractionType", "RuleLeft", "RuleRight", "NumPairs", "DurationMillis");
      final String header = CsvUtils.formatCsvLine(headerEntries);
      final File statisticsFile = new File(resultsDirectory, StatisticsUtils.STATISTICS_FILE_NAME);
      if (!statisticsFile.exists())
      {
         FileUtils.writeLines(statisticsFile, Arrays.asList(header));
      }
      return statisticsFile;
   }

   /**
    * Write a CSV line into the given statistics file
    * @param ruleLeft name of left rule
    * @param ruleRight name of right rule
    * @param analysisGoal analysis goal
    * @param result CPA result
    * @param durationMillis duration of CPA
    * @param statisticsFile target file
    * @throws IOException if writing fails
    */
   public static void writeStatisticsLine(final Rule ruleLeft, final Rule ruleRight, final AnalysisGoal analysisGoal, final CPAResult result,
         final long durationMillis, final File statisticsFile) throws IOException
   {
      final int numPairs = result.getCriticalPairs().size();
      final String rightName = ruleRight.getName();
      final String leftName = ruleLeft.getName();
      TopologyControlCriticalPairAnalysisMain.logger
            .info(String.format("%s %s-%s: Saved %d critical pairs after %dms\n", analysisGoal, leftName, rightName, numPairs, durationMillis));
      final List<String> entries = Arrays.asList(analysisGoal.toString(), leftName, rightName, Integer.toString(numPairs), Long.toString(durationMillis));
      final String csvLine = CsvUtils.formatCsvLine(entries);
      FileUtils.writeLines(statisticsFile, Arrays.asList(csvLine), true);
   }

}
