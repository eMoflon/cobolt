/*
 * Copyright (c) 2005-2010 KOM - Multimedia Communications Lab
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

package de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.analyzer;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import de.tud.kom.p2psim.api.analyzer.EnergyAnalyzer;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.energy.EnergyComponent;
import de.tud.kom.p2psim.api.energy.EnergyState;
import de.tud.kom.p2psim.api.linklayer.mac.Link;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.topology.Topology;
import de.tud.kom.p2psim.api.topology.views.TopologyView;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.views.VisualizationTopologyView;
import de.tud.kom.p2psim.impl.topology.views.VisualizationTopologyView.VisualizationInjector;
import de.tud.kom.p2psim.impl.topology.views.visualization.world.NodeVisInteractionListener;
import de.tud.kom.p2psim.impl.util.oracle.GlobalOracle;
import de.tudarmstadt.maki.simonstrator.api.Binder;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent.Analyzer;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.ComponentFinder;
import de.tudarmstadt.maki.simonstrator.tc.component.TopologyControlComponent;

/**
 * Visualizes the topology links in the {@link VisualizationTopologyView}
 * 
 * @author Michael Stein
 * 
 */
public class TopologyLinkVisualizationAnalyzer extends JComponent implements Analyzer, EnergyAnalyzer {

	private static final Color NODE_LABEL_BACKGROUND = new Color(0.5f, 0.5f, 0.5f, 0.4f);

	private static final long serialVersionUID = 1L;

	private static final PhyType DEFAULT_PHY_TYPE = PhyType.WIFI;

	/**
	 * Using this value with {@link #setScreenshotInterval(long)} disables
	 * taking screenshots
	 */
	public static final long NO_SCREENSHOTS_INTERVAL = -1;

	private double currentScale = VisualizationInjector.getScale();

	private LinkedList<Link> links;

	private TopologyView topologyView;

	private Map<MacAddress, PositionVector> macToPosition = new HashMap<MacAddress, PositionVector>();

	private ArrayList<MacAddress> macAddresses = null;

	private BiMap<MacAddress, Host> macToHost = HashBiMap.create();

	private Color linkColor = Color.BLUE;

	private PhyType phyType = DEFAULT_PHY_TYPE;

	private Set<MacAddress> emptyNodes = new LinkedHashSet<MacAddress>();

	private File screenshotOutputFolder;
	private String screenshotFormat;
	private long screenshotIntervalInSimulationTimeUnits;
	private long timeOfNextScreenshot;

	/**
	 * UDG Connectivity in PeerfactSim is only updated when messages are being
	 * sent. Therefore, we explicitly enforce a topology update based on this
	 * interval
	 */
	private long updateInterval;

	public TopologyLinkVisualizationAnalyzer() {
		this.setScreenshotInterval(10 * Time.MINUTE);
		this.setScreenshotOutputFolderPath(Simulator.getConfigurator().getVariables().get("screenshotOutputFolder"));
		this.setScreenshotFormat("png");
		this.setUpdateInterval(Simulator.SECOND_UNIT);
	}

	/**
	 * Sets the {@link PhyType} of this view
	 * 
	 * @see PhyType#valueOf(String)
	 */
	public void setPhyType(final String phyType) {
		this.phyType = PhyType.valueOf(phyType);
	}

	public void setConnectionColor(final String rgbString) {
		final String[] splitString = rgbString.split(";");
		final int r = Integer.parseInt(splitString[0]);
		final int g = Integer.parseInt(splitString[1]);
		final int b = Integer.parseInt(splitString[2]);
		this.linkColor = new Color(r, g, b);
	}

	public void setScreenshotInterval(long screenshotInterval) {
		this.screenshotIntervalInSimulationTimeUnits = screenshotInterval;
	}

	public void setScreenshotOutputFolderPath(final String screenshotOutputFolderPath) {
		if (screenshotOutputFolderPath == null || screenshotOutputFolderPath.isEmpty())
			return;

		final File folder = new File(screenshotOutputFolderPath);

		if (folder.exists() && !folder.canWrite())
			throw new IllegalArgumentException("Cannot write to folder " + folder);

		if (!folder.exists() && !folder.mkdirs())
			throw new IllegalArgumentException("Cannot create missing folder " + folder);

		this.screenshotOutputFolder = folder;
	}

	public void setScreenshotFormat(String screenshotFormat) {
		this.screenshotFormat = screenshotFormat;
	}

	public void setUpdateInterval(long updateInterval) {
		this.updateInterval = updateInterval;
	}

