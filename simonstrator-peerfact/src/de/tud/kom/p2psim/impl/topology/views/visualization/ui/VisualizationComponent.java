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
import javax.swing.JMenu;

/**
 * This interface is to be implemented by all components that are to be added to
 * the visualization.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, Jul 4, 2016
 */
public interface VisualizationComponent {

	/**
	 * Name of the component to be used in menus.
	 * 
	 * @return
	 */
	public String getDisplayName();

	/**
	 * The actual component to be drawn (this is drawn on top of the main
	 * visualization area!)
	 * 
	 * @return
	 */
	public JComponent getComponent();

	/**
	 * May return null, if no menu is to be shown.
	 * 
	 * @return
	 */
	public JMenu getCustomMenu();

	/**
	 * Return true, if this component is hidden by default.
	 * 
	 * @return
	 */
	public boolean isHidden();

}
