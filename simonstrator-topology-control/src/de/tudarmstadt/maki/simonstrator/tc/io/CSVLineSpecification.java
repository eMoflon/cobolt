package de.tudarmstadt.maki.simonstrator.tc.io;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class stores a CSV line.
 * 
 * New columns can be added via {@link #addSpecification(String, Object)} and a
 * formatted CSV line can be obtained via {@link #format()}.
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class CSVLineSpecification {
	public static final String DEFAULT_CSV_SEPARATOR = ";";
	private final List<String> formatSpecifiers = new ArrayList<>();
	private final List<Object> objects = new ArrayList<>();
	private final int expectedLength;
	private final String fieldSeparator;

	/**
	 * Empty CSV line with given expected length and field separator for
	 * formatting
	 * 
	 * @param expectedLength
	 *            the expected length
	 * @param fieldSeparator
	 *            the field separator
	 */
	public CSVLineSpecification(final int expectedLength,
			String fieldSeparator) {
		this.expectedLength = expectedLength;
		this.fieldSeparator = fieldSeparator;
	}

	/**
	 * Empty CSV line with default field separator
	 * ({@link #DEFAULT_CSV_SEPARATOR})
	 * 
	 * @param expectedLength
	 *            the expected length
	 */
	public CSVLineSpecification(final int expectedLength) {
		this(expectedLength, DEFAULT_CSV_SEPARATOR);
	}

	/**
	 * Copy constructor
	 * 
	 * Invokes {@link #addSpecification(String, Object)} for each entry of the
	 * given {@link CSVLineSpecification}
	 * 
	 * @param other
	 *            the {@link CSVLineSpecification} to copy
	 */
	public CSVLineSpecification(final CSVLineSpecification other) {
		this(other.expectedLength, other.fieldSeparator);
		for (int i = 0; i < other.formatSpecifiers.size(); ++i) {
			this.addSpecification(other.formatSpecifiers.get(i),
					other.objects.get(i));
		}
	}

	/**
	 * Adds a field to this specification
	 * 
	 * @param format
	 *            the format specifier for the value
	 * @param value
	 *            the value to be put in the field
	 * 
	 * @throws IllegalArgumentException
	 *             if the format specifier is inapplicable to the given value
	 */
	public void addSpecification(final String format, final Object value) {
		validateCompatibility(format, value);

		this.formatSpecifiers.add(format);
		this.objects.add(value);
	}

	/**
	 * Formats the stored fields as CSV line, using the configured CSV field
	 * separator ({@link #getFieldSeparator()}.
	 * 
	 * @return
	 * @throws IllegalStateException
	 *             if the number of {@link #addSpecification(String, Object)}
	 *             calls is not equal to {@link #getExpectedLength()}.
	 */
	public String format() {
		if (this.objects.size() != this.expectedLength)
			throw new IllegalStateException(
					String.format("Expected length: %d. Actual length: %d",
							this.expectedLength, this.objects.size()));

		final Object[] objectArray = objects
				.toArray(new Object[]{objects.size()});

		return String.format(StringUtils.join(formatSpecifiers, fieldSeparator),
				objectArray);
	}

	/**
	 * Returns the object at the given index
	 * 
	 * 0 <= index < {@link #getLength()}
	 * 
	 * @param index
	 *            index
	 * @return the object
	 */
	public Object getValue(final int index) {
		validateIndex(index);
		return this.objects.get(index);
	}

	/**
	 * Returns the expected (final) length of the line.
	 */
	public int getExpectedLength() {
		return expectedLength;
	}

	/**
	 * The current length of this line
	 * 
	 * It is 0 in the beginning and should be equal to
	 * {@link #getExpectedLength()} in the end.
	 * 
	 * @return the current length of the line
	 */
	public int getLength() {
		return this.objects.size();
	}

	public String getFieldSeparator() {
		return fieldSeparator;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("CSV line [");
		for (int i = 0; i < this.formatSpecifiers.size(); ++i) {
			sb.append(String.format(formatSpecifiers.get(i),
					this.objects.get(i)));
			sb.append(", ");
		}
		sb.append("]");
		return sb.toString();
	}

	public JSONArray convertToJsonArray(CSVLineSpecification csvHeader) {
		if (this.objects.size() != this.expectedLength)
			throw new IllegalStateException(
					String.format("Expected length: %d. Actual length: %d",
							this.expectedLength, this.objects.size()));

		final JSONArray jsonArray = new JSONArray();

		for (int i = 0; i < this.objects.size(); ++i) {
			final JSONObject jsonEntry = new JSONObject()
					.put(csvHeader.getValue(i).toString(), String.format(
							this.formatSpecifiers.get(i), this.objects.get(i)));
			jsonArray.put(jsonEntry);
		}

		return jsonArray;
	}

	/**
	 * Updates the value for the given index. The list of values must have
	 * 
	 * @param index
	 *            the index
	 * @param newValue
	 *            the new value
	 */
	public void updateSpecification(final int index, final Object newValue) {
		validateIndex(index);
		validateCompatibility(this.formatSpecifiers.get(index), newValue);
		this.objects.set(index, newValue);
	}

	/**
	 * Validates that the given index can be used to access/update objects
	 * 
	 * @param index
	 *            the index to be checked
	 * @throws IllegalArgumentException
	 *             if the index is invalid
	 */
	private void validateIndex(int index) {
		if (index < 0 || index >= this.getLength())
			throw new IllegalArgumentException(
					String.format("Index %d is out of bounds: [0,%d)", index,
							this.getLength()));
	}
	
	/**
	 * Validates that the given value is compatible with the given format specifier
	 * @param format the format specifier (for {@link String#format(String, Object...)}
	 * @param value the value
	 */
	private void validateCompatibility(final String format,
			final Object value) {
		final boolean isFloatingType = value instanceof Double
				|| value instanceof Float;
		final boolean isIntegerType = value instanceof Integer
				|| value instanceof Long;
		if ((format.contains("f") && !isFloatingType)
				|| format.contains("d") && !isIntegerType)
			throw new IllegalArgumentException(String.format(
					"Format %s is not applicable to value %s", format, value));
	}
}