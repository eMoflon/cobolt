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

package de.tud.kom.p2psim.impl.topology.placement;

import java.util.Random;

import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tudarmstadt.maki.simonstrator.api.Randoms;

/**
 * Generates random 2D or 3D PositionVectors
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 04/26/2011
 */
public class RandomPositionDistribution extends PositionDistribution {

	@Override
	public PositionVector getNextPosition() {
		Random r = Randoms.getRandom(RandomPositionDistribution.class);
		double[] vec = new double[getDimensions()];
		for (int i = 0; i < getDimensions(); i++) {
			vec[i] = r.nextInt((int) getWorldDimensions().getEntry(i));
		}
		PositionVector position = new PositionVector(vec);
		return position;
	}

}
