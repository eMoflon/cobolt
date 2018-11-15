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

# Install via: install.packages("RColorBrewer", dependencies=T)
library(RColorBrewer)

source("./cfg_colnames.R")
source("./cfg_scenarios.R")

RCEScopeKey = "RCEScope"
RCEScopeNormalizedByNodeOutdegreeKey = "RCEScopeNormalizedByNodeOutdegree"
RIncrVsBatchTCOnlyInLSMsKey = "RIncrVsBatchTCOnlyInLSMs"
RIncrVsBatchTCAndCEInLSMsKey = "RIncrVsBatchTCAndCEInLSMs"
RTopologySizeKey = "RTopologySize"
RTotalTimeInMinutesForGTKey = "RTotalTimeInMinutesForGT"

TAB="  "
PCH_X= 120
GRID_COL = "lightgray"
INCR_VS_BATCH_BREAK_EVEN_COLOR = "darkgreen"
INCR_VS_BATCH_BREAK_EVEN_WIDTH = 3
GRID_LINE_TYPE = 6
CSV_SEP = ";"
SMALL_CEX = 0.3
MEDIUM_CEX = 1.0
ALGORITHMS = c("ID_KTC")
NICE_ALGORITHMS = c("No TC", "kTC", "e-kTC")

FILENAME_PATTERN_AGGREGATED_DATA="%s/output/aggregated.csv" # parentDir, algo
FILENAME_PATTERN_JOINED_DATA="%s/output/joined.csv" # parentDir, algo

ROOT="../../../../output/rkluge/jvlc/"

currentRunRootDirectory = NULL
performanceComparisonTable = NULL
scalabilityTable = NULL

joinedDataCache = list()
averageDataCache = list()

main <- function() {
    allDirs = list.dirs(ROOT, recursive = F)
	runDirectories = allDirs[grepl('^.*/batchrun_.*$', allDirs)]
	sapply(runDirectories, processRun)
    print("Done.")
}

processRun <- function(parentDir) {
	print(sprintf("Run directory: %s", parentDir))
	currentRunRootDirectory <<- parentDir
	prepareOutputDirectory(parentDir)
	allDirs = list.dirs(parentDir, recursive = F)
	configurationDirs = allDirs[grepl('^.*/run_.*$', allDirs)]
	sapply(configurationDirs, processConfiguration)
}

#
# This function processes one run at a time
processConfiguration <- function(parentDir)
{
    print(sprintf("Processing configuration %s", parentDir))
    prepareOutputDirectory(parentDir)
	aggregateResultsOfOneConfiguration(parentDir)
	findFailedSimulationRuns(parentDir)
	listConstraintViolations(parentDir)
    evaluateIncrementality(parentDir)
    evaluatePerformance(parentDir)
}

prepareOutputDirectory <- function(parentDir) {
	dirName = sprintf("%s/output", parentDir)
	sapply(list.files(dirName, patter="*.pdf$"), unlink)
	sapply(list.files(dirName, patter="*.txt$"), unlink)
	#unlink(dirName, T, F)
	dir.create(dirName, showWarnings=F)
}

findFailedSimulationRuns <- function(parentDir) {
	filenames = list.files(path=sprintf("%s", parentDir), pattern="*.csv.log$", full.names=T)
	for (file in filenames)
	{
		content = paste(readLines(file), collapse=" ")
		foundFailure = grepl("with errors", content, T)
		if (foundFailure)
		{
			print(sprintf("%s%sFailed:     %s", TAB, TAB, file))
		}
		
		foundFinished = grepl("Simulation successfully finished", content, T)
		if (!foundFinished)
		{
			print(sprintf("%s%sUnfinished: %s", TAB, TAB, file))
		}
	}
}

listConstraintViolations <- function(parentDir)
{
	print(sprintf("%sRQ1: Listing constraint violations", TAB, parentDir)) 
	
	filenames = list.files(path=sprintf("%s", parentDir), pattern="*.csv$", full.names=T)
	
	totalViolationCount = 0
	totalRowCount = 0
	
	for (file in filenames)
	{
		dataSet = read.csv(file, sep = CSV_SEP)
		violationCount = max(dataSet$incrTCViolationCount) # + max(dataSet$)
		totalViolationCount = totalViolationCount + violationCount
		totalRowCount = totalRowCount + nrow(dataSet)
		
		if (violationCount > 0)
		{
			print(sprintf("%s%s%s%s has %d violations", TAB, TAB, TAB, file, count))
		}
	}
	
	print(sprintf("%s%sSummary for %s: %s violations in %s rows.", TAB, TAB, parentDir, totalViolationCount, totalRowCount))
}

