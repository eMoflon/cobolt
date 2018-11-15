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

package de.tud.kom.p2psim.impl.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

// TODO: Auto-generated Javadoc
/**
 * The Class CSVWriter.
 */
public class CSVWriter {
	
	/** The delimiter. */
	final private String delimiter;
	
	/** The out stream. */
	final private BufferedWriter outStream;

	/**
	 * Instantiates a new cSV writer.
	 *
	 * @param filename the filename
	 * @param delimiter the delimiter
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public CSVWriter(String filename, String delimiter) throws IOException {

		final FileWriter fstream = new FileWriter(filename);
		outStream = new BufferedWriter(fstream);
		
		this.delimiter = delimiter;

	}

	/**
	 * Adds the line.
	 *
	 * @param args the args
	 */
	public void addLine(String[] args) {

		int i = 0;
		String line = "";

		for (String string : args) {

			line += string;
			if (i < args.length - 1) {
				line += delimiter;
			}

			i++;
		}

		try {
			outStream.write(line + "\r\n");
			outStream.flush();
		} catch (IOException e) {
			throw new AssertionError(e);
		}

	}

	/**
	 * Close.
	 */
	public void close() {
		try {
			outStream.close();
		} catch (IOException e) {
			// None.
		}
	}

}
