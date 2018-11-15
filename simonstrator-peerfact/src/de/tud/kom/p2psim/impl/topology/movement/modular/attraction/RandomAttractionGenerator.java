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

package de.tud.kom.p2psim.impl.topology.movement.modular.attraction;

import java.util.List;
import java.util.Random;
import java.util.Vector;

import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.api.topology.Topology;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tudarmstadt.maki.simonstrator.api.Binder;
import de.tudarmstadt.maki.simonstrator.api.Randoms;

/**
 * Implementation of the interface {@link AttractionGenerator}.
 * 
 * It generates the given number of {@link AttractionPoint}s and sets the
 * Position randomly.
 * 
 * @author Christoph Muenker
 * @version 1.0, 02.07.2013
 */
public class RandomAttractionGenerator implements AttractionGenerator {

	private Random rand;

	private PositionVector worldDimension;

	private int numberOfAttractionPoints;

	private boolean numberOfAPsSet = false;

	private double minSpeed = 2;

	private double maxSpeed = 2;

	public RandomAttractionGenerator() {
		this.rand = Randoms.getRandom(RandomAttractionGenerator.class);
		this.worldDimension = Binder.getComponentOrNull(Topology.class)
				.getWorldDimensions();
	}

	@Override
	public List<AttractionPoint> getAttractionPoints() {
		if (!numberOfAPsSet) {
			throw new ConfigurationException(
					"Number of Attraction Points is not set in RandomAttractionGenerator!");
		}

		List<AttractionPoint> result = new Vector<AttractionPoint>();
		for (int i = 0; i < numberOfAttractionPoints; i++) {
			PositionVector posVec = createPosVec();
			AttractionPoint aPoint = new AttractionPoint(posVec, minSpeed,
					maxSpeed);
			result.add(aPoint);
		}
		return result;
	}

	private PositionVector createPosVec() {
		double x = rand.nextDouble() * worldDimension.getX();
		double y = rand.nextDouble() * worldDimension.getY();
		return new PositionVector(x, y);
	}

	@Override
	public void setNumberOfAttractionPoints(int numberOfAttractionPoints) {
		if (numberOfAttractionPoints <= 0) {
			throw new ConfigurationException(
					"NumberOfAttractionPoints should be at least 1!");
		}

		this.numberOfAPsSet = true;
		this.numberOfAttractionPoints = numberOfAttractionPoints;
	}

}
