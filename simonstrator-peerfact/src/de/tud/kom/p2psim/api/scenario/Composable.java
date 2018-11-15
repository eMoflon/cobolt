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



package de.tud.kom.p2psim.api.scenario;


/**
 * An implementation of this interface indicates that it requires other
 * configurables in order to configure itself properly. These configurables can
 * be retrieved from the <code>Configurator</code> which calls the
 * <code>compose(configurator)</code> method on this composable configurable.
 * 
 * @author Konstantin Pussep
 * @author Sebastian Kaune
 * @version 3.0, 03.12.2007
 * 
 * @see Configurator#getConfigurable(String)
 * 
 */
public interface Composable {
	/**
	 * Upon the call of this method the composable configurable can access other
	 * configurables by accessing the <code>Configurator</code>.
	 * 
	 * @param config
	 *            <code>Configurator</code> instance holding all available
	 *            configurables.
	 */
	public void compose(Configurator config);
}
