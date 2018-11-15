# gnuplot generalized file. defines some things which should be equal for all plots. import this file from all gnuplot scripts.
# optional parameters: raw_input_file, output_folder, temp_folder

#reset

if (!exists("raw_input_file")) raw_input_file='topology_evaluation/aggregated_results.csv'
if (!exists("output_folder")) output_folder='topology_evaluation/plots/'
if (!exists("temp_folder")) temp_folder=output_folder

filterCall(outFile, overlayEnabled,k,a,overlayMetric) = "python src/de/tudarmstadt/maki/simonstrator/peerfact/application/sensor/evaluation/filter.py " . raw_input_file . " " . outFile . " " . overlayEnabled . " " . k . " " . a . " " . overlayMetric

#readSingleValue(a, overlayMetric) = "2.8" + 0   # add 0 in order to cast to number
readSingleValueAsNumber(inFile, columnName) = system("python src/de/tudarmstadt/maki/simonstrator/peerfact/application/sensor/evaluation/singleValue.py " . inFile . " " . columnName) + 0   # add 0 in order to cast to number

set datafile separator ";"

# allow macros and define some styles
set macros
error_line_style = "smooth unique w yerrorlines"

#####################
# output
######################

#set terminal png size 640,480
#output_ending = ".png"

#set terminal postscript eps enhanced
# http://www.gnuplotting.org/output-terminals/
#set terminal postscript eps size 3.5,2.62 enhanced color \
#    font 'Helvetica,20' linewidth 2
set terminal postscript eps size 3.5,2.62 enhanced color font 'Helvetica,20' linewidth 2
output_ending = ".eps"

# make margins smaller
set lmargin at screen 0.1
set rmargin at screen 0.97
set bmargin at screen 0.08
set tmargin at screen 0.95

#set linetype  1 lc rgb "dark-violet" lw 1
 #set linetype  2 lc rgb "#009e73" lw 1
 #set linetype  3 lc rgb "#56b4e9" lw 1
 #set linetype  4 lc rgb "#e69f00" lw 1
 #set linetype  5 lc rgb "#f0e442" lw 1
 #set linetype  6 lc rgb "#0072b2" lw 1
 #set linetype  7 lc rgb "#e51e10" lw 1
 #set linetype  8 lc rgb "black"   lw 1
 #set linetype  9 lc rgb "gray50"  lw 1
 #set linetype cycle  9



###########
# change line style order
###########
set style line 1 lt 12 linecolor rgb "dark-violet"	# global    linecolor rgb "#009e73"
set style line 2 lt 2 linecolor rgb "#009e73"  # local
set style line 3 lt -1 # linecolor rgb "#56b4e9"	# none
# line 4 is skiiped because it is also occupied by NONE
set style line 5 lt 0 linecolor rgb "#e51e10" lw 2	# base
# line 6 is skipped because it is also occupied by BASE
set style line 7 lt 8 linecolor rgb "#56b4e9"   # INITIAL
# line 8 is skipped because it is also occupied by INITIAL

set style increment user

