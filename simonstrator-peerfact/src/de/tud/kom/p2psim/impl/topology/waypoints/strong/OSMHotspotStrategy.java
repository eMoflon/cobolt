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

package de.tud.kom.p2psim.impl.topology.waypoints.strong;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;

import org.apache.commons.math.random.JDKRandomGenerator;
import org.apache.commons.math.random.RandomGenerator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.api.topology.obstacles.Obstacle;
import de.tud.kom.p2psim.api.topology.waypoints.WaypointModel;
import de.tud.kom.p2psim.api.util.geo.maps.Node;
import de.tud.kom.p2psim.api.util.geo.maps.Way;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.views.VisualizationTopologyView.VisualizationInjector;
import de.tud.kom.p2psim.impl.topology.views.VisualizationTopologyView.VisualizationInjector.DisplayString;
import de.tud.kom.p2psim.impl.topology.waypoints.StrongWaypointSupport;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.StrongWaypoint;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.WeakWaypoint;
import de.tud.kom.p2psim.impl.util.Tuple;
import de.tud.kom.p2psim.impl.util.geo.maps.osm.OSMMap;
import de.tud.kom.p2psim.impl.util.geo.maps.osm.OSMNode;
import de.tud.kom.p2psim.impl.util.geo.maps.osm.OSMObstacle;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;

public class OSMHotspotStrategy extends StrongWaypointStrategy {

	protected ArrayList<HotspotDeplacmentInfo> hotspotMoveLerpPositions = new ArrayList<HotspotDeplacmentInfo>();

	private static String AMENITIES_FILENAME = "data/maps/valid_amenities.ordering";

	private RandomGenerator rnd;

	protected int placementRetries = 1000;

	// The number of strong waypoints that shall be added to the map
	protected int noOfWaypoints = 1000;

	// Holds the actually existing amenities in the map (given that they are
	// valid)
	ArrayList<String> existingAmenities;

	private WaypointModel model = null;

	/**
	 * A list of the most used amenities in open street map sorted by the
	 * likelihood of being hotspots where many people meet.
	 */
	private static ArrayList<String> validAmenities = new ArrayList<String>();

	public OSMHotspotStrategy() {
		// The fresh random generator based on the configured seed
		// ensures that the amount of waypoints and their positioning is
		// reproducible
		rnd = new JDKRandomGenerator();
		rnd.setSeed(Simulator.getSeed());

		validAmenities = readAmenitiesFromFile();
	}

	/**
	 * Reads the valid amenities from the file AMENETIES_FILENAME and returns
	 * them as a list of strings.
	 * 
	 * @return
	 */
	private ArrayList<String> readAmenitiesFromFile() {
		File ordering = new File(AMENITIES_FILENAME);

		if (!ordering.exists())
			throw new ConfigurationException(
					"Unable to read the amenities file (" + AMENITIES_FILENAME
							+ ")");

		ArrayList<String> amenities = new ArrayList<String>();

		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(ordering));

			String line = reader.readLine();
			while (line != null) {
				if (!line.equals("")) {
					amenities.add(line);
				}

				line = reader.readLine();
			}

