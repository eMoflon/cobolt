package de.tud.kom.p2psim.impl.network.modular.st.latency;

import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.impl.network.AbstractNetLayer;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;
import de.tud.kom.p2psim.impl.network.modular.st.LatencyStrategy;
import de.tudarmstadt.maki.simonstrator.api.Time;

/** Applies a latency derived from the geographical distance of two hosts.
 * The latency is. 31ms * 0.01ms/km * distance between the hosts in km.
 *
 * Based on GeographicalLatency.
 *
 * @author Andreas Hemel
 *
 */
public class FootprintLatency implements LatencyStrategy {

	protected static final double geoDistFactor = 0.00001d;
	protected static final long staticPart = 31;

	@Override
	public long getMessagePropagationDelay(
			NetMessage msg,
			AbstractNetLayer nlSender,
			AbstractNetLayer nlReceiver,
			NetMeasurementDB db) {

		if (db != null) {
			throw new IllegalArgumentException("FootprintLatency is incompatible with the NetMeasurementDB");
		}

		double distance = nlSender.getNetPosition()
				.distanceTo(nlReceiver.getNetPosition());

		return (staticPart + Math.round(geoDistFactor * distance))
				* Time.MILLISECOND;
	}

	@Override
	public void writeBackToXML(BackWriter bw) {
		//throw new UnsupportedOperationException();
	}
}
