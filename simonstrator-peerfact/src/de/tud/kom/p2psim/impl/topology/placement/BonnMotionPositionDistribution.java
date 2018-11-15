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

package de.tud.kom.p2psim.impl.topology.placement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.topology.PositionVector;

public class BonnMotionPositionDistribution extends PositionDistribution {


	private Vector<String> mobilityTraceFiles;
	
	private Vector<PositionVector> offsets;

	private List<PositionVector> availablePositions;

	private int counter;
	
	private int fileCounter = 1;

	public BonnMotionPositionDistribution() {
		super();
		
		this.mobilityTraceFiles = new Vector<String>();
		this.offsets = new Vector<PositionVector>();
		
		Map<String, String> variables = Simulator.getConfigurator()
				.getVariables();
		System.out.println(variables.toString());
		String traceFile = variables.get("mobilityTraceFile-File-"+fileCounter);
		String configFile = variables.get("mobilityConfigFile-File-"+fileCounter);
		String xString = variables.get("xOffset-File-"+fileCounter);
		String yString = variables.get("yOffset-File-"+fileCounter);
		
		
		while (traceFile != null && configFile != null && xString != null
				&& yString != null) {
			Integer xOffset = Integer.parseInt(xString);
			Integer yOffset = Integer.parseInt(yString);
			this.mobilityTraceFiles.add(traceFile);
			this.offsets.add(new PositionVector(xOffset.intValue(), yOffset
					.intValue()));
			
			fileCounter++;
			traceFile = variables.get("mobilityTraceFile-File-"+fileCounter);
			configFile = variables.get("mobilityConfigFile-File-"+fileCounter);
			xString = variables.get("xOffset-File-"+fileCounter);
			yString = variables.get("yOffset-File-"+fileCounter);
		}
		availablePositions = new LinkedList<PositionVector>();
		extractPositions();
		counter = 0;
	}

	@Override
	public PositionVector getNextPosition() {
		if (counter >= availablePositions.size()) {
			throw new RuntimeException(
					"There are more Hosts configured than available in the corresponding BonnMotionTraceFile."
							+ " Please configure less hosts or run BonnMotion with the correct number of hosts.");
		}
		PositionVector pos = availablePositions.get(counter);
		counter++;
		return pos;
	}

	private void extractPositions() {
		for (int a =0;a<mobilityTraceFiles.size();a++) {
			File mobilityTraceFileObject = new File(mobilityTraceFiles.get(a));
			PositionVector offset = offsets.get(a);
			try {

				BufferedReader buf = new BufferedReader(new InputStreamReader(
						new GZIPInputStream(new FileInputStream(
								mobilityTraceFileObject))));

				String line = null;
				String[] tokens = null;
				while (buf.ready()) {
					line = buf.readLine();
					tokens = line.split(" ");
					availablePositions.add(new PositionVector(Double
							.parseDouble(tokens[1]) + offset.getX(), Double
							.parseDouble(tokens[2]) + offset.getY()));
				}
				buf.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
