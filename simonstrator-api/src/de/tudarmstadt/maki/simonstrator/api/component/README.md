# The Components

Within the simonstrator API, a *host* (device in the network) is composed out of multiple `HostComponent`s. These components are only loosely coupled via the `host`-object. If a component wants to access another component's methods, it requests the other component by calling

```java
host.getComponent(ComponentAPI.class);
```

Besides `HostComponent`s, there exist several `GlobalComponent`s. Those define platform-specific methods to allow access to event handling, for example. If you want to add new functionality, you most likely need to define your own `HostComponent` or implement an existing one.

In the following, existing components are briefly explained. More detailed documentation is provided within the respective subpackages.

## Existing Components

### Core

`core` contains interfaces for global components (i.e., components that are registered within the `Binder`). Usually, you do not need to add new global components. Please note: in order to access these components, use the static classes provided in the top-level API-folder (`Event`, `Monitor`, `Oracle`, `Randoms`, `Time`).

### MAPE

Contains the **M**onitor, **A**nalyze, **P**lan, and **E**xecute control loop for distributed systems.

### Network

Layer 3 of the ISO-OSI-Stack - providing wrappers for message-based data transmissions.

### Overlay

Generic base interfaces for application layer overlays.

### PubSub

Publish/Subscribe overlays and protocols, providing a generic event distribution service.

### Sensor

Access to sensor information, for example location of a device or current battery state. Very much Android-inspired.

### Transition

MAKI-specific framework for the description of transition-enabled components within distributed software systems.

### Transport

Layer 4 of the ISO-OSI-stack.