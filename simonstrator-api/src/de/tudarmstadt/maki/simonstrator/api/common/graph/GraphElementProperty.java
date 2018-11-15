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

package de.tudarmstadt.maki.simonstrator.api.common.graph;

import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;

/**
 * Graph element property. In most cases you should be able to use the available
 * {@link SiSType}s. If no matching type is registered, or you need custom
 * properties, just create an instance of this class. The resulting
 * {@link SiSType} is <strong>NOT</strong> added to the global SiS namespace
 * (SiSTypes).
 * 
 * @author Bjoern Richerzhagen
 *
 */
public class GraphElementProperty<T> extends SiSType<T> {

	public GraphElementProperty(String name, Class<T> valueType) {
		/*
		 * To avoid collisions, a name prefix is added.
		 */
		super("GraphElementProperty-" + name, valueType, null);
	}

}
