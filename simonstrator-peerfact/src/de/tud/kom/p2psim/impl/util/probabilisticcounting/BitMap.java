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

import static java.lang.Math.max;
import static java.lang.Math.pow;

import java.util.ArrayList;
import java.util.BitSet;

public class BitMap extends BitSet {

	public static final double PHI = 1.54703;

	public static class List extends ArrayList<BitMap> {

		public List() {
			super();
		}

		public void or(List bitMap) {
			for (int i = 0; i < max(this.size(), bitMap.size()); i++) {
				if (this.size() <= i) {
					this.add(bitMap.get(i));
				} else {
					this.get(i).or(bitMap.get(i));
				}
			}
		}

		public double getNodes() {
			double result = 0.0;
			for (BitMap bitMap : this) {
				result += bitMap.getR();
			}
			result = result / new Double(this.size());
			return 1.0d / PHI * pow(2.0d, new Double(result));
		}
	}

	private int size;

	public BitMap(int size) {
		super(size);
		this.size = size;
	}

	public int getSize() {
		return this.size;
	}

	public static BitMap valueOf(byte[] bytes, int size) {
		BitMap result = new BitMap(size);
		for (int i = 0; i < bytes.length; i++) {
			for (int b = 0; b < 8; b++) {
				if ((bytes[i] & 1 << (7 - b)) != 0) {
					result.set(i * 8 + b);
				}
				if (i * 8 + b == size) {
					return result;
				}
			}
		}
		return result;
	}

	public double getNodes() {
		return 1.0 / PHI * pow(2.0, this.getR());
	}

	int getR() {
		return this.nextClearBit(0) + 1;
	}
}
