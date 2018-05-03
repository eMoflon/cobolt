package org.cobolt.tccpa.interactionanalysis;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.cobolt.tccpa.CsvUtils;
import org.cobolt.tccpa.HenshinRules;
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

	public static void main(final String[] args) throws Exception {

   	if (args.length >= 3) {
   		System.out.println("PID of running CPA process: " + getProcessId());

   		final String ruleLeftStr = args[0];
   		final String ruleRightStr = args[1];
   		final String analysisGoalStr = args[2];

   		final Module module = initialize();
   		final Rule ruleLeft = HenshinRules.getRuleByName(module, ruleLeftStr).orElse(null);
   		final Rule ruleRight = HenshinRules.getRuleByName(module, ruleRightStr).orElse(null);
   		final AnalysisGoal analysisGoal = AnalysisGoal.getAnalysisGoalByName(analysisGoalStr)//
   				.orElseThrow(() -> new IllegalArgumentException("Invalid analysis goal: " + analysisGoalStr));

   		runSingleAnalysis(analysisGoal, ruleLeft, ruleRight, -1, -1);

   	} else {
   		runAllAnalyses(); // we assume the working directory is the root of the plug-in
   	}
   }

   /**
	 * Runs some tests with the specified rules
	 */
	private static void runAllAnalyses() {
		final Module module = initialize();

		final List<Rule> rules = HenshinRules.collectAllRules(module);
		logger.info("Start CPA.");
		int currentRun = 0;
		final int totalRunCount = rules.size() * rules.size() * 2;
		for (final AnalysisGoal analysisGoal : AnalysisGoal.values()) {
			final List<Rule> rulesLeft = rules;
			final List<Rule> rulesRight = rules;
			for (final Rule ruleLeft : rulesLeft) {
				for (final Rule ruleRight : rulesRight) {
					++currentRun;
					runSingleAnalysis(analysisGoal, ruleLeft, ruleRight, currentRun, totalRunCount);
				}
			}
		}

	}

   private static void runSingleAnalysis(final AnalysisGoal analysisGoal, final Rule ruleLeft, final Rule ruleRight,
   		final int currentRun, final int totalRunCount) {

   	if (null == ruleLeft)
   		throw new IllegalArgumentException("LHS rule is null.");

   	if (null == ruleRight)
   		throw new IllegalArgumentException("RHS rule is null.");

   	if (null == analysisGoal)
   		throw new IllegalArgumentException("Analysis goal is null.");

   	if (currentRun >= 0) {
   		final double progress = 1.0 * currentRun / totalRunCount * 100;
   		logger.info(String.format("[%03d/%03d] %s %s-%s (Progress: %.1f%%)", currentRun, totalRunCount, analysisGoal,
   				ruleLeft.getName(), ruleRight.getName(), progress));
   	} else {
   		logger.info(String.format("%s %s-%s", analysisGoal, ruleLeft.getName(), ruleRight.getName()));
   	}

   	try {
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

   		cpa.init(Arrays.asList(ruleLeft), Arrays.asList(ruleRight), options);

   		final CPAResult result;
   		switch (analysisGoal) {
   		case DEPENDENCY:
   			result = cpa.runDependencyAnalysis();
   			break;
   		case CONFLICT:
   			result = cpa.runConflictAnalysis();
   			break;
   		default:
   			throw new IllegalStateException(analysisGoal.toString());
   		}
   		if (!result.getCriticalPairs().isEmpty()) {
   			CPAUtility.persistCpaResult(result, resultsPath);
   			copyMetamodelToResultDirectory(rulesDirectory, resultDir);
   		}
   		final long endTimeMillis = System.currentTimeMillis();
   		final long durationMillis = endTimeMillis - startTimeMillis;
   		final int numPairs = result.getCriticalPairs().size();
   		final String rightName = ruleRight.getName();
   		final String leftName = ruleLeft.getName();
   		logger.info(String.format("%s %s-%s: Saved %d critical pairs after %dms\n", analysisGoal, leftName, rightName, numPairs, durationMillis));
   		final String header = StringUtils.join(
   				Arrays.asList("InteractionType", "RuleLeft", "RuleRight", "NumPairs", "DurationMillis"), CsvUtils.CSV_SEP);
   		final File statisticsFile = new File(resultDir, STATISTICS_FILE_NAME);
   		if (!statisticsFile.exists()) {
   			FileUtils.writeLines(statisticsFile, Arrays.asList(header));
   		}
   		final String dataLine = StringUtils
   				.join(Arrays.asList(analysisGoal, leftName, rightName, numPairs, durationMillis), CsvUtils.CSV_SEP);
   		FileUtils.writeLines(statisticsFile, Arrays.asList(dataLine), true);

   	} catch (final Exception e) {
   		e.printStackTrace();
   	}
   }

   /**
    * Initializes the Henshin infrastructure
    * @return
    */
   private static Module initialize() {
		// Create a resource set with a base directory:
		final HenshinResourceSet resourceSet = new HenshinResourceSet(HenshinRules.getRulesDirectory());

		// Load the module:
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

   private static File getResultDirectory() {
		return new File("output");
	}

	private static Path copyMetamodelToResultDirectory(final String path, final File resultDir) throws IOException {
	   final File originalMetamodelFile = new File(path, HenshinRules.getMetamodelFileName());
		return Files.walkFileTree(resultDir.toPath(), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(final Path directory, final BasicFileAttributes attributes)
					throws IOException {

			   final boolean containsConflictMetamodel = containsConflictMetamodel(directory);
				final boolean containsMetamodel = containsMetamodel(directory);

				if (containsConflictMetamodel && !containsMetamodel) {
               final File targetMetamodelFile = new File(directory.toFile(), HenshinRules.getMetamodelFileName());
               FileUtils.copyFile(originalMetamodelFile, targetMetamodelFile);
				}

				return FileVisitResult.CONTINUE;
			}
		});
	}

	private static boolean containsMetamodel(final Path directory) {
		final File[] metamodelFiles = directory.toFile().listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return "tccpa.ecore".equals(name);
			}
		});
		final boolean containsMetamodel = metamodelFiles.length != 0;
		return containsMetamodel;
	}

	private static boolean containsConflictMetamodel(final Path directory) {
		final File[] minimalEcoreFiles = directory.toFile().listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return "minimal-model.ecore".equals(name);
			}
		});
		final boolean containsConflictMetamodel = minimalEcoreFiles.length != 0;
		return containsConflictMetamodel;
	}

	public static long getProcessId() {
		String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
		return Long.parseLong(processName.split("@")[0]);
	}
}