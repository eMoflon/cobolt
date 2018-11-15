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

package de.tud.kom.p2psim.impl.topology.movement.modular.attraction;

import java.util.Random;

import de.tud.kom.p2psim.api.topology.movement.SimLocationActuator;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.LocationActuator;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.LocationListener;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.LocationRequest;

/**
 * This is the implementation of a AttractionPoint, which implements the
 * {@link LocationActuator} interface. So a {@link AttractionPoint} has the
 * ability to be moved.
 * 
 * @author Christoph Muenker
 * @version 1.0, 02.07.2013
 */
public class AttractionPoint implements SimLocationActuator {
	protected static Random rnd = Randoms.getRandom(AttractionPoint.class);

	private PositionVector posVec;

	private double minSpeed;

	private double maxSpeed;

	private double currentSpeed = -1;

	public AttractionPoint(PositionVector posVec, double minSpeed,
			double maxSpeed) {
		this.posVec = posVec;

		this.minSpeed = minSpeed;
		this.maxSpeed = maxSpeed;
	}

	@Override
	public double getMinMovementSpeed() {
		return minSpeed;
	}

	@Override
	public double getMaxMovementSpeed() {
		return maxSpeed;
	}

	@Override
	public void setMovementSpeed(double speed) {
		this.currentSpeed = speed;
	}

	@Override
	public double getMovementSpeed() {
		if (currentSpeed == -1) {
			double min_speed = getMinMovementSpeed();
			double max_speed = getMaxMovementSpeed();

			double value = rnd.nextDouble();
			this.currentSpeed = (value * (max_speed - min_speed)) + min_speed;
		}
		return currentSpeed;
	}

	@Override
	public Location getLastLocation() {
		return posVec;
	}

	@Override
	public void requestLocationUpdates(LocationRequest request,
			LocationListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeLocationUpdates(LocationListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public LocationRequest getLocationRequest() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void initialize() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void shutdown() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Host getHost() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateCurrentLocation(double longitude, double latitude) {
		posVec.setEntries(longitude, latitude);
	}

	@Override
	public void setNewTargetLocation(double longitude, double latitude)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public PositionVector getRealPosition() {
		return posVec;
	}

}
