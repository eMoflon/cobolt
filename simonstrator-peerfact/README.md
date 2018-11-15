# PeerfactSim.KOM, Simonstrator-Version, Open-Street-Map Integration

This branch of the PeerfactSim-project implements the functionality to load real world maps data out of several resources. All relevant modifications are in the `de.tud.kom.p2psim.impl.topology.movement.modularosm`-package. The `ModularMovementModel`-class is the main-class for this approach. This class loads all other classes as defined in the XML-configurable file in the simRunner-project.

For the possible implementations of the AttractionGenerator (generates POIs), the map visualization and the transition strategy we have defined interfaces and implemented at least two implementations for each interface.   
One configurable interface we copied from the `de.tud.kom.p2psim.impl.topology.movement.modular`-package. In the `de.tud.kom.p2psim.impl.topology.movement.local`-package are some classes, which calculate the routes of the moving nodes. Here you can use some simple algorithmic approaches or use one out of two possible implementations, which use real world street data to navigate the nodes through the sreets to their destination.

The code itself is (hopefully) self-explaining. All possible configurations can be made via the XML-config in the simRunner-project.