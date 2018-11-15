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

import java.util.List;
import java.util.Map;

import org.dom4j.Element;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tudarmstadt.maki.simonstrator.api.component.GlobalComponent;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponentFactory;

/**
 * HostBuilder is used to parse a portion of the configuration file and to build
 * hosts based on it. HostBuilder will use other modules already configured by
 * the <code>Configurator</code>, mainly the {@linkplain HostComponentFactory
 * component factories} to create single components for this host.
 * 
 * @author Konstantin Pussep
 * @author Sebastian Kaune
 * @version 3.0, 03.12.2007
 * 
 * @see SimHost
 * @see Configurator
 */
public interface HostBuilder extends Builder, GlobalComponent {

	/**
	 * Return a map with all hosts indexed by group ids.
	 * 
	 * @return map with groupId -> hosts mappings
	 */
	public Map<String, List<SimHost>> getAllHostsWithGroupIDs();

	/**
	 * Return all hosts regardless group ids.
	 * 
	 * @return all hosts created
	 */
	public List<SimHost> getAllHosts();

	/*
	 * @see de.tud.kom.p2psim.api.scenario.Builder#parse(org.dom4j.Element,
	 * de.tud.kom.p2psim.api.scenario.Configurator)
	 */
	public void parse(Element elem, Configurator config);
}
