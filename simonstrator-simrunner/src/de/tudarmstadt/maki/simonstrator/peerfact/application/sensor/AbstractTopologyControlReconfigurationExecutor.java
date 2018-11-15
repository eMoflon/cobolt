package de.tudarmstadt.maki.simonstrator.peerfact.application.sensor;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.math.IntRange;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerRepository;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.tud.kom.p2psim.impl.util.toolkits.DOMToolkit;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.evaluation.TaskExecutorUtils;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.util.run.SimulationTask;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.util.run.TaskExecutor;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.io.TeePrintStream;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.TopologyControlComponentConfig;
import de.tudarmstadt.maki.simonstrator.tc.scenario.ScenarioType;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;
import de.tudarmstadt.maki.simonstrator.tc.utils.DateHelper;
import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Implementation base class for all executors of the TC reconfiguration project
 *
 * @author Roland Kluge - Initial implementation
 *
 */
public abstract class AbstractTopologyControlReconfigurationExecutor {

	protected static final String CMD_OPTION_JAVA_MAX_MEMORY_LONG = "javaXmx";
	protected static final String CMD_OPTION_UNIQUE_RUN_ID_LONG = "uniqueRunName";
	protected static final String CMD_OPTION_THREAD_COUNT_LONG = "numThreads";
	protected static final String CMD_OPTION_SEED_COUNT_LONG = "numSeeds";
	protected static final String CMD_OPTION_CONFIGURATION_RANGE_LONG = "configurationRange";
	protected static final String CMD_OPTION_SHALL_PERSIST_LONG = "shallPersistConfiguration";
	protected static final String CMD_OPTION_OUTPUT_FOLDER_LONG = "outputFolder";
	protected static final String CMD_OPTION_HELP_LONG = "help";
	protected static final String CMD_OPTION_DRY_RUN_LONG = "dryRun";
	protected static final String CMD_OPTION_EXECUTOR_LONG = "executor";
	protected static final String CMD_OPTION_SIMULATION_CONFIGURATION_FILE = "c";
	protected static final String CMD_OPTION_DRY_RUN = "d";
	protected static final String CMD_OPTION_EXECUTOR = "e";
	protected static final String CMD_OPTION_HELP = "h";
	protected static final String CMD_OPTION_OUTPUT_FOLDER = "o";
	protected static final String CMD_OPTION_SHALL_PERSIST = "p";
	protected static final String CMD_OPTION_CONFIGURATION_RANGE = "r";
	protected static final String CMD_OPTION_SEED_COUNT = "s";
	protected static final String CMD_OPTION_THREAD_COUNT = "t";
	protected static final String CMD_OPTION_UNIQUE_RUN_ID = "u";
	protected static final String CMD_OPTION_JAVA_MAX_MEMORY = "x";
	protected static final String CMD_OPTION_SIMULATION_CONFIGURATION_FILE_LONG = "configFile";
	protected Logger logger;
	protected List<IntRange> configurationNumberRanges = new ArrayList<>();
	protected boolean dryRun = false;
	protected boolean shallPersistConfiguration = true;
	protected File simulationConfigurationFile = new File("config/wsntraces/wsntraces.xml");
	protected File outputFolder = new File("./output/wsntraces");
	protected int jvmMaximumHeapSpaceInMegabyte = 1500;
	protected int numParallelTasks = 1;
	protected String uniqueRunName = null;
	protected int numberOfSeeds = 5;
	protected File outputFolderForResults;
	protected String executor;

