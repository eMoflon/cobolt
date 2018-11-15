# Simonstrator-Overlays

The brain of the Simonstrator-architecture: here, you develop your fancy overlays and services - independent of the target platform. The code solely depends on the Simonstrator-API and can, thus, run on any Java-enabled PC, on Android, and of course in simulations. Below, you find a brief description of how to use this platform and some best practices that you should keep in mind when developing here.

## Platforms

### Simulation

Usually, you start to develop your overlay within the simulation framework. Here, debugging is by far easier than in a real-world deployment. Currently, we support PeerfactSim.KOM as our main simulation framework. When using the Simonstrator-Platform, you need the Simonstrator-Version of PeerfactSim.KOM, available within the MAKI-Common namespace. Simonstrator-Peerfact relies on the Simonstrator-API and provides the relevant wrappers for scheduling, analyzing, network models, etc. The Simonstator-Platform is **not** compatible to older versions of PeerfactSim.KOM!

To configure simulations, define workloads and write your own analyzers, you also need the project `simonstator-simRunner`. Within this project, you can write all the overlay and simulation-specific code you need to evaluate your overlay. The project relies on both: the `simonstrator-overlay` as well as the `simonstrator-peerfact` project and provides the basic runners (main classes) that you need to run simulations. Usually, you should create a new branch in the `simRunner` project and add all the config files, analyzers, and application logic you need. Documentation for that is provided in the `simRunner` project.

#### Best Practices
Analyzers, configurations, and workload generators (applications) belong in the `simRunner` project! **Interfaces** for **overlay-specific** analyzers belong into the `simonstrator-overlays` project - usually in a package within your overlay. The implementation of these interfaces is then done in the `simRunner` project!


### Android and Java-Standalone
To deploy your code on any PC that supports Java and on Android devices, you should make use of the `simonstrator-utils` that provide implementations of the main platform components. Entry point in this project is the `StandaloneHost`, which you can extend in order to implement your own runner. Examples are provided in the `simonstrator-standalone` project. For Android, you need to wrap that code within an Android Activity or Service, examples are provided in the respective projects.

