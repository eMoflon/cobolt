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


package de.tudarmstadt.maki.simonstrator.overlay.api;

import de.tudarmstadt.maki.simonstrator.api.component.overlay.OverlayComponent;
import de.tudarmstadt.maki.simonstrator.api.component.overlay.OverlayContact;

/**
 * The <code>OverlayNode</code> interface is the root interface of all specific
 * OverlayNodes. In general, an OverlayNode represents an instance of a
 * participant in the overlay. Note that one or more OverlayNodes may be hosted
 * by a single <NetID>. Participating OverlayNodes are assigned uniform random
 * <code>OverlayID</code>s from a large identifier space. Application-specific
 * objects (documents etc.) are assigned unique identifiers called
 * <code>OverlayKey</code>s, selected from the same identifier space.
 * OverlayNodes can perform several actions like joining, leaving or execute
 * particular operations depending whether the overlay is structured,
 * unstructured or hybrid.
 * 
 * @author Sebastian Kaune <kaune@kom.tu-darmstadt.de>
 * @version 1.0, 11/25/2007
 */
public interface OverlayNode extends OverlayComponent {

	/**
	 * Returns the OverlayContact of this node, which includes its IP-Adress and
	 * its OverlayNodeID
	 * 
	 * @return
	 */
	public OverlayContact getLocalOverlayContact();

}
