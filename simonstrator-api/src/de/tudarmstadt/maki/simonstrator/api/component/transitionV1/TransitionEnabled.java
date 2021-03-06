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

package de.tudarmstadt.maki.simonstrator.api.component.transitionV1;

/**
 * A {@link TransitionEnabled} class can be controlled by the Transi
 * 
 * @author bjoern
 *
 */
public interface TransitionEnabled {

	/**
	 * Prepare internal state / datastructures, no communication
	 * 
	 * @param cb
	 */
	public void onInit(StateCallback cb);

	// FIXME Future Work public void onStartup(StateCallback cb);

	/**
	 * "Start" the overlay: join, and on join successful call cb.finished
	 * 
	 * @param cb
	 */
	public void onRunning(StateCallback cb);

	public void onShutdown(StateCallback cb, ParallelActiveCallback paC);

	public void onCleanup(StateCallback cb);
	
	// public void onFinished(StateCallback cb);

}
