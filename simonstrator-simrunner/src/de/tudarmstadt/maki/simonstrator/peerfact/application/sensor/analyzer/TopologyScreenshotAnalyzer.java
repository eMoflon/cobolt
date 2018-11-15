package de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.analyzer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

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
import de.tud.kom.p2psim.impl.topology.views.VisualizationTopologyView.VisualizationInjector;
import de.tud.kom.p2psim.impl.util.oracle.GlobalOracle;
import de.tudarmstadt.maki.simonstrator.api.Binder;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent.Analyzer;

public class TopologyScreenshotAnalyzer implements Analyzer, EnergyAnalyzer {

	private static final String SCREENSHOT_EXT = "png";

	private List<MacAddress> macAddresses;

	private Map<MacAddress, PositionVector> macToPosition = new HashMap<MacAddress, PositionVector>();

	private Color connectionDrawingColor = Color.BLUE;

	private PhyType phyType = PhyType.WIFI;

	private TopologyView topologyView;

	private LinkedList<Link> links;

	private Set<MacAddress> emptyNodes = new LinkedHashSet<MacAddress>();

	private int nextScreenshotIndex = 0;

	private String saveFilePattern = "screenshot_%04d.%s";

	// See also: https://xmlgraphics.apache.org/batik/using/svg-generator.html
	// See also
	// https://stackoverflow.com/questions/6575578/convert-a-graphics2d-to-an-image-or-bufferedimage
	@Override
	public void start() {

		this.loadInitialLinks();

		Simulator.getScheduler().scheduleIn(10 * Time.MINUTE, new EventHandler() {

			@Override
			public void eventOccurred(Object content, int type) {

				createImage();

				// Do not reschedule
				// Simulator.getScheduler().scheduleIn(UPDATE_INTERVAL, this,
				// null, 0);
			}
		}, null, 0);
	}

	@Override
	public void stop(Writer out) {
		// nop
	}

	@Override
	public void batteryIsEmpty(SimHost host) {
		MacAddress macAddress = host.getLinkLayer().getMac(this.phyType).getMacAddress();
		emptyNodes.add(macAddress);
	}

	private void loadInitialLinks() {
		List<SimHost> hosts = GlobalOracle.getHosts();
		this.macAddresses = new ArrayList<MacAddress>(hosts.size());
		for (SimHost host : hosts) {
			MacAddress macAddr = host.getLinkLayer().getMac(this.phyType).getMacAddress();
			macAddresses.add(macAddr);

			// remember position vectors
			this.macToPosition.put(macAddr, host.getTopologyComponent().getRealPosition());
		}
		try {
			this.topologyView = Binder.getComponent(Topology.class).getTopologyView(this.phyType);
		} catch (ComponentNotAvailableException e) {
			throw new IllegalStateException("Topology component not available");
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

	private void createImage() {
		int worldX = VisualizationInjector.getWorldX();
		int worldY = VisualizationInjector.getWorldY();
		int imageWidthInPixels = 1920;
		int imageHeightInPixels = imageWidthInPixels * worldY / worldX;
		BufferedImage image = new BufferedImage(imageWidthInPixels, imageHeightInPixels, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = (Graphics2D) image.getGraphics();
		graphics.setBackground(Color.white);
		graphics.clearRect(0, 0, imageWidthInPixels, imageHeightInPixels);
		graphics.dispose();

		if (links != null) {
			for (Link link : links) {
				visualizeLink(graphics, link);
			}
		}

		final File outputFile = new File(String.format(this.saveFilePattern, this.nextScreenshotIndex, SCREENSHOT_EXT));
		try {
			ImageIO.write(image, SCREENSHOT_EXT, outputFile);
		} catch (IOException e) {
			Monitor.log(getClass(), Level.ERROR, "Saving screenshot to %s failed: %s", outputFile, e);
			e.printStackTrace();
		}
	}

	private void visualizeLink(Graphics g, Link link) {
		Graphics2D g2 = (Graphics2D) g;

		if (link.isConnected() && !isOffline(link.getSource()) && !isOffline(link.getDestination())) {
			double scale = VisualizationInjector.getScale();

			PositionVector sourcePos = this.macToPosition.get(link.getSource());
			PositionVector destPos = this.macToPosition.get(link.getDestination());

			g2.setColor(connectionDrawingColor);
			g2.drawLine((int) (sourcePos.getX() / scale), (int) (sourcePos.getY() / scale),
					(int) (destPos.getX() / scale), (int) (destPos.getY() / scale));
			drawArrowHead(g2, new Point((int) (destPos.getX()), (int) destPos.getY()),
					new Point((int) (sourcePos.getX()), (int) sourcePos.getY()), connectionDrawingColor);
		}
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

	private boolean isOffline(MacAddress mac) {
		return emptyNodes.contains(mac);
	}

	@Override
	public void consumeEnergy(SimHost host, double energy, EnergyComponent consumer, EnergyState energyState) {
		// TODO Auto-generated method stub

	}

	@Override
	public void highPowerMode(SimHost host, long time, double consumedEnergy, EnergyComponent component) {
		// TODO Auto-generated method stub

	}

	@Override
	public void lowPowerMode(SimHost host, long time, double consumedEnergy, EnergyComponent component) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tailMode(SimHost host, long time, double consumedEnergy, EnergyComponent component) {
		// TODO Auto-generated method stub

	}

	@Override
	public void offMode(SimHost host, long time, double consumedEnergy, EnergyComponent component) {
		// TODO Auto-generated method stub

	}

}
