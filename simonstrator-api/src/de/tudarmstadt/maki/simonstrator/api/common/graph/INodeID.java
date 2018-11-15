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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * As the SiS and Monitoring deal with multiple topologies collected in the
 * underlay, any overlay and the applications, we need a common identifier for
 * nodes. The host now has an {@link INodeID}, which is also contained in
 * {@link IOverlayContact}s.
 * 
 * @author Bjoern Richerzhagen
 *
 */
public class INodeID extends GraphElementID {

	private static final long serialVersionUID = 1L;

	protected final transient static Map<Long, INodeID> ids = new LinkedHashMap<>();
	protected final transient static Map<String, INodeID> stringIds = new LinkedHashMap<>();
	
	public static INodeID get(long longId) {
		INodeID id = ids.get(longId);
		if (id == null) {
			id = new INodeID(longId);
			ids.put(longId, id);
		}
		return id;
	}

	public static INodeID get(String str) {
		INodeID id = stringIds.get(str);
		if (id == null) {
			id = new INodeID(str);
			stringIds.put(str, id);
		}
		return id;
	}
	
	/**
	 * Convenience method returns a set containing only one id
	 * 
	 * @param id
	 * @return
	 */
	public static Set<INodeID> getSingleIDSet(INodeID id) {
		Set<INodeID> set = new LinkedHashSet<>();
		set.add(id);
		return set;
	}

	/**
	 * Private constructor
	 * 
	 * @param id
	 */
	protected INodeID(long id) {
		super(id);
	}

	protected INodeID(String stringId) {
		super(stringId);
	}
}
