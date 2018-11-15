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

package de.tud.kom.p2psim.impl.topology.views.visualization.ui;

import javax.swing.JComponent;

/**
 * This component is targeted at demonstrations, where you want to provide some
 * application-specific controls and metric visualizations in addition to the
 * usual layer of elements on top of the world visualization.
 * 
 * Components implementing the {@link InteractiveVisualizationComponent} just
 * need to provide another JComponent that is displayed as a sidebar.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, Jul 5, 2016
 */
public interface InteractiveVisualizationComponent
		extends VisualizationComponent {

	/**
	 * Sidebar component, might contain plots or controls or any arbitrarily
	 * complex stuff. For some helpers on common elements (e.g., plots) please
	 * refer to the {@link VisHelper} class.
	 * 
	 * @return
	 */
	public JComponent getSidebarComponent();

}
