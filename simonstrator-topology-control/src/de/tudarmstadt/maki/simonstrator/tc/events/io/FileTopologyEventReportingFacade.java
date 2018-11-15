package de.tudarmstadt.maki.simonstrator.tc.events.io;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

public class FileTopologyEventReportingFacade extends AbstractTopologyEventRecordingFacade {
	private final File outputFile;

	private final ObjectMapper objectMapper;

	@XMLConfigurableConstructor({ "outputFile" })
	public FileTopologyEventReportingFacade(final File outputFile) {
		this.outputFile = outputFile;
		this.objectMapper = new ObjectMapper();
	}

	@Override
	public void initalize() {

	}

	@Override
	public void shutdown() {
		writeEventLog();
	}

	public void writeEventLog() {
		try {
			this.objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, getEventLog());
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to write data.", e);
		}
	}
}
