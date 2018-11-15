package de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import de.tudarmstadt.maki.simonstrator.api.common.UniqueID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphElementProperty;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IElement;

/**
 * This constraint is fulfilled for a list of graph elements if the attribute
 * value of the first element in the list is the maximum of the attribute value
 * of all elements in the list.
 */
public class MaximumAttributeValueConstraint extends GraphElementConstraint {

	private static final long serialVersionUID = 5783794003318985046L;
	private GraphElementProperty<? extends Double> attribute;

	public MaximumAttributeValueConstraint(final List<? extends UniqueID> variables,
			final GraphElementProperty<? extends Double> attribute) {
		super(variables);
		this.attribute = attribute;
	}

	@Override
	protected boolean checkCandidates(final Collection<? extends IElement> bindingCandidates) {
		final Iterator<? extends IElement> elementsIterator = bindingCandidates.iterator();
		final IElement firstElement = elementsIterator.next();
		
		double maximum = Double.MIN_VALUE;
		for (final IElement element : bindingCandidates)
		{
			maximum = Math.max(maximum, element.getProperty(attribute).doubleValue());
		}
		
		return firstElement.getProperty(attribute) == maximum;
	}

}
