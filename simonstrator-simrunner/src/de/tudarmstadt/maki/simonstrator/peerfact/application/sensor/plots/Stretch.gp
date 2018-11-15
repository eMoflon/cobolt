load "src/de/tudarmstadt/maki/simonstrator/peerfact/application/sensor/plots/common.gp"

# static input
k = "*"
a = "*"
overlayMetric = "SumEdgeLength"

# vary the overlay variant
load "src/de/tudarmstadt/maki/simonstrator/peerfact/application/sensor/plots/variants.gp"

#set xlabel "a"
#set ylabel "Stretch"

set key right bottom

set yrange [0:]

##
# max, SumEdgeLength
##

# length, max, pairwise
#set title "LengthStretch - Max - Pairwise - Overlay-Metric: SumEdgeLength"
set xrange [:3]
set key center right
set output output_folder . "LengthStretchMaxPairwise_SumEdgeLength" . output_ending
use = "'a':'LengthSpannerMaxPairwise_AVG':'LengthSpannerMaxPairwise_CONFIDENCE_MIN':'LengthSpannerMaxPairwise_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"LengthSpannerMaxPairwise_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"LengthSpannerMaxPairwise_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title
set autoscale x
set key right bottom

# length, max, pairwise without BASE
#set title "LengthStretch - Max - Pairwise - Overlay-Metric: SumEdgeLength"
set xrange [:3]
set output output_folder . "LengthStretchMaxPairwise_SumEdgeLength_woBase" . output_ending
use = "'a':'LengthSpannerMaxPairwise_AVG':'LengthSpannerMaxPairwise_CONFIDENCE_MIN':'LengthSpannerMaxPairwise_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"LengthSpannerMaxPairwise_AVG") linetype 3 notitle#,lbase_file using @use @error_line_style title lbase_title
set autoscale x


# power, max, pairwise
#set title "PowerStretch - Max - Pairwise - Overlay-Metric: SumEdgeLength"
set output output_folder . "PowerStretchMaxPairwise_SumEdgeLength" . output_ending
use = "'a':'PowerAlpha2.0SpannerMaxPairwise_AVG':'PowerAlpha2.0SpannerMaxPairwise_CONFIDENCE_MIN':'PowerAlpha2.0SpannerMaxPairwise_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"PowerAlpha2.0SpannerMaxPairwise_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"PowerAlpha2.0SpannerMaxPairwise_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

# hop, max, pairwise
#set title "HopStretch - Max - Pairwise - Overlay-Metric: SumEdgeLength"
set output output_folder . "HopStretchMaxPairwise_SumEdgeLength" . output_ending
use = "'a':'HopSpannerMaxPairwise_AVG':'HopSpannerMaxPairwise_CONFIDENCE_MIN':'HopSpannerMaxPairwise_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"HopSpannerMaxPairwise_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"HopSpannerMaxPairwise_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

# length, max, base
#set title "LengthStretch - Max - ToBase - Overlay-Metric: SumEdgeLength"
set output output_folder . "LengthStretchMaxToBase_SumEdgeLength" . output_ending
set xrange [:3]
use = "'a':'LengthSpannerMaxToBase_AVG':'LengthSpannerMaxToBase_CONFIDENCE_MIN':'LengthSpannerMaxToBase_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"LengthSpannerMaxToBase_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"LengthSpannerMaxToBase_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title
set autoscale x

# power, max, base
#set title "PowerStretch - Max - ToBase - Overlay-Metric: SumEdgeLength"
set output output_folder . "PowerStretchMaxToBase_SumEdgeLength" . output_ending
use = "'a':'PowerAlpha2.0SpannerMaxToBase_AVG':'PowerAlpha2.0SpannerMaxToBase_CONFIDENCE_MIN':'PowerAlpha2.0SpannerMaxToBase_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"PowerAlpha2.0SpannerMaxToBase_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"PowerAlpha2.0SpannerMaxToBase_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

