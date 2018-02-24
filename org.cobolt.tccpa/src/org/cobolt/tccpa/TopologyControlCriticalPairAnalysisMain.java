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
import java.util.List;
import java.util.stream.Collectors;

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

         List<Rule> rules = module.getUnits().stream().filter(unit -> unit instanceof Rule).map(unit -> (Rule) unit).collect(Collectors.toList());
         ICriticalPairAnalysis cpa = new CpaByAGG();
         final CPAOptions options = new CPAOptions();
         options.setComplete(true);
         options.setIgnore(false);
         options.setReduceSameRuleAndSameMatch(true);
         options.persist(resultsPath + "/.cpa.options");
         cpa.init(rules, options);
         final CPAResult dependencies = cpa.runDependencyAnalysis();
         final CPAResult conflicts = cpa.runConflictAnalysis();
         final CPAResult jointCpaResult = new CPAResult();
         dependencies.getCriticalPairs().forEach(pair -> jointCpaResult.addResult(pair));
         conflicts.getCriticalPairs().forEach(pair -> jointCpaResult.addResult(pair));

         CPAUtility.persistCpaResult(jointCpaResult, resultsPath);
         final long endTimeMillis = System.currentTimeMillis();
         System.out.printf("Saved %d critical pairs after %dms\n", jointCpaResult.getCriticalPairs().size(), (endTimeMillis - startTimeMillis));

      } catch (UnsupportedRuleException e)
      {
         e.printStackTrace();
      }

   }

   public static void main(String[] args)
   {
      run(PATH, true); // we assume the working directory is the root of the examples plug-in
   }
}
