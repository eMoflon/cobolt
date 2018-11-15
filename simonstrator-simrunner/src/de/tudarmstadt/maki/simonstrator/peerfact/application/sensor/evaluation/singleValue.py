__author__ = 'Michael'

import sys
from globals import CSV_SEPARATOR

# 1. parameter: csv-file
# 2. parameter: column-name
if len(sys.argv) != 3:
    print("wrong call number of parameters")
    exit(-1)

#inFile = sys.argv[1]
#print "2.3"

csvFile = open(sys.argv[1], 'r')
headLine = csvFile.readline()


# mapping from columnName to index
index = 0
columnNames = headLine.split(CSV_SEPARATOR)
for columnName in columnNames:
    if columnName.rstrip() == sys.argv[2]:
        value = csvFile.read().split(CSV_SEPARATOR)[index]
        print(value)
        break
    else:
        index += 1

if index == len(columnNames):
    raise Exception("!!!!!column not found: " + sys.argv[2] + "!!!!!")