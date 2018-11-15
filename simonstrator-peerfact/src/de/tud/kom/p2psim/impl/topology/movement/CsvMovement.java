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
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.network.SimNetInterface;
import de.tud.kom.p2psim.api.topology.Topology;
import de.tud.kom.p2psim.api.topology.movement.MovementInformation;
import de.tud.kom.p2psim.api.topology.movement.SimLocationActuator;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.movement.modular.ModularMovementModel;
import de.tud.kom.p2psim.impl.topology.movement.modular.attraction.AttractionPoint;
import de.tud.kom.p2psim.impl.topology.movement.modular.transition.FixedAssignmentStrategy;
import de.tudarmstadt.maki.simonstrator.api.Binder;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * Movement of the {@link AttractionPoint}s in the {@link ModularMovementModel}.
 * {@link AttractionPoint}s follow path given via a .csv file.
 * 
 * @author Nils Richerzhagen
 * @version 1.0, 02.08.2014
 */
public class CsvMovement extends AbstractMovementModel {

	private FixedAssignmentStrategy transitionStrategy;

	private PositionVector worldDimensions;

	private final String SEP = ";";

	private final String INTERMEDIATE_SEP = ",";

	private String file;

	private LinkedList<LinkedList<CsvPathInfo>> readPathInfos;

	private Map<SimLocationActuator, LinkedList<CsvPathInfo>> componentsPathsInfos;

	private Map<SimLocationActuator, CsvMovementInfo> stateInfo;

	/**
	 * 
	 * @param movementPointsFile
	 */
	@XMLConfigurableConstructor({ "movementPointsFile" , "minMovementSpeed", "maxMovementSpeed" })
	public CsvMovement(String movementPointsFile, double minMovementSpeed, double maxMovementSpeed) {
		super();
		this.worldDimensions = Binder.getComponentOrNull(Topology.class)
				.getWorldDimensions();
		this.file = movementPointsFile;
		this.readPathInfos = new LinkedList<LinkedList<CsvPathInfo>>();
		this.stateInfo = new LinkedHashMap<SimLocationActuator, CsvMovementInfo>();
		this.componentsPathsInfos = new LinkedHashMap<SimLocationActuator, LinkedList<CsvPathInfo>>();

		readData(minMovementSpeed, maxMovementSpeed);
	}

	@Override
	public void addComponent(SimLocationActuator component) {
		super.addComponent(component);
		LinkedList<CsvPathInfo> first = readPathInfos.removeFirst();
		componentsPathsInfos.put(component, first);
		stateInfo.put(component, new CsvMovementInfo());
	}

	@Override
	public void move() {
		Set<SimLocationActuator> comps = getComponents();
		for (SimLocationActuator comp : comps) {

			PositionVector pos = comp.getRealPosition();
			CsvMovementInfo info = stateInfo.get(comp);

			if (info.getRemainingSteps() == 0 || info.getDelta() == null) {
				// assign next delta and next steps for next path point
				if (!assignNextMovementInfo(comp)) {
					return;
				}
			}
			updatePosition(comp, pos.plus(info.getDelta()));
			info.setRemainingSteps(info.getRemainingSteps() - 1);
		}
	}

	protected boolean assignNextMovementInfo(SimLocationActuator comp) {
		CsvMovementInfo info = stateInfo.get(comp);

		PositionVector actPos = comp.getRealPosition();
		PositionVector delta = null;
		int steps = 1;

		if (componentsPathsInfos.get(comp).isEmpty()) {
			return false;
		}

		// PositionVector targetPos =
		CsvPathInfo pathInfo = componentsPathsInfos.get(comp).removeFirst();
		PositionVector targetPos = pathInfo.getNextPostion();
		
		if(actPos.getX() == targetPos.getX() && actPos.getY() == targetPos.getY()){
			new Error("New position is exactly on the same place where old is. Do not do that!");
		}

		double distancePerMoveOperation = pathInfo.getSpeed()
				* getTimeBetweenMoveOperations() / Time.SECOND;
		double distance = actPos.distanceTo(targetPos);

		steps = (int) Math.round(distance / distancePerMoveOperation);

		double xDelta = (targetPos.getX() - actPos.getX()) / steps;
		double yDelta = (targetPos.getY() - actPos.getY()) / steps;

		String groupId = transitionStrategy
				.getGroupIdOfAttractionPoint((AttractionPoint) comp);

		if (!(groupId == null)) {

			// Go offline whenever intervals says to do so.
			if (!pathInfo.isOnline()) {
				List<SimHost> hosts = Simulator.getInstance().getScenario()
						.getHosts().get(groupId);

				for (SimHost simHost : hosts) {
					for (SimNetInterface net : simHost.getNetworkComponent()
							.getSimNetworkInterfaces()) {
						if (net.isOnline())
							net.goOffline();
					}
				}
			} else if (pathInfo.isOnline()) {
				List<SimHost> hosts = Simulator.getInstance().getScenario()
						.getHosts().get(groupId);

				for (SimHost simHost : hosts) {

					for (SimNetInterface net : simHost.getNetworkComponent()
							.getSimNetworkInterfaces()) {
						if (net.isOffline())
							net.goOnline();
					}
				}
			}
		}

		delta = new PositionVector(xDelta, yDelta);

		info.setDelta(delta);
		info.setRemainingSteps(steps);
		return true;
	}

