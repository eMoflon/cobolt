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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.api.scenario.Scenario;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * Factory to create a default scenario from a CSV file. Expected file format:
 * Host-Id(int) timepoint(long) opName(String) parameters(optional list of
 * Strings?). Default token delimiter is ' ' (space). Lines can be commented by
 * delimiter (default is "#"). Comments and token delimiter can be set via
 * setXYDelimiter methods.
 * 
 * @author Konstantin Pussep
 * @author Sebastian Kaune
 * @version 3.0, 13.12.2007
 * 
 */
public class CSVScenarioFactory extends AbstractScenarioFactory {
	String commentsDelimiter = "#";

	private String tokenDelimiter = " ";

	// Element behaviorElem;

	// private int experimentSize;
	// private int seed;
	// private double timeDeviance;

	File actionsFile;

    /**
	 * Set the file with the list of scenario actions
	 * 
	 * @param filename
	 *            - file name (with relative or absolute path)
	 */
	public void setActionsFile(String filename) {
		this.actionsFile = new File(filename);
	}

	/**
	 * Create one action or a group of actions from the given line. Expected
	 * line format:
	 * "(host-id|group-id) (timepoint|range) operationName (optional parameters)*"
	 * The actual creation of actions from the string specification is delegated
	 * to hosts.
	 * 
	 * @param line
	 *            action(s) specification
	 * @return number of actions created
	 */
	int parseActions(String line) {
		String[] tokens = line.split(tokenDelimiter);
		assert tokens.length >= 3 : Arrays.asList(tokens);

		// check the built-in commands of the scenario
		if (tokens[0].equals("_ECHO")) {
			long time = scenario.createTimePoints(1, tokens[1])[0];
			String echoText = line.substring(line.indexOf("\""));
			Event.scheduleWithDelay(time, new EventHandler() {

				public void eventOccurred(Object data, int type) {
					Monitor.log(
							CSVScenarioFactory.class,
							Level.INFO,
							"Scenario Echo: " + data.toString() + " at "
							+ Time.getCurrentTime());
				}

			}, echoText, 0);
			return 0;
		} else {

			// now calculate the ids of involved hosts
			// int[] hostIds = parseHostIds(tokens[0]);
			String hostIds = tokens[0];
			String timeInterval = tokens[1];
			String method = tokens[2];
			String[] params = new String[tokens.length - 3];// everything except
			// the 3 leading
			// tokens are params
			System.arraycopy(tokens, 3, params, 0, params.length);

			return scenario
					.createActions(hostIds, timeInterval, method, params);
		}
	}

	/**
	 * @param commentsDelimiter
	 *            - which character mark lines as comments
	 */
	public void setCommentsDelimiter(String commentsDelimiter) {
		this.commentsDelimiter = commentsDelimiter;
	}

	/**
	 * Set token delimiter, e.g. space, coma, tabulator. Typically columns are
	 * separated by spaces or tabulators.
	 * 
	 * @param tokenDelimiter
	 *            - token delimiter, e.g. ' ', '\t', ','
	 */
	public void setTokenDelimiter(String tokenDelimiter) {
		this.tokenDelimiter = tokenDelimiter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tud.kom.p2psim.api.scenario.ScenarioFactory#createScenario()
	 */
	public Scenario createScenario() {
		ExtendedScenario scenario = newScenario();

		// read file with actions, parse them and store as list.
		try {
			BufferedReader in = new BufferedReader(new FileReader(actionsFile));
			String line;
			int actionCounter = 0;
			while ((line = in.readLine()) != null) {
				if (line.length() == 0 || line.startsWith(commentsDelimiter))
					continue;
				else
					actionCounter += parseActions(line);
			}
			Monitor.log(CSVScenarioFactory.class, Level.INFO, "Created "
					+ actionCounter + " actions");
		} catch (IOException e) {
			throw new ConfigurationException("Failed to parse " + actionsFile
					+ " reason: ", e);
		}

		return scenario;
	}
}
