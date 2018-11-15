package de.tudarmstadt.maki.simonstrator.tc.events.dto;

import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSTypes;

public class AddNodeEvent extends NodeEvent {
	public static final String TYPE_ID = "add-node";
	private final double x;
	private final double y;

	public AddNodeEvent(INode prototype) {
		super(TYPE_ID, prototype);
		final Location location = prototype.getProperty(SiSTypes.PHY_LOCATION);
		if (location != null) {
			this.x = location.getLongitude();
			this.y = location.getLatitude();
		} else {
			this.x = 0;
			this.y = 0;
		}
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	@Override
	public String toString() {
		return String.format("#%d:+n %s", this.getNumber(), this.getNodeId());
	}
}