package de.tudarmstadt.maki.simonstrator.tc.reconfiguration.analysis;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.json.JSONArray;

import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.tc.component.TopologyControlComponent;
import de.tudarmstadt.maki.simonstrator.tc.io.CSVLineSpecification;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.TopologyControlInformationStoreComponent;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.MetricUtils;

public class ReconfigurationPerformanceReportingHelper
{

   public static void writeReconfigurationStatististic(final TopologyControlComponent tcc, final File outputFile)
   {
      writeReconfigurationStatististic(tcc, outputFile, null);
   }

   public static void writeReconfigurationStatististic(final TopologyControlComponent topologyControlComponent, final File outputFile, Writer out)
   {
      final CSVLineSpecification csvHeader = getReconfigurationStatisticsCsvHeader();
      final CSVLineSpecification csvDataLine = getReconfigurationStatisticsCsvData(topologyControlComponent, csvHeader);
      if (out != null)
      {
         final JSONArray jsonData = csvDataLine.convertToJsonArray(csvHeader);
         try
         {
            out.write("Reconfiguration statistics\n");
            out.write(jsonData.toString(2));
            out.write('\n');
            out.flush();
         } catch (final IOException e)
         {
            e.printStackTrace();
         }
      }

      try
      {
         if (!outputFile.exists())
         {
            FileUtils.writeLines(outputFile, Arrays.asList(csvHeader.format()));
         }

         FileUtils.writeLines(outputFile, Arrays.asList(csvDataLine.format()), true);
         Monitor.log(ReconfigurationPerformanceReportingHelper.class, Level.INFO, "Reconfiguration statistics written to %s", outputFile);
      } catch (final IOException e)
      {
         e.printStackTrace();
      }
   }

   private static CSVLineSpecification getReconfigurationStatisticsCsvHeader()
   {

      final List<String> defaultEntries = Arrays.asList(//
            "simulationConfigurationNumber", //
            "simulationIteration", //
            "trainingSetFraction", //
            "trainingSetSeed", //
            "trainingSetNfp", //
            "adaptationLogicInitializationInMillis", //
            "meanMapeDurationInMillis", //
            "meanPlanningDurationInMillis", //
            "meanSystemRuntimeInMillis" //
      );
      final CSVLineSpecification result = new CSVLineSpecification(defaultEntries.size());
      defaultEntries.forEach(entry -> result.addSpecification("%s", entry));
      return result;

   }

   private static CSVLineSpecification getReconfigurationStatisticsCsvData(final TopologyControlComponent tcc, final CSVLineSpecification csvHeader)
   {
      final TopologyControlInformationStoreComponent informationStore = tcc.getInformationStore();
      final CSVLineSpecification lineSpecification = new CSVLineSpecification(csvHeader.getExpectedLength());
      lineSpecification.addSpecification("%d", getConfigurationNumber(tcc));
      lineSpecification.addSpecification("%d", getIterationNumber(tcc));
      lineSpecification.addSpecification("%.2f", getReconfigurationTrainingSetFraction(tcc));
      lineSpecification.addSpecification("%d", getReconfigurationTrainingSetSeed(tcc));
      lineSpecification.addSpecification("%s", getReconfigurationTrainingSetNonfunctionalProperty(tcc));
      lineSpecification.addSpecification("%.1f", evaluateAdaptationLogicInitializationInMillis(informationStore));
      lineSpecification.addSpecification("%.1f", evaluateMeanMapeDurationInMillis(informationStore));
      lineSpecification.addSpecification("%.1f", evaluateMeanPlanningDurationInMillis(informationStore));
      lineSpecification.addSpecification("%.1f", evaluateMeanMapeIntermediateDurationInMillis(informationStore));
      return lineSpecification;
   }

   /**
    * Returns the number of the current iteration
    * @param tcc the {@link TopologyControlComponent}
    * @return the iteration number
    */
   private static int getIterationNumber(final TopologyControlComponent tcc)
   {
      return tcc.getStatisticsHelper().getIterationCounter();
   }

   /**
    * @param tcc the {@link TopologyControlComponent}
    * @return training set fraction or {@link Double#NaN}
    */
   public static double getReconfigurationTrainingSetFraction(final TopologyControlComponent tcc)
   {
      final String filename = SplConquerorHelper.getSplConquerorConfigurationOption(tcc);
      final Optional<Double> result = SplConquerorHelper.extractFraction(filename);
      return result.orElse(Double.NaN);
   }

   /**
    * @param tcc the {@link TopologyControlComponent}
    * @return the training set splitter seed or {@link Integer#MIN_VALUE}
    */
   public static int getReconfigurationTrainingSetSeed(final TopologyControlComponent tcc)
   {
      final String filename = SplConquerorHelper.getSplConquerorConfigurationOption(tcc);
      final Optional<Integer> result = SplConquerorHelper.extractSeed(filename);
      return result.orElse(Integer.MIN_VALUE);
   }

   /**
    * @param tcc the {@link TopologyControlComponent}
    * @return NFP name or 'n/a'
    */
   public static String getReconfigurationTrainingSetNonfunctionalProperty(final TopologyControlComponent tcc)
   {
      final String filename = SplConquerorHelper.getSplConquerorConfigurationOption(tcc);
      final Optional<String> matcher = SplConquerorHelper.extractNonfunctionalProperty(filename);
      return matcher.orElse("n/a");
   }

   private static double evaluateAdaptationLogicInitializationInMillis(TopologyControlInformationStoreComponent informationStore)
   {
      final DescriptiveStatistics stats = MetricUtils
            .getStatisticsOfOverallMetrics(informationStore.getMetricsByName("AdaptationLogicInitializationDurationMetric"));
      return stats.getMean() / 1000;
   }

   private static double evaluateMeanMapeDurationInMillis(TopologyControlInformationStoreComponent informationStore)
   {
      final DescriptiveStatistics stats = MetricUtils.getStatisticsOfOverallMetrics(informationStore.getMetricsByName("AdaptationLogicMapeLoopDurationMetric"));
      return stats.getMean() / 1000;
   }

   static double evaluateMeanPlanningDurationInMillis(TopologyControlInformationStoreComponent informationStore)
   {
      final DescriptiveStatistics stats = MetricUtils.getStatisticsOfOverallMetrics(informationStore.getMetricsByName("AdaptationLogicPlanningDurationMetric"));
      return stats.getMean() / 1000;
   }

   static double evaluateMeanMapeIntermediateDurationInMillis(TopologyControlInformationStoreComponent informationStore)
   {
      final DescriptiveStatistics stats = MetricUtils
            .getStatisticsOfOverallMetrics(informationStore.getMetricsByName("AdaptationLogicIntermediateDurationMetric"));
      return stats.getMean() / 1000;
   }

   public static int getConfigurationNumber(final TopologyControlComponent topologyControlComponent)
   {
      return topologyControlComponent.getConfiguration().configurationNumber;
   }
}
