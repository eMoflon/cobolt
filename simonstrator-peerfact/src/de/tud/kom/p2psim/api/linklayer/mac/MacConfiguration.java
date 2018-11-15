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

package de.tud.kom.p2psim.api.linklayer.mac;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.impl.linklayer.LinkLayerFactory;

/**
 * We want to ease configuration - therefore each MAC provides one
 * {@link MacConfiguration} that can be used in conjunction with the
 * {@link LinkLayerFactory}. Each configuration can provide a number of options
 * to the user (but it should always assume reasonable default values). The idea
 * behind that: a user configures a LinkLayer using only these Configuration
 * Objects, keeping simple configs very short. Please make sure that especially
 * the Config of your MAC-Layer is very well documented!
 * 
 * <pre>
 * &lt;LinkLayer class="path.to.LinkLayerFactory"&gt;
 * 	&lt;Mac class="path.to.macConfigurationImplementationA" /&gt;
 * 	&lt;Mac class="path.to.macConfigurationImplementationB"&gt;
 * 		[Additional Options for MacB]
 * 	&lt;/Mac&gt;
 * &lt;/LinkLayer&gt;
 * </pre>
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 22.02.2012
 */
public interface MacConfiguration {

	/**
	 * We request a configured Instance of the MAC-Layer for this host. This is
	 * the factory-method of the configuration.
	 * 
	 * @param host
	 * @param macAddress
	 *            Configure the MAC with the provided address!
	 * @return
	 */
	public MacLayer getConfiguredMacLayer(SimHost host, MacAddress address);

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
	 * of config-values that make no sense in your MAC-Layer, please check for
	 * the occurrence of such combinations and return false. The MacFactory will
	 * then issue an exception informing the user about possible conflicts.
	 * Please also make sure that getHelp() contains some information on this
	 * issue, as it will be printed out too.
	 * 
	 * If there is no way to mess up configuration of your MAC, just return true
	 * :)
	 * 
	 * @return
	 */
	public boolean isWellConfigured();

}
