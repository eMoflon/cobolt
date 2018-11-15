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

import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.core.SchedulerComponent;

/**
 * Lightweight variant to an operation: an event. The Event is scheduled,
 * executes some code and then terminates. No callback, no expected result, no
 * typing. It is that simple.
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public final class Event {

	private static SchedulerComponent scheduler = null;

	private static SchedulerComponent getScheduler() {
		if (scheduler == null) {
			try {
				scheduler = Binder.getComponent(SchedulerComponent.class);
			} catch (ComponentNotAvailableException e) {
				System.err
						.println("Simonstrator-API WARNING: Events were scheduled, although the current platform is not providing a SchedulerComponent!");
				scheduler = new SchedulerComponent() {
					@Override
					public void scheduleIn(long time, EventHandler handler,
							Object content, int type) {
						// fail silently
					}
				};
			}
		}
		return scheduler;
	}

	/**
	 * 
	 * @param handler
	 * @param content
	 */
	public static void scheduleImmediately(EventHandler handler,
			Object content, int type) {
		getScheduler().scheduleIn(0, handler, content, type);
	}

	/**
	 * 
	 * @param delay
	 *            a delay computed using the Time.-units.
	 * @param handler
	 * @param content
	 * @param type
	 *            a int-type field for easier filtering of events. You should
	 *            define the ints as constants in your EventHandlers.
	 */
	public static void scheduleWithDelay(long delay, EventHandler handler,
			Object content, int type) {
		getScheduler().scheduleIn(delay, handler, content, type);
	}

}
