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

package de.tud.kom.p2psim.impl.topology.movement;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

public class BonnMotionDataGenerator {

	private String dataLocation = "data" + File.separator + "mobility"
			+ File.separator;

	private String experimentName;

	private String configFileEnd = ".params";

	private File configFile;

	private String traceFileEnd = ".movements.gz";

	private File traceFile;

	private String command;

	private List<String> arguments;
	
	private String postFix = "-File-1";
	
	private int xOffset = 0;
	
	private int yOffset = 0;
	
	@XMLConfigurableConstructor({ "command" })
	public BonnMotionDataGenerator(String command) {
		this.command = command;
		arguments = new Vector<String>();
	}

	public void setBonnMotionConfig(Properties bonnMotionConfig) {
		arguments.add(command);

		if (checkForFiles(bonnMotionConfig)) {
			Monitor.log(BonnMotionMovementModel.class, Level.WARN, traceFile
					+ " and " + configFile
					+ " already exist and need not to be created!");
		} else {
			arguments.add("-f" +  experimentName);
			arguments.add((String) bonnMotionConfig.remove("model"));
			arguments.add("-n " + (String) bonnMotionConfig.remove("n"));
			Iterator<Object> iter = bonnMotionConfig.keySet().iterator();
			String key = null;
			while (iter.hasNext()) {
				key = (String) iter.next();
				arguments.add("-" + key + "" + bonnMotionConfig.getProperty(key));
			}
			runBonnMotion();
		}
		
		Monitor.log(BonnMotionMovementModel.class, Level.WARN,
				"Writing the location of the created files into the config of PFS.KOM");
		Map<String, String> variables = Simulator.getConfigurator()
				.getVariables();
		variables.put("mobilityTraceFile" + postFix, traceFile.toString());
		variables.put("mobilityConfigFile" + postFix, configFile.toString());
		variables.put("xOffset" + postFix, Integer.toString(xOffset));
		variables.put("yOffset" + postFix, Integer.toString(yOffset));
		Simulator.getConfigurator().setVariables(variables);
	}

	public void setPostFix(String postFix) {
		this.postFix = "-File-" + postFix;
	}

	public void setXOffset(int xOffset) {
		this.xOffset = xOffset;
	}

	public void setYOffset(int yOffset) {
		this.yOffset = yOffset;
	}

	private void runBonnMotion() {
		Monitor.log(BonnMotionMovementModel.class, Level.WARN,
				"Running the creation!!!");
		Monitor.log(BonnMotionMovementModel.class, Level.WARN,
				arguments.toString());
		ProcessBuilder pb = new ProcessBuilder(arguments);
		try {
			
			pb.directory(new File(dataLocation));
			Process p = pb.start();
			BufferedReader read = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
			p.waitFor();
			while (read.ready()) {
				System.out.println(read.readLine());
			}
			read.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private boolean checkForFiles(Properties bonnMotionConfig) {
		experimentName = "run";
		Set<Object> keySet = bonnMotionConfig.keySet();
		Object[] keys = keySet.toArray(new Object[keySet.size()]);
		Arrays.sort(keys);
		for (Object k : keys) {
			experimentName = experimentName + "_" + (String) k + "_"
					+ bonnMotionConfig.getProperty((String) k);
		}
		experimentName = experimentName + postFix;
		
		traceFile = new File(dataLocation + experimentName + traceFileEnd);
		configFile = new File(dataLocation + experimentName + configFileEnd);

		Monitor.log(BonnMotionMovementModel.class, Level.WARN, "Instantiated "
				+ traceFile + " and " + configFile);

		return traceFile.exists() && configFile.exists();
	}

}
