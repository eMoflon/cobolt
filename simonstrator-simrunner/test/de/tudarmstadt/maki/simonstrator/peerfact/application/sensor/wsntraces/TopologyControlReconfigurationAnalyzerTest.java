package de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.wsntraces;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.MetricUtils;

public class TopologyControlReconfigurationAnalyzerTest {

	@Test
	public void test_validateConformanceToFeatureModel() throws Exception {
		TraceReportingHelper.validateConformanceToFeatureModel();
	}

	@Test
	public void testJain() {
		// Absolute fair
		DescriptiveStatistics stat = new DescriptiveStatistics(new double[] { 1.0, 1.0 });
		Assert.assertEquals(1.0, MetricUtils.getJainFairness(stat), 0.0);

		// Absolute unfair
		stat = new DescriptiveStatistics(new double[] { 0.0, 1.0, 0.0 });
		Assert.assertEquals(1.0 / 3, MetricUtils.getJainFairness(stat), 0.0);

		// More complex, intermediate test case
		stat = new DescriptiveStatistics(new double[] { 3.0, 0.0, 7.0, 9.0, 11.0, 0.0, 0.0 });
		// Nominator = (3+7+9+11)^2 = 900
		// Denominator = 7 * (9+49+81+121) = 1820
		Assert.assertEquals(900.0 / 1820.0, MetricUtils.getJainFairness(stat), 0.0);
	}
}
