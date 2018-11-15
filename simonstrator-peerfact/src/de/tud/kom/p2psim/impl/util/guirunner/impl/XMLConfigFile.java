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

package de.tud.kom.p2psim.impl.util.guirunner.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.tud.kom.p2psim.impl.util.Tuple;
import de.tud.kom.p2psim.impl.util.toolkits.DOMToolkit;


public class XMLConfigFile implements ConfigFile {

	private File configFile;

	final List<Tuple<String, String>> variableTuples = new LinkedList<Tuple<String, String>>();

	final Map<String, String> variables = new LinkedHashMap<String, String>();
	
	final Map<String, String> changedVariables = new LinkedHashMap<String, String>();

	String desc = null;
	
	boolean parsed = false;

	private String seed = null;

	public XMLConfigFile(File configFile) {
		this.configFile = configFile;
	}

	@Override
	public void loadConfiguration(final ConfigLoadedCallback callback) {
		new Thread() {
			@Override
			public void run() {
				try {
					 parseCond();
				} catch (Exception e) {
					e.printStackTrace();
				}
				callback.loadingFinished();
			}
		}.start();
	}
	
	public String toString() {
		return "ConfigFile(" + configFile + ")";
	}

	private void parseContent() throws Exception {
		InputStream is = new FileInputStream(configFile);
		BufferedInputStream buf = new BufferedInputStream(is);
		DocumentBuilder b = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document doc = b.parse(buf);

		Element root = DOMToolkit.getFirstChildElemMatching(doc, "Configuration");
		if (root == null) throw new ConfigFileException("Cannot parse document. The root element 'Configuration' is missing");
		Element variableRoot = DOMToolkit.getFirstChildElemMatching(root, "Default");
		if (variableRoot != null) {
			for (Element elem : DOMToolkit.getAllChildElemsMatching(
					variableRoot, "Variable")) {
				String name = elem.getAttribute("name");
				String value = elem.getAttribute("value");
				if (name != null && value != null) {
					if ("seed".equalsIgnoreCase(name)) {
						seed = value;
					} else {
						variables.put(name, value);
						variableTuples.add(new Tuple<String, String>(name,
								value));
					}
				} else {
					log("Cannot add name/value of the attribute to map. Name or value attribute is missing.");
				}
			}
		} else {
			log("Cannot parse variables. There is no 'Default' element as child of 'Configuration'");
		}
		Element descElem = DOMToolkit.getFirstChildElemMatching(root, "Description");
		if (descElem != null) {
			desc = descElem.getTextContent().trim();
		} else log("Optional element Description is missing.");
	}
	
	void parseCond() {
		if (!parsed) {
			try {
				parseContent();
				parsed = true;
			} catch (Exception e) {
				log("Problems while parsing. Will not retry.");
				e.printStackTrace();
				parsed = true;
			}
		}
	}

	public List<Tuple<String, String>> getVariables() {
		parseCond();
		return variableTuples;
	}

	public String getDesc() {
		parseCond();
		return desc;
	}

	public File getFile() {
		return configFile;
	}

	public void log(Object logContent) {
		System.out.println(logContent);
	}

	static class ConfigFileException extends Exception {

		public ConfigFileException() {
			super();
		}

		public ConfigFileException(String message, Throwable cause) {
			super(message, cause);
		}

		public ConfigFileException(String message) {
			super(message);
		}

		public ConfigFileException(Throwable cause) {
			super(cause);

		}

	}

	public String getSeedInConfig() {
		parseCond();
		return seed ;
	}

	@Override
	public List<String> getVariations() {
		return Collections.emptyList();
	}

	@Override
	public List<String> getSelectedVariations() {
		return Collections.emptyList();
	}

	@Override
	public void setSelectedVariations(List<String> selectedVariation) {
		// Not supported with the XML configuration at the moment
	}

	@Override
	public void setVariable(String name, Object aValue) {
		if (!(aValue instanceof String)) return;
		
		String value = (String)aValue;
		
		changedVariables.put(name, value);
		for (Tuple<String, String> variable : variableTuples) {
			if (variable.getA().equals(name)) {
				variable.setB(value);
				return;
			}
		}
	}

	@Override
	public List<Tuple<String, String>> getModifiedVariables() {
		List<Tuple<String, String>> changedTuples = new LinkedList<Tuple<String, String>>();
		for (Entry<String, String> entry : changedVariables.entrySet()) {
			changedTuples.add(new Tuple<String, String>(entry.getKey(), entry
					.getValue()));
		}
		return changedTuples;
	}
}
