/*
 * Copyright (c) 2005-2011 KOM - Multimedia Communications Lab
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


package de.tud.kom.p2psim.impl.network.gnp.topology;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB.Country;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB.GlobalSummaryRelation;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB.PingErRegion;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB.RegionRegionSummaryRelation;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB.SummaryRelation;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import umontreal.iro.lecuyer.probdist.LognormalDist;

/**
 * This Class Implements a container for the PingER summary reports used as a
 * lookup table for rtt, packet loss and jitter distribution
 * 
 * @author Gerald Klunker
 * @version 0.1, 05.02.2008
 * 
 */
public class PingErLookup {

	private HashMap<String, String> files = new HashMap<String, String>();

	public enum DataType {
		MIN_RTT, AVERAGE_RTT, VARIATION_RTT, PACKET_LOSS
	}

	private HashMap<String, HashMap<String, LinkProperty>> data = new HashMap<String, HashMap<String, LinkProperty>>();

	private LinkProperty averageLinkProperty;

	/**
	 * 
	 * @param from
	 *            PingEr Country or Region String
	 * @param to
	 *            PingEr Country or Region String
	 * @return minimum RTT
	 */
	public double getMinimumRtt(String from, String to) {
		return data.get(from).get(to).minRtt;
	}

	/**
	 * 
	 * @param from
	 *            PingEr Country or Region String
	 * @param to
	 *            PingEr Country or Region String
	 * @return average RTT
	 */
	public double getAverageRtt(String from, String to) {
		return data.get(from).get(to).averageRtt;
	}

	/**
	 * 
	 * @param from
	 *            PingEr Country or Region String
	 * @param to
	 *            PingEr Country or Region String
	 * @return IQR of RTT
	 */
	public double getRttVariation(String from, String to) {
		return data.get(from).get(to).delayVariation;
	}

	/**
	 * 
	 * @param from
	 *            PingEr Country or Region String
	 * @param to
	 *            PingEr Country or Region String
	 * @return packet Loss Rate in Percent
	 */
	public double getPacktLossRate(String from, String to) {
		return data.get(from).get(to).packetLoss;
	}

	/**
	 * 
	 * @param ccFrom
	 *            2-digits Country Code
	 * @param ccTo
	 *            2-digits Country Code
	 * @param cl
	 *            GeoIP to PingEr Dictionary
	 * @return log-normal jitter distribution
	 */
	public LognormalDist getJitterDistribution(String ccFrom, String ccTo,
			CountryLookup cl) {
		return getLinkProperty(ccFrom, ccTo, cl).getJitterDistribution();
	}

	/**
	 * 
	 * @param ccFrom
	 *            2-digits Country Code
	 * @param ccTo
	 *            2-digits Country Code
	 * @param cl
	 *            GeoIP to PingEr Dictionary
	 * @return minimum RTT
	 */
	public double getMinimumRtt(String ccFrom, String ccTo, CountryLookup cl) {
		return getLinkProperty(ccFrom, ccTo, cl).minRtt;
	}

	/**
	 * 
	 * @param ccFrom
	 *            2-digits Country Code
	 * @param ccTo
	 *            2-digits Country Code
	 * @param cl
	 *            GeoIP to PingEr Dictionary
	 * @return average RTT
	 */
	public double getAverageRtt(String ccFrom, String ccTo, CountryLookup cl) {
		return getLinkProperty(ccFrom, ccTo, cl).averageRtt;
	}

	/**
	 * 
	 * @param ccFrom
	 *            2-digits Country Code
	 * @param ccTo
	 *            2-digits Country Code
	 * @param cl
	 *            GeoIP to PingEr Dictionary
	 * @return packet Loss Rate in Percent
	 */
	public double getPacktLossRate(String ccFrom, String ccTo, CountryLookup cl) {
		return getLinkProperty(ccFrom, ccTo, cl).packetLoss;
	}

	/**
	 * 
	 * @param from
	 *            PingEr Country or Region String
	 * @param to
	 *            PingEr Country or Region String
	 * @return LinkProperty object that contains all PingEr informations for a
	 *         link
	 */
	private LinkProperty getLinkProperty(String from, String to) {
		return data.get(from).get(to);
	}

