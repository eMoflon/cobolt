#
# This R script aggregates the CSV files of the different runs of the same configuration with different seeds.
#

options(error=traceback)

CSV_SEP = ";"
GRID_COL = "lightgray"
GRID_LINE_TYPE = 6

currentRunRootDirectory = NULL

timeInMinutesKey = "timeInMinutes"
consumedEnergyInJouleKey = "consumedEnergyInJoule"
batteryLevelInPercentKey = "batteryLevelInPercent"
averagePowerInWattKey = "averagePowerInWatt"


overviewSender = data.frame(dataRate=numeric(0), averagePowerInWatt=numeric(0), worldSize=numeric(0))
overviewReceiver = data.frame(dataRate=numeric(0), averagePowerInWatt=numeric(0), worldSize=numeric(0))

worldSizes = data.frame(worldSize=numeric(0))
dataRates = data.frame(dataRate=numeric(0))

main <- function() {
	allDirs = list.dirs(".")
	configurationDirs = allDirs[grepl('^.*/rateInMBPerSec[0-9]+.[0-9]+$', allDirs)]
	#configurationDirs = allDirs[grepl('^.*/ws[0-9]+$', allDirs)]
	sapply(configurationDirs, processConfiguration)
	otherPlotsSender(currentRunRootDirectory)
	otherPlotsReceiver(currentRunRootDirectory)
	print("Done.")
}

processConfiguration <- function(parentDir)
{
	print(sprintf("Processing configuration %s", parentDir))
	currentRunRootDirectory <<- parentDir
	prepareOutputDirectory(parentDir)
	#aggregateResultsOfOneConfiguration(parentDir)
	plotEnergyConsumption(parentDir)
	#evaluateNetworkLifetime(parentDir)
}

prepareOutputDirectory <- function(parentDir) {dir.create(sprintf("%s/output", parentDir), showWarnings=F)}



extractWorldSizeFromConfigDirName <- function(dir) {
	as.numeric(regmatches(dir,regexec("ws(\\d+)",dir))[[1]][2])
}

extractDataRateFromConfigDirName <- function(dir) {
	as.numeric(regmatches(dir,regexec("rateInMBPerSec(\\d+\\d+)",dir))[[1]][2])
}

plotEnergyConsumption <- function(parentDir)
{
    #worldSize = as.numeric(gsub(".*ws", "", parentDir))
	worldSize = extractWorldSizeFromConfigDirName(parentDir)
	
	dataRate = extractDataRateFromConfigDirName(parentDir)
    
	senderFiles = list.files(sprintf("%s/sender", parentDir), pattern="*.csv$", full.names=T)
    receiverFiles = list.files(sprintf("%s/receiver", parentDir), pattern="*.csv$", full.names=T)
    
    senderData = Reduce("+", lapply(senderFiles, read.csv, sep=CSV_SEP)) / length(senderFiles)
    receiverData = Reduce("+", lapply(receiverFiles, read.csv, sep=CSV_SEP)) / length(receiverFiles)
	
	senderTimeInMinutes = senderData[timeInMinutesKey]
	senderconsumedEnergyInJoule = senderData[consumedEnergyInJouleKey]
	senderbatteryLevelInPercent = senderData[batteryLevelInPercentKey]
	
#	senderAveragePowerInWatt = senderData[averagePowerInWattKey]
	

	receiverTimeInMinutes = receiverData[timeInMinutesKey]
	receiverconsumedEnergyInJoule = receiverData[consumedEnergyInJouleKey]
	receiverbatteryLevelInPercent = receiverData[batteryLevelInPercentKey]
	
	
	#senderTimeInMinutes = senderData[,grepl(timeInMinutesKey, colnames(senderData))]
	#senderconsumedEnergyInJoule = senderData[,grepl(consumedEnergyInJouleKey, colnames(senderData))]
	
	#receiverTimeInMinutes = receiverData[,grepl(timeInMinutesKey, colnames(receiverData))]
	#receiverconsumedEnergyInJoule = receiverData[,grepl(consumedEnergyInJouleKey, colnames(receiverData))]

    pdf(sprintf("%s/output/Energy_vs_time_sender.pdf", parentDir))
    plot(data.frame(senderTimeInMinutes, senderconsumedEnergyInJoule), 
            xlab="Simulation Time [min]", ylab="Consumed Energy [J]", pch=4, type="o", xlim=c(0, max(senderTimeInMinutes)), ylim=c(0, max(senderconsumedEnergyInJoule)),
            panel.first = grid(NULL, NULL, lty = GRID_LINE_TYPE, col = GRID_COL)
        )
    dev.off()
    
    
    pdf(sprintf("%s/output/Energy_vs_Time_receiver.pdf", parentDir))
    plot(data.frame(receiverTimeInMinutes, receiverconsumedEnergyInJoule), 
            xlab="Simulation Time [min]", ylab="Consumed Energy [J]", pch=4, type="o", xlim=c(0, max(receiverTimeInMinutes)), ylim=c(0, max(receiverconsumedEnergyInJoule)),
            panel.first = grid(NULL, NULL, lty = GRID_LINE_TYPE, col = GRID_COL)
        )
    dev.off()
    
	worldSizes[nrow(worldSizes)+1,] <<- c(worldSize)
	dataRates[nrow(dataRates)+1,] <<- c(dataRate)
	
	senderAveragePowerAtMinimalBattery <- subset(senderData, batteryLevelInPercent == min(senderbatteryLevelInPercent))
	senderAveragePowerInWatt = senderAveragePowerAtMinimalBattery[averagePowerInWattKey]
	
	receiverAveragePowerAtMinimalBattery <- subset(receiverData, batteryLevelInPercent == min(receiverbatteryLevelInPercent))
	receiverAveragePowerInWatt = receiverAveragePowerAtMinimalBattery[averagePowerInWattKey]
	
	overviewSender[nrow(overviewSender)+1,] <<- c(dataRate, senderAveragePowerInWatt[1,1], worldSize)
	overviewReceiver[nrow(overviewReceiver)+1,] <<- c(dataRate, receiverAveragePowerInWatt[1,1], worldSize)
}

