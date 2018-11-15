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

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.common.IDSpace;
import de.tudarmstadt.maki.simonstrator.api.common.UniqueID;


/**
 * Default Implementation of IDSpace, this should always be extended if you
 * really need to implement your own IDSpace (which generally should not be the
 * case!). NEVER implement own UniqueIDs, instead implement own IDSpaces if you
 * need additional utility methods.
 * 
 * This IDSpace creates positive IDs between (and including) 0 and
 * (2^numberOfBits - 1).
 * 
 * Use the IDSpace to create new IDs, do not call the ID-Constructors directly!
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 06/18/2011
 */
public class DefaultIDSpace implements IDSpace {

	// public static BigInteger lastId = BigInteger.ONE;

	private int numberOfBits;

	private BigInteger numberOfIDs;

	private UniqueID emptyID;

	private final BigInteger TWO = new BigInteger("2");

	/**
	 * This Space generates positive IDs represented by
	 * <code>numberOfBits</code> Bits (unsigned)
	 * 
	 * @param numberOfBits
	 */
	public DefaultIDSpace(int numberOfBits) {
		this.numberOfBits = numberOfBits;
		this.numberOfIDs = TWO.pow(numberOfBits);
		this.emptyID = this.createEmptyID();
	}

	@Override
	public int getNumberOfBits() {
		return numberOfBits;
	}

	@Override
	public int getNumberOfBytes() {
		// Sign-Bit for Two's complement
		return (int) Math.ceil((numberOfBits + 1) / 8);
	}

	@Override
	public BigInteger getNumberOfDistinctIDs() {
		return numberOfIDs;
	}

	@Override
	public UniqueID getEmptyID() {
		return emptyID;
	}

	/**
	 * CDan be overwritten by an extending class to provide another
	 * implementation of the EmptyID
	 * 
	 * @return
	 */
	protected UniqueID createEmptyID() {
		return new OverlayNodeID(numberOfBits, new BigInteger("-1"));
	}


	@Override
	public UniqueID createID(BigInteger bigInt) {
		// only positive IDs
		bigInt = bigInt.abs();
		if (bigInt.compareTo(numberOfIDs) > 0) {
			// only IDs within range
			bigInt = bigInt.mod(numberOfIDs);
		}
		return new OverlayNodeID(numberOfBits, bigInt);
	}

	@Override
	public UniqueID createID(long value) {
		return new OverlayNodeID(numberOfBits, value);
	}

	@Override
	public UniqueID createIDUsingSHA1(String stringToHash) {
		MessageDigest md;
		byte[] sha1hash = new byte[numberOfBits];
		try {
			md = MessageDigest.getInstance("SHA-1");
			md.update(stringToHash.getBytes("iso-8859-1"), 0,
					stringToHash.length());
			sha1hash = md.digest();
		} catch (NoSuchAlgorithmException e) {
			System.err.println("NoSuchAlgorithmException");
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			System.err.println("UnsupportedEncodingException");
			e.printStackTrace();
		}
		BigInteger value = new BigInteger(1, sha1hash);

		// Make sure the value does not have more than numberOfBits bits
		value = value.mod(numberOfIDs);
		return createID(value);
	}

	@Override
	public UniqueID createRandomID() {
		// DefaultIDSpace.lastId =
		// DefaultIDSpace.lastId.add(BigInteger.ONE).mod(
		// getNumberOfDistinctIDs());
		// return createID(DefaultIDSpace.lastId);
		byte[] newBytes = new byte[getNumberOfBytes()];
		Randoms.getRandom(IDSpace.class).nextBytes(newBytes);
		BigInteger value = new BigInteger(1, newBytes);
		value = value.mod(numberOfIDs);
		return createID(value);
	}

	// protected BigInteger createBigInteger(byte[] bytes) {
	// return new BigInteger(bytes);
	// }
	//
	// protected byte[] createByteArray(BigInteger bigInt) {
	// byte[] unpadded = bigInt.toByteArray();
	// if (unpadded.length == getNumberOfBytes()) {
	// return unpadded;
	// }
	// if (unpadded[0] < 0) { // negative
	// byte padding = -1; // pad with ones
	// return pad(padding, unpadded);
	// } else {
	// byte padding = 0; // pad with zeros
	// return pad(padding, unpadded);
	// }
	// }
	//
	// private byte[] pad(byte padding, byte[] keep) {
	// byte[] ret = new byte[getNumberOfBytes()];
	// for (int i = 0; i < ret.length; i++) {
	// if (i < ret.length - keep.length) {
	// ret[i] = padding;
	// } else {
	// ret[i] = keep[i - ret.length + keep.length];
	// }
	// }
	// return ret;
	// }

	public boolean isBetween(UniqueID test, UniqueID a, UniqueID b) {
		if ((a.compareTo(b) < 0 && a.compareTo(test) < 0 && test.compareTo(b) < 0)
				|| (b.compareTo(a) < 0 && (test.compareTo(a) < 0 || b
						.compareTo(test) < 0))) {
			return true;
		}
		return false;
	}

	public BigInteger getClockwiseDistance(OverlayNodeID a, OverlayNodeID b) {
		if (a == null || b == null)
			return null;

		BigInteger oId = b.getBigInteger();
		BigInteger d;

		if (a.getBigInteger().compareTo(oId) <= 0) {
			// oId -ID
			d = oId.subtract(a.getBigInteger());
		} else {
			// 2^ID_LENGTH - ID + oId
			d = getNumberOfDistinctIDs().subtract(a.getBigInteger()).add(oId);
		}
		return d.abs();
	}

	public BigInteger getCounterClockwiseDistance(OverlayNodeID a,
			OverlayNodeID b) {
		if (a == null || b == null)
			return null;

		BigInteger oId = b.getBigInteger();
		BigInteger d;

		if (a.getBigInteger().compareTo(oId) >= 0) {
			// ID -oId
			d = a.getBigInteger().subtract(oId);
		} else {
			// ID + 2^ID_LENGTH - oId
			d = a.getBigInteger().add(getNumberOfDistinctIDs()).subtract(oId);
		}
		return d.abs();
	}

	public BigInteger getMinDistance(OverlayNodeID a, OverlayNodeID b) {
		return getClockwiseDistance(a, b)
				.min(getCounterClockwiseDistance(a, b));
	}
	
	public int getDigit(OverlayNodeID id, int i, int b) {
		BigInteger rId;

		// Shift the ID by i*b bits
		int shift = i * b;
		rId = id.getBigInteger().shiftRight(shift);

		// Remove the leading digits by doing a mod 2^b
		rId = rId.mod(TWO.pow(b));

		// Check whether we can return the result as an integer or not
		if (rId.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0)
			return rId.intValue();
		else
			return -1;
	}

	public int indexOfMSDD(OverlayNodeID id, OverlayNodeID otherId, int b) {
		// Compare the digits, starting with the most significant bit
		for (int i = (getNumberOfBits() / b) - 1; i >= 0; i--) {
			if (getDigit(id, i, b) != getDigit(otherId, i, b))
				return i;
		}
		return -1;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IDSpace) {
			IDSpace other = (IDSpace) obj;
			if (other.getNumberOfBits() == this.getNumberOfBits()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getNumberOfBits();
	}

}