#
# Creates for each scenario one table with all data points in it
#
aggregateResultsOfOneConfiguration <- function(parentDir)
{
    print(sprintf("%sAggregating results of individual simulations", TAB, parentDir))  
    
    filenames = list.files(path=sprintf("%s", parentDir), pattern="*.csv$", full.names=T)
	
	worldSize = extractWorldSizeFromConfigDirName(parentDir)
	nodeCount = extractNodeCountFromConfigDirName(parentDir)
	
	if (is.na(worldSize))
		stop(sprintf("Could not parse worldSize from parent dir %s", parentDir))
	if (is.na(nodeCount))
		stop(sprintf("Could not parse nodeCount from parent dir %s", parentDir))
	
	print(sprintf("%s%sn=%d, ws=%d => scenario=%s", TAB, TAB, nodeCount, worldSize, lookupScenario(nodeCount, worldSize)))
	
	joinedDataOutputFile = sprintf(FILENAME_PATTERN_JOINED_DATA, parentDir)
	averageDataOutputFile = sprintf(FILENAME_PATTERN_AGGREGATED_DATA, parentDir)
	
	sourceFileMTime = max(file.mtime(filenames))
	targetFileMTime = file.mtime(joinedDataOutputFile)
	
	# DISABLED
	if (F && sourceFileMTime < targetFileMTime) 
	{
		print(sprintf("%s%sUsing cached version of data sets", TAB, TAB));
		joinedDataCache[[parentDir]] = read.table(joinedDataOutputFile, sep=CSV_SEP,head=T)
		averageDataCache[[parentDir]] = read.table(averageDataOutputFile, sep=CSV_SEP,head=T)
		return
	}
	            
    dataFromAllFiles = lapply(filenames, read.csv, sep = CSV_SEP)
    minRowCount = min(unlist(lapply(dataFromAllFiles, "nrow")))
    dataFromAllFiles = lapply(dataFromAllFiles, '[', c(1:minRowCount), )  # parameters of '[': row range i, col range j
    
    averageDataFromAllFiles = Reduce("+", dataFromAllFiles) / length(dataFromAllFiles)
	
	nodeOutdegreeAvg = getColumn(averageDataFromAllFiles, nodeOutdegreeAvgKey)
	nodeCountAlive = getColumn(averageDataFromAllFiles, nodeCountAliveKey)
	edgeCount = getColumn(averageDataFromAllFiles, edgeCountTotalKey)
	incrCERuleCountTotal = getColumn(averageDataFromAllFiles, incrCERuleCountTotalKey)
	incrCELSMCountTotal=getColumn(averageDataFromAllFiles, incrCELSMCountEffectiveKey)
	incrTCLSMCountTotal=getColumn(averageDataFromAllFiles, incrTCLSMCountEffectiveKey)
	batTCLSMCountTotal = getColumn(averageDataFromAllFiles, batTCLSMCountTotalKey)
	
	incrTCTimeInMillis = getColumn(averageDataFromAllFiles, incrTCTimeInMillisKey)
	incrTCCheckTimeInMillis = getColumn(averageDataFromAllFiles, incrTCCheckTimeInMillisKey)
	incrCETimeInMillis = getColumn(averageDataFromAllFiles, incrCETimeInMillisKey)
	incrCECheckTimeInMillis = getColumn(averageDataFromAllFiles, incrCECheckTimeInMillisKey)
	
	averageDataFromAllFiles[[RCEScopeKey]] = (incrCELSMCountTotal + incrTCLSMCountTotal) / incrCERuleCountTotal
	averageDataFromAllFiles[[RCEScopeNormalizedByNodeOutdegreeKey]] = averageDataFromAllFiles[[RCEScopeKey]] / nodeOutdegreeAvg
	averageDataFromAllFiles[[RIncrVsBatchTCOnlyInLSMsKey]] = incrTCLSMCountTotal / batTCLSMCountTotal
	averageDataFromAllFiles[[RIncrVsBatchTCAndCEInLSMsKey]] = (incrTCLSMCountTotal + incrCELSMCountTotal) / batTCLSMCountTotal
	averageDataFromAllFiles[[RTopologySizeKey]] = nodeCountAlive + edgeCount
	averageDataFromAllFiles[[RTotalTimeInMinutesForGTKey]] = (incrTCTimeInMillis + incrTCCheckTimeInMillis + incrCETimeInMillis + incrCECheckTimeInMillis) / 1000 / 60
	
    
    write.table(averageDataFromAllFiles, averageDataOutputFile, sep=CSV_SEP, append=F, row.names=F)     
	
	
    
    joinedDataFromAllFiles = Reduce("rbind", lapply(dataFromAllFiles, '[',))
	
	nodeOutdegreeAvg = getColumn(joinedDataFromAllFiles, nodeOutdegreeAvgKey)
	nodeCountAlive = getColumn(joinedDataFromAllFiles, nodeCountAliveKey)
	edgeCount = getColumn(joinedDataFromAllFiles, edgeCountTotalKey)
	incrCERuleCountTotal = getColumn(joinedDataFromAllFiles, incrCERuleCountTotalKey)
	incrCELSMCountTotal=getColumn(joinedDataFromAllFiles, incrCELSMCountEffectiveKey)
	incrTCLSMCountTotal=getColumn(joinedDataFromAllFiles, incrTCLSMCountEffectiveKey)
	batTCLSMCountTotal = getColumn(joinedDataFromAllFiles, batTCLSMCountTotalKey)
	
	incrTCTimeInMillis = getColumn(joinedDataFromAllFiles, incrTCTimeInMillisKey)
	incrTCCheckTimeInMillis = getColumn(joinedDataFromAllFiles, incrTCCheckTimeInMillisKey)
	incrCETimeInMillis = getColumn(joinedDataFromAllFiles, incrCETimeInMillisKey)
	incrCECheckTimeInMillis = getColumn(joinedDataFromAllFiles, incrCECheckTimeInMillisKey)
	
	
	joinedDataFromAllFiles[[RCEScopeKey]] = (incrCELSMCountTotal + incrTCLSMCountTotal) / incrCERuleCountTotal
	joinedDataFromAllFiles[[RCEScopeNormalizedByNodeOutdegreeKey]] = joinedDataFromAllFiles[[RCEScopeKey]] / nodeOutdegreeAvg
	joinedDataFromAllFiles[[RIncrVsBatchTCOnlyInLSMsKey]] = incrTCLSMCountTotal / batTCLSMCountTotal
	joinedDataFromAllFiles[[RIncrVsBatchTCAndCEInLSMsKey]] = (incrTCLSMCountTotal + incrCELSMCountTotal) / batTCLSMCountTotal
	joinedDataFromAllFiles[[RTopologySizeKey]] = nodeCountAlive + edgeCount
	joinedDataFromAllFiles[[RTotalTimeInMinutesForGTKey]] = (incrTCTimeInMillis + incrTCCheckTimeInMillis + incrCETimeInMillis + incrCECheckTimeInMillis) / 1000 / 60
	
    write.table(joinedDataFromAllFiles, joinedDataOutputFile, sep=CSV_SEP, append=F, row.names=F)     

	joinedDataCache[[parentDir]] <<- joinedDataFromAllFiles
	averageDataCache[[parentDir]] <<- averageDataFromAllFiles
	
	# Clean up variables
    #remove(filenames, dataFromAllFiles, averageDataFromAllFiles)

}

