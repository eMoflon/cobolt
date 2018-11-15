package de.tudarmstadt.maki.simonstrator.tc.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmParamters;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.TopologyControlComponentConfig;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;

/**
 * This class captures per-iteration statistics of the
 * {@link TopologyControlComponent}
 *
 * @author Roland Kluge - Initial implementation
 */
public class EvaluationStatistics {

	public static final int NOT_SET = -1;

	public static final String iterationKey = "iter";
	public static final String simulationTimeInMinutesKey = "simulationTimeInMinutes";
	public static final String nodeCountTotalKey = "nodeCountTotal";
	public static final String nodeCountEmptyKey = "nodeCountEmpty";
	public static final String nodeCountAliveKey = "nodeCountAlive";
	public static final String edgeCountTotalKey = "edgeCountTotal";
	public static final String nodeOutdegreeAvgKey = "nodeOutdegreeAvg";
	public static final String numStronglyConnectedComponentsOutputKey = "numSCCsOutput";
	public static final String numStronglyConnectedComponentsInputKey = "numSCCsInput";
	public static final String nodeCountReachableFromBaseStationKey = "nodeCountReachableFromBaseStation";
	public static final String nodeCountInFacadeKey = "nodeCountInFacade";
	public static final String edgeCountInFacadeKey = "edgeCountInFacade";
	public static final String hopSpannerAvgKey = "hopSpannerAvg";
	public static final String hopSpannerMaxKey = "hopSpannerMax";
	public static final String tcLSMCountTotalKey = "tcLSMCountTotal";
	public static final String tcLSMCountActKey = "tcLSMCountAct";
	public static final String tcLSMCountInactKey = "tcLSMCountInact";
	public static final String tcLSMCountClassificationKey = "tcLSMCountClassification";
	public static final String tcLSMCountUnclassificationKey = "tcLSMCountUnclassification";
	public static final String tcLSMCountEffectiveKey = "tcLSMCountEffective";
	public static final String tcTimeInMillisKey = "tcTimeInMillis";
	public static final String ceLSMCountTotalKey = "ceLSMCountTotal";
	public static final String ceRuleCountTotalKey = "ceRuleCountTotal";
	public static final String ceLSMsPerRuleKey = "ceLSMsPerRule";
	public static final String ceLSMCountUnclassificationKey = "ceLSMCountUnclassification";
	public static final String ceLSMCountEffectiveKey = "ceLSMCountEffective";
	public static final String ceNodeAddtionCountKey = "ceNodeAddtionCount";
	public static final String ceNodeRemovalCountKey = "ceNodeRemovalCount";
	public static final String ceEdgeAdditionCountKey = "ceEdgeAdditionCount";
	public static final String ceEdgeRemovalCountKey = "ceEdgeRemovalCount";
	public static final String ceDistanceModCountKey = "ceDistanceModCount";
	public static final String ceRemainingEnergyModCountKey = "ceRemainingEnergyModCount";
	public static final String ceRequiredPowerModCountKey = "ceRequiredPowerModCount";
	public static final String ceTimeInMillisKey = "ceTimeInMillis";
	public static final String statTimeInMillisKey = "statTimeInMillis";
	public static final String energyLevelAvgKey = "energyLevelAvg";
	public static final String energyLevelMinKey = "energyLevelMin";
	public static final String energyLevelMaxKey = "energyLevelMax";
	public static final String energyPercentageAvgKey = "energyPercentageAvg";
	public static final String energyPercentageMinKey = "energyPercentageMin";
	public static final String energyPercentageMaxKey = "energyPercentageMax";
	public static final String tcViolationCountKey = "tcViolationCount";
	public static final String totalTimeInMinutesKey = "totalTimeInMinutes";
	public static final String ceCheckTimeInMillisKey = "ceCheckTimeInMillis";
	public static final String tcCheckTimeInMillisKey = "tcCheckTimeInMillis";
	public static final String remainingLifetimeMeanKey = "remainingLifetimeMean";
	public static final String remainingLifetimeStddevKey = "remainingLifetimeStddev";
	public static final String lifetimeOneKey = "lifetimeOneKey";
	public static final String lifetimeFirstQuartileKey = "lifetimeFirstQuartileKey";
	public static final String lifetimeSecondQuartileKey = "lifetimeSecondQuartileKey";
	public static final String lifetimeThirdQuartileKey = "lifetimeThirdQuartileKey";
	public static final String lifetimeAllKey = "lifetimeAllKey";

