package de.tudarmstadt.maki.simonstrator.tc.events.dto;

import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;

public class UpdateEdgeEvent extends EdgeEvent {
	public static final String TYPE_ID = "mod-edge";
	private final String property;
	private final Object newValue;

	public UpdateEdgeEvent(final IEdge edge, final SiSType<?> property) {
		this(edge, property.getName(), edge.getProperty(property));
	}

	public UpdateEdgeEvent(final IEdge edge, final String property, final Object newValue) {
		super(TYPE_ID, edge);
		this.property = property;
		this.newValue = newValue;
	}

	public String getProperty() {
		return property;
	}

	public Object getNewValue() {
		return newValue;
	}

	@Override
	public String toString() {
		return String.format("#%d:mod-e %s.%s=%s", this.getNumber(), this.getEdgeId(), this.getProperty(), this.getNewValue());
	}
}