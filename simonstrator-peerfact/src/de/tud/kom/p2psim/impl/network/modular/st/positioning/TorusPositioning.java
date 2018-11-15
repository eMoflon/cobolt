/*
 * Copyright (c) 2005-2011 KOM - Multimedia Communications Lab
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


package de.tud.kom.p2psim.impl.network.modular.st.positioning;

import java.util.Random;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;
import de.tud.kom.p2psim.impl.network.modular.st.PositioningStrategy;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;

/**
 * Applies a uniformly-distributed random position on a 2-or multi-dimensional
 * torus surface. Parameters: torusDimensionSize(double), noOfDimensions(int)
 * 
 * @author Leo Nobach
 * 
 */
public class TorusPositioning implements PositioningStrategy {

	private int noOfDimensions = 2;

	double torusDimensionSize = 1d;

	double halfTorusDimensionSize = 0.5d;

	Random rand = Randoms.getRandom(TorusPosition.class);

	@Override
	public Location getPosition(SimHost host, NetMeasurementDB db,
			NetMeasurementDB.Host hostMeta) {

		double[] rawPos = getRawTorusPositionFor(host);
		assert rawPos.length == noOfDimensions : "The raw torus position returned by getRawTorusPositionFor(Host) does not return an array of length "
				+ noOfDimensions
				+ ", instead it returns one with the length "
				+ rawPos.length;

		return new TorusPosition(rawPos);

	}

	/**
	 * May be overridden for more detailled torus positioning.
	 * 
	 * @param host
	 * @return
	 */
	protected double[] getRawTorusPositionFor(SimHost host) {
		double[] result = new double[noOfDimensions];
		for (int i = 0; i < noOfDimensions; i++)
			result[i] = rand.nextDouble() * halfTorusDimensionSize * 2;
		return result;
	}

	/**
	 * To be set by the configurator.
	 * 
	 * @param noOfDimensions
	 */
	public void setNoOfDimensions(int noOfDimensions) {
		this.noOfDimensions = 2;
	}

	/**
	 * To be set by the configurator.
	 * 
	 * @param torusDimensionSize
	 */
	public void setTorusDimensionSize(double torusDimensionSize) {
		this.torusDimensionSize = torusDimensionSize;
		this.halfTorusDimensionSize = torusDimensionSize * 0.5d;
	}

	public class TorusPosition extends PositionVector {


		TorusPosition(double[] rawPos) {
			super(rawPos);
		}

		@Override
		public double distanceTo(Location netPosition) {
			if (!(netPosition instanceof PositionVector))
				throw new AssertionError(
						"Can not calculate distances between different position classes: "
								+ this.getClass() + " and "
								+ netPosition.getClass());
			TorusPosition other = (TorusPosition) netPosition;

			double accuDistanceSq = 0;

			for (int i = 0; i < getDimensions(); i++) {
				double distDim = getEntry(i) - other.getEntry(i);
				if (distDim > halfTorusDimensionSize) {
					// we wrap the torus
					distDim = torusDimensionSize - distDim;
				} else if (distDim < -halfTorusDimensionSize) {
					// we wrap the torus, other way
					distDim = -torusDimensionSize + distDim;
				}
				accuDistanceSq += distDim * distDim;
			}

			return Math.sqrt(accuDistanceSq);

		}

		@Override
		public int getTransmissionSize() {
			return 8 * getDimensions(); // 2 * double
		}

		public double[] getRawPos() {
			return asDoubleArray();
		}

		@Override
		public TorusPosition clone() {
			return new TorusPosition(asDoubleArray());
		}

	}

	@Override
	public void writeBackToXML(BackWriter bw) {
		bw.writeSimpleType("torusDimensionSize", torusDimensionSize);
		bw.writeSimpleType("torusDimensionSize", torusDimensionSize);
	}

}
