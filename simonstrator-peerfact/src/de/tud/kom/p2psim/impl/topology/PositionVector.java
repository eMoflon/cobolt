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

package de.tud.kom.p2psim.impl.topology;

import java.awt.Point;
import java.util.Arrays;

import com.vividsolutions.jts.geom.Coordinate;

import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;

/**
 * N-Dimensional Vector containing a Position. Wherever possible, applications
 * and overlay should only use the {@link Position}-interface to implement their
 * functionality.
 * 
 * Just a minor note: if you add functionality, change existing functionality,
 * or "fix bugs", please comment AND SIGN your changes - you might
 * (unintentionally) break other people's code! This is a core component within
 * Peerfact - so each change might have a lot of undesired side effects!
 * 
 * CHANGELOG
 * 
 * - 14/08/08 Bjoern Richerzhagen: removed a number of unused, uncommented, and
 * dubious methods from this class. Fixed the replace-bug (discovered by Nils)
 * with little overhead. Implemented assertions with assertions - previous
 * method introduces unwanted overhead outside of development settings. Removed
 * the dubious equals-tolerance, which violates the hashCode contract. Seriously
 * guys...
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 04/25/2011
 */
public class PositionVector implements Location {

	private int dimensions;

	/**
	 * The private (!) coordinates of this vector. Ensures, that all necessary
	 * transforms can be performed in the getter-methods.
	 */
	private double[] values;

	/**
	 * Create a new Position Vector
	 * 
	 * @param dimensions
	 */
	public PositionVector(int dimensions) {
		if (dimensions < 2) {
			throw new AssertionError("Less than 2 Dimensions make no sense.");
		}
		this.dimensions = dimensions;
		this.values = new double[dimensions];
	}

	/**
	 * Clone a PositionVector
	 * 
	 * @param vec
	 */
	public PositionVector(PositionVector vec) {
		this(vec.getDimensions());
		for (int i = 0; i < vec.getDimensions(); i++) {
			setEntry(i, vec.getEntry(i));
		}
	}

	/**
	 * Convenience Constructor, initializes a Vector with values.length
	 * Dimensions and sets Entries, using the callback setEntry
	 * 
	 * @param values
	 */
	public PositionVector(double... values) {
		this(values.length);
		for (int i = 0; i < values.length; i++) {
			setEntry(i, values[i]);
		}
	}

	/**
	 * Number of Dimensions
	 * 
	 * @return
	 */
	public final int getDimensions() {
		return dimensions;
	}

	/**
	 * returns the nth position in the coord-Vector, starting with 0
	 * 
	 * @param dim
	 * @return
	 */
	public double getEntry(int dim) {
		return values[dim];
	}

	/**
	 * Saves a new value. Implementations might perform error control or
	 * additional scaling/translation
	 * 
	 * @param dim
	 * @param value
	 */
	public void setEntry(int dim, double value) {
		values[dim] = value;
	}

	/**
	 * Sets all entries.
	 * 
	 * @param values
	 */
	public void setEntries(double... values) {
		assert values.length == dimensions;
		for (int i = 0; i < values.length; i++) {
			setEntry(i, values[i]);
		}
	}

	/**
	 * Getter for the common X dimension.
	 * 
	 * @return value of dimension 0
	 */
	public double getX() {
		return getEntry(0);
	}

	/**
	 * Getter for the common Y dimension.
	 * 
	 * @return value of dimension 1
	 */
	public double getY() {
		return getEntry(1);
	}

	/**
	 * Getter for the common Z dimension.
	 * 
	 * @return value of dimension 2
	 */
	public double getZ() {
		return getEntry(2);
	}

	/**
	 * Modifies the current positionVector-instace by adding the delta-vector.
	 * Addition is done for each element of the vector.
	 * 
	 * @param delta
	 */
	public void add(PositionVector delta) {
		assert dimensions == delta.getDimensions();
		for (int i = 0; i < dimensions; i++) {
			setEntry(i, getEntry(i) + delta.getEntry(i));
		}
	}

