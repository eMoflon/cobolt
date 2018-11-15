package de.tud.kom.p2psim.impl.util.stat.distributions;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.util.Distribution;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * This distribution loads CDF data from a given CSV file and provides random
 * values out of the loaded data set.
 * 
 * TODO: better scaling! Scaled with the highest value! But it gives many more
 * cases for scaling in CDF.
 * 
 * @author Fabio Zöllner
 * 
 */
public class CustomDistribution implements Distribution {

	private Random rand = Randoms.getRandom(this);

	private String csvFile = "";

	private boolean scale = false;
    private double scaleFactor = 1;

	private TreeMap<Double, Double> cdfData = new TreeMap<Double, Double>();

	@XMLConfigurableConstructor({ "csvFile" })
	public CustomDistribution(String csvFile) {
		readCDFData(csvFile);
	}

    public CustomDistribution(TreeMap<Double, Double> cdfData) {
        this.cdfData = cdfData;
    }

    public CustomDistribution(TreeMap<Double, Double> cdfData, double scaleFactor) {
        this(cdfData);
        this.scaleFactor = scaleFactor;
        this.scale = true;
        scaleCDF(scaleFactor);
    }

	public int getSize() {
		return cdfData.size();
	}

	@Override
	public double returnValue() {
		double randomDouble = rand.nextDouble();

		Map.Entry<Double, Double> greaterOrEqualEntry = cdfData
				.ceilingEntry(randomDouble);

		if (greaterOrEqualEntry == null) {
			Monitor.log(CustomDistribution.class, Level.WARN,
					"No entry with a key greater or equal to " + randomDouble
					+ " has been found. (Has the data been loaded?)");
			return 0;
		} else {
			return greaterOrEqualEntry.getValue();
		}
	}

	/**
	 * Reads a simple two column comma separated list of doubles and returns
	 * them in a TreeMap.
	 * 
	 * @param csvFilename
	 *            The path to the CSV file
	 * @return The read double values as a TreeMap
	 */
	private void readCDFData(String csvFilename) {

		Monitor.log(CustomDistribution.class, Level.INFO,
				"Reading CDF data from CSV file %s", csvFilename);
		cdfData.clear();

		boolean entrySuccessfullyRead = false;
		double scaleFactor = Double.MIN_VALUE;
		long counter = 0;

		BufferedReader csv = null;
		try {
			csv = new BufferedReader(new FileReader(csvFilename));

			while (csv.ready()) {
				counter++;
				String line = csv.readLine();

				if (line.indexOf(",") > -1) {
					String[] parts = line.split(",");

					if (parts.length == 2) {
						try {
							Double x = Double.parseDouble(parts[0]);
							Double cf = Double.parseDouble(parts[1]);
							scaleFactor = Math.max(scaleFactor, x);

							cdfData.put(cf, x);
							entrySuccessfullyRead = true;
						} catch (NumberFormatException e) {
							// Ignore leading comments
							if (entrySuccessfullyRead) {
								Monitor.log(CustomDistribution.class,
										Level.WARN,
										"Couldn't parse cdf entry %s", line);
							}
						}
					} else {
						throw new AssertionError("To many columns in CSV.");
					}
				}
			}

		} catch (FileNotFoundException e) {
			throw new RuntimeException(
					"Could not open CSV file with CDF data (\"" + csvFilename
							+ "\")");
		} catch (IOException e) {
			throw new RuntimeException("Failed to read the CDF data (\""
					+ csvFilename + "\")");
		} finally {
			if (csv != null)
				try {
					csv.close();
				} catch (IOException e) {
					//
				}
		}

		Monitor.log(CustomDistribution.class, Level.INFO,
				"Read " + cdfData.size() + " unique entries from "
						+ csvFilename + "with " + counter
						+ " and got a scaling factor of " + scaleFactor);

		/*
		 * Scale entries to a value range of ]0,1] if scale == true. This has to
		 * be done only once this way.
		 */
		if (scale == true) {
            this.scaleFactor = scaleFactor;
			scaleCDF(scaleFactor);
		}
	}

    private void scaleCDF(double scaleFactor) {
        TreeMap<Double, Double> scaledCdfData = new TreeMap<Double, Double>();
        for (Entry<Double, Double> actEntry : cdfData.entrySet()) {
            scaledCdfData.put(actEntry.getKey(), actEntry.getValue()
                    / scaleFactor);
        }
        // replace original data by scaled data
        cdfData = scaledCdfData;
    }

    public void setScale(boolean scale) {
		this.scale = scale;
	}

    public double getScaleFactor() {
        return this.scaleFactor;
    }

    public Map<Double, Double> getMap() {
        return cdfData;
    }
}
