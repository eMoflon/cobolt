package de.tud.kom.p2psim.impl.churn;

import java.util.List;

import de.tud.kom.p2psim.api.churn.ChurnModel;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.impl.util.stat.distributions.CustomDistribution;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.util.Distribution;

/**
 * This churn model determines the uptime of a host through the CDF data loaded
 * from the given CSV file. Hosts will have no downtime as they reconnect
 * immediately.
 * 
 * @author Fabio Zöllner
 * 
 */
public class CDFChurnModel implements ChurnModel {

	private String distributionDataFile = "";

	private Distribution distribution = null;

	public CDFChurnModel() {

	}

	public void setDistributionDataFile(String distributionDataFile) {
		this.distributionDataFile = distributionDataFile;

		this.distribution = new CustomDistribution(this.distributionDataFile);
	}

	@Override
	public long getNextUptime(SimHost host) {
		long uptime = 0; // Immediately reconnect
		return uptime;
	}

	@Override
	public long getNextDowntime(SimHost host) {
		long downtime = (long) (distribution.returnValue() * (double) Time.MINUTE);
		return downtime;
	}

	@Override
	public void prepare(List<SimHost> churnHosts) {
		// Not required
	}
}