	/**
	 * The 'main method' of the executor, which parses the command-line parameters,
	 * initializes folders/file/logging and invokes
	 * {@link #generateSimulationConfigurations()}
	 *
	 * @param args
	 *                 should be identical to the command-line arguments received by
	 *                 the static main method
	 *
	 * @return exit code. 0 indicates a successful run
	 */
	public int run(final String[] args) {
		final Options options = createCommandLineOptionSpecification();

		final CommandLine parsedCommandLineOptions;
		try {
			parsedCommandLineOptions = parseCommandLineOptions(args, options);
		} catch (final ParseException e) {
			System.err.println("Problem while parsing command-line options: " + e.getMessage());
			showHelp(options);
			return -1;
		}

		try {
			final int exitCode = processCommandLine(parsedCommandLineOptions, options);
			if (exitCode != 0)
				return exitCode;
		} catch (final IllegalArgumentException e) {
			showHelp(options);
			throw e;
		}

		this.outputFolderForResults = initializeResultsFolder();

		configureLogging(this.outputFolderForResults);

		printSummaryOfConfiguration();

		logger.info(String.format("Generating simulation configurations..."));
		final List<TopologyControlComponentConfig> configs = generateSimulationConfigurations();
		logger.info(String.format("There are %,d configuration candidates to be filtered.", configs.size()));

		final List<TopologyControlComponentConfig> filteredConfigs = filterConfigurations(configs);

		validate(filteredConfigs);

		logger.info(String.format("Generated %,d configurations. Active configuration IDs: %s", filteredConfigs.size(),
				summarizeConfigurationNumbers(filteredConfigs)));

		persistConfigurationsIfNecessary(filteredConfigs, outputFolderForResults);

		if (dryRun) {
			logger.info("Will not start simulations due to dry-run mode.");
		} else {
			startSimulations(filteredConfigs);
		}

		return 0;
	}

	/**
	 * The actual implementation of the evaluation code
	 *
	 * @return the set of
	 */
	protected abstract List<TopologyControlComponentConfig> generateSimulationConfigurations();

	/**
	 * Returns the list of possible values for the executor to run
	 */
	protected abstract List<String> getSupportedExecutors();

	protected int processCommandLine(final CommandLine parsedCommandLineOptions, final Options possibleOptions) {
		if (parsedCommandLineOptions.hasOption(CMD_OPTION_SIMULATION_CONFIGURATION_FILE)) {
			simulationConfigurationFile = new File(
					parsedCommandLineOptions.getOptionValue(CMD_OPTION_SIMULATION_CONFIGURATION_FILE));
			if (!simulationConfigurationFile.exists())
				throw new IllegalArgumentException(String.format("Simulation configuration file '%s' is not accessible",
						simulationConfigurationFile));
			if (!simulationConfigurationFile.getName().endsWith(".xml"))
				throw new IllegalArgumentException(
						String.format("Simulation configuration file '%s' is not XML.", simulationConfigurationFile));
		}

		dryRun = parsedCommandLineOptions.hasOption(CMD_OPTION_DRY_RUN);

		if (parsedCommandLineOptions.hasOption(CMD_OPTION_EXECUTOR)) {
			executor = parsedCommandLineOptions.getOptionValue(CMD_OPTION_EXECUTOR);
			if (!getSupportedExecutors().contains(executor))
				throw new IllegalArgumentException(String.format("Unsupported executor %s", executor));
		}

		if (parsedCommandLineOptions.hasOption(CMD_OPTION_HELP)) {
			showHelp(possibleOptions);
			return 1;
		}

		if (parsedCommandLineOptions.hasOption(CMD_OPTION_OUTPUT_FOLDER)) {
			outputFolder = new File(parsedCommandLineOptions.getOptionValue(CMD_OPTION_OUTPUT_FOLDER));
		}

		if (parsedCommandLineOptions.hasOption(CMD_OPTION_SHALL_PERSIST)) {
			shallPersistConfiguration = Boolean
					.parseBoolean(parsedCommandLineOptions.getOptionValue(CMD_OPTION_SHALL_PERSIST));
		}

		if (parsedCommandLineOptions.hasOption(CMD_OPTION_CONFIGURATION_RANGE)) {
			final String optionValue = parsedCommandLineOptions.getOptionValue(CMD_OPTION_CONFIGURATION_RANGE);
			final String[] rangeSpecifications = optionValue.split(",");
			for (final String rangeSpecificationStr : rangeSpecifications) {
				final String[] rangeSpecification = rangeSpecificationStr.split("-");
				if (!Arrays.asList(1, 2).contains(rangeSpecification.length))
					throw new IllegalArgumentException(
							String.format("Cannot parse the following expression as range of configuration numbers: %s",
									optionValue));
				try {
					final String firstEntry = rangeSpecification[0];
					final int lowerConfigurationNumberLimit = Integer.parseInt(firstEntry);
					final int upperConfigurationNumberLimit;
					if (rangeSpecification.length == 2) {
						upperConfigurationNumberLimit = Integer.parseInt(rangeSpecification[1]);
					} else if (rangeSpecification.length == 1 && rangeSpecificationStr.contains("-")) {
						upperConfigurationNumberLimit = Integer.MAX_VALUE;
					} else if (rangeSpecification.length == 1) {
						upperConfigurationNumberLimit = lowerConfigurationNumberLimit;
					} else {
						throw new IllegalStateException(
								String.format("Unsupported range structure %s", rangeSpecificationStr));
					}
					this.configurationNumberRanges
							.add(new IntRange(lowerConfigurationNumberLimit, upperConfigurationNumberLimit));
				} catch (final NumberFormatException e) {
					throw new IllegalArgumentException(
							String.format("Failed to parse configuration number: %s", rangeSpecificationStr), e);
				}
			}
			Collections.sort(this.configurationNumberRanges, new Comparator<IntRange>() {

				@Override
				public int compare(final IntRange range1, final IntRange range2) {
					final int lowerResult = Integer.compare(range1.getMinimumInteger(), range2.getMinimumInteger());
					final int upperResult = Integer.compare(range1.getMaximumInteger(), range2.getMaximumInteger());
					return lowerResult != 0 ? lowerResult : upperResult;
				}
			});
		}

		if (parsedCommandLineOptions.hasOption(CMD_OPTION_SEED_COUNT)) {
			numberOfSeeds = Integer.parseInt(parsedCommandLineOptions.getOptionValue(CMD_OPTION_SEED_COUNT));
			if (numberOfSeeds <= 0)
				throw new IllegalArgumentException("Option 's' must be positive");
		}

		if (parsedCommandLineOptions.hasOption(CMD_OPTION_THREAD_COUNT)) {
			numParallelTasks = Integer.parseInt(parsedCommandLineOptions.getOptionValue(CMD_OPTION_THREAD_COUNT));
			if (numParallelTasks <= 0)
				throw new IllegalArgumentException("Option 't' must be positive");
		}

		if (parsedCommandLineOptions.hasOption(CMD_OPTION_UNIQUE_RUN_ID)) {
			uniqueRunName = parsedCommandLineOptions.getOptionValue(CMD_OPTION_UNIQUE_RUN_ID);
		}

		if (parsedCommandLineOptions.hasOption(CMD_OPTION_JAVA_MAX_MEMORY)) {
			jvmMaximumHeapSpaceInMegabyte = Integer
					.parseInt(parsedCommandLineOptions.getOptionValue(CMD_OPTION_JAVA_MAX_MEMORY));
			if (jvmMaximumHeapSpaceInMegabyte <= 0)
				throw new IllegalArgumentException("Option 'x' must be positive");
		}
		return 0;
	}

