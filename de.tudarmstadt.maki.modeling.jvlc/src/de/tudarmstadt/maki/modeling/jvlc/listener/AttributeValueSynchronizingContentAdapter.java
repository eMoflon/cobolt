package de.tudarmstadt.maki.modeling.jvlc.listener;

import org.eclipse.emf.ecore.EAttribute;

import de.tudarmstadt.maki.modeling.graphmodel.DoubleAttribute;
import de.tudarmstadt.maki.modeling.graphmodel.Edge;
import de.tudarmstadt.maki.modeling.graphmodel.Node;
import de.tudarmstadt.maki.modeling.graphmodel.ObjectAttribute;
import de.tudarmstadt.maki.modeling.graphmodel.listener.GraphContentAdapter;
import de.tudarmstadt.maki.modeling.jvlc.AttributeNames;
import de.tudarmstadt.maki.modeling.jvlc.JvlcPackage;
import de.tudarmstadt.maki.modeling.jvlc.KTCLink;
import de.tudarmstadt.maki.modeling.jvlc.KTCNode;
import de.tudarmstadt.maki.modeling.jvlc.LinkState;

/**
 * This class handles the synchronization of the kTC-specific attributes and the generic attributes defined in the graph model interface.
 *
 * For instance, if the link state is changed via {@link KTCLink#setState(LinkState)}, the corresponding generic attribute is automatically set via {@link Edge#setDoubleAttribute(String, double)} (key = {@link AttributeNames#ATTR_STATE}).
 */
public class AttributeValueSynchronizingContentAdapter extends GraphContentAdapter {

	@Override
	protected void nodeAttributeChanged(final Node node, final EAttribute attribute, final Object oldValue) {
		super.nodeAttributeChanged(node, attribute, oldValue);

		switch (attribute.getFeatureID()) {
		case JvlcPackage.KTC_NODE__REMAINING_ENERGY:
			final double oldDoubleValue = node.getDoubleAttribute(AttributeNames.ATTR_REMAINING_ENERGY);
			final Double newValue = (Double) node.eGet(attribute);
			if (newValue != null && oldDoubleValue != newValue) {
				node.setDoubleAttribute(AttributeNames.ATTR_REMAINING_ENERGY, newValue);
			}
			break;
		}

	}

	@Override
	protected void edgeAttributeChanged(final Edge edge, final EAttribute attribute, final Object oldValue) {
		super.edgeAttributeChanged(edge, attribute, oldValue);

		switch (attribute.getFeatureID()) {
		case JvlcPackage.KTC_LINK__STATE:
			final LinkState oldState = (LinkState) edge.getObjectAttribute(AttributeNames.ATTR_STATE);
			final LinkState newState = (LinkState) edge.eGet(attribute);
			if (!newState.equals(oldState)) {
				edge.setObjectAttribute(AttributeNames.ATTR_STATE, newState);
			}
			break;
		case JvlcPackage.KTC_LINK__DISTANCE:
			final double oldDistance = edge.getDoubleAttribute(AttributeNames.ATTR_DISTANCE);
			final Double newDistance = (Double) edge.eGet(attribute);
			if (newDistance != null && oldDistance != newDistance) {
				edge.setDoubleAttribute(AttributeNames.ATTR_DISTANCE, newDistance);
			}
			break;
		case JvlcPackage.KTC_LINK__REQUIRED_TRANSMISSION_POWER:
			final double oldTransmissionPower = edge.getDoubleAttribute(AttributeNames.ATTR_REQUIRED_TRANSMISSION_POWER);
			final Double newTransmissionPower = (Double) edge.eGet(attribute);
			if (newTransmissionPower != null && oldTransmissionPower != newTransmissionPower) {
				edge.setDoubleAttribute(AttributeNames.ATTR_REQUIRED_TRANSMISSION_POWER, newTransmissionPower);
			}
			break;
		}
	}

	@Override
	protected void doubleAttributeChanged(final DoubleAttribute attribute, final Double oldDoubleValue) {
		super.doubleAttributeChanged(attribute, oldDoubleValue);
		if (attribute.getElement() instanceof KTCNode) {
			final KTCNode node = (KTCNode) attribute.getElement();

			switch (attribute.getKey()) {
			case AttributeNames.ATTR_REMAINING_ENERGY:
				final Double oldRemainingEnergy = node.getRemainingEnergy();
				final Double newRemainingEnergy = attribute.getValue();
				if (!newRemainingEnergy.equals(oldRemainingEnergy)) {
					node.setRemainingEnergy(newRemainingEnergy);
				}
				break;
			}
		}

		if (attribute.getElement() instanceof KTCLink) {
			final KTCLink link = (KTCLink) attribute.getElement();
			switch (attribute.getKey()) {
			case AttributeNames.ATTR_DISTANCE:
				final double oldDistance = link.getDistance();
				final double newDistance = attribute.getValue();
				if (newDistance != oldDistance) {
					link.setDistance(newDistance);
				}
				break;
			case AttributeNames.ATTR_REQUIRED_TRANSMISSION_POWER:
				final double oldRequiredTransmissionPower = link.getRequiredTransmissionPower();
				final double newRequiredTransmissionPower = attribute.getValue();
				if (newRequiredTransmissionPower != oldRequiredTransmissionPower) {
					link.setRequiredTransmissionPower(newRequiredTransmissionPower);
				}
				break;
			}
		}
	}

	@Override
	protected void objectAttributeChanged(final ObjectAttribute attribute, final Object oldValue) {
		super.attributeChanged(attribute, oldValue);

		if (attribute.getElement() instanceof KTCLink) {
			final KTCLink link = (KTCLink) attribute.getElement();
			switch (attribute.getKey()) {
			case AttributeNames.ATTR_STATE:
				final LinkState oldState = link.getState();
				final LinkState newState = (LinkState) attribute.getValue();
				if (newState == null || !newState.equals(oldState)) {
					link.setState(newState);
				}
				break;
			}
		}

	}
}
