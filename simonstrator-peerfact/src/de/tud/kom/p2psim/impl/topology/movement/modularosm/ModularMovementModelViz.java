/*
 * Copyright (c) 2005-2015 KOM – Multimedia Communications Lab
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

package de.tud.kom.p2psim.impl.topology.movement.modularosm;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import javax.swing.JComponent;

import de.tud.kom.p2psim.impl.topology.movement.modularosm.attraction.AttractionPoint;
import de.tud.kom.p2psim.impl.topology.views.VisualizationTopologyView;
import de.tud.kom.p2psim.impl.topology.views.VisualizationTopologyView.VisualizationInjector;

/**
 * Visualization Component of the Attraction Points in the Modular Movement Model.
 * 
 * 
 * @author Martin Hellwig
 * @version 1.0, 02.07.2015
 */
public class ModularMovementModelViz extends JComponent {

	private ModularMovementModel movementModel;

	private static Color COLOR_ATTR_POINT = new Color(0.2f, 0.2f, 0.2f, 0.4f);

	public ModularMovementModelViz(ModularMovementModel model) {
		setBounds(0, 0, VisualizationInjector.getWorldX(),
				VisualizationInjector.getWorldY());
		setOpaque(true);
		setVisible(true);
		this.movementModel = model;
	}

	@Override
	public void paint(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		for (AttractionPoint aPoint : movementModel.getAttractionPoints()) {
			Point point = aPoint.getRealPosition().asPoint();
			// draw border
			g2.setColor(Color.BLACK);
			g2.setFont(VisualizationTopologyView.FONT_MEDIUM);
			g2.drawString(aPoint.getName(),
					VisualizationInjector.scaleValue(point.x) - 15,
					VisualizationInjector.scaleValue(point.y) - 15);
			g2.setColor(COLOR_ATTR_POINT);
			g2.fillOval(VisualizationInjector.scaleValue(point.x) - 15,
					VisualizationInjector.scaleValue(point.y) - 15, 20, 20);

		}
	}
}
