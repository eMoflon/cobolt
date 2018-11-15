package de.tudarmstadt.maki.simonstrator.tc.reconfiguration.analysis;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tudarmstadt.maki.simonstrator.tc.component.TopologyControlComponent;

public class SplConquerorHelper {

	public static final String SIM_CFG_OPTION_SPLC_FILE = "splcOutputFile";

	public static Optional<Double> extractFraction(final String filename) {
		final String safeFilename = filename != null ? filename : "";
		final Pattern pattern = Pattern.compile("^.*fraction(\\d+).*$");
		final Matcher matcher = pattern.matcher(safeFilename);
		if (matcher.matches()) {
			return Optional.of(Integer.parseInt(matcher.group(1)) / 100.0);
		} else {
			return Optional.empty();
		}
	}

	public static Optional<Integer> extractSeed(final String filename) {
		final String safeFilename = filename != null ? filename : "";
		final Pattern pattern = Pattern.compile("^.*seed(\\d+).*$");
		final Matcher matcher = pattern.matcher(safeFilename);
		if (matcher.matches()) {
			return Optional.of(Integer.parseInt(matcher.group(1)));
		} else {
			return Optional.empty();
		}
	}

	public static Optional<String> extractNonfunctionalProperty(final String filename) {
		final String safeFilename = filename != null ? filename : "";
		final Pattern pattern = Pattern.compile("^.*seed\\d+_(.*)\\.log$");
		final Matcher matcher = pattern.matcher(safeFilename);
		if (matcher.matches()) {
			return Optional.of(matcher.group(1));
		} else {
			return Optional.empty();
		}
	}

	static String getSplConquerorConfigurationOption(final TopologyControlComponent tcc) {
		return tcc.getConfiguration().splcOutputFile;
	}
}