	public static final String cWorldSizeKey = "cWorldSize";
	public static final String cNodeCountKey = "cNodeCount";
	public static final String cKtcParameterKKey = "cKtcParameterK";
	public static final String cSeedKey = "cSeed";
	public static final String cAlgoKey = "cAlgoId";
	public static final String cAlgoName = "cAlgoName";
	public static final String cAlgoMode = "cAlgoMode";
	public static final String cMinimumDistanceThresholdInMetersKey = "cMinimumDistanceThresholdInMeters";

	//
	public int iteration = NOT_SET;
	public double simulationTimeInMinutes = NOT_SET;
	public int nodeCountTotal = NOT_SET;
	public int nodeCountEmpty = NOT_SET;
	public int nodeCountAlive = NOT_SET;
	public int edgeCountTotal = NOT_SET;
	public double nodeOutdegreeAvg = NOT_SET;
	public int numStronglyConnectedComponentsOutput = NOT_SET;
	public int numStronglyConnectedComponentsInput = NOT_SET;
	public double nodeCountReachableFromBaseStation = NOT_SET;
	public int nodeCountInFacade;
	public int edgeCountInFacade;
	public double hopSpannerAvg = NOT_SET;
	public double hopSpannerMax = NOT_SET;
	public double tcTimeInMillis = NOT_SET;
	public int tcLSMCountTotal = NOT_SET;
	public int tcLSMCountAct = NOT_SET;
	public int tcLSMCountInact = NOT_SET;
	public int tcLSMCountClassification = NOT_SET;
	public int tcLSMCountUnclassification = NOT_SET;
	public int tcLSMCountEffective = NOT_SET;
	public double ceTimeInMillis = NOT_SET;
	public int ceRuleCountTotal = NOT_SET;
	public int ceLSMCountTotal = NOT_SET;
	public int ceLSMCountUnclassification = NOT_SET;
	public int ceLSMCountEffective = NOT_SET;
	public int ceLSMsPerRule = NOT_SET;
	public int ceNodeAddtionCount = NOT_SET;
	public int ceNodeRemovalCount = NOT_SET;
	public int ceEdgeAdditionCount = NOT_SET;
	public int ceEdgeRemovalCount = NOT_SET;
	public int ceDistanceModCount = NOT_SET;
	public int ceRemainingEnergyModCount = NOT_SET;
	public int ceRequiredPowerModCount = NOT_SET;
	public double statTimeInMillis = NOT_SET;
	public double energyLevelAvg = NOT_SET;
	public double energyLevelMin = NOT_SET;
	public double energyLevelMax = NOT_SET;
	public double energyPercentageAvg = NOT_SET;
	public double energyPercentageMin = NOT_SET;
	public double energyPercentageMax = NOT_SET;
	public int tcViolationCount = NOT_SET;
	public double totalTimeInMinutes = NOT_SET;
	public double tcCheckTimeInMillis = NOT_SET;
	public double ceCheckTimeInMillis = NOT_SET;
	public double remainingLifetimeMean = NOT_SET;
	public double remainingLifetimeStddev = NOT_SET;
	public double lifetimeOne = NOT_SET;
	public double lifetimeFirstQuartile = NOT_SET;
	public double lifetimeSecondQuartile = NOT_SET;
	public double lifetimeThirdQuartile = NOT_SET;
	public double lifetimeAll = NOT_SET;
	public TopologyControlComponentConfig simulationConfiguration;

