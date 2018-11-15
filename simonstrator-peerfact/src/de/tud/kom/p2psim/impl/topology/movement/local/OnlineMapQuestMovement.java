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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
 * This movement strategy uses the data from MapQuest
 * 
 * @author Martin Hellwig
 * @version 1.0, 24.07.2015
 */
public class OnlineMapQuestMovement extends AbstractLocalMovementStrategy {
	
	private PositionVector worldDimensions;
	
	private static HashMap<Integer, RealWorldMovementPoints> movementPoints = new HashMap<>();
	
	private String movementType; // {Fastest, Shortest, Pedestrian, Bicycle}
	private String mapQuestKey;
	private double latLeft; //Values from -90 to 90; always smaller than latRight
	private double latRight; //Values from -90 to 90
	private double lonLeft; //Values from -180 to 180; Always smaller than lonRight
	private double lonRight; //Values from -180 to 180
	private double maxDistanceNextPoint; //This defines the maximum distance to the next point in line; 
										//if the distance is smaller than the given one, the node will choose the next point in the list
	
	public OnlineMapQuestMovement() {
		this.worldDimensions = Binder.getComponentOrNull(Topology.class)
				.getWorldDimensions();
		latLeft = GPSCalculation.getLatLower();
		latRight = GPSCalculation.getLatUpper();
		lonLeft = GPSCalculation.getLonLeft();
		lonRight = GPSCalculation.getLonRight();
	}

	public Either<PositionVector, Boolean> nextPosition(SimLocationActuator comp,
			PositionVector destination) {
		PositionVector newPosition = null;
		
		if (destination
				.distanceTo(comp.getRealPosition()) < getMovementSpeed(comp)) {
			newPosition = destination.clone();
		} else {
			//if not set already for this node or new destination is different than last one
			PointList pointList;
			if(!movementPoints.containsKey(comp.hashCode()) || destination.distanceTo(movementPoints.get(comp.hashCode()).getDestination()) > 1.0) {
				double[] startPosition = transformOwnWorldWindowToGPS(comp.getRealPosition().getX(), comp.getRealPosition().getY());
				double[] destinationPosition = transformOwnWorldWindowToGPS(destination.getX(), destination.getY());
				
				//Get Json data from page
				String directionsString = "";
				JSONArray allDirections = null;
				InputStream in;
				try {
					String url = "http://www.mapquestapi.com/directions/v2/route?key=" + mapQuestKey + "&outFormat=json&routeType=" + movementType + 
							"&shapeFormat=raw&locale=de_DE&unit=k&from=" + startPosition[0] + "," + startPosition[1] + "&to=" + 
							destinationPosition[0] + "," + destinationPosition[1];
					in = new URL(url).openStream();
					directionsString = IOUtils.toString(in);
					JSONObject directionsData = new JSONObject(directionsString);
					allDirections = directionsData.getJSONObject("route").getJSONArray("legs").getJSONObject(0).getJSONArray("maneuvers");
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//read in all waypoints
				pointList = new PointList();
				if(allDirections != null) {
					for(int i = 0; i < allDirections.length(); i++) {
						try {
							double lat = allDirections.getJSONObject(i).getJSONObject("startPoint").getDouble("lat");
							double lon = allDirections.getJSONObject(i).getJSONObject("startPoint").getDouble("lng");
							pointList.add(new GHPoint(lat, lon));
						}
						catch (JSONException e) {
							//This bar had no name defined, so there was an error. Not so bad
						}
					}
				}
				movementPoints.put(comp.hashCode(), new RealWorldMovementPoints(comp.getRealPosition(), destination, pointList, 0));
			}
			else {
				pointList = movementPoints.get(comp.hashCode()).getPointList();
			}
			
			int actualIndex = movementPoints.get(comp.hashCode()).getActualIndex();
			int i = 0;
			for(GHPoint3D temp : pointList) {
				if(i==actualIndex) {
					PositionVector nextPoint = transformGPSWindowToOwnWorld(temp.getLat(), temp.getLon());
					newPosition = comp.getRealPosition().moveStep(nextPoint, getMovementSpeed(comp));
					
					if(nextPoint.distanceTo(comp.getRealPosition()) < maxDistanceNextPoint) actualIndex++;
				}
				i++;
			}
			
			movementPoints.put(comp.hashCode(), new RealWorldMovementPoints(movementPoints.get(comp.hashCode()).getStart(), destination, pointList, actualIndex));
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
		return new PositionVector(x, y);
	}
	
	public void setMovementType(String movementType) {
		this.movementType = movementType;
	}
	
	public void setMaxDistanceNextPoint(double maxDistanceNextPoint) {
		this.maxDistanceNextPoint = maxDistanceNextPoint;
	}
	
	public void setMapQuestKey(String mapQuestKey) {
		this.mapQuestKey = mapQuestKey;
	}
}