	@Override
	public void start() {
		setBounds(0, 0, scaleCoordinate(VisualizationInjector.getWorldX()),
				scaleCoordinate(VisualizationInjector.getWorldY()));

		VisualizationInjector.injectComponent("Topology Link Visualization", 1, this);
		VisualizationInjector.addInteractionListener(new NodeVisInteractionListener() {

			@Override
			public void onHostClick(long hostID, boolean isActive) {
				Monitor.log(TopologyLinkVisualizationAnalyzer.class, Level.INFO, "Host clicked: %d [active=%b]", hostID,
						isActive);
			}
		});

		this.loadLinks();

		Simulator.getScheduler().scheduleIn(100 * Simulator.MILLISECOND_UNIT, new EventHandler() {

			@Override
			public void eventOccurred(Object content, int type) {

				// refresh the topology
				for (MacAddress mac : macAddresses) {
					topologyView.getNeighbors(mac);
				}

				// re-schedule the event
				Simulator.getScheduler().scheduleIn(updateInterval, this, null, 0);
			}
		}, null, 0);

		this.timeOfNextScreenshot = Simulator.getCurrentTime() + this.screenshotIntervalInSimulationTimeUnits;
	}

	private void loadLinks() {
		List<SimHost> hosts = GlobalOracle.getHosts();
		this.macAddresses = new ArrayList<MacAddress>(hosts.size());
		this.macToHost.clear();
		for (final SimHost host : hosts) {
			MacAddress macAddr = host.getLinkLayer().getMac(this.phyType).getMacAddress();
			macAddresses.add(macAddr);

			// remember position vectors
			this.macToPosition.put(macAddr, host.getTopologyComponent().getRealPosition());
			this.macToHost.put(macAddr, host);
		}
		try {
			// this.topologyView =
			// GlobalOracle.getTopology(LogicalWiFiTopology.class,
			// getInputTopologyIdentifier())
			this.topologyView = Binder.getComponent(Topology.class).getTopologyView(this.phyType);
		} catch (final ComponentNotAvailableException e) {
			throw new AssertionError("Should never happen...", e);
		}

		LinkedList<Link> links = new LinkedList<Link>();
		for (int i = 0; i < macAddresses.size(); i++) {
			for (int j = 0; j < macAddresses.size(); j++) {
				if (i != j) {
					Link l = topologyView.getLinkBetween(macAddresses.get(i), macAddresses.get(j));
					links.add(l);
				}
			}
		}
		this.links = links;
	}

	@Override
	public void stop(Writer output) {
		// nop
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		final Graphics2D g2d = Graphics2D.class.cast(g);
		if (this.links != null) {
			for (Link link : this.links) {
				drawLink(g2d, link);
			}
			for (MacAddress macAddress : this.macAddresses) {
				drawNode(g2d, macAddress);
			}
		}

		if (shallTakeScreenshot()) {
			takeScreenshot();
			this.timeOfNextScreenshot += this.screenshotIntervalInSimulationTimeUnits;
		}
	}

	private boolean shallTakeScreenshot() {
		return this.screenshotOutputFolder != null && Simulator.getCurrentTime() > 0
				&& this.screenshotIntervalInSimulationTimeUnits != NO_SCREENSHOTS_INTERVAL
				&& Simulator.getCurrentTime() >= this.timeOfNextScreenshot;
	}

	private void takeScreenshot() {
		final double scaleCorrection = 0.5; // Corresponds to enlarging the
											// image by 1/scaleCorrection
		this.currentScale *= scaleCorrection;
		final int scaledImageWidth = (int) (getWidth() / scaleCorrection);
		final int scaledImageHeight = (int) (getHeight() / scaleCorrection);
		BufferedImage img = new BufferedImage(scaledImageWidth, scaledImageHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D) img.getGraphics();
		g2d.setBackground(Color.WHITE);
		g2d.setColor(Color.BLACK);
		g2d.drawRect(0, 0, scaledImageWidth, scaledImageHeight);
		if (this.links != null) {
			for (Link link : this.links) {
				drawLink(g2d, link);
			}
			for (MacAddress macAddress : this.macAddresses) {
				drawNode(g2d, macAddress);
			}

		}
		try {
			final TopologyControlComponent tcComponent = ComponentFinder.findTopologyControlComponent();
			final String algorithmName = tcComponent.getConfiguration().topologyControlAlgorithmID.getName();

			final File outputFolder = new File(screenshotOutputFolder,
					String.format("screenshots_%s_a=%s", getSimulationIdentifier(), algorithmName));
			outputFolder.mkdirs();

			final String filename = String.format("screenshot_t=%s.%s", getFormattedSimulationTime(),
					this.screenshotFormat);

			ImageIO.write(img, screenshotFormat, new File(outputFolder, filename));
		} catch (IOException | IllegalStateException e) {
			e.printStackTrace();
		}
		this.currentScale /= scaleCorrection;
	}

