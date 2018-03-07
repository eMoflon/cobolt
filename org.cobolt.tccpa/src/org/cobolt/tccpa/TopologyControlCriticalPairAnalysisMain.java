/**
 * <copyright>
 * Copyright (c) 2010-2016 Henshin developers. All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * </copyright>
 */
package org.cobolt.tccpa;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
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

public class TopologyControlCriticalPairAnalysisMain
{
   private static final Logger logger = Logger.getLogger(TopologyControlCriticalPairAnalysisMain.class);

   /**
    * Project-relative path to the folder containing models and metamodels
    */
   private static final String PATH = "src/org/cobolt/tccpa";

   /**
    * Runs some tests with the specified rules
    * @param path
    * @param saveResult
    */
   public static void run(String path, boolean saveResult)
   {
      final long startTimeMillis = System.currentTimeMillis();
      // Create a resource set with a base directory:
      HenshinResourceSet resourceSet = new HenshinResourceSet(path);

      // Load the module:
      Module module = resourceSet.getModule("tccpa.henshin", false);

      try
      {
         Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("henshin", new HenshinResourceFactory());
         Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
         Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("henshinCp", new HenshinResourceFactory());
         final File resultDir = new File("output");
         resultDir.mkdirs();
         final String resultsPath = resultDir.getAbsolutePath();

         final List<Rule> rules = module.getUnits().stream().filter(unit -> unit instanceof Rule).map(unit -> (Rule) unit).collect(Collectors.toList());
         final CPAResult jointCpaResult = new CPAResult();
         for (final String analysisGoal : Arrays.asList("C"))
         {
            final ICriticalPairAnalysis cpa = new CpaByAGG();

            final CPAOptions options = new CPAOptions();
            options.setComplete(true);
            options.setIgnore(false);
            options.setReduceSameRuleAndSameMatch(false);
            options.persist(resultsPath + "/.cpa.options");

            final List<Rule> rulesLeft = rules;
            final List<Rule> rulesRight = rules;
            cpa.init(rulesLeft, rulesRight, options);

            switch(analysisGoal)
            {
            case "D":
               final CPAResult dependencies = cpa.runDependencyAnalysis(new CPAProgressMonitor(analysisGoal));
               dependencies.getCriticalPairs().forEach(pair -> jointCpaResult.addResult(pair));
               break;
            case "C":
               final CPAResult conflicts = cpa.runConflictAnalysis(new CPAProgressMonitor(analysisGoal));
               conflicts.getCriticalPairs().forEach(pair -> jointCpaResult.addResult(pair));
               break;
            }
         }

         CPAUtility.persistCpaResult(jointCpaResult, resultsPath);
         final long endTimeMillis = System.currentTimeMillis();
         System.out.printf("Saved %d critical pairs after %dms\n", jointCpaResult.getCriticalPairs().size(), (endTimeMillis - startTimeMillis));

      } catch (final UnsupportedRuleException e)
      {
         e.printStackTrace();
      }

   }

   public static void main(String[] args)
   {
      run(PATH, true); // we assume the working directory is the root of the examples plug-in
   }

   private static final class CPAProgressMonitor implements IProgressMonitor
   {
      private final String processName;

      private String taskName = "";

      private String subtaskName = "";

      private int workDone;

      private boolean canceled;

      public CPAProgressMonitor(final String taskName)
      {
         this.processName = taskName;
      }

      @Override
      public void worked(int work)
      {
         this.workDone += work;
         logger.info(String.format("%s Progress: %d work units done", this.formatTask(), this.workDone));
      }

      private String formatTask()
      {
         final StringBuilder sb = new StringBuilder();
         sb.append(processName);
         if (!this.taskName.isEmpty())
            sb.append("::").append(this.taskName);
         if (!this.subtaskName.isEmpty())
            sb.append("::").append(this.subtaskName);
         return sb.toString();
      }

      @Override
      public void subTask(String name)
      {
         this.subtaskName = name;
      }

      @Override
      public void setTaskName(String name)
      {
         this.taskName = name;
         this.subtaskName = "";
      }

      @Override
      public void setCanceled(boolean value)
      {
         this.canceled = value;
      }

      @Override
      public boolean isCanceled()
      {
         return this.canceled;
      }

      @Override
      public void internalWorked(double work)
      {
         // Nop
      }

      @Override
      public void done()
      {
         System.out.println(this.formatTask() + " completed!");
      }

      @Override
      public void beginTask(String name, int totalWork)
      {
         this.setTaskName(name);
         this.workDone = 0;
      }
   }
}
