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


package de.tud.kom.p2psim.impl.network.modular.common;

import java.util.List;

public class GNPToolkit {

	public static double getDistance(List<Double> gnpPosA, List<Double> gnpPosB) {
		int thisSz = gnpPosA.size();
		int otherSz = gnpPosB.size();
		
		if (thisSz != otherSz) throw new AssertionError("GNP Coordinates Dimension mismatch: " + gnpPosA.size() + " and " + gnpPosB.size());
		
		double accDistSq = 0d;
		for (int i = 0; i < thisSz; i++) {
			double dimDiff = gnpPosA.get(i) - gnpPosB.get(i);
			accDistSq += dimDiff * dimDiff;
		}
		return Math.sqrt(accDistSq);
	}
	
}
