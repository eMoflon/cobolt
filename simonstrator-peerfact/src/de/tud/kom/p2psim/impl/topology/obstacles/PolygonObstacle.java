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

import java.awt.Color;
import java.util.List;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Polygon;

import de.tud.kom.p2psim.impl.topology.PositionVector;

/**
 * This Obstacle is build from a list of coordinates that form a Polygon. The
 * coordinates have to be specified in clockwise direction.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 20.03.2012
 */
public class PolygonObstacle extends AbstractObstacle {

	private final GeometryFactory factory = new GeometryFactory();

	private Polygon poly;

	private double damping = 1.0;

	int[] xpoints;

	int[] ypoints;

	double minX = Double.POSITIVE_INFINITY;

	double minY = Double.POSITIVE_INFINITY;

	double maxX = Double.NEGATIVE_INFINITY;

	double maxY = Double.NEGATIVE_INFINITY;

	private Color customColor;
	
	private List<PositionVector> vertices;

	/**
	 * A Polygon that is damping the signal (no link through this obstacle is
	 * possible)
	 * 
	 * @param vertices
	 */
	public PolygonObstacle(List<PositionVector> vertices) {
		this(vertices, 1.0);
	}

	public PolygonObstacle(List<PositionVector> vertices, double damping) {

		if (damping > 1)
			damping = 1;
		if (damping < 0)
			damping = 0;
		this.damping = damping;
		
		rebuildPolygon(vertices);
	}
	
	/**
	 * Returns the vertices this polygon was build with. Modifications
	 * to this list won't affect the obstacle. Use rebuildPolygon() to
	 * restructure the obstacle.
	 * 
	 * @return
	 */
	public List<PositionVector> getVertices() {
		return Lists.newArrayList(vertices);
	}
	
	public void rebuildPolygon(List<PositionVector> vertices) {
		assert vertices.size() > 2 : "You have to specify at least 3 Points";

		this.vertices = vertices;
		
		this.minX = Double.POSITIVE_INFINITY;
		this.minY = Double.POSITIVE_INFINITY;
		this.maxX = Double.NEGATIVE_INFINITY;
		this.maxY = Double.NEGATIVE_INFINITY;
		
		Coordinate[] coordinates = new Coordinate[vertices.size()+ 1];
		xpoints = new int[vertices.size()];
		ypoints = new int[vertices.size()];
		
		PositionVector firstVertex = vertices.get(0);
		coordinates[coordinates.length - 1] = new Coordinate(firstVertex.getX(), firstVertex.getY());
		int i = 0;
		for (PositionVector vertex : vertices) {
			coordinates[i] = new Coordinate(vertex.getX(), vertex.getY());
			xpoints[i] = (int) vertex.getX();
			ypoints[i] = (int) vertex.getY();
			if (xpoints[i] < minX) {
				minX = xpoints[i];
			}
			if (ypoints[i] < minY) {
				minY = ypoints[i];
			}
			if (xpoints[i] > maxX) {
				maxX = xpoints[i];
			}
			if (ypoints[i] > maxY) {
				maxY = ypoints[i];
			}

			i++;
		}
		poly = factory.createPolygon(factory.createLinearRing(coordinates),
				null);
	}

	public int[] getXPoints() {
		return this.xpoints;
	}
	
	public int[] getYPoints() {
		return this.ypoints;
	}

	@Override
	public boolean intersectsWith(PositionVector a, PositionVector b) {
		/*
		 * Adding a bit of greedy decisions, to not always use the poly-methods
		 */
		double dist = a.distanceTo(b);
		if (a.getX() + dist < minX || a.getX() - dist > maxX
				|| a.getY() + dist < minY || a.getY() - dist > maxY) {
			return false;
		}

		Coordinate[] lineCoords = new Coordinate[]{new Coordinate(a.getX(), a.getY()), new Coordinate(b.getX(), b.getY())};
		return poly.intersects(factory.createLineString(lineCoords));
	}

	@Override
	public boolean contains(Geometry g) {
		return poly.contains(g);
	}

	@Override
	public double totalIntersectionLength(PositionVector a, PositionVector b) {
		Coordinate[] lineCoords = new Coordinate[] {
				new Coordinate(a.getX(), a.getY()),
				new Coordinate(b.getX(), b.getY()) };
		Geometry testLine = factory.createLineString(lineCoords);

		double fullLength = testLine.getLength();
		testLine = testLine.difference(poly);

		if (testLine instanceof MultiLineString) { // some intersections
			MultiLineString resultLines = (MultiLineString) testLine;
			return fullLength - resultLines.getLength();
		}
		if (testLine instanceof GeometryCollection) { // all is intersected
			GeometryCollection geocol = (GeometryCollection) testLine;
			if (geocol.isEmpty())
				return fullLength;

			assert (false) : "Geometry collection is not empty";
			return fullLength;
		} else { // no intersection
			assert (testLine instanceof LineString);
			LineString resultLine = (LineString) testLine;
			return fullLength - resultLine.getLength();
		}
	}

	@Override
	public double dampingFactor() {
		return damping;
	}

	public java.awt.Polygon getAwtPolygon(double scale) {
		int[] xScaled = new int[xpoints.length];
		int[] yScaled = new int[ypoints.length];
		for (int i = 0; i < yScaled.length; i++) {
			xScaled[i] = (int) (xpoints[i] / scale);
			yScaled[i] = (int) (ypoints[i] / scale);
		}
		return new java.awt.Polygon(xScaled, yScaled, yScaled.length);
	}

	@Override
	public Geometry getGeometry() {
		return this.poly;
	}

	public PositionVector getCentroid() {
		double xCenter = 0;
		double yCenter = 0;
	    
		for (int i = 0; i < xpoints.length; i++) {
	    	xCenter += xpoints[i];
	    	yCenter += ypoints[i];
	    }
	    
		return new PositionVector(xCenter * (1/xpoints.length), yCenter * (1/ypoints.length));
	}

	public void setDampeningFactor(double dampeningFactor) {
		this.damping = dampeningFactor;
	}

	public void setCustomColor(Color color) {
		this.customColor = color;
	}
	
	public Color getCustomColor() {
		return this.customColor;
	}
}
