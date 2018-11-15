load "src/de/tudarmstadt/maki/simonstrator/peerfact/application/sensor/plots/common.gp"

#set title "Average Node Degree - Overlay-Metric: SumEdgeLength"

# static input
k = "*"
a = "*"
overlayMetric = "SumEdgeLength"

# vary the overlay variant
load "src/de/tudarmstadt/maki/simonstrator/peerfact/application/sensor/plots/variants.gp"

set output output_folder . "AverageNodeDegree_SumEdgeLength" . output_ending

#set xlabel "a"
#set ylabel "Degree"

set xrange [:5]
set yrange [0:15]

# use different file format and margin because I have only three items in a row in the NetSys paper.
# I want these plots to have more width but the same height as the other plots (four in a row)
set terminal postscript eps size 3.5,1.9751 enhanced font 'Helvetica,20' linewidth 2
output_ending = ".eps"
set bmargin at screen 0.1

use = "'a':'AverageUnderlayDegree_AVG':'AverageUnderlayDegree_CONFIDENCE_MIN':'AverageUnderlayDegree_CONFIDENCE_MAX'"

plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"AverageUnderlayDegree_AVG") lt 3 notitle #,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"AverageUnderlayDegree_AVG") lt 5 notitle,initial_file using @use @error_line_style title initial_title, readSingleValueAsNumber(initial_file,"AverageUnderlayDegree_AVG") lt 7 notitle#,lbase_file using @use @error_line_style title lbase_title

unset xrange

#################### the same for other hop metric ##################

#set title "Average Node Degree - Overlay-Metric: HopCount"

overlayMetric = "HopCount"

load "src/de/tudarmstadt/maki/simonstrator/peerfact/application/sensor/plots/variants.gp"

set output output_folder . "AverageNodeDegree_HopCount" . output_ending

plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"AverageUnderlayDegree_AVG") linetype 3 notitle #,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"AverageUnderlayDegree_AVG") lt 5 notitle,initial_file using @use @error_line_style title initial_title, readSingleValueAsNumber(initial_file,"AverageUnderlayDegree_AVG") lt 7 notitle#,lbase_file using @use @error_line_style title lbase_title

unset xrange


#################### the same for power metric ##################

#set title "Average Node Degree - Overlay-Metric: POWER"

overlayMetric = "POWER"

set xrange [:5]

load "src/de/tudarmstadt/maki/simonstrator/peerfact/application/sensor/plots/variants.gp"

set output output_folder . "AverageNodeDegree_POWER" . output_ending

plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"AverageUnderlayDegree_AVG") linetype 3 notitle #,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"AverageUnderlayDegree_AVG") lt 5 notitle,initial_file using @use @error_line_style title initial_title, readSingleValueAsNumber(initial_file,"AverageUnderlayDegree_AVG") lt 7 notitle#,lbase_file using @use @error_line_style title lbase_title

unset xrange









