package de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.wsntraces;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.cardygan.fm.FM;
import org.cardygan.fm.Feature;
import org.cardygan.fm.util.FmUtil;
import org.cardygan.xtext.util.CardyUtil;
import org.coala.adaptationlogic.api.SASConfiguration;
import org.coala.simonstrator.AdaptationLogicObjectiveValueMetric;
import org.json.JSONArray;

import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.TotalLatencyMetric;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.TotalLinkDropRateMetric;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.TotalReceivedMessageCountMetric;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric.TotalSentMessageCountMetric;
import de.tudarmstadt.maki.simonstrator.tc.component.TopologyControlComponent;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmParameterId;
import de.tudarmstadt.maki.simonstrator.tc.io.CSVLineSpecification;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.TopologyControlInformationStoreComponent;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.EdgeCountMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.GraphDensitySpannerMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.GraphStorageSpannerMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.MaxHopSpannerMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.MeanDegreeMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.MeanHopSpannerMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.MetricUtils;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.NodeCountMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.WorldSizeMetric;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.NonfunctionalProperties;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.NonfunctionalProperty;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.analysis.ReconfigurationPerformanceReportingHelper;
import de.tudarmstadt.maki.simonstrator.tc.scenario.ScenarioType;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;

public class TraceReportingHelper {

	private static final String COL_NAME_NFPVALUE = "reconf-nfpValue";
	private static final String COL_NAME_NFPNAME = "reconf-nfpName";
	private static final String MOBILITY_SPEED_ATTRIBUTE = "mobilitySpeed";
	private static final String MOBILITY_SPEED = "fcMobSpeed";
	private static final String TC_INTERVAL_ATTRIBUTE = "fsTCint";
	private static final String YAO_CONE_COUNT = "parameterYaoConeCount";
	private static final String YAO = "fsYao";
	private static final String LSTARKTC_A_ATTRIBUTE = "parameterLStarKtcA";
	private static final String LSTARKTC_K_ATTRIBUTE = "parameterLStarKtcK";
	private static final String LSTARKTC = "fsLSTARKTC";
	private static final String EKTC_K_ATTRIBUTE = "parameterEKtcK";
	private static final String EKTC = "fsEKTC";
	private static final String DKTC_K_ATTRIBUTE = "parameterKtcK";
	private static final String DKTC = "fsDKTC";
	private static final String TC_ALGO = "fsTCAlgo";
	private static final String DENSITY_ATTRIBUTE = "topologyDensity";
	private static final String NODE_COUNT_ATTRIBUTE = "nodeCount";
	private static final String EDGE_COUNT_ATTRIBUTE = "edgeCount";
	private static final String WORLD_SIZE_COUNT_ATTRIBUTE = "worldSize";
	private static final String DENSITY_FEATURE = "fcTopology";
	private static final String SCENARIO = "fcScenario";
	private static final String SCENARIO_DC = "fcDATACOLLECTION";
	private static final String SCENARIO_DC_PROBABILITY = "fcDATACOLLECTION.datacollectionProbability";
	private static final String SCENARIO_GOSSIP = "fcGOSSIP";
	private static final String SCENARIO_P2P = "fcPOINTTOPOINT";
	private static final String WOPT_ATTRIBUTE = "weightOptimizationThreshold";
	private static final String WOPT_FEATURE = "fsWOpt";
	private static final double NOT_APPLICABLE = Double.NaN;
	// private static final String RNG = "fsRNG";
	// private static final String GG = "fsGG";
	private static final String LMST = "fsLMST";
	private static final String GMST = "fsGMST";
	private static final String SCENARIO_WILDFIRE = "fsWILDFIRE";
	private static final String SCENARIO_SILECNE = "fsSILENCE";
	private static final String SCENARIO_ALLTOALL = "fsALLTOALL";