	/**
	 * Method will search the available PingER Data for best fit with ccFrom and
	 * ccTo. If there is no Country-Country connections other possibilities are
	 * testet like Country-Region, Region-Country, Region-Region also in
	 * opposite direction. If there is no PingEr Data for the pair a
	 * World-Average Link Property will be returned.
	 * 
	 * @param ccFrom
	 *            2-digits Country Code
	 * @param ccTo
	 *            2-digits Country Code
	 * @param cl
	 *            GeoIP to PingEr Dictionary
	 * @return LinkProperty object that contains all PingEr informations for a
	 *         link
	 */
	private LinkProperty getLinkProperty(String ccFrom, String ccTo,
			CountryLookup cl) {

		String countrySender = cl.getPingErCountryName(ccFrom);
		String countryReceiver = cl.getPingErCountryName(ccTo);
		String regionSender = cl.getPingErRegionName(ccFrom);
		String regionReceiver = cl.getPingErRegionName(ccTo);

		if (contains(countrySender, countryReceiver))
			return getLinkProperty(countrySender, countryReceiver);
		if (contains(countrySender, regionReceiver))
			return getLinkProperty(countrySender, regionReceiver);
		if (contains(regionSender, countryReceiver))
			return getLinkProperty(regionSender, countryReceiver);
		if (contains(regionSender, regionReceiver))
			return getLinkProperty(regionSender, regionReceiver);

		if (contains(countryReceiver, countrySender))
			return getLinkProperty(countryReceiver, countrySender);
		if (contains(countryReceiver, regionSender))
			return getLinkProperty(countryReceiver, regionSender);
		if (contains(regionReceiver, countrySender))
			return getLinkProperty(regionReceiver, countrySender);
		if (contains(regionReceiver, regionSender))
			return getLinkProperty(regionReceiver, regionSender);

		return getAverageLinkProperty();
	}

