package de.tud.kom.p2psim.impl.util.db.metric;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "hostmetrics", indexes = {
		@Index(columnList = "id", name = "id"),
		@Index(columnList = "hostId", name = "hostId"),
		@Index(columnList = "metricId", name = "metricId") })
/** Database mapping between metrics and hosts.
 *
 * This class is a POJO that maps to a table in a database. Instances
 * of this class become rows in the corresponding table when persisted.
 *
 * @author Andreas Hemel
 */
public class HostMetric {

	@SuppressWarnings("unused")
	@Id
	@GeneratedValue
	private int id;

	private long hostId;
	
	/**
	 * GroupName of the host
	 */
	@Column(length = 1023)
	private String groupName;

	@ManyToOne
	@JoinColumn(name = "metricId")
	private Metric metric;

    protected HostMetric() {

    }

	public HostMetric(Metric metric, long hostId, String groupName) {
		this.metric = metric;
		this.hostId = hostId;
		this.groupName = groupName;
	}
	
	public long getHostId() {
		return this.hostId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (hostId ^ (hostId >>> 32));
		result = prime * result + ((metric == null) ? 0 : metric.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof HostMetric))
			return false;
		HostMetric other = (HostMetric) obj;
		if (hostId != other.hostId)
			return false;
		if (metric == null) {
			if (other.metric != null)
				return false;
		} else if (!metric.equals(other.metric))
			return false;
		return true;
	}

    @Override
    public String toString() {
        return "HostMetric{" +
                "id=" + id +
                ", hostId=" + hostId +
                ", metric=" + metric +
                '}';
    }

    public int getId() {
        return id;
    }
}