otherPlotsSender <- function(parentDir)
{
	print(sprintf("Creating other Plots: %s/%s/overviewSender.csv", currentRunRootDirectory, "output"))
	
	worldSizes <- unique(worldSizes)
	dataRates <- unique(dataRates)	
	
	for (i in 1:nrow(worldSizes)) {
		onePlotData <- subset(overviewSender, worldSize == worldSizes[i,1])
		
		onePlotData <- onePlotData[with(onePlotData, order(dataRate)), ]
		
		averagePowerInWatt = onePlotData[averagePowerInWattKey]
		dataRate = onePlotData["dataRate"]
		
		pdf(sprintf("%s/output/Sender_Power_vs_DataRate_fixWorldSize_%s.pdf", parentDir, worldSizes[i,1]))
		plot(data.frame(dataRate, averagePowerInWatt), 
				xlab="Data Rate [MB/s]", ylab="Average Power [W]", pch=4, type="o", log = "x", xlim=c(min(dataRate), max(dataRate)), ylim=c(0, max(averagePowerInWatt)),
				panel.first = grid(NULL, NULL, lty = GRID_LINE_TYPE, col = GRID_COL)
		)
		dev.off()
		
		pdf(sprintf("%s/output/Sender_Power_vs_DataRate_fixWorldSize_normalScale_%s.pdf", parentDir, worldSizes[i,1]))
		plot(data.frame(dataRate, averagePowerInWatt), 
				xlab="Data Rate [MB/s]", ylab="Average Power [W]", pch=4, type="o", log = "", xlim=c(min(dataRate), max(dataRate)), ylim=c(0, max(averagePowerInWatt)),
				panel.first = grid(NULL, NULL, lty = GRID_LINE_TYPE, col = GRID_COL)
		)
		dev.off()
		
		#write.table(dataRates, sprintf("%s/%s/dataRates.csv", currentRunRootDirectory, "output"), sep=CSV_SEP, row.names = T, col.names = NA)
		#write.table(onePlotData,sprintf("%s/output/Sender_Energy_vs_DataRate_fixWorldSize_%s.csv", parentDir, dataRates[i,1]), sep=CSV_SEP, row.names = T, col.names = NA)	
	}	
	
	for (i in 1:nrow(dataRates)) {
		onePlotData <- subset(overviewSender, dataRate == dataRates[i,1])
		
		averagePowerInWatt = onePlotData[averagePowerInWattKey]
		worldSize = onePlotData["worldSize"]
		
		pdf(sprintf("%s/output/Sender_Power_vs_WorldSize_fixDataRate_%s.pdf", parentDir, dataRates[i,1]))
		plot(data.frame(worldSize, averagePowerInWatt), 
				xlab="World Size [m]", ylab="Average Power [W]",  pch=4, type="o", log = "", xlim=c(0, max(worldSize)), ylim=c(0, max(averagePowerInWatt)),
				panel.first = grid(NULL, NULL, lty = GRID_LINE_TYPE, col = GRID_COL)
		)
		dev.off()
		
		#write.table(sprintf("%s/output/Sender_Energy_vs_WorldSize_fixDataRate_%s.csv", parentDir, dataRates[i,1]), sep=CSV_SEP, row.names = T, col.names = NA)	
	}	
	
	write.table(overviewSender, sprintf("%s/%s/SenderData_Overview.csv", currentRunRootDirectory, "output"), sep=CSV_SEP, row.names = T, col.names = NA)
	
}


