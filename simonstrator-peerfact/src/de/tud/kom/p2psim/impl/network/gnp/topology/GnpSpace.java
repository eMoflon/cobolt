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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.commons.math.stat.StatUtils;

/**
 * Implements the downhill simplx algorithm for positioning host in an
 * multi-dimensional space, so that the difference betwen the measured rtts and
 * the calculated euclidean distance is minimized.
 * 
 * @author Gerald Klunker
 * @version 0.1, 09.01.2008
 * 
 */
public class GnpSpace implements Comparable<GnpSpace> {

	private static final long serialVersionUID = 6941552219570807190L;

	private HostMap mapRef;

	private int noOfDimensions;

	private HashMap<Integer, GnpPosition> coordinateIndex;

	private GnpPosition[] monitorPositions;

	/*
	 * Attributes for statistical analysis of measured to calculated errors
	 */
	private double[] relativeErrorDistribution = new double[0];

	private LinkedList<Double>[] directionalRelativeErrors = new LinkedList[0];

	private HashSet<Host>[] hostsByRelativeError = new HashSet[0];

	private double maxRelativeError = 0;

	/*
	 * Status information about current calculation and progress will be used by
	 * a gui-thread
	 */
	public static int calculationStepStatus = 0;

	public static int calculationProgressStatus = 0;

	public static boolean calculationInProgress = false;

	/**
	 * 
	 * @param noOfDimensions
	 *            number of dimensions of the generated gnp space
	 * @param mapRef
	 *            reference to the related HostMap
	 */
	public GnpSpace(int noOfDimensions, HostMap mapRef) {
		super();

		this.mapRef = mapRef;
		this.noOfDimensions = noOfDimensions;

		this.coordinateIndex = new HashMap<Integer, GnpPosition>();
		this.monitorPositions = new GnpPosition[mapRef.getNoOfMonitors()];

		int c = 0;
		for (Host monitor : mapRef.getMonitorIndex().values()) {
			monitorPositions[c] = new GnpPosition(noOfDimensions, monitor, this);
			c++;
		}
	}

	/**
	 * 
	 * @return number of monitor positions
	 */
	protected int getNumberOfMonitors() {
		return monitorPositions.length;
	}

	/**
	 * 
	 * @param monitorIndex
	 *            internal index of monitor
	 * @param monitor
	 *            new monitor position
	 */
	private void setMonitorPosition(int monitorIndex, GnpPosition monitor) {
		monitorPositions[monitorIndex] = monitor;
	}

	/**
	 * 
	 * @param monitorIndex
	 * @return position of monitor with internal index monitorIndex
	 */
	protected GnpPosition getMonitorPosition(int monitorIndex) {
		return monitorPositions[monitorIndex];
	}

	/**
	 * 
	 * @return related HostMap object
	 */
	protected HostMap getMapRef() {
		return mapRef;
	}

	/**
	 * Calcuates the dimension of the gnp space.
	 * 
	 * @return array[x][y] x:pos in coordinate array, y=0: minimum of all
	 *         values, y=1: maximum of all values , y=2: max - min value
	 */
	private double[][] getDimension() {
		double[][] returnvalue = new double[this.noOfDimensions][3];
		double min;
		double max;
		for (int c = 0; c < this.noOfDimensions; c++) {
			min = this.monitorPositions[0].getGnpCoordinates(c);
			max = min;
			for (int d = 1; d < this.getNumberOfMonitors(); d++) {
				double current = this.monitorPositions[d].getGnpCoordinates(c);
				min = (min < current) ? min : current;
				max = (max > current) ? max : current;
			}
			returnvalue[c][0] = min;
			returnvalue[c][1] = max;
			returnvalue[c][2] = max - min;
		}
		return returnvalue;
	}

	/**
	 * 
	 * @return number of dimensions of the gnp space
	 */
	private int getNoOfDimensions() {
		return this.noOfDimensions;
	}