# hop, max, base
#set title "HopStretch - Max - ToBase - Overlay-Metric: SumEdgeLength"
set output output_folder . "HopStretchMaxToBase_SumEdgeLength" . output_ending
use = "'a':'HopSpannerMaxToBase_AVG':'HopSpannerMaxToBase_CONFIDENCE_MIN':'HopSpannerMaxToBase_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"HopSpannerMaxToBase_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"HopSpannerMaxToBase_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

##
# avg, SumEdgeLength
##

set key right
# length, Avg, pairwise
#set title "LengthStretch - Avg - Pairwise - Overlay-Metric: SumEdgeLength"
set output output_folder . "LengthStretchAvgPairwise_SumEdgeLength" . output_ending
set xrange [:3]
use = "'a':'LengthSpannerAveragePairwise_AVG':'LengthSpannerAveragePairwise_CONFIDENCE_MIN':'LengthSpannerAveragePairwise_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"LengthSpannerAveragePairwise_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"LengthSpannerAveragePairwise_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title
set key right bottom
set autoscale x

# power, Avg, pairwise
#set title "PowerStretch - Avg - Pairwise - Overlay-Metric: SumEdgeLength"
set output output_folder . "PowerStretchAvgPairwise_SumEdgeLength" . output_ending
use = "'a':'PowerAlpha2.0SpannerAveragePairwise_AVG':'PowerAlpha2.0SpannerAveragePairwise_CONFIDENCE_MIN':'PowerAlpha2.0SpannerAveragePairwise_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"PowerAlpha2.0SpannerAveragePairwise_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"PowerAlpha2.0SpannerAveragePairwise_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

# hop, Avg, pairwise
#set title "HopStretch - Avg - Pairwise - Overlay-Metric: SumEdgeLength"
set output output_folder . "HopStretchAvgPairwise_SumEdgeLength" . output_ending
use = "'a':'HopSpannerAveragePairwise_AVG':'HopSpannerAveragePairwise_CONFIDENCE_MIN':'HopSpannerAveragePairwise_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"HopSpannerAveragePairwise_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"HopSpannerAveragePairwise_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

# length, Avg, base
#set title "LengthStretch - Avg - ToBase - Overlay-Metric: SumEdgeLength"
set xrange [:3]
set output output_folder . "LengthStretchAvgToBase_SumEdgeLength" . output_ending
use = "'a':'LengthSpannerAverageToBase_AVG':'LengthSpannerAverageToBase_CONFIDENCE_MIN':'LengthSpannerAverageToBase_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"LengthSpannerAverageToBase_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"LengthSpannerAverageToBase_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title
set autoscale x

# power, Avg, base
#set title "PowerStretch - Avg - ToBase - Overlay-Metric: SumEdgeLength"
set output output_folder . "PowerStretchAvgToBase_SumEdgeLength" . output_ending
use = "'a':'PowerAlpha2.0SpannerAverageToBase_AVG':'PowerAlpha2.0SpannerAverageToBase_CONFIDENCE_MIN':'PowerAlpha2.0SpannerAverageToBase_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"PowerAlpha2.0SpannerAverageToBase_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"PowerAlpha2.0SpannerAverageToBase_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

# hop, Avg, base
#set title "HopStretch - Avg - ToBase - Overlay-Metric: SumEdgeLength"
set output output_folder . "HopStretchAvgToBase_SumEdgeLength" . output_ending
use = "'a':'HopSpannerAverageToBase_AVG':'HopSpannerAverageToBase_CONFIDENCE_MIN':'HopSpannerAverageToBase_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"HopSpannerAverageToBase_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"HopSpannerAverageToBase_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title


#########################################################################
#################### the same for other overlay metric ##################
#########################################################################

overlayMetric = "HopCount"

load "src/de/tudarmstadt/maki/simonstrator/peerfact/application/sensor/plots/variants.gp"


##
# max, HopCount
##

# length, max, pairwise
#set title "LengthStretch - Max - Pairwise - Overlay-Metric: HopCount"
set output output_folder . "LengthStretchMaxPairwise_HopCount" . output_ending
use = "'a':'LengthSpannerMaxPairwise_AVG':'LengthSpannerMaxPairwise_CONFIDENCE_MIN':'LengthSpannerMaxPairwise_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"LengthSpannerMaxPairwise_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"LengthSpannerMaxPairwise_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

