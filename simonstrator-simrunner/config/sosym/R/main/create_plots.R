#
# This R script aggregates the CSV files of the different runs of the same configuration with different seeds.
#

# Quick ref
# Output dummy column for rownames in write.table: write.table(..., col.names = NA) 
# see: https://stackoverflow.com/questions/2478352/write-table-in-r-screws-up-header-when-has-rownames
#
# performanceComparisonTableOnlyNumeric = data.frame(performanceComparisonTable[-c(1), , drop=F]) # use [-c(1), , drop=F] to avoid a coercion a single column to a list/vector
#

options(error=traceback)

require(xtable)

JOIN = "join"
SINGLE = "single"
TAB="  "
#http://rgraphics.limnology.wisc.edu/images/miscellaneous/pch.pngs
PCH_X= 120
PCH_RECT = 0
PCH_CIRC = 1
PCH_PLUS = 2
COLORS = palette(rainbow(10))
COL1 = COLORS[1]
COL2 = COLORS[2]
COL1 = COLORS[3]

GRID_COL = "lightgray"
GRID_LINE_TYPE = 6
CSV_SEP = ";"
SMALL_CEX = 0.3
MEDIUM_CEX = 1.0

FILENAME_PATTERN_ALL_DATA="%s/output/all.csv" # %s: parentDir
FILENAME_PATTERN_MEAN_DATA="%s/output/all_mean.csv" # %s: parentDir

ROOT="../../../../../output/rkluge/sosym/main"

COL_DIST="distanceInMeters"
COL_ENERGY="consumedEnergyInJoule"
COL_TIME="timeInMinutes"

source("./cfg_colnames_sosym.R")
source("./cfg_utilities.R")
source("./cfg_algorithms.R")

main <- function() {
    allDirs = list.dirs(ROOT, recursive = F)
	runDirectories = allDirs[grepl('^.*/batchrun_.*$', allDirs)]
	sapply(runDirectories, processRunDirectory)
    log("Done.")
}

processRunDirectory <- function(parentDir) {
	parentDir <<- parentDir
	
	prepareOutputDirectory()
	outputFileAllData = sprintf(FILENAME_PATTERN_ALL_DATA, parentDir)
	outputFileMeanData = sprintf(FILENAME_PATTERN_MEAN_DATA, parentDir)
	if (file.exists(outputFileAllData))
	{
		log("Reading cached files ...")
		allData <<- read.table(outputFileAllData, sep = CSV_SEP, header = T)
		allDataMeanOverSeed <<- read.table(outputFileMeanData, sep = CSV_SEP, header = T)
	}
	else{
		preprocessData();
	}
	
	#plotAliveNodesVsTime_Individual()
	plotAliveNodesVsTime_AlgorithmComparison()
	createPerformanceCharts()
	
}

#
# === PROCESSING FUNCTIONS ===
#
preprocessData <- function()
{
	checkGlobalParentDir()
	
	dataFiles = list.files(path=parentDir, pattern="^data.csv$", recursive =  T, full.names = T)
	log("Pre-processing %d CSV files...", length(dataFiles))
	perSeedDataFrames = lapply(dataFiles, read.csv, sep = CSV_SEP)
	allData <<- Reduce(rbind, perSeedDataFrames)
	allDataMeanOverSeed <<- aggregate(allData, 
			by = list(
					allData[[cWorldSizeKey]], 
					allData[[cNodeCountKey]], 
					allData[[cAlgoKey]], 
					allData[[cKtcParameterKKey]],
					allData[[cMinimumDistanceThresholdInMetersKey]],
					allData[[iterationKey]]
			), 
			FUN = mean, na.rm = T, na.action=na.pass) 
	# Ad na.rm = T, na.action=na.pass
	# See https://stackoverflow.com/questions/17737174/blend-of-na-omit-and-na-pass-using-aggregate-in-r/17737308#17737308
	#allDataMeanOverSeed <<- allDataMeanOverSeed[ !(names(allDataMeanOverSeed) %in% c("Group.1", "Group.2", "Group.3", "Group.4", "Group.5", "Group.6"))]
	allDataMeanOverSeed <<- allDataMeanOverSeed[ !(grepl("Group.*", names(allDataMeanOverSeed)))]
	allDataMeanOverSeed <<- allDataMeanOverSeed[order(
					allDataMeanOverSeed[[cWorldSizeKey]], 
					allDataMeanOverSeed[[cNodeCountKey]], 
					allDataMeanOverSeed[[cAlgoKey]],
					allDataMeanOverSeed[[cKtcParameterKKey]],
					allDataMeanOverSeed[[cMinimumDistanceThresholdInMetersKey]],
					allDataMeanOverSeed[[iterationKey]]
			),] 
	write.table(allData, file = sprintf(FILENAME_PATTERN_ALL_DATA, parentDir), sep = CSV_SEP, row.names = F, col.names = T)
	write.table(allDataMeanOverSeed, file = sprintf(FILENAME_PATTERN_MEAN_DATA, parentDir), sep = CSV_SEP, row.names = F, col.names = T)
	log("Pre-processing done.")
}

