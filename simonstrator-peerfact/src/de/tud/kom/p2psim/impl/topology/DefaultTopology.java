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

package de.tud.kom.p2psim.impl.topology;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.topology.Topology;
import de.tud.kom.p2psim.api.topology.TopologyComponent;
import de.tud.kom.p2psim.api.topology.TopologyListener;
import de.tud.kom.p2psim.api.topology.obstacles.ObstacleModel;
import de.tud.kom.p2psim.api.topology.social.SocialView;
import de.tud.kom.p2psim.api.topology.views.TopologyView;
import de.tud.kom.p2psim.api.topology.waypoints.WaypointModel;

/**
 * Very basic Topology
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 29.02.2012
 */
public class DefaultTopology implements Topology {

	private List<TopologyComponent> components;

	// private List<Obstacle> obstacles;

	private WaypointModel waypointModel;

	private ObstacleModel obstacleModel;

	private List<TopologyListener> topoListeners;

	private List<TopologyView> topoViews;

	private List<SocialView> socialViews;

	private PositionVector worldDimensions;

	private boolean initializedSocial = false;

	public DefaultTopology(PositionVector worldDimensions) {
		this.worldDimensions = worldDimensions;
		components = new LinkedList<TopologyComponent>();
		// obstacles = new LinkedList<Obstacle>();
		topoListeners = new LinkedList<TopologyListener>();
		topoViews = new ArrayList<TopologyView>();
		socialViews = new ArrayList<SocialView>();
	}

	/*
	 * @Override public void addObstacle(Obstacle obstacle) {
	 * obstacles.add(obstacle); for (TopologyListener listener : topoListeners)
	 * { listener.addedObstacle(obstacle); } }
	 */

	@Override
	public void addComponent(TopologyComponent comp) {
		components.add(comp);
		for (TopologyListener listener : topoListeners) {
			listener.addedComponent(comp);
		}
	}

	@Override
	public void setWaypointModel(WaypointModel model) {
		waypointModel = model;
		for (TopologyListener listener : topoListeners) {
			listener.changedWaypointModel(model);
		}
	}

	@Override
	public void setObstacleModel(ObstacleModel model) {
		obstacleModel = model;
		for (TopologyListener listener : topoListeners) {
			listener.changedObstacleModel(model);
		}
	}

	@Override
	public void addTopologyListener(TopologyListener listener) {
		if (!topoListeners.contains(listener)) {
			topoListeners.add(listener);
			for (TopologyComponent comp : components) {
				listener.addedComponent(comp);
			}
			/*
			 * for (Obstacle obstacle : obstacles) {
			 * listener.addedObstacle(obstacle); }
			 */

			listener.changedWaypointModel(waypointModel);
			listener.changedObstacleModel(obstacleModel);
		}
	}

	@Override
	public void removeTopologyListener(TopologyListener listener) {
		topoListeners.remove(listener);
	}

	@Override
	public void addTopologyView(TopologyView tView) {
		topoViews.add(tView);
		addTopologyListener(tView);
	}

	@Override
	public TopologyView getTopologyView(PhyType phy) {
		for (TopologyView view : topoViews) {
			if (view.getPhyType() != null && view.getPhyType().equals(phy)) {
				return view;
			}
		}
		return null;
	}

	@Override
	public PositionVector getWorldDimensions() {
		return worldDimensions;
	}

	@Override
	public void addSocialView(SocialView sView) {
		socialViews.add(sView);
		addTopologyListener(sView);

	}

	@Override
	public SocialView getSocialView(String id) {
		for (SocialView view : socialViews) {
			if (view != null && view.getIdentifier().equals(id)) {
				return view;
			}
		}
		return null;
	}

	@Override
	public void initializeSocial() {
		if (!initializedSocial) {
			for (SocialView view : socialViews) {
				view.initialize();
//				SocialViewComponentVis vis = new SocialViewComponentVis(view);
//				VisualizationInjector.injectComponent(vis);
			}
			initializedSocial = true;
		}
	}

	@Override
	public WaypointModel getWaypointModel() {
		return waypointModel;
	}

	@Override
	public ObstacleModel getObstacleModel() {
		return obstacleModel;
	}
}
