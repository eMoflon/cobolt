package de.tudarmstadt.maki.simonstrator.tc.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Java implementation for tee'ing an {@link OutputStream}
 *
 * Thanks to https://stackoverflow.com/a/1994721
 *
 * @author Carlos Heuberger - Initial implementation
 * @author Roland Kluge - Added to Simonstrator
 *
 */
public class TeePrintStream extends PrintStream {
	private final PrintStream second;

	public TeePrintStream(OutputStream main, PrintStream second) {
		super(main);
		this.second = second;
	}

	/**
	 * Closes the main stream. The second stream is just flushed but <b>not</b>
	 * closed.
	 *
	 * @see java.io.PrintStream#close()
	 */
	@Override
	public void close() {
		// just for documentation
		super.close();
	}

	@Override
	public void flush() {
		super.flush();
		second.flush();
	}

	@Override
	public void write(byte[] buf, int off, int len) {
		super.write(buf, off, len);
		second.write(buf, off, len);
	}

	@Override
	public void write(int b) {
		super.write(b);
		second.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		super.write(b);
		second.write(b);
	}
}