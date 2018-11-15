package de.tud.kom.p2psim.impl.util.db.metric;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/** Database representation of a metric.
 *
 * It is nearly an exact equivalent of {@link MetricDescription} but
 * additionally stores a metric type, to distinguish between single value
 * and aggregate metrics.
 *
 * This class is a POJO that maps to a table in a database. Instances
 * of this class become rows in the corresponding table when persisted.
 *
 * @author Christoph Muenker
 * @author Andreas Hemel
 */
@Entity
@Table(name = "metrics", indexes = { @Index(columnList = "id", name = "id"),
		@Index(columnList = "experimentId", name = "experimentId") })
public class Metric {
	/**
	 * The unique id for this table, which is created by the database
	 */
	@SuppressWarnings("unused")
	@Id
	@GeneratedValue
	private int id;

	/**
	 * The name of the metric
	 */
	@Column(name = "name")
	private String name;

    /**
     * The name of the analyzer,which have created this metric.
     */
	@Column(name = "analyzer")
    private String analyzerName;

	/**
	 * The type of the metric, like single or aggregate. So it can decide in
	 * which table the data are stored.
	 */
	private String type;

	/**
	 * The comment for this metric.
	 */
	@Column(length = 1024)
	private String comment;

	/**
	 * The unit to this metric.
	 */
	private String unit;

	/**
	 * Used for the mapping to an experiment.
	 */
	@ManyToOne
	@JoinColumn(name = "experimentId")
	private Experiment experiment;

	/**
	 * Creates a {@link Metric}-Object with the given parameters.
	 *
	 * @param metric
	 *            The {@link MetricDescription}, which contains information to the metric, like name, unit, comment ...
	 * @param type
	 *            The type of the metric, like single or aggregate. So it can
	 *            decide in which table the data are stored.
	 * @param experiment
	 *            Used for the mapping to an experiment.
	 */
	public Metric(MetricDescription metric, String type,
			Experiment experiment) {
		super();
		this.name = metric.getName();
        this.analyzerName = metric.getAnalyzerName();
		this.comment = metric.getComment();
		this.unit = metric.getUnit();
		this.type = type;
		this.experiment = experiment;
	}

    protected Metric() {

    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((analyzerName == null) ? 0 : analyzerName.hashCode());
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result
				+ ((experiment == null) ? 0 : experiment.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Metric))
			return false;
		Metric other = (Metric) obj;
		if (analyzerName == null) {
			if (other.analyzerName != null)
				return false;
		} else if (!analyzerName.equals(other.analyzerName))
			return false;
		if (comment == null) {
			if (other.comment != null)
				return false;
		} else if (!comment.equals(other.comment))
			return false;
		if (experiment == null) {
			if (other.experiment != null)
				return false;
		} else if (!experiment.equals(other.experiment))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (unit == null) {
			if (other.unit != null)
				return false;
		} else if (!unit.equals(other.unit))
			return false;
		return true;
	}

    @Override
    public String toString() {
        return "Metric{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