	/**
	 * Read the given csv file.
	 * 
	 * x, y, 'ONLINE'/'OFFLINE', min speed, max speed
	 * 
	 * if min speed == max speed = speed
	 */
	private void readData(double minMovementSpeed, double maxMovementSpeed) {
		readPathInfos.clear();
		boolean entrySuccessfullyRead = false;
		BufferedReader csv = null;
		try {
			csv = new BufferedReader(new FileReader(file));

			while (csv.ready()) {
				String line = csv.readLine();

				LinkedList<CsvPathInfo> currentPathInfos = new LinkedList<CsvPathInfo>();

				if (line.indexOf(SEP) > -1) {
					String[] parts = line.split(SEP);
					for (String actPart : parts) {
						String[] subParts = actPart.split(INTERMEDIATE_SEP);

						if (subParts.length == 5) {
							try {
								Double x = Double.parseDouble(subParts[0]);
								Double y = Double.parseDouble(subParts[1]);
								String online = subParts[2];
								online = online.replaceAll("\\s+","");
								Double minSpeed;
								Double maxSpeed;
								if(online.equals("OFFLINE")){
									minSpeed = Double
											.parseDouble(subParts[3]);
									maxSpeed = Double
											.parseDouble(subParts[4]);
								}
								else{
									minSpeed = minMovementSpeed;
									maxSpeed = maxMovementSpeed;
								}
//								log.error("Min speed: " + minSpeed + " max speed " + maxSpeed );

								if (x > worldDimensions.getX()
										|| y > worldDimensions.getY() || x < 0
										|| y < 0) {
									System.err.println("Skipped entry " + x
											+ ";" + y);
									continue;
								}

								CsvPathInfo actPathInfo = new CsvPathInfo(
										new PositionVector(x, y), online,
										minSpeed, maxSpeed);
								currentPathInfos.add(actPathInfo);
								entrySuccessfullyRead = true;

							} catch (NumberFormatException e) {
								// Ignore leading comments
								if (entrySuccessfullyRead) {
									// System.err.println("CSV ParseError " +
									// line);
								}
							}
						} else {
							throw new AssertionError("To many columns in CSV.");
						}
					}
				}
				// Put MovementInfos of one line into the full vector.
				readPathInfos.add(currentPathInfos);
			}
		} catch (Exception e) {
			System.err.println(e.toString());
		} finally {
			if (csv != null) {
				try {
					csv.close();
				} catch (IOException e) {
					//
				}
			}
		}
	}

	/**
	 * 
	 * @param transStrategy
	 */
	public void addTransitionStrategy(FixedAssignmentStrategy transStrategy) {
		this.transitionStrategy = transStrategy;
	}

	/**
	 * 
	 * @author Nils Richerzhagen
	 * @version 1.0, 16.07.2014
	 */
	public class CsvMovementInfo implements MovementInformation {

		private PositionVector delta;

		private int remainingSteps = 0;

		public void setDelta(PositionVector delta) {
			this.delta = delta;
		}

		public void setRemainingSteps(int remainingSteps) {
			this.remainingSteps = remainingSteps;
		}

		public PositionVector getDelta() {
			return delta;
		}

		public int getRemainingSteps() {
			return remainingSteps;
		}
	}

	/**
	 * 
	 * @author Nils Richerzhagen
	 * @version 1.0, 02.08.2014
	 */
	public class CsvPathInfo {
		private PositionVector nextPostion;

		private boolean online;

		private double minSpeed;

		private double maxSpeed;

		public CsvPathInfo(PositionVector nextPosition, String online,
				double minSpeed, double maxSpeed) {
			this.nextPostion = nextPosition;
			this.minSpeed = minSpeed;
			this.maxSpeed = maxSpeed;

			if (online.equals("ONLINE"))
				this.online = true;
			else if (online.equals("OFFLINE"))
				this.online = false;
			else
				throw new Error(online + " no valid String in CsvMovement");
		}

		public PositionVector getNextPostion() {
			return nextPostion;
		}

		public double getSpeed() {
			if (minSpeed == maxSpeed)
				return minSpeed;

			return getRandomDouble(minSpeed, maxSpeed);
		}

		public boolean isOnline() {
			return online;
		}

	}

}
