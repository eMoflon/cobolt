package de.tudarmstadt.maki.simonstrator.tc.underlay;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphElementProperty;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IElement;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.GraphElementConstraint;

public class KTCConstraint extends GraphElementConstraint {
	
	private static final long serialVersionUID = 2155800528814526935L;

	private GraphElementProperty<? extends Number> attribute;
	private final Double k;

	public <S extends Number> KTCConstraint(final EdgeID max,
			final EdgeID lhs, final EdgeID rhs, final double k, final GraphElementProperty<S> attribute) {
		super(Arrays.asList(max, lhs, rhs));
		this.attribute = attribute;
		this.k = k;
	}

	@Override
	protected boolean checkCandidates(final Collection<? extends IElement> bindingCandidates) {
		final Iterator<? extends IElement> elementIterator = bindingCandidates.iterator();
		final Number max = elementIterator.next().getProperty(attribute);
		final Number lhs = elementIterator.next().getProperty(attribute);
		final Number rhs = elementIterator.next().getProperty(attribute);
				
		boolean b = ((max.doubleValue() >= Math.max(lhs.doubleValue(), rhs.doubleValue())) && (max.doubleValue() >= this.k * Math.min(lhs.doubleValue(), rhs.doubleValue())));
		
		return b;
	}
	
	public EdgeID getMax() {
		return (EdgeID) this.getVariables().get(0);
	}

	public EdgeID getLhs(){
		return (EdgeID) this.getVariables().get(1);
	}
	
	public EdgeID getRhs(){
		return (EdgeID) this.getVariables().get(2);
	}
	
	public double getKtcConstant(){
		return k;
	}
	
}