	protected Options createCommandLineOptionSpecification() {
		final Options options = new Options();
		options.addOption(CMD_OPTION_SIMULATION_CONFIGURATION_FILE, CMD_OPTION_SIMULATION_CONFIGURATION_FILE_LONG, true,
				"Path to configuration file");
		options.addOption(CMD_OPTION_DRY_RUN, CMD_OPTION_DRY_RUN_LONG, false, "Enable dry-run mode");
		options.addOption(CMD_OPTION_EXECUTOR, CMD_OPTION_EXECUTOR_LONG, true,
				String.format("Configure executor. Possible values: %s", this.getSupportedExecutors()));
		options.addOption(CMD_OPTION_HELP, CMD_OPTION_HELP_LONG, false, "Print this message");
		options.addOption(CMD_OPTION_OUTPUT_FOLDER, CMD_OPTION_OUTPUT_FOLDER_LONG, true,
				"Target folder for generated output");
		options.addOption(CMD_OPTION_SHALL_PERSIST, CMD_OPTION_SHALL_PERSIST_LONG, false, "Persist configurations?");
		options.addOption(CMD_OPTION_CONFIGURATION_RANGE, CMD_OPTION_CONFIGURATION_RANGE_LONG, true,
				"Range specifications as comma-separated list with each entry having the form 'singleConfigurationNumber', 'lowerCongfigurationNumber-upperConfigurationNumber' or 'lowerCongfigurationNumber-'");
		options.addOption(CMD_OPTION_SEED_COUNT, CMD_OPTION_SEED_COUNT_LONG, true, "Number of random seeds to use");
		options.addOption(CMD_OPTION_THREAD_COUNT, CMD_OPTION_THREAD_COUNT_LONG, true, "Number of threads to use");
		options.addOption(CMD_OPTION_UNIQUE_RUN_ID, CMD_OPTION_UNIQUE_RUN_ID_LONG, true,
				"A unique identifier, which is prepended to the output folder");
		options.addOption(CMD_OPTION_JAVA_MAX_MEMORY, CMD_OPTION_JAVA_MAX_MEMORY_LONG, true,
				"JVM maximum heap size (in MB)");
		return options;
	}

