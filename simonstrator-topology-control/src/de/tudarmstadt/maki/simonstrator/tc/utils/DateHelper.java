package de.tudarmstadt.maki.simonstrator.tc.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class DateHelper {

	/**
	 * This serves as the unique date per simulation run. Since parallel
	 * simulations are not supported within a JVM, using a singleton here is
	 * safe.
	 */
	private static final Date SYSTEM_DATE = new Date();

	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HHmmss");

	public static synchronized String getFormattedDate() {
		return DATE_FORMATTER.format(SYSTEM_DATE);
	}

   public static String substitutePlaceholders(final String outputFolder) {
   	if (null == outputFolder)
   		return outputFolder;
   
   	return outputFolder.replaceAll(Pattern.quote("[DATETIME]"), getFormattedDate());
   }

}