plotAliveNodesVsTime_Individual <- function(){
	checkGlobalParentDir()
	
	configurations = unique(allDataMeanOverSeed[c(cWorldSizeKey,cNodeCountKey,cKtcParameterKKey,cAlgoKey,cMinimumDistanceThresholdInMetersKey)]);
	targetDirectory = sprintf("%s/output/alive-nodes_vs_simtime_individual/", parentDir)
	if (!dir.exists(targetDirectory))
		dir.create(targetDirectory)
	
	log("Creating %d individual plots for 'Alive nodes vs. simulation time'...", nrow(configurations))
	for (i in 1:nrow(configurations))
	{
		cat(".")
		configuration = configurations[i,]
		dataPerConfiguration = getDataForConfiguration(configuration)
		suffixPerConfiguration = getSuffixForConfiguration(configuration)
		#log("%s%s%s", TAB, TAB, suffixPerConfiguration)	
		
		pdf(sprintf("%s%s.pdf", targetDirectory, suffixPerConfiguration))
		plot(dataPerConfiguration[[simulationTimeInMinutesKey]], 
				dataPerConfiguration[[nodeCountAliveKey]], 
				xlab="Simulation time [min]", ylab="Number of alive nodes",
				pch=PCH_X, cex=MEDIUM_CEX,
				panel.first = grid(NULL, NULL, lty = GRID_LINE_TYPE, col = GRID_COL))
		dev.off()
	}
	cat("\n")

}

plotAliveNodesVsTime_AlgorithmComparison <- function(){
	checkGlobalParentDir()
	
	configurations = unique(allDataMeanOverSeed[c(cWorldSizeKey,cNodeCountKey,cKtcParameterKKey,cMinimumDistanceThresholdInMetersKey)]);
	targetDirectory = sprintf("%s/output/alive-nodes_vs_simtime_comparison/", parentDir)
	if (!dir.exists(targetDirectory))
		dir.create(targetDirectory)
	
	log("Creating %d comparison plots for 'Alive nodes vs. simulation time'...", nrow(configurations))
	
	outputFileMode = JOIN # JOIN, SINGLE
	for (outputFileMode in c(JOIN, SINGLE))
	{
		cat(outputFileMode)
		joinedOutputFile = sprintf("%sjoined.pdf", targetDirectory)
		if (outputFileMode == JOIN)
			pdf(joinedOutputFile, width=16, height=5)
		
		for (i in 1:nrow(configurations))
		{
			cat(".")
			configuration = configurations[i,]
			suffixPerConfiguration = getSuffixForConfiguration(configuration)
			#log("%s%s%s", TAB, TAB, suffixPerConfiguration)
			singleOutputFile = sprintf("%s%s.pdf", targetDirectory, suffixPerConfiguration)
			
			dataForConfiguration = getDataForConfiguration(configuration)
			maxTime = max(dataForConfiguration[[simulationTimeInMinutesKey]])
			maxNodeCount = max(dataForConfiguration[[nodeCountAliveKey]])
			
			tryCatch({
				if (outputFileMode == SINGLE)
					pdf(singleOutputFile, width=16, height=5)
				
				algos = c(MAXPOWER_ID, KTC_ID, EKTC_ID)
				for (algo in algos)
				{
					configurationForAlgo = configuration
					configurationForAlgo[[cAlgoKey]] = algo
					dataForAlgo = getDataForConfiguration(configurationForAlgo)
									
					if (algo != algos[1])
						par(new=TRUE)		
					plot(dataForAlgo[[simulationTimeInMinutesKey]], 
					        dataForAlgo[[nodeCountAliveKey]], 
					        xlab="Simulation time [min]", ylab="Number of alive nodes",
							xlim=c(0,maxTime), ylim=c(0,maxNodeCount),
					        pch=algoPch(algo), cex=MEDIUM_CEX, col=algoCol(algo),
					        panel.first = grid(NULL, NULL, lty = GRID_LINE_TYPE, col = GRID_COL))
					legend("bottomleft", "Legend", legend=sapply(algos, algoName), col=sapply(algos, algoCol), pch=sapply(algos, algoPch))
					
					if (outputFileMode == JOIN)
						title(suffixPerConfiguration)
				}
			}, finally={if(outputFileMode == SINGLE){closeAllDevices()}});
		}
		
		if (outputFileMode == JOIN)
			closeAllDevices()
		
		cat("\n")
	}

}

