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

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Represents the header of a bitmap.
 * 
 * @author Andre Mink, Sebastian Kaune
 */
public class BitmapHeader {
	public int nsize; // Size of file

	public int nbisize; // Size of bitmapinfoheader

	public int nwidth; // image width

	public int nheight; // image height

	public int nplanes; // # of planes

	public int nbitcount; // bitcount

	public int ncompression; // compression method

	public int nsizeimage; // image size

	public int nxpm; // pixels per meter (x axis)

	public int nypm; // pixels per meter (y axis)

	public int nclrused; // # used colors

	public int nclrimp; // # important colors

	public BitmapHeader() {
	}

	public void read(FileInputStream fs) throws IOException {

		final int bflen = 14; // 14 byte BITMAPFILEHEADER

		byte bf[] = new byte[bflen];

		fs.read(bf, 0, bflen);

		final int bilen = 40; // 40-byte BITMAPINFOHEADER

		byte bi[] = new byte[bilen];

		fs.read(bi, 0, bilen);
		nsize = getInt(bf, 2);
		nbisize = getInt(bi, 2);
		nwidth = getInt(bi, 4);
		nheight = getInt(bi, 8);
		nplanes = getShort(bi, 12);
		nbitcount = getShort(bi, 14);
		ncompression = getInt(bi, 16);
		nsizeimage = getInt(bi, 20);
		nxpm = getInt(bi, 24);
		nypm = getInt(bi, 28);
		nclrused = getInt(bi, 32);
		nclrimp = getInt(bi, 36);
	}

	public int getInt(byte[] in, int offset) {

		int ret = ((int) in[offset + 3] & 0xff);
		ret = (ret << 8) | ((int) in[offset + 2] & 0xff);
		ret = (ret << 8) | ((int) in[offset + 1] & 0xff);
		ret = (ret << 8) | ((int) in[offset + 0] & 0xff);

		return (ret);

	}

	// build an int from a byte array - convert little to big endian
	public int getInt3(byte[] in, int offset) {

		int ret = 0xff;
		ret = (ret << 8) | ((int) in[offset + 2] & 0xff);
		ret = (ret << 8) | ((int) in[offset + 1] & 0xff);
		ret = (ret << 8) | ((int) in[offset + 0] & 0xff);

		return (ret);

	}

	// build an int from a byte array - convert little to big endian
	public long getLong(byte[] in, int offset) {

		long ret = ((long) in[offset + 7] & 0xff);
		ret |= (ret << 8) | ((long) in[offset + 6] & 0xff);
		ret |= (ret << 8) | ((long) in[offset + 5] & 0xff);
		ret |= (ret << 8) | ((long) in[offset + 4] & 0xff);
		ret |= (ret << 8) | ((long) in[offset + 3] & 0xff);
		ret |= (ret << 8) | ((long) in[offset + 2] & 0xff);
		ret |= (ret << 8) | ((long) in[offset + 1] & 0xff);
		ret |= (ret << 8) | ((long) in[offset + 0] & 0xff);

		return (ret);
	}

	// build an double from a byte array - convert little to big endian
	public double getDouble(byte[] in, int offset) {

		long ret = getLong(in, offset);

		return (Double.longBitsToDouble(ret));
	}

	// build an short from a byte array - convert little to big endian
	public static short getShort(byte[] in, int offset) {

		short ret = (short) ((short) in[offset + 1] & 0xff);
		ret = (short) ((ret << 8) | (short) ((short) in[offset + 0] & 0xff));

		return (ret);
	}
}