extractNodeCountFromConfigDirName <- function(dir) {
	as.numeric(regmatches(dir,regexec("_nodeCount=(\\d+)_", dir))[[1]][2])
}

extractWorldSizeFromConfigDirName <- function(dir) {
	as.numeric(regmatches(dir,regexec("_worldSize=(\\d+)_",dir))[[1]][2])
}

getColumn <- function(df, colName)
{
	pattern = sprintf("\\<%s\\>", colName)
	matchesForColumnName = grepl(pattern, colnames(df))
	
	if(!any(matchesForColumnName))
		stop(sprintf("No column named %s. Available column names: %s", colName, paste(colnames(df), collapse=", ")))
	
	numberOfMatches = length(matchesForColumnName[matchesForColumnName==T])
	if (numberOfMatches > 1)
		stop(sprintf("Match for %s is not unique. Matching columns: %s", colName, paste(colnames(df)[matchesForColumnName], collapse=", ")))
	
	result = df[,matchesForColumnName]
	return(result)
}

evaluateIncrementality <- function(parentDir)
{
    print(sprintf("%sRQ2: Evaluating incrementality", TAB, parentDir))  
	RQ_PREFIX = sprintf("%s/output/RQ2", parentDir) # was: rq2_incr_

    joinedDataFromAllFiles = joinedDataCache[[parentDir]] # read.table(sprintf(FILENAME_PATTERN_JOINED_DATA, parentDir), sep=CSV_SEP,head=T)
	rowsToKeep = joinedDataFromAllFiles$simulationTimeInMinutes != 10 # skip first iteration
	joinedDataFromAllFiles = joinedDataFromAllFiles[rowsToKeep, ]
	
	averageDataFromAllFiles = averageDataCache[[parentDir]]
	rowsToKeep = averageDataFromAllFiles$simulationTimeInMinutes != 10
	averageDataFromAllFiles = averageDataFromAllFiles[rowsToKeep, ]
    
	worldSize = extractWorldSizeFromConfigDirName(parentDir)
	nodeCount= extractNodeCountFromConfigDirName(parentDir)
	
	dataset = averageDataFromAllFiles # joinedDataFromAllFiles
	
	nodeOutdegreeAvg = getColumn(dataset, nodeOutdegreeAvgKey)
    nodeCountAlive = getColumn(dataset, nodeCountAliveKey)
	incrCERuleCountTotal = getColumn(dataset, incrCERuleCountTotalKey)
	incrCELSMCountTotal=getColumn(dataset, incrCELSMCountTotalKey)
	incrTCLSMCountTotal=getColumn(dataset, incrTCLSMCountTotalKey)
	incrCELSMCountEffective=getColumn(dataset, incrCELSMCountEffectiveKey)
	incrTCLSMCountEffective=getColumn(dataset, incrTCLSMCountEffectiveKey)
	batTCLSMCountTotal = getColumn(dataset, batTCLSMCountTotalKey)
	ceScope = getColumn(dataset, RCEScopeKey)
	ceScopeNormalizedByNodeOutdegree = getColumn(dataset, RCEScopeNormalizedByNodeOutdegreeKey)
	ceScopeNormalizedByNodeOutdegree = getColumn(dataset, RCEScopeNormalizedByNodeOutdegreeKey)
	incrVsBatchTCOnlyInLSMs = getColumn(dataset, RIncrVsBatchTCOnlyInLSMsKey)
	incrVsBatchTCAndCEInLSMs = getColumn(dataset, RIncrVsBatchTCAndCEInLSMsKey)
	
	
	# IN PAPER
	tryCatch({pdf(sprintf("%sNormScopeVsNodes.pdf", RQ_PREFIX))
	# scope-normalized-by-node-outdgree_vs_node-count.pdf
	plot(nodeCountAlive, ceScopeNormalizedByNodeOutdegree, 
			xlab="Number of alive nodes", ylab="Degree-normalized scope [LSM / link]",
			pch=PCH_X, cex=MEDIUM_CEX,
			panel.first = grid(NULL, NULL, lty = GRID_LINE_TYPE, col = GRID_COL)
	)
	}, finally={dev.off()})
	capture.output(summary(ceScope), file = sprintf("%sNormScopeVsNodesSummary.txt", RQ_PREFIX))
	
	tryCatch({pdf(sprintf("%sNormScopeVsNodesLog.pdf", RQ_PREFIX))
	# scope-normalized-by-node-outdgree_vs_node-count_log.pdf
	plot(nodeCountAlive[ceScopeNormalizedByNodeOutdegree > 0],
			ceScopeNormalizedByNodeOutdegree[ceScopeNormalizedByNodeOutdegree > 0], 
			xlab="Number of alive nodes", ylab="Degree-normalized scope [LSM / link]",
			#yaxt="n",
			log="y", ylim=c(0.01, 10),
			pch=PCH_X, cex=MEDIUM_CEX,
			#panel.first = grid(NULL, NULL, lty = GRID_LINE_TYPE, col = GRID_COL)
			panel.first = grid(equilogs=FALSE, lty = GRID_LINE_TYPE, col = GRID_COL)
	)
	#log10.axis(2,c(-2,2,2))
	
	
	
	filter = is.finite(ceScopeNormalizedByNodeOutdegree)
	regression = lm(nodeCountAlive[filter]~ceScopeNormalizedByNodeOutdegree[filter])
	capture.output(summary(regression), file = sprintf("%sNormScopeVsNodesLinReg.txt", RQ_PREFIX))
	
	correlation = cor(nodeCountAlive[filter], ceScopeNormalizedByNodeOutdegree[filter], method="spearman")
	capture.output(summary(correlation), file = sprintf("%ssNormScopeVsNodesSpearman.txt", RQ_PREFIX))
	
	}, finally={dev.off()})
	capture.output(summary(ceScope), file = sprintf("%sNormScopeVsNodesSummary.txt", RQ_PREFIX))
	
	
	# IN PAPER
	tryCatch({pdf(sprintf("%sScopeVsNodes.pdf", RQ_PREFIX)) #scope_vs_node-count.pdf
	plot(nodeCountAlive, ceScope, 
			xlab="Number of alive nodes", ylab="Scope [LSM]", 
			pch=PCH_X, cex=MEDIUM_CEX,
			panel.first = grid(NULL, NULL, lty = GRID_LINE_TYPE, col = GRID_COL)
	)
	}, finally={dev.off()})
	capture.output(summary(ceScope), file = sprintf("%sScopeVsNodesSummary.txt", RQ_PREFIX))
	
	
	# IN PAPER
	tryCatch({pdf(sprintf("%sIncBatchVsNodes.pdf", RQ_PREFIX)) #i-ktc-vs-b-ktc_vs_node-count-alive.pdf
	plot(nodeCountAlive, incrVsBatchTCAndCEInLSMs, type="p", 
			xlab="Number of alive nodes", ylab="i-kTC-to-b-kTC LSM ratio [LSM/LSM]", 
			pch=PCH_X, cex=MEDIUM_CEX,
			panel.first=grid(NULL, NULL, lty = GRID_LINE_TYPE, col = GRID_COL)
	) 
	abline(h=1, col=INCR_VS_BATCH_BREAK_EVEN_COLOR, lwd=INCR_VS_BATCH_BREAK_EVEN_WIDTH)
	}, finally={dev.off()})
	
	tryCatch({pdf(sprintf("%sIncBatchVsNodesLog.pdf", RQ_PREFIX)) # i-ktc-vs-b-ktc_vs_node-count-alive_log
	plot(nodeCountAlive[incrVsBatchTCAndCEInLSMs > 0], 
			incrVsBatchTCAndCEInLSMs[incrVsBatchTCAndCEInLSMs > 0],
			type="p",  ylim=c(0.001, 13),
			xlab="Number of alive nodes", ylab="i-kTC-to-b-kTC LSM ratio [LSM/LSM]", 
			pch=PCH_X, cex=MEDIUM_CEX,
			log="y",
			#panel.first=grid(NULL, NULL, lty = GRID_LINE_TYPE, col = GRID_COL)
			panel.first = grid(equilogs=FALSE, lty = GRID_LINE_TYPE, col = GRID_COL)
	) 
	abline(h=1, col=INCR_VS_BATCH_BREAK_EVEN_COLOR, lwd=INCR_VS_BATCH_BREAK_EVEN_WIDTH)
	}, finally={dev.off()})
	
	lengthLessThanOne = length(incrVsBatchTCAndCEInLSMs[(!is.na(incrVsBatchTCAndCEInLSMs)) & (incrVsBatchTCAndCEInLSMs < 1.0)])
	lengthEqualOne = length(incrVsBatchTCAndCEInLSMs[(!is.na(incrVsBatchTCAndCEInLSMs)) & (incrVsBatchTCAndCEInLSMs == 1.0)])
	lengthMoreThanOne = length(incrVsBatchTCAndCEInLSMs[(!is.na(incrVsBatchTCAndCEInLSMs)) & (incrVsBatchTCAndCEInLSMs > 1.0)])
	lengthNA = length(incrVsBatchTCAndCEInLSMs[(is.na(incrVsBatchTCAndCEInLSMs))])
	total = length(incrVsBatchTCAndCEInLSMs)
	line = sprintf("%s%s%s[n=%.0f,w=%.0f]i-kTC vs. b-kTC: <1 : %d (%.1f%%), ==1: %d (%.1f%%), >1: %d (%.1f%%), is.na: %d (%.1f%%), total: %d", TAB, TAB, TAB,
					nodeCount, worldSize,
					lengthLessThanOne, lengthLessThanOne / total * 100,
					lengthEqualOne, lengthEqualOne / total * 100,		
					lengthMoreThanOne, lengthMoreThanOne / total * 100,
					lengthNA, lengthNA / total * 100, 
					total
			)
	
	cat(line, file=sprintf("%s/%s/IncBatch.txt", currentRunRootDirectory, "output"), sep="\n", append=TRUE)
		
	tryCatch({pdf(sprintf("%sScopeVsNodesLog.pdf", RQ_PREFIX)) # scope_vs_node-count_log
	plot(nodeCountAlive, ceScope, 
			xlab="Number of alive nodes", ylab="Scope [LSM]", pch=PCH_X, cex=SMALL_CEX,
			panel.first = grid(NULL, NULL, lty = GRID_LINE_TYPE, col = GRID_COL)
	)
	}, finally={dev.off()})
	
    tryCatch({pdf(sprintf("%sOutdegreeVsNodes.pdf", RQ_PREFIX)) #average-outdegree_vs_node-count.pdf
    plot(nodeCountAlive, nodeOutdegreeAvg, 
        xlab="Number of alive nodes", ylab="Average node out-degree", pch=PCH_X, cex=SMALL_CEX,
        panel.first = grid(NULL, NULL, lty = GRID_LINE_TYPE, col = GRID_COL)
    )
    }, finally={dev.off()})
    
	
	
    tryCatch({pdf(sprintf("%sCELsmVsRules.pdf", RQ_PREFIX)) # ce-lsms_vs_ce-rule-count.pdf
    plot(incrCERuleCountTotal, incrCELSMCountTotal, 
        xlab="Number of rule applications during CE handling", ylab="Number of LSMs during CE handling", pch=PCH_X, cex=SMALL_CEX,
        panel.first = grid(NULL, NULL, lty = GRID_LINE_TYPE, col = GRID_COL)
    )
    
    }, finally={dev.off()})
    
    
	
	

	

    tryCatch({pdf(sprintf("%sTCLsmVsCELsm.pdf", RQ_PREFIX)) # tc-lsms_vs_ce-lsms-total.pdf
    plot(incrCELSMCountTotal, incrTCLSMCountTotal, type="p", 
        xlab="Number of LSMs during CE handling", ylab="Number of LSMs during TC",
		pch=PCH_X, cex=SMALL_CEX,
        panel.first=grid(NULL, NULL, lty = GRID_LINE_TYPE, col = GRID_COL)
    ) 
    }, finally={dev.off()})
	
	
	tryCatch({pdf(sprintf("%sTCLsmVsCELsmEff.pdf", RQ_PREFIX)) # tc-lsms_vs_ce-lsms-effective.pdf
	plot(incrCELSMCountEffective, incrTCLSMCountEffective, type="p", 
			xlab="Number of LSMs during CE handling", ylab="Number of LSMs during TC",
			pch=PCH_X, cex=SMALL_CEX,
			panel.first=grid(NULL, NULL, lty = GRID_LINE_TYPE, col = GRID_COL)
	) 
	}, finally={dev.off()})
	
	
    
    tryCatch({pdf(sprintf("%sTCLsmVsCERules.pdf", RQ_PREFIX)) # tc-lsms_vs_ce-rule-count.pdf
    plot(incrCERuleCountTotal, incrTCLSMCountTotal, type="p", 
        xlab="Number of CE rule applications", ylab="Number of LSMs during TC",
		pch=PCH_X, cex=SMALL_CEX,
        panel.first=grid(NULL, NULL, lty = GRID_LINE_TYPE, col = GRID_COL)
    ) 
    }, finally={dev.off()})
    	
	tryCatch({pdf(sprintf("%sIncBatVsCERules.pdf", RQ_PREFIX)) # i-ktc-vs-b-ktc_vs_ce-rule-count.pdf
	plot(incrCERuleCountTotal, incrVsBatchTCAndCEInLSMs, type="p", 
			xlab="Number of rule applications during CE handling", ylab="i-kTC-to-b-kTC LSM ratio [LSM/LSM]",
			pch=PCH_X, cex=SMALL_CEX,
			panel.first=grid(NULL, NULL, lty = GRID_LINE_TYPE, col = GRID_COL)
	)
	abline(h=1, col=INCR_VS_BATCH_BREAK_EVEN_COLOR, lwd=INCR_VS_BATCH_BREAK_EVEN_WIDTH)
	}, finally={dev.off()})
	capture.output(
			summary(incrVsBatchTCAndCEInLSMs), 
			sprintf("%sIncBatVsCERulesSummary.txt", RQ_PREFIX))
	
## tryCatch({pdf(sprintf("%si-ktc-vs-b-ktc_vs_ce-lsms.pdf", RQ_PREFIX))
## plot(incrCELSMCountTotal, incrVsBatchTCAndCEInLSMs, type="p", 
##         xlab="Number of LSMs during CE handling", ylab="i-kTC-to-b-kTC LSM ratio [LSM/LSM]", pch=PCH_X, cex=SMALL_CEX,
##         panel.first=grid(NULL, NULL, lty = GRID_LINE_TYPE, col = GRID_COL)
## )
## abline(h=1, col=INCR_VS_BATCH_BREAK_EVEN_COLOR, lwd=INCR_VS_BATCH_BREAK_EVEN_WIDTH)
## }, finally={dev.off()})
## 
## 
## maxLSMCountOfIncrAndBatch = max(max(batTCLSMCountTotal), max(incrTCLSMCountTotal + incrCELSMCountTotal))
## tryCatch({pdf(sprintf("%si-ktc_vs_b-ktc.pdf", RQ_PREFIX))
## plot(batTCLSMCountTotal, incrTCLSMCountTotal + incrCELSMCountTotal, type="p", 
##         xlab="Number of LSMs for b-kTC", ylab="Number of LSMs for i-kTC", pch=PCH_X, cex=SMALL_CEX,
##         xlim=c(0, maxLSMCountOfIncrAndBatch), ylim=c(0, maxLSMCountOfIncrAndBatch),
##         panel.first=grid(NULL, NULL, lty = GRID_LINE_TYPE, col = GRID_COL)
## ) 
## abline(coef=c(0,1), col=INCR_VS_BATCH_BREAK_EVEN_COLOR, lwd=INCR_VS_BATCH_BREAK_EVEN_WIDTH)
## }, finally={dev.off()})
}

