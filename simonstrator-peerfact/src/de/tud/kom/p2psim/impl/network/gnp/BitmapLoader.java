/*
 * Copyright (c) 2005-2011 KOM - Multimedia Communications Lab
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


package de.tud.kom.p2psim.impl.network.gnp;

import java.awt.Color;
import java.awt.Point;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Reads a bitmap byte by byte into a 2-dimensional array and extracts the
 * colorTable of the bitmap. Currently, there are only 8bit bitmaps (256 colors)
 * supported.
 * 
 * @author Andre Mink, Sebastian Kaune
 */
public class BitmapLoader {
	public int[][] cartesianSpace;

	public Color[] colorTable;

	public int width;

	public int height;

	public BitmapLoader(String image) {
		try {
			FileInputStream in = new FileInputStream(image);
			read(in);
			this.width = cartesianSpace.length;
			this.height = cartesianSpace[0].length;

			String lastColor = "";
			for (int i = 0; i < colorTable.length; i++) {
				if (!lastColor.equals(colorTable[i].toString())) {
					lastColor = colorTable[i].toString();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public double[][] getDistributionFromBitmap() {
		Color c;
		double sum = 0;
		double tmp;
		double[][] distribution = new double[this.width][this.height];
		// iterate ever pixel
		for (int width = 0; width < this.width; width++) {
			for (int height = 0; height < this.height; height++) {
				// get the color for this pixel and calculate the ratio for this
				// pixel accoring to its grey value
				c = this.colorTable[this.cartesianSpace[width][height]];
				// use only grey colors
				if (c.getBlue() == c.getGreen() && c.getBlue() == c.getRed()) {
					tmp = 1.0 - (c.getBlue() / 255.0);
					distribution[width][height] = tmp;
					sum += tmp;
				} else
					distribution[width][height] = 0;
			}
		}
		// iterate ever pixel again and calculate the overall ratio
		for (int width = 0; width < this.width; width++) {
			for (int height = 0; height < this.height; height++) {
				distribution[width][height] = distribution[width][height] / sum;
			}
		}
		return distribution;
	}

	public ArrayList assignPeers(int nbPeers, double[][] distribution) {
		ArrayList al = new ArrayList(nbPeers);
		int sum = 0;
		int value = 0;
		double deviation = 0;
		int[][] result = new int[this.width][this.height];

		for (int width = 0; width < this.width; width++) {
			for (int height = 0; height < this.height; height++) {
				if (deviation > 0)
					value = (int) Math.ceil(distribution[width][height]
							* nbPeers);
				else if (deviation <= 0)
					value = (int) Math.floor(distribution[width][height]
							* nbPeers);

				sum += value;
				deviation += (distribution[width][height] * nbPeers) - value;
				result[width][height] = value;
				if (value > 0) {
					for (int i = 0; i < value; i++) {
						al.add(new Point(width, height));
					}
				}
			}
		}
		return al;
	}

	public void read(FileInputStream fs) {
		try {
			BitmapHeader bh = new BitmapHeader();
			bh.read(fs);

			if (bh.nbitcount == 24)
				this.cartesianSpace = read24BitMap(fs, bh);
			else if (bh.nbitcount == 32)
				this.cartesianSpace = read32BitMap(fs, bh);
			else if (bh.nbitcount == 8)
				this.cartesianSpace = read8BitMap(fs, bh);

			fs.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private int[][] read8BitMap(FileInputStream fs, BitmapHeader bh)
			throws IOException {
		// get the number of colors
		int nNumColors = 0;
		// # colors is in bitmap header
		if (bh.nclrused > 0) {
			nNumColors = bh.nclrused;
		}
		// calculate colors based on bitsperpixel
		else {
			nNumColors = (1 & 0xff) << bh.nbitcount;
		}

		// some images have no imagesize in the bitmap header => calculate
		if (bh.nsizeimage == 0) {
			bh.nsizeimage = ((((bh.nwidth * bh.nbitcount) + 31) & ~31) >> 3);
			bh.nsizeimage *= bh.nheight;
		}

		// get the color table
		Color colorTable[] = new Color[nNumColors];
		byte byteColorTable[] = new byte[nNumColors * 4];
		fs.read(byteColorTable, 0, nNumColors * 4);
		int colorTableIndex = 0;
		Color c;
		// Field1 = Blue, Field2 = Green, Field3 = Red, Field4 = reserved
		for (int n = 0; n < byteColorTable.length; n = n + 4) {
			c = new Color(byteColorTable[n + 2] & 0xff,
					byteColorTable[n + 1] & 0xff, byteColorTable[n] & 0xff);
			colorTable[colorTableIndex] = c;
			colorTableIndex++;
		}

		// Read the image data.
		// calculate the padding for each line
		int npad = (bh.nsizeimage / bh.nheight) - bh.nwidth;

		// cartesianSpace stores the pointers to the colorTable
		int[][] cartesianSpace = new int[bh.nwidth][bh.nheight];
		byte bdata[] = new byte[(bh.nwidth + npad) * bh.nheight];

		fs.read(bdata, 0, (bh.nwidth + npad) * bh.nheight);
		int nindex = 0;

		for (int j = 0; j < bh.nheight; j++) {
			for (int i = 0; i < bh.nwidth; i++) {
				cartesianSpace[i][j] = bdata[nindex] & 0xff;
				nindex++;
			}
			nindex += npad;
		}
		this.colorTable = colorTable;
		return cartesianSpace;
	}

	private int[][] read24BitMap(FileInputStream fs, BitmapHeader bh)
			throws IOException {
		// not supported yet
		return new int[0][0];
	}

	private int[][] read32BitMap(FileInputStream fs, BitmapHeader bh)
			throws IOException {
		// not supported yet
		return new int[0][0];
	}
}
