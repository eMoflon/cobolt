package de.tud.kom.p2psim.impl.util.db.metric;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/** Statistical representation of a series of measurements in the database.
*
* This class is a POJO that maps to a table in a database. Instances
* of this class become rows in the corresponding table when persisted.
*
* This class has the task to store information about a paired value
* measurements consisting of an integer and a double value.
*
* @author Andreas Hemel
*/
@Entity
@Table(name = "measurements_pair", indexes = { @Index(columnList = "id", name = "id") })
public class MeasurementPair {
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
	 * The integer key for this measurement.
	 *
	 * It is called "the_key, because "key" is a (My?)SQL keyword and
	 * apparently hibernate is unable to escape it.
	 */
	@SuppressWarnings("unused")
	@Column(nullable = false)
	private int thekey;


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
	@OnDelete(action=OnDeleteAction.CASCADE)
	private HostMetric hostMetric;

	/**
	 * Creates a {@link MeasurementPair}-Object with the given parameters. If
	 * value has the value infinity or NaN, then will be set this value to null.
	 *
	 * @param time
	 *            The simulation time as date to this measurement
	 * @param key
	 *            The integer value for this measurement
	 * @param value
	 *            The double value for this measurement
	 * @param statistic
	 *            The reference to the {@link Metric}-Object, which describes
	 *            this metric. Is used for the mapping.
	 */
	public MeasurementPair(long time, int key, Double value, HostMetric hostMetric) {
		super();
		this.thekey = key;
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
