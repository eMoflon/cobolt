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

import java.util.List;
import java.util.Random;
import java.util.Vector;

import de.tud.kom.p2psim.api.topology.TopologyComponent;
import de.tud.kom.p2psim.api.topology.placement.PlacementModel;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tudarmstadt.maki.simonstrator.api.Randoms;

/**
 * This Positioning tries to distribute the Nodes evenly on the field, making up
 * some kind of grid. Information on the total amount of to be positioned nodes
 * is therefore needed.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 05/08/2011
 */
public class GridPositionDistribution implements PlacementModel {

	private int numberOfComponents = 0;

	private boolean randInCell = true;

	private int placedComponents = 0;

	private PositionVector worldDimensions = null;
	
	/**
	 * Allows us to create a "larger" grid, even if we only have a smaller number of nodes.
	 * Eg., create a 3*3 grid for only 2 nodes
	 */
	private int fakeNumberOfComponents = -1;

	private Random random = Randoms
			.getRandom(GridPositionDistribution.class);

	private List<PositionVector> positions = new Vector<PositionVector>();

	@Override
	public void addComponent(TopologyComponent comp) {
		if (worldDimensions == null) {
			worldDimensions = comp.getTopology().getWorldDimensions();
		}
		numberOfComponents++;
	}

	@Override
	public PositionVector place(TopologyComponent comp) {
		if (positions.isEmpty()) {
			calcPositions2D();
		}
		PositionVector pos = positions.get(placedComponents);
		placedComponents = (placedComponents + 1) % numberOfComponents;
		return pos;
	}
	
	public void setRandomOffset(boolean randInCell) {
		this.randInCell = randInCell;
	}
	
	public void setFakeNumberOfComponents(int fakeNumberOfComponents) {
		this.fakeNumberOfComponents = fakeNumberOfComponents;
	}

	private void calcPositions2D() {
		if (fakeNumberOfComponents != -1) {
			numberOfComponents = fakeNumberOfComponents;
		}
		float ratio = (float) (worldDimensions.getX() / worldDimensions.getY());
		float ratio_1 = 1 / ratio;
		int anz_x = (int) Math.ceil(Math.sqrt(ratio * numberOfComponents));
		int anz_y = (int) Math.ceil( Math.sqrt(ratio_1 * numberOfComponents));
		int dist_x = (int) worldDimensions.getX() / anz_x;
		int dist_y = (int) worldDimensions.getY() / anz_y;
		for (int x = 0; x < anz_x; x++) {
			for (int y = 0; y < anz_y; y++) {
				double xcenter = x * dist_x + dist_x / 2;
				double ycenter = y * dist_y + dist_y / 2;
				if (randInCell) {
					xcenter += random.nextDouble() * dist_x / 2
							- dist_x / 4;
					ycenter += random.nextDouble() * dist_y / 2
							- dist_y / 4;
				}
				PositionVector vec = new PositionVector(xcenter, ycenter);
				positions.add(vec);
			}
		}
	}

}
