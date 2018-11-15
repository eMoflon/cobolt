__author__ = 'Michael'

import subprocess
import globals
import os.path


def __call_gnuplot(scriptName):
    # weitere gnuplot-parameter durch semikolon getrennt anhaengen ;outputfile=\'" + file + "\'"
    subprocess.call(["gnuplot", "-e", "output_folder=\'"+ globals.PLOT_DIRECTORY_PATH + "\';raw_input_file=\'"+ globals.AGGREGATED_CSV_FILE+"\'", os.path.join("src/de/tudarmstadt/maki/simonstrator/peerfact/application/sensor/plots/", scriptName)])

if not os.path.exists(globals.PLOT_DIRECTORY_PATH):
    os.makedirs(globals.PLOT_DIRECTORY_PATH)

__call_gnuplot('AvgNodeDegree.gp')
__call_gnuplot('AvgTransmissionRange.gp')
__call_gnuplot('MaxMaxTransmissionRange.gp')
__call_gnuplot('AvgMaxTransmissionRange.gp')
__call_gnuplot('Stretch.gp')
__call_gnuplot('TotalLinkLengthRelativeUDG.gp')
