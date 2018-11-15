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

package de.tudarmstadt.maki.simonstrator.api.component;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.component.transition.TransitionEnabled;

/**
 * Components that are instantiated per host. Please note, that all
 * {@link HostComponent}s should be registered with a Host and built with a
 * {@link HostComponentFactory}. If you use components within your design, you
 * should define your own interfaces (do not use {@link Component}, as it has
 * another semantic meaning within the API). In case you are working on
 * Transitions within your sub-components (or getting paid via MAKI) we
 * recommend you check out the {@link TransitionEnabled} interface for your
 * sub-components.
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public interface HostComponent extends Component {

	/**
	 * Called once for the initial configuration of the component. This call is
	 * triggered by the Host, as soon as all components have been registered via
	 * Host.registerComponent(). This is usually done directly after the call to
	 * the respective {@link HostComponentFactory}.
	 */
	public void initialize();

	/**
	 * Called once if components are de-registered in the host. This method is
	 * not enabled for most lower-layer components such as transport or network
	 * components, as the behavior is not well-defined. It should only be used
	 * on overlays, services, and applications (i.e., everything on the 'upper'
	 * side of the API).
	 * 
	 */
	public void shutdown();

	/**
	 * Returns the host for access to other per-host components
	 * 
	 * @return
	 */
	public Host getHost();

}
