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

package de.tud.kom.p2psim.api.topology.social;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.topology.TopologyListener;

/**
 * This interface contains the View for the Social Graph. It has the interface
 * to request the structure of the social graph. Additionally it contains the
 * activity and the interactions of the nodes, which can be used to fill a
 * workload generator. With activity, we mean the activity of the user. This is
 * a value between 0 and 1. The interactions is a edge weight, which represents
 * the interactions between the nodes. The value for the interactions is between
 * 0 and 1.<br>
 * Further, it exists the possibility to derive the community Structure of the
 * social graph. For this, the nodes will be split in clusters.
 * 
 * <p>
 * NOTE: The social connections must <b>not</b> be directional. It is possible
 * that many links are undirected!
 * 
 * <p>
 * ATTENTION: It is possible to create multiple Social graph Views (for example,
 * one for Application and one for Mobility Model). It is not possible to create
 * different {@link SocialView}s for different Host Groups, with the same
 * identifier, because the using Component doesn't know the right component for
 * the host.
 * 
 * @author Christoph Muenker
 * @version 1.0, 06.06.2013
 */
public interface SocialView extends TopologyListener {

	/**
	 * Initialize the Social Graph. In the most cases, the Social Graph needs
	 * the number of available hosts. But this is only possible, after all Hosts
	 * are created and added through the {@link TopologyListener}.
	 */
	public void initialize();

	/**
	 * Gets an identifier for this Social View. This can be used to find the
	 * right SocialView.
	 * 
	 * @return The identifier for this Social View
	 */
	public String getIdentifier();

	/**
	 * Checks the social view, if the host was registered!
	 * 
	 * @param host
	 *            The host, which should be checked
	 * @return <code>true</code> if the host is added to this view, otherwise
	 *         <code>false</code>
	 */
	public boolean isHostInView(SimHost host);

	/**
	 * Gets the social neighbors to the Host.
	 * 
	 * @param host
	 *            The host, to which the social neighbors should be found.
	 * @return A list of {@link Host}s, which are connected with the given host.
	 *         If the host not exists in the graph, then will be returned
	 *         <code>null</code>.
	 */
	public List<SimHost> getNeighbors(SimHost host);

	/**
	 * Is the Host x with the Host y connected? Note, that y must not be
	 * connected with x, because the connections can be unidirectional!
	 * 
	 * @param x
	 *            The host x
	 * @param y
	 *            The host y
	 * @return <code>true</code> if it exists a link between x and y, otherwise
	 *         <code>false</code>;
	 */
	public boolean isXWithYConnected(SimHost x, SimHost y);

	/**
	 * Gets the Communities Clusters for all Hosts. A host is only in one
	 * cluster!
	 * 
	 * @return A Set of Clusters, which contains a Set of {@link Host}s.
	 */
	public Set<Set<SimHost>> getClusters();

	/**
	 * Gets the Cluster with {@link Host}s, in which the given host is.
	 * 
	 * @param host
	 *            The host that belongs to the requested cluster.
	 * @return A Set of Hosts, which are all in the same Cluster. The given host
	 *         is in this Set. If the host doesn't exists in the graph, then
	 *         will be returned null.
	 */
	public Set<SimHost> getCluster(SimHost host);

	/**
	 * Check are the two given hosts in the same cluster.
	 * 
	 * @param x
	 *            The Host x
	 * @param y
	 *            The Host y
	 * @return <code>true</code> if x and y are in the same cluster, otherwise
	 *         <code>false</code>;
	 */
	public boolean isInSameCluster(SimHost x, SimHost y);

	/**
	 * Gets the activity for a host. This activity is a value between 0 and 1.
	 * The activity is only a factor.
	 * 
	 * @param host
	 *            A host, for which the activity is requested.
	 * @return A value between 0 and 1. If the host doesn't exist in the graph,
	 *         then will be returned 0.
	 */
	public double getActivity(SimHost host);

	/**
	 * Gets to a host, the interactions. This mean, that to every neighbor
	 * exists a factor with a value between 0 and 1, which describes the
	 * interaction between them.
	 * 
	 * @param host
	 *            The host, for which are the interactions is requested.
	 * @return A Map with all neighbors and the interaction between the host and
	 *         a neighbor. If the host doesn't exist in the graph, then will be
	 *         returned <code>null</code>.
	 */
	public Map<SimHost, Double> getInteractions(SimHost host);

	/**
	 * Gets the interaction between host x and host y. If there are no
	 * neighbors, then is the value 0.
	 * <p>
	 * NOTE: The call (x,y) can produce an other value as (y,x)!
	 * 
	 * @param x
	 *            The host x.
	 * @param y
	 *            The host y.
	 * @return A value between 0 and 1. If one host not exist in the graph, then
	 *         will be returned 0.
	 */
	public double getInteractionBetween(SimHost x, SimHost y);
}
