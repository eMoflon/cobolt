load "src/de/tudarmstadt/maki/simonstrator/peerfact/application/sensor/plots/common.gp"

#set title "Total Link Length Relative to UDG - Overlay-Metric: SumEdgeLength"

# static input
k = "*"
a = "*"
overlayMetric = "SumEdgeLength"

# vary the overlay variant
load "src/de/tudarmstadt/maki/simonstrator/peerfact/application/sensor/plots/variants.gp"

set output output_folder . "TotalLinkLengthRelativeInitial_SumEdgeLength" . output_ending

#set xlabel "a"
#set ylabel "Total Link Length"

use = "'a':'TotalLinkLengthRelativeInitial_AVG':'TotalLinkLengthRelativeInitial_CONFIDENCE_MIN':'TotalLinkLengthRelativeInitial_CONFIDENCE_MAX'"

plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title,readSingleValueAsNumber(none_file,"TotalLinkLengthRelativeInitial_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"TotalLinkLengthRelativeInitial_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title



#################### the same for hop overlay metric ##################

#set title "Total Link Length Relative to UDG - Overlay-Metric: HopCount"

overlayMetric = "HopCount"

load "src/de/tudarmstadt/maki/simonstrator/peerfact/application/sensor/plots/variants.gp"

set output output_folder . "TotalLinkLengthRelativeInitial_HopCount" . output_ending

plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,ordered_file using @use @error_line_style title ordered_title, none_file using @use @error_line_style title none_title,readSingleValueAsNumber(none_file,"TotalLinkLengthRelativeInitial_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"TotalLinkLengthRelativeInitial_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title


#################### the same for hop overlay metric ##################

#set title "Total Link Length Relative to UDG - Overlay-Metric: POWER"

overlayMetric = "POWER"

load "src/de/tudarmstadt/maki/simonstrator/peerfact/application/sensor/plots/variants.gp"

set output output_folder . "TotalLinkLengthRelativeInitial_POWER" . output_ending

plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,ordered_file using @use @error_line_style title ordered_title, none_file using @use @error_line_style title none_title,readSingleValueAsNumber(none_file,"TotalLinkLengthRelativeInitial_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"TotalLinkLengthRelativeInitial_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title