# Cobolt - A model-based tool for evaluating topology control algorithms

This repository hosts the code of Cobolt, a model-based tool for the rapid simulative evaluation of topology control algorithms.

The Cobolt repository comprises a modified version of the [Simonstrator](https://dev.kom.e-technik.tu-darmstadt.de/simonstrator/) network simulator (version 2.5) and 
a set of of sample topology control algorithms specified using the modeling tool [eMoflon](https://emoflon.org/).

## Setup instructions

1. **Install Eclipse 2018-09 R with Modeling Components Oxygen (or newer)**
   * All Eclipse packages are available here: https://eclipse.org/downloads/
1. **Install the Maven 2 Eclipse integration (m2e)**
   1. Navigate to the *Install* dialog (*Help &rarr; Install New Software...*).
   1. Select the standard Eclipse update site (e.g., http://download.eclipse.org/releases/2018-09 for Eclipse 2018-09)
   1. Install *General Purpose Tools/m2e - Maven Integration for Eclipse* &ndash;no need to restart Eclipse afterwards
1. **Install eMoflon 3.5.1**
   1. Open the *Install* dialog.
   1. First, install only *PlantUML 1.1.21* (or above)&ndash;no need to restart Eclipse afterwards: https://hallvard.github.io/plantuml/
   1. Open the *Install* dialog.
   1. Paste the following Eclipse update site: https://github.com/eMoflon/eMoflon.github.io/raw/emoflon-tie-updatesite_3.5.1/eclipse-plugin/beta/updatesite
   1. Select *Manage...*.
   1. Enable at least the following update sites:
        * https://emoflon.org/eclipse-plugin/beta/updatesite/ (aka. https://emoflon.github.io/eclipse-plugin/beta/updatesite )
        * http://emoflon.org/emoflon-core-updatesite/stable/updatesite/
        * "Latest Eclipse release"
   1. Go back via *Apply and Close*.
   1. Make sure that the option *Contact all update sites during install to find required software* is enabled.
   1. Select *eMoflon::TIE-SDM* and complete the installation via *Next* etc.
        * Note: The dependency resolution may take some time...
   1. Restart Eclipse, open a fresh workspace, and switch to the *eMoflon* perspective.
1. **Install Enterprise Architect 12 (or later)**
   * A 30-days trial version of Enterprise Architect is available here: https://www.sparxsystems.de/uml/download-trial/
   * Install the eMoflon 3.5.1 Enterprise Architect addin: https://github.com/eMoflon/eMoflon.github.io/raw/emoflon-tie-updatesite_3.5.1/eclipse-plugin/beta/updatesite/ea-ecore-addin.zip
1. **Set up Cobolt**
   1. Right-click in Project Explorer &rarr; Import... &rarr; Team/Team Project Set
   1. URL: https://raw.githubusercontent.com/eMoflon/cobolt/master/cobolt.psf
   1. Let Eclipse check out and build all Cobolt and Simonstrator projects for you
      * Make sure that *Build &rarr; Build automatically* is enabled.
   1. Eclipse complains about the missing Tycho lifecycle plugin (*org.eclipse.tycho:tycho-compiler-plugin:1.0.0:compile*).
      * Use the quick fix (*Ctrl+1* when pointing at the error marker) to discover and install the appropriate m2e connectors.
   1. Restart Eclipse and let wait until the auto-build has completed.
1. **Run a sample simulation**
   1. Navigate to *simonstrator-simrunner/config/dissertation/*.
   1. Right-click *GuiRunner.launch* and select *Run as... &rarr; GuiRunner*.
   1. Select the simulation configuration file *cobolt.xml* (in the folder *dissertation*).
      * If you wish, you can experiment with different settings
         * *size* configures the number of motes
         * *world_size* configures the side length of the area in meters
         * *topologyControlAlgorithm* configures the active topology control algorithms (e.g., MAXPOWER_TC D_KTC E_KTC LSTAR_KTC GG Yao RNG GMST LMST)
         * *topologyControlIntervalInMinutes* configures the interval between topology control executions in minutes
   1. Press *Start Simulation*.
   1. Two windows pop up: the simulation progress view and the topology visualization.
      * The shown blue graph is the virtual topology of the topology control algorithm (by default, kTC with k=1).
      * The topology control algorithm is executed periodically (by default, once every simulated minute).
      * In the Eclipse Console window, you can observe the simulation output.
         * Lines with 'iter#xxx CEH' refer to the context event handling.
         * Lines with 'iter#xxx TCA' refer to the topology control algorithm execution.
         * Lines with 'iter#xxx STAT' refer to the statistics recording.
         * 'xxx' is the iteration counter
      * Additional output is available in *simonstrator-simrunner/output/wsntraces/*
         * The subfolder *log* contains the logfile (similar to the console output)
         * The subfolder *energyConsumptionPerNode* contains statistics about how long each mote was in each state.
         * The subfolder *data* contains the performance data (e.g., execution time, link state modification counts,...)
         * The file *wsntraces_scenarioStatistics.csv* summarizes information about the scenario (e.g., initial topology density).

## Reproducing the evaluation results

Conduct the following steps to reproduce the Cobolt evaluation results.
1. Install Jupyter: https://jupyter.org/
1. Execute the launcher *simonstrator-simrunner/config/dissertation/CoboltEvaluationExecutor.launch* and wait for it to complete
   * This may take several days depending on the hardware platform.
   * **Important:** Abort the batch run using the 'STOP batch run' button, otherwise simulation processes may continue to run in the background.
1. Concatenate the CSV files in *output/wsntraces/[...]/data* (without repeating the header!)
2. Place the concatenated file in *jupyter/data/cobolt/dataCollected.csv*
3. Execute the Jupyter notebook: *jupyter/DissertationEvaluation.ipynb*.
   * This notebook produces all evaluation plots (also for the tool cMoflon).
   
## Licensing
GPLv3, see [LICENSE.txt](LICENSE.txt).