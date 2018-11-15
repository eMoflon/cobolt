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

package de.tudarmstadt.maki.simonstrator.api.component.overlay;


/**
 * This interface may be implemented by a {@link OverlayComponent} to provide
 * information about the current node state that can be queried by a
 * visualization, for example. This is not intended to be a complex analyzer.
 * 
 * Some functions that should be valid on all types of overlays are already
 * defined in this interface to ease interoperability. Overlay-specific
 * information can be retrieved by a string-identifier.
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public interface NodeInformation {

	/*
	 * General functions
	 */

	/**
	 * Only show active components.
	 * 
	 * @return
	 */
	public boolean isActive();

	/**
	 * This string is - for example - shown in the visualization. Add all the
	 * relevant information you want to appear there.
	 * 
	 * @return
	 */
	public String getNodeDescription();

	/*
	 * Color-concept
	 */

	/**
	 * A "color" is used to distinguish nodes of the same overlay, that might
	 * currently offer different functionality or be in a different state. The
	 * "meaning" of a color is undefined at this stage, the only contract is
	 * that all nodes of the same color should share the same distinct feature
	 * that made them wear this color in the first place. Per contract, the
	 * color -1 counts as "null", i.e., it is ignored.
	 * 
	 * There may exist multiple dimensions of colors, as defined by the overlay.
	 * 
	 * This is NOT a color in the sense of awt.color...
	 * 
	 * @param dimension
	 *            starting with 0
	 * 
	 * @return a "color", starting with 0. -1 is interpreted as uncolored. There
	 *         are no gaps allowed, i.e., if color 3 is returned, color 2 and 1
	 *         must exist as well.
	 */
	public int getNodeColor(int dimension);

	/**
	 * Number of dimensions of colors.
	 * 
	 * @return
	 */
	public int getNodeColorDimensions();

	/**
	 * Short textual descriptions of NodeColorDimensions. In a visualization,
	 * those dimensions translate into checkboxes to turn on/off a given layer
	 * of information.
	 * 
	 * @return
	 */
	public String[] getNodeColorDimensionDescriptions();

	/**
	 * All descriptions of colors within a given dimension.
	 * 
	 * @param dimension
	 * @return
	 */
	public String[] getNodeColorDescriptions(int dimension);
	
}
