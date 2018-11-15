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

package de.tud.kom.p2psim.impl.topology.waypoints.graph;

import java.util.Set;
import java.util.WeakHashMap;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

/**
 * The simple weighted edge retrievable graph caches the edges
 * for later batch retrieval.
 * 
 * It uses the WeakHashMap to avoid unintentional references to
 * dead objects.
 * 
 * @author Fabio Zöllner
 * @version 1.0, 27.03.2012
 * @param <V> Vertices
 * @param <E> Edges
 */
public class DefaultDirectedWeightedEdgeRetrievableGraph<V, E> extends DefaultDirectedWeightedGraph<V, E> {
	private WeakHashMap<E, Object> edgeMap = new WeakHashMap<E, Object>();
	
	@SuppressWarnings("unchecked")
	public DefaultDirectedWeightedEdgeRetrievableGraph(EdgeFactory edgefactory) {
        super(edgefactory);
    }

    @SuppressWarnings("unchecked")
	public DefaultDirectedWeightedEdgeRetrievableGraph(Class class1) {
    	super(class1);
    }
    
    public E addEdge(V v1, V v2) {
        E e = getEdgeFactory().createEdge(v1, v2);
        addEdge(v1, v2, e);
        
        return e;
    }

    public boolean addEdge(V v1, V v2, E e) {
    	edgeMap.put(e, null);
    	
    	return super.addEdge(v1, v2, e);
    }
    
    public Set<E> getAllEdges() {
    	return edgeMap.keySet();
    }
}
