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
* Expansion operations - add, copy, merge, split, transform.
* Contraction operations - remove
* Schema migration for multiple applications simultaneously
* Support multi node configuration for application
* Without shutting down Midas, you can -
  * Add or remove applications on-the-fly
  * Add or remove nodes on-the-fly
  * Add or remove deltas/changeset on-the-fly

Documentation:
* On how to write delta scripts, follow the conventions given in Midas-Overview-Guide.pdf
* Midas Command Reference (Midas-Commands.md) is available in the documentation section
  of this distribution.
* Further, documentation of scenarios in the form of user journeys are available within
  the journeys folder under documentation of this distribution.  These are all executable
  specifications.

Pre-Requisites:
* It assumes that you have JDK1.6 or 1.7 installed and you have java
in your path.

Running Midas:
* After having exploded the Midas zip -
  * For Unix machines, grant executable permissions to midas.sh script (chmod +x midas.sh)
  * For Windows, run midas.bat to start Midas.
  * By default, it runs on localhost and port 27020 and assumes mongod is also running
    on localhost and its default port 27017.  It watches the deltas directory within this
    distribution.  All these default settings can be overridden by you.  Type midas --help
    at the prompt for the settings

