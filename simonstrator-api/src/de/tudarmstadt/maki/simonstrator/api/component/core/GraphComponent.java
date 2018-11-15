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

package de.tudarmstadt.maki.simonstrator.api.component.core;

import de.tudarmstadt.maki.simonstrator.api.Binder;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.component.GlobalComponent;

/**
 * Enables replacement of the default GraphComponent contained in the API. To
 * add a custom realization, it has to be registered with the {@link Binder}
 * during initialization of the platform.
 * 
 * @author Bjoern Richerzhagen
 *
 */
public interface GraphComponent extends GlobalComponent {

	/*
	 * TODO Define meaningful interfaces and helpers in here, if needed. Note,
	 * that most graph-related objects (like nodes and edges) should be created
	 * via the graph interface itself.
	 */

	/**
	 * Returns a new graph object (actual implementation is up to the runtime)
	 * 
	 * @return
	 */
	public Graph createGraph();

}
