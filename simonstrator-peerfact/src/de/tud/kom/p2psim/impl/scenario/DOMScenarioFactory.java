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



package de.tud.kom.p2psim.impl.scenario;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import de.tud.kom.p2psim.api.scenario.Builder;
import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.api.scenario.Configurator;
import de.tud.kom.p2psim.api.scenario.Scenario;

/**
 * Factory to create a default scenario from a DOM subtree. Expected format:
 * <Scenario> <Action hostId=[int]" time="[long]"> "opName;param1;param2;..."/>.
 * </Action> </Scenario> There can be 0, 1 or more params separated by ";"
 * (default params delimiter). Comments and token delimiter can be set via
 * setXYDelimiter methods.
 * 
 * @author Konstantin Pussep
 * 
 */
// TODO write a JUnit test for it
public class DOMScenarioFactory extends AbstractScenarioFactory implements
		Builder {
	/**
	 * Separate parameters in the param list.
	 */
	private String paramsDelimiter = ";";

	private static final Logger log = Logger
			.getLogger(DOMScenarioFactory.class);

	//private Scenario scenario;

	// Element behaviorElem;

	// private int experimentSize;
	// private int seed;
	// private double timeDeviance;

	// private File actionsFile;

	public Scenario createScenario() {
		return scenario;
	}

	public void parse(Element elem, Configurator config) {
		// create the scenario wright now.
		ExtendedScenario scenario = newScenario();
		// List<OperationBasedScenarioAction> actions = new
		// LinkedList<OperationBasedScenarioAction>();
		try {
			int actionCounter = 0;
			for (Iterator it = elem.elementIterator(Configurator.ACTION_TAG); it
					.hasNext();) {
				Element actionElem = (Element) it.next();
				String hostId = actionElem.attributeValue("hostID");
				String time = actionElem.attributeValue("time");
				String line = actionElem.getTextTrim();
				String[] tokens = line.split(paramsDelimiter);
				assert tokens.length >= 1 : Arrays.asList(tokens);

				String method = tokens[0];
				String[] params = new String[tokens.length - 1];
				System.arraycopy(tokens, 1, params, 0, params.length);

				scenario.createActions(hostId, time, method, params);
				actionCounter++;
			}
			log.debug("Created " + actionCounter + " actions");
		} catch (Exception e) {
			throw new ConfigurationException("Failed to parse DOM element "
					+ elem.asXML() + " reason: ", e);
		}
	}

	/**
	 * Set the delimieter which will be used to separate parameter values for
	 * methods, default is ";".
	 * 
	 * @param paramsDelimiter
	 *            - some character, e.g. ";", "," or ":"
	 */
	public void setParamsDelimiter(String paramsDelimiter) {
		this.paramsDelimiter = paramsDelimiter;
	}

}
