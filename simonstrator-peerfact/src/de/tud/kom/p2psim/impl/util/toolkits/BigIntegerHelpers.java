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


package de.tud.kom.p2psim.impl.util.toolkits;

import java.math.BigInteger;

/**
 * 
 * @author Tobias Lauinger <tl1003@rbg.informatik.tu-darmstadt.de>
 * 
 */
public class BigIntegerHelpers {

	/**
	 * Returns a BigInteger with the <code>n</code> rightmost bits of
	 * <code>in</code>.
	 * 
	 * @param in
	 *            the BigInteger of which the n rightmost bits are of interest.
	 * @param n
	 *            the number of bits from the right of in that are of interest
	 *            (n &gt;= 0, and probably n &lt;= in.bitLength()).
	 * 
	 * @return a BigInteger with the BTREE rightmost bits of <code>in</code>.
	 */
	public static BigInteger getNRightmostBits(final BigInteger in, final int n) {
		// make sure only the last n bits are set
		final BigInteger mask = BigInteger.valueOf(2).pow(n).subtract(
				BigInteger.ONE);
		// BigInteger mask = BigInteger.valueOf(Math.round(Math.pow(2, n)) - 1);
		return in.and(mask);

		/*
		 * TODO: This code does not work (because of sign extension??)
		 * BigInteger negMask = BigInteger.ONE.shiftLeft(n); return
		 * in.andNot(negMask);
		 */
	}

	/**
	 * Shifts <code>register</code> left by n bits and sets these n bits to the
	 * n rightmost bits of <code>input</code>.
	 * 
	 * @param register
	 *            the BigInteger to be shifted to the left.
	 * @param input
	 *            the BigInteger that will be appended to <code>register</code>.
	 * @param n
	 *            the number of bits to shift <code>register</code> left and the
	 *            number of rightmost bits of <code>input</code> that will be
	 *            appended.
	 * @return a BigInteger with the n rightmost bits of <code>input</code>
	 *         appended to <code>register</code>.
	 */
	public static BigInteger shiftLeft(final BigInteger register,
			final BigInteger input, final int n) {
		// make sure only the last n bits of input are set
		final BigInteger cutSuffix = getNRightmostBits(input, n);
		return register.shiftLeft(n).or(cutSuffix);
	}

	/**
	 * Extracts the i-th substring of length len of bitstring starting from the
	 * right, where the rightmost n bits have index 0.
	 * 
	 * For example, for <code>len=2</code>, <code>i=1</code> and
	 * <code>bitstring=10001100</code>, the wanted bits are <code>11</code>.
	 * 
	 * If <code>(i+1)*len &lt;= bitstring.bitLength()</code>, the standard
	 * extension mechanisms of BigInteger are used.
	 * 
	 * @param bitstring
	 *            a BigInteger from which the binary representation is used to
	 *            extract the n-th substring of length len from the right.
	 * @param i
	 *            the index of the len-length substring that is to be extracted.
	 *            First index starts with 0 and is equal to the len least
	 *            significant bits of bitstring.
	 * @param len
	 *            the length of the substring to be extracted.
	 * @return a BigInteger with the interesting bits (shifted to the right).
	 */
	public static BigInteger getNthBitstring(final BigInteger bitstring,
			final int i, final int len) {
		// shift out the rightmost bits that are uninteresting
		final BigInteger idShifted = bitstring.shiftRight(i * len);
		// mask uninteresting leftmost bits
		return BigIntegerHelpers.getNRightmostBits(idShifted, len);
	}

}
