# variable
global_title = "g-kTC"
global_overlay = "GLOBAL"
global_file = temp_folder . "GLOBAL.csv"
system filterCall(global_file, global_overlay,k,a,overlayMetric)
#
local_title = "l-kTC"
local_overlay = "LOCAL"
local_file = temp_folder . "LOCAL.csv"
system filterCall(local_file, local_overlay,k,a,overlayMetric)
#
none_title = "kTC"
none_overlay = "NONE"
none_file = temp_folder . "NONE.csv"
system filterCall(none_file, none_overlay,k,"*","*")
#
ordered_title = "ORDERED"
ordered_overlay = "ORDERED"
ordered_file = temp_folder . "ORDERED.csv"
system filterCall(ordered_file, ordered_overlay,k,a,overlayMetric)
#
lbase_title = "LocalBaseTopologyControl"
lbase_overlay = "LocalBaseTopologyControl"
lbase_file = temp_folder . "LocalBaseTopologyControl.csv"
system filterCall(lbase_file, lbase_overlay,k,a,overlayMetric)
#
baseline_title = "BASE"
baseline_overlay = "BaselineAlgorithm"
baseline_file = temp_folder . "BASELINE.csv"
system filterCall(baseline_file, baseline_overlay,k,a,overlayMetric)
#
initial_title = "Initial UDG"
initial_overlay = "INITIAL"
initial_file = temp_folder . "INITIAL.csv"
system filterCall(initial_file, initial_overlay,"*","*","*")