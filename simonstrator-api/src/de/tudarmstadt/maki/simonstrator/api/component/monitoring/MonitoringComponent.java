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

package de.tudarmstadt.maki.simonstrator.api.component.monitoring;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSComponent;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSInformationConsumer.AggregationFunction;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSInformationProvider;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSRequest;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;

/**
 * Control interface for generic Monitoring components, as used by the
 * {@link SiSComponent}.
 * 
 * TODO the {@link MonitoringComponent} should not register via the
 * {@link SiSInformationProvider}, instead the SiS should actively use the
 * getCurrentDataGraph() method to avoid needless overhead.
 * 
 * @author Bjoern Richerzhagen
 *
 */
public interface MonitoringComponent extends HostComponent {

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
	 * @param resultCallback
	 *            triggered, once the result is available
	 * @return
	 */
	public <T> MonitoringHandle collectAggregatedObservation(
			AggregationFunction aggFunction, SiSType<T> type,
			SiSRequest request, MonitoringResultCallback<T> resultCallback);

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
	public <T> MonitoringHandle collectRawObservations(SiSType<T> type,
			SiSRequest request);

	/**
	 * Returns the complete monitoring information available locally on the
	 * current node, encoded within a {@link Graph}. The {@link IEdge}s in the
	 * graph contain observations, e.g.: source of the edge is the observer,
	 * destination of the edge is the observed node. For example: edge from A to
	 * B carrying the property RTT with value 200ms means: A thinks its RTT to B
	 * is 200ms. The {@link INode}s in the graph carry local state.
	 * 
	 * This method does not trigger any collection process - it just returns the
	 * current state. New types can be triggered via the other methods provided
	 * in {@link MonitoringComponent}
	 * 
	 * @return
	 */
	public Graph getCurrentDataGraph();

	/**
	 * Stop collecting
	 * 
	 * @param handle
	 */
	public void stopCollecting(MonitoringHandle handle);

	public interface MonitoringHandle {
		// marker
	}

	public interface MonitoringResultCallback<T> {
		public void onResult(T result);
	}

}
