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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import de.tud.kom.p2psim.api.topology.movement.SimLocationActuator;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.topology.DefaultTopologyComponent;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

public class BonnMotionMovementModel extends AbstractMovementModel implements
		EventHandler {

	private final int EVENT_ID = 1;

	private Vector<String> mobilityTraceFiles;

	private Vector<String> mobilityConfigFiles;
	
	private Vector<PositionVector> offsets;
	
	private long hostCounter;

	private long movementInterval;

	private PositionVector previousPosition;

	private long previousTime;

	private long lastNotificationTime;
	
	private int fileCounter = 1;

	@XMLConfigurableConstructor({ "movementInterval" })
	public BonnMotionMovementModel(long movementInterval) {
		super();
		this.movementInterval = movementInterval;
		this.lastNotificationTime = 0;
		this.hostCounter = 1;
		
		this.mobilityTraceFiles = new Vector<String>();
		this.mobilityConfigFiles = new Vector<String>();
		this.offsets = new Vector<PositionVector>();
		
		Map<String, String> variables = Simulator.getConfigurator()
				.getVariables();
		
		String traceFile = variables.get("mobilityTraceFile-File-"+fileCounter);
		String configFile = variables.get("mobilityConfigFile-File-"+fileCounter);
		String xString = variables.get("xOffset-File-" + fileCounter);
		String yString = variables.get("yOffset-File-" + fileCounter);

		while (traceFile != null && configFile != null && xString != null
				&& yString != null) {
			this.mobilityTraceFiles.add(traceFile);
			this.mobilityConfigFiles.add(configFile);
			Integer xOffset = Integer.parseInt(xString);
			Integer yOffset = Integer.parseInt(yString);
			this.offsets.add(new PositionVector(xOffset.intValue(), yOffset
					.intValue()));

			fileCounter++;
			traceFile = variables.get("mobilityTraceFile-File-" + fileCounter);
			configFile = variables
					.get("mobilityConfigFile-File-" + fileCounter);
			xString = variables.get("xOffset-File-" + fileCounter);
			yString = variables.get("yOffset-File-" + fileCounter);
		}
		
		checkConfiguration();
		extractMovement();
	}

	@Override
	public void move() {
		// This method is not required, because the hosts are moved over an
		// trace-file.
	}

	private void checkConfiguration() {
		for (String s : mobilityConfigFiles) {
			StringBuffer sBuf = new StringBuffer();
			File configFile = new File(s);
			Properties props = new Properties();
			try {
				props.load(new FileInputStream(configFile));
				Map<String, String> variables = Simulator.getConfigurator()
						.getVariables();

				// FIXME Find an alternative way to ensure correct number of
				// nodes
				// Check for correct dimensions
				// if (Double.parseDouble(variables.get("WORLD-X")) != Double
				// .parseDouble((String) props.get("x"))
				// || Double.parseDouble(variables.get("WORLD-Y")) != Double
				// .parseDouble((String) props.get("y"))) {
				// sBuf.append("- The dimensions in the PFS-Config-File do not match the dimensions in the BonnMotion-Config-File!\n");
				// }

				// FIXME Find an alternative way to ensure correct number of
				// nodes
				// Check for correct number of nodes
				// if (Integer.parseInt(variables.get("movingPeersSize")) >
				// Integer
				// .parseInt((String) props.get("nn"))) {
				// sBuf.append("- The PFS-Config-File specifies more nodes than the BonnMotion-Config-File!\n");
				// }

				// Check for correct length of the simulation
				if ((Simulator.getEndTime() / (double) Simulator.SECOND_UNIT) > Double
						.parseDouble((String) props.get("duration"))) {
					sBuf.append("- The PFS-Config-File specifies a longer simulation than the BonnMotion-Config-File!\n");
				}
				if (sBuf.length() > 0) {
					throw new RuntimeException(sBuf.toString());
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void extractMovement() {
		for (int a=0;a<mobilityTraceFiles.size();a++) {
			PositionVector offset = offsets.get(a);
			File mobilityTraceFileObject = new File(mobilityTraceFiles.get(a));
			BufferedReader buf;
			try {
				buf = new BufferedReader(new InputStreamReader(
						new GZIPInputStream(new FileInputStream(
								mobilityTraceFileObject))));
				String line = null;
				String[] tokens = null;
				PositionVector nextPosition = null;
				long nextTime = -1;

				PositionVector direction = null;
				long timeSpan = -1;

				while (buf.ready()) {
					line = buf.readLine();
					tokens = line.split(" ");
					assert tokens.length % 3 == 0;
					previousPosition = new PositionVector(
							Double.valueOf(tokens[1]) + offset.getX(),
							Double.valueOf(tokens[2]) + offset.getY());
					previousTime = (long) (Double.parseDouble(tokens[0]) * Simulator.SECOND_UNIT);
					for (int i = 3; i < tokens.length; i += 3) {
						nextPosition = new PositionVector(
								Double.parseDouble(tokens[i + 1])+ offset.getX(),
								Double.parseDouble(tokens[i + 2])+ offset.getY());
						nextTime = (long) Double.parseDouble(tokens[i])
								* Simulator.SECOND_UNIT;

						// Check if a fragmentation should be done
						if (movementInterval != -1
								&& (nextPosition.getX() != previousPosition
										.getX())
								&& (nextPosition.getY() != previousPosition
										.getY())
								&& ((nextTime - previousTime) > movementInterval)) {

							// calculate the direction and the time interval
							// between
							// the previous and the next point
							direction = new PositionVector(nextPosition.getX()
									- previousPosition.getX(),
									nextPosition.getY()
											- previousPosition.getY());
							timeSpan = nextTime - previousTime;
							long deltaTime = movementInterval;

							Monitor.log(
									BonnMotionMovementModel.class,
									Level.DEBUG,
									"Creating "
											+ timeSpan
											/ movementInterval
									+ " intermediate points between "
									+ previousPosition + " and " + nextPosition
									+ " at time "
									+ Simulator.getFormattedTime(previousTime)
									+ " and "
									+ Simulator.getFormattedTime(nextTime));

							// add the intermediate steps
							while (deltaTime < timeSpan) {
								double x = previousPosition.getX() + deltaTime
										/ (double) timeSpan * direction.getX();
								double y = previousPosition.getY() + deltaTime
										/ (double) timeSpan * direction.getY();
								PositionVector interStep = new PositionVector(
										x, y);
								Monitor.log(
										BonnMotionMovementModel.class,
										Level.DEBUG,
										"Creating "
										+ interStep
										+ " for time "
										+ Simulator.getFormattedTime(deltaTime
												+ previousTime));
								Event.scheduleWithDelay(deltaTime
										+ previousTime, this,
										new BonnMotionEvent(interStep,
												hostCounter), EVENT_ID);
								deltaTime = deltaTime + movementInterval;
							}

							// Schedule the final position and set this position
							// and
							// time as the previous ones
							Event.scheduleWithDelay(nextTime, this,
									new BonnMotionEvent(nextPosition,
											hostCounter), EVENT_ID);

							previousPosition = nextPosition;
							previousTime = nextTime;

						} else {
							// Schedule the final position and set this position
							// and
							// time as the previous ones
							Event.scheduleWithDelay(nextTime, this,
									new BonnMotionEvent(nextPosition,
											hostCounter), EVENT_ID);

							previousPosition = nextPosition;
							previousTime = nextTime;
						}

					}
					hostCounter++;
				}
				Monitor.log(BonnMotionMovementModel.class, Level.WARN,
						"Successfully parsed " + mobilityTraceFiles.get(a)
						+ " and created movement for " + (hostCounter - 1)
						+ " hosts");
				buf.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void eventOccurred(Object content, int type) {
		if (type == EVENT_ID) {
			BonnMotionEvent bme = (BonnMotionEvent) content;
			Set<SimLocationActuator> components = getComponents();
			for (SimLocationActuator comp : components) {
				if (((DefaultTopologyComponent) comp).getHost().getHostId() == bme
						.getHostID()) {
					PositionVector newPos = bme.getNextPosition();
					updatePosition(comp, newPos);
					
					// FIXME Delete!! Only test-logger!
//					log.warn(Simulator.getFormattedTime(Simulator.getCurrentTime())+" ID = " + bme.getHostID() +" pos = " + pos);
				}
			}
		}
	}

	private class BonnMotionEvent {
		private PositionVector nextPosition;

		private long hostID;

		public BonnMotionEvent(PositionVector nextPosition, long hostID) {
			super();
			this.nextPosition = nextPosition;
			this.hostID = hostID;
		}

		public PositionVector getNextPosition() {
			return nextPosition;
		}

		public long getHostID() {
			return hostID;
		}

	}

}
