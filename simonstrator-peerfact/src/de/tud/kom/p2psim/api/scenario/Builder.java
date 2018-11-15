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

import org.dom4j.Element;

import de.tudarmstadt.maki.simonstrator.api.component.Component;

/**
 * Another way (than to implement Configurable or Composable interface) to
 * configure simulator modules from an XML file: here, the
 * <code>Configurator</code> will pass the control to the builder which in turn
 * can build objects from the XML data. This is mainly designed for complex
 * things, e.g. creation of hosts.
 * <p>
 * Note, that we use the <a href="http://www.dom4j.org/">dom4j</a> library to
 * process the XML data.
 * 
 * @author Konstantin Pussep
 * @author Sebastian Kaune
 * @version 3.0, 03.12.2007
 * 
 * @see Composable
 * @see Configurator
 */
public interface Builder extends Component {
	/**
	 * Parse the XML subtree (with the provided element as a root) and create
	 * all the necessary data.
	 * 
	 * @param elem
	 *            - root of an XML subtree with configuration data (represented
	 *            as with the help from dom4j)
	 * @param config
	 *            - configurator which can be used to obtain other configurables
	 *            or call helper methods
	 */
	public void parse(Element elem, Configurator config);
}
