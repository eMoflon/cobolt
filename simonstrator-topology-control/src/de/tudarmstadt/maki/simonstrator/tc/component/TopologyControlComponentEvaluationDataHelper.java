package de.tudarmstadt.maki.simonstrator.tc.component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;

import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.TopologyControlComponentConfig;
import de.tudarmstadt.maki.simonstrator.tc.utils.DateHelper;

public class TopologyControlComponentEvaluationDataHelper {

	public static final String CSV_SEP = ";";
	public static final boolean DO_APPEND = true;
	public static final boolean DO_NOT_APPEND = false;
	public static final File EVAL_ROOT_FOLDER = new File("./output/tc/");

	private TopologyControlComponent component;

	private File evaluationDataFile;

	TopologyControlComponentEvaluationDataHelper(final TopologyControlComponent component) {
		this.component = component;
		this.initialize();
	}

	/**
	 * Initializes the target file for the evaluation output
	 */
	private void initialize() {
		final File fallbackOutputFolder = new File(EVAL_ROOT_FOLDER,
				"singlerun_" + DateHelper.getFormattedDate());
		this.component.getConfiguration().outputFolder = this.component.getConfiguration().outputFolder == null
				? fallbackOutputFolder : this.component.getConfiguration().outputFolder;

		final String relativePath = TopologyControlComponentEvaluationDataHelper
				.createFileForConfiguration(this.component.getConfiguration());
		this.evaluationDataFile = new File(this.component.getConfiguration().outputFolder, relativePath);
		this.component.getConfiguration().evaluationDataFilename = evaluationDataFile.getAbsolutePath();

		String logfileRelativePath = String.format("log/%05d_logfile.txt",
				this.component.getConfiguration().configurationNumber);
		this.component.getConfiguration().logfileName = new File(this.component.getConfiguration().outputFolder,
				logfileRelativePath).getAbsolutePath();

		try {
			FileUtils.writeLines(evaluationDataFile,
					Arrays.asList(EvaluationStatistics.createHeaderOfEvaluationDataFile(CSV_SEP)), true);
		} catch (final IOException e) {
			final String message = String.format("Failed to write header to %s", evaluationDataFile);
			Monitor.log(getClass(), Level.WARN, message);
			throw new IllegalStateException(message);
		}
	}

	private static String createFileForConfiguration(TopologyControlComponentConfig configuration) {
		return String.format("data%s%05d_data.csv", File.separator, configuration.configurationNumber);
	}

	void writeDataLine() {
		try {
			FileUtils.writeLines(evaluationDataFile,
					Arrays.asList(component.getStatisticsHelper().getStatisticsDTO().formatAsCsvLine(CSV_SEP)),
					true);
		} catch (final IOException e) {
			Monitor.log(getClass(), Level.WARN, "Failed to write to %s", evaluationDataFile);
		}
	}
}
