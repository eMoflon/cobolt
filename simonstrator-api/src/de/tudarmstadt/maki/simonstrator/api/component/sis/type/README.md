# SiS and Types

As discussed during the MAKI-Seminar on 3.6.15, nobody in MAKI actually wants to define a taxonomy/ontology/... for metrics and state within MAKI-enabled systems.

For the sake of an integrated SiS and the ability to connect overlays and services to that SiS, we proposed a pragmatical approach to the taxonomy design. Although the current design is desired to be extensible and flexible enough to support all MAKI-needs, this is not mandatory for us. This document aims in providing some help w.r.t. extending and using the taxonomy in your systems.


## SiSTypes and the Graph-API
SiSType extends the IGraphElementProperty-class, enabling SiSTypes to act as annotations within a graph. This allows monitoring to connect node state to the respective node object.