otherPlotsReceiver <- function(parentDir)
{	
	worldSizes <- unique(worldSizes)
	dataRates <- unique(dataRates)
	
	for (i in 1:nrow(worldSizes)) {
		onePlotData <- subset(overviewReceiver, worldSize == worldSizes[i,1])
		
		onePlotData <- onePlotData[with(onePlotData, order(dataRate)), ]
		
		averagePowerInWatt = onePlotData[averagePowerInWattKey]
		dataRate = onePlotData["dataRate"]		
		
		pdf(sprintf("%s/output/Receiver_Power_vs_DataRate_fixWorldSize_%s.pdf", parentDir, worldSizes[i,1]))
		plot(data.frame(dataRate, averagePowerInWatt), 
				xlab="Data Rate [MB/s]", ylab="Average Power [W]",  pch=4, type="o", log = "x", xlim=c(min(dataRate), max(dataRate)), ylim=c(0, max(averagePowerInWatt)),
				panel.first = grid(NULL, NULL, lty = GRID_LINE_TYPE, col = GRID_COL)
		)
		dev.off()
		
		pdf(sprintf("%s/output/Receiver_Power_vs_DataRate_fixWorldSize_normalScale_%s.pdf", parentDir, worldSizes[i,1]))
		plot(data.frame(dataRate, averagePowerInWatt), 
				xlab="Data Rate [MB/s]", ylab="Average Power [W]",  pch=4, type="o", log = "", xlim=c(min(dataRate), max(dataRate)), ylim=c(0, max(averagePowerInWatt)),
				panel.first = grid(NULL, NULL, lty = GRID_LINE_TYPE, col = GRID_COL)
		)
		dev.off()
		
	#write.table(sprintf("%s/output/Receiver_Energy_vs_DataRate_fixWorldSize_%s.csv", parentDir, dataRates[i,1]), sep=CSV_SEP, row.names = T, col.names = NA)	
	}	
	
	for (i in 1:nrow(dataRates)) {
		onePlotData <- subset(overviewReceiver, dataRate == dataRates[i,1])
		
		averagePowerInWatt = onePlotData[averagePowerInWattKey]
		worldSize = onePlotData["worldSize"]
		
		pdf(sprintf("%s/output/Receiver_Power_vs_WorldSize_fixDataRate_%s.pdf", parentDir, dataRates[i,1]))
		plot(data.frame(worldSize, averagePowerInWatt), 
				xlab="World Size [m]", ylab="Average Power [W]",  pch=4, type="o", log = "", xlim=c(0, max(worldSize)), ylim=c(0, max(averagePowerInWatt)),
				panel.first = grid(NULL, NULL, lty = GRID_LINE_TYPE, col = GRID_COL)
		)
		dev.off()
		
		#write.table(sprintf("%s/output/Receiver_Energy_vs_WorldSize_fixDataRate_%s.csv", parentDir, dataRates[i,1]), sep=CSV_SEP, row.names = T, col.names = NA)	
	}			
	write.table(overviewReceiver, sprintf("%s/%s/ReceiverData_Overview.csv", currentRunRootDirectory, "output"), sep=CSV_SEP, row.names = T, col.names = NA)
	
}

main()