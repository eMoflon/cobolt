package de.tudarmstadt.maki.simonstrator.tc.component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;
import de.tudarmstadt.maki.simonstrator.tc.GraphUtil;
import de.tudarmstadt.maki.simonstrator.tc.analyzer.metrics.spanner.HopSpanner;
import de.tudarmstadt.maki.simonstrator.tc.facade.CountingContextEventListener;
import de.tudarmstadt.maki.simonstrator.tc.facade.CountingLinkStateListener;
import de.tudarmstadt.maki.simonstrator.tc.facade.ITopologyControlFacade;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.graph.algorithm.FloydWarshallAlgorithm;
import de.tudarmstadt.maki.simonstrator.tc.graph.algorithm.TarjanSCC;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.InformationRecord;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.GraphDensitySpannerMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.GraphStorageSpannerMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.HopCountToBaseStationMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.MaxHopSpannerMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.MeanHopSpannerMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.MetricUtils;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.NumberOfNodesReachableFromBaseStation;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.TransmissionRangeMetric;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.TopologyControlComponentConfig;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;

public class TopologyControlComponentStatisticsHelper {

	private static final boolean APPEND_TO_FILE = true;

	private final TopologyControlComponent component;

	private EvaluationStatistics statisticsDTO;

	private EvaluationStatistics previousStatisticsDTO;

	private long globalTic = -1;

	private int iterationCounter;

	public TopologyControlComponentStatisticsHelper(final TopologyControlComponent component) {
		this.component = component;
		this.iterationCounter = 0;
		this.initializeLogFile();
	}

