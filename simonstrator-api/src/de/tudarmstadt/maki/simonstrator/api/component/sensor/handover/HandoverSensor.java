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

package de.tudarmstadt.maki.simonstrator.api.component.sensor.handover;

import de.tudarmstadt.maki.simonstrator.api.component.network.NetworkComponent.NetInterfaceName;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.SensorComponent;

/**
 * Simple sensor notifying the interested parties of a handover from Mobile to
 * Wi-Fi-AP connectivity, associated to the {@link NetInterfaceName} MOBILE.
 * 
 * This mimics a smartphone's behavior when performing a transparent handover
 * between cellular Internet connectivity and Wi-Fi access point based
 * connectivity.
 * 
 * Note: in peerfact this is implemented within the 5GTopologyView. The
 * underlying {@link NetInterfaceName} does not change (still MOBILE), only the
 * link properties vary.
 * 
 * On Android, this should notify applications and overlays as soon as a
 * handover from Cell to Access point took place.
 * 
 * @author Bjoern Richerzhagen
 *
 */
public interface HandoverSensor extends SensorComponent {

	/**
	 * True, if this device is currently connected to an access point.
	 * 
	 * @return
	 */
	public boolean isConnectedToAccessPoint();

	public void addHandoverListener(HandoverListener listener);

	public boolean removeHandoverListener(HandoverListener listenerToRemove);

	public interface HandoverListener {

		public void onHandover(boolean handoverToAccessPoint);

	}

}
