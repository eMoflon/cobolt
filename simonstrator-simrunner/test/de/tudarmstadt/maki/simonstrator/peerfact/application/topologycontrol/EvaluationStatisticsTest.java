package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.tc.component.EvaluationStatistics;
import de.tudarmstadt.maki.simonstrator.tc.component.TopologyControlComponentEvaluationDataHelper;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmParamters;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlOperationMode;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.TopologyControlComponentConfig;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;

/**
 * Unit tests for {@link EvaluationStatistics}
 */
public class EvaluationStatisticsTest {

	private EvaluationStatistics statistics;

	@Before
	public void setUp() {
		statistics = new EvaluationStatistics();
	}

	@Test
	public void testHeader() throws Exception {
		for (final Object object : EvaluationStatistics.EVALUATION_RESULT_FILE_HEADER) {
			Assert.assertTrue(object instanceof String);
		}
	}

	@Test
	public void testOrderOfFields() throws Exception {
		TopologyControlComponentConfig simulationConfiguration = new TopologyControlComponentConfig();
		simulationConfiguration.setTopologyControlAlgorithm(UnderlayTopologyControlAlgorithms.D_KTC.getName());
		simulationConfiguration.setTopologyControlAlgorithmParamters(
				TopologyControlAlgorithmParamters.create(UnderlayTopologyControlAlgorithms.KTC_PARAM_K, 1.41));
		simulationConfiguration.setTopologyControlOperationMode(TopologyControlOperationMode.BATCH.toString());
		simulationConfiguration.setWorldSize(131);
		simulationConfiguration.setNodeCount(99);
		simulationConfiguration.setSeed(-42);
		simulationConfiguration.setMinimumDistanceThresholdInMeters(242.5);

		int i = 0;
		statistics.simulationConfiguration = simulationConfiguration;
		statistics.iteration = ++i;
		statistics.simulationTimeInMinutes = ++i;
		statistics.nodeCountTotal = ++i;
		statistics.nodeCountEmpty = ++i;
		statistics.nodeCountAlive = ++i;
		statistics.edgeCountTotal = ++i;
		statistics.nodeOutdegreeAvg = ++i;
		statistics.numStronglyConnectedComponentsOutput = ++i;
		statistics.nodeCountReachableFromBaseStation = ++i;
		statistics.hopSpannerAvg = ++i;
		statistics.hopSpannerMax = ++i;
		statistics.tcTimeInMillis = ++i;
		statistics.tcLSMCountTotal = ++i;
		statistics.tcLSMCountAct = ++i;
		statistics.tcLSMCountInact = ++i;
		statistics.tcLSMCountClassification = ++i;
		statistics.tcLSMCountUnclassification = ++i;
		statistics.tcLSMCountEffective = ++i;
		statistics.ceTimeInMillis = ++i;
		statistics.ceRuleCountTotal = ++i;
		statistics.ceLSMCountTotal = ++i;
		statistics.ceLSMCountUnclassification = ++i;
		statistics.ceLSMCountEffective = ++i;
		statistics.ceLSMsPerRule = ++i;
		statistics.ceNodeAddtionCount = ++i;
		statistics.ceNodeRemovalCount = ++i;
		statistics.ceEdgeAdditionCount = ++i;
		statistics.ceEdgeRemovalCount = ++i;
		statistics.ceDistanceModCount = ++i;
		statistics.ceRemainingEnergyModCount = ++i;
		statistics.ceRequiredPowerModCount = ++i;
		statistics.statTimeInMillis = ++i;
		statistics.energyLevelAvg = ++i;
		statistics.energyLevelMin = ++i;
		statistics.energyLevelMax = ++i;
		EvaluationStatistics.createHeaderOfEvaluationDataFile(TopologyControlComponentEvaluationDataHelper.CSV_SEP);
		statistics.formatAsCsvLine(TopologyControlComponentEvaluationDataHelper.CSV_SEP);
	}
}
