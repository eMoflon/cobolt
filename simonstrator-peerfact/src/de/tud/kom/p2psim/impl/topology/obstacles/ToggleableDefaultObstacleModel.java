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

package de.tud.kom.p2psim.impl.topology.obstacles;

import java.util.List;

import com.google.common.collect.Lists;

import de.tud.kom.p2psim.api.topology.TopologyComponent;
import de.tud.kom.p2psim.api.topology.obstacles.Obstacle;
import de.tud.kom.p2psim.api.topology.obstacles.ObstacleModel;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponentFactory;

/**
 * This obstacle model can load a map and its provided obstacles for the
 * obstacle placement. Additionally it is able to toggle the obstacles
 * on and off.
 * 
 * @author Fabio Zöllner
 * @version 1.0, 02.04.2012
 */
public class ToggleableDefaultObstacleModel extends AbstractObstacleModel {

    private boolean isActive = true;

	@Override
	public void generateObstacles() {
		//
	}

    protected void notifyAddedObstacle(Obstacle obstacle) {
        if (!isActive) return;
        super.notifyAddedObstacle(obstacle);
    }

    @Override
    public List<Obstacle> getObstacles() {
        if (!isActive) return Lists.newArrayList();
        return super.getObstacles();
    }

    public void setActive(boolean isActive) {
        super.notifyAddedObstacle(null); // Dirty, dirty hack that ensures that ObstacleComponentVis redraws everything
        this.isActive = isActive;
    }

    @Override
    public Obstacle getEnclosing(PositionVector loc) {
        if (!isActive) return null;
        return super.getEnclosing(loc);
    }


    public static class ObstacleToggleFactory implements HostComponentFactory {

        @Override
        public HostComponent createComponent(Host host) {
            return new ObstacleToggle(host);
        }
    }

    public static class ObstacleToggle implements HostComponent {
        private Host host;

        public ObstacleToggle(Host host) {
            this.host = host;
        }

        @Override
        public void initialize() {
			// unused
		}

		@Override
		public void shutdown() {
			// unused
        }

        public void activate() {
            setActive(true);
        }

        public void deactivate() {
            setActive(false);
        }

        public void setActive(boolean isActive) {
        	ObstacleModel obstacleModel;
			try {
				obstacleModel = host.getComponent(TopologyComponent.class).getTopology().getObstacleModel();
				if (obstacleModel instanceof ToggleableDefaultObstacleModel) {
	                ((ToggleableDefaultObstacleModel)obstacleModel).setActive(isActive);
	            }
			} catch (ComponentNotAvailableException e) {
				e.printStackTrace();
			}
            
        }

//        @Override
//        public void setHost(Host host) {
//            this.host = host;
//        }

        @Override
        public Host getHost() {
            return host;
        }
    }
}