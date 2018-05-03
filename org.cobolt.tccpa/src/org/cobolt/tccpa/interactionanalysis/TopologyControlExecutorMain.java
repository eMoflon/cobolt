package org.cobolt.tccpa.interactionanalysis;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.cobolt.tccpa.HenshinRules;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;

/**
 * This class starts a separate CPA for each rule pair
 *
 * @author Roland Kluge - Initial implementation
 *
 * @see TopologyControlCriticalPairAnalysisMain
 */
public final class TopologyControlExecutorMain
{

   private static final Logger logger = Logger.getLogger(TopologyControlExecutorMain.class);

   /**
    * Starts the interaction analysis in separate processes
    *
    * This avoids potential memory leaks observed when running numerous CPAs within one process
    *
    * @param args ignored
    */
   public static void main(final String[] args) throws IOException, InterruptedException, URISyntaxException
   {

      final HenshinResourceSet resourceSet = new HenshinResourceSet(HenshinRules.getRulesDirectory());
      final Module rulesModule = resourceSet.getModule("tccpa.henshin", false);
      final List<Rule> rules = HenshinRules.collectAllRules(rulesModule);

      final List<Triple> specifications = new ArrayList<>();
      for (final AnalysisGoal analysisGoal : AnalysisGoal.values())
      {
         for (final Rule ruleLeft : rules)
         {
            for (final Rule ruleRight : rules)
            {
               specifications.add(new Triple(ruleLeft.getName(), ruleRight.getName(), analysisGoal.toString()));
            }
         }
      }

      System.out.println("Starting CPA runs.");
      final int numRuns = specifications.size();
      for (int i = 0; i < numRuns; i++)
      {
         final double progressInPct = 100.0 * i / numRuns;
         final Triple specification = specifications.get(i);
         final String ruleLeftStr = specification.first;
         final String ruleRightStr = specification.second;
         final String analysisGoal = specification.third;
         logger.info(String.format("Starting process %d of %d (%.1f%%) with %s", i, numRuns, progressInPct, specification));
         TopologyControlExecutorMain.exec(TopologyControlCriticalPairAnalysisMain.class, ruleLeftStr, ruleRightStr, analysisGoal);
      }
      System.out.println("All CPA runs finished.");
   }

   /**
    * Starts the given runner class for CPA in a separate process (synchronous)
    * @param runnerClass the runner
    * @param ruleLeftStr the LHS rule
    * @param ruleRightStr the RHS rule
    * @param analysisGoal the analysis goal
    * @return exit code of the processs
    * @throws IOException
    * @throws InterruptedException
    */
   // Solution from https://stackoverflow.com/a/723914
   public static int exec(final Class<? extends TopologyControlCriticalPairAnalysisMain> runnerClass, String ruleLeftStr, String ruleRightStr,
         String analysisGoal) throws IOException, InterruptedException
   {
      final String memoryConfiguration = "-Xmx6000m";
      final String javaHome = System.getProperty("java.home");
      final String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
      final String classpath = System.getProperty("java.class.path");
      final String className = runnerClass.getCanonicalName();

      final ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, memoryConfiguration, className, ruleLeftStr, ruleRightStr, analysisGoal);
      builder.inheritIO();

      final Process process = builder.start();
      process.waitFor();
      return process.exitValue();
   }

   /**
    * Immutable data container holding three strings
    *
    * @author Roland Kluge - Initial implementation
    */
   private static class Triple
   {
      private final String first;

      private final String second;

      private final String third;

      public Triple(final String first, final String second, final String third)
      {
         this.first = first;
         this.second = second;
         this.third = third;
      }

      @Override
      public String toString()
      {
         return "(" + first + ", " + second + ", " + third + ")";
      }

   }
}
