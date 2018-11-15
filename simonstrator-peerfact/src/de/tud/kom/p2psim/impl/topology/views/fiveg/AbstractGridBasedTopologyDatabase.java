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

package de.tud.kom.p2psim.impl.topology.views.fiveg;

import java.util.LinkedHashMap;
import java.util.Map;

import de.tud.kom.p2psim.impl.topology.views.VisualizationTopologyView.VisualizationInjector;
import de.tud.kom.p2psim.impl.topology.views.visualization.world.FiveGVisualization;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;

/**
 * Abstract base class working on grids of configurable size.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, Nov 5, 2015
 */
public abstract class AbstractGridBasedTopologyDatabase
		implements FiveGTopologyDatabase {

	private int gridSize;

	private boolean supportCloudlets;

	private final Map<Integer, FiveGTopologyDatabase.Entry> cloudEntries;

	private final Map<Integer, FiveGTopologyDatabase.Entry> cloudletEntries;

	public static int _visId = 1;

	/**
	 * Grid-based {@link CellularTopologyDatabase} with a grid length (squares)
	 * of gridSize in meters.
	 * 
	 * @param gridSize
	 */
	public AbstractGridBasedTopologyDatabase(int gridSize,
			boolean supportCloudlets) {
		this.gridSize = gridSize;
		this.cloudEntries = new LinkedHashMap<>();
		this.cloudletEntries = new LinkedHashMap<>();
	}

	public void setEnableVis(boolean enableVis) {
		if (enableVis == true) {
			Event.scheduleImmediately(new EventHandler() {

				@Override
				public void eventOccurred(Object content, int type) {
					VisualizationInjector.injectComponent("5G " + _visId++, -1,
							new FiveGVisualization(
									AbstractGridBasedTopologyDatabase.this),
							false, true);
				}
			}, null, 0);
		}
	}

	public void setGridSize(int gridSize) {
		this.gridSize = gridSize;
	}

	public void setSupportCloudlets(boolean supportCloudlets) {
		this.supportCloudlets = supportCloudlets;
	}

	public int getGridSize() {
		return gridSize;
	}

	@Override
	public int getSegmentID(double xd, double yd) {
		int x = (int) Math.floor(xd / gridSize);
		int y = (int) Math.floor(yd / gridSize);
		return (((x + y + 1) * (x + y + 2)) / 2 - y);
	}

	/**
	 * Create a new {@link Entry} object for the given segmentID. This is called
	 * at most TWICE for every ID as soon as the ID is required by a node inside
	 * the given area (once for CLOUD, and once for CLOUDLET if supported).
	 * 
	 * @param segmentID
	 * @param isCloudlet
	 *            entry for cloudlets
	 * @return an {@link Entry} object. For static links, consider using
	 *         {@link StaticEntry} instances provided by
	 *         {@link AbstractCellularTopologyDatabase}
	 */
	protected abstract Entry createEntryFor(int segmentID, boolean isCloudlet);

	@Override
	public Entry getEntryFor(int segmentID, boolean isCloudlet) {
		if (supportCloudlets && isCloudlet) {
			Entry entry = cloudletEntries.get(Integer.valueOf(segmentID));
			if (entry == null) {
				entry = createEntryFor(segmentID, true);
				cloudletEntries.put(Integer.valueOf(segmentID), entry);
			}
			return entry;
		} else {
			Entry entry = cloudEntries.get(Integer.valueOf(segmentID));
			if (entry == null) {
				entry = createEntryFor(segmentID, false);
				cloudEntries.put(Integer.valueOf(segmentID), entry);
			}
			return entry;
		}
	}

	/**
	 * Very basic entry implementation
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, Nov 5, 2015
	 */
	public class StaticEntry implements FiveGTopologyDatabase.Entry {

		private final int segment;

		private final double dropUp, dropDown;

		private final long latencyUp, latencyDown, bandwidthUp, bandwidthDown;


		/**
		 * Segment offering both, cell and access point with different
		 * characteristics.
		 * 
		 * @param segment
		 * @param dropUp
		 * @param dropDown
		 * @param latencyUp
		 * @param latencyDown
		 * @param bandwidthUp
		 * @param bandwidthDown
		 */
		public StaticEntry(int segment, double dropUp, double dropDown,
				long latencyUp, long latencyDown, long bandwidthUp,
				long bandwidthDown) {
			this.segment = segment;
			this.dropUp = dropUp;
			this.dropDown = dropDown;
			this.latencyUp = latencyUp;
			this.latencyDown = latencyDown;
			this.bandwidthUp = bandwidthUp;
			this.bandwidthDown = bandwidthDown;
		}

		@Override
		public int getSegmentID() {
			return segment;
		}

		@Override
		public double getDropProbability(boolean isUpload) {
			return isUpload ? dropUp : dropDown;
		}

		@Override
		public long getLatency(boolean isUpload) {
			return isUpload ? latencyUp : latencyDown;
		}

		@Override
		public long getBandwidth(boolean isUpload) {
			return isUpload ? bandwidthUp : bandwidthDown;
		}

	}

}
