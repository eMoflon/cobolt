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



package de.tud.kom.p2psim;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.tud.kom.p2psim.impl.simengine.Simulator;

/**
 * This class is used to run simulations in <a
 * href="http://www.peerfact.org/">PeerfactSim.KOM</a>.
 * 
 * @author Konstantin Pussep
 * @author Sebastian Kaune
 * @version 3.0, 04.12.2007
 * 
 */
public class SimulatorRunner implements Runnable {

	private String[] args;

	/**
	 * Set private to prevent instantiation.
	 * 
	 */
	protected SimulatorRunner(String[] args) {
		this.args = args;
	}

	/**
	 * This method can be used to run a simulation. The expected arguments are:
	 * <code>config file</code> and an optional list of zero or many variable
	 * assignments<code>(variable=value)*")</code> or from the command line
	 * <code> java Scenario {config file} {variable=value}*</code>.
	 * 
	 * @param args
	 *            expect an array with the name of the configuration file and
	 *            optional variable assignments
	 */
	public static void main(String[] args) {
		new SimulatorRunner(args).run();
	}

	private static Map<String, String> parseVariables(String[] args, int startAt) {
		Map<String, String> variables = new LinkedHashMap<String, String>();
		for (int j = startAt; j < args.length; j++) {
			if (!args[j].contains("=")) continue;
			String[] tokens = args[j].split("=");
			assert tokens.length == 2 : "Bad format " + args[j];
			variables.put(tokens[0], tokens[1]);
		}
		return variables;
	}

	private static String parseVariation(String[] args, int startAt) {
		for (int j = startAt; j < args.length; j++) {
			if (args[j].contains(":")) {
				return args[j].split(":")[1];
			}
		}
		
		return null;
	}
	
	@Override
	public void run() {
		if (args.length >= 1) {
			Simulator sim = Simulator.getInstance();
			String variation = parseVariation(args, 1);
			Map<String, String> variables = parseVariables(args, 1);
			List<String> variations = new LinkedList<String>(
					Arrays.asList(variation.split(";")));
			sim.configure(args[0], variables, variables, variations);
			configure(sim, args[0], variables);
			sim.start(shallThrowExceptions());
		} else {
			System.err
					.println("usage: Scenario <config file> (variation:name) (<variable=value>)*");
		}
	}
	
	protected boolean shallThrowExceptions() {
		return false;
	}
	
	protected void configure(Simulator sim, String configFile, Map<String, String> variables) {
		//Nothing to do here
	}
}
