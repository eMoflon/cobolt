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
 * A {@link TransitionEnabled} mechanism is controlled by the
 * {@link TransitionEngine}. It allows for the transparent exchange of the
 * underlying implementation of that mechanism and for basic lifecycle
 * callbacks.
 * 
 * @author Bjoern Richerzhagen, Alexander Froemmgen, Julius Rueckert
 *
 */
public interface TransitionEnabled {

	/**
	 * Notifies the mechanism that it is now supposed to run. "Start" the
	 * mechanism: e.g., join by sending some messages or connect to other nodes.
	 * Once you are ready to fully operate, trigger the callback. State has been
	 * transferred right before this method is called.
	 * 
	 * @param cb
	 *            Callback to be triggered on success (when the mechanism is
	 *            fully functional) or failure (join did not succeed, ...)
	 */
	public void startMechanism(TransitionEnabled.Callback cb);

	/**
	 * A transition occurred and this implementation of the
	 * {@link TransitionEnabled} mechanism has to stop operating (e.g., by
	 * gracefully leaving a system and sending some final messages). Once
	 * operation has stopped, invoke the callback. State has already been
	 * transferred to the new target component.
	 * 
	 * @param cb
	 *            Callback to be triggered on success or failure!
	 */
	public void stopMechanism(TransitionEnabled.Callback cb);

	/**
	 * Simple callback - has to be invoked to terminate a lifecycle-phase of a
	 * mechanism.
	 * 
	 * @author Bjoern Richerzhagen
	 *
	 */
	public static interface Callback {

		/**
		 * The respective lifecycle-phase was finished. Do also trigger this in
		 * case of an error!
		 * 
		 * @param successful
		 *            true, if the phase is finished successfully.
		 */
		public void finished(boolean successful);

	}

}