	/**
	 * Subtract a vector from the given vector
	 * 
	 * @param delta
	 */
	public void subtract(PositionVector delta) {
		assert dimensions == delta.getDimensions();
		for (int i = 0; i < dimensions; i++) {
			setEntry(i, getEntry(i) - delta.getEntry(i));
		}
	}

	/**
	 * Multiply this vector with a scalar value
	 * 
	 * @param multi
	 */
	public void multiplyScalar(double multi) {
		for (int i = 0; i < dimensions; i++) {
			setEntry(i, multi * getEntry(i));
		}
	}

	/**
	 * Converts this vector to its normalized form (ie. its length is equal to
	 * one)
	 */
	public void normalize() {
		double hyp = 0.0;

		for (int i = 0; i < dimensions; i++) {
			hyp += getEntry(i) * getEntry(i);
		}

		hyp = Math.sqrt(hyp);

		for (int i = 0; i < dimensions; i++) {
			setEntry(i, getEntry(i) / hyp);
		}
	}

	/**
	 * Additive arithmetic. Produces a new vector as result. Current vector is
	 * not changed. If you want the current vector instance to change, you
	 * should use add instead.
	 * 
	 * @param delta
	 * @return addition of this vector plus delta vector
	 */
	public PositionVector plus(PositionVector delta) {
		assert dimensions == delta.getDimensions();
		PositionVector result = new PositionVector(dimensions);
		for (int i = 0; i < dimensions; i++) {
			result.setEntry(i, this.getEntry(i) + delta.getEntry(i));
		}
		return result;
	}

	/**
	 * Subtractive arithmetic. Produces a new vector as result. Current vector
	 * is not changed. If you want the current vector instance to change, you
	 * should use subtract instead.
	 * 
	 * @param delta
	 * @return subtraction of this vector minus delta vector
	 */
	public PositionVector minus(PositionVector delta) {
		assert dimensions == delta.getDimensions();
		PositionVector result = new PositionVector(dimensions);
		for (int i = 0; i < dimensions; i++) {
			result.setEntry(i, this.getEntry(i) - delta.getEntry(i));
		}
		return result;
	}

	@Override
	public int getTransmissionSize() {
		return getDimensions() * 8;
	}

	/**
	 * Mainly for drawing purposes, Representation of the first two dimensions
	 * as a Point
	 * 
	 * @return
	 */
	public Point asPoint() {
		return new Point((int) getEntry(0), (int) getEntry(1));
	}

	/**
	 * Representation of the Vector as a Double-Array
	 * 
	 * @return
	 */
	public double[] asDoubleArray() {
		return Arrays.copyOf(values, dimensions);
	}

	/**
	 * Cast of 2 or 3-dimensional PositionVector to jts geometry library
	 * coordinates.
	 * 
	 * @return 2 or 3 dimensional coordinates
	 */
	public Coordinate asCoordinate() {
		if (dimensions < 2 || dimensions > 3) {
			throw new AssertionError(
					"Cast to Coordinate only possible with two or three dimensional PositionVector");
		}
		if (this.dimensions == 2)
			return new Coordinate(getX(), getY());
		else
			return new Coordinate(getX(), getY(), getZ());
	}

	@Override
	public String toString() {
		return "PositionVector " + Arrays.toString(values);
	}

	@Override
	public PositionVector clone() {
		/*
		 * If you extend Position Vector, make sure to overwrite this method!
		 */
		return new PositionVector(this); // use clone constructor
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dimensions;
		result = prime * result + Arrays.hashCode(values);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PositionVector other = (PositionVector) obj;
		if (dimensions != other.dimensions)
			return false;

		return Arrays.equals(values, other.values);
	}

