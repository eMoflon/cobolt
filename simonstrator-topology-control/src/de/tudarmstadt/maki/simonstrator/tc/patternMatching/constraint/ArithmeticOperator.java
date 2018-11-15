package de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint;

public enum ArithmeticOperator {
	MINUS("-"), PLUS("+"), MULTIPLY("*"), DIVIDE("/");

	private final String description;

	private ArithmeticOperator(final String description) {
		this.description = description;
	}

	/**
	 * Applies this operator to the given arguments
	 */
	public double evaluate(final Number lhs, final Number rhs) {
		switch (this) {
		case MINUS:
			return lhs.doubleValue() - rhs.doubleValue();
		case PLUS:
			return lhs.doubleValue() + rhs.doubleValue();
		case MULTIPLY:
			return lhs.doubleValue() * rhs.doubleValue();
		case DIVIDE:
			return lhs.doubleValue() / rhs.doubleValue();
		default:
			throw new UnsupportedOperationException("Don't know how to evaluate operator " + this);
		}

	}

	public static ArithmeticOperator fromString(final String str) {
		switch (str) {
		case "+":
			return PLUS;
		case "-":
			return MINUS;
		case "*":
			return MULTIPLY;
		case "/":
			return DIVIDE;
		default:
			throw new IllegalArgumentException("Cannot translate '" + str + "' into an operator");
		}
	}

	@Override
	public String toString() {
		return this.description;
	}

}