# power, max, pairwise
#set title "PowerStretch - Max - Pairwise - Overlay-Metric: HopCount"
set output output_folder . "PowerStretchMaxPairwise_HopCount" . output_ending
use = "'a':'PowerAlpha2.0SpannerMaxPairwise_AVG':'PowerAlpha2.0SpannerMaxPairwise_CONFIDENCE_MIN':'PowerAlpha2.0SpannerMaxPairwise_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title, none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"PowerAlpha2.0SpannerMaxPairwise_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"PowerAlpha2.0SpannerMaxPairwise_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

# hop, max, pairwise
#set title "HopStretch - Max - Pairwise - Overlay-Metric: HopCount"
set output output_folder . "HopStretchMaxPairwise_HopCount" . output_ending
use = "'a':'HopSpannerMaxPairwise_AVG':'HopSpannerMaxPairwise_CONFIDENCE_MIN':'HopSpannerMaxPairwise_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"HopSpannerMaxPairwise_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"HopSpannerMaxPairwise_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

# hop, max, pairwise without BASE
#set title "HopStretch - Max - Pairwise - Overlay-Metric: HopCount"
set output output_folder . "HopStretchMaxPairwise_HopCount_woBase" . output_ending
use = "'a':'HopSpannerMaxPairwise_AVG':'HopSpannerMaxPairwise_CONFIDENCE_MIN':'HopSpannerMaxPairwise_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"HopSpannerMaxPairwise_AVG") linetype 3 notitle#,lbase_file using @use @error_line_style title lbase_title


# length, max, base
#set title "LengthStretch - Max - ToBase - Overlay-Metric: HopCount"
set output output_folder . "LengthStretchMaxToBase_HopCount" . output_ending
use = "'a':'LengthSpannerMaxToBase_AVG':'LengthSpannerMaxToBase_CONFIDENCE_MIN':'LengthSpannerMaxToBase_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"LengthSpannerMaxToBase_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"LengthSpannerMaxToBase_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

# power, max, base
#set title "PowerStretch - Max - ToBase - Overlay-Metric: HopCount"
set output output_folder . "PowerStretchMaxToBase_HopCount" . output_ending
use = "'a':'PowerAlpha2.0SpannerMaxToBase_AVG':'PowerAlpha2.0SpannerMaxToBase_CONFIDENCE_MIN':'PowerAlpha2.0SpannerMaxToBase_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"PowerAlpha2.0SpannerMaxToBase_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"PowerAlpha2.0SpannerMaxToBase_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

# hop, max, base
#set title "HopStretch - Max - ToBase - Overlay-Metric: HopCount"
set output output_folder . "HopStretchMaxToBase_HopCount" . output_ending
use = "'a':'HopSpannerMaxToBase_AVG':'HopSpannerMaxToBase_CONFIDENCE_MIN':'HopSpannerMaxToBase_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title, none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"HopSpannerMaxToBase_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"HopSpannerMaxToBase_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

##
# avg, HopCount
##

# length, Avg, pairwise
#set title "LengthStretch - Avg - Pairwise - Overlay-Metric: HopCount"
set output output_folder . "LengthStretchAvgPairwise_HopCount" . output_ending
use = "'a':'LengthSpannerAveragePairwise_AVG':'LengthSpannerAveragePairwise_CONFIDENCE_MIN':'LengthSpannerAveragePairwise_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"LengthSpannerAveragePairwise_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"LengthSpannerAveragePairwise_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

# power, Avg, pairwise
#set title "PowerStretch - Avg - Pairwise - Overlay-Metric: HopCount"
set output output_folder . "PowerStretchAvgPairwise_HopCount" . output_ending
use = "'a':'PowerAlpha2.0SpannerAveragePairwise_AVG':'PowerAlpha2.0SpannerAveragePairwise_CONFIDENCE_MIN':'PowerAlpha2.0SpannerAveragePairwise_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"PowerAlpha2.0SpannerAveragePairwise_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"PowerAlpha2.0SpannerAveragePairwise_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

