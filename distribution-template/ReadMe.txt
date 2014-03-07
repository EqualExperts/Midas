ReadMe.txt
==========

Midas is an on-the-fly schema migration for MongoDB.

Currently, applications have to hand-roll their own schema migration infrastructure or use some third-party tool.
It is difficult to migrate TBs of data without downtime (unacceptable from SLA stand-point!).
This is where Midas fills the gap.

It intercepts responses at MongoDB Protocol level and upgrades or downgrades document schema in-transit.
As Midas works at protocol level, it is agnostic of Language specific MongoDB drivers (Ruby, Python, C#
and Java drivers) and their versions within those languages.

Further, Midas is agnostic of the MongoDB configurations like Standalone, Replica Sets, Sharded environments.

Features Summary:
* Schema migration for multiple applications simultaneously
* Support multi node configuration for application
* Expansion operations - add, copy, merge, split, transform.
* Contraction operations - remove
* Addition and removal of application on the fly
* Addition and removal of nodes on the fly
* Addition and removal of deltas/changeset on the fly

Pre-Requisites
* It assumes that you have JDK1.6 or 1.7 installed and you have java
in your path.

Running Midas
* After having exploded the Midas zip -
  * For Unix machine, grant executable permissions to midas.sh script.
  * In order to start Midas, run midas.sh/midas.bat backing up a MongoDB (participating in a ReplicaSet), you need to provide
    path of deltas directory, mongoHost, mongoPort, midasHost and midasPort.
  * For delta scripts follow the conventions given in Midas-Overview-Guide.pdf

