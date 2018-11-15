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

/**
 * 
 */
package de.tud.kom.p2psim.impl.scenario.simcfg2.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.tud.kom.p2psim.impl.scenario.simcfg2.SimCfgConfigurator;

/**
 * Indicates that the method it is applied to
 * should be called by the {@link SimCfgConfigurator} to configure
 * the component.
 * 
 * Expected signature of the method:
 * public void|boolean method();
 * 
 * Parameters can be:
 * - ParsedConfiguration
 * - Configurator | SimCfgConfigurator
 * - HostBuilder
 * 
 * Should the return type of the method be boolean returning
 * false will indicate that the method has to be called again
 * and configuration isn't done yet. This happens after every
 * configured component was called once.
 * 
 * @author Fabio Zöllner
 * @version 1.0, 07.05.2012
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Configure {
}
