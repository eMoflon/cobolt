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

import java.util.Map;

import com.google.common.collect.Maps;

import de.tud.kom.p2psim.api.topology.movement.SimLocationActuator;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

public class DebugMovementModel extends AbstractWaypointMovementModel {
	private Map<SimLocationActuator, PositionVector> lastDestinations = Maps.newHashMap();
	
	private SimLocationActuator monitoredComp = null;
	private long lastMonitoredPositionRequest = -1;
	
	@XMLConfigurableConstructor({"worldX", "worldY"})
	public DebugMovementModel(double worldX, double worldY) {
		super(worldX, worldY);
	
	}
	
	@Override
	public void nextPosition(SimLocationActuator component) {
		PositionVector destination;
		long pauseTime = 0;
		long monitoredTime = 0;
		
		PositionVector metricDimensions = waypointModel.getMetricDimensions();
		
		PositionVector mapPixelRatio = metricDimensions.clone();
		mapPixelRatio.divide(this.worldDimensions);	

		PositionVector speed = new PositionVector(getSpeedLimit(), getSpeedLimit());
		speed.divide(mapPixelRatio);
		
		
		double width = getWorldDimension(0);
		double height = getWorldDimension(1);
		
		if (monitoredComp == null) {
			monitoredComp = component;
		}
		
		if (component == monitoredComp) {
			if (lastMonitoredPositionRequest != -1) {
				monitoredTime = Time.getCurrentTime() - lastMonitoredPositionRequest;
				monitoredTime = monitoredTime / Time.SECOND;
			}
			
			lastMonitoredPositionRequest = Time.getCurrentTime();
		}
		
		PositionVector lastDestination = lastDestinations.get(component);
		
		if (lastDestination != null) {
			double lastX = lastDestination.getX();
			double lastY = lastDestination.getY();
			
			
			if (lastX == 0 && lastY == 0) {
				if (component == monitoredComp) {
					System.err.println("Vertical time is " + monitoredTime + " with a speed of " + speed);
				}
				//System.err.println("Destination: North/West");
				destination = new PositionVector(width, 0);
			} else if (lastX != 0 && lastY != 0) {
				if (component == monitoredComp) {
					System.err.println("Vertical time is " + monitoredTime + " with a speed of " + speed);
				}
				//System.err.println("Destination: North/East");
				destination = new PositionVector(0, height); //  * VisualizationInjector.getVisualScaleFactor()
			} else if (lastX != 0 && lastY == 0) {
				if (component == monitoredComp) {
					System.err.println("Horizontal time is " + monitoredTime + " with a speed of " + speed);
				}
				//System.err.println("Destination: South/East");
				destination = new PositionVector(width, height);
			} else { //if (lastX == 0 && lastY != 0) {
				if (component == monitoredComp) {
					System.err.println("Horizontal time is " + monitoredTime + " with a speed of " + speed);
				}
				//System.err.println("Destination: South/West");
				destination = new PositionVector(0, 0);
			}
		} else {
			//System.err.println("Destination: North/West");
			destination = new PositionVector(0, 0);
		}
		
		lastDestinations.put(component, destination);
		nextDestination(component, destination, pauseTime);
	}
}
