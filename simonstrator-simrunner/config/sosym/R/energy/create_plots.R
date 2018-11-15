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

TAB="  "
PCH_X= 120
GRID_COL = "lightgray"
GRID_LINE_TYPE = 6
CSV_SEP = ";"
SMALL_CEX = 0.3
MEDIUM_CEX = 1.0

FILENAME_PATTERN_AGGREGATED_DATA="%s/output/aggregated.csv" # parentDir, algo
FILENAME_PATTERN_JOINED_DATA="%s/output/joined.csv" # parentDir, algo

ROOT="../../../../../output/rkluge/sosym/energy"

COL_DIST="distanceInMeters"
COL_ENERGY="consumedEnergyInJoule"
COL_TIME="timeInMinutes"

main <- function() {
    allDirs = list.dirs(ROOT, recursive = F)
	runDirectories = allDirs[grepl('^.*/energy-calib_.*$', allDirs)]
	sapply(runDirectories, processRunDirectory)
    print("Done.")
}

processRunDirectory <- function(parentDir) {
	prepareOutputDirectory(parentDir)
	allDirs = list.dirs(parentDir, recursive = F)
	subdirs = allDirs[grepl('^.*/rateInMBPerSec.*$', allDirs)]
	sapply(subdirs, processDataRateDirectory)
}

processDataRateDirectory <- function(parentDir) {
	print(sprintf("Rate dir: %s", parentDir))
	prepareOutputDirectory(parentDir)
	
	receiverFile = list.files(sprintf("%s/RECEIVER/", parentDir), "*.csv", full.names=T)[1]
	senderFile = list.files(sprintf("%s/SENDER/", parentDir), "*.csv", full.names=T)[1]
	receiverData = read.csv(receiverFile, sep=CSV_SEP)
	senderData = read.csv(senderFile, sep=CSV_SEP)
	distances = unique(receiverData[COL_DIST])
	collectedDataColnames = c("distance", "avgPowerSender", "avgPowerReceiver")
	collectedData = read.csv(text="", col.names=collectedDataColnames)
	for (i in 1:nrow(distances))
	{
		distance = distances[i,]
		
		selectedRowsRcv= receiverData[receiverData[COL_DIST] == distance, ]
		consumedEnergyRcv = selectedRowsRcv[COL_ENERGY]
		timeInSecondsRcv = selectedRowsRcv[COL_TIME] * 60
		energyDifferenceRcv = max(consumedEnergyRcv) - min(consumedEnergyRcv)
		if (energyDifferenceRcv > 0.0)
		{
			timeDifferenceRcv = max(timeInSecondsRcv) - min(timeInSecondsRcv)
			averagePowerRcv = energyDifferenceRcv / timeDifferenceRcv
		}
		
		selectedRowsSender = senderData[receiverData[COL_DIST] == distance, ]
		consumedEnergySender = selectedRowsSender[COL_ENERGY]
		timeInSecondsSender = selectedRowsSender[COL_TIME] * 60
		energyDifferenceSender = max(consumedEnergySender) - min(consumedEnergySender)
		if (energyDifferenceSender > 0.0)
		{
			timeDifferenceSender = max(timeInSecondsSender) - min(timeInSecondsSender)
			averagePowerSender = energyDifferenceSender / timeDifferenceSender
		}
		
		if (energyDifferenceSender > 0.0 && energyDifferenceRcv > 0.0)
		{
			line = c(distance, averagePowerSender, averagePowerRcv)
			collectedData = rbind(collectedData, line)
		}
	}
	
	colnames(collectedData) = collectedDataColnames
	
	write.table(collectedData, file=sprintf("%s/output/avg_power_vs_distance.csv", parentDir), sep=CSV_SEP, row.names=F, col.names=T)
	
	tryCatch({
		pdf(sprintf("%s/output/avg_power_vs_distance.pdf", parentDir))
		plot(collectedData$"distance", collectedData$"avgPowerSender", type="o", 
				xlab="Distance [m]", ylab="Avg. power [W]", 
				ylim=range(collectedData$"avgPowerSender", collectedData$"avgPowerReceiver"),
				pch=PCH_X, col=2, lty=1,
				panel.first=grid(NULL, NULL, lty = GRID_LINE_TYPE, col = GRID_COL)
		) 
		par(new=T)
		lines(collectedData$"distance", collectedData$"avgPowerReceiver", col=3, lty=2)
		points(collectedData$"distance", collectedData$"avgPowerReceiver", col=3, pch=PCH_X)
		legend("topleft", legend=c("Sender", "Receiver"), pch=PCH_X, col=c(2,3), lty = c(1,2), title="Legend")
		par(new=F)
	}, finally = {dev.off()})
	
	
}

prepareOutputDirectory <- function(parentDir) {
	dirName = sprintf("%s/output", parentDir)
	sapply(list.files(dirName, patter="*.pdf$"), unlink)
	sapply(list.files(dirName, patter="*.txt$"), unlink)
	#unlink(dirName, T, F)
	dir.create(dirName, showWarnings=F)
}

main()