	/**
	 * 
	 * @param from
	 *            PingEr Country or Region String
	 * @param to
	 *            PingEr Country or Region String
	 * @return true if there are PinEr Data for the pair
	 */
	private boolean contains(String from, String to) {
		if (data.containsKey(from)) {
			if (data.get(from).containsKey(to)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param from
	 *            PingEr Country or Region String
	 * @param to
	 *            PingEr Country or Region String
	 * @param value
	 * @param dataType
	 */
	private void setData(String from, String to, double value, DataType dataType) {
		if (!data.containsKey(from))
			data.put(from, new HashMap<String, LinkProperty>());
		if (!data.containsKey(to))
			data.put(to, new HashMap<String, LinkProperty>());
		if (!data.get(from).containsKey(to))
			data.get(from).put(to, new LinkProperty());
		LinkProperty values = data.get(from).get(to);
		if (dataType == DataType.MIN_RTT)
			values.minRtt = value;
		else if (dataType == DataType.AVERAGE_RTT)
			values.averageRtt = value;
		else if (dataType == DataType.VARIATION_RTT)
			values.delayVariation = value;
		else if (dataType == DataType.PACKET_LOSS)
			values.packetLoss = value;
	}

	/**
	 * Loads the Class Attributes from an XML Element
	 * 
	 * @param element
	 */
	public void loadFromXML(Element element) {
		for (Iterator<Element> iter = element.elementIterator("SummaryReport"); iter
				.hasNext();) {
			Element variable = iter.next();
			String regionFrom = variable.attributeValue("from");
			String regionTo = variable.attributeValue("to");
			double minRtt = Double.parseDouble(variable
					.attributeValue("minimumRtt"));
			double averageRtt = Double.parseDouble(variable
					.attributeValue("averageRtt"));
			double delayVariation = Double.parseDouble(variable
					.attributeValue("delayVariation"));
			double packetLoss = Double.parseDouble(variable
					.attributeValue("packetLoss"));
			setData(regionFrom, regionTo, minRtt, DataType.MIN_RTT);
			setData(regionFrom, regionTo, averageRtt, DataType.AVERAGE_RTT);
			setData(regionFrom, regionTo, delayVariation,
					DataType.VARIATION_RTT);
			setData(regionFrom, regionTo, packetLoss, DataType.PACKET_LOSS);
		}
	}

	/**
	 * Export the Class Attributes to an XML Element
	 * 
	 * @param element
	 */
	public Element exportToXML() {
		DefaultElement pingEr = new DefaultElement("PingErLookup");
		Set<String> fromKeys = data.keySet();
		for (String from : fromKeys) {
			Set<String> toKeys = data.get(from).keySet();
			for (String to : toKeys) {
				DefaultElement pingErXml = new DefaultElement("SummaryReport");
				pingErXml.addAttribute("from", from);
				pingErXml.addAttribute("to", to);
				pingErXml.addAttribute("minimumRtt", String
						.valueOf(getMinimumRtt(from, to)));
				pingErXml.addAttribute("averageRtt", String
						.valueOf(getAverageRtt(from, to)));
				pingErXml.addAttribute("delayVariation", String
						.valueOf(getRttVariation(from, to)));
				pingErXml.addAttribute("packetLoss", String
						.valueOf(getPacktLossRate(from, to)));
				pingEr.add(pingErXml);
			}
		}
		return pingEr;
	}

	/**
	 * Loads PingEr Summary Reports in CVS-Format as provided on the PingER
	 * Website
	 * 
	 * @param file
	 * @param dataType
	 */
	public void loadFromTSV(File file, DataType dataType) {
		String[][] data = parseTsvFile(file);
		for (int i = 1; i < data.length - 1; i++) { // To
			for (int j = 1; j < data[i].length; j++) { // From
				if (!data[i][j].equals("."))
					setData(data[0][j].replace('+', ' '), data[i][0].replace(
							'+', ' '), Double.parseDouble(data[i][j]), dataType);
			}
		}
		files.put(file.getName(), dataType.name());
	}

	/**
	 * 
	 * @param file
	 * @return TSV-File Data in an 2-dimensional String Array
	 */
	private String[][] parseTsvFile(File file) {
		ArrayList<String[]> tempResult = new ArrayList<String[]>();
		String[][] result = new String[1][1];
		try {
			FileReader inputFile = new FileReader(file);
			BufferedReader input = new BufferedReader(inputFile);
			String line = input.readLine();
			while (line != null) {
				tempResult.add(line.split("\t"));
				line = input.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return tempResult.toArray(result);
	}

	/**
	 * 
	 * @return world-average LinkProperty Object
	 */
	private LinkProperty getAverageLinkProperty() {
		if (averageLinkProperty == null) {
			int counter = 0;
			averageLinkProperty = new LinkProperty();
			Set<String> fromKeys = data.keySet();
			for (String from : fromKeys) {
				Set<String> toKeys = data.get(from).keySet();
				for (String to : toKeys) {
					averageLinkProperty.minRtt += getMinimumRtt(from, to);
					averageLinkProperty.averageRtt += getAverageRtt(from, to);
					averageLinkProperty.delayVariation += getRttVariation(from,
							to);
					averageLinkProperty.packetLoss += getPacktLossRate(from, to);
					counter++;
				}
			}
			averageLinkProperty.minRtt /= counter;
			averageLinkProperty.averageRtt /= counter;
			averageLinkProperty.delayVariation /= counter;
			averageLinkProperty.packetLoss /= counter;
		}
		return averageLinkProperty;
	}

	/**
	 * 
	 * @return loaded PingEr TSV-Files (map: File -> DataType)
	 */
	public HashMap<String, String> getFiles() {
		return files;
	}

	/**
	 * 
	 * @return reference to the PingER Adjacency List (used by the GUI only)
	 */
	public HashMap<String, HashMap<String, LinkProperty>> getData() {
		return data;
	}

	/**
	 * This class encapsulates the PingER summary Data for one aggregated
	 * Country/Region to Country/Region link.
	 * 
	 * The calculation of the log-normal jitter distribution is implemented
	 * within this class.
	 * 
	 */
	public class LinkProperty {

		protected double minRtt = 0;

		protected double averageRtt = 0;

		protected double delayVariation = 0; // IQR

		protected double packetLoss = 0;

		private LognormalDist jitterDistribution;

		/**
		 * 
		 * @return log_normal jitter distribution calculated with the iqr and
		 *         expectet jitter
		 */
		public LognormalDist getJitterDistribution() {
			if (jitterDistribution == null) {
				JitterParameter optimized = getJitterParameterDownhillSimplex(
						averageRtt - minRtt, delayVariation);
				jitterDistribution = new LognormalDist(optimized.m, optimized.s);
				Monitor.log(PingErLookup.class, Level.DEBUG,
						"Set lognormal Jitter-Distribution with Average Jitter of "
								+ optimized.getAverageJitter()
								+ " ("
								+ (averageRtt - minRtt)
								+ ") and an IQR of "
								+ optimized.getIQR()
								+ " ("
								+ delayVariation
								+ ")");
			}
			return jitterDistribution;
		}

		/**
		 * Implemenation of a downhill simplex algortihm that finds the
		 * log-normal parameters mu and sigma that minimized the error between
		 * measured expectation and iqr and the resulting expectation and iqr.
		 * 
		 * @param expectation
		 *            value
		 * @param iqr
		 *            variation
		 * @return JitterParameter object with the log-normal parameter mu and
		 *         sigma
		 */
		private JitterParameter getJitterParameterDownhillSimplex(
				double expectation, double iqr) {

			ArrayList<JitterParameter> solutions = new ArrayList<JitterParameter>();
			solutions.add(new JitterParameter(0.1, 0.1, expectation, iqr));
			solutions.add(new JitterParameter(0.1, 5.0, expectation, iqr));
			solutions.add(new JitterParameter(5.0, 0.1, expectation, iqr));
			Collections.sort(solutions);

			// 100 interations are enough for good results
			for (int c = 0; c < 100; c++) {
				JitterParameter newSolution = getNewParameter1(solutions,
						expectation, iqr);
				if (newSolution != null
						&& newSolution.getError() < solutions.get(0).getError()) {
					JitterParameter newSolution2 = getNewParameter2(solutions,
							expectation, iqr);
					if (newSolution2 != null
							&& newSolution2.getError() < newSolution.getError()) {
						solutions.remove(2);
						solutions.add(newSolution2);
					} else {
						solutions.remove(2);
						solutions.add(newSolution);
					}
				} else if (newSolution != null
						&& newSolution.getError() < solutions.get(2).getError()) {
					solutions.remove(2);
					solutions.add(newSolution);
				} else {
					solutions.get(1).m = solutions.get(1).m + 0.5
							* (solutions.get(0).m - solutions.get(1).m);
					solutions.get(2).m = solutions.get(2).m + 0.5
							* (solutions.get(0).m - solutions.get(2).m);
					solutions.get(1).s = solutions.get(1).s + 0.5
							* (solutions.get(0).s - solutions.get(1).s);
					solutions.get(2).s = solutions.get(2).s + 0.5
							* (solutions.get(0).s - solutions.get(2).s);
				}
				Collections.sort(solutions);
			}
			return solutions.get(0);
		}

		/**
		 * movement of factor 2 to center of solutions
		 * 
		 * @param solutions
		 * @param expectation
		 * @param iqr
		 * @return moved solution
		 */
		private JitterParameter getNewParameter1(
				ArrayList<JitterParameter> solutions, double expectation,
				double iqr) {
			double middleM = (solutions.get(0).m + solutions.get(1).m + solutions
					.get(2).m) / 3.0;
			double middleS = (solutions.get(0).s + solutions.get(1).s + solutions
					.get(2).s) / 3.0;
			double newM = middleM + (solutions.get(0).m - solutions.get(2).m);
			double newS = middleS + (solutions.get(0).s - solutions.get(2).s);
			if (newS > 0)
				return new JitterParameter(newM, newS, expectation, iqr);
			else
				return null;
		}

		/**
		 * movement of factor 3 to center of solutions
		 * 
		 * @param solutions
		 * @param expectation
		 * @param iqr
		 * @return moved solution
		 */
		private JitterParameter getNewParameter2(
				ArrayList<JitterParameter> solutions, double expectation,
				double iqr) {
			double middleM = (solutions.get(0).m + solutions.get(1).m + solutions
					.get(2).m) / 3.0;
			double middleS = (solutions.get(0).s + solutions.get(1).s + solutions
					.get(2).s) / 3.0;
			double newM = middleM + 2
					* (solutions.get(0).m - solutions.get(2).m);
			double newS = middleS + 2
					* (solutions.get(0).s - solutions.get(2).s);
			if (newS > 0)
				return new JitterParameter(newM, newS, expectation, iqr);
			else
				return null;
		}

		@Override
		public String toString() {
			String min = (minRtt == -1) ? "-" : String.valueOf(minRtt);
			String average = (averageRtt == -1) ? "-" : String
					.valueOf(averageRtt);
			String delayVar = (delayVariation == -1) ? "-" : String
					.valueOf(delayVariation);
			String loss = (packetLoss == -1) ? "-" : String.valueOf(packetLoss);
			return min + " / " + average + " / " + delayVar + " / " + loss;
		}

		/**
		 * Container the log-normal distribution parameters. Used in the
		 * Downhill Simplex method.
		 * 
		 */
		private class JitterParameter implements Comparable<JitterParameter> {

			double m;

			double s;

			double ew;

			double iqr;

			public JitterParameter(double m, double s, double ew, double iqr) {
				this.m = m;
				this.s = s;
				this.ew = ew;
				this.iqr = iqr;
			}

			/**
			 * error will be minimized within the downhill simplx algorithm
			 * 
			 * @return error (variation between measured expectation and iqr and
			 *         the resulting log-normal expectation and iqr.
			 */
			public double getError() {
				LognormalDist jitterDistribution = new LognormalDist(m, s);
				double error1 = Math.pow((iqr - (jitterDistribution
						.inverseF(0.75) - jitterDistribution.inverseF(0.25)))
						/ iqr, 2);
				double error2 = Math.pow((ew - Math.exp(m
						+ (Math.pow(s, 2) / 2.0)))
						/ ew, 2);
				return error1 + error2;
			}

			public int compareTo(JitterParameter p) {
				double error1 = this.getError();
				double error2 = p.getError();
				if (error1 < error2)
					return -1;
				else if (error1 > error2)
					return 1;
				else
					return 0;
			}

			/**
			 * 
			 * @return expectation value of the log-normal distribution
			 */
			public double getAverageJitter() {
				return Math.exp(m + (Math.pow(s, 2) / 2.0));
			}

			/**
			 * 
			 * @return iqr of the log-normal distribution
			 */
			public double getIQR() {
				LognormalDist jitterDistribution = new LognormalDist(m, s);
				return jitterDistribution.inverseF(0.75)
						- jitterDistribution.inverseF(0.25);
			}

			@Override
			public String toString() {
				LognormalDist jitterDistribution = new LognormalDist(m, s);
				double iqr1 = jitterDistribution.inverseF(0.75)
						- jitterDistribution.inverseF(0.25);
				double ew1 = Math.exp(m + (Math.pow(s, 2) / 2.0));
				return "m: " + m + " s: " + s + " Error: " + getError()
						+ " iqr: " + iqr1 + " ew: " + ew1;
			}

		}

	}

	/**
	 * 
	 * @author leo@relevantmusic.de
	 * @param db
	 */
	public void insertIntoRelationalDB(NetMeasurementDB db) {	
		Set<String> fromKeys = data.keySet();
		int succRelationsCounter = 0;
		int failRelationsCounter = 0;
		for (String from : fromKeys) {
			
			Country ctFrom = db.getStringAddrObjFromStr(Country.class, from);
			PingErRegion regFrom = db.getStringAddrObjFromStr(PingErRegion.class, from);
			Set<String> toKeys = data.get(from).keySet();
			for (String to : toKeys) {
				
				double minRtt = getMinimumRtt(from, to);
				double avgRtt = getAverageRtt(from, to);
				double delVar = getRttVariation(from, to);
				double pLoss = getPacktLossRate(from, to);
				
				//System.out.println("Handling PingER entry " + from + ", " + to);
				
				Country ctTo = db.getStringAddrObjFromStr(Country.class, to);
				PingErRegion regTo = db.getStringAddrObjFromStr(PingErRegion.class, to);
				
				boolean anyRelationAdded = false;
				if (ctFrom != null && ctTo != null) {
					SummaryRelation rel = db.getSummaryRelFrom(ctFrom, ctTo);
					if (rel != null)
						Monitor.log(
								PingErLookup.class,
								Level.DEBUG,
								"Conflict. Found "
										+ rel
										+ " at country "
										+ ctFrom
										+ " and country "
										+ ctTo
										+ " where a new entry should be placed.");
					db.new CountryCountrySummaryRelation(ctFrom, ctTo, minRtt, avgRtt, delVar, pLoss);
					anyRelationAdded = true;
				}
				if (ctFrom != null && regTo != null) {
					SummaryRelation rel = db.getSummaryRelFrom(ctFrom, regTo);
					if (rel != null)
						Monitor.log(
								PingErLookup.class,
								Level.DEBUG,
								"Conflict. Found "
										+ rel
										+ " at country "
										+ ctFrom
										+ " and region "
										+ regTo
										+ " where a new entry should be placed.");
					db.new CountryRegionSummaryRelation(ctFrom, regTo, minRtt, avgRtt, delVar, pLoss);
					anyRelationAdded = true;
				}
				if (regFrom != null && ctTo != null) {
					SummaryRelation rel = db.getSummaryRelFrom(regFrom, ctTo);
					if (rel != null)
						Monitor.log(
								PingErLookup.class,
								Level.DEBUG,
								"Conflict. Found "
										+ rel
										+ " at region "
										+ regFrom
										+ " and country "
										+ ctTo
										+ " where a new entry should be placed.");
					db.new RegionCountrySummaryRelation(regFrom, ctTo, minRtt, avgRtt, delVar, pLoss);
					anyRelationAdded = true;
				}
				if (regFrom != null && regTo != null) {
					SummaryRelation rel = db.getSummaryRelFrom(regFrom, regTo);
					if (rel != null)
						Monitor.log(
								PingErLookup.class,
								Level.DEBUG,
								"Conflict. Found "
										+ rel
										+ " at region "
										+ regFrom
										+ " and region "
										+ regTo
										+ " where a new entry should be placed.");
					db.new RegionRegionSummaryRelation(regFrom, regTo, minRtt, avgRtt, delVar, pLoss);
					anyRelationAdded = true;
				}
				//assert anyRelationAdded : getWarning(relationsCounter, from, ctFrom, regFrom, to, ctTo, regTo);
				if (!anyRelationAdded) {
					Monitor.log(
							PingErLookup.class,
							Level.WARN,
							getWarning(succRelationsCounter, from, ctFrom,
									regFrom, to, ctTo, regTo));
					failRelationsCounter ++;	
				} else {
					succRelationsCounter++;
				}
			}
		}
		
		createGlobalSumRel(db);
		
		Monitor.log(PingErLookup.class, Level.INFO, "Successfully put "
				+ succRelationsCounter
				+ " entries into the database. Failed relations: "
				+ failRelationsCounter);
	}

	private void createGlobalSumRel(NetMeasurementDB db) {
		List<RegionRegionSummaryRelation> objs = db.getAllObjects(RegionRegionSummaryRelation.class);
		
		int sz = objs.size();
		
		if (objs.size() == 0) throw new AssertionError("Can not create a global summary relation when no region-region summary relations are available.");
		
		double avgRttAcc = 0;
		double minRttAcc = 0;
		double dVarAcc = 0;
		double pLossAcc = 0;
		
		for (SummaryRelation sumRel : objs) {
			avgRttAcc += sumRel.getAvgRtt();
			minRttAcc += sumRel.getMinRtt();
			dVarAcc += sumRel.getdVar();
			pLossAcc += sumRel.getpLoss();
		}
		
		GlobalSummaryRelation sumRel = db.new GlobalSummaryRelation(minRttAcc/sz, avgRttAcc/sz, dVarAcc/sz, pLossAcc/sz);
		
		Monitor.log(
				PingErLookup.class,
				Level.INFO,
				"Created a global summary relation with minRtt="
						+ sumRel.getMinRtt() + ", avgRtt=" + sumRel.getAvgRtt()
						+ ", dVar=" + sumRel.getdVar() + ", pLoss="
						+ sumRel.getpLoss());
		
	}

	private String getWarning(int relationsCounter, String from, Country ctFrom,
			PingErRegion regFrom, String to, Country ctTo, PingErRegion regTo) {
		return "No PingER summary relation could be found that matches the countries or regions in the database: " +						
		"Entry strings were: from=" + from + " to=" + to + ". Found ctFrom=" + (ctFrom != null) + " ctTo=" + (ctTo != null) + " regFrom=" + (regFrom != null) + " regTo=" + (regTo != null) + 
				"Successfully" +" added " + relationsCounter + " relations until now.";
		
		
	}

}
