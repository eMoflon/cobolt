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

package de.tud.kom.p2psim.impl.topology.views.visualization.world;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.Set;

import javax.swing.JComponent;

import de.tud.kom.p2psim.api.topology.waypoints.WaypointModel;
import de.tud.kom.p2psim.api.topology.waypoints.WaypointModelListener;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.views.VisualizationTopologyView.VisualizationInjector;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.DefaultWeightedEdgeRetrievableGraph;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.Path;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.Waypoint;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.WeakWaypoint;
import de.tudarmstadt.maki.simonstrator.api.Randoms;

/**
 * 
 * @author Fabio Zöllner
 * @version 1.0, 27.03.2012
 */
public class WeakWaypointComponentVis extends JComponent implements WaypointModelListener {

	protected final int WAYPOINT_STRONG_RADIUS = 2;
	protected final int WAYPOINT_WEAK_RADIUS = 1;
	
	protected Color COLOR_PATH;
	
	protected Color COLOR_STRONG_WAYPOINT;
	protected Color COLOR_WEAK_WAYPOINT;
	
	protected Random rnd = Randoms
			.getRandom(WeakWaypointComponentVis.class);
	
	protected BufferedImage image;
	
	protected volatile boolean needsRedraw = true;

	private WaypointModel model;
	
	public WeakWaypointComponentVis(WaypointModel model) {
		this.model = model;
		setBounds(0, 0, VisualizationInjector.getWorldX(),
				VisualizationInjector.getWorldY());
		setOpaque(false);
		setVisible(true);
		COLOR_STRONG_WAYPOINT = new Color(30, 144, 255, 255);
		COLOR_WEAK_WAYPOINT = new Color(30, 144, 255, 200);
		COLOR_PATH = new Color(49, 79, 79, 130);
		
		image = new BufferedImage(VisualizationInjector.getWorldX(),
				VisualizationInjector.getWorldY(),
				BufferedImage.TYPE_INT_ARGB);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (needsRedraw) 
			redraw();
		
		Graphics2D g2 = (Graphics2D) g;

		g2.drawImage(image, 0, 0, null);
	}
	
	protected void redraw() {
		needsRedraw = false;
		
		Graphics2D g2 = (Graphics2D) image.getGraphics();

		Composite c = g2.getComposite();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
		Rectangle2D.Double rect = new Rectangle2D.Double(0, 0,
				VisualizationInjector.getWorldX(),
				VisualizationInjector.getWorldY());
		g2.fill(rect);
		g2.setComposite(c);
		
		// TODO Change to non-colored version
		try {
			//drawColoredGraph(g2);
			drawPaths(g2);
		} catch (RuntimeException e) {
			needsRedraw = true;
		}
		
		try {
			drawWaypoints(g2);
		} catch (RuntimeException e) {
			needsRedraw = true;
		}
	}
	
	protected void drawColoredGraph(Graphics2D g2) {
		ArrayList<Path> usedWaypoints = new ArrayList<Path>();
		ArrayList<Waypoint> starts = new ArrayList<Waypoint>();
		Collection<Waypoint> waypoints = model.getWaypoints();
		
		DefaultWeightedEdgeRetrievableGraph<Waypoint, Path> graph = model.getGraph();

		if (waypoints == null || waypoints.isEmpty() || graph == null)  {
			throw new RuntimeException() {
				@Override public synchronized Throwable fillInStackTrace() { return null; }
			};
		}
		
		for (Waypoint wp : waypoints) {
			if (starts.contains(wp)) continue;
			starts.add(wp);
			
			ArrayList<Path> workList = new ArrayList<Path>();
			workList.addAll(graph.edgesOf(wp));
			
			Color pathColor = getRandomColor();
			
			while (!workList.isEmpty()) {
				Path p = workList.remove(0);
				
				Waypoint target = p.getTarget();
				starts.add(target);
				
				if (containsPath(usedWaypoints, p))
					continue;
				
				workList.addAll(graph.edgesOf(target));
				
				PositionVector sourcePos = p.getSource().getPosition();
				PositionVector targetPos = p.getTarget().getPosition();
				
				g2.setColor(pathColor);
				g2.setStroke(new BasicStroke(0.8f));
				
				g2.drawLine(VisualizationInjector.scaleValue(sourcePos.getX()),
						VisualizationInjector.scaleValue(sourcePos.getY()),
						VisualizationInjector.scaleValue(targetPos.getX()),
						VisualizationInjector.scaleValue(targetPos.getY()));

				usedWaypoints.add(p);
			}
		}
	}

	private boolean containsPath(ArrayList<Path> workList, Path p1) {
		for (Path p : workList) {
			if (p.equals(p1)) {
				return true;
			}
		}
		return false;
	}
	
	private Color getRandomColor() {
		return new Color(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat());
	}
	
	protected void drawPaths(Graphics2D g2) {
		Set<Path> paths = model.getPaths();
		
		if (paths.isEmpty()) {
			throw new RuntimeException() {
				@Override public synchronized Throwable fillInStackTrace() { return null; }
			};
		}
		
		for (Path p : paths) {
			PositionVector sourcePos = p.getSource().getPosition();
			PositionVector targetPos = p.getTarget().getPosition();

			g2.setColor(COLOR_PATH);
			g2.setStroke(new BasicStroke(0.8f));
			g2.drawLine(VisualizationInjector.scaleValue(sourcePos.getX()),
					VisualizationInjector.scaleValue(sourcePos.getY()),
					VisualizationInjector.scaleValue(targetPos.getX()),
					VisualizationInjector.scaleValue(targetPos.getY()));
		}
	}
	
	protected void drawWaypoints(Graphics2D g2) {
		Collection<Waypoint> waypoints = model.getWaypoints();
		
		for (Waypoint wp : waypoints) {
			PositionVector pos = wp.getPosition();
			
			if (wp instanceof WeakWaypoint) {
				g2.setColor(COLOR_WEAK_WAYPOINT);
				g2.fillOval(
						VisualizationInjector.scaleValue(pos.getX())
								- WAYPOINT_WEAK_RADIUS,
						VisualizationInjector.scaleValue(pos.getY())
								- WAYPOINT_WEAK_RADIUS,
						WAYPOINT_WEAK_RADIUS * 2, WAYPOINT_WEAK_RADIUS * 2);
			}
		}
	}

	@Override
	public void addedPath(Path path) {
		needsRedraw = true;
	}

	@Override
	public void addedWaypoint(Waypoint waypoint) {
		needsRedraw = true;
	}
	
	@Override
	public void modifiedWaypoints() {
		needsRedraw = true;
	}

}