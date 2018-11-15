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

import java.util.Set;

import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSInformationProvider.SiSProviderHandle;
import de.tudarmstadt.maki.simonstrator.api.component.sis.exception.InformationNotAvailableException;

/**
 * Callback provided by the DataProvider that is then queried for the actual
 * data if the SiS needs it. If the data is (currently) not available, throw an
 * {@link InformationNotAvailableException} exception. This does
 * <strong>not</strong> remove the source. Instead, use revoke to tell the SiS
 * that you do no longer provide information.
 * 
 * @author Bjoern Richerzhagen
 *
 * @param <T>
 *            result type
 */
public interface SiSDataCallback<T> {

	/**
	 * Return the current value
	 * 
	 * @param providerHandle
	 *            can be used to revoke access to the provider
	 * @return
	 */
	public T getValue(INodeID nodeID, SiSProviderHandle providerHandle)
			throws InformationNotAvailableException;

	/**
	 * Returns a list of all {@link INodeID}s we currently have data for
	 * 
	 * @return
	 */
	public Set<INodeID> getObservedNodes();

	/**
	 * Description of the data provided via this callback.
	 * 
	 * @return
	 */
	public SiSInfoProperties getInfoProperties();

}
