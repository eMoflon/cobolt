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

package de.tud.kom.p2psim.impl.util.probabilisticcounting;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.tud.kom.p2psim.impl.util.probabilisticcounting.BitMap.List;

public class Counter {

	public static final String HASH = "SHA";

	public static BitMap.List getBitMap(byte[] id, int size, int m)
			throws NoSuchAlgorithmException {
		BitMap.List result = new List();
		MessageDigest md = MessageDigest.getInstance(HASH);
		BitMap hash = BitMap.valueOf(md.digest(id), size);
		Permute p = Permute.Instance();
		for (int i = 0; i < m; i++) {
			BitMap bit = p.permute(hash, i);
			bit.clear(bit.nextSetBit(0) + 1, bit.size());
			result.add(bit);
		}
		return result;
	}

	/**
	 * Returns Bitmap for probabilistic counting
	 * 
	 * @param id
	 *            byte array to hash
	 * @return bitmap
	 * @throws NoSuchAlgorithmException
	 *             if HASH is not available
	 */
	public static BitMap getBitMap(byte[] id, int size)
			throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance(HASH);
		byte[] hash = md.digest(id);
		BitMap result = BitMap.valueOf(hash, size);
		result.clear(result.nextSetBit(0) + 1, result.size());
		return result;
	}
}
