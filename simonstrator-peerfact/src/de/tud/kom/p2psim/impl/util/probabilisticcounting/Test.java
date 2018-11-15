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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Random;

public class Test {
	public static void main(String[] args) throws NoSuchAlgorithmException,
			IOException {
		Test t = new Test();
		int bitMapSize = 64;
		int numberOfIDs = 100000;

		File results = null;
		FileWriter fw = null;
		int start = 1;
		do {
			results = new File("outputs" + File.separator + "results_"
					+ start++ + ".dat");
		} while (results.exists());
		fw = new FileWriter(results);
		fw.write("#of Hashes" + "\t" + "#of IDs" + "\t" + "#calculated IDs");
		fw.write("\n");

		int hashNr = 32;
		for (int i = 0; i < 100; i++) {
			fw.write(hashNr + "\t" + numberOfIDs + "\t"
					+ t.test(bitMapSize, numberOfIDs, hashNr));
			fw.write("\n");
			System.out.println(hashNr + "\t" + numberOfIDs + "\t"
					+ t.test(bitMapSize, numberOfIDs, hashNr));
		}

		fw.flush();
		fw.close();
	}

	public double test(int bitMapSize, int numberOfIDs, int m)
			throws NoSuchAlgorithmException {
		Random rn = new Random();
		byte[] id;
		BitMap.List bits = new BitMap.List();
		bits.add(new BitMap(bitMapSize));
		HashSet<byte[]> h = new HashSet<byte[]>(numberOfIDs);
		for (int i = 0; i < numberOfIDs; i++) {
			id = new byte[20];
			do {
				rn.nextBytes(id);
			} while (h.contains(id));
			h.add(id);
			bits.or(Counter.getBitMap(id, bitMapSize, m));
		}
		return bits.getNodes();
	}

	public double test(int bitMapSize, int numberOfIDs)
			throws NoSuchAlgorithmException {
		Random rn = new Random();
		byte[] id;
		HashSet<byte[]> h = new HashSet<byte[]>(numberOfIDs);
		BitMap bits = new BitMap(bitMapSize);
		for (int i = 0; i < numberOfIDs; i++) {
			id = new byte[8];
			do {
				rn.nextBytes(id);
			} while (h.contains(id));
			h.add(id);
			bits.or(Counter.getBitMap(id, bitMapSize));
		}
		return bits.getNodes();
	}
}