# hop, Avg, pairwise
#set title "HopStretch - Avg - Pairwise - Overlay-Metric: HopCount"
set output output_folder . "HopStretchAvgPairwise_HopCount" . output_ending
use = "'a':'HopSpannerAveragePairwise_AVG':'HopSpannerAveragePairwise_CONFIDENCE_MIN':'HopSpannerAveragePairwise_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"HopSpannerAveragePairwise_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"HopSpannerAveragePairwise_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

# length, Avg, base
#set title "LengthStretch - Avg - ToBase - Overlay-Metric: HopCount"
set output output_folder . "LengthStretchAvgToBase_HopCount" . output_ending
use = "'a':'LengthSpannerAverageToBase_AVG':'LengthSpannerAverageToBase_CONFIDENCE_MIN':'LengthSpannerAverageToBase_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"LengthSpannerAverageToBase_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"LengthSpannerAverageToBase_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

# power, Avg, base
#set title "PowerStretch - Avg - ToBase - Overlay-Metric: HopCount"
set output output_folder . "PowerStretchAvgToBase_HopCount" . output_ending
use = "'a':'PowerAlpha2.0SpannerAverageToBase_AVG':'PowerAlpha2.0SpannerAverageToBase_CONFIDENCE_MIN':'PowerAlpha2.0SpannerAverageToBase_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title, none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"PowerAlpha2.0SpannerAverageToBase_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"PowerAlpha2.0SpannerAverageToBase_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

# hop, Avg, base
#set title "HopStretch - Avg - ToBase - Overlay-Metric: HopCount"
set output output_folder . "HopStretchAvgToBase_HopCount" . output_ending
use = "'a':'HopSpannerAverageToBase_AVG':'HopSpannerAverageToBase_CONFIDENCE_MIN':'HopSpannerAverageToBase_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"HopSpannerAverageToBase_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"HopSpannerAverageToBase_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title







#########################################################################
#################### the same for other overlay metric ##################
#########################################################################

overlayMetric = "POWER"

load "src/de/tudarmstadt/maki/simonstrator/peerfact/application/sensor/plots/variants.gp"


##
# max, POWER
##

# length, max, pairwise
#set title "LengthStretch - Max - Pairwise - Overlay-Metric: POWER"
set output output_folder . "LengthStretchMaxPairwise_POWER" . output_ending
use = "'a':'LengthSpannerMaxPairwise_AVG':'LengthSpannerMaxPairwise_CONFIDENCE_MIN':'LengthSpannerMaxPairwise_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"LengthSpannerMaxPairwise_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"LengthSpannerMaxPairwise_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

# power, max, pairwise without BASE
#set title "PowerStretch - Max - Pairwise - Overlay-Metric: POWER"
set output output_folder . "PowerStretchMaxPairwise_POWER_woBase" . output_ending
use = "'a':'PowerAlpha2.0SpannerMaxPairwise_AVG':'PowerAlpha2.0SpannerMaxPairwise_CONFIDENCE_MIN':'PowerAlpha2.0SpannerMaxPairwise_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title, none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"PowerAlpha2.0SpannerMaxPairwise_AVG") linetype 3 notitle#,lbase_file using @use @error_line_style title lbase_title

# hop, max, pairwise
#set title "HopStretch - Max - Pairwise - Overlay-Metric: POWER"
set output output_folder . "HopStretchMaxPairwise_POWER" . output_ending
use = "'a':'HopSpannerMaxPairwise_AVG':'HopSpannerMaxPairwise_CONFIDENCE_MIN':'HopSpannerMaxPairwise_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"HopSpannerMaxPairwise_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"HopSpannerMaxPairwise_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

# length, max, base
#set title "LengthStretch - Max - ToBase - Overlay-Metric: POWER"
set output output_folder . "LengthStretchMaxToBase_POWER" . output_ending
use = "'a':'LengthSpannerMaxToBase_AVG':'LengthSpannerMaxToBase_CONFIDENCE_MIN':'LengthSpannerMaxToBase_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"LengthSpannerMaxToBase_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"LengthSpannerMaxToBase_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

