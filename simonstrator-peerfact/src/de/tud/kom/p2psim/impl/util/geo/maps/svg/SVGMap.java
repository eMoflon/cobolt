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

package de.tud.kom.p2psim.impl.util.geo.maps.svg;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.PathIterator;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;
import org.apache.batik.parser.AWTPathProducer;
import org.apache.batik.parser.PathParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vividsolutions.jts.algorithm.LineIntersector;
import com.vividsolutions.jts.algorithm.RobustLineIntersector;

import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.obstacles.PolygonObstacle;
import de.tud.kom.p2psim.impl.topology.views.VisualizationTopologyView.VisualizationInjector;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.Path;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.Waypoint;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.WeakWaypoint;
import de.tud.kom.p2psim.impl.util.geo.maps.AbstractMap;

/**
 * This map generates waypoints and obstacles based on a given SVG file.
 * 
 * The SVG file must adhere to the following conventions: - Everything must be
 * contained in a rect element named "map" (id) - Obstacles are implemented
 * using rect or path elements. - Their id must start with "obstacle" - Ways for
 * the movement of nodes are implemented using path elements. - Their id must
 * start with "way"
 * 
 * The size of the map element will be used to determine the map size and to
 * scale the map to the configured dimensions. All elements besides rect and
 * path elements with the previously named ids are ignored.
 * 
 * Note: The SVGMap will not verify if the obstacles or ways are inside of the
 * map rect. All rects or paths with the correct ids will be loaded regardless
 * of their positioning.
 * 
 * @author Fabio Zöllner
 * @version 1.0, 22.10.2012
 */
public class SVGMap extends AbstractMap {

	private Map<PositionVector, Waypoint> waypointCache = Maps.newHashMap();