	/**
	 * Change this position vector by dividing each coordinate through the
	 * corresponding coordinate of the multiplicator
	 * 
	 * @param divisor
	 */
	public void divide(PositionVector divisor) {
		assert dimensions == divisor.getDimensions();
		for (int i = 0; i < dimensions; i++) {
			setEntry(i, this.getEntry(i) / divisor.getEntry(i));
		}
	}

	/**
	 * Change this position vector by multiplying each coordinate with the
	 * corresponding coordinate of the multiplicator
	 * 
	 * @param multiplicator
	 */
	public void multiply(PositionVector multiplicator) {
		assert dimensions == multiplicator.getDimensions();
		for (int i = 0; i < dimensions; i++) {
			setEntry(i, this.getEntry(i) * multiplicator.getEntry(i));
		}
	}

	/**
	 * Changes the current positionVector to be equal to the passed one.
	 * 
	 * @param vector
	 */
	public void replace(PositionVector vector) {
		assert dimensions == vector.getDimensions();
		this.values = Arrays.copyOf(vector.values, dimensions);
	}

	/**
	 * Returns a new PositionVector that is a <strong>copy</strong> of the
	 * current position moved into the direction of destination with the given
	 * speed. Does not alter the current position-vector instance. Does not
	 * alter the destination vector instance.
	 * 
	 * IFF the speed would lead to us overshooting the destination, we will just
	 * move right onto the destination instead. This prevents oscillations in
	 * movement models.
	 * 
	 * FIXME BR: this method signature and purpose is not well defined. As it is
	 * only used in movement models, its functionality might be better
	 * implemented there...
	 * 
	 * @param destination
	 * @param speed
	 * @return
	 */
	public PositionVector moveStep(PositionVector destination, double speed) {

		double distance = destination.distanceTo(this);
		if (distance < speed) {
			/*
			 * We would overshoot the target.
			 */
			return new PositionVector(destination);
		}

		PositionVector direction = new PositionVector(destination);
		direction.subtract(this);
		direction.normalize();
		direction.multiplyScalar(speed);

		PositionVector newPosition = new PositionVector(this);
		newPosition.add(direction);

		return newPosition;
	}

	public double getLength() {
		double sum = 0;
		for (double val : values) {
			sum += val * val;
		}
		return Math.sqrt(sum);
	}

	@Override
	public void set(Location l) {
		assert (l instanceof PositionVector);
		this.replace((PositionVector) l);
	}

	@Override
	public double getLatitude() {
		/*
		 * TODO this is only a stub, as we do not work on long/lat in the
		 * simulator (yet?)
		 */
		return getY();
	}

	@Override
	public double getLongitude() {
		/*
		 * TODO this is only a stub, as we do not work on long/lat in the
		 * simulator (yet?)
		 */
		return getX();
	}

	@Override
	public long getAgeOfLocation() {
		return 0; // always a fresh location
	}

	@Override
	public double distanceTo(Location dest) {
		if (dest instanceof PositionVector) {
			PositionVector pv = (PositionVector) dest;
			if (pv.getDimensions() == getDimensions()) {
				double dist = 0;
				for (int i = 0; i < dimensions; i++) {
					// faster as Math.pow
					dist += (pv.getEntry(i) - getEntry(i))
							* (pv.getEntry(i) - getEntry(i));
				}
				return Math.sqrt(dist);
			} else {
				throw new AssertionError(
						"Can not compute distance between Vectors of different length!");
			}
		} else {
			throw new AssertionError("Incompatible Types!");
		}
	}

	@Override
	public float bearingTo(Location dest) {
		if (dest instanceof PositionVector) {
			PositionVector t = (PositionVector) dest;
			/*
			 * Calculates the angle using atan2 - this implies that the first
			 * two dimensions in your vector are the plane you are interested
			 * in.
			 */
			return (float) Math.atan2(t.getEntry(1) - this.getEntry(1),
					t.getEntry(0) - this.getEntry(0));
		} else {
			throw new AssertionError(
					"Can only calculate an Angle on elements of type position vector");
		}
	}
}
