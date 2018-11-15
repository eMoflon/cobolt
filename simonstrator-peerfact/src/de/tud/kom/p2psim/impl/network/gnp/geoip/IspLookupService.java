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


package de.tud.kom.p2psim.impl.network.gnp.geoip;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

public class IspLookupService {

	SortedMap<Long, String> data;

	public IspLookupService(String path) {
		data = new TreeMap<Long, String>();
		try {
			readFile(path);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void readFile(String path) throws IOException {
		FileReader fr = new FileReader(path);
		BufferedReader in = new BufferedReader(fr);
		String line = null;
		while ((line = in.readLine()) != null) {
			String[] tmp = line.split(",");
			data.put(Long.valueOf(tmp[0]), tmp[2].replace("\"", ""));
			data.put(Long.valueOf(tmp[1]), tmp[2].replace("\"", ""));
		}
	}

	public String getISP(long ip) {
		Iterator<Entry<Long, String>> it = data.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Long, String> lowerBound = it.next();
			if (ip >= lowerBound.getKey()) {
				if (it.hasNext()) {
					Entry<Long, String> upperBound = it.next();
					if (ip <= upperBound.getKey()
							&& upperBound.getValue().equals(
									lowerBound.getValue()))
						return lowerBound.getValue();
				}
			}
		}
		return null;
	}
}
