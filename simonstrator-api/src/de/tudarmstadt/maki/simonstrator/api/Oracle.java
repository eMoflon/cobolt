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

package de.tudarmstadt.maki.simonstrator.api;

import java.util.List;

import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.core.OracleComponent;

/**
 * The oracle object usually only makes sense in simulation environments. It can
 * be used within overlays to check if we are running on a simulative platform.
 * In this case, we might want to allow some cheats or global knowledge
 * functionality or there might be optimized code for performance reasons - this
 * would then be encapsulated into Oracle.isSimulation() calls.
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public final class Oracle {

	/*
	 * Provide meaningful Oracle via the oracle component
	 */

	private static OracleComponent oracle = null;

	private static OracleComponent getOracle() {
		if (oracle == null) {
			try {
				oracle = Binder.getComponent(OracleComponent.class);
			} catch (ComponentNotAvailableException e) {
				oracle = new OracleComponent() {
					@Override
					public boolean isSimulation() {
						return false;
					}

					@Override
					public List<Host> getAllHosts() {
						return null;
					}
				};
			}
		}
		return oracle;
	}

	/**
	 * Can be used within overlays to check if we are running on a simulative
	 * platform. In this case, we might want to allow some cheats or global
	 * knowledge functionality or there might be optimized code for performance
	 * reasons.
	 * 
	 * @return
	 */
	public static boolean isSimulation() {
		return getOracle().isSimulation();
	}

	/**
	 * Access to all host objects. This is usually only available in a
	 * simulation environment.
	 * 
	 * @return
	 */
	public static List<Host> getAllHosts() {
		return getOracle().getAllHosts();
	}

	/**
	 * Access to the host with the given {@link INodeID}
	 * 
	 * @param nodeId
	 * @return
	 */
	public static Host getHostByID(INodeID nodeId) {
		for (Host host : getAllHosts()) {
			if (host.getId().equals(nodeId)) {
				return host;
			}
		}
		return null;
	}

}
