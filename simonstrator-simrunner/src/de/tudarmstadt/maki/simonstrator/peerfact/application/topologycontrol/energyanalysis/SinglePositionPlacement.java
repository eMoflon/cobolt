package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.energyanalysis;

import de.tud.kom.p2psim.api.topology.TopologyComponent;
import de.tud.kom.p2psim.api.topology.placement.PlacementModel;
import de.tud.kom.p2psim.impl.topology.PositionVector;

public class SinglePositionPlacement implements PlacementModel {

	private double longitude;
	private double latitude;

	public void setLatitude(final double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(final double longitude) {
		this.longitude = longitude;
	}

	@Override
	public void addComponent(TopologyComponent comp) {
		// Nop
	}

	@Override
	public PositionVector place(TopologyComponent comp) {
		return new PositionVector(this.longitude, this.latitude);
	}

}
