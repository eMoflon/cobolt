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

package de.tudarmstadt.maki.simonstrator.api.common;

import java.math.BigInteger;

/**
 * An IDSpace Object should be passed to every Overlay upon configuration. This
 * will ensure proper generation of OverlayIDs and OverlayKeys. By specifying
 * multiple IDSpaces for one Overlay you might implement different Spaces for
 * Keys and IDs (if that makes any sense at all). Please have a look at the
 * DefaultIDSpace-Class
 * 
 * IDSpaces are non-serializable!
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 06/18/2011
 */
public interface IDSpace {

	/**
	 * Get the number of Bits used for IDs in this IDSpace
	 * 
	 * @return
	 */
	public int getNumberOfBits();

	/**
	 * Number of bytes used by an ID in this Space
	 * 
	 * @return
	 */
	public int getNumberOfBytes();

	/**
	 * Return the number of Distinct IDs (2^numberOfBits)
	 * 
	 * @return
	 */
	public BigInteger getNumberOfDistinctIDs();

	/**
	 * This will return a UniqueID to be used for Empty-IDs
	 * 
	 * @return
	 */
	public UniqueID getEmptyID();

	/**
	 * Create a new ID using the long provided
	 * 
	 * @param bigInt
	 * @return
	 */
	public UniqueID createID(long value);

	/**
	 * Create a new ID using the BigInteger provided
	 * 
	 * @param bigInt
	 * @return
	 */
	public UniqueID createID(BigInteger bigInt);

	/**
	 * Convenience Method to create an ID from a String using SHA1.
	 * 
	 * @param stringToHash
	 * @return
	 */
	public UniqueID createIDUsingSHA1(String stringToHash);

	/**
	 * Returns a pseudorandom ID
	 * 
	 * @return
	 */
	public UniqueID createRandomID();


}