	/**
	 * @param inputTopology
	 */
	public void collectAndWriteInitialInputTopologyStatistics(final Graph inputTopology) {
		try {
			final boolean isConnected = GraphUtil.isStronglyConnected(inputTopology);

			final File algorithmIdMappingFile = new File(getConfiguration().outputFolder,
					getConfiguration().outputFilePrefix + "algorithms.csv");

			final List<String> lines = new ArrayList<>();
			lines.add(StringUtils.join(Arrays.asList("id", "name"),
					TopologyControlComponentEvaluationDataHelper.CSV_SEP));
			for (final TopologyControlAlgorithmID algorithm : UnderlayTopologyControlAlgorithms.getAlgorithms()) {
				final String normalizedAlgorithmName = algorithm.getName().replaceAll("\\s+", "_");
				final int algoId = algorithm.getUniqueId();
				lines.add(String.format("%d%s%s", algoId, TopologyControlComponentEvaluationDataHelper.CSV_SEP,
						normalizedAlgorithmName));
			}
			FileUtils.writeLines(algorithmIdMappingFile, lines);
			final File scenarioFile = new File(getConfiguration().outputFolder,
					getConfiguration().outputFilePrefix + "scenarioStatistics.csv");
			if (!scenarioFile.exists()) {
				final List<String> statisticsHeader = Arrays
						.asList(StringUtils.join(
								Arrays.asList("nodeCount", "linkCount", "worldSizeInMeters", "isConnected", "seed",
										"avgOutdegree", "avgIndegree", "avgDegree", "avgPairwiseHops",
										"maxPairwiseHops", "avgHopCountToBasestation", "medianHopCountToBasestation",
										"hopCountToBasestationMax", "transmissionRangeInMeters"),
								TopologyControlComponentEvaluationDataHelper.CSV_SEP));
				FileUtils.writeLines(scenarioFile, statisticsHeader);
			}

			final DescriptiveStatistics hopCountStatistics = MetricUtils
					.getStatisticsOverHosts(getMetric(HopCountToBaseStationMetric.class));

			final FloydWarshallAlgorithm floydWarshallAlgorithm = new FloydWarshallAlgorithm();
			floydWarshallAlgorithm.compute(inputTopology);
			final double meanPairwiseHopCountDistance = floydWarshallAlgorithm.getAverageDistance();
			final double maxPairwiseHopCountDistance = floydWarshallAlgorithm.getMaximumDistance();

			final double edgeCount = inputTopology.getEdgeCount();
			final double connectivityFlag = isConnected ? 1 : 0;
			final double avgOutdegree = GraphUtil.getAverageOutdegree(inputTopology);
			final double avgIndegree = GraphUtil.getAverageIndegree(inputTopology);
			final double avgDegree = GraphUtil.getAverageDegree(inputTopology);
			final double nodeCount = inputTopology.getNodeCount();
			final double worldSize = getConfiguration().worldSize;
			final double seed = getConfiguration().seed;

			FileUtils
					.writeLines(scenarioFile,
							Arrays.asList(
									StringUtils.join(
											Arrays.asList(nodeCount, edgeCount, worldSize, connectivityFlag, seed,
													avgOutdegree, avgIndegree, avgDegree, meanPairwiseHopCountDistance,
													maxPairwiseHopCountDistance, hopCountStatistics.getMean(),
													hopCountStatistics.getPercentile(50), hopCountStatistics.getMax(),
													MetricUtils.getOverallDoubleMetric(
															getMetric(TransmissionRangeMetric.class))),
											TopologyControlComponentEvaluationDataHelper.CSV_SEP)),
							APPEND_TO_FILE);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void collectStatistics(final Graph inputTopology, final Graph outputTopology) {
		final Graph facadeGraph = this.getIncrementalFacade().getGraph();

		/*
		 * ENERGY, ALIVE, DEAD
		 */
		final DescriptiveStatistics energyLevelStatistics = new DescriptiveStatistics();
		final DescriptiveStatistics energyPercentageStatistics = new DescriptiveStatistics();
		final List<INodeID> emptyNodes = new ArrayList<>();
		for (final INodeID node : inputTopology.getNodeIds()) {
			if (this.getAttributeHelper().isBatteryEmpty(node))
				emptyNodes.add(node);

			final double energyLevel = this.getAttributeHelper().calculateEnergyLevel(node);
			final double energyPercentage = this.getAttributeHelper().calculateEnergyPercentage(node);
			energyLevelStatistics.addValue(energyLevel);
			energyPercentageStatistics.addValue(energyPercentage);
		}
		this.statisticsDTO.energyLevelAvg = energyLevelStatistics.getMean();
		this.statisticsDTO.energyLevelMin = energyLevelStatistics.getMin();
		this.statisticsDTO.energyLevelMax = energyLevelStatistics.getMax();
		this.statisticsDTO.energyPercentageAvg = energyPercentageStatistics.getMean();
		this.statisticsDTO.energyPercentageMin = energyPercentageStatistics.getMin();
		this.statisticsDTO.energyPercentageMax = energyPercentageStatistics.getMax();
		this.statisticsDTO.nodeCountTotal = inputTopology.getNodeCount();
		this.statisticsDTO.nodeCountEmpty = emptyNodes.size();
		this.statisticsDTO.nodeCountAlive = this.statisticsDTO.nodeCountTotal - this.statisticsDTO.nodeCountEmpty;
		this.statisticsDTO.edgeCountTotal = this.getAttributeHelper()
				.calculateEdgeCountBetweenNonEmptyNodes(inputTopology);
		// The facade only contains the non-empty nodes!
		this.statisticsDTO.nodeOutdegreeAvg = facadeGraph.getNodeCount() > 0
				? GraphUtil.getAverageOutdegree(facadeGraph)
				: Double.NaN;
		this.statisticsDTO.numStronglyConnectedComponentsOutput = new TarjanSCC(outputTopology).getNumberOfSccs();
		this.statisticsDTO.numStronglyConnectedComponentsInput = new TarjanSCC(inputTopology).getNumberOfSccs();
		this.statisticsDTO.nodeCountReachableFromBaseStation = MetricUtils
				.getOverallDoubleMetric(getMetric(NumberOfNodesReachableFromBaseStation.class));

		this.statisticsDTO.nodeCountInFacade = facadeGraph.getNodeCount();
		this.statisticsDTO.edgeCountInFacade = facadeGraph.getEdgeCount();

		/*
		 * LIFETIME
		 */
		final double totalNodeCountFirstQuartile = this.statisticsDTO.nodeCountTotal * 0.25;
		final double totalNodeCountSecondQuartile = this.statisticsDTO.nodeCountTotal * 0.50;
		final double totalNodeCountThirdQuartile = this.statisticsDTO.nodeCountTotal * 0.75;
		if (this.statisticsDTO.nodeCountEmpty >= 1.0 && this.previousStatisticsDTO.nodeCountEmpty < 1)
			this.statisticsDTO.lifetimeOne = this.statisticsDTO.simulationTimeInMinutes;
		else
			this.statisticsDTO.lifetimeOne = this.previousStatisticsDTO.lifetimeOne;
		if (this.statisticsDTO.nodeCountEmpty >= totalNodeCountFirstQuartile
				&& this.previousStatisticsDTO.nodeCountEmpty < totalNodeCountFirstQuartile)
			this.statisticsDTO.lifetimeFirstQuartile = this.statisticsDTO.simulationTimeInMinutes;
		else
			this.statisticsDTO.lifetimeFirstQuartile = this.previousStatisticsDTO.lifetimeFirstQuartile;
		if (this.statisticsDTO.nodeCountEmpty >= totalNodeCountSecondQuartile
				&& this.previousStatisticsDTO.nodeCountEmpty < totalNodeCountSecondQuartile)
			this.statisticsDTO.lifetimeSecondQuartile = this.statisticsDTO.simulationTimeInMinutes;
		else
			this.statisticsDTO.lifetimeSecondQuartile = this.previousStatisticsDTO.lifetimeSecondQuartile;
		if (this.statisticsDTO.nodeCountEmpty >= totalNodeCountThirdQuartile
				&& this.previousStatisticsDTO.nodeCountEmpty < totalNodeCountThirdQuartile)
			this.statisticsDTO.lifetimeThirdQuartile = this.statisticsDTO.simulationTimeInMinutes;
		else
			this.statisticsDTO.lifetimeThirdQuartile = this.previousStatisticsDTO.lifetimeThirdQuartile;
		if (this.statisticsDTO.nodeCountEmpty >= this.statisticsDTO.nodeCountTotal
				&& this.previousStatisticsDTO.nodeCountEmpty < this.statisticsDTO.nodeCountTotal)
			this.statisticsDTO.lifetimeAll = this.statisticsDTO.simulationTimeInMinutes;
		else
			this.statisticsDTO.lifetimeAll = this.previousStatisticsDTO.lifetimeAll;

		/*
		 * GRAPH METRICS: Hop spanner, graph storage
		 */
		final HopSpanner hopSpanner = new HopSpanner();
		hopSpanner.compute(inputTopology, outputTopology);
		hopSpanner.compute(inputTopology, outputTopology);

		final double avgPairwiseHopSpanner = hopSpanner.getAveragePairwiseSpanner();
		if (Double.isFinite(avgPairwiseHopSpanner)) {
			this.statisticsDTO.hopSpannerAvg = avgPairwiseHopSpanner;
			this.component.getInformationStore().put(new MeanHopSpannerMetric(hopSpanner));
		} else {
			this.statisticsDTO.hopSpannerAvg = TopologyControlComponentAttributeHelper.INVALID_DATAPOINT_MARKER;
		}

		final double maxPairwiseHopSpanner = hopSpanner.getMaximumPairwiseSpanner();
		if (Double.isFinite(maxPairwiseHopSpanner)) {
			this.statisticsDTO.hopSpannerMax = maxPairwiseHopSpanner;
			this.component.getInformationStore().put(new MaxHopSpannerMetric(hopSpanner));
		} else {
			this.statisticsDTO.hopSpannerMax = TopologyControlComponentAttributeHelper.INVALID_DATAPOINT_MARKER;
		}

		if (inputTopology.getNodeCount() > 0) {
			this.component.getInformationStore().put(new GraphStorageSpannerMetric(inputTopology, outputTopology));
		}

		if (inputTopology.getNodeCount() != 0 && outputTopology.getEdgeCount() != 0) {
			this.component.getInformationStore().put(new GraphDensitySpannerMetric(inputTopology, outputTopology));
		}

		/*
		 * EXPECTED LIFETIME
		 */
		final DescriptiveStatistics estimatedLifetimeStatistics = new DescriptiveStatistics();
		for (final INodeID nodeId : facadeGraph.getNodeIds()) {
			final double expectedLifetime = this.getAttributeHelper().calculateExpectedLifetime(nodeId, facadeGraph);
			estimatedLifetimeStatistics.addValue(expectedLifetime);
		}
		this.statisticsDTO.remainingLifetimeMean = estimatedLifetimeStatistics.getMean();
		this.statisticsDTO.remainingLifetimeStddev = estimatedLifetimeStatistics.getStandardDeviation();

	}

	private TopologyControlComponentAttributeHelper getAttributeHelper() {
		return component.getAttributeHelper();
	}

	private ITopologyControlFacade getIncrementalFacade() {
		return component.getTopologyControlFacade();
	}

	/**
	 * Redirects the logger output to
	 */
	void initializeLogFile() {
		try {
			final FileAppender appender = new FileAppender(
					new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n"),
					this.getConfiguration().logfileName);
			appender.setThreshold(org.apache.log4j.Level.DEBUG);
			Logger.getRootLogger().addAppender(appender);
		} catch (final IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private TopologyControlComponentConfig getConfiguration() {
		return this.component.getConfiguration();
	}

	public int getIterationCounter() {
		return this.iterationCounter;
	}

	long getGlobalTic() {
		return this.globalTic;
	}

	public EvaluationStatistics getStatisticsDTO() {
		return this.statisticsDTO;
	}

	public void beginNextIteration() {
		if (globalTic == -1)
			globalTic = System.currentTimeMillis();

		++iterationCounter;

		previousStatisticsDTO = statisticsDTO != null ? statisticsDTO : new EvaluationStatistics();
		statisticsDTO = new EvaluationStatistics();

		getStatisticsDTO().iteration = getIterationCounter();
		getStatisticsDTO().simulationTimeInMinutes = Time.getCurrentTime() / Time.MINUTE;
		getStatisticsDTO().simulationConfiguration = this.component.getConfiguration();

	}

	void recordPostContextEventStatistics(final TopologyControlComponent topologyControlComponent,
			final CountingLinkStateListener ceLSMListener,
			final CountingContextEventListener intraCEExecutionCountingContextEventListener,
			final long contextEventDuration, final long contextEventCheckTime) {
		getStatisticsDTO().ceTimeInMillis = contextEventDuration;
		getStatisticsDTO().ceCheckTimeInMillis = contextEventCheckTime;
		getStatisticsDTO().ceLSMCountEffective = ceLSMListener.getEffectiveLinkStateChangeCount();
		getStatisticsDTO().ceLSMCountTotal = ceLSMListener.getAggregatedLinkStateChangeCount();
		getStatisticsDTO().ceRuleCountTotal = intraCEExecutionCountingContextEventListener
				.getAggregatedContextEventCount();
		getStatisticsDTO().ceLSMsPerRule = getStatisticsDTO().ceRuleCountTotal != 0
				? getStatisticsDTO().ceLSMCountTotal / getStatisticsDTO().ceRuleCountTotal
				: 0;
		getStatisticsDTO().ceLSMCountUnclassification = ceLSMListener.getUnclassificationCount();

		getStatisticsDTO().ceNodeAddtionCount = intraCEExecutionCountingContextEventListener.getNodeAddedCount();
		getStatisticsDTO().ceNodeRemovalCount = intraCEExecutionCountingContextEventListener.getNodeRemovedCount();
		getStatisticsDTO().ceEdgeAdditionCount = intraCEExecutionCountingContextEventListener.getEdgeAddedCount();
		getStatisticsDTO().ceEdgeRemovalCount = intraCEExecutionCountingContextEventListener.getEdgeRemovedCount();
		getStatisticsDTO().ceDistanceModCount = intraCEExecutionCountingContextEventListener
				.getEdgeAttributeUpdateCount(UnderlayTopologyProperties.WEIGHT);
		getStatisticsDTO().ceRemainingEnergyModCount = intraCEExecutionCountingContextEventListener
				.getEdgeAttributeUpdateCount(UnderlayTopologyProperties.REMAINING_ENERGY);
		getStatisticsDTO().ceRequiredPowerModCount = intraCEExecutionCountingContextEventListener
				.getEdgeAttributeUpdateCount(UnderlayTopologyProperties.REQUIRED_TRANSMISSION_POWER);
	}

	/**
	 * Returns the metric of the given type or <code>null</code> if the metric is
	 * not present in the current {@link InformationRecord}
	 *
	 * @param metricClass
	 *                        the type of the metric
	 */
	private Metric<MetricValue<?>> getMetric(final Class<? extends Metric<?>> metricClass) {
		return this.component.getInformationStore().getLatestByType(metricClass);
	}

}
