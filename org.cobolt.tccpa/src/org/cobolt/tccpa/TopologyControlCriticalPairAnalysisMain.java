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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.interpreter.UnitApplication;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.interpreter.impl.EngineImpl;
import org.eclipse.emf.henshin.interpreter.impl.UnitApplicationImpl;
import org.eclipse.emf.henshin.model.Module;
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

      // Create a resource set with a base directory:
      HenshinResourceSet resourceSet = new HenshinResourceSet(path);

      // Load the module:
      Module module = resourceSet.getModule("tccpa.henshin", false);

      // Load the example model into an EGraph:
      EGraph graph = new EGraphImpl(resourceSet.getResource("topology-input.xmi"));
      System.out.println(graph);
      final EObject topology = graph.getRoots().get(0);
      topology.eContents().forEach(o -> {
         System.out.println(o);
      });

      // Create an engine and a rule application:
      Engine engine = new EngineImpl();
      {
         UnitApplication removeLink = new UnitApplicationImpl(engine);
         removeLink.setEGraph(graph);
         removeLink.setUnit(module.getUnit("removeLink"));
         removeLink.setParameterValue("linkId", "e12");
         removeLink.setParameterValue("topology", topology);
         if (!removeLink.execute(null))
         {
            throw new RuntimeException("");
         }
      }
      {
         UnitApplication addLink = new UnitApplicationImpl(engine);
         addLink.setEGraph(graph);
         addLink.setUnit(module.getUnit("addLink"));
         addLink.setParameterValue("srcId", "n2");
         addLink.setParameterValue("trgId", "n1");
         addLink.setParameterValue("linkId", "e21");
         addLink.setParameterValue("weight", 3.0);
         addLink.setParameterValue("topology", topology);
         if (!addLink.execute(null))
         {
            throw new RuntimeException("");
         }
      }

      if (saveResult)
      {
         resourceSet.saveEObject(topology, "topology-output.xmi");
         System.out.println("Saved result file.");
      }
   }

   public static void main(String[] args)
   {
      run(PATH, true); // we assume the working directory is the root of the examples plug-in
   }

}