	/**
	 * Removes the gnp position of a host
	 * 
	 * @param hostIP
	 */
	protected void removePosition(long hostIP) {
		this.coordinateIndex.remove(hostIP);
	}

	/*
	 * ======================================================================
	 * The following Methods implements methods for the statistical analysis of
	 * the quality of positions according to the measured rtts.
	 * ======================================================================
	 */

	/**
	 * Iterates through all Positions to calculate statistical data like -
	 * statistic 1: maximum relative error - statistic 2: directional relative
	 * errors grouped by rtt - statistic 3: hosts grouped by maximum relative
	 * error - statistic 4: distribution of relative errors
	 */
	public void builtStatisticalData() {

		// set calculation status to 3 (calculation of statistical data)
		calculationStepStatus = 3;

		maxRelativeError = 0;
		directionalRelativeErrors = new LinkedList[21];
		hostsByRelativeError = new HashSet[21];
		ArrayList<Double> relativeErrorsList = new ArrayList<Double>(4000000);

		for (int d = 0; d < hostsByRelativeError.length; d++)
			hostsByRelativeError[d] = new HashSet<Host>();
		for (int d = 0; d < directionalRelativeErrors.length; d++)
			directionalRelativeErrors[d] = new LinkedList<Double>();

		calculationProgressStatus = 0;
		for (GnpPosition host : coordinateIndex.values()) {
			calculationProgressStatus++;
			double maxError = 0;
			for (GnpPosition monitor : monitorPositions) {
				double measuredDistace = host.getMeasuredRtt(monitor);
				if (measuredDistace > 0) {
					double directionalRelativeError = host
							.getDirectionalRelativError(monitor);
					double relativeError = Math.abs(directionalRelativeError);
					relativeErrorsList.add(relativeError);

					// statistic 1: maximum relative error
					if (maxError < relativeError)
						maxError = relativeError;

					// statistic 2: directional relative errors grouped by rtt
					int groupID = (int) Math.floor(measuredDistace / 50);
					if (groupID < 20)
						directionalRelativeErrors[groupID]
								.add(directionalRelativeError);
					else
						directionalRelativeErrors[20]
								.add(directionalRelativeError);
				}
				// statistic 3: hosts grouped by maximum relative error
				int groupID = (int) Math.floor(maxError * 10);
				if (groupID >= 20)
					hostsByRelativeError[20].add(host.getHostRef());
				else
					hostsByRelativeError[groupID].add(host.getHostRef());

			}
			// statistic 1: maximum relative error
			if (maxRelativeError < maxError)
				maxRelativeError = maxError;
		}

		// statistic 4: distribution of relative errors
		double[] relativeErrorsArray = new double[relativeErrorsList.size()];
		for (int c = 0; c < relativeErrorsList.size(); c++)
			relativeErrorsArray[c] = relativeErrorsList.get(c);
		relativeErrorDistribution = new double[100];
		for (int c = 1; c < 100; c++)
			relativeErrorDistribution[c] = StatUtils.percentile(
					relativeErrorsArray, c);

		// set calculation status to 0 (no calculation)
		calculationStepStatus = 0;
	}

	/**
	 * 
	 * @return array with distribution of relativ errors of all positions.
	 *         index: 0-100%
	 */
	public double[] getRelativeErrorDistribution() {
		return relativeErrorDistribution;
	}

	/**
	 * 
	 * @return array of lists, that contains error values. index x=0: 0-50ms,
	 *         x=1: 50-100ms, ... x=20: >1000ms
	 */
	public LinkedList<Double>[] getDirectionalRelativeErrorsGroupedByRtt() {
		return directionalRelativeErrors;
	}

	/**
	 * 
	 * @return array of sets, that contains host with related relative errors of
	 *         a certain amount. index x=0: error 0-0.1, x=1: error 0.1-0.2 ...
	 *         x=20 error > 2
	 */
	public HashSet<Host>[] getHostsGroupedByMaxRelativeError() {
		return hostsByRelativeError;
	}