createPerformanceCharts <- function()
{
	checkGlobalParentDir()
	
	
	targetDirectory = sprintf("%s/output/performance_charts/", parentDir)
	if (!dir.exists(targetDirectory))
		dir.create(targetDirectory)
	
	# 3 columns, contains all unique rows with WS, NC, k
	configurationsByWS_NC_K = unique(allDataMeanOverSeed[c(cWorldSizeKey,cNodeCountKey,cKtcParameterKKey)]);
	log("Creating %d performance charts...", nrow(configurationsByWS_NC_K))
	# Create one table for each tuple of (world size, node count, k)
	for (i in 1:nrow(configurationsByWS_NC_K))
	{
		cat(".")
		configurationByWS_NC_K = configurationsByWS_NC_K[i,]
		dataForWS_NC_K = getDataForConfiguration(configurationByWS_NC_K)
		suffixPerConfiguration = getSuffixForConfiguration(configurationByWS_NC_K)
		
		worldSize = configurationByWS_NC_K[[cWorldSizeKey]]
		nodeCount = configurationByWS_NC_K[[cNodeCountKey]]
		kParameter = configurationByWS_NC_K[[cKtcParameterKKey]]
		
		options(stringsAsFactors=FALSE) # Do not interpret the column content as factors to allow adding further values later on
		outputTable = data.frame(
				"Algo." = character(0),
				"wThresh" = numeric(0),
				"L1InMinutes" = numeric(0),
				"relL1InMinutes" = numeric(0),
				"L50pInMinutes" = numeric(0),
				"relL50pInMinutes" = numeric(0),
				"L100pInMinutes" = numeric(0),
				"relL100pInMinutes" = numeric(0),
				"MeanTopologySize" = numeric(0),
				"relTopologySize" = numeric(0),
				"MeanExecutionTime" = numeric(0),
				"RelativeExecutionTime" = numeric(0),
				"MeanLSMCount" = numeric(0),
				"RelativeLSMCount" = numeric(0)
		)
		
		# 1 column, contains all unique rows with A for a fixed WS, NC, K
		configurationsByWS_NC_K_A = unique(dataForWS_NC_K[c(cAlgoKey)])
		
		# Create one 'block row' for each algorithm
		intermediateLinePositions = c() # stores the pos. of the \midrule lines
		nextOutputTableRowNumber = 1
		for (j in 1:nrow(configurationsByWS_NC_K_A))
		{
			configurationByWS_NC_K_A = configurationsByWS_NC_K_A[j,,drop=F]
			configurationByWS_NC_K_A = cbind(configurationByWS_NC_K_A, configurationByWS_NC_K) # create a valid complete configuration
			
			algorithmId = configurationByWS_NC_K_A[[cAlgoKey]]
			algorithmName = algoName(algorithmId)
			
			dataForWS_NC_K_A = getDataForConfiguration(configurationByWS_NC_K_A)
			
			# 1 column, contains all unique rows for MW for a fixed WS, NC, K, A 
			configurationsByWS_NC_K_A_MW = unique(dataForWS_NC_K_A[c(cMinimumDistanceThresholdInMetersKey)])
			
			if (algorithmId == MAXPOWER_ID)
			{
				configurationsByWS_NC_K_A_MW = configurationsByWS_NC_K_A_MW[configurationsByWS_NC_K_A_MW[cMinimumDistanceThresholdInMetersKey] == 0, ,drop = F]
			}
			
			# Create one row for each combination of algorithm and weight threshold
			for (k in 1:nrow(configurationsByWS_NC_K_A_MW))
			{
				configurationByWS_NC_K_A_MW = configurationsByWS_NC_K_A_MW[k,,drop=F]
				configurationByWS_NC_K_A_MW = cbind(configurationByWS_NC_K_A_MW, configurationByWS_NC_K_A)
				
				minimumDistanceThresholdInMeters = configurationByWS_NC_K_A_MW[[cMinimumDistanceThresholdInMetersKey]]
										
				dataForWS_NC_K_A_MW = getDataForConfiguration(configurationByWS_NC_K_A_MW)
								
				oneLifetime = max(dataForWS_NC_K_A_MW[[lifetimeOneKey]])
				fiftyPctLifetime = max(dataForWS_NC_K_A_MW[[lifetimeSecondQuartileKey]])
				hundredPctLifetime = max(dataForWS_NC_K_A_MW[[lifetimeAllKey]])
				executionTime = mean(dataForWS_NC_K_A_MW[[incrCETimeInMillisKey]] + dataForWS_NC_K_A_MW[[incrTCTimeInMillisKey]])
				meanTopologySize = mean(dataForWS_NC_K_A_MW[[nodeCountInFacadeKey]] + dataForWS_NC_K_A_MW[[edgeCountInFacadeKey]])
				meanLSMCount = mean(dataForWS_NC_K_A_MW[[incrCELSMCountEffectiveKey]] + dataForWS_NC_K_A_MW[[incrTCLSMCountEffectiveKey]])
				
				if (minimumDistanceThresholdInMeters == 0)
				{
					baselineOneLifetime = oneLifetime
					baselineFirtyPcLifetime = fiftyPctLifetime
					baselineHundredPctLifetime = hundredPctLifetime
					baselineExecutionTime = executionTime
					baselineMeanTopologySize = meanTopologySize
					baselineLSMCount = meanLSMCount
				}
				
				relativeOneLifetime = oneLifetime / baselineOneLifetime
				relativeFirtyPcLifetime = fiftyPctLifetime / baselineFirtyPcLifetime
				relativeHundredPctLifetime = hundredPctLifetime / baselineHundredPctLifetime
				relativeExecutionTime = executionTime / baselineExecutionTime
				relativeMeanTopologySize = meanTopologySize / baselineMeanTopologySize
				relativeLSMCount = meanLSMCount / baselineLSMCount
				
				outputTable[nextOutputTableRowNumber,] = list(
						algorithmName, minimumDistanceThresholdInMeters, 
						oneLifetime, relativeOneLifetime, 
						fiftyPctLifetime, relativeFirtyPcLifetime, 
						hundredPctLifetime, relativeHundredPctLifetime, 
						meanTopologySize, relativeMeanTopologySize, 
						executionTime, relativeExecutionTime,
						meanLSMCount, relativeLSMCount)
				nextOutputTableRowNumber = nextOutputTableRowNumber + 1
			}
			intermediateLinePositions = c(intermediateLinePositions, nextOutputTableRowNumber - 1)
		
		}
		
		write.table(outputTable, file = sprintf("%s/%s.txt", targetDirectory, suffixPerConfiguration), row.names = F, col.names = T)
		colnames(outputTable) = c(
				"Algo.", 
				"\\weightThreshold [m]", 
				"\\remainingLifetime{1}{} [min]",
				"\\relativeRemainingLifetime{1}{}",
				"\\remainingLifetimePct{50}{} [min]",
				"\\relativeRemainingLifetime{50}{}",
				"\\remainingLifetimePct{100}{} [min]",
				"\\relativeRemainingLifetime{100}{}",
				"\\meanTopologySize{}",
				"\\relativeTopologySize{}",
				"\\meanExecutionTime{} [ms]",
				"\\relativeExecutionTime{}",
				"\\meanLsmCount{}",
				"\\relativeLSMCount{}"
				)
		firstTable = outputTable[c(1:8)]
		secondTable = outputTable[c(1,2,c(9:14))]
		alignspecFirstTable = c("r", "c", "r|", "p{1.3cm}","p{0.9cm}", "p{1.3cm}","p{0.9cm}", "p{1.4cm}","p{0.9cm}")
		digitsspecFirstTable = c(rep(0,3), rep(c(1,2),3))
		label = sprintf("tab:eval-chart-ws%d-nc%d-k%.2f", worldSize, nodeCount, kParameter)
		firstLatexTable = xtable(firstTable,
				align = alignspecFirstTable,
				digits = digitsspecFirstTable,
				label=label, 
				caption = sprintf(
"Algorithm performance for different weight thresholds \\weightThreshold (world size: \\SI{%d}{\\meter}, node count: %d, $k = %.2f$).
Lifetime values (\\remainingLifetime{1}{}, \\remainingLifetime{\\SI{50}{\\percent}}{}, \\remainingLifetime{\\SI{100}{\\percent}}{}) are in simulated minutes. 
Execution time $\\overline{\\executionTime{G_{\\weightThreshold}}}$ is in CPU milliseconds.
\\relativeMetric{X}{}: ratio of metrix $X$ compared to simulation run with same algorithm, but $\\weightThreshold = 0$.
The best values for each combination of algorithm, \\weightThreshold, and metric are highlighted in bold font.
", worldSize, nodeCount, kParameter))
		secondLatexTable = xtable(secondTable,
				align = alignspecFirstTable,
				digits = digitsspecFirstTable)
		print(firstLatexTable,
				comment = FALSE,
				file = sprintf("%s/%s.tex", targetDirectory, suffixPerConfiguration),
				hline.after = c(-1, 0, intermediateLinePositions),
				include.rownames=FALSE, include.colnames = T,
				floating = T, caption.placement = "top",   booktabs = T, sanitize.colnames.function = myBold, type="latex")
		print(secondLatexTable,  
				file = sprintf("%s/%s.tex", targetDirectory, suffixPerConfiguration),
				comment = FALSE,
				append = T,
				table.placement = c("H"),
				hline.after = c(-1, 0, intermediateLinePositions),
				include.rownames=FALSE, include.colnames = T,
				floating = T, caption.placement = "top",   booktabs = T, sanitize.colnames.function = myBold, type="latex")
				
	}
	cat("\n")
	
	# Join all generated tex files for easier copy-pasting
	outputFileForJoinedTexFiles = sprintf("%s/joined_tables.tex", targetDirectory);
	if (file.exists(outputFileForJoinedTexFiles))
		file.remove(outputFileForJoinedTexFiles)
	texFiles = list.files(path = targetDirectory, pattern="*.tex", full.names = T)
	lines = c()
	for (texFile in texFiles)
	{
		lines = c(lines, readLines(texFile))
	}
	joinedContent = paste(lines, collapse = "\n")
	joinedContent = gsub("\\\\end.table.\n\\\\begin.table.\\[H\\]", "\n\\vspace{2ex}\\nopagebreak", joinedContent)
	write(joinedContent, outputFileForJoinedTexFiles)
}

myBold <- function(x) {
	sanitizedColnames = sprintf("\\multicolumn{1}{c}{\\textbf{%s}}", x)
	sanitizedColnames[2] = sprintf("\\multicolumn{1}{c|}{\\textbf{%s}}", x[2])
	sanitizedColnames
}

#
# === UTILITY FUNCTIONS ===
#
checkGlobalParentDir <- function()
{
	if (!dir.exists(parentDir))
		throw(sprintf("Parent dir %s does not exist. Stop.", parentDir))
}

getSuffixForConfiguration <- function(configuration) {
	if (hasAllColumnNames(configuration, c(cWorldSizeKey, cNodeCountKey, cKtcParameterKKey, cAlgoKey, cMinimumDistanceThresholdInMetersKey)))
	{
		sprintf("worldSize=%04d_nodeCount=%04d_algo=%02d_kTCParK=%.3f_minDistInM=%.1f", 
			configuration[[cWorldSizeKey]], configuration[[cNodeCountKey]], configuration[[cAlgoKey]], configuration[[cKtcParameterKKey]], configuration[[cMinimumDistanceThresholdInMetersKey]] );
	}
	else if (hasAllColumnNames(configuration, c(cWorldSizeKey, cNodeCountKey, cKtcParameterKKey, cMinimumDistanceThresholdInMetersKey)))
	{
		sprintf("worldSize=%04d_nodeCount=%04d_kTCParK=%.3f_minDistInM=%.1f", 
				configuration[[cWorldSizeKey]], configuration[[cNodeCountKey]], configuration[[cKtcParameterKKey]], configuration[[cMinimumDistanceThresholdInMetersKey]] );
	}
	else if (hasAllColumnNames(configuration, c(cWorldSizeKey, cNodeCountKey, cKtcParameterKKey)))
	{
		sprintf("worldSize=%04d_nodeCount=%04d_kTCParK=%.3f", 
				configuration[[cWorldSizeKey]], configuration[[cNodeCountKey]], configuration[[cKtcParameterKKey]] );
	}
	else
	{
		stop(sprintf("Unsupported configuration with columns [%s]", paste(colnames(configuration), collapse = ', ')))
	}
}

getDataForConfiguration <- function(configuration) {
	if (hasAllColumnNames(configuration, c(cWorldSizeKey, cNodeCountKey, cKtcParameterKKey, cAlgoKey, cMinimumDistanceThresholdInMetersKey)))
	{
		allDataMeanOverSeed[
				allDataMeanOverSeed[[cWorldSizeKey]] == configuration[[cWorldSizeKey]] &
						allDataMeanOverSeed[[cNodeCountKey]] == configuration[[cNodeCountKey]] &
						allDataMeanOverSeed[[cKtcParameterKKey]] == configuration[[cKtcParameterKKey]] & 
						allDataMeanOverSeed[[cAlgoKey]] == configuration[[cAlgoKey]] &
						allDataMeanOverSeed[[cMinimumDistanceThresholdInMetersKey]] == configuration[[cMinimumDistanceThresholdInMetersKey]] 
				, ]
	}
	else if (hasAllColumnNames(configuration, c(cWorldSizeKey, cNodeCountKey, cKtcParameterKKey, cMinimumDistanceThresholdInMetersKey)))
	{
		allDataMeanOverSeed[
				allDataMeanOverSeed[[cWorldSizeKey]] == configuration[[cWorldSizeKey]] &
						allDataMeanOverSeed[[cNodeCountKey]] == configuration[[cNodeCountKey]] &
						allDataMeanOverSeed[[cKtcParameterKKey]] == configuration[[cKtcParameterKKey]] &
						allDataMeanOverSeed[[cMinimumDistanceThresholdInMetersKey]] == configuration[[cMinimumDistanceThresholdInMetersKey]] 
				, ]
	} else if (hasAllColumnNames(configuration, c(cWorldSizeKey, cNodeCountKey, cKtcParameterKKey)))
	{
		allDataMeanOverSeed[
				allDataMeanOverSeed[[cWorldSizeKey]] == configuration[[cWorldSizeKey]] &
						allDataMeanOverSeed[[cNodeCountKey]] == configuration[[cNodeCountKey]] &
						allDataMeanOverSeed[[cKtcParameterKKey]] == configuration[[cKtcParameterKKey]]
				, ]
	} else 
	{
		stop(sprintf("Unsupported configuration with columns [%s]", paste(colnames(configuration), collapse = ', ')))
	}
}

hasAllColumnNames <- function(dataframe, colnames)
{
	hasAllColumnNames = TRUE
	for (colname in colnames)
	{
		hasAllColumnNames = hasAllColumnNames & any(grep(colname, colnames(dataframe)))
		if (!hasAllColumnNames)
			break
	}
	hasAllColumnNames
}

closeAllDevices <- function() {
	while (dev.cur() != 1) {
		dev.off()
	}
}

prepareOutputDirectory <- function() {
	checkGlobalParentDir()
	
	dirName = sprintf("%s/output", parentDir)
	sapply(list.files(dirName, patter="*.pdf$"), unlink)
	sapply(list.files(dirName, patter="*.txt$"), unlink)
	sapply(list.files(dirName, patter="*.csv$"), unlink)
	#unlink(dirName, T, F)
	dir.create(dirName, showWarnings=F)
}

main()