package de.tudarmstadt.maki.simonstrator.tc.analyzer.writer;

import java.util.HashMap;
import java.util.Map;

public class MapPropertyWriter implements PropertyWriter {

	private Map<String, Object> properties = new HashMap<String, Object>();

	@Override
	public void writeProperty(String k, double value) {
		properties.put(k, value);
	}

	@Override
	public void writeProperty(String k, boolean value) {
		properties.put(k, value);
	}

	@Override
	public void writeComment(String comment) {
		// not supported
	}

}
