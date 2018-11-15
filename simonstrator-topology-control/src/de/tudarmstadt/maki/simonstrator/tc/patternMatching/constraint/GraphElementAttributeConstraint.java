package de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphElementID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphElementProperty;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IElement;

public class GraphElementAttributeConstraint extends GraphElementConstraint {

	private static final long serialVersionUID = 8245969950168432991L;
	private final ArithmeticOperator arithmeticOperator;
	private final Double value;
	private final ComparisonOperator comparisonOperator;
	private GraphElementProperty<? extends Number> lhsAttribute;
	private GraphElementProperty<? extends Number> rhsAttribute;

	public <S extends Number, T extends Number> GraphElementAttributeConstraint(final GraphElementID lhs,
			final GraphElementProperty<S> lhsAttribute, final GraphElementID rhs,
			final GraphElementProperty<T> rhsAttribute, final double value, final ComparisonOperator comparisonOperator,
			final ArithmeticOperator arithmeticOperator) {
		super(Arrays.asList(lhs, rhs));
		this.lhsAttribute = lhsAttribute;
		this.rhsAttribute = rhsAttribute;
		this.comparisonOperator = comparisonOperator;
		this.value = value;
		this.arithmeticOperator = arithmeticOperator;
	}

	public <S extends Number, T extends Number> GraphElementAttributeConstraint(final GraphElementID lhs,
			final GraphElementProperty<S> lhsAttribute, final double value,
			final ComparisonOperator comparisonOperator) {
		super(Arrays.asList(lhs));
		this.lhsAttribute = lhsAttribute;
		this.comparisonOperator = comparisonOperator;
		this.value = value;
		// Unused attributes
		this.rhsAttribute = null;
		this.arithmeticOperator = null;
	}

	public <S extends Number, T extends Number> GraphElementAttributeConstraint(final GraphElementID lhs,
			final GraphElementProperty<S> lhsAttribute, final GraphElementID rhs,
			final GraphElementProperty<T> rhsAttribute, final ComparisonOperator comparisonOperator) {
		super(Arrays.asList(lhs, rhs));
		this.lhsAttribute = lhsAttribute;
		this.rhsAttribute = rhsAttribute;
		this.comparisonOperator = comparisonOperator;
		// Unused attributes
		this.value = Double.NaN;
		this.arithmeticOperator = null;
	}

	@Override
	protected boolean checkCandidates(final Collection<? extends IElement> bindingCandidates) {
		final Iterator<? extends IElement> elementIterator = bindingCandidates.iterator();
		if (this.value.isNaN() && this.arithmeticOperator == null) {
			return this.getComparisonOperator().evaluate(elementIterator.next().getProperty(lhsAttribute),
					elementIterator.next().getProperty(rhsAttribute));
		} else if (this.rhsAttribute == null && this.arithmeticOperator == null) {
			IElement nextElement = elementIterator.next();
			return this.getComparisonOperator().evaluate(nextElement .getProperty(lhsAttribute), this.value);
		} else {
			IElement nextElement = elementIterator.next();
			IElement nextNextElement = elementIterator.next();
			return this.getComparisonOperator().evaluate(nextElement.getProperty(lhsAttribute),
					arithmeticOperator.evaluate(nextNextElement.getProperty(rhsAttribute), this.value));
		}
	}

	@Override
	public String toString() {
		return "GraphAttributeConstraint [arithmeticOperator=" + arithmeticOperator + ", value=" + value
				+ ", comparisonOperator=" + comparisonOperator + ", lhsAttribute=" + lhsAttribute + ", rhsAttribute="
				+ rhsAttribute + "]";
	}

	public ArithmeticOperator getArithmeticOperator() {
		return arithmeticOperator;
	}

	public double getValue() {
		return value;
	}

	public ComparisonOperator getComparisonOperator() {
		return comparisonOperator;
	}
}
