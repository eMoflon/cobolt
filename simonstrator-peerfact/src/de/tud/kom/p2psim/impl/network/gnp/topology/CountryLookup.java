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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;

/**
 * This class manages the mapping of the 2-digits country code to country and
 * region used in the aggregated PingEr reports.
 * 
 * @author Gerald Klunker
 * @version 0.1, 09.01.2008
 * 
 */
public class CountryLookup implements Serializable {

	private static final long serialVersionUID = -3994762133062677848L;

	private HashMap<String, String[]> countryLookup = new HashMap<String, String[]>();

	private HashMap<String, String> pingErCountryRegions = new HashMap<String, String>();

	private String pingErCountryRegionFilename;

	// sorted list holding PingEr Data for using in a graphical frontend only
	private ArrayList<String> pingErCountry = new ArrayList<String>();

	private ArrayList<String> pingErRegion = new ArrayList<String>();

	/**
	 * 
	 * @param 2-digits country code
	 * @return dedicated GeoIP country name
	 */
	public String getGeoIpCountryName(String countryCode) {
		if (countryLookup.containsKey(countryCode))
			return countryLookup.get(countryCode)[0];
		else
			return null;
	}

	/**
	 * 
	 * @param 2-digits country code
	 * @return dedicated PingEr country name
	 */
	public String getPingErCountryName(String countryCode) {
		if (countryLookup.containsKey(countryCode))
			return countryLookup.get(countryCode)[1];
		else
			return null;
	}

	/**
	 * 
	 * @param country
	 *            2-digits country code or pingEr country name
	 * @return dedicated PingEr region name
	 */
	public String getPingErRegionName(String country) {
		if (countryLookup.containsKey(country))
			return countryLookup.get(country)[2];
		else if (pingErCountryRegions.containsKey(country))
			return pingErCountryRegions.get(country);
		return null;
	}

	/**
	 * Adds GeoIP country code and country name.
	 * 
	 * It will be assign automatically to PingEr Countries, if there are obvious
	 * consenses.
	 * 
	 * @param 2-digits country code
	 * @param dedicated
	 *            country name from GeoIP
	 */
	public void addCountryFromGeoIP(String countryCode, String country) {
		if (!countryLookup.containsKey(countryCode)) {
			String[] names = new String[3];
			names[0] = country;
			if (pingErCountryRegions.containsKey(country)) {
				names[1] = country;
				names[2] = pingErCountryRegions.get(country);
			}
			countryLookup.put(countryCode, names);
		}
	}

	/**
	 * Assign a country code (from GeoIP) to PingER country and/or region name.
	 * Attention: Nothing happens, if country code was not added before
	 * 
	 * @param 2-digits country code
	 * @param dedicated
	 *            country name from PingER
	 * @param dedicated
	 *            region name from PingER
	 */
	public void assignCountryCodeToPingErData(String code, String country,
			String region) {
		if (countryLookup.containsKey(code)) {
			String[] names = countryLookup.get(code);
			names[1] = country;
			names[2] = region;
		}
	}

