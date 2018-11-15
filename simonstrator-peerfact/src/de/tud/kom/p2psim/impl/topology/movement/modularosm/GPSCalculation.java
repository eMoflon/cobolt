/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
 *
 * This file is part of PeerfactSim.KOM.
 * 
 * PeerfactSim.KOM is free software: you can redistribute it and/or modify
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

package de.tud.kom.p2psim.impl.topology.movement.modularosm;

import de.tud.kom.p2psim.api.topology.Topology;
import de.tud.kom.p2psim.impl.topology.views.VisualizationTopologyView.VisualizationInjector;
import de.tudarmstadt.maki.simonstrator.api.Binder;

/**
 * 
 * @author Martin Hellwig
 * @version 1.0, Nov 3, 2015
 */
public class GPSCalculation {

	private static double latCenter;

	private static double lonCenter;

	private static int zoom;

	private static double scaleFactor;

	public GPSCalculation() {
	}

	private void setScaleFactor() {
		// this.scaleFactor = Math.pow(2.0d, (13 - zoom));
		/*
		 * BR: set scaleFactor to fixed zoom level 15 ==> 0.125 (as in this
		 * case, 1px == 1m) - this way, the world-size specified in the configs
		 * is valid on all zoom levels.
		 */
		this.scaleFactor = 0.125;
		// 17: 2, 16: 1, 15: 0.5, 14: 0.25
		VisualizationInjector.setScale(Math.pow(2.0d, (zoom - 16)));
	}

	public static double getLatCenter() {
		return latCenter;
	}

	public static double getLonCenter() {
		return lonCenter;
	}

	public static int getZoom() {
		return zoom;
	}

	public static double getLatUpper() {
		return latCenter + scaleFactor * 0.027613 * Binder
				.getComponentOrNull(Topology.class).getWorldDimensions().getX()
				/ 1000;
	}

	public static double getLatLower() {
		return latCenter - scaleFactor * 0.027613 * Binder
				.getComponentOrNull(Topology.class).getWorldDimensions().getX()
				/ 1000;
	}

	public static double getLonLeft() {
		return lonCenter - scaleFactor * 0.0419232 * Binder
				.getComponentOrNull(Topology.class).getWorldDimensions().getY()
				/ 1000;
	}

	public static double getLonRight() {
		return lonCenter + scaleFactor * 0.0419232 * Binder
				.getComponentOrNull(Topology.class).getWorldDimensions().getY()
				/ 1000;
	}

	public void setLatCenter(double latCenter) {
		this.latCenter = latCenter;
	}

	public void setLonCenter(double lonCenter) {
		this.lonCenter = lonCenter;
	}

	public void setZoom(int zoom) {
		this.zoom = zoom;
		setScaleFactor();
	}
}
