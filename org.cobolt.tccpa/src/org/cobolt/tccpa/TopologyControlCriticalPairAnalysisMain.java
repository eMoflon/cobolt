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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.henshin.cpa.CPAOptions;
import org.eclipse.emf.henshin.cpa.CPAUtility;
import org.eclipse.emf.henshin.cpa.CpaByAGG;
import org.eclipse.emf.henshin.cpa.ICriticalPairAnalysis;
import org.eclipse.emf.henshin.cpa.result.CPAResult;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.model.resource.HenshinResourceFactory;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;

public class TopologyControlCriticalPairAnalysisMain {
	private static final String STATISTICS_FILE_NAME = "statistics.csv";

	static final Logger logger = Logger.getLogger(TopologyControlCriticalPairAnalysisMain.class);

	/**
	 * Project-relative path to the folder containing models and metamodels
	 */
	private static final String PATH = "src/org/cobolt/tccpa";

	private static final String CSV_SEP = ";";

	/**
	 * Runs some tests with the specified rules
	 * 
	 * @param path
	 * @param saveResult
	 */
	public static void run(String path, boolean saveResult) {
		// Create a resource set with a base directory:
		HenshinResourceSet resourceSet = new HenshinResourceSet(path);

		// Load the module:
		Module module = resourceSet.getModule("tccpa.henshin", false);

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("henshin", new HenshinResourceFactory());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("henshinCp", new HenshinResourceFactory());
		final File resultDir = new File("output");
		resultDir.mkdirs();
		final String resultsPath = resultDir.getAbsolutePath();

		final List<Rule> rules = module.getUnits().stream().filter(unit -> unit instanceof Rule)
				.map(unit -> (Rule) unit).collect(Collectors.toList());
		logger.info("Start CPA.");
		for (final String analysisGoal : Arrays.asList("C", "D")) {
			final List<Rule> rulesLeft = rules;
			final List<Rule> rulesRight = rules;
			for (final Rule ruleLeft : rulesLeft) {
				for (final Rule ruleRight : rulesRight) {
					try {
						final long startTimeMillis = System.currentTimeMillis();
						final ICriticalPairAnalysis cpa = new CpaByAGG();

						final CPAOptions options = new CPAOptions();
						options.setComplete(true);
						options.setIgnore(false);
						options.setReduceSameRuleAndSameMatch(true);
						options.persist(resultsPath + "/.cpa.options");

						cpa.init(Arrays.asList(ruleLeft), Arrays.asList(ruleRight), options);

						final CPAResult result;
						switch (analysisGoal) {
						case "D":
							result = cpa.runDependencyAnalysis();
							break;
						case "C":
							result = cpa.runConflictAnalysis();
							break;
						default:
							throw new IllegalStateException(analysisGoal);
						}
						CPAUtility.persistCpaResult(result, resultsPath);
						final long endTimeMillis = System.currentTimeMillis();
						final long durationMillis = endTimeMillis - startTimeMillis;
						final int numPairs = result.getCriticalPairs().size();
						final String rightName = ruleRight.getName();
						final String leftName = ruleLeft.getName();
						System.out.printf("%s: Saved %d critical pairs of (%s,%s) after %dms\n", analysisGoal, numPairs,
								leftName, rightName, durationMillis);
						final String header = StringUtils.join(
								Arrays.asList("InteractionType", "RuleLeft", "RuleRight", "NumPairs", "DurationMillis"),
								CSV_SEP);
						final File statisticsFile = new File(resultDir, STATISTICS_FILE_NAME);
						if (!statisticsFile.exists()) {
							FileUtils.writeLines(statisticsFile, Arrays.asList(header));
						}
						final String dataLine = StringUtils.join(
								Arrays.asList(analysisGoal, leftName, rightName, numPairs, durationMillis), CSV_SEP);
						FileUtils.writeLines(statisticsFile, Arrays.asList(dataLine), true);

					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

	public static void main(String[] args) {
		run(PATH, true); // we assume the working directory is the root of the plug-in
	}
}
