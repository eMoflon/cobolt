load "src/de/tudarmstadt/maki/simonstrator/peerfact/application/sensor/plots/common.gp"

#set title "Average Maximal (AvgMax) Transmission Range - Overlay-Metric: SumEdgeLength"

# static input
k = "*"
a = "*"
overlayMetric = "SumEdgeLength"

# vary the overlay variant
load "src/de/tudarmstadt/maki/simonstrator/peerfact/application/sensor/plots/variants.gp"

set output output_folder . "AvgMaxTransmissionRange_SumEdgeLength" . output_ending

#set xlabel "a"
#set ylabel "Distance"

set xrange [:5]
set yrange [0.4:1]

# use different file format and margin because I have only three items in a row in the NetSys paper.
# I want these plots to have more width but the same height as the other plots (four in a row)
set terminal postscript eps size 3.5,1.9751 enhanced font 'Helvetica,20' linewidth 2
output_ending = ".eps"
set bmargin at screen 0.1

set key right center

use = "'a':(column('AvgMaxUnderlayEdgeLength_AVG')/130.921457):(column('AvgMaxUnderlayEdgeLength_CONFIDENCE_MIN')/130.921457):(column('AvgMaxUnderlayEdgeLength_CONFIDENCE_MAX')/130.921457)"

plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"AvgMaxUnderlayEdgeLength_AVG")/130.921457 linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"AvgMaxUnderlayEdgeLength_AVG")/130.921457 lt 5 notitle,initial_file using @use @error_line_style title initial_title, readSingleValueAsNumber(initial_file,"AvgMaxUnderlayEdgeLength_AVG")/130.921457 lt 7 notitle#,lbase_file using @use @error_line_style title lbase_title



#################### the same for hop overlay metric ##################

#set title "Average Maximal (AvgMax) Transmission Range - Overlay-Metric: HopCount"

overlayMetric = "HopCount"

load "src/de/tudarmstadt/maki/simonstrator/peerfact/application/sensor/plots/variants.gp"

set output output_folder . "AvgMaxTransmissionRange_HopCount" . output_ending

plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title, none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"AvgMaxUnderlayEdgeLength_AVG")/130.921457 linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"AvgMaxUnderlayEdgeLength_AVG")/130.921457 lt 5 notitle,initial_file using @use @error_line_style title initial_title, readSingleValueAsNumber(initial_file,"AvgMaxUnderlayEdgeLength_AVG")/130.921457 lt 7 notitle#,lbase_file using @use @error_line_style title lbase_title




###################### and for power metric #################################

#set title "Average Maximal (AvgMax) Transmission Range - Overlay-Metric: POWER"

overlayMetric = "POWER"

load "src/de/tudarmstadt/maki/simonstrator/peerfact/application/sensor/plots/variants.gp"

set output output_folder . "AvgMaxTransmissionRange_POWER" . output_ending

plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title, none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"AvgMaxUnderlayEdgeLength_AVG")/130.921457 linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"AvgMaxUnderlayEdgeLength_AVG")/130.921457 lt 5 notitle,initial_file using @use @error_line_style title initial_title, readSingleValueAsNumber(initial_file,"AvgMaxUnderlayEdgeLength_AVG")/130.921457 lt 7 notitle#,lbase_file using @use @error_line_style title lbase_title