# power, max, base
#set title "PowerStretch - Max - ToBase - Overlay-Metric: POWER"
set output output_folder . "PowerStretchMaxToBase_POWER" . output_ending
use = "'a':'PowerAlpha2.0SpannerMaxToBase_AVG':'PowerAlpha2.0SpannerMaxToBase_CONFIDENCE_MIN':'PowerAlpha2.0SpannerMaxToBase_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"PowerAlpha2.0SpannerMaxToBase_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"PowerAlpha2.0SpannerMaxToBase_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

# hop, max, base
#set title "HopStretch - Max - ToBase - Overlay-Metric: POWER"
set output output_folder . "HopStretchMaxToBase_POWER" . output_ending
use = "'a':'HopSpannerMaxToBase_AVG':'HopSpannerMaxToBase_CONFIDENCE_MIN':'HopSpannerMaxToBase_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title, none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"HopSpannerMaxToBase_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"HopSpannerMaxToBase_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

##
# avg, POWER
##

# length, Avg, pairwise
#set title "LengthStretch - Avg - Pairwise - Overlay-Metric: POWER"
set output output_folder . "LengthStretchAvgPairwise_POWER" . output_ending
use = "'a':'LengthSpannerAveragePairwise_AVG':'LengthSpannerAveragePairwise_CONFIDENCE_MIN':'LengthSpannerAveragePairwise_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"LengthSpannerAveragePairwise_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"LengthSpannerAveragePairwise_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

# power, Avg, pairwise
#set title "PowerStretch - Avg - Pairwise - Overlay-Metric: POWER"
set output output_folder . "PowerStretchAvgPairwise_POWER" . output_ending
use = "'a':'PowerAlpha2.0SpannerAveragePairwise_AVG':'PowerAlpha2.0SpannerAveragePairwise_CONFIDENCE_MIN':'PowerAlpha2.0SpannerAveragePairwise_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"PowerAlpha2.0SpannerAveragePairwise_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"PowerAlpha2.0SpannerAveragePairwise_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

# hop, Avg, pairwise
#set title "HopStretch - Avg - Pairwise - Overlay-Metric: POWER"
set output output_folder . "HopStretchAvgPairwise_POWER" . output_ending
use = "'a':'HopSpannerAveragePairwise_AVG':'HopSpannerAveragePairwise_CONFIDENCE_MIN':'HopSpannerAveragePairwise_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"HopSpannerAveragePairwise_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"HopSpannerAveragePairwise_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

# length, Avg, base
#set title "LengthStretch - Avg - ToBase - Overlay-Metric: POWER"
set output output_folder . "LengthStretchAvgToBase_POWER" . output_ending
use = "'a':'LengthSpannerAverageToBase_AVG':'LengthSpannerAverageToBase_CONFIDENCE_MIN':'LengthSpannerAverageToBase_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"LengthSpannerAverageToBase_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"LengthSpannerAverageToBase_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

# power, Avg, base
#set title "PowerStretch - Avg - ToBase - Overlay-Metric: POWER"
set output output_folder . "PowerStretchAvgToBase_POWER" . output_ending
use = "'a':'PowerAlpha2.0SpannerAverageToBase_AVG':'PowerAlpha2.0SpannerAverageToBase_CONFIDENCE_MIN':'PowerAlpha2.0SpannerAverageToBase_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title, none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"PowerAlpha2.0SpannerAverageToBase_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"PowerAlpha2.0SpannerAverageToBase_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title

# hop, Avg, base
#set title "HopStretch - Avg - ToBase - Overlay-Metric: POWER"
set output output_folder . "HopStretchAvgToBase_POWER" . output_ending
use = "'a':'HopSpannerAverageToBase_AVG':'HopSpannerAverageToBase_CONFIDENCE_MIN':'HopSpannerAverageToBase_CONFIDENCE_MAX'"
plot global_file using @use @error_line_style title global_title,local_file using @use @error_line_style title local_title,none_file using @use @error_line_style title none_title, readSingleValueAsNumber(none_file,"HopSpannerAverageToBase_AVG") linetype 3 notitle,baseline_file using @use @error_line_style title baseline_title, readSingleValueAsNumber(baseline_file,"HopSpannerAverageToBase_AVG") lt 5 notitle#,lbase_file using @use @error_line_style title lbase_title


















