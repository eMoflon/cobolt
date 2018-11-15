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

package de.tud.kom.p2psim.impl.scenario;

import de.tud.kom.p2psim.api.scenario.Scenario;

/**
 * Just returns an empty scenario.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, Jun 15, 2016
 */
public class NoScenarioFactory extends AbstractScenarioFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tud.kom.p2psim.api.scenario.ScenarioFactory#createScenario()
	 */
	public Scenario createScenario() {
		this.defaultComponentClass = Object.class;
		ExtendedScenario scenario = newScenario();
		return scenario;
	}

}
