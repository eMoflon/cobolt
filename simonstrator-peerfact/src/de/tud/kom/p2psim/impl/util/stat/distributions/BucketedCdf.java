/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
 *
 * This file is part of PeerfactSim.KOM.
 *
 * PeerfactSim.KOM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * PeerfactSim.KOM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PeerfactSim.KOM.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package de.tud.kom.p2psim.impl.util.stat.distributions;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import de.tud.kom.p2psim.impl.util.BackToXMLWritable.BackWriter;
import de.tud.kom.p2psim.impl.util.Tuple;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.util.Distribution;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * 
 * 
 * @author Andreas Hemel, modified to fit into the Distribution-Framework and to
 *         be XML-configurable by Bjoern Richerzhagen
 * @version 1.0, 08.04.2012
 */
public class BucketedCdf implements Distribution {

	private static final Map<String, BucketedCdf> instances = new HashMap<String, BucketedCdf>();

	/** sum of all bucket weights */
	private int totalCount = 0;

	/** Maps from y-values (between 0 and 1) to x-values of the CDF */
	private final TreeMap<Double, Double> cdf = new TreeMap<Double, Double>();

	/**
	 * Create a BucketedCdf out of the given file. To support the
	 * XML-Configurator we do not force the singleton pattern - which is why
	 * this constructor is public.
	 * 
	 * @param path
	 */
	@XMLConfigurableConstructor({ "path" })
	public BucketedCdf(String path) {
		readDb(path);
		instances.put(path, this);
	}

	/**
	 * Access an instance of the CDF by config-file
	 * 
	 * @param path
	 * @return
	 */
	public static BucketedCdf getInstance(String path) {
		BucketedCdf cdf = instances.get(path);
		if (cdf == null) {
			cdf = new BucketedCdf(path);
		}
		return cdf;
	}

	private void readDb(String path) {
		try {
			FileInputStream fis = new FileInputStream(path);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			BufferedReader br = new BufferedReader(isr);

			ArrayList<Tuple<Double, Integer>> tmpList = new ArrayList<Tuple<Double, Integer>>();

			for (String line = br.readLine(); line != null; line = br.readLine()) {
				String parts[] = line.split("\t");
				if (parts.length != 3) {
					throw new RuntimeException("cdf input file broken");
				}

				double start = Double.parseDouble(parts[0]); // left border of bucket
				double end = Double.parseDouble(parts[1]); // right border of bucket
				int count = Integer.parseInt(parts[2]);  // weight of bucket
				double midpoint = start + (end - start) / 2.; // center of bucket
				totalCount += count;
				tmpList.add(new Tuple<Double, Integer>(midpoint, count));
			}

			int runningSum = 0;
			for (Tuple<Double, Integer> pair : tmpList) {
				double midpoint = pair.getA();
				int count = pair.getB();

				// calculate y-value of the cdf
				double fraction = ((double) runningSum) / ((double) totalCount);

				cdf.put(fraction, midpoint);
				runningSum += count;
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException("could not read footprint database: file not found");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("could not read footprint database: encoding troubles");
		} catch (IOException e) {
			throw new RuntimeException("could not read footprint database: IO error");
		}
	}

	/**
	 * Generate a random value whose distribution is based on the CDF.
	 * 
	 * The returned value is normally distributed between two discrete values of
	 * the CDF.
	 */
	public double getInverseSample() {
		double rnd = 0;
		do {
			rnd = Randoms.getRandom(BucketedCdf.class).nextDouble();
		} while (cdf.tailMap(rnd).isEmpty() || cdf.headMap(rnd).isEmpty());

		double upperY = cdf.tailMap(rnd).firstKey();
		double lowerY = cdf.headMap(rnd).lastKey();
		double upperX = cdf.get(upperY);
		double lowerX = cdf.get(lowerY);

		double yIntervalSize = upperY - lowerY;
		double yIntervalPos = (rnd - lowerY) / yIntervalSize;

		double xIntervalSize = upperX - lowerX;
		return lowerX + (yIntervalPos * xIntervalSize);
	}

	@Override
	public double returnValue() {
		/*
		 * FIXME is the intention of returnValue to get the inverse Sample?
		 */
		return getInverseSample();
	}

	public void writeBackToXML(BackWriter bw) {
		//
	}
}
