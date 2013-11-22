# Midas
***On-the-fly Schema Migration Tool for MongoDB***
##Overview
  Currently, applications have to hand-roll their own schema migration infrastructure or use some third-party tool
  It is difficult to migrate TBs of data without downtime (unacceptable from SLA stand-point!).  This is where
  Midas fills the gap.

  It intercepts responses at MongoDB Protocol level and upgrades or downgrades document schema in-transit.
  As Midas works at protocol level, it is agnostic of Language specific MongoDB drivers (Ruby, Python, C#
  and Java drivers) and their versions within those languages

  Further, Midas is Agnostic of the MongoDB configurations like Standalone, Replica Sets, Sharded environments.
<br>

## Build Info
We are using Gradle 1.8 for our builds.  Please do not checkin Eclipse or Intellij or any IDE specific files.  
For Idea or Eclipse they can be generated using
* `gradlew eclipse`
* `gradlew idea`

Note:
Eclipse IDE might show the following error for groovy files:
"Error compiling Groovy project. Either the Groovy-JDT patch is not installed or JavaBuilder is not being used."
This is because groovy plugin for eclipse needs JavaBuilder but doesn't understand ScalaBuilder.

## Project Versioning
We will be following [JBoss Versioning Convention](https://community.jboss.org/wiki/JBossProjectVersioning?_sscc=t)
* `major.minor.micro.Alpha[n]`
* `major.minor.micro.Beta[n]`
* `major.minor.micro.CR[n]`
Please refer to `AppConfig.groovy` - a single place of change for all the project configuration changes

## License
**Midas is licensed under the terms of the [FreeBSD License](http://en.wikipedia.org/wiki/BSD_licenses)**