	/**
	 * 
	 * @return maximum relative error within tis gnp space
	 */
	public double getMaximumRelativeError() {
		return maxRelativeError;
	}

	/*
	 * ======================================================================
	 * The following Methods implements 2 downhill simplex algorithms for the
	 * optimization of positions of monitors and host in the gnp space
	 * ======================================================================
	 */

	/**
	 * Run this method to generate GNP Positions for Monitors and Hosts
	 */
	public static GnpSpace getGnp(int noOfDimensions, int monitorResheduling,
			int hostResheduling, HostMap mapRef) {
		// positioning of monitors
		GnpSpace gnp = getGnpWithDownhillSimplex(noOfDimensions,
				monitorResheduling, mapRef);
		// positionning of ordinary hosts
		gnp.insertCoordinates(hostResheduling);
		return gnp;
	}

	/**
	 * 
	 * @param noOfDimensions
	 *            number of Dimensions must be smaller than number of Monitors
	 * @param monitorResheduling
	 *            number of rescheduling the downhill simplex
	 * @param mapRef
	 *            reference to HostMap
	 * @return optimized positions for Monitors
	 */
	private static GnpSpace getGnpWithDownhillSimplex(int noOfDimensions,
			int monitorResheduling, HostMap mapRef) {

		GnpSpace.calculationStepStatus = 1;
		GnpSpace.calculationInProgress = true;

		double alpha = 1.0;
		double beta = 0.5;
		double gamma = 2;
		double maxDiversity = 0.5;

		// N + 1 initial random Solutions
		int dhs_N = mapRef.getNoOfMonitors();
		ArrayList<GnpSpace> solutions = new ArrayList<GnpSpace>(dhs_N + 1);
		for (int c = 0; c < dhs_N + 1; c++)
			solutions.add(new GnpSpace(noOfDimensions, mapRef));

		// best and worst solution
		GnpSpace bestSolution = Collections.min(solutions);
		GnpSpace worstSolution = Collections.max(solutions);
		double bestError = bestSolution.getObjectiveValueMonitor();
		double worstError = worstSolution.getObjectiveValueMonitor();

		for (int z = 0; z < monitorResheduling; z++) {
			GnpSpace.calculationProgressStatus = z;

			// resheduling
			int count = 0;
			for (GnpSpace gnp : solutions) {
				if (gnp != bestSolution) {
					GnpPosition monitor = gnp.getMonitorPosition(count);
					monitor.diversify(gnp.getDimension(), maxDiversity);
					count++;
				}
			}

			// best and worst solution
			bestSolution = Collections.min(solutions);
			worstSolution = Collections.max(solutions);
			bestError = bestSolution.getObjectiveValueMonitor();
			worstError = worstSolution.getObjectiveValueMonitor();

			// stop criterion
			while (worstError - bestError > 0.00001 && calculationInProgress) {

				// move to center ...
				GnpSpace center = GnpSpace.getCenterSolution(solutions);
				GnpSpace newSolution1 = GnpSpace.getMovedSolution(
						worstSolution, center, 1 + alpha);
				double newError1 = newSolution1.getObjectiveValueMonitor();
				if (newError1 <= bestError) {
					int IndexOfWorstSolution = solutions.indexOf(worstSolution);
					GnpSpace newSolution2 = GnpSpace.getMovedSolution(
							worstSolution, center, 1 + alpha + gamma);
					double newError2 = newSolution2.getObjectiveValueMonitor();
					if (newError2 <= newError1) {
						solutions.set(IndexOfWorstSolution, newSolution2);
						bestError = newError2;
					} else {
						solutions.set(IndexOfWorstSolution, newSolution1);
						bestError = newError1;
					}
					bestSolution = solutions.get(IndexOfWorstSolution);
				} else if (newError1 < worstError) {
					int IndexOfWorstSolution = solutions.indexOf(worstSolution);
					solutions.set(IndexOfWorstSolution, newSolution1);
				} else { // ... or contract around best solution
					for (int c = 0; c < solutions.size(); c++) {
						if (solutions.get(c) != bestSolution)
							solutions.set(c, GnpSpace.getMovedSolution(
									solutions.get(c), bestSolution, beta));
					}
					bestSolution = Collections.min(solutions);
					bestError = bestSolution.getObjectiveValueMonitor();
				}
				worstSolution = Collections.max(solutions);
				worstError = worstSolution.getObjectiveValueMonitor();
			}

		}

		// Set the Coordinate Reference to the Peer
		for (int c = 0; c < bestSolution.getNumberOfMonitors(); c++) {
			bestSolution.getMonitorPosition(c).getHostRef()
					.setPositionReference(bestSolution.getMonitorPosition(c));
		}

		// GnpSpace.calculationStepStatus = 0;
		// GnpSpace.calculationInProgress = false;
		return bestSolution;
	}

