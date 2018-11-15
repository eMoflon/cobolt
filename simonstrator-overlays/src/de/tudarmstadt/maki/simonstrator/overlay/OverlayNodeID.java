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

package de.tudarmstadt.maki.simonstrator.overlay;

import java.math.BigInteger;

import de.tudarmstadt.maki.simonstrator.api.common.UniqueID;

/**
 * This is the new Version of OverlayID/Key which tries to unify ID-Creation and
 * Handling in Overlays and Applications. Internally, this relies on BigInteger.
 * 
 * One important change is that you should not extend this class (ex.
 * ChordUniqueID) but instead provide an own IDSpace, if you need additional
 * functionality. All Methods calculating something on IDs are implemented in
 * the IDSpace!
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 06/18/2011
 */
public class OverlayNodeID implements UniqueID {

	private static final long serialVersionUID = 1L;

	/**
	 * BigInt-Representation of this ID
	 */
	private BigInteger bigInt;

	/**
	 * Performance boost on comparisons for small IDSpaces?
	 */
	private transient boolean hasLongRepresentation;

	private transient long longRepresentation;

	private int numberOfBits;

	@SuppressWarnings("unused")
	private OverlayNodeID() {
		// for Kryo
	}

	/**
	 * Convenience constructor for shorter IDs (below 64 bit)
	 * 
	 * @param idSpace
	 * @param bigDecimal
	 */
	public OverlayNodeID(int numberOfBits, long value) {
		this(numberOfBits, BigInteger.valueOf(value));
	}

	/**
	 * Constructor, <b>only to be used by an IDSpace</b>! To achieve consistency
	 * in ID creation you should always use the IDSpace to create IDs!
	 * 
	 * @param idSpace
	 * @param bigDecimal
	 */
	public OverlayNodeID(int numberOfBits, BigInteger value) {
		this.bigInt = value;
		if (numberOfBits < 64) {
			this.hasLongRepresentation = true;
			this.longRepresentation = value.longValue();
		} else {
			this.hasLongRepresentation = false;
			this.longRepresentation = Long.MIN_VALUE;
		}
		this.numberOfBits = numberOfBits;
	}

	@Override
	public int getTransmissionSize() {
		return numberOfBits;
	}

	@Override
	public int compareTo(UniqueID uid) {
		if (uid instanceof OverlayNodeID) {
			OverlayNodeID o = (OverlayNodeID) uid;
			if (hasLongRepresentation && o.hasLongRepresentation) {
				return Long.compare(longRepresentation, o.longRepresentation);
			}
			return this.bigInt.compareTo(o.bigInt);
		}
		if (hasLongRepresentation) {
			return Long.compare(longRepresentation, uid.value());
		} else {
			return this.bigInt.compareTo(BigInteger.valueOf(uid.value()));
		}
	}

	public BigInteger getBigInteger() {
		return bigInt;
	}

	@Override
	public long value() {
		return longRepresentation;
	}

	@Override
	public String valueAsString() {
		if (hasLongRepresentation) {
			return String.valueOf(longRepresentation);
		}
		return bigInt.toString();
	}

	@Override
	public boolean equals(Object obj) {
		/*
		 * Two IDs are only equal, if they have the same numerical value.
		 */
		if (obj instanceof OverlayNodeID) {
			OverlayNodeID id = (OverlayNodeID) obj;
			if (this.hasLongRepresentation && id.hasLongRepresentation) {
				return this.longRepresentation == id.longRepresentation;
			}
			return bigInt.equals(id.getBigInteger());
		}
		return false;
	}

	@Override
	public int hashCode() {
		if (hasLongRepresentation) {
			return Long.valueOf(longRepresentation).hashCode();
		}
		return bigInt.hashCode();
	}

	@Override
	public String toString() {
		/*
		 * As some analyzers rely on a numeric value for parsing, we just return
		 * the value and no additional information in this implementation.
		 */
		return this.bigInt.toString();
	}

}
