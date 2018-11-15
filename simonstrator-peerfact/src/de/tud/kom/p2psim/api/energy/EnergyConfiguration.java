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

package de.tud.kom.p2psim.api.energy;

import de.tud.kom.p2psim.api.common.SimHost;

/**
 * A class that helps a user to configure an {@link EnergyModel} by providing a
 * fully configured {@link EnergyComponent}. Please note, that in the
 * {@link EnergyModel} only one configuration can be passed to the Factory -
 * otherwise there could be duplicates of components and other hard to find
 * configuration errors.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 27.02.2012
 */
public interface EnergyConfiguration<E extends EnergyComponent> {

	/**
	 * We request a configured instance of an {@link EnergyComponent} for this
	 * Host.
	 * 
	 * @param host
	 * 
	 * @return
	 */
	public E getConfiguredEnergyComponent(SimHost host);

	/**
	 * We expect each Configuration to provide a text containing help on how to
	 * configure the Layer. This Text is then printed to the console (or the
	 * logger). It should be dynamic in that it contains information about the
	 * currently configured values as well. You should also include a list of
	 * presets if any are provided.
	 * 
	 * @return
	 */
	public String getHelp();

	/**
	 * Optional check of configuration parameters. If there exist combinations
	 * of config-values that make no sense, please check for the occurrence of
	 * such combinations and return false. The Factory will then issue an
	 * exception informing the user about possible conflicts. Please also make
	 * sure that getHelp() contains some information on this issue, as it will
	 * be printed out too.
	 * 
	 * If there is no way to mess up configuration, just return true :)
	 * 
	 * @return
	 */
	public boolean isWellConfigured();

}
