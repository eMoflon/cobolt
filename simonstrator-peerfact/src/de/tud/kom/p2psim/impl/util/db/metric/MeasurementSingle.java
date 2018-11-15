package de.tud.kom.p2psim.impl.util.db.metric;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/** Statistical representation of a series of measurements in the database.
 *
 * This class is a POJO that maps to a table in a database. Instances
 * of this class become rows in the corresponding table when persisted.
 *
 * This class has the task to store information about a single value
 * measurements, that is, only a single value is logged at the measurement
 * time.
 *
 * @author Christoph Muenker
 * @author Andreas Hemel
 */
@Entity
@Table(name = "measurements_single", indexes = {
		@Index(columnList = "time", name = "time"),
		@Index(columnList = "hostMetricId", name = "hostMetricId") })
public class MeasurementSingle {
	/**
	 * A unique Id, will be set by the database
	 */
	@SuppressWarnings("unused")
	@Id
	@GeneratedValue()
	private int id;

	/**
	 * The simulation time of this measurement
	 */
	@SuppressWarnings("unused")
	private long time;

	/**
	 * The value for this measurement
	 */
	@SuppressWarnings("unused")
	@Column(nullable = true)
	private Double value;

	/**
	 * The mapping Object of this measurement to the {@link Metric}-Object,
	 * which describes this metric.
	 */
	@SuppressWarnings("unused")
	@ManyToOne
	@JoinColumn(name = "hostMetricId")
	private HostMetric hostMetric;

	/**
	 * Creates a {@link MeasurementSingle}-Object with the given parameters. If
	 * value has the value infinity or NaN, then will be set this value to null.
	 *
	 * @param time
	 *            The simulation time as date to this measurement
	 * @param value
	 *            The value for this measurement
	 * @param statistic
	 *            The reference to the {@link Metric}-Object, which describes
	 *            this metric. Is used for the mapping.
	 */
	public MeasurementSingle(long time, Double value, HostMetric hostMetric) {
		super();
		this.time = time;
		this.hostMetric = hostMetric;

		// check for infinity or NaN
		this.value = checkForSpecialNumbers(value);
	}

	/**
	 * Check for special numbers, like infinity or NaN. If the given value is
	 * equals this numbers then will be return null, otherwise will be returned
	 * the given value.
	 *
	 * @param value
	 *            The value, which should be checked.
	 * @return The value or null, if it is a special number.
	 */
	private Double checkForSpecialNumbers(Double value) {
		if (value == null)
			return value;
		if (value.equals(Double.NEGATIVE_INFINITY)
				|| value.equals(Double.POSITIVE_INFINITY)
				|| value.equals(Double.NaN)) {
			return null;
		} else {
			return value;
		}
	}
}