	/**
	 * Import all country and region names, that are used by the PingER Project.
	 * The Country - Region Mapping File can be downloaded form the website of
	 * the project.
	 * (http://www-iepm.slac.stanford.edu/pinger/region_country.txt)
	 * 
	 * GeoIP countries will be assign automatically to PingEr Countries, if
	 * there are obviouse consensuses.
	 * 
	 * @param file
	 */
	public void importPingErCountryRegionFile(File file) {
		try {
			FileReader inputFilePingER = new FileReader(file);
			BufferedReader inputPingER = new BufferedReader(inputFilePingER);
			HashMap<String, String> countryToRegion = new HashMap<String, String>();
			String line = inputPingER.readLine();
			while (line != null) {
				String[] parts = line.split(",");
				countryToRegion.put(parts[0], parts[1]);
				line = inputPingER.readLine();
			}
			inputPingER.close();
			inputFilePingER.close();

			Set<String> codes = countryLookup.keySet();
			for (String cc : codes) {
				String geoIpCountry = getGeoIpCountryName(cc);
				if (geoIpCountry != null
						&& countryToRegion.containsKey(geoIpCountry)) {
					assignCountryCodeToPingErData(cc, geoIpCountry,
							countryToRegion.get(geoIpCountry));
				}
			}
			pingErCountryRegions.putAll(countryToRegion);
			this.pingErCountryRegionFilename = file.getAbsolutePath();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Import lockup data from an xml-element.
	 * 
	 * @param element
	 */
	public void importFromXML(Element element) {
		Iterator<Element> iter = element.elementIterator("CountryKey");
		while (iter.hasNext()) {
			Element variable = iter.next();
			String code = variable.attributeValue("code");
			String[] names = new String[3];
			names[0] = variable.attributeValue("countryGeoIP");
			names[1] = variable.attributeValue("countryPingEr");
			names[2] = variable.attributeValue("regionPingEr");
			countryLookup.put(code, names);
			pingErCountryRegions.put(names[1], names[2]);
		}
	}

	/**
	 * Import lockup data from an xml-file.
	 * 
	 * @param file
	 */
	public void importFromXML(File file) {
		try {
			SAXReader reader = new SAXReader(false);
			Document configuration = reader.read(file);
			Element root = configuration.getRootElement();
			importFromXML(root);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return xml-element containing current lookup data
	 */
	public Element exportToXML() {
		DefaultElement country = new DefaultElement("CountryLookup");
		Set<String> codeKeys = countryLookup.keySet();
		for (String code : codeKeys) {
			DefaultElement countryXml = new DefaultElement("CountryKey");
			countryXml.addAttribute("code", code);
			countryXml.addAttribute("countryGeoIP", getGeoIpCountryName(code));
			countryXml
					.addAttribute("countryPingEr", getPingErCountryName(code));
			countryXml.addAttribute("regionPingEr", getPingErRegionName(code));
			country.add(countryXml);
		}
		return country;
	}

	/**
	 * 
	 * @return set of all available country codes added
	 */
	public Set<String> getCountryCodes() {
		return countryLookup.keySet();
	}

	/**
	 * 
	 * @return sorted list of all available PingEr country names
	 */
	public ArrayList<String> getPingErCountrys() {
		if (pingErCountry.size() <= 1) {
			pingErCountry.clear();
			pingErCountry.addAll(pingErCountryRegions.keySet());
			pingErCountry.add("");
			Collections.sort(pingErCountry);
		}
		return pingErCountry;
	}

	/**
	 * 
	 * @return sorted list of all available PingEr region names
	 */
	public ArrayList<String> getPingErRegions() {
		if (pingErRegion.size() <= 1) {
			pingErRegion.clear();
			Set<String> region = new HashSet<String>();
			region.addAll(pingErCountryRegions.values());
			pingErRegion.addAll(region);
			pingErRegion.add("");
			Collections.sort(pingErRegion);
		}
		return pingErRegion;
	}

	/**
	 * Remove all countries that are not defined in the Set
	 * 
	 * @param countryCodes
	 *            Set of country codes
	 */
	public void keepCountries(Set<String> countryCodes) {
		countryLookup.keySet().retainAll(countryCodes);
	}

	/**
	 * 
	 * @param name
	 *            of country or region
	 * @return true, if there is a country or region with name exist
	 */
	public boolean containsPingErCountryOrRegion(String name) {
		for (String[] names : countryLookup.values()) {
			if (names[1] != null && names[1].equals(name))
				return true;
			if (names[2] != null && names[2].equals(name))
				return true;
		}
		return false;
	}

	/**
	 * 
	 * @return PingER Country - Region Mapping Filename (region_country.txt)
	 *         used for naming
	 */
	public String getPingErCountryRegionFilename() {
		return pingErCountryRegionFilename;
	}
}
