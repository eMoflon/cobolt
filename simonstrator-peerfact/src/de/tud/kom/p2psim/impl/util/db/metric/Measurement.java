package de.tud.kom.p2psim.impl.util.db.metric;

import javax.persistence.CascadeType;
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
 * @author Christoph Muenker
 * @author Andreas Hemel
 */
@Entity
@Table(name = "measurements", indexes = { @Index(columnList = "id", name = "id") })
public class Measurement implements HostMetricBound {
	/**
	 * The id of this table
	 */
	@SuppressWarnings("unused")
	@Id
	@GeneratedValue
	private int id;

	/**
	 * The simulation time for to this measurement in simulator time, that is, microseconds.
	 */
	@SuppressWarnings("unused")
	private long time;

	/**
	 * The number of values for this measurement
	 */
	@SuppressWarnings("unused")
	@Column(name = "[count]") 
	private int count;

	/**
	 * The sum of all values for this measurement
	 */
	@SuppressWarnings("unused")
	@Column(nullable = true, name = "[sum]")
	private Double sum;

	/**
	 * The square sum of all values for this measurement
	 */
	@SuppressWarnings("unused")
	@Column(nullable = true)
	private Double sum2;

	/**
	 * The minimum of all values for this measurement
	 */
	@SuppressWarnings("unused")
	@Column(nullable = true, name = "[min]")
	private Double min;

	/**
	 * The maximum of all values for this measurement
	 */
	@SuppressWarnings("unused")
	@Column(nullable = true, name = "[max]")
	private Double max;

	/**
	 * The mapping Object of this measurement to the {@link Metric}-Object,
	 * which describes this metric.
	 */
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "hostMetricId")
	HostMetric hostMetric;

	/**
	 * Creates a {@link Measurement}-Object with the given parameters. If sum,
	 * sum2, min and max have the value infinity or NaN, then will be set this
	 * value to null.
	 *
	 * @param time
	 *            The simulation time for to this measurement as Date
	 * @param count
	 *            The number of values for this measurement
	 * @param sum
	 *            The sum of all values for this measurement
	 * @param sum2
	 *            The square sum of all values for this measurement
	 * @param min
	 *            The minimum of all values for this measurement
	 * @param max
	 *            The maximum of all values for this measurement
	 * @param hostMetric
	 *            The reference to the {@link HostMetric}-Object, which describes
	 *            this metric. Is used for the mapping.
	 */
	public Measurement(long time, int count, double sum, double sum2,
			double min, double max, HostMetric hostMetric) {
		super();
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

    public HostMetric getHostMetric() {
        return hostMetric;
    }

    @Override
    public void setHostMetric(HostMetric metric) {
        this.hostMetric = metric;
    }
}
