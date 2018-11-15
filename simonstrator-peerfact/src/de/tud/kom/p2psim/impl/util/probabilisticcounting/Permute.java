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

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Permute {

	private static Permute instance;

	public static Permute Instance() {
		if (instance == null) {
			instance = new Permute();
		}
		return instance;
	}

	private List<int[]> permuatations;

	protected Permute() {
		this.permuatations = new LinkedList<int[]>();
	}

	public BitMap permute(BitMap bits, int m) {
		int[] perm = this.getPermutation(m, bits.getSize());
		BitMap result = new BitMap(bits.getSize());
		for (int i = 0; i < perm.length; i++) {
			result.set(i, bits.get(perm[i]));
		}
		return result;
	}

	private int[] getPermutation(int m, int size) {
		if (this.permuatations.size() < m + 1) {
			int[] perm = new int[size];
			List<Integer> numbers = new LinkedList<Integer>();
			for (int i = 0; i < size; i++) {
				numbers.add(i);
			}
			Random rn = new Random();
			for (int i = 0; i < size; i++) {
				perm[i] = numbers.remove(rn.nextInt(numbers.size()));
			}
			this.permuatations.add(perm);
		}
		return this.permuatations.get(m);
	}

}
