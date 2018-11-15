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

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;

/**
 * Interface used by the SiS to enable components to provide additional
 * information (e.g., by adding local state to the SiS or by providing access to
 * internal metrics).
 * 
 * This interface is <strong>invoked</strong> by other components, not
 * implemented by them. If you want to store state in the SiS, just invoke
 * provide() on the {@link SiSComponent}. To describe your data source, make use
 * of the {@link SiSInfoPropertiesFactory} and the resulting
 * {@link SiSInfoProperties} objects reachable via the {@link SiSComponent}.
 * 
 * @author Bjoern Richerzhagen
 *
 */
public interface SiSInformationProvider {

	/**
	 * This method denotes that state about another nodes or our own node has
	 * been collected. State of the own node is identified by our
	 * {@link INodeID}, accessible via the {@link Host} interface.
	 * 
	 * @param type
	 * @param dataCallback
	 *            containing the {@link SiSInfoProperties}
	 * @return
	 */
	public <T> SiSProviderHandle nodeState(SiSType<T> type,
			SiSDataCallback<T> dataCallback);

	/**
	 * Revoke access to the information registered with the provided handle
	 * 
	 * @param handle
	 */
	public void revoke(SiSProviderHandle handle);

	/**
	 * A unique handle that allows the provider to revoke access to local
	 * metrics.
	 * 
	 * @author Bjoern Richerzhagen
	 *
	 */
	public interface SiSProviderHandle {
		// marker
	}

}