	static void writeTrace(final TopologyControlComponent tcc, final File outputFile, final Writer out) {
		final CSVLineSpecification csvHeader = getTracesCsvHeader();
		final List<CSVLineSpecification> csvDataLines = getTracesCsvDataLines(tcc, csvHeader);
		if (out != null) {
			for (final CSVLineSpecification csvDataLine : csvDataLines) {
				final JSONArray jsonData = csvDataLine.convertToJsonArray(csvHeader);
				try {
					out.write("WSN traces statistics\n");
					out.write(jsonData.toString(2));
					out.write('\n');
					out.flush();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}

		try {
			if (!outputFile.exists()) {
				FileUtils.writeLines(outputFile, Arrays.asList(csvHeader.format()));
			}

			FileUtils.writeLines(outputFile,
					csvDataLines.stream().map(CSVLineSpecification::format).collect(Collectors.toList()), true);
			Monitor.log(TraceReportingHelper.class, Level.INFO, "Traces written to %s", outputFile);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public static void validateConformanceToFeatureModel() {
		try {
			final SASConfiguration sasConfiguration = SASConfiguration.readFromStream(
					TopologyControlReconfigurationAnalyzer.class.getResourceAsStream("/sasconfig.properties"));
			final FM fm = CardyUtil.loadFmFromFile(sasConfiguration.getFeatureModelFile());
			final Feature root = fm.getRoot();
			final List<Optional<?>> testValues = Arrays.asList(
					FmUtil.findAttributeByName(root, WOPT_FEATURE, WOPT_ATTRIBUTE), //
					FmUtil.findFeatureByName(root, SCENARIO), //
					FmUtil.findFeatureByName(root, SCENARIO_P2P), //
					FmUtil.findFeatureByName(root, SCENARIO_GOSSIP), //
					FmUtil.findFeatureByName(root, SCENARIO_DC), //
					FmUtil.findAttributeByName(root, DENSITY_FEATURE, DENSITY_ATTRIBUTE), //
					FmUtil.findAttributeByName(root, DENSITY_FEATURE, NODE_COUNT_ATTRIBUTE), //
					FmUtil.findAttributeByName(root, DENSITY_FEATURE, EDGE_COUNT_ATTRIBUTE), //
					FmUtil.findAttributeByName(root, DENSITY_FEATURE, WORLD_SIZE_COUNT_ATTRIBUTE), //
					FmUtil.findAttributeByName(root, MOBILITY_SPEED, MOBILITY_SPEED_ATTRIBUTE), //
					FmUtil.findAttributeByName(root, TC_ALGO, TC_INTERVAL_ATTRIBUTE), //
					FmUtil.findAttributeByName(root, DKTC, DKTC_K_ATTRIBUTE), //
					FmUtil.findAttributeByName(root, EKTC, EKTC_K_ATTRIBUTE), //
					FmUtil.findAttributeByName(root, LSTARKTC, LSTARKTC_K_ATTRIBUTE), //
					FmUtil.findAttributeByName(root, LSTARKTC, LSTARKTC_A_ATTRIBUTE), //
					// FmUtil.findAttributeByName(root, YAO, YAO_CONE_COUNT),
					// //
					// FmUtil.findFeatureByName(root, RNG), //
					// FmUtil.findFeatureByName(root, GG), //
					FmUtil.findFeatureByName(root, LMST), //
					FmUtil.findFeatureByName(root, GMST) //
			);
			List<Optional<?>> missingValues = testValues.stream().filter(optional -> !optional.isPresent())
					.collect(Collectors.toList());
			if (!missingValues.isEmpty())
				throw new RuntimeException("CSV header traces file is inconsistent with feature model");

		} catch (final IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static DescriptiveStatistics getEnergyStatistics() {
		final DescriptiveStatistics energyStatistics = new DescriptiveStatistics();
		TopologyControlReconfigurationAnalyzer.totalEnergyConsumption.values()
				.forEach(value -> energyStatistics.addValue(value));
		return energyStatistics;
	}

	private static CSVLineSpecification getTracesCsvHeader() {

		validateConformanceToFeatureModel();

		final List<String> defaultEntries = Arrays.asList(
				// --- Metadata ---
				"meta-configNo", //
				"meta-seed", //
				// --- System features ---
				TC_ALGO, //
				DKTC + "." + DKTC_K_ATTRIBUTE, //
				EKTC + "." + EKTC_K_ATTRIBUTE, //
				LSTARKTC + "." + LSTARKTC_K_ATTRIBUTE, //
				LSTARKTC + "." + LSTARKTC_A_ATTRIBUTE, //
				YAO + "." + YAO_CONE_COUNT, //
				WOPT_FEATURE + "." + WOPT_ATTRIBUTE, TC_INTERVAL_ATTRIBUTE,
				// --- Context features ---
				MOBILITY_SPEED + "." + MOBILITY_SPEED_ATTRIBUTE, //
				SCENARIO, //
				SCENARIO_DC_PROBABILITY, //
				DENSITY_FEATURE + "." + WORLD_SIZE_COUNT_ATTRIBUTE, //
				DENSITY_FEATURE + "." + NODE_COUNT_ATTRIBUTE, //
				DENSITY_FEATURE + "." + EDGE_COUNT_ATTRIBUTE, //
				DENSITY_FEATURE + "." + DENSITY_ATTRIBUTE, //
				// --- Metrics ---
				NonfunctionalProperties.EMEAN.getName(), //
				NonfunctionalProperties.EMEDIAN.getName(), //
				NonfunctionalProperties.ESTDDEV.getName(), //
				NonfunctionalProperties.EJAIN.getName(), //
				NonfunctionalProperties.LINK_DROPRATE.getName(), //
				NonfunctionalProperties.END_TO_END_DROPRATE.getName(), //
				NonfunctionalProperties.LATENCY.getName(), //
				NonfunctionalProperties.NFP_PREFIX + MetricUtils.getDefaultName(GraphStorageSpannerMetric.class), //
				NonfunctionalProperties.NFP_PREFIX + MetricUtils.getDefaultName(GraphDensitySpannerMetric.class), //
				NonfunctionalProperties.NFP_PREFIX + MetricUtils.getDefaultName(MeanHopSpannerMetric.class), //
				NonfunctionalProperties.NFP_PREFIX + MetricUtils.getDefaultName(MaxHopSpannerMetric.class), //
				NonfunctionalProperties.NFP_PREFIX
						+ MetricUtils.getDefaultName(AdaptationLogicObjectiveValueMetric.class), //
				// --- Reconfiguration ---
				"reconf-trainingFraction", //
				"reconf-splitterSeed", //
				COL_NAME_NFPNAME, //
				COL_NAME_NFPVALUE, //
				"reconf-splcFeatureInteractionLineNumber", //
				"reconf-planner" //
		);
		final CSVLineSpecification result = new CSVLineSpecification(defaultEntries.size());
		defaultEntries.forEach(entry -> result.addSpecification("%s", entry));
		return result;

	}

	private static List<CSVLineSpecification> getTracesCsvDataLines(final TopologyControlComponent tcc,
			CSVLineSpecification csvHeader) {
		final TopologyControlInformationStoreComponent informationStore = tcc.getInformationStore();
		final List<CSVLineSpecification> lineSpecifications = new ArrayList<>();

		final CSVLineSpecification lineSpecification = new CSVLineSpecification(csvHeader.getExpectedLength());
		// Metadata
		lineSpecification.addSpecification("%d", ReconfigurationPerformanceReportingHelper.getConfigurationNumber(tcc));
		lineSpecification.addSpecification("%d", getSeed(tcc));
		// System features
		lineSpecification.addSpecification("%s", getTCAlgorithm(tcc));
		lineSpecification.addSpecification("%.2f", getKtcParameterKSafe(tcc));
		lineSpecification.addSpecification("%.2f", getEKtcParameterKSafe(tcc));
		lineSpecification.addSpecification("%.2f", getLStarKtcParameterKSafe(tcc));
		lineSpecification.addSpecification("%.2f", getLStarKtcParameterASafe(tcc));
		lineSpecification.addSpecification("%f", getYaoParameterConeCountSafe(tcc));
		lineSpecification.addSpecification("%.2f", getWeightOptimizationValue(tcc));
		lineSpecification.addSpecification("%.1f", getTCInterval(tcc));
		// Context features
		lineSpecification.addSpecification("%.3f", getMobilitySpeed(tcc));
		lineSpecification.addSpecification("%s", getScenario(tcc));
		lineSpecification.addSpecification("%.3f", getDataCollectionProbability(tcc));
		lineSpecification.addSpecification("%.1f", getWorldSize(informationStore));
		lineSpecification.addSpecification("%d", getNodeCount(informationStore));
		lineSpecification.addSpecification("%d", getEdgeCount(informationStore));
		lineSpecification.addSpecification("%.1f", getMeanDegree(informationStore));
		// Metrics - Energy
		final DescriptiveStatistics energyStatistics = getEnergyStatistics();
		lineSpecification.addSpecification("%.2f", energyStatistics.getMean());
		lineSpecification.addSpecification("%.2f", energyStatistics.getPercentile(50));
		lineSpecification.addSpecification("%.2f", energyStatistics.getStandardDeviation());
		lineSpecification.addSpecification("%.2f", MetricUtils.getJainFairness(energyStatistics));
		// Metrics - Drop rate
		lineSpecification.addSpecification("%.4f", getLinkDropRate(informationStore));
		lineSpecification.addSpecification("%.4f", getEndToEndDropRate(informationStore));
		lineSpecification.addSpecification("%.4f", getEndToEndLatency(informationStore));
		// Metrics - Graph
		lineSpecification.addSpecification("%.2f", getMeanTopologyStorageSpanner(informationStore));
		lineSpecification.addSpecification("%.2f", getMeanTopologyDensitySpanner(informationStore));
		lineSpecification.addSpecification("%.2f", getMeanOfMaximumHopSpanner(informationStore));
		lineSpecification.addSpecification("%.2f", getMeanOfAverageHopSpanner(informationStore));
		// Metrics - Reconfiguration
		lineSpecification.addSpecification("%.2f", getMeanAdaptationLogicObjectiveValue(informationStore));
		// --- Reconfiguration ---
		final double trainingSetFraction = ReconfigurationPerformanceReportingHelper
				.getReconfigurationTrainingSetFraction(tcc);
		final String nfpName = ReconfigurationPerformanceReportingHelper
				.getReconfigurationTrainingSetNonfunctionalProperty(tcc);
		lineSpecification.addSpecification("%.2f", trainingSetFraction);
		lineSpecification.addSpecification("%d",
				ReconfigurationPerformanceReportingHelper.getReconfigurationTrainingSetSeed(tcc));
		lineSpecification.addSpecification("%s", nfpName);
		lineSpecification.addSpecification("%.4f", getNonfunctionalPropertyValue(nfpName, tcc));
		lineSpecification.addSpecification("%d", getFeatureInteractionLineCount(tcc));
		lineSpecification.addSpecification("%s", getPlanner(tcc));

		lineSpecifications.add(lineSpecification);

		/*
		 * Optimization for baseline: If the training-set fraction is 0%, we
		 * simply clone the current CSV line and exchange the NFP names and
		 * values
		 */
		if (trainingSetFraction == 0.0) {

			final int nfpNameIdx = getColumnIndexByName(csvHeader, COL_NAME_NFPNAME);
			final int nfpValueIdx = getColumnIndexByName(csvHeader, COL_NAME_NFPVALUE);

			final List<NonfunctionalProperty> metricsForEvaluation = Arrays.asList(NonfunctionalProperties.EMEAN,
					NonfunctionalProperties.EJAIN, NonfunctionalProperties.LATENCY,
					NonfunctionalProperties.END_TO_END_DROPRATE);
			for (final NonfunctionalProperty nfp : metricsForEvaluation) {
				final String clonedNfpName = nfp.getName();
				if (!nfpName.equals(clonedNfpName)) {
					final CSVLineSpecification clonedLine = new CSVLineSpecification(lineSpecification);
					clonedLine.updateSpecification(nfpNameIdx, clonedNfpName);
					clonedLine.updateSpecification(nfpValueIdx, getNonfunctionalPropertyValue(clonedNfpName, tcc));

					lineSpecifications.add(clonedLine);
				}
			}
		}

		return lineSpecifications;
	}

	private static int getColumnIndexByName(final CSVLineSpecification csvHeader, final String columnName) {
		for (int i = 0; i < csvHeader.getLength(); ++i) {
			if (columnName.equals(csvHeader.getValue(i))) {
				return i;
			}
		}
		throw new IllegalArgumentException(String.format("No index for column name %s", columnName));
	}

	private static double getNonfunctionalPropertyValue(final String nfpName, final TopologyControlComponent tcc) {
		final TopologyControlInformationStoreComponent informationStore = tcc.getInformationStore();
		final DescriptiveStatistics energyStatistics = getEnergyStatistics();
		if (NonfunctionalProperties.EMEAN.getName().equals(nfpName)) {
			return energyStatistics.getMean();
		} else if (NonfunctionalProperties.EJAIN.getName().equals(nfpName)) {
			return MetricUtils.getJainFairness(energyStatistics);
		} else if (NonfunctionalProperties.EMEDIAN.getName().equals(nfpName)) {
			return energyStatistics.getPercentile(50);
		} else if (NonfunctionalProperties.END_TO_END_DROPRATE.getName().equals(nfpName)) {
			return getEndToEndDropRate(informationStore);
		} else if (NonfunctionalProperties.ESTDDEV.getName().equals(nfpName)) {
			return energyStatistics.getStandardDeviation();
		} else if (NonfunctionalProperties.LATENCY.getName().equals(nfpName)) {
			return getEndToEndLatency(informationStore);
		} else if (NonfunctionalProperties.LINK_DROPRATE.getName().equals(nfpName)) {
			return getLinkDropRate(informationStore);
		} else {
			return Double.NaN;
		}
	}

	private static int getFeatureInteractionLineCount(final TopologyControlComponent tcc) {
		return tcc.getConfiguration().splcFeatureInteractionLineNumber;
	}

	private static String getPlanner(final TopologyControlComponent tcc) {
		return tcc.getConfiguration().adaptationLogicPlanner;
	}

	private static int getNodeCount(final TopologyControlInformationStoreComponent informationStore) {
		return MetricUtils.getOverallIntegerMetric(informationStore.getOldestByType(NodeCountMetric.class));
	}

	private static int getEdgeCount(final TopologyControlInformationStoreComponent informationStore) {
		return MetricUtils.getOverallIntegerMetric(informationStore.getOldestByType(EdgeCountMetric.class));
	}

	private static double getMeanDegree(final TopologyControlInformationStoreComponent informationStore) {
		return MetricUtils.getOverallDoubleMetric(informationStore.getOldestByType(MeanDegreeMetric.class));
	}

	private static double getYaoParameterConeCountSafe(final TopologyControlComponent tcc) {
		return isYaoActive(tcc) ? getYaoConeCount(tcc).doubleValue() : Double.NaN;
	}

	private static double getLStarKtcParameterASafe(final TopologyControlComponent tcc) {
		return isLStarKtcActive(tcc) ? getLStarKtcParameterA(tcc).doubleValue() : Double.NaN;
	}

	private static double getLStarKtcParameterKSafe(final TopologyControlComponent tcc) {
		return isLStarKtcActive(tcc) ? getLStarKtcParameterK(tcc).doubleValue() : Double.NaN;
	}

	private static double getEKtcParameterKSafe(final TopologyControlComponent tcc) {
		return isEKtcActive(tcc) ? getEKtcParameterK(tcc).doubleValue() : Double.NaN;
	}

	private static double getKtcParameterKSafe(final TopologyControlComponent tcc) {
		return isDKtcActive(tcc) ? getDKtcParameterK(tcc).doubleValue() : Double.NaN;
	}

	private static boolean isDKtcActive(final TopologyControlComponent tcc) {
		return UnderlayTopologyControlAlgorithms.D_KTC == tcc.getConfiguration().topologyControlAlgorithmID;
	}

	private static boolean isEKtcActive(final TopologyControlComponent tcc) {
		return UnderlayTopologyControlAlgorithms.E_KTC == tcc.getConfiguration().topologyControlAlgorithmID;
	}

	private static boolean isLStarKtcActive(final TopologyControlComponent tcc) {
		return UnderlayTopologyControlAlgorithms.LSTAR_KTC == tcc.getConfiguration().topologyControlAlgorithmID;
	}

	private static boolean isYaoActive(final TopologyControlComponent tcc) {
		return UnderlayTopologyControlAlgorithms.YAO == tcc.getConfiguration().topologyControlAlgorithmID;
	}

	private static double getWorldSize(final TopologyControlInformationStoreComponent informationStore) {
		return MetricUtils.getOverallDoubleMetric(informationStore.getOldestByType(WorldSizeMetric.class));
	}

	private static double getMeanOfMaximumHopSpanner(final TopologyControlInformationStoreComponent informationStore) {
		final double meanOfMaxHopSpanner = calculateMean(informationStore, MaxHopSpannerMetric.class);
		return meanOfMaxHopSpanner;
	}

	private static double getMeanOfAverageHopSpanner(final TopologyControlInformationStoreComponent informationStore) {
		final double meanOfAverageHopSpanner = calculateMean(informationStore, MeanHopSpannerMetric.class);
		return meanOfAverageHopSpanner;
	}

	private static Object getMeanAdaptationLogicObjectiveValue(
			TopologyControlInformationStoreComponent informationStore) {
		return calculateMean(informationStore, AdaptationLogicObjectiveValueMetric.class);
	}

	private static double getMeanTopologyStorageSpanner(
			final TopologyControlInformationStoreComponent informationStore) {
		final double storageSpanner = calculateMean(informationStore, GraphStorageSpannerMetric.class);
		return storageSpanner;
	}

	private static double getMeanTopologyDensitySpanner(
			final TopologyControlInformationStoreComponent informationStore) {
		final double storageSpanner = calculateMean(informationStore, GraphDensitySpannerMetric.class);
		return storageSpanner;
	}

	/**
	 * Determines the mean value of all metrics of the given type in the
	 * {@link TopologyControlInformationStoreComponent}
	 */
	private static double calculateMean(final TopologyControlInformationStoreComponent informationStore,
			final Class<? extends Metric<?>> metricType) {
		final Collection<Metric<MetricValue<?>>> metrics = informationStore.getMetricsByType(metricType);
		if (metrics.isEmpty()) {
			return Double.NaN;
		} else {
			final double meanValue = MetricUtils.getStatisticsOfOverallMetrics(metrics).getMean();
			return meanValue;
		}
	}

	private static double getEndToEndDropRate(final TopologyControlInformationStoreComponent informationStore) {
		final Metric<MetricValue<?>> totalReceivedMetric = informationStore
				.getLatestByType(TotalReceivedMessageCountMetric.class);
		final Metric<MetricValue<?>> totalSentMetric = informationStore
				.getLatestByType(TotalSentMessageCountMetric.class);
		if (totalReceivedMetric != null && totalSentMetric != null) {
			final double totalReceivedMessage = MetricUtils.getStatisticsOverHosts(totalReceivedMetric).getSum();
			final double totalSentMessages = MetricUtils.getStatisticsOverHosts(totalSentMetric).getSum();
			return 1.0 * (totalSentMessages - totalReceivedMessage) / totalSentMessages;
		} else {
			return NOT_APPLICABLE;
		}
	}

	private static double getEndToEndLatency(final TopologyControlInformationStoreComponent informationStore) {
		final Metric<MetricValue<?>> totalLatencyMetric = informationStore.getLatestByType(TotalLatencyMetric.class);
		if (totalLatencyMetric != null) {
			final double totalLatency = MetricUtils.getStatisticsOverHostsIgnoringUnavailableValues(totalLatencyMetric,
					TotalLatencyMetric.NOT_AVAILABLE).getMean();
			return totalLatency;
		} else {
			return NOT_APPLICABLE;
		}

	}

	private static double getLinkDropRate(final TopologyControlInformationStoreComponent informationStore) {
		return MetricUtils.getOverallDoubleMetric(informationStore.getLatestByType(TotalLinkDropRateMetric.class));
	}

	private static long getSeed(final TopologyControlComponent topologyControlComponent) {
		return topologyControlComponent.getConfiguration().seed;
	}

	private static String getScenario(final TopologyControlComponent tcc) {
		final ScenarioType scenario = tcc.getConfiguration().scenario;
		switch (scenario) {
		case DATACOLLECTION:
			return SCENARIO_DC;
		case GOSSIP:
			return SCENARIO_GOSSIP;
		case POINTTOPOINT:
			return SCENARIO_P2P;
		case ALLTOALL:
			return SCENARIO_ALLTOALL;
		case WILDFIRE:
			return SCENARIO_WILDFIRE;
		case SILENCE:
			return SCENARIO_SILECNE;
		default:
			throw new IllegalArgumentException(scenario.toString());
		}
	}

	private static Double getDataCollectionProbability(TopologyControlComponent tcc) {
		if (getScenario(tcc) == SCENARIO_DC) {
			return tcc.getConfiguration().datacollectionProbability;
		}
		return Double.NaN;
	}

	private static double getMobilitySpeed(final TopologyControlComponent tcc) {
		return tcc.getConfiguration().movementMaxSpeedInMetersPerSecond;
	}

	private static double getTCInterval(final TopologyControlComponent tcc) {
		return tcc.getConfiguration().topologyControlIntervalInMinutes;
	}

	private static String getTCAlgorithm(final TopologyControlComponent tcc) {
		return tcc.getConfiguration().topologyControlAlgorithmID.getName();
	}

	private static Number getDKtcParameterK(final TopologyControlComponent tcc) {
		return (Number) getParamterValueOrNaN(UnderlayTopologyControlAlgorithms.KTC_PARAM_K, tcc);
	}

	private static Number getEKtcParameterK(final TopologyControlComponent tcc) {
		return (Number) getParamterValueOrNaN(UnderlayTopologyControlAlgorithms.E_KTC_PARAM_K, tcc);
	}

	private static Number getLStarKtcParameterK(final TopologyControlComponent tcc) {
		return (Number) getParamterValueOrNaN(UnderlayTopologyControlAlgorithms.LSTAR_KTC_PARAM_K, tcc);
	}

	private static Number getLStarKtcParameterA(final TopologyControlComponent tcc) {
		return (Number) getParamterValueOrNaN(UnderlayTopologyControlAlgorithms.LSTAR_KTC_PARAM_A, tcc);
	}

	private static Number getYaoConeCount(final TopologyControlComponent tcc) {
		return (Number) getParamterValueOrNaN(UnderlayTopologyControlAlgorithms.YAO_PARAM_CONE_COUNT, tcc);
	}

	private static Object getParamterValueOrNaN(final TopologyControlAlgorithmParameterId<?> parameter,
			TopologyControlComponent tcc) {
		final Object value = tcc.getConfiguration().topologyControlAlgorithmParamters.getValue(parameter);
		if (value == null)
			return Double.NaN;
		else
			return value;
	}

	private static double getWeightOptimizationValue(final TopologyControlComponent tcc) {
		return tcc.getConfiguration().minimumDistanceThresholdInMeters;
	}

}