	private String getSimulationIdentifier() {
		return new SimpleDateFormat("YYYY-MM-DD'T'HH-mm-ss").format(new Date(Simulator.getStartTime()));
	}

	private String getFormattedSimulationTime() {
		final long time = Simulator.getCurrentTime();
		return Time.getHours(time) + "h" + Time.getSimMinutes(time) % 60 + "m" + Time.getSimSeconds(time) % 60 + "s"
				+ Time.getSimMilliSeconds(time) % 1000 + "ms";
	}

	private boolean isOffline(MacAddress mac) {
		return emptyNodes.contains(mac);
	}

	private void drawLink(Graphics2D g2, Link link) {
		if (link.isConnected() && !isOffline(link.getSource()) && !isOffline(link.getDestination())) {
			PositionVector sourcePos = this.macToPosition.get(link.getSource());
			PositionVector destPos = this.macToPosition.get(link.getDestination());

			g2.setColor(linkColor);
			g2.drawLine(scaleCoordinate(sourcePos.getX()), //
					scaleCoordinate(sourcePos.getY()), //
					scaleCoordinate(destPos.getX()), //
					scaleCoordinate(destPos.getY()));
			drawArrowHead(g2, new Point(scaleCoordinate(destPos.getX()), scaleCoordinate(destPos.getY())),
					new Point(scaleCoordinate(sourcePos.getX()), scaleCoordinate(sourcePos.getY())), linkColor);
		}
	}

	private void drawNode(Graphics2D g2d, MacAddress macAddress) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		final PositionVector sourcePos = this.macToPosition.get(macAddress);
		final int x = scaleCoordinate(sourcePos.getX());
		final int y = scaleCoordinate(sourcePos.getY());
		// final String str = link.getSource().toString();
		final String str = this.macToHost.get(macAddress).getId().toString();

		final FontMetrics fm = g2d.getFontMetrics();
		final Rectangle2D rect = fm.getStringBounds(str, g2d);
		g2d.setColor(NODE_LABEL_BACKGROUND);
		g2d.fillRect(x, y - fm.getAscent(), (int) rect.getWidth(), (int) rect.getHeight());

		if (this.emptyNodes.contains(macAddress)) {
			g2d.setColor(Color.GRAY);
			g2d.setBackground(Color.GRAY);
		} else {
			g2d.setColor(Color.BLACK);
			g2d.setBackground(Color.BLACK);
		}
		g2d.drawOval(x, y, 5, 5);

		g2d.drawString(str, x, y);
	}

	private int scaleCoordinate(final double x) {
		return (int) (x / this.currentScale);
	}

	/*
	 * Source: http://www.coderanch.com/t/340443/GUI/java/Draw-arrow-head-line
	 */
	private void drawArrowHead(Graphics2D g2, Point tip, Point tail, Color color) {
		double phi = Math.toRadians(40);
		int barb = 10;

		g2.setPaint(color);
		double dy = tip.y - tail.y;
		double dx = tip.x - tail.x;
		double theta = Math.atan2(dy, dx);
		// System.out.println("theta = " + Math.toDegrees(theta));
		double x, y, rho = theta + phi;
		for (int j = 0; j < 2; j++) {
			x = tip.x - barb * Math.cos(rho);
			y = tip.y - barb * Math.sin(rho);
			g2.draw(new Line2D.Double(tip.x, tip.y, x, y));
			rho = theta - phi;
		}
	}

	@Override
	public void consumeEnergy(SimHost host, double energy, EnergyComponent consumer, EnergyState energyState) {
		// nop
	}

	@Override
	public void batteryIsEmpty(SimHost host) {
		MacAddress macAddress = host.getLinkLayer().getMac(this.phyType).getMacAddress();
		emptyNodes.add(macAddress);
	}

	@Override
	public void highPowerMode(SimHost host, long time, double consumedEnergy, EnergyComponent component) {
		// nop
	}

	@Override
	public void lowPowerMode(SimHost host, long time, double consumedEnergy, EnergyComponent component) {
		// nop
	}

	@Override
	public void tailMode(SimHost host, long time, double consumedEnergy, EnergyComponent component) {
		// nop
	}

	@Override
	public void offMode(SimHost host, long time, double consumedEnergy, EnergyComponent component) {
		// nop
	}
}