	public static final List<String> EVALUATION_RESULT_FILE_HEADER = Arrays.asList( //
			cWorldSizeKey, "%d", //
			cNodeCountKey, "%d", //
			cSeedKey, "%d", //
			cAlgoKey, "%d", //
			cAlgoName, "%s", //
			cAlgoMode, "%s", //
			cKtcParameterKKey, "%.2f", //
			cMinimumDistanceThresholdInMetersKey, "%.1f", //
			//
			iterationKey, "%d", //
			simulationTimeInMinutesKey, "%.3f", //
			totalTimeInMinutesKey, "%.2f", //
			nodeCountTotalKey, "%d", //
			nodeCountEmptyKey, "%d", //
			nodeCountAliveKey, "%d", //
			edgeCountTotalKey, "%d", //
			nodeOutdegreeAvgKey, "%.1f", //
			numStronglyConnectedComponentsOutputKey, "%d", //
			numStronglyConnectedComponentsInputKey, "%d", //
			nodeCountReachableFromBaseStationKey, "%.2f", //
			nodeCountInFacadeKey, "%d", //
			edgeCountInFacadeKey, "%d", //
			hopSpannerAvgKey, "%.2f", //
			hopSpannerMaxKey, "%.2f", //
			//
			tcTimeInMillisKey, "%.2f", //
			tcLSMCountTotalKey, "%d", //
			tcLSMCountActKey, "%d", //
			tcLSMCountInactKey, "%d", //
			tcLSMCountClassificationKey, "%d", //
			tcLSMCountUnclassificationKey, "%d", //
			tcLSMCountEffectiveKey, "%d", //
			//
			ceTimeInMillisKey, "%.2f", //
			ceRuleCountTotalKey, "%d", //
			ceLSMCountTotalKey, "%d", //
			ceLSMCountUnclassificationKey, "%d", //
			ceLSMCountEffectiveKey, "%d", //
			ceLSMsPerRuleKey, "%d", //
			//
			ceNodeAddtionCountKey, "%d", //
			ceNodeRemovalCountKey, "%d", //
			ceEdgeAdditionCountKey, "%d", //
			ceEdgeRemovalCountKey, "%d", //
			ceDistanceModCountKey, "%d", //
			ceRemainingEnergyModCountKey, "%d", //
			ceRequiredPowerModCountKey, "%d", //
			//
			//
			statTimeInMillisKey, "%.2f", //
			energyLevelAvgKey, "%.2f", //
			energyLevelMinKey, "%.2f", //
			energyLevelMaxKey, "%.2f", //
			energyPercentageAvgKey, "%.2f", //
			energyPercentageMinKey, "%.2f", //
			energyPercentageMaxKey, "%.2f", //
			tcViolationCountKey, "%d", //
			ceCheckTimeInMillisKey, "%.2f", //
			tcCheckTimeInMillisKey, "%.2f", //
			//
			remainingLifetimeMeanKey, "%.2f", //
			remainingLifetimeStddevKey, "%.2f",
			//
			lifetimeOneKey, "%.2f", //
			lifetimeFirstQuartileKey, "%.2f", //
			lifetimeSecondQuartileKey, "%.2f", //
			lifetimeThirdQuartileKey, "%.2f", //
			lifetimeAllKey, "%.2f");

	private static final List<String> COLUMN_NAMES = extractColumnNames(EVALUATION_RESULT_FILE_HEADER);
	private static final List<String> FORMATTING_SPECIFIERS = extractFormattingSpecifier(EVALUATION_RESULT_FILE_HEADER);

	private static List<String> extractColumnNames(final List<String> evaluationResultFileHeader) {
		final List<String> columnNames = new ArrayList<>();
		for (int i = 0; i < EVALUATION_RESULT_FILE_HEADER.size(); i += 2) {
			columnNames.add(EVALUATION_RESULT_FILE_HEADER.get(i));
		}
		return columnNames;
	}

