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

package de.tud.kom.p2psim.impl.topology.placement;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import de.tud.kom.p2psim.api.topology.TopologyComponent;
import de.tud.kom.p2psim.api.topology.placement.PlacementModel;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tudarmstadt.maki.simonstrator.api.Randoms;

/**
 * A configurable grid where occupied positions in the grid are configured via a
 * simple matrix. Scales with the world-size.
 * 
 * Format of the matrix: lines with 0,1,0,0,0,1\n0,1,...
 * 
 * where 1 means: position a node and 0 means: empty cell.
 * 
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, Sep 19, 2013
 */
public class ConfigurableGridPositionDistribution implements PlacementModel {

	private int numberOfComponents = 0;

	private boolean randInCell = true;

	private final String SEP = ";";

	private final String OCCUPIED = "1";

	private final String EMPTY = "0";

	private int placedComponents = 0;

	private int cols = -1;

	private int rows = -1;

	private BitSet occupiedBits = new BitSet();

	private PositionVector worldDimensions = null;

	private Random random = Randoms
			.getRandom(ConfigurableGridPositionDistribution.class);

	private List<PositionVector> positions = new Vector<PositionVector>();

	private int offset = 0;

	@Override
	public void addComponent(TopologyComponent comp) {
		if (worldDimensions == null) {
			worldDimensions = comp.getTopology().getWorldDimensions();
		}
		numberOfComponents++;
		if (numberOfComponents == positions.size() + offset + 1) {
			System.err
					.println("WARNING: Your specified positioning-topology does not contain enough places for the number of components in your scenario!");
		}
	}

	@Override
	public PositionVector place(TopologyComponent comp) {
		if (positions.isEmpty()) {
			calcPositions2D();
		}
		PositionVector pos = positions.get(placedComponents + offset);
		placedComponents = (placedComponents + 1) % positions.size();
		return pos;
	}

	private void calcPositions2D() {
		assert rows > 0 && cols > 0;
		// float ratio = (float) (worldDimensions.getX() /
		// worldDimensions.getY());
		// float ratio_1 = 1 / ratio;
		// int anz_x = ((int) Math.sqrt(ratio * cols * rows)) + 1;
		int anz_x = cols;
		// int anz_y = ((int) Math.sqrt(ratio_1 * cols * rows)) + 1;
		int anz_y = rows;
		int dist_x = (int) worldDimensions.getX() / anz_x;
		int dist_y = (int) worldDimensions.getY() / anz_y;
		for (int y = 0; y < anz_y; y++) {
			for (int x = 0; x < anz_x; x++) {
				if (occupiedBits.get(y * cols + x)) {
					// valid
					double xcenter = x * dist_x + dist_x / 2;
					double ycenter = y * dist_y + dist_y / 2;
					if (randInCell) {
						xcenter += random.nextDouble() * dist_x / 4 - dist_x
								/ 8;
						ycenter += random.nextDouble() * dist_y / 4 - dist_y
								/ 8;
					}
					PositionVector vec = new PositionVector(xcenter, ycenter);
					positions.add(vec);
				}
			}
		}
	}

	/**
	 * Allow a random offset between the grid points and the actual node
	 * position.
	 * 
	 * @param randomOffset
	 *            default: true
	 */
	public void setRandomOffset(boolean randomOffset) {
		this.randInCell = randomOffset;
	}
	
	/**
	 * The offset to use when {@link #place(TopologyComponent)} is invoked
	 * @param offset
	 */
	public void setOffset(int offset) {
		this.offset  = offset;
		
	}

	/**
	 * The given file is parsed.
	 * 
	 * @param filename
	 */
	public void setPlacementFile(String file) {
		BufferedReader csv = null;
		try {
			csv = new BufferedReader(new FileReader(file));

			while (csv.ready()) {
				String line = csv.readLine();

				if (line.startsWith("#")) {
					continue;
				}

				if (line.indexOf(SEP) > -1) {
					String[] parts = line.split(SEP);
					if (cols == -1) {
						cols = parts.length;
					}
					assert cols == parts.length;

					if (rows == -1) {
						rows = 0;
					}
					
					int actCol = 0;
					for (String part : parts) {
						if (part.equals(OCCUPIED)) {
							occupiedBits.set(rows * cols + actCol);
						} else if (part.equals(EMPTY)) {
							// ok.
						} else {
							throw new AssertionError("Unknown entry " + part);
						}
						actCol++;
					}
					rows++;

				}
			}

		} catch (Exception e) {
			throw new AssertionError("Positioning-Input failed: " + e);
		} finally {
			if (csv != null) {
				try {
					csv.close();
				} catch (IOException e) {
					//
				}
			}
		}
	}

}
