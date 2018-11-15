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

import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;

/**
 * This component provides the service interface for the SiS (System Information
 * Service), acting as a proxy to resolve information. Internally, the SiS uses
 * local storage as well as the monitoring component to answer data requests.
 * This interface is to be used by components that want to access the SiS to (i)
 * access information from other components or the network, or (ii) provide
 * information to other components or the network.
 * 
 * Bringing providers and consumers together is then an integral part of the
 * SiS's internal logic.
 * 
 * The intentions and the contracts of this component are based on a proposal in
 * the MAKI-Seminar (see Synology for slideset).
 * 
 * @author Bjoern Richerzhagen
 *
 */
public interface SiSComponent extends HostComponent {

	/**
	 * Enables components to provide information (once or periodically or based
	 * on events) using the {@link SiSInformationProvider} interface of the SiS.
	 * For most methods, you need to create a {@link SiSRequest} first, using
	 * the createRequestFor-method of the SiS.
	 * 
	 * @return
	 */
	public SiSInformationProvider provide();

	/**
	 * Access to information in the SiS. The complex logic is hidden behind the
	 * {@link SiSInformationConsumer} interface. For most methods, you need to
	 * create a {@link SiSRequest} first, using the createRequestFor-method of
	 * the SiS.
	 * 
	 * @return
	 */
	public SiSInformationConsumer get();

}