	/**
	 * Calculates good positions for all Hosts in Map
	 * 
	 * @param monitorResheduling
	 *            number of rescheduling the downhill simplex
	 */
	private void insertCoordinates(int monitorResheduling) {
		GnpSpace.calculationStepStatus = 2;
		coordinateIndex.clear();
		HashMap<Integer, Host> peers = this.getMapRef().getHostIndex();
		int c = 0;
		for (Host host : peers.values()) {
			GnpSpace.calculationProgressStatus = c;
			if (host.getHostType() == Host.HOST) {
				GnpPosition coord = this.insertCoordinateDownhillSimplex(host,
						monitorResheduling);
				coordinateIndex.put(host.getIpAddress(), coord);
				c++;
			}
			if (!calculationInProgress)
				return;
		}
		GnpSpace.calculationStepStatus = 0;
		GnpSpace.calculationInProgress = false;
	}

	/**
	 * Calculates a good positions for the host
	 * 
	 * @param host
	 *            to position
	 * @param monitorResheduling
	 *            number of rescheduling the downhill simplex
	 * @return gnp position for peer
	 */
	private GnpPosition insertCoordinateDownhillSimplex(Host host,
			int monitorResheduling) {

		double alpha = 1.0;
		double beta = 0.5;
		double gamma = 2;
		double maxDiversity = 0.5;

		// N + 1 initial random Solutions
		ArrayList<GnpPosition> solutions = new ArrayList<GnpPosition>(
				noOfDimensions + 1);

		for (int c = -1; c < noOfDimensions; c++) {
			GnpPosition coord = new GnpPosition(noOfDimensions, host, this);
			solutions.add(coord);
		}

		// best and worst solution
		GnpPosition bestSolution = Collections.min(solutions);
		GnpPosition worstSolution = Collections.max(solutions);
		double bestError = bestSolution.getDownhillSimplexError();
		double worstError = worstSolution.getDownhillSimplexError();

		double newError = 0.0;

		for (int z = 0; z < monitorResheduling; z++) {

			// resheduling
			for (GnpPosition coord : solutions) {
				if (coord != bestSolution) {
					coord.diversify(this.getDimension(), maxDiversity);
				}
			}

			// best and worst solution
			bestSolution = Collections.min(solutions);
			worstSolution = Collections.max(solutions);
			bestError = bestSolution.getDownhillSimplexError();
			worstError = worstSolution.getDownhillSimplexError();

			// stop criterion
			while (worstError - bestError > 0.000001 && calculationInProgress) {

				// move to center ...
				GnpPosition center = GnpPosition.getCenterSolution(solutions);
				GnpPosition newSolution1 = GnpPosition.getMovedSolution(
						worstSolution, center, 1 + alpha);
				newError = newSolution1.getDownhillSimplexError();
				if (newError <= bestError) {
					GnpPosition newSolution2 = GnpPosition.getMovedSolution(
							worstSolution, center, 1 + alpha + gamma);
					int IndexOfWorstSolution = solutions.indexOf(worstSolution);
					if (newSolution2.getDownhillSimplexError() <= newError) {
						solutions.set(IndexOfWorstSolution, newSolution2);
					} else {
						solutions.set(IndexOfWorstSolution, newSolution1);
					}
					bestSolution = solutions.get(IndexOfWorstSolution);
					bestError = bestSolution.getDownhillSimplexError();
				} else if (newError < worstError) {
					int IndexOfWorstSolution = solutions.indexOf(worstSolution);
					solutions.set(IndexOfWorstSolution, newSolution1);
				} else { // ... or contract around best solution
					for (int c = 0; c < solutions.size(); c++) {
						if (solutions.get(c) != bestSolution)
							solutions.set(c, GnpPosition.getMovedSolution(
									solutions.get(c), bestSolution, beta));
					}
					bestSolution = Collections.min(solutions);
					bestError = bestSolution.getDownhillSimplexError();
				}
				worstSolution = Collections.max(solutions);
				worstError = worstSolution.getDownhillSimplexError();
			}
		}

		// Set the Coordinate Reference to the Peer
		host.setPositionReference(bestSolution);
		return bestSolution;
	}

