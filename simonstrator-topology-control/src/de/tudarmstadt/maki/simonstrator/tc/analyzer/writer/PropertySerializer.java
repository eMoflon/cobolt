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

package de.tudarmstadt.maki.simonstrator.tc.analyzer.writer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * 
 * @author Michael Stein
 *
 */
public class PropertySerializer implements PropertyWriter {

	private Writer writer = null;

	public PropertySerializer(String path) {
		try {
			this.writer = new FileWriter(path);
		} catch (IOException e) {
			throw new IllegalStateException("Failed opening writer", e);
		}
	}

	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			throw new IllegalStateException("Failed writing into output", e);
		}
	}


	private void checkValidity(String str) {
		if (str.contains("\n") || str.contains("=")
				|| str.toString().contains("#"))
			throw new IllegalArgumentException(
					"At least one parameter contains a forbidden character");
	}

	@Override
	public void writeProperty(String k, double value) {
		checkValidity(k + value);

		try {
			writer.write(k + "=" + value + "\n");
		} catch (IOException e) {
			throw new IllegalStateException("unable to write property", e);
		}
	}

	@Override
	public void writeProperty(String k, boolean value) {
		checkValidity(k + value);

		try {
			writer.write(k + "=" + value + "\n");
		} catch (IOException e) {
			throw new IllegalStateException("unable to write property", e);
		}
	}

	@Override
	public void writeComment(String comment) {
		checkValidity(comment);
		
		try {
			writer.write("#" + comment + "\n");
		} catch (IOException e) {
			throw new IllegalStateException("unable to write property", e);
		}
	}
}
