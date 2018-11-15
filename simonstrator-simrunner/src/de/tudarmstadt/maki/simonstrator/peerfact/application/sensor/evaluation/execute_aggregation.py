__author__ = 'Michael Stein'

import os
import numpy as np
import scipy as sp
import scipy.stats
import globals

# returns the name of all sub directories (not the path)
def get_immediate_subdirectories(dir):
    return [name for name in os.listdir(dir)
            if os.path.isdir(os.path.join(dir, name))]


def read_attributes(file, valueLists, metrics):
    count = 0
    for line in open(file, 'r'):
        if '=' in line:
            parts = line.split('=')
            property = parts[0]
            if property in metrics:
                value = parts[1]
                if not valueLists.has_key(property):
                    valueLists[property] = []
                valueLists[property].append(float(value))
                count += 1

    if not count == len(metrics):
        print("number of metrics = " + str(len(metrics)) + " != " + str(count) + "number of read values! " +
              "file: " + str(file))


# computes average and confidence interval
def mean_confidence_interval(data, confidence=0.95):
    a = 1.0 * np.array(data)
    n = len(a)
    m, se = np.mean(a), scipy.stats.sem(a)
    h = se * sp.stats.t._ppf((1 + confidence) / 2., n - 1)
    return m, m - h, m + h

# maps configuration results to value-Map
configurationResults = {}

# config values used for the csv file in head line
configNames = []

# collect metric results for all seeds and configurations
sub_dirs = get_immediate_subdirectories(globals.ROOT_FOLDER)
i = 1
for seedDirectoryName in sub_dirs:
    seedResultPath = os.path.join(globals.ROOT_FOLDER, seedDirectoryName, "result")

    print("Current directory (" + str(i) + "/" + str(len(sub_dirs)) + "): " + str(os.path.join(globals.ROOT_FOLDER, seedDirectoryName)))
    i += 1

    for configStr in get_immediate_subdirectories(seedResultPath):

        # extract configuration properties from config name
        for configAssignment in configStr.split("_"):
            propertyName = configAssignment.split("=")[0]
            if not propertyName in configNames:
                print("\tAdd property:" + propertyName)
                configNames.append(propertyName)

        if not configurationResults.has_key(configStr):
            configurationResults[configStr] = {}

        metricFilePath = os.path.join(seedResultPath, configStr, "metrics.txt")

        read_attributes(metricFilePath, configurationResults[configStr], globals.METRICS)


print("Now writing CSV file...")

# create csv file and write head line
csvFile = open(globals.AGGREGATED_CSV_FILE, 'w')

# write headline (two columns for each metric -> avg and confidence interval)
for configName in configNames:
    csvFile.write(configName + globals.CSV_SEPARATOR)
for metric in globals.METRICS:
    csvFile.write(
        metric + "_AVG" + globals.CSV_SEPARATOR + metric + "_CONFIDENCE_MIN" + globals.CSV_SEPARATOR + metric + "_CONFIDENCE_MAX" + globals.CSV_SEPARATOR)

# write aggregated metric results to csv file
for configStr, rawResults in configurationResults.iteritems():
    csvFile.write("\n")

    # read attributes from config string and write values to csv file
    configValues = {}
    for propertyAssignment in configStr.split("_"):
        configValues[propertyAssignment.split("=")[0]] = propertyAssignment.split("=")[1]

    for configName in configNames:
        if configValues.has_key(configName):
            csvFile.write(configValues[configName])
        csvFile.write(globals.CSV_SEPARATOR);


    for metric in globals.METRICS:
        avg, confLow, confHigh = mean_confidence_interval(rawResults[metric])
        csvFile.write(str(avg) + globals.CSV_SEPARATOR + str(confLow) + globals.CSV_SEPARATOR + str(
            confHigh) + globals.CSV_SEPARATOR)
csvFile.close()


