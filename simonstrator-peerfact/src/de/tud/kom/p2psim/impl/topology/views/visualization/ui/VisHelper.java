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

package de.tud.kom.p2psim.impl.topology.views.visualization.ui;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;

/**
 * Helpers for your own visualization components
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 27.08.2012
 */
public class VisHelper {

	/**
	 * Draws an arrow pointing from x1,y1 to x2,y2 with the given thickness
	 * 
	 * @param g2
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param thickness
	 */
	public static void drawArrow(Graphics2D g2, double x1, double y1,
			double x2, double y2, float thickness) {

		if (Point2D.distance(x1, y1, x2, y2) < 10) {
			return;
		}

		g2.setStroke(new BasicStroke(thickness));
		g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
		Polygon arrowPolygon = new Polygon();
		double arc = Math.signum(y1 - y2) * Math.signum(x1 - x2)
				* Math.atan((y2 - y1) / (x2 - x1));
		double xt = x2 - Math.signum(x1 - x2) * 3 * thickness
				* Math.cos(arc + 3 * Math.PI / 4);
		double yt = y2 - Math.signum(y1 - y2) * 3 * thickness
				* Math.sin(arc + 3 * Math.PI / 4);
		double xb = x2 - Math.signum(x1 - x2) * 3 * thickness
				* Math.cos(arc - 3 * Math.PI / 4);
		double yb = y2 - Math.signum(y1 - y2) * 3 * thickness
				* Math.sin(arc - 3 * Math.PI / 4);
		arrowPolygon.addPoint((int) xt, (int) yt);
		arrowPolygon.addPoint((int) xb, (int) yb);
		arrowPolygon.addPoint((int) x2, (int) y2);
		g2.fill(arrowPolygon);
		g2.draw(arrowPolygon);
	}

	public static void drawArrow(Graphics2D g2, Point from, Point to,
			float thickness) {
		drawArrow(g2, from.getX(), from.getY(), to.getX(), to.getY(), thickness);
	}

}
