/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
 *
 * This file is part of Simonstrator.KOM.
 * 
 * Simonstrator.KOM is free software: you can redistribute it and/or modify
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

package de.tudarmstadt.maki.simonstrator.api;

import java.util.Random;

import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.core.RandomGeneratorComponent;


/**
 * Use this class to access random numbers. Do not use Math.random, as
 * simulations will not be deterministic in this case!
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public final class Randoms {
	
	/**
	 * 
	 */
	public static RandomGeneratorComponent bindedRandom = null;

	private static RandomGeneratorComponent getRandomGeneratorComponent() {
		if (bindedRandom == null) {
			try {
				bindedRandom = Binder
						.getComponent(RandomGeneratorComponent.class);
			} catch (ComponentNotAvailableException e) {
				System.err
						.println("Simonstrator-API WARNING: You are using Random, although the platform provides no RandomGeneratorComponent. A simple default implementation is used.");
				bindedRandom = new RandomGeneratorComponent() {
					@Override
					public Random getRandom(Object topic) {
						return new Random();
					}
				};
			}
		}
		return bindedRandom;
	}

	/**
	 * Returns a random-generator for the given component
	 * 
	 * @param topic
	 *            for each new topic, a new source of random numbers is
	 *            generated. A topic could, thus, be a given overlay node.
	 * @return
	 */
	public static Random getRandom(Object topic) {
		return getRandomGeneratorComponent().getRandom(topic);
	}

}
