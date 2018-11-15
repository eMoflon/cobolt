package de.tudarmstadt.maki.simonstrator.tc.analyzer.writer;

public interface PropertyWriter {
	public void writeProperty(String k, double value);

	public void writeProperty(String k, boolean value);

	public void writeComment(String comment);
}
