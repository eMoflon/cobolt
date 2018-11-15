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

package de.tud.kom.p2psim.impl.topology.views.fiveg;

import java.util.Random;

import de.tud.kom.p2psim.impl.topology.views.FiveGTopologyView;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * Simple random grid {@link FiveGTopologyDatabase} for the
 * {@link FiveGTopologyView}.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, Nov 5, 2015
 */
public class RandomGridDatabase extends AbstractGridBasedTopologyDatabase {

	private Random rnd = Randoms.getRandom(RandomGridDatabase.class);

	public RandomGridDatabase() {
		super(100, true);
	}

	@Override
	protected Entry createEntryFor(int segmentID, boolean isCloudlet) {
		long latRnd = ((long) (rnd.nextDouble() * 200 * Time.MILLISECOND))
				+ 50 * Time.MILLISECOND
				+ (isCloudlet ? 0 : 200 * Time.MILLISECOND);
		double dropRnd = rnd.nextDouble();
		return new StaticEntry(segmentID, dropRnd, dropRnd, latRnd, latRnd,
				1000000, 1000000);
	}

}
