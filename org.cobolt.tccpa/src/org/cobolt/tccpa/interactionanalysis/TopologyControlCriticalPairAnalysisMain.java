package org.cobolt.tccpa.interactionanalysis;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.cobolt.tccpa.HenshinRules;
import org.cobolt.tccpa.StatisticsUtils;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.henshin.cpa.CPAOptions;
import org.eclipse.emf.henshin.cpa.CPAUtility;
import org.eclipse.emf.henshin.cpa.CpaByAGG;
import org.eclipse.emf.henshin.cpa.ICriticalPairAnalysis;
import org.eclipse.emf.henshin.cpa.UnsupportedRuleException;
import org.eclipse.emf.henshin.cpa.result.CPAResult;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.model.resource.HenshinResourceFactory;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;

/**
 * Runner class for Henshin CPA
 *
 * @author Roland Kluge - Initial implementation
 */
public class TopologyControlCriticalPairAnalysisMain
{
   public static final Logger logger = Logger.getLogger(TopologyControlCriticalPairAnalysisMain.class);

   /**
    * Runs the CPA either for a triple (left rule name, right rule name, analysis goal) or for all rules as configured in {@link HenshinRules}
    * @param args
    * @throws Exception
    */
   public static void main(final String[] args) throws Exception
   {

      if (args.length >= 3)
      {
         logger.info("PID: " + getProcessId());

         final String ruleLeftStr = args[0];
         final String ruleRightStr = args[1];
         final String analysisGoalStr = args[2];

         final Module module = initialize();
         final Rule ruleLeft = HenshinRules.getRuleByName(module, ruleLeftStr).orElse(null);
         final Rule ruleRight = HenshinRules.getRuleByName(module, ruleRightStr).orElse(null);
         final AnalysisGoal analysisGoal = AnalysisGoal.getAnalysisGoalByName(analysisGoalStr)//
               .orElseThrow(() -> new IllegalArgumentException("Invalid analysis goal: " + analysisGoalStr));

         runSingleAnalysis(analysisGoal, ruleLeft, ruleRight, -1, -1);

      } else
      {
         runAllAnalyses();
      }
   }

   /**
    * Runs some tests with the specified rules
    * @throws IOException if writing results fails
    */
   private static void runAllAnalyses() throws IOException
   {
      final Module module = initialize();

      final List<Rule> rules = HenshinRules.collectAllRules(module);
      logger.info("Start CPA.");
      int currentRun = 0;
      final int totalRunCount = rules.size() * rules.size() * 2;
      for (final AnalysisGoal analysisGoal : AnalysisGoal.values())
      {
         final List<Rule> rulesLeft = rules;
         final List<Rule> rulesRight = rules;
         for (final Rule ruleLeft : rulesLeft)
         {
            for (final Rule ruleRight : rulesRight)
            {
               ++currentRun;
               runSingleAnalysis(analysisGoal, ruleLeft, ruleRight, currentRun, totalRunCount);
            }
         }
      }

   }

   private static void runSingleAnalysis(final AnalysisGoal analysisGoal, final Rule ruleLeft, final Rule ruleRight, final int currentRun,
         final int totalRunCount) throws IOException
   {

      Validate.notNull(ruleLeft, "LHS rule is null.");
      Validate.notNull(ruleRight, "RHS rule is null.");
      Validate.notNull(analysisGoal, "Analysis goal is null.");

      final File statisticsFile = StatisticsUtils.initializeStatisticsFile(getResultDirectory());

      if (currentRun >= 0)
      {
         final double progress = 1.0 * currentRun / totalRunCount * 100;
         logger.info(String.format("[%03d/%03d] %s %s-%s (Progress: %.1f%%)", currentRun, totalRunCount, analysisGoal, ruleLeft.getName(), ruleRight.getName(),
               progress));
      } else
      {
         logger.info(String.format("%s %s-%s", analysisGoal, ruleLeft.getName(), ruleRight.getName()));
      }

      final String rulesDirectory = HenshinRules.getRulesDirectory();
      final File resultDir = getResultDirectory();
      final String resultsPath = resultDir.getAbsolutePath();
      final long startTimeMillis = System.currentTimeMillis();
      final ICriticalPairAnalysis cpa = new CpaByAGG();

      final CPAOptions options = new CPAOptions();
      options.setComplete(true);
      options.setIgnore(false);
      options.setReduceSameRuleAndSameMatch(true);
      options.persist(resultsPath + "/.cpa.options");

      try
      {
         cpa.init(Arrays.asList(ruleLeft), Arrays.asList(ruleRight), options);
      } catch (final UnsupportedRuleException e)
      {
         throw new IllegalArgumentException(e);
      }

      final CPAResult result;
      switch (analysisGoal)
      {
      case DEPENDENCY:
         result = cpa.runDependencyAnalysis();
         break;
      case CONFLICT:
         result = cpa.runConflictAnalysis();
         break;
      default:
         throw new IllegalArgumentException(analysisGoal.toString());
      }

      if (!result.getCriticalPairs().isEmpty())
      {
         CPAUtility.persistCpaResult(result, resultsPath);
         copyMetamodelToResultDirectory(rulesDirectory, resultDir);
      }

      final long endTimeMillis = System.currentTimeMillis();
      final long durationMillis = endTimeMillis - startTimeMillis;
      StatisticsUtils.writeStatisticsLine(ruleLeft, ruleRight, analysisGoal, result, durationMillis, statisticsFile);
   }

   /**
    * Initializes the Henshin infrastructure
    * @return the initialized {@link Module}
    */
   private static Module initialize()
   {
      final HenshinResourceSet resourceSet = new HenshinResourceSet(HenshinRules.getRulesDirectory());

      final Module module = resourceSet.getModule(HenshinRules.getHenshinRulesFilename(), false);

      Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("henshin", new HenshinResourceFactory());
      Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
      Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("henshinCp", new HenshinResourceFactory());
      {
         final File resultDir = getResultDirectory();
         resultDir.mkdirs();
      }
      return module;
   }

   /**
    * Returns the project-relative file of the output directory
    * @return output directory
    */
   private static File getResultDirectory()
   {
      return new File("output");
   }

   /**
    * Traverses the given result directory and copies the metamodel file into each folder representing a concrete interaction.
    *
    * An folder contains an interaction if it contains a conflict metamodel (see {@link HenshinRules#containsConflictMetamodel(Path)}).
    * @param metamodelParentDirectory the path to the directory containing the metamodel to be copied
    * @param resultDirectory the Henshin results root directory to traverse
    * @throws IOException if copying fails
    */
   private static void copyMetamodelToResultDirectory(final String metamodelParentDirectory, final File resultDirectory) throws IOException
   {
      final File originalMetamodelFile = new File(metamodelParentDirectory, HenshinRules.getMetamodelFileName());
      Files.walkFileTree(resultDirectory.toPath(), new SimpleFileVisitor<Path>() {
         @Override
         public FileVisitResult preVisitDirectory(final Path directory, final BasicFileAttributes attributes) throws IOException
         {

            final boolean containsConflictMetamodel = HenshinRules.containsConflictMetamodel(directory);
            final boolean containsMetamodel = HenshinRules.containsMetamodel(directory);

            if (containsConflictMetamodel && !containsMetamodel)
            {
               final File targetMetamodelFile = new File(directory.toFile(), HenshinRules.getMetamodelFileName());
               FileUtils.copyFile(originalMetamodelFile, targetMetamodelFile);
            }

            return FileVisitResult.CONTINUE;
         }
      });
   }

   public static long getProcessId()
   {
      String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
      return Long.parseLong(processName.split("@")[0]);
   }
}