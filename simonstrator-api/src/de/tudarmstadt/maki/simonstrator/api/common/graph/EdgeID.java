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
import java.util.Map;

public class EdgeID extends GraphElementID {

	private static final long serialVersionUID = 1L;

	private final transient static Map<Long, EdgeID> ids = new LinkedHashMap<>();
	private final transient static Map<String, EdgeID> stringIds = new LinkedHashMap<>();

	public static EdgeID get(long longId) {
		EdgeID id = ids.get(longId);
		if (id == null) {
			id = new EdgeID(longId);
			ids.put(longId, id);
		}
		return id;
	}

	public static EdgeID get(String str) {
		EdgeID id = stringIds.get(str);
		if (id == null) {
			final int hashCode = determineHashCode(str);
			id = new EdgeID(str, hashCode);
			stringIds.put(str, id);
		}
		return id;
	}

	/**
	 * This method calculates the hash code for the given edge ID.
	 * 
	 * If the ID has the specific format for directed edges (e.g., 12->541),
	 * then the hash code is calculated in a way that avoids hash collisions (at
	 * least for node counts up to 1e5).
	 * 
	 * @param edgeId
	 * @return the hash code to be used
	 */
	private static int determineHashCode(String edgeId) {
		if (edgeId.matches("\\d+->\\d+")) {
			final String[] segments = edgeId.split("->");
			final int sourceId = Integer.parseInt(segments[0]);
			final int targetId = Integer.parseInt(segments[1]);
			return sourceId * 100000 + targetId;
		} else {
			return edgeId.hashCode();
		}
	}

	public static EdgeID get(INodeID startNode, INodeID endNode) {
		return get(startNode.valueAsString() + "->" + endNode.valueAsString());
	}

	protected EdgeID(long id) {
		super(id);
	}

	protected EdgeID(String stringId, int hashCode) {
		super(stringId, hashCode);
	}

}
