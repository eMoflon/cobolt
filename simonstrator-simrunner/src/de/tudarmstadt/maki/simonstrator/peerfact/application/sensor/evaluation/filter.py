#! /usr/bin/env python

__author__ = 'Michael'

import sys
from globals import CSV_SEPARATOR


def check_column(line, columnNumber, expValue):
    if expValue == '*':
        return True

    columns = line.split(CSV_SEPARATOR)
    value = columns[columnNumber].rstrip()
    return value == expValue


if len(sys.argv) != 7:
    print("invalid number of parameters")
    exit(-1)

inFile = sys.argv[1]
outFile = sys.argv[2]
overlayEnabled = sys.argv[3]
k = sys.argv[4]
a = sys.argv[5]
overlayMetric = sys.argv[6]

csvLines = open(inFile, 'r').readlines()


# mapping from columnName to index
index = 0
headLine = csvLines[0]
columnNames = headLine.split(CSV_SEPARATOR)
columnNameToIndex = {}
for columnName in columnNames:
    columnNameToIndex[columnName.rstrip()] = index
    index += 1

# filter all lines
out = open(outFile, "w")
out.write(csvLines[0])
for line in csvLines:
    if (not line.rstrip() == '') \
            and check_column(line, columnNameToIndex['overlayEnabled'], overlayEnabled) \
            and check_column(line, columnNameToIndex['k'], k) \
            and check_column(line, columnNameToIndex['a'], a) \
            and check_column(line, columnNameToIndex['overlayMetric'], overlayMetric):
        out.write(line)
out.close();