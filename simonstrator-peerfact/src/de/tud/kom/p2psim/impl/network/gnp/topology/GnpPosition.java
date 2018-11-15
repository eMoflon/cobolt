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

import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;

/**
 * This class implements a NetPosition for a GNP-Based calculation of round trip
 * times. Therefore it includes methods for error estimation and methods for
 * positioning by a downhill simplex algorithm in the GnpSpace class
 * 
 * @author Gerald Klunker
 * @version 0.1, 09.01.2008
 * 
 */

public class GnpPosition implements Location, Comparable<GnpPosition> {

	private static final long serialVersionUID = -1103996725403557900L;

	private double[] gnpCoordinates;

	private GnpSpace gnpRef;

	private Host hostRef;

	private double error = -1.0;

	/**
	 * 
	 * @param gnpCoordinates
	 *            coordinate array for new position
	 */
	public GnpPosition(double[] gnpCoordinates) {
		super();
		this.gnpCoordinates = gnpCoordinates;
	}

	/**
	 * Object will be initialized with a random position. Position must be
	 * random according to the downhill simplex
	 * 
	 * @param noOfDimensions
	 *            number of dimensions
	 * @param hostRef
	 *            related Host object
	 * @param gnpRef
	 *            related GnpSpace object
	 */
	public GnpPosition(int noOfDimensions, Host hostRef, GnpSpace gnpRef) {
		super();
		gnpCoordinates = new double[noOfDimensions];
		this.hostRef = hostRef;
		this.gnpRef = gnpRef;
		for (int c = 0; c < gnpCoordinates.length; c++)
			gnpCoordinates[c] = Randoms.getRandom(GnpPosition.class)
					.nextDouble();
	}

	/**
	 * 
	 * @param dimension
	 * @param maxDiversity
	 */
	public void diversify(double[][] dimension, double maxDiversity) {
		for (int c = 0; c < this.gnpCoordinates.length; c++) {
			double rand = (2 * maxDiversity * Math.random()) - maxDiversity;
			gnpCoordinates[c] = gnpCoordinates[c] + (rand * dimension[c][2]);
		}
		error = -1.0;
	}

	/**
	 * reposition
	 * 
	 * @param pos
	 *            position in the coordinate array
	 * @param value
	 *            new value at position pos
	 */
	public void setGnpCoordinates(int pos, double value) {
		gnpCoordinates[pos] = value;
		error = -1.0;
	}

	/**
	 * 
	 * @return the related GnpSpace object
	 */
	private GnpSpace getGnpRef() {
		return gnpRef;
	}

	/**
	 * 
	 * @return the related Host object
	 */
	public Host getHostRef() {
		return hostRef;
	}

	/**
	 * 
	 * @return number of dimensions
	 */
	public int getNoOfDimensions() {
		return gnpCoordinates.length;
	}

	/**
	 * 
	 * @param pos
	 *            position in the coordinate array
	 * @return value at position pos
	 */
	public double getGnpCoordinates(int pos) {
		return gnpCoordinates[pos];
	}

	/**
	 * Calculates the sum of all errors according to the downhill simplex
	 * operator.
	 * 
	 * @return error
	 */
	public double getDownhillSimplexError() {
		if (error < 0.0) {
			error = 0.0;
			for (int c = 0; c < getGnpRef().getNumberOfMonitors(); c++) {
				error += getDownhillSimplexError(getGnpRef()
						.getMonitorPosition(c));
			}
		}
		return error;
	}

	/**
	 * Calculates the error to a monitor according to the downhill simplex
	 * operator
	 * 
	 * @param monitor
	 * @return error
	 */
	public double getDownhillSimplexError(GnpPosition monitor) {
		double calculatedDistance = this.distanceTo(monitor);
		double measuredDistance = this.getMeasuredRtt(monitor);
		if (Double.compare(measuredDistance, Double.NaN) == 0)
			return 0.0;
		double error = Math.pow((calculatedDistance - measuredDistance)
				/ calculatedDistance, 2);
		return error;
	}