	/**
	 * Validates the given set of {@link TopologyControlComponentConfig}s
	 */
	protected void validate(final List<TopologyControlComponentConfig> configs) {
		configs.forEach(config -> config.validate());
	}

	protected SimulationTask createSimulationTask(final TopologyControlComponentConfig config) {
		return new TopologyControlSimulationTask(config);
	}

	protected String getUniqueRunName() {
		return (uniqueRunName != null) ? uniqueRunName : "";
	}

	@SafeVarargs
	protected static <T> T[] asArray(final T... values) {
		return values;
	}

	protected static boolean isScenarioWithBaseStation(final ScenarioType scenarioType) {
		return scenarioType == ScenarioType.DATACOLLECTION;
	}

	protected static List<TopologyControlAlgorithmID> asModifiableList(
			final TopologyControlAlgorithmID... algorithmIDs) {
		return new ArrayList<>(Arrays.asList(algorithmIDs));
	}

	protected static boolean isLStarKTC(final TopologyControlAlgorithmID algorithm) {
		return algorithm == UnderlayTopologyControlAlgorithms.LSTAR_KTC;
	}

	protected static boolean isKTCLikeAlgorithm(final TopologyControlAlgorithmID algorithm) {
		return Arrays.asList(UnderlayTopologyControlAlgorithms.D_KTC, UnderlayTopologyControlAlgorithms.E_KTC,
				UnderlayTopologyControlAlgorithms.LSTAR_KTC).contains(algorithm);
	}

	protected static String createDurationDescriptor(final double topologyControlIntervalInMinutes) {
		return topologyControlIntervalInMinutes >= 1.0 ? ((int) topologyControlIntervalInMinutes) + "m"
				: ((int) (topologyControlIntervalInMinutes * 60)) + CMD_OPTION_SEED_COUNT;
	}

	/**
	 * Returns true if the given configuration shall be preserved
	 *
	 * @param config
	 * @return
	 */
	protected boolean shallKeepConfiguration(final TopologyControlComponentConfig config) {
		if (!this.configurationNumberRanges.isEmpty()) {
			boolean match = false;
			for (final IntRange configurationNumberRange : this.configurationNumberRanges) {
				if (configurationNumberRange.containsInteger(config.configurationNumber)) {
					match = true;
					break;
				}
			}
			if (!match) {
				return false;
			}
		}

		return true;
	}

	protected String getSummaryOfConfiguration() {
		return String.format("Summary of effective command-line configuration (from %s):\n" //
				+ "  %s: %s\n" //
				+ "  %s: %b\n" //
				+ "  %s: %s\n" //
				+ "  %s: %s\n" //
				+ "  %s: %b\n" //
				+ "  %s: %s\n" //
				+ "  %s: %d\n" //
				+ "  %s: %d\n" //
				+ "  %s (effective): '%s'\n" //
				+ "  %s: %dm\n", //
				AbstractTopologyControlReconfigurationExecutor.class.getSimpleName(), //
				CMD_OPTION_SIMULATION_CONFIGURATION_FILE_LONG, simulationConfigurationFile, //
				CMD_OPTION_DRY_RUN_LONG, dryRun, //
				CMD_OPTION_EXECUTOR_LONG, executor, //
				CMD_OPTION_OUTPUT_FOLDER_LONG, outputFolder, //
				CMD_OPTION_SHALL_PERSIST_LONG, shallPersistConfiguration, //
				CMD_OPTION_CONFIGURATION_RANGE_LONG, formatConfigurationRanges(this.configurationNumberRanges), //
				CMD_OPTION_SEED_COUNT_LONG, numberOfSeeds, //
				CMD_OPTION_THREAD_COUNT_LONG, numParallelTasks, //
				CMD_OPTION_UNIQUE_RUN_ID_LONG, getUniqueRunName(), //
				CMD_OPTION_JAVA_MAX_MEMORY_LONG, jvmMaximumHeapSpaceInMegabyte);
	}

	private void showHelp(final Options possibleOptions) {
		final HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.setWidth(120);
		helpFormatter.printHelp("[program]", possibleOptions);
	}