	@Override
	protected void doLoadMap() {
		File osmFile = new File(getFilename());

		if (!osmFile.exists())
			throw new ConfigurationException("Couldn't find SVG file: "
					+ getFilename());

		try {

			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			SAXParser parser;
			parser = parserFactory.newSAXParser();

			parser.parse(new File(filename), new SVGMapHandler());

			postProcessing();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void postProcessing() {
		waypointCache.clear();

		resolveIntersections();
	}

	private void resolveIntersections() {
		List<PositionVector> intersections = Lists.newLinkedList();

		List<Path> worklist = Lists.newLinkedList(getPaths());
		Iterator<Path> iter = concurrentIterator(worklist);

		while (iter.hasNext()) {
			Path path = iter.next();

			Iterator<Path> compareIter = concurrentIterator(worklist);
			while (compareIter.hasNext()) {
				Path comparePath = compareIter.next();

				// Prevent intersection comparison for the same path and
				// connected paths
				if (path.equals(comparePath))
					continue;
				if (path.getSource().getPosition()
						.equals(comparePath.getSource().getPosition()))
					continue;
				if (path.getSource().getPosition()
						.equals(comparePath.getTarget().getPosition()))
					continue;
				if (path.getTarget().getPosition()
						.equals(comparePath.getSource().getPosition()))
					continue;
				if (path.getTarget().getPosition()
						.equals(comparePath.getTarget().getPosition()))
					continue;

				PositionVector intersection = intersects(path, comparePath);
				if (intersection != null) {
					Path[] splitPath1 = splitPath(path, intersection);
					Path[] splitPath2 = splitPath(comparePath, intersection);

					// Prevent the creation of loops
					if (splitPath1[0].getSource().equals(
							splitPath1[0].getTarget()))
						continue;
					if (splitPath1[1].getSource().equals(
							splitPath1[1].getTarget()))
						continue;
					if (splitPath2[0].getSource().equals(
							splitPath2[0].getTarget()))
						continue;
					if (splitPath2[1].getSource().equals(
							splitPath2[1].getTarget()))
						continue;

					worklist.add(splitPath1[0]);
					worklist.add(splitPath1[1]);
					worklist.add(splitPath2[0]);
					worklist.add(splitPath2[1]);

					intersections.add(intersection);

					compareIter.remove();
					iter.remove();

					break;
				}
			}
		}

		getPaths().clear();
		getPaths().addAll(worklist);

		VisualizationInjector.injectComponent("New intersections", 1,
				new MarkPositions(intersections, this));
	}

	public <E> Iterator<E> concurrentIterator(final List<E> list) {
		return new Iterator<E>() {
			int index = -1;

			@Override
			public boolean hasNext() {
				System.err.println("Checking if index " + (index + 1)
						+ " is in bounds");
				return index + 1 < list.size();
			}

			@Override
			public E next() {
				index++;

				if (index < list.size()) {
					return list.get(index);
				} else {
					throw new IndexOutOfBoundsException(
							"List has "
									+ list.size()
									+ " entries at the moment. Couldn't retrieve entry at position "
									+ index);
				}
			}

			@Override
			public void remove() {
				if (index < list.size()) {
					list.remove(index);
					index--;
				} else {
					throw new IndexOutOfBoundsException(
							"List has "
									+ list.size()
									+ " entries at the moment. Couldn't retrieve entry at position "
									+ index);
				}
			}

		};
	}

	private Path[] splitPath(Path path, PositionVector intersectionPoint) {
		Path[] paths = new Path[2];

		Waypoint wp1 = path.getSource();
		Waypoint wp2 = path.getTarget();
		Waypoint iwp = getWaypoint(intersectionPoint);

		paths[0] = new Path(wp1, iwp);
		paths[1] = new Path(wp2, iwp);

		return paths;
	}

	private PositionVector intersects(Path path1, Path path2) {
		LineIntersector inter = new RobustLineIntersector();

		inter.computeIntersection(path1.getSource().getPosition()
				.asCoordinate(),
				path1.getTarget().getPosition().asCoordinate(), path2
						.getSource().getPosition().asCoordinate(), path2
						.getTarget().getPosition().asCoordinate());

		if (inter.getIntersectionNum() == 0) {
			return null;
		}

		return new PositionVector(inter.getIntersection(0).x,
				inter.getIntersection(0).y);
	}

	private class SVGMapHandler extends DefaultHandler {
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attrs) throws SAXException {
			if (checkId(attrs, "map")) {
				int x = (int) Double.parseDouble(attrs.getValue("x"));
				int y = (int) Double.parseDouble(attrs.getValue("y"));
				int width = (int) Double.parseDouble(attrs.getValue("width"));
				int height = (int) Double.parseDouble(attrs.getValue("height"));

				setMinPosition(new PositionVector(x, y));
				setMaxPosition(new PositionVector(x + width, y + height));

			} else if (checkId(attrs, "obstacle")) {
				if (qName.equals("rect")) {
					int x = (int) Double.parseDouble(attrs.getValue("x"));
					int y = (int) Double.parseDouble(attrs.getValue("y"));
					int width = (int) Double.parseDouble(attrs
							.getValue("width"));
					int height = (int) Double.parseDouble(attrs
							.getValue("height"));

					addObstacle(new PolygonObstacle(Lists.newArrayList(
							pos(x, y), pos(x + width, y),
							pos(x + width, y + height), pos(x, y + height))));
				} else if (qName.equals("path")) {
					List<PositionVector> positions = parseObstaclePositions(attrs
							.getValue("d"));
					addObstacle(new PolygonObstacle(positions));
				}
				// TODO: Add path
			} else if (checkId(attrs, "way")) {
				parseWay(attrs.getValue("d"));

			}
		}
	}

	private Waypoint getWaypoint(PositionVector pos) {
		Waypoint wp = waypointCache.get(pos);
		if (wp == null) {
			wp = new WeakWaypoint(pos);
			waypointCache.put(pos, wp);
		}
		return wp;
	}

	void parseWay(String d) {
		PathParser parser = new PathParser();

		AWTPathProducer producer = new AWTPathProducer();
		parser.setPathHandler(producer);

		parser.parse(d);

		ExtendedGeneralPath path = (ExtendedGeneralPath) producer.getShape();

		PathIterator iter = path.getPathIterator(null, 1.0);

		Waypoint lastWaypoint = null;
		Waypoint lastMoveTo = null;

		double[] coords = new double[6];
		while (!iter.isDone()) {
			int type = iter.currentSegment(coords);

			if (type == iter.SEG_MOVETO) {
				lastWaypoint = getWaypoint(new PositionVector((int) coords[0],
						(int) coords[1]));
				lastMoveTo = lastWaypoint;
				System.err.println("Moved to " + lastMoveTo);
			} else if (type == iter.SEG_LINETO) {
				Waypoint targetWaypoint = getWaypoint(new PositionVector(
						(int) coords[0], (int) coords[1]));
				createPath(lastWaypoint, targetWaypoint);
				lastWaypoint = targetWaypoint;
				System.err.println("Created a line to " + targetWaypoint);
			} else if (type == iter.SEG_CLOSE) {
				createPath(lastWaypoint, lastMoveTo);
				lastWaypoint = lastMoveTo;
				System.err.println("Closed a line to " + lastMoveTo);
			}

			iter.next();
		}
	}

	@Override
	public void createPath(Waypoint wp1, Waypoint wp2) {
		if (wp1.equals(wp2))
			return; // Prevent loops

		super.createPath(wp1, wp2);
	}

	List<PositionVector> parseObstaclePositions(String d) {
		PathParser parser = new PathParser();

		AWTPathProducer producer = new AWTPathProducer();
		parser.setPathHandler(producer);

		parser.parse(d);

		ExtendedGeneralPath path = (ExtendedGeneralPath) producer.getShape();

		PathIterator iter = path.getPathIterator(null, 1.0);

		PositionVector firstPosition = null;

		List<PositionVector> positions = Lists.newLinkedList();
		double[] coords = new double[6];
		while (!iter.isDone()) {
			int type = iter.currentSegment(coords);

			PositionVector newPosition = new PositionVector((int) coords[0],
					(int) coords[1]);

			if (firstPosition == null) {
				firstPosition = newPosition;
			}

			if (type == iter.SEG_MOVETO || type == iter.SEG_LINETO) {
				positions.add(newPosition);
			} else if (type == iter.SEG_CLOSE) {
				positions.add(firstPosition);
			} else {
				throw new ConfigurationException(
						"Cannot handle obstacles with a path more complex than a simple closed path. (e.g. using more than moveto, lineto or close commands)");
			}

			iter.next();
		}

		return positions;
	}

	boolean checkId(Attributes attrs, String value) {
		if (attrs.getValue("id") == null)
			return false;

		return attrs.getValue("id").startsWith(value);
	}

	private class MarkPositions extends JComponent {
		private List<PositionVector> positions;

		public MarkPositions(List<PositionVector> positions, SVGMap map) {
			super();

			this.positions = positions;

			setBounds(0, 0, VisualizationInjector.getWorldX(),
					VisualizationInjector.getWorldY());
			setOpaque(false);
			setVisible(true);
		}

		protected PositionVector toPixelCoords(PositionVector position,
				PositionVector pixelPerMeter) {
			PositionVector clonedPosition = position.clone();
			PositionVector relativePosition = clonedPosition
					.minus(getMinPosition());
			relativePosition.multiply(pixelPerMeter);

			return relativePosition;
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			Graphics2D g2 = (Graphics2D) g;

			g2.setFont(new Font("SansSerif", Font.PLAIN, 9));

			PositionVector lst = null;
			for (PositionVector p : positions) {
				PositionVector pos = p;

				if (ppm != null) {
					pos = toPixelCoords(p, ppm);
				}

				g2.setColor(Color.RED);
				g2.drawOval((int) pos.getX() - 10, (int) pos.getY() - 10, 20,
						20);

				g2.setColor(Color.BLACK);
				g2.drawLine((int) pos.getX() - 2, (int) pos.getY() - 2,
						(int) pos.getX() + 2, (int) pos.getY() + 2);
				g2.drawLine((int) pos.getX() - 2, (int) pos.getY() + 2,
						(int) pos.getX() + 2, (int) pos.getY() - 2);

				if (lst != null) {
					g2.setColor(Color.GREEN);
					g2.drawLine((int) lst.getX(), (int) lst.getY(),
							(int) pos.getX(), (int) pos.getY());
				}
				lst = pos;
			}
		}
	}
}
