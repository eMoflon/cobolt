package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.analyzer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class ConsoleWriter extends Writer {

	Writer out;

	public ConsoleWriter() {
		out = new BufferedWriter(new OutputStreamWriter(System.out));
	}

	@Override
	public void close() throws IOException {
		out.close();
	}

	@Override
	public void flush() throws IOException {
		out.flush();
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		out.write(cbuf, off, len);
	}

}
