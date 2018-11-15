package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.wildfire;

import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;

/**
 * Represents a data point that is collected by the sensor node
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class WildfireMonitoringMeasurement {

	/**
	 * Unique identifier of the sensor node (e.g., via
	 * INode.getId().asStringValue())
	 */
	public final INodeID nodeId;
	/**
	 * Timestamp in simulation time units
	 */
	public final long timestamp;
	/**
	 * Actual measurement
	 */
	public final double measurement;

	public WildfireMonitoringMeasurement(final INodeID nodeId, final long timestamp, final double measurement) {
		this.nodeId = nodeId;
		this.timestamp = timestamp;
		this.measurement = measurement;
	}

	/**
	 * Hash code based on all attributes
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(measurement);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		return result;
	}

	/**
	 * Equality based on all attributes
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WildfireMonitoringMeasurement other = (WildfireMonitoringMeasurement) obj;
		if (Double.doubleToLongBits(measurement) != Double.doubleToLongBits(other.measurement))
			return false;
		if (nodeId == null) {
			if (other.nodeId != null)
				return false;
		} else if (!nodeId.equals(other.nodeId))
			return false;
		if (timestamp != other.timestamp)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("Node: %s, time: %s, data: %.2f", this.nodeId, Time.getFormattedTime(this.timestamp),
				this.measurement);
	}
}
