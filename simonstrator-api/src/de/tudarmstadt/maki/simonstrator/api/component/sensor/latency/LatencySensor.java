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

package de.tudarmstadt.maki.simonstrator.api.component.sensor.latency;

import java.util.Set;

import de.tudarmstadt.maki.simonstrator.api.component.overlay.OverlayContact;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.SensorComponent;

/**
 * Latency sensor that provides access to the last known latency to specified
 * {@link ProbeHandler}s (i.e. {@link OverlayContact}s).
 * 
 * Retrieve a {@link LatencyRequest} using the {@link LatencySensor}s
 * implementation. Afterwards request latency updates from specified nodes
 * {@link ProbeHandler}, provide a {@link LatencyListener} that handles the
 * latency updates and give the retrieved {@link LatencyRequest}.
 * 
 * 
 * @author Nils Richerzhagen & Bjoern Richerzhagen
 */
public interface LatencySensor extends SensorComponent {

	/**
	 * The final port for {@link LatencySensor} communication.
	 */
	public final static int _LATENCY_SERVICE_PORT = 2266;

	/**
	 * After requesting a {@link LatencyRequest} one can request
	 * {@link LatencySensor} readings using this register method.
	 * 
	 * From that point in time, the latency measurements are provided with the
	 * characteristics of the {@link LatencyRequest}.
	 * 
	 * @param handler
	 *            - to whom should the latency be calculated.
	 * @param listener
	 *            - who wants the information callback.
	 * @param request
	 *            - the request characteristics.
	 */
	public void requestLatencyUpdates(ProbeHandler handler,
			LatencyListener listener, LatencyRequest request);

	/**
	 * Remove the respective {@link LatencyListener} from the update list.
	 * 
	 * @param listener
	 */
	public void removeLatencyUpdates(LatencyListener listener);

	/**
	 * Retrieve a {@link LatencyRequest} object with the specified interval for
	 * requestLocationUpdates
	 * 
	 * @return
	 */
	public LatencyRequest getLatencyRequest(long interval);

	/**
	 * The Set of {@link OverlayContact}s that are to be measured. I.e. the
	 * {@link OverlayContact}s that need to be ping'ed by the
	 * {@link LatencySensor}s implementation.
	 * 
	 * @author Nils Richerzhagen
	 *
	 */
	public static interface ProbeHandler {

		/**
		 * The Set of {@link OverlayContact}s.
		 * 
		 * @return
		 */
		public Set<OverlayContact> getContacts();
	}

	/**
	 * The {@link LatencyListener}, called once the latency is updated.
	 * Including the respective {@link OverlayContact} to which the latency is
	 * measured.
	 * 
	 * This should be implemented by the requesting component or any other
	 * component to handle the latency updates.
	 * 
	 * @author Nils Richerzhagen
	 *
	 */
	public static interface LatencyListener {

		/**
		 * The listener for any latency updates.
		 * 
		 * @param contact
		 *            - the contact {@link OverlayContact} to whom this latency
		 *            is measured
		 * @param latency
		 *            - the latency {@link Long} between the two (one-way) in
		 *            Milliseconds.
		 */
		public void updatedLatency(OverlayContact contact, long latency);
	}
}
