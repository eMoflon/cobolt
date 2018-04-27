package org.cobolt.tccpa;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.eclipse.emf.henshin.model.Module;

public final class TopologyControlExecutorMain {

	public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {

		if (args.length != 1)
			throw new IllegalArgumentException("Expected one parameter");
		final URL resourceUrl = TopologyControlExecutorMain.class.getClassLoader().getResource(args[0]);
		final File analysisSpecificationFile = new File(resourceUrl.toURI());
		if (!analysisSpecificationFile.exists())
			throw new IllegalArgumentException("Parameter must point to an existing file");
		final List<String> lines = FileUtils.readLines(analysisSpecificationFile);
		final List<Triple> specifications = lines.stream().map(line -> {
			final String[] lineSegments = line.split("\\s+");
			return new Triple(lineSegments[0], lineSegments[1], lineSegments[2]);
		}).collect(Collectors.toList());

		System.out.println("Validating analysis specifications.");
		final Module module = TopologyControlCriticalPairAnalysisMain.initialize();
		for (final Triple specification : specifications) {
			final String ruleLeftStr = specification.first;
			final String ruleRightStr = specification.second;
			final String analysisGoal = specification.third;
			if (!TopologyControlCriticalPairAnalysisMain.isValidRuleName(ruleLeftStr, module))
				throw new IllegalArgumentException("Invalid LHS rule name:" + ruleLeftStr);
			
			if (!TopologyControlCriticalPairAnalysisMain.isValidRuleName(ruleRightStr, module))
				throw new IllegalArgumentException("Invalid RHS rule name:" + ruleRightStr);
			
			if (!TopologyControlCriticalPairAnalysisMain.isValidAnalysisGoal(analysisGoal))
				throw new IllegalArgumentException("Invalid analysis goal:" + analysisGoal);
		}
		System.out.println("Validation successful.");
		
		for (final Triple specification : specifications) {
			final String ruleLeftStr = specification.first;
			final String ruleRightStr = specification.second;
			final String analysisGoal = specification.third;
			TopologyControlExecutorMain.exec(TopologyControlCriticalPairAnalysisMain.class, ruleLeftStr, ruleRightStr, analysisGoal);
		}
	}
	
	// Solution from https://stackoverflow.com/a/723914
	public static int exec(final Class<? extends TopologyControlCriticalPairAnalysisMain> runnerClass, String ruleLeftStr, String ruleRightStr, String analysisGoal)
			throws IOException, InterruptedException {
		final String javaHome = System.getProperty("java.home");
		final String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
		final String classpath = System.getProperty("java.class.path");
		final String className = runnerClass.getCanonicalName();

		final ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className, ruleLeftStr, ruleRightStr, analysisGoal);
		builder.inheritIO();

		final Process process = builder.start();
		process.waitFor();
		return process.exitValue();
	}

	private static class Triple {
		private final String first;
		private final String second;
		private final String third;
		
		public Triple(final String first, final String second, final String third) {
			this.first = first;
			this.second = second;
			this.third = third;
		}
	}
}
