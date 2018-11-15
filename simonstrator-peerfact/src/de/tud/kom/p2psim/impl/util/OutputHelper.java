package de.tud.kom.p2psim.impl.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.util.oracle.GlobalOracle;

/**
 * This class is meant to help dealing with output folder name generation
 * 
 * @author Julius Rueckert
 * 
 */
public class OutputHelper {

	/**
	 * This class represents an output folder
	 */
	public static class OutputFolder {

		private final String completePath;

		public OutputFolder(String parentFolderName,
				LinkedHashMap<String, String> addFields) {
			this.completePath = OutputHelper.getCompletePath(parentFolderName,
					addFields);
			init();
		}

		/**
		 * @return return the complete path of this output folder
		 */
		public String getCompletePath() {
			return completePath;
		}

		private void init() {
			File statDir = new File(completePath);
			if (!statDir.exists() || !statDir.isDirectory()) {
				statDir.mkdirs();
			}
		}
	}

	private static String uniqueFolderName;

	public static String getUniqueFolderName(
			LinkedHashMap<String, String> addFields) {
		if (uniqueFolderName == null) {
			String name = "";

			Calendar cal = Calendar.getInstance();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			String date = df.format(cal.getTime());

			String seed = "seed" + Simulator.getSeed();

			if (GlobalOracle.getHosts().size() > 0)
				name += "size" + GlobalOracle.getHosts().size()
						+ File.separator;

			boolean first = true;
			for (Entry<String, String> e : addFields.entrySet()) {
				if (first) {
					name += e.getKey() + e.getValue();
					first = false;
				} else
					name += "_" + e.getKey() + e.getValue();
			}

			if (!name.isEmpty())
				name = name + File.separator;

			uniqueFolderName = name + date + "_" + seed + File.separator;
		}
		return uniqueFolderName;
	}

	public static String getCompletePath(String folderName,
			LinkedHashMap<String, String> addFields) {
		return "outputs" + File.separator
				+ (!folderName.isEmpty() ? (folderName + File.separator) : "")
				+ getUniqueFolderName(addFields);
	}

	/**
	 * @param parentFolderName
	 *            the parent folder to be used
	 * @param addFields
	 *            additional field to be included in the folders name
	 * @return a new output folder instance
	 */
	public static OutputFolder getNewOutputFolder(String parentFolderName,
			LinkedHashMap<String, String> addFields) {

		return new OutputFolder(parentFolderName, addFields);

	}

	/**
	 * @param parentFolderName
	 *            the parent folder to be used
	 * @return a new output folder instance
	 */
	public static OutputFolder getNewOutputFolder(String parentFolderName) {
		return getNewOutputFolder(parentFolderName,
				new LinkedHashMap<String, String>());
	}

	/**
	 * @return a new output folder instance
	 */
	public static OutputFolder getNewOutputFolder() {
		return getNewOutputFolder("");
	}

}
