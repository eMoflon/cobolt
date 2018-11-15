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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;

import com.google.common.collect.Maps;

import de.tud.kom.p2psim.impl.util.guirunner.progress.SimulationProgressView;

public class PlottingView extends JFrame {
	private static final int VIEW_WIDTH = 900;

	private static final int VIEW_HEIGHT = 800;

	private static final int PLOT_HEIGHT = 200;
	private static final Color PLOT_BACKGROUND_COLOR = Color.WHITE;

	private Map<String, XYChart> nameToPlotMap = Maps.newLinkedHashMap();

	private JPanel content;

	private JPanel selectorPanel;

	private Box plotBox;

	private GridBagConstraints layoutConstraints;

	public PlottingView(String name) {
		// this.name = name;

		setTitle(name);
		setVisible(true);

		buildUI();
	}

	private void buildUI() {
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				SimulationProgressView.getInstance().onCancelWithConfirmation();
			}
		});

		content = new JPanel();
		content.setLayout(new BorderLayout());

		selectorPanel = new JPanel();
		selectorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		layoutConstraints = new GridBagConstraints();
		layoutConstraints.anchor = GridBagConstraints.NORTH;
		layoutConstraints.weighty = 1;

		plotBox = Box.createVerticalBox();
		plotBox.setBackground(PLOT_BACKGROUND_COLOR);
		plotBox.setBorder(BorderFactory.createEtchedBorder());

		content.add(selectorPanel, BorderLayout.NORTH);
		content.add(new JScrollPane(plotBox), BorderLayout.CENTER);

		getContentPane().add(content, BorderLayout.CENTER);

		setSize(VIEW_WIDTH, VIEW_HEIGHT);
	}

	public XYChart createPlot(String name, String seriesName) {
		XYChart plot = new XYChart(PLOT_BACKGROUND_COLOR, name, seriesName);

		JPanel chartPanel = plot.getChartPanel();

		chartPanel = wrapInCollabsable(chartPanel, name);
		chartPanel.setPreferredSize(new Dimension((int)chartPanel.getPreferredSize().getWidth(), PLOT_HEIGHT));

		plotBox.add(chartPanel);
		plotBox.validate();

		nameToPlotMap.put(name, plot);

		this.pack();

		return plot;
	}

	public XYChart createPlot(String name) {
		return createPlot(name, name);
	}

	private JPanel wrapInCollabsable(JPanel chartPanel, String title) {
		final JPanel innerPanel = new JPanel();
		innerPanel.setLayout(new BorderLayout());
		innerPanel.add(new JLabel("  "), BorderLayout.WEST);
		innerPanel.add(chartPanel, BorderLayout.CENTER);
		innerPanel.setBackground(PLOT_BACKGROUND_COLOR);

		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBackground(PLOT_BACKGROUND_COLOR);

		Box header = Box.createHorizontalBox();
		header.setBackground(PLOT_BACKGROUND_COLOR);

		final JLabel collapseButton = new JLabel("-");
		Dimension buttonSize = new Dimension(11, 11);
		collapseButton.setBackground(PLOT_BACKGROUND_COLOR);
		collapseButton.setPreferredSize(buttonSize);
		collapseButton.setMaximumSize(buttonSize);
		collapseButton.setMinimumSize(buttonSize);
		collapseButton
				.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		collapseButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				collapseButton.setBorder(
						BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				collapseButton.setBorder(
						BorderFactory.createBevelBorder(BevelBorder.RAISED));

				if (collapseButton.getText().equals("-")) {
					collapseButton.setText("+");
					panel.remove(innerPanel);
				} else {
					collapseButton.setText("-");
					panel.add(innerPanel, BorderLayout.CENTER);
				}
			}
		});

		JLabel titleLabel = new JLabel(" " + title);
		Font font = titleLabel.getFont();
		titleLabel.setFont(font.deriveFont(5));
		titleLabel.setBackground(PLOT_BACKGROUND_COLOR);

		header.add(new JLabel(" "));
		header.add(collapseButton);
		header.add(titleLabel);

		panel.add(header, BorderLayout.NORTH);
		panel.add(innerPanel, BorderLayout.CENTER);

		return panel;
	}

	public void removeAllPlots() {
		nameToPlotMap.clear();
		plotBox.removeAll();
		content.doLayout();
		content.validate();
	}
}