	private static String formatConfigurationRanges(final List<IntRange> configurationNumberRanges) {
		if (configurationNumberRanges.isEmpty()) {
			return "all";
		} else {
			final StringBuilder sb = new StringBuilder();
			final IntRange firstRange = configurationNumberRanges.get(0);
			for (final IntRange intRange : configurationNumberRanges) {
				if (!intRange.equals(firstRange)) {
					sb.append(",");
				}
				sb.append(intRange.getMinimumInteger());
				sb.append("-");
				if (intRange.getMaximumInteger() == Integer.MAX_VALUE) {
					sb.append("max");
				} else {
					sb.append(intRange.getMaximumInteger());
				}
			}
			return sb.toString();
		}
	}

	private static CommandLine parseCommandLineOptions(final String[] args, final Options options)
			throws ParseException {
		final CommandLineParser parser = new DefaultParser();
		final CommandLine cmd = parser.parse(options, args);
		return cmd;
	}

	private void printSummaryOfConfiguration() {
		logger.info(getSummaryOfConfiguration());
	}

	/**
	 * Prints the range of configuration numbers in a user-friendly way
	 *
	 * @param configs
	 * @return
	 */
	private static String summarizeConfigurationNumbers(final List<TopologyControlComponentConfig> configs) {
		final StringBuilder sb = new StringBuilder();
		int beginOfLastRange = Integer.MIN_VALUE;
		int lastConfigurationNumber = Integer.MIN_VALUE;

		for (final TopologyControlComponentConfig config : configs) {
			if (beginOfLastRange == Integer.MIN_VALUE) {
				beginOfLastRange = config.configurationNumber;
				sb.append(beginOfLastRange);
			} else if (config.configurationNumber != lastConfigurationNumber + 1) {
				if (beginOfLastRange != lastConfigurationNumber) {
					sb.append("-");
					sb.append(lastConfigurationNumber);
				}
				sb.append(",");
				beginOfLastRange = config.configurationNumber;
				sb.append(beginOfLastRange);
			}

			lastConfigurationNumber = config.configurationNumber;
		}

		if (beginOfLastRange != lastConfigurationNumber) {
			sb.append("-");
			sb.append(lastConfigurationNumber);
		}

		return sb.toString();
	}

	private void configureLogging(final File outputFolderForResults) {
		System.setProperty("logfile.name",
				new File(outputFolderForResults, "simrunnerlog_" + DateHelper.getFormattedDate() + ".log").getPath());
		Locale.setDefault(Locale.US);
		final LoggerRepository loggerRepository = LogManager.getLoggerRepository();
		loggerRepository.setThreshold(org.apache.log4j.Level.INFO);
		logger = Logger.getLogger(getClass());

		final File fileForStdout = new File(
				outputFolderForResults + "/stdout_" + DateHelper.getFormattedDate() + ".txt");
		try {
			final FileOutputStream file = new FileOutputStream(fileForStdout);
			final TeePrintStream tee = new TeePrintStream(file, System.out);
			System.setOut(tee);
		} catch (final FileNotFoundException e1) {
			throw new IllegalStateException("Cannot write to " + fileForStdout);
		}

		logger.info(String.format("Forwarding standard output to " + fileForStdout));
	}

	private File initializeResultsFolder() {
		final File outputFolderForResults = new File(outputFolder,
				getUniqueRunName() + "batchrun_" + DateHelper.getFormattedDate());
		outputFolderForResults.mkdirs();
		return outputFolderForResults;
	}

	private void persistConfigurationsIfNecessary(final List<TopologyControlComponentConfig> configs,
			final File outputFolderForResults) {
		if (shallPersistConfiguration) {
			logger.info(String.format("Persisting %,d configurations...", configs.size()));
			configs.forEach(config -> persistConcreteConfiguration(outputFolderForResults, simulationConfigurationFile,
					config));
		}
	}

