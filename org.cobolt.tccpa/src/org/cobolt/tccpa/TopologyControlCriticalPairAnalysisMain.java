package org.cobolt.tccpa;

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
import java.util.Optional;
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

	private static final String CSV_SEP = ";";

	private static final String METAMODEL_FILENAME = "tccpa.ecore";

	private enum AnalysisGoal {
		CONFLICT, DEPENDENCY;
	}

	/**
	 * Runs some tests with the specified rules
	 */
	private static void runAllAnalyses() {
		final Module module = initialize();

		// @formatter:off
		final List<Rule> rules = module.getUnits().stream() //
				.filter(unit -> unit instanceof Rule) //
				.map(unit -> (Rule) unit).collect(Collectors.toList());
		// @formatter:on
		logger.info("Start CPA.");
		boolean shallSkip = true;
		final boolean isDryRun = false;
		int currentRun = 0;
		final int totalRunCount = rules.size() * rules.size() * 2;
		for (final AnalysisGoal analysisGoal : AnalysisGoal.values()) {
			final List<Rule> rulesLeft = rules;
			final List<Rule> rulesRight = rules;
			for (final Rule ruleLeft : rulesLeft) {
				for (final Rule ruleRight : rulesRight) {
					++currentRun;

					if (shallSkip && analysisGoal == AnalysisGoal.CONFLICT
							&& ruleLeft.getName().equals("xhandleLinkRemoval2")
							&& ruleRight.getName().equals("xinactivateLink"))
						shallSkip = false;

					if (shallSkip || isDryRun)
						continue;

					runSingleAnalysis(analysisGoal, ruleLeft, ruleRight, currentRun, totalRunCount);
				}
			}
		}

	}

	public static Module initialize() {
		// Create a resource set with a base directory:
		final HenshinResourceSet resourceSet = new HenshinResourceSet(getRulesDirectory());

		// Load the module:
		final Module module = resourceSet.getModule("tccpa.henshin", false);

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("henshin", new HenshinResourceFactory());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("henshinCp", new HenshinResourceFactory());
		{
			final File resultDir = getResultDirectory();
			resultDir.mkdirs();
		}
		return module;
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
			final String rulesDirectory = getRulesDirectory();
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
					Arrays.asList("InteractionType", "RuleLeft", "RuleRight", "NumPairs", "DurationMillis"), CSV_SEP);
			final File statisticsFile = new File(resultDir, STATISTICS_FILE_NAME);
			if (!statisticsFile.exists()) {
				FileUtils.writeLines(statisticsFile, Arrays.asList(header));
			}
			final String dataLine = StringUtils
					.join(Arrays.asList(analysisGoal, leftName, rightName, numPairs, durationMillis), CSV_SEP);
			FileUtils.writeLines(statisticsFile, Arrays.asList(dataLine), true);

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private static String getRulesDirectory() {
		return "src/org/cobolt/tccpa";
	}

	private static File getResultDirectory() {
		return new File("output");
	}

	private static Path copyMetamodelToResultDirectory(final String path, final File resultDir) throws IOException {
		return Files.walkFileTree(resultDir.toPath(), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(final Path directory, final BasicFileAttributes attributes)
					throws IOException {
				final boolean containsConflictMetamodel = containsConflictMetamodel(directory);
				final boolean containsMetamodel = containsMetamodel(directory);
				if (containsConflictMetamodel && !containsMetamodel) {
					FileUtils.copyFile(new File(path, METAMODEL_FILENAME),
							new File(directory.toFile(), METAMODEL_FILENAME));
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}

	public static void main(String[] args) throws Exception {

		if (args.length >= 3) {
			System.out.println("PID of running CPA process: " + getProcessId());
			final String ruleLeftStr = args[0];
			final String ruleRightStr = args[1];
			final String analysisGoalStr = args[2];
			final Module module = initialize();
			final Rule ruleLeft = getRuleByName(module, ruleLeftStr).orElse(null);
			final Rule ruleRight = getRuleByName(module, ruleRightStr).orElse(null);
			final AnalysisGoal analysisGoal = getAnalysisGoalByName(analysisGoalStr)//
					.orElseThrow(() -> new IllegalArgumentException("Invalid analysis goal: " + analysisGoalStr));
			runSingleAnalysis(analysisGoal, ruleLeft, ruleRight, -1, -1);
		} else {
			initialize();
			runAllAnalyses(); // we assume the working directory is the root of the plug-in
		}
	}

	private static Optional<AnalysisGoal> getAnalysisGoalByName(final String analysisGoalStr) {
		return Arrays.asList(AnalysisGoal.values()).stream()//
				.filter(goal -> goal.toString().equals(analysisGoalStr))//
				.findAny();
	}

	private static Optional<Rule> getRuleByName(Module module, String ruleName) {
		return module.getUnits().stream() //
				.filter(unit -> unit instanceof Rule) //
				.map(unit -> (Rule) unit) //
				.filter(rule -> rule.getName().equals(ruleName))//
				.findAny();
	}

	private static boolean containsMetamodel(final Path directory) {
		File[] metamodelFiles = directory.toFile().listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return METAMODEL_FILENAME.equals(name);
			}
		});
		final boolean containsMetamodel = metamodelFiles.length != 0;
		return containsMetamodel;
	}

	private static boolean containsConflictMetamodel(final Path directory) {
		File[] minimalEcoreFiles = directory.toFile().listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return "minimal-model.ecore".equals(name);
			}
		});
		final boolean containsConflictMetamodel = minimalEcoreFiles.length != 0;
		return containsConflictMetamodel;
	}

	public static boolean isValidRuleName(String ruleLeftStr, Module module) {
		final Optional<Rule> rule = getRuleByName(module, ruleLeftStr);
		return rule.isPresent();
	}

	public static boolean isValidAnalysisGoal(final String analysisGoal) {
		return getAnalysisGoalByName(analysisGoal).isPresent();
	}

	public static long getProcessId() {
		String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
		return Long.parseLong(processName.split("@")[0]);
	}
}