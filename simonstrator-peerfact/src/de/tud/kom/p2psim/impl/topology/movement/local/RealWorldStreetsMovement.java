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

package de.tud.kom.p2psim.impl.topology.movement.local;

import java.util.HashMap;
import java.util.Locale;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.GHPoint3D;

import de.tud.kom.p2psim.api.topology.Topology;
import de.tud.kom.p2psim.api.topology.movement.SimLocationActuator;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.movement.modularosm.GPSCalculation;
import de.tud.kom.p2psim.impl.util.Either;
import de.tud.kom.p2psim.impl.util.Left;
import de.tudarmstadt.maki.simonstrator.api.Binder;

/**
 * This movement strategy uses the data from osm and navigates the nodes throught streets to the destination
 * 
 * @author Martin Hellwig
 * @version 1.0, 07.07.2015
 */
public class RealWorldStreetsMovement extends AbstractLocalMovementStrategy {
	
	private PositionVector worldDimensions;
	private GraphHopper hopper;
	private boolean init = false;
	
	private static HashMap<SimLocationActuator, RealWorldMovementPoints> movementPoints = new HashMap<>();
	
	private String osmFileLocation; //use pbf-format, because osm-format causes problems (xml-problems)
	private String graphFolderFiles;
	private String movementType; //car, bike or foot
	private String navigationalType; //fastest, 
	private double latLeft; //Values from -90 to 90; always smaller than latRight
	private double latRight; //Values from -90 to 90
	private double lonLeft; //Values from -180 to 180; Always smaller than lonRight
	private double lonRight; //Values from -180 to 180
	
	/**
	 * Tolerance in meters (if the node reached a waypoint up to "tolerance"
	 * meters, it will select the next waypoint in the path.
	 */
	private double tolerance = 1;

	public RealWorldStreetsMovement() {
		this.worldDimensions = Binder.getComponentOrNull(Topology.class)
				.getWorldDimensions();
		latLeft = GPSCalculation.getLatLower();
		latRight = GPSCalculation.getLatUpper();
		lonLeft = GPSCalculation.getLonLeft();
		lonRight = GPSCalculation.getLonRight();
	}
	
	private void init() {
		hopper = new GraphHopper().forServer();
		hopper.setOSMFile(osmFileLocation);
		// where to store graphhopper files?
		hopper.setGraphHopperLocation(graphFolderFiles);
		hopper.setEncodingManager(new EncodingManager(movementType));
		hopper.importOrLoad();
		init = true;
	}

	public Either<PositionVector, Boolean> nextPosition(SimLocationActuator comp,
			PositionVector destination) {
		if(!init) init();
		PositionVector newPosition = null;
		
		if (destination
				.distanceTo(comp.getRealPosition()) > getMovementSpeed(comp)) {
			//if not set already for this node or new destination is different than last one
			PointList pointList;
			if(!movementPoints.containsKey(comp) || destination.distanceTo(movementPoints.get(comp).getDestination()) > 1.0) {
				double[] startPosition = transformOwnWorldWindowToGPS(comp.getRealPosition().getX(), comp.getRealPosition().getY());
				double[] destinationPosition = transformOwnWorldWindowToGPS(destination.getX(), destination.getY());
				GHRequest req = new GHRequest(startPosition[0], startPosition[1], destinationPosition[0], destinationPosition[1]).
					    setWeighting(navigationalType).
					    setVehicle(movementType).
					    setLocale(Locale.GERMANY);
				GHResponse rsp = hopper.route(req);
				//If the requested point is not in the map data, simple return the destination as next point
				if(rsp.hasErrors()) {
					System.err.println("Requested Points (" + startPosition[0] + ", " + startPosition[1]
										+ ") or (" + destinationPosition[0] + ", " + destinationPosition[1] + ") are out of the bounding box.");
					
					pointList = new PointList();
					pointList.add(new GHPoint(destination.getLatitude(), destination.getLongitude()));
					movementPoints.put(comp, new RealWorldMovementPoints(comp.getRealPosition(), destination, pointList, 0));
				}
				else {
					pointList = rsp.getPoints();
					movementPoints.put(comp, new RealWorldMovementPoints(comp.getRealPosition(), destination, pointList, 0));
				}
			}
			else {
				pointList = movementPoints.get(comp).getPointList();
			}
			
			int actualIndex = movementPoints.get(comp).getActualIndex();
			int i = 0;
			for(GHPoint3D temp : pointList) {
				if(i==actualIndex) {
					PositionVector nextPoint = transformGPSWindowToOwnWorld(temp.getLat(), temp.getLon());
					newPosition = comp.getRealPosition().moveStep(nextPoint, getMovementSpeed(comp));
					
					if (nextPoint
							.distanceTo(comp.getRealPosition()) < tolerance) {
						actualIndex++;
					}
				}
				i++;
			}
			
			movementPoints.put(comp, new RealWorldMovementPoints(movementPoints.get(comp).getStart(), destination, pointList, actualIndex));
		}
		return new Left<PositionVector, Boolean>(newPosition);
	}
	
	/**
	 * Projects the world coordinates in the given gps window to the gps-coordinates
	 * @param x
	 * @param y
	 * @return The projected position in gps-coordinates (lat, long)
	 */
	private double[] transformOwnWorldWindowToGPS(double x, double y) {
		double[] gps_coordinates = new double[2];
		gps_coordinates[0] = latLeft + (latRight - latLeft) * (worldDimensions.getY() - y)/worldDimensions.getY();
		gps_coordinates[1] = lonLeft + (lonRight - lonLeft) * x/worldDimensions.getX();
		return gps_coordinates;
	}
	
	/**
	 * Projects the gps coordinates in the given gps window to the world-coordinates given in world-dimensions
	 * @param lat
	 * @param lon
	 * @return The projected position in world-dimensions
	 */
	private PositionVector transformGPSWindowToOwnWorld(double lat, double lon) {
		double x = worldDimensions.getX() * (lon - lonLeft)/(lonRight - lonLeft);
		double y = worldDimensions.getY() - worldDimensions.getY() * (lat - latLeft)/(latRight - latLeft);
		x = Math.max(0, x);
		x = Math.min(worldDimensions.getX(), x);
		y = Math.max(0, y);
		y = Math.min(worldDimensions.getY(), y);
		return new PositionVector(x, y);
	}
	
	public void setOsmFileLocation(String osmFileLocation) {
		this.osmFileLocation = osmFileLocation;
	}

	public void setGraphFolderFiles(String graphFolderFiles) {
		this.graphFolderFiles = graphFolderFiles;
	}

	public void setMovementType(String movementType) {
		this.movementType = movementType;
	}

	public void setNavigationalType(String navigationalType) {
		this.navigationalType = navigationalType;
	}
	
	public void setWaypointTolerance(double tolerance) {
		this.tolerance = tolerance;
	}
}
