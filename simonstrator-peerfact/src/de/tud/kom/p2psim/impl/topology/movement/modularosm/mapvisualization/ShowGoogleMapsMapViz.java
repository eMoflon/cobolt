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

package de.tud.kom.p2psim.impl.topology.movement.modularosm.mapvisualization;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.swing.JComponent;

import de.tud.kom.p2psim.impl.topology.movement.modularosm.GPSCalculation;
import de.tud.kom.p2psim.impl.topology.views.VisualizationTopologyView.VisualizationInjector;

public class ShowGoogleMapsMapViz extends JComponent implements IMapVisualization{
	
	private String tempImageFilePath;
	
	private boolean initialized = false;
	
	public ShowGoogleMapsMapViz() {
		setBounds(0, 0, VisualizationInjector.getWorldX(),
				VisualizationInjector.getWorldY());
		setOpaque(true);
		setVisible(true);	
	}
	
	private void initializeImage() {
		if(!initialized) {
			tempImageFilePath = tempImageFilePath + 
					"googlemaps" + 
					GPSCalculation.getLatCenter() + 
					GPSCalculation.getLonCenter() + 
					GPSCalculation.getZoom() + ".jpg";

			//Check if the file with same properties (same location) already exists
			File f = new File(tempImageFilePath);
			if(!f.exists()) {
				try {
		            String imageUrl = "http://maps.google.com/maps/api/staticmap?center=" + 
		            		GPSCalculation.getLatCenter() + "," + 
		            		GPSCalculation.getLonCenter() + "&zoom=" + 
		            		GPSCalculation.getZoom() + "&size=500x500&scale=2";
		            URL url = new URL(imageUrl);
		            InputStream is = url.openStream();
		            OutputStream os = new FileOutputStream(tempImageFilePath);
	
		            byte[] b = new byte[2048];
		            int length;
	
		            while ((length = is.read(b)) != -1) {
		                os.write(b, 0, length);
		            }
	
		            is.close();
		            os.close();
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
			}
			initialized = true;
		}
	}

	@Override
	public void paint(Graphics g) {
		initializeImage();
		
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
		Image imageToDraw = resize(
				Toolkit.getDefaultToolkit().getImage(tempImageFilePath),
				VisualizationInjector.getWorldX(),
				VisualizationInjector.getWorldY());
		g2.drawImage(imageToDraw , 0, 0, this);		
	}
	
	public void setTempImageFilePath(String tempImageFilePath) {
		this.tempImageFilePath = tempImageFilePath;
	}
	
	/**
	 * Resizes the given image to the given width and height
	 *
	 * @param originalImage
	 * @param width
	 * @param height
	 */
	private Image resize(Image originalImage, int width, int height) {
	    int type = BufferedImage.TYPE_INT_ARGB;
	    BufferedImage resizedImage = new BufferedImage(width, height, type);
	    Graphics2D g = resizedImage.createGraphics();

	    g.setComposite(AlphaComposite.Src);
	    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	    g.drawImage(originalImage, 0, 0, width, height, this);
	    g.dispose();
	    return resizedImage;
	}
}
