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

import javax.swing.JComponent;

import de.tud.kom.p2psim.impl.topology.views.FiveGTopologyView;
import de.tud.kom.p2psim.impl.topology.views.VisualizationTopologyView.VisualizationInjector;
import de.tud.kom.p2psim.impl.topology.views.fiveg.AbstractGridBasedTopologyDatabase;
import de.tud.kom.p2psim.impl.topology.views.fiveg.FiveGTopologyDatabase.Entry;
import de.tudarmstadt.maki.simonstrator.api.Rate;
import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * Visualization for the {@link FiveGTopologyView}
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, Nov 5, 2015
 */
public class FiveGVisualization extends JComponent {

	protected BufferedImage image;

	protected volatile boolean needsRedraw = true;

	private final AbstractGridBasedTopologyDatabase database;

	public FiveGVisualization(AbstractGridBasedTopologyDatabase database) {
		setBounds(0, 0, VisualizationInjector.getWorldX(),
				VisualizationInjector.getWorldY());
		setOpaque(true);
		setVisible(true);

		this.database = database;
		image = new BufferedImage(VisualizationInjector.getWorldX(),
				VisualizationInjector.getWorldY(), BufferedImage.TYPE_INT_ARGB);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (needsRedraw) {
			redraw();
		}

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
			drawEntries(g2);
		} catch (RuntimeException e) {
			needsRedraw = true;
		}
	}

	private void drawEntries(Graphics2D g2) {
		// Iterate over grid coordinates
		int stepSize = database.getGridSize();

		double maxLatency = 0;
		double minLatency = Double.MAX_VALUE;
		boolean isUpload = false;

		for (int x = 0; x < VisualizationInjector.getWorldX(); x += stepSize) {
			for (int y = 0; y < VisualizationInjector
					.getWorldY(); y += stepSize) {
				// TODO add checkbox for cloudlets?
				Entry entry = database.getEntryFor(database.getSegmentID(x, y),
						false);
				if (entry == null) {
					continue;
				}
				if (entry.getLatency(isUpload) > maxLatency) {
					maxLatency = entry.getLatency(isUpload);
				}
				if (entry.getLatency(isUpload) < minLatency) {
					minLatency = entry.getLatency(isUpload);
				}
			}
		}

		for (int x = 0; x < VisualizationInjector.getWorldX(); x += stepSize) {
			for (int y = 0; y < VisualizationInjector
					.getWorldY(); y += stepSize) {

				// TODO add checkbox for cloudlets?
				Entry entry = database.getEntryFor(database.getSegmentID(x, y),
						false);
				if (entry == null) {
					continue;
				}

				// TODO add checkbox for upload/download toggle?

				// Latency
				double latencyFactor = (entry.getLatency(isUpload) - minLatency)
						/ (maxLatency - minLatency);
				g2.setColor(
						new Color(255, 0, 0, 10 + (int) (40 * latencyFactor)));
				g2.fillRect(x, y, stepSize, stepSize);

				// Drop-Prob
				g2.setColor(new Color(255, 0, 0,
						10 + (int) (100 * entry.getDropProbability(isUpload))));
				g2.setStroke(new BasicStroke(
						(float) (10 * entry.getDropProbability(isUpload))));
				g2.drawRect(x, y, stepSize, stepSize);
				g2.setColor(new Color(255, 255, 255, 255));
				g2.drawString("L: "
						+ entry.getLatency(isUpload) / Time.MILLISECOND + " ms",
						x + 10, y + 15);
				g2.drawString(
						"D: " + (int) (entry.getDropProbability(isUpload) * 100)
								+ " %",
						x + 10, y + 25);
				g2.drawString("BW: "
						+ (int) (entry.getBandwidth(isUpload) / Rate.kbit_s)
						+ " kBit/s", x + 10, y + 35);
			}
		}

	}

}
