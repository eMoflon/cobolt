package de.tudarmstadt.maki.simonstrator.tc.events.analyzer;

import java.io.Writer;

import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent.Analyzer;
import de.tudarmstadt.maki.simonstrator.tc.component.TopologyControlComponent;
import de.tudarmstadt.maki.simonstrator.tc.events.io.FileTopologyEventReportingFacade;
import de.tudarmstadt.maki.simonstrator.tc.facade.ITopologyControlFacade;

/**
 * This analyzer saves the topology event log to the configured output file
 *
 * @author Roland Kluge - Initial implementation
 *
 * @see FileTopologyEventReportingFacade
 */
public class JsonTopologyEventReportingAnalyzer implements Analyzer {

	@Override
	public void start() {
		// Nothing to do
	}

	@Override
	public void stop(Writer out) {
		final TopologyControlComponent tcc = TopologyControlComponent.find();
		if (tcc != null) {
			ITopologyControlFacade eventRecordingFacade = tcc.getEventRecordingFacade();
			if (eventRecordingFacade instanceof FileTopologyEventReportingFacade) {
				((FileTopologyEventReportingFacade) eventRecordingFacade).writeEventLog();
			}
		}
	}

}