	private static List<String> extractFormattingSpecifier(final List<String> evaluationResultFileHeader) {
		final List<String> columnNames = new ArrayList<>();
		for (int i = 1; i < EVALUATION_RESULT_FILE_HEADER.size(); i += 2) {
			columnNames.add(EVALUATION_RESULT_FILE_HEADER.get(i));
		}
		return columnNames;
	}

	private static List<String> getColumnNames() {
		return COLUMN_NAMES;
	}

	private static List<String> getFormattingSpecifiersNames() {
		return FORMATTING_SPECIFIERS;
	}

	public static int getColumnCount() {
		return COLUMN_NAMES.size();
	}

	public static String createHeaderOfEvaluationDataFile(final String separator) {
		return StringUtils.join(getColumnNames(), separator);
	}

	public String formatAsCsvLine(final String separator) {
		final String formattingSpecification = getFormattingSpecifiersNames().stream()
				.collect(Collectors.joining(separator));
		return String.format(Locale.US, formattingSpecification, //
				simulationConfiguration.worldSize, //
				simulationConfiguration.nodeCount, //
				simulationConfiguration.seed, //
				simulationConfiguration.topologyControlAlgorithmID.getUniqueId(), //
				simulationConfiguration.topologyControlAlgorithmID.getName(), //
				simulationConfiguration.topologyControlOperationMode.toString().charAt(0), //
				getKtcParameterK(), //
				simulationConfiguration.minimumDistanceThresholdInMeters, //
				//
				iteration, //
				simulationTimeInMinutes, //
				totalTimeInMinutes, //
				nodeCountTotal, //
				nodeCountEmpty, //
				nodeCountAlive, //
				edgeCountTotal, //
				nodeOutdegreeAvg, //
				numStronglyConnectedComponentsOutput, //
				numStronglyConnectedComponentsInput, //
				nodeCountReachableFromBaseStation, //
				nodeCountInFacade, //
				edgeCountInFacade, //
				hopSpannerAvg, //
				hopSpannerMax, //
				//
				tcTimeInMillis, //
				tcLSMCountTotal, //
				tcLSMCountAct, //
				tcLSMCountInact, //
				tcLSMCountClassification, //
				tcLSMCountUnclassification, //
				tcLSMCountEffective, //
				//
				ceTimeInMillis, //
				ceRuleCountTotal, //
				ceLSMCountTotal, //
				ceLSMCountUnclassification, //
				ceLSMCountEffective, //
				ceLSMsPerRule, //
				//
				ceNodeAddtionCount, //
				ceNodeRemovalCount, //
				ceEdgeAdditionCount, //
				ceEdgeRemovalCount, //
				ceDistanceModCount, //
				ceRemainingEnergyModCount, //
				ceRequiredPowerModCount, //
				//
				statTimeInMillis, //
				energyLevelAvg, //
				energyLevelMin, //
				energyLevelMax, //
				energyPercentageAvg, //
				energyPercentageMin, //
				energyPercentageMax, //
				tcViolationCount, //
				ceCheckTimeInMillis, //
				tcCheckTimeInMillis, //
				//
				remainingLifetimeMean, //
				remainingLifetimeStddev, //
				//
				lifetimeOne, //
				lifetimeFirstQuartile, //
				lifetimeSecondQuartile, //
				lifetimeThirdQuartile, //
				lifetimeAll //
		);
	}

	private double getKtcParameterK() {
		final TopologyControlAlgorithmParamters topologyControlAlgorithmParamters = simulationConfiguration.topologyControlAlgorithmParamters;
		if (topologyControlAlgorithmParamters.hasParameter(UnderlayTopologyControlAlgorithms.KTC_PARAM_K)) {
			return ((Number) topologyControlAlgorithmParamters.getValue(UnderlayTopologyControlAlgorithms.KTC_PARAM_K))
					.doubleValue();
		} else {
			return Double.NaN;
		}
	}

}