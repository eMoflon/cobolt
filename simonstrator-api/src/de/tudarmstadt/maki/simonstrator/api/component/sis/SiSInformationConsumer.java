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

package de.tudarmstadt.maki.simonstrator.api.component.sis;

import java.util.Map;

import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.sis.exception.InformationNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;

/**
 * Consumer interface, i.e., interface used to access information from the SiS.
 * Complex request patterns are modeled using {@link SiSRequest}s. In general we
 * distinguish between <i>local state</i> and <i>observations</i>.
 * 
 * <i>Local state</i> is associated to our own node (e.g., a buffer length or a
 * battery level) and can be retrieved from one or more of the currently running
 * mechanisms on that node (without using monitoring). If the value cannot be
 * directly retrieved, it may be derived from another state value. As these
 * operations involve only the local node, the result is returned directly.
 * 
 * <i>Observations</i> denote state of other nodes in the network. An
 * observation might, for example, be the RTT to another node, or the average
 * RTT in the current subnet. Usually, some observations are kept locally (e.g.,
 * the RTT to direct neighbors), whereas others might need to be fetched by the
 * monitoring system (e.g., the average RTT in the whole subnet). Therefore, two
 * differnet access method types exist in this interface: local observations can
 * be retrieved without any network activity (e.g., solely based on local
 * available information of other mechanisms), meaning that the result is
 * directly available. Aggregated and raw observations answering a general
 * {@link SiSRequest} might involve monitoring or active collection, therefore
 * requiring a callback for result delivery.
 * 
 * @author Bjoern Richerzhagen
 *
 */
public interface SiSInformationConsumer {

	/**
	 * Identifiers of aggregation functions supported by the SiS and (hopefully)
	 * the underlying monitoring solution.
	 * 
	 * @author Bjoern Richerzhagen
	 *
	 */
	public enum AggregationFunction {
		AVG, MIN, MAX, SUM, COUNT
	}

	/**
	 * Retrieve local state information about our own node.
	 * Source-component/layer/mechanism can be filtered via the
	 * {@link SiSRequest} - object if required.
	 * 
	 * @param type
	 * @param request
	 *            a request object specifying the desired information or
	 *            SiSRequest.NONE if you do not want to specify request
	 *            properties.
	 * 
	 * @throws InformationNotAvailableException
	 *             if no data can be derived
	 * @return
	 */
	public <T> T localState(SiSType<T> type, SiSRequest request)
			throws InformationNotAvailableException;

	/**
	 * Retrieve an observation of another node (identified by its
	 * {@link INodeID}). In an overlay sense, an observed node would be a
	 * neighbor in the overlay (e.g., a routing table entry). This method only
	 * uses local information or derivations, it does not trigger network
	 * operations. Use the callback-based method if you also allow collection of
	 * the requested value in the network.
	 * 
	 * In case you do not yet know the ID of the node (e.g., you want to
	 * retrieve all known RTTs), just use the localObservationsOf method
	 * returning a map of {@link INodeID}s and values.
	 * 
	 * @param observedNode
	 *            id of the node you want to get information on (Use
	 *            INodeID.get() to retrieve such an ID based on a number of
	 *            input identifiers). If the ID is equal to (one of) our own
	 *            local IDs, this method behaves just as localState(...) does.
	 * @param type
	 * @param request
	 *            a request object specifying the desired information or
	 *            SiSRequest.NONE if you do not want to specify request
	 *            properties.
	 * 
	 * @throws InformationNotAvailableException
	 * @return
	 */
	public <T> T localObservationOf(INodeID observedNode, SiSType<T> type,
			SiSRequest request) throws InformationNotAvailableException;

	/**
	 * Returns an observation made by the observer node of the state of the
	 * observed node, iff the information is available locally (as part of the
	 * usual monitoring process). E.g., if I am node A and I want to know the
	 * RTT between B and C, I can retrieve the RTT between B and C as observed
	 * by B or by C.
	 * 
	 * Basically, this method provides a convenience bridge to the
	 * MonitoringComponents getCurrentDataGraph()-method for a common mechanism
	 * query. Does not trigger collection.
	 * 
	 * @param observer
	 *            the observer, e.g., the source of the information
	 * @param observedNode
	 *            the node we want to retrieve information about (from the
	 *            observers viewport)
	 * @param type
	 * @param request
	 * @return
	 * @throws InformationNotAvailableException
	 */
	@Deprecated
	public <T> T remoteObservationOf(INodeID observer, INodeID observedNode,
			SiSType<T> type, SiSRequest request)
			throws InformationNotAvailableException;

	/**
	 * Retrieve a map of all currently observed nodes and the available state
	 * information for the given {@link SiSType} and {@link SiSRequest}. Can be
	 * used, e.g., during the discovery phase of an overlay to retrieve the RTT
	 * to all currently known hosts measured by any other overlay.
	 * 
	 * @param type
	 * @param request
	 *            a request object specifying the desired information or
	 *            SiSRequest.NONE if you do not want to specify request
	 *            properties.
	 * @return a map, can be empty.
	 */
	public <T> Map<INodeID, T> allLocalObservations(SiSType<T> type,
			SiSRequest request);

	/**
	 * Request an aggregate according to the {@link SiSRequest}s parameters and
	 * the given {@link AggregationFunction}. Usually, these requests trigger
	 * monitoring. Sometimes, local data might be sufficient to answer the
	 * request, depending on the resolver logic - leading to an instant trigger
	 * of the provided callback.
	 * 
	 * @param aggFunction
	 *            desired aggregation
	 * @param type
	 *            the {@link SiSType}
	 * @param request
	 *            additional request properties
	 * @param callback
	 *            result callback
	 * @return
	 */
	public <T> SiSConsumerHandle aggregatedObservation(
			AggregationFunction aggFunction, SiSType<T> type,
			SiSRequest request, SiSResultCallback<T> callback);

	/**
	 * Requests raw values (i.e., per node) for the given type according to the
	 * {@link SiSRequest} parameters (e.g., regionally, globally, ...). Usually,
	 * this request involves active collection (monitoring) to be answered. If
	 * the request can be answered using local state (maybe from past requests)
	 * without violating the {@link SiSRequest} parameters, the callback will
	 * fire immediately with the local results.
	 * 
	 * @param type
	 *            the {@link SiSType}
	 * @param request
	 *            additional request properties
	 * @param callback
	 *            result callback, containing a map of values associated to node
	 *            IDs
	 */
	public <T> SiSConsumerHandle rawObservations(SiSType<T> type,
			SiSRequest request, SiSResultCallback<Map<INodeID, T>> callback);

	/**
	 * Revoke interest in the collected metrics registered with the provided
	 * handle
	 * 
	 * @param handle
	 */
	public void revoke(SiSConsumerHandle handle);

	/**
	 * A unique handle that allows the consumer to revoke interest in collected
	 * metrics.
	 * 
	 * @author Bjoern Richerzhagen
	 *
	 */
	public interface SiSConsumerHandle {
		// marker
	}

}
