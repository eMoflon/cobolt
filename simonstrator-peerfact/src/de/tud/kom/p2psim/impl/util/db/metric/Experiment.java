package de.tud.kom.p2psim.impl.util.db.metric;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

/** Database representation of an experiment, that is, a simulator run.
 *
 * This class is a POJO that maps to a table in a database. Instances
 * of this class become rows in the corresponding table when persisted.
 *
 * This contains an unique id, the seed, the start date of this experiment,
 * the end date of this experiment, the name of this experiment, the system
 * which describes the config of the run, and the workload, which are the
 * variables for this run.
 *
 * @author Christoph Muenker
 * @version 1.0, 07/05/2011
 */
@Entity
@Table(name = "experiments", indexes = { @Index(columnList = "id", name = "id") })
public class Experiment {
	/**
	 * A unique id of this experiment, which will be set by the database.
	 */
	@Id
	@GeneratedValue
	private int id;

	/**
	 * The seed of this experiment
	 */
	private long seed;

	/**
	 * The start date of this experiment
	 */
	private Date date;

	/**
	 * The end date of this experiment
	 */
	private Date endDate;

	/**
	 * The description of this experiment (easy readable)
	 */
	@Column(length = 1023)
	private String name;

	/**
	 * The parsed XML-configuration
	 */
	@Type(type="text")
	private String system;

	/**
	 * The variables of this experiment. (The variables of the XML-Config or
	 * variables which are given as parameter by start)
	 */
	@Type(type="text")
	private String workload;

	/**
	 * Constructor for an experiment-Object. It sets the needed data, except for
	 * end date. This can first set, if the experiment is finished.
	 *
	 * @param seed
	 *            The seed for this experiment
	 * @param date
	 *            The start date for this experiment
	 * @param name
	 *            The description for this experiment (easy readable)
	 * @param system
	 *            The parsed XML-Configuration.
	 * @param workload
	 *            The variables for this experiment.
	 */
	public Experiment(long seed, Date date, String name, String system,
			String workload) {
		this.seed = seed;
		this.date = date;
		this.name = name;
		this.system = system;
		this.workload = workload;
	}
	
	public Experiment() {
		
	}

	// ****************
	// Getter and Setter for the attributes of this class.
	// ****************
	public int getId() {
		return id;
	}

	public long getSeed() {
		return seed;
	}

	public Date getDate() {
		return date;
	}

	public String getName() {
		return name;
	}

	public String getSystem() {
		return system;
	}

	public String getWorkload() {
		return workload;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setSeed(int seed) {
		this.seed = seed;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSystem(String system) {
		this.system = system;
	}

	public void setWorkload(String workload) {
		this.workload = workload;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@Override
	public String toString() {
		return "Experiment [id=" + id + ", seed=" + seed + ", date=" + date
				+ ", endDate=" + endDate + ", name=" + name + ", system="
				+ system + ", workload=" + workload + "]";
	}
}
