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

package de.tud.kom.p2psim.impl.util.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 * @author Leo Nobach
 *
 */
public class StreamCopy {

	static final int DEFAULT_BUFFER_SIZE = 2048;
	
	long bytesCopied = 0;

	private int bufferSize;

	private boolean terminated;
	
	public StreamCopy() {
		this(DEFAULT_BUFFER_SIZE);
	}
	
	public StreamCopy(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	/**
	 * Copies is to os until EOF of is
	 * @param is
	 * @param os
	 * @return if the stream was not terminated asynchronously
	 * @throws IOException
	 */
	public boolean copy(InputStream is, OutputStream os) throws IOException {
		bytesCopied = 0;
		
		byte[] buffer = new byte[bufferSize];
		
		int len; 
		while ((len = is.read(buffer)) > 0) {
			if (terminated) return false;
			os.write(buffer, 0, len);
			bytesCopied += len;
		}
		
		return true;
	}
	
	/**
	 * Here, the stream copy can be stopped asynchronously
	 */
	public void terminate() {
		terminated = true;
	}
	
	public long getBytesCopied() {
		return bytesCopied;
	}
	
}
