# See Java class de.tudarmstadt.maki.simonstrator.tc.ktc.UnderlayTopologyControlAlgorithms
MAXPOWER_ID = 0
KTC_ID = 1
EKTC_ID = 2



# http://research.stowers-institute.org/efg/Report/UsingColorInR.pdf
algoCol <- function(algo)
{
	switch(toString(algo),
			"0" = "black",
			"1" = "blue",
			"2" = "red"
			)	
}

algoPch <- function(algo) {
	switch(toString(algo),
			"0" = PCH_X,
			"1" = 3,
			"2" = PCH_PLUS
	)	
}

algoName <- function(algo) {
	switch(toString(algo),
			"0" = "Maxpower",
			"1" = "kTC",
			"2" = "e-kTC"
	)	
}

## print(algoCol(MAXPOWER_ID))
## print(algoCol(KTC_ID))
## print(algoCol(EKTC_ID))
## print(algoPch(MAXPOWER_ID))
## print(algoPch(KTC_ID))
## print(algoPch(EKTC_ID))
## print(sapply(c(MAXPOWER_ID, EKTC_ID), algoCol))