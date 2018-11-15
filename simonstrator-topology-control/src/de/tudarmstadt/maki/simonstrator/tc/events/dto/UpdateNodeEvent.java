package de.tudarmstadt.maki.simonstrator.tc.events.dto;

import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;

public class UpdateNodeEvent extends NodeEvent {
	public static final String TYPE_ID = "mod-node";
	private final String property;
	private final Object newValue;

	public UpdateNodeEvent(final INode node, final SiSType<?> property) {
		this(node, property.getName(), node.getProperty(property).toString());
	}

	public UpdateNodeEvent(final INode node, final String property, final String newValue) {
		super(TYPE_ID, node);
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
		return String.format("#%d:%dmod-n %s.%s=%s", this.getNumber(), this.getNodeId(), this.getProperty(), this.getNewValue());
	}
}