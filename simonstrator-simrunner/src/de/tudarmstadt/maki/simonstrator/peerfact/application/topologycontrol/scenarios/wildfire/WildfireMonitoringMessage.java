package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.wildfire;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.TopologyControlEvaluationMessage;

/**
 * A {@link WildfireMonitoringMessage} contains a list of
 * {@link WildfireMonitoringMeasurement}s
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class WildfireMonitoringMessage extends TopologyControlEvaluationMessage {

	private static final long serialVersionUID = 2294478159791660543L;

	private final List<WildfireMonitoringMeasurement> measurements;

	public WildfireMonitoringMessage(final Collection<WildfireMonitoringMeasurement> measurements) {
		this.measurements = new ArrayList<>(measurements);
	}

	@Override
	public long getSize() {
		return this.measurements.size() * getSizeContributionOfWildfireMonitoringMeasurement();
	}

	@Override
	public Message getPayload() {
		return null;
	}

	/**
	 * Returns the {@link WildfireMonitoringMeasurement}s contained in this
	 * message
	 * 
	 * @return
	 */
	public Collection<WildfireMonitoringMeasurement> getMeasurements() {
		return this.measurements;
	}

	/**
	 * Returns the size of a single measurement in Byte
	 */
	public static int getSizeContributionOfWildfireMonitoringMeasurement() {
		/*
		 * sizeof(String) w. max. 255 chars sizeof(long) = 8 sizeof(double) = 8
		 */
		return 255 + 8 + 8;
	}
}
