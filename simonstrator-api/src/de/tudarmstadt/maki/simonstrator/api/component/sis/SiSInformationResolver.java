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
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSInformationConsumer.AggregationFunction;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSInformationConsumer.SiSConsumerHandle;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSInformationProvider.SiSProviderHandle;
import de.tudarmstadt.maki.simonstrator.api.component.sis.exception.InformationNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;

/**
 * Logic that is to be implemented as part of the SiS to resolve information.
 * 
 * @author Bjoern Richerzhagen
 *
 */
public interface SiSInformationResolver {

	/**
	 * Returns the local state, if the given request can be resolved locally
	 * (either by accessing the local storage for past values, or by querying
	 * one of the registered information providers).
	 * 
	 * @param observedNode
	 *            can be null, meaning we want to know sth. about our own local
	 *            node.
	 * @param request
	 *            the requests object contains QoS parameters related to the
	 *            request (e.g., desired granularity, max timeout)
	 * @return local data
	 */
	public <T> T resolveLocally(INodeID observedNode, SiSType<T> type,
			SiSRequest request) throws InformationNotAvailableException;

	/**
	 * Returns a map of all currently available local observations of the given
	 * type.
	 * 
	 * @param type
	 * @param request
	 * @return a (potentially empty) map of results
	 */
	public <T> Map<INodeID, T> getAllLocalObservations(SiSType<T> type,
			SiSRequest request);

	public <T> SiSConsumerHandle aggregatedObservation(
			AggregationFunction aggFunction, SiSType<T> type,
			SiSRequest request, SiSResultCallback<T> callback);

	public <T> SiSConsumerHandle rawObservations(SiSType<T> type,
			SiSRequest request, SiSResultCallback<Map<INodeID, T>> callback);

	public void removeConsumer(SiSConsumerHandle handle);

	/**
	 * Adds a local resolver to the SiS
	 * 
	 * @param type
	 * @param dataCallback
	 * @return
	 */
	public <T> SiSProviderHandle addObservationProvider(SiSType<T> type,
			SiSDataCallback<T> dataCallback);

	/**
	 * Removes the local provider with the given handle
	 * 
	 * @param handle
	 */
	public void removeLocalProvider(SiSProviderHandle handle);

}