			reader.close();
		} catch (FileNotFoundException e) {
			throw new ConfigurationException(
					"Unable to read the amenities file (" + AMENITIES_FILENAME
							+ ")", e);
		} catch (IOException e) {
			throw new ConfigurationException(
					"Unable to read the amenities file (" + AMENITIES_FILENAME
							+ ")", e);
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				// ...
			}
		}

		return amenities;

	}

	@Override
	public void generateStrongWaypoints(WaypointModel wpModel) {

		this.model = wpModel;
		
		if (!(wpModel instanceof StrongWaypointSupport))
			throw new ConfigurationException(
					"OSMHotspotStrategy requires a waypoint model with strong waypoint support to work correctly.");

		if (!(model.getMap() instanceof OSMMap))
			throw new ConfigurationException(
					"OSMHotspotStrategy requires a OSMMap to work correctly.");

		OSMMap map = (OSMMap) model.getMap();

		StrongWaypointSupport sws = (StrongWaypointSupport) wpModel;

		noOfWaypoints = sws.getNoOfWaypoints();

		// Find hotspots based on the validAmenities list
		final List<Hotspot> hotspots = findHotspots(map);

		// Filter the existing valid amenities
		existingAmenities = existingAmenity(hotspots);

		// Assign radii based on the priority of the amenities
		assignRadii(hotspots, map);

		// Move the hotspots if they are inside of an obstacle
		if (obstacleModel != null) {
			moveWaypointsBasedOnObstacles(hotspots);
		}

		// Add the number of specified way points distributed by the radius size
		addStrongWaypoints(hotspots);

		// Inject the JComponent for displaying the hotspots

		VisualizationInjector.injectComponent("Hotspots", 0, new ShowHotspots(
				hotspots, model), false);

		VisualizationInjector.addDisplayString(new DisplayString() {
			@Override
			public String getDisplayString() {
				int distinctAmenities = 0;
				if (existingAmenities != null)
					distinctAmenities = existingAmenities.size();

				return "OSM[Waypoints (strong/weak): "
						+ model.getNumberOfWaypoints(StrongWaypoint.class)
						+ "/" + model.getNumberOfWaypoints(WeakWaypoint.class)
						+ ", Hotspots: " + hotspots.size()
						+ ", Distinct amenities: " + distinctAmenities
						+ ", Highest amenity: " + hotspots.get(0).amenity + "]";
			}
		});

	}

	/**
	 * Uses the information provided by the obstacle model to move the hotspots
	 * outside of the obstacles should the be completely covered.
	 */
	private void moveWaypointsBasedOnObstacles(List<Hotspot> hotspots) {
		for (Hotspot hotspot : hotspots) {
			createHotspotGeometry(hotspot);
			Obstacle obstacle = isHotspotInsideObstacle(hotspot);

			if (obstacle == null)
				continue;

			hotspot.wasHidden = true;

			Tuple<PositionVector, PositionVector> closestEdges = findClosestEdgest(
					hotspot, obstacle);

			double t = rnd.nextDouble();

			PositionVector a = closestEdges.getA();
			PositionVector b = closestEdges.getB();

			PositionVector newPosition = a.clone();
			PositionVector p = b.minus(newPosition);
			p.multiplyScalar(t);
			newPosition.add(p);

			PositionVector direction = a.clone();
			p = b.minus(direction);
			p.multiplyScalar(0.5);
			direction.add(p);

			direction = direction.minus(hotspot.position);

			direction.normalize();
			direction.multiplyScalar(hotspot.radius / 2);

			newPosition.add(direction);

			HotspotDeplacmentInfo lerpPos = new HotspotDeplacmentInfo(
					closestEdges.getA(), closestEdges.getB(), newPosition,
					hotspot.position);

			hotspotMoveLerpPositions.add(lerpPos);

			hotspot.position = newPosition;
		}
	}

	private Tuple<PositionVector, PositionVector> findClosestEdgest(
			Hotspot hotspot, Obstacle obstacle) {
		Geometry geometry = obstacle.getGeometry();
		Coordinate[] coordinates = geometry.getCoordinates();
		PositionVector[] positionCache = new PositionVector[coordinates.length];

		double closestDistance = -1;
		int closestIdx = -1;

		for (int i = 0; i < coordinates.length; i++) {
			positionCache[i] = new PositionVector(coordinates[i].x,
					coordinates[i].y);
			double d = positionCache[i].distanceTo(hotspot.position);

			if (closestDistance > d || closestDistance == -1) {
				closestDistance = d;
				closestIdx = i;
			}
		}

		PositionVector before = positionCache[closestIdx - 1 == -1 ? coordinates.length - 1
				: closestIdx - 1];
		PositionVector after = positionCache[closestIdx + 1 == coordinates.length ? 0
				: closestIdx + 1];

		if (before.distanceTo(hotspot.position) < after
				.distanceTo(hotspot.position)) {
			return new Tuple<PositionVector, PositionVector>(
					positionCache[closestIdx].clone(), before);
		} else {
			return new Tuple<PositionVector, PositionVector>(
					positionCache[closestIdx].clone(), after);
		}
	}

	private Obstacle isHotspotInsideObstacle(Hotspot hotspot) {
		for (Obstacle obstacle : obstacleModel.getObstacles()) {
			if (obstacle.contains(hotspot.geometry)) {
				return obstacle;
			}
		}

		return null;
	}

	/**
	 * Uses the validAmenities list to find hotspots in the map and returns a
	 * list newly created Hotspots.
	 * 
	 * @param map
	 * @return
	 */
	private List<Hotspot> findHotspots(OSMMap map) {
		List<Hotspot> hotspots = new ArrayList<Hotspot>();

		Collection<OSMNode> nodes = map.getNodes();
		Collection<Way> ways = map.getWays();
		List<Obstacle> obstacles = map.getObstacles();

		// Node and Way are currently the only retained
		// information and also the most used

		// TODO: Add a MovementStrategy that doesn't use waypoints but avoids
		// obstacles
		// TODO: Add a MovementStrategy that uses the waypoints but doesn't move
		// in a straight line
		// TODO: Add an option to assign StrongWaypoints based on the density of
		// WeakWaypoints or convert some WeakWaypoints to StrongWaypoints

		for (Obstacle o : obstacles) {
			OSMObstacle obstacle = (OSMObstacle) o;
			if (obstacle.containsAttribute("amenity")) {
				if (getAmenityIndex(obstacle.getAttribute("amenity"),
						validAmenities) < 0)
					continue;

				Hotspot hotspot = new Hotspot();
				hotspot.position = obstacle.getVertices().get(0);
				hotspot.amenity = obstacle.getAttribute("amenity");
				hotspot.type = obstacle.getAttribute("name");

				hotspots.add(hotspot);
			}
		}

		/*
		 * for (Node n : nodes) { OSMNode node = (OSMNode)n; if
		 * (node.containsAttribute("amenity")) { if
		 * (getAmenityIndex(node.getAttribute("amenity"), validAmenities) < 0)
		 * continue;
		 * 
		 * Hotspot hotspot = new Hotspot(); hotspot.position =
		 * node.getWorldPosition(); hotspot.amenity =
		 * node.getAttribute("amenity"); hotspot.type = "node";
		 * 
		 * hotspots.add(hotspot); } }
		 * 
		 * for (Way w : ways) { OSMWay way = (OSMWay)w; if
		 * (way.containsAttribute("amenity")) { if
		 * (getAmenityIndex(way.getAttribute("amenity"), validAmenities) < 0)
		 * continue;
		 * 
		 * PositionVector center = getCenter(way.getNodes());
		 * 
		 * Hotspot hotspot = new Hotspot(); hotspot.position = center;
		 * hotspot.amenity = way.getAttribute("amenity"); hotspot.type = "way";
		 * 
		 * hotspots.add(hotspot); } }
		 */

		Collections.sort(hotspots, new HotspotComparator(validAmenities));

		return hotspots;
	}

	/**
	 * Filters the existing amenities based on the validAmenities list
	 * 
	 * @param hotspots
	 *            Hotspots with amenities
	 * @return List of existing valid amenities
	 */
	public ArrayList<String> existingAmenity(List<Hotspot> hotspots) {
		ArrayList<String> list = new ArrayList<String>();

		for (String vh : validAmenities) {
			for (Hotspot h : hotspots) {
				if (h.amenity.equals(vh)) {
					list.add(vh);
					break;
				}
			}
		}

		return list;
	}

	/**
	 * Returns the center of a list of nodes
	 * 
	 * @param points
	 * @return
	 */
	private PositionVector getCenter(Vector<Node> points) {
		PositionVector centroid = new PositionVector(0, 0);

		for (Node n : points) {
			centroid.add(n.getWorldPosition());
		}

		centroid.multiplyScalar(1 / (double) points.size());

		return centroid;
	}

	/**
	 * Calculates the maximum radius size based on the map size and assigns the
	 * radii based on the hotspots amenities priority.
	 * 
	 * The hotspot with the highest priority e.g. the first hotspot in the list
	 * receives the maximum radius.
	 * 
	 * Note: This method also creates the Geometry object for the hotspot
	 * 
	 * @param hotspots
	 *            Hotspots whos radius shall be calculated
	 * @param map
	 *            The current map
	 */
	private void assignRadii(List<Hotspot> hotspots, OSMMap map) {
		PositionVector dimensions = map.getDimensions();

		double llsqrt = Math.sqrt(dimensions.getX() + dimensions.getY());

		double defaultRadius = 0.630 * llsqrt; // 0.887

		int idx;
		for (Hotspot h : hotspots) {
			idx = getAmenityIndex(h.amenity, existingAmenities);
			h.radius = (defaultRadius / (idx + 1));
			h.geometry = createHotspotGeometry(h);
		}
	}

	private Geometry createHotspotGeometry(Hotspot h) {
		int points = 20;
		double slice = 2 * Math.PI / points;
		ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();

		for (int i = 0; i < points; i++) {
			double angle = slice * i;
			int newX = (int) (h.position.getX() + h.radius * Math.cos(angle));
			int newY = (int) (h.position.getY() + h.radius * Math.sin(angle));

			coordinates.add(new Coordinate(newX, newY));
		}

		coordinates.add(coordinates.get(0));

		Coordinate[] coordinateArray = new Coordinate[coordinates.size()];

		GeometryFactory gf = new GeometryFactory();

		return gf.createLinearRing(coordinates.toArray(coordinateArray));
	}

	/**
	 * Adds strong way points to the graph that are used for the navigation of
	 * AbstractWaypointMovementModel implementations.
	 * 
	 * FIXME: Find a way to place all waypoints (currently only used x retries)
	 * FIXME: Remove hotspots that are fully contained in an obstacle!!
	 * 
	 * @param hotspots
	 *            Hotspots to whom the new way points shall be added
	 */
	private void addStrongWaypoints(List<Hotspot> hotspots) {
		double radiiSum = 0.0;

		for (Hotspot h : hotspots) {
			radiiSum += h.radius;
		}

		double wps = noOfWaypoints / radiiSum;

		int wpCount = 0;
		addHotspots: {
			for (Hotspot h : hotspots) {
				int nrWps = (int) (Math.round(wps * h.radius));
				int retries = 0;
				for (int i = 0; i < nrWps; i++) {
					PositionVector loc = selectRandomLocation(h);

					if (obstacleModel != null) {
						if (!obstacleModel.contains(loc)) {
							model.addWaypoint(new StrongWaypoint<Object>(loc));
							if (++wpCount >= noOfWaypoints)
								break addHotspots;
						} else {
							if (!(retries++ >= placementRetries))
								i--;
						}
					} else {
						model.addWaypoint(new StrongWaypoint<Object>(loc));
						if (++wpCount >= noOfWaypoints)
							break addHotspots;
					}
				}
			}
		}

		Monitor.log(OSMHotspotStrategy.class, Level.INFO, "Added " + wpCount
				+ " strong waypoints.");
	}

	/**
	 * Selects a random location inside the hotspots radius
	 * 
	 * @param hotspot
	 * @return
	 */
	private PositionVector selectRandomLocation(Hotspot hotspot) {
		double theta = rnd.nextDouble() * Math.PI * 2;
		double length = Math.sqrt(rnd.nextDouble());

		double x = Math.cos(theta) * hotspot.radius * length;
		double y = Math.sin(theta) * hotspot.radius * length;

		return new PositionVector(hotspot.position.getX() + x,
				hotspot.position.getY() + y);
	}

	/**
	 * Returns the position of the given amenity in the validHotspots list.
	 * 
	 * @param amenity
	 * @param validHotspots
	 * @return
	 */
	private static int getAmenityIndex(String amenity,
			ArrayList<String> validHotspots) {
		if (validHotspots == null)
			return 0;

		for (int i = 0; i < validHotspots.size(); i++) {
			if (validHotspots.get(i).equals(amenity)) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Bean for hotspots
	 * 
	 * @author Fabio Zöllner
	 * @version 1.0, 08.04.2012
	 */
	private static class Hotspot {
		public Hotspot() {
			//
		}

		public double weight;

		public String amenity;

		public PositionVector position;

		public String type;

		public double radius;

		public Geometry geometry;

		public boolean wasHidden = false;
	}

	/**
	 * Compares hotspots based on their amenity and prioritizes ways over nodes.
	 * 
	 * @author Fabio Zöllner
	 * @version 1.0, 08.04.2012
	 */
	private static class HotspotComparator implements Comparator<Hotspot> {
		private ArrayList<String> validHotspots;

		public HotspotComparator(ArrayList<String> validHotspots) {
			this.validHotspots = validHotspots;
		}

		@SuppressWarnings("synthetic-access")
		@Override
		public int compare(Hotspot h1, Hotspot h2) {
			int idx1 = getAmenityIndex(h1.amenity, validHotspots);
			int idx2 = getAmenityIndex(h2.amenity, validHotspots);
			int diff = idx1 - idx2;

			if (diff != 0) {
				return diff;
			} else {
				if ("way".equals(h1) && "node".equals(h2))
					return 1;
				if ("node".equals(h1) && "way".equals(h2))
					return -1;

				return 0;
			}
		}
	}

	private class HotspotDeplacmentInfo {
		public PositionVector edge1;

		public PositionVector edge2;

		public PositionVector newPosition;

		public PositionVector oldPosition;

		public HotspotDeplacmentInfo(PositionVector edge1,
				PositionVector edge2, PositionVector newPosition,
				PositionVector oldPosition) {
			this.edge1 = edge1;
			this.edge2 = edge2;
			this.newPosition = newPosition;
			this.oldPosition = oldPosition;
		}
	}

	/**
	 * Draws the hotspots on the map and displays some extra information
	 * 
	 * @author Fabio Zöllner
	 * @version 1.0, 08.04.2012
	 */
	private class ShowHotspots extends JComponent {
		private List<Hotspot> hotspots;

		private WaypointModel model;

		public ShowHotspots(List<Hotspot> hotspots, WaypointModel model) {
			super();

			this.hotspots = hotspots;
			this.model = model;

			PositionVector dimension = model.getMap().getDimensions();

			setBounds(0, 0, (int) dimension.getX(), (int) dimension.getY());
			setOpaque(false);
			setVisible(true);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			Graphics2D g2 = (Graphics2D) g;

			g2.setFont(new Font("SansSerif", Font.PLAIN, 9));

			int i = 0;
			for (Hotspot h : hotspots) {
				if (!h.wasHidden) {
					g2.setColor(new Color(255, 255, 0, 100));
				} else {
					g2.setColor(new Color(0, 0, 255, 100));
				}
				g2.fillOval((int) (h.position.getX() - h.radius),
						(int) (h.position.getY() - h.radius),
						(int) h.radius * 2, (int) h.radius * 2);

				g2.setColor(Color.BLACK);
				if (h.type != null) {
					g2.drawString(
							h.type,
							(int) (h.position.getX() - h.amenity.length() * 1.5),
							(int) h.position.getY());
				}
			}

			drawLerp(g2);
		}

		protected void drawLerp(Graphics2D g2) {
			for (HotspotDeplacmentInfo l : hotspotMoveLerpPositions) {

				g2.setColor(new Color(0, 0, 0, 255));
				g2.drawLine((int) l.edge1.getX(), (int) l.edge1.getY(),
						(int) l.edge2.getX(), (int) l.edge2.getY());

				g2.fillOval((int) l.edge1.getX() - 2, (int) l.edge1.getY() - 2,
						4, 4);
				g2.fillOval((int) l.edge2.getX() - 2, (int) l.edge2.getY() - 2,
						4, 4);

				g2.setColor(new Color(238, 232, 170, 100));
				g2.fillOval((int) l.newPosition.getX() - 2,
						(int) l.newPosition.getY() - 2, 4, 4);
				g2.setColor(new Color(238, 232, 170, 100));
				g2.drawLine((int) l.oldPosition.getX(),
						(int) l.oldPosition.getY(), (int) l.newPosition.getX(),
						(int) l.newPosition.getY());
			}
		}
	}
}
