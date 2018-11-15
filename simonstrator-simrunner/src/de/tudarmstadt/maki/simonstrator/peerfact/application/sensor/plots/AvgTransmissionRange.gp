load "src/de/tudarmstadt/maki/simonstrator/peerfact/application/sensor/plots/common.gp"

#set title "Average Transmission Range - Overlay-Metric: SumEdgeLength"

# static input
k = "*"
a = "*"
overlayMetric = "SumEdgeLength"

# vary the overlay variant
load "src/de/tudarmstadt/maki/simonstrator/peerfact/application/sensor/plots/variants.gp"

set output output_folder . "AverageTransmissionRange_SumEdgeLength" . output_ending

#set xlabel "a"
#set ylabel "Distance"

set yrange [50:110]

set key center right

use = "'a':'AvgUnderlayEdgeLength_AVG':'AvgUnderlayEdgeLength_CONFIDENCE_MIN':'AvgUnderlayEdgeLength_CONFIDENCE_MAX'"

plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title,readSingleValueAsNumber(none_file,"AvgUnderlayEdgeLength_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"AvgUnderlayEdgeLength_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title


#################### the same for hop overlay metric ##################

#set title "Average Transmission Range - Overlay-Metric: HopCount"

overlayMetric = "HopCount"

load "src/de/tudarmstadt/maki/simonstrator/peerfact/application/sensor/plots/variants.gp"

set output output_folder . "AverageTransmissionRange_HopCount" . output_ending

plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title,readSingleValueAsNumber(none_file,"AvgUnderlayEdgeLength_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"AvgUnderlayEdgeLength_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title


#################### the same for power overlay metric ##################

#set title "Average Transmission Range - Overlay-Metric: POWER"

overlayMetric = "POWER"

load "src/de/tudarmstadt/maki/simonstrator/peerfact/application/sensor/plots/variants.gp"

set output output_folder . "AverageTransmissionRange_POWER" . output_ending

plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title,readSingleValueAsNumber(none_file,"AvgUnderlayEdgeLength_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"AvgUnderlayEdgeLength_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title