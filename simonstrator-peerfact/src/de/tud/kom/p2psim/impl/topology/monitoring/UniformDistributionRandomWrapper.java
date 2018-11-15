package de.tud.kom.p2psim.impl.topology.monitoring;

import de.tud.kom.p2psim.impl.util.stat.distributions.UniformDistribution;
import de.tudarmstadt.maki.simonstrator.api.util.Distribution;

/***
 * Wrapper class for the NormalDistribution class, to ensure that the returned
 * value differ by each call of {@code returnValue()}.
 * 
 * @author Julian Maurice Klomp, TU Darmstadt, klomp@stud.tu-darmstadt.de
 *
 */
public class UniformDistributionRandomWrapper implements Distribution {

	private double min, max;

	public UniformDistributionRandomWrapper(double min, double max) {
		this.min = min;
		this.max = max;
	}

	@Override
	public double returnValue() {
		return UniformDistribution.returnValue(min, max);
	}

}
