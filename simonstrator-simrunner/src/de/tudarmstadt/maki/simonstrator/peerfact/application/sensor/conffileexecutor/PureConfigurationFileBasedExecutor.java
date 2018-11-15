package de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.conffileexecutor;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.AbstractTopologyControlReconfigurationExecutor;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.util.run.SimulationTask;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.TopologyControlComponentConfig;

public class PureConfigurationFileBasedExecutor extends AbstractTopologyControlReconfigurationExecutor {

	public class ConfigurationFilenameFilter implements FilenameFilter {

		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".xml");
		}

	}

	private List<File> configurationFiles;

	/**
	 * Runs the runtime evaluation for the 'Topology Control Reconfiguration'
	 * project.
	 *
	 * For reproducibility, every simulation is started in a separate process.
	 *
	 * To stop the whole evaluation, open this process's console and type 'quit'
	 * + ENTER.
	 */
	public static void main(final String[] args) {
		final int exitCode = new PureConfigurationFileBasedExecutor().run(args);
		System.exit(exitCode);
	}

	@Override
	protected int processCommandLine(CommandLine parsedCommandLineOptions, Options possibleOptions) {
		final int superExitCode = super.processCommandLine(parsedCommandLineOptions, possibleOptions);
		if (superExitCode != 0)
			return superExitCode;

		File configurationFileFolder = new File(
				"C:\\Users\\rkluge\\Documents\\repos\\simonstrator-simrunner\\output\\rkluge\\wsntraces\\eval2444batchrun_2018-01-15T160345\\configs\\");
		if (!configurationFileFolder.exists())
			throw new IllegalArgumentException(
					String.format("Configuration files folder does not exist: %s", configurationFileFolder));
		if (!configurationFileFolder.isDirectory())
			throw new IllegalArgumentException(
					String.format("Configuration files folder is not a folder: %s", configurationFileFolder));

		configurationFiles = Arrays.asList(configurationFileFolder.listFiles(new ConfigurationFilenameFilter()));

		if (configurationFiles.isEmpty())
			throw new IllegalArgumentException(
					String.format("No configuration files found in %s", configurationFileFolder));

		return 0;
	}

	@Override
	protected List<TopologyControlComponentConfig> generateSimulationConfigurations() {
		List<TopologyControlComponentConfig> configs = new ArrayList<>();
		for (final File configurationFile : configurationFiles) {
			final TopologyControlComponentConfig config = new TopologyControlComponentConfig();
			config.simulationConfigurationFile = configurationFile.getAbsolutePath();
			Pattern filenamePattern = Pattern.compile("(\\d+).*");
			final Matcher matcher = filenamePattern.matcher(configurationFile.getName());
			if (matcher.matches()) {
				config.configurationNumber = Integer.parseInt(matcher.group(1));
			} else {
				config.configurationNumber = configs.size() + 1;
			}
			config.outputFolder = outputFolderForResults;
			config.tracesOutputFolder = outputFolderForResults;
			config.outputFilePrefix = "wsntraces";
			configs.add(config);
		}
		return configs;
	}

	@Override
	protected List<String> getSupportedExecutors() {
		return Arrays.asList("");
	}

	@Override
	protected void validate(final List<TopologyControlComponentConfig> configs) {
		// Skip
	}

	@Override
	protected SimulationTask createSimulationTask(TopologyControlComponentConfig config) {
		return new PureConfigurationFileSimulationTask(config);
	}
}