	/**
	 * Calculates an error, that indicates the deviation of the measured vs. the
	 * calculated rtt.
	 * 
	 * @param monitor
	 * @return error value
	 */
	public double getDirectionalRelativError(GnpPosition monitor) {
		double calculatedDistance = this.distanceTo(monitor);
		double measuredDistance = this.getMeasuredRtt(monitor);
		if (Double.compare(measuredDistance, Double.NaN) == 0)
			return Double.NaN;
		double error = (calculatedDistance - measuredDistance)
				/ Math.min(calculatedDistance, measuredDistance);
		return error;
	}

	/**
	 * Method must be overwrite to sort different GnpPositions in order of their
	 * quality.
	 * 
	 * Is needed for the positioning with the downhill simplex
	 * 
	 */
	public int compareTo(GnpPosition arg0) {
		double val1 = this.getDownhillSimplexError();
		double val2 = arg0.getDownhillSimplexError();
		if (val1 < val2)
			return -1;
		if (val1 > val2)
			return 1;
		else
			return 0;
	}

	/**
	 * 
	 * @return Comma-separated list of coordinates
	 */
	public String getCoordinateString() {
		if (gnpCoordinates.length == 0) {
			return "";
		} else {
			String result = String.valueOf(gnpCoordinates[0]);
			for (int c = 1; c < gnpCoordinates.length; c++)
				result = result + "," + gnpCoordinates[c];
			return result;
		}
	}

	/**
	 * 
	 * @param monitor
	 * @return measured rtt to monitor, nan if no rtt was measured
	 */
	public double getMeasuredRtt(GnpPosition monitor) {
		return this.getHostRef().getRtt(monitor.getHostRef());
	}

	/**
	 * @return euclidean distance
	 */
	public double getDistance(Location point) {
		GnpPosition coord = (GnpPosition) point;
		double distance = 0.0;
		for (int c = 0; c < gnpCoordinates.length; c++)
			distance += Math.pow(
					gnpCoordinates[c] - coord.getGnpCoordinates(c), 2);
		return Math.sqrt(distance);
	}

	@Override
	public int getTransmissionSize() {
		return 16; // 2 * double
	}

	/**
	 * Static method generates a new GnpPosition according to the downhill
	 * simplex operator
	 * 
	 * @param solution
	 * @param moveToSolution
	 * @param moveFactor
	 * @return new position
	 */
	public static GnpPosition getMovedSolution(GnpPosition solution,
			GnpPosition moveToSolution, double moveFactor) {
		GnpPosition returnValue = new GnpPosition(solution.getNoOfDimensions(),
				solution.getHostRef(), solution.getGnpRef());
		for (int c = 0; c < solution.getNoOfDimensions(); c++) {
			double newCoord = (moveToSolution.getGnpCoordinates(c) - solution
					.getGnpCoordinates(c))
					* moveFactor + solution.getGnpCoordinates(c);
			returnValue.setGnpCoordinates(c, newCoord);
		}
		return returnValue;
	}

	/**
	 * Static method generates a new GnpPosition according to the downhill
	 * simplex operator
	 * 
	 * @param solution
	 * @param moveToSolution
	 * @param moveFactor
	 * @return new position
	 */
	public static GnpPosition getCenterSolution(ArrayList<GnpPosition> solutions) {
		GnpPosition returnValue = new GnpPosition(solutions.get(0)
				.getNoOfDimensions(), solutions.get(0).getHostRef(), solutions
				.get(0).getGnpRef());
		for (int d = 0; d < solutions.size(); d++) {
			for (int c = 0; c < solutions.get(0).getNoOfDimensions(); c++) {
				returnValue.setGnpCoordinates(c, returnValue
						.getGnpCoordinates(c)
						+ solutions.get(d).getGnpCoordinates(c));
			}
		}
		for (int c = 0; c < returnValue.getNoOfDimensions(); c++) {
			returnValue.setGnpCoordinates(c, returnValue.getGnpCoordinates(c)
					/ solutions.size());
		}
		return returnValue;
	}

	public GnpPosition clone() {
		return new GnpPosition(gnpCoordinates);
	}
	
	@Override
	public float bearingTo(Location dest) {
		throw new AssertionError(
				"bearingTo is not defined for this Position-Type");
	}
	
	@Override
	public double distanceTo(Location dest) {
		return getDistance(dest);
	}
	
	@Override
	public long getAgeOfLocation() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public double getLatitude() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public double getLongitude() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void set(Location l) {
		throw new UnsupportedOperationException();
	}
}
