package de.tudarmstadt.maki.simonstrator.tc.events.io;

import de.tudarmstadt.maki.simonstrator.tc.events.dto.Event;

public class UpdateWorldSizeEvent extends Event {

	private final double worldSizeX;
	private final double worldSizeY;

	public UpdateWorldSizeEvent(final double worldSizeX, final double worldSizeY) {
		super("update-world-size");
		this.worldSizeX = worldSizeX;
		this.worldSizeY = worldSizeY;
	}

	public double getWorldSizeX() {
		return worldSizeX;
	}

	public double getWorldSizeY() {
		return worldSizeY;
	}

	@Override
	public String toString() {
		return String.format("#%d:%s %.1f %.1f", this.getNumber(), this.getType(), this.getWorldSizeX(), this.getWorldSizeY());
	}

}
