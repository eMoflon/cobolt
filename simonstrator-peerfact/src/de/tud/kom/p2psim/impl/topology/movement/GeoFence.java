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

package de.tud.kom.p2psim.impl.topology.movement;

import de.tud.kom.p2psim.api.topology.Topology;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tudarmstadt.maki.simonstrator.api.Binder;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

public class GeoFence implements Comparable<GeoFence> {

	private final long timestamp;

	private final double sizeX;

	private final double sizeY;

	private Location center;

	@XMLConfigurableConstructor({ "t", "sizeX", "sizeY" })
	public GeoFence(final long timestamp, final double sizeX,
			final double sizeY) {
		this.timestamp = timestamp;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
	}

	public boolean contains(final PositionVector pos) {
		final double x = pos.getEntry(0);
		final double y = pos.getEntry(1);
		final boolean isXInsideBorders = getXMin() <= x && x <= getXMax();
		final boolean isYInsideBorders = getYMin() <= y && y <= getYMax();
		return isXInsideBorders && isYInsideBorders;
	}

	public Location getCenter() {
		final Topology topologyComponent = Binder
				.getComponentOrNull(Topology.class);

		if (topologyComponent != null) {
			final PositionVector worldDimensions = topologyComponent
					.getWorldDimensions();
			this.center = new PositionVector(worldDimensions.getEntry(0) / 2,
					worldDimensions.getEntry(1) / 2);
		}

		return this.center;
	}

	public double getXMin() {
		return this.getCenter().getLongitude() - this.sizeX / 2;
	}

	public double getXMax() {
		return this.getCenter().getLongitude() + this.sizeX / 2;
	}

	public double getYMin() {
		return this.getCenter().getLatitude() - this.sizeY / 2;
	}

	public double getYMax() {
		return this.getCenter().getLatitude() + this.sizeY / 2;
	}

	public double getSizeX() {
		return this.sizeX;
	}

	public double getSizeY() {
		return sizeY;
	}

	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public String toString() {
		return "GeoFence [timestamp=" + Time.getFormattedTime(getTimestamp())
				+ ", sizeX=" + sizeX + ", sizeY=" + sizeY + ", center="
				+ getCenter() + "]";
	}

	@Override
	public int compareTo(final GeoFence o) {
		if (o == null)
			throw new IllegalArgumentException();

		return Long.compare(this.getTimestamp(), o.getTimestamp());
	}

}
