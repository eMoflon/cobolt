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

/**
 * A key-list pair metric.
 * 
 * @author Björn Richerzhagen
 */
@Entity
@Table(name = "measurements_pairlist", indexes = { @Index(columnList = "id", name = "id") })
public class MeasurementPairList {
	/**
	 * A unique Id, will be set by the database
	 */
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
	 * It is called "thekey", because "key" is a (My?)SQL keyword and apparently
	 * hibernate is unable to escape it.
	 */
	@Column(nullable = false)
	private int thekey;

	/**
	 * The number of values for this measurement
	 */
	@Column(name = "[count]")
	private int count;

	/**
	 * The sum of all values for this measurement
	 */
	@Column(nullable = true, name = "[sum]")
	private Double sum;

	/**
	 * The square sum of all values for this measurement
	 */
	@Column(nullable = true)
	private Double sum2;

	/**
	 * The minimum of all values for this measurement
	 */
	@Column(nullable = true, name = "[min]")
	private Double min;

	/**
	 * The maximum of all values for this measurement
	 */
	@Column(nullable = true, name = "[max]")
	private Double max;

	/**
	 * The mapping Object of this measurement to the {@link Metric}-Object,
	 * which describes this metric.
	 */
	@ManyToOne
	@JoinColumn(name = "hostMetricId")
	@OnDelete(action = OnDeleteAction.CASCADE)
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
	public MeasurementPairList(long time, int key, int count, double sum,
			double sum2, double min, double max, HostMetric hostMetric) {
		super();
		this.thekey = key;
		this.time = time;
		this.count = count;
		this.hostMetric = hostMetric;

		// check for infinity or NaN
		this.sum = checkForSpecialNumbers(sum);
		this.sum2 = checkForSpecialNumbers(sum2);
		this.min = checkForSpecialNumbers(min);
		this.max = checkForSpecialNumbers(max);
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
