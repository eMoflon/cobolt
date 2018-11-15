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

import java.util.List;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.common.SimHostComponent;
import de.tud.kom.p2psim.api.topology.TopologyComponent;

/**
 * An Energy Model in PeerfactSim.KOM is a Component (as it is configured on a
 * per-host basis with {@link EnergyComponent}s and energy values for these
 * components). The EnergyComponents are registered with the model inside the
 * Factory for the EnergyModel. To access {@link EnergyComponent}s and
 * information about the current battery state, you access this model via the
 * {@link SimHost} object and use the {@link EnergyInfo}-API.
 * 
 * The integration of advanced features such as energy consumption for position
 * determination is implemented in the component that provides the information.
 * In our example, a call to {@link TopologyComponent} getPosition(accuracy)
 * will trigger the corresponding energy consumption if a Positioning-Module is
 * configured as part of this energy model. Long sentence short: in most cases
 * your application should <b>not</b> access {@link EnergyComponent}s directly
 * via this interface but instead rely on the services provided by the
 * corresponding layer.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 21.02.2012
 */
public interface EnergyModel extends SimHostComponent {

	/**
	 * Use the {@link EnergyInfo}-object to access read-only status information
	 * about your host. This information can then be used to decide based on the
	 * current energy state.
	 * 
	 * @return
	 */
	public EnergyInfo getInfo();

	/**
	 * Save some casting and use this method to return all components of the
	 * given type and the given ... type. This is handy if you want to perform
	 * actions on a more advanced component such as a communication interface
	 * which may provide additional methods.
	 * 
	 * @param type
	 * @param componentClass
	 * @return
	 */
	public <T extends EnergyComponent> List<T> getComponents(
			ComponentType type, Class<T> componentClass);

	/**
	 * If only one instance of a component with the given {@link ComponentType}
	 * exists, you can use this method to ease access to the component. No
	 * warning or error is generated if there were multiple instances, instead
	 * the first encountered instance is returned.
	 * 
	 * @param type
	 * @param componentClass
	 * @return
	 */
	public <T extends EnergyComponent> T getComponent(ComponentType type,
			Class<T> componentClass);

	/**
	 * Add a new component to this hosts energy model. This will enable the
	 * polling for energy consumption for this component.
	 * 
	 * @param comp
	 */
	public void registerComponent(EnergyComponent comp);

	/**
	 * Resets all energy components to their configured defaults
	 */
	public void reset();

}
