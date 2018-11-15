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
import de.tudarmstadt.maki.simonstrator.api.component.sis.exception.InformationNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSTypeDerivation;

/**
 * Access current data hidden behind a Type. This is currently used for the
 * {@link SiSTypeDerivation}s, but may also be an abstraction for the local
 * storage later on.
 * 
 * TODO this does explicitly not handle sets, as we define types to be flat.
 * 
 * @author Bjoern Richerzhagen
 *
 */
public interface SiSLocalData {

	/**
	 * Returns the current value of the specified type
	 * 
	 * @param nodeId
	 * @param type
	 * @param request
	 *            (optional)
	 * @return
	 */
	public <T> T getValue(INodeID nodeId, SiSType<T> type,
			SiSRequest request)
			throws InformationNotAvailableException;

	/**
	 * Returns a list of all {@link INodeID}s we currently have data for
	 * 
	 * @return
	 */
	public <T> Set<INodeID> getObservedNodes(SiSType<T> type,
			SiSRequest request);

}
