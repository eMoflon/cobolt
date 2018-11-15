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

package de.tudarmstadt.maki.simonstrator.api.component.transition;

/**
 * A {@link SelfTransition} can be used to reconfigure a mechanism, e.g., alter
 * its state (previously: a parameter transition) through this simple callback
 * mechanism. This object <strong>must not maintain state</strong>.
 * 
 * @author Bjoern Richerzhagen
 *
 */
public interface SelfTransition<T extends TransitionEnabled> {

	/**
	 * Invoked as soon as the self-transition is executed. Use this method to
	 * alter the state accordingly (e.g., change some parameters).
	 * 
	 * @param mechanism
	 */
	public void alterState(T mechanism);

}