evaluatePerformance <- function(parentDir) {
	print(sprintf("%sRQ3: Evaluating performance", TAB, parentDir))
	RQ_PREFIX = sprintf("%s/output/RQ3", parentDir) # rq3_perf_
	
	averageData = averageDataCache[[parentDir]] # read.table(sprintf(FILENAME_PATTERN_AGGREGATED_DATA, parentDir), sep=CSV_SEP, header=T)
	joinedDataFromAllFiles = joinedDataCache[[parentDir]] #read.table(sprintf(FILENAME_PATTERN_JOINED_DATA, parentDir), sep=CSV_SEP,head=T)
	
	dataset = averageData
	
	simulationTimeInMinutes = getColumn(dataset, totalTimeInMinutesKey)
	incrTCTimeInMillis = getColumn(dataset, incrTCTimeInMillisKey)
	incrTCCheckTimeInMillis = getColumn(dataset, incrTCCheckTimeInMillisKey)
	incrCETimeInMillis = getColumn(dataset, incrCETimeInMillisKey)
	incrCECheckTimeInMillis = getColumn(dataset, incrCECheckTimeInMillisKey)
	
	totalDurationTCInMinutes = sum(incrTCTimeInMillis) / 1000 / 60
	totalDurationTCCheckInMinutes = sum(incrTCCheckTimeInMillis) / 1000 / 60
	totalDurationCEInMinutes = sum(incrCETimeInMillis) / 1000 / 60
	totalDurationCECheckTimeInMinutes = sum(incrCECheckTimeInMillis) / 1000 / 60
	totalDurationOfSimulationOnly = max(simulationTimeInMinutes) - totalDurationCEInMinutes - totalDurationTCInMinutes - totalDurationCECheckTimeInMinutes - totalDurationTCCheckInMinutes
	
	scenario = lookupScenarioBasedOnDirectory(parentDir)
	
	performanceDataframe = data.frame(
			scenario=c(totalDurationCEInMinutes, totalDurationCECheckTimeInMinutes, totalDurationTCInMinutes, totalDurationTCCheckInMinutes, totalDurationOfSimulationOnly))
	colnames(performanceDataframe) = c(scenario)
	rownames(performanceDataframe) = c("CE", "CE check", "TC", "TC check", "Simulation")
	performanceMatrix = matrix(performanceDataframe[[scenario]], ncol=1)
	
	barplotColors = brewer.pal(nrow(performanceDataframe), "BuGn")
	
	tryCatch({pdf(sprintf("%sproportion_of_runtime.pdf", RQ_PREFIX))
	barplot(performanceMatrix, 
		ylab="CPU time [minutes]", xlab="Configuration", names.arg=colnames(performanceDataframe),
		xlim = c(0,20), width = 1,
		col = barplotColors
	)
	legend("bottomright", 
			legend = rownames(performanceDataframe), #in order from top to bottom
			fill = barplotColors[nrow(performanceDataframe):1], # reorders so legend order matches graph
			title = "Step")
	}, finally={dev.off()})
	write.table(performanceDataframe, sprintf("%scputime_distribution.csv", RQ_PREFIX), sep=CSV_SEP, row.names = T, col.names = NA)
	
	if (scenario != UNKNOWN_SCENARIO)
	{
		# Add column to global performance comparion table
		if (is.null(performanceComparisonTable))
		{
			print(sprintf("%s%sCreating performance table...", TAB, TAB))
			performanceComparisonTable <<- data.frame(row.names=rownames(performanceDataframe))
		}
		performanceComparisonTable[[scenario]] <<- performanceDataframe[[scenario]]
			
		write.table(performanceComparisonTable, sprintf("%s/%s/performance_comparison.csv", currentRunRootDirectory, "output"), sep=CSV_SEP, row.names = T, col.names = NA)
		write.table(t(performanceComparisonTable), sprintf("%s/%s/performance_comparison-transposed.csv", currentRunRootDirectory, "output"), sep=CSV_SEP, row.names = T, col.names = NA)
		
		performanceComparisonMatrix = as.matrix(performanceComparisonTable)
		performanceComparisonMatrix = apply(performanceComparisonMatrix, 2, as.numeric)
		
		barplotColors <- brewer.pal(nrow(performanceComparisonMatrix), "BuGn")
	
		for (mode in c("beside", "stacked"))
		{
			tryCatch({pdf(sprintf("%s/%s/performance_comparions_%s.pdf", currentRunRootDirectory, "output", mode))
			options(scipen = 999) # force *non-*scientific formatting
			bp = barplot(performanceComparisonMatrix, 
					ylab="CPU time [minutes]", xlab="Configuration", 
					names.arg=gsub("w", "\nw", colnames(performanceComparisonTable)),
					log="y",
					#cex.axis = 0.7, cex.lab = 0.7,
					#xlim = c(0,20), width = 1,
					beside = (mode == "beside"),
					col = barplotColors[nrow(performanceComparisonTable):1],
					space= if (mode == "beside") c(0,3) else 2
			)
			# Tried to add the concrete values to the columns
			#text(bp, 0, round(performanceComparisonMatrix), cex = 1, pos=3, las = 2)
			legend("topleft", 
					legend = rownames(performanceComparisonTable)[c(nrow(performanceComparisonTable):1)], #in order from top to bottom
					fill = barplotColors, # reorders so legend order matches graph
					title = "Task")
			}, finally={dev.off()})
		}	
			
		scalabilityTableColnames = c(nodeCountAliveKey, edgeCountTotalKey, RTopologySizeKey, totalTimeInMinutesKey, RTotalTimeInMinutesForGTKey, incrTCTimeInMillisKey, incrTCCheckTimeInMillisKey, incrCETimeInMillisKey, incrCECheckTimeInMillisKey)
		
		if (is.null(scalabilityTable))
		{
			print(sprintf("%s%sCreating scalability table...", TAB, TAB))
			scalabilityTable <<- read.csv(text="", col.names=scalabilityTableColnames)
		}
		
		additionalScalabilityData = joinedDataFromAllFiles[scalabilityTableColnames]
		scalabilityTable <<- rbind(scalabilityTable, additionalScalabilityData)
		tryCatch({
			pdf(sprintf("%s/%s/scalability.pdf", currentRunRootDirectory, "output"))
			plot(scalabilityTable[[RTopologySizeKey]], scalabilityTable[[RTotalTimeInMinutesForGTKey]]*60, type="p", 
					xlab="Topology size [node count + link count]", ylab="Execution time of TC+CE (incl. consistency checks) [s]", pch=PCH_X, cex=SMALL_CEX,
					panel.first=grid(NULL, NULL, lty = GRID_LINE_TYPE, col = GRID_COL)
			) 
		}, finally={dev.off()})
		# None of the regressions produced meaningful results (R^2 was around 0.2-0.25)
		#print(summary(lm(scalabilityTable[[RTopologySizeKey]]~scalabilityTable[[RTotalTimeInMinutesForGTKey]])))
		#print(summary(lm(scalabilityTable[[RTopologySizeKey]]^2~scalabilityTable[[RTotalTimeInMinutesForGTKey]])))
		#print(summary(lm(scalabilityTable[[RTopologySizeKey]]^3~scalabilityTable[[RTotalTimeInMinutesForGTKey]])))
		
		write.table(scalabilityTable, sprintf("%s/%s/scalability.csv", currentRunRootDirectory, "output"), sep=CSV_SEP, row.names = F)
		
	}
}

# side: 1:bottom, 2:left, 3:top, 4:right
log10.axis <- function(side, at, ...) {
	at.minor <- log10(outer(1:9, 10^(min(at):max(at))))
	lab <- sapply(at, function(i) as.expression(bquote(10^ .(i))))
	axis(side=side, at=at.minor, labels=NA, tcl=par("tcl")*0.5, ...)
	axis(side=side, at=at, labels=lab, ...)
}

main()