getFormattedTime <- function() {
	format(Sys.time(), "%Y-%m-%d|%X")
}

#
#
# Example: log("Hi %s, today is the %s", "Jack", date())
#
log <- function(formatString, ...)
{
	newFormatString = sprintf("%s - %s", getFormattedTime(), formatString)
	print(sprintf(newFormatString, ...))
}