	/**
	 * objective function for monitor downhill simplex
	 * 
	 * @return summation of monitor error values
	 */
	private double getObjectiveValueMonitor() {
		double value = 0.0;
		for (int i = 0; i < this.getNumberOfMonitors() - 1; i++) {
			for (int j = i + 1; j < this.getNumberOfMonitors(); j++) {
				value = value
						+ monitorPositions[i]
								.getDownhillSimplexError(monitorPositions[j]);
			}
		}
		return value;
	}

	/**
	 * Method must be overwrite to sort different GnpSpaces in order of the
	 * quality of monitor positions.
	 * 
	 * Is needed for the positioning of monitors with the downhill simplex
	 * 
	 */
	public int compareTo(GnpSpace gnp) {
		double val1 = this.getObjectiveValueMonitor();
		double val2 = gnp.getObjectiveValueMonitor();
		if (val1 < val2)
			return -1;
		if (val1 > val2)
			return 1;
		else
			return 0;
	}

	/**
	 * Static method generates a new GnpSpace according to the downhill simplex
	 * operator
	 * 
	 * @param solutions
	 * @return center solution
	 */
	private static GnpSpace getCenterSolution(ArrayList<GnpSpace> solutions) {
		GnpSpace returnValue = new GnpSpace(solutions.get(0)
				.getNoOfDimensions(), solutions.get(0).getMapRef());
		for (int c = 0; c < returnValue.getNumberOfMonitors(); c++) {
			ArrayList<GnpPosition> coords = new ArrayList<GnpPosition>();
			for (int d = 0; d < solutions.size(); d++) {
				coords.add(solutions.get(d).getMonitorPosition(c));
			}
			returnValue.setMonitorPosition(c, GnpPosition
					.getCenterSolution(coords));
		}
		return returnValue;
	}

	/**
	 * Static method generates a new GnpSpace according to the downhill simplex
	 * operator
	 * 
	 * @param solution
	 * @param moveToSolution
	 * @param moveFactor
	 * @return moved solution
	 */
	private static GnpSpace getMovedSolution(GnpSpace solution,
			GnpSpace moveToSolution, double moveFactor) {
		GnpSpace returnValue = new GnpSpace(solution.getNoOfDimensions(),
				solution.getMapRef());
		for (int c = 0; c < returnValue.getNumberOfMonitors(); c++) {
			returnValue.setMonitorPosition(c, GnpPosition.getMovedSolution(
					solution.getMonitorPosition(c), moveToSolution
							.getMonitorPosition(c), moveFactor));
		}
		return returnValue;
	}

}
