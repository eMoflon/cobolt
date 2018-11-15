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
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JComponent;

import de.tud.kom.p2psim.api.topology.obstacles.Obstacle;
import de.tud.kom.p2psim.api.topology.obstacles.ObstacleModel;
import de.tud.kom.p2psim.api.topology.obstacles.ObstacleModelListener;
import de.tud.kom.p2psim.impl.topology.obstacles.PolygonObstacle;
import de.tud.kom.p2psim.impl.topology.views.VisualizationTopologyView.VisualizationInjector;

/**
 * 
 * @author Fabio Zöllner
 * @version 1.0, 27.03.2012
 */
public class ObstacleComponentVis extends JComponent implements ObstacleModelListener {
	
	protected BufferedImage image;
	
	protected volatile boolean needsRedraw = true;

	private ObstacleModel model;

	public ObstacleComponentVis(ObstacleModel model) {
		this.model = model;
		setBounds(0, 0, VisualizationInjector.getWorldX(),
				VisualizationInjector.getWorldY());
		setOpaque(false);
		setVisible(true);

		image = new BufferedImage(VisualizationInjector.getWorldX(),
				VisualizationInjector.getWorldY(), BufferedImage.TYPE_INT_ARGB);
			
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
		
		try {
			drawObstacles(g2);
		} catch (RuntimeException e) {
			needsRedraw = true;
		}
	}
	
	protected void drawObstacles(Graphics2D g2) {
		List<Obstacle> obstacles = model.getObstacles();
		
		if (obstacles.isEmpty())  {
			throw new RuntimeException() {
				@Override public synchronized Throwable fillInStackTrace() { return null; }
			};
		}
		
		String label;
		Shape shape;
		
		for (Obstacle obstacle : obstacles) {
			if (!(obstacle instanceof PolygonObstacle)) 
				continue;
			
			PolygonObstacle pObstacle = (PolygonObstacle) obstacle;
			
			Color color = pObstacle.getCustomColor();
			if (color == null) {
				g2.setColor(Color.RED);
			} else {
				g2.setColor(color);
			}

			label = "Damping: " + obstacle.dampingFactor();
			shape = pObstacle.getAwtPolygon(VisualizationInjector.getScale());

			int[] xpoints = pObstacle.getXPoints();
			int[] ypoints = pObstacle.getYPoints();
			
			for (int i = 0; i < xpoints.length; i++) {
				xpoints[i] = (int) (xpoints[i]
						/ VisualizationInjector.getScale());
				ypoints[i] = (int) (ypoints[i]
						/ VisualizationInjector.getScale());
			}
			
			shape = new Polygon(xpoints, ypoints, xpoints.length);
			
			//g2.setFont(VisualizationTopologyView.FONT_MEDIUM);
			
			//g2.drawString(label,
			//		(int) (shape.getBounds2D().getCenterX() - label.length() * 1.5),
			//		(int) shape.getBounds2D().getCenterY());
			
			if (color == null) {
				g2.setColor(new Color(255, 0, 0, 10 + (int) (100 * obstacle.dampingFactor())));
			} else {
				Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(), 10 + (int) (100 * obstacle.dampingFactor()));
				g2.setColor(c);
			}
			g2.fill(shape);
		}
	}

	@Override
	public void addedObstacle(Obstacle obstacle) {
		needsRedraw = true;
	}

}