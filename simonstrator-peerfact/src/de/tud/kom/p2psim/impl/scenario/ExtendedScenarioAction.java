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



package de.tud.kom.p2psim.impl.scenario;

import java.lang.reflect.Method;

import de.tud.kom.p2psim.api.scenario.ScenarioAction;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;

/**
 * Specifies which action and when should take place in the simulator. The
 * method is specified and called via reflection.
 * 
 * @author Konstantin Pussep
 * @author Sebastian Kaune
 * @version 3.0, 13.12.2007
 * 
 */
public final class ExtendedScenarioAction implements ScenarioAction {

	// Operation operation;
	/**
	 * Method which will be called upon execute.
	 */
	Method method;

	long offset;

	HostComponent target;

	Object[] params;

	/**
	 * Creates a new scenario action.
	 * 
	 * @param target
	 *            - where to invoke
	 * @param method
	 *            - what to invoke
	 * @param offset
	 *            - after which time (TODO absolute time or relative? now
	 *            relative)
	 * @param params
	 *            - with which parameters
	 */
	protected ExtendedScenarioAction(HostComponent target, Method method,
			long offset, Object[] params) {
		assert method != null;
		assert offset >= 0;
		this.target = target;
		this.method = method;
		this.params = params;
		this.offset = offset;
	}

	long getOffset() {
		return offset;
	}

	public void schedule() {
		Monitor.log(ExtendedScenarioAction.class, Level.DEBUG,
				"Schedule action for time " + offset);
		Event.scheduleWithDelay(offset, new EventHandler() {
			public void eventOccurred(Object event, int type) {
				try {
					method.invoke(target, params);
				} catch (Exception e) {
					Monitor.log(ExtendedScenarioAction.class, Level.ERROR,
							"Failed to execute action.", e);
					throw new RuntimeException("Failed to execute action.", e);
				}
			}
		}, null, 0);
	}

	@Override
	public String toString() {
		return "OperationBasedScenarioAction(" + method + ", " + offset + ")";
	}

}