	private void persistConcreteConfiguration(final File outputFolder, final File simulationConfigurationFile,
			final TopologyControlComponentConfig config) throws TransformerFactoryConfigurationError {
		final File targetFolderForConcreteConfig = new File(outputFolder, "configs");
		targetFolderForConcreteConfig.mkdirs();
		final File targetFileForConcreteConfig = new File(targetFolderForConcreteConfig,
				String.format("%04d_config.xml", config.configurationNumber));
		try {
			final InputStream is = new FileInputStream(simulationConfigurationFile);
			final BufferedInputStream buf = new BufferedInputStream(is);
			final DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			final Document doc = b.parse(buf);
			final Element root = DOMToolkit.getFirstChildElemMatching(doc, "Configuration");
			final Element variableRoot = DOMToolkit.getFirstChildElemMatching(root, "Default");
			final Map<String, String> simConfigurationParameters = collectConfigurationParameters(config);
			for (final Element elem : DOMToolkit.getAllChildElemsMatching(variableRoot, "Variable")) {
				final String elementName = elem.getAttribute("name");
				if (simConfigurationParameters.containsKey(elementName)) {
					final String value = simConfigurationParameters.get(elementName);
					final String safeValue = prepareValueForDebugging(elementName, value);
					elem.setAttribute("value", safeValue);
				}
			}
			final DOMSource source = new DOMSource(doc);
			final FileWriter writer = new FileWriter(targetFileForConcreteConfig);
			final StreamResult result = new StreamResult(writer);

			final TransformerFactory transformerFactory = TransformerFactory.newInstance();
			final Transformer transformer = transformerFactory.newTransformer();
			transformer.transform(source, result);

		} catch (final IOException | ParserConfigurationException | FactoryConfigurationError | SAXException
				| TransformerException e) {
			Monitor.log(AbstractTopologyControlReconfigurationExecutor.class, Level.ERROR,
					"Failed to store adjusted config file for configuration number %d to %s (see the following stacktrace)",
					config.configurationNumber, targetFileForConcreteConfig);
			e.printStackTrace();
		}
	}

	private void startSimulations(final List<TopologyControlComponentConfig> configs) {
		final List<SimulationTask> simulations = configs.stream().map(config -> createSimulationTask(config))
				.collect(Collectors.toList());

		final TaskExecutor taskExecutor = new TaskExecutor(simulations, numParallelTasks);
		taskExecutor.setJvmOptionXmx(jvmMaximumHeapSpaceInMegabyte + "m");
		taskExecutor.start();

		logger.info("Enter 'quit' to abort simulation.");
		if (!GraphicsEnvironment.isHeadless()) {
			new StopFrame().setVisible(true);
		}
		TaskExecutorUtils.waitForQuitOrTerminationOfSubprocesses(taskExecutor);

		logger.info("Batch run finished - closing all threads");
		taskExecutor.stop();
	}

	private static String prepareValueForDebugging(final String elementName, final String value) {
		switch (elementName) {
		case CMD_OPTION_OUTPUT_FOLDER_LONG:
		case "tracesOutputFolder":
			return value + "_debug";
		case "enableVisualization":
			return "true";
		}
		return value;
	}

	private List<TopologyControlComponentConfig> filterConfigurations(
			final List<TopologyControlComponentConfig> configs) {
		return configs.stream().filter(config -> shallKeepConfiguration(config)).collect(Collectors.toList());
	}

	private Map<String, String> collectConfigurationParameters(final TopologyControlComponentConfig config) {
		final SimulationTask task = createSimulationTask(config);
		final Map<String, String> simConfigurationParameters = new HashMap<>();
		task.getParams().stream().filter(s -> s.contains("=")).map(s -> s.split("=")).forEach(entries -> {
			if (entries.length == 1)
				throw new IllegalArgumentException(
						String.format("Unsupported variable assignment: %s", Arrays.toString(entries)));
			simConfigurationParameters.put(entries[0], entries[1]);
		});
		return simConfigurationParameters;
	}

	// Idea taken from: http://jnetpcap.com/node/1106
	private class StopFrame extends JFrame {
		private static final long serialVersionUID = -4819501171985552545L;

		StopFrame() {
			setTitle("STOP Batch Run.");
			final ImageIcon img = new ImageIcon(getClass().getResource("/dwarf_noun_1434563.png"));
			setIconImage(img.getImage());
			final JButton stopButton = new JButton("STOP Batch Run!");
			stopButton.setBackground(Color.RED);
			stopButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					final int dialogResult = JOptionPane.showConfirmDialog(null, "Really stop the batch run?", "DANGER",
							JOptionPane.YES_NO_OPTION);
					if (dialogResult == JOptionPane.YES_OPTION) {
						System.exit(0);
					}
				}
			});
			this.getContentPane().add(stopButton);
			this.pack();
			this.setResizable(false);
		}
	}

}
