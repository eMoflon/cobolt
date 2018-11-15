UNKNOWN_SCENARIO = "UNK"

scenarios = c()
scenarios["99,250"] = "n100w250"
scenarios["99,750"] = "n100w750"
scenarios["99,500"] = "n100w500"
scenarios["999,1000"] = "n1000w1000"
scenarios["999,1500"] = "n1000w1500"
scenarios["999,2000"] = "n1000w2000"

lookupScenario <-function(nodeCount, worldSize) {
	scenario = scenarios[sprintf("%d,%d", nodeCount, worldSize)]
	if (is.na(scenario))
		return(UNKNOWN_SCENARIO)
	else
		return(scenario)
}

lookupScenarioBasedOnDirectory <- function(dir) {
	nodeCount = extractNodeCountFromConfigDirName(dir)
	worldSize = extractWorldSizeFromConfigDirName(dir)
	lookupScenario(nodeCount, worldSize)
} 

#print(sprintf("%s", lookupScenario(99, 750)))
#print(lookupScenario(99, 751))
#x=c(T, T, F)
#print(length(x[x==T]))
#print(max(1,2,3))
#df = read.csv(text="", col.names=c("a", "b", "c"))
#print(df$a)
#library(Hmisc)
#d <- data.frame(a=LETTERS[1:5], x=rnorm(5))
#latex(d, file="")            # If you want all the data
#latex(describe(d), file="")  # If you just want a summary