package de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint;

/**
 * Basic comparison operators
 */
public enum ComparisonOperator {

	GREATER(">"), GREATER_OR_EQUAL(">="), EQUAL("=="), NOT_EQUAL("!="), LESS_OR_EQUAL("<="), LESS("<");

	private static final double EPSILON = 1e-12;

	private final String description;

	private ComparisonOperator(final String description) {
		this.description = description;
	}

	public boolean evaluate(final Number lhs, final Number rhs) {
		if (lhs == null || rhs == null)
			return false;
		
		switch (this) {
		case GREATER:
			return lhs.doubleValue() > rhs.doubleValue();
		case GREATER_OR_EQUAL:
			return lhs.doubleValue() >= rhs.doubleValue();
		case EQUAL:
			return Math.abs(lhs.doubleValue() - rhs.doubleValue()) < EPSILON;
		case NOT_EQUAL:
			return lhs != rhs;
		case LESS_OR_EQUAL:
			return lhs.doubleValue() <= rhs.doubleValue();
		case LESS:
			return lhs.doubleValue() < rhs.doubleValue();
		default:
			throw new UnsupportedOperationException("Unknown operator: " + this);
		}
	}

	public static ComparisonOperator fromString(final String str) {
		switch (str) {
		case "<":
			return LESS;
		case "<=":
			return LESS_OR_EQUAL;
		case "==":
			return EQUAL;
		case "!=":
			return NOT_EQUAL;
		case ">=":
			return GREATER_OR_EQUAL;
		case ">":
			return GREATER;
		default:
			throw new IllegalArgumentException("Cannot translate string '" + str + "' into a comparison operator");
		}
	}

	@Override
	public String toString() {
		return this.description;
	}
}
