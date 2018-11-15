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

import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSInformationConsumer.SiSConsumerHandle;


/**
 * A result-callback (as method calls are non-blocking!) that is to be provided
 * by the requesting component.
 * 
 * @author Bjoern Richerzhagen
 *
 * @param <T>
 */
public interface SiSResultCallback<T> {

	/**
	 * Providing some information, as to why a given request was not answered.
	 * 
	 * TODO extend this
	 * 
	 * @author Bjoern Richerzhagen
	 *
	 */
	public enum AbortReason {
		UNKNOWN, TIMEOUT, NO_DATA
	}

	/**
	 * Called, whenever results to the given request arrive at the SiS.
	 * Depending on the request resolver, this call might be triggered more than
	 * once (e.g., periodically). Use the {@link SiSConsumerHandle} obtained
	 * upon requesting the data or provided as a parameter to revoke ay active
	 * requests.
	 * 
	 * @param result
	 */
	public void onResult(T result, SiSConsumerHandle consumerHandle);

	/**
	 * Called, when the request is aborted by the SiS. No further calls of
	 * onResult or onAbort will occur in this case. The request does not need to
	 * be revoked via the {@link SiSConsumerHandle}
	 * 
	 * @param reason
	 */
	public void onAbort(AbortReason reason);